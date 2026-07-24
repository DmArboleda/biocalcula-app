plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}


android {
    namespace = "com.arboleda.biocalcula"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.arboleda.biocalcula"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    // ── Espresso UI Tests ──────────────────────────────────────────────────
    // rules: provee ActivityScenarioRule para lanzar Activities en tests de UI
    androidTestImplementation("androidx.test:rules:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // ── Pruebas de integración con Room In-Memory ──────────────────────────
    // room-testing: provee Room.inMemoryDatabaseBuilder() para tests
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    // core-ktx para ApplicationProvider.getApplicationContext() en androidTest
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    // coroutines-test: runBlocking y TestCoroutineScope para suspend functions
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // WorkManager — notificaciones programadas
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // MPAndroidChart — gráficos de dona y barras
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}