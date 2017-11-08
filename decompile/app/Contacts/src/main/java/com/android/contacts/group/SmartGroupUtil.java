package com.android.contacts.group;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class SmartGroupUtil {
    private static final String[] COLUMNS = new String[]{"_id", "display_name", "photo_uri", "lookup", "contact_presence", "contact_status", "photo_id", "name_raw_contact_id"};
    public static final Uri COMPANY_CLASSIFY_URI = Uri.parse("content://com.android.contacts/company/classify");
    public static final Uri LAST_TIME_CONTACT_URI = Uri.parse("content://com.android.contacts/frequent/classify");
    public static final Uri LOCATION_URI = Uri.parse("content://com.android.contacts/location/classify");
    public static final Uri SMART_GROUP_URI = Uri.parse("content://com.android.contacts/smartGroup");

    public static boolean isSmratGroup(String queryType) {
        if ("smart_groups_company".equals(queryType) || "smart_groups_location".equals(queryType) || "smart_groups_last_contact_time".equals(queryType)) {
            return true;
        }
        return false;
    }

    public static String getDefaultGroupTitle(String queryType, Context context) {
        if (context == null) {
            return null;
        }
        if ("smart_groups_company".equals(queryType)) {
            return context.getResources().getString(R.string.contacts_company_null);
        }
        if ("smart_groups_location".equals(queryType)) {
            return context.getResources().getString(R.string.contacts_location_null);
        }
        return null;
    }

    public static String[] parseSmartGroupUri(Uri uri) {
        if (uri == null) {
            return new String[0];
        }
        String smartGroupType = uri.getQueryParameter("smart_groups_type");
        String smartGroupTitle = uri.getQueryParameter("smart_group_title");
        String predefinedSmartGroupType = uri.getQueryParameter("predefined_smart_group_type");
        if (smartGroupType == null || smartGroupTitle == null || predefinedSmartGroupType == null) {
            return new String[0];
        }
        return new String[]{smartGroupType, smartGroupTitle, predefinedSmartGroupType};
    }

    public static CursorLoader createSmartGroupLoader(Context context, Uri groupUri) {
        if (context == null) {
            return null;
        }
        String sortOrder;
        if (new ContactsPreferences(context).getSortOrder() == 1) {
            sortOrder = "sort_key";
        } else {
            sortOrder = "sort_key_alt";
        }
        String[] smartGroupParameters = parseSmartGroupUri(groupUri);
        if (smartGroupParameters.length != 0) {
            if ("smart_groups_company".equals(smartGroupParameters[0])) {
                return createSmartGroupCompanyDetailLoader(context, smartGroupParameters[1], Integer.parseInt(smartGroupParameters[2]), sortOrder);
            }
            if ("smart_groups_location".equals(smartGroupParameters[0])) {
                return createSmartGroupLocationDetailLoader(context, smartGroupParameters[1], Integer.parseInt(smartGroupParameters[2]), sortOrder);
            }
            if ("smart_groups_last_contact_time".equals(smartGroupParameters[0])) {
                return createSmartGroupLastTimeContactDetailLoader(context, Integer.parseInt(smartGroupParameters[2]), sortOrder);
            }
        }
        return null;
    }

    private static CursorLoader createSmartGroupCompanyDetailLoader(Context context, String title, int preSmartGroupType, String sortOrder) {
        if (context == null) {
            return null;
        }
        if (preSmartGroupType == 1) {
            String str = sortOrder + " ASC";
            return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "_id NOT IN ( SELECT DISTINCT contact_id FROM raw_contacts WHERE company IS NOT NULL AND deleted=0 AND REPLACE(company,\" \",\"\")<>\"\" )", null, str);
        }
        String[] strArr = new String[]{title};
        str = sortOrder + " ASC";
        return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "_id IN ( SELECT DISTINCT contact_id FROM raw_contacts WHERE company=? AND deleted=0 )", strArr, str);
    }

    private static CursorLoader createSmartGroupLocationDetailLoader(Context context, String title, int preSmartGroupType, String sortOrder) {
        if (context == null) {
            return null;
        }
        if (preSmartGroupType == 2) {
            String str = sortOrder + " ASC";
            return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "_id NOT IN (SELECT DISTINCT contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/phone_v2' AND data6 IS NOT NULL AND data6<>'N')", null, str);
        }
        String[] strArr = new String[]{title};
        str = sortOrder + " ASC";
        return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "_id IN (SELECT DISTINCT contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/phone_v2' AND data6 = ?)", strArr, str);
    }

    private static CursorLoader createSmartGroupLastTimeContactDetailLoader(Context context, int preSmartGroupType, String sortOrder) {
        if (context == null) {
            return null;
        }
        String curentTime = String.valueOf(System.currentTimeMillis());
        String[] strArr;
        String str;
        if (preSmartGroupType == 3) {
            strArr = new String[]{curentTime};
            str = sortOrder + " ASC";
            return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "?-last_time_contacted<=604800000", strArr, str);
        } else if (preSmartGroupType == 4) {
            strArr = new String[]{curentTime, curentTime};
            str = sortOrder + " ASC";
            return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "?-last_time_contacted>604800000 AND ?-last_time_contacted<=2592000000", strArr, str);
        } else if (preSmartGroupType == 5) {
            strArr = new String[]{curentTime, curentTime};
            str = sortOrder + " ASC";
            return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "?-last_time_contacted>2592000000 AND ?-last_time_contacted<=7776000000", strArr, str);
        } else if (preSmartGroupType != 6) {
            return null;
        } else {
            strArr = new String[]{curentTime};
            str = sortOrder + " ASC";
            return new CursorLoader(context, Contacts.CONTENT_URI, COLUMNS, "?-last_time_contacted>7776000000", strArr, str);
        }
    }

    public static String getAccurateNumberLocation(Context context, String numberLocation) {
        if (context == null || numberLocation == null) {
            return numberLocation;
        }
        String[] uselessNumberLocation = context.getResources().getStringArray(R.array.useless_number_location);
        String[] filterStrings = new String[(uselessNumberLocation.length + 2)];
        System.arraycopy(uselessNumberLocation, 0, filterStrings, 0, uselessNumberLocation.length);
        filterStrings[uselessNumberLocation.length] = context.getResources().getString(R.string.numberLocationUnknownLocation2);
        filterStrings[uselessNumberLocation.length + 1] = context.getResources().getString(R.string.geo_number_location);
        for (int i = 0; i < filterStrings.length; i++) {
            if (numberLocation.indexOf(filterStrings[i]) >= 0) {
                numberLocation = numberLocation.substring(0, numberLocation.indexOf(filterStrings[i]));
                break;
            }
        }
        numberLocation = numberLocation.trim();
        if (numberLocation.equals("")) {
            numberLocation = null;
        } else if (numberLocation.indexOf(HwCustPreloadContacts.EMPTY_STRING) >= 0) {
            numberLocation = numberLocation.substring(0, numberLocation.indexOf(HwCustPreloadContacts.EMPTY_STRING));
        }
        return numberLocation;
    }

    public static void updateNumberLocation(Context context, final String number, final String lGeocode, final String location) {
        if (context == null || number == null) {
            HwLog.i("SmartGroupUtil", "Context or number is null!");
            return;
        }
        final Context lContext = context.getApplicationContext();
        new Thread() {
            public void run() {
                String accurateGeocode = SmartGroupUtil.getAccurateNumberLocation(lContext, lGeocode);
                if (accurateGeocode == null) {
                    accurateGeocode = "N";
                }
                if (!accurateGeocode.equals(location)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("data6", accurateGeocode);
                    lContext.getContentResolver().update(Data.CONTENT_URI, contentValues, "data1= ? AND mimetype='vnd.android.cursor.item/phone_v2'", new String[]{number});
                }
            }
        }.start();
    }
}
