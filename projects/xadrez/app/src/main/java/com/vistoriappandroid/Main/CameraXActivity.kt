
package com.vistoriappandroid.Main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vistoriappandroid.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var btnCapturar: ImageButton
    private lateinit var btnConfirmar: ImageButton
    private lateinit var btnCancelar: ImageButton
    private lateinit var outputDirectory: File
    private lateinit var nomeAmbiente: String
    private lateinit var nomeCliente: String

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private val fotosSessaoAtual = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x)

        initViews()
        setupCamera()
        setupButtons()
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        btnCapturar = findViewById(R.id.btnCapturar)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun setupCamera() {
        nomeAmbiente = intent.getStringExtra("nomeAmbiente") ?: "Ambiente"
        nomeCliente = intent.getStringExtra("nomeCliente") ?: "SemCliente"
        outputDirectory = File(filesDir, "$nomeCliente/$nomeAmbiente").apply { mkdirs() }
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun setupButtons() {
        btnCapturar.setOnClickListener {
            if ((outputDirectory.listFiles()?.size ?: 0) + fotosSessaoAtual.size < MAX_FOTOS) {
                takePhoto()
            } else {
                Toast.makeText(this, getString(R.string.max_fotos_atingido, MAX_FOTOS), Toast.LENGTH_SHORT).show()
            }
        }

        btnConfirmar.setOnClickListener {
            // Salva todas as fotos capturadas na sessão
            fotosSessaoAtual.forEach { file ->
                val destination = File(outputDirectory, file.name)
                if (!file.renameTo(destination)) {
                    Log.e(TAG, "Falha ao salvar foto: ${file.name}")
                }
            }
            // Navega para DescricaoAmbientesActivity
            val intent = Intent(this, DescricaoAmbientesActivity::class.java).apply {
                putExtra("nomeAmbiente", nomeAmbiente)
                putExtra("nomeCliente", nomeCliente)
            }
            startActivity(intent)
            setResult(RESULT_OK)
            finish()
        }

        btnCancelar.setOnClickListener {
            fotosSessaoAtual.forEach { it.delete() }
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Falha ao iniciar câmera", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File.createTempFile(
            "temp_${System.currentTimeMillis()}",
            ".jpg",
            cacheDir
        )

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(photoFile).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    fotosSessaoAtual.add(photoFile)
                    runOnUiThread {
                        Toast.makeText(this@CameraXActivity, "Foto capturada!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Erro ao capturar foto", exception)
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissão da câmera necessária", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXActivity"
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private const val MAX_FOTOS = 8
    }
}