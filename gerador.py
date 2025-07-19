import os
import shutil

BASE_DIR = os.path.abspath(os.path.dirname(__file__))

def gerar_codigo(app_id):
    modelo_path = os.path.join(BASE_DIR, "modelo_base")
    novo_app_path = os.path.join(BASE_DIR, "apps", app_id)
    if not os.path.exists(modelo_path):
        raise Exception("Pasta modelo_base não encontrada.")

    shutil.copytree(modelo_path, novo_app_path, dirs_exist_ok=True)

    descricao_path = os.path.join(novo_app_path, "descricao.txt")
    if os.path.exists(descricao_path):
        with open(descricao_path) as f:
            descricao = f.read()
        print(f"[INFO] Descrição recebida para o app {app_id}: {descricao}")

    print(f"[OK] Código do app {app_id} gerado com base no modelo.")

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print("Uso: python3 gerador.py <app_id>")
        exit(1)
    gerar_codigo(sys.argv[1])
