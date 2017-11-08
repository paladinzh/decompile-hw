package com.huawei.systemmanager.spacecleanner.ui.deepscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwCustomDataItemTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.ApkFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.FileTrashItem;
import com.huawei.systemmanager.spacecleanner.utils.HwMediaFile;
import com.huawei.systemmanager.util.HwLog;

public class DownloadAppTrashItem extends FileTrashItem<HwCustomDataItemTrash> {
    private static final String TAG = "DownloadAppTrashItem";
    public static final TrashTransFunc<DownloadAppTrashItem> sTransFunc = new TrashTransFunc<DownloadAppTrashItem>() {
        public DownloadAppTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "DownloadAppTrashItem trans, input is null!");
                return null;
            } else if (input instanceof HwCustomDataItemTrash) {
                return new DownloadAppTrashItem((HwCustomDataItemTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "DownloadAppTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 81920;
        }
    };
    private ApkFileTrash mRelateTrash;

    public DownloadAppTrashItem(HwCustomDataItemTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize());
    }

    public String getFileFromPkg() {
        return ((HwCustomDataItemTrash) this.mTrash).getFileFromPkg();
    }

    public Drawable getItemIcon() {
        int resId = HwMediaFile.getIconResByPath(getTrashPath());
        Context ctx = GlobalContext.getContext();
        if (resId > 0) {
            return ctx.getResources().getDrawable(resId);
        }
        return ctx.getResources().getDrawable(R.drawable.list_file_unkown);
    }

    public String getDesctiption2() {
        Context ctx = GlobalContext.getContext();
        String des = "";
        switch (getFileType()) {
            case 4:
                return buildDescription(ctx);
            default:
                return des;
        }
    }

    public void setRelateTrash(ApkFileTrash relateTrash) {
        this.mRelateTrash = relateTrash;
    }

    public int getFileType() {
        return ((HwCustomDataItemTrash) this.mTrash).getFileBigType();
    }

    protected String buildDescription(Context ctx) {
        if (this.mRelateTrash == null) {
            HwLog.i(TAG, "buildDescription ,but mRelateTrash is null");
        } else if (this.mRelateTrash.isBroken()) {
            return ctx.getString(R.string.space_clean_trash_attrs_broken);
        } else {
            if (this.mRelateTrash.isRepeat()) {
                return ctx.getString(R.string.space_clean_trash_attrs_repeat);
            }
            if (this.mRelateTrash.isInstalled()) {
                return ctx.getString(R.string.space_clean_trash_attrs_installed);
            }
            String versionTag = getVersionInfo(ctx);
            if (!TextUtils.isEmpty(versionTag)) {
                return versionTag;
            }
        }
        return "";
    }

    private String getVersionInfo(Context ctx) {
        String versionTag = "";
        if (this.mRelateTrash == null) {
            return versionTag;
        }
        int version = this.mRelateTrash.getVersionCode();
        int installedVersion = this.mRelateTrash.getInstalledVersionCode();
        if (installedVersion == Integer.MIN_VALUE) {
            versionTag = ctx.getString(R.string.space_clean_trash_attrs_not_installed);
        } else if (installedVersion > version) {
            versionTag = ctx.getString(R.string.space_cleann_apk_old_version);
        } else if (installedVersion == version) {
            versionTag = ctx.getString(R.string.space_clean_trash_attrs_installed);
        } else {
            versionTag = ctx.getString(R.string.space_cleann_apk_new_version);
        }
        return versionTag;
    }

    public boolean isUseIconAlways() {
        return false;
    }

    public boolean shouldLoadPic() {
        switch (getFileType()) {
            case 1:
            case 5:
            case 6:
            case 7:
                return false;
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    public String getName() {
        return FileUtil.getFileName(((HwCustomDataItemTrash) this.mTrash).getPath());
    }

    public int getIconWidth(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_apk_icon_width);
    }

    public int getIconHeight(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_apk_icon_height);
    }
}
