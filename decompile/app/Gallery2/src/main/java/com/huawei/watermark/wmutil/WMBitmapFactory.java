package com.huawei.watermark.wmutil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import com.huawei.watermark.decoratorclass.WMLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WMBitmapFactory {
    private static WMBitmapFactory instance;
    private BitmapPool mBitmapPool = new BitmapPool(15);

    private static class BitmapPool {
        private final ArrayList<Bitmap> mPool;
        private final int mPoolLimit;

        public BitmapPool(int poolLimit) {
            this.mPoolLimit = poolLimit;
            this.mPool = new ArrayList(poolLimit);
        }

        public synchronized Bitmap getBitmap(int width, int height) {
            for (int i = this.mPool.size() - 1; i >= 0; i--) {
                Bitmap b = (Bitmap) this.mPool.get(i);
                if (b.getWidth() == width && b.getHeight() == height) {
                    return (Bitmap) this.mPool.remove(i);
                }
            }
            return null;
        }

        public void recycle(Bitmap bitmap) {
            if (bitmap != null && !bitmap.isRecycled()) {
                if (bitmap.isMutable()) {
                    synchronized (this) {
                        if (this.mPool.size() >= this.mPoolLimit) {
                            recycleBitmap((Bitmap) this.mPool.remove(0));
                        }
                        this.mPool.add(bitmap);
                    }
                    return;
                }
                bitmap.recycle();
            }
        }

        public synchronized void clear() {
            for (Bitmap bmp : this.mPool) {
                recycleBitmap(bmp);
            }
            this.mPool.clear();
        }

        private void recycleBitmap(Bitmap bmp) {
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
        }
    }

    public static synchronized WMBitmapFactory getInstance() {
        WMBitmapFactory wMBitmapFactory;
        synchronized (WMBitmapFactory.class) {
            if (instance == null) {
                instance = new WMBitmapFactory();
            }
            wMBitmapFactory = instance;
        }
        return wMBitmapFactory;
    }

    private WMBitmapFactory() {
    }

    public Bitmap decodeStream(InputStream is) throws IOException {
        Options targetOptions = WMBitmapUtil.newOptions();
        targetOptions.inJustDecodeBounds = true;
        byte[] data = loadBytesFromInputStream(is);
        BitmapFactory.decodeByteArray(data, 0, data.length, targetOptions);
        Bitmap inBitmap = this.mBitmapPool.getBitmap(targetOptions.outWidth, targetOptions.outHeight);
        WMLog.v("WMBitmapFactory", "WMBitmapFactory reuse bitmap :" + inBitmap);
        return BitmapFactory.decodeByteArray(data, 0, data.length, WMBitmapUtil.newOptions(inBitmap));
    }

    private byte[] loadBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int size = is.read(buffer);
            if (size == -1) {
                return os.toByteArray();
            }
            os.write(buffer, 0, size);
        }
    }

    public void recycleBitmap(Bitmap bitmap) {
        this.mBitmapPool.recycle(bitmap);
    }

    public void release() {
        this.mBitmapPool.clear();
    }
}
