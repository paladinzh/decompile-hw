package com.huawei.mms.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.text.TextUtils;
import android.util.LruCache;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.android.mms.util.ItemLoadedCallback;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressLint({"NewApi"})
public class AvatarCache {
    private static volatile TypedArray sColors = null;
    private static volatile int sDefaultColor = 0;
    private static byte[] sEmptyAvatar = new byte[0];
    private static AvatarCache self = new AvatarCache(false, false);
    private LruCache<Long, ContactDrawable> mLruCache;
    private Set<Long> mValidSets;

    private static class ContactDrawable {
        private byte[] mOrgData;
        private Drawable mRoundDrawable;
        private Drawable mSquareDrawable;

        private ContactDrawable() {
            this.mOrgData = null;
            this.mSquareDrawable = null;
            this.mRoundDrawable = null;
        }
    }

    public static AvatarCache instance() {
        return self;
    }

    public static AvatarCache newCache(boolean fullCache) {
        return new AvatarCache(fullCache);
    }

    private AvatarCache(boolean fullCache) {
        this(fullCache, true);
    }

    private AvatarCache(boolean fullCache, boolean square) {
        this.mLruCache = null;
        this.mValidSets = new HashSet();
        this.mLruCache = new LruCache(20);
    }

    public void clearCache() {
        synchronized (this.mLruCache) {
            this.mValidSets.clear();
        }
        MLog.d("AvatarCache", "AvatarCache's Data is cleared");
    }

    private byte[] loadAvatarData(Context context, boolean isMe, long personId) {
        byte[] bArr = null;
        if (!isMe && personId <= 0) {
            return sEmptyAvatar;
        }
        InputStream inputStream = null;
        try {
            inputStream = Contacts.openContactPhotoInputStream(context.getContentResolver(), isMe ? Profile.CONTENT_URI : ContentUris.withAppendedId(Contacts.CONTENT_URI, personId));
            if (inputStream != null) {
                bArr = new byte[inputStream.available()];
                MLog.i("AvatarCache", "Avtar data len = " + inputStream.read(bArr, 0, bArr.length));
            } else {
                bArr = sEmptyAvatar;
                MLog.i("AvatarCache", "Avtar Stream is Empty");
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException ex) {
            MLog.e("AvatarCache", "loadAvatarData fail. IOException " + ex.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception ex2) {
            MLog.e("AvatarCache", "loadAvatarData fail. Exception " + ex2.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
        }
        return bArr;
    }

    public Drawable loadAvatar(Context context, boolean isMe, long personId, Drawable defaultValue, Contact contact) {
        return loadAvatar(context, isMe, personId, defaultValue, true, contact);
    }

    @SuppressLint({"NewApi"})
    public Drawable loadAvatar(Context context, boolean isMe, long personId, Drawable defaultValue, boolean square, Contact contact) {
        if (!isMe && personId <= 0) {
            return defaultValue;
        }
        long id;
        if (isMe) {
            id = 0;
        } else {
            id = personId;
        }
        return loadAvatarCommon(context, isMe, false, id, null, defaultValue, square, contact);
    }

    public static void preLoadData(Context context, boolean isMe, long personId, Contact contact) {
        final Context context2 = context;
        final boolean z = isMe;
        final long j = personId;
        final Contact contact2 = contact;
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                AvatarCache.self.loadAvatar(context2, z, j, ResEx.self().getAvtarDefault(contact2), false, contact2);
            }
        });
    }

    @SuppressLint({"NewApi"})
    private void asyncSetAvatar(Context context, boolean isMe, long personId, boolean square, Drawable defaultValue, ItemLoadedCallback<Drawable> r, Contact contact) {
        final long j = personId;
        final Context context2 = context;
        final boolean z = isMe;
        final Drawable drawable = defaultValue;
        final boolean z2 = square;
        final Contact contact2 = contact;
        final ItemLoadedCallback<Drawable> itemLoadedCallback = r;
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                MLog.d("AvatarCache", "Load Avatar data " + j);
                final Drawable avatar = AvatarCache.this.loadAvatar(context2, z, j, drawable, z2, contact2);
                Handler uIHandler = HwBackgroundLoader.getUIHandler();
                final ItemLoadedCallback itemLoadedCallback = itemLoadedCallback;
                uIHandler.post(new Runnable() {
                    public void run() {
                        itemLoadedCallback.onItemLoaded(avatar, null);
                    }
                });
            }
        });
    }

    public boolean setAvatar(Context context, boolean isMe, long personId, boolean square, Drawable defaultValue, ItemLoadedCallback<Drawable> onLoaded, Contact contact) {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("AvatarCache", "setAvatar isMe = " + isMe + "  personId = " + personId);
        }
        if (isMe || personId > 0) {
            long id;
            if (isMe) {
                id = 0;
            } else {
                id = personId;
            }
            if (setAvatarCommon(context, id, square, defaultValue, onLoaded)) {
                return false;
            }
            asyncSetAvatar(context, isMe, personId, square, defaultValue, onLoaded, contact);
            return false;
        }
        onLoaded.onItemLoaded(defaultValue, null);
        return false;
    }

    public boolean setAvatar(Context context, long ypContactId, String photoUri, boolean square, Drawable defaultValue, ItemLoadedCallback<Drawable> onLoaded, Contact contact) {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("AvatarCache", "setAvatar ypContactId = " + ypContactId + "  photoUri = " + photoUri);
        }
        if (context == null || ypContactId <= 0 || TextUtils.isEmpty(photoUri)) {
            onLoaded.onItemLoaded(defaultValue, null);
            return false;
        }
        if (setAvatarCommon(context, ypContactId * -1, square, defaultValue, onLoaded)) {
            return false;
        }
        asyncSetAvatarYp(context, ypContactId, photoUri, square, defaultValue, onLoaded, contact);
        return false;
    }

    public static void preLoadData(Context context, long ypContactId, String photoUri, Contact contact) {
        final Context context2 = context;
        final long j = ypContactId;
        final String str = photoUri;
        final Contact contact2 = contact;
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                AvatarCache.self.loadAvatar(context2, j, str, ResEx.self().getAvtarDefault(contact2), false, contact2);
            }
        });
    }

    public Drawable loadAvatar(Context context, long ypContactId, String photoUri, Drawable defaultValue, Contact contact) {
        return loadAvatar(context, ypContactId, photoUri, defaultValue, true, contact);
    }

    @SuppressLint({"NewApi"})
    public Drawable loadAvatar(Context context, long ypContactId, String photoUri, Drawable defaultValue, boolean square, Contact contact) {
        if (context == null || ypContactId <= 0 || TextUtils.isEmpty(photoUri)) {
            return defaultValue;
        }
        return loadAvatarCommon(context, false, true, ypContactId * -1, photoUri, defaultValue, square, contact);
    }

    @SuppressLint({"NewApi"})
    private void asyncSetAvatarYp(Context context, long ypContactId, String photoUri, boolean square, Drawable defaultValue, ItemLoadedCallback<Drawable> r, Contact contact) {
        final long j = ypContactId;
        final Context context2 = context;
        final String str = photoUri;
        final Drawable drawable = defaultValue;
        final boolean z = square;
        final Contact contact2 = contact;
        final ItemLoadedCallback<Drawable> itemLoadedCallback = r;
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                MLog.d("AvatarCache", "Load Avatar data " + j);
                final Drawable avatar = AvatarCache.this.loadAvatar(context2, j, str, drawable, z, contact2);
                Handler uIHandler = HwBackgroundLoader.getUIHandler();
                final ItemLoadedCallback itemLoadedCallback = itemLoadedCallback;
                uIHandler.post(new Runnable() {
                    public void run() {
                        itemLoadedCallback.onItemLoaded(avatar, null);
                    }
                });
            }
        });
    }

    private byte[] loadAvatarDataFromYp(Context context, long ypContactId, String photoUri) {
        byte[] bArr = null;
        if (TextUtils.isEmpty(photoUri)) {
            return sEmptyAvatar;
        }
        InputStream inputStream = null;
        try {
            AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(photoUri), "r");
            if (fileDescriptor == null) {
                return sEmptyAvatar;
            }
            inputStream = fileDescriptor.createInputStream();
            if (inputStream != null) {
                bArr = new byte[inputStream.available()];
                MLog.i("AvatarCache", "Avtar data len = " + inputStream.read(bArr, 0, bArr.length));
            } else {
                bArr = sEmptyAvatar;
                MLog.i("AvatarCache", "Avtar Stream is Empty");
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            return bArr;
        } catch (IOException ex) {
            MLog.e("AvatarCache", "loadAvatarData fail. IOException " + ex.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            MLog.e("AvatarCache", "loadAvatarData fail. Exception " + e3);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    private boolean setAvatarCommon(Context context, long ContactId, boolean square, Drawable defaultValue, ItemLoadedCallback<Drawable> onLoaded) {
        long id = ContactId;
        Drawable drawable = null;
        boolean hasGotAvatar = false;
        synchronized (this.mLruCache) {
            ContactDrawable cdraw = (ContactDrawable) this.mLruCache.get(Long.valueOf(ContactId));
            MLog.d("AvatarCache", "setAvatarCommon avatar is not cached " + ContactId);
            if (cdraw != null) {
                drawable = square ? cdraw.mSquareDrawable : cdraw.mRoundDrawable;
                hasGotAvatar = this.mValidSets.contains(Long.valueOf(ContactId));
                if (drawable == null && !(cdraw.mSquareDrawable == null && cdraw.mRoundDrawable == null)) {
                    hasGotAvatar = false;
                }
            }
        }
        if (drawable != null) {
            defaultValue = drawable;
        }
        onLoaded.onItemLoaded(defaultValue, null);
        if (!hasGotAvatar) {
            return false;
        }
        MLog.d("AvatarCache", "Not need update this avtar");
        return true;
    }

    private Drawable loadAvatarCommon(Context context, boolean isMe, boolean isYp, long ContactId, String photoUri, Drawable defaultValue, boolean square, Contact contact) {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("AvatarCache", "loadAvatarCommon isMe = " + isMe + "  isYp = " + isYp + "  ContactId = " + ContactId);
        }
        long id = ContactId;
        byte[] avatarData = null;
        synchronized (this.mLruCache) {
            Object cdraw = (ContactDrawable) this.mLruCache.get(Long.valueOf(ContactId));
            if (cdraw == null) {
                cdraw = new ContactDrawable();
            } else {
                if (cdraw.mOrgData != null) {
                    if (this.mValidSets.contains(Long.valueOf(ContactId))) {
                        MLog.d("AvatarCache", "avatar data is valid. use existing : " + ContactId);
                        avatarData = cdraw.mOrgData;
                    }
                }
                MLog.d("AvatarCache", "avatar data is invalid");
            }
        }
        if (avatarData == null) {
            if (isYp) {
                avatarData = loadAvatarDataFromYp(context, ContactId, photoUri);
            } else {
                avatarData = loadAvatarData(context, isMe, ContactId);
            }
            if (avatarData == null) {
                avatarData = sEmptyAvatar;
                MLog.d("AvatarCache", "can't get avatar data set to empty ");
            }
        }
        if (!Arrays.equals(avatarData, cdraw.mOrgData)) {
            cdraw.mOrgData = avatarData;
        } else if (square && cdraw.mSquareDrawable != null) {
            return cdraw.mSquareDrawable;
        } else {
            if (!(square || cdraw.mRoundDrawable == null)) {
                return cdraw.mRoundDrawable;
            }
        }
        Drawable drawable = null;
        if (avatarData == null || avatarData == sEmptyAvatar) {
            cdraw = null;
            MLog.d("AvatarCache", "avatar data is empty or is null");
        } else {
            try {
                MLog.d("AvatarCache", "avatar data len=" + avatarData.length);
                Bitmap orgBmp = BitmapFactory.decodeByteArray(avatarData, 0, avatarData.length);
                if (orgBmp != null) {
                    if (square) {
                        cdraw.mSquareDrawable = new BitmapDrawable(context.getResources(), ResEx.getRoundedCornerBitmap(orgBmp, 6.0f));
                        drawable = cdraw.mSquareDrawable;
                    } else {
                        cdraw.mRoundDrawable = createRoundPhotoDrawable(context, orgBmp, pickColor(contact));
                        drawable = cdraw.mRoundDrawable;
                    }
                    orgBmp.recycle();
                } else {
                    MLog.e("AvatarCache", "can't decode bitmap from data:" + Arrays.toString(avatarData));
                }
            } catch (Exception ex) {
                cdraw = null;
                MLog.e("AvatarCache", "decodeByteArray has exception : " + ex);
            } catch (Error e) {
                cdraw = null;
                MLog.e("AvatarCache", "decodeByteArray has error : " + e);
            }
        }
        synchronized (this.mLruCache) {
            this.mValidSets.add(Long.valueOf(ContactId));
            if (cdraw != null) {
                this.mLruCache.put(Long.valueOf(ContactId), cdraw);
            } else {
                this.mLruCache.remove(Long.valueOf(ContactId));
            }
            MLog.d("AvatarCache", "cache avatar for: " + ContactId);
        }
        if (drawable == null) {
            drawable = defaultValue;
        }
        return drawable;
    }

    public static int pickColor(Contact contact) {
        return pickColor(contact, 0);
    }

    public static int pickColor(Contact contact, int contactImgFlag) {
        Resources res = MmsApp.getApplication().getResources();
        if (sDefaultColor == 0) {
            sDefaultColor = res.getColor(R.color.letter_tile_default_color);
        }
        if (sColors == null) {
            sColors = res.obtainTypedArray(R.array.letter_tile_colors);
        }
        switch (contactImgFlag) {
            case -4:
            case -2:
                return res.getColor(R.color.notice_avatar_default_bg_color);
            case -3:
                return res.getColor(R.color.favorites_image_background_color);
            case -1:
                return getMyBgColor();
            default:
                return getColorByHashCode(contact);
        }
    }

    private static int getMyBgColor() {
        if (sColors == null || sColors.length() <= 0) {
            return sDefaultColor;
        }
        return sColors.getColor(0, sDefaultColor);
    }

    private static int getColorByHashCode(Contact contact) {
        String identifier;
        long id = 0;
        if (contact != null && contact.isYpContact()) {
            id = contact.getYpContactId();
        } else if (contact != null) {
            id = contact.getPersonId();
        }
        if (id != 0 || contact == null) {
            identifier = String.valueOf(id);
        } else {
            identifier = contact.getNumber();
        }
        if (sColors == null || sColors.length() <= 0) {
            return sDefaultColor;
        }
        return sColors.getColor(Math.abs(identifier.hashCode() & 4095) % sColors.length(), sDefaultColor);
    }

    public static Drawable createRoundPhotoDrawable(Context context, Bitmap bitmap, int color) {
        if (bitmap == null) {
            return null;
        }
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Rect rect = new Rect(0, 0, size, size);
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), output);
        bitmapDrawable.setBounds(0, 0, size, size);
        return bitmapDrawable;
    }

    public static Drawable createRoundPhotoDrawable(Context context, Drawable drawable, int color) {
        if (drawable == null) {
            return null;
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        int size = context.getResources().getDimensionPixelSize(R.dimen.avatar_view_width_height_conversation_item);
        if (width > size) {
            width = size;
        }
        if (height > size) {
            height = size;
        }
        Rect resRect = new Rect(0, 0, width, height);
        Rect tarRect = new Rect((size - width) / 2, (size - height) / 2, (size + width) / 2, (size + height) / 2);
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setAlpha(255);
        canvas.drawBitmap(drawableToBitmap(drawable), resRect, tarRect, paint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), output);
        bitmapDrawable.setBounds(resRect);
        return bitmapDrawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Config config;
        int height = drawable.getIntrinsicHeight();
        int width = drawable.getIntrinsicWidth();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
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
}
