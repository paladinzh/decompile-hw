package com.android.contacts;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.lang.reflect.Method;

public class HwCustSpecialCharSequenceMgrImpl extends HwCustSpecialCharSequenceMgr {
    private static final String SAR_ACTION = "com.huawei.secretcode.HW_SAR_VALUE";
    private static final String SAR_CODE = "*#07#";
    private static final String SAR_VALUE_NAME = "SAR_VALUE";
    private static final String SHOW_TF_STATUS_ACTION = "com.huawei.action.SHOW_TF_STATUS";
    private static final String SHOW_TF_STATUS_CODE = "#83782887#";
    private static final String TAG = "HwCustSpecialCharSequenceMgrImpl";
    private static final String TF_SIM_UNLOCK_ACTION = "com.huawei.action.TF_SIM_UNLOCK";
    private static final String TF_SIM_UNLOCK_CODE = "#83865625#";
    private String sarValue = null;

    private boolean isHandleSAR(Context context) {
        this.sarValue = getSarValue(context);
        if (this.sarValue == null || "".equals(this.sarValue)) {
            return false;
        }
        return true;
    }

    private String getSarValue(Context context) {
        if (this.sarValue == null) {
            String allSarInfo = System.getString(context.getContentResolver(), "sar_valuebyproductname");
            if (allSarInfo != null) {
                for (String saInfo : allSarInfo.split(";")) {
                    String[] productAndSarValue = saInfo.split(":");
                    if (productAndSarValue.length == 2 && Build.PRODUCT.equals(productAndSarValue[0].trim())) {
                        this.sarValue = productAndSarValue[1].trim();
                        break;
                    }
                }
            }
        }
        return this.sarValue;
    }

    public boolean handleCustSpecialCharSequence(Context context, String input) {
        if (SAR_CODE.equals(input) && isHandleSAR(context)) {
            try {
                Intent intent = new Intent();
                intent.setAction(SAR_ACTION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setFlags(268435456);
                intent.putExtra(SAR_VALUE_NAME, this.sarValue);
                context.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Can not find the SAR activity");
            }
        }
        return false;
    }

    public boolean isEnableCustomSwitch(String lSubString, Context context) {
        if (SystemProperties.getBoolean("ro.config.regionalphone", false)) {
            if (SimFactoryManager.isDualSim()) {
                String imei1 = SimFactoryManager.getDeviceId(0);
                String imei2 = SimFactoryManager.getDeviceId(1);
                if (lSubString.equals(imei1) || lSubString.equals(imei2)) {
                    context.startActivity(new Intent("regionalphone.settingview"));
                    return true;
                }
            } else if (lSubString.equals(SimFactoryManager.getDeviceId(-1))) {
                context.startActivity(new Intent("regionalphone.settingview"));
                return true;
            }
        }
        return false;
    }

    public String customizedMeidDisplay(String meidStr) {
        if (!HwCustContactFeatureUtils.isSupportCustomizedMeid()) {
            return meidStr;
        }
        String imeiStr = TelephonyManager.getDefault().getImei();
        if (TextUtils.isEmpty(imeiStr)) {
            return meidStr;
        }
        return meidStr + "\nIMEI:" + imeiStr;
    }

    public String customizedMeidTitle(Context context, String lTitleString, String meidStr) {
        if (context == null || !HwCustContactFeatureUtils.isSupportCustomizedMeid() || TextUtils.isEmpty(meidStr) || !meidStr.contains("IMEI:")) {
            return lTitleString;
        }
        String imei = context.getResources().getString(R.string.imei);
        if (TextUtils.isEmpty(lTitleString)) {
            return lTitleString;
        }
        return lTitleString + "_" + imei;
    }

    public String customizedImeiDisplay(String imeiStr) {
        if (HwCustContactFeatureUtils.isSupportCustomizedImei()) {
            try {
                String lMeid = HwTelephonyManager.getDefault().getMeid();
                String lPesn = HwTelephonyManager.getDefault().getPesn();
                if (!TextUtils.isEmpty(imeiStr)) {
                    imeiStr = "IMEI:" + imeiStr;
                }
                if (!TextUtils.isEmpty(lMeid)) {
                    imeiStr = imeiStr + "\nMEID:" + lMeid;
                }
                if (!TextUtils.isEmpty(lPesn)) {
                    imeiStr = imeiStr + "\nPESN:" + lPesn;
                }
            } catch (Exception e) {
                Log.e(TAG, "Can not find customized IMEI");
            }
        }
        return imeiStr;
    }

    public String customizedImeiDisplay(Context context, String imeiStr) {
        if (!HwCustContactFeatureUtils.isShowImeiSvn() || context == null || TextUtils.isEmpty(imeiStr)) {
            return imeiStr;
        }
        String imei = context.getString(R.string.imei);
        String svn = context.getString(R.string.att_imei_svn);
        String softwareVersion = TelephonyManager.getDefault().getDeviceSoftwareVersion();
        if (TextUtils.isEmpty(softwareVersion)) {
            return imeiStr;
        }
        return imei + ": " + imeiStr + "\n" + svn + ": " + softwareVersion;
    }

    public String customizedImeiTitle(Context context, String lTitleString, String imeiStr) {
        if (context == null || !HwCustContactFeatureUtils.isSupportCustomizedImei() || TextUtils.isEmpty(imeiStr) || !imeiStr.contains("MEID:")) {
            return lTitleString;
        }
        String meid = context.getResources().getString(R.string.meid);
        if (TextUtils.isEmpty(lTitleString)) {
            return lTitleString;
        }
        return lTitleString + "_" + meid;
    }

    public String getCustomizedMEID(Context context, String aMeidStr) {
        if (context == null || !HwCustContactFeatureUtils.isCustomHideMeid()) {
            return aMeidStr;
        }
        String str = null;
        TelephonyManager lTeleMngr = CommonUtilMethods.getTelephonyManager(context);
        if (lTeleMngr == null) {
            return aMeidStr;
        }
        String imeiStr = lTeleMngr.getImei();
        try {
            Method method = TelephonyManager.class.getDeclaredMethod("getPesn", new Class[0]);
            method.setAccessible(true);
            String lPesn = (String) method.invoke(lTeleMngr, (Object[]) null);
            if (!TextUtils.isEmpty(lPesn)) {
                str = "IMEI:" + imeiStr + "\nPESN:" + lPesn;
            }
        } catch (Exception aEx) {
            aEx.printStackTrace();
        }
        if (str == null) {
            str = imeiStr;
        }
        return str;
    }

    public boolean handleSimUnLockBroadcast(Context context, String input) {
        if (context == null || !HwCustContactFeatureUtils.is2CkNetworkLockEnabled()) {
            return false;
        }
        Intent intent;
        if (TF_SIM_UNLOCK_CODE.equals(input)) {
            intent = new Intent(TF_SIM_UNLOCK_ACTION, Uri.parse("tf_sim_unlock_code://83865625"));
            intent.addFlags(268435456);
            context.sendBroadcast(intent);
            return true;
        } else if (!SHOW_TF_STATUS_CODE.equals(input)) {
            return false;
        } else {
            intent = new Intent(SHOW_TF_STATUS_ACTION, Uri.parse("tf_status_code://83782887"));
            intent.addFlags(268435456);
            context.sendBroadcast(intent);
            return true;
        }
    }

    public boolean checkForDisableHiddenMenuItems(Context context, String inputString) {
        if (!TextUtils.isEmpty(inputString) && inputString.length() > 6 && inputString.startsWith("*#") && (inputString.endsWith("#*#*") || inputString.endsWith("#"))) {
            return HwCustContactFeatureUtils.isDisabledHiddenMenuCode(context, inputString);
        }
        return false;
    }
}
