#!/bin/bash

echo "🔗 Gerando QR Code para o link do APK..."

python3 -c "
import qrcode
qr = qrcode.make('http://localhost:8000/app-debug.apk')
qr.save('apk-qrcode.png')
print('✅ QR Code gerado! Veja o arquivo apk-qrcode.png')
"
