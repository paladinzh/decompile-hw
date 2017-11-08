package com.huawei.systemmanager.netassistant.traffic.trafficstatistics;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Formatter;
import com.huawei.netassistant.analyse.TrafficAnalyseManager;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.setting.RoamingTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo.TrafficData;
import com.huawei.systemmanager.util.HwLog;

public class RoamingTraffic extends ITrafficInfo {
    private static final String TAG = "RoamingTraffic";
    private static OnClickListener mClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            CommonMethodUtil.toggleGprs(true);
        }
    };
    private static OnDismissListener mDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            TrafficAnalyseManager.getInstance().setDialogDismiss();
        }
    };
    RoamingTrafficSetting mSetting;

    public RoamingTraffic(String imsi, int month) {
        super(imsi, month);
        this.mSetting = new RoamingTrafficSetting(imsi).get();
    }

    public void updateBytes(long updateByte) {
        HwLog.i(TAG, "update Byte, traffic Bytes = " + this.trafficBytes + " update bytes = " + updateByte);
        long total = this.trafficBytes + updateByte;
        if (this.mSetting.getPackage() >= 0 && this.trafficBytes < this.mSetting.getPackage() && total >= this.mSetting.getPackage()) {
            notifyUI();
        }
        super.updateBytes(total);
    }

    protected void notifyUI() {
        switch (this.mSetting.getNotifyType()) {
            case 1:
                HwLog.i(TAG, "roaming traffic only notify");
                NotificationUtil.notifyRoamingTrafficWarn(GlobalContext.getContext(), this.mImsi, CommonMethodUtil.formatBytes(GlobalContext.getContext(), this.trafficBytes));
                return;
            case 2:
                HwLog.i(TAG, "roaming traffic disable network and notify");
                NotificationUtil.notifyRoamingTrafficWarn(GlobalContext.getContext(), this.mImsi, CommonMethodUtil.formatBytes(GlobalContext.getContext(), this.trafficBytes));
                disableNetworkOperation();
                return;
            default:
                return;
        }
    }

    public int getType() {
        return 303;
    }

    public long getLeftTraffic() {
        return this.mSetting.getPackage() - getTraffic();
    }

    public TrafficData getTrafficData() {
        Context context = GlobalContext.getContext();
        TrafficData td = new TrafficData();
        long usedTraffic = this.trafficBytes >= 0 ? this.trafficBytes : 0;
        long restTraffic = this.mSetting.getPackage() - usedTraffic;
        String[] restTrafficSizeStr;
        String[] usedTrafficSizeStr;
        if (restTraffic >= 0) {
            td.setTrafficUsedMessage(context.getString(R.string.roaming_traffic_used_message, new Object[]{Formatter.formatFileSize(context, restTraffic), Formatter.formatFileSize(context, usedTraffic)}));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, restTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, usedTraffic);
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(false);
        } else {
            td.setTrafficUsedMessage(context.getString(R.string.roaming_traffic_over_message, new Object[]{Formatter.formatFileSize(context, -restTraffic), Formatter.formatFileSize(context, usedTraffic)}));
            restTrafficSizeStr = FileUtil.formatFileSizeByString(context, -restTraffic);
            td.setTrafficLeftData(restTrafficSizeStr[0]);
            td.setTrafficLeftUnit(restTrafficSizeStr[1]);
            usedTrafficSizeStr = FileUtil.formatFileSizeByString(context, usedTraffic);
            td.setTrafficUsedData(usedTrafficSizeStr[0]);
            td.setTrafficUsedUnit(usedTrafficSizeStr[1]);
            td.setOverData(true);
        }
        td.setTrafficStateMessage(context.getString(R.string.roaming_traffic_state_message));
        return td;
    }

    private void disableNetworkOperation() {
        Context context = GlobalContext.getContext();
        NetworkInfo mobNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(0);
        if (mobNetInfo != null && mobNetInfo.isConnected()) {
            createDialog(context);
            HwLog.v(TAG, "disableNetworkOperation success!");
        }
    }

    public void createDialog(Context context) {
        Builder builder = new Builder(context);
        builder.setTitle(R.string.net_assistant_notification_roaming_disable_network_title);
        builder.setMessage(R.string.net_assistant_notification_excess_disable_network_content);
        builder.setPositiveButton(17039370, null);
        builder.setNegativeButton(R.string.net_assistant_notification_excess_disable_network_button_reopen, mClickListener);
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.setOnDismissListener(mDismissListener);
        CommonMethodUtil.toggleGprs(false);
        dialog.show();
    }

    public long getTotalLimit() {
        return this.mSetting.getPackage();
    }
}
