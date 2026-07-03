package com.arboleda.biocalcula.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arboleda.biocalcula.R
import com.arboleda.biocalcula.data.model.RegistroPeso

class RegistroPesoAdapter(
    private val onEditar: (RegistroPeso) -> Unit,
    private val onEliminar: (RegistroPeso) -> Unit
) : ListAdapter<RegistroPeso, RegistroPesoAdapter.RegistroPesoViewHolder>(DiffCallback()) {

    inner class RegistroPesoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPeso: TextView = itemView.findViewById(R.id.tvPeso)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)

        fun bind(registro: RegistroPeso) {
            tvPeso.text = "${registro.peso_registrado} kg"
            tvFecha.text = registro.fecha_registro

            // Click normal → abrir formulario de edición con datos precargados
            itemView.setOnClickListener { onEditar(registro) }

            // Long click → confirmar eliminación con AlertDialog + Snackbar
            itemView.setOnLongClickListener {
                onEliminar(registro)
                true // consumir el evento
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroPesoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro_peso, parent, false)
        return RegistroPesoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegistroPesoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<RegistroPeso>() {
        override fun areItemsTheSame(oldItem: RegistroPeso, newItem: RegistroPeso) =
            oldItem.id_registro == newItem.id_registro
        override fun areContentsTheSame(oldItem: RegistroPeso, newItem: RegistroPeso) =
            oldItem == newItem
    }
}
