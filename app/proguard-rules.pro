# ProGuard / R8 Optimization & Keep Rules for StandBy Android
# Reference: https://developer.android.com/topic/performance/app-optimization/enable-app-optimization

# 1. Line numbers and source files for readable stack traces & Play Console retracing
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. General Kotlin and reflection metadata attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 3. Android System Services & Components declared in AndroidManifest
-keep public class com.hoandesign.standby.MainActivity { *; }
-keep public class com.hoandesign.standby.StandbyDreamService { *; }
-keep public class com.hoandesign.standby.receiver.ChargingReceiver { *; }

# 4. StandBy Data Models and State
-keep class com.hoandesign.standby.model.** { *; }

# 5. Kotlinx Serialization (preserves generated serializer fields and methods)
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# 6. Jetpack Compose Runtime and ViewModel Keep Rules
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# 7. Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**
