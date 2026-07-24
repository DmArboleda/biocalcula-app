package com.arboleda.biocalcula.ui.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.arboleda.biocalcula.BioCalculaApp
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.ui.activities.PerfilActivity
import com.arboleda.biocalcula.util.NotifPreferences
import java.util.concurrent.TimeUnit

/**
 * Worker de WorkManager que envía el recordatorio semanal de pesaje.
 *
 * - Se programa como PeriodicWorkRequest con el intervalo configurado en NotifPreferences.
 * - Consulta el último peso registrado en Room para personalizar el mensaje.
 * - Al tocar la notificación abre PerfilActivity para registrar el nuevo peso.
 *
 * Programación:
 *   WorkerScheduler.programarPeso(context) — activa/reactiva
 *   WorkerScheduler.cancelarPeso(context)  — desactiva
 */
class RecordatorioPesoWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val UNIQUE_NAME = "recordatorio_peso_semanal"
        private const val NOTIF_ID = 2001
    }

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val ultimoRegistro = database.registroPesoDao().getUltimoRegistro()
        val notifPrefs = NotifPreferences(applicationContext)

        val mensajePeso = if (ultimoRegistro != null) {
            "Tu último registro fue %.1f kg. ¿Cuánto pesas hoy?".format(ultimoRegistro.peso_registrado)
        } else {
            "Registra tu peso para ajustar tus macros personalizados."
        }

        val intervalo = notifPrefs.pesoIntervalosDias
        val titulo = "⚖️ Momento de registrar tu peso"
        val textoCorto = mensajePeso
        val textoLargo = "$mensajePeso\n\nMantener tu historial actualizado permite que BioCalcula ajuste tus macros con precisión. ¡Un dato a la semana hace la diferencia!"

        enviarNotificacion(titulo, textoCorto, textoLargo)

        // MODO DEMO: Re-programar el worker para que se ejecute de nuevo en 45 segundos
        val proximaEjecucion = OneTimeWorkRequestBuilder<RecordatorioPesoWorker>()
            .setInitialDelay(45, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            proximaEjecucion
        )

        return Result.success()
    }

    private fun enviarNotificacion(titulo: String, texto: String, textoLargo: String) {
        val intent = Intent(applicationContext, PerfilActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ABRIR_DIALOGO_PESO", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, NOTIF_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(applicationContext, BioCalculaApp.CHANNEL_PESO)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIF_ID, notificacion)
            }
        }
    }
}

/**
 * Utilidad centralizada para programar/cancelar los workers de notificación.
 * Usar este objeto desde PerfilActivity para mantener la lógica en un solo lugar.
 */
object WorkerScheduler {

    private const val PESO_UNIQUE = RecordatorioPesoWorker.UNIQUE_NAME
    private const val RUTINA_UNIQUE = RecordatorioRutinaWorker.UNIQUE_NAME

    /** Programa (o reprograma) el recordatorio de pesaje. */
    fun programarPeso(context: Context) {
        // MODO DEMO: En lugar de usar días, usamos 45 segundos para el video
        val request = OneTimeWorkRequestBuilder<RecordatorioPesoWorker>()
            .setInitialDelay(45, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            PESO_UNIQUE,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Cancela el recordatorio de pesaje. */
    fun cancelarPeso(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PESO_UNIQUE)
    }

    /** Programa (o reprograma) el recordatorio diario de rutina. */
    fun programarRutina(context: Context) {
        // MODO DEMO: En lugar de usar 24h, usamos 45 segundos para el video
        val request = OneTimeWorkRequestBuilder<RecordatorioRutinaWorker>()
            .setInitialDelay(45, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            RUTINA_UNIQUE,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Cancela el recordatorio diario de rutina. */
    fun cancelarRutina(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(RUTINA_UNIQUE)
    }
}
