package com.huawei.gallery.editor.cache;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.imageshow.MasterImage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class BitmapCache {
    private HashMap<Long, ArrayList<WeakReference<Bitmap>>> mBitmapCache = new HashMap();
    private Handler mHandler;
    private MasterImage mImage;
    private ArrayList<Long> mKeyCache = new ArrayList(4);

    public BitmapCache(MasterImage image) {
        this.mImage = image;
        this.mHandler = new Handler(image.getContext().getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Bitmap bitmap = msg.obj;
                        if (bitmap != null && !bitmap.isRecycled()) {
                            GLRoot root = BitmapCache.this.mImage.getGLRoot();
                            if (root == null) {
                                bitmap.recycle();
                                return;
                            }
                            root.lockRenderThread();
                            try {
                                bitmap.recycle();
                                break;
                            } finally {
                                root.unlockRenderThread();
                            }
                        } else {
                            return;
                        }
                        break;
                }
            }
        };
    }

    public synchronized boolean cache(Bitmap bitmap) {
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                if (bitmap.isMutable()) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (width * height > 1048576) {
                        GalleryLog.e("BitmapCache", "Trying to cache a big bitmap");
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, bitmap));
                        return true;
                    }
                    ArrayList<WeakReference<Bitmap>> list;
                    int i;
                    Long key = calcKey((long) width, (long) height);
                    int keySize = this.mKeyCache.size();
                    if (keySize >= 4) {
                        list = (ArrayList) this.mBitmapCache.remove((Long) this.mKeyCache.remove(keySize - 1));
                        if (list != null) {
                            for (i = 0; i < list.size(); i++) {
                                Bitmap cache = (Bitmap) ((WeakReference) list.get(i)).get();
                                if (cache != null) {
                                    this.mHandler.sendMessage(this.mHandler.obtainMessage(1, cache));
                                }
                            }
                            list.clear();
                        }
                    }
                    this.mKeyCache.remove(key);
                    this.mKeyCache.add(0, key);
                    list = (ArrayList) this.mBitmapCache.get(key);
                    if (list == null) {
                        list = new ArrayList();
                        this.mBitmapCache.put(key, list);
                    }
                    i = 0;
                    while (i < list.size()) {
                        if (((WeakReference) list.get(i)).get() == null) {
                            list.remove(i);
                        } else {
                            i++;
                        }
                    }
                    for (i = 0; i < list.size(); i++) {
                        if (((WeakReference) list.get(i)).get() == null) {
                            list.remove(i);
                        }
                    }
                    for (i = 0; i < list.size(); i++) {
                        if (((WeakReference) list.get(i)).get() == bitmap) {
                            return true;
                        }
                    }
                    if (list.size() < 4) {
                        list.add(new WeakReference(bitmap));
                    } else {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, bitmap));
                    }
                    return true;
                }
                GalleryLog.e("BitmapCache", "Trying to cache a non mutable bitmap");
                return true;
            }
        }
        return true;
    }

    public synchronized Bitmap getBitmap(int w, int h) {
        Bitmap bitmap;
        Long key = calcKey((long) w, (long) h);
        WeakReference weakReference = null;
        ArrayList<WeakReference<Bitmap>> list = (ArrayList) this.mBitmapCache.get(key);
        if (list != null && list.size() > 0) {
            weakReference = (WeakReference) list.remove(0);
            if (list.size() == 0) {
                this.mBitmapCache.remove(key);
            }
        }
        bitmap = null;
        if (weakReference != null) {
            bitmap = (Bitmap) weakReference.get();
        }
        if (bitmap != null && bitmap.getWidth() == w) {
            if (bitmap.getHeight() == h) {
                if (bitmap.isRecycled()) {
                }
            }
        }
        bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        return bitmap;
    }

    public synchronized Bitmap getBitmapCopy(Bitmap source) {
        if (source == null) {
            return null;
        }
        Bitmap bitmap = getBitmap(source.getWidth(), source.getHeight());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvas.drawBitmap(source, 0.0f, 0.0f, paint);
        return bitmap;
    }

    public synchronized Bitmap getBitmapCopy(Bitmap source, float scale) {
        int width = (int) (((float) source.getWidth()) * scale);
        int height = (int) (((float) source.getHeight()) * scale);
        if (width == 0 || height == 0) {
            return null;
        }
        Bitmap bitmap = getBitmap(width, height);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvas.drawBitmap(source, matrix, paint);
        return bitmap;
    }

    private Long calcKey(long w, long h) {
        return Long.valueOf((w << 32) | h);
    }

    public synchronized void clearup() {
        long size = 0;
        for (Long key : this.mBitmapCache.keySet()) {
            ArrayList<WeakReference<Bitmap>> list = (ArrayList) this.mBitmapCache.get(key);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    Bitmap cache = (Bitmap) ((WeakReference) list.get(i)).get();
                    if (cache != null) {
                        size += (long) cache.getByteCount();
                        cache.recycle();
                    }
                }
                list.clear();
            }
        }
        this.mBitmapCache.clear();
        GalleryLog.v("BitmapCache", "Has clearup size(MB):" + (((float) size) / 1048576.0f));
    }
}
