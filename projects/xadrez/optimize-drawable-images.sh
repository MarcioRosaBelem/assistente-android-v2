#!/bin/bash

DRAWABLE_DIR="./app/src/main/res/drawable"

echo "🚀 Otimizando imagens na pasta drawable..."

# Verifica se a pasta existe
if [ -d "$DRAWABLE_DIR" ]; then
    find "$DRAWABLE_DIR" -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" \) | while read -r image; do
        echo "🖼️ Otimizando $image..."
        # Converter para reduzir qualidade para 85% e diminuir tamanho (JPEG ou PNG)
        convert "$image" -strip -interlace Plane -gaussian-blur 0.05 -quality 85 "$image"
    done
    echo "✅ Otimização concluída!"
else
    echo "❌ Pasta drawable não encontrada!"
fi
