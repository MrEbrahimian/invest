#!/usr/bin/env python3
import os
import shutil
import zipfile

def setup_project():
    source_dir = "./app"
    target_dir = "./vip_bourse_project"
    
    # Clean old run if any
    if os.path.exists(target_dir):
        shutil.rmtree(target_dir)
        
    os.makedirs(target_dir, exist_ok=True)
    
    # 1. Create top-level directories
    print("Creating project directories...")
    os.makedirs(os.path.join(target_dir, "gradle"), exist_ok=True)
    os.makedirs(os.path.join(target_dir, "app"), exist_ok=True)
    os.makedirs(os.path.join(target_dir, "app", "src", "main"), exist_ok=True)
    
    # Target package structure com/vipbourse/app
    java_target_pkg_dir = os.path.join(target_dir, "app", "src", "main", "java", "com", "vipbourse", "app")
    os.makedirs(java_target_pkg_dir, exist_ok=True)
    
    # Copy gradle/libs.versions.toml
    if os.path.exists("./gradle"):
        shutil.copytree("./gradle", os.path.join(target_dir, "gradle"), dirs_exist_ok=True)
        
    # Copy Gradle wrapper/properties config
    for name in ["gradle.properties", "local.properties", "gradlew", "gradlew.bat"]:
        src_path = os.path.join(".", name)
        if os.path.exists(src_path):
            shutil.copy2(src_path, os.path.join(target_dir, name))
            
    # Copy gradle/wrapper folder
    wrapper_src = "./gradle/wrapper"
    if os.path.exists(wrapper_src):
        shutil.copytree(wrapper_src, os.path.join(target_dir, "gradle", "wrapper"), dirs_exist_ok=True)
            
    # Write settings.gradle.kts (project level)
    settings_content = """pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\\\.android.*")
        includeGroupByRegex("com\\\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "VIP Bourse"

include(":app")
"""
    with open(os.path.join(target_dir, "settings.gradle.kts"), "w", encoding="utf-8") as f:
        f.write(settings_content)

    # Write project build.gradle.kts
    project_gradle_content = """// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}
"""
    with open(os.path.join(target_dir, "build.gradle.kts"), "w", encoding="utf-8") as f:
        f.write(project_gradle_content)

    # Write app build.gradle.kts
    app_gradle_content = """plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.vipbourse.app"
  compileSdk { version = 36 }

  defaultConfig {
    applicationId = "com.vipbourse.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.jsoup)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
"""
    with open(os.path.join(target_dir, "app", "build.gradle.kts"), "w", encoding="utf-8") as f:
        f.write(app_gradle_content)
        
    # Copy proguard file
    shutil.copy2("./app/proguard-rules.pro", os.path.join(target_dir, "app", "proguard-rules.pro"))

    # Write clean premium AndroidManifest.xml
    manifest_content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
"""
    with open(os.path.join(target_dir, "app", "src", "main", "AndroidManifest.xml"), "w", encoding="utf-8") as f:
        f.write(manifest_content)

    # 2. Copy and refactor resources values, xml, mimpap, drawables
    print("Migrating and configuring Android resource assets...")
    shutil.copytree("./app/src/main/res", os.path.join(target_dir, "app", "src", "main", "res"), dirs_exist_ok=True)
    
    # Let's adjust strings.xml to show "VIP Bourse" as app name
    strings_xml_path = os.path.join(target_dir, "app", "src", "main", "res", "values", "strings.xml")
    if os.path.exists(strings_xml_path):
        with open(strings_xml_path, "r", encoding="utf-8") as f:
            s_content = f.read()
        
        # Replace template name with VIP Bourse
        s_content = s_content.replace("<string name=\"app_name\">My Application</string>", "<string name=\"app_name\">VIP Bourse</string>")
        with open(strings_xml_path, "w", encoding="utf-8") as f:
            f.write(s_content)

    # 3. Read, migrate & rewrite Kotlin source code with refactored imports & packages
    print("Recalibrating Kotlin files package names and packages imports index space...")
    java_src_root = "./app/src/main/java/com/example"
    
    for root, dirs, files in os.walk(java_src_root):
        for file in files:
            if file.endswith(".kt"):
                full_path = os.path.join(root, file)
                # Compute relative package path
                rel_path = os.path.relpath(root, java_src_root)
                
                # Determine new target sub-directory
                if rel_path == ".":
                    target_sub_dir = java_target_pkg_dir
                else:
                    target_sub_dir = os.path.join(java_target_pkg_dir, rel_path)
                
                os.makedirs(target_sub_dir, exist_ok=True)
                
                with open(full_path, "r", encoding="utf-8") as f:
                    content = f.read()
                    
                # Replace package definitions and imports
                refactored = content.replace("package com.example", "package com.vipbourse.app")
                refactored = refactored.replace("import com.example.", "import com.vipbourse.app.")
                
                # Edge case in MainActivity where layout direction reference or theme uses com.example
                refactored = refactored.replace("com.example.MainActivity", "com.vipbourse.app.MainActivity")
                refactored = refactored.replace("com.example.ui.theme.MyApplicationTheme", "com.vipbourse.app.ui.theme.MyApplicationTheme")
                
                target_file_path = os.path.join(target_sub_dir, file)
                with open(target_file_path, "w", encoding="utf-8") as f:
                    f.write(refactored)
                    
    # Generate unified GitHub Actions file
    github_workflows_dir = os.path.join(target_dir, ".github", "workflows")
    os.makedirs(github_workflows_dir, exist_ok=True)
    github_actions_content = """name: Build VIP Bourse App Android

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant execute permission for gradlew (if wrapper included)
      run: if [ -f "./gradlew" ]; then chmod +x ./gradlew; fi

    - name: Build with Gradle
      run: |
        # If gradlew wrapper isn't committed, use system gradle or install
        if [ -f "./gradlew" ]; then
          ./gradlew assembleDebug
        else
          gradle assembleDebug
        fi
"""
    with open(os.path.join(github_workflows_dir, "android-build.yml"), "w", encoding="utf-8") as f:
        f.write(github_actions_content)

    print("Zipping up finished project archive...")
    zip_path = "./vip_bourse_standard_project.zip"
    if os.path.exists(zip_path):
        os.remove(zip_path)
        
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(target_dir):
            for file in files:
                abs_path = os.path.join(root, file)
                rel_archive_path = os.path.relpath(abs_path, target_dir)
                zipf.write(abs_path, rel_archive_path)
                
    print(f"Project built in standard Android layout: '{zip_path}' ✔️")

if __name__ == "__main__":
    setup_project()
