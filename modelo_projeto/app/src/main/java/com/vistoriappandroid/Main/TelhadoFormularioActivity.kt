package com.vistoriappandroid.Main

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vistoriappandroid.R
import java.io.File

class TelhadoFormularioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TelhadoFormulario", "onCreate iniciado - Antes de super.onCreate")
        try {
            super.onCreate(savedInstanceState)
            Log.d("TelhadoFormulario", "super.onCreate executado com sucesso")
            setContentView(R.layout.activity_telhado_formulario)
            Log.d("TelhadoFormulario", "setContentView executado com sucesso")
        } catch (e: Exception) {
            Log.e("TelhadoFormulario", "Erro ao executar onCreate: ${e.message}", e)
            Toast.makeText(this, "Erro ao abrir TelhadoFormularioActivity: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            // Referências aos campos do formulário
            val tipoMaterial = findViewById<EditText>(R.id.telhadoTipoMaterial)
            val area = findViewById<EditText>(R.id.telhadoArea)
            val inclinacao = findViewById<EditText>(R.id.telhadoInclinacao)
            val capacidadeCarga = findViewById<EditText>(R.id.telhadoCapacidadeCarga)
            val exposicaoSolar = findViewById<EditText>(R.id.telhadoExposicaoSolar)
            val conclusoesRecomendacoes = findViewById<EditText>(R.id.telhadoConclusoesRecomendacoes)
            val tipoSistema = findViewById<EditText>(R.id.telhadoTipoSistema)
            val potenciaInstalada = findViewById<EditText>(R.id.telhadoPotenciaInstalada)
            val numeroPlacas = findViewById<EditText>(R.id.telhadoNumeroPlacas)
            val marcaModeloPlacas = findViewById<EditText>(R.id.telhadoMarcaModeloPlacas)
            val inversor = findViewById<EditText>(R.id.telhadoInversor)
            val producaoEstimada = findViewById<EditText>(R.id.telhadoProducaoEstimada)
            Log.d("TelhadoFormulario", "Campos do formulário inicializados com sucesso")

            // Configurar o valor padrão de "Tipo de Sistema"
            tipoSistema.setText("Placas Solares")

            // Carregar dados salvos (se existirem)
            val dir = File(filesDir, "Telhado")
            if (!dir.exists()) dir.mkdirs()
            val formularioFile = File(dir, "formulario_telhado.txt")
            if (formularioFile.exists()) {
                try {
                    val dados = formularioFile.readText().split("|||")
                    if (dados.size >= 12) {
                        tipoMaterial.setText(dados[0])
                        area.setText(dados[1])
                        inclinacao.setText(dados[2])
                        capacidadeCarga.setText(dados[3])
                        exposicaoSolar.setText(dados[4])
                        conclusoesRecomendacoes.setText(dados[5])
                        tipoSistema.setText(dados[6])
                        potenciaInstalada.setText(dados[7])
                        numeroPlacas.setText(dados[8])
                        marcaModeloPlacas.setText(dados[9])
                        inversor.setText(dados[10])
                        producaoEstimada.setText(dados[11])
                    }
                    Log.d("TelhadoFormulario", "Dados salvos carregados com sucesso")
                } catch (e: Exception) {
                    Log.e("TelhadoFormulario", "Erro ao carregar dados do formulário: ${e.message}", e)
                }
            }

            // Botões Cancelar e Salvar
            findViewById<Button>(R.id.btnCancelar).setOnClickListener {
                Log.d("TelhadoFormulario", "Botão Cancelar clicado")
                finish()
            }

            findViewById<Button>(R.id.btnSalvar).setOnClickListener {
                Log.d("TelhadoFormulario", "Botão Salvar clicado")
                // Salvar os dados do formulário
                val dados = listOf(
                    tipoMaterial.text.toString(),
                    area.text.toString(),
                    inclinacao.text.toString(),
                    capacidadeCarga.text.toString(),
                    exposicaoSolar.text.toString(),
                    conclusoesRecomendacoes.text.toString(),
                    tipoSistema.text.toString(),
                    potenciaInstalada.text.toString(),
                    numeroPlacas.text.toString(),
                    marcaModeloPlacas.text.toString(),
                    inversor.text.toString(),
                    producaoEstimada.text.toString()
                ).joinToString("|||")

                try {
                    formularioFile.writeText(dados)
                    Log.d("TelhadoFormulario", "Dados do formulário salvos com sucesso")
                    Toast.makeText(this, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("TelhadoFormulario", "Erro ao salvar dados do formulário: ${e.message}", e)
                    Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("TelhadoFormulario", "Erro ao configurar formulário: ${e.message}", e)
            Toast.makeText(this, "Erro ao configurar formulário: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}