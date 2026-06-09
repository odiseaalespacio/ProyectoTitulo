package com.example.cloty_apoderado.notification

// esta parte es nueva

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cloty_apoderado.data.ClotyRepository

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "ClotyNotificationWorker"
        private const val PREFS_NAME = "cloty_notif_prefs"
        private const val KEY_LAST_ID = "last_notified_id"
    }

    override suspend fun doWork(): Result {
        return try {
            val repo = ClotyRepository(applicationContext)

            val me = repo.me()
            val idApoderado = me.idApoderado ?: return Result.success()

            val notificaciones = repo.notificaciones(idApoderado)

            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastNotifiedId = prefs.getInt(KEY_LAST_ID, 0)

            val nuevas = notificaciones.filter { n ->
                val id = n.idNotificacion ?: 0
                id > lastNotifiedId && n.leida != true
            }

            if (nuevas.isNotEmpty()) {
                NotificationHelper.createChannel(applicationContext)

                for (n in nuevas) {
                    NotificationHelper.show(
                        applicationContext,
                        n.idNotificacion ?: 0,
                        n.titulo ?: "Cloty",
                        n.mensaje ?: "Tienes una nueva notificación"
                    )
                }

                val maxId = nuevas.maxOf { it.idNotificacion ?: 0 }
                prefs.edit().putInt(KEY_LAST_ID, maxId).apply()
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error polling notifications", e)
            Result.retry()
        }
    }
}
