package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_FR_FR extends PhoneNumberRule {
    public PhoneNumberRule_FR_FR(String str) {
        super(str);
        init();
    }

    public void init() {
        String str = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
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
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        arrayList.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + str + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + str + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        arrayList.add(new RegexRule("(?<![\\p{L}])(plus(\\p{Blank}+de)?|faire|n°(\\p{Blank}+d'entreprise)|fais|(ref|id|num|qq|ICQ|tweets?|twitter)[\\p{Blank}:]*|du|prime|dépassé\\p{Blank}les|yen|tapes)\\p{Blank}*[0-9\\p{P}]{4,16}", 66));
        arrayList.add(new RegexRule("[0-9.,]{4,16}\\p{Blank}*(((de|i?[èe]me)\\p{Blank}+)?fois|" + "gallons|degrés\\p{Blank}+Celsius|pintes|kilojoules|kilohertz|calories|tonnes\\p{Blank}+courtes|m²|MW|gigaoctets|millisecondes|To|gigahertz|mégaoctets|oz|ha|secondes|bit|octets|Tb|centimètres\\p{Blank}+carrés|décilitres|pm|gal|Mb|kilogrammes|A|hPa|onces|degrés|carats|Mo|J|K|degrés\\p{Blank}+Fahrenheit|mètres|W|V|mégabits|kelvins|mg|dm|dl|hectopascals|térabits|ml|kHz|téraoctets|joules|min|sem\\.|g|mm|Gb|ms|kilooctets|l|m|j|nmi|h|yards|kWh|s|millimètres|octet|volts|kilocalories|kW|kilomètres|cm²|Go|grammes|décimètres|kg|Ω|kb|mois|ans|kilowatts|sh\\p{Blank}+tn|hertz|kilowattheures|watts|ko|kilobits|litres|jours|ct|km|pte|ampères|milligrammes|Hz|cm|GHz|gigabits|mA|°|lb|milliampères|mètres\\p{Blank}+carrés|millilitres|picomètres|ohms|hectares|centimètres|heures|bits|milles\\p{Blank}+marins|semaines|cal|MHz|°C|minutes|°F|yd|kcal|mégahertz|kJ|mégawatts|livres" + ")(?![\\p{L}0-9])", 66));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.YEAR_PERIOD)));
        arrayList.add(new RegexRule("([012]?\\d|3[01])(-|\\p{Blank}){0,2}(janv\\.|févr\\.|mars|avr\\.|mai|juin|juil\\.|août|sept\\.|oct\\.|nov\\.|déc\\.|janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)\\p{Blank}{0,2}(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?", 66));
        this.negativeRules = arrayList;
        arrayList2.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = arrayList2;
        this.codesRules = new ArrayList();
        this.codesRules.add(new RegexRule(this, "") {
            public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
                String rawString = phoneNumberMatch.rawString();
                if (rawString.trim().startsWith("+") || rawString.trim().startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) || rawString.trim().startsWith("(+") || rawString.trim().startsWith("(0")) {
                    return phoneNumberMatch;
                }
                return null;
            }
        });
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
