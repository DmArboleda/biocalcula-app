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
import com.arboleda.biocalcula.ui.activities.RegistroConsumoActivity
import com.arboleda.biocalcula.util.NutricionHelper
import com.arboleda.biocalcula.util.NotifPreferences
import com.arboleda.biocalcula.util.SessionManager
import java.util.concurrent.TimeUnit

/**
 * Worker de WorkManager que envía el recordatorio nocturno diario de rutina.
 *
 * Se ejecuta cada día (intervalo de 24h con PeriodicWorkRequest).
 * Consulta los macros calculados del usuario desde Room y muestra una notificación
 * rica con el desglose de metas del día: proteínas, carbos, grasas y vasos de agua.
 *
 * Al tocar la notificación abre RegistroConsumoActivity donde el usuario puede
 * ingresar cuánto consumió realmente de cada macro.
 *
 * Programación:
 *   WorkerScheduler.programarRutina(context) — activa (desde PerfilActivity)
 *   WorkerScheduler.cancelarRutina(context)  — desactiva
 */
class RecordatorioRutinaWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val UNIQUE_NAME = "recordatorio_rutina_diaria"
        private const val NOTIF_ID = 3001
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val session = SessionManager(applicationContext)
        val notifPrefs = NotifPreferences(applicationContext)

        // Verificar que el recordatorio sigue activo en preferencias
        if (!notifPrefs.rutinaActiva) return Result.success()

        val userId = session.obtenerUserId()
        if (userId == -1) return Result.success() // Sin sesión activa

        val usuario = db.usuarioDao().buscarPorId(userId) ?: return Result.success()

        // Verificar que el usuario tiene datos biométricos para calcular macros
        if (usuario.peso == null || usuario.talla == null || usuario.edad == null
            || usuario.sexo == null || usuario.objetivo == null) {
            return Result.success()
        }

        // Calcular plan nutricional personalizado
        val plan = NutricionHelper.calcularPlan(
            peso = usuario.peso,
            talla = usuario.talla,
            edad = usuario.edad,
            sexo = usuario.sexo,
            objetivo = usuario.objetivo
        )

        val vasosAgua = (plan.aguaMl / 250f).let { kotlin.math.ceil(it.toDouble()).toInt() }
        val hora = notifPrefs.horaFormateada()

        val titulo = "🌙 ¿Completaste tu rutina de hoy?"
        val textoCorto = "Proteínas ${plan.proteinaG}g · Carbos ${plan.carbosG}g · Grasas ${plan.grasasG}g · Agua $vasosAgua vasos"
        val textoLargo = """
            ¡Es hora de revisar tu día! Registra lo que consumiste:
            
            🥩 Proteínas: ${plan.proteinaG}g
            🌾 Carbohidratos: ${plan.carbosG}g
            🥑 Grasas: ${plan.grasasG}g
            💧 Agua: ${plan.aguaL}L = $vasosAgua vasos de 250ml
            
            Toca para ingresar tu consumo real y mejorar tu análisis semanal.
        """.trimIndent()

        enviarNotificacion(titulo, textoCorto, textoLargo, plan.proteinaG, plan.carbosG, plan.grasasG, vasosAgua, plan.aguaMl)

        // MODO DEMO: Re-programar el worker para que se ejecute de nuevo en 45 segundos
        val proximaEjecucion = OneTimeWorkRequestBuilder<RecordatorioRutinaWorker>()
            .setInitialDelay(45, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            proximaEjecucion
        )

        return Result.success()
    }

    private fun enviarNotificacion(
        titulo: String,
        textoCorto: String,
        textoLargo: String,
        proteinaG: Int,
        carbosG: Int,
        grasasG: Int,
        vasosAgua: Int,
        aguaMl: Int
    ) {
        // Intent principal → abre RegistroConsumoActivity con los datos de metas
        val intent = Intent(applicationContext, RegistroConsumoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(RegistroConsumoActivity.EXTRA_PROTEINA_META, proteinaG)
            putExtra(RegistroConsumoActivity.EXTRA_CARBOS_META, carbosG)
            putExtra(RegistroConsumoActivity.EXTRA_GRASAS_META, grasasG)
            putExtra(RegistroConsumoActivity.EXTRA_VASOS_META, vasosAgua)
            putExtra(RegistroConsumoActivity.EXTRA_AGUA_ML_META, aguaMl)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, NOTIF_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(applicationContext, BioCalculaApp.CHANNEL_RUTINA)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(titulo)
            .setContentText(textoCorto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_input_add,
                "Registrar consumo",
                pendingIntent
            )
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
