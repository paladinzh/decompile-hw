package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.common.base.Function;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;

public class CommonTrashItem<T extends Trash> extends ITrashItem {
    protected final T mTrash;
    protected long mUncleanedCachedSize = -1;

    public static abstract class TrashTransFunc<T extends CommonTrashItem> implements Function<Trash, T> {
        public static final String TAG = "TrashTransFunc";

        public abstract int getTrashType();

        public T apply(Trash input) {
            return null;
        }
    }

    public CommonTrashItem(T trash) {
        this.mTrash = trash;
        refreshCleanState();
    }

    public Drawable getItemIcon() {
        return null;
    }

    public String getName() {
        return "";
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSizeCleaned(false));
    }

    public int getTrashType() {
        if (this.mTrash == null) {
            return 0;
        }
        return this.mTrash.getType();
    }

    public long getTrashSize() {
        if (this.mTrash == null) {
            return 0;
        }
        return this.mTrash.getTrashSize();
    }

    public int getTrashCount() {
        if (this.mTrash == null) {
            return 0;
        }
        return this.mTrash.getTrashCount();
    }

    public void refreshContent() {
        refreshCleanState();
        refreshCacheSize();
    }

    public long getTrashSizeCleaned(boolean cleaned) {
        if (this.mTrash == null) {
            return 0;
        }
        if (cleaned) {
            return this.mTrash.getTrashSizeCleaned(cleaned);
        }
        return this.mUncleanedCachedSize;
    }

    public boolean isSuggestClean() {
        if (this.mTrash == null) {
            return false;
        }
        return this.mTrash.isSuggestClean();
    }

    public boolean isNoTrash() {
        if (this.mTrash == null) {
            return true;
        }
        return false;
    }

    public T getTrash() {
        return this.mTrash;
    }

    private void refreshCleanState() {
        if (!isCleaned()) {
            if (this.mTrash == null || this.mTrash.isCleaned()) {
                setCleaned();
            }
        }
    }

    private void refreshCacheSize() {
        if (isCleaned()) {
            this.mUncleanedCachedSize = 0;
        } else if (this.mTrash == null) {
            this.mUncleanedCachedSize = 0;
        } else {
            this.mUncleanedCachedSize = this.mTrash.getTrashSizeCleaned(false);
        }
    }
}
