plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "ec.zero.assistant"
 compileSdk = 35
 defaultConfig { applicationId = "ec.zero.assistant"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
