-keep public class in.indoreflorist.paper1mock.MainActivity {
    public <init>();
    protected void onCreate(android.os.Bundle);
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'a'
