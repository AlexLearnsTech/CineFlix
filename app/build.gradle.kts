import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}
val localProperties = Properties().apply {
    val arquivo = rootProject.file("local.properties")

    if (arquivo.exists()) {
        arquivo.inputStream().use {
            load(it)
        }
    }
}

val tmdbApiKey = localProperties.getProperty(
    "TMDB_API_KEY",
    ""
)

android {
    namespace = "com.example.cineflix"
    compileSdk {
        version = release(version = 36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.cineflix"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "TMDB_API_KEY",
            "\"$tmdbApiKey\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

        implementation(libs.androidx.activity.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.core.ktx)
        implementation(libs.material)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(libs.androidx.junit)

        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("io.coil-kt:coil:2.6.0")

}