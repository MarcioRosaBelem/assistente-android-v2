#!/bin/bash

echo "🚀 Limpando APKs antigos de pastas extras..."
find ./output/ -name "app-debug.apk" -exec rm -f {} \;
find ./workspace/apk/ -name "app-debug.apk" -exec rm -f {} \;

echo "🚀 Limpando e buildando APK do projeto correto..."
./gradlew clean assembleDebug --no-build-cache

APK_PATH="./app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo "✅ APK gerado em: $APK_PATH"

    echo "🚀 Iniciando servidor para download do APK correto..."

    cat > serve-apk.py <<SERVE
import http.server
import socketserver
import webbrowser
import threading
import os

PORT = 8000
APK_DIRECTORY = os.path.abspath("./app/build/outputs/apk/debug")

print(f"✅ Servindo APK do diretório: {APK_DIRECTORY}")

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=APK_DIRECTORY, **kwargs)

def open_browser():
    webbrowser.open(f"http://localhost:{PORT}/app-debug.apk")

threading.Timer(1.5, open_browser).start()

with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print(f"🚀 Servindo APK atualizado em http://localhost:{PORT}/app-debug.apk")
    httpd.serve_forever()
SERVE

    python3 serve-apk.py

else
    echo "❌ APK não encontrado. Verifique o build."
fi
