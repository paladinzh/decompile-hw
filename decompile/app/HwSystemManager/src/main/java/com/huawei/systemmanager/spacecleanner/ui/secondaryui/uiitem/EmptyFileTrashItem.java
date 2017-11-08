package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.trash.EmptyFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class EmptyFileTrashItem extends FileTrashItem<EmptyFileTrash> {
    public static final TrashTransFunc<EmptyFileTrashItem> sTransFunc = new TrashTransFunc<EmptyFileTrashItem>() {
        public EmptyFileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof EmptyFileTrash) {
                return new EmptyFileTrashItem((EmptyFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "LogFileTrash trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 32;
        }
    };

    public EmptyFileTrashItem(EmptyFileTrash trash) {
        super(trash);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_file);
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
