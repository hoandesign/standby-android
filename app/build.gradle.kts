import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

val keystorePropertiesFile = listOf(
  file("keystore.properties"),
  file("${System.getProperty("user.home")}/Documents/Projects/my-moves-signing/keystore.properties")
).firstOrNull { it.exists() && it.canRead() }

val keystoreProperties = Properties().apply {
  if (keystorePropertiesFile != null) {
    runCatching {
      keystorePropertiesFile.inputStream().use { load(it) }
    }
  }
}

android {
  namespace = "com.hoandesign.standby"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.hoandesign.standby"
    minSdk = 26
    targetSdk = 36
    versionCode = 5
    versionName = "1.0.4"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val storePath = keystoreProperties.getProperty("storeFile")
      if (!storePath.isNullOrBlank() && file(storePath).exists()) {
        storeFile = file(storePath)
        storePassword = keystoreProperties.getProperty("storePassword")
        keyAlias = keystoreProperties.getProperty("keyAlias")
        keyPassword = keystoreProperties.getProperty("keyPassword")
      } else {
        val debugKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
        if (debugKeystore.exists()) {
          storeFile = debugKeystore
          storePassword = "android"
          keyAlias = "androiddebugkey"
          keyPassword = "android"
        }
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      val releaseSigning = signingConfigs.getByName("release")
      signingConfig = if (releaseSigning.storeFile != null) {
        releaseSigning
      } else {
        signingConfigs.getByName("debug")
      }
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.play.services.location)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
