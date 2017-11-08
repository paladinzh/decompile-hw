package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;

public abstract class DeepItem {
    public static final int TYPE_APPDATA = 1;
    public static final int TYPE_DEFAULT_STORAGE_CHANGE = 9;
    public static final int TYPE_DOWNLOAD = 11;
    public static final int TYPE_LARGEFILE = 2;
    public static final int TYPE_MUSIC = 3;
    public static final int TYPE_PHOTO = 4;
    public static final int TYPE_PREINSTALL = 7;
    public static final int TYPE_RESOTRE = 8;
    public static final int TYPE_UNUSED = 5;
    public static final int TYPE_VIDEO = 6;
    public static final int TYPE_WECHAT = 10;
    private boolean mIsFinished;

    public abstract int getDeepItemType();

    public abstract String getDescription(Context context);

    public abstract Drawable getIcon(Context context);

    public abstract Intent getIntent(Context context);

    public abstract String getResolveDes(Context context);

    public abstract String getTag();

    public abstract String getTitle(Context context);

    public abstract long getTrashSize();

    public abstract boolean isDeepItemDisplay(TrashScanHandler trashScanHandler);

    public abstract boolean isEmpty();

    public abstract boolean onCheckFinished(TrashScanHandler trashScanHandler);

    public abstract boolean shouldCheckFinished();

    public abstract boolean showInfrequentlyTip();

    public abstract boolean showTip();

    public boolean isFinished() {
        return this.mIsFinished;
    }

    protected void setFinish() {
        this.mIsFinished = true;
    }

    public boolean checkIfFinished(TrashScanHandler handler) {
        return onCheckFinished(handler);
    }
}
