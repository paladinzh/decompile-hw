package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.trash.LogFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class LogFileTrashItem extends FileTrashItem<LogFileTrash> {
    public static final TrashTransFunc<LogFileTrashItem> sTransFunc = new TrashTransFunc<LogFileTrashItem>() {
        public LogFileTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof LogFileTrash) {
                return new LogFileTrashItem((LogFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "LogFileTrash trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 8;
        }
    };

    public LogFileTrashItem(LogFileTrash trash) {
        super(trash);
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_cache);
    }

    public boolean isUseIconAlways() {
        return true;
    }
}
