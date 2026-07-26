import io.github.frankois944.spmForKmp.definition.product.ProductName

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.spmForKmp)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mockative)
}

mockative {

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
    android {
        namespace = "com.ragl.divide.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
        withHostTest { }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
        if (name.startsWith("compileTestKotlin")) {
            dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
        }
    }

    // TODO: Re-enable iOS tests once Mockative supports Kotlin 2.4.x or after migrating to Mokkery
    tasks.matching { it.name.startsWith("compileTestKotlinIos") }.configureEach {
        enabled = false
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.ragl.divide")
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.core.splashscreen)

            implementation(libs.javassist)
            implementation(libs.objenesis)
            implementation(libs.kotlin.reflect)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.material3)
            implementation(libs.material3.window.size)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.components.uiToolingPreview)
            implementation(compose.preview)
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
        commonTest {
            kotlin.srcDir("build/generated/ksp/metadata/commonTest/kotlin")
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.assertk)
                implementation(libs.mockative)
                implementation(kotlin("test-annotations-common"))
                implementation(libs.compose.ui.test)
            }
        }
        val iosArm64Test by getting {
            dependencies {
                implementation(libs.mockative)
            }
        }
        val iosSimulatorArm64Test by getting {
            dependencies {
                implementation(libs.mockative)
            }
        }
    }
}

//dependencies {
//    add("kspAndroid", "io.mockative:mockative-processor:${libs.versions.mockative.get()}")
//    add("kspAndroidHostTest", "io.mockative:mockative-processor:${libs.versions.mockative.get()}")
//    add("kspIosArm64", "io.mockative:mockative-processor:${libs.versions.mockative.get()}")
//    add("kspIosArm64Test", "io.mockative:mockative-processor:${libs.versions.mockative.get()}")
//    add("kspIosSimulatorArm64", "io.mockative:mockative-processor:${libs.versions.mockative.get()}")
//    add("kspIosSimulatorArm64Test", "io.mockative:mockative-processor:${libs.versions.mockative.get()}")
//}


swiftPackageConfig {
    val localDeps = firebaseDeps
    create("cinterop") {
        dependency {
            linkerOpts = listOf("-ObjC")
            remotePackageVersion(
                // Repository URL
                url = uri("https://github.com/firebase/firebase-ios-sdk.git"),
                // Libraries from the package
                products = {
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