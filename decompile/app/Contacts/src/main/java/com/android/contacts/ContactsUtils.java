package com.android.contacts;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.DisplayPhoto;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings.System;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.widget.EditText;
import com.android.contacts.compatibility.ContactsCompat;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.huawei.contact.util.SettingsWrapper;
import com.huawei.cust.HwCustUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactsUtils {
    static final ComponentName CALL_INTENT_DESTINATION = new ComponentName("com.android.server.telecom", "com.android.server.telecom.PrivilegedCallActivity");
    public static final boolean FLAG_N_FEATURE;
    private static final String WAIT_SYMBOL_AS_STRING = String.valueOf(';');
    static Map<String, PredefinedNumbers> mPredefinedMap;
    private static int sThumbnailSize = -1;

    public static class PredefinedNumbers {
        public boolean isEmergencyNumber;
        public String mPredefinedName;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UserType {
    }

    static {
        boolean startsWith;
        if (VERSION.SDK_INT <= 23) {
            startsWith = VERSION.CODENAME.startsWith("N");
        } else {
            startsWith = true;
        }
        FLAG_N_FEATURE = startsWith;
    }

    public static String lookupProviderNameFromId(int protocol) {
        switch (protocol) {
            case 0:
                return "AIM";
            case 1:
                return "MSN";
            case 2:
                return "Yahoo";
            case 3:
                return "SKYPE";
            case 4:
                return "QQ";
            case 5:
                return "GTalk";
            case 6:
                return "ICQ";
            case 7:
                return "JABBER";
            default:
                return null;
        }
    }

    public static boolean isGraphic(CharSequence str) {
        return !TextUtils.isEmpty(str) ? TextUtils.isGraphic(str) : false;
    }

    @NeededForTesting
    public static boolean areObjectsEqual(Object a, Object b) {
        if (a != b) {
            return (a == null || b == null) ? false : a.equals(b);
        } else {
            return true;
        }
    }

    public static final boolean areIntentActionEqual(Intent a, Intent b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return TextUtils.equals(a.getAction(), b.getAction());
    }

    public static boolean areContactWritableAccountsAvailable(Context context) {
        if (AccountTypeManager.getInstance(context).getAccounts(true).isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean areGroupWritableAccountsAvailable(Context context) {
        return !AccountTypeManager.getInstance(context).getGroupWritableAccounts().isEmpty();
    }

    public static int getThumbnailSize(Context context) {
        if (sThumbnailSize == -1) {
            Cursor c = context.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"thumbnail_max_dim"}, null, null, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        sThumbnailSize = c.getInt(0);
                    }
                    c.close();
                } catch (Throwable th) {
                    c.close();
                }
            }
        }
        return sThumbnailSize;
    }

    public static boolean isNumberDialable(String number, int presentation) {
        return (TextUtils.isEmpty(number) || presentation == 3 || presentation == 2 || presentation == 4) ? false : true;
    }

    public static boolean isUnknownNumber(int presentation) {
        if (presentation == 3 || presentation == 2 || presentation == 4) {
            return true;
        }
        return false;
    }

    public static boolean okToDialByMotion(String number, int presentation, Context context) {
        boolean validNumber = !TextUtils.isEmpty(number) ? presentation == 1 : false;
        if (!validNumber) {
            return false;
        }
        boolean isCalling;
        if (SimFactoryManager.isCallStateIdle()) {
            isCalling = false;
        } else {
            isCalling = true;
        }
        if (isCalling) {
            return false;
        }
        boolean isAirModeOn;
        if (System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
            isAirModeOn = true;
        } else {
            isAirModeOn = false;
        }
        return !isAirModeOn;
    }

    public static Intent getDialIntent(String aNumber) {
        return new Intent("android.intent.action.DIAL", CallUtil.getCallUri(aNumber));
    }

    public static String getEmergencyOrHotlineName(Context aContext, String number) {
        if (mPredefinedMap == null) {
            loadPredefinedNumbersList(aContext);
        }
        if (mPredefinedMap == null || !mPredefinedMap.containsKey(number)) {
            return null;
        }
        PredefinedNumbers lPredefined = (PredefinedNumbers) mPredefinedMap.get(number);
        if (lPredefined.isEmergencyNumber) {
            return aContext.getResources().getString(R.string.emergency_number);
        }
        return lPredefined.mPredefinedName;
    }

    private static void loadPredefinedNumbersList(Context aContext) {
        mPredefinedMap = new HashMap();
        String lEmergencyNumbers = SharePreferenceUtil.getDefaultSp_de(aContext).getString("emergency_numbers", null);
        if (lEmergencyNumbers != null) {
            String[] numbers = lEmergencyNumbers.split(",");
            PredefinedNumbers mPredefined = new PredefinedNumbers();
            for (Object put : numbers) {
                mPredefined.isEmergencyNumber = true;
                mPredefined.mPredefinedName = "";
                mPredefinedMap.put(put, mPredefined);
            }
        }
        String lHotlineNumbers = SettingsWrapper.getString(aContext.getContentResolver(), "predefined_hotline_numbers");
        if (lHotlineNumbers != null) {
            updatePredefinedHotlineNumbers(lHotlineNumbers, aContext);
        }
        String lSDNInformationCfg = SettingsWrapper.getString(aContext.getContentResolver(), "sdn_information_show_in_calllog");
        if (lSDNInformationCfg != null) {
            updateSdnNumbers(lSDNInformationCfg);
        }
    }

    private static void updatePredefinedHotlineNumbers(String specialNumbers, Context context) {
        String[] predefineHotlinePairs = specialNumbers.split(";");
        if (predefineHotlinePairs != null) {
            for (String hotlinePair : predefineHotlinePairs) {
                String[] hotlineNumber = hotlinePair.split(",");
                if (hotlineNumber != null && hotlineNumber.length == 2) {
                    String[] infos = hotlineNumber[0].split(":");
                    String hwCustNumber = hotlineNumber[0];
                    String hwNumber = infos.length > 2 ? infos[2] : "";
                    PredefinedNumbers mPredefined = new PredefinedNumbers();
                    mPredefined.isEmergencyNumber = false;
                    HwCustContactsUtils hwCustContactsUtils = null;
                    if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                        hwCustContactsUtils = (HwCustContactsUtils) HwCustUtils.createObj(HwCustContactsUtils.class, new Object[0]);
                    }
                    if (hwCustContactsUtils != null) {
                        ArrayList<String> strlist = hwCustContactsUtils.getHotlineNumber(hotlineNumber, hwCustNumber);
                        if (strlist != null) {
                            mPredefined.mPredefinedName = (String) strlist.get(0);
                            hwNumber = (String) strlist.get(1);
                        } else if (infos.length > 1 && isMatchMcc(infos[1], context)) {
                            mPredefined.mPredefinedName = hotlineNumber[1];
                        }
                    } else if (infos.length > 1 && isMatchMcc(infos[1], context)) {
                        mPredefined.mPredefinedName = hotlineNumber[1];
                    }
                    if (!(mPredefinedMap.containsKey(hwNumber) || TextUtils.isEmpty(mPredefined.mPredefinedName))) {
                        mPredefinedMap.put(hwNumber, mPredefined);
                    }
                }
            }
        }
    }

    private static boolean isMatchMcc(String mcc_mnc, Context context) {
        String currentMcc_mnc = CommonUtilMethods.getTelephonyManager(context).getSimOperator();
        if (TextUtils.isEmpty(currentMcc_mnc) || currentMcc_mnc.length() < 5) {
            return false;
        }
        String currentMCC = currentMcc_mnc.substring(0, 3);
        if (TextUtils.isEmpty(mcc_mnc) || !mcc_mnc.contains(currentMCC)) {
            return false;
        }
        return true;
    }

    private static void updateSdnNumbers(String sdnNumbers) {
        String[] sdnPairs = sdnNumbers.split(";");
        if (sdnPairs != null) {
            for (String sdnPair : sdnPairs) {
                String[] sdnNumber = sdnPair.split(":");
                if (!(sdnNumber == null || sdnNumber.length != 2 || mPredefinedMap.containsKey(sdnNumber[1]))) {
                    PredefinedNumbers mPredefined = new PredefinedNumbers();
                    mPredefined.isEmergencyNumber = false;
                    mPredefined.mPredefinedName = sdnNumber[0];
                    mPredefinedMap.put(sdnNumber[1], mPredefined);
                }
            }
        }
    }

    public static Map<String, PredefinedNumbers> getPredefinedMap(Context aContext) {
        if (mPredefinedMap == null) {
            loadPredefinedNumbersList(aContext);
        }
        HwCustContactsUtils hwCustContactsUtils = null;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            hwCustContactsUtils = (HwCustContactsUtils) HwCustUtils.createObj(HwCustContactsUtils.class, new Object[0]);
        }
        if (hwCustContactsUtils != null && hwCustContactsUtils.isReinitHotlineNumber()) {
            loadPredefinedNumbersList(aContext);
        }
        return mPredefinedMap;
    }

    public static boolean displayEmergencyNumber(Context aContext) {
        return SharePreferenceUtil.getDefaultSp_de(aContext).getBoolean("display_emergency_number", true);
    }

    public static String getChinaFormatNumber(String formatNumber) {
        if (TextUtils.isEmpty(formatNumber) || formatNumber.contains(",")) {
            return formatNumber;
        }
        String newNumber = removeDashesAndBlanks(formatNumber);
        if (TextUtils.isEmpty(newNumber)) {
            return formatNumber;
        }
        newNumber = PhoneNumberUtils.formatNumber(newNumber, "CN");
        if (TextUtils.isEmpty(newNumber)) {
            newNumber = formatNumber;
        }
        return newNumber;
    }

    public static String removeDashesAndBlanks(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return paramString;
        }
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramString.length(); i++) {
            char c = paramString.charAt(i);
            if (!(c == ' ' || c == '-')) {
                localStringBuilder.append(c);
            }
        }
        return localStringBuilder.toString();
    }

    public static String getExportString(Context context, String name) {
        if (context == null) {
            return null;
        }
        String exportString;
        if (SimFactoryManager.isDualSim()) {
            exportString = context.getResources().getString(R.string.export_to_sim_card_title);
        } else {
            exportString = context.getResources().getString(R.string.title_export_to_sim_card);
        }
        Object[] objArr = new Object[1];
        if (name == null) {
            name = null;
        }
        objArr[0] = name;
        return String.format(exportString, objArr);
    }

    public static String getImportString(Context context, String name) {
        if (context == null) {
            return null;
        }
        String importString;
        if (SimFactoryManager.isDualSim()) {
            importString = context.getResources().getString(R.string.import_from_sim_card_title);
        } else {
            importString = context.getResources().getString(R.string.title_import_from_sim_card);
        }
        Object[] objArr = new Object[1];
        if (name == null) {
            name = null;
        }
        objArr[0] = name;
        return String.format(importString, objArr);
    }

    public static String formatPhoneNumber(String number, String normalizedNumber, String countryIso, Context aContext) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        if (PhoneNumberUtils.isUriNumber(number)) {
            return number;
        }
        if (TextUtils.isEmpty(countryIso)) {
            countryIso = getCurrentCountryIso(aContext);
        }
        return PhoneNumberUtils.formatNumber(number, normalizedNumber, countryIso);
    }

    public static final String getCurrentCountryIso(Context context) {
        return CountryMonitor.getInstance(context).getCountryIso();
    }

    public static void configureSearchViewInputType(EditText searchView) {
        if (searchView != null) {
            searchView.setInputType(8388608 | searchView.getInputType());
        }
    }

    public static Bitmap compressBitmap(Bitmap bitmap, int destWidth, int destHeight) {
        if (bitmap == null || destWidth <= 0 || destHeight <= 0) {
            return null;
        }
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width < destWidth || height < destHeight) {
            return bitmap;
        }
        matrix.postScale(((float) destWidth) / ((float) bitmap.getWidth()), ((float) destHeight) / ((float) bitmap.getHeight()));
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static Uri ensureIsContactUri(ContentResolver resolver, Uri uri) throws IllegalArgumentException {
        if (resolver == null || uri == null) {
            HwLog.w("ContactsUtils", "input parameter must not be null");
            return null;
        }
        String authority = uri.getAuthority();
        if ("com.android.contacts".equals(authority)) {
            String type = resolver.getType(uri);
            if ("vnd.android.cursor.item/contact".equals(type)) {
                return uri;
            }
            if ("vnd.android.cursor.item/raw_contact".equals(type)) {
                return RawContacts.getContactLookupUri(resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, ContentUris.parseId(uri)));
            }
            HwLog.w("ContactsUtils", "uri format is unknown");
            return null;
        }
        String OBSOLETE_AUTHORITY = "contacts";
        if ("contacts".equals(authority)) {
            return RawContacts.getContactLookupUri(resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, ContentUris.parseId(uri)));
        }
        HwLog.w("ContactsUtils", "uri authority is unknown");
        return null;
    }

    public static boolean isCallRecorderInstalled(Context context) {
        try {
            if (context.getPackageManager().getPackageInfo("com.android.phone.recorder", 1) != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            HwLog.e("ContactsUtils", "isCallRecorderInstalled is error");
        }
        return false;
    }

    public static long determineUserType(Long directoryId, Long contactId) {
        long j = 1;
        if (directoryId == null) {
            return (contactId == null || contactId.longValue() == 0 || !ContactsCompat.isEnterpriseContactId(contactId.longValue())) ? 0 : 1;
        } else {
            if (!DirectoryCompat.isEnterpriseDirectoryId(directoryId.longValue())) {
                j = 0;
            }
            return j;
        }
    }

    public static void updatePredefinedMapForSimChange(Context mContext) {
        if (mPredefinedMap != null) {
            loadPredefinedNumbersList(mContext);
        }
    }

    public static Intent getEmergencyIntent() {
        Intent intent = new Intent("android.telephony.action.EMERGENCY_ASSISTANCE");
        intent.setComponent(new ComponentName("com.android.emergency", "com.android.emergency.view.ViewInfoActivity"));
        return intent;
    }
}
