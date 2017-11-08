package com.huawei.systemmanager.spacecleanner.ui.deepscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.RootFolderTrashGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.util.HwLog;

public class RootFolderTrashItem extends CommonTrashItem<RootFolderTrashGroup> {
    private static final String TAG = "RootFolderTrashItem";
    private String mComeFromCache;

    public static class RootFolderTransFunc extends TrashTransFunc<RootFolderTrashItem> {
        private final int trashType;

        public RootFolderTransFunc(int type) {
            this.trashType = type;
        }

        public int getTrashType() {
            return this.trashType;
        }

        public RootFolderTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "RootFolderTransFunc, input is null!");
                return null;
            } else if (input instanceof RootFolderTrashGroup) {
                return new RootFolderTrashItem((RootFolderTrashGroup) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "RootFolderTransFunc, input in not RootFolderTrashGroup!,input type:" + input.getType());
                return null;
            }
        }
    }

    private RootFolderTrashItem(RootFolderTrashGroup trash) {
        super(trash);
        this.mComeFromCache = null;
    }

    public String getName() {
        return GlobalContext.getContext().getString(R.string.space_clean_deep_root_folder_trash_description, new Object[]{getComeFrom()});
    }

    public Drawable getItemIcon() {
        return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storagecleaner_file);
    }

    private String getComeFrom() {
        if (this.mComeFromCache != null) {
            return this.mComeFromCache;
        }
        Context ctx = GlobalContext.getContext();
        String comeFrom = "";
        String folerName;
        switch (((RootFolderTrashGroup) this.mTrash).getPosition()) {
            case 2:
                if (!((RootFolderTrashGroup) this.mTrash).isInRootDirectory()) {
                    folerName = ((RootFolderTrashGroup) this.mTrash).getFolderName();
                    comeFrom = ctx.getString(R.string.space_clean_deep_file_trash_innernal_perfix, new Object[]{folerName});
                    break;
                }
                comeFrom = ctx.getString(R.string.space_clean_deep_file_trash_inner_rootpath);
                break;
            case 3:
                if (!((RootFolderTrashGroup) this.mTrash).isInRootDirectory()) {
                    folerName = ((RootFolderTrashGroup) this.mTrash).getFolderName();
                    comeFrom = ctx.getString(R.string.space_clean_deep_file_trash_sdcard_perfix, new Object[]{folerName});
                    break;
                }
                comeFrom = ctx.getString(R.string.space_clean_deep_file_trash_sdcard_rootpath);
                break;
            default:
                comeFrom = ((RootFolderTrashGroup) this.mTrash).getPath();
                break;
        }
        this.mComeFromCache = comeFrom;
        return this.mComeFromCache;
    }

    public static RootFolderTransFunc getTransFunc(int type) {
        return new RootFolderTransFunc(type);
    }

    public boolean hasSecondary() {
        return true;
    }

    public OpenSecondaryParam getOpenSecondaryParam() {
        OpenSecondaryParam param = buildCommonSecondaryParam();
        param.setEmptyIconID(R.drawable.ic_no_folder);
        param.setUniqueDescription(((RootFolderTrashGroup) this.mTrash).getUniqueDes());
        param.setOperationResId(R.string.common_delete);
        param.setEmptyTextID(R.string.no_file_trash_tip);
        param.setTitleStr(getComeFrom());
        return param;
    }
}
