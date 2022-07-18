package dev.moru3.temporaryserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask

@Plugin(id="temporaryserver",name="Temporary Server", version="1.0")
class TemporaryServer {

    // OKHttpのクライアントを作成
    val okHttpClient = OkHttpClient()
    // Gsonのクライアントを作成
    val gson = GsonBuilder().setDateFormat("yyyy/MM/DD HH:mm").create()

    // 開催予定のイベントの一覧
    var events: List<Event> = mutableListOf<Event>()

    /**
     * 誰かがこのプロキシーにpingを飛ばしたときに発火するイベント。
     */
    @Subscribe
    fun onLogin(event: ProxyPingEvent) {
        // MOTDを生成
        val motd = events
            .firstNotNullOf { Event(Date(Long.MAX_VALUE),Date(Long.MAX_VALUE),"Yamaで遊ぼう") }
            .let { event -> if(Date().before(event.start)) { "§c§l§o§nRedTownServer§e //§r §nイベントは終了しました。！！§r\n§c${event.name}は終了しました。ご参加ありがとうございました。" } else { "§c§l§o§nRedTownServer§e //§r §n定期イベント開催予定！！§r\n§c${SimpleDateFormat("MM/DD HH:mm").format(event.start)}§fから§4§l${event.name}§fイベント開催予定！ぜひ参加してね！！！" } }
        event.ping = event.ping.asBuilder()
            .maximumPlayers(Int.MAX_VALUE)
            .onlinePlayers(Int.MIN_VALUE)
            .description(Component.text(motd)).build()
    }

    /**
     * @param start イベントの開始時間
     * @param end イベントの終了時間
     * @param name イベント名
     */
    class Event(val start: Date, val end: Date, val name: String)

    init {
        // タイマーインスタンスを作成
        val timer = Timer()
        // 定期実行(1日おき)
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                // サーバーからイベントの予定一覧を取得
                val response = okHttpClient.newCall(Request.Builder().url("https://moru348.github.io/rts-tmp/events.json").get().build()).execute()
                // イベントをインスタンスに変換して開催時間でソートしてから開催済のイベントを削除
                events = gson.fromJson(response.body?.string(),JsonArray::class.java)
                    .map { gson.fromJson(it,Event::class.java) }
                    .sortedBy { it.start }
                    .filter { Date().after(it.end) }
            }
        }, 0, 1000*60*60*24)
    }
}

fun main() {
    // OKHttpのクライアントを作成
    val okHttpClient = OkHttpClient()
    // Gsonのクライアントを作成
    val gson = GsonBuilder().setDateFormat("yyyy/MM/DD HH:mm").create()
    val response = okHttpClient.newCall(Request.Builder().url("https://moru348.github.io/rts-tmp/events.json").get().build()).execute()
    val event = gson.fromJson(response.body?.string(),JsonArray::class.java)
        .map { gson.fromJson(it, TemporaryServer.Event::class.java) }
        .sortedBy { it.start }
        .filter { Date().after(it.end) }[0]
    println((event.start.time-Date().time) / ( 1000 * 60 * 60 * 24 ))
    println(event.start)
    println(SimpleDateFormat("yyyy/MM/DD HH:mm").format(Date()))
}