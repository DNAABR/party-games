# Keep Jetpack Compose rules
-keepclassmembers class * extends androidx.compose.ui.node.Owner { *; }
-dontwarn androidx.compose.**

# OkHttp ProGuard Rules
-keepattributes Signature
-keepattributes AnnotationDefault
-keepclassmembers class * extends okhttp3.Response { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Leminno Remote AI Gateway Data Models
-keepclassmembers class com.leminno.partygames.data.remote.** { *; }
-keepclassmembers class com.leminno.partygames.data.model.** { *; }

# Keep Coroutine stack trace information
-keepattributes SourceFile,LineNumberTable
-keepclassmembers class kotlinx.coroutines.** { *; }
