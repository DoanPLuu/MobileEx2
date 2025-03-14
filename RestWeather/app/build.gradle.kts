import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

val WEATHER_API_KEY: String = localProperties.getProperty("WEATHER_API_KEY", "")

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.restweather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.restweather"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEATHER_API_KEY", "\"$WEATHER_API_KEY\"")


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Optional: OkHttp for logging (useful for debugging)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Optional: RxJava adapter if you're using reactive programming
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
