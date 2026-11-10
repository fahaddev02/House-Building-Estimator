import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.paintcalculatorestimator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hellowordapps.construction.cost.estimator"
        minSdk = 28
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.4"

        val date = Date()
        val formattedDate = SimpleDateFormat("ddMMyyyy").format(date)


        setProperty("archivesBaseName", "ConstructionEstimator" + "_V" + versionName + "_" + formattedDate)
    }

    buildTypes {

        debug {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
            resValue("string", "app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue ("string", "open_app_ad_splash", "ca-app-pub-3940256099942544/9257395921")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            resValue("string", "app_id", "ca-app-pub-9728452702722475~4639338687")
            resValue ("string", "open_app_ad_splash", "ca-app-pub-9728452702722475/3853794060")
            isMinifyEnabled = true
            isShrinkResources = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.cardview)
    implementation(libs.firebase.config)
    implementation(libs.androidx.appcompat)
    kapt(libs.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // google ads
    implementation(libs.play.services.ads)
    //in-app purchase
    implementation (libs.billing)


    debugImplementation(libs.androidx.compose.ui.tooling)
}
