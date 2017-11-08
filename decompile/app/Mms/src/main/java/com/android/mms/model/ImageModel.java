package com.android.mms.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import com.android.mms.ContentRestrictionException;
import com.android.mms.MmsApp;
import com.android.mms.ui.UriImage;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ItemLoadedFuture;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.events.Event;

public class ImageModel extends RegionMediaModel {
    private static final Set<String> SUPPORTED_MMS_IMAGE_CONTENT_TYPES = new HashSet(Arrays.asList(new String[]{"image/jpeg"}));
    private SoftReference<Bitmap> mFullSizeBitmapCache = new SoftReference(null);
    private int mHeight;
    private ItemLoadedFuture mItemLoadedFuture;
    private int mWidth;

    public ImageModel(Context context, Uri uri, RegionModel region) throws MmsException {
        super(context, "img", uri, region);
        initModelFromUri(uri);
        checkContentRestriction(uri);
    }

    public ImageModel(Context context, String contentType, String src, Uri uri, RegionModel region) throws MmsException {
        super(context, "img", contentType, src, uri, region);
        decodeImageBounds(uri);
    }

    public String toString() {
        return " mSrc=" + getSrc() + " mContentType=" + this.mContentType + " width=" + getWidth() + " height= " + getHeight();
    }

    private void initModelFromUri(Uri uri) throws MmsException {
        UriImage uriImage = new UriImage(this.mContext, uri);
        this.mContentType = uriImage.getContentType();
        if (TextUtils.isEmpty(this.mContentType)) {
            throw new MmsException("Type of media is unknown.");
        }
        this.mSrc = uriImage.getSrc();
        this.mWidth = uriImage.getWidth();
        this.mHeight = uriImage.getHeight();
    }

    private void decodeImageBounds(Uri uri) {
        UriImage uriImage = new UriImage(this.mContext, uri);
        this.mWidth = uriImage.getWidth();
        this.mHeight = uriImage.getHeight();
    }

    public void handleEvent(Event evt) {
        if (evt.getType().equals("SmilMediaStart")) {
            this.mVisible = true;
        } else if (this.mFill != (short) 1) {
            this.mVisible = false;
        }
        notifyModelChanged(false);
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    protected void checkContentRestriction(Uri uri) throws ContentRestrictionException {
        ContentRestrictionFactory.getContentRestriction().checkImageContentType(this.mContentType, this.mContext, uri);
    }

    public ItemLoadedFuture loadThumbnailBitmap(ItemLoadedCallback callback) {
        this.mItemLoadedFuture = MmsApp.getApplication().getThumbnailManager().getThumbnail(getUri(), callback);
        return this.mItemLoadedFuture;
    }

    public void cancelThumbnailLoading() {
        if (this.mItemLoadedFuture != null && !this.mItemLoadedFuture.isDone()) {
            if (MLog.isLoggable("Mms_app", 3)) {
                MLog.v("Mms/image", "cancelThumbnailLoading for: " + this);
            }
            this.mItemLoadedFuture.cancel(getUri());
            this.mItemLoadedFuture = null;
        }
    }

    private Bitmap createBitmap(int thumbnailBoundsLimit, Uri uri) {
        byte[] data = UriImage.getResizedImageData(this.mWidth, this.mHeight, thumbnailBoundsLimit, thumbnailBoundsLimit, 102400, uri, this.mContext);
        if (data == null || data.length == 0) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception ex) {
            MLog.e("Mms/image", "decodeByteArray has exception : " + ex);
        } catch (Error e) {
            MLog.e("Mms/image", "decodeByteArray has error : " + e);
        }
        return bitmap;
    }

    public Bitmap getBitmap(int width, int height) {
        Bitmap bm = (Bitmap) this.mFullSizeBitmapCache.get();
        if (bm == null) {
            try {
                bm = createBitmap(Math.max(width, height), getUri());
                if (bm != null) {
                    this.mFullSizeBitmapCache = new SoftReference(bm);
                }
            } catch (OutOfMemoryError e) {
            }
        }
        return bm;
    }
}
