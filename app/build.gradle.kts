@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    with(libs.plugins) {
        id(application.get().pluginId)
        id(kotlinAndroid.get().pluginId)
    }
}

android {
    namespace = "ru.ekr.zoomimage"
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        applicationId = namespace
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = "${libs.versions.versionName.get()}($versionCode)"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
    }
    composeOptions { kotlinCompilerExtensionVersion = libs.versions.kotlinCompiler.get() }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    with(libs) {
        //    #   Androidx core-ktx
        implementation(androidx.core.ktx)

        //    #   Androidx runtime-ktx
        implementation(androidx.lifecycle)

        //    #   Coil
        implementation(coil.compose)
        implementation(coil.svg)

        //    #   Compose
        implementation(compose.activity)
        implementation(platform(compose.bom))
        implementation(compose.material)
        runtimeOnly(compose.animation)
        runtimeOnly(compose.foundation)
        implementation(compose.ui)
        implementation(compose.ui.graphics)
        implementation(compose.ui.tooling.preview)
        debugImplementation(compose.ui.tooling)
    }
}