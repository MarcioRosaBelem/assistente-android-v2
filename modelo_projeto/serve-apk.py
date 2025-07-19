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
