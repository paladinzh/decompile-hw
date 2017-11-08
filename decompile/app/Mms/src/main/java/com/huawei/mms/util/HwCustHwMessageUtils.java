package com.huawei.mms.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Message;
import java.util.ArrayList;

public class HwCustHwMessageUtils {
    public void removeSendMsgMenu(Context context, String number, ArrayList<Integer> arrayList) {
    }

    public void processCotaAtlXml(Context context) {
    }

    public void processCotaBtlXml(Context context) {
    }

    public boolean getEnableCotaFeature() {
        return false;
    }

    public void backupMmsConfigXml(Context context) {
    }

    public void processRegionalPhoneXmls(Context context) {
    }

    public boolean isReginalPhoneActivated(Context context) {
        return false;
    }

    public Uri encodeUssdNumUri(String numberUri) {
        return Uri.parse(numberUri);
    }

    public String getUssdNumberTitle(String numberTmp) {
        return numberTmp;
    }

    public boolean isDeviceTimeForRecievingMms() {
        return false;
    }

    public String getMessageQueuedStr(Message msg, String str, Context context) {
        return str;
    }

    public int getToastType(int toastType, int transactionType) {
        return toastType;
    }

    public boolean isInEncrypt(Activity activity) {
        return false;
    }
}
