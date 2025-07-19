package com.vistoriappandroid.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.vistoriappandroid.Main.CameraXActivity
import com.vistoriappandroid.Main.DescricaoAmbientesActivity
import com.vistoriappandroid.R
import com.vistoriappandroid.model.Ambiente

class AmbientesAdapter(
    private val context: Context,
    private val ambientes: List<Ambiente>,
    private val nomeClienteProvider: () -> String
) : RecyclerView.Adapter<AmbientesAdapter.AmbienteViewHolder>() {

    class AmbienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagem: ImageView = itemView.findViewById(R.id.imagemAmbiente)
        val nome: TextView = itemView.findViewById(R.id.nomeAmbiente)
        val status: TextView = itemView.findViewById(R.id.statusAmbiente)
        val qtdFotos: TextView = itemView.findViewById(R.id.qtdFotos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmbienteViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_ambiente, parent, false)
        return AmbienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmbienteViewHolder, position: Int) {
        val ambiente = ambientes[position]
        holder.imagem.setImageResource(ambiente.imagem)
        holder.nome.text = ambiente.nome
        holder.status.text = ambiente.status
        holder.qtdFotos.text = context.getString(R.string.fotos_format, ambiente.qtdFotos)

        holder.itemView.setOnClickListener {
            val nomeCliente = nomeClienteProvider()
            if (nomeCliente.isEmpty()) {
                Toast.makeText(context, "Por favor, insira o nome do cliente", Toast.LENGTH_SHORT).show()
                Log.d("AmbientesAdapter", "Tentativa de abrir ambiente ${ambiente.nome} bloqueada: nome do cliente vazio")
            } else {
                val targetActivity = if (ambiente.qtdFotos == 0) {
                    CameraXActivity::class.java
                } else {
                    DescricaoAmbientesActivity::class.java
                }

                Intent(context, targetActivity).apply {
                    putExtra("nomeAmbiente", ambiente.nome)
                    putExtra("nomeCliente", nomeCliente)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(this)
                    Log.d("AmbientesAdapter", "Abrindo $targetActivity para ${ambiente.nome} com cliente $nomeCliente")
                }
            }
        }
    }

    override fun getItemCount(): Int = ambientes.size
}