package com.arboleda.biocalcula.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.viewmodel.RegistroPesoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla reutilizable para Crear y Editar un RegistroPeso.
 *
 * Patrón UX:
 * - Sin EXTRA_REGISTRO_ID → modo CREATE (campos vacíos, botón "Guardar")
 * - Con EXTRA_REGISTRO_ID → modo EDIT (datos precargados, botón "Actualizar Cambios")
 *
 * La dualidad se maneja inspeccionando el Intent extra al arrancar.
 */
class FormularioPesoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REGISTRO_ID = "EXTRA_REGISTRO_ID"
    }

    private val viewModel: RegistroPesoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_peso)

        val tvTitulo = findViewById<TextView>(R.id.tvTituloPantalla)
        val etPeso = findViewById<EditText>(R.id.etPesoIngresado)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarPeso)

        // Recuperar el ID (-1 = modo CREATE, cualquier otro valor = modo EDIT)
        val registroId = intent.getIntExtra(EXTRA_REGISTRO_ID, -1)

        if (registroId != -1) {
            // ── MODO EDICIÓN ──────────────────────────────────────────────────
            tvTitulo.text = "Editar Registro de Peso"
            btnGuardar.text = "Actualizar Cambios"

            // Observar el registro en Room y precargar el valor en el campo
            viewModel.getRegistroPesoById(registroId).observe(this) { registro ->
                registro?.let {
                    // Mostrar el valor sin ".0" si es entero, o con decimales si los tiene
                    val pesoStr = if (it.peso_registrado % 1 == 0f)
                        it.peso_registrado.toInt().toString()
                    else
                        it.peso_registrado.toString()
                    etPeso.setText(pesoStr)
                    etPeso.setSelection(pesoStr.length) // cursor al final
                }
            }

            btnGuardar.setOnClickListener {
                val pesoTexto = etPeso.text.toString().trim()
                if (pesoTexto.isNotEmpty()) {
                    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.actualizar(
                        RegistroPeso(
                            id_registro = registroId,
                            peso_registrado = pesoTexto.toFloat(),
                            fecha_registro = fechaHoy
                        )
                    )
                    finish() // Volver al historial
                }
            }

        } else {
            // ── MODO CREAR ────────────────────────────────────────────────────
            tvTitulo.text = "Nuevo Registro"
            btnGuardar.text = "Guardar"

            btnGuardar.setOnClickListener {
                val pesoTexto = etPeso.text.toString().trim()
                if (pesoTexto.isNotEmpty()) {
                    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.insertar(
                        RegistroPeso(
                            peso_registrado = pesoTexto.toFloat(),
                            fecha_registro = fechaHoy
                        )
                    )
                    finish()
                }
            }
        }
    }
}
