package com.arboleda.biocalcula.ui.activities

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.util.NutricionHelper
import com.arboleda.biocalcula.util.NotifPreferences
import com.arboleda.biocalcula.util.SessionManager
import com.arboleda.biocalcula.viewmodel.RegistroPesoViewModel
import com.arboleda.biocalcula.viewmodel.UsuarioViewModel
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.ui.workers.WorkerScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla 5 — Perfil del Usuario.
 *
 * Muestra:
 *  - Avatar con iniciales del nombre
 *  - Nombre y objetivo activo
 *  - Datos biométricos: Peso, Talla, Edad, Sexo
 *  - Botón "Editar datos" → diálogo para actualizar peso
 *  - Tarjeta de recordatorios con 2 switches:
 *      · 🌙 Rutina diaria (macros + agua) — hora nocturna configurable (TimePicker)
 *      · ⚖️ Registro de peso — intervalo en días configurable (NumberPicker)
 *  - Botón cerrar sesión
 *  - Barra de navegación inferior
 */
class PerfilActivity : AppCompatActivity() {

    private val usuarioViewModel: UsuarioViewModel by viewModels()
    private val registroViewModel: RegistroPesoViewModel by viewModels()
    private lateinit var session: SessionManager
    private lateinit var notifPrefs: NotifPreferences

    // ── Datos biométricos ──────────────────────────────────────────────────────
    private lateinit var tvAvatar           : TextView
    private lateinit var tvNombrePerfil     : TextView
    private lateinit var chipObjetivoPerfil : Chip
    private lateinit var tvPesoPerfil       : TextView
    private lateinit var tvTallaPerfil      : TextView
    private lateinit var tvEdadPerfil       : TextView
    private lateinit var tvSexoPerfil       : TextView
    private lateinit var btnEditarDatos     : MaterialButton
    private lateinit var btnCerrarSesion    : MaterialButton
    private lateinit var bottomNav          : BottomNavigationView

    // ── Switches de recordatorios ──────────────────────────────────────────────
    private lateinit var switchRutinaDiaria : SwitchMaterial
    private lateinit var switchPesoSemanal  : SwitchMaterial

    // ── Paneles de configuración ───────────────────────────────────────────────
    private lateinit var layoutHoraRutina   : LinearLayout
    private lateinit var tvHoraRutina       : TextView
    private lateinit var layoutIntervaloPeso: LinearLayout
    private lateinit var tvIntervaloPeso    : TextView

    // Opciones de intervalo de días para el picker de peso
    private val OPCIONES_DIAS = intArrayOf(3, 5, 7, 10, 14, 30)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        session     = SessionManager(this)
        notifPrefs  = NotifPreferences(this)
        val userId  = session.obtenerUserId()

        inicializarVistas()
        configurarNavegacion()
        observarViewModel()
        usuarioViewModel.cargarUsuario(userId)

        // ── Botón editar datos / actualizar peso ──────────────────────────────
        btnEditarDatos.setOnClickListener {
            mostrarDialogoActualizarPeso(userId)
        }

        // ── Botón cerrar sesión ───────────────────────────────────────────────
        btnCerrarSesion.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("🚨 Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    session.cerrarSesion()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .show()
        }

        configurarRecordatorios()

        // Si el worker de peso envió un extra para abrir el diálogo directamente
        if (intent.getBooleanExtra("ABRIR_DIALOGO_PESO", false)) {
            mostrarDialogoActualizarPeso(userId)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inicialización
    // ─────────────────────────────────────────────────────────────────────────

    private fun inicializarVistas() {
        tvAvatar            = findViewById(R.id.tvAvatar)
        tvNombrePerfil      = findViewById(R.id.tvNombrePerfil)
        chipObjetivoPerfil  = findViewById(R.id.chipObjetivoPerfil)
        tvPesoPerfil        = findViewById(R.id.tvPesoPerfil)
        tvTallaPerfil       = findViewById(R.id.tvTallaPerfil)
        tvEdadPerfil        = findViewById(R.id.tvEdadPerfil)
        tvSexoPerfil        = findViewById(R.id.tvSexoPerfil)
        btnEditarDatos      = findViewById(R.id.btnEditarDatos)
        btnCerrarSesion     = findViewById(R.id.btnCerrarSesion)
        bottomNav           = findViewById(R.id.bottomNav)

        // Recordatorios
        switchRutinaDiaria  = findViewById(R.id.switchRutinaDiaria)
        switchPesoSemanal   = findViewById(R.id.switchPesoSemanal)
        layoutHoraRutina    = findViewById(R.id.layoutHoraRutina)
        tvHoraRutina        = findViewById(R.id.tvHoraRutina)
        layoutIntervaloPeso = findViewById(R.id.layoutIntervaloPeso)
        tvIntervaloPeso     = findViewById(R.id.tvIntervaloPeso)
    }

    private fun configurarNavegacion() {
        bottomNav.selectedItemId = R.id.nav_perfil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_perfil    -> true
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    false
                }
                R.id.nav_historial -> {
                    startActivity(Intent(this, HistorialActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configurar switches de recordatorios
    // ─────────────────────────────────────────────────────────────────────────

    private fun configurarRecordatorios() {
        // ── Estado inicial desde SharedPreferences ─────────────────────────
        switchRutinaDiaria.isChecked    = notifPrefs.rutinaActiva
        switchPesoSemanal.isChecked     = notifPrefs.pesoActivo

        tvHoraRutina.text               = notifPrefs.horaFormateada()
        tvIntervaloPeso.text            = "${notifPrefs.pesoIntervalosDias} días"

        layoutHoraRutina.visibility     = if (notifPrefs.rutinaActiva)  View.VISIBLE else View.GONE
        layoutIntervaloPeso.visibility  = if (notifPrefs.pesoActivo)    View.VISIBLE else View.GONE

        // ── Switch rutina diaria ───────────────────────────────────────────
        switchRutinaDiaria.setOnCheckedChangeListener { _, isChecked ->
            notifPrefs.rutinaActiva = isChecked
            layoutHoraRutina.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                WorkerScheduler.programarRutina(this)
            } else {
                WorkerScheduler.cancelarRutina(this)
            }
        }

        // ── Tocar la hora abre TimePicker ──────────────────────────────────
        tvHoraRutina.setOnClickListener {
            val hora   = notifPrefs.rutinaHora
            val minuto = notifPrefs.rutinaMinuto
            TimePickerDialog(this, { _, h, m ->
                notifPrefs.rutinaHora   = h
                notifPrefs.rutinaMinuto = m
                tvHoraRutina.text       = "%02d:%02d".format(h, m)
                // Reprogramar con la nueva hora
                WorkerScheduler.cancelarRutina(this)
                WorkerScheduler.programarRutina(this)
            }, hora, minuto, true).show()
        }

        // ── Switch peso semanal ────────────────────────────────────────────
        switchPesoSemanal.setOnCheckedChangeListener { _, isChecked ->
            notifPrefs.pesoActivo = isChecked
            layoutIntervaloPeso.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                WorkerScheduler.programarPeso(this)
            } else {
                WorkerScheduler.cancelarPeso(this)
            }
        }

        // ── Tocar el intervalo abre NumberPicker ───────────────────────────
        tvIntervaloPeso.setOnClickListener {
            mostrarPickerIntervaloPeso()
        }
    }

    /**
     * Muestra un diálogo con NumberPicker para escoger el intervalo de días
     * entre recordatorios de peso. Opciones: 3, 5, 7, 10, 14, 30 días.
     */
    private fun mostrarPickerIntervaloPeso() {
        val dialogView = layoutInflater.inflate(android.R.layout.select_dialog_singlechoice, null)

        // Crear NumberPicker programáticamente
        val picker = NumberPicker(this).apply {
            minValue    = 0
            maxValue    = OPCIONES_DIAS.size - 1
            displayedValues = OPCIONES_DIAS.map { "$it días" }.toTypedArray()
            wrapSelectorWheel = false
            // Seleccionar el índice actual
            val currentDias = notifPrefs.pesoIntervalosDias
            val idx = OPCIONES_DIAS.indexOfFirst { it == currentDias }.coerceAtLeast(2) // default = índice 2 (7 días)
            value = idx
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("⚖️ Frecuencia de pesaje")
            .setMessage("¿Cada cuántos días quieres que te recuerde registrar tu peso?")
            .setView(picker)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val diasSeleccionados = OPCIONES_DIAS[picker.value]
                notifPrefs.pesoIntervalosDias = diasSeleccionados
                tvIntervaloPeso.text = "$diasSeleccionados días"
                // Reprogramar con el nuevo intervalo
                WorkerScheduler.cancelarPeso(this)
                WorkerScheduler.programarPeso(this)
            }
            .show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observer del ViewModel
    // ─────────────────────────────────────────────────────────────────────────

    private fun observarViewModel() {
        usuarioViewModel.usuario.observe(this) { usuario ->
            if (usuario == null) return@observe

            // Avatar: iniciales del nombre
            val iniciales = usuario.nombre
                .trim()
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifEmpty { "U" }
            tvAvatar.text = iniciales

            // Nombre y objetivo
            tvNombrePerfil.text = usuario.nombre
            val etiqueta = if (usuario.objetivo != null)
                NutricionHelper.etiquetaObjetivo(usuario.objetivo)
            else "Sin objetivo"
            chipObjetivoPerfil.text = etiqueta

            // Datos biométricos
            tvPesoPerfil.text  = if (usuario.peso  != null) "${usuario.peso} kg"    else "-- kg"
            tvTallaPerfil.text = if (usuario.talla != null) "${usuario.talla} cm"   else "-- cm"
            tvEdadPerfil.text  = if (usuario.edad  != null) "${usuario.edad} años"  else "-- años"
            tvSexoPerfil.text  = usuario.sexo ?: "--"
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Diálogo para actualizar peso
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra un AlertDialog para que el usuario ingrese su nuevo peso.
     * Guarda el nuevo peso en la tabla Usuario (para recalcular macros)
     * y también en RegistroPeso (historial cronológico).
     */
    private fun mostrarDialogoActualizarPeso(userId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_actualizar_peso, null)
        val etPesoDialog = dialogView.findViewById<TextInputEditText>(R.id.etPesoDialog)

        // Prellenar con el peso actual si existe
        usuarioViewModel.usuario.value?.peso?.let { pesoActual ->
            etPesoDialog.setText(pesoActual.toString())
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Actualizar peso")
            .setMessage("Ingresa tu peso actual para recalcular tus macros")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val pesoStr  = etPesoDialog.text.toString().trim()
                val nuevoPeso = pesoStr.toFloatOrNull()
                if (nuevoPeso != null && nuevoPeso > 0) {
                    // Actualizar peso en tabla usuario (para recalcular macros)
                    usuarioViewModel.actualizarPeso(userId, nuevoPeso) {
                        // También registrar en historial de pesajes
                        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        registroViewModel.insertar(
                            RegistroPeso(
                                peso_registrado  = nuevoPeso,
                                fecha_registro   = fechaHoy
                            )
                        )
                    }
                }
            }
            .show()
    }
}
