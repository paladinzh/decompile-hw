package com.huawei.g11n.tmr.datetime.utils;

import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigit;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitBn;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitFa;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitNe;
import com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigitZh;
import java.util.Locale;

public class StringConvert {
    private String convertQanChar(String str) {
        StringBuffer stringBuffer = new StringBuffer("");
        String str2 = "　：／．＼∕，.！（）？﹡；：【】－＋＝｛｝１２３４５６７８９０ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
        String str3 = " :/.\\/,.!()?*;:[]-+={}1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < str.length(); i++) {
            String substring = str.substring(i, i + 1);
            int indexOf = str2.indexOf(substring);
            if (indexOf != -1) {
                stringBuffer.append(str3.substring(indexOf, indexOf + 1));
            } else {
                stringBuffer.append(substring);
            }
        }
        return stringBuffer.toString();
    }

    private String replaceZh(String str) {
        return str.replaceAll("礼拜", "星期").replaceAll("星期天", "星期日").replaceAll("週", "周").replaceAll("周天", "周日").replaceAll("後", "后").replaceAll("個", "个").replaceAll("兩", "两").replaceAll("鍾", "钟");
    }

    public String convertString(String str, String str2) {
        if (str2.equals("zh_hans") || str2.equals("en")) {
            return replaceZh(convertQanChar(str));
        }
        if (str2.equals("fa")) {
            str = convertDigit(str, "fa");
        } else if (str2.equals("ne")) {
            str = convertDigit(str, "ne");
        } else if (str2.equals("bn")) {
            str = convertDigit(str, "bn");
        } else if (str2.equals("ru") || str2.equals("lt") || str2.equals("kk") || str2.equals("be")) {
            str = str.toLowerCase(new Locale(str2));
        }
        return str;
    }

    public String convertDigit(String str, String str2) {
        LocaleDigit localeDigit = getLocaleDigit(str2);
        if (localeDigit != null) {
            return localeDigit.convert(str);
        }
        return str;
    }

    public boolean isDigit(String str, String str2) {
        LocaleDigit localeDigit = getLocaleDigit(str2);
        if (localeDigit != null) {
            return localeDigit.isDigit(str);
        }
        return false;
    }

    public LocaleDigit getLocaleDigit(String str) {
        if (str.equals("zh_hans") || str.equals("ja") || str.equals("en")) {
            return new LocaleDigitZh();
        }
        if (str.equals("fa")) {
            return new LocaleDigitFa();
        }
        if (str.equals("ne")) {
            return new LocaleDigitNe();
        }
        if (str.equals("bn")) {
            return new LocaleDigitBn();
        }
        return null;
    }
}
