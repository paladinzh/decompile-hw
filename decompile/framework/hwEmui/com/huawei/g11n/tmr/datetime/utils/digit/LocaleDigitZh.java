package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleDigitZh extends LocaleDigit {
    public LocaleDigitZh() {
        this.pattern = "[0-9零一二三四五六七八九十两整半科钟鍾兩]+";
    }

    public String convert(String str) {
        Object replaceAll = str.replaceAll("半", "30").replaceAll("钟", "00").replaceAll("鍾", "00").replaceAll("整", "00").replaceAll("一刻", "15").replaceAll("三刻", "45");
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('一'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('二'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('三'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('四'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('五'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('六'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('七'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('八'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('九'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('零'), Integer.valueOf(0));
        hashMap.put(Character.valueOf('十'), Integer.valueOf(10));
        hashMap.put(Character.valueOf('两'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('兩'), Integer.valueOf(2));
        Matcher matcher = Pattern.compile("[零一二三四五六七八九十两兩]{1,10}").matcher(replaceAll);
        StringBuffer stringBuffer = new StringBuffer(replaceAll);
        while (matcher.find()) {
            int intValue;
            String group = matcher.group();
            switch (group.length()) {
                case 1:
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue();
                    break;
                case 2:
                    if (group.charAt(0) != '十') {
                        if (group.charAt(1) == '十') {
                            if (group.charAt(0) != '零') {
                                intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 10;
                                break;
                            }
                            intValue = 10;
                            break;
                        }
                        intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(1)))).intValue() + (((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 10);
                        break;
                    }
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(1)))).intValue() + 10;
                    break;
                case 3:
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(2)))).intValue() + (((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 10);
                    break;
                case 4:
                    intValue = ((Integer) hashMap.get(Character.valueOf(group.charAt(3)))).intValue() + (((((Integer) hashMap.get(Character.valueOf(group.charAt(0)))).intValue() * 1000) + (((Integer) hashMap.get(Character.valueOf(group.charAt(1)))).intValue() * 100)) + (((Integer) hashMap.get(Character.valueOf(group.charAt(2)))).intValue() * 10));
                    break;
                default:
                    intValue = 0;
                    break;
            }
            stringBuffer.replace(stringBuffer.indexOf(group), group.length() + stringBuffer.indexOf(group), "" + intValue);
        }
        return stringBuffer.toString();
    }
}
