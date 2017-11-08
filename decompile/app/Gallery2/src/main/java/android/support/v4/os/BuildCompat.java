package android.support.v4.os;

import android.os.Build.VERSION;

public class BuildCompat {
    private BuildCompat() {
    }

    public static boolean isAtLeastN() {
        return VERSION.SDK_INT >= 24;
    }
}
