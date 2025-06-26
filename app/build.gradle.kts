plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.graphql.apollo)
}

android {
    namespace = "com.example.collegeentrancesystem"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.collegeentrancesystem"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // android base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    //retrofit
    implementation(libs.bundles.retrofit)

    //okhttp3
    implementation(libs.bundles.okhttp3)

    //apollo
    implementation(libs.bundles.apollo)

    //navigation
    implementation(libs.bundles.navigation)

    //lifecycle
    implementation(libs.bundles.lifecycle)

    //rxjava
    implementation(libs.bundles.rxjava)

    //图片加载库
    implementation(libs.glide.runtime)
    annotationProcessor(libs.glide.compiler)

    /**UI组件**/
    //SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // Flexbox布局库
    implementation(libs.flexbox)

    // MPAndroidChart - 图表库
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    /**其他工具**/
    //mmkv
    implementation(libs.utils.mmkv)
    implementation(libs.utils.utilcode)
    //支持应用更新的库
    implementation(libs.utils.downloader)

    // Gson - 用于解析省市区数据
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(kotlin("reflect"))
}