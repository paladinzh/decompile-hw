package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.spacecleanner.engine.trash.PhotoTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class PhotoTrashItem extends FileTrashItem<PhotoTrash> {
    public static final TrashTransFunc<PhotoTrashItem> sTransFunc = new TrashTransFunc<PhotoTrashItem>() {
        public PhotoTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "PhotoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof PhotoTrash) {
                return new PhotoTrashItem((PhotoTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "PhotoTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 128;
        }
    };

    public PhotoTrashItem(PhotoTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        return "";
    }

    public Drawable getIcon(Context context) {
        return null;
    }

    public String getName() {
        return "";
    }
}
