dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // کتابخانه OkHttp3 برای فراخوانی‌های سریع HTTP در کسری از ثانیه
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // پردازش سریع جیسون
    implementation("com.google.code.gson:gson:2.10.1")
    
    // کش کردن محلی آفلاین با SQLite Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
}