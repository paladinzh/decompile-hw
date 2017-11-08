package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.trash.TempFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class TempFileTrashItem extends FileTrashItem<TempFileTrash> {
    public static final TrashTransFunc<TempFileTrashItem> sTransFunc = new TrashTransFunc<TempFileTrashItem>() {
        public TempFileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof TempFileTrash) {
                return new TempFileTrashItem((TempFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "TempFileTrash trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 16;
        }
    };

    public TempFileTrashItem(TempFileTrash trash) {
        super(trash);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_cache);
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
