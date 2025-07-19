#!/bin/bash
echo "🚀 Forçando rebuild COMPLETO do projeto (limpeza manual de build e cache)..."

# Remove manualmente os diretórios de build
rm -rf app/build
rm -rf ~/.gradle/caches/

# Limpa e rebuilda completamente sem cache
./gradlew clean --no-build-cache --refresh-dependencies
./gradlew app:assembleDebug --no-build-cache --refresh-dependencies

echo "✅ Build finalizado!"

# Caminho do APK gerado
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Verifica se o APK foi realmente gerado
if [ -f "$APK_PATH" ]; then
    echo "✅ APK gerado com sucesso: $APK_PATH"

    # Cria a pasta destino se não existir
    DEST_DIR="./workspace/apk"
    mkdir -p $DEST_DIR

    # Copia o APK para a pasta fácil de acessar
    cp "$APK_PATH" "$DEST_DIR/"

    echo "🚀 APK copiado para $DEST_DIR/"
    echo "👉 Caminho final do APK: $DEST_DIR/app-debug.apk"
else
    echo "❌ APK não encontrado! Verifique erros no build."
fi
