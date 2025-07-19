from flask import Flask, request, render_template, jsonify, send_from_directory
import os
import subprocess
import shutil
from datetime import datetime
from dotenv import load_dotenv
import requests

load_dotenv()

app = Flask(__name__)
PROJECTS_DIR = "projects"
APK_OUTPUT_DIR = "static/apks"
LOGS_DIR = "logs"

MODELO_BASE_REPO = os.getenv("MODELO_BASE_REPO", "https://github.com/MarcioRosa-QuickQuote/modeloapp.git")
GIT_TOKEN = os.getenv("GIT_TOKEN", "")
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")

os.makedirs(PROJECTS_DIR, exist_ok=True)
os.makedirs(APK_OUTPUT_DIR, exist_ok=True)
os.makedirs(LOGS_DIR, exist_ok=True)

def log(msg, projeto="geral"):
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    with open(f"{LOGS_DIR}/{projeto}.txt", "a", encoding="utf-8") as f:
        f.write(f"[{timestamp}] {msg}\n")

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/logs/<projeto>')
def logs(projeto):
    path = os.path.join(LOGS_DIR, f"{projeto}.txt")
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            return "<pre>" + f.read() + "</pre>"
    return "Nenhum log encontrado."

@app.route('/projetos', methods=['GET'])
def listar_projetos():
    return jsonify(os.listdir(PROJECTS_DIR))

@app.route('/salvar', methods=['POST'])
def salvar_projeto():
    data = request.get_json()
    nome = data.get('nome_projeto', '').strip()
    if not nome:
        return jsonify({"mensagem": "Nome inválido."}), 400

    destino = os.path.join(PROJECTS_DIR, nome)
    if not os.path.exists(destino):
        repo_url = MODELO_BASE_REPO
        if "github.com" in repo_url and GIT_TOKEN:
            repo_url = repo_url.replace("https://", f"https://{GIT_TOKEN}@")
        log(f"Clonando repositório {repo_url} para {destino}", nome)
        subprocess.run(["git", "clone", repo_url, destino])

    return jsonify({"mensagem": f"Projeto '{nome}' salvo com sucesso!"})

def aplicar_prompt_groq(nome, prompt):
    log(f"Aplicando prompt via Groq: {prompt}", nome)
    response = requests.post(
        "https://api.groq.com/openai/v1/chat/completions",
        headers={
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type": "application/json"
        },
        json={
            "model": "llama3-70b-8192",
            "messages": [
                {"role": "system", "content": "Você é um assistente que edita projetos Android nativos."},
                {"role": "user", "content": f"Projeto Android em: /projects/{nome}. Modifique esse projeto conforme esse pedido: {prompt}"}
            ]
        }
    )
    if response.status_code == 200:
        resposta = response.json()["choices"][0]["message"]["content"]
        log("Resposta do Groq:\n" + resposta, nome)
        return resposta
    else:
        erro = f"Erro na chamada da API Groq: {response.text}"
        log(erro, nome)
        return erro

@app.route('/gerar', methods=['POST'])
def gerar_apk():
    data = request.get_json()
    nome_projeto = data.get("nome_projeto", "").strip()
    prompt = data.get("prompt", "").strip()

    if not nome_projeto or not prompt:
        return jsonify({"mensagem": "Nome do projeto e prompt são obrigatórios."}), 400

    caminho_projeto = os.path.join(PROJECTS_DIR, nome_projeto)
    if not os.path.exists(caminho_projeto):
        return jsonify({"mensagem": "Projeto não encontrado."}), 404

    log(f"Geração iniciada para '{nome_projeto}' com prompt: {prompt}", nome_projeto)
    aplicar_prompt_groq(nome_projeto, prompt)

    for tentativa in range(5):
        log(f"Tentativa {tentativa+1}: Build real", nome_projeto)
        comando = ["cmd", "/c", "gradlew.bat", "clean", "assembleDebug"]
        try:
            resultado = subprocess.run(comando, cwd=caminho_projeto, capture_output=True, text=True, timeout=300)
            log(f"STDOUT:\n{resultado.stdout}", nome_projeto)
            log(f"STDERR:\n{resultado.stderr}", nome_projeto)

            if resultado.returncode == 0:
                apk_origem = os.path.join(caminho_projeto, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
                apk_destino = os.path.join(APK_OUTPUT_DIR, f"{nome_projeto}.apk")
                if os.path.exists(apk_origem):
                    shutil.copy(apk_origem, apk_destino)
                    log(f"APK gerado com sucesso em {apk_destino}", nome_projeto)
                    return jsonify({"mensagem": f"APK gerado com sucesso!", "apk": f"/apk-download/{nome_projeto}.apk"})
            else:
                log("Falha no build. Tentando corrigir com novo prompt.", nome_projeto)
                aplicar_prompt_groq(nome_projeto, f"Corrija o erro abaixo no projeto Android:\n{resultado.stderr}")
        except subprocess.TimeoutExpired:
            log("Tempo limite excedido ao gerar o APK.", nome_projeto)
            return jsonify({"mensagem": "Tempo limite excedido ao gerar o APK."}), 500

    return jsonify({"mensagem": "Falha ao gerar o APK após múltiplas tentativas."}), 500

@app.route('/apk-download/<nome_apk>')
def baixar_apk(nome_apk):
    return send_from_directory(APK_OUTPUT_DIR, nome_apk, as_attachment=True)

if __name__ == "__main__":
    app.run(debug=True)
