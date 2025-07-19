#!/bin/bash
echo "🧹 Limpando projeto..."
./gradlew clean

echo "🛠️ Compilando APK..."
./gradlew assembleDebug

echo "📦 Movendo APK para pasta de downloads..."
mkdir -p ~/downloads
cp app/build/outputs/apk/debug/app-debug.apk ~/downloads/

echo "✅ APK compilado e disponível em ~/downloads/app-debug.apk"
