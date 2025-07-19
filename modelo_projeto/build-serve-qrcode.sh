#!/bin/bash

echo "🚀 Limpando APKs antigos de pastas extras..."
find ./output/ -name "app-debug.apk" -exec rm -f {} \;
find ./workspace/apk/ -name "app-debug.apk" -exec rm -f {} \;

echo "🚀 Limpando e buildando APK do projeto correto..."
./gradlew clean assembleDebug --no-build-cache

APK_PATH="./app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo "✅ APK gerado em: $APK_PATH"

    echo "🔗 Gerando QR Code para o link do APK..."
    python3 -c "
import qrcode
qr = qrcode.make('http://localhost:8000/app-debug.apk')
qr.save('apk-qrcode.png')
print('✅ QR Code gerado! Veja o arquivo apk-qrcode.png')
"

    echo "🚀 Iniciando servidor para download do APK e QR Code..."

    cat > serve-apk.py <<SERVE
import http.server
import socketserver
import webbrowser
import threading
import os

PORT = 8000
DIRECTORY = os.path.abspath(".")

print(f"✅ Servindo arquivos do diretório: {DIRECTORY}")

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

def open_browser():
    webbrowser.open(f"http://localhost:{PORT}/apk-qrcode.png")

threading.Timer(1.5, open_browser).start()

with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print(f"🚀 Servindo arquivos em http://localhost:{PORT}/")
    httpd.serve_forever()
SERVE

    python3 serve-apk.py

else
    echo "❌ APK não encontrado. Verifique o build."
fi
