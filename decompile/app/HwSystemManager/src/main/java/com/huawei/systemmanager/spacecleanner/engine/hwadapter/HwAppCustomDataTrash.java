package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwAppCustomMgr;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashConst;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashInfo;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustom.HwCustomAppDataGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppModelTrash;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HwAppCustomDataTrash extends AppModelTrash {
    private static final String TAG = "HwAppCustomDataTrash";
    private int mCustType;
    private Drawable mIcon;
    private boolean mIsSuggest;
    private PathEntry mPathEntry;
    private String mPkgName;
    private String mRule;
    private int mTrashType;

    public PathEntry getPathEntry() {
        return this.mPathEntry;
    }

    public HwAppCustomDataTrash(Map<String, Long> fileMap, int trashType, PathEntry pathEntry) {
        super(fileMap);
        this.mPathEntry = pathEntry;
        this.mTrashType = trashType;
    }

    public int getCustType() {
        return this.mCustType;
    }

    public String getRule() {
        return this.mRule;
    }

    public int getType() {
        return this.mTrashType;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public boolean isSuggestClean() {
        return this.mIsSuggest;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("Description:").append(getName()).append(",suggestClean:").append(String.valueOf(this.mIsSuggest)).append(",size:").append(String.valueOf(FileUtil.getFileSize(getTrashSize()))).append("\n");
        for (String path : getFiles()) {
            appendable.append("    ").append(path).append("\n");
        }
    }

    public static HwAppCustomDataTrash create(Map<String, Long> fileMap, HwCustTrashInfo trashInfo, int trashType, PathEntry pathEntry) {
        HwAppCustomDataTrash trash = new HwAppCustomDataTrash(fileMap, trashType, pathEntry);
        trash.mPkgName = trashInfo.getPkgName();
        trash.mCustType = trashInfo.getTrashType();
        trash.mIsSuggest = trashInfo.getRecommend();
        trash.mRule = trashInfo.getMatchRule();
        return trash;
    }

    public String getAppLabel() {
        Context ctx = GlobalContext.getContext();
        String pkgName = getPackageName();
        if (!TextUtils.isEmpty(pkgName)) {
            if (pkgName.equalsIgnoreCase(HwCustTrashConst.GALLERY_DEFAULT_PKG_NAME)) {
                return ctx.getString(R.string.space_clean_tab_cloud);
            }
            if (pkgName.equalsIgnoreCase(HwCustTrashConst.DOWMLOAD_DEFAULT_NAME)) {
                return ctx.getString(R.string.space_clean_system_download);
            }
        }
        return HsmPackageManager.getInstance().getLabel(getPackageName());
    }

    public Drawable getAppIcon() {
        if (this.mIcon != null) {
            return this.mIcon;
        }
        this.mIcon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(getPackageName()));
        return this.mIcon;
    }

    public String getModelName() {
        String[] descriptions = GlobalContext.getContext().getResources().getStringArray(R.array.spacecleanner_hwcust_trash_type);
        String trashType = descriptions[0];
        if (this.mCustType >= descriptions.length || this.mCustType < 0) {
            return trashType;
        }
        return descriptions[this.mCustType];
    }

    public int getDiffDays() {
        return 0;
    }

    public boolean isTrashEquals(HwAppCustomDataTrash other) {
        boolean result = false;
        if (other == null) {
            return false;
        }
        if (this.mCustType == other.getCustType() && this.mIsSuggest == other.isSuggestClean()) {
            result = true;
        }
        return result;
    }

    public HwCustomAppDataGroup changeToAppGroup() {
        List<String> files = getFiles();
        if (files.size() <= 0) {
            HwLog.e(TAG, "changeToGroup files is empty.mPkgName:" + this.mPkgName + " mCustType:" + this.mCustType);
            return null;
        }
        HwCustomAppDataGroup group = new HwCustomAppDataGroup(getType(), getPackageName(), getAppLabel(), isSuggestClean());
        for (String filePath : files) {
            if (HwAppCustomMgr.isRuleMatched(getRule(), FileUtil.getFileName(filePath))) {
                group.addChild(new HwCustomDataItemTrash(filePath, this.mPathEntry, getPackageName(), getType()));
            }
        }
        return group;
    }
}
