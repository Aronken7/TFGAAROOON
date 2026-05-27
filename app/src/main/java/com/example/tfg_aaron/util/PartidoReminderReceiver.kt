package com.example.tfg_aaron.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PartidoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tipo = intent.getStringExtra("tipo") ?: "PARTIDO"
        val titulo = intent.getStringExtra("titulo") ?: ""
        val subtitulo = intent.getStringExtra("subtitulo") ?: ""
        val notifId = intent.getIntExtra("notifId", 0)

        NotificationHelper.createChannel(context)
        NotificationHelper.mostrarReminderNotification(context, notifId, tipo, titulo, subtitulo)
    }
}
