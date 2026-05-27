package com.example.tfg_aaron.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.tfg_aaron.MainActivity
import com.example.tfg_aaron.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PARTIDO_ID = "partido_id"
        const val EXTRA_RIVAL = "rival"
        const val EXTRA_FECHA = "fecha"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val partidoId = intent.getIntExtra(EXTRA_PARTIDO_ID, -1)
        val rival = intent.getStringExtra(EXTRA_RIVAL) ?: "Rival"
        val fecha = intent.getLongExtra(EXTRA_FECHA, System.currentTimeMillis())

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = sdf.format(Date(fecha))

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            partidoId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationHelper.createChannel(context)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Partido en 2 horas")
            .setContentText("vs $rival a las $hora")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Tienes un partido contra $rival a las $hora. ¡Prepara al equipo!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(partidoId, notification)
    }
}
