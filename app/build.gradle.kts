plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.tosspayments.paymentsdk.sample"
    compileSdk = 35

    flavorDimensions += listOf("server", "source")

    defaultConfig {
        applicationId = "com.tosspayments.paymentsdk.sample"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = project.property("versionName") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        missingDimensionStrategy("server", "live")
        missingDimensionStrategy("source", "remote")
    }

    productFlavors {
        create("dev") {
            dimension = "server"
            applicationIdSuffix = ".dev"
        }
        create("staging") {
            dimension = "server"
            applicationIdSuffix = ".staging"
        }
        create("live") {
            dimension = "server"
            applicationIdSuffix = ".live"
        }
        create("local") {
            isDefault = true
            dimension = "source"
        }
        create("remote") {
            dimension = "source"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    add("localImplementation", project(":paymentsdk"))
    add(
        "remoteImplementation",
        "com.github.tosspayments:payment-sdk-android:${project.property("versionName")}"
    )

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.bundles.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
