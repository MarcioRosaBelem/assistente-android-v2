import os

def gerar_projeto_android(prompt, nome_projeto):
    nome_projeto = nome_projeto.strip().replace(" ", "_")
    pasta_projeto = f"projetos/{nome_projeto}"
    os.makedirs(pasta_projeto, exist_ok=True)

    arquivo_kt = os.path.join(pasta_projeto, "MainActivity.kt")
    with open(arquivo_kt, "w", encoding="utf-8") as f:
        f.write(f'''
package com.example.{nome_projeto.lower()}

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {{
    override fun onCreate(savedInstanceState: Bundle?) {{
        super.onCreate(savedInstanceState)
        println("App gerado para: {prompt}")
    }}
}}
''')

    with open(os.path.join(pasta_projeto, "build.gradle"), "w", encoding="utf-8") as f:
        f.write("apply plugin: 'com.android.application'\n")

    print(f"✅ Projeto gerado em: {pasta_projeto}")
    return pasta_projeto
