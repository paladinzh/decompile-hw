package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitNe extends LocaleDigit {
    public LocaleDigitNe() {
        this.pattern = "[०१२३४५६७८९]+";
    }

    public String convert(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('१'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('२'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('३'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('४'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('५'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('६'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('७'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('८'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('९'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('०'), Integer.valueOf(0));
        for (Entry entry : hashMap.entrySet()) {
            str = str.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return str;
    }
}
