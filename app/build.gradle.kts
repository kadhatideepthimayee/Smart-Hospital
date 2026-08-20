import java.io.File
import java.io.FileNotFoundException
import java.util.Properties
import java.net.NetworkInterface
import java.net.Inet4Address

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
}

fun getLocalIpAddress(): String {
    try {
        val ips = mutableListOf<String>()
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (address is Inet4Address) {
                    ips.add(address.hostAddress)
                }
            }
        }
        
        // 1. Prioritize physical Wi-Fi subnet first
        val wifiIp = ips.find { it.startsWith("172.23.") }
        if (wifiIp != null) return wifiIp

        // 2. Fallback to general 172.x subnets
        val general172 = ips.find { it.startsWith("172.") }
        if (general172 != null) return general172

        // 3. Fallback to physical 192.168 subnets (ignoring VMware VMnet subnets)
        val general192 = ips.find { it.startsWith("192.168.") && !it.startsWith("192.168.11.") && !it.startsWith("192.168.74.") }
        if (general192 != null) return general192

        // 4. Default fallback list
        val anyMatch = ips.find { it.startsWith("192.168.") || it.startsWith("10.") || it.startsWith("172.") }
        if (anyMatch != null) return anyMatch
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "10.0.2.2"
}

android {
    namespace = "com.example.medplus"
    compileSdk = 35

    val homeDir = System.getProperty("user.home")
    val secretsFile = File(homeDir, ".medplus_secrets")
    val secrets = Properties().apply {
        if (secretsFile.exists()) {
            secretsFile.inputStream().use { load(it) }
        }
    }
    val firebaseApiKey = secrets.getProperty("FIREBASE_API_KEY") ?: "PLACEHOLDER_KEY"

    defaultConfig {
        applicationId = "com.example.medplus"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject Firebase configurations directly as string resources,
        // allowing us to load the API key from a safe external location.
        resValue("string", "google_api_key", firebaseApiKey)
        resValue("string", "google_app_id", "1:366692040766:android:fea6231f06287d7de5516f")
        resValue("string", "project_id", "medplus-a50ca")
        resValue("string", "gcm_defaultSenderId", "366692040766")
        resValue("string", "google_storage_bucket", "medplus-a50ca.firebasestorage.app")
        resValue("string", "default_web_client_id", "366692040766-iduuu65jevefkpt2n04na5flh294jpec.apps.googleusercontent.com")
        
        val hostIp = getLocalIpAddress()
        resValue("string", "default_api_url", "http://$hostIp:5000/")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.play.services.auth)
    
    // Retrofit & OkHttp networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
}
