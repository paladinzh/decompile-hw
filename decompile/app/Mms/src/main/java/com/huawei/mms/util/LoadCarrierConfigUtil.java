package com.huawei.mms.util;

import android.content.Context;
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
import java.util.Map;
import java.util.Set;

public class LoadCarrierConfigUtil {
    private Set<String> mOwnConfigs = null;

    public void setOwnConfigs(Set<String> configs) {
        this.mOwnConfigs = configs;
    }

    public void loadCarrierConfig(Context context) {
        if (context == null) {
            MLog.w("LoadCarrierConfigUtil", "Null context.");
        } else if (UserHandle.myUserId() != 0) {
            MLog.v("LoadCarrierConfigUtil", "UserHandle user  is not system user.");
        } else if (checkSIMCardPresentState(SubscriptionManager.getDefaultSubscriptionId(), context)) {
            parseConfig(context);
        } else {
            MLog.d("LoadCarrierConfigUtil", "SIM " + SubscriptionManager.getDefaultSubscriptionId() + " is not present!");
        }
    }

    private void parseConfig(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                LoadCarrierConfigUtil.this.parseConfigImpl(context);
            }
        }).start();
    }

    private void parseConfigImpl(Context context) {
        boolean bChange = false;
        Context lContext = context.createDeviceProtectedStorageContext();
        CarrierConfigManager cfgMgr = (CarrierConfigManager) lContext.getSystemService("carrier_config");
        if (cfgMgr == null) {
            MLog.e("LoadCarrierConfigUtil", "parseConfigImpl cfgMgr null");
            return;
        }
        PersistableBundle carrierConfig = cfgMgr.getConfig();
        if (carrierConfig == null) {
            MLog.e("LoadCarrierConfigUtil", "parseConfigImpl carrierConfig null");
            return;
        }
        SharedPreferences cfgPrefs = lContext.getSharedPreferences("rcs_defaults", 0);
        Map<String, ?> prefs = cfgPrefs.getAll();
        Iterable cfgKeySet = null;
        if (this.mOwnConfigs != null) {
            for (String key : this.mOwnConfigs) {
                if (!TextUtils.equals(carrierConfig.getString(key), (String) prefs.get(key))) {
                    bChange = true;
                    break;
                }
            }
            cfgKeySet = this.mOwnConfigs;
        }
        if (bChange) {
            String localRcsEnabled = cfgPrefs.getString("huawei_rcs_enabler", "false");
            Editor editor = cfgPrefs.edit();
            editor.clear();
            for (String key2 : r4) {
                String value = carrierConfig.getString(key2);
                if (value != null) {
                    editor.putString(key2, value);
                }
            }
            editor.commit();
            String rcsEnabled = carrierConfig.getString("huawei_rcs_enabler");
            boolean enableChanged = !TextUtils.equals(rcsEnabled, localRcsEnabled);
            MLog.d("LoadCarrierConfigUtil", "parseConfigImpl enableChanged is " + enableChanged);
            if (enableChanged && "true".equals(rcsEnabled)) {
                try {
                    Secure.putInt(context.getContentResolver(), "huawei_rcs_switcher", Integer.parseInt(carrierConfig.getString("huawei_rcs_switcher")));
                } catch (NumberFormatException e) {
                    MLog.e("LoadCarrierConfigUtil", "getInt NumberFormatException");
                }
            }
        }
    }

    public boolean checkSIMCardPresentState(int simSlot, Context context) {
        int lState = getSimState(simSlot, context);
        MLog.d("LoadCarrierConfigUtil", "isSIMCardPresent:" + lState + " for sslotId: " + simSlot);
        if (lState == 2 || lState == 3 || lState == 4 || lState == 5) {
            return true;
        }
        return false;
    }

    public int getSimState(int slotId, Context context) {
        if (context == null) {
            return 0;
        }
        int state;
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService("phone");
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            state = tm.getSimState(slotId);
        } else {
            state = tm.getSimState();
        }
        MLog.d("LoadCarrierConfigUtil", "Get SIM state from SIM factory manager: " + state + ",For slotId:" + slotId);
        return state;
    }
}
