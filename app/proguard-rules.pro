# Keep data models
-keep class com.panchangam100.live.data.model.** { *; }

# Astronomy engine
-keep class io.github.cosinekitty.astronomy.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Firebase / AdMob
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **.*$serializer { native <methods>; }
-keep,includedescriptorclasses class com.panchangam100.live.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.panchangam100.live.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Enums
-keepclassmembers enum * { *; }

# Parcelable
-keep class * implements android.os.Parcelable { *; }

# Reflection
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
