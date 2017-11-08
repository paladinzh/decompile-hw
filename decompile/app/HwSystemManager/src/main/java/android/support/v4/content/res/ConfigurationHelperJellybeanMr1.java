package android.support.v4.content.res;

import android.content.res.Resources;
import android.support.annotation.NonNull;

class ConfigurationHelperJellybeanMr1 {
    ConfigurationHelperJellybeanMr1() {
    }

    static int getDensityDpi(@NonNull Resources resources) {
        return resources.getConfiguration().densityDpi;
    }
}
