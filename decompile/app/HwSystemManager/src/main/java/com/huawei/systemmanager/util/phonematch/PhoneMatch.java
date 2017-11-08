package com.huawei.systemmanager.util.phonematch;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PhoneMatch {
    private static boolean INIT_LOG_FLAG = false;
    private static final int MIN_MATCH_LENGTH = getMinMatchLengthForPhoneNumber();
    private static int NUM_LONG = 0;
    private static int NUM_SHORT = 0;
    private static boolean PHONE_MATCH_CONFIGURED = false;
    private static final String TAG = "PhoneMatch";

    public static synchronized PhoneMatchInfo getPhoneNumberMatchInfo(String phone) {
        synchronized (PhoneMatch.class) {
            PhoneMatchInfo phoneMatchInfo = new PhoneMatchInfo(phone, false);
            if (TextUtils.isEmpty(phone)) {
                HwLog.w(TAG, "getPhoneNumberMatchInfo: Invalid phone number");
                return phoneMatchInfo;
            }
            boolean isExactMatch;
            initPhoneMatchCust();
            phone = PhoneNumberUtils.stripSeparators(phone);
            int nPhoneLength = phone.length();
            if (nPhoneLength >= NUM_LONG) {
                phone = phone.substring(nPhoneLength - NUM_LONG);
                isExactMatch = false;
            } else if (nPhoneLength >= NUM_SHORT) {
                phone = phone.substring(nPhoneLength - NUM_SHORT);
                isExactMatch = false;
            } else {
                isExactMatch = true;
            }
            phoneMatchInfo.setPhoneNumber(phone);
            phoneMatchInfo.setIsExactMatch(isExactMatch);
            return phoneMatchInfo;
        }
    }

    public static int getMinMatchLengthForPhoneNumber() {
        try {
            final Field field = Class.forName("android.telephony.PhoneNumberUtils").getDeclaredField("MIN_MATCH");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            int nMatchLength = ((Integer) field.get(null)).intValue();
            if (nMatchLength < 7) {
                nMatchLength = 7;
            }
            return nMatchLength;
        } catch (Exception e) {
            HwLog.w(TAG, "getMinMatchLengthForPhoneNumber Exception" + e);
            return 7;
        } catch (Error error) {
            HwLog.w(TAG, "getMinMatchLengthForPhoneNumber Error" + error);
            return 7;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized void initPhoneMatchCust() {
        synchronized (PhoneMatch.class) {
            if (PHONE_MATCH_CONFIGURED) {
                return;
            }
            int hwMatchLength = SystemProperties.getInt(PhoneMatchConst.PROPERTY_HW_NUM_MATCH, 0);
            if (hwMatchLength == 0) {
                int numMatch = SystemProperties.getInt(PhoneMatchConst.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 0);
                int numMatchShort = SystemProperties.getInt(PhoneMatchConst.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, 0);
                if (numMatch == 0 && numMatchShort == 0) {
                    NUM_LONG = MIN_MATCH_LENGTH;
                    NUM_SHORT = MIN_MATCH_LENGTH;
                } else {
                    int i;
                    if (numMatch < MIN_MATCH_LENGTH) {
                        i = MIN_MATCH_LENGTH;
                    } else {
                        i = numMatch;
                    }
                    NUM_LONG = i;
                    if (numMatchShort == 0) {
                        numMatchShort = numMatch;
                    }
                    if (numMatchShort > NUM_LONG) {
                        numMatchShort = NUM_LONG;
                    }
                    NUM_SHORT = numMatchShort;
                    PHONE_MATCH_CONFIGURED = true;
                    HwLog.i(TAG, "initPhoneMatchCust config is true, from PROPERTY_GLOBAL_VERSION_NUM_MATCH, " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
                }
            } else {
                NUM_LONG = hwMatchLength < MIN_MATCH_LENGTH ? MIN_MATCH_LENGTH : hwMatchLength;
                int hwMatchNumShort = SystemProperties.getInt(PhoneMatchConst.PROPERTY_HW_NUM_MATCH_SHORT, NUM_LONG);
                if (hwMatchNumShort > NUM_LONG) {
                    hwMatchNumShort = NUM_LONG;
                }
                NUM_SHORT = hwMatchNumShort;
                PHONE_MATCH_CONFIGURED = true;
                HwLog.i(TAG, "initPhoneMatchCust config is true, from PROPERTY_HW_NUM_MATCH_SHORT, " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
            }
            if (!INIT_LOG_FLAG) {
                HwLog.i(TAG, "initPhoneMatchCust: ro.config.hwft_MatchNum = " + hwMatchLength + ", Min match length = " + MIN_MATCH_LENGTH);
                INIT_LOG_FLAG = true;
            }
            if (PHONE_MATCH_CONFIGURED) {
                HwLog.i(TAG, "initPhoneMatchCust success, NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
            }
        }
    }

    public static synchronized boolean isConfigured() {
        boolean z;
        synchronized (PhoneMatch.class) {
            z = PHONE_MATCH_CONFIGURED;
        }
        return z;
    }

    public static void outputPhoneMathConfig() {
        synchronized (PhoneMatch.class) {
            HwLog.i(TAG, "PhoneMathConfig: isConfiged = " + PHONE_MATCH_CONFIGURED + ", MIN_MATCH = " + MIN_MATCH_LENGTH + ", NUM_SHORT = " + NUM_SHORT + ", NUM_LONG = " + NUM_LONG);
        }
    }

    public static String getDefalutChinaMatchPhoneNumber(String pNum) {
        if (TextUtils.isEmpty(pNum)) {
            return "";
        }
        String phoneNum = PhoneNumberUtils.stripSeparators(pNum);
        if (phoneNum == null) {
            return "";
        }
        int lenth = phoneNum.length();
        if (lenth <= 11) {
            return phoneNum;
        }
        return phoneNum.substring(lenth - 11);
    }
}
