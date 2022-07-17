package dev.moru3.temporaryserver

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.server.ServerPing
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date
import java.util.Timer
import java.util.TimerTask

@Plugin(id="temporaryserver",name="Temporary Server", version="1.0")
class TemporaryServer {

    val okHttpClient = OkHttpClient()
    val gson = GsonBuilder().setDateFormat("yyyy/MM/DD HH:mm").create()
    var description = "§c§l§o§nRedTownServer§e //§r §nイベント定期開催中！！§r\n§c--/-- --:--§fから§4§l--§fイベント！参加してね！！！"

    val events = mutableListOf<Event>()

    @Subscribe
    fun onLogin(event: ProxyPingEvent) {
        event.ping.asBuilder().maximumPlayers(Int.MAX_VALUE).onlinePlayers(Int.MIN_VALUE).description(description)
    }

    class Event(val start: Date, val end: Date, val name: String)

    init {
        val timer = Timer()
        timer.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                Request.Builder().url("")
                gson.fromJson()
            }
        }, 0, 1000*60*10)
    }
}