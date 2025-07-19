package com.vistoriappandroid.Main

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.vistoriappandroid.R
import java.io.FileNotFoundException

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView = findViewById<ImageView>(R.id.fullscreenImageView)
        val closeButton = findViewById<ImageButton>(R.id.btnCloseFullScreen)

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        closeButton.setOnClickListener {
            finish()
        }
    }
}
