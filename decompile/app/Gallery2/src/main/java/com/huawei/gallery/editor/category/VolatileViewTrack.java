package com.huawei.gallery.editor.category;

import android.content.Context;
import android.util.AttributeSet;

public class VolatileViewTrack extends BaseViewTrack {
    public VolatileViewTrack(Context context) {
        super(context);
    }

    public VolatileViewTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected boolean isVolatile() {
        return true;
    }
}
