package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_PT_PT extends PhoneNumberRule {
    public PhoneNumberRule_PT_PT(String str) {
        super(str);
        init();
    }

    public void init() {
        List arrayList = new ArrayList();
        String str = "FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?";
        ConstantsUtils constantsUtils = new ConstantsUtils();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.FLOAT_1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.YEAR_PERIOD)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        String str2 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(" + "jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez|janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro" + ")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
        arrayList.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + str + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + str + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        arrayList.add(new RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(" + str + ")", 66));
        str = "(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(" + "minutos|segundos|kilobytes|megabytes|toneladas|dias|kilohertz|byte|metros\\p{Blank}+quadrados|quilómetros|MB|decilitros|m²|MW|mililitros|gigahertz|pints|oz|ha|bit|hectopascais|Tb|pm|gal|Mb|milissegundos|bytes|quilocalorias|A|hPa|GB|jardas|J|K|picómetros|W|V|quilojoules|kelvins|mg|dm|dl|megawatts|ml|kHz|joules|min|quilogramas|g|sem\\.|mm|graus|miliamperes|miligramas|Gb|centímetros|ms|l|milhas\\p{Blank}+náuticas|m|nmi|h|kWh|s|quilates|semanas|volts|kW|horas|cm²|calorias|libras|meses|anos|kg|Ω|quilowatts|amperes|kb|quilowatts-hora|gigabytes|hertz|megahertz|watts|kilobits|onças|ct|km|milímetros|Hz|cm|GHz|gigabits|mA|graus\\p{Blank}+Celsius|°|centímetros\\p{Blank}+quadrados|lb|metros|ohms|litros|hectares|decímetros|bits|ton|cal|MHz|TB|megabits|°C|°F|yd|terabits|kcal|galões|kJ|pt|gramas|terabytes|graus\\p{Blank}+Fahrenheit|kB" + ")(?![\\p{L}0-9])";
        arrayList.add(new RegexRule("(?<![\\p{L}])((ref|id|num|qq|ICQ|tweets?|twitter|(decreto|consulta)\\p{Blank}+nº|Série|nif|FM|contas)\\p{Blank}*:?)\\p{Blank}*[0-9]{4,16}", 66));
        arrayList.add(new RegexRule(str2, 2));
        arrayList.add(new RegexRule(str, 2));
        arrayList.add(new RegexRule("(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(vezes|(de\\p{Blank}+)?tweets?|twittar|rts|temporada|contas|capitulos|páginas|no\\p{Blank}+total\\p{Blank}+em\\p{Blank}+dinheiro|milhoes|mil)(?!\\p{L})", 2));
        this.negativeRules = arrayList;
        arrayList = new ArrayList();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = arrayList;
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
