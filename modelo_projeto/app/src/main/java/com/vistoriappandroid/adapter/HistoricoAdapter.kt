package com.vistoriappandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vistoriappandroid.R
import com.vistoriappandroid.model.Vistoria

class HistoricoAdapter(
    private val historico: MutableList<Vistoria>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNomeCliente: TextView = itemView.findViewById(R.id.textNomeCliente)
        val textData: TextView = itemView.findViewById(R.id.textData)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        val btnApagar: ImageButton = itemView.findViewById(R.id.btnApagar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historico, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vistoria = historico[position]
        holder.textNomeCliente.text = vistoria.nomeCliente
        holder.textData.text = "Data: ${vistoria.tipo}"

        holder.btnEditar.setOnClickListener {
            onEditClick(position)
        }

        holder.btnApagar.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = historico.size
}