package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.text.TextUtils;
import com.google.common.base.Objects;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import java.io.IOException;

public abstract class ApkFileTrash extends FileTrash implements IAppTrashInfo {
    protected volatile boolean mRepeat;

    public abstract int getInstalledVersionCode();

    public abstract int getVersionCode();

    public abstract String getVersionName();

    public abstract boolean isBroken();

    public abstract boolean isInstalled();

    public ApkFileTrash(String path, PathEntry pathEntry) {
        super(path, pathEntry);
    }

    public int getType() {
        return 1024;
    }

    public boolean isSuggestClean() {
        if (isRepeat() || isBroken()) {
            return true;
        }
        return false;
    }

    public void setRepeat(boolean repeat) {
        this.mRepeat = repeat;
    }

    public boolean isRepeat() {
        return this.mRepeat;
    }

    public boolean checkIfRepeat(ApkFileTrash other) {
        String leftPkg = getPackageName();
        String rightPkg = other.getPackageName();
        if (TextUtils.isEmpty(leftPkg) || !TextUtils.equals(leftPkg, rightPkg)) {
            return false;
        }
        int myVersionCode = getVersionCode();
        if (myVersionCode != Integer.MIN_VALUE && myVersionCode == other.getVersionCode() && Objects.equal(getVersionName(), other.getVersionName())) {
            return true;
        }
        return false;
    }

    public void printf(Appendable appendable) throws IOException {
        super.printf(appendable);
        appendable.append("\n").append("    ").append("pkg:").append(getPackageName()).append(",label:").append(getAppLabel()).append(",broken:").append(String.valueOf(isBroken())).append(",installed:").append(String.valueOf(isInstalled())).append(",isRepeat:").append(String.valueOf(isRepeat()));
        if (getVersionCode() != Integer.MIN_VALUE) {
            appendable.append(",versionName:").append(getVersionName()).append(",vsersionCode:").append(String.valueOf(getVersionCode()));
        }
    }
}
