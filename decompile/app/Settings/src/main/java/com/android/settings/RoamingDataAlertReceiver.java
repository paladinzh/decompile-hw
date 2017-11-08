package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.MSimTelephonyManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.android.provider.SettingsEx.Systemex;

public class RoamingDataAlertReceiver extends BroadcastReceiver {
    private static boolean sIsChinaTelecom;

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        sIsChinaTelecom = equals;
    }

    public void onReceive(Context context, Intent intent) {
        if (!sIsChinaTelecom) {
            return;
        }
        if (intent == null) {
            Log.d("RoamingDataAlertReceiver", "intent is null!");
            return;
        }
        String action = intent.getAction();
        Log.d("RoamingDataAlertReceiver", "action :" + action);
        if ("com.android.systemui.statusbar.policy.GET_ROAM_STATE".equals(action)) {
            handleDataConnectivityChangedIntent(context, intent);
        } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
            handleServiceStateChangedIntent(context, intent);
        }
    }

    private void handleDataConnectivityChangedIntent(Context context, Intent intent) {
        if (isDataConnected(context)) {
            startShowAlertDialog(context);
        } else {
            Log.d("RoamingDataAlertReceiver", "data not connect.");
        }
    }

    private void handleServiceStateChangedIntent(Context context, Intent intent) {
        if (intent.getExtras() == null) {
            Log.e("RoamingDataAlertReceiver", "null intent attack!");
            return;
        }
        ServiceState ss = ServiceState.newFromBundle(intent.getExtras());
        if (ss == null) {
            Log.e("RoamingDataAlertReceiver", "ServiceState is null!");
            return;
        }
        int serviceState = ss.getState();
        String plmn = ss.getOperatorNumeric();
        boolean equals = ("46003".equals(plmn) || "45502".equals(plmn)) ? true : "46011".equals(plmn);
        Log.d("RoamingDataAlertReceiver", "plmn:" + plmn + ",besure:" + equals);
        String value = SystemProperties.get("gsm.sim.operator.numeric");
        if (value != null && value.length() > 1) {
            if (serviceState != 0) {
                Log.e("RoamingDataAlertReceiver", "not in service.");
            } else if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                int subscription = intent.getIntExtra("subscription", -1);
                if (subscription == -1) {
                    Log.e("RoamingDataAlertReceiver", "subscription is invlid. subscription:" + subscription);
                    return;
                }
                Log.d("RoamingDataAlertReceiver", "subscription:" + subscription + " service state changed.");
                if (subscription == 0 && !isSimRoamingNow(context, subscription) && equals) {
                    clearAlertUserFlagWhenNotInRoaming(context, subscription);
                } else if (subscription == 1 && !isSimRoamingNow(context, subscription)) {
                    clearAlertUserFlagWhenNotInRoaming(context, subscription);
                }
                if ((subscription == 0 && value.startsWith("00101")) || (1 == subscription && value.endsWith(",00101"))) {
                    Log.d("RoamingDataAlertReceiver", "card is test card");
                    return;
                }
                startShowAlertDialog(context, subscription);
            } else {
                if (!isSimRoamingNow(context) && equals) {
                    clearAlertUserFlagWhenNotInRoaming(context, 0);
                }
                if (value.startsWith("00101")) {
                    Log.d("RoamingDataAlertReceiver", "Surport Sigle Card : test card");
                    return;
                }
                startShowAlertDialog(context);
            }
        }
    }

    private int getCurrentDataEffectSubscription(Context context) {
        int sub = 0;
        String name = "multi_sim_data_call";
        try {
            sub = Global.getInt(context.getContentResolver(), name, 0);
        } catch (Exception e) {
            MLog.e("RoamingDataAlertReceiver", "Settings Exception reading values of " + name);
        }
        return sub;
    }

    private void alertUser(Context context, int simSub) {
        showAlertDialog(context);
        setAlertUserFlag(context, simSub, true);
    }

    private void setAlertUserFlag(Context context, int simSub, boolean alerted) {
        Log.d("RoamingDataAlertReceiver", "set alert_user_before value to " + alerted + " for sub" + simSub);
        Systemex.putInt(context.getContentResolver(), "alert_user_before_" + simSub, alerted ? 1 : 0);
    }

    private boolean isDataConnected(Context context) {
        boolean mUserDataEnabled = Global.getInt(context.getContentResolver(), "mobile_data", 1) == 1;
        Log.v("RoamingDataAlertReceiver", "mUserDataEnabled = " + mUserDataEnabled);
        return mUserDataEnabled;
    }

    private void startShowAlertDialog(Context context, int activieSub) {
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            Log.v("RoamingDataAlertReceiver", "active sub is " + activieSub);
            if (isSimRoamingNow(context, activieSub) && !isSimRoamingAlertBefore(context, activieSub) && isDataConnected(context)) {
                Log.d("RoamingDataAlertReceiver", "sub is roaming, and not alert user before, alert now");
                alertUser(context, activieSub);
            }
        }
    }

    private void startShowAlertDialog(Context context) {
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            int activieSub = getCurrentDataEffectSubscription(context);
            Log.v("RoamingDataAlertReceiver", "active sub is " + activieSub);
            if (isSimRoamingNow(context, activieSub) && !isSimRoamingAlertBefore(context, activieSub) && isDataConnected(context)) {
                Log.d("RoamingDataAlertReceiver", "sub is roaming, and not alert user before, alert now");
                alertUser(context, activieSub);
            }
        } else if (isSimRoamingNow(context) && !isSimRoamingAlertBefore(context, 0) && isDataConnected(context)) {
            Log.d("RoamingDataAlertReceiver", "phone is roaming, and not alert user before, alert now");
            alertUser(context, 0);
        }
    }

    private boolean isSimRoamingNow(Context context, int simSub) {
        boolean isRoaming = MSimTelephonyManager.getDefault().isNetworkRoaming(simSub);
        Log.d("RoamingDataAlertReceiver", "sub[" + simSub + "] isroaming:" + isRoaming);
        return isRoaming;
    }

    private boolean isSimRoamingNow(Context context) {
        boolean isRoaming = TelephonyManager.getDefault().isNetworkRoaming();
        Log.d("RoamingDataAlertReceiver", "Current sim roaming state is:" + isRoaming);
        return isRoaming;
    }

    private boolean isSimRoamingAlertBefore(Context context, int simSub) {
        boolean isAlertUserBefore = Systemex.getInt(context.getContentResolver(), new StringBuilder().append("alert_user_before_").append(simSub).toString(), 0) == 1;
        Log.v("RoamingDataAlertReceiver", "sub[" + simSub + "] is alert user before:" + isAlertUserBefore);
        return isAlertUserBefore;
    }

    private void clearAlertUserFlagWhenNotInRoaming(Context context, int simSub) {
        boolean isAlerted = isSimRoamingAlertBefore(context, simSub);
        Log.d("RoamingDataAlertReceiver", "Enter clearAlertUserFlagWhenNotInRoaming, isAlerted=" + isAlerted);
        if (isAlerted) {
            setAlertUserFlag(context, simSub, false);
        }
    }

    public void showAlertDialog(Context context) {
        Log.d("RoamingDataAlertReceiver", "show alert dialog to alert user!!");
        Intent roamAlertIntent = new Intent("android.intent.action.ROAMING_ALERT");
        roamAlertIntent.setFlags(268435456);
        context.startActivity(roamAlertIntent);
    }
}
