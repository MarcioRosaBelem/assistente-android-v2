package com.vistoriappandroid.Main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.kernel.geom.PageSize
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class PdfExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PdfExportActivity", "onCreate iniciado")

        try {
            // Extrair dados do intent
            val nomeCliente = intent.getStringExtra("nomeCliente")?.takeIf { it.isNotBlank() } ?: "SemCliente"
            val endereco = intent.getStringExtra("endereco")?.takeIf { it.isNotBlank() } ?: "Não informado"
            val qtdChaves = intent.getStringExtra("qtdChaves")?.takeIf { it.isNotBlank() } ?: "Não informado"
            val medidorEnergia = intent.getStringExtra("medidorEnergia")?.takeIf { it.isNotBlank() } ?: "Não informado"
            val medidorAgua = intent.getStringExtra("medidorAgua")?.takeIf { it.isNotBlank() } ?: "Não informado"
            val metodologia = intent.getStringExtra("metodologia")?.takeIf { it.isNotBlank() } ?: "Por observação estética"
            val tipo = intent.getStringExtra("tipo")?.takeIf { it.isNotBlank() } ?: "Captação"
            val formularioDados = intent.getSerializableExtra("formularioDados") as? HashMap<String, String> ?: HashMap()
            val caminhosFotos = intent.getSerializableExtra("caminhosFotos") as? HashMap<String, List<String>> ?: HashMap()
            val vistoriaId = intent.getStringExtra("vistoriaId")?.takeIf { it.isNotBlank() } ?: "0"

            Log.d("PdfExportActivity", "Dados recebidos: nomeCliente=$nomeCliente, endereco=$endereco, qtdChaves=$qtdChaves, " +
                    "medidorEnergia=$medidorEnergia, medidorAgua=$medidorAgua, metodologia=$metodologia, tipo=$tipo")
            Log.d("PdfExportActivity", "Formulários: $formularioDados")
            Log.d("PdfExportActivity", "Fotos: $caminhosFotos")

            // Criar diretório para PDFs
            val pdfDir = File(filesDir, "pdfs")
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }

            // Gerar PDF
            val pdfFile = File(pdfDir, "${nomeCliente}_vistoria_$vistoriaId.pdf")
            try {
                val writer = PdfWriter(FileOutputStream(pdfFile))
                val pdf = PdfDocument(writer)
                val document = Document(pdf, PageSize.A4)
                document.setMargins(20f, 20f, 20f, 20f)
                val pageWidth = PageSize.A4.width - 40f
                val pageHeight = PageSize.A4.height - 40f
                val padding = 2f
                val maxPhotosPerPage = 4
                val maxPhotosPerAmbiente = 8

                // Página 1: Dados do cliente
                var y = 20f
                val maxY = pageHeight
                document.add(Paragraph("Dados do Cliente")
                    .setFontSize(24f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(y))
                y += 30f
                document.add(Paragraph("Nome: $nomeCliente")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 15f
                document.add(Paragraph("Endereço: $endereco")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 15f
                document.add(Paragraph("Chaves Recebidas: $qtdChaves")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 15f
                document.add(Paragraph("Medidor de Energia: $medidorEnergia")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 15f
                document.add(Paragraph("Medidor de Água: $medidorAgua")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 15f
                document.add(Paragraph("Metodologia: $metodologia")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 15f
                document.add(Paragraph("Tipo: $tipo")
                    .setFontSize(12f)
                    .setMarginLeft(20f)
                    .setMarginTop(y))
                y += 20f

                // Adicionar logomarca (se fornecida)
                val logoFile = File(filesDir, "logo.png")
                if (logoFile.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                        if (bitmap == null) {
                            Log.e("PdfExportActivity", "Falha ao carregar bitmap para logomarca")
                        } else {
                            val imageData = ImageDataFactory.create(logoFile.absolutePath)
                            val image = Image(imageData)
                            val logoSize = 50f
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height
                            val logoWidth = if (aspectRatio > 1) logoSize else logoSize * aspectRatio
                            val logoHeight = if (aspectRatio > 1) logoSize / aspectRatio else logoSize
                            image.scaleToFit(logoWidth, logoHeight)
                            image.setFixedPosition(pageWidth - logoWidth, 20f)
                            document.add(image)
                        }
                    } catch (e: Exception) {
                        Log.e("PdfExportActivity", "Erro ao adicionar logomarca: ${e.message}", e)
                    }
                } else {
                    Log.d("PdfExportActivity", "Logomarca não encontrada em ${logoFile.absolutePath}")
                }

                // Páginas de fotos e formulários por ambiente
                val ambientesValidos = caminhosFotos.keys.union(formularioDados.keys.filter { formularioDados[it]?.isNotBlank() == true })
                ambientesValidos.forEach { ambiente ->
                    // Páginas de fotos (página 2 e seguintes, até 8 por ambiente)
                    caminhosFotos[ambiente]?.let { fotos ->
                        if (fotos.isNotEmpty()) {
                            Log.d("PdfExportActivity", "Processando ${fotos.size} fotos para o ambiente: $ambiente")
                            val fotosLimitadas = fotos.take(maxPhotosPerAmbiente)
                            var i = 0
                            while (i < fotosLimitadas.size) {
                                pdf.addNewPage()
                                y = 30f // Aumentado para evitar sobreposição
                                document.add(Paragraph("Fotos do Ambiente: $ambiente" + if (i > 0) " (Continuação)" else "")
                                    .setFontSize(16f)
                                    .setBold()
                                    .setMarginLeft(20f)
                                    .setMarginTop(y))
                                y += 30f

                                val grupo = fotosLimitadas.subList(i, min(i + maxPhotosPerPage, fotosLimitadas.size))
                                val availableHeight = pageHeight - 30f - padding - 46f
                                val imgHeight = availableHeight / 2
                                val imgWidth = (pageWidth - padding) / 2
                                val positions = listOf(
                                    floatArrayOf(20f, 30f + 46f),
                                    floatArrayOf(20f + imgWidth + padding, 30f + 46f),
                                    floatArrayOf(20f, 30f + 46f + imgHeight + padding),
                                    floatArrayOf(20f + imgWidth + padding, 30f + 46f + imgHeight + padding)
                                )

                                grupo.forEachIndexed { j, caminho ->
                                    val fotoFile = File(caminho)
                                    Log.d("PdfExportActivity", "Tentando carregar foto: $caminho")
                                    if (fotoFile.exists()) {
                                        try {
                                            val bitmap = BitmapFactory.decodeFile(caminho)
                                            if (bitmap == null) {
                                                Log.e("PdfExportActivity", "Falha ao carregar bitmap para $caminho")
                                                return@forEachIndexed
                                            }
                                            val imageData = ImageDataFactory.create(caminho)
                                            val image = Image(imageData)
                                            val imageAspectRatio = bitmap.width.toFloat() / bitmap.height
                                            val slotAspectRatio = imgWidth / imgHeight
                                            var finalWidth = imgWidth
                                            var finalHeight = imgHeight
                                            if (imageAspectRatio > slotAspectRatio) {
                                                finalWidth = imgWidth
                                                finalHeight = imgWidth * (bitmap.height.toFloat() / bitmap.width)
                                            } else {
                                                finalHeight = imgHeight
                                                finalWidth = imgHeight * (bitmap.width.toFloat() / bitmap.height)
                                            }
                                            val xOffset = (imgWidth - finalWidth) / 2
                                            val yOffset = (imgHeight - finalHeight) / 2
                                            val imageX = positions[j][0] + xOffset
                                            val imageY = pageHeight - positions[j][1] - yOffset - finalHeight
                                            image.scaleToFit(finalWidth, finalHeight)
                                            image.setFixedPosition(imageX, imageY)
                                            document.add(image)
                                            Log.d("PdfExportActivity", "Foto adicionada: $caminho")
                                        } catch (e: Exception) {
                                            Log.e("PdfExportActivity", "Erro ao adicionar foto $caminho: ${e.message}", e)
                                        }
                                    } else {
                                        Log.w("PdfExportActivity", "Foto não encontrada: $caminho")
                                    }
                                }
                                i += maxPhotosPerPage
                            }
                        }
                    }

                    // Página do formulário (apenas se preenchido)
                    formularioDados[ambiente]?.let { formulario ->
                        val linhas = formulario.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                        Log.d("PdfExportActivity", "Formulário bruto para $ambiente: '$formulario'")
                        if (linhas.isNotEmpty()) {
                            Log.d("PdfExportActivity", "Adicionando formulário para $ambiente com ${linhas.size} linhas")
                            pdf.addNewPage()
                            y = 20f
                            document.add(Paragraph("Formulário do Ambiente: $ambiente")
                                .setFontSize(16f)
                                .setBold()
                                .setMarginLeft(20f)
                                .setMarginTop(y))
                            y += 10f

                            linhas.forEachIndexed { index, linha ->
                                Log.d("PdfExportActivity", "Linha $index do formulário $ambiente: $linha")
                                if (y + 15 > maxY) {
                                    pdf.addNewPage()
                                    y = 20f
                                }
                                document.add(Paragraph(linha)
                                    .setFontSize(10f)
                                    .setMarginLeft(20f)
                                    .setMarginTop(y))
                                y += 15f
                            }
                        } else {
                            Log.d("PdfExportActivity", "Formulário vazio para o ambiente: $ambiente, não será exibido")
                        }
                    } ?: Log.d("PdfExportActivity", "Nenhum formulário encontrado para o ambiente: $ambiente")
                }

                document.close()
                Log.d("PdfExportActivity", "PDF gerado com sucesso: ${pdfFile.absolutePath}")

                // Abrir PdfViewerActivity
                val intent = Intent(this, PdfViewerActivity::class.java).apply {
                    putExtra("pdfPath", pdfFile.absolutePath)
                }
                startActivity(intent)

                setResult(RESULT_OK)
            } catch (e: Exception) {
                Log.e("PdfExportActivity", "Erro ao gerar PDF: ${e.message}", e)
                Toast.makeText(this, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
            }

            finish()
        } catch (e: Exception) {
            Log.e("PdfExportActivity", "Erro ao executar onCreate: ${e.message}", e)
            Toast.makeText(this, "Erro ao iniciar PdfExportActivity: ${e.message}", Toast.LENGTH_LONG).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}