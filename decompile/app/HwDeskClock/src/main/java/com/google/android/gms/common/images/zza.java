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
import com.google.android.gms.internal.zzkr;
import com.google.android.gms.internal.zzks;
import com.google.android.gms.internal.zzkt;
import com.google.android.gms.internal.zzku;
import java.lang.ref.WeakReference;

/* compiled from: Unknown */
public abstract class zza {
    final zza zzabR;
    protected int zzabT;
    protected boolean zzabU;
    protected OnImageLoadedListener zzabV;
    private boolean zzabW;
    private boolean zzabX;
    protected int zzabZ;

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
        private WeakReference<ImageView> zzaca;

        private void zza(ImageView imageView, Drawable drawable, boolean z, boolean z2, boolean z3) {
            Uri uri = null;
            if (z2 || z3) {
                Object obj = null;
            } else {
                int i = 1;
            }
            if (obj != null && (imageView instanceof zzkt)) {
                int zzog = ((zzkt) imageView).zzog();
                if (this.zzabT != 0 && zzog == this.zzabT) {
                    return;
                }
            }
            boolean zzb = zzb(z, z2);
            Drawable newDrawable = (this.zzabU && drawable != null) ? drawable.getConstantState().newDrawable() : drawable;
            if (zzb) {
                newDrawable = zza(imageView.getDrawable(), newDrawable);
            }
            imageView.setImageDrawable(newDrawable);
            if (imageView instanceof zzkt) {
                zzkt zzkt = (zzkt) imageView;
                if (z3) {
                    uri = this.zzabR.uri;
                }
                zzkt.zzj(uri);
                zzkt.zzbv(obj == null ? 0 : this.zzabT);
            }
            if (zzb) {
                ((zzkr) newDrawable).startTransition(250);
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof zzb)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            ImageView imageView = (ImageView) this.zzaca.get();
            ImageView imageView2 = (ImageView) ((zzb) obj).zzaca.get();
            boolean z = (imageView2 == null || imageView == null || !zzw.equal(imageView2, imageView)) ? false : true;
            return z;
        }

        public int hashCode() {
            return 0;
        }

        protected void zza(Drawable drawable, boolean z, boolean z2, boolean z3) {
            ImageView imageView = (ImageView) this.zzaca.get();
            if (imageView != null) {
                zza(imageView, drawable, z, z2, z3);
            }
        }
    }

    /* compiled from: Unknown */
    public static final class zzc extends zza {
        private WeakReference<OnImageLoadedListener> zzacb;

        public boolean equals(Object obj) {
            if (!(obj instanceof zzc)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            zzc zzc = (zzc) obj;
            OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.zzacb.get();
            OnImageLoadedListener onImageLoadedListener2 = (OnImageLoadedListener) zzc.zzacb.get();
            boolean z = onImageLoadedListener2 != null && onImageLoadedListener != null && zzw.equal(onImageLoadedListener2, onImageLoadedListener) && zzw.equal(zzc.zzabR, this.zzabR);
            return z;
        }

        public int hashCode() {
            return zzw.hashCode(this.zzabR);
        }

        protected void zza(Drawable drawable, boolean z, boolean z2, boolean z3) {
            if (!z2) {
                OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.zzacb.get();
                if (onImageLoadedListener != null) {
                    onImageLoadedListener.onImageLoaded(this.zzabR.uri, drawable, z3);
                }
            }
        }
    }

    private Drawable zza(Context context, zzku zzku, int i) {
        Resources resources = context.getResources();
        if (this.zzabZ <= 0) {
            return resources.getDrawable(i);
        }
        com.google.android.gms.internal.zzku.zza zza = new com.google.android.gms.internal.zzku.zza(i, this.zzabZ);
        Drawable drawable = (Drawable) zzku.get(zza);
        if (drawable == null) {
            drawable = resources.getDrawable(i);
            if ((this.zzabZ & 1) != 0) {
                drawable = zza(resources, drawable);
            }
            zzku.put(zza, drawable);
        }
        return drawable;
    }

    protected Drawable zza(Resources resources, Drawable drawable) {
        return zzks.zza(resources, drawable);
    }

    protected zzkr zza(Drawable drawable, Drawable drawable2) {
        if (drawable == null) {
            drawable = null;
        } else if (drawable instanceof zzkr) {
            drawable = ((zzkr) drawable).zzoe();
        }
        return new zzkr(drawable, drawable2);
    }

    void zza(Context context, Bitmap bitmap, boolean z) {
        com.google.android.gms.common.internal.zzb.zzr(bitmap);
        if ((this.zzabZ & 1) != 0) {
            bitmap = zzks.zza(bitmap);
        }
        Drawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
        if (this.zzabV != null) {
            this.zzabV.onImageLoaded(this.zzabR.uri, bitmapDrawable, true);
        }
        zza(bitmapDrawable, z, false, true);
    }

    void zza(Context context, zzku zzku, boolean z) {
        Drawable drawable = null;
        if (this.zzabT != 0) {
            drawable = zza(context, zzku, this.zzabT);
        }
        if (this.zzabV != null) {
            this.zzabV.onImageLoaded(this.zzabR.uri, drawable, false);
        }
        zza(drawable, z, false, false);
    }

    protected abstract void zza(Drawable drawable, boolean z, boolean z2, boolean z3);

    protected boolean zzb(boolean z, boolean z2) {
        return (this.zzabW && !z2) ? !z || this.zzabX : false;
    }
}
