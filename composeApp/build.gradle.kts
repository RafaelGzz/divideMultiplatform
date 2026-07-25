@file:OptIn(ExperimentalComposeLibrary::class)

import io.github.frankois944.spmForKmp.definition.product.ProductName
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("org.jetbrains.kotlinx.kover")
    id("io.mockative") version "3.0.1"
    id("com.google.devtools.ksp")
    id("io.github.frankois944.spmForKmp")
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        load(FileInputStream(file))
    }
}

val firebaseDeps =
    listOf(
        ProductName("FirebaseCore"),
        ProductName("FirebaseAnalytics"),
        ProductName("FirebaseCrashlytics"),
        ProductName("FirebaseAuth"),
        ProductName("FirebaseDatabase"),
        ProductName("FirebasePerformance"),
        ProductName("FirebaseStorage"),
    )

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions{
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.ragl.divide")
        }
        iosTarget.compilations{
            val main by getting {
                cinterops.create("cinterop")
            }
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.core.splashscreen)

            implementation("org.javassist:javassist:3.29.2-GA")
            implementation("org.objenesis:objenesis:3.3")
            implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlin.coreLibrariesVersion}")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(libs.material3)
            implementation(libs.material3.window.size)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.material.icons.core)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.annotations)

            implementation(libs.firebase.auth)
            implementation(libs.firebase.database)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.performance)

            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.firebase)
            implementation(libs.kmpauth.uihelper)
            implementation(libs.composeIcons.fontAwesome)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)

            implementation(libs.kotlinx.serialization.json)

            api(libs.androidx.datastore)
            api(libs.androidx.datastore.preferences)

            implementation(libs.landscapist.coil3)

            implementation(libs.mockative)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(kotlin("test-annotations-common"))
            implementation(compose.uiTest)
        }
    }
}

android {
    namespace = "com.ragl.divide"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ragl.divide"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1114
        versionName = "1.1.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            // Solo configurar signing si las propiedades existen
            if (keystoreProperties.containsKey("storeFile") && keystoreProperties["storeFile"] != null) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Solo usar signingConfig si está configurado
            if (keystoreProperties.containsKey("storeFile") && keystoreProperties["storeFile"] != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}

swiftPackageConfig{
    val localDeps = firebaseDeps
    create("cinterop"){
        dependency {
            linkerOpts = listOf("-ObjC")
            remotePackageVersion(
                // Repository URL
                url = uri("https://github.com/firebase/firebase-ios-sdk.git"),
                // Libraries from the package
                products = {
                    // Export to Kotlin for use in shared Kotlin code and use it in your swift code
                    // the export doesn't work when gitlive is implemented, my guess is a bug with cinterop
                    // because gitlive already use cinterop
                    localDeps.forEach { add(it, exportToKotlin = false) }
                },
                // Package version
                version = "11.6.0",
            )
            remotePackageVersion(
                url = uri("https://github.com/google/GoogleSignIn-iOS"),
                products = {
                    add("GoogleSignIn")
                },
                version = "9.0.0"
            )
        }
    }
}