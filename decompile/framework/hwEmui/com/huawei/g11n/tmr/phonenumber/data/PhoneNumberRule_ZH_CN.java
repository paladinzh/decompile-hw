package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.Leniency;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRule_ZH_CN extends PhoneNumberRule {
    public PhoneNumberRule_ZH_CN(String str) {
        super(str);
        init();
    }

    public void init() {
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        this.codesRules = new ArrayList();
        arrayList.add(new RegexRule("(?<![a-zA-Z_0-9.@])((https?|ftp)://)?([a-zA-Z_0-9][a-zA-Z0-9_-]*(\\.[a-zA-Z0-9_-]{1,20})*\\.(org|com|edu|net|[a-z]{2})|(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2}\\.(?!0)[1-2]?[0-9]{1,2})(?![a-zA-Z0-9_.])(:[1-9][0-9]{0,4})?(/([a-zA-Z0-9/_.\\p{Punct}]*(\\?\\S+)?)?)?(?![a-zA-Z_0-9])"));
        arrayList.add(new RegexRule("\\d{3,17}(kg|千克|毫升|mL|(平|立)方米|(m²)|(m³)|((平方|立方)?分米)|((平方|立方)?厘米)|((平方|立方)?毫米)|(千米)|(英尺)|(公里)|(公斤))(?!\\p{Alpha})", 2));
        arrayList.add(new RegexRule("第\\d{3,17}(只|次|页|条|个|句)"));
        arrayList.add(new RegexRule("(\\d{1,16}\\p{Blank}*[.．~～]\\p{Blank}*)+\\d{1,16}"));
        arrayList.add(new RegexRule("(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))"));
        arrayList.add(new RegexRule("[a-zA-Z_0-9]{1,20}@[a-zA-Z_0-9]{1,20}\\.[A-Za-z]{1,10}"));
        arrayList.add(new RegexRule("(代金券|(账|帐)户号?|ID|id|验证码|校验码|动态码|密码|卡号|票号|单号|订单号?|证号|身份证(号码?)?|学号|邮编|代号|编号|昵称|(账|帐)号名?)(是|为)?\\p{Blank}*[:：]?\\p{Blank}*[A-Za-z0-9_-]{1,30}"));
        arrayList.add(new RegexRule("((WEIXIN|WeiBo|yy|qq)号?|群号|微博号?|微信号?|编(号|码))(是|为)?\\p{Blank}*[:：]?\\p{Blank}*\\d{4,17}", 2));
        arrayList.add(new RegexRule("(?<!\\d)201[0-9](0?[1-9]|1[0-2])(0?[1-9]|[1-2][0-9]|3[01])(?!\\d)"));
        arrayList.add(new RegexRule("(\\d{1,16}[*.]{2,8})+(\\d{1,8})?"));
        arrayList.add(new RegexRule("((\\d{1,16}(\\.)?\\d{1,10})(\\p{Sc}|印尼盾|美元|亿元|十万元?|百万元?|千万元?|万元|((港|澳|新?台|日)(币|元))|人民币))|((((港|澳|新?台|日)(币|元))|人民币|\\p{Sc}|标价为?|售价为?|价格为?)[:：]?(\\d{1,16}(\\.)?\\d{1,16}))"));
        arrayList.add(new RegexRule("[A-Za-z]{1,20}(?<!(mobile|phone|tel(ephone(\\p{Blank}{1,4}number)?)?))[\\d-]{3,11}(?![-\\d])", 2));
        arrayList.add(new RegexRule("\\{\\d{2,4}\\}(\\p{Blank})*\\d{1,4}"));
        arrayList.add(new RegexRule("(?<![-\\d])(20|19)[0-9]{2}-?(1[0-2]|0?[1-9])-?([1-2][0-9]|3[0-1]|0?[1-9])(0?[0-9]|1[0-9]|2[0-4])(\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9])((\\p{Blank})*[:：](\\p{Blank})*([1-5][0-9]|0?[0-9]))?"));
        arrayList.add(new RegexRule("[@#][a-zA-Z_-]{0,20}[0-9]{4,}[a-zA-Z_-]{0,20}"));
        this.negativeRules = arrayList;
        arrayList2.add(new RegexRule(this, "(?<![-\\d])\\d{5,6}[/|]\\d{5,6}(?![-\\d])") {
            public List<MatchedNumberInfo> handle(PhoneNumberMatch phoneNumberMatch, String str) {
                int i = 0;
                List arrayList = new ArrayList();
                MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                MatchedNumberInfo matchedNumberInfo2 = new MatchedNumberInfo();
                Object rawString = phoneNumberMatch.rawString();
                Matcher matcher = getPattern().matcher(rawString);
                if (matcher.find()) {
                    int start = matcher.start();
                    try {
                        List access$0 = PhoneNumberRule_ZH_CN.getNumbersWithSlant(rawString);
                        if (access$0.size() == 2) {
                            if (start != 1) {
                            }
                            if (!access$0.isEmpty()) {
                                matchedNumberInfo.setBegin((((MatchedNumberInfo) access$0.get(0)).getBegin() + i) + phoneNumberMatch.start());
                                matchedNumberInfo.setEnd(((MatchedNumberInfo) access$0.get(0)).getEnd() + phoneNumberMatch.start());
                                matchedNumberInfo.setContent(((MatchedNumberInfo) access$0.get(0)).getContent());
                                arrayList.add(matchedNumberInfo);
                                if (access$0.size() == 2) {
                                    matchedNumberInfo2.setBegin((((MatchedNumberInfo) access$0.get(1)).getBegin() + i) + phoneNumberMatch.start());
                                    matchedNumberInfo2.setEnd(((MatchedNumberInfo) access$0.get(1)).getEnd() + phoneNumberMatch.start());
                                    matchedNumberInfo2.setContent(((MatchedNumberInfo) access$0.get(1)).getContent());
                                    arrayList.add(matchedNumberInfo2);
                                }
                            }
                        }
                        i = start;
                        if (access$0.isEmpty()) {
                            matchedNumberInfo.setBegin((((MatchedNumberInfo) access$0.get(0)).getBegin() + i) + phoneNumberMatch.start());
                            matchedNumberInfo.setEnd(((MatchedNumberInfo) access$0.get(0)).getEnd() + phoneNumberMatch.start());
                            matchedNumberInfo.setContent(((MatchedNumberInfo) access$0.get(0)).getContent());
                            arrayList.add(matchedNumberInfo);
                            if (access$0.size() == 2) {
                                matchedNumberInfo2.setBegin((((MatchedNumberInfo) access$0.get(1)).getBegin() + i) + phoneNumberMatch.start());
                                matchedNumberInfo2.setEnd(((MatchedNumberInfo) access$0.get(1)).getEnd() + phoneNumberMatch.start());
                                matchedNumberInfo2.setContent(((MatchedNumberInfo) access$0.get(1)).getContent());
                                arrayList.add(matchedNumberInfo2);
                            }
                        }
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }
                return arrayList;
            }
        });
        arrayList2.add(new RegexRule(this, "((?<!([-\\d])|(\\d\\p{Blank}{1,5}))[2-9](\\d{2}\\p{Blank}+\\d{4,5}|\\d{3}\\p{Blank}+\\d{3,4})(?!\\p{Blank}*\\d)|(?<![-\\d])[2-9]\\d{6,7}(?![\\d]))(;\\d{1})?") {
            public List<MatchedNumberInfo> handle(PhoneNumberMatch phoneNumberMatch, String str) {
                String str2 = "5201314";
                List<MatchedNumberInfo> arrayList = new ArrayList();
                MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                String rawString = phoneNumberMatch.rawString();
                Matcher matcher = getPattern().matcher(rawString);
                Matcher matcher2 = Pattern.compile("(?<![-\\d])(23{6,7})(?![-\\d])").matcher(rawString);
                if (!matcher.find() || matcher2.find() || rawString.equals(str2)) {
                    return arrayList;
                }
                if (phoneNumberMatch.rawString().charAt(0) == '(' || phoneNumberMatch.rawString().charAt(0) == '[') {
                    matchedNumberInfo.setBegin(phoneNumberMatch.start());
                } else {
                    matchedNumberInfo.setBegin(matcher.start() + phoneNumberMatch.start());
                }
                matchedNumberInfo.setEnd(matcher.end() + phoneNumberMatch.start());
                matchedNumberInfo.setContent(rawString);
                arrayList.add(matchedNumberInfo);
                return arrayList;
            }
        });
        arrayList2.add(new RegexRule(this, "(?<![-\\d])100\\d{4}(?![\\d])") {
            public List<MatchedNumberInfo> handle(PhoneNumberMatch phoneNumberMatch, String str) {
                List<MatchedNumberInfo> arrayList = new ArrayList();
                MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                if (phoneNumberMatch.rawString().startsWith("(") || phoneNumberMatch.rawString().startsWith("[")) {
                    matchedNumberInfo.setBegin(phoneNumberMatch.start() + 1);
                } else {
                    matchedNumberInfo.setBegin(phoneNumberMatch.start());
                }
                matchedNumberInfo.setEnd(phoneNumberMatch.end());
                matchedNumberInfo.setContent(str);
                arrayList.add(matchedNumberInfo);
                return arrayList;
            }
        });
        this.positiveRules = arrayList2;
        this.codesRules.add(new RegexRule(this, "") {
            public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
                int i;
                String rawString = phoneNumberMatch.rawString();
                int indexOf = rawString.trim().indexOf(";ext=");
                if (indexOf != -1) {
                    rawString = rawString.trim().substring(0, indexOf);
                }
                if (rawString.startsWith("(") || rawString.startsWith("[")) {
                    String str2 = "";
                    str2 = "";
                    if (rawString.startsWith("(")) {
                        str2 = "(";
                        str2 = ")";
                    }
                    if (rawString.startsWith("[")) {
                        str2 = "[";
                        str2 = "]";
                    }
                    int indexOf2 = rawString.indexOf(str2);
                    indexOf = str.indexOf(str2);
                    if (indexOf2 == -1) {
                        rawString = rawString.substring(1);
                    } else {
                        int access$1 = PhoneNumberRule_ZH_CN.countDigits(rawString.substring(0, indexOf2));
                        int access$12 = PhoneNumberRule_ZH_CN.countDigits(rawString.substring(indexOf2));
                        if (access$1 > 4) {
                            if (access$12 == 1 || access$12 == 2) {
                                String substring = rawString.substring(1, indexOf2);
                                for (PhoneNumberMatch phoneNumberMatch2 : PhoneNumberUtil.getInstance().findNumbers(str.substring(0, indexOf + 1), "CN", Leniency.POSSIBLE, Long.MAX_VALUE)) {
                                }
                                rawString = substring;
                            }
                        }
                        rawString = rawString.substring(1);
                    }
                }
                if (rawString.startsWith("1") && PhoneNumberRule_ZH_CN.countDigits(rawString) > 11) {
                    i = (rawString.startsWith("11808") || rawString.startsWith("17909") || rawString.startsWith("12593") || rawString.startsWith("17951") || rawString.startsWith("17911")) ? 1 : 0;
                } else if (rawString.startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) && PhoneNumberRule_ZH_CN.countDigits(rawString) > 12 && !rawString.substring(1, 4).equals("086")) {
                    i = 0;
                } else {
                    if (rawString.startsWith("400") || rawString.startsWith("800")) {
                        if (PhoneNumberRule_ZH_CN.countDigits(rawString) != 10) {
                            i = 0;
                        }
                    }
                    i = (rawString.startsWith("1") || rawString.startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) || rawString.startsWith("400") || rawString.startsWith("800") || rawString.startsWith("+") || PhoneNumberRule_ZH_CN.countDigits(rawString) < 9) ? PhoneNumberRule_ZH_CN.countDigits(rawString) > 4 ? 1 : 0 : (rawString.trim().startsWith("9") || rawString.trim().startsWith("1")) ? (rawString.contains("/") || rawString.contains("\\") || rawString.contains("|")) ? 1 : 1 : 0;
                }
                if (i == 0) {
                    return null;
                }
                return phoneNumberMatch;
            }
        });
        arrayList = new ArrayList();
        arrayList.add(new RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,}|10{8,})", 2, 9));
        this.borderRules = arrayList;
    }

    private static int countDigits(String str) {
        int i = 0;
        for (char isDigit : str.toCharArray()) {
            if (Character.isDigit(isDigit)) {
                i++;
            }
        }
        return i;
    }

    private static List<MatchedNumberInfo> getNumbersWithSlant(String str) throws NumberParseException {
        List arrayList = new ArrayList();
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        ShortNumberInfo instance2 = ShortNumberInfo.getInstance();
        String str2 = "";
        String str3 = "";
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (str.charAt(i2) == '/') {
                str3 = str.substring(0, i2);
                str2 = str3;
                str3 = str.substring(i2 + 1, str.length());
                i = i2;
            }
        }
        PhoneNumber parse = instance.parse(str2, "CN");
        PhoneNumber parse2 = instance.parse(str3, "CN");
        if (instance2.isValidShortNumber(parse)) {
            MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
            matchedNumberInfo.setBegin(0);
            matchedNumberInfo.setEnd(i);
            matchedNumberInfo.setContent(str2);
            arrayList.add(matchedNumberInfo);
        }
        if (instance2.isValidShortNumber(parse2)) {
            matchedNumberInfo = new MatchedNumberInfo();
            matchedNumberInfo.setBegin(i + 1);
            matchedNumberInfo.setEnd(str.length());
            matchedNumberInfo.setContent(str3);
            arrayList.add(matchedNumberInfo);
        }
        return arrayList;
    }
}
