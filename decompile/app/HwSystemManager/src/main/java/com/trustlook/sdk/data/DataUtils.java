package com.trustlook.sdk.data;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Secure;
import com.trustlook.sdk.Constants;

public class DataUtils {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void saveRegionValue(Context context, Region region) {
        int i = 0;
        if (Region.CHN == region) {
            i = 1;
        }
        saveIntValue(context, Constants.CLIENT_REGION, i);
    }

    public static void saveIntValue(Context context, String str, int i) {
        Editor edit = context.getSharedPreferences(Constants.PREFERENCE_NAME, 0).edit();
        edit.putInt(str, i);
        edit.commit();
    }

    public static void saveStringValue(Context context, String str, String str2) {
        Editor edit = context.getSharedPreferences(Constants.PREFERENCE_NAME, 0).edit();
        edit.putString(str, str2);
        edit.commit();
    }

    public static Region getRegionValue(Context context, int i) {
        int intValue = getIntValue(context, Constants.CLIENT_REGION, i);
        if (intValue == 1) {
            return Region.CHN;
        }
        if (intValue != 0) {
            return Region.INTL;
        }
        return Region.INTL;
    }

    public static int getIntValue(Context context, String str, int i) {
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, 0).getInt(str, i);
    }

    public static String getStringValue(Context context, String str, String str2) {
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, 0).getString(str, str2);
    }

    public static String getAndroidId(Context context) {
        String string = Secure.getString(context.getContentResolver(), "android_id");
        new StringBuilder("before = ").append(string);
        if (string == null) {
            return string;
        }
        string = a(string);
        new StringBuilder("encrypt = ").append(string);
        return string;
    }

    private static String a(String str) {
        String toLowerCase = str.toLowerCase();
        StringBuffer stringBuffer = new StringBuffer();
        int length = toLowerCase.length();
        for (int i = 0; i < length; i++) {
            char charAt = toLowerCase.charAt(i);
            if (charAt >= 'a' && charAt <= 'z') {
                charAt = (char) (charAt + 2);
                if (charAt > 'z') {
                    charAt = (char) (charAt - 26);
                }
            }
            if (charAt >= '0' && charAt <= '9') {
                charAt = (char) (charAt + 2);
                if (charAt > '9') {
                    charAt = (char) (charAt - 10);
                }
            }
            stringBuffer.append(charAt);
        }
        return stringBuffer.toString();
    }
}
