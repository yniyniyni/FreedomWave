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

# --- WorkManager / Room (dashboard home-screen widget refresh worker) ---
# WorkManager initializes at app startup via androidx.startup's
# InitializationProvider and reflectively instantiates its Room-generated
# WorkDatabase_Impl. R8 full mode does not see the reflective call and strips
# the no-arg constructor, causing a startup crash:
#   RuntimeException: Unable to get provider androidx.startup.InitializationProvider
#   Caused by: NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []
# Keep the no-arg constructor of every Room database (covers WorkDatabase_Impl).
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
# Keep our CoroutineWorker so WorkManager's default factory can construct it by name.
-keep class org.freedomwave.widget.WidgetRefreshWorker { <init>(...); }