package com.android.settings.wifi.ap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.SystemClock;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.CustomDialogPreference;
import com.android.settings.MLog;
import java.util.Date;

public class WifiApConnectedDevicePreference extends CustomDialogPreference {
    private String mConnectedDate = "";
    private String mConnectedTime = "";
    private Context mContext;
    private WifiApClientInfo mInfo;
    private OnViewDeviceListener mListener;
    private TextView mMacView;

    interface OnViewDeviceListener {
        void onAddAllowedDevice(WifiApClientInfo wifiApClientInfo);

        void onDisconnectDevice(WifiApClientInfo wifiApClientInfo);
    }

    public WifiApConnectedDevicePreference(Context context, WifiApClientInfo info, OnViewDeviceListener listener) {
        super(context, null);
        this.mListener = listener;
        this.mInfo = new WifiApClientInfo(info);
        setLayout();
        this.mContext = context;
    }

    protected void setLayout() {
        setLayoutResource(2130969262);
        setTitle(loadDeviceName());
        setDialogTitle(loadDeviceName());
        setSummaryIp();
        setSummaryMac();
        setWidgetLayoutResource(2130968998);
        setIcon(2130838260);
    }

    public void setSummaryIp() {
        setSummary((CharSequence) String.format(getContext().getString(2131627419), new Object[]{getIP()}));
    }

    public void setSummaryMac() {
        if (this.mMacView != null) {
            this.mMacView.setText(String.format(getContext().getString(2131627420), new Object[]{getMAC()}));
        }
    }

    public CharSequence loadDeviceName() {
        CharSequence deviceName = this.mInfo.getDeviceName();
        if (deviceName == null || "".equals(deviceName)) {
            return getContext().getString(2131627426);
        }
        return deviceName;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mMacView = (TextView) view.findViewById(2131887457);
        this.mMacView.setVisibility(0);
        setSummaryMac();
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        formatConnectedTime();
        textArray = new CharSequence[4];
        textArray[0] = this.mContext.getResources().getString(2131627749, new Object[]{getIP()});
        textArray[1] = this.mContext.getResources().getString(2131627750, new Object[]{getMAC()});
        textArray[2] = this.mContext.getResources().getString(2131628241, new Object[]{this.mConnectedDate, this.mConnectedTime});
        textArray[3] = this.mContext.getResources().getString(2131627752, new Object[]{getConnectedDuration()});
        builder.setItems(textArray, null);
        if (WifiApClientUtils.getInstance(this.mContext).isSupportConnectManager()) {
            builder.setPositiveButton(2131627741, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiApConnectedDevicePreference.this.mListener != null) {
                        WifiApConnectedDevicePreference.this.mListener.onAddAllowedDevice(WifiApConnectedDevicePreference.this.mInfo);
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(2131627744, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (WifiApConnectedDevicePreference.this.mListener != null) {
                        WifiApConnectedDevicePreference.this.mListener.onDisconnectDevice(WifiApConnectedDevicePreference.this.mInfo);
                    }
                    dialog.dismiss();
                }
            });
            return;
        }
        builder.setNegativeButton(2131625656, null);
        builder.setPositiveButton(null, null);
        MLog.d("ConnectedDevice", "add to allow and disconnect is not supported");
    }

    public void onDialogFragmentStart() {
        super.onDialogFragmentStart();
        boolean canAddDevice = !WifiApClientUtils.getInstance(this.mContext).isDeviceAllowed(this.mInfo) ? WifiApClientUtils.getInstance(this.mContext).canAddDevice() : false;
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getListView().setEnabled(false);
            Button buttion = dialog.getButton(-1);
            if (buttion != null) {
                buttion.setEnabled(canAddDevice);
            }
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    private CharSequence getIP() {
        return this.mInfo.getIP() == null ? "" : this.mInfo.getIP();
    }

    private CharSequence getMAC() {
        return this.mInfo.getMAC() == null ? "" : this.mInfo.getMAC();
    }

    private void formatConnectedTime() {
        if (this.mInfo.getConnectedTime() == 0) {
            MLog.d("ConnectedDevice", "/formatConnectedTime/getConnectedTime is null");
            return;
        }
        Date date = new Date(convertBootTimeToSystemTime(this.mInfo.getConnectedTime()));
        this.mConnectedDate = DateFormat.getDateFormat(getContext()).format(date);
        this.mConnectedTime = DateFormat.getTimeFormat(getContext()).format(date);
    }

    private CharSequence getConnectedDuration() {
        if (this.mInfo.getConnectedTime() == 0) {
            return "";
        }
        long duration = (SystemClock.elapsedRealtime() - this.mInfo.getConnectedTime()) / 1000;
        long seconds = duration % 60;
        long minutes = (duration / 60) % 60;
        return String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(duration / 3600), Long.valueOf(minutes), Long.valueOf(seconds)});
    }

    private long convertBootTimeToSystemTime(long timeSinceBoot) {
        return System.currentTimeMillis() - (SystemClock.elapsedRealtime() - timeSinceBoot);
    }
}
