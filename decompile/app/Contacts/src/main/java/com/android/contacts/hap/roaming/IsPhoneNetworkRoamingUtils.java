package com.android.contacts.hap.roaming;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.ContactsUtils;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.numberlocation.RoamingPhoneGeoNumberLocationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class IsPhoneNetworkRoamingUtils {
    static String CHINA_COUNTRY_ISO = "0086";
    static int CHINA_PHONE_NUMBER_LENGTH = 11;
    static String CHINA_PHONE_PREFIX = "+86";
    static String DEFAULT_INTERNATIONAL_PHONE_PREFIX = "+";
    private static final String[] MaritimeHEAD = new String[]{"00870", "00871", "00872", "00873", "00874"};

    private IsPhoneNetworkRoamingUtils() {
    }

    public static boolean isPhoneNetworkRoaming(int subScriptionId) {
        return SimFactoryManager.isNetworkRoaming(subScriptionId);
    }

    public static boolean isPhoneNetworkRoamging() {
        if (!SimFactoryManager.isDualSim()) {
            return SimFactoryManager.isNetworkRoaming(0);
        }
        boolean isSIM1NetworkRoaming = false;
        boolean isSIM2NetworkRoaming = false;
        if (SimFactoryManager.isSIM1CardPresent()) {
            isSIM1NetworkRoaming = SimFactoryManager.isNetworkRoaming(0);
        }
        if (SimFactoryManager.isSIM2CardPresent()) {
            isSIM2NetworkRoaming = SimFactoryManager.isNetworkRoaming(1);
        }
        return !isSIM1NetworkRoaming ? isSIM2NetworkRoaming : true;
    }

    public static String getPhoneNumber(Context context, DetailViewEntry entry, PhoneDataItem phoneDataItem) {
        List<String> list = new ArrayList();
        String data4 = phoneDataItem.getNormalizedNumber();
        if (data4 == null || data4.length() == 0) {
            entry.setNullOriginalRoamingData(true);
        }
        String result = parseRoamingPhoneNumber(phoneDataItem.getNumber(), data4, list);
        if (!list.isEmpty()) {
            HashMap<String, String> hm = parseRoamingPhoneCountry(context, list);
            RoamingPhoneGatherUtils roamingPhoneGatherUtils = new RoamingPhoneGatherUtils();
            roamingPhoneGatherUtils.setPhoneCountry(hm);
            roamingPhoneGatherUtils.setPhoneNumber(list);
            entry.setRoamingPhoneGatherUtils(roamingPhoneGatherUtils);
        }
        return result;
    }

    public static RoamingPhoneGatherUtils getPhoneNumberRoamingPhoneGatherUtils(Context context, String data1, String data4) {
        List<String> list = new ArrayList();
        if (isNumberMatchCurrentCountry(data1, data4, context)) {
            return null;
        }
        parseRoamingPhoneNumber(data1, data4, list);
        if (list.isEmpty()) {
            return null;
        }
        HashMap<String, String> hm = parseRoamingPhoneCountry(context, list);
        RoamingPhoneGatherUtils roamingPhoneGatherUtils = new RoamingPhoneGatherUtils();
        roamingPhoneGatherUtils.setPhoneCountry(hm);
        roamingPhoneGatherUtils.setPhoneNumber(list);
        return roamingPhoneGatherUtils;
    }

    public static HashMap<String, String> parseRoamingPhoneCountry(Context context, List<String> list) {
        if (list == null || context == null) {
            return null;
        }
        HashMap<String, String> countryHM = new HashMap();
        for (String pn : list) {
            if (pn.startsWith(DEFAULT_INTERNATIONAL_PHONE_PREFIX)) {
                String cnString = RoamingPhoneGeoNumberLocationUtil.getRoamingPhoneGeoNumberLocation(context, pn);
                if (cnString == null || cnString.length() == 0) {
                    countryHM.put(pn, context.getString(R.string.roaming_dial_by_data1_number));
                } else {
                    countryHM.put(pn, cnString);
                }
            } else {
                countryHM.put(pn, context.getString(R.string.roaming_dial_by_data1_number));
            }
        }
        return countryHM;
    }

    public static String parseRoamingPhoneNumber(String data1, String data4, List<String> phonenumList) {
        data1 = removeDashesAndBlanksBrackets(data1);
        String result = compEditPhoneNumer(data1, data4);
        if (isStringEmpty(result)) {
            result = produceData4BySIM(data1, phonenumList);
            if (isStringEmpty(result)) {
                result = data1;
            }
            addDataToList(phonenumList, data1);
        }
        return result;
    }

    private static String produceData4BySIM(String data1, List<String> phonenumList) {
        if (SimFactoryManager.isDualSim()) {
            CharSequence charSequence = null;
            String str = null;
            if (SimFactoryManager.isSIM1CardPresent() && SimFactoryManager.isNetworkRoaming(0)) {
                charSequence = produceData4BySIMCountryISO(data1, 0);
                addDataToList(phonenumList, charSequence);
            }
            if (SimFactoryManager.isSIM2CardPresent() && SimFactoryManager.isNetworkRoaming(1)) {
                str = produceData4BySIMCountryISO(data1, 1);
                addDataToList(phonenumList, str);
            }
            return TextUtils.isEmpty(charSequence) ? str : charSequence;
        } else {
            String result = produceData4BySIMCountryISO(data1, 0);
            addDataToList(phonenumList, result);
            return result;
        }
    }

    private static void addDataToList(List<String> phonenumList, String data) {
        if (phonenumList != null && !isStringEmpty(data) && !phonenumList.contains(data)) {
            phonenumList.add(data);
        }
    }

    private static String produceData4BySIMCountryISO(String data1, int slotId) {
        String sim1 = getToUpperCaseSimCountryIso(slotId);
        String data4 = null;
        if (!isStringEmpty(sim1)) {
            if ("CN".equals(sim1)) {
                data4 = parseNumber(data1);
            } else {
                data4 = produectData4ByCountryISO(data1, sim1);
            }
        }
        if (isNumberMatchCurrentCountry(data1, data4)) {
            return null;
        }
        return data4;
    }

    private static String getToUpperCaseSimCountryIso(int slotId) {
        return toUpperCaseCountryIso(SimFactoryManager.getSimCountryIso(slotId));
    }

    private static String toUpperCaseCountryIso(String defaultCountryIso) {
        if (isStringEmpty(defaultCountryIso)) {
            return defaultCountryIso;
        }
        return defaultCountryIso.toUpperCase(Locale.getDefault());
    }

    public static String removeDashesAndBlanksBrackets(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return paramString;
        }
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramString.length(); i++) {
            char c = paramString.charAt(i);
            if (!(c == ' ' || c == '-' || c == '(' || c == ')')) {
                localStringBuilder.append(c);
            }
        }
        return localStringBuilder.toString();
    }

    static String deleteChinaISO(String num) {
        if (isStringEmpty(num)) {
            return num;
        }
        String result;
        if (num.startsWith(CHINA_COUNTRY_ISO)) {
            result = num.substring(CHINA_COUNTRY_ISO.length());
        } else if (num.startsWith(CHINA_PHONE_PREFIX)) {
            result = num.substring(CHINA_PHONE_PREFIX.length());
        } else {
            result = num;
        }
        return result;
    }

    static String compEditPhoneNumer(String data1, String data4) {
        String result = data4;
        if (TextUtils.isEmpty(data1)) {
            return null;
        }
        data1 = removeDashesAndBlanksBrackets(data1);
        if (!isStringEmpty(data4)) {
            StringBuilder sb = new StringBuilder();
            data4 = removeDashesAndBlanksBrackets(data4);
            if (!data4.startsWith(CHINA_PHONE_PREFIX)) {
                result = dealWithNoChinaInconformity(data1, data4);
            } else if (!data1.contains(data4.substring(CHINA_PHONE_PREFIX.length()))) {
                if (data1.matches("^(\\+86|86|0086)?((179[01356][0189])|(11808)|(12593)|(10193)|(96435))(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])\\d{8}$") || data1.matches("^(\\+86|86|0086)?((179[01356][0189])|(11808)|(12593)|(10193)|(96435))(([1-9]\\d{8})|([1-9]\\d{9})|([1-9]\\d{10}))$")) {
                    data1 = deleteChinaISO(data1);
                    if (CommonUtilMethods.isContainIPHead(data1)) {
                        data1 = data1.substring(5);
                    }
                    if (data1 != null && (data1.matches("(([1-9]\\d{8})|([1-9]\\d{9})|([1-9]\\d{10}))") || data1.matches("(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])\\d{8}"))) {
                        data1 = CHINA_PHONE_PREFIX + data1;
                    }
                }
                if (CommonUtilMethods.isContainIPHead(data1)) {
                    data1 = data1.substring(5);
                }
                disposeMobileOrFamilyNumber(data1, sb);
                result = sb.toString();
            }
        }
        return result;
    }

    static String dealWithNoChinaInconformity(String data1, String data4) {
        if (isStringEmpty(data1) || isStringEmpty(data4)) {
            return null;
        }
        String data1Temp;
        String data4Temp;
        String result = null;
        int data1TempLenght = data1.length();
        int data4TempLenght = data4.length();
        if (data1TempLenght >= 7) {
            data1Temp = data1.substring(data1TempLenght - 7);
            if (data4TempLenght >= 7) {
                data4Temp = data4.substring(data4TempLenght - 7);
            } else {
                data4Temp = data4;
            }
        } else {
            data1Temp = data1;
            if (data4TempLenght > data1TempLenght) {
                data4Temp = data4.substring(data4TempLenght - data1TempLenght);
            } else {
                data4Temp = data4;
            }
        }
        if (data4Temp.equals(data1Temp)) {
            result = data4;
        }
        return result;
    }

    public static boolean isStringEmpty(String param) {
        return param == null || param.length() == 0;
    }

    static String parseNumber(String num) {
        StringBuilder result = new StringBuilder();
        if (isStringEmpty(num)) {
            return num;
        }
        num = ContactsUtils.removeDashesAndBlanks(num);
        String originalString = num;
        int len = num.length();
        if (len >= CHINA_PHONE_NUMBER_LENGTH) {
            String num2;
            if (len <= 17) {
                num2 = num;
            } else if (num.startsWith(CHINA_PHONE_PREFIX)) {
                num2 = disposeMobileOrFamilyNumberISOIpHead(num);
            } else {
                num2 = num;
            }
            if (CommonUtilMethods.isContainIPHead(num2)) {
                num2 = num2.substring(5);
            }
            disposeMobileOrFamilyNumber(num2, result);
            if (num2 == null) {
            } else if (num2.equals(result.toString()) && !num2.startsWith(CHINA_PHONE_PREFIX)) {
                return num;
            } else {
                num = num2;
            }
        } else if (num.matches("^400\\d{7}$")) {
            disposeMobileOrFamilyNumber(num, result);
        }
        return result.toString();
    }

    static String disposeMobileOrFamilyNumberISOIpHead(String num) {
        if (num == null || num.length() == 0) {
            return num;
        }
        String original = num;
        String resultString = null;
        StringBuilder sb = new StringBuilder();
        sb.append(CHINA_PHONE_PREFIX);
        boolean isMatches = false;
        if (num.matches("^(\\+86|86|0086)?((179[01356][0189])|(11808)|(12593)|(10193)|(96435))(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])\\d{8}$")) {
            num = num.substring(CHINA_PHONE_PREFIX.length() + 5);
            if (num.matches("(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])\\d{8}")) {
                sb.append(num);
                resultString = sb.toString();
                isMatches = true;
            }
        } else if (num.matches("^(\\+86|86|0086)?((179[01356][0189])|(11808)|(12593)|(10193)|(96435))(([1-9]\\d{8})|([1-9]\\d{9})|([1-9]\\d{10}))$")) {
            num = num.substring(CHINA_PHONE_PREFIX.length() + 5);
            if (num.matches("(([1-9]\\d{8})|([1-9]\\d{9})|([1-9]\\d{10}))")) {
                sb.append(num);
                resultString = sb.toString();
                isMatches = true;
            }
        }
        if (!isMatches) {
            resultString = original;
        }
        return resultString;
    }

    static void disposeMobileOrFamilyNumber(String num, StringBuilder result) {
        if (num != null && result != null) {
            if (num.matches("^(0|\\+86|86|0086)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$")) {
                disposeMobileNumber(num, result);
            } else if (num.matches("^(0[1-9]\\d{8})|(0[1-9]\\d{9})|(0[1-9]\\d{10})$")) {
                disposeFamilyPhoneNumber(num, result);
            } else if (num.matches("^(0086)(([1-9]\\d{8})|([1-9]\\d{9})|([1-9]\\d{10}))$")) {
                disposeFamilyPhoneNumber(num, result);
            } else if (num.matches("^400\\d{7}$")) {
                disposeSpecialNumber(num, result);
            } else {
                result.append(num);
            }
        }
    }

    static void disposeSpecialNumber(String num, StringBuilder result) {
        if (num != null && result != null) {
            result.append(CHINA_PHONE_PREFIX);
            result.append(num);
        }
    }

    static void disposeFamilyPhoneNumber(String num, StringBuilder result) {
        if (num != null && result != null) {
            result.append(CHINA_PHONE_PREFIX);
            if (num.startsWith(CHINA_COUNTRY_ISO)) {
                result.append(num.substring(CHINA_COUNTRY_ISO.length()));
            } else {
                result.append(num.substring(1));
            }
        }
    }

    static void disposeMobileNumber(String num, StringBuilder result) {
        if (num != null && result != null) {
            if (num.startsWith(CHINA_COUNTRY_ISO)) {
                result.append(CHINA_PHONE_PREFIX);
                result.append(num.substring(CHINA_COUNTRY_ISO.length()));
            } else if (num.length() == CHINA_PHONE_NUMBER_LENGTH) {
                result.append(CHINA_PHONE_PREFIX);
                result.append(num);
            } else if (num.matches("^0(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$")) {
                result.append(CHINA_PHONE_PREFIX);
                result.append(num.substring(1));
            } else {
                result.append(num);
            }
        }
    }

    public static boolean isChinaSIMCard() {
        String isSim1 = null;
        String isSim2 = null;
        if (SimFactoryManager.isDualSim()) {
            boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
            boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
            if (isFirstSimEnabled) {
                isSim1 = SimFactoryManager.getSimCountryIso(0);
            }
            if (isSecondSimEnabled) {
                isSim2 = SimFactoryManager.getSimCountryIso(1);
            }
            if ("CN".equalsIgnoreCase(isSim1)) {
                return true;
            }
            return "CN".equalsIgnoreCase(isSim2);
        }
        return "CN".equalsIgnoreCase(SimFactoryManager.getSimCountryIso(0));
    }

    static String getNetworkCountryIso() {
        return toUpperCaseCountryIso(SimFactoryManager.getNetworkCountryIso());
    }

    public static boolean isNumberMatchCurrentCountry(String number, String data4) {
        if (number == null || data4 == null) {
            HwLog.i("IsPhoneNetworkRoamingUtils", "the param is null");
            return false;
        }
        String tempNum = removeDashesAndBlanksBrackets(number);
        if (isCreatePhoneNumberInUS(tempNum, data4)) {
            return false;
        }
        return data4.equals(produectData4(tempNum));
    }

    public static boolean isNumberMatchCurrentCountry(String number, String data4, Context context) {
        if (number != null && data4 != null && context != null) {
            return isNumberMatchCurrentCountry(number, data4);
        }
        HwLog.i("IsPhoneNetworkRoamingUtils", "the param is null");
        return false;
    }

    public static String produectData4(String number) {
        if (number == null || number.length() == 0) {
            HwLog.i("IsPhoneNetworkRoamingUtils", "the number is null!");
            return null;
        }
        String defaultCountryIso = getNetworkCountryIso();
        String newData4 = null;
        if (!TextUtils.isEmpty(defaultCountryIso)) {
            newData4 = produectData4ByCountryISO(number, defaultCountryIso);
        }
        return newData4;
    }

    public static String produectData4ByCountryISO(String number, String defaultCountryIso) {
        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(defaultCountryIso)) {
            return PhoneNumberUtils.formatNumberToE164(number, defaultCountryIso);
        }
        HwLog.i("IsPhoneNetworkRoamingUtils", "the parameter is null");
        return null;
    }

    private static boolean isCreatePhoneNumberInUS(String data1, String data4) {
        if (data1 == null || data1.length() == 0 || data4 == null || data4.length() == 0) {
            return false;
        }
        boolean result = false;
        if (data1.matches("(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])\\d{8}") && data4.matches("^\\+(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$")) {
            result = true;
        }
        return result;
    }

    public static boolean isRoamingDealWithNumber(Context context, String data1) {
        if (data1 == null || data1.startsWith(DEFAULT_INTERNATIONAL_PHONE_PREFIX) || data1.length() <= 0) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isMaritimeSatelliteNumber(String number) {
        if (number != null && number.length() >= 6 && number.length() <= 20 && number.startsWith("0087")) {
            for (String startsWith : MaritimeHEAD) {
                if (number.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }
}
