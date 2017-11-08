package com.android.mms.layout;

import android.content.Context;
import android.content.res.Configuration;
import com.huawei.cspcommon.MLog;

public class LayoutManager {
    private static LayoutManager sInstance;
    private final Context mContext;
    private LayoutParameters mLayoutParams;

    private LayoutManager(Context context) {
        this.mContext = context;
        try {
            initLayoutParameters(context.getResources().getConfiguration());
        } catch (NullPointerException e) {
            MLog.e("LayoutManager", "error occurs in LayoutManager : ", (Throwable) e);
        }
    }

    private void initLayoutParameters(Configuration configuration) {
        int i;
        if (configuration.orientation == 1) {
            i = 11;
        } else {
            i = 10;
        }
        this.mLayoutParams = getLayoutParameters(i);
    }

    private LayoutParameters getLayoutParameters(int displayType) {
        switch (displayType) {
            case 10:
                return new HVGALayoutParameters(this.mContext, 10);
            case 11:
                return new HVGALayoutParameters(this.mContext, 11);
            default:
                throw new IllegalArgumentException("Unsupported display type: " + displayType);
        }
    }

    public static void init(Context context) {
        if (sInstance != null) {
            MLog.w("LayoutManager", "Already initialized.");
        }
        sInstance = new LayoutManager(context);
    }

    public static LayoutManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        throw new IllegalStateException("Uninitialized.");
    }

    public void onConfigurationChanged(Configuration newConfig) {
        initLayoutParameters(newConfig);
    }

    public LayoutParameters getLayoutParameters() {
        return this.mLayoutParams;
    }
}
