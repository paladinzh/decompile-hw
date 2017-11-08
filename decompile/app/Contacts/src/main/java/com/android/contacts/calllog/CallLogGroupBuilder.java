package com.android.contacts.calllog;

import android.database.Cursor;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.util.SparseArray;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class CallLogGroupBuilder {
    private static final boolean sUseCallerInfo = SystemProperties.getBoolean("ro.config.hw_caller_info", true);

    public static HashSet<Long> getGroups(Cursor cursor, SparseArray<Integer> listPos) {
        RuntimeException e;
        int p;
        int count = cursor.getCount();
        LinkedHashSet<Long> groupSet = new LinkedHashSet();
        if (count == 0) {
            return groupSet;
        }
        String firstNumber;
        int currentGroupSize = 1;
        cursor.moveToFirst();
        if (CommonUtilMethods.isIpCallEnabled()) {
            firstNumber = CommonUtilMethods.deleteIPHead(cursor.getString(1));
        } else {
            firstNumber = cursor.getString(1);
        }
        String firstNumberCountryIso = cursor.getString(5);
        String firstNumberName = cursor.getString(8);
        int firstCallType = cursor.getInt(4);
        int firstFeatures = cursor.getInt(20);
        String firstPostDialDigits = CompatUtils.isNCompatible() ? cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
        String currentPostDialDigits = "";
        String currentViaNumbers = "";
        int p2 = 0;
        while (cursor.moveToNext()) {
            String currentNumber;
            if (CommonUtilMethods.isIpCallEnabled()) {
                currentNumber = CommonUtilMethods.deleteIPHead(cursor.getString(1));
            } else {
                try {
                    currentNumber = cursor.getString(1);
                } catch (RuntimeException e2) {
                    e = e2;
                    p = p2;
                }
            }
            String currentNumberCountryIso = cursor.getString(5);
            String currentNumberName = cursor.getString(8);
            int callType = cursor.getInt(4);
            int features = cursor.getInt(20);
            currentPostDialDigits = CompatUtils.isNCompatible() ? cursor.getString(CallLogQuery.POST_DIAL_DIGITS) : "";
            boolean shouldGroup = false;
            if ((!(firstCallType == 1 || firstCallType == 2) || (callType != 1 && (callType != 2 || features == 32))) && !(CommonUtilMethods.isMissedType(firstCallType) && CommonUtilMethods.isMissedType(callType))) {
                if (firstFeatures == 32 && features == 32) {
                }
                if (shouldGroup) {
                    if (currentGroupSize > 1) {
                        groupSet.add(Long.valueOf(getGroupToken(cursor.getPosition() - currentGroupSize, currentGroupSize)));
                    }
                    currentGroupSize = 1;
                    p = p2 + 1;
                    try {
                        listPos.put(cursor.getPosition() - 1, Integer.valueOf(p2));
                        firstNumber = currentNumber;
                        firstNumberCountryIso = currentNumberCountryIso;
                        firstNumberName = currentNumberName;
                        firstCallType = callType;
                        firstFeatures = features;
                        firstPostDialDigits = currentPostDialDigits;
                    } catch (RuntimeException e3) {
                        e = e3;
                    }
                } else {
                    currentGroupSize++;
                    listPos.put(cursor.getPosition() - 1, Integer.valueOf(p2));
                    p = p2;
                }
                p2 = p;
            }
            if (CommonUtilMethods.equalByNameOrNumber(firstNumberName, firstNumber, currentNumberName, currentNumber) && equalNumbers(firstNumber, firstNumberCountryIso, currentNumber, currentNumberCountryIso) && currentPostDialDigits.equals(firstPostDialDigits)) {
                shouldGroup = true;
                if (firstFeatures == 32 && features != 32) {
                    shouldGroup = false;
                }
            }
            if (shouldGroup) {
                if (currentGroupSize > 1) {
                    groupSet.add(Long.valueOf(getGroupToken(cursor.getPosition() - currentGroupSize, currentGroupSize)));
                }
                currentGroupSize = 1;
                p = p2 + 1;
                listPos.put(cursor.getPosition() - 1, Integer.valueOf(p2));
                firstNumber = currentNumber;
                firstNumberCountryIso = currentNumberCountryIso;
                firstNumberName = currentNumberName;
                firstCallType = callType;
                firstFeatures = features;
                firstPostDialDigits = currentPostDialDigits;
            } else {
                currentGroupSize++;
                listPos.put(cursor.getPosition() - 1, Integer.valueOf(p2));
                p = p2;
            }
            p2 = p;
        }
        p = p2;
        listPos.put(cursor.getCount() - 1, Integer.valueOf(p));
        if (currentGroupSize > 1) {
            groupSet.add(Long.valueOf(getGroupToken(count - currentGroupSize, currentGroupSize)));
        }
        return groupSet;
        HwLog.e("CallLogGroupBuilder", "Make sure the Cursor is initialized correctly before accessing data from it; Exception info --> " + e.getMessage());
        listPos.put(cursor.getCount() - 1, Integer.valueOf(p));
        if (currentGroupSize > 1) {
            groupSet.add(Long.valueOf(getGroupToken(count - currentGroupSize, currentGroupSize)));
        }
        return groupSet;
    }

    private static long getGroupToken(int cursorPosition, int size) {
        return (((long) size) << 32) | ((long) cursorPosition);
    }

    @VisibleForTesting
    boolean equalNumbers(String number1, String number2) {
        if (PhoneNumberUtils.isUriNumber(number1) || PhoneNumberUtils.isUriNumber(number2)) {
            return compareSipAddresses(number1, number2);
        }
        if (number1 != null && number2 != null && number1.length() > 1 && number2.length() > 1 && (number1.codePointAt(number1.length() - 1) != number2.codePointAt(number2.length() - 1) || number1.codePointAt(number1.length() - 2) != number2.codePointAt(number2.length() - 2))) {
            return false;
        }
        if (sUseCallerInfo) {
            return CommonUtilMethods.compareNumsHw(number1, number2);
        }
        return PhoneNumberUtils.compare(number1, number2);
    }

    static boolean equalNumbers(String number1, String countryIso1, String number2, String countryIso2) {
        if (PhoneNumberUtils.isUriNumber(number1) || PhoneNumberUtils.isUriNumber(number2)) {
            return compareSipAddresses(number1, number2);
        }
        if (number1 != null && number2 != null && number1.length() > 1 && number2.length() > 1 && (number1.codePointAt(number1.length() - 1) != number2.codePointAt(number2.length() - 1) || number1.codePointAt(number1.length() - 2) != number2.codePointAt(number2.length() - 2))) {
            return false;
        }
        if (sUseCallerInfo) {
            return CommonUtilMethods.compareNumsHw(number1, countryIso1, number2, countryIso2);
        }
        return PhoneNumberUtils.compare(number1, number2);
    }

    @VisibleForTesting
    static boolean compareSipAddresses(String number1, String number2) {
        boolean z = false;
        if (number1 == null || number2 == null) {
            return false;
        }
        String userinfo1;
        String rest1;
        String userinfo2;
        String rest2;
        int index1 = number1.indexOf(64);
        if (index1 != -1) {
            userinfo1 = number1.substring(0, index1);
            rest1 = number1.substring(index1);
        } else {
            userinfo1 = number1;
            rest1 = "";
        }
        int index2 = number2.indexOf(64);
        if (index2 != -1) {
            userinfo2 = number2.substring(0, index2);
            rest2 = number2.substring(index2);
        } else {
            userinfo2 = number2;
            rest2 = "";
        }
        if (userinfo1.equals(userinfo2)) {
            z = rest1.equalsIgnoreCase(rest2);
        }
        return z;
    }
}
