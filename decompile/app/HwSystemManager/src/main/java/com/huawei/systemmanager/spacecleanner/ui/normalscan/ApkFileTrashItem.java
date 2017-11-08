package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.ApkFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class ApkFileTrashItem extends CommonTrashItem<ApkFileTrash> {
    public static final TrashTransFunc<ApkFileTrashItem> sTransFunc = new TrashTransFunc<ApkFileTrashItem>() {
        public ApkFileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "ApkFileTrashItem trans input is null!");
                return null;
            } else if (input instanceof ApkFileTrash) {
                return new ApkFileTrashItem((ApkFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "ApkFileTrashItem trans type error, origin is:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 1024;
        }
    };

    public ApkFileTrashItem(ApkFileTrash trash) {
        super(trash);
    }

    public String getName() {
        String label = ((ApkFileTrash) this.mTrash).getAppLabel();
        if (TextUtils.isEmpty(label)) {
            return FileUtil.getFileName(((ApkFileTrash) this.mTrash).getPath());
        }
        return label;
    }

    public String getDescription(Context ctx) {
        String size = FileUtil.getFileSize(getTrashSizeCleaned(false));
        String result = "";
        if (TextUtils.isEmpty(((ApkFileTrash) this.mTrash).getVersionName())) {
            return size;
        }
        return size + " " + ctx.getString(R.string.space_clean_apk_file_version, new Object[]{version});
    }

    public String getDesctiption2() {
        return buildDescription(GlobalContext.getContext());
    }

    public Drawable getItemIcon() {
        return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storagecleaner_apppackages);
    }

    public boolean shouldLoadPic() {
        if (((ApkFileTrash) this.mTrash).isBroken()) {
            return false;
        }
        return true;
    }

    public String getTrashPath() {
        return ((ApkFileTrash) this.mTrash).getPath();
    }

    protected String buildDescription(Context ctx) {
        if (((ApkFileTrash) this.mTrash).isBroken()) {
            return ctx.getString(R.string.space_clean_trash_attrs_broken);
        }
        if (((ApkFileTrash) this.mTrash).isRepeat()) {
            return ctx.getString(R.string.space_clean_trash_attrs_repeat);
        }
        if (((ApkFileTrash) this.mTrash).isInstalled()) {
            return ctx.getString(R.string.space_clean_trash_attrs_installed);
        }
        String versionTag = getVersionInfo(ctx);
        if (TextUtils.isEmpty(versionTag)) {
            return "";
        }
        return versionTag;
    }

    private String getVersionInfo(Context ctx) {
        int version = ((ApkFileTrash) this.mTrash).getVersionCode();
        int installedVersion = ((ApkFileTrash) this.mTrash).getInstalledVersionCode();
        String versionTag = "";
        if (installedVersion == Integer.MIN_VALUE) {
            return ctx.getString(R.string.space_clean_trash_attrs_not_installed);
        }
        if (installedVersion > version) {
            return ctx.getString(R.string.space_cleann_apk_old_version);
        }
        if (installedVersion == version) {
            return ctx.getString(R.string.space_clean_trash_attrs_installed);
        }
        return ctx.getString(R.string.space_cleann_apk_new_version);
    }
}
