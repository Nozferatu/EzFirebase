plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
//    id("com.google.gms.google-services")
    id("maven-publish")
}

android {
    namespace = "com.cmj.ezfirebase"
    compileSdk = 34

    defaultConfig {
//        applicationId = "com.cmj.ezfirebase"
        minSdk = 30
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "17"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.Nozferatu"
            artifactId = "EzFirebase"
            version = "0.1.6.4-pre"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //Firebase
    implementation(libs.firebase.database.ktx)
    implementation(platform(libs.firebase.bom))
    implementation ("com.google.android.gms:play-services-auth:20.1.0")
}
