package com.huawei.mms.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.directory.DirectoryQuery;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.cache.MmsMatchContact;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class MatchedContactsHelper {
    private static final String[] PROJECTION_MATCH_CONTACTS = new String[]{"contact_id", "display_name", "data1", "sort_key", "sort_key_alt"};
    private static HwCustMatchedContactsHelper mCust = ((HwCustMatchedContactsHelper) HwCustUtils.createObj(HwCustMatchedContactsHelper.class, new Object[]{MmsApp.getApplication().getApplicationContext()}));
    private static int mDisplayNameOrder = -1;
    private static int mDisplayOrder = -1;
    private static String[] recentContactProjection = new String[]{"display_name", "data1", "contact_id", "last_time_used"};
    static HashSet<String> set = new HashSet();

    public static String caculateSearchKey(CharSequence s) {
        if (s == null || TextUtils.isEmpty(s)) {
            return "";
        }
        int length = s.length();
        for (int i = length; i > 0; i--) {
            char charAt = s.charAt(i - 1);
            if (',' == charAt || ';' == charAt) {
                return s.subSequence(i, length).toString().trim();
            }
        }
        return s.toString().trim();
    }

    public static ArrayList<MmsMatchContact> getMatchedContacts(Context context, String queryStr) {
        String sortOrder;
        Uri uri = null;
        if (!TextUtils.isEmpty(queryStr)) {
            uri = Uri.withAppendedPath(SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, Uri.encode(queryStr)).buildUpon().appendQueryParameter("search_type", "search_contacts_mms").build().buildUpon().appendQueryParameter("search_email", "true").build().buildUpon().appendQueryParameter("limit", String.valueOf(VTMCDataCache.MAXSIZE)).build();
            if (!(mCust == null || TextUtils.isEmpty(mCust.getHwRussiaNumberRelevance()))) {
                uri = uri.buildUpon().appendQueryParameter("search_number", mCust.getFlagForRussiaNumberRelevance()).build();
            }
        }
        if (TextUtils.isEmpty(queryStr) || !Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            sortOrder = SortCursor.getSortOrder(context);
        } else {
            sortOrder = "pinyin_name";
        }
        ArrayList<MmsMatchContact> matchContacts = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, uri, PROJECTION_MATCH_CONTACTS, null, null, sortOrder);
            set.clear();
            ArrayList<MmsMatchContact> enterpriseContacts;
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                if (MmsConfig.isEnableAFW()) {
                    enterpriseContacts = getMatchedEnterpriseContacts(context, queryStr);
                    if (enterpriseContacts != null) {
                        matchContacts.addAll(enterpriseContacts);
                    }
                }
                return matchContacts;
            }
            do {
                int sortKeyIndex;
                long contactId = cursor.getLong(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                int mMatchType = getMatchType(cursor);
                MmsMatchContact contact = new MmsMatchContact(name, number, Long.valueOf(contactId));
                if (getDisplayNameOrder(context) == 1) {
                    sortKeyIndex = cursor.getColumnIndex("sort_key");
                } else {
                    sortKeyIndex = cursor.getColumnIndex("sort_key_alt");
                }
                if (sortKeyIndex != -1) {
                    contact.mSortKey = cursor.getString(sortKeyIndex);
                }
                contact.setType(mMatchType);
                set.add(formateNumber(contact.mNumber));
                matchContacts.add(contact);
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            if (MmsConfig.isEnableAFW()) {
                enterpriseContacts = getMatchedEnterpriseContacts(context, queryStr);
                if (enterpriseContacts != null) {
                    matchContacts.addAll(enterpriseContacts);
                }
            }
            return matchContacts;
        } catch (Exception e) {
            MLog.e("MatchedContactsHelper", "getMainContacts::query the matched contacts exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static ArrayList<MmsMatchContact> getMatchedEnterpriseContacts(Context context, String queryStr) {
        long enterpriseDirectoryId = queryDiretory(context);
        if (enterpriseDirectoryId <= -1 || TextUtils.isEmpty(queryStr)) {
            return null;
        }
        String sortOrder;
        Builder builder = Uri.withAppendedPath(Phone.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(queryStr)).buildUpon();
        builder.appendQueryParameter("search_type", "search_contacts_mms");
        builder.appendQueryParameter("directory", String.valueOf(enterpriseDirectoryId));
        builder.appendQueryParameter("search_email", "true");
        builder.appendQueryParameter("limit", String.valueOf(VTMCDataCache.MAXSIZE));
        Uri enterpriseUri = builder.build();
        if (!(mCust == null || TextUtils.isEmpty(mCust.getHwRussiaNumberRelevance()))) {
            enterpriseUri = enterpriseUri.buildUpon().appendQueryParameter("search_number", mCust.getFlagForRussiaNumberRelevance()).build();
        }
        if (TextUtils.isEmpty(queryStr) || !Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            sortOrder = SortCursor.getSortOrder(context);
        } else {
            sortOrder = "pinyin_name";
        }
        Cursor cursor = null;
        ArrayList<MmsMatchContact> matchContacts = new ArrayList();
        try {
            cursor = SqliteWrapper.query(context, enterpriseUri, DirectoryQuery.getEnterpriseProjection(), null, null, sortOrder);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                MLog.d("MatchedContactsHelper", "getMatchedContacts,matchContacts.size : " + matchContacts.size());
                return matchContacts;
            }
            do {
                int sortKeyIndex;
                long contactId = cursor.getLong(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                String lookupKey = cursor.getString(5);
                int mMatchType = getMatchType(cursor);
                MmsMatchContact contact = new MmsMatchContact(name, number, Long.valueOf(contactId), lookupKey);
                if (getDisplayNameOrder(context) == 1) {
                    sortKeyIndex = cursor.getColumnIndex("sort_key");
                } else {
                    sortKeyIndex = cursor.getColumnIndex("sort_key_alt");
                }
                if (sortKeyIndex != -1) {
                    contact.mSortKey = cursor.getString(sortKeyIndex);
                }
                contact.setType(mMatchType);
                if (!set.contains(formateNumber(contact.mNumber))) {
                    matchContacts.add(contact);
                }
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            MLog.d("MatchedContactsHelper", "getMatchedContacts,matchContacts.size : " + matchContacts.size());
            return matchContacts;
        } catch (Exception e) {
            MLog.e("MatchedContactsHelper", "getMatchedEnterpriseContacts::query the matched contacts exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static String formateNumber(String number) {
        return number.contains(" ") ? number.replaceAll(" ", "") : number;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static long queryDiretory(Context context) {
        Cursor directoryCursor = SqliteWrapper.query(context, DirectoryQuery.URI, DirectoryQuery.getProjection(), null, null, "_id");
        long enterpriseDirectoryId = -1;
        if (directoryCursor != null) {
            long directoryId;
            do {
                try {
                    if (!directoryCursor.moveToNext()) {
                        break;
                    }
                    directoryId = directoryCursor.getLong(0);
                } catch (Exception e) {
                    MLog.e("MatchedContactsHelper", "getMatchedContacts::query the matched enterprise contact exception: " + e);
                } catch (Throwable th) {
                    directoryCursor.close();
                }
            } while (!DirectoryQuery.isEnterpriseDirectoryId(directoryId));
            enterpriseDirectoryId = directoryId;
            directoryCursor.close();
        }
        return enterpriseDirectoryId;
    }

    public static ArrayList<MmsMatchContact> removeDuplicateMatchContacts(ArrayList<MmsMatchContact> oldMatchList) {
        if (oldMatchList == null || oldMatchList.size() <= 1) {
            return oldMatchList;
        }
        ArrayList<MmsMatchContact> newMatchList = new ArrayList();
        int length = oldMatchList.size();
        for (int i = 0; i < length; i++) {
            MmsMatchContact contact = (MmsMatchContact) oldMatchList.get(i);
            boolean isEmail = Contact.isEmailAddress(contact.mNumber);
            boolean needRemove = false;
            for (int j = i - 1; j >= 0; j--) {
                MmsMatchContact lastContact = (MmsMatchContact) oldMatchList.get(j);
                if (contact.mName != null && !contact.mName.equals(lastContact.mName)) {
                    break;
                }
                if (isEmail) {
                    if (contact.mNumber.equals(lastContact.mNumber)) {
                        needRemove = true;
                        break;
                    }
                } else if (AddrMatcher.isNumbersEqualWithoutSign(contact.mNumber, lastContact.mNumber)) {
                    needRemove = true;
                    break;
                }
            }
            if (!needRemove) {
                newMatchList.add(contact);
            }
        }
        oldMatchList.clear();
        return newMatchList;
    }

    public static ArrayList<MmsMatchContact> initRecentContact(Context context, int maxRecentContactsCount) {
        Cursor cursor = null;
        ArrayList<MmsMatchContact> recentCantacts = new ArrayList();
        try {
            cursor = SqliteWrapper.query(context, Data.CONTENT_URI, recentContactProjection, "mimetype_id=5 AND last_time_used>0", null, "last_time_used DESC");
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return recentCantacts;
            }
            int iCount = 0;
            while (cursor.moveToNext() && iCount < maxRecentContactsCount) {
                recentCantacts.add(new MmsMatchContact(cursor.getString(0), NumberUtils.formatAndParseNumber(cursor.getString(1), null), Long.valueOf(cursor.getLong(2))));
                iCount++;
            }
            if (cursor != null) {
                cursor.close();
            }
            return recentCantacts;
        } catch (Exception e) {
            MLog.e("MatchedContactsHelper", "initRecentContact::query the recent contact exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static ArrayList<MmsMatchContact> removeDuplicateRecentContacts(ArrayList<MmsMatchContact> oldMatchList, List<String> addresses) {
        if (oldMatchList == null || oldMatchList.size() < 1 || addresses == null || addresses.size() < 1) {
            return oldMatchList;
        }
        ArrayList<MmsMatchContact> newMatchList = new ArrayList();
        for (MmsMatchContact contact : oldMatchList) {
            boolean isEmailOld = Contact.isEmailAddress(contact.mNumber);
            boolean needRemove = false;
            for (String address : addresses) {
                boolean isEmailNew = Contact.isEmailAddress(address);
                if (!isEmailOld) {
                    if (!isEmailNew && HwNumberMatchUtils.isNumbersMatched(address, contact.mNumber)) {
                        needRemove = true;
                        break;
                    }
                } else if (isEmailNew && contact.mNumber.equals(address)) {
                    needRemove = true;
                    break;
                }
            }
            if (!needRemove) {
                newMatchList.add(contact);
            }
        }
        return newMatchList;
    }

    public static int getMatchType(Cursor cursor) {
        int columnIndex = cursor.getColumnIndexOrThrow("search_result");
        if (columnIndex < 0) {
            return -1;
        }
        return shiftMatchType(cursor.getLong(columnIndex));
    }

    protected static int shiftMatchType(long result) {
        return (int) ((result >> 32) & 255);
    }

    public static boolean isDisplayOrderUserChangeable(Context context) {
        return context.getResources().getBoolean(R.bool.config_display_order_user_changeable);
    }

    public static int getDefaultDisplayOrder(Context context) {
        if (context.getResources().getBoolean(R.bool.config_default_display_order_primary)) {
            return 1;
        }
        return 2;
    }

    public static int getDisplayOrder(Context context) {
        if (!isDisplayOrderUserChangeable(context)) {
            return getDefaultDisplayOrder(context);
        }
        if (mDisplayOrder == -1) {
            try {
                mDisplayOrder = System.getInt(context.getContentResolver(), "android.contacts.DISPLAY_ORDER");
            } catch (SettingNotFoundException e) {
                mDisplayOrder = getDefaultDisplayOrder(context);
            }
        }
        return mDisplayOrder;
    }

    private static int getDisplayNameOrder(Context context) {
        if (mDisplayNameOrder == -1) {
            mDisplayNameOrder = getDisplayOrder(context);
        }
        return mDisplayNameOrder;
    }
}
