package com.vistoriappandroid.Main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vistoriappandroid.R
import com.vistoriappandroid.adapter.FotosAdapter
import com.vistoriappandroid.databinding.ActivityDescricaoAmbientesBinding
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

// Classe de dados para armazenar informações do formulário
data class FormularioData(
    val portaPortao: String = "",
    val janela: String = "",
    val parede: String = "",
    val piso: String = "",
    val acessorio1: String = "",
    val acessorio2: String = "",
    val acessorio3: String = "",
    val elementosExtras: List<String> = emptyList(),
    val acessoriosExtras: List<String> = emptyList()
) : Serializable

class DescricaoAmbientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDescricaoAmbientesBinding
    private lateinit var nomeAmbiente: String
    private lateinit var nomeCliente: String
    private lateinit var fotosAdapter: FotosAdapter
    private val listaCaminhosFotos = mutableListOf<String>()
    private val fotosSelecionadas = mutableSetOf<Int>()
    private var modoSelecao = false
    private var elementoCounter = 1
    private var acessorioCounter = 1
    private val MAX_FOTOS = 8

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) carregarFotosDoAmbiente()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescricaoAmbientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setupData()
            configurarRecyclerView()
            carregarDadosSalvos()
            carregarFotosDoAmbiente()
            configurarBotoes()
        } catch (e: Exception) {
            Log.e("DescricaoAmbientes", "Erro na inicialização: ${e.message}", e)
            Toast.makeText(this, "Erro ao inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupData() {
        nomeAmbiente = intent.getStringExtra("nomeAmbiente") ?: "Ambiente"
        nomeCliente = intent.getStringExtra("nomeCliente") ?: "SemCliente"
        binding.textTitulo.text = nomeAmbiente
        Log.d("DescricaoAmbientes", "Inicializado com nomeAmbiente: $nomeAmbiente, nomeCliente: $nomeCliente")
    }

    private fun configurarRecyclerView() {
        binding.recyclerViewFotos.layoutManager = GridLayoutManager(this, 4)
        fotosAdapter = FotosAdapter(listaCaminhosFotos, modoSelecao, fotosSelecionadas) { pos, longClick ->
            if (longClick) ativarModoSelecao(pos) else tratarClique(pos)
        }
        binding.recyclerViewFotos.adapter = fotosAdapter
    }

    private fun ativarModoSelecao(position: Int) {
        modoSelecao = true
        binding.selectionButtonsContainer.visibility = View.VISIBLE
        toggleSelecao(position)
    }

    private fun tratarClique(position: Int) {
        if (modoSelecao) {
            toggleSelecao(position)
        } else {
            abrirImagem(position)
        }
    }

    private fun toggleSelecao(position: Int) {
        if (fotosSelecionadas.contains(position)) {
            fotosSelecionadas.remove(position)
        } else {
            fotosSelecionadas.add(position)
        }
        fotosAdapter.notifyItemChanged(position)
    }

    private fun configurarBotoes() {
        binding.btnAdicionarFotos.setOnClickListener {
            if (listaCaminhosFotos.size < MAX_FOTOS) {
                cameraLauncher.launch(Intent(this, CameraXActivity::class.java).apply {
                    putExtra("nomeAmbiente", nomeAmbiente)
                    putExtra("nomeCliente", nomeCliente)
                })
            } else {
                Toast.makeText(this, "Limite de $MAX_FOTOS fotos atingido", Toast.LENGTH_SHORT).show()
            }
        }

        binding.botaoSalvar.setOnClickListener {
            salvarTudo()
            finish()
        }

        binding.btnLixeira.setOnClickListener { toggleModoSelecao() }
        binding.botaoExcluirSelecionadas.setOnClickListener { apagarFotosSelecionadas() }
        binding.botaoCancelarSelecao.setOnClickListener { cancelarSelecao() }
        binding.btnPreencherFormulario.setOnClickListener { mostrarFormulario() }
    }

    private fun toggleModoSelecao() {
        modoSelecao = !modoSelecao
        fotosSelecionadas.clear()
        atualizarAdapter()
        binding.selectionButtonsContainer.visibility = if (modoSelecao) View.VISIBLE else View.GONE
    }

    private fun cancelarSelecao() {
        modoSelecao = false
        fotosSelecionadas.clear()
        atualizarAdapter()
        binding.selectionButtonsContainer.visibility = View.GONE
    }

    private fun mostrarFormulario() {
        // Reiniciar contadores para evitar duplicação
        elementoCounter = 1
        acessorioCounter = 1

        val formularioView = layoutInflater.inflate(R.layout.formulario_ambiente, null)
        binding.formularioContainer.removeAllViews()
        binding.formularioContainer.addView(formularioView)
        binding.formularioContainer.visibility = View.VISIBLE
        Log.d("DescricaoAmbientes", "Formulário aberto para $nomeAmbiente")

        // Configurar os botões do formulário com verificação de null
        formularioView.findViewById<Button>(R.id.btnAdicionarElemento)?.setOnClickListener {
            adicionarElemento(formularioView)
            Log.d("DescricaoAmbientes", "Adicionado elemento $elementoCounter")
        } ?: Log.e("DescricaoAmbientes", "Botão btnAdicionarElemento não encontrado")

        formularioView.findViewById<Button>(R.id.btnAdicionarAcessorio)?.setOnClickListener {
            adicionarAcessorio(formularioView)
            Log.d("DescricaoAmbientes", "Adicionado acessório $acessorioCounter")
        } ?: Log.e("DescricaoAmbientes", "Botão btnAdicionarAcessorio não encontrado")

        // Atualizar o nome do ambiente no formulário
        formularioView.findViewById<TextView>(R.id.textNomeAmbiente)?.let {
            it.text = "Ambiente: $nomeAmbiente"
        } ?: Log.e("DescricaoAmbientes", "TextView textNomeAmbiente não encontrado")

        // Carregar dados salvos do formulário
        carregarDadosFormulario(formularioView)

        // Rolagem automática para o formulário
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.formularioContainer.top)
        }
    }

    private fun adicionarElemento(formularioView: View) {
        val elementoView = layoutInflater.inflate(R.layout.item_elemento_adicional, null)
        val label = elementoView.findViewById<TextView>(R.id.labelElemento)
        label.text = "Item Adicional $elementoCounter - Tipo, Cor e Estado:"
        elementoView.tag = "elemento_$elementoCounter"
        formularioView.findViewById<LinearLayout>(R.id.elementosExtrasContainer).addView(elementoView)
        elementoCounter++
    }

    private fun adicionarAcessorio(formularioView: View) {
        val acessorioView = layoutInflater.inflate(R.layout.item_acessorio_adicional, null)
        val label = acessorioView.findViewById<TextView>(R.id.labelAcessorio)
        label.text = "Acessório Adicional $acessorioCounter - Tipo, Cor e Estado:"
        acessorioView.tag = "acessorio_$acessorioCounter"
        formularioView.findViewById<LinearLayout>(R.id.acessoriosExtrasContainer).addView(acessorioView)
        acessorioCounter++
    }

    private fun salvarFormulario(formularioView: View) {
        val formularioData = FormularioData(
            portaPortao = formularioView.findViewById<EditText>(R.id.portaPortao)?.text.toString() ?: "",
            janela = formularioView.findViewById<EditText>(R.id.janela)?.text.toString() ?: "",
            parede = formularioView.findViewById<EditText>(R.id.parede)?.text.toString() ?: "",
            piso = formularioView.findViewById<EditText>(R.id.piso)?.text.toString() ?: "",
            acessorio1 = formularioView.findViewById<EditText>(R.id.acessorio1)?.text.toString() ?: "",
            acessorio2 = formularioView.findViewById<EditText>(R.id.acessorio2)?.text.toString() ?: "",
            acessorio3 = formularioView.findViewById<EditText>(R.id.acessorio3)?.text.toString() ?: "",
            elementosExtras = (0 until formularioView.findViewById<LinearLayout>(R.id.elementosExtrasContainer).childCount).map { i ->
                formularioView.findViewById<LinearLayout>(R.id.elementosExtrasContainer).getChildAt(i)
                    .findViewById<EditText>(R.id.editElemento).text.toString()
            },
            acessoriosExtras = (0 until formularioView.findViewById<LinearLayout>(R.id.acessoriosExtrasContainer).childCount).map { i ->
                formularioView.findViewById<LinearLayout>(R.id.acessoriosExtrasContainer).getChildAt(i)
                    .findViewById<EditText>(R.id.editAcessorio).text.toString()
            }
        )

        try {
            val file = File(getDirAmbiente(), "formulario_$nomeAmbiente.dat")
            // Deletar arquivo antigo para evitar conflitos
            if (file.exists()) {
                file.delete()
                Log.d("DescricaoAmbientes", "Arquivo antigo deletado: ${file.absolutePath}")
            }
            ObjectOutputStream(FileOutputStream(file)).use {
                it.writeObject(formularioData)
                Log.d("DescricaoAmbientes", "Formulário salvo: $formularioData")
            }
            Toast.makeText(this, "Formulário salvo com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DescricaoAmbientes", "Erro ao salvar formulário: ${e.message}", e)
            Toast.makeText(this, "Erro ao salvar formulário: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarDadosFormulario(formularioView: View) {
        val file = File(getDirAmbiente(), "formulario_$nomeAmbiente.dat")
        if (!file.exists()) {
            Log.d("DescricaoAmbientes", "Nenhum arquivo de formulário encontrado para $nomeAmbiente")
            return
        }

        try {
            ObjectInputStream(FileInputStream(file)).use { ois ->
                val data = ois.readObject() as? FormularioData
                data?.let {
                    formularioView.findViewById<EditText>(R.id.portaPortao)?.setText(it.portaPortao)
                    formularioView.findViewById<EditText>(R.id.janela)?.setText(it.janela)
                    formularioView.findViewById<EditText>(R.id.parede)?.setText(it.parede)
                    formularioView.findViewById<EditText>(R.id.piso)?.setText(it.piso)
                    formularioView.findViewById<EditText>(R.id.acessorio1)?.setText(it.acessorio1)
                    formularioView.findViewById<EditText>(R.id.acessorio2)?.setText(it.acessorio2)
                    formularioView.findViewById<EditText>(R.id.acessorio3)?.setText(it.acessorio3)

                    // Reiniciar contadores antes de adicionar elementos
                    elementoCounter = 1
                    acessorioCounter = 1

                    it.elementosExtras.forEach { elemento ->
                        adicionarElemento(formularioView)
                        formularioView.findViewById<LinearLayout>(R.id.elementosExtrasContainer)
                            .getChildAt(formularioView.findViewById<LinearLayout>(R.id.elementosExtrasContainer).childCount - 1)
                            .findViewById<EditText>(R.id.editElemento).setText(elemento)
                        Log.d("DescricaoAmbientes", "Carregado elemento: $elemento")
                    }

                    it.acessoriosExtras.forEach { acessorio ->
                        adicionarAcessorio(formularioView)
                        formularioView.findViewById<LinearLayout>(R.id.acessoriosExtrasContainer)
                            .getChildAt(formularioView.findViewById<LinearLayout>(R.id.acessoriosExtrasContainer).childCount - 1)
                            .findViewById<EditText>(R.id.editAcessorio).setText(acessorio)
                        Log.d("DescricaoAmbientes", "Carregado acessório: $acessorio")
                    }
                } ?: Log.e("DescricaoAmbientes", "Dados do formulário não são do tipo FormularioData")
            }
        } catch (e: Exception) {
            Log.e("DescricaoAmbientes", "Erro ao carregar formulário: ${e.message}", e)
            // Deletar arquivo corrompido para evitar erros futuros
            if (file.exists()) {
                file.delete()
                Log.d("DescricaoAmbientes", "Arquivo corrompido deletado: ${file.absolutePath}")
                Toast.makeText(this, "Arquivo de dados corrompido foi removido. Tente novamente.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun salvarTudo() {
        try {
            // Salva descrição
            File(getDirAmbiente(), "descricao.txt").writeText(binding.editDescricao.text.toString())
            Log.d("DescricaoAmbientes", "Descrição salva: ${binding.editDescricao.text}")

            // Salva formulário se estiver visível
            if (binding.formularioContainer.visibility == View.VISIBLE && binding.formularioContainer.childCount > 0) {
                salvarFormulario(binding.formularioContainer.getChildAt(0))
            } else {
                Log.d("DescricaoAmbientes", "Formulário não está visível, não salvo")
            }

            Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DescricaoAmbientes", "Erro ao salvar: ${e.message}", e)
            Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarFotosDoAmbiente() {
        val pasta = getDirAmbiente()
        listaCaminhosFotos.clear()

        val arquivos = pasta.listFiles(object : FileFilter {
            override fun accept(file: File): Boolean {
                return file.extension.lowercase() in listOf("jpg", "jpeg", "png")
            }
        })?.sortedBy { it.name }

        arquivos?.forEach { file ->
            listaCaminhosFotos.add(file.absolutePath)
            Log.d("DescricaoAmbientes", "Foto carregada: ${file.absolutePath}")
        }

        fotosAdapter.notifyDataSetChanged()
    }

    private fun carregarDadosSalvos() {
        // Carrega descrição
        File(getDirAmbiente(), "descricao.txt").takeIf { it.exists() }?.let {
            binding.editDescricao.setText(it.readText())
            Log.d("DescricaoAmbientes", "Descrição carregada: ${it.readText()}")
        }
    }

    private fun apagarFotosSelecionadas() {
        fotosSelecionadas.sortedDescending().forEach { index ->
            if (index in listaCaminhosFotos.indices) {
                File(listaCaminhosFotos[index]).delete()
                Log.d("DescricaoAmbientes", "Foto deletada: ${listaCaminhosFotos[index]}")
            }
        }
        fotosSelecionadas.clear()
        modoSelecao = false
        binding.selectionButtonsContainer.visibility = View.GONE
        carregarFotosDoAmbiente()
    }

    private fun abrirImagem(index: Int) {
        listaCaminhosFotos.getOrNull(index)?.let { caminho ->
            File(caminho).takeIf { it.exists() }?.let { arquivo ->
                try {
                    val uri = FileProvider.getUriForFile(
                        this,
                        "$packageName.fileprovider",
                        arquivo
                    )
                    startActivity(Intent(this, FullScreenImageActivity::class.java).apply {
                        putExtra("imageUri", uri.toString())
                    })
                    Log.d("DescricaoAmbientes", "Imagem aberta: $caminho")
                } catch (e: Exception) {
                    Log.e("DescricaoAmbientes", "Erro ao abrir imagem: ${e.message}", e)
                    Toast.makeText(this, "Erro ao abrir imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getDirAmbiente(): File {
        return File(filesDir, "$nomeCliente/$nomeAmbiente").apply { mkdirs() }
    }

    private fun atualizarAdapter() {
        fotosAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        carregarFotosDoAmbiente()
    }
}