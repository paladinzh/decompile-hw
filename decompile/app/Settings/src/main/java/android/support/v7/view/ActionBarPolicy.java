package android.support.v7.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.support.v4.content.res.ConfigurationHelper;
import android.support.v4.view.ViewConfigurationCompat;
import android.view.ViewConfiguration;

public class ActionBarPolicy {
    private Context mContext;

    public static ActionBarPolicy get(Context context) {
        return new ActionBarPolicy(context);
    }

    private ActionBarPolicy(Context context) {
        this.mContext = context;
    }

    public int getMaxActionButtons() {
        Resources res = this.mContext.getResources();
        int widthDp = ConfigurationHelper.getScreenWidthDp(res);
        int heightDp = ConfigurationHelper.getScreenHeightDp(res);
        if (ConfigurationHelper.getSmallestScreenWidthDp(res) > 600 || widthDp > 600 || ((widthDp > 960 && heightDp > 720) || (widthDp > 720 && heightDp > 960))) {
            return 5;
        }
        if (widthDp >= 500 || ((widthDp > 640 && heightDp > 480) || (widthDp > 480 && heightDp > 640))) {
            return 4;
        }
        if (widthDp >= 360) {
            return 3;
        }
        return 2;
    }

    public boolean showsOverflowMenuButton() {
        boolean z = true;
        if (VERSION.SDK_INT >= 19) {
            return true;
        }
        if (ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(this.mContext))) {
            z = false;
        }
        return z;
    }

    public int getEmbeddedMenuWidthLimit() {
        return this.mContext.getResources().getDisplayMetrics().widthPixels / 2;
    }
}
