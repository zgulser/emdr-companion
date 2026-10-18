# R8 / Compose Multiplatform
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Compose Multiplatform resources (WAV ticks, generated accessors)
-keep class com.zeki.emdr.resources.** { *; }
-keep class org.jetbrains.compose.resources.** { *; }

# Entry points the manifest and notification intents resolve by name
-keep class com.zeki.emdr.MainActivity { <init>(); }
-keep class com.zeki.emdr.session.EmdrSessionService { <init>(); }

# ViewModels constructed from Compose factories
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Play Billing callbacks
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Kotlin
-dontwarn kotlinx.coroutines.**
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
