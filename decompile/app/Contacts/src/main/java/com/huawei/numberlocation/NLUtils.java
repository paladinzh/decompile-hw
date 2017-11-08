package com.huawei.numberlocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.google.android.gms.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class NLUtils {
    private static int CHINA_SHORT_NUMBER_AND_ARAR_LENGHT = 10;
    static HashMap<String, ArrayList<String>> initernationCallPrefixMap = new HashMap();
    private static boolean mIsInit = false;

    static final class LogExt {
        LogExt() {
        }

        static void e(String tag, String msg) {
            HwLog.e(tag, msg);
        }

        static void d(String tag, String msg) {
            if (HwLog.HWDBG) {
                HwLog.d(tag, msg);
            }
        }

        static void i(String tag, String msg) {
            if (HwLog.HWDBG) {
                HwLog.i(tag, msg);
            }
        }
    }

    static String getGeoNumberLocation(Context context, String number, Boolean showNotification) {
        String resultString = null;
        if (context == null || TextUtils.isEmpty(number)) {
            return null;
        }
        String countryIso = CountryMonitor.getInstance(context).getCountryIso();
        if (HwLog.HWFLOW) {
            StringBuilder sb4Log = new StringBuilder("getGeoNumberLocation: number.length: ");
            sb4Log.append(number.length()).append("; countryIso: ").append(countryIso);
            HwLog.i("NLUtils", sb4Log.toString());
        }
        if (isNeedConvertToCountryISO(countryIso)) {
            resultString = getGeoNumberLocationMacao(context, number, showNotification, countryIso);
        }
        if (!TextUtils.isEmpty(resultString)) {
            if (HwLog.HWFLOW) {
                sb4Log = new StringBuilder("getGeoNumberLocation for special country result.");
                sb4Log.append(resultString.length());
                HwLog.i("NLUtils", sb4Log.toString());
            }
            return resultString;
        } else if (EmuiFeatureManager.isChinaArea()) {
            ensureDataExistent(context);
            if (number != null) {
                number = iPHeadBarber(number);
            }
            if (TextUtils.isEmpty(number)) {
                return null;
            }
            if ("CN".equals(countryIso) || TextUtils.isEmpty(countryIso)) {
                resultString = getAttributionInfo(context, number, showNotification);
                if (HwLog.HWFLOW) {
                    sb4Log = new StringBuilder("getGeoNumberLocation for CN.");
                    sb4Log.append(resultString == null ? "" : Integer.valueOf(resultString.length()));
                    HwLog.i("NLUtils", sb4Log.toString());
                }
            } else if (number.length() < 7) {
                return null;
            } else {
                number = deleteInternationalPrefix(number, countryIso, context);
                if (number.startsWith("+86") || number.startsWith("0086")) {
                    resultString = getAttributionInfo(context, number, showNotification);
                } else if (number.matches("^(\\+[1-9]([0-9]{0,14}))$|^(00[1-9]([0-9]{0,13}))$")) {
                    resultString = getAttributionInfo(context, number, showNotification);
                } else {
                    resultString = getGeoDescription(context, number, countryIso);
                }
                if (HwLog.HWFLOW) {
                    sb4Log = new StringBuilder("getGeoNumberLocation for not CN.");
                    sb4Log.append(resultString == null ? "" : Integer.valueOf(resultString.length()));
                    HwLog.i("NLUtils", sb4Log.toString());
                }
            }
            return resultString;
        } else {
            resultString = getGeoDescription(context, number, countryIso);
            if (HwLog.HWFLOW) {
                sb4Log = new StringBuilder("getGeoNumberLocation for not China area.");
                sb4Log.append(resultString == null ? "" : Integer.valueOf(resultString.length()));
                HwLog.i("NLUtils", sb4Log.toString());
            }
            return resultString;
        }
    }

    private static String getAttributionInfo(Context context, String number, Boolean showNotification) {
        if (number.matches("0(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}")) {
            number = number.substring(1);
        }
        if (number.matches("^((\\+86)|(86)|(0086))?(1)[1-9]\\d{9}$")) {
            return new MobilePhoneNumber(context, number).getParseResult();
        }
        if (number.matches("^(\\+[1-9]([0-9]{0,14}))$|^(00[1-9]([0-9]{0,13}))$")) {
            return getNATAttributionInfo(context, number);
        }
        if (number.matches("(0\\d{9})|(0\\d{10})|(0\\d{11})")) {
            return new FixedPhoneNumber(context, number).getParseResult();
        }
        if (showNotification.booleanValue() && number.matches("^((\\+86)|(86)|(0086))?(1)\\d{2,10}$")) {
            MobilePhoneNumber mpn = new MobilePhoneNumber(context, number);
            mpn.getParseResult();
            String showLocationString = mpn.getLocation();
            String showOperatorsString = mpn.getPhoneOperator();
            if (showLocationString == null) {
                return showOperatorsString;
            }
            if (showOperatorsString == null) {
                return showLocationString;
            }
            return showLocationString + HwCustPreloadContacts.EMPTY_STRING + showOperatorsString;
        } else if (showNotification.booleanValue() && number.matches("^(0\\d{2,9})|(0\\d{2,10})|(0\\d{2,11})$")) {
            return new FixedPhoneNumber(context, number).getParseResult();
        } else {
            return null;
        }
    }

    private static String getNATAttributionInfo(Context context, String number) {
        boolean isChinaFixNumber = false;
        if (number.startsWith("+86")) {
            number = number.substring(3);
            isChinaFixNumber = true;
        } else if (number.startsWith("0086")) {
            number = number.substring(4);
            isChinaFixNumber = true;
        }
        if (!isChinaFixNumber) {
            return getGeoDescription(context, number, CountryMonitor.getInstance(context).getCountryIso());
        }
        if (!number.startsWith("0")) {
            number = "0" + number;
        }
        return new FixedPhoneNumber(context, number).getParseResult();
    }

    public static void ensureDataExistent(Context context) {
        if (!new File(context.createDeviceProtectedStorageContext().getFilesDir() + File.separator + "numberlocation.dat").exists() || needRebuildDatabase(context)) {
            LogExt.d("NLUtils", "databasedat dosen't exist!");
            addNumberLocationDatabase(context);
        }
    }

    private static String iPHeadBarber(String oriNumber) {
        String result = oriNumber;
        List<String> IpList = Arrays.asList(new String[]{"17900", "17901", "17908", "17909", "11808", "17950", "17951", "12593", "17931", "17910", "17911", "17960", "17968", "17969", "10193", "96435"});
        int numberLen = oriNumber.length();
        if (numberLen < 5) {
            LogExt.d("NLUtils", "iPHeadBarber->oriNumber.length=" + numberLen + ",is short than 5!");
            return oriNumber;
        }
        if (IpList.contains(oriNumber.substring(0, 5))) {
            result = oriNumber.substring(5, numberLen);
            LogExt.d("NLUtils", "deleteIPHead() The phone number is IP number = ");
        } else {
            LogExt.d("NLUtils", "deleteIPHead() The phone number is not IP number!");
        }
        return result;
    }

    private static void addNumberLocationDatabase(Context context) {
        IOException e;
        Throwable th;
        String LOCATION_DATA_DAT = context.createDeviceProtectedStorageContext().getFilesDir() + File.separator + "numberlocation.dat";
        File databasedat = new File(LOCATION_DATA_DAT);
        int readSum = 0;
        byte[] buffer = new byte[10000];
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            inputStream = context.getAssets().open("numberlocation.dat");
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            try {
                OutputStream output = new FileOutputStream(LOCATION_DATA_DAT);
                int readCount;
                do {
                    try {
                        readCount = bis.read(buffer);
                        readSum += readCount;
                        output.write(buffer, 0, readCount);
                    } catch (IOException e2) {
                        e = e2;
                        bufferedInputStream = bis;
                        outputStream = output;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedInputStream = bis;
                        outputStream = output;
                    }
                } while (readCount >= 10000);
                HwLog.i("NLUtils", "NumberLocationDatabase file Sumsize is: " + readSum);
                output.flush();
                SharedPreferences sp = SharePreferenceUtil.getDefaultSp_de(context);
                sp.edit().putLong("key_ver_1", Long.parseLong(context.getString(R.string.pre_ver_nl))).apply();
                LogExt.d("NLUtils", "copy numberlocation.dat success");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                outputStream = output;
            } catch (IOException e4) {
                e322 = e4;
                bufferedInputStream = bis;
                try {
                    e322.printStackTrace();
                    if (!databasedat.delete()) {
                        LogExt.d("NLUtils", "delete failure, file name is " + LOCATION_DATA_DAT);
                        ExceptionCapture.captureNLException("delete failure, file name is " + LOCATION_DATA_DAT, null);
                    }
                    LogExt.d("NLUtils", "copy numberlocation.dat failed");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e32222) {
                            e32222.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e322222) {
                            e322222.printStackTrace();
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3222222) {
                            e3222222.printStackTrace();
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e32222222) {
                            e32222222.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e322222222) {
                            e322222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                bufferedInputStream = bis;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            e322222222 = e5;
            e322222222.printStackTrace();
            if (databasedat.delete()) {
                LogExt.d("NLUtils", "delete failure, file name is " + LOCATION_DATA_DAT);
                ExceptionCapture.captureNLException("delete failure, file name is " + LOCATION_DATA_DAT, null);
            }
            LogExt.d("NLUtils", "copy numberlocation.dat failed");
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static String getGeoDescription(Context context, String number, String countryIso) {
        if (context == null || TextUtils.isEmpty(number) || TextUtils.isEmpty(countryIso)) {
            return null;
        }
        if ("CN".equals(countryIso) && ((number.length() == 7 || number.length() == 8) && number.charAt(0) != '0')) {
            return null;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
        PhoneNumber phoneNumber = null;
        try {
            phoneNumber = util.parse(number, countryIso);
        } catch (NumberParseException e) {
            HwLog.w("NLUtils", "getGeoDescription: NumberParseException for incoming number.");
        }
        if (phoneNumber == null) {
            return null;
        }
        String description = null;
        try {
            description = geocoder.getDescriptionForNumber(phoneNumber, context.getResources().getConfiguration().locale);
        } catch (RuntimeException re) {
            HwLog.e("NLUtils", "Runtime exception", re);
        }
        return description;
    }

    public static String deleteInternationalPrefix(String number, String countryIso, Context context) {
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(countryIso)) {
            return number;
        }
        String initernationCallPrefix = null;
        String newNumber = number;
        if (!mIsInit) {
            initCallPrefix(context);
            mIsInit = true;
        }
        ArrayList<String> prefixNumbers = (ArrayList) initernationCallPrefixMap.get(countryIso);
        if (prefixNumbers == null) {
            return number;
        }
        for (String prefix : prefixNumbers) {
            if (number.startsWith(prefix)) {
                initernationCallPrefix = prefix;
                break;
            }
        }
        if (initernationCallPrefix != null && number.startsWith(initernationCallPrefix)) {
            newNumber = "+" + number.substring(initernationCallPrefix.length());
        }
        return newNumber;
    }

    private static synchronized void initCallPrefix(Context context) {
        synchronized (NLUtils.class) {
            String[] CallPrefixCountry = context.getResources().getStringArray(R.array.CALL_PREFIX_COUNTRY);
            String[] CallPrefixNumber = context.getResources().getStringArray(R.array.CALL_PREFIX_NUMBER);
            if (!(CallPrefixNumber == null || CallPrefixCountry == null)) {
                if (CallPrefixCountry.length > 0 && CallPrefixNumber.length > 0 && CallPrefixCountry.length == CallPrefixNumber.length) {
                    for (int i = 0; i < CallPrefixCountry.length; i++) {
                        ArrayList<String> numberArray;
                        if (CallPrefixNumber[i].contains(",")) {
                            numberArray = new ArrayList(Arrays.asList(CallPrefixNumber[i].split(",")));
                        } else {
                            numberArray = new ArrayList();
                            numberArray.add(CallPrefixNumber[i]);
                        }
                        initernationCallPrefixMap.put(CallPrefixCountry[i], numberArray);
                    }
                    return;
                }
            }
            LogExt.e("NLUtils", "parse coutry prefix error!");
        }
    }

    public static String getAreaCode(Context context, String number) {
        String resultString = null;
        String FIXED_NUMBER_TOP2_TOKEN1 = "01";
        String FIXED_NUMBER_TOP2_TOKEN2 = "02";
        if (context == null || TextUtils.isEmpty(number)) {
            return null;
        }
        String LOCATION_DATA_DAT = context.createDeviceProtectedStorageContext().getFilesDir() + File.separator + "numberlocation.dat";
        String countryIso = CountryMonitor.getInstance(context).getCountryIso();
        ensureDataExistent(context);
        if (number != null) {
            number = iPHeadBarber(number);
        }
        if (number == null || "".equals(number)) {
            return null;
        }
        if ("CN".equals(countryIso) && !TextUtils.isEmpty(countryIso)) {
            if (number.matches("^((\\+86)|(86)|(0086))?(1)[1-9]\\d{9}$")) {
                resultString = NumberLocationDb.queryUnicodeAreaCodeByPhoneNum(number.substring(0, 7), LOCATION_DATA_DAT);
            } else if (number.matches("(0\\d{9})|(0\\d{10})|(0\\d{11})") && number.length() >= 3) {
                String top2String = number.substring(0, 2);
                if (top2String.equals(FIXED_NUMBER_TOP2_TOKEN1) || top2String.equals(FIXED_NUMBER_TOP2_TOKEN2)) {
                    resultString = number.substring(0, 3);
                } else if (number.length() >= 4) {
                    resultString = number.substring(0, 4);
                }
            }
        }
        return resultString;
    }

    public static synchronized boolean needRebuildDatabase(Context context) {
        synchronized (NLUtils.class) {
            boolean needRebuild = false;
            if (context == null) {
                return false;
            }
            if (SharePreferenceUtil.getDefaultSp_de(context).getLong("key_ver_1", 0) < Long.parseLong(context.getString(R.string.pre_ver_nl))) {
                needRebuild = true;
            }
            LogExt.i("NLUtils", "needRebuild=" + needRebuild);
            return needRebuild;
        }
    }

    static String getGeoNumberLocationMacao(Context context, String number, Boolean showNotification, String network_countryIso) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if (!number.matches("(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}") && !number.matches("0[1-9](\\d{9}|\\d{10})")) {
            return null;
        }
        if (EmuiFeatureManager.isChinaArea()) {
            return getAttributionInfo(context, number, showNotification);
        }
        return getGeoDescription(context, number, "CN");
    }

    private static boolean isNeedConvertToCountryISO(String network_countryIso) {
        if ("MO".equals(network_countryIso)) {
            return isCard_MO_CN();
        }
        return false;
    }

    private static boolean isCard_MO_CN() {
        if (SimFactoryManager.isDualSim()) {
            boolean result;
            String sim1Country = null;
            if (SimFactoryManager.isSIM1CardPresent()) {
                sim1Country = SimFactoryManager.getSimCountryIso(0);
            }
            if ("MO".equalsIgnoreCase(sim1Country)) {
                result = true;
            } else {
                result = "CN".equalsIgnoreCase(sim1Country);
            }
            if (result) {
                return result;
            }
            String sim2Country = null;
            if (SimFactoryManager.isSIM2CardPresent()) {
                sim2Country = SimFactoryManager.getSimCountryIso(1);
            }
            return !"MO".equalsIgnoreCase(sim2Country) ? "CN".equalsIgnoreCase(sim2Country) : true;
        } else {
            String simCountry = SimFactoryManager.getSimCountryIso(0);
            return !"MO".equalsIgnoreCase(simCountry) ? "CN".equalsIgnoreCase(simCountry) : true;
        }
    }

    public static boolean handleWithChinaPhoneOrFixNumberLogic(String number) {
        return number != null && number.length() >= CHINA_SHORT_NUMBER_AND_ARAR_LENGHT;
    }
}
