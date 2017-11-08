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
import android.os.SystemClock;
import android.util.Log;
import com.google.android.gms.internal.zzku;
import com.google.android.gms.internal.zzlf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/* compiled from: Unknown */
public final class ImageManager {
    private static final Object zzabC = new Object();
    private static HashSet<Uri> zzabD = new HashSet();
    private final Context mContext;
    private final Handler mHandler;
    private final ExecutorService zzabG;
    private final zzb zzabH;
    private final zzku zzabI;
    private final Map<zza, ImageReceiver> zzabJ;
    private final Map<Uri, ImageReceiver> zzabK;
    private final Map<Uri, Long> zzabL;

    /* compiled from: Unknown */
    private final class ImageReceiver extends ResultReceiver {
        private final Uri mUri;
        private final ArrayList<zza> zzabM;
        final /* synthetic */ ImageManager zzabN;

        public void onReceiveResult(int resultCode, Bundle resultData) {
            this.zzabN.zzabG.execute(new zzc(this.zzabN, this.mUri, (ParcelFileDescriptor) resultData.getParcelable("com.google.android.gms.extra.fileDescriptor")));
        }
    }

    /* compiled from: Unknown */
    public interface OnImageLoadedListener {
        void onImageLoaded(Uri uri, Drawable drawable, boolean z);
    }

    /* compiled from: Unknown */
    private static final class zzb extends zzlf<zza, Bitmap> {
        protected /* synthetic */ void entryRemoved(boolean z, Object obj, Object obj2, Object obj3) {
            zza(z, (zza) obj, (Bitmap) obj2, (Bitmap) obj3);
        }

        protected /* synthetic */ int sizeOf(Object obj, Object obj2) {
            return zza((zza) obj, (Bitmap) obj2);
        }

        protected int zza(zza zza, Bitmap bitmap) {
            return bitmap.getHeight() * bitmap.getRowBytes();
        }

        protected void zza(boolean z, zza zza, Bitmap bitmap, Bitmap bitmap2) {
            super.entryRemoved(z, zza, bitmap, bitmap2);
        }
    }

    /* compiled from: Unknown */
    private final class zzc implements Runnable {
        private final Uri mUri;
        final /* synthetic */ ImageManager zzabN;
        private final ParcelFileDescriptor zzabO;

        public zzc(ImageManager imageManager, Uri uri, ParcelFileDescriptor parcelFileDescriptor) {
            this.zzabN = imageManager;
            this.mUri = uri;
            this.zzabO = parcelFileDescriptor;
        }

        public void run() {
            Bitmap bitmap = null;
            com.google.android.gms.common.internal.zzb.zzci("LoadBitmapFromDiskRunnable can't be executed in the main thread");
            boolean z = false;
            if (this.zzabO != null) {
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(this.zzabO.getFileDescriptor());
                } catch (Throwable e) {
                    Log.e("ImageManager", "OOM while loading bitmap for uri: " + this.mUri, e);
                    z = true;
                }
                try {
                    this.zzabO.close();
                } catch (Throwable e2) {
                    Log.e("ImageManager", "closed failed", e2);
                }
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.zzabN.mHandler.post(new zzf(this.zzabN, this.mUri, bitmap, z, countDownLatch));
            try {
                countDownLatch.await();
            } catch (InterruptedException e3) {
                Log.w("ImageManager", "Latch interrupted while posting " + this.mUri);
            }
        }
    }

    /* compiled from: Unknown */
    private final class zzf implements Runnable {
        private final Bitmap mBitmap;
        private final Uri mUri;
        final /* synthetic */ ImageManager zzabN;
        private boolean zzabQ;
        private final CountDownLatch zzoR;

        public zzf(ImageManager imageManager, Uri uri, Bitmap bitmap, boolean z, CountDownLatch countDownLatch) {
            this.zzabN = imageManager;
            this.mUri = uri;
            this.mBitmap = bitmap;
            this.zzabQ = z;
            this.zzoR = countDownLatch;
        }

        private void zza(ImageReceiver imageReceiver, boolean z) {
            ArrayList zza = imageReceiver.zzabM;
            int size = zza.size();
            for (int i = 0; i < size; i++) {
                zza zza2 = (zza) zza.get(i);
                if (z) {
                    zza2.zza(this.zzabN.mContext, this.mBitmap, false);
                } else {
                    this.zzabN.zzabL.put(this.mUri, Long.valueOf(SystemClock.elapsedRealtime()));
                    zza2.zza(this.zzabN.mContext, this.zzabN.zzabI, false);
                }
                if (!(zza2 instanceof com.google.android.gms.common.images.zza.zzc)) {
                    this.zzabN.zzabJ.remove(zza2);
                }
            }
        }

        public void run() {
            com.google.android.gms.common.internal.zzb.zzch("OnBitmapLoadedRunnable must be executed in the main thread");
            boolean z = this.mBitmap != null;
            if (this.zzabN.zzabH != null) {
                if (this.zzabQ) {
                    this.zzabN.zzabH.evictAll();
                    System.gc();
                    this.zzabQ = false;
                    this.zzabN.mHandler.post(this);
                    return;
                } else if (z) {
                    this.zzabN.zzabH.put(new zza(this.mUri), this.mBitmap);
                }
            }
            ImageReceiver imageReceiver = (ImageReceiver) this.zzabN.zzabK.remove(this.mUri);
            if (imageReceiver != null) {
                zza(imageReceiver, z);
            }
            this.zzoR.countDown();
            synchronized (ImageManager.zzabC) {
                ImageManager.zzabD.remove(this.mUri);
            }
        }
    }
}
