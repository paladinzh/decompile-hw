package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import android.content.Context;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class TrashItemGroup extends ITrashItem implements IGroup {
    private boolean mScanFinished;
    protected String mTitle;
    protected int mTrashType;

    public int getTrashType() {
        return this.mTrashType;
    }

    public String getName() {
        return this.mTitle;
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSizeCleaned(false));
    }

    public boolean isSuggestClean() {
        return false;
    }

    public long getTrashSize() {
        return 0;
    }

    public int getTrashCount() {
        return 0;
    }

    public long getTrashSizeCleaned(boolean cleaned) {
        return 0;
    }

    public int doClickAction() {
        return 0;
    }

    public int getSize() {
        return 0;
    }

    public boolean isEmpty() {
        return getSize() >= 0;
    }

    public ITrashItem getItem(int index) {
        return null;
    }

    public List<Trash> getUncleanedCheckedTrash() {
        return Collections.emptyList();
    }

    public void setScanFinished(boolean finished) {
        this.mScanFinished = finished;
    }

    public boolean isScanFinished() {
        return this.mScanFinished;
    }

    public Iterator<ITrashItem> iterator() {
        return Collections.emptyIterator();
    }
}
