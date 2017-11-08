package android.support.v4.print;

import android.os.Build.VERSION;

public final class PrintHelper {
    public static boolean systemSupportsPrint() {
        if (VERSION.SDK_INT >= 19) {
            return true;
        }
        return false;
    }
}
