package com.arboleda.biocalcula.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.data.model.RegistroPeso
import com.arboleda.biocalcula.ui.adapter.RegistroPesoAdapter
import com.arboleda.biocalcula.viewmodel.RegistroPesoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * Pantalla de Historial de Peso.
 *
 * Patrones UX implementados:
 *
 * ① EDITAR: Click normal en un ítem → abre FormularioPesoActivity en modo EDIT
 *    con los datos precargados. El usuario no tiene que recordar el valor anterior.
 *
 * ② ELIMINAR con confirmación: Long click → MaterialAlertDialog (bloquea la acción
 *    destructiva accidental). Si confirma → Snackbar con "Deshacer" (red de seguridad
 *    inmediata sin interrumpir el flujo con otro diálogo).
 *
 * ③ CREAR: FAB "+" en la esquina → abre FormularioPesoActivity en modo CREATE.
 */
class HistorialActivity : AppCompatActivity() {

    private val viewModel: RegistroPesoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fab = findViewById<FloatingActionButton>(R.id.fabNuevoRegistro)

        // ── Adapter con callbacks de editar y eliminar ────────────────────────
        val adapter = RegistroPesoAdapter(
            onEditar = { registro ->
                // Click normal → modo EDIT: pasar el ID al formulario
                val intent = Intent(this, FormularioPesoActivity::class.java)
                intent.putExtra(FormularioPesoActivity.EXTRA_REGISTRO_ID, registro.id_registro)
                startActivity(intent)
            },
            onEliminar = { registro ->
                // Long click → diálogo de confirmación (Material Design 3)
                confirmarEliminacion(registro)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ── Observer de Room → actualiza la lista en tiempo real ──────────────
        viewModel.todosLosRegistros.observe(this) { lista ->
            adapter.submitList(lista)
        }

        // ── FAB → modo CREATE (sin ID extra) ──────────────────────────────────
        fab.setOnClickListener {
            startActivity(Intent(this, FormularioPesoActivity::class.java))
        }
    }

    /**
     * Muestra un AlertDialog de Material Design 3 para confirmar la eliminación.
     *
     * Patrón UX — ¿cuándo usar AlertDialog?
     * Para acciones DESTRUCTIVAS e irreversibles. El diálogo interrumpe el flujo
     * deliberadamente para que el usuario confirme conscientemente antes de perder datos.
     *
     * Patrón UX — ¿cuándo usar Snackbar "Deshacer"?
     * Como red de seguridad posterior. Permite revertir sin usar un segundo diálogo,
     * agilizando la experiencia: el usuario elimina rápido y puede arrepentirse sin fricción.
     */
    private fun confirmarEliminacion(registro: RegistroPeso) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar registro de peso?")
            .setMessage("Se borrará el registro de ${registro.peso_registrado} kg del ${registro.fecha_registro}. Esta acción alterará tu historial de progreso.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                // Ejecutar el borrado en Room vía ViewModel
                viewModel.eliminar(registro)

                // Snackbar con acción "Deshacer" — permite recuperar si fue accidental
                val rootView = findViewById<RecyclerView>(R.id.recyclerView)
                Snackbar.make(rootView, "Registro eliminado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") {
                        // Re-insertar el registro exacto (con su fecha original)
                        viewModel.insertar(registro)
                    }
                    .setActionTextColor(getColor(android.R.color.holo_green_light))
                    .show()
            }
            .show()
    }
}
