plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {

    /**
     * Advanced Configuration
     */
    var BACK_BUTTON_QUITS   = true // Use back button to quit the app?
    var SHOW_FPS            = false // Display the FPS monitor?
    var FORCE_CANVAS        = false // Disables fast WebGL rendering when available
    var FORCE_NO_AUDIO      = false // Disables WebAudio

    namespace = "com.julioolivares90.rpgmakerandroidclient"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.julioolivares90.rpgmakerandroidclient"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        all { // 'all' reemplaza a 'each' para iterar sobre los buildTypes
            buildConfigField("boolean", "BACK_BUTTON_QUITS", BACK_BUTTON_QUITS.toString())
            buildConfigField("boolean", "SHOW_FPS", SHOW_FPS.toString())
            buildConfigField("boolean", "FORCE_CANVAS", FORCE_CANVAS.toString())
            buildConfigField("boolean", "FORCE_NO_AUDIO", FORCE_NO_AUDIO.toString())
        }
    }
    flavorDimensions.add("RPGMakerAndroidClient") // O directamente flavorDimensions += "mv_android_client"

    productFlavors {
        create("webview") { // 'create' se usa para definir un nuevo productFlavor
            dimension = "RPGMakerAndroidClient"
            minSdk = 24 // minSdkVersion se convierte a minSdk

            buildConfigField("boolean", "BOOTSTRAP_INTERFACE", "true")
        }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
}