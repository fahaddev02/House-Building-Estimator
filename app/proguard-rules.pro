# ProGuard / R8 Configuration Rules
# Keep line numbers and source files for readable Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Annotations & Signature Attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------------------
# App Domain & Data Models
# Preserve data entities, models, relations, and Preferences
# ------------------------------------------------------------------------------
-keep class com.example.paintcalculatorestimator.data.** { *; }
-keep class com.example.paintcalculatorestimator.domain.model.** { *; }
-keepclassmembers class com.example.paintcalculatorestimator.data.** { *; }
-keepclassmembers class com.example.paintcalculatorestimator.domain.model.** { *; }

# ------------------------------------------------------------------------------
# Room Database Rules
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.RoomDatabase { *; }

# ------------------------------------------------------------------------------
# Dagger / Hilt Dependency Injection
# ------------------------------------------------------------------------------
-keep class * extends dagger.hilt.internal.UnsafeCasts
-keep class com.google.dagger.hilt.** { *; }
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
    @javax.inject.Inject <init>(...);
}

# ------------------------------------------------------------------------------
# Kotlin Coroutines & Serialization
# ------------------------------------------------------------------------------
-keepclassmembers class kotlinx.coroutines.android.AndroidDispatcherFactory {
    public *;
}
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler {
    public *;
}
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------
# Google Mobile Ads (AdMob)
# ------------------------------------------------------------------------------
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ------------------------------------------------------------------------------
# Firebase & Crashlytics
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**