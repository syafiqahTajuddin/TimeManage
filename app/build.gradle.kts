plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.navigation.safe.args)
}

android {
    namespace = "com.nursyafiqahtajuddin.timemanage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nursyafiqahtajuddin.timemanage"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)   //Material Components
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore) // For Firestore
    implementation(libs.firebase.auth)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.recyclerview)  // For Authentication
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.itextpdf:itext7-core:7.2.3")

    // Navigation Components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Handling ViewModel logic
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Glide library
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // For tab layout
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

}