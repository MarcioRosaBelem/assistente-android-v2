import os
import subprocess
import shutil

def build_apk(projeto_path):
    gradle_path = os.path.abspath(projeto_path)

    # Verifica se o gradlew.bat existe
    gradlew_bat = os.path.join(gradle_path, "gradlew.bat")
    if not os.path.exists(gradlew_bat):
        print("⚠️ gradlew.bat não encontrado. Criando wrapper...")
        comando = ["gradle", "wrapper"]
        try:
            subprocess.run(comando, cwd=gradle_path, check=True)
        except Exception as e:
            print("❌ Erro ao criar wrapper:", e)
            return None

    print("🔧 Iniciando build do APK...")
    comando = ["gradlew.bat", "assembleDebug"]
    try:
        resultado = subprocess.run(comando, cwd=gradle_path, capture_output=True, text=True)
        print(resultado.stdout)
        print(resultado.stderr)

        apk_origem = os.path.join(gradle_path, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
        apk_destino = os.path.join("static", "app-debug.apk")

        if os.path.exists(apk_origem):
            os.makedirs("static", exist_ok=True)
            shutil.copyfile(apk_origem, apk_destino)
            return apk_destino
        else:
            print("❌ APK não encontrado.")
            return None
    except Exception as e:
        print("❌ Erro ao gerar o APK:", e)
        return None
