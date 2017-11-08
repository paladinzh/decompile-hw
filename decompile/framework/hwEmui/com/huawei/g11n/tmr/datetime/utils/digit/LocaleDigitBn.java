package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitBn extends LocaleDigit {
    public LocaleDigitBn() {
        this.pattern = "[০১২৩৪৫৬৭৮৯]+";
    }

    public String convert(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('১'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('২'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('৩'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('৪'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('৫'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('৬'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('৭'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('৮'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('৯'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('০'), Integer.valueOf(0));
        for (Entry entry : hashMap.entrySet()) {
            str = str.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return str;
    }
}
