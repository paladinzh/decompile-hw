package com.android.mms.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class ContactUtil {
    private static final String[] CONTACTS_COLUMNS = new String[]{"_id", "display_name"};

    public static class NumberInfo {
        public long dataId;
        public boolean isDefaultNumber;
        public String label;
        public String number;
    }

    public static List<HwQuickActionContact> getFavoriteContact(Context context) {
        Exception e;
        Uri uri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter("limit", String.valueOf(3)).build();
        List<HwQuickActionContact> favoriteList = new ArrayList();
        Cursor cursor = context.getContentResolver().query(uri, CONTACTS_COLUMNS, "starred = 1", null, "sort_key ASC");
        if (cursor == null) {
            return favoriteList;
        }
        HwQuickActionContact mmsContact;
        Log.i("ContentUtils", "has FavoriteContact count " + cursor.getCount());
        HwQuickActionContact mmsContact2 = null;
        while (cursor.moveToNext()) {
            try {
                mmsContact = new HwQuickActionContact();
                try {
                    String contactName = cursor.getString(cursor.getColumnIndex("display_name"));
                    long contactId = cursor.getLong(cursor.getColumnIndex("_id"));
                    byte[] photoData = openPhoto(context, contactId);
                    if (TextUtils.isEmpty(contactName)) {
                        contactName = context.getResources().getString(R.string.missing_name);
                    }
                    mmsContact.setId(contactId);
                    mmsContact.setContactName(contactName);
                    mmsContact.setData(photoData);
                    favoriteList.add(mmsContact);
                    mmsContact2 = mmsContact;
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
                mmsContact = mmsContact2;
            } catch (Throwable th) {
                Throwable th2 = th;
                mmsContact = mmsContact2;
            }
        }
        cursor.close();
        mmsContact = mmsContact2;
        return favoriteList;
        try {
            Log.e("ContentUtils", e.getMessage());
            cursor.close();
            return favoriteList;
        } catch (Throwable th3) {
            th2 = th3;
            cursor.close();
            throw th2;
        }
    }

    public static List<NumberInfo> getContactNumberInfosByCursor(Context context, Cursor cursor) {
        Exception e;
        List<NumberInfo> numbers = new ArrayList();
        if (cursor == null) {
            return numbers;
        }
        NumberInfo numberInfo;
        Log.i("ContentUtils", "ContactNumber count :" + cursor.getCount());
        NumberInfo numberInfo2 = null;
        while (cursor.moveToNext()) {
            try {
                numberInfo = new NumberInfo();
                try {
                    String number = cursor.getString(cursor.getColumnIndex("data1"));
                    int isDefault = cursor.getInt(cursor.getColumnIndex("is_super_primary"));
                    int phoneType = cursor.getInt(cursor.getColumnIndex("data2"));
                    String customPhoneLabel = cursor.getString(cursor.getColumnIndex("data3"));
                    long dataId = cursor.getLong(cursor.getColumnIndex("data_id"));
                    String label = (String) Phone.getTypeLabel(context.getResources(), phoneType, customPhoneLabel);
                    if (isDefault == 1) {
                        numberInfo.isDefaultNumber = true;
                    }
                    numberInfo.number = number;
                    numberInfo.label = label;
                    numberInfo.dataId = dataId;
                    numbers.add(numberInfo);
                    numberInfo2 = numberInfo;
                } catch (Exception e2) {
                    e = e2;
                }
            } catch (Exception e3) {
                e = e3;
                numberInfo = numberInfo2;
            } catch (Throwable th) {
                Throwable th2 = th;
                numberInfo = numberInfo2;
            }
        }
        cursor.close();
        numberInfo = numberInfo2;
        return numbers;
        try {
            Log.e("ContentUtils", e.getMessage());
            cursor.close();
            return numbers;
        } catch (Throwable th3) {
            th2 = th3;
            cursor.close();
            throw th2;
        }
    }

    public static byte[] openPhoto(Context context, long contactId) {
        byte[] noData = new byte[0];
        Uri photoUri = Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId), "photo");
        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{"data15"}, null, null, null);
        if (cursor == null) {
            return noData;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return data;
                }
                cursor.close();
                return noData;
            }
            cursor.close();
            return noData;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }

    public static Bitmap createRoundPhoto(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0, 0, size, size);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public static void setSuperPrimary(Context context, long dataId) {
        Log.i("ContentUtils", " setSuperPrimary");
        if (dataId == -1) {
            Log.e("ContentUtils", "Invalid arguments for setSuperPrimary request");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("is_super_primary", Integer.valueOf(1));
        values.put("is_primary", Integer.valueOf(1));
        values.put("is_super_primary", Integer.valueOf(1));
        values.put("data5", Integer.valueOf(0));
        Log.i("ContentUtils", "ContentUris.withAppendedId(Data.CONTENT_URI, dataId) " + ContentUris.withAppendedId(Data.CONTENT_URI, dataId));
        Log.i("ContentUtils", "ContentUris.withAppendedId(Contacts.CONTENT_URI, dataId) " + ContentUris.withAppendedId(Contacts.CONTENT_URI, dataId));
        context.getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, dataId), values, null, null);
    }

    public static Uri getProfileLookupUri(Context mContext) {
        if (mContext == null) {
            return null;
        }
        Cursor cursor = mContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id", "lookup"}, null, null, null);
        Uri profileLookupUri = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    profileLookupUri = ensureIsContactUri(mContext.getContentResolver(), Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1)));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return profileLookupUri;
                }
            } catch (Exception e) {
                Log.e("ContentUtils", "" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public static Uri ensureIsContactUri(ContentResolver resolver, Uri uri) throws IllegalArgumentException {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
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
            Log.e("ContentUtils", "uri:" + uri);
            throw new IllegalArgumentException("uri format is unknown");
        } else if (isYellowPageUri(uri)) {
            return uri;
        } else {
            String OBSOLETE_AUTHORITY = "contacts";
            if ("contacts".equals(authority)) {
                return RawContacts.getContactLookupUri(resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, ContentUris.parseId(uri)));
            }
            Log.e("ContentUtils", "uri:" + uri);
            throw new IllegalArgumentException("uri authority is unknown");
        }
    }

    public static boolean isYellowPageUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        return "com.android.contacts.app".equals(uri.getAuthority());
    }
}
