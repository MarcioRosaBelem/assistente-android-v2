package com.vistoriappandroid.Main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vistoriappandroid.R
import com.vistoriappandroid.adapter.HistoricoAdapter
import com.vistoriappandroid.model.Vistoria
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class HistoricoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoricoAdapter
    private lateinit var textSemVistorias: TextView
    private lateinit var btnVoltar: ImageButton
    private val historico = mutableListOf<Vistoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico)

        recyclerView = findViewById(R.id.recyclerViewHistorico)
        textSemVistorias = findViewById(R.id.textSemVistorias)
        btnVoltar = findViewById(R.id.btnVoltar)

        carregarHistorico()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoricoAdapter(
            historico,
            onEditClick = { position ->
                val vistoria = historico[position]
                val intent = Intent().apply {
                    putExtra("vistoria", vistoria)
                }
                setResult(RESULT_OK, intent)
                finish()
                Log.d("HistoricoActivity", "Vistoria selecionada para edição: ${vistoria.nomeCliente}")
            },
            onDeleteClick = { position ->
                val vistoria = historico[position]
                // Remove all related directories for the client
                val clienteDir = File(filesDir, vistoria.nomeCliente)
                if (clienteDir.exists()) {
                    clienteDir.listFiles { file ->
                        file.isDirectory && file.name.startsWith("vistoria_")
                    }?.forEach { vistoriaDir ->
                        if (vistoriaDir.name == "vistoria_${vistoria.vistoriaId}") {
                            vistoriaDir.deleteRecursively()
                            Log.d("HistoricoActivity", "Diretório da vistoria removido: ${vistoriaDir.absolutePath}")
                        }
                    }
                    // Remove client directory if no vistorias remain
                    if (historico.none { it.nomeCliente == vistoria.nomeCliente }) {
                        clienteDir.deleteRecursively()
                        Log.d("HistoricoActivity", "Diretório de cliente removido: ${clienteDir.absolutePath}")
                    }
                }
                // Remove vistoria from list
                historico.removeAt(position)
                adapter.notifyItemRemoved(position)
                salvarHistorico()
                atualizarVisibilidade()
            }
        )
        recyclerView.adapter = adapter

        atualizarVisibilidade()

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun carregarHistorico() {
        val historicoFile = File(filesDir, "historico.dat")
        if (historicoFile.exists()) {
            try {
                historicoFile.inputStream().use { fis ->
                    ObjectInputStream(fis).use { ois ->
                        @Suppress("UNCHECKED_CAST")
                        val loadedHistorico = ois.readObject() as List<Vistoria>?
                        loadedHistorico?.let {
                            historico.clear()
                            historico.addAll(it)
                            Log.d("HistoricoActivity", "Histórico carregado: ${historico.size} vistorias")
                            // Clean up orphaned vistoria directories
                            filesDir.listFiles { file ->
                                file.isDirectory
                            }?.forEach { clienteDir ->
                                clienteDir.listFiles { file ->
                                    file.isDirectory && file.name.startsWith("vistoria_")
                                }?.forEach { vistoriaDir ->
                                    val vistoriaId = vistoriaDir.name.removePrefix("vistoria_")
                                    if (historico.none { it.vistoriaId == vistoriaId }) {
                                        vistoriaDir.deleteRecursively()
                                        Log.d("HistoricoActivity", "Diretório órfão removido: ${vistoriaDir.absolutePath}")
                                    }
                                }
                                // Remove client directory if empty
                                if (clienteDir.listFiles()?.isEmpty() == true) {
                                    clienteDir.deleteRecursively()
                                    Log.d("HistoricoActivity", "Diretório de cliente vazio removido: ${clienteDir.absolutePath}")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoricoActivity", "Erro ao carregar histórico: ${e.message}", e)
                // Delete corrupted history file to prevent issues
                if (historicoFile.exists()) {
                    historicoFile.delete()
                    Log.d("HistoricoActivity", "Arquivo historico.dat corrompido deletado")
                }
            }
        }
    }

    private fun salvarHistorico() {
        val historicoFile = File(filesDir, "historico.dat")
        try {
            FileOutputStream(historicoFile).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(historico)
                }
            }
            Log.d("HistoricoActivity", "Histórico salvo com sucesso: ${historico.size} vistorias")
        } catch (e: Exception) {
            Log.e("HistoricoActivity", "Erro ao salvar histórico: ${e.message}", e)
        }
    }

    private fun atualizarVisibilidade() {
        if (historico.isEmpty()) {
            textSemVistorias.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textSemVistorias.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}