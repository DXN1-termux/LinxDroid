-keep class com.linxdroid.app.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-dontwarn org.apache.commons.**
-keep class org.apache.commons.compress.** { *; }

-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okio.**
-keep class okio.** { *; }
