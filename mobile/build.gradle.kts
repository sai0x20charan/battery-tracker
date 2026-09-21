import com.android.build.api.dsl.Lint
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.utils.property
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.google.services)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.mikepenz.aboutlibrary)
    alias(libs.plugins.google.firebase.crashlytics)
}

val appName = "Battery Tracker"

base {
    archivesName.set("Battery-Tracker-Mobile")
}

android {
    namespace = "com.charan.batterytracker"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.charan.batterytracker"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()

    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = keystoreProperties.getProperty("storeFile")?.let { path ->
                    val f = file(path)
                    if (f.exists()) f else rootProject.file(path)
                }
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            resValue("string", "app_name", appName)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            resValue("string", "app_name", "$appName-Debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
        resValues = true
    }

    hilt { enableAggregatingTask = false }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        disable.add("NullSafeMutableLiveData")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.mobile)

    implementation(libs.firebase.crashlytics)

    implementation(libs.bundles.glance)

    implementation(libs.bundles.retrofit)

    implementation(libs.play.services.wearable)

    implementation(libs.version.tracker.android.library)
    implementation(libs.bundles.hilt.mobile)
    kapt(libs.hilt.compiler)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.aboutlibraries.compose.m3)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.test.android)

    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.bundles.compose.debug)
}
