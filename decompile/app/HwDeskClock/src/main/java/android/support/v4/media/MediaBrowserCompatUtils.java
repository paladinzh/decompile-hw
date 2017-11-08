package android.support.v4.media;

import android.os.Bundle;

public class MediaBrowserCompatUtils {
    public static boolean areSameOptions(Bundle options1, Bundle options2) {
        boolean z = true;
        boolean z2 = false;
        if (options1 == options2) {
            return true;
        }
        if (options1 == null) {
            if (options2.getInt("android.media.browse.extra.PAGE", -1) != -1) {
                z = false;
            } else if (options2.getInt("android.media.browse.extra.PAGE_SIZE", -1) != -1) {
                z = false;
            }
            return z;
        } else if (options2 == null) {
            if (options1.getInt("android.media.browse.extra.PAGE", -1) == -1 && options1.getInt("android.media.browse.extra.PAGE_SIZE", -1) == -1) {
                z2 = true;
            }
            return z2;
        } else {
            if (options1.getInt("android.media.browse.extra.PAGE", -1) == options2.getInt("android.media.browse.extra.PAGE", -1) && options1.getInt("android.media.browse.extra.PAGE_SIZE", -1) == options2.getInt("android.media.browse.extra.PAGE_SIZE", -1)) {
                z2 = true;
            }
            return z2;
        }
    }
}
