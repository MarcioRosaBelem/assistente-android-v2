import os
import requests
import json

def aplicar_personalizacao(pasta_projeto, prompt_usuario):
    arquivos_para_editar = []
    for root, _, files in os.walk(pasta_projeto):
        for file in files:
            if file.endswith(('.kt', '.java', '.xml')) and 'build' not in root:
                arquivos_para_editar.append(os.path.join(root, file))

    for caminho in arquivos_para_editar:
        with open(caminho, 'r', encoding='utf-8') as f:
            conteudo_original = f.read()

        resposta = enviar_para_groq(prompt_usuario, conteudo_original)

        if resposta:
            with open(caminho, 'w', encoding='utf-8') as f:
                f.write(resposta)

def enviar_para_groq(prompt, conteudo):
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        print("GROQ_API_KEY não configurada")
        return None

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "mixtral-8x7b-32768",
        "messages": [
            {"role": "system", "content": "Você é um assistente que modifica arquivos de um app Android."},
            {"role": "user", "content": f"Arquivo original:\n{conteudo}\n\nModifique de acordo com o seguinte pedido:\n{prompt}"}
        ]
    }

    try:
        r = requests.post("https://api.groq.com/openai/v1/chat/completions", headers=headers, data=json.dumps(payload))
        r.raise_for_status()
        resposta = r.json()['choices'][0]['message']['content']
        return resposta
    except Exception as e:
        print(f"Erro ao chamar Groq: {e}")
        return None
