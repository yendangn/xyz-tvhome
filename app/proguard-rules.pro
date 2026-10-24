# window.TV: core.js calls these by name, so R8 must keep and not rename them.
-keepclassmembers class com.xyz.tvhome.web.Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
