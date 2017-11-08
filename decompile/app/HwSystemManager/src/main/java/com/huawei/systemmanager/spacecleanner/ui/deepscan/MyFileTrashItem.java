package com.huawei.systemmanager.spacecleanner.ui.deepscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;

public class MyFileTrashItem extends CommonTrashItem<Trash> {
    public static final MyFileTransFunc sAudioTransFunc = new MyFileTransFunc() {
        public int getTrashType() {
            return 512;
        }

        public int getTitleResId() {
            return R.string.space_clean_trash_audio;
        }
    };
    public static final MyFileTransFunc sPhototTransFunc = new MyFileTransFunc() {
        public int getTrashType() {
            return 128;
        }

        public int getTitleResId() {
            return R.string.space_clean_trash_photo;
        }
    };
    public static final MyFileTransFunc sVideotTransFunc = new MyFileTransFunc() {
        public int getTrashType() {
            return 256;
        }

        public int getTitleResId() {
            return R.string.space_clean_trash_video;
        }
    };
    private final int mTitleResId;
    private final int mTrashType;

    public static abstract class MyFileTransFunc extends TrashTransFunc<MyFileTrashItem> {
        public abstract int getTitleResId();

        public abstract int getTrashType();

        public MyFileTrashItem apply(Trash input) {
            return new MyFileTrashItem(input, getTrashType(), getTitleResId());
        }
    }

    public MyFileTrashItem(Trash trash, int trashType, int titleResId) {
        super(trash);
        this.mTrashType = trashType;
        this.mTitleResId = titleResId;
    }

    public int getTrashType() {
        return this.mTrashType;
    }

    public String getName() {
        return GlobalContext.getContext().getString(this.mTitleResId);
    }

    public boolean hasSecondary() {
        return true;
    }

    public Drawable getItemIcon() {
        Context ctx = GlobalContext.getContext();
        Drawable icon = ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_file);
        if (128 == getTrashType()) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_pic);
        }
        if (256 == getTrashType()) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_cache);
        }
        if (512 == getTrashType()) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_music);
        }
        return icon;
    }

    public OpenSecondaryParam getOpenSecondaryParam() {
        OpenSecondaryParam param = buildCommonSecondaryParam();
        param.setTitleStr(getName());
        switch (this.mTrashType) {
            case 128:
                param.setEmptyIconID(R.drawable.ic_no_camera);
                param.setEmptyTextID(R.string.no_photo_trash_tip);
                param.setOperationResId(R.string.common_delete);
                break;
            case 256:
                param.setEmptyIconID(R.drawable.ic_no_video);
                param.setEmptyTextID(R.string.spaceclean_no_video_trash);
                param.setOperationResId(R.string.common_delete);
                break;
            case 512:
                param.setEmptyIconID(R.drawable.ic_no_music);
                param.setEmptyTextID(R.string.no_file_audio_trash_tip);
                param.setOperationResId(R.string.common_delete);
                break;
            default:
                return null;
        }
        return param;
    }
}
