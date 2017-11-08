package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_ES_ES extends PhoneNumberRule {
    public PhoneNumberRule_ES_ES(String str) {
        super(str);
        init();
    }

    public void init() {
        String str = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        arrayList.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + str + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + str + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        arrayList.add(new RegexRule("(?<![\\p{L}])(ci|ref|plus(\\p{Blank}+de)?|i\\.?d|sn|tweets?|twitter|icq|qq)[\\p{Blank}:]*[0-9]{4,16}", 66));
        arrayList.add(new RegexRule("[0-9.,]{4,16}(?<![.,])\\p{Blank}*(" + "minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kilómetros|MB|m²|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|picómetros|W|V|millas\\p{Blank}+náuticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|centímetros|gramos|ms|a|l|m\\.|m|kilogramos|hectáreas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm²|galones|centímetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|Ω|kb|días|julios|gigabytes|pintas|kilobits|km|milímetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|°|lb|yardas|calorías|metros|litros|decímetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalorías|°C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|°F|yd|milisegundos|terabits|kcal|años|kJ|pt|terabytes|kB|hercios" + "|" + "tweet|millones|mil" + ")(?![\\p{L}0-9])", 66));
        String str2 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(" + "ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre" + ")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
        ConstantsUtils constantsUtils = new ConstantsUtils();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.FLOAT_1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        arrayList.add(new RegexRule(str2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        this.negativeRules = arrayList;
        arrayList2.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = arrayList2;
        this.codesRules = new ArrayList();
        this.codesRules.add(new RegexRule(this, "") {
            public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
                int i = 0;
                char[] toCharArray;
                int i2;
                char c;
                if (phoneNumberMatch.start() - 1 >= 0) {
                    toCharArray = str.substring(0, phoneNumberMatch.start()).toCharArray();
                    i2 = 0;
                    while (i2 < toCharArray.length) {
                        c = toCharArray[(toCharArray.length - 1) - i2];
                        if (i2 == 0) {
                            if (!Character.isUpperCase(c)) {
                                break;
                            }
                        }
                        if (i2 < 2 && Character.isLetter(c)) {
                            if (!Character.isUpperCase(c)) {
                                break;
                            }
                            i2++;
                        } else if (c != '-' && c != '\'') {
                            if (Character.isDigit(c)) {
                                i = 1;
                            } else if (Character.isWhitespace(c)) {
                                i = 1;
                            } else if (!Character.isLetter(c)) {
                                i = 1;
                            }
                        }
                    }
                    i = 1;
                    if (i != 0) {
                        return null;
                    }
                    return phoneNumberMatch;
                } else if (phoneNumberMatch.end() > str.length() - 1) {
                    return phoneNumberMatch;
                } else {
                    toCharArray = str.substring(phoneNumberMatch.end()).toCharArray();
                    i2 = 0;
                    while (i2 < toCharArray.length) {
                        c = toCharArray[i2];
                        if (i2 == 0) {
                            if (!Character.isUpperCase(c)) {
                                break;
                            }
                        }
                        if (i2 < 2 && Character.isLetter(c)) {
                            if (!Character.isUpperCase(c)) {
                                break;
                            }
                        } else if (i2 == 1 || i2 == 2) {
                            if (c != '-' && c != '\'') {
                                if (Character.isDigit(c)) {
                                    i = 1;
                                } else if (Character.isWhitespace(c)) {
                                    i = 1;
                                } else if (!Character.isLetter(c)) {
                                    i = 1;
                                }
                            }
                        }
                        i2++;
                    }
                    i = 1;
                    if (i != 0) {
                        return null;
                    }
                    return phoneNumberMatch;
                }
            }
        });
    }
}
