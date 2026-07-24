package com.arboleda.biocalcula.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.data.db.AppDatabase
import com.arboleda.biocalcula.data.model.CumplimientoDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity para registrar el consumo real del día.
 *
 * Se abre al tocar la notificación nocturna de rutina.
 * Recibe los extras con las metas del usuario (calculadas en RecordatorioRutinaWorker)
 * y permite ingresar cuánto consumió realmente de cada macro y agua.
 *
 * Agua: slider de vasos (0-16) o campo en litros (toggle)
 * Macros: checkbox "Cumplí" o campo de gramos reales si no cumplió
 *
 * Al guardar hace un UPSERT en cumplimiento_diario (elimina el de hoy y reinserta).
 */
class RegistroConsumoActivity : AppCompatActivity() {

    companion object {
        // Extras enviados desde RecordatorioRutinaWorker
        const val EXTRA_PROTEINA_META   = "extra_proteina_meta"   // Int (gramos)
        const val EXTRA_CARBOS_META     = "extra_carbos_meta"     // Int (gramos)
        const val EXTRA_GRASAS_META     = "extra_grasas_meta"     // Int (gramos)
        const val EXTRA_VASOS_META      = "extra_vasos_meta"      // Int (vasos de 250ml)
        const val EXTRA_AGUA_ML_META    = "extra_agua_ml_meta"    // Int (ml totales)

        private const val ML_POR_VASO = 250
    }

    // ── Metas recibidas desde la notificación ─────────────────────────────────
    private var metaProteinaG  = 0
    private var metaCarbosG    = 0
    private var metaGrasasG    = 0
    private var metaVasos      = 0
    private var metaAguaMl     = 0

    // ── Vistas ────────────────────────────────────────────────────────────────
    // Agua
    private lateinit var tvAguaMeta         : TextView
    private lateinit var toggleAguaUnidad   : MaterialButtonToggleGroup
    private lateinit var btnToggleVasos     : MaterialButton
    private lateinit var btnToggleLitros    : MaterialButton
    private lateinit var layoutVasosIconos  : LinearLayout
    private lateinit var sliderVasos        : Slider
    private lateinit var layoutLitros       : TextInputLayout
    private lateinit var etLitros           : TextInputEditText
    private lateinit var tvAguaSeleccionada : TextView

    // Proteínas
    private lateinit var tvProteinaMeta     : TextView
    private lateinit var cbProteinaCumplida : MaterialCheckBox
    private lateinit var layoutProteinaGramos : TextInputLayout
    private lateinit var etProteinaGramos   : TextInputEditText

    // Carbos
    private lateinit var tvCarbosMeta       : TextView
    private lateinit var cbCarbosCumplidos  : MaterialCheckBox
    private lateinit var layoutCarbosGramos : TextInputLayout
    private lateinit var etCarbosGramos     : TextInputEditText

    // Grasas
    private lateinit var tvGrasasMeta       : TextView
    private lateinit var cbGrasasCumplidas  : MaterialCheckBox
    private lateinit var layoutGrasasGramos : TextInputLayout
    private lateinit var etGrasasGramos     : TextInputEditText

    // Botón guardar
    private lateinit var btnGuardarConsumo  : MaterialButton

    // ── Estado ────────────────────────────────────────────────────────────────
    private var usandoLitros = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_consumo)

        // Leer metas desde extras (enviados por el worker)
        metaProteinaG  = intent.getIntExtra(EXTRA_PROTEINA_META,  0)
        metaCarbosG    = intent.getIntExtra(EXTRA_CARBOS_META,    0)
        metaGrasasG    = intent.getIntExtra(EXTRA_GRASAS_META,    0)
        metaVasos      = intent.getIntExtra(EXTRA_VASOS_META,     8)
        metaAguaMl     = intent.getIntExtra(EXTRA_AGUA_ML_META,   2000)

        inicializarVistas()
        configurarMetas()
        configurarAgua()
        configurarMacros()
        configurarBotonGuardar()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inicialización de vistas
    // ─────────────────────────────────────────────────────────────────────────

    private fun inicializarVistas() {
        // Agua
        tvAguaMeta          = findViewById(R.id.tvAguaMeta)
        toggleAguaUnidad    = findViewById(R.id.toggleAguaUnidad)
        btnToggleVasos      = findViewById(R.id.btnToggleVasos)
        btnToggleLitros     = findViewById(R.id.btnToggleLitros)
        layoutVasosIconos   = findViewById(R.id.layoutVasosIconos)
        sliderVasos         = findViewById(R.id.sliderVasos)
        layoutLitros        = findViewById(R.id.layoutLitros)
        etLitros            = findViewById(R.id.etLitros)
        tvAguaSeleccionada  = findViewById(R.id.tvAguaSeleccionada)

        // Proteínas
        tvProteinaMeta          = findViewById(R.id.tvProteinaMeta)
        cbProteinaCumplida      = findViewById(R.id.cbProteinaCumplida)
        layoutProteinaGramos    = findViewById(R.id.layoutProteinaGramos)
        etProteinaGramos        = findViewById(R.id.etProteinaGramos)

        // Carbos
        tvCarbosMeta            = findViewById(R.id.tvCarbosMeta)
        cbCarbosCumplidos       = findViewById(R.id.cbCarbosCumplidos)
        layoutCarbosGramos      = findViewById(R.id.layoutCarbosGramos)
        etCarbosGramos          = findViewById(R.id.etCarbosGramos)

        // Grasas
        tvGrasasMeta            = findViewById(R.id.tvGrasasMeta)
        cbGrasasCumplidas       = findViewById(R.id.cbGrasasCumplidas)
        layoutGrasasGramos      = findViewById(R.id.layoutGrasasGramos)
        etGrasasGramos          = findViewById(R.id.etGrasasGramos)

        // Guardar
        btnGuardarConsumo = findViewById(R.id.btnGuardarConsumo)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configurar las metas del día en las vistas
    // ─────────────────────────────────────────────────────────────────────────

    private fun configurarMetas() {
        val aguaL = "%.1f".format(metaAguaMl / 1000f)
        tvAguaMeta.text    = "Meta: ${aguaL}L = $metaVasos vasos de 250ml"
        tvProteinaMeta.text = "Meta: ${metaProteinaG}g"
        tvCarbosMeta.text   = "Meta: ${metaCarbosG}g"
        tvGrasasMeta.text   = "Meta: ${metaGrasasG}g"

        // Ajustar el máximo del slider según la meta (máx = meta+4 vasos, mínimo 16)
        sliderVasos.valueTo = maxOf(16f, (metaVasos + 4).toFloat())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configurar sección de agua
    // ─────────────────────────────────────────────────────────────────────────

    private fun configurarAgua() {
        // Seleccionar "Vasos" por defecto
        toggleAguaUnidad.check(R.id.btnToggleVasos)
        actualizarIconosVasos(0)

        // Toggle L / Vasos
        toggleAguaUnidad.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            usandoLitros = (checkedId == R.id.btnToggleLitros)
            if (usandoLitros) {
                // Mostrar campo litros, ocultar slider + iconos
                sliderVasos.visibility      = View.GONE
                layoutVasosIconos.visibility = View.GONE
                layoutLitros.visibility     = View.VISIBLE
            } else {
                // Mostrar slider + iconos, ocultar campo litros
                sliderVasos.visibility      = View.VISIBLE
                layoutVasosIconos.visibility = View.VISIBLE
                layoutLitros.visibility     = View.GONE
            }
        }

        // Slider de vasos
        sliderVasos.addOnChangeListener { _, value, _ ->
            val vasos = value.toInt()
            val ml = vasos * ML_POR_VASO
            tvAguaSeleccionada.text = "$vasos vasos ($ml ml)"
            actualizarIconosVasos(vasos)
        }

        // Campo litros: actualizar texto resumen al cambiar
        etLitros.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val litros = etLitros.text.toString().toFloatOrNull() ?: 0f
                val vasos = (litros * 1000 / ML_POR_VASO).toInt()
                val ml = (litros * 1000).toInt()
                tvAguaSeleccionada.text = "$vasos vasos ($ml ml)"
            }
        }
    }

    /**
     * Dibuja dinámicamente íconos de vasos (💧 = lleno, 🔵 = vacío)
     * en el layoutVasosIconos según cuántos vasos seleccionó el usuario.
     * Muestra como máximo 12 iconos para no romper la UI.
     */
    private fun actualizarIconosVasos(vasosSeleccionados: Int) {
        layoutVasosIconos.removeAllViews()
        val totalMostrar = minOf(metaVasos, 12)
        for (i in 1..totalMostrar) {
            val tv = TextView(this)
            tv.text = if (i <= vasosSeleccionados) "💧" else "○"
            tv.textSize = 22f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(4, 0, 4, 0)
            tv.layoutParams = params
            layoutVasosIconos.addView(tv)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configurar sección de macros (checkbox "Cumplí" / campo manual)
    // ─────────────────────────────────────────────────────────────────────────

    private fun configurarMacros() {
        // Por defecto: campo de gramos visible (no cumplido por defecto)
        // Al marcar "Cumplí" → ocultar campo de gramos
        cbProteinaCumplida.setOnCheckedChangeListener { _, checked ->
            layoutProteinaGramos.visibility = if (checked) View.GONE else View.VISIBLE
            if (checked) etProteinaGramos.text?.clear()
        }
        cbCarbosCumplidos.setOnCheckedChangeListener { _, checked ->
            layoutCarbosGramos.visibility = if (checked) View.GONE else View.VISIBLE
            if (checked) etCarbosGramos.text?.clear()
        }
        cbGrasasCumplidas.setOnCheckedChangeListener { _, checked ->
            layoutGrasasGramos.visibility = if (checked) View.GONE else View.VISIBLE
            if (checked) etGrasasGramos.text?.clear()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Guardar consumo del día en Room
    // ─────────────────────────────────────────────────────────────────────────

    private fun configurarBotonGuardar() {
        btnGuardarConsumo.setOnClickListener {
            guardarConsumo()
        }
    }

    private fun guardarConsumo() {
        // ── Calcular vasos de agua ─────────────────────────────────────────
        val vasosConsumidos: Int = if (usandoLitros) {
            val litros = etLitros.text.toString().toFloatOrNull() ?: 0f
            (litros * 1000 / ML_POR_VASO).toInt()
        } else {
            sliderVasos.value.toInt()
        }

        // ── Proteínas ──────────────────────────────────────────────────────
        val proteinaCumplida = cbProteinaCumplida.isChecked
        val proteinaGramos: Float? = if (proteinaCumplida) {
            metaProteinaG.toFloat()  // Si cumplió, usar la meta
        } else {
            etProteinaGramos.text.toString().toFloatOrNull()
        }

        // ── Carbos ─────────────────────────────────────────────────────────
        val carbosCumplidos = cbCarbosCumplidos.isChecked
        val carbosGramos: Float? = if (carbosCumplidos) {
            metaCarbosG.toFloat()
        } else {
            etCarbosGramos.text.toString().toFloatOrNull()
        }

        // ── Grasas ─────────────────────────────────────────────────────────
        val grasasCumplidas = cbGrasasCumplidas.isChecked
        val grasasGramos: Float? = if (grasasCumplidas) {
            metaGrasasG.toFloat()
        } else {
            etGrasasGramos.text.toString().toFloatOrNull()
        }

        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val cumplimiento = CumplimientoDiario(
            fecha                  = fechaHoy,
            proteinaCumplida       = proteinaCumplida,
            carbosCumplidos        = carbosCumplidos,
            grasasCumplida         = grasasCumplidas,
            aguaCumplida           = vasosConsumidos >= metaVasos,
            proteinaGramosConsumidos = proteinaGramos,
            carbosGramosConsumidos   = carbosGramos,
            grasasGramosConsumidos   = grasasGramos,
            aguaVasosConsumidos      = vasosConsumidos
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                // UPSERT manual: eliminar el de hoy si existe y reinsertar
                db.cumplimientoDiarioDao().eliminarPorFecha(fechaHoy)
                db.cumplimientoDiarioDao().insertar(cumplimiento)
            }
            Toast.makeText(
                this@RegistroConsumoActivity,
                "✅ ¡Consumo del día guardado!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
