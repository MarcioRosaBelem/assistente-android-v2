package com.vistoriappandroid.model

data class Ambiente(
    val nome: String,
    val imagem: Int,  // Propriedade para o ID do recurso drawable
    val qtdFotos: Int,
    val status: String
)