package com.example.tfg_aaron.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NotificationScheduler {

    private const val PARTIDO_OFFSET = 1000
    private const val SESION_OFFSET = 2000
    private const val REMINDER_MS = 60 * 60 * 1000L // 1 hour

    fun schedulePartidoReminder(context: Context, partidoId: Int, rival: String, fechaMillis: Long) {
        val reminderTime = fechaMillis - REMINDER_MS
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, PartidoReminderReceiver::class.java).apply {
            putExtra("tipo", "PARTIDO")
            putExtra("titulo", "Partido en 1 hora")
            putExtra("subtitulo", "vs $rival")
            putExtra("notifId", PARTIDO_OFFSET + partidoId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PARTIDO_OFFSET + partidoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(context, reminderTime, pendingIntent)
    }

    fun scheduleSesionReminder(context: Context, sesionId: Int, titulo: String, fechaMillis: Long) {
        val reminderTime = fechaMillis - REMINDER_MS
        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, PartidoReminderReceiver::class.java).apply {
            putExtra("tipo", "SESION")
            putExtra("titulo", "Entrenamiento en 1 hora")
            putExtra("subtitulo", titulo)
            putExtra("notifId", SESION_OFFSET + sesionId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SESION_OFFSET + sesionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(context, reminderTime, pendingIntent)
    }

    fun cancelPartidoReminder(context: Context, partidoId: Int) {
        cancelPending(context, PARTIDO_OFFSET + partidoId)
    }

    fun cancelSesionReminder(context: Context, sesionId: Int) {
        cancelPending(context, SESION_OFFSET + sesionId)
    }

    private fun scheduleExact(context: Context, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelPending(context: Context, requestCode: Int) {
        val intent = Intent(context, PartidoReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}
