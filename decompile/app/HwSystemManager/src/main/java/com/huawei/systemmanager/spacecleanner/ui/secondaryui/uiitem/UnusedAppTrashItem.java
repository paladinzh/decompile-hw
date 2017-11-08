package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.SpaceCleannerManager;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class UnusedAppTrashItem extends CommonTrashItem<UnusedAppTrash> {
    private static final String TAG = "UnusedAppTrashItem";
    public static final TrashTransFunc<UnusedAppTrashItem> sTransFunc = new TrashTransFunc<UnusedAppTrashItem>() {
        public UnusedAppTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "UnusedAppTrashItem trans, input is null!");
                return null;
            } else if (input instanceof UnusedAppTrash) {
                return new UnusedAppTrashItem((UnusedAppTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "UnusedAppTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 2;
        }
    };
    private boolean isNotCommonlyUsed;

    public UnusedAppTrashItem(UnusedAppTrash trash) {
        super(trash);
        this.isNotCommonlyUsed = false;
        this.isNotCommonlyUsed = isNotCommonlyUsed();
    }

    public String getDescription(Context ctx) {
        Resources res = GlobalContext.getContext().getResources();
        String size = FileUtil.getFileSize(getTrashSize());
        return res.getString(R.string.space_clean_list_size_detail, new Object[]{size});
    }

    public String getSummary() {
        if (this.isNotCommonlyUsed) {
            SpaceCleannerManager.getInstance();
            if (SpaceCleannerManager.isSupportHwFileAnalysis()) {
                int unUsedDay = ((UnusedAppTrash) this.mTrash).getUnusedDay();
                if (unUsedDay <= 0) {
                    HwLog.e(TAG, "unused days is error.unUsedDay:" + unUsedDay);
                    return "";
                }
                return GlobalContext.getContext().getResources().getQuantityString(R.plurals.spaceclean_not_commonly_used_data_tip, 90, new Object[]{Integer.valueOf(90)});
            }
        }
        return "";
    }

    public boolean isNotCommonlyUsed() {
        return ((UnusedAppTrash) this.mTrash).isNotCommonlyUsed();
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
    }

    public boolean isUseIconAlways() {
        return true;
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public String getName() {
        return ((UnusedAppTrash) this.mTrash).getAppLabel();
    }
}
