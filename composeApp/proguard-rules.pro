# R8 full mode + proguard-android-optimize.txt already handle Android
# framework, Kotlin, coroutines, Billing, and MediaSession consumer rules.
# Do not keep androidx.compose.** — that blocks shrinking, obfuscation, and
# inlining of unused Material 3 / icon code and tanks Play DEX scores.

-allowaccessmodification
-repackageclasses

# Manifest components (also emitted by AAPT). Class names stay; members may
# still be optimized.
-keep class com.zeki.emdr.MainActivity { <init>(); }
-keep class com.zeki.emdr.session.EmdrSessionService { <init>(); }

# Compose Multiplatform resource loading. Class names can be obfuscated;
# members stay because accessors are resolved reflectively.
-keep,allowobfuscation class org.jetbrains.compose.resources.** { *; }
-dontwarn org.jetbrains.compose.resources.**
