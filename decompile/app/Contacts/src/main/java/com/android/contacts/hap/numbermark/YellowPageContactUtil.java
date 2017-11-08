package com.android.contacts.hap.numbermark;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.common.collect.ImmutableList.Builder;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YellowPageContactUtil {
    static String[] YELLOW_PAGE_PROJECTION = new String[]{"_ID", MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, "name", "photo", "group_name"};
    static String[] YELLOW_PAGE_VIEW_PROJECTION = new String[]{"ypid", "photo", "name"};

    public static Contact loadContactEntityFromYellowPage(ContentResolver resolver, Uri contactUri) {
        Cursor cursor = resolver.query(contactUri, YELLOW_PAGE_PROJECTION, null, null, null);
        if (cursor == null) {
            HwLog.e("YellowPageContactUtil", "No cursor returned in loadContactEntityFromYellowPage");
            return Contact.forNotFound(contactUri);
        }
        Contact forNotFound;
        try {
            if (cursor.moveToFirst()) {
                String photoUri = cursor.getString(3);
                Contact contact = new Contact(contactUri, contactUri, contactUri, 0, null, cursor.getLong(0), 0, 0, photoUri == null ? -5 : 1, photoUri, cursor.getString(2), cursor.getString(2), null, false, null, false, null, false);
                Builder<RawContact> rawContactsBuilder = new Builder();
                RawContact rawContact = buildDefaultRawContact(cursor.getLong(0));
                rawContactsBuilder.add((Object) rawContact);
                buildDataValues(rawContact, new JSONObject(cursor.getString(1)));
                contact.setRawContacts(rawContactsBuilder.build());
                contact.setYellowPage(true);
                cursor.close();
                return contact;
            }
            forNotFound = Contact.forNotFound(contactUri);
            return forNotFound;
        } catch (Throwable jse) {
            HwLog.e("YellowPageContactUtil", "data error", jse);
            forNotFound = Contact.forError(contactUri, jse);
            return forNotFound;
        } finally {
            cursor.close();
        }
    }

    private static RawContact buildDefaultRawContact(long contactId) {
        ContentValues cv = new ContentValues();
        cv.put("_id", Long.valueOf(-1));
        cv.putNull("account_name");
        cv.putNull("account_type");
        cv.putNull("data_set");
        cv.putNull("account_type_and_data_set");
        cv.put("dirty", Integer.valueOf(0));
        cv.put("version", Integer.valueOf(1));
        cv.putNull("sourceid");
        cv.putNull("sync1");
        cv.putNull("sync2");
        cv.putNull("sync3");
        cv.putNull("sync4");
        cv.put("deleted", Integer.valueOf(0));
        cv.put("starred", Integer.valueOf(0));
        cv.put("contact_id", Long.valueOf(contactId));
        return new RawContact(cv);
    }

    private static void buildDataValues(RawContact rawContact, JSONObject entry) throws JSONException {
        JSONArray array = entry.getJSONArray("phone");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            ContentValues cv = new ContentValues();
            cv.put("_id", Long.valueOf(0));
            cv.put("mimetype", "vnd.android.cursor.item/phone_v2");
            cv.put("data2", Integer.valueOf(0));
            cv.put("data3", obj.getString("name"));
            cv.put("data1", obj.getString("phone"));
            rawContact.addDataItemValues(cv);
        }
        if (entry.has("address")) {
            cv = new ContentValues();
            cv.put("_id", Long.valueOf(0));
            cv.put("mimetype", "vnd.android.cursor.item/postal-address_v2");
            cv.put("data1", entry.getString("address"));
            cv.put("data4", entry.getString("address"));
            cv.put("data2", Integer.valueOf(2));
            rawContact.addDataItemValues(cv);
        }
    }

    public static void buildContentValuesList(JSONObject entry, Map<String, List<ContentValues>> map) throws JSONException {
        String name = entry.getString("name");
        ArrayList<ContentValues> nameList = new ArrayList();
        ContentValues cv = new ContentValues();
        cv.put("data1", name);
        nameList.add(cv);
        cv = new ContentValues();
        cv.put("data2", name);
        nameList.add(cv);
        map.put("vnd.android.cursor.item/name", nameList);
        ArrayList<ContentValues> phoneList = new ArrayList();
        JSONArray array = entry.getJSONArray("phone");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put("data1", obj.getString("phone"));
            values.put("data3", obj.getString("name"));
            values.put("data2", Integer.valueOf(0));
            phoneList.add(values);
        }
        map.put("vnd.android.cursor.item/phone_v2", phoneList);
    }

    public static void loadPhotoBinaryData(Contact contactData) {
        String photoUri = contactData.getPhotoUri();
        if (photoUri != null) {
            try {
                contactData.setPhotoBinaryData(readPhotoBinaryData(photoUri));
            } catch (IOException e) {
            }
        }
    }

    private static byte[] readPhotoBinaryData(String path) throws IOException {
        if (path == null) {
            return null;
        }
        byte[] buffer = new byte[16384];
        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            try {
                int size = fis.read(buffer);
                if (size == -1) {
                    break;
                }
                baos.write(buffer, 0, size);
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    HwLog.e("YellowPageContactUtil", "failed to close FileOutputStream");
                }
            }
        }
        byte[] toByteArray = baos.toByteArray();
        return toByteArray;
    }

    public static Uri queryYellowPageUriForNumber(Context context, String number, boolean normalizationNeeded) {
        if (context == null || TextUtils.isEmpty(number)) {
            return null;
        }
        String normalizedNumber;
        if (normalizationNeeded) {
            normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
        } else {
            normalizedNumber = number;
        }
        if (TextUtils.isEmpty(normalizedNumber)) {
            return null;
        }
        Cursor c = context.getContentResolver().query(ContactsAppProvider.YELLOW_PAGE_DATA_URI, YELLOW_PAGE_VIEW_PROJECTION, "PHONE_NUMBERS_EQUAL(number,?)", new String[]{normalizedNumber}, null);
        if (c == null) {
            return null;
        }
        try {
            if (c.moveToFirst()) {
                Uri withAppendedPath = Uri.withAppendedPath(ContactsAppProvider.YELLOW_PAGE_URI, c.getString(0));
                return withAppendedPath;
            }
            c.close();
            return null;
        } finally {
            c.close();
        }
    }

    public static boolean isYellowPageUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        return "com.android.contacts.app".equals(uri.getAuthority());
    }
}
