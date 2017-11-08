package com.google.android.gms.common.images;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.internal.zzmd;
import com.google.android.gms.internal.zzne;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public final class ImageManager {
    private static HashSet<Uri> zzajA = new HashSet();
    private static ImageManager zzajB;
    private static ImageManager zzajC;
    private static final Object zzajz = new Object();
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService zzajD = Executors.newFixedThreadPool(4);
    private final zzb zzajE;
    private final zzmd zzajF;
    private final Map<zza, ImageReceiver> zzajG;
    private final Map<Uri, ImageReceiver> zzajH;
    private final Map<Uri, Long> zzajI;

    @KeepName
    /* compiled from: Unknown */
    private final class ImageReceiver extends ResultReceiver {
        private final Uri mUri;
        private final ArrayList<zza> zzajJ = new ArrayList();
        final /* synthetic */ ImageManager zzajK;

        ImageReceiver(ImageManager imageManager, Uri uri) {
            this.zzajK = imageManager;
            super(new Handler(Looper.getMainLooper()));
            this.mUri = uri;
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
            this.zzajK.zzajD.execute(new zzc(this.zzajK, this.mUri, (ParcelFileDescriptor) resultData.getParcelable("com.google.android.gms.extra.fileDescriptor")));
        }

        public void zzb(zza zza) {
            com.google.android.gms.common.internal.zzb.zzcD("ImageReceiver.addImageRequest() must be called in the main thread");
            this.zzajJ.add(zza);
        }

        public void zzc(zza zza) {
            com.google.android.gms.common.internal.zzb.zzcD("ImageReceiver.removeImageRequest() must be called in the main thread");
            this.zzajJ.remove(zza);
        }

        public void zzqm() {
            Intent intent = new Intent("com.google.android.gms.common.images.LOAD_IMAGE");
            intent.putExtra("com.google.android.gms.extras.uri", this.mUri);
            intent.putExtra("com.google.android.gms.extras.resultReceiver", this);
            intent.putExtra("com.google.android.gms.extras.priority", 3);
            this.zzajK.mContext.sendBroadcast(intent);
        }
    }

    /* compiled from: Unknown */
    public interface OnImageLoadedListener {
        void onImageLoaded(Uri uri, Drawable drawable, boolean z);
    }

    @TargetApi(11)
    /* compiled from: Unknown */
    private static final class zza {
        static int zza(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    /* compiled from: Unknown */
    private static final class zzb extends LruCache<zza, Bitmap> {
        public zzb(Context context) {
            super(zzas(context));
        }

        @TargetApi(11)
        private static int zzas(Context context) {
            Object obj = null;
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            if ((context.getApplicationInfo().flags & 1048576) != 0) {
                obj = 1;
            }
            int zza = (obj != null && zzne.zzsd()) ? zza.zza(activityManager) : activityManager.getMemoryClass();
            return (int) (((float) (zza * 1048576)) * 0.33f);
        }

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
        final /* synthetic */ ImageManager zzajK;
        private final ParcelFileDescriptor zzajL;

        public zzc(ImageManager imageManager, Uri uri, ParcelFileDescriptor parcelFileDescriptor) {
            this.zzajK = imageManager;
            this.mUri = uri;
            this.zzajL = parcelFileDescriptor;
        }

        public void run() {
            Bitmap bitmap = null;
            com.google.android.gms.common.internal.zzb.zzcE("LoadBitmapFromDiskRunnable can't be executed in the main thread");
            boolean z = false;
            if (this.zzajL != null) {
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(this.zzajL.getFileDescriptor());
                } catch (Throwable e) {
                    Log.e("ImageManager", "OOM while loading bitmap for uri: " + this.mUri, e);
                    z = true;
                }
                try {
                    this.zzajL.close();
                } catch (Throwable e2) {
                    Log.e("ImageManager", "closed failed", e2);
                }
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.zzajK.mHandler.post(new zzf(this.zzajK, this.mUri, bitmap, z, countDownLatch));
            try {
                countDownLatch.await();
            } catch (InterruptedException e3) {
                Log.w("ImageManager", "Latch interrupted while posting " + this.mUri);
            }
        }
    }

    /* compiled from: Unknown */
    private final class zzd implements Runnable {
        final /* synthetic */ ImageManager zzajK;
        private final zza zzajM;

        public zzd(ImageManager imageManager, zza zza) {
            this.zzajK = imageManager;
            this.zzajM = zza;
        }

        public void run() {
            com.google.android.gms.common.internal.zzb.zzcD("LoadImageRunnable must be executed on the main thread");
            ImageReceiver imageReceiver = (ImageReceiver) this.zzajK.zzajG.get(this.zzajM);
            if (imageReceiver != null) {
                this.zzajK.zzajG.remove(this.zzajM);
                imageReceiver.zzc(this.zzajM);
            }
            zza zza = this.zzajM.zzajO;
            if (zza.uri != null) {
                Bitmap zza2 = this.zzajK.zza(zza);
                if (zza2 == null) {
                    Long l = (Long) this.zzajK.zzajI.get(zza.uri);
                    if (l != null) {
                        if (SystemClock.elapsedRealtime() - l.longValue() >= 3600000) {
                            this.zzajK.zzajI.remove(zza.uri);
                        } else {
                            this.zzajM.zza(this.zzajK.mContext, this.zzajK.zzajF, true);
                            return;
                        }
                    }
                    this.zzajM.zza(this.zzajK.mContext, this.zzajK.zzajF);
                    imageReceiver = (ImageReceiver) this.zzajK.zzajH.get(zza.uri);
                    if (imageReceiver == null) {
                        imageReceiver = new ImageReceiver(this.zzajK, zza.uri);
                        this.zzajK.zzajH.put(zza.uri, imageReceiver);
                    }
                    imageReceiver.zzb(this.zzajM);
                    if (!(this.zzajM instanceof com.google.android.gms.common.images.zza.zzc)) {
                        this.zzajK.zzajG.put(this.zzajM, imageReceiver);
                    }
                    synchronized (ImageManager.zzajz) {
                        if (!ImageManager.zzajA.contains(zza.uri)) {
                            ImageManager.zzajA.add(zza.uri);
                            imageReceiver.zzqm();
                        }
                    }
                    return;
                }
                this.zzajM.zza(this.zzajK.mContext, zza2, true);
                return;
            }
            this.zzajM.zza(this.zzajK.mContext, this.zzajK.zzajF, true);
        }
    }

    @TargetApi(14)
    /* compiled from: Unknown */
    private static final class zze implements ComponentCallbacks2 {
        private final zzb zzajE;

        public zze(zzb zzb) {
            this.zzajE = zzb;
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void onLowMemory() {
            this.zzajE.evictAll();
        }

        public void onTrimMemory(int level) {
            if (level >= 60) {
                this.zzajE.evictAll();
            } else if (level >= 20) {
                this.zzajE.trimToSize(this.zzajE.size() / 2);
            }
        }
    }

    /* compiled from: Unknown */
    private final class zzf implements Runnable {
        private final Bitmap mBitmap;
        private final Uri mUri;
        final /* synthetic */ ImageManager zzajK;
        private boolean zzajN;
        private final CountDownLatch zzpJ;

        public zzf(ImageManager imageManager, Uri uri, Bitmap bitmap, boolean z, CountDownLatch countDownLatch) {
            this.zzajK = imageManager;
            this.mUri = uri;
            this.mBitmap = bitmap;
            this.zzajN = z;
            this.zzpJ = countDownLatch;
        }

        private void zza(ImageReceiver imageReceiver, boolean z) {
            ArrayList zza = imageReceiver.zzajJ;
            int size = zza.size();
            for (int i = 0; i < size; i++) {
                zza zza2 = (zza) zza.get(i);
                if (z) {
                    zza2.zza(this.zzajK.mContext, this.mBitmap, false);
                } else {
                    this.zzajK.zzajI.put(this.mUri, Long.valueOf(SystemClock.elapsedRealtime()));
                    zza2.zza(this.zzajK.mContext, this.zzajK.zzajF, false);
                }
                if (!(zza2 instanceof com.google.android.gms.common.images.zza.zzc)) {
                    this.zzajK.zzajG.remove(zza2);
                }
            }
        }

        public void run() {
            com.google.android.gms.common.internal.zzb.zzcD("OnBitmapLoadedRunnable must be executed in the main thread");
            boolean z = this.mBitmap != null;
            if (this.zzajK.zzajE != null) {
                if (this.zzajN) {
                    this.zzajK.zzajE.evictAll();
                    System.gc();
                    this.zzajN = false;
                    this.zzajK.mHandler.post(this);
                    return;
                } else if (z) {
                    this.zzajK.zzajE.put(new zza(this.mUri), this.mBitmap);
                }
            }
            ImageReceiver imageReceiver = (ImageReceiver) this.zzajK.zzajH.remove(this.mUri);
            if (imageReceiver != null) {
                zza(imageReceiver, z);
            }
            this.zzpJ.countDown();
            synchronized (ImageManager.zzajz) {
                ImageManager.zzajA.remove(this.mUri);
            }
        }
    }

    private ImageManager(Context context, boolean withMemoryCache) {
        this.mContext = context.getApplicationContext();
        if (withMemoryCache) {
            this.zzajE = new zzb(this.mContext);
            if (zzne.zzsg()) {
                zzqj();
            }
        } else {
            this.zzajE = null;
        }
        this.zzajF = new zzmd();
        this.zzajG = new HashMap();
        this.zzajH = new HashMap();
        this.zzajI = new HashMap();
    }

    public static ImageManager create(Context context) {
        return zzc(context, false);
    }

    private Bitmap zza(zza zza) {
        return this.zzajE != null ? (Bitmap) this.zzajE.get(zza) : null;
    }

    public static ImageManager zzc(Context context, boolean z) {
        if (z) {
            if (zzajC == null) {
                zzajC = new ImageManager(context, true);
            }
            return zzajC;
        }
        if (zzajB == null) {
            zzajB = new ImageManager(context, false);
        }
        return zzajB;
    }

    @TargetApi(14)
    private void zzqj() {
        this.mContext.registerComponentCallbacks(new zze(this.zzajE));
    }

    public void loadImage(ImageView imageView, int resId) {
        zza(new com.google.android.gms.common.images.zza.zzb(imageView, resId));
    }

    public void loadImage(ImageView imageView, Uri uri) {
        zza(new com.google.android.gms.common.images.zza.zzb(imageView, uri));
    }

    public void loadImage(ImageView imageView, Uri uri, int defaultResId) {
        zza zzb = new com.google.android.gms.common.images.zza.zzb(imageView, uri);
        zzb.zzbM(defaultResId);
        zza(zzb);
    }

    public void loadImage(OnImageLoadedListener listener, Uri uri) {
        zza(new com.google.android.gms.common.images.zza.zzc(listener, uri));
    }

    public void loadImage(OnImageLoadedListener listener, Uri uri, int defaultResId) {
        zza zzc = new com.google.android.gms.common.images.zza.zzc(listener, uri);
        zzc.zzbM(defaultResId);
        zza(zzc);
    }

    public void zza(zza zza) {
        com.google.android.gms.common.internal.zzb.zzcD("ImageManager.loadImage() must be called in the main thread");
        new zzd(this, zza).run();
    }
}
