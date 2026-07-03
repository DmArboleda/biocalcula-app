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
import com.arboleda.biocalcula.ui.activities.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Worker de WorkManager que actúa como un recordatorio dinámico de uso.
 * Calcula los días restantes para el pesaje obligatorio semanal basándose en Room.
 */
class RecordatorioPesoWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val ultimoRegistro = database.registroPesoDao().getUltimoRegistro()

        val n: Int = if (ultimoRegistro == null) {
            0 // Si no hay registros, pedimos el peso inmediatamente
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val fechaUltimo = sdf.parse(ultimoRegistro.fecha_registro)
                val fechaHoy = Date()
                
                if (fechaUltimo != null) {
                    val diffInMillis = fechaHoy.time - fechaUltimo.time
                    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
                    
                    // Queremos un recordatorio cada 7 días.
                    // Si pasaron 3 días, faltan 4.
                    // Si pasaron 7 o más días, faltan 0 (es el momento).
                    val faltan = 7 - diffInDays
                    if (faltan < 0) 0 else faltan
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        }

        enviarNotificacion(n)

        // MODO DEMO: Re-programar el worker para que se ejecute de nuevo en 1 minuto
        val proximaEjecucion = OneTimeWorkRequestBuilder<RecordatorioPesoWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "RecordatorioPesajeDemo",
            ExistingWorkPolicy.REPLACE,
            proximaEjecucion
        )

        return Result.success()
    }

    private fun enviarNotificacion(diasRestantes: Int) {
        // Intent para abrir MainActivity al tocar la notificación
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val titulo = "¡Momento de tu control biométrico! ⚖️"
        val contenido = "Faltan $diasRestantes días para registrar tu peso de hoy en BioCalcula."
        val textoLargo = "Faltan $diasRestantes días para registrar tu peso de hoy en BioCalcula. Mantener tu historial actualizado te ayuda a visualizar tu progreso real. ¡Un dato a la semana hace la diferencia!"

        // Construir la notificación con Material Design
        val notificacion = NotificationCompat.Builder(applicationContext, BioCalculaApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Mostrar solo si tenemos permiso (obligatorio desde Android 13)
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(1001, notificacion)
            }
        }
    }
}
