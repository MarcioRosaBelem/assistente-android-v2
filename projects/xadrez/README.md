# VistoriaApp - Android

Aplicativo nativo Android para vistorias imobiliárias profissionais. Desenvolvido com Kotlin e seguindo as melhores práticas de desenvolvimento Android.

## Requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17 ou superior
- Android SDK (compileSdk 34, minSdk 24)
- Dispositivo Android com API 24 (Android 7.0) ou superior

## Funcionalidades

- Captura de fotos diretamente no app
- Organização por ambientes
- Descrições detalhadas para cada ambiente
- Geração de relatórios em PDF
- Histórico de vistorias realizadas
- Personalização de temas e aparência

## Como compilar o projeto

1. Clone o repositório
2. Abra o projeto no Android Studio
3. Aguarde a sincronização do Gradle
4. Execute o app em um dispositivo ou emulador

## Estrutura do projeto

- `/app/src/main/java/com/vistoria/app/` - Código fonte em Kotlin
  - `/models/` - Modelos de dados
  - `/ui/` - Interfaces de usuário (Activities, Fragments)
  - `/utils/` - Utilitários e helpers
  - `/adapters/` - Adaptadores para RecyclerViews
- `/app/src/main/res/` - Recursos do app
  - `/layout/` - Layouts XML
  - `/drawable/` - Imagens e ícones
  - `/values/` - Strings, cores, estilos
- `/app/src/main/AndroidManifest.xml` - Manifesto do app

## Permissões necessárias

- Câmera - Para captura de fotos
- Armazenamento externo - Para salvar fotos e PDFs gerados

## Licença

Este projeto está licenciado sob termos privados. Todos os direitos reservados.