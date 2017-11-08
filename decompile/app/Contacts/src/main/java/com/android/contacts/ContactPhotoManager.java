package com.android.contacts;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.lettertiles.LetterTileDrawable;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public abstract class ContactPhotoManager implements ComponentCallbacks2 {
    static final boolean DEBUG = HwLog.HWDBG;
    public static final DefaultImageProvider DEFAULT_AVATAR = new AvatarDefaultImageProvider();
    public static final DefaultImageProvider DEFAULT_BLANK = new BlankDefaultImageProvider();
    private static int s180DipInPixel = -1;
    private static volatile TypedArray sColors;
    private static volatile int sDefaultColor;
    private static volatile Drawable sDefaultLetterAvatar = null;
    private static Resources sResources;

    public static abstract class DefaultImageProvider {
        HashMap<String, SoftReference<Bitmap>> mDefaultImageBitmpaCache = new HashMap();

        public abstract void applyDefaultImage(ImageView imageView, int i, boolean z, DefaultImageRequest defaultImageRequest);

        public abstract void applyDefaultImage(ImageView imageView, int i, boolean z, DefaultImageRequest defaultImageRequest, int i2, long j);

        public Bitmap getDefaultImageBitmapForPrivateContact(int aViewWidth, int aViewHeight, int aDefaultResId) {
            return null;
        }
    }

    private static class AvatarDefaultImageProvider extends DefaultImageProvider {
        private HashMap<String, SoftReference<Bitmap>> mRoundedEdgeBitmapCacheForSIM;

        private AvatarDefaultImageProvider() {
            this.mRoundedEdgeBitmapCacheForSIM = new HashMap();
        }

        public void applyDefaultImage(ImageView view, int extent, boolean darkTheme, DefaultImageRequest defaultImageRequest) {
            if (ContactPhotoManager.DEBUG) {
                HwLog.d("ContactPhotoManager", "AvatarDefaultImageProvider applyDefaultImage extent:" + extent + "  darkTheme:" + darkTheme);
            }
            if (view == null) {
                HwLog.w("ContactPhotoManager", "applyDefaultImage view is null");
                return;
            }
            if (defaultImageRequest == null || !EmuiFeatureManager.isSupportMultiColorPhoto()) {
                view.setImageResource(ContactPhotoManager.getDefaultAvatarResId(view.getContext(), extent, darkTheme));
            } else {
                view.setImageDrawable(getDefaultImageForContact(view.getResources(), defaultImageRequest));
            }
        }

        public static Drawable getDefaultImageForContact(Resources resources, DefaultImageRequest defaultImageRequest) {
            LetterTileDrawable drawable = new LetterTileDrawable(resources);
            if (defaultImageRequest != null) {
                if (TextUtils.isEmpty(defaultImageRequest.identifier)) {
                    drawable.setContactDetails(null, defaultImageRequest.displayName);
                } else {
                    drawable.setContactDetails(defaultImageRequest.displayName, defaultImageRequest.identifier);
                }
                drawable.setContactType(defaultImageRequest.contactType);
                drawable.setScale(defaultImageRequest.scale);
                drawable.setOffset(defaultImageRequest.offset);
                drawable.setIsCircular(defaultImageRequest.isCircular);
            }
            return drawable;
        }

        public void applyDefaultImage(ImageView view, int extent, boolean darkTheme, DefaultImageRequest defaultImageRequest, int flags, long aPhotoId) {
            if (ContactPhotoManager.DEBUG) {
                HwLog.d("ContactPhotoManager", "AvatarDefaultImageProvider applyDefaultImage:" + aPhotoId);
            }
            if (aPhotoId >= -3) {
                applyDefaultImage(view, extent, darkTheme, defaultImageRequest);
            } else if (ContactPhotoManager.isPrivacyIndicatorRequired(flags)) {
                int[] lWidhtAndHeight = ContactPhotoManager.getWidthAndHeightForOverlayBitmapFromView(view);
                Bitmap lImageBitmap = getDefaultImageBitmapForPrivateContact(lWidhtAndHeight[0], lWidhtAndHeight[1], R.drawable.ic_contact_picture_holo_light);
                if (lImageBitmap != null) {
                    view.setImageBitmap(lImageBitmap);
                } else {
                    view.setImageResource(R.drawable.ic_contact_picture_holo_light);
                }
            } else {
                view.setImageResource(R.drawable.ic_contact_picture_holo_light);
            }
        }

        public Bitmap getDefaultImageBitmapForPrivateContact(int aViewWidth, int aViewHeight, int aDefaultResId) {
            String lKey;
            if (ContactPhotoManager.DEBUG) {
                HwLog.d("ContactPhotoManager", "AvatarDefaultImageProvider getDefaultImageBitmapForPrivateContact");
            }
            switch (aDefaultResId) {
                case R.drawable.csp_default_avatar:
                    lKey = "default_avatar_blue";
                    break;
                default:
                    lKey = "default_avatar";
                    break;
            }
            Bitmap bitmap = null;
            if (TextUtils.isEmpty(lKey)) {
                return null;
            }
            SoftReference<Bitmap> lRef = (SoftReference) this.mDefaultImageBitmpaCache.get(lKey);
            if (lRef == null || lRef.get() == null) {
                try {
                    bitmap = BitmapFactory.decodeResource(ContactPhotoManager.sResources, aDefaultResId);
                    if (bitmap != null) {
                        this.mDefaultImageBitmpaCache.put(lKey, new SoftReference(bitmap));
                    }
                } catch (OutOfMemoryError e) {
                    HwLog.e("ContactPhotoManager", "OutOfMemoryError " + e);
                    ExceptionCapture.capturePhotoManagerException("ContactPhotoManager->getDefaultImageBitmapForPrivateContact OutOfMemoryError");
                }
            } else {
                bitmap = (Bitmap) lRef.get();
            }
            return bitmap;
        }
    }

    private static class BlankDefaultImageProvider extends DefaultImageProvider {
        private static volatile Drawable sDrawable;

        private BlankDefaultImageProvider() {
        }

        public void applyDefaultImage(ImageView view, int extent, boolean darkTheme, DefaultImageRequest defaultImageRequest) {
            if (sDrawable == null) {
                sDrawable = new ColorDrawable(view.getContext().getResources().getColor(R.color.image_placeholder));
            }
            view.setImageDrawable(sDrawable);
        }

        public void applyDefaultImage(ImageView view, int extent, boolean darkTheme, DefaultImageRequest defaultImageRequest, int flags, long aPhotoId) {
            applyDefaultImage(view, extent, darkTheme, defaultImageRequest);
        }
    }

    public static class DefaultImageRequest {
        public int contactType;
        public String displayName;
        public String identifier;
        public boolean isCircular;
        public float offset;
        public float scale;

        public DefaultImageRequest() {
            this.contactType = 1;
            this.scale = 1.0f;
            this.offset = 0.0f;
            this.isCircular = false;
        }

        public DefaultImageRequest(String displayName, String identifier, boolean isCircular) {
            this(displayName, identifier, 1, 1.0f, 0.0f, isCircular);
        }

        public DefaultImageRequest(String displayName, String identifier, int contactType, boolean isCircular) {
            this(displayName, identifier, contactType, 1.0f, 0.0f, isCircular);
        }

        public DefaultImageRequest(String displayName, String identifier, int contactType, float scale, float offset, boolean isCircular) {
            this.contactType = 1;
            this.scale = 1.0f;
            this.offset = 0.0f;
            this.isCircular = false;
            this.displayName = displayName;
            this.identifier = identifier;
            this.contactType = contactType;
            this.scale = scale;
            this.offset = offset;
            this.isCircular = isCircular;
        }
    }

    public abstract void loadPhoto(ImageView imageView, Uri uri, int i, boolean z, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultImageProvider);

    public abstract void loadThumbnail(ImageView imageView, long j, boolean z, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultImageProvider);

    public abstract void pause();

    public abstract void preloadPhotosInBackground();

    public abstract void refreshCache();

    public abstract void resume();

    public static Drawable getDefaultAvatarDrawableForContact(Resources resources, boolean hires, DefaultImageRequest defaultImageRequest) {
        if (defaultImageRequest != null) {
            return AvatarDefaultImageProvider.getDefaultImageForContact(resources, defaultImageRequest);
        }
        if (sDefaultLetterAvatar == null) {
            sDefaultLetterAvatar = AvatarDefaultImageProvider.getDefaultImageForContact(resources, null);
        }
        return sDefaultLetterAvatar;
    }

    protected static int getDefaultAvatarResId(Context context, int extent, boolean darkTheme) {
        boolean hires = true;
        if (s180DipInPixel == -1) {
            s180DipInPixel = (int) TypedValue.applyDimension(1, 180.0f, context.getResources().getDisplayMetrics());
        }
        if (extent == -1 || extent <= s180DipInPixel) {
            hires = false;
        }
        return getDefaultAvatarResId(hires, darkTheme);
    }

    public static int getDefaultAvatarResId(boolean hires, boolean darkTheme) {
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "getDefaultAvatarResId hires:" + hires + " darkTheme:" + darkTheme);
        }
        if ((hires && darkTheme) || hires) {
            return R.drawable.contact_avatar_180_holo;
        }
        if (darkTheme) {
            return R.drawable.ic_contact_picture_holo_dark;
        }
        return R.drawable.ic_contact_picture_holo_light;
    }

    public static void refreshDefaultImageCache(Context context) {
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "refreshDefaultImageCache");
        }
        initOnlyResource(context.getApplicationContext().getResources());
        LetterTileDrawable.refreshAvatarCache(sResources);
    }

    public static void initOnlyResource(Resources aResources) {
        sResources = aResources;
    }

    public static ContactPhotoManager getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        ContactPhotoManager service = (ContactPhotoManager) applicationContext.getSystemService("contactPhotos");
        if (sResources == null) {
            initOnlyResource(applicationContext.getResources());
        }
        if (service != null) {
            return service;
        }
        service = createContactPhotoManager(applicationContext);
        HwLog.e("ContactPhotoManager", "No contact photo service in context: " + applicationContext);
        return service;
    }

    static synchronized ContactPhotoManager createContactPhotoManager(Context context) {
        ContactPhotoManager contactPhotoManagerImpl;
        synchronized (ContactPhotoManager.class) {
            contactPhotoManagerImpl = new ContactPhotoManagerImpl(context);
        }
        return contactPhotoManagerImpl;
    }

    public final void loadThumbnail(ImageView aView, long aPhotoId, boolean aDarkTheme, DefaultImageRequest defaultImageRequest) {
        loadThumbnail(aView, aPhotoId, aDarkTheme, defaultImageRequest, DEFAULT_AVATAR, -1, 0);
    }

    public final void loadThumbnail(ImageView aView, long aPhotoId, boolean aDarkTheme, DefaultImageRequest defaultImageRequest, int aFlags) {
        loadThumbnail(aView, aPhotoId, aDarkTheme, defaultImageRequest, DEFAULT_AVATAR, -1, aFlags);
    }

    public final void loadThumbnail(ImageView aView, long aPhotoId, boolean aDarkTheme, DefaultImageRequest defaultImageRequest, long aContactsId) {
        loadThumbnail(aView, aPhotoId, aDarkTheme, defaultImageRequest, DEFAULT_AVATAR, aContactsId, 0);
    }

    public final void loadThumbnail(ImageView aView, long aPhotoId, boolean aDarkTheme, DefaultImageRequest defaultImageRequest, long aContactsId, int aFlags) {
        loadThumbnail(aView, aPhotoId, aDarkTheme, defaultImageRequest, DEFAULT_AVATAR, aContactsId, aFlags);
    }

    public void loadThumbnail(ImageView aView, long aPhotoId, boolean aDarkTheme, DefaultImageRequest defaultImageRequest, DefaultImageProvider aDefaultAvatar, long aContactsId, int aFlags) {
        loadThumbnail(aView, aPhotoId, aDarkTheme, defaultImageRequest, aDefaultAvatar);
    }

    public final void loadPhoto(ImageView view, Uri photoUri, int requestedExtent, boolean darkTheme, DefaultImageRequest defaultImageRequest) {
        loadPhoto(view, photoUri, requestedExtent, darkTheme, defaultImageRequest, DEFAULT_AVATAR);
    }

    public final void loadPhoto(ImageView view, Uri photoUri, int requestedExtent, boolean darkTheme, int flags, DefaultImageRequest defaultImageRequest) {
        loadPhoto(view, photoUri, requestedExtent, darkTheme, defaultImageRequest, DEFAULT_AVATAR, flags);
    }

    public void loadPhoto(ImageView view, Uri photoUri, int requestedExtent, boolean darkTheme, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultProvider, int flags) {
        loadPhoto(view, photoUri, requestedExtent, darkTheme, defaultImageRequest, defaultProvider);
    }

    public final void loadDirectoryPhoto(ImageView view, Uri photoUri, boolean darkTheme, int flags, DefaultImageRequest defaultImageRequest) {
        loadPhoto(view, photoUri, -1, darkTheme, defaultImageRequest, DEFAULT_AVATAR, flags);
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public static boolean isRoundedEdgeBitMapRequired(int aFlags) {
        return false;
    }

    public static boolean isPrivacyIndicatorRequired(int aFlags) {
        return 4 == (aFlags & 4);
    }

    public static boolean isScaleToImageViewSize(int aFlags) {
        return 8 == (aFlags & 8);
    }

    public static Bitmap getOverlayBitmapForPrivateContact(Bitmap aCachedBitmap, int aViewWidth, int aViewHeight) {
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "getOverlayBitmapForPrivateContact aCachedBitmap :: " + aCachedBitmap);
        }
        if (aCachedBitmap == null) {
            return null;
        }
        return aCachedBitmap;
    }

    static int[] getWidthAndHeightForOverlayBitmapFromView(ImageView aView) {
        int[] lWidthAndHeight = new int[2];
        if (aView != null) {
            int lViewWidth;
            int lViewHeight;
            LayoutParams lLP = aView.getLayoutParams();
            if (lLP == null || lLP.width <= 0 || lLP.height <= 0) {
                lViewWidth = aView.getWidth();
                lViewHeight = aView.getHeight();
            } else {
                lViewWidth = lLP.width;
                lViewHeight = lLP.height;
            }
            lWidthAndHeight[0] = lViewWidth;
            lWidthAndHeight[1] = lViewHeight;
        }
        return lWidthAndHeight;
    }

    public static int pickColor(String identifier, Resources res) {
        if (sDefaultColor == 0) {
            sDefaultColor = res.getColor(R.color.letter_tile_default_color);
        }
        if (TextUtils.isEmpty(identifier)) {
            return sDefaultColor;
        }
        if (sColors == null) {
            sColors = res.obtainTypedArray(R.array.letter_tile_colors_default);
        }
        if (sColors == null || sColors.length() <= 0) {
            return sDefaultColor;
        }
        return sColors.getColor(Math.abs(identifier.hashCode() & 4095) % sColors.length(), sDefaultColor);
    }

    public static Drawable createRoundPhotoDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        int size = Math.min(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        if (size <= 0) {
            HwLog.w("ContactPhotoManager", "width and height must be > 0 ");
            return null;
        }
        Rect rect = new Rect(0, 0, size, size);
        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-65536);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(drawableToBitmap(drawable), rect, rect, paint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(sResources, output);
        bitmapDrawable.setBounds(0, 0, size, size);
        return bitmapDrawable;
    }

    public static Drawable setImageViewRoundPhoto(ImageView imageView, Resources resources, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        drawable.setAntiAlias(true);
        int lWidth = drawable.getIntrinsicWidth();
        int lHeight = drawable.getIntrinsicHeight();
        drawable.setCornerRadius(((float) Math.max(lHeight, lWidth)) / 2.0f);
        if (imageView != null) {
            if (lWidth != lHeight) {
                imageView.setScaleType(ScaleType.CENTER_INSIDE);
            } else {
                imageView.setScaleType(ScaleType.CENTER_CROP);
            }
            imageView.setImageDrawable(drawable);
        }
        return drawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Config config;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
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
        paint.setColor(-16777216);
        canvas.drawCircle(((float) size) / 2.0f, ((float) size) / 2.0f, ((float) size) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

    public static Bitmap getLetterTileDrawableBitmap(LetterTileDrawable drawable) {
        if (drawable == null || drawable.mWidth <= 0 || drawable.mHeight <= 0) {
            return null;
        }
        Config config;
        int i = drawable.mWidth;
        int i2 = drawable.mHeight;
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(i, i2, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.mWidth, drawable.mHeight);
        drawable.draw(canvas);
        return bitmap;
    }
}
