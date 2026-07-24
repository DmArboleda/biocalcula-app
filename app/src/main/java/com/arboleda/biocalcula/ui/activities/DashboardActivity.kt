package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.util.NutricionHelper
import com.arboleda.biocalcula.util.SessionManager
import com.arboleda.biocalcula.viewmodel.DashboardViewModel

/**
 * Pantalla 3 — Dashboard Principal.
 *
 * Muestra:
 *  - Chip con el objetivo activo
 *  - Caja de calorías diarias totales (Mifflin-St Jeor)
 *  - Gráfico de dona con distribución de macros (MPAndroidChart)
 *  - 3 tarjetas: gramos de proteínas / carbohidratos / grasas
 *  - Tarjeta de agua diaria (35 ml × kg)
 *  - Checklist diario con "Guardar día"
 *  - Barra de navegación inferior
 */
class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var session: SessionManager

    // Vistas
    private lateinit var tvSaludo: TextView
    private lateinit var chipObjetivo: Chip
    private lateinit var tvCalorias: TextView
    private lateinit var pieChart: PieChart
    private lateinit var tvProteinaG: TextView
    private lateinit var tvCarbosG: TextView
    private lateinit var tvGrasasG: TextView
    private lateinit var tvAguaL: TextView
    private lateinit var cbProteina: CheckBox
    private lateinit var cbCarbos: CheckBox
    private lateinit var cbGrasas: CheckBox
    private lateinit var cbAgua: CheckBox
    private lateinit var tvDiaCompletado: TextView
    private lateinit var btnGuardarDia: MaterialButton
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)
        val userId = session.obtenerUserId()

        inicializarVistas()
        configurarNavegacion()
        configurarPieChart()
        observarViewModel()

        viewModel.cargarDashboard(userId)

        btnGuardarDia.setOnClickListener {
            viewModel.guardarCumplimiento(
                proteina = cbProteina.isChecked,
                carbos   = cbCarbos.isChecked,
                grasas   = cbGrasas.isChecked,
                agua     = cbAgua.isChecked
            )
        }

        // Detectar si todos los checks están marcados para mostrar el mensaje
        val checkboxes = listOf(cbProteina, cbCarbos, cbGrasas, cbAgua)
        checkboxes.forEach { cb ->
            cb.setOnCheckedChangeListener { _, _ ->
                actualizarEstadoDiaCompletado()
            }
        }
    }

    private fun inicializarVistas() {
        tvSaludo          = findViewById(R.id.tvSaludo)
        chipObjetivo      = findViewById(R.id.chipObjetivo)
        tvCalorias        = findViewById(R.id.tvCalorias)
        pieChart          = findViewById(R.id.pieChart)
        tvProteinaG       = findViewById(R.id.tvProteinaG)
        tvCarbosG         = findViewById(R.id.tvCarbosG)
        tvGrasasG         = findViewById(R.id.tvGrasasG)
        tvAguaL           = findViewById(R.id.tvAguaL)
        cbProteina        = findViewById(R.id.cbProteina)
        cbCarbos          = findViewById(R.id.cbCarbos)
        cbGrasas          = findViewById(R.id.cbGrasas)
        cbAgua            = findViewById(R.id.cbAgua)
        tvDiaCompletado   = findViewById(R.id.tvDiaCompletado)
        btnGuardarDia     = findViewById(R.id.btnGuardarDia)
        bottomNav         = findViewById(R.id.bottomNav)
    }

    private fun configurarNavegacion() {
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true  // ya estamos aquí
                R.id.nav_historial -> {
                    startActivity(Intent(this, HistorialActivity::class.java))
                    false  // no cambiar la selección (volvemos al dashboard)
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    /** Configura el aspecto visual base del PieChart antes de recibir datos. */
    private fun configurarPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled     = true
            holeRadius            = 55f
            transparentCircleRadius = 60f
            setHoleColor(getColor(R.color.color_surface))
            setTransparentCircleColor(getColor(R.color.color_surface))
            legend.isEnabled      = false
            setDrawEntryLabels(false)
            isRotationEnabled     = false
            animateY(800)
        }
    }

    private fun observarViewModel() {
        // ── Datos del usuario ──────────────────────────────────────────────────
        viewModel.usuario.observe(this) { usuario ->
            if (usuario != null) {
                val nombre = usuario.nombre.split(" ").firstOrNull() ?: usuario.nombre
                tvSaludo.text = "¡Hola, $nombre!"

                val etiqueta = if (usuario.objetivo != null)
                    NutricionHelper.etiquetaObjetivo(usuario.objetivo)
                else "Sin objetivo"
                chipObjetivo.text = "✓ $etiqueta"
            }
        }

        // ── Plan nutricional calculado ─────────────────────────────────────────
        viewModel.macrosPlan.observe(this) { plan ->
            if (plan != null) {
                tvCalorias.text  = String.format("%,d", plan.caloriasObjetivo)
                tvProteinaG.text = "${plan.proteinaG}g"
                tvCarbosG.text   = "${plan.carbosG}g"
                tvGrasasG.text   = "${plan.grasasG}g"
                tvAguaL.text     = String.format("%.1f L", plan.aguaL)

                // Actualizar gráfico de dona
                actualizarPieChart(
                    plan.proteinaPct.toFloat(),
                    plan.carbosPct.toFloat(),
                    plan.grasasPct.toFloat()
                )
            }
        }

        // ── Cumplimiento de hoy ────────────────────────────────────────────────
        viewModel.cumplimientoHoy.observe(this) { cumplimiento ->
            if (cumplimiento != null) {
                cbProteina.isChecked = cumplimiento.proteinaCumplida
                cbCarbos.isChecked   = cumplimiento.carbosCumplidos
                cbGrasas.isChecked   = cumplimiento.grasasCumplida
                cbAgua.isChecked     = cumplimiento.aguaCumplida
                actualizarEstadoDiaCompletado()
            }
        }

        // ── Resultado de guardar ───────────────────────────────────────────────
        viewModel.guardadoExito.observe(this) { exito ->
            if (exito == true) {
                Toast.makeText(this, "¡Progreso guardado! 💪", Toast.LENGTH_SHORT).show()
                actualizarEstadoDiaCompletado()
            }
        }
    }

    /** Rellena el PieChart con los porcentajes de macros. */
    private fun actualizarPieChart(pctProteina: Float, pctCarbos: Float, pctGrasas: Float) {
        val entries = listOf(
            PieEntry(pctProteina, "Proteínas"),
            PieEntry(pctCarbos,   "Carbohidratos"),
            PieEntry(pctGrasas,   "Grasas")
        )

        val colors = listOf(
            getColor(R.color.color_proteina),
            getColor(R.color.color_carbos),
            getColor(R.color.color_grasas)
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace     = 3f
            selectionShift = 6f
            valueTextSize  = 13f
            valueTextColor = getColor(R.color.color_text_primary)
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float) = "${value.toInt()}%"
            })
        }

        pieChart.data = data
        pieChart.invalidate()

        // Texto central con calorías
        pieChart.centerText = "Macros"
        pieChart.setCenterTextColor(getColor(R.color.color_text_primary))
        pieChart.setCenterTextSize(14f)
    }

    /** Muestra el mensaje "¡Día completado! ✅" si todos los checkboxes están marcados. */
    private fun actualizarEstadoDiaCompletado() {
        val todosCompletos = cbProteina.isChecked && cbCarbos.isChecked &&
                             cbGrasas.isChecked && cbAgua.isChecked
        tvDiaCompletado.visibility = if (todosCompletos) View.VISIBLE else View.GONE
    }
}
