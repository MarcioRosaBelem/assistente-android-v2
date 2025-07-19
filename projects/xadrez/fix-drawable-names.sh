#!/bin/bash

# Caminho da pasta drawable
DRAWABLE_DIR="./app/src/main/res/drawable"

echo "🚀 Renomeando arquivos da pasta drawable para o padrão Android (lowercase e underscores)..."

# Verifica se a pasta existe
if [ -d "$DRAWABLE_DIR" ]; then
    cd "$DRAWABLE_DIR" || exit
    for file in *; do
        # Ignora se for diretório
        if [ -f "$file" ]; then
            # Cria novo nome: minúsculas e substitui espaços por underscores
            newfile=$(echo "$file" | tr '[:upper:]' '[:lower:]' | tr ' ' '_' | tr -cd '[:alnum:]_.')
            if [ "$file" != "$newfile" ]; then
                echo "Renomeando: '$file' -> '$newfile'"
                mv "$file" "$newfile"
            fi
        fi
    done
    cd - > /dev/null || exit
    echo "✅ Renomeação concluída!"
else
    echo "❌ Pasta drawable não encontrada!"
fi
