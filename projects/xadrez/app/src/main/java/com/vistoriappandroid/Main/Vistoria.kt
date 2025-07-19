package com.vistoriappandroid.model

data class Vistoria(
    val nomeCliente: String,
    val endereco: String,
    val qtdChaves: String,
    val medidorEnergia: String,
    val medidorAgua: String,
    val metodologia: String,
    val tipo: String,
    val descricao: String,
    val formularioDados: Map<String, String>,
    val caminhosFotos: Map<String, List<String>>,
    val vistoriaId: String
) : java.io.Serializable