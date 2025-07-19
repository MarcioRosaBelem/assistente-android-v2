package com.vistoriappandroid.Main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.vistoriappandroid.R
import java.io.*

class FormularioAmbienteActivity : AppCompatActivity() {

    private lateinit var nomeAmbiente: String
    private lateinit var elementosExtrasContainer: LinearLayout
    private lateinit var acessoriosExtrasContainer: LinearLayout
    private var elementoCounter = 1
    private var acessorioCounter = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.formulario_ambiente)

        nomeAmbiente = intent.getStringExtra("ambiente") ?: "Ambiente"
        val textNomeAmbiente = findViewById<TextView>(R.id.textNomeAmbiente)
        textNomeAmbiente.text = "Ambiente: $nomeAmbiente"

        elementosExtrasContainer = findViewById(R.id.elementosExtrasContainer)
        acessoriosExtrasContainer = findViewById(R.id.acessoriosExtrasContainer)

        val btnAdicionarElemento = findViewById<Button>(R.id.btnAdicionarElemento)
        val btnAdicionarAcessorio = findViewById<Button>(R.id.btnAdicionarAcessorio)

        btnAdicionarElemento.setOnClickListener {
            adicionarElemento()
        }

        btnAdicionarAcessorio.setOnClickListener {
            adicionarAcessorio()
        }

        carregarDadosSalvos()
    }

    private fun adicionarElemento() {
        val elementoView = layoutInflater.inflate(R.layout.item_elemento_adicional, null)
        val label = elementoView.findViewById<TextView>(R.id.labelElemento)
        label.text = "Item Adicional $elementoCounter - Tipo, Cor e Estado:"
        elementoView.tag = "elemento_$elementoCounter"
        elementosExtrasContainer.addView(elementoView)
        elementoCounter++
    }

    private fun adicionarAcessorio() {
        val acessorioView = layoutInflater.inflate(R.layout.item_acessorio_adicional, null)
        val label = acessorioView.findViewById<TextView>(R.id.labelAcessorio)
        label.text = "Acessório Adicional $acessorioCounter - Tipo, Cor e Estado:"
        acessorioView.tag = "acessorio_$acessorioCounter"
        acessoriosExtrasContainer.addView(acessorioView)
        acessorioCounter++
    }

    private fun salvarFormulario() {
        val formularioData = Bundle().apply {
            putString("portaPortao", findViewById<EditText>(R.id.portaPortao).text.toString())
            putString("janela", findViewById<EditText>(R.id.janela).text.toString())
            putString("parede", findViewById<EditText>(R.id.parede).text.toString())
            putString("piso", findViewById<EditText>(R.id.piso).text.toString())
            putString("acessorio1", findViewById<EditText>(R.id.acessorio1).text.toString())
            putString("acessorio2", findViewById<EditText>(R.id.acessorio2).text.toString())
            putString("acessorio3", findViewById<EditText>(R.id.acessorio3).text.toString())

            val elementosExtras = ArrayList<String>()
            for (i in 0 until elementosExtrasContainer.childCount) {
                val view = elementosExtrasContainer.getChildAt(i)
                elementosExtras.add(view.findViewById<EditText>(R.id.editElemento).text.toString())
            }
            putStringArrayList("elementosExtras", elementosExtras)

            val acessoriosExtras = ArrayList<String>()
            for (i in 0 until acessoriosExtrasContainer.childCount) {
                val view = acessoriosExtrasContainer.getChildAt(i)
                acessoriosExtras.add(view.findViewById<EditText>(R.id.editAcessorio).text.toString())
            }
            putStringArrayList("acessoriosExtras", acessoriosExtras)
        }

        saveFormDataToFile(formularioData)

        val intent = Intent().apply {
            putExtra("formularioDados", formularioData)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun saveFormDataToFile(data: Bundle) {
        try {
            val file = File(filesDir, "formulario_$nomeAmbiente.dat")
            ObjectOutputStream(FileOutputStream(file)).use { oos ->
                oos.writeObject(data)
            }
        } catch (e: Exception) {
            Log.e("Formulario", "Erro ao salvar formulário", e)
            Toast.makeText(this, "Erro ao salvar formulário", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarDadosSalvos() {
        try {
            val file = File(filesDir, "formulario_$nomeAmbiente.dat")
            if (file.exists()) {
                val data = ObjectInputStream(FileInputStream(file)).use { ois ->
                    ois.readObject() as Bundle
                }

                findViewById<EditText>(R.id.portaPortao).setText(data.getString("portaPortao", ""))
                findViewById<EditText>(R.id.janela).setText(data.getString("janela", ""))
                findViewById<EditText>(R.id.parede).setText(data.getString("parede", ""))
                findViewById<EditText>(R.id.piso).setText(data.getString("piso", ""))
                findViewById<EditText>(R.id.acessorio1).setText(data.getString("acessorio1", ""))
                findViewById<EditText>(R.id.acessorio2).setText(data.getString("acessorio2", ""))
                findViewById<EditText>(R.id.acessorio3).setText(data.getString("acessorio3", ""))

                // Carregar elementos extras
                data.getStringArrayList("elementosExtras")?.forEach { elemento ->
                    adicionarElemento()
                    elementosExtrasContainer.getChildAt(elementosExtrasContainer.childCount - 1)
                        .findViewById<EditText>(R.id.editElemento).setText(elemento)
                }

                // Carregar acessórios extras
                data.getStringArrayList("acessoriosExtras")?.forEach { acessorio ->
                    adicionarAcessorio()
                    acessoriosExtrasContainer.getChildAt(acessoriosExtrasContainer.childCount - 1)
                        .findViewById<EditText>(R.id.editAcessorio).setText(acessorio)
                }
            }
        } catch (e: Exception) {
            Log.e("Formulario", "Erro ao carregar formulário", e)
        }
    }
}