package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.LargeFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class LargeFileTrashItem extends FileTrashItem<LargeFileTrash> {
    public static final String TAG = "LargeFileTrashItem";
    public static final TrashTransFunc<LargeFileTrashItem> sTransFunc = new TrashTransFunc<LargeFileTrashItem>() {
        public LargeFileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof LargeFileTrash) {
                return new LargeFileTrashItem((LargeFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "LogFileTrash trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 4;
        }
    };

    public LargeFileTrashItem(LargeFileTrash trash) {
        super(trash);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_file);
    }

    public Drawable getItemIcon() {
        return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storagecleaner_cache);
    }

    public boolean isNotCommonlyUsed() {
        return ((LargeFileTrash) this.mTrash).isNotCommonlyUsed();
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
