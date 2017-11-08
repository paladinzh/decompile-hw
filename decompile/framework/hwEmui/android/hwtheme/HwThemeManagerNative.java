package android.hwtheme;

public class HwThemeManagerNative {
    public static final native String getColor();

    static {
        System.loadLibrary("hwtheme_jni");
    }
}
