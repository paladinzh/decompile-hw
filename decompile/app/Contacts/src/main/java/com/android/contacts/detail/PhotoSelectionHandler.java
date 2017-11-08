package com.android.contacts.detail;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.DisplayPhoto;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.contacts.editor.PhotoActionPopup;
import com.android.contacts.editor.PhotoActionPopup.Listener;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.UiClosables;
import com.google.android.gms.R;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class PhotoSelectionHandler implements OnClickListener {
    private static final String TAG = PhotoSelectionHandler.class.getSimpleName();
    protected final Context mContext;
    private Uri mCroppedPhotoUri;
    private AlertDialog mDialog;
    private final boolean mIsDirectoryContact;
    private final int mPhotoMode;
    private final int mPhotoPickSize = getPhotoPickSize();
    private final View mPhotoView;
    private SharedPreferences mPrefs;
    private final RawContactDeltaList mState;
    private Uri mTempPhotoUri;

    public abstract class PhotoActionListener implements Listener {
        public abstract Uri getCurrentPhotoUri();

        public abstract void onPhotoSelected(Uri uri) throws FileNotFoundException;

        public abstract void onPhotoSelectionDismissed();

        public void onUseAsPrimaryChosen() {
        }

        public void onRemovePictureChosen() {
            ExceptionCapture.reportScene(48);
            final ArrayList<Long> ids = PhotoSelectionHandler.getRawContactIdsContainPhoto(PhotoSelectionHandler.this.mContext, PhotoSelectionHandler.this.mState);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    for (Long id : ids) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("mimetype_id = (SELECT _id FROM mimetypes WHERE mimetype = 'vnd.android.cursor.item/photo') and raw_contact_id =");
                        sb.append(id);
                        if (ContactsContract.isProfileId(id.longValue())) {
                            PhotoSelectionHandler.this.mContext.getContentResolver().delete(Uri.parse("content://com.android.contacts/profile/data"), sb.toString(), null);
                        } else {
                            PhotoSelectionHandler.this.mContext.getContentResolver().delete(Uri.parse("content://com.android.contacts/data"), sb.toString(), null);
                        }
                    }
                }
            });
            thread.setName("delete contact head picture Thread");
            thread.start();
        }

        public void onTakePhotoChosen() {
            try {
                ExceptionCapture.reportScene(45);
                PhotoSelectionHandler.this.mTempPhotoUri = ContactPhotoUtils.generateTempImageUri(PhotoSelectionHandler.this.mContext);
                PhotoSelectionHandler.this.startTakePhotoActivity(PhotoSelectionHandler.this.mTempPhotoUri);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(PhotoSelectionHandler.this.mContext, R.string.photoPickerNotFoundText_Toast, 1).show();
            }
        }

        public void onPickFromGalleryChosen() {
            try {
                ExceptionCapture.reportScene(47);
                PhotoSelectionHandler.this.mTempPhotoUri = ContactPhotoUtils.generateTempImageUri(PhotoSelectionHandler.this.mContext);
                PhotoSelectionHandler.this.startPickFromGalleryActivity(PhotoSelectionHandler.this.mTempPhotoUri);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(PhotoSelectionHandler.this.mContext, R.string.photoPickerNotFoundText_Toast, 1).show();
            }
        }
    }

    public abstract PhotoActionListener getListener();

    protected abstract void startPhotoActivity(Intent intent, int i, Uri uri);

    public PhotoSelectionHandler(Context context, View photoView, int photoMode, boolean isDirectoryContact, RawContactDeltaList state) {
        this.mContext = context;
        this.mPhotoView = photoView;
        this.mPhotoMode = photoMode;
        this.mPrefs = SharePreferenceUtil.getDefaultSp_de(context);
        this.mIsDirectoryContact = isDirectoryContact;
        this.mState = state;
    }

    public void destroy() {
        UiClosables.closeQuietly(this.mDialog);
    }

    public void onClick(View v) {
        final PhotoActionListener listener = getListener();
        if (listener != null && getWritableEntityIndex() != -1) {
            if (this.mDialog == null) {
                this.mDialog = PhotoActionPopup.createAlertDialog(this.mContext, this.mPhotoView, listener, this.mPhotoMode);
                this.mDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        listener.onPhotoSelectionDismissed();
                    }
                });
            }
            this.mDialog.show();
        }
    }

    public boolean handlePhotoActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoActionListener listener = getListener();
        if (resultCode == -1) {
            Uri uri;
            switch (requestCode) {
                case AMapException.CODE_AMAP_ID_NOT_EXIST /*2001*/:
                case AMapException.CODE_AMAP_SERVICE_MAINTENANCE /*2002*/:
                    Uri toCrop;
                    boolean isWritable = false;
                    if (data == null || data.getData() == null) {
                        uri = listener.getCurrentPhotoUri();
                        isWritable = true;
                    } else {
                        uri = data.getData();
                    }
                    if (isWritable) {
                        toCrop = uri;
                    } else {
                        if (this.mTempPhotoUri == null) {
                            this.mTempPhotoUri = ContactPhotoUtils.generateTempImageUri(this.mContext);
                        }
                        toCrop = this.mTempPhotoUri;
                        try {
                            if (!ContactPhotoUtils.savePhotoFromUriToUri(this.mContext, uri, toCrop, false)) {
                                return false;
                            }
                        } catch (SecurityException e) {
                            HwLog.e(TAG, "Did not have read-access to uri.");
                            return false;
                        }
                    }
                    this.mCroppedPhotoUri = ContactPhotoUtils.generateTempCroppedImageUri(this.mContext);
                    doCropPhoto(toCrop, this.mCroppedPhotoUri);
                    this.mPrefs.edit().putString("CroppedPhotoUri", String.valueOf(this.mCroppedPhotoUri)).apply();
                    return true;
                case AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST /*2003*/:
                    try {
                        listener.onPhotoSelected(listener.getCurrentPhotoUri());
                        return true;
                    } catch (FileNotFoundException e2) {
                        HwLog.e(TAG, "Did not have read-access to uri.");
                        return false;
                    }
                case 2004:
                    if (data == null || data.getData() == null) {
                        String croppedPhotoUri = this.mPrefs.getString("CroppedPhotoUri", "null");
                        if (!"null".equals(croppedPhotoUri)) {
                            this.mCroppedPhotoUri = Uri.parse(croppedPhotoUri);
                            this.mPrefs.edit().putString("CroppedPhotoUri", "null").apply();
                        }
                        uri = this.mCroppedPhotoUri;
                    } else {
                        uri = data.getData();
                    }
                    try {
                        if (this.mTempPhotoUri != null) {
                            this.mContext.getContentResolver().delete(this.mTempPhotoUri, null, null);
                        }
                        listener.onPhotoSelected(uri);
                        return true;
                    } catch (FileNotFoundException e3) {
                        return false;
                    }
            }
        }
        return false;
    }

    private int getWritableEntityIndex() {
        if (this.mIsDirectoryContact) {
            return -1;
        }
        return this.mState.indexOfFirstWritableRawContact(this.mContext);
    }

    private static ArrayList<Long> getRawContactIdsContainPhoto(Context context, RawContactDeltaList dataList) {
        int entityIndex = 0;
        ArrayList<Long> ids = new ArrayList();
        if (!(dataList == null || context == null)) {
            for (RawContactDelta delta : dataList) {
                if (delta.getRawContactAccountType(context.getApplicationContext()).areContactsWritable() && delta.hasMimeEntries("vnd.android.cursor.item/photo")) {
                    ids.add(((RawContactDelta) dataList.get(entityIndex)).getValues().getId());
                }
                entityIndex++;
            }
        }
        return ids;
    }

    public RawContactDeltaList getDeltaForAttachingPhotoToContact() {
        int writableEntityIndex = getWritableEntityIndex();
        if (writableEntityIndex == -1) {
            return null;
        }
        RawContactDelta delta = (RawContactDelta) this.mState.get(writableEntityIndex);
        ContentValues entityValues = delta.getValues().getCompleteValues();
        ValuesDelta child = RawContactModifier.ensureKindExists(delta, AccountTypeManager.getInstance(this.mContext).getAccountType(entityValues.getAsString("account_type"), entityValues.getAsString("data_set")), "vnd.android.cursor.item/photo");
        child.setFromTemplate(false);
        child.setSuperPrimary(true);
        return this.mState;
    }

    private void doCropPhoto(Uri inputUri, Uri outputUri) {
        Intent intent = getCropImageIntent(inputUri, outputUri);
        if (hasIntentHandler(intent)) {
            try {
                startPhotoActivity(intent, 2004, inputUri);
            } catch (Exception e) {
                HwLog.e(TAG, "Cannot crop image : " + e.getMessage());
                Toast.makeText(this.mContext, R.string.photoPickerNotFoundText_Toast, 1).show();
            }
            return;
        }
        try {
            getListener().onPhotoSelected(inputUri);
        } catch (FileNotFoundException e2) {
            HwLog.e(TAG, "Cannot save uncropped photo : " + e2.getMessage());
            Toast.makeText(this.mContext, R.string.contactPhotoSavedErrorToast, 1).show();
        }
    }

    private void startTakePhotoActivity(Uri photoUri) {
        startPhotoActivity(getTakePhotoIntent(photoUri), AMapException.CODE_AMAP_ID_NOT_EXIST, photoUri);
    }

    private void startPickFromGalleryActivity(Uri photoUri) {
        startPhotoActivity(getPhotoPickIntent(photoUri), AMapException.CODE_AMAP_SERVICE_MAINTENANCE, photoUri);
    }

    private int getPhotoPickSize() {
        Cursor c = this.mContext.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"display_max_dim"}, null, null, null);
        if (c == null) {
            return 0;
        }
        try {
            if (!c.moveToFirst() || c.isNull(0)) {
                HwLog.e(TAG, "Unable to get the picture size, return default size.");
                return 480;
            }
            int i = c.getInt(0);
            c.close();
            return i;
        } finally {
            c.close();
        }
    }

    private Intent getPhotoPickIntent(Uri outputUri) {
        Intent intent = new Intent("android.intent.action.PICK", null);
        intent.setType("image/*");
        ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri);
        return intent;
    }

    private boolean hasIntentHandler(Intent intent) {
        List<ResolveInfo> resolveInfo = this.mContext.getPackageManager().queryIntentActivities(intent, 65536);
        if (resolveInfo == null || resolveInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    private Intent getCropImageIntent(Uri inputUri, Uri outputUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(inputUri, "image/*");
        ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri);
        ContactPhotoUtils.addCropExtras(intent, this.mPhotoPickSize);
        return intent;
    }

    private Intent getTakePhotoIntent(Uri outputUri) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE", null);
        ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri);
        return intent;
    }

    public void initPopup() {
        final PhotoActionListener listener = getListener();
        if (listener != null && getWritableEntityIndex() != -1) {
            this.mDialog = PhotoActionPopup.createAlertDialog(this.mContext, this.mPhotoView, listener, this.mPhotoMode);
            this.mDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    listener.onPhotoSelectionDismissed();
                }
            });
            this.mDialog.show();
        }
    }
}
