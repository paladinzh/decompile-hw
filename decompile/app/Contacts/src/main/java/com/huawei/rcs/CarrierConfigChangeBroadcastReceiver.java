package com.huawei.rcs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.telephony.CarrierConfigManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.rcs.util.MLog;
import com.huawei.rcs.util.RcsFeatureEnabler;
import com.huawei.rcs.util.RcsXmlParser;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CarrierConfigChangeBroadcastReceiver extends BroadcastReceiver {
    private static ChangeListener mChangeListener = null;
    private static Set<String> mOwnConfigs = null;

    public interface ChangeListener {
        void onChange(boolean z);
    }

    public static void setOwnConfigs(Set<String> configs, ChangeListener listener) {
        mOwnConfigs = configs;
        mChangeListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        if (UserHandle.myUserId() != 0) {
            MLog.v("CarrierConfigChangeBroadcastReveiver", "Received broadcast for user that is not system.");
        } else if (intent == null) {
            MLog.w("CarrierConfigChangeBroadcastReveiver", "Null intent.");
        } else if (context == null) {
            MLog.w("CarrierConfigChangeBroadcastReveiver", "Null context.");
        } else if (RcsFeatureEnabler.getInstance().isRcsPropertiesConfigOn()) {
            String action = intent.getAction();
            if (action == null) {
                MLog.w("CarrierConfigChangeBroadcastReveiver", "Null action for intent.");
                return;
            }
            if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                MLog.d("CarrierConfigChangeBroadcastReveiver", "ACTION_CARRIER_CONFIG_CHANGED subId: " + subId);
                if (subId != SubscriptionManager.getDefaultSubscriptionId()) {
                    MLog.d("CarrierConfigChangeBroadcastReveiver", "ACTION_CARRIER_CONFIG_CHANGED not for default sub");
                } else if (checkSIMCardPresentState(subId, context)) {
                    parseConfig(context);
                } else {
                    MLog.d("CarrierConfigChangeBroadcastReveiver", "SIM " + subId + " is not present!");
                }
            }
        } else {
            MLog.w("CarrierConfigChangeBroadcastReveiver", "Phone is not support RCS.");
        }
    }

    private void parseConfig(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                CarrierConfigChangeBroadcastReceiver.this.parseConfigImpl(context);
            }
        }).start();
    }

    private void parseConfigImpl(Context context) {
        boolean bChange = false;
        Context lContext = context.createDeviceProtectedStorageContext();
        CarrierConfigManager cfgMgr = (CarrierConfigManager) lContext.getSystemService("carrier_config");
        if (cfgMgr == null) {
            MLog.e("CarrierConfigChangeBroadcastReveiver", "parseConfigImpl cfgMgr null");
            return;
        }
        PersistableBundle carrierConfig = cfgMgr.getConfig();
        if (carrierConfig == null) {
            MLog.e("CarrierConfigChangeBroadcastReveiver", "parseConfigImpl carrierConfig null");
            return;
        }
        SharedPreferences cfgPrefs = lContext.getSharedPreferences("rcs_defaults", 0);
        Map<String, ?> prefs = cfgPrefs.getAll();
        Set<String> cfgKeySet;
        if (mOwnConfigs == null) {
            Set<String> defaultKeySet = RcsXmlParser.getDefaultConfig().keySet();
            cfgKeySet = new HashSet();
            for (String key : defaultKeySet) {
                if (carrierConfig.getString(key) != null) {
                    cfgKeySet.add(key);
                }
            }
            if (prefs.size() == cfgKeySet.size()) {
                for (String key2 : cfgKeySet) {
                    if (!TextUtils.equals(carrierConfig.getString(key2), (String) prefs.get(key2))) {
                        bChange = true;
                        break;
                    }
                }
            }
            bChange = true;
        } else {
            for (String key22 : mOwnConfigs) {
                if (!TextUtils.equals(carrierConfig.getString(key22), (String) prefs.get(key22))) {
                    bChange = true;
                    break;
                }
            }
            cfgKeySet = mOwnConfigs;
        }
        if (bChange) {
            String localRcsEnabled = cfgPrefs.getString("huawei_rcs_enabler", "false");
            Editor editor = cfgPrefs.edit();
            editor.clear();
            for (String key222 : cfgKeySet) {
                String value = carrierConfig.getString(key222);
                if (value != null) {
                    editor.putString(key222, value);
                }
            }
            editor.commit();
            String rcsEnabled = carrierConfig.getString("huawei_rcs_enabler");
            boolean enableChanged = !TextUtils.equals(rcsEnabled, localRcsEnabled);
            MLog.d("CarrierConfigChangeBroadcastReveiver", "parseConfigImpl enableChanged is " + enableChanged);
            if (enableChanged && "true".equals(rcsEnabled)) {
                try {
                    Secure.putInt(context.getContentResolver(), "huawei_rcs_switcher", Integer.parseInt(carrierConfig.getString("huawei_rcs_switcher")));
                } catch (NumberFormatException e) {
                    MLog.e("CarrierConfigChangeBroadcastReveiver", "getInt NumberFormatException");
                }
            }
            if (mChangeListener != null) {
                mChangeListener.onChange(enableChanged);
            }
        }
    }

    public boolean checkSIMCardPresentState(int simSlot, Context context) {
        int lState = getSimState(simSlot, context);
        MLog.d("CarrierConfigChangeBroadcastReveiver", "isSIMCardPresent:" + lState + " for sslotId: " + simSlot);
        if (lState == 2 || lState == 3 || lState == 4 || lState == 5) {
            return true;
        }
        return false;
    }

    public int getSimState(int slotId, Context context) {
        if (context == null) {
            return -1;
        }
        int state;
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService("phone");
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            state = tm.getSimState(slotId);
        } else {
            state = tm.getSimState();
        }
        MLog.d("CarrierConfigChangeBroadcastReveiver", "Get SIM state from SIM factory manager: " + state + ",For slotId:" + slotId);
        return state;
    }
}
