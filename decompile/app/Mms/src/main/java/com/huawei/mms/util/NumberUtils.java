package com.huawei.mms.util;

import android.database.Cursor;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.i18n.phonenumbers.CountryCodeToRegionCodeMap;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NumberUtils {
    private static final Pattern CONTACT_PATTERN = Pattern.compile("([A-Za-z]+)");

    public static class AddrMatcher {
        private static final boolean IS_CHINA_REGION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
        private static final boolean IS_CHINA_TELECOM;
        private static int MAT_LEN_MAX = 7;
        private static int MAT_LEN_MIN = 7;
        private static Map<Integer, List<String>> mCountryCallingCodeToRegionCodeMap;

        static {
            boolean z = false;
            if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
                z = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
            }
            IS_CHINA_TELECOM = z;
            mCountryCallingCodeToRegionCodeMap = null;
            decideMatchLengthMin();
            try {
                final Method method = CountryCodeToRegionCodeMap.class.getDeclaredMethod("getCountryCodeToRegionCodeMap", new Class[0]);
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        method.setAccessible(true);
                        return null;
                    }
                });
                mCountryCallingCodeToRegionCodeMap = (Map) method.invoke(CountryCodeToRegionCodeMap.class, new Object[0]);
            } catch (NoSuchMethodException e) {
                MLog.e("Mms::NumberUtils", "Mms:: NoSuchMethodException for getCountryCodeToRegionCodeMap! " + e);
            } catch (SecurityException e2) {
                MLog.e("Mms::NumberUtils", "Mms:: SecurityException for getCountryCodeToRegionCodeMap! " + e2);
            } catch (IllegalAccessException e3) {
                MLog.e("Mms::NumberUtils", "Mms:: IllegalAccessException for getCountryCodeToRegionCodeMap! " + e3);
            } catch (IllegalArgumentException e4) {
                MLog.e("Mms::NumberUtils", "Mms:: IllegalArgumentException for getCountryCodeToRegionCodeMap! " + e4);
            } catch (InvocationTargetException e5) {
                MLog.e("Mms::NumberUtils", "Mms:: InvocationTargetException for getCountryCodeToRegionCodeMap! " + e5);
            } catch (ClassCastException e6) {
                MLog.e("Mms::NumberUtils", "Mms:: ClassCastException for Object to Map! " + e6);
            }
        }

        public static void decideMatchLengthMin() {
            MAT_LEN_MAX = 7;
            MAT_LEN_MIN = 7;
            if (IS_CHINA_REGION && !IS_CHINA_TELECOM && MmsApp.getDefaultTelephonyManager().isNetworkRoaming()) {
                MLog.v("checkNumberMatchLength", "CN in roaming status NUM_LONG = " + MAT_LEN_MAX + ", NUM_SHORT = " + MAT_LEN_MIN);
                return;
            }
            int max = SystemProperties.getInt("ro.config.hwft_MatchNum", 0);
            int min = SystemProperties.getInt("ro.config.hwft_MatchNumShort", 0);
            if (max == 0) {
                max = SystemProperties.getInt("gsm.hw.matchnum", 0);
                min = SystemProperties.getInt("gsm.hw.matchnum.short", 0);
            }
            if (min == 0) {
                min = max;
            }
            if (IS_CHINA_REGION && min == 0 && max == 0) {
                MAT_LEN_MIN = 11;
                MAT_LEN_MAX = 11;
            } else if (min == 0 || max == 0) {
                if (max <= MAT_LEN_MAX) {
                    max = MAT_LEN_MAX;
                }
                MAT_LEN_MAX = max;
                if (min <= MAT_LEN_MIN) {
                    min = MAT_LEN_MIN;
                }
                MAT_LEN_MIN = min;
                MAT_LEN_MIN = MAT_LEN_MIN > MAT_LEN_MAX ? MAT_LEN_MAX : MAT_LEN_MIN;
            } else {
                MAT_LEN_MAX = max;
                MAT_LEN_MIN = min;
                MAT_LEN_MIN = MAT_LEN_MIN > MAT_LEN_MAX ? MAT_LEN_MAX : MAT_LEN_MIN;
            }
            MLog.v("Mms:NumberMatch", "status NUM_LONG = " + MAT_LEN_MAX + ", NUM_SHORT = " + MAT_LEN_MIN);
        }

        public static int isNumberMatch(String addr, String targ) {
            if (addr == null || targ == null) {
                return 0;
            }
            int addrType = getNumberType(addr);
            int targType = getNumberType(targ);
            if (addrType != targType) {
                if ((1 == addrType && targType == 0) || (addrType == 0 && 1 == targType)) {
                    if (addrType == 0) {
                        addr = addr.replaceFirst("\\+", "00");
                    } else {
                        targ = targ.replaceFirst("\\+", "00");
                    }
                } else if (2 == addrType) {
                    addr = addr.replaceFirst("0", "");
                } else if (2 == targType) {
                    targ = targ.replaceFirst("0", "");
                }
            }
            int imx1 = addr.length();
            int imx2 = targ.length();
            int matched = 0;
            while (imx1 > 0 && imx2 > 0) {
                char c1 = addr.charAt(imx1 - 1);
                if (PhoneNumberUtils.isISODigit(c1)) {
                    char c2 = targ.charAt(imx2 - 1);
                    if (!PhoneNumberUtils.isISODigit(c2)) {
                        imx2--;
                    } else if (c1 == c2) {
                        imx1--;
                        imx2--;
                        matched++;
                    } else if (matched >= MAT_LEN_MAX) {
                        return 3;
                    } else {
                        return 0;
                    }
                }
                imx1--;
                if (c1 == targ.charAt(imx2 - 1)) {
                    imx2--;
                }
            }
            if ((imx1 == 0 && imx2 == 0) || (matched > 0 && ((imx1 == 0 && isNoDigitRemained(targ, imx2)) || (imx2 == 0 && isNoDigitRemained(addr, imx1))))) {
                return 9;
            }
            if (imx1 != 0 || -1 == targType) {
                if (imx2 == 0 && -1 != addrType && imx1 > 2 && prefixIsCountryCode(addr, imx1)) {
                    return 9;
                }
            } else if (imx2 > 2 && prefixIsCountryCode(targ, imx2)) {
                return 9;
            }
            if (matched >= MAT_LEN_MAX) {
                return 3;
            }
            return (matched < MAT_LEN_MIN || !(imx1 == 0 || imx2 == 0)) ? 0 : 1;
        }

        public static boolean isNumbersEqualWithoutSign(String number, String compNum) {
            if (number == null || compNum == null) {
                return false;
            }
            int imx1 = number.length();
            int imx2 = compNum.length();
            while (imx1 > 0 && imx2 > 0) {
                char c1 = number.charAt(imx1 - 1);
                if (PhoneNumberUtils.isISODigit(c1)) {
                    char c2 = compNum.charAt(imx2 - 1);
                    if (!PhoneNumberUtils.isISODigit(c2)) {
                        imx2--;
                    } else if (c1 != c2) {
                        return false;
                    } else {
                        imx1--;
                        imx2--;
                    }
                } else {
                    imx1--;
                    if (c1 == compNum.charAt(imx2 - 1)) {
                        imx2--;
                    }
                }
            }
            if ((imx1 == 0 && imx2 == 0) || ((imx1 == 0 && isNoDigitRemained(compNum, imx2)) || (imx2 == 0 && isNoDigitRemained(number, imx1)))) {
                return true;
            }
            return false;
        }

        private static int getNumberType(String number) {
            if (number == null || number.length() < 2) {
                return -1;
            }
            char firstChar = number.charAt(0);
            if ('+' == firstChar) {
                return 0;
            }
            if ('0' == firstChar) {
                return '0' == number.charAt(1) ? 1 : 2;
            } else {
                return -1;
            }
        }

        private static boolean prefixIsCountryCode(String number, int imx) {
            if (mCountryCallingCodeToRegionCodeMap == null) {
                return false;
            }
            int idx = 1;
            int countryCode = 0;
            for (int i = imx - 1; i > 0; i--) {
                char c = number.charAt(i);
                if (PhoneNumberUtils.isISODigit(c)) {
                    countryCode += (c - 48) * idx;
                    idx *= 10;
                }
            }
            return mCountryCallingCodeToRegionCodeMap.containsKey(Integer.valueOf(countryCode));
        }

        public static Contact getPriorityMatchContact(Contact[] contactList, String originNum) {
            int matchType = 0;
            Contact matchedContact = null;
            int size = contactList == null ? 0 : contactList.length;
            for (int i = 0; i < size; i++) {
                Contact c = contactList[i];
                if (matchType == 9 || c == null) {
                    break;
                }
                int tempMatchType = isNumberMatch(c.getNumber(), originNum);
                if (tempMatchType > matchType) {
                    matchedContact = c;
                    matchType = tempMatchType;
                }
            }
            return matchedContact;
        }

        public static Cursor getPriorityMatchCursor(Cursor cursor, String originNum, String columnIndex) {
            int matchType = 0;
            int matchedPos = -1;
            while (cursor.moveToNext() && matchType != 9) {
                int tempMatchType = isNumberMatch(cursor.getString(cursor.getColumnIndexOrThrow(columnIndex)), originNum);
                if (tempMatchType > matchType) {
                    matchedPos = cursor.getPosition();
                    matchType = tempMatchType;
                }
            }
            if (matchedPos == -1 || !cursor.moveToPosition(matchedPos)) {
                return null;
            }
            return cursor;
        }

        public static boolean isNoDigitRemained(String number, int pos) {
            if (pos >= number.length()) {
                return false;
            }
            for (int i = pos; i > 0; i--) {
                if (PhoneNumberUtils.isISODigit(number.charAt(i - 1))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static long emailKey(CharSequence emailNumber) {
        return ((((long) emailNumber.hashCode()) + 2147483647L) + 2147483647L) + 1;
    }

    public static long key(CharSequence phoneNumber) {
        int position = phoneNumber.length();
        int resultCount = 0;
        int key = 0;
        while (true) {
            position--;
            if (position < 0) {
                break;
            }
            char c = phoneNumber.charAt(position);
            if ('0' <= c && c <= '9') {
                key = ((c - 48) + 1) + (key << 4);
                resultCount++;
                if (resultCount == 7) {
                    break;
                }
            }
        }
        if (key == 0) {
            return emailKey(phoneNumber);
        }
        return (long) key;
    }

    public static boolean isMatchedSpecialNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        return CONTACT_PATTERN.matcher(number).find();
    }

    public static String formatNumber(String nubmer, String numberE164) {
        if (nubmer == null) {
            return null;
        }
        if (!Contact.isEmailAddress(nubmer)) {
            nubmer = PhoneNumberUtils.formatNumber(nubmer, numberE164, MmsApp.getApplication().getCurrentCountryIso());
        }
        return nubmer;
    }

    public static String formatAndParseNumber(String nubmer, String numberE164) {
        if (nubmer == null) {
            return null;
        }
        String parseNumber = MessageUtils.parseMmsAddress(nubmer, true);
        if (TextUtils.isEmpty(parseNumber)) {
            parseNumber = nubmer;
        }
        return formatNumber(parseNumber, numberE164);
    }

    public static String normalizeNumber(String phoneNumber) {
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (i == 0 && c == '+') {
                sb.append(c);
            } else {
                if (c < 'a' || c > 'z') {
                    if (c >= 'A' && c <= 'Z') {
                    }
                }
                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    public static final String searchInNumber(char[] number, char[] query) {
        int idn = 0;
        int idq = 0;
        int start = -1;
        while (idn < number.length && idq < query.length) {
            char nch = number[idn];
            char qch = query[idq];
            if (nch == qch) {
                if (start == -1) {
                    start = idn;
                }
                idq++;
                idn++;
            } else if (MessageUtils.isSugarChar(qch)) {
                idq++;
            } else if (MessageUtils.isSugarChar(nch)) {
                idn++;
            } else if (start >= 0) {
                idn = start + 1;
                idq = 0;
                start = -1;
            } else {
                idn++;
            }
            if (idq == query.length) {
                break;
            }
        }
        if (idq != query.length || start < 0 || idn > number.length) {
            return null;
        }
        return String.copyValueOf(number, start, idn - start);
    }

    public static final boolean isHwMessageNumber(String number) {
        if (number == null || number.length() < 11) {
            return false;
        }
        number = number.replaceAll(" ", "");
        for (String prefix : "1065796709,1065502043,1065902090,106575550211,10690133830,1069055999,106550200271,1065902002801,106906060012,106900679901,106900679914,106900679916,106903345801,106903345814,106903345816,106903345820,106903345901,106903345914".split(",")) {
            if (number.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static String getFilterNumber(String searchKey) {
        if (TextUtils.isEmpty(searchKey)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchKey.length(); i++) {
            if ("()- ".indexOf(searchKey.charAt(i)) == -1) {
                sb.append(searchKey.charAt(i));
            }
        }
        return sb.toString();
    }
}
