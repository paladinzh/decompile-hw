package com.huawei.mms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import com.android.mms.ui.UriImage;
import com.android.mms.util.BackgroundLoaderManager;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.SimpleCache;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.huawei.cspcommon.MLog;
import java.util.Set;

public class HwSimpleImageLoader extends BackgroundLoaderManager {
    private final Context mContext;
    private int mHeightLimit;
    private final SimpleCache<Uri, Bitmap> mImageCache = new SimpleCache(8, 16, 0.75f, true);
    private int mWidthLimit;

    public class ImageLoadTask implements Runnable {
        private final boolean mIsVideo;
        private final Uri mUri;

        public ImageLoadTask(Uri uri, boolean isVideo) {
            if (uri == null) {
                throw new NullPointerException();
            }
            this.mUri = uri;
            this.mIsVideo = isVideo;
        }

        public void run() {
            final Bitmap resultBitmap = getBitmap();
            HwSimpleImageLoader.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Set<ItemLoadedCallback> callbacks = (Set) HwSimpleImageLoader.this.mCallbacks.get(ImageLoadTask.this.mUri);
                    if (callbacks != null) {
                        Bitmap bitmap = resultBitmap == null ? ImageLoadTask.this.mIsVideo ? ResEx.self().getEmptyVedio() : ResEx.self().getEmptyImage() : resultBitmap;
                        for (ItemLoadedCallback<ImageLoaded> callback : BackgroundLoaderManager.asList(callbacks)) {
                            callback.onItemLoaded(new ImageLoaded(bitmap, ImageLoadTask.this.mIsVideo), null);
                        }
                    }
                    if (resultBitmap != null) {
                        HwSimpleImageLoader.this.mImageCache.put(ImageLoadTask.this.mUri, resultBitmap);
                    }
                    HwSimpleImageLoader.this.mCallbacks.remove(ImageLoadTask.this.mUri);
                    HwSimpleImageLoader.this.mPendingTaskUris.remove(ImageLoadTask.this.mUri);
                }
            });
        }

        private Bitmap getBitmap() {
            int BounsLimit = Math.max(HwSimpleImageLoader.this.mWidthLimit, HwSimpleImageLoader.this.mHeightLimit);
            UriImage uriImage = new UriImage(HwSimpleImageLoader.this.mContext, this.mUri);
            byte[] data = UriImage.getResizedImageData(uriImage.getWidth(), uriImage.getHeight(), BounsLimit, BounsLimit, 102400, this.mUri, HwSimpleImageLoader.this.mContext);
            if (data == null) {
                return null;
            }
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (Exception ex) {
                MLog.e("HwSimpleImageLoader", "decodeByteArray has exception : " + ex);
            } catch (Error e) {
                MLog.e("HwSimpleImageLoader", "decodeByteArray has error : " + e);
            }
            return bitmap;
        }
    }

    public HwSimpleImageLoader(Context context, Handler handler) {
        super(context, handler);
        this.mContext = context;
    }

    public void loadImage(Uri uri, boolean isVideo, ItemLoadedCallback<ImageLoaded> callback, int widthLimit, int heightLimit) {
        if (uri == null) {
            MLog.e("HwSimpleImageLoader", "Load image uri is null.");
            return;
        }
        this.mHeightLimit = heightLimit;
        this.mWidthLimit = widthLimit;
        Bitmap image = (Bitmap) this.mImageCache.get(uri);
        boolean imageExists = image != null;
        boolean newTaskRequired = (imageExists || this.mPendingTaskUris.contains(uri)) ? false : true;
        boolean callbackRequired = callback != null;
        if (imageExists) {
            if (callbackRequired) {
                callback.onItemLoaded(new ImageLoaded(image, isVideo), null);
            }
            return;
        }
        if (callbackRequired) {
            addCallback(uri, callback);
        }
        if (newTaskRequired) {
            this.mPendingTaskUris.add(uri);
            try {
                this.mExecutor.execute(new ImageLoadTask(uri, isVideo));
            } catch (Exception e) {
                MLog.e("HwSimpleImageLoader", "Image load task exception");
            }
        }
    }

    public synchronized void clear() {
        super.clear();
        this.mImageCache.clear();
    }

    public String getTag() {
        return "HwSimpleImageLoader";
    }
}
