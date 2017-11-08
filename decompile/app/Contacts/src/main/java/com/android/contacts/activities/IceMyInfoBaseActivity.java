package com.android.contacts.activities;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.util.Log;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.util.ContactLoaderUtils;

public class IceMyInfoBaseActivity extends Activity {
    private final String[] PROJECTION_PRIMARY = new String[]{"photo_thumb_uri", "photo_id", "mimetype", "data1", "data4", "data15"};
    protected String mDisplayName;
    private Uri profileLookupUri;

    protected Bitmap loadProfileDataInfo(Uri mProfileLookupUri) {
        this.mDisplayName = null;
        Bitmap bitmap = null;
        Cursor dataCursor = null;
        if (mProfileLookupUri != null) {
            dataCursor = getContentResolver().query(Uri.withAppendedPath(mProfileLookupUri, "entities"), this.PROJECTION_PRIMARY, null, null, null);
        }
        if (dataCursor != null) {
            if (dataCursor.moveToFirst()) {
                Bitmap bitmap2 = null;
                do {
                    String mimetype = dataCursor.getString(2);
                    String data = dataCursor.getString(3);
                    if ("vnd.android.cursor.item/name".equals(mimetype)) {
                        this.mDisplayName = data;
                    } else if ("vnd.android.cursor.item/photo".equals(mimetype)) {
                        byte[] photoData = dataCursor.getBlob(5);
                        if (photoData != null) {
                            try {
                                bitmap2 = ContactPhotoManager.createRoundPhoto(BitmapFactory.decodeByteArray(photoData, 0, photoData.length, null));
                            } catch (Exception e) {
                                Log.e("IceMyInfoBaseActivity", e.getMessage());
                            } catch (Throwable th) {
                                if (dataCursor != null) {
                                    dataCursor.close();
                                }
                            }
                            bitmap = bitmap2;
                        }
                    }
                } while (dataCursor.moveToNext());
            }
        }
        if (dataCursor != null) {
            dataCursor.close();
        }
        return bitmap;
    }

    protected Uri getProfileLookupUri() {
        Cursor cursor = getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id", "lookup"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    this.profileLookupUri = Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                    this.profileLookupUri = ContactLoaderUtils.ensureIsContactUri(getContentResolver(), this.profileLookupUri);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return this.profileLookupUri;
                }
            } catch (IllegalArgumentException e) {
                Log.e("IceMyInfoBaseActivity", e.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        Uri uri = this.profileLookupUri;
        if (cursor != null) {
            cursor.close();
        }
        return uri;
    }
}
