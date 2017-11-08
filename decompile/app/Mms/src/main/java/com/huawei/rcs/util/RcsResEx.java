package com.huawei.rcs.util;

import android.graphics.drawable.Drawable;
import com.android.mms.util.LruSoftCache;
import com.android.rcs.RcsCommonConfig;

public class RcsResEx {
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();
    private LruSoftCache<Integer, Drawable> mStateListDrawableCache;

    public void removeStateListDrawable(int resId) {
        if (this.isRcsEnable && this.mStateListDrawableCache != null) {
            this.mStateListDrawableCache.remove(Integer.valueOf(resId));
        }
    }

    public void setStateListDrawableCache(LruSoftCache<Integer, Drawable> stateListDrawableCache) {
        if (this.isRcsEnable) {
            this.mStateListDrawableCache = stateListDrawableCache;
        }
    }
}
