package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import com.android.settings.deviceinfo.HwCustStatus;
import com.huawei.cust.HwCustUtils;

public class TestingSettingsBroadcastReceiver extends BroadcastReceiver {
    private HwCustStatus mHwCustStatus;
    private SharedPreferences mPreferences;

    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SECRET_CODE".equals(intent.getAction())) {
            Intent i = new Intent("android.intent.action.MAIN");
            Class<?> cls = SettingsExtUtils.getClassForCommandCode(context, intent);
            if (cls != null) {
                i.setClass(context, cls);
                i.setFlags(268435456);
                context.startActivity(i);
            } else {
                return;
            }
        }
        if ("android.intent.action.LTE_CA_STATE".equals(intent.getAction())) {
            setCaState(context, intent.getBooleanExtra("LteCAstate", false));
        }
        try {
            this.mHwCustStatus = (HwCustStatus) HwCustUtils.createObj(HwCustStatus.class, new Object[0]);
            if (this.mHwCustStatus != null && this.mHwCustStatus.isDisplayIms()) {
                this.mHwCustStatus.setImsStatus(context, intent);
            }
        } catch (Exception e) {
            Log.e("TestingSettingsBroadcastReceiver", "Exception e :" + e.toString());
        }
    }

    public void setCaState(Context context, boolean isCAstate) {
        this.mPreferences = context.getSharedPreferences("caStatePreferences", 0);
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("isCAstate", isCAstate);
        editor.apply();
    }
}
