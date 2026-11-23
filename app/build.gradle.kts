import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

val backendHost = providers.gradleProperty("backendHost").getOrElse("http://52.6.72.225:9090")
val keystorePropiedades = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.example.bremautomotriz"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.bremautomotriz"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"$backendHost\"")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(
                keystorePropiedades.getProperty("storeFile") ?: "app/signing/brem-release.jks"
            )
            storePassword = keystorePropiedades.getProperty("storePassword").orEmpty()
            keyAlias = keystorePropiedades.getProperty("keyAlias").orEmpty()
            keyPassword = keystorePropiedades.getProperty("keyPassword").orEmpty()
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
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
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

jacoco {
    toolVersion = "0.8.12"
}

val coberturaClases = fileTree(
    layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
) {
    include("com/example/bremautomotriz/ui/viewmodel/**")
    include("com/example/bremautomotriz/data/repository/**")
    include("com/example/bremautomotriz/domain/**")
}

val coberturaEjecucion = files(
    layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"),
    layout.buildDirectory.file("jacoco/testReleaseUnitTest.exec")
)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(coberturaClases)
    executionData.setFrom(provider { coberturaEjecucion.filter { it.exists() } })
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    classDirectories.setFrom(coberturaClases)
    executionData.setFrom(provider { coberturaEjecucion.filter { it.exists() } })
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                minimum = 0.80.toBigDecimal()
            }
        }
    }
}

tasks.register<Copy>("copiarApkRelease") {
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("*.apk")
    }
    into(rootProject.file("app/release"))
    rename { "app-release.apk" }
}

tasks.configureEach {
    if (name == "assembleRelease") {
        finalizedBy("copiarApkRelease")
    }
}
