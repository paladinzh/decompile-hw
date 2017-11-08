package com.huawei.systemmanager.spacecleanner.ui.deepscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustom.HwCustomTypeDataGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class DownloadTypeGroupItem extends CommonTrashItem<HwCustomTypeDataGroup> {
    private static final String TAG = "DownloadTypeGroupItem";

    public static class DownloadTypeGroupTransFunc extends TrashTransFunc<DownloadTypeGroupItem> {
        private final int trashType;

        public DownloadTypeGroupTransFunc(int type) {
            this.trashType = type;
        }

        public int getTrashType() {
            return this.trashType;
        }

        public DownloadTypeGroupItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "DownloadAppGroupTransFunc, input is null!");
                return null;
            } else if (input instanceof HwCustomTypeDataGroup) {
                return new DownloadTypeGroupItem((HwCustomTypeDataGroup) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "DownloadTypeGroupTransFunc, input in not HwCustomTypeDataGroup!,input type:" + input.getType());
                return null;
            }
        }
    }

    private DownloadTypeGroupItem(HwCustomTypeDataGroup trash) {
        super(trash);
    }

    public String getName() {
        String name = "";
        HwCustomTypeDataGroup trash = (HwCustomTypeDataGroup) getTrash();
        if (trash == null) {
            HwLog.e(TAG, "DownloadTypeGroupItem trash is null!");
            return name;
        }
        Context ctx = GlobalContext.getContext();
        int fileType = trash.getFileType();
        switch (fileType) {
            case 1:
                name = ctx.getString(R.string.space_clean_downlad_audio_type);
                break;
            case 2:
                name = ctx.getString(R.string.space_clean_downlad_vodeo_type);
                break;
            case 3:
                name = ctx.getString(R.string.space_clean_downlad_image_type);
                break;
            case 4:
                name = ctx.getString(R.string.space_clean_downlad_apk_type);
                break;
            case 5:
                name = ctx.getString(R.string.space_clean_downlad_document_type);
                break;
            case 6:
                name = ctx.getString(R.string.space_clean_downlad_compress_type);
                break;
            case 7:
                name = ctx.getString(R.string.space_clean_downlad_other_type);
                break;
            default:
                HwLog.e(TAG, "getName,file type is not invalidate.fileType:" + fileType);
                break;
        }
        return name;
    }

    public Drawable getItemIcon() {
        return null;
    }

    public static DownloadTypeGroupTransFunc getGroupTransFunc(int type) {
        return new DownloadTypeGroupTransFunc(type);
    }

    public boolean hasSecondary() {
        return false;
    }
}
