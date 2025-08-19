plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"
    id("kotlin-kapt")
    id("kotlinx-serialization")
}

android {
    namespace = "com.example.rahmatmas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rahmatmas"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ADMIN_USERNAME",
            "\"adminrahmat\""
        )

        buildConfigField(
            "String",
            "ADMIN_PASSWORD",
            "\"rahmat123\""
        )

        buildConfigField(
            "String",
            "BASE_URL_ANEKA_LOGAM",
            "\"https://logam-mulia-api.vercel.app/\""
        )

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"https://awhyvidcoelagcgwkqks.supabase.co\""
        )

        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF3aHl2aWRjb2VsYWdjZ3drcWtzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTIyMTM0MjUsImV4cCI6MjA2Nzc4OTQyNX0.jVTeO5bBkjcI5vRc_48UpViYLNiOt50TjKRTqQ51Y-c\""
        )

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

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Untuk Room
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //navigation compose
    implementation("androidx.navigation:navigation-compose:2.9.1")

    //viewmodel compoee
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    //google
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")

    //supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.1.1")

    // Serialization untuk Supabase
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //ktor
    implementation("io.ktor:ktor-client-okhttp:3.0.3")

    // Coil untuk loading image
    implementation("io.coil-kt:coil-compose:2.7.0")

    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // Network monitoring
    implementation ("androidx.lifecycle:lifecycle-process:2.9.2")

}