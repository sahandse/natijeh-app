-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exception
-keep class kotlin.Metadata { *; }
-keep class com.natijeh.data.model.** { *; }
-keep class com.natijeh.data.remote.dto.** { *; }
-keep class com.natijeh.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep class **JsonAdapter { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class androidx.work.** { *; }
