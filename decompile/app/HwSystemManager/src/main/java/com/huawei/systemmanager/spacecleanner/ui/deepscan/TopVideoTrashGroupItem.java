package com.huawei.systemmanager.spacecleanner.ui.deepscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.TencenTopVideoAppGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;

public class TopVideoTrashGroupItem extends CommonTrashItem<TencenTopVideoAppGroup> {
    private static final String TAG = "TopVideoTrashGroupItem";

    public static class TopVideoGroupTransFunc extends TrashTransFunc<TopVideoTrashGroupItem> {
        private final int trashType;

        public TopVideoGroupTransFunc(int type) {
            this.trashType = type;
        }

        public int getTrashType() {
            return this.trashType;
        }

        public TopVideoTrashGroupItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "TopVideoGroupTransFunc, input is null!");
                return null;
            } else if (input instanceof TencenTopVideoAppGroup) {
                return new TopVideoTrashGroupItem((TencenTopVideoAppGroup) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "TopVideoGroupTransFunc, input in not TencenTopVideoAppGroup!,input type:" + input.getType());
                return null;
            }
        }
    }

    private TopVideoTrashGroupItem(TencenTopVideoAppGroup trash) {
        super(trash);
    }

    public String getName() {
        return ((TencenTopVideoAppGroup) getTrash()).getAppLabel();
    }

    public Drawable getItemIcon() {
        Context ctx = GlobalContext.getContext();
        Drawable icon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(((TencenTopVideoAppGroup) getTrash()).getPackageName()));
        if (icon == null) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
        }
        return icon;
    }

    public static TopVideoGroupTransFunc getGroupTransFunc(int type) {
        return new TopVideoGroupTransFunc(type);
    }

    public boolean hasSecondary() {
        return false;
    }
}
