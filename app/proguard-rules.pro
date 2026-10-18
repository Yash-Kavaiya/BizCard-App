# Project-specific R8 rules. Libraries in use (Retrofit, OkHttp, Moshi codegen, Room, CameraX,
# Coroutines, Compose) ship their own consumer keep rules, so none are repeated here.

# Keep line numbers so crashes can be de-obfuscated with the uploaded mapping.txt,
# while hiding original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Strip verbose, debug and info logging from release builds. Warnings and errors are kept.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
