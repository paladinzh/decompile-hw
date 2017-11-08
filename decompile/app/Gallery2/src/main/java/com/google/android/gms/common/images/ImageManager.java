package com.google.android.gms.common.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.util.Log;
import com.google.android.gms.common.images.a.a;
import com.google.android.gms.internal.ed;
import com.google.android.gms.internal.ev;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/* compiled from: Unknown */
public final class ImageManager {
    private static final Object Ar = new Object();
    private static HashSet<Uri> As = new HashSet();
    private final ExecutorService Av;
    private final b Aw;
    private final Map<a, ImageReceiver> Ax;
    private final Map<Uri, ImageReceiver> Ay;
    private final Context mContext;
    private final Handler mHandler;

    /* compiled from: Unknown */
    private final class ImageReceiver extends ResultReceiver {
        boolean AA;
        final /* synthetic */ ImageManager AB;
        private final ArrayList<a> Az;
        private final Uri mUri;

        public void onReceiveResult(int resultCode, Bundle resultData) {
            this.AB.Av.execute(new c(this.AB, this.mUri, (ParcelFileDescriptor) resultData.getParcelable("com.google.android.gms.extra.fileDescriptor")));
        }
    }

    /* compiled from: Unknown */
    public interface OnImageLoadedListener {
        void onImageLoaded(Uri uri, Drawable drawable, boolean z);
    }

    /* compiled from: Unknown */
    private static final class b extends ev<a, Bitmap> {
        protected int a(a aVar, Bitmap bitmap) {
            return bitmap.getHeight() * bitmap.getRowBytes();
        }

        protected void a(boolean z, a aVar, Bitmap bitmap, Bitmap bitmap2) {
            super.entryRemoved(z, aVar, bitmap, bitmap2);
        }

        protected /* synthetic */ void entryRemoved(boolean x0, Object x1, Object x2, Object x3) {
            a(x0, (a) x1, (Bitmap) x2, (Bitmap) x3);
        }

        protected /* synthetic */ int sizeOf(Object x0, Object x1) {
            return a((a) x0, (Bitmap) x1);
        }
    }

    /* compiled from: Unknown */
    private final class c implements Runnable {
        final /* synthetic */ ImageManager AB;
        private final ParcelFileDescriptor AC;
        private final Uri mUri;

        public c(ImageManager imageManager, Uri uri, ParcelFileDescriptor parcelFileDescriptor) {
            this.AB = imageManager;
            this.mUri = uri;
            this.AC = parcelFileDescriptor;
        }

        public void run() {
            Bitmap bitmap = null;
            ed.ad("LoadBitmapFromDiskRunnable can't be executed in the main thread");
            boolean z = false;
            if (this.AC != null) {
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(this.AC.getFileDescriptor());
                } catch (Throwable e) {
                    Log.e("ImageManager", "OOM while loading bitmap for uri: " + this.mUri, e);
                    z = true;
                }
                try {
                    this.AC.close();
                } catch (Throwable e2) {
                    Log.e("ImageManager", "closed failed", e2);
                }
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.AB.mHandler.post(new f(this.AB, this.mUri, bitmap, z, countDownLatch));
            try {
                countDownLatch.await();
            } catch (InterruptedException e3) {
                Log.w("ImageManager", "Latch interrupted while posting " + this.mUri);
            }
        }
    }

    /* compiled from: Unknown */
    private final class f implements Runnable {
        final /* synthetic */ ImageManager AB;
        private boolean AE;
        private final Bitmap mBitmap;
        private final Uri mUri;
        private final CountDownLatch zf;

        public f(ImageManager imageManager, Uri uri, Bitmap bitmap, boolean z, CountDownLatch countDownLatch) {
            this.AB = imageManager;
            this.mUri = uri;
            this.mBitmap = bitmap;
            this.AE = z;
            this.zf = countDownLatch;
        }

        private void a(ImageReceiver imageReceiver, boolean z) {
            imageReceiver.AA = true;
            ArrayList a = imageReceiver.Az;
            int size = a.size();
            for (int i = 0; i < size; i++) {
                a aVar = (a) a.get(i);
                if (z) {
                    aVar.a(this.AB.mContext, this.mBitmap, false);
                } else {
                    aVar.b(this.AB.mContext, false);
                }
                if (aVar.AI != 1) {
                    this.AB.Ax.remove(aVar);
                }
            }
            imageReceiver.AA = false;
        }

        public void run() {
            ed.ac("OnBitmapLoadedRunnable must be executed in the main thread");
            boolean z = this.mBitmap != null;
            if (this.AB.Aw != null) {
                if (this.AE) {
                    this.AB.Aw.evictAll();
                    System.gc();
                    this.AE = false;
                    this.AB.mHandler.post(this);
                    return;
                } else if (z) {
                    this.AB.Aw.put(new a(this.mUri), this.mBitmap);
                }
            }
            ImageReceiver imageReceiver = (ImageReceiver) this.AB.Ay.remove(this.mUri);
            if (imageReceiver != null) {
                a(imageReceiver, z);
            }
            this.zf.countDown();
            synchronized (ImageManager.Ar) {
                ImageManager.As.remove(this.mUri);
            }
        }
    }
}
