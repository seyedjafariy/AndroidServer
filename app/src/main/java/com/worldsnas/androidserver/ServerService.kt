package com.worldsnas.androidserver

import android.R
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ryanharter.ktor.moshi.moshi
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit
import kotlin.random.Random


private const val CHANNEL_SERVER = "ServerChannel"
private const val REQUEST_ID_CLOSE_SERVER = 1000
private const val ACTION_SHUT_DOWN_SERVER = "ACTION_SHUT_DOWN_SERVER"
class ServerService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    lateinit var server: ApplicationEngine
    private val notifID = Random.nextInt()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createNotification()

        server = embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                gson {}
            }
            routing {
                get("/") {
                    call.respond(processName(
                        call.request.queryParameters["name"] ?: "Arash"
                    ))
                }
            }
        }
        server.start(wait = false)

    }

    private fun processName(name : String = "Arash") : Map<String, String> =
        mapOf("message" to "Hellow $name")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_SHUT_DOWN_SERVER) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop(0, 0, TimeUnit.SECONDS)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVER,
                "Server Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(){
        val notificationIntent = Intent(this, ServerService::class.java).apply {
            action = ACTION_SHUT_DOWN_SERVER
        }
        val pendingIntent = PendingIntent.getService(
            this,
            REQUEST_ID_CLOSE_SERVER,
            notificationIntent,
            0
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_SERVER)
            .setContentTitle("Server Running")
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .addAction(R.drawable.ic_menu_close_clear_cancel, "shut down", pendingIntent)
            .build()
        startForeground(notifID, notification)
    }
}