# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep crypto classes
-keep class com.apkprotector.app.crypto.** { *; }
-keep class com.apkprotector.app.stub.** { *; }

# Keep BouncyCastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep APK signing classes
-keep class com.android.apksig.** { *; }
-dontwarn com.android.apksig.**

# Keep ZSTD
-keep class com.github.luben.zstd.** { *; }
-dontwarn com.github.luben.zstd.**

# Keep reflection-based classes
-keepclassmembers class * {
    public <init>(android.content.Context);
}

# Keep Application classes
-keep public class * extends android.app.Application
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
