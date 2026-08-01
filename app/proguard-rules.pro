# Keep Moshi-generated adapters so JSON parsing survives minification.
-keep class com.studysprint.app.data.remote.dto.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier @interface *

# Retrofit / OkHttp
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations, AnnotationDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Room entities are kept by the compiler; nothing extra needed.
