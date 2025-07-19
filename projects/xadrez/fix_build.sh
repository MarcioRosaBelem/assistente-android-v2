#!/bin/bash

echo "Atualizando gradle-wrapper.properties para Gradle 8.10..."
sed -i 's|distributionUrl=.*|distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip|' gradle/wrapper/gradle-wrapper.properties

echo "Atualizando build.gradle do projeto..."
cat << PROJECT_EOF > build.gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
PROJECT_EOF

echo "Atualizando build.gradle do módulo app..."
cat << APP_EOF > app/build.gradle
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'

android {
    compileSdk 34
    namespace 'com.vistoriappandroid'

    defaultConfig {
        applicationId "com.vistoriappandroid"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.camera:camera-core:1.3.4'
    implementation 'androidx.camera:camera-camera2:1.3.4'
    implementation 'androidx.camera:camera-lifecycle:1.3.4'
    implementation 'androidx.camera:camera-view:1.3.4'
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    kapt 'com.github.bumptech.glide:compiler:4.16.0'
    implementation 'androidx.activity:activity-ktx:1.9.2'
    implementation 'androidx.fragment:fragment-ktx:1.8.4'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
}
APP_EOF

echo "Corrigindo activity_descricao_fotos.xml..."
cat << LAYOUT_EOF > app/src/main/res/layout/activity_descricao_fotos.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/layoutDescricao"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    tools:context=".Main.TelaDescricaoFotosActivity">

    <EditText
        android:id="@+id/editDescricao"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Digite a descrição da foto"
        android:minHeight="100dp" />

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:scaleType="centerCrop"
        android:contentDescription="Pré-visualização da foto" />

    <Button
        android:id="@+id/btnSalvarDescricao"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="Salvar Descrição" />

    <LinearLayout
        android:id="@+id/containerFotosDescricao"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical" />
</LinearLayout>
LAYOUT_EOF

echo "Configurando JAVA_HOME..."
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.15.6-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Sincronizando e construindo o projeto..."
./gradlew clean build --refresh-dependencies --stacktrace

echo "Build concluído!"
