package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitFa extends LocaleDigit {
    public LocaleDigitFa() {
        this.pattern = "[۰۱۲۳۴۵۶۷۸۹]+";
    }

    public String convert(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('۱'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('۲'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('۳'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('۴'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('۵'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('۶'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('۷'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('۸'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('۹'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('۰'), Integer.valueOf(0));
        for (Entry entry : hashMap.entrySet()) {
            str = str.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return str;
    }
}
