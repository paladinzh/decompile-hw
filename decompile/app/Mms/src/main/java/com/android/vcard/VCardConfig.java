package com.android.vcard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VCardConfig {
    public static int VCARD_TYPE_DEFAULT = -1073741824;
    static String VCARD_TYPE_V21_GENERIC_STR = "v21_generic";
    private static boolean notConvertPhoneNumber = false;
    private static final Set<Integer> sJapaneseMobileTypeSet = new HashSet();
    private static final Map<String, Integer> sVCardTypeMap = new HashMap();

    static {
        sVCardTypeMap.put(VCARD_TYPE_V21_GENERIC_STR, Integer.valueOf(-1073741824));
        sVCardTypeMap.put("v30_generic", Integer.valueOf(-1073741823));
        sVCardTypeMap.put("v21_europe", Integer.valueOf(-1073741820));
        sVCardTypeMap.put("v30_europe", Integer.valueOf(-1073741819));
        sVCardTypeMap.put("v21_japanese_utf8", Integer.valueOf(-1073741816));
        sVCardTypeMap.put("v30_japanese_utf8", Integer.valueOf(-1073741815));
        sVCardTypeMap.put("v21_japanese_mobile", Integer.valueOf(402653192));
        sVCardTypeMap.put("docomo", Integer.valueOf(939524104));
        sJapaneseMobileTypeSet.add(Integer.valueOf(-1073741816));
        sJapaneseMobileTypeSet.add(Integer.valueOf(-1073741815));
        sJapaneseMobileTypeSet.add(Integer.valueOf(402653192));
        sJapaneseMobileTypeSet.add(Integer.valueOf(939524104));
        if (notConvertPhoneNumber) {
            setNotConvertPhoneNumber();
        }
    }

    public static boolean isVersion21(int vcardType) {
        return (vcardType & 3) == 0;
    }

    public static boolean isVersion30(int vcardType) {
        return (vcardType & 3) == 1;
    }

    public static boolean isVersion40(int vcardType) {
        return (vcardType & 3) == 2;
    }

    public static boolean shouldUseQuotedPrintable(int vcardType) {
        return !isVersion30(vcardType);
    }

    public static int getNameOrderType(int vcardType) {
        return vcardType & 12;
    }

    public static boolean usesAndroidSpecificProperty(int vcardType) {
        return (Integer.MIN_VALUE & vcardType) != 0;
    }

    public static boolean usesDefactProperty(int vcardType) {
        return (1073741824 & vcardType) != 0;
    }

    public static boolean shouldRefrainQPToNameProperties(int vcardType) {
        return (shouldUseQuotedPrintable(vcardType) && (268435456 & vcardType) == 0) ? false : true;
    }

    public static boolean appendTypeParamName(int vcardType) {
        return isVersion30(vcardType) || (67108864 & vcardType) != 0;
    }

    public static boolean isJapaneseDevice(int vcardType) {
        return sJapaneseMobileTypeSet.contains(Integer.valueOf(vcardType));
    }

    static boolean refrainPhoneNumberFormatting(int vcardType) {
        return (33554432 & vcardType) != 0;
    }

    public static boolean needsToConvertPhoneticString(int vcardType) {
        return (134217728 & vcardType) != 0;
    }

    public static boolean onlyOneNoteFieldIsAvailable(int vcardType) {
        return vcardType == 939524104;
    }

    public static boolean isDoCoMo(int vcardType) {
        return (536870912 & vcardType) != 0;
    }

    private VCardConfig() {
    }

    public static void setNotConvertPhoneNumber() {
        notConvertPhoneNumber = true;
    }

    public static boolean getNotConvertPhoneNumber() {
        return notConvertPhoneNumber;
    }
}
