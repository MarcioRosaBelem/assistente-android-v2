# Dockerfile para rodar emulador Android com GUI no GitHub Codespaces

# Imagem base
FROM ubuntu:22.04

# Evita prompts durante instalação
ENV DEBIAN_FRONTEND=noninteractive

# Instalar pacotes essenciais
RUN apt-get update && apt-get install -y \
    curl git unzip wget zip \
    libglu1-mesa openjdk-17-jdk \
    qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils virtinst \
    libvirt-daemon qemu-utils \
    xfce4 xfce4-goodies tightvncserver novnc websockify \
    && rm -rf /var/lib/apt/lists/*

# Variáveis do Android SDK
ENV ANDROID_HOME=/android
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator

# Baixar Android command line tools
RUN mkdir -p /android && cd /android && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip && \
    unzip cmdline-tools.zip && rm cmdline-tools.zip && \
    mkdir -p /android/cmdline-tools/latest && \
    mv cmdline-tools/* /android/cmdline-tools/latest/

# Aceitar licenças e instalar SDKs
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "platforms;android-30" "emulator" "system-images;android-30;google_apis;x86_64"

# Criar AVD padrão (em tempo de container)
CMD echo "no" | avdmanager create avd -n test_avd -k "system-images;android-30;google_apis;x86_64" && \
    tightvncserver :1 && \
    websockify --web=/usr/share/novnc/ 6080 localhost:5901 & \
    DISPLAY=:1 emulator -avd test_avd -noaudio -no-boot-anim -no-snapshot -accel off
