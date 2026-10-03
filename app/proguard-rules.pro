# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve Room entities, DAOs, and database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase$Callback
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Preserve Moshi models, entities, and serialization adapters
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class com.example.util.BudgetBackupData** { *; }
-keep class com.example.ui.theme.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep class * implements com.squareup.moshi.JsonAdapter { *; }

# Preserve Google Sign In / Play Services Auth / Credentials
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# OkHttp, Retrofit & Coroutines
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Biometrics
-keep class androidx.biometric.** { *; }

