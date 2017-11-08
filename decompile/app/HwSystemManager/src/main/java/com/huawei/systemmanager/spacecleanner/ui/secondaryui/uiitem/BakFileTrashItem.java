package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.trash.BakFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class BakFileTrashItem extends FileTrashItem<BakFileTrash> {
    public static final TrashTransFunc<BakFileTrashItem> sTransFunc = new TrashTransFunc<BakFileTrashItem>() {
        public BakFileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "BakFileTrashItem trans, input is null!");
                return null;
            } else if (input instanceof BakFileTrash) {
                return new BakFileTrashItem((BakFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "BakFileTrash trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 2097152;
        }
    };

    public BakFileTrashItem(BakFileTrash trash) {
        super(trash);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_cache);
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
