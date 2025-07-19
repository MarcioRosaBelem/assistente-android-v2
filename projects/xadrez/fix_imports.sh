#!/bin/bash
# AmbienteAdapter.kt
cat > "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/AmbienteAdapter.kt" <<INNER_EOF
package com.vistoria.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vistoria.app.R
import com.vistoria.app.model.Ambiente
import com.vistoria.app.util.loadImageFromAsset

class AmbienteAdapter(
    private val ambientes: List<Ambiente>,
    private val onClick: (Ambiente) -> Unit
) : RecyclerView.Adapter<AmbienteAdapter.AmbienteViewHolder>() {

    inner class AmbienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewAmbiente)
        private val textView: TextView = itemView.findViewById(R.id.textViewNomeAmbiente)

        fun bind(ambiente: Ambiente) {
            textView.text = ambiente.nome
            imageView.loadImageFromAsset(ambiente.imagePath)
            itemView.setOnClickListener { onClick(ambiente) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmbienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_home, parent, false)
        return AmbienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmbienteViewHolder, position: Int) {
        holder.bind(ambientes[position])
    }

    override fun getItemCount(): Int = ambientes.size
}
INNER_EOF

# PhotoAdapter.kt
cat > "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/adapters/PhotoAdapter.kt" <<INNER_EOF
package com.vistoria.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.vistoria.app.R
import com.vistoria.app.util.loadImageFromPath

class PhotoAdapter(
    private val photos: List<String>,
    private val onRemovePhoto: (String) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewPhoto)
        private val imageButtonRemovePhoto: ImageButton = itemView.findViewById(R.id.imageButtonRemovePhoto)

        fun bind(photo: String) {
            imageViewPhoto.loadImageFromPath(photo)
            imageButtonRemovePhoto.setOnClickListener { onRemovePhoto(photo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_thumbnail, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size
}
INNER_EOF

# NovaVistoriaFragment.kt
cat > "/workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/ui/fragments/NovaVistoriaFragment.kt" <<INNER_EOF
package com.vistoria.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.vistoria.app.R
import com.vistoria.app.adapters.AmbienteAdapter
import com.vistoria.app.databinding.FragmentNovaVistoriaBinding
import com.vistoria.app.model.Ambiente

class NovaVistoriaFragment : Fragment() {

    private var _binding: FragmentNovaVistoriaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNovaVistoriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val ambientes = listOf<Ambiente>() // Preencha com dados reais
        binding.recyclerViewAmbientes.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerViewAmbientes.adapter = AmbienteAdapter(ambientes) { ambiente ->
            findNavController().navigate(
                NovaVistoriaFragmentDirections.actionNovaVistoriaFragmentToRoomDescriptionFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
INNER_EOF
