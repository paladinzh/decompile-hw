package com.huawei.mms.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.os.SystemProperties;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;
import java.util.ArrayList;

public class HwCustHwMessageUtilsImpl extends HwCustHwMessageUtils {
    private static final String TAG = "HwCustHwMessageUtilsImpl";
    private static final int TOAST_MSG_QUEUED = 1;
    private static boolean mIsSetDeviceTime = SystemProperties.getBoolean("ro.config.device_time_for_mms", false);

    public void removeSendMsgMenu(Context context, String number, ArrayList<Integer> menuItems) {
        if (number.endsWith("#") && MmsConfig.isSmsEnabled(context)) {
            menuItems.remove(1);
        }
    }

    public void processCotaAtlXml(Context context) {
        MmsCotaUtils.processCotaAtlXml(context);
    }

    public void processCotaBtlXml(Context context) {
        MmsCotaUtils.processCotaBtlXml(context);
    }

    public boolean getEnableCotaFeature() {
        return HwCustMmsConfigImpl.getEnableCotaFeature();
    }

    public void backupMmsConfigXml(Context context) {
        MmsRegionalPhoneUtils.backupMmsConfigXml(context);
    }

    public void processRegionalPhoneXmls(Context context) {
        MmsRegionalPhoneUtils.processRegionalPhoneXmls(context);
    }

    public boolean isReginalPhoneActivated(Context context) {
        return MmsRegionalPhoneUtils.isReginalPhoneActivated(context);
    }

    public Uri encodeUssdNumUri(String numberUri) {
        Uri uri = Uri.parse(numberUri);
        if (numberUri.startsWith("tel") && numberUri.endsWith("#")) {
            return Uri.parse(Uri.encode(numberUri));
        }
        return uri;
    }

    public String getUssdNumberTitle(String numberTmp) {
        if (numberTmp != null && numberTmp.startsWith("tel") && numberTmp.endsWith("#")) {
            return numberTmp.substring("tel:".length());
        }
        return numberTmp;
    }

    public String getMessageQueuedStr(Message msg, String str, Context context) {
        if (HwCustMmsConfigImpl.enableMmsQueuedToast() && msg.what == 1) {
            return context.getString(R.string.message_queued);
        }
        return str;
    }

    public int getToastType(int toastType, int transactionType) {
        if (!HwCustMmsConfigImpl.enableMmsQueuedToast()) {
            return toastType;
        }
        if (transactionType == 2) {
            toastType = 1;
        }
        return toastType;
    }

    public boolean isDeviceTimeForRecievingMms() {
        return mIsSetDeviceTime;
    }

    public boolean isInEncrypt(Activity activity) {
        return HwCustMmsConfigImpl.isInEncrypt(activity);
    }
}
