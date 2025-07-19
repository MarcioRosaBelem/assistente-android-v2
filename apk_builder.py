import os
import subprocess
import shutil

BASE_DIR = os.path.abspath(os.path.dirname(__file__))
STATIC_DIR = os.path.join(BASE_DIR, "static")

def build_apk(app_id):
    app_path = os.path.join(BASE_DIR, "apps", app_id)
    if not os.path.exists(app_path):
        raise Exception("App não encontrado.")

    gradlew_path = os.path.join(app_path, "gradlew.bat")
    if not os.path.exists(gradlew_path):
        raise Exception("gradlew.bat não encontrado no projeto.")

    print(f"[BUILD] Gerando APK para {app_id}...")
    subprocess.run(["cmd", "/c", "gradlew.bat", "assembleDebug"], cwd=app_path, check=True)

    apk_origem = os.path.join(app_path, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    apk_destino = os.path.join(STATIC_DIR, f"{app_id}.apk")

    os.makedirs(STATIC_DIR, exist_ok=True)
    shutil.copy2(apk_origem, apk_destino)

    print(f"[OK] APK gerado: {apk_destino}")

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print("Uso: python3 apk_builder.py <app_id>")
        exit(1)
    build_apk(sys.argv[1])
