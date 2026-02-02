# Retrofit 2 rules
-keepattributes Signature, InnerClasses, AnnotationDefault
-keep public class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Gson rules
-keep class com.google.gson.** { *; }
-keep class com.sandeshkoli.yttrendy.models.** { *; } # APNE MODELS WALA PACKAGE DALO

# Glide rules
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule

# Room Database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# YouTube Player Library rules
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }