package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.SharedPreferences;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.Const;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;

public abstract class UnusedAppTrash extends SimpleTrash implements IAppTrashInfo {
    public abstract int getUnusedDay();

    public abstract boolean isPreInstalled();

    public String getName() {
        return getAppLabel();
    }

    public int getType() {
        return 2;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public boolean isNotCommonlyUsed() {
        SharedPreferences unusedTimePerfer = GlobalContext.getContext().getSharedPreferences(Const.SPACE_CLEAN_SHARED_PERFERENCE, 0);
        long exceedDay = unusedTimePerfer.getLong(Const.UNUSEED_APP_EXCEED_KEY, 90);
        long validityDay = unusedTimePerfer.getLong(Const.UNUSEED_APP_VALIDITY_KEY, SpaceConst.FILE_ANALYSIS_VALIDITY_PERIOD_DAYS);
        long unUsedDay = (long) getUnusedDay();
        if (unUsedDay <= 0) {
            HwLog.e(Trash.TAG, "unUsedTime is error.unUsedTime:" + unUsedDay);
            return false;
        } else if (unUsedDay > validityDay) {
            return false;
        } else {
            HwLog.i(Trash.TAG, "unUsedTime is unUsedDay:=" + unUsedDay);
            if (unUsedDay > exceedDay) {
                return true;
            }
            return false;
        }
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("     ").append("apkName:").append(getAppLabel()).append(", pkgName:").append(getPackageName()).append(",size").append(FileUtil.getFileSize(getTrashSize())).append(", unusedDay:").append(String.valueOf(getUnusedDay()));
    }
}
