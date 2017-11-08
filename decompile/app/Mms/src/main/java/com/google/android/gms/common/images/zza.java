package com.google.android.gms.common.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import com.google.android.gms.common.images.ImageManager.OnImageLoadedListener;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.internal.zzma;
import com.google.android.gms.internal.zzmb;
import com.google.android.gms.internal.zzmc;
import com.google.android.gms.internal.zzmd;
import java.lang.ref.WeakReference;

/* compiled from: Unknown */
public abstract class zza {
    final zza zzajO;
    protected int zzajP = 0;
    protected int zzajQ = 0;
    protected boolean zzajR = false;
    protected OnImageLoadedListener zzajS;
    private boolean zzajT = true;
    private boolean zzajU = false;
    private boolean zzajV = true;
    protected int zzajW;

    /* compiled from: Unknown */
    static final class zza {
        public final Uri uri;

        public zza(Uri uri) {
            this.uri = uri;
        }

        public boolean equals(Object obj) {
            return obj instanceof zza ? this != obj ? zzw.equal(((zza) obj).uri, this.uri) : true : false;
        }

        public int hashCode() {
            return zzw.hashCode(this.uri);
        }
    }

    /* compiled from: Unknown */
    public static final class zzb extends zza {
        private WeakReference<ImageView> zzajX;

        public zzb(ImageView imageView, int i) {
            super(null, i);
            com.google.android.gms.common.internal.zzb.zzv(imageView);
            this.zzajX = new WeakReference(imageView);
        }

        public zzb(ImageView imageView, Uri uri) {
            super(uri, 0);
            com.google.android.gms.common.internal.zzb.zzv(imageView);
            this.zzajX = new WeakReference(imageView);
        }

        private void zza(ImageView imageView, Drawable drawable, boolean z, boolean z2, boolean z3) {
            Uri uri = null;
            if (z2 || z3) {
                Object obj = null;
            } else {
                int i = 1;
            }
            if (obj != null && (imageView instanceof zzmc)) {
                int zzqp = ((zzmc) imageView).zzqp();
                if (this.zzajQ != 0 && zzqp == this.zzajQ) {
                    return;
                }
            }
            boolean zzb = zzb(z, z2);
            Drawable newDrawable = (this.zzajR && drawable != null) ? drawable.getConstantState().newDrawable() : drawable;
            if (zzb) {
                newDrawable = zza(imageView.getDrawable(), newDrawable);
            }
            imageView.setImageDrawable(newDrawable);
            if (imageView instanceof zzmc) {
                zzmc zzmc = (zzmc) imageView;
                if (z3) {
                    uri = this.zzajO.uri;
                }
                zzmc.zzm(uri);
                zzmc.zzbO(obj == null ? 0 : this.zzajQ);
            }
            if (zzb) {
                ((zzma) newDrawable).startTransition(250);
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof zzb)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            ImageView imageView = (ImageView) this.zzajX.get();
            ImageView imageView2 = (ImageView) ((zzb) obj).zzajX.get();
            boolean z = (imageView2 == null || imageView == null || !zzw.equal(imageView2, imageView)) ? false : true;
            return z;
        }

        public int hashCode() {
            return 0;
        }

        protected void zza(Drawable drawable, boolean z, boolean z2, boolean z3) {
            ImageView imageView = (ImageView) this.zzajX.get();
            if (imageView != null) {
                zza(imageView, drawable, z, z2, z3);
            }
        }
    }

    /* compiled from: Unknown */
    public static final class zzc extends zza {
        private WeakReference<OnImageLoadedListener> zzajY;

        public zzc(OnImageLoadedListener onImageLoadedListener, Uri uri) {
            super(uri, 0);
            com.google.android.gms.common.internal.zzb.zzv(onImageLoadedListener);
            this.zzajY = new WeakReference(onImageLoadedListener);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof zzc)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            zzc zzc = (zzc) obj;
            OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.zzajY.get();
            OnImageLoadedListener onImageLoadedListener2 = (OnImageLoadedListener) zzc.zzajY.get();
            boolean z = onImageLoadedListener2 != null && onImageLoadedListener != null && zzw.equal(onImageLoadedListener2, onImageLoadedListener) && zzw.equal(zzc.zzajO, this.zzajO);
            return z;
        }

        public int hashCode() {
            return zzw.hashCode(this.zzajO);
        }

        protected void zza(Drawable drawable, boolean z, boolean z2, boolean z3) {
            if (!z2) {
                OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.zzajY.get();
                if (onImageLoadedListener != null) {
                    onImageLoadedListener.onImageLoaded(this.zzajO.uri, drawable, z3);
                }
            }
        }
    }

    public zza(Uri uri, int i) {
        this.zzajO = new zza(uri);
        this.zzajQ = i;
    }

    private Drawable zza(Context context, zzmd zzmd, int i) {
        Resources resources = context.getResources();
        if (this.zzajW <= 0) {
            return resources.getDrawable(i);
        }
        com.google.android.gms.internal.zzmd.zza zza = new com.google.android.gms.internal.zzmd.zza(i, this.zzajW);
        Drawable drawable = (Drawable) zzmd.get(zza);
        if (drawable == null) {
            drawable = resources.getDrawable(i);
            if ((this.zzajW & 1) != 0) {
                drawable = zza(resources, drawable);
            }
            zzmd.put(zza, drawable);
        }
        return drawable;
    }

    protected Drawable zza(Resources resources, Drawable drawable) {
        return zzmb.zza(resources, drawable);
    }

    protected zzma zza(Drawable drawable, Drawable drawable2) {
        if (drawable == null) {
            drawable = null;
        } else if (drawable instanceof zzma) {
            drawable = ((zzma) drawable).zzqn();
        }
        return new zzma(drawable, drawable2);
    }

    void zza(Context context, Bitmap bitmap, boolean z) {
        com.google.android.gms.common.internal.zzb.zzv(bitmap);
        if ((this.zzajW & 1) != 0) {
            bitmap = zzmb.zzb(bitmap);
        }
        Drawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
        if (this.zzajS != null) {
            this.zzajS.onImageLoaded(this.zzajO.uri, bitmapDrawable, true);
        }
        zza(bitmapDrawable, z, false, true);
    }

    void zza(Context context, zzmd zzmd) {
        if (this.zzajV) {
            Drawable drawable = null;
            if (this.zzajP != 0) {
                drawable = zza(context, zzmd, this.zzajP);
            }
            zza(drawable, false, true, false);
        }
    }

    void zza(Context context, zzmd zzmd, boolean z) {
        Drawable drawable = null;
        if (this.zzajQ != 0) {
            drawable = zza(context, zzmd, this.zzajQ);
        }
        if (this.zzajS != null) {
            this.zzajS.onImageLoaded(this.zzajO.uri, drawable, false);
        }
        zza(drawable, z, false, false);
    }

    protected abstract void zza(Drawable drawable, boolean z, boolean z2, boolean z3);

    protected boolean zzb(boolean z, boolean z2) {
        return (this.zzajT && !z2) ? !z || this.zzajU : false;
    }

    public void zzbM(int i) {
        this.zzajQ = i;
    }
}
