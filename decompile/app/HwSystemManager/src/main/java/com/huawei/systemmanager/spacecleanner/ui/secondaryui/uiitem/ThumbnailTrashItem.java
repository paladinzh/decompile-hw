package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.trash.ThumbnailTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class ThumbnailTrashItem extends FileTrashItem<ThumbnailTrash> {
    public static final TrashTransFunc<ThumbnailTrashItem> sTransFunc = new TrashTransFunc<ThumbnailTrashItem>() {
        public ThumbnailTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof ThumbnailTrash) {
                return new ThumbnailTrashItem((ThumbnailTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "ThumbnailTrash trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 2048;
        }
    };

    public ThumbnailTrashItem(ThumbnailTrash trash) {
        super(trash);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_pic);
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
