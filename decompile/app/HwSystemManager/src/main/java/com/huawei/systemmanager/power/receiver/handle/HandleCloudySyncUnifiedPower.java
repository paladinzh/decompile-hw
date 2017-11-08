package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.power.model.UnifiedPowerBean;
import com.huawei.systemmanager.power.provider.SmartProviderHelper;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.UnifiedPowerAppsConfigBean;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class HandleCloudySyncUnifiedPower implements IBroadcastHandler {
    private static final String TAG = "HandleCloudySyncUnifiedPower";
    private Context mContext;
    private List<UnifiedPowerBean> mUserUnChangedList = null;

    public void handleBroadcast(Context ctx, Intent intent) {
        HwLog.i(TAG, " INTENT_CLOUDY_SYNC_UNIFILEDPOWER handle begin");
        this.mContext = ctx;
        this.mUserUnChangedList = SmartProviderHelper.getUserUnChangedUnifiedPowerList(this.mContext);
        if (this.mUserUnChangedList == null) {
            HwLog.i(TAG, "mUserUnChangedList == null");
            return;
        }
        if (this.mUserUnChangedList.size() > 0) {
            HwLog.i(TAG, "mUserUnChangedList.size() = " + this.mUserUnChangedList.size());
            for (UnifiedPowerBean bean : this.mUserUnChangedList) {
                UnifiedPowerAppsConfigBean cloudBean = CloudDBAdapter.getInstance(this.mContext).getSingleUnifiedPowerAppsConfigBean(bean.getPkg_name());
                if (cloudBean == null) {
                    HwLog.i(TAG, "null == cloudBean");
                } else if (!compareTwoBeans(bean, cloudBean)) {
                    SmartProviderHelper.updateUnifiedPowerAppListForDB(cloudBean.getPkgName(), cloudBean.isProtected(), cloudBean.isShow(), this.mContext);
                    HwLog.i(TAG, "" + cloudBean.getPkgName() + " is updated, to ensure same with cloud.");
                }
            }
        } else {
            HwLog.i(TAG, "mUserUnChangedList <= 0");
        }
        HwLog.i(TAG, "INTENT_CLOUDY_SYNC_UNIFILEDPOWER handle end");
    }

    private boolean compareTwoBeans(UnifiedPowerBean bean1, UnifiedPowerAppsConfigBean bean2) {
        if (bean1.is_protected() == bean2.isProtected() && bean1.is_show() == bean2.isShow() && bean1.getPkg_name().equals(bean2.getPkgName())) {
            return true;
        }
        return false;
    }
}
