package com.vistoriappandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vistoriappandroid.R

class FotosAdapter(
    private var fotos: List<String>,
    private var modoSelecaoAtivo: Boolean = false,
    private var fotosSelecionadas: MutableSet<Int> = mutableSetOf(),
    private val onItemClick: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {

    inner class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewFoto)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition, false)
            }

            itemView.setOnLongClickListener {
                onItemClick(adapterPosition, true)
                true
            }
        }

        fun bind(position: Int) {
            val foto = fotos[position]
            Glide.with(itemView.context)
                .load(foto)
                .into(imageView)

            itemView.alpha = if (fotosSelecionadas.contains(position)) 0.7f else 1.0f
            itemView.setBackgroundResource(
                if (fotosSelecionadas.contains(position)) R.drawable.borda_selecionada
                else android.R.color.transparent
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = fotos.size

    fun atualizarFotos(novasFotos: List<String>) {
        fotos = novasFotos
        notifyDataSetChanged()
    }

    fun setModoSelecao(ativo: Boolean) {
        modoSelecaoAtivo = ativo
        if (!ativo) {
            fotosSelecionadas.clear()
        }
        notifyDataSetChanged()
    }

    fun getFotosSelecionadas(): Set<Int> = fotosSelecionadas.toSet()

    fun clearSelection() {
        fotosSelecionadas.clear()
        notifyDataSetChanged()
    }
}