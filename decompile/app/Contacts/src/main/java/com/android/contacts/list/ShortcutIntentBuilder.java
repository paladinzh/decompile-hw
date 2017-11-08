package com.android.contacts.list;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import com.android.contacts.ChooseSubActivity;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;

public class ShortcutIntentBuilder {
    private static final String[] CONTACT_COLUMNS = new String[]{"display_name", "photo_id", "_id"};
    private static final String[] PHONE_COLUMNS = new String[]{"display_name", "photo_id", "data1", "data2", "data3", "contact_id", "data4"};
    private static final String[] PHOTO_COLUMNS = new String[]{"data15"};
    private final Context mContext;
    private final int mIconDensity;
    private int mIconSize;
    private final OnShortcutIntentCreatedListener mListener;

    public interface OnShortcutIntentCreatedListener {
        void onShortcutIntentCreated(Uri uri, Intent intent);
    }

    private abstract class LoadingAsyncTask extends AsyncTask<Void, Void, Void> {
        protected byte[] mBitmapData;
        protected long mContactId;
        protected String mContentType;
        protected String mDisplayName;
        protected long mPhotoId;
        protected Uri mUri;

        protected abstract void loadData();

        public LoadingAsyncTask(Uri uri) {
            this.mUri = uri;
        }

        protected Void doInBackground(Void... params) {
            if (this.mUri == null) {
                return null;
            }
            this.mContentType = ShortcutIntentBuilder.this.mContext.getContentResolver().getType(this.mUri);
            loadData();
            loadPhoto();
            return null;
        }

        private void loadPhoto() {
            if (this.mPhotoId != 0) {
                Cursor cursor = ShortcutIntentBuilder.this.mContext.getContentResolver().query(Data.CONTENT_URI, ShortcutIntentBuilder.PHOTO_COLUMNS, "_id=?", new String[]{String.valueOf(this.mPhotoId)}, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            this.mBitmapData = cursor.getBlob(0);
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
        }
    }

    private final class ContactLoadingAsyncTask extends LoadingAsyncTask {
        public ContactLoadingAsyncTask(Uri uri) {
            super(uri);
        }

        protected void loadData() {
            Cursor cursor = ShortcutIntentBuilder.this.mContext.getContentResolver().query(this.mUri, ShortcutIntentBuilder.CONTACT_COLUMNS, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        this.mDisplayName = cursor.getString(0);
                        this.mPhotoId = cursor.getLong(1);
                        this.mContactId = cursor.getLong(2);
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                }
            }
        }

        protected void onPostExecute(Void result) {
            ShortcutIntentBuilder.this.createContactShortcutIntent(this.mUri, this.mContentType, this.mContactId, this.mDisplayName, this.mBitmapData, this.mPhotoId);
        }
    }

    private final class ContactLoadingNumbercutAsyncTask extends LoadingAsyncTask {
        private String mPhoneNumber;
        private final String mShortcutAction;

        public ContactLoadingNumbercutAsyncTask(Uri uri, String shortcutAction, String phoneNumber) {
            super(uri);
            this.mShortcutAction = shortcutAction;
            this.mPhoneNumber = phoneNumber;
        }

        protected void loadData() {
            Cursor cursor = ShortcutIntentBuilder.this.mContext.getContentResolver().query(this.mUri, ShortcutIntentBuilder.CONTACT_COLUMNS, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        this.mDisplayName = cursor.getString(0);
                        this.mPhotoId = cursor.getLong(1);
                        this.mContactId = cursor.getLong(2);
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                }
            }
        }

        protected void onPostExecute(Void result) {
            ShortcutIntentBuilder.this.createPhoneNumberShortcutIntent(this.mUri, this.mDisplayName, this.mContactId, this.mBitmapData, this.mPhoneNumber, -1, null, this.mShortcutAction, this.mPhotoId);
        }
    }

    private final class PhoneNumberLoadingAsyncTask extends LoadingAsyncTask {
        private long mContactId;
        private String mNormalizedNumber;
        private String mPhoneLabel;
        private String mPhoneNumber;
        private int mPhoneType;
        private final String mShortcutAction;

        public PhoneNumberLoadingAsyncTask(Uri uri, String shortcutAction) {
            super(uri);
            this.mShortcutAction = shortcutAction;
        }

        protected void loadData() {
            Cursor cursor = ShortcutIntentBuilder.this.mContext.getContentResolver().query(this.mUri, ShortcutIntentBuilder.PHONE_COLUMNS, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        this.mDisplayName = cursor.getString(0);
                        this.mPhotoId = cursor.getLong(1);
                        this.mPhoneNumber = PhoneNumberFormatter.parsePhoneNumber(cursor.getString(2));
                        this.mPhoneType = cursor.getInt(3);
                        this.mPhoneLabel = cursor.getString(4);
                        this.mContactId = cursor.getLong(5);
                        this.mNormalizedNumber = cursor.getString(6);
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                }
            }
        }

        protected void onPostExecute(Void result) {
            ShortcutIntentBuilder.this.createPhoneNumberShortcutIntent(this.mUri, this.mDisplayName, this.mContactId, this.mBitmapData, this.mPhoneNumber, this.mPhoneType, this.mPhoneLabel, this.mShortcutAction, this.mPhotoId, this.mNormalizedNumber);
        }
    }

    public ShortcutIntentBuilder(Context context, OnShortcutIntentCreatedListener listener) {
        this.mContext = context;
        this.mListener = listener;
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        this.mIconSize = context.getResources().getDimensionPixelSize(R.dimen.shortcut_icon_size);
        if (this.mIconSize == 0) {
            this.mIconSize = am.getLauncherLargeIconSize();
        }
        this.mIconDensity = am.getLauncherLargeIconDensity();
    }

    public void createContactShortcutIntent(Uri contactUri) {
        new ContactLoadingAsyncTask(contactUri).execute(new Void[0]);
    }

    public void createPhoneNumberShortcutIntent(Uri dataUri, String shortcutAction) {
        new PhoneNumberLoadingAsyncTask(dataUri, shortcutAction).execute(new Void[0]);
    }

    public void createContactPhoneNumberShortcutIntent(Uri dataUri, String shortcutAction, String number) {
        new ContactLoadingNumbercutAsyncTask(dataUri, shortcutAction, number).execute(new Void[0]);
    }

    private Bitmap getPhotoBitmap(byte[] bitmapData) {
        return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, null);
    }

    private void createContactShortcutIntent(Uri contactUri, String contentType, long contactId, String displayName, byte[] bitmapData, long aPhotoId) {
        Bitmap bitmap;
        if (aPhotoId > 0 && bitmapData != null) {
            bitmap = getPhotoBitmap(bitmapData);
        } else if (EmuiFeatureManager.isSupportMultiColorPhoto()) {
            bitmap = generateQuickContactIcon(ContactPhotoManager.getDefaultAvatarDrawableForContact(this.mContext.getResources(), false, new DefaultImageRequest(displayName, String.valueOf(contactId), false)));
        } else {
            bitmap = ((BitmapDrawable) this.mContext.getResources().getDrawableForDensity(R.drawable.contact_shortcut_default_avatar_dark, this.mIconDensity)).getBitmap();
        }
        Intent shortcutIntent = new Intent("android.provider.action.QUICK_CONTACT");
        shortcutIntent.setPackage("com.android.contacts");
        shortcutIntent.addFlags(276856832);
        shortcutIntent.putExtra("com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION", true);
        shortcutIntent.setDataAndType(contactUri, contentType);
        shortcutIntent.putExtra("android.provider.extra.MODE", 3);
        shortcutIntent.putExtra("android.provider.extra.EXCLUDE_MIMES", (String[]) null);
        shortcutIntent.putExtra("com.android.launcher.intent.extra.shortcut.CHECK_CONTACT_EXIST", true);
        shortcutIntent.putExtra("fromWhere", "fromLauncher");
        shortcutIntent.setComponent(new ComponentName(this.mContext.getPackageName(), QuickContactActivity.class.getName()));
        Bitmap icon = bitmap;
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.shortcut.ICON", icon);
        if (TextUtils.isEmpty(displayName)) {
            intent.putExtra("android.intent.extra.shortcut.NAME", this.mContext.getResources().getString(R.string.missing_name));
        } else {
            intent.putExtra("android.intent.extra.shortcut.NAME", displayName);
        }
        shortcutIntent.putExtra("EXTRA_SHORTCUT_CONTENT_DESCRIPTION", this.mContext.getString(R.string.content_description_shortcut_view_contact, new Object[]{intent.getStringExtra("android.intent.extra.shortcut.NAME")}));
        intent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
        this.mListener.onShortcutIntentCreated(contactUri, intent);
    }

    private void createPhoneNumberShortcutIntent(Uri uri, String displayName, long contactId, byte[] bitmapData, String phoneNumber, int phoneType, String phoneLabel, String shortcutAction, long aPhotoId) {
        createPhoneNumberShortcutIntent(uri, displayName, contactId, bitmapData, phoneNumber, phoneType, phoneLabel, shortcutAction, aPhotoId, null);
    }

    private void createPhoneNumberShortcutIntent(Uri uri, String displayName, long contactId, byte[] bitmapData, String phoneNumber, int phoneType, String phoneLabel, String shortcutAction, long aPhotoId, String normalizedNumber) {
        Bitmap bitmap;
        boolean useDefaultAvatar;
        Intent shortcutIntent;
        if (aPhotoId <= 0 || bitmapData == null) {
            if (EmuiFeatureManager.isSupportMultiColorPhoto()) {
                bitmap = generateQuickContactIcon(ContactPhotoManager.getDefaultAvatarDrawableForContact(this.mContext.getResources(), false, new DefaultImageRequest(displayName, String.valueOf(contactId), false)));
            } else {
                bitmap = ((BitmapDrawable) this.mContext.getResources().getDrawableForDensity(R.drawable.contact_shortcut_default_avatar_dark, this.mIconDensity)).getBitmap();
            }
            useDefaultAvatar = true;
        } else {
            bitmap = getPhotoBitmap(bitmapData);
            useDefaultAvatar = false;
        }
        boolean fromWidget = false;
        Uri phoneUri;
        if ("android.intent.action.CALL".equals(shortcutAction) || "android.intent.action.CALL_PRIVILEGED".equals(shortcutAction) || "com.android.contacts.action.CHOOSE_SUB".equals(shortcutAction) || "com.android.contacts.action.CHOOSE_SUB_HUAWEI".equals(shortcutAction)) {
            phoneUri = Uri.fromParts("tel", phoneNumber, null);
            bitmap = generatePhoneNumberIcon(bitmap, phoneType, phoneLabel, R.drawable.badge_action_call, useDefaultAvatar);
            shortcutIntent = new Intent(shortcutAction, phoneUri);
            shortcutIntent.setFlags(67108864);
            shortcutIntent.setComponent(new ComponentName(this.mContext.getPackageName(), ChooseSubActivity.class.getName()));
            if ("com.android.contacts.action.CHOOSE_SUB_HUAWEI".equals(shortcutAction)) {
                shortcutIntent.setComponent(new ComponentName(this.mContext.getPackageName(), "com.android.contacts.HuaweiChooseSubActivity"));
            }
            shortcutIntent.putExtra("EXTRA_SHORTCUT_CONTENT_DESCRIPTION", this.mContext.getString(R.string.content_description_shortcut_dial_number, new Object[]{displayName}));
        } else {
            phoneUri = Uri.fromParts("smsto", phoneNumber, null);
            fromWidget = true;
            bitmap = generatePhoneNumberIcon(bitmap, phoneType, phoneLabel, R.drawable.badge_action_sms, useDefaultAvatar);
            shortcutIntent = new Intent(shortcutAction, phoneUri);
            shortcutIntent.setFlags(67108864);
            shortcutIntent.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity"));
            shortcutIntent.putExtra("EXTRA_SHORTCUT_CONTENT_DESCRIPTION", this.mContext.getString(R.string.content_description_shortcut_send_message, new Object[]{displayName}));
        }
        if (fromWidget) {
            shortcutIntent.putExtra("fromWidget", true);
        }
        IntentProvider.addRoamingDataIntent(shortcutIntent, displayName, normalizedNumber, null, null, 0);
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.shortcut.ICON", bitmap);
        intent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
        intent.putExtra("android.intent.extra.shortcut.NAME", displayName);
        this.mListener.onShortcutIntentCreated(uri, intent);
    }

    private Bitmap generatePhoneNumberIcon(Bitmap photo, int phoneType, String phoneLabel, int actionResId, boolean defaultAvatar) {
        Resources r = this.mContext.getResources();
        Bitmap phoneIcon = ((BitmapDrawable) r.getDrawableForDensity(actionResId, this.mIconDensity)).getBitmap();
        Bitmap icon = Bitmap.createBitmap(this.mIconSize, this.mIconSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);
        Rect src = new Rect(0, 0, photo.getWidth(), photo.getHeight());
        Rect dst = new Rect(0, 0, this.mIconSize, this.mIconSize);
        canvas.drawBitmap(photo, src, dst, photoPaint);
        if (!defaultAvatar) {
            Bitmap mask = BitmapFactory.decodeResource(r, R.drawable.contact_shortcut_avatar_mask);
            canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), dst, photoPaint);
            mask.recycle();
        }
        src.set(0, 0, phoneIcon.getWidth(), phoneIcon.getHeight());
        int iconWidth = icon.getWidth();
        float badgeSize = r.getDimension(R.dimen.contact_shortcut_badge_size);
        float badgeIconMargin = r.getDimension(R.dimen.contact_shortcut_badge_icon_margin);
        dst.set(iconWidth - ((int) badgeSize), (int) badgeIconMargin, iconWidth - ((int) badgeIconMargin), (int) badgeSize);
        canvas.drawBitmap(phoneIcon, src, dst, photoPaint);
        canvas.setBitmap(null);
        return icon;
    }

    private Bitmap generateQuickContactIcon(Drawable photo) {
        Bitmap bitmap = Bitmap.createBitmap(this.mIconSize, this.mIconSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        photo.setBounds(new Rect(0, 0, this.mIconSize, this.mIconSize));
        photo.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }
}
