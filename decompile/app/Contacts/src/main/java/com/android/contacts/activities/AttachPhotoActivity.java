package com.android.contacts.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.ContactsContract.DisplayPhoto;
import android.widget.Toast;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactLoader;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.io.FileNotFoundException;
import java.util.List;

public class AttachPhotoActivity extends ContactsActivity {
    private static final String TAG = AttachPhotoActivity.class.getSimpleName();
    private Uri mContactUri;
    private ContentResolver mContentResolver;
    private Uri mCroppedPhotoUri;
    private int mPhotoDim;
    private Uri mTempPhotoUri;

    private interface Listener {
        void onContactLoaded(Contact contact);
    }

    public void onCreate(Bundle icicle) {
        Uri uri = null;
        super.onCreate(icicle);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        this.mContentResolver = getContentResolver();
        Cursor c = this.mContentResolver.query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"display_max_dim"}, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    this.mPhotoDim = c.getInt(0);
                }
                c.close();
            } catch (Throwable th) {
                c.close();
            }
        }
        if (icicle != null) {
            String uri2 = icicle.getString("contact_uri");
            if (uri2 != null) {
                uri = Uri.parse(uri2);
            }
            this.mContactUri = uri;
            this.mTempPhotoUri = Uri.parse(icicle.getString("temp_photo_uri"));
            this.mCroppedPhotoUri = Uri.parse(icicle.getString("cropped_photo_uri"));
            return;
        }
        this.mTempPhotoUri = ContactPhotoUtils.generateTempImageUri(this);
        this.mCroppedPhotoUri = ContactPhotoUtils.generateTempCroppedImageUri(this);
        Intent intent;
        if (getIntent().getStringExtra("gallary_contact_uri") != null) {
            Intent myIntent = getIntent();
            intent = new Intent("com.android.camera.action.CROP", myIntent.getData());
            if (myIntent.getStringExtra("mimeType") != null) {
                intent.setDataAndType(myIntent.getData(), myIntent.getStringExtra("mimeType"));
            }
            ContactPhotoUtils.addPhotoPickerExtras(intent, this.mCroppedPhotoUri);
            ContactPhotoUtils.addCropExtras(intent, this.mPhotoDim);
            try {
                startActivityForResult(intent, 2);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.quickcontact_missing_app_Toast, 0).show();
            }
            this.mContactUri = Uri.parse(myIntent.getStringExtra("gallary_contact_uri"));
            return;
        }
        intent = new Intent("android.intent.action.PICK");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.putExtra("PICK_CONTACT_PHOTO", true);
        startActivityForResult(intent, 1);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mContactUri != null) {
            outState.putString("contact_uri", this.mContactUri.toString());
        }
        if (this.mTempPhotoUri != null) {
            outState.putString("temp_photo_uri", this.mTempPhotoUri.toString());
        }
        if (this.mCroppedPhotoUri != null) {
            outState.putString("cropped_photo_uri", this.mCroppedPhotoUri.toString());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == 1) {
            if (resultCode != -1) {
                finish();
                return;
            }
            Uri toCrop;
            Intent myIntent = getIntent();
            Uri uri = null;
            int perm = -1;
            if (myIntent != null) {
                uri = myIntent.getData();
                if (uri != null) {
                    try {
                        perm = checkUriPermission(uri, Process.myPid(), Process.myUid(), 3);
                    } catch (NullPointerException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (perm != -1) {
                toCrop = uri;
            } else if (ContactPhotoUtils.savePhotoFromUriToUri(this, uri, this.mTempPhotoUri, false)) {
                toCrop = this.mTempPhotoUri;
            } else {
                finish();
                return;
            }
            Intent intent = new Intent("com.android.camera.action.CROP", toCrop);
            if (!(myIntent == null || myIntent.getStringExtra("mimeType") == null)) {
                intent.setDataAndType(toCrop, myIntent.getStringExtra("mimeType"));
            }
            ContactPhotoUtils.addPhotoPickerExtras(intent, this.mCroppedPhotoUri);
            ContactPhotoUtils.addCropExtras(intent, this.mPhotoDim);
            if (hasIntentHandler(intent)) {
                try {
                    startActivityForResult(intent, 2);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.quickcontact_missing_app_Toast, 0).show();
                }
                this.mContactUri = result.getData();
            } else {
                this.mCroppedPhotoUri = this.mTempPhotoUri;
                this.mContactUri = result.getData();
                loadContact(this.mContactUri, new Listener() {
                    public void onContactLoaded(Contact contact) {
                        AttachPhotoActivity.this.saveContact(contact);
                    }
                });
            }
        } else if (requestCode == 2) {
            getContentResolver().delete(this.mTempPhotoUri, null, null);
            if (resultCode != -1) {
                finish();
                return;
            }
            loadContact(this.mContactUri, new Listener() {
                public void onContactLoaded(Contact contact) {
                    AttachPhotoActivity.this.saveContact(contact);
                }
            });
        }
    }

    private boolean hasIntentHandler(Intent intent) {
        List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(intent, 65536);
        if (resolveInfo == null || resolveInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    private void loadContact(Uri contactUri, final Listener listener) {
        ContactLoader loader = new ContactLoader(this, contactUri, true);
        loader.registerListener(0, new OnLoadCompleteListener<Contact>() {
            public void onLoadComplete(Loader<Contact> loader, Contact contact) {
                try {
                    loader.reset();
                } catch (RuntimeException e) {
                    HwLog.e(AttachPhotoActivity.TAG, "Error resetting loader", e);
                }
                listener.onContactLoaded(contact);
            }
        });
        loader.startLoading();
    }

    private void saveContact(Contact contact) {
        RawContactDeltaList deltaList = contact.createRawContactDeltaList();
        if (deltaList == null) {
            HwLog.w(TAG, "no writable raw-contact found");
            finish();
            return;
        }
        RawContactDelta raw = deltaList.getFirstWritableRawContact(this);
        if (raw == null) {
            HwLog.w(TAG, "no writable raw-contact found");
            return;
        }
        int size = ContactsUtils.getThumbnailSize(this);
        try {
            byte[] compressed = ContactPhotoUtils.compressBitmap(Bitmap.createScaledBitmap(ContactPhotoUtils.getBitmapFromUri(this, this.mCroppedPhotoUri), size, size, false));
            if (compressed == null) {
                HwLog.w(TAG, "could not create scaled and compressed Bitmap");
                return;
            }
            ValuesDelta values = RawContactModifier.ensureKindExists(raw, raw.getRawContactAccountType(this), "vnd.android.cursor.item/photo");
            if (values == null) {
                HwLog.w(TAG, "cannot attach photo to this account type");
                return;
            }
            values.setPhoto(compressed);
            HwLog.v(TAG, "all prerequisites met, about to save photo to contact");
            startService(ContactSaveService.createSaveContactIntent((Context) this, deltaList, "", 0, contact.isUserProfile(), null, null, raw.getRawContactId().longValue(), this.mCroppedPhotoUri));
            finish();
        } catch (FileNotFoundException e) {
            HwLog.e(TAG, "Could not find bitmap");
        }
    }
}
