#!/bin/bash
mkdir -p /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/util
cat > /workspaces/vistoriappandroid-v2/app/src/main/java/com/vistoria/app/util/Util.kt <<INNER_EOF
package com.vistoria.app.util

import android.widget.ImageView
import java.io.File

fun ImageView.loadImageFromPath(path: String) {
    val file = File(path)
    if (file.exists()) {
        setImageURI(android.net.Uri.fromFile(file))
    } else {
        setImageResource(android.R.drawable.ic_menu_gallery) // Fallback
    }
}

fun ImageView.loadImageFromAsset(assetPath: String) {
    try {
        val inputStream = context.assets.open(assetPath)
        val drawable = android.graphics.drawable.Drawable.createFromStream(inputStream, null)
        setImageDrawable(drawable)
        inputStream.close()
    } catch (e: Exception) {
        setImageResource(android.R.drawable.ic_menu_gallery) // Fallback
    }
}
INNER_EOF
