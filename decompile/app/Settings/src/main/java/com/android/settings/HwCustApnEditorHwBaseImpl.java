package com.android.settings;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class HwCustApnEditorHwBaseImpl extends HwCustApnEditorHwBase {
    protected static final Uri APN_SIM1_URI1 = Uri.parse("content://telephony/carriers_sim1");
    protected static final Uri APN_SIM2_URI1 = Uri.parse("content://telephony/carriers_sim2");
    public static final String APN_URI1 = "content://telephony/carriers_sim1";
    public static final String APN_URI2 = "content://telephony/carriers_sim2";
    private static final boolean IS_ATT;
    private static final boolean IS_DUN_APN_EDITABLE_DELETEABLE = SystemProperties.getBoolean("ro.config.dun_apn_edit_delete", false);
    private static final boolean REMOVE_APN_MVNO = SystemProperties.getBoolean("ro.config.remove_apn_MVNO", false);
    static final String TAG = "HwCustApnEditorHwBaseImpl";

    public HwCustApnEditorHwBaseImpl(ApnEditorHwBase apnEditorHwBase) {
        super(apnEditorHwBase);
    }

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("07")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        } else {
            equals = false;
        }
        IS_ATT = equals;
    }

    public String getApnDisplayTitle(Context context, String name) {
        return UtilsCustEx.getApnDisplayTitle(context, name);
    }

    public boolean isApnReadable(Context context, int subId) {
        String listOfMccMnc = Systemex.getString(context.getContentResolver(), "hw_config_readonly_apn_mccmnc_list");
        if (!TextUtils.isEmpty(listOfMccMnc)) {
            String currentMccMnc = TelephonyManager.from(context).getSimOperator(subId);
            if (!TextUtils.isEmpty(currentMccMnc)) {
                for (String mcc : listOfMccMnc.split(",")) {
                    if (currentMccMnc.startsWith(mcc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Uri getApnUri(Uri apn_uri, int sub) {
        Uri apn_uri_temp = apn_uri;
        if (SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
            if (sub == 0) {
                apn_uri_temp = APN_SIM1_URI1;
            } else if (sub == 1) {
                apn_uri_temp = APN_SIM2_URI1;
            }
        }
        Log.i(TAG, "ro.config.mtk_platform_apn = " + SystemProperties.getBoolean("ro.config.mtk_platform_apn", false) + ",apn_uri_temp = " + apn_uri_temp + ",sub = " + sub);
        return apn_uri_temp;
    }

    public void removeApnMvno(ListPreference mMvnoType, EditTextPreference mMvnoMatchData, PreferenceScreen mPreferenceScreen) {
        if (REMOVE_APN_MVNO && mMvnoType != null && mMvnoMatchData != null) {
            mPreferenceScreen.removePreference(mMvnoType);
            mPreferenceScreen.removePreference(mMvnoMatchData);
        }
    }

    public boolean disableProtocol() {
        return SystemProperties.getBoolean("ro.config.preapn_protcl_disable", true);
    }

    public String getDefaultPort(Context context) {
        int setNewApnPort = Systemex.getInt(context.getContentResolver(), "set_new_apn_port", 0);
        if (setNewApnPort == 0) {
            return null;
        }
        Log.v(TAG, "new APNport is null, need to set 8080");
        return Integer.toString(setNewApnPort);
    }

    public void setDefaultPort(Context context, EditTextPreference mPort) {
        String DefaultPort = getDefaultPort(context);
        if (DefaultPort != null && mPort != null) {
            mPort.setText(DefaultPort);
        }
    }

    public boolean[] initApnEidtOrDel(Context context, int slotId) {
        boolean[] apnEidtOrDel = new boolean[]{false, false};
        String cardNum = SystemProperties.get("gsm.sim.operator.numeric");
        String confPlmn = Systemex.getString(context.getContentResolver(), "plmn_apn");
        if (cardNum == null || cardNum.length() <= 4) {
            return apnEidtOrDel;
        }
        String curOperatorNum;
        if (slotId == 0) {
            curOperatorNum = cardNum.substring(0, 5);
        } else {
            curOperatorNum = cardNum.substring(cardNum.indexOf(",") + 1);
        }
        if (confPlmn != null) {
            String[] mccmncVS = confPlmn.split(";");
            int i = 0;
            int length = mccmncVS.length;
            while (i < length) {
                String[] mcc = mccmncVS[i].split(":");
                if (mcc.length != 3) {
                    return apnEidtOrDel;
                }
                if (curOperatorNum.equals(mcc[0].trim())) {
                    if ("1".equals(mcc[1].trim())) {
                        apnEidtOrDel[0] = true;
                    }
                    if ("1".equals(mcc[2].trim())) {
                        apnEidtOrDel[1] = true;
                    }
                } else {
                    i++;
                }
            }
        }
        return apnEidtOrDel;
    }

    public void custForApnBearer(PreferenceScreen mPreferenceScreen, DialogPreference mBearer) {
        if (SystemProperties.getBoolean("ro.config.apn_remove_Bearer", false)) {
            mPreferenceScreen.removePreference(mBearer);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isDunApnEditableAndDeletable(Context context, Cursor cursor) {
        if (!(context == null || cursor == null || !IS_DUN_APN_EDITABLE_DELETEABLE)) {
            try {
                if (cursor.moveToFirst()) {
                    String type = cursor.getString(cursor.getColumnIndex("type"));
                    if (type != null && type.contains("dun")) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void setDefaultApnType(EditTextPreference editTextPreference) {
        if (editTextPreference != null && IS_ATT) {
            editTextPreference.setText("default,hipri");
        }
    }

    public String[] getDefaultProtocol(Context context, String curMccmnc) {
        String mccmncList = System.getString(context.getContentResolver(), "cust_protocol_mccmnc_list");
        String[] IPvalue = new String[]{"", ""};
        String VALUE = "IP,IPV4V6,IPV6";
        if (TextUtils.isEmpty(mccmncList) || TextUtils.isEmpty(curMccmnc)) {
            return IPvalue;
        }
        Log.v(TAG, "getDefaultProtocol, curMccmnc =" + curMccmnc + " mccmncList =" + mccmncList);
        for (String conf : mccmncList.split(",")) {
            String[] node = conf.split(":");
            if (node.length != 3) {
                return IPvalue;
            }
            if (curMccmnc.equals(node[0]) || "all".equals(node[0])) {
                if (VALUE.contains(node[1]) && VALUE.contains(node[2])) {
                    IPvalue[0] = node[1];
                    IPvalue[1] = node[2];
                    Log.v(TAG, "getDefaultProtocol, IPvalue[0] =" + IPvalue[0] + "  IPvalue[1]=" + IPvalue[1]);
                } else {
                    Log.v(TAG, "getDefaultProtocol, cust_protocol_mccmnc_list config error! ");
                }
                return IPvalue;
            }
        }
        return IPvalue;
    }
}
