package com.huawei.systemmanager.netassistant.traffic.trafficstatistics;

import android.content.Context;
import android.text.format.Formatter;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo.TrafficData;
import com.huawei.systemmanager.util.HwLog;

public class LeisureTraffic extends ITrafficInfo {
    private static final String TAG = LeisureTraffic.class.getSimpleName();
    LeisureTrafficSetting mSetting;
    ITrafficInfo normalTrafficInfo;

    public LeisureTraffic(String imsi, int month) {
        super(imsi, month);
    }

    public void updateBytes(long deltaByte) {
        long usedTraffic = this.trafficBytes + deltaByte;
        HwLog.i(TAG, "updateBytes total = " + this.mSetting.getmPackageSize() + " used = " + usedTraffic);
        if (!this.mSetting.ismSwitch()) {
            this.normalTrafficInfo.updateBytes(deltaByte);
        } else if (usedTraffic > this.mSetting.getmPackageSize()) {
            if (this.trafficBytes != this.mSetting.getmPackageSize()) {
                HwLog.i(TAG, "traffic byte = " + this.trafficBytes + " setting = " + this.mSetting.getmPackageSize());
                notifyUI();
            }
            this.trafficBytes = this.mSetting.getmPackageSize();
            this.normalTrafficInfo.updateBytes(usedTraffic - this.mSetting.getmPackageSize());
            super.updateBytes(this.trafficBytes);
        } else {
            super.updateBytes(usedTraffic);
        }
    }

    protected void notifyUI() {
        HwLog.i(TAG, "leisure traffic notify UI");
        if (this.mSetting.ismNotify()) {
            NotificationUtil.notifyLeisureTrafficWarn(GlobalContext.getContext(), this.mImsi, CommonMethodUtil.formatBytes(GlobalContext.getContext(), this.trafficBytes));
        }
    }

    protected void onCreate() {
        super.onCreate();
        HwLog.i(TAG, "onCreate");
        this.mSetting = new LeisureTrafficSetting(this.mImsi);
        this.normalTrafficInfo = ITrafficInfo.create(this.mImsi, this.mMonth, 301);
        this.mSetting.get();
    }

    public int getType() {
        return 302;
    }

    public TrafficData getTrafficData() {
        Context context = GlobalContext.getContext();
        TrafficData td = new TrafficData();
        long restTraffic = this.mSetting.getmPackageSize() - getTraffic();
        long restNormalTraffic = this.normalTrafficInfo.getTotalLimit() - this.normalTrafficInfo.getTraffic();
        long normalTraffic = this.normalTrafficInfo.getTraffic() > 0 ? this.normalTrafficInfo.getTraffic() : 0;
        String[] restTrafficSizeStr;
        String[] usedTrafficSizeStr;
        if (restTraffic > 0) {
            td.setTrafficUsedMessage(context.getString(R.string.net_assistant_leisure_package_text, new Object[]{Formatter.formatFileSize(context, restTraffic), Formatter.formatFileSize(context, getTraffic())}));
            td.setTrafficStateMessage(context.getString(R.string.net_assistant_leisure_package_message));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, restTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, getTraffic());
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(false);
        } else if (restNormalTraffic >= 0) {
            td.setTrafficUsedMessage(context.getResources().getString(R.string.net_assistant_normal_package_text, new Object[]{Formatter.formatFileSize(context, restNormalTraffic), Formatter.formatFileSize(context, normalTraffic)}));
            td.setTrafficStateMessage(context.getString(R.string.net_assistant_leisure_package_over_message));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, restNormalTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, normalTraffic);
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(false);
        } else {
            td.setTrafficUsedMessage(context.getResources().getString(R.string.net_assistant_normal_package_over_text, new Object[]{Formatter.formatFileSize(context, -restNormalTraffic), Formatter.formatFileSize(context, normalTraffic)}));
            td.setTrafficStateMessage(context.getString(R.string.net_assistant_leisure_package_over_message));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, -restNormalTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, normalTraffic);
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(true);
        }
        td.setNormalUsedTraffic(this.normalTrafficInfo.getTraffic());
        return td;
    }

    public long getLeftTraffic() {
        if (getTraffic() >= this.mSetting.getmPackageSize()) {
            return this.normalTrafficInfo.getLeftTraffic();
        }
        return this.mSetting.getmPackageSize() - getTraffic();
    }

    public long getTotalLimit() {
        return this.mSetting.getmPackageSize();
    }
}
