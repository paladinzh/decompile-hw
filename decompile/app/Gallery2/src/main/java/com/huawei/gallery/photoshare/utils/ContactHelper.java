package com.huawei.gallery.photoshare.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.ui.WMEditor;
import java.util.ArrayList;
import java.util.List;

public class ContactHelper {
    private static final Uri CALLS_WITH_PRESENCE_URI = Calls.CONTENT_URI;
    private static final String[] CALL_ID_PROJECTION = new String[]{"_id", WMEditor.TYPENUM, "name"};
    private static final String[] CONTACT_ID_PROJECTION = new String[]{"_id", "data1", "display_name"};
    private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

    public static class Contact {
        private String mId;
        private String mName;
        private String mNumber;

        public Contact(String id, String name, String number) {
            this.mId = id;
            this.mName = name;
            this.mNumber = number;
        }

        public String getName() {
            return this.mName;
        }

        public String getNumber() {
            return this.mNumber;
        }
    }

    public static List<Contact> getContactInfoForPhoneUris(Context context, Parcelable[] uris) {
        if (uris.length == 0) {
            return null;
        }
        StringBuilder idsBuilderData = new StringBuilder();
        boolean hasContactData = false;
        StringBuilder idsBuilderCalls = new StringBuilder();
        boolean hasCallLogData = false;
        for (Parcelable p : uris) {
            Uri uri = (Uri) p;
            if (uri != null && "content".equals(uri.getScheme())) {
                if (uri.toString().startsWith("content://com.android.contacts/data/")) {
                    if (hasContactData) {
                        idsBuilderData.append(',').append(uri.getLastPathSegment());
                    } else {
                        hasContactData = true;
                        idsBuilderData.append(uri.getLastPathSegment());
                    }
                } else if (uri.toString().startsWith("content://call_log/calls/")) {
                    if (hasCallLogData) {
                        idsBuilderCalls.append(',').append(uri.getLastPathSegment());
                    } else {
                        hasCallLogData = true;
                        idsBuilderCalls.append(uri.getLastPathSegment());
                    }
                }
            }
        }
        return getContacts(context, idsBuilderData, hasContactData, idsBuilderCalls, hasCallLogData);
    }

    private static List<Contact> getContacts(Context context, StringBuilder idsBuilderData, boolean hasContactData, StringBuilder idsBuilderCalls, boolean hasCallLogData) {
        if (!hasContactData && !hasCallLogData) {
            return null;
        }
        List<Contact> entries = new ArrayList();
        if (hasContactData) {
            List<Contact> dataContacts = getContactsFromDataDB(context, idsBuilderData.toString());
            if (dataContacts != null && dataContacts.size() > 0) {
                entries.addAll(dataContacts);
            }
        }
        if (hasCallLogData) {
            List<Contact> callsContacts = getContactsFromCallLogDB(context, idsBuilderCalls.toString());
            if (callsContacts != null && callsContacts.size() > 0) {
                entries.addAll(callsContacts);
            }
        }
        return entries;
    }

    private static List<Contact> getContactsFromDataDB(Context context, String whereCause) {
        Cursor cursor = null;
        if (whereCause.length() > 0) {
            try {
                cursor = context.getContentResolver().query(PHONES_WITH_PRESENCE_URI, CONTACT_ID_PROJECTION, "_id IN (" + whereCause + ")", null, null);
            } catch (Exception e) {
                GalleryLog.v("ContactHelper", " getContactsFromDataDB occur exception when query contact!" + e);
            }
        }
        if (cursor == null) {
            return null;
        }
        List<Contact> dataContacts = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                String id = cursor.getString(0);
                String number = parseMmsAddress(cursor.getString(1));
                if (TextUtils.isEmpty(number)) {
                    number = cursor.getString(1);
                }
                dataContacts.add(new Contact(id, cursor.getString(2), number));
            } finally {
                cursor.close();
            }
        }
        return dataContacts;
    }

    private static List<Contact> getContactsFromCallLogDB(Context context, String whereCause) {
        Cursor cursor = null;
        if (whereCause.length() > 0) {
            try {
                cursor = context.getContentResolver().query(CALLS_WITH_PRESENCE_URI, CALL_ID_PROJECTION, "_id IN (" + whereCause + ")", null, null);
            } catch (Exception e) {
                GalleryLog.v("ContactHelper", "getContactsFromCallLogDB occur exception when query contact!" + e);
            }
        }
        if (cursor == null) {
            return null;
        }
        List<Contact> callsContacts = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                callsContacts.add(new Contact(cursor.getString(0), cursor.getString(2), cursor.getString(1)));
            } finally {
                cursor.close();
            }
        }
        return callsContacts;
    }

    private static String parseMmsAddress(String address) {
        String retVal = parsePhoneNumberForMms(address);
        if (TextUtils.isEmpty(retVal)) {
            return null;
        }
        return retVal;
    }

    private static String parsePhoneNumberForMms(String address) {
        if (TextUtils.isEmpty(address)) {
            return address;
        }
        StringBuilder builder = new StringBuilder();
        int len = address.length();
        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);
            if (c == '+' && builder.length() == 0) {
                builder.append(c);
            } else if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
