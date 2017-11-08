package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;

public class SaveMoreTrashItem extends CommonTrashItem<Trash> {
    public static final SaveMoreTransFunc sSaveMoreTransFunc = new SaveMoreTransFunc() {
        public int getTrashType() {
            return 131072;
        }

        public int getTitleResId() {
            return R.string.space_clean_space_manager;
        }
    };
    private final int mTitleResId;
    private final int mTrashType;

    public static abstract class SaveMoreTransFunc extends TrashTransFunc<SaveMoreTrashItem> {
        public abstract int getTitleResId();

        public abstract int getTrashType();

        public SaveMoreTrashItem apply(Trash input) {
            return new SaveMoreTrashItem(input, getTrashType(), getTitleResId());
        }
    }

    public SaveMoreTrashItem(Trash trash, int trashType, int titleResId) {
        super(trash);
        this.mTrashType = trashType;
        this.mTitleResId = titleResId;
    }

    public int getTrashType() {
        return this.mTrashType;
    }

    public Drawable getItemIcon() {
        return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storgemanager);
    }

    public String getName() {
        return GlobalContext.getContext().getString(this.mTitleResId);
    }

    public String getDesctiption2() {
        return buildDescription(GlobalContext.getContext());
    }

    protected String buildDescription(Context ctx) {
        return ctx.getString(R.string.space_clean_save_more_sub);
    }
}
