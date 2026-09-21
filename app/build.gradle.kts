plugins { id("com.android.application") }
android {
    namespace = "co.edu.triqui"
    compileSdk = 37
    defaultConfig {
        applicationId = "co.edu.triqui.reto4"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies { testImplementation("junit:junit:4.13.2") }
