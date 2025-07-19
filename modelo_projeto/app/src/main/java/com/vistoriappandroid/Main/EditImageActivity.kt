package com.vistoriappandroid.Main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vistoriappandroid.R
import java.io.File
import java.io.FileOutputStream

class EditImageActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnRotateLeft: Button
    private lateinit var btnRotateRight: Button
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var btnSave: Button
    private lateinit var imagePath: String
    private lateinit var originalBitmap: Bitmap
    private var rotationAngle = 0f
    private var brightness = 0f
    private var contrast = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)
        Log.d("EditImageActivity", "Layout definido: activity_edit_image")

        imageView = findViewById(R.id.imageViewFullScreen)
        btnRotateLeft = findViewById(R.id.btnRotateLeft)
        btnRotateRight = findViewById(R.id.btnRotateRight)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarContrast = findViewById(R.id.seekBarContrast)
        btnSave = findViewById(R.id.btnSave)
        Log.d("EditImageActivity", "Views inicializadas")

        imagePath = intent.getStringExtra("imagePath") ?: ""
        if (imagePath.isEmpty()) {
            Log.e("EditImageActivity", "Caminho da imagem vazio")
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        originalBitmap = BitmapFactory.decodeFile(imagePath)
        if (originalBitmap == null) {
            Log.e("EditImageActivity", "Bitmap não carregado: $imagePath")
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        imageView.setImageBitmap(originalBitmap)
        Log.d("EditImageActivity", "Imagem carregada: $imagePath")

        // Rotação à esquerda
        btnRotateLeft.setOnClickListener {
            rotationAngle -= 90f
            if (rotationAngle < 0) rotationAngle += 360f
            updateImage()
            Log.d("EditImageActivity", "Rotação à esquerda: $rotationAngle")
        }

        // Rotação à direita
        btnRotateRight.setOnClickListener {
            rotationAngle += 90f
            if (rotationAngle >= 360f) rotationAngle -= 360f
            updateImage()
            Log.d("EditImageActivity", "Rotação à direita: $rotationAngle")
        }

        // Ajuste de brilho
        seekBarBrightness.max = 200
        seekBarBrightness.progress = 100 // Centro (sem alteração)
        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightness = (progress - 100) / 100f * 255f // Varia de -255 a 255
                updateImage()
                Log.d("EditImageActivity", "Brilho ajustado: $brightness")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Ajuste de contraste
        seekBarContrast.max = 200
        seekBarContrast.progress = 100 // Centro (sem alteração)
        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrast = (progress / 100f) * 2f // Varia de 0 a 4
                updateImage()
                Log.d("EditImageActivity", "Contraste ajustado: $contrast")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Salvar alterações
        btnSave.setOnClickListener {
            val editedBitmap = createEditedBitmap()
            val file = File(imagePath)
            try {
                FileOutputStream(file).use { out ->
                    editedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                Toast.makeText(this, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK, Intent().putExtra("imagePath", imagePath))
                finish()
                Log.d("EditImageActivity", "Imagem salva: $imagePath")
            } catch (e: Exception) {
                Log.e("EditImageActivity", "Erro ao salvar imagem: ${e.message}", e)
                Toast.makeText(this, "Erro ao salvar imagem: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateImage() {
        val editedBitmap = createEditedBitmap()
        imageView.setImageBitmap(editedBitmap)
    }

    private fun createEditedBitmap(): Bitmap {
        // Criar uma matriz para aplicar brilho e contraste
        val colorMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        val colorFilter = ColorMatrixColorFilter(colorMatrix)

        // Criar um bitmap rotacionado
        val matrix = android.graphics.Matrix().apply {
            postRotate(rotationAngle)
        }
        val rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        // Aplicar brilho e contraste ao bitmap rotacionado
        val editedBitmap = Bitmap.createBitmap(rotatedBitmap.width, rotatedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(editedBitmap)
        val paint = android.graphics.Paint()  // Removido apply para evitar confusão
        paint.colorFilter = colorFilter  // Atribuição direta
        canvas.drawBitmap(rotatedBitmap, 0f, 0f, paint)

        return editedBitmap
    }
}