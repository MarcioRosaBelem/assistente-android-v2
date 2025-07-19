package com.vistoriappandroid.Main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.vistoriappandroid.R
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val pdfView = findViewById<PDFView>(R.id.pdfView)
        val btnShare = findViewById<ImageButton>(R.id.btnSharePdf)

        val path = intent.getStringExtra("pdfPath")
        if (path == null || !File(path).exists()) {
            Toast.makeText(this, "Arquivo PDF não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val file = File(path)

        pdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .spacing(10)
            .load()

        btnShare.setOnClickListener {
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Compartilhar PDF via"))
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao compartilhar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}