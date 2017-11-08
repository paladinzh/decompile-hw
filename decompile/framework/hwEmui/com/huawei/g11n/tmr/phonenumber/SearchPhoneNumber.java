package com.huawei.g11n.tmr.phonenumber;

public class SearchPhoneNumber {
    private static volatile AbstractPhoneNumberMatcher instance = null;

    private static synchronized AbstractPhoneNumberMatcher getInstance(String str) {
        AbstractPhoneNumberMatcher abstractPhoneNumberMatcher;
        synchronized (SearchPhoneNumber.class) {
            if (instance == null) {
                instance = new PhoneNumberMatcher(str);
            } else if (!instance.getCountry().equals(str.trim())) {
                instance = new PhoneNumberMatcher(str);
            }
            abstractPhoneNumberMatcher = instance;
        }
        return abstractPhoneNumberMatcher;
    }

    public static int[] getMatchedPhoneNumber(String str, String str2) {
        return getInstance(str2).getMatchedPhoneNumber(str, str2);
    }
}
