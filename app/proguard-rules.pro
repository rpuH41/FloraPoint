# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# ============ Hilt ============
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ============ Room ============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# ============ Firebase ============
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
#-keep class com.liulkovich.florapoint.domain.** { *; }
#-keep class com.liulkovich.florapoint.domain.cloud.** { *; }
-keep class com.liulkovich.florapoint.domain.SyncState { *; }


# ============ Kotlin Serialization ============
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ============ WorkManager ============
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ============ OSMDroid ============
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

# ============ Отладка крашей ============
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Сохраняем ресурсы и R-классы
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Если используешь Coil / Glide
-keep class coil.** { *; }
-keep class com.bumptech.glide.** { *; }
-dontwarn coil.**
-dontwarn com.bumptech.glide.**