package com.android.contacts.hap.rcs;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.CallLog.Calls;
import android.provider.Settings.Secure;
import android.support.v4.content.FileProvider;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.contacts.CallUtil;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.rcs.activities.RcsPreCallActivity;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.huawei.android.telephony.MSimSmsManagerEx;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.util.RcsXmlParser;
import java.io.File;
import java.util.ArrayList;

public class RcsContactsUtils {
    public static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final boolean IS_RCS_ICON_EANBLE;
    public static final Uri RCS_PRE_CALL_CACHE_URI = Uri.parse("content://com.android.contacts.rcs-call/rcs_pre_call");
    private static final String[] mProjection = new String[]{"picture", "post_call_voice"};

    static {
        boolean z = false;
        if (!"false".equals(RcsXmlParser.getValueByNameFromXml("hw_rcs_contact_icon_on"))) {
            z = true;
        }
        IS_RCS_ICON_EANBLE = z;
        try {
            HwLog.i("RcsContactsUtils", "System.loadLibrary");
            System.loadLibrary("gdinamapv4sdk752");
            System.loadLibrary("gdinamapv4sdk752ex");
        } catch (UnsatisfiedLinkError e) {
            HwLog.i("RcsContactsUtils", " UnsatisfiedLinkError");
        } catch (SecurityException e2) {
            HwLog.i("RcsContactsUtils", "SecurityException");
        }
    }

    public static boolean isBBVersion() {
        return RcsXmlParser.getInt("hw_rcs_version", 0) == 0;
    }

    public static boolean isSupportCLIR() {
        return RcsXmlParser.getBoolean("is_support_CLIR", false);
    }

    private RcsContactsUtils() {
    }

    public static String getExcludeExistedNumbersClause(Context context, int matchLength) {
        ArrayList excludeNumberList = null;
        StringBuilder sb = new StringBuilder();
        if (context instanceof ContactMultiSelectionActivity) {
            ContactMultiSelectionActivity activity = (ContactMultiSelectionActivity) context;
            if (activity.getRcsCust() != null) {
                excludeNumberList = activity.getRcsCust().getMemberListFromForward();
            }
        }
        if (!(excludeNumberList == null || excludeNumberList.isEmpty() || matchLength <= 0)) {
            ArrayList<String> normalizedNums = getNormalizedForwardNums(excludeNumberList, matchLength);
            sb.append(" AND rcs_capability.only_number NOT IN (");
            if (normalizedNums == null) {
                return "";
            }
            int size = normalizedNums.size();
            for (int i = 0; i < size - 1; i++) {
                sb.append("'").append((String) normalizedNums.get(i)).append("'");
                sb.append(",");
            }
            if (size <= 0) {
                return "";
            }
            sb.append("'").append((String) normalizedNums.get(size - 1)).append("'");
            sb.append(")");
        }
        return sb.toString();
    }

    public static void configSelectionToGetOnlyRCSContactsExcludeExistedNumbers(StringBuilder aSelectionBuilder, int match_length, String excludeClause) {
        aSelectionBuilder.append(" AND _id in (select phone_lookup.data_id from phone_lookup where CASE WHEN length(phone_lookup.normalized_number) > ").append(match_length).append(" THEN");
        aSelectionBuilder.append(" substr(phone_lookup.normalized_number, length(phone_lookup.normalized_number)-").append(match_length).append("+1, length(phone_lookup.normalized_number))");
        aSelectionBuilder.append(" ELSE phone_lookup.normalized_number END in");
        aSelectionBuilder.append(" (select rcs_capability.only_number from rcs_capability where rcs_capability.iRCSType != 255");
        aSelectionBuilder.append(excludeClause);
        aSelectionBuilder.append("))");
    }

    public static boolean isRCSContactIconEnable() {
        return IS_RCS_ICON_EANBLE;
    }

    public static boolean isValidFromActivity(int fromActivity) {
        switch (fromActivity) {
            case 1:
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    public static String getCurrentUserNumber() {
        CapabilityService rcsService = CapabilityService.getInstance("contacts");
        if (rcsService != null) {
            return rcsService.getCurrentLoginUserNumber();
        }
        return null;
    }

    public static ArrayList<String> getNormalizedForwardNums(ArrayList<String> memberList, int matchNums) {
        if (matchNums <= 0 || memberList == null || memberList.size() == 0) {
            return null;
        }
        ArrayList<String> normalizedNums = new ArrayList();
        for (String str : memberList) {
            String norNum = PhoneNumberUtils.normalizeNumber(str);
            int length = norNum.length();
            if (length > matchNums) {
                norNum = norNum.substring(length - matchNums, length);
            }
            normalizedNums.add(norNum);
        }
        return normalizedNums;
    }

    public static void startPreCallActivity(Activity activity, String number) {
        startPreCallActivity(activity, number, null);
    }

    public static void startPreCallActivity(Activity activity, String number, Uri uri) {
        if (activity != null && !TextUtils.isEmpty(number)) {
            Intent intent = new Intent();
            intent.setClass(activity, RcsPreCallActivity.class);
            intent.putExtra("lookuri", uri != null ? uri.toString() : "");
            intent.putExtra("pre_call_number", number);
            activity.startActivity(intent);
        }
    }

    public static void startPreCallActivity(Activity activity, PhoneCallDetails details) {
        if (activity != null && !TextUtils.isEmpty(details.number)) {
            String number = details.number.toString();
            Intent intent = new Intent();
            intent.setClass(activity, RcsPreCallActivity.class);
            intent.putExtra("_id", details.mId);
            intent.putExtra("pre_call_number", number);
            intent.putExtra("is_primary", details.mIsPrimary);
            activity.startActivity(intent);
        }
    }

    public static void deleteRcsMapAndPicture(ContentResolver resolver, StringBuilder callIds) {
        if (resolver != null && callIds != null) {
            deleteRcsMediaResource(resolver, "_id IN (" + callIds + ") AND " + "features" + "=" + 33);
        }
    }

    public static void deleteRcsMapAndPicture(ContentResolver resolver, long callIds) {
        if (resolver != null) {
            deleteRcsMediaResource(resolver, "_id=" + callIds + " AND " + "features" + "=" + 33);
        }
    }

    private static void deleteRcsMediaResource(ContentResolver resolver, String selection) {
        if (resolver != null && selection != null) {
            Cursor cursor = resolver.query(Calls.CONTENT_URI, mProjection, selection, null, "date DESC");
            while (cursor.moveToNext()) {
                try {
                    String picturePath = cursor.getString(0);
                    String postCallVoicePath = cursor.getString(1);
                    deleteFile(picturePath);
                    deleteFile(postCallVoicePath);
                } catch (Exception e) {
                    HwLog.e("RcsContactsUtils", e.getMessage());
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void deleteFile(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.isFile() && file.delete()) {
                HwLog.d("RcsContactsUtils", path + " : is deletd");
            }
        }
    }

    public static void startRcsCall(Context context, String number, String subject, boolean importance, double longitude, double latitude, String picturePath, boolean isRcsCall) {
        if (context != null) {
            int subId = SimFactoryManager.getUserDefaultSubscription();
            Intent intent = new Intent();
            try {
                intent = CallUtil.getCallIntent(number, subId);
                MSimSmsManagerEx.setSimIdToIntent(intent, subId);
            } catch (Exception e) {
                intent.putExtra("subscription", subId);
                e.printStackTrace();
            }
            intent.putExtra("subject", subject);
            intent.putExtra("is_primary", importance);
            intent.putExtra("longitude", longitude);
            intent.putExtra("latitude", latitude);
            intent.putExtra("picture", picturePath);
            intent.putExtra("is_rcs_call", isRcsCall);
            context.startActivity(intent);
            deleteRcsCache(context, number);
        }
    }

    public static void deleteRcsCache(final Context context, final String number) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                context.getContentResolver().delete(RcsContactsUtils.RCS_PRE_CALL_CACHE_URI, "number = ? AND from_where = 0", new String[]{number});
            }
        });
    }

    public static void deleteRcsCache(final Context context, final String[] numbers) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                RcsContactsUtils.deleteContactsData(context, numbers);
            }
        });
    }

    public static void startPictureView(Context context, Activity activity, String picturePath) {
        if (context != null && activity != null && picturePath != null) {
            if (!new File(picturePath).exists()) {
                HwLog.i("RcsContactsUtils", "pre call picture not exist");
            }
            Intent viewImageIntent = new Intent("android.intent.action.VIEW");
            viewImageIntent.setDataAndType(FileProvider.getUriForFile(context, "com.android.contacts.files", new File(picturePath)), "image/png");
            viewImageIntent.addFlags(1);
            viewImageIntent.putExtra("view-as-uri-image", true);
            viewImageIntent.putExtra("SingleItemOnly", true);
            activity.startActivity(viewImageIntent);
        }
    }

    public static boolean isInChina(Context context) {
        boolean inChina = true;
        boolean googlePlayStoreExist = false;
        String networkOperator = ((TelephonyManager) context.getSystemService("phone")).getNetworkOperator();
        if (networkOperator != null && networkOperator.trim().length() >= 3) {
            inChina = networkOperator.startsWith("460");
        }
        if (!inChina) {
            googlePlayStoreExist = isPackagesExist(context, "com.google.android.gms", "com.android.vending");
        }
        if (inChina || !r0) {
            return true;
        }
        return false;
    }

    public static boolean isPackagesExist(Context context, String... pkgs) {
        if (pkgs == null) {
            return false;
        }
        try {
            for (String pkg : pkgs) {
                context.getPackageManager().getPackageGids(pkg);
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static void deleteContactsData(Context context, String[] numbers) {
        StringBuilder sbNumbers = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            sbNumbers.append("number").append("= ?");
            if (i < numbers.length - 1) {
                sbNumbers.append(" OR ");
            }
        }
        sbNumbers.append(" AND ");
        context.getContentResolver().delete(RCS_PRE_CALL_CACHE_URI, sbNumbers.toString() + "from_where" + " = 0", numbers);
    }

    public static boolean isSettingsLocationOpen(Context context) {
        if (Secure.getInt(context.getContentResolver(), "location_mode", 0) != 0) {
            return true;
        }
        return false;
    }
}
