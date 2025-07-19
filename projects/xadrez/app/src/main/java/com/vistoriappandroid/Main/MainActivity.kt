package com.vistoriappandroid.Main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vistoriappandroid.R
import com.vistoriappandroid.adapter.AmbientesAdapter
import com.vistoriappandroid.model.Ambiente
import com.vistoriappandroid.model.Vistoria
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewAmbientes: RecyclerView
    private lateinit var adapterAmbientes: AmbientesAdapter
    private val ambientes = mutableListOf<Ambiente>()
    private val historico = mutableListOf<Vistoria>()
    private var vistoriaSalvaManualmente = false // Flag para evitar duplicação

    private lateinit var edtNomeCliente: EditText
    private lateinit var edtEndereco: EditText
    private lateinit var edtQtdChaves: EditText
    private lateinit var edtMedidorEnergia: EditText
    private lateinit var edtMedidorAgua: EditText
    private lateinit var editMetodologia: EditText
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnVerMais: TextView
    private lateinit var btnVerMenos: TextView
    private lateinit var layoutCamposOcultos: LinearLayout
    private lateinit var btnNovaVistoria: Button // Declarado como campo da classe
    private lateinit var btnHistorico: Button // Declarado como campo da classe
    private lateinit var btnConfiguracoes: Button // Declarado como campo da classe
    private lateinit var btnFinalizar: Button // Declarado como campo da classe

    private lateinit var sharedPreferences: SharedPreferences

    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.CAMERA
        )
    } else {
        arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )
    }

    private val historicoResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getSerializableExtra("vistoria")?.let { serializableVistoria ->
                val vistoria = serializableVistoria as? Vistoria
                if (vistoria != null) {
                    preencherCamposVistoria(vistoria)
                    Log.d("MainActivity", "Vistoria carregada na MainActivity: ${vistoria.nomeCliente}")
                } else {
                    Log.e("MainActivity", "Vistoria nula ou inválida no resultado do histórico")
                    Toast.makeText(this, "Erro ao carregar vistoria: dados inválidos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val pdfExportResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "PDF gerado com sucesso, salvando vistoria e limpando tela")
            vistoriaSalvaManualmente = true
            salvarVistoria()
            limparParaNovaVistoria()
            vistoriaSalvaManualmente = false
        } else {
            Log.e("MainActivity", "Falha ao gerar PDF")
            Toast.makeText(this, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate iniciado - Antes de super.onCreate")
        try {
            super.onCreate(savedInstanceState)
            Log.d("MainActivity", "super.onCreate executado com sucesso")
            setContentView(R.layout.activity_main)
            Log.d("MainActivity", "setContentView executado")
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao executar onCreate: ${e.message}", e)
            Toast.makeText(this, "Erro ao iniciar MainActivity: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        sharedPreferences = getSharedPreferences("VistoriAppPrefs", MODE_PRIVATE)

        // Limpar diretórios se não houver nome de cliente salvo
        val nomeClienteSalvo = sharedPreferences.getString("ultimoNomeCliente", null)
        if (nomeClienteSalvo.isNullOrEmpty()) {
            val dirs = File(filesDir, "SemCliente").listFiles() ?: emptyArray()
            dirs.forEach { dir ->
                if (dir.isDirectory) {
                    dir.deleteRecursively()
                    Log.d("MainActivity", "Diretório limpo: ${dir.absolutePath}")
                }
            }
        }

        Log.d("MainActivity", "Inicializando views")
        try {
            edtNomeCliente = findViewById(R.id.edtNomeCliente)
            edtEndereco = findViewById(R.id.edtEndereco)
            edtQtdChaves = findViewById(R.id.edtQtdChaves)
            edtMedidorEnergia = findViewById(R.id.edtMedidorEnergia)
            edtMedidorAgua = findViewById(R.id.edtMedidorAgua)
            editMetodologia = findViewById(R.id.editMetodologia)
            spinnerTipo = findViewById(R.id.spinnerTipo)
            btnVerMais = findViewById(R.id.btnVerMais)
            btnVerMenos = findViewById(R.id.btnVerMenos)
            layoutCamposOcultos = findViewById(R.id.layoutCamposOcultos)
            recyclerViewAmbientes = findViewById(R.id.recyclerViewAmbientes)
            btnNovaVistoria = findViewById(R.id.btnNovaVistoria)
            btnHistorico = findViewById(R.id.btnHistorico)
            btnConfiguracoes = findViewById(R.id.btnConfiguracoes)
            btnFinalizar = findViewById(R.id.btnFinalizar)

            btnNovaVistoria.text = "Nova Vistoria2"

            val buttons = listOf(btnNovaVistoria, btnHistorico, btnConfiguracoes, btnFinalizar)
            for (button in buttons) {
                button.setTextColor(android.graphics.Color.WHITE)
                button.paint.apply {
                    setShadowLayer(
                        1f,
                        0f,
                        0f,
                        android.graphics.Color.BLACK
                    )
                }
            }

            val tipoOptions = arrayOf("Tipo", "Captação", "Locação", "Desocupação")
            val tipoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipoOptions)
            tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipo.adapter = tipoAdapter

            btnVerMais.setOnClickListener {
                layoutCamposOcultos.visibility = View.VISIBLE
                btnVerMais.visibility = View.GONE
                btnVerMenos.visibility = View.VISIBLE
            }

            btnVerMenos.setOnClickListener {
                layoutCamposOcultos.visibility = View.GONE
                btnVerMais.visibility = View.VISIBLE
                btnVerMenos.visibility = View.GONE
            }

            btnNovaVistoria.setOnClickListener {
                mostrarNotificacaoNovaVistoria()
            }

            btnHistorico.setOnClickListener {
                val intent = Intent(this, HistoricoActivity::class.java)
                historicoResultLauncher.launch(intent)
            }

            btnFinalizar.setOnClickListener {
                if (allPermissionsGranted()) {
                    val nomeCliente = if (edtNomeCliente.text.isNotBlank()) edtNomeCliente.text.toString() else {
                        Toast.makeText(this, "Por favor, insira o nome do cliente", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val metodologiaInserida = editMetodologia.text.toString().trim().ifEmpty { "Por observação estética" }
                    val tipoSelecionado = if (spinnerTipo.selectedItemPosition > 0) {
                        spinnerTipo.selectedItem.toString()
                    } else {
                        "Captação"
                    }

                    // Coletar dados dos ambientes
                    val formularioDadosPorAmbiente = mutableMapOf<String, String>()
                    val caminhosFotosPorAmbiente = mutableMapOf<String, List<String>>()
                    val nomesAmbientes = listOf("Sala", "Cozinha", "Quarto", "Banheiro", "Telhado", "Garagem", "Cozinha2")
                    nomesAmbientes.forEach { ambienteNome ->
                        val dir = File(filesDir, "$nomeCliente/$ambienteNome")
                        if (dir.exists()) {
                            val formularioFile = File(dir, "formulario_dados.txt")
                            if (formularioFile.exists()) {
                                formularioDadosPorAmbiente[ambienteNome] = formularioFile.readText()
                            }
                            val fotos = dir.listFiles { file ->
                                file.extension.lowercase() in listOf("jpg", "jpeg", "png")
                            }?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
                            caminhosFotosPorAmbiente[ambienteNome] = fotos
                        }
                    }

                    val intent = Intent(this, PdfExportActivity::class.java).apply {
                        putExtra("nomeCliente", nomeCliente)
                        putExtra("endereco", edtEndereco.text.toString())
                        putExtra("qtdChaves", edtQtdChaves.text.toString())
                        putExtra("medidorEnergia", edtMedidorEnergia.text.toString())
                        putExtra("medidorAgua", edtMedidorAgua.text.toString())
                        putExtra("metodologia", metodologiaInserida)
                        putExtra("tipo", tipoSelecionado)
                        putExtra("formularioDados", HashMap(formularioDadosPorAmbiente))
                        putExtra("caminhosFotos", HashMap(caminhosFotosPorAmbiente))
                        putExtra("vistoriaId", historico.size.toString())
                    }
                    pdfExportResultLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Permissões necessárias não concedidas", Toast.LENGTH_LONG).show()
                }
            }

            Log.d("MainActivity", "Views inicializadas com sucesso")
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao inicializar views: ${e.message}", e)
            Toast.makeText(this, "Erro ao inicializar views: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("MainActivity", "Carregando ambientes")
        carregarAmbientes()

        try {
            val spanCount = if (ambientes.size > 1) 2 else 1
            recyclerViewAmbientes.layoutManager = GridLayoutManager(this, spanCount)
            adapterAmbientes = AmbientesAdapter(
                context = this,
                ambientes = ambientes,
                nomeClienteProvider = { edtNomeCliente.text.toString().trim() }
            )
            recyclerViewAmbientes.adapter = adapterAmbientes
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao configurar RecyclerView: ${e.message}", e)
            Toast.makeText(this, "Erro ao configurar RecyclerView: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        carregarHistorico()

        val ultimoIndice = sharedPreferences.getInt("ultimaVistoriaIndice", -1)
        if (ultimoIndice != -1 && ultimoIndice < historico.size) {
            preencherCamposVistoria(historico[ultimoIndice])
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        carregarAmbientes()
        adapterAmbientes.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        val nomeCliente = edtNomeCliente.text.toString().trim()
        if (nomeCliente.isNotEmpty()) {
            val editor = sharedPreferences.edit()
            editor.putString("ultimoNomeCliente", nomeCliente)
            editor.apply()
        }
    }

    private fun mostrarNotificacaoNovaVistoria() {
        if (vistoriaEmAndamento()) {
            AlertDialog.Builder(this)
                .setMessage("Deseja salvar esta vistoria?")
                .setPositiveButton("Sim") { _, _ ->
                    vistoriaSalvaManualmente = true // Marcar como salva manualmente
                    salvarVistoria()
                    limparParaNovaVistoria()
                    vistoriaSalvaManualmente = false // Resetar após limpar
                }
                .setNegativeButton("Não") { _, _ ->
                    limparParaNovaVistoria()
                }
                .show()
        } else {
            limparParaNovaVistoria()
        }
    }

    private fun vistoriaEmAndamento(): Boolean {
        return edtNomeCliente.text.isNotBlank() ||
                edtEndereco.text.isNotBlank() ||
                edtQtdChaves.text.isNotBlank() ||
                edtMedidorEnergia.text.isNotBlank() ||
                edtMedidorAgua.text.isNotBlank() ||
                editMetodologia.text.isNotBlank() ||
                spinnerTipo.selectedItemPosition > 0
    }

    private fun salvarVistoria() {
        val nomeCliente = if (edtNomeCliente.text.isNotBlank()) edtNomeCliente.text.toString() else "SemCliente"
        val formularioDadosPorAmbiente = mutableMapOf<String, String>()
        val caminhosFotosPorAmbiente = mutableMapOf<String, List<String>>()
        val nomesAmbientes = listOf("Sala", "Cozinha", "Quarto", "Banheiro", "Telhado", "Garagem", "Cozinha2")

        val vistoriaId = historico.size.toString()
        val vistoriaDir = File(filesDir, "$nomeCliente/vistoria_$vistoriaId")
        if (!vistoriaDir.exists()) {
            vistoriaDir.mkdirs()
        }

        nomesAmbientes.forEach { ambienteNome ->
            val dir = File(filesDir, "$nomeCliente/$ambienteNome")
            if (dir.exists()) {
                val formularioFile = File(dir, "formulario_dados.txt")
                if (formularioFile.exists()) {
                    formularioDadosPorAmbiente[ambienteNome] = formularioFile.readText()
                }

                val fotos = dir.listFiles { file ->
                    file.extension.lowercase() in listOf("jpg", "jpeg", "png")
                }?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
                caminhosFotosPorAmbiente[ambienteNome] = fotos

                // Remover o diretório original do ambiente após salvar
                if (dir.exists()) {
                    dir.deleteRecursively()
                    Log.d("MainActivity", "Diretório do ambiente removido após salvar: ${dir.absolutePath}")
                }
            }
        }

        val descricaoPorAmbiente = mutableMapOf<String, String>()
        nomesAmbientes.forEach { ambienteNome ->
            val dir = File(filesDir, "$nomeCliente/$ambienteNome")
            if (dir.exists()) {
                val descricaoFile = File(dir, "descricao.txt")
                if (descricaoFile.exists()) {
                    descricaoPorAmbiente[ambienteNome] = descricaoFile.readText()
                    descricaoFile.delete()
                }
            }
        }

        val metodologiaInserida = editMetodologia.text.toString().trim().ifEmpty { "Por observação estética" }
        val vistoria = Vistoria(
            nomeCliente = nomeCliente,
            endereco = edtEndereco.text.toString(),
            qtdChaves = edtQtdChaves.text.toString(),
            medidorEnergia = edtMedidorEnergia.text.toString(),
            medidorAgua = edtMedidorAgua.text.toString(),
            metodologia = metodologiaInserida,
            tipo = if (spinnerTipo.selectedItemPosition > 0) spinnerTipo.selectedItem.toString() else "Captação",
            descricao = "",
            formularioDados = formularioDadosPorAmbiente,
            caminhosFotos = caminhosFotosPorAmbiente,
            vistoriaId = vistoriaId
        )

        // Verificar se já existe uma vistoria com o mesmo nomeCliente e vistoriaId
        val existingIndex = historico.indexOfFirst { it.nomeCliente == nomeCliente && it.vistoriaId == vistoriaId }
        if (existingIndex != -1) {
            historico[existingIndex] = vistoria
            Log.d("MainActivity", "Vistoria atualizada para $nomeCliente (ID: $vistoriaId)")
        } else {
            historico.add(vistoria)
            Log.d("MainActivity", "Nova vistoria adicionada para $nomeCliente (ID: $vistoriaId)")
        }
        salvarHistorico()
    }

    private fun limparParaNovaVistoria() {
        val nomeCliente = edtNomeCliente.text.toString().trim()
        if (nomeCliente.isNotEmpty()) {
            val nomesAmbientes = listOf("Sala", "Cozinha", "Quarto", "Banheiro", "Telhado", "Garagem", "Cozinha2")
            nomesAmbientes.forEach { nome ->
                val dir = File(filesDir, "$nomeCliente/$nome")
                if (dir.exists()) {
                    dir.deleteRecursively()
                    Log.d("MainActivity", "Diretório limpo para nova vistoria: ${dir.absolutePath}")
                }
            }
        }

        edtNomeCliente.text.clear()
        edtEndereco.text.clear()
        edtQtdChaves.text.clear()
        edtMedidorEnergia.text.clear()
        edtMedidorAgua.text.clear()
        editMetodologia.text.clear()
        spinnerTipo.setSelection(0)
        layoutCamposOcultos.visibility = View.GONE
        btnVerMais.visibility = View.VISIBLE
        btnVerMenos.visibility = View.GONE

        val nomes = listOf("Sala", "Cozinha", "Quarto", "Banheiro", "Telhado", "Garagem", "Cozinha2")
        nomes.forEach { nome ->
            val dir = File(filesDir, "SemCliente/$nome")
            if (dir.exists()) {
                dir.deleteRecursively()
                Log.d("MainActivity", "Diretório limpo: ${dir.absolutePath}")
            }
        }
        carregarAmbientes()
        adapterAmbientes.notifyDataSetChanged()

        val editor = sharedPreferences.edit()
        editor.putInt("ultimaVistoriaIndice", -1)
        editor.remove("ultimoNomeCliente")
        editor.apply()
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
                            Log.d("MainActivity", "Histórico carregado: ${historico.size} vistorias")
                            // Limpar diretórios órfãos
                            filesDir.listFiles { file ->
                                file.isDirectory
                            }?.forEach { clienteDir ->
                                clienteDir.listFiles { file ->
                                    file.isDirectory && file.name.startsWith("vistoria_")
                                }?.forEach { vistoriaDir ->
                                    val vistoriaId = vistoriaDir.name.removePrefix("vistoria_")
                                    if (historico.none { it.vistoriaId == vistoriaId }) {
                                        vistoriaDir.deleteRecursively()
                                        Log.d("MainActivity", "Diretório órfão removido: ${vistoriaDir.absolutePath}")
                                    }
                                }
                                // Remover diretório de cliente se vazio
                                if (clienteDir.listFiles()?.isEmpty() == true) {
                                    clienteDir.deleteRecursively()
                                    Log.d("MainActivity", "Diretório de cliente vazio removido: ${clienteDir.absolutePath}")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erro ao carregar histórico: ${e.message}", e)
                // Deletar arquivo de histórico corrompido
                if (historicoFile.exists()) {
                    historicoFile.delete()
                    Log.d("MainActivity", "Arquivo historico.dat corrompido deletado")
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
            Log.d("MainActivity", "Histórico salvo com sucesso: ${historico.size} vistorias")
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao salvar histórico: ${e.message}", e)
        }
    }

    private fun abrirVistoria(vistoria: Vistoria) {
        if (vistoriaEmAndamento()) {
            AlertDialog.Builder(this)
                .setMessage("A vistoria de ${vistoria.nomeCliente} será aberta. Deseja salvar esta vistoria?")
                .setPositiveButton("Sim") { _, _ ->
                    vistoriaSalvaManualmente = true // Marcar como salva manualmente
                    salvarVistoria()
                    preencherCamposVistoria(vistoria)
                    vistoriaSalvaManualmente = false // Resetar após salvar
                }
                .setNegativeButton("Não") { _, _ ->
                    preencherCamposVistoria(vistoria)
                }
                .show()
        } else {
            preencherCamposVistoria(vistoria)
        }
    }

    private fun preencherCamposVistoria(vistoria: Vistoria) {
        edtNomeCliente.setText(vistoria.nomeCliente)
        edtEndereco.setText(vistoria.endereco)
        edtQtdChaves.setText(vistoria.qtdChaves)
        edtMedidorEnergia.setText(vistoria.medidorEnergia)
        edtMedidorAgua.setText(vistoria.medidorAgua)
        editMetodologia.setText(vistoria.metodologia)

        val tipoOptions = arrayOf("Tipo", "Captação", "Locação", "Desocupação")
        val tipoPosition = tipoOptions.indexOf(vistoria.tipo)
        spinnerTipo.setSelection(if (tipoPosition != -1) tipoPosition else 0)

        layoutCamposOcultos.visibility = View.VISIBLE
        btnVerMais.visibility = View.GONE
        btnVerMenos.visibility = View.VISIBLE

        val nomesAmbientes = listOf("Sala", "Cozinha", "Quarto", "Banheiro", "Telhado", "Garagem", "Cozinha2")
        nomesAmbientes.forEach { ambienteNome ->
            val dir = File(filesDir, "${vistoria.nomeCliente}/$ambienteNome")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
            dir.mkdirs()

            vistoria.formularioDados[ambienteNome]?.let { dados ->
                val formularioFile = File(dir, "formulario_dados.txt")
                formularioFile.writeText(dados)
            }

            vistoria.caminhosFotos[ambienteNome]?.let { caminhos ->
                caminhos.forEach { caminho ->
                    val origem = File(caminho)
                    if (origem.exists()) {
                        val destino = File(dir, origem.name)
                        try {
                            origem.copyTo(destino, overwrite = true)
                            Log.d("MainActivity", "Foto copiada: ${origem.absolutePath} -> ${destino.absolutePath}")
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Erro ao copiar foto: ${e.message}", e)
                        }
                    } else {
                        Log.w("MainActivity", "Arquivo de foto não encontrado: ${origem.absolutePath}")
                    }
                }
            }
        }

        carregarAmbientes()
        adapterAmbientes.notifyDataSetChanged()

        val editor = sharedPreferences.edit()
        editor.putInt("ultimaVistoriaIndice", historico.indexOf(vistoria))
        editor.putString("ultimoNomeCliente", vistoria.nomeCliente)
        editor.apply()
    }

    private fun carregarAmbientes() {
        val nomes = listOf("Sala", "Cozinha", "Quarto", "Banheiro", "Telhado", "Garagem", "Cozinha2")
        val imagens = listOf(
            R.drawable.img_sala,
            R.drawable.img_cozinha,
            R.drawable.img_quarto,
            R.drawable.img_banheiro,
            R.drawable.img_telhado,
            R.drawable.img_garagem,
            R.drawable.img_cozinha
        )

        ambientes.clear()

        for (i in nomes.indices) {
            val nome = nomes[i]
            val nomeCliente = sharedPreferences.getString("ultimoNomeCliente", "SemCliente")
            val dir = File(filesDir, "$nomeCliente/$nome")
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val fotos = dir.listFiles { _, name ->
                name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")
            } ?: emptyArray()

            val descricao = File(dir, "descricao.txt")
            val status = if (fotos.isNotEmpty() && descricao.exists() && descricao.readText().isNotBlank()) {
                "Concluído"
            } else {
                "Pendente"
            }

            val ambiente = Ambiente(nome, imagens[i], fotos.size, status)
            ambientes.add(ambiente)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                carregarAmbientes()
                adapterAmbientes.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Permissões necessárias não concedidas", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        carregarAmbientes()
        adapterAmbientes.notifyDataSetChanged()
    }
}