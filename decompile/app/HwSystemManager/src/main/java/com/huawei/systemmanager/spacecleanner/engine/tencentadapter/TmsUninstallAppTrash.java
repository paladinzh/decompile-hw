package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.engine.trash.FolderTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.IAppTrashInfo;
import java.io.IOException;
import java.util.List;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;

public class TmsUninstallAppTrash extends FolderTrash implements IAppTrashInfo {
    private List<String> mFileList = Lists.newArrayList();
    private SoftRubModel mSoftRubModel;

    TmsUninstallAppTrash(SoftRubModel model) {
        this.mSoftRubModel = model;
        covertFiles();
    }

    public String getName() {
        return getAppLabel();
    }

    public int getType() {
        return 8192;
    }

    public long getTrashSize() {
        return this.mSoftRubModel.mRubbishFileSize;
    }

    public boolean isSuggestClean() {
        return true;
    }

    public boolean clean(Context context) {
        return false;
    }

    public String getPackageName() {
        return this.mSoftRubModel.mAppName;
    }

    public String getAppLabel() {
        return this.mSoftRubModel.mAppName;
    }

    public Drawable getAppIcon() {
        return null;
    }

    public List<String> getFiles() {
        return Lists.newArrayList(this.mFileList);
    }

    private void covertFiles() {
        List<String> pathes = this.mSoftRubModel.mRubbishFiles;
        if (pathes != null && !pathes.isEmpty()) {
            List<String> result = Lists.newArrayListWithCapacity(pathes.size());
            for (String r : pathes) {
                result.add(r);
            }
            this.mFileList.addAll(result);
        }
    }

    public void printf(Appendable appendable) throws IOException {
    }
}
