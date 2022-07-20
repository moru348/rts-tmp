package dev.moru3.temporaryserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
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
    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create()

    val sdf = SimpleDateFormat("MM-dd HH:mm")

    val yearSdf = SimpleDateFormat("yyyy-MM-dd HH:mm")

    // 開催予定のイベントの一覧
    var events: List<Event> = mutableListOf<Event>()

    /**
     * 誰かがこのプロキシーにpingを飛ばしたときに発火するイベント。
     */
    @Subscribe
    fun onLogin(event: ProxyPingEvent) {
        // MOTDを生成
        val nextEvent = events.firstOrNull()?:Event(Date(Long.MAX_VALUE),Date(Long.MAX_VALUE),"Yamaで遊ぼう")
        val motd = nextEvent.let { event -> if(Date().after(event.start)) { "§c§l§o§nRedTownServer§e //§r §nイベントは終了しました。！！§r\n§c${event.name}は終了しました。ご参加ありがとうございました。" } else { "§c§l§o§nRedTownServer§e //§r §n定期イベント開催予定！！§r\n§c${SimpleDateFormat("MM/dd HH:mm").format(event.start)}§fから§4§l${event.name}§fイベント開催予定！ぜひ参加してね！！！" } }
        event.ping = event.ping.asBuilder()
            .maximumPlayers(Int.MAX_VALUE)
            .onlinePlayers(Int.MIN_VALUE)
            .description(Component.text(motd))
            .version(ServerPing.Version(1,"開催まであと${(nextEvent.start.time-Date().time) / ( 1000 * 60 * 60 * 24 )}日！")).build()
    }

    @Subscribe
    fun onPreLogin(event: PreLoginEvent) {
        val nextEvent = events.firstOrNull()?:Event(Date(Long.MAX_VALUE),Date(Long.MAX_VALUE),"Yamaで遊ぼう")
        val description = """
            §c§l§n§o- RedTownServer -§f
            RedTownServerでは定期的にイベントを開催しています。
            ぜひご参加ください！！！
            §e--- 次のイベント ---
            §c開始時間: ${sdf.format(nextEvent.start)} から！
            開催イベント: ${nextEvent.name}
            参加可否: ご自由に参加できます。
            ライブURL: 開始までお待ちください。
            
            §7--- イベントの予定 ---
            ${events.joinToString("\n") { "${it.name}: ${yearSdf.format(nextEvent.start)}" }}
        """.trimIndent()
        event.result = PreLoginEvent.PreLoginComponentResult.denied(Component.text(description))
    }

    @Subscribe
    fun onInit(event: ProxyInitializeEvent) {
        // タイマーインスタンスを作成
        val timer = Timer()
        // 定期実行(1日おき)
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                // サーバーからイベントの予定一覧を取得
                println("start send request")
                val response = okHttpClient.newCall(Request.Builder().url("https://moru348.github.io/rts-tmp/events.json").get().build()).execute()
                events = gson.fromJson(response.body?.string(),JsonArray::class.java)
                    .map { gson.fromJson(it, Event::class.java) }
                    .sortedBy { it.start }
                    .filter { Date().before(it.end) }
            }
        }, 0, 1000*60*60*24)
    }

    /**
     * @param start イベントの開始時間
     * @param end イベントの終了時間
     * @param name イベント名
     */
    class Event(val start: Date, val end: Date, val name: String)
}