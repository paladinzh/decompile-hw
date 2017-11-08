package com.android.contacts.dialpad;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SmartDialPrefix {
    private static final String PREF_USER_SIM_COUNTRY_CODE_DEFAULT = null;
    private static final SmartDialMap mMap = new LatinSmartDialMap();
    private static Set<String> sCountryCodes = null;
    private static Set<String> sNanpCountries = null;
    private static boolean sNanpInitialized = false;
    private static boolean sUserInNanpRegion = false;
    private static String sUserSimCountryCode = PREF_USER_SIM_COUNTRY_CODE_DEFAULT;

    public static class PhoneNumberTokens {
        final String countryCode;
        final int countryCodeOffset;
        final int nanpCodeOffset;

        public PhoneNumberTokens(String countryCode, int countryCodeOffset, int nanpCodeOffset) {
            this.countryCode = countryCode;
            this.countryCodeOffset = countryCodeOffset;
            this.nanpCodeOffset = nanpCodeOffset;
        }
    }

    public static void initializeNanpSettings(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
        if (manager != null) {
            sUserSimCountryCode = manager.getSimCountryIso();
        }
        SharedPreferences prefs = SharePreferenceUtil.getDefaultSp_de(context);
        if (sUserSimCountryCode != null) {
            prefs.edit().putString("DialtactsActivity_user_sim_country_code", sUserSimCountryCode).apply();
        } else {
            sUserSimCountryCode = prefs.getString("DialtactsActivity_user_sim_country_code", PREF_USER_SIM_COUNTRY_CODE_DEFAULT);
        }
        sUserInNanpRegion = isCountryNanp(sUserSimCountryCode);
        sNanpInitialized = true;
    }

    @VisibleForTesting
    public static void setUserInNanpRegion(boolean userInNanpRegion) {
        sUserInNanpRegion = userInNanpRegion;
    }

    public static ArrayList<String> parseToIndexTokens(String contactName) {
        int length = contactName.length();
        ArrayList<String> result = Lists.newArrayList();
        StringBuilder currentIndexToken = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = mMap.normalizeCharacter(contactName.charAt(i));
            if (mMap.isValidDialpadCharacter(c)) {
                currentIndexToken.append(mMap.getDialpadIndex(c));
            } else {
                if (currentIndexToken.length() != 0) {
                    result.add(currentIndexToken.toString());
                }
                currentIndexToken.delete(0, currentIndexToken.length());
            }
        }
        if (currentIndexToken.length() != 0) {
            result.add(currentIndexToken.toString());
        }
        return result;
    }

    public static ArrayList<String> generateNamePrefixes(String index) {
        ArrayList<String> result = Lists.newArrayList();
        ArrayList<String> indexTokens = parseToIndexTokens(index);
        if (indexTokens.size() > 0) {
            int i;
            StringBuilder fullNameToken = new StringBuilder();
            for (i = indexTokens.size() - 1; i >= 0; i--) {
                fullNameToken.insert(0, (String) indexTokens.get(i));
                result.add(fullNameToken.toString());
            }
            ArrayList<String> fullNames = Lists.newArrayList();
            fullNames.add((String) indexTokens.get(indexTokens.size() - 1));
            int recursiveNameStart = result.size();
            int recursiveNameEnd = result.size();
            String initial = "";
            i = indexTokens.size() - 2;
            while (i >= 0) {
                if (i >= indexTokens.size() - 2 || i < 2) {
                    int j;
                    initial = ((String) indexTokens.get(i)).substring(0, 1);
                    for (j = 0; j < fullNames.size(); j++) {
                        result.add(initial + ((String) fullNames.get(j)));
                    }
                    for (j = recursiveNameStart; j < recursiveNameEnd; j++) {
                        result.add(initial + ((String) result.get(j)));
                    }
                    recursiveNameEnd = result.size();
                    fullNames.add(((String) indexTokens.get(i)) + ((String) fullNames.get(fullNames.size() - 1)));
                }
                i--;
            }
        }
        return result;
    }

    public static ArrayList<String> parseToNumberTokens(String number) {
        ArrayList<String> result = Lists.newArrayList();
        if (!TextUtils.isEmpty(number)) {
            result.add(SmartDialNameMatcher.normalizeNumber(number, mMap));
            PhoneNumberTokens phoneNumberTokens = parsePhoneNumber(number);
            if (phoneNumberTokens.countryCodeOffset != 0) {
                result.add(SmartDialNameMatcher.normalizeNumber(number, phoneNumberTokens.countryCodeOffset, mMap));
            }
            if (phoneNumberTokens.nanpCodeOffset != 0) {
                result.add(SmartDialNameMatcher.normalizeNumber(number, phoneNumberTokens.nanpCodeOffset, mMap));
            }
        }
        return result;
    }

    public static PhoneNumberTokens parsePhoneNumber(String number) {
        String countryCode = "";
        int countryCodeOffset = 0;
        int nanpNumberOffset = 0;
        if (!TextUtils.isEmpty(number)) {
            String normalizedNumber = SmartDialNameMatcher.normalizeNumber(number, mMap);
            if (number.charAt(0) == '+') {
                int i = 1;
                while (i <= 4 && number.length() > i) {
                    countryCode = number.substring(1, i);
                    if (isValidCountryCode(countryCode)) {
                        countryCodeOffset = i;
                        break;
                    }
                    i++;
                }
            } else if (normalizedNumber.length() == 11 && normalizedNumber.charAt(0) == '1' && sUserInNanpRegion) {
                countryCode = CallInterceptDetails.BRANDED_STATE;
                countryCodeOffset = number.indexOf(normalizedNumber.charAt(1));
                if (countryCodeOffset == -1) {
                    countryCodeOffset = 0;
                }
            }
            if (sUserInNanpRegion) {
                String areaCode = "";
                if (countryCode.equals("") && normalizedNumber.length() == 10) {
                    areaCode = normalizedNumber.substring(0, 3);
                } else if (countryCode.equals(CallInterceptDetails.BRANDED_STATE) && normalizedNumber.length() == 11) {
                    areaCode = normalizedNumber.substring(1, 4);
                }
                if (!(areaCode.equals("") || number.indexOf(areaCode) == -1)) {
                    nanpNumberOffset = number.indexOf(areaCode) + 3;
                }
            }
        }
        return new PhoneNumberTokens(countryCode, countryCodeOffset, nanpNumberOffset);
    }

    private static boolean isValidCountryCode(String countryCode) {
        if (sCountryCodes == null) {
            sCountryCodes = initCountryCodes();
        }
        return sCountryCodes.contains(countryCode);
    }

    private static Set<String> initCountryCodes() {
        HashSet<String> result = new HashSet();
        result.add(CallInterceptDetails.BRANDED_STATE);
        result.add("7");
        result.add("20");
        result.add("27");
        result.add("30");
        result.add("31");
        result.add("32");
        result.add("33");
        result.add("34");
        result.add("36");
        result.add("39");
        result.add("40");
        result.add("41");
        result.add("43");
        result.add("44");
        result.add("45");
        result.add("46");
        result.add("47");
        result.add("48");
        result.add("49");
        result.add("51");
        result.add("52");
        result.add("53");
        result.add("54");
        result.add("55");
        result.add("56");
        result.add("57");
        result.add("58");
        result.add("60");
        result.add("61");
        result.add("62");
        result.add("63");
        result.add("64");
        result.add("65");
        result.add("66");
        result.add("81");
        result.add("82");
        result.add("84");
        result.add("86");
        result.add("90");
        result.add("91");
        result.add("92");
        result.add("93");
        result.add("94");
        result.add("95");
        result.add("98");
        result.add("211");
        result.add("212");
        result.add("213");
        result.add("216");
        result.add("218");
        result.add("220");
        result.add("221");
        result.add("222");
        result.add("223");
        result.add("224");
        result.add("225");
        result.add("226");
        result.add("227");
        result.add("228");
        result.add("229");
        result.add("230");
        result.add("231");
        result.add("232");
        result.add("233");
        result.add("234");
        result.add("235");
        result.add("236");
        result.add("237");
        result.add("238");
        result.add("239");
        result.add("240");
        result.add("241");
        result.add("242");
        result.add("243");
        result.add("244");
        result.add("245");
        result.add("246");
        result.add("247");
        result.add("248");
        result.add("249");
        result.add("250");
        result.add("251");
        result.add("252");
        result.add("253");
        result.add("254");
        result.add("255");
        result.add("256");
        result.add("257");
        result.add("258");
        result.add("260");
        result.add("261");
        result.add("262");
        result.add("263");
        result.add("264");
        result.add("265");
        result.add("266");
        result.add("267");
        result.add("268");
        result.add("269");
        result.add("290");
        result.add("291");
        result.add("297");
        result.add("298");
        result.add("299");
        result.add("350");
        result.add("351");
        result.add("352");
        result.add("353");
        result.add("354");
        result.add("355");
        result.add("356");
        result.add("357");
        result.add("358");
        result.add("359");
        result.add("370");
        result.add("371");
        result.add("372");
        result.add("373");
        result.add("374");
        result.add("375");
        result.add("376");
        result.add("377");
        result.add("378");
        result.add("379");
        result.add("380");
        result.add("381");
        result.add("382");
        result.add("385");
        result.add("386");
        result.add("387");
        result.add("389");
        result.add("420");
        result.add("421");
        result.add("423");
        result.add("500");
        result.add("501");
        result.add("502");
        result.add("503");
        result.add("504");
        result.add("505");
        result.add("506");
        result.add("507");
        result.add("508");
        result.add("509");
        result.add("590");
        result.add("591");
        result.add("592");
        result.add("593");
        result.add("594");
        result.add("595");
        result.add("596");
        result.add("597");
        result.add("598");
        result.add("599");
        result.add("670");
        result.add("672");
        result.add("673");
        result.add("674");
        result.add("675");
        result.add("676");
        result.add("677");
        result.add("678");
        result.add("679");
        result.add("680");
        result.add("681");
        result.add("682");
        result.add("683");
        result.add("685");
        result.add("686");
        result.add("687");
        result.add("688");
        result.add("689");
        result.add("690");
        result.add("691");
        result.add("692");
        result.add("800");
        result.add("808");
        result.add("850");
        result.add("852");
        result.add("853");
        result.add("855");
        result.add("856");
        result.add("870");
        result.add("878");
        result.add("880");
        result.add("881");
        result.add("882");
        result.add("883");
        result.add("886");
        result.add("888");
        result.add("960");
        result.add("961");
        result.add("962");
        result.add("963");
        result.add("964");
        result.add("965");
        result.add("966");
        result.add("967");
        result.add("968");
        result.add("970");
        result.add("971");
        result.add("972");
        result.add("973");
        result.add("974");
        result.add("975");
        result.add("976");
        result.add("977");
        result.add("979");
        result.add("992");
        result.add("993");
        result.add("994");
        result.add("995");
        result.add("996");
        result.add("998");
        return result;
    }

    public static SmartDialMap getMap() {
        return mMap;
    }

    @VisibleForTesting
    public static boolean isCountryNanp(String country) {
        if (TextUtils.isEmpty(country)) {
            return false;
        }
        if (sNanpCountries == null) {
            sNanpCountries = initNanpCountries();
        }
        return sNanpCountries.contains(country.toUpperCase());
    }

    private static Set<String> initNanpCountries() {
        HashSet<String> result = new HashSet();
        result.add("US");
        result.add("CA");
        result.add("AS");
        result.add("AI");
        result.add("AG");
        result.add("BS");
        result.add("BB");
        result.add("BM");
        result.add("VG");
        result.add("KY");
        result.add("DM");
        result.add("DO");
        result.add("GD");
        result.add("GU");
        result.add("JM");
        result.add("PR");
        result.add("MS");
        result.add("MP");
        result.add("KN");
        result.add("LC");
        result.add("VC");
        result.add("TT");
        result.add("TC");
        result.add("VI");
        return result;
    }
}
