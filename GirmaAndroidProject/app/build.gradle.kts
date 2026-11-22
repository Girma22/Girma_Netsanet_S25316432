
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

// Read local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

// Read a property from local.properties
fun readLocalProperty(propertyName: String): String {
    return localProperties.getProperty(propertyName, "")
}

// Escape values for BuildConfig String fields
fun escapeBuildConfig(value: String): String {
    return "\"" + value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"") + "\""
}

android {

    namespace = "com.example.girma_netsanet_s2531643"

    compileSdk = 36

    defaultConfig {

        applicationId = "com.example.girma_netsanet_s2531643"

        minSdk = 26
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Traffic Scotland credentials
        buildConfigField(
            "String",
            "TRAFFIC_CLIENT_ID",
            escapeBuildConfig(
                readLocalProperty("TRAFFIC_CLIENT_ID")
            )
        )

        buildConfigField(
            "String",
            "TRAFFIC_CLIENT_KEY",
            escapeBuildConfig(
                readLocalProperty("TRAFFIC_CLIENT_KEY")
            )
        )

        // Traffic Scotland XML URLs
        buildConfigField(
            "String",
            "TRAFFIC_UNPLANNED_URL",
            escapeBuildConfig(
                "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml"
            )
        )

        buildConfigField(
            "String",
            "TRAFFIC_ROADWORKS_URL",
            escapeBuildConfig(
                "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml"
            )
        )

        // Google Maps API key
        manifestPlaceholders["MAPS_API_KEY"] =
            readLocalProperty("MAPS_API_KEY")
    }

    // Generate BuildConfig.java
    buildFeatures {
        buildConfig = true
    }

    // Release configuration
    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    // Java configuration
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Existing AndroidX dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:20.1.0")

    // AndroidX Fragment
    implementation("androidx.fragment:fragment:1.8.8")

    // AndroidX Lifecycle - ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.2")

    // AndroidX Lifecycle - LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.2")

    // Unit testing
    testImplementation(libs.junit)

    // Android testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
