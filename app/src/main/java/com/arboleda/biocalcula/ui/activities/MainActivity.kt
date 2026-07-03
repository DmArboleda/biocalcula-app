package com.arboleda.biocalcula.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.ui.workers.RecordatorioPesoWorker
import java.util.concurrent.TimeUnit

/**
 * Pantalla principal post-login.
 *
 * Responsabilidades:
 * 1. Solicitar permiso POST_NOTIFICATIONS en Android 13+ (API 33).
 *    Sin este permiso el sistema descarta silenciosamente las notificaciones.
 * 2. Programar el recordatorio semanal de pesaje con WorkManager.
 * 3. Navegar al Historial de Peso.
 */
class MainActivity : AppCompatActivity() {

    // Lanzador de contrato para solicitar el permiso de notificaciones
    private val pedirPermisoNotificacion = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            programarRecordatorioDiario()
        } else {
            Toast.makeText(
                this,
                "Las alertas están desactivadas. No recibirás recordatorios de pesaje.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ── Permiso y programación de notificaciones ──────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: verificar si ya tenemos el permiso antes de pedirlo
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                programarRecordatorioDiario()
            } else {
                pedirPermisoNotificacion.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 y anteriores: no se requiere permiso explícito
            programarRecordatorioDiario()
        }

        // ── Navegación al Historial ───────────────────────────────────────────
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
    }

    /**
     * MODO DEMO: Programa un Worker que se ejecuta en 1 minuto.
     * Al terminar, el propio Worker se volverá a programar.
     */
    private fun programarRecordatorioDiario() {
        val solicitud = OneTimeWorkRequestBuilder<RecordatorioPesoWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "RecordatorioPesajeDemo",
            ExistingWorkPolicy.REPLACE,
            solicitud
        )
    }
}
