package com.huawei.numberlocation;

import com.android.contacts.util.ExceptionCapture;

public class NumberLocationDb {
    String t = null;

    private static native String queryAreaCodebyPhoneNumber(String str, String str2);

    private static native String queryOPNamebyPhoneNumber(String str, String str2);

    private static native String queryPhoneNumberLocation(String str, String str2);

    private static native String queryTelNumberLocation(String str, String str2);

    static {
        try {
            System.loadLibrary("contactsnaqtodat");
            LogExt.d("NaqToDat", "The library lib naqtodat.so be loaded!");
        } catch (UnsatisfiedLinkError e) {
            LogExt.d("NaqToDat", "The library lib naqtodat.so could not be loaded");
            ExceptionCapture.captureNumLocationDBException("The lib naqtodat.so could not be loaded", null);
        } catch (SecurityException e2) {
            LogExt.d("NaqToDat", "The library lib naqtodat.so was not allowed to be loaded");
            ExceptionCapture.captureNumLocationDBException("The library lib naqtodat.so was not allowed to be loaded", null);
        }
    }

    public static String queryUnicodeOPNamebyPhoneNumber(String phoneNum, String datFile) {
        String fullUnicodeInformation = null;
        if (phoneNum == null || datFile == null) {
            return null;
        }
        try {
            fullUnicodeInformation = queryOPNamebyPhoneNumber(phoneNum, datFile);
        } catch (UnsatisfiedLinkError e) {
            LogExt.d("NaqToDat", "queryOpNmaebyPhoneNumber error");
        }
        if (fullUnicodeInformation == null || 3 >= fullUnicodeInformation.length()) {
            return null;
        }
        int actualNum = Integer.parseInt(fullUnicodeInformation.substring(1, 2));
        if (actualNum + 2 > fullUnicodeInformation.length()) {
            return null;
        }
        return fullUnicodeInformation.substring(2, actualNum + 2);
    }

    public static String queryUnicodeAreaCodeByPhoneNum(String phoneNum, String datFile) {
        String areaCode = null;
        if (phoneNum == null || datFile == null) {
            return null;
        }
        try {
            areaCode = queryAreaCodebyPhoneNumber(phoneNum, datFile);
        } catch (UnsatisfiedLinkError e) {
            LogExt.d("NaqToDat", "queryAreaCodebyPhoneNumber error");
        }
        if (areaCode == null || 2 >= areaCode.length()) {
            return null;
        }
        return areaCode;
    }

    public static String queryUnicodeInformationByPhoneNum(String phoneNum, String datFile) {
        if (phoneNum == null || datFile == null) {
            return null;
        }
        String str = null;
        try {
            str = queryPhoneNumberLocation(phoneNum, datFile);
        } catch (UnsatisfiedLinkError e) {
            LogExt.d("NaqToDat", "queryPhoneUnicodeInformation error");
        }
        if (str == null || 3 >= str.length()) {
            return null;
        }
        int actualNum = Integer.parseInt(str.substring(1, 2));
        if (actualNum + 2 > str.length()) {
            return null;
        }
        return str.substring(2, actualNum + 2);
    }

    public static String queryUnicodeInformationByTelNum(String TelNum, String datFile) {
        if (TelNum == null || datFile == null) {
            return null;
        }
        String str = null;
        try {
            str = queryTelNumberLocation(TelNum, datFile);
        } catch (UnsatisfiedLinkError e) {
            LogExt.d("NaqToDat", "queryTelUnicodeInformation error");
        }
        if (str == null || 3 >= str.length()) {
            LogExt.d("NaqToDat", "no such distriction code!");
            return null;
        }
        int actualNum = Integer.parseInt(str.substring(1, 2));
        if (actualNum + 2 > str.length()) {
            return null;
        }
        return str.substring(2, actualNum + 2);
    }
}
