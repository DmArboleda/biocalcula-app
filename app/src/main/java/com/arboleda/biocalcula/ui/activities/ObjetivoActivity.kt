package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.util.NutricionHelper
import com.arboleda.biocalcula.util.SessionManager
import com.arboleda.biocalcula.viewmodel.UsuarioViewModel

/**
 * Pantalla 2 — Selección de Objetivo Nutricional.
 *
 * Muestra 3 tarjetas: Perder Grasa / Mantener Peso / Ganar Músculo.
 * Al seleccionar una tarjeta se resalta y aparece el botón "Ver mis macros →".
 * Guarda el objetivo en Room y navega al Dashboard.
 */
class ObjetivoActivity : AppCompatActivity() {

    private val viewModel: UsuarioViewModel by viewModels()
    private lateinit var session: SessionManager

    private lateinit var cardPerderGrasa: MaterialCardView
    private lateinit var cardMantener: MaterialCardView
    private lateinit var cardGanarMusculo: MaterialCardView
    private lateinit var btnVerMacros: MaterialButton

    private var objetivoSeleccionado: String? = null

    // Colores de borde para tarjetas seleccionadas/deseleccionadas
    private val colorOutline    get() = getColor(R.color.color_outline)
    private val colorPerder     get() = getColor(R.color.color_perder_grasa)
    private val colorMantener   get() = getColor(R.color.color_mantener)
    private val colorGanar      get() = getColor(R.color.color_ganar_musculo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetivo)

        session = SessionManager(this)

        cardPerderGrasa  = findViewById(R.id.cardPerderGrasa)
        cardMantener     = findViewById(R.id.cardMantener)
        cardGanarMusculo = findViewById(R.id.cardGanarMusculo)
        btnVerMacros     = findViewById(R.id.btnVerMacros)

        btnVerMacros.visibility = View.GONE

        // ── Listeners de selección ─────────────────────────────────────────────
        cardPerderGrasa.setOnClickListener {
            seleccionarObjetivo(NutricionHelper.PERDER_GRASA)
        }
        cardMantener.setOnClickListener {
            seleccionarObjetivo(NutricionHelper.MANTENER)
        }
        cardGanarMusculo.setOnClickListener {
            seleccionarObjetivo(NutricionHelper.GANAR_MUSCULO)
        }

        // ── Botón ver macros ───────────────────────────────────────────────────
        btnVerMacros.setOnClickListener {
            val objetivo = objetivoSeleccionado
            if (objetivo == null) {
                Toast.makeText(this, "Selecciona un objetivo primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = session.obtenerUserId()
            viewModel.actualizarObjetivo(userId, objetivo) {
                session.marcarOnboardingCompleto()
                irAlDashboard()
            }
        }
    }

    /**
     * Aplica el estado visual de selección a la tarjeta elegida
     * y deselecciona las demás.
     */
    private fun seleccionarObjetivo(objetivo: String) {
        objetivoSeleccionado = objetivo

        // Resetear todas las tarjetas
        resetCard(cardPerderGrasa)
        resetCard(cardMantener)
        resetCard(cardGanarMusculo)

        // Resaltar la seleccionada
        when (objetivo) {
            NutricionHelper.PERDER_GRASA  -> resaltarCard(cardPerderGrasa,  colorPerder)
            NutricionHelper.MANTENER      -> resaltarCard(cardMantener,      colorMantener)
            NutricionHelper.GANAR_MUSCULO -> resaltarCard(cardGanarMusculo,  colorGanar)
        }

        // Mostrar botón de acción
        btnVerMacros.visibility = View.VISIBLE
    }

    private fun resaltarCard(card: MaterialCardView, strokeColor: Int) {
        card.strokeColor = strokeColor
        card.strokeWidth = 6
        card.cardElevation = 12f
    }

    private fun resetCard(card: MaterialCardView) {
        card.strokeColor = colorOutline
        card.strokeWidth = 3
        card.cardElevation = 4f
    }

    private fun irAlDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
