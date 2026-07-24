package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.ui.adapter.RegistroPesoAdapter
import com.arboleda.biocalcula.viewmodel.HistorialViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * Pantalla 4 — Historial y Reporte Semanal.
 *
 * Sección A — Reporte semanal:
 *   - BarChart (MPAndroidChart) mostrando días cumplidos por macro
 *   - Chip con el porcentaje general de cumplimiento
 *   - TextViews mostrando X/7 días para cada macro
 *
 * Sección B — Historial de Peso:
 *   - RecyclerView con registros cronológicos de pesaje
 *   - FAB "+" para agregar nuevo pesaje
 *   - Editar (click) y Eliminar con confirmación (long click)
 *
 * Conectado a la barra de navegación inferior.
 */
class HistorialActivity : AppCompatActivity() {

    private val viewModel: HistorialViewModel by viewModels()

    private lateinit var chipPorcentaje: Chip
    private lateinit var barChart: BarChart
    private lateinit var tvDiasProteina: TextView
    private lateinit var tvDiasCarbos: TextView
    private lateinit var tvDiasGrasas: TextView
    private lateinit var tvDiasAgua: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        inicializarVistas()
        configurarNavegacion()
        configurarBarChart()
        configurarRecyclerView()
        observarViewModel()

        viewModel.cargarReporteSemanal()
    }

    private fun inicializarVistas() {
        chipPorcentaje  = findViewById(R.id.chipPorcentaje)
        barChart        = findViewById(R.id.barChart)
        tvDiasProteina  = findViewById(R.id.tvDiasProteina)
        tvDiasCarbos    = findViewById(R.id.tvDiasCarbos)
        tvDiasGrasas    = findViewById(R.id.tvDiasGrasas)
        tvDiasAgua      = findViewById(R.id.tvDiasAgua)
        recyclerView    = findViewById(R.id.recyclerView)
        fab             = findViewById(R.id.fabNuevoRegistro)
        bottomNav       = findViewById(R.id.bottomNav)
    }

    private fun configurarNavegacion() {
        bottomNav.selectedItemId = R.id.nav_historial
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_historial -> true
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    false
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    /** Configura el aspecto visual del BarChart antes de cargar datos. */
    private fun configurarBarChart() {
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled      = false
            setDrawGridBackground(false)
            setFitBars(true)
            setScaleEnabled(false)
            animateY(800)

            xAxis.apply {
                position         = XAxis.XAxisPosition.BOTTOM
                granularity      = 1f
                setDrawGridLines(false)
                textColor        = getColor(R.color.color_text_secondary)
                textSize         = 11f
                valueFormatter   = IndexAxisValueFormatter(
                    arrayOf("Prot.", "Carbos", "Grasas", "Agua")
                )
            }
            axisLeft.apply {
                axisMinimum  = 0f
                axisMaximum  = 7f
                granularity  = 1f
                textColor    = getColor(R.color.color_text_secondary)
                textSize     = 11f
            }
            axisRight.isEnabled = false
        }
    }

    private fun configurarRecyclerView() {
        val adapter = RegistroPesoAdapter(
            onEditar = { registro ->
                val intent = Intent(this, FormularioPesoActivity::class.java)
                intent.putExtra(FormularioPesoActivity.EXTRA_REGISTRO_ID, registro.id_registro)
                startActivity(intent)
            },
            onEliminar = { registro ->
                confirmarEliminacion(registro)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.todosLosRegistros.observe(this) { lista ->
            adapter.submitList(lista)
        }

        fab.setOnClickListener {
            startActivity(Intent(this, FormularioPesoActivity::class.java))
        }
    }

    private fun observarViewModel() {
        viewModel.reporteSemanal.observe(this) { reporte ->
            if (reporte == null) {
                // Sin datos esta semana
                chipPorcentaje.text = "0%"
                tvDiasProteina.text = "0/7"
                tvDiasCarbos.text   = "0/7"
                tvDiasGrasas.text   = "0/7"
                tvDiasAgua.text     = "0/7"
                barChart.clear()
                barChart.invalidate()
                return@observe
            }

            val total = reporte.totalDiasEvaluados
            chipPorcentaje.text = "${reporte.porcentajeGeneral}%"
            tvDiasProteina.text = "${reporte.diasProteina}/$total"
            tvDiasCarbos.text   = "${reporte.diasCarbos}/$total"
            tvDiasGrasas.text   = "${reporte.diasGrasas}/$total"
            tvDiasAgua.text     = "${reporte.diasAgua}/$total"

            actualizarBarChart(reporte.diasProteina, reporte.diasCarbos,
                               reporte.diasGrasas, reporte.diasAgua)
        }
    }

    private fun actualizarBarChart(
        proteina: Int, carbos: Int, grasas: Int, agua: Int
    ) {
        val entries = listOf(
            BarEntry(0f, proteina.toFloat()),
            BarEntry(1f, carbos.toFloat()),
            BarEntry(2f, grasas.toFloat()),
            BarEntry(3f, agua.toFloat())
        )

        val colors = listOf(
            getColor(R.color.color_proteina),
            getColor(R.color.color_carbos),
            getColor(R.color.color_grasas),
            getColor(R.color.color_agua)
        )

        val dataSet = BarDataSet(entries, "Días cumplidos").apply {
            this.colors    = colors
            valueTextColor = getColor(R.color.color_text_primary)
            valueTextSize  = 12f
        }

        barChart.data = BarData(dataSet).apply { barWidth = 0.5f }
        barChart.invalidate()
    }

    /** Confirmación antes de eliminar un registro de peso. */
    private fun confirmarEliminacion(registro: RegistroPeso) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar registro de peso?")
            .setMessage("Se borrará el registro de ${registro.peso_registrado} kg del ${registro.fecha_registro}.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminar(registro)
                Snackbar.make(recyclerView, "Registro eliminado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") {
                        viewModel.insertar(registro)
                    }
                    .setActionTextColor(getColor(android.R.color.holo_green_light))
                    .show()
            }
            .show()
    }
}
