package com.google.android.gms.common.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.images.ImageManager.OnImageLoadedListener;
import com.google.android.gms.internal.ea;
import com.google.android.gms.internal.eb;
import com.google.android.gms.internal.ec;
import com.google.android.gms.internal.ed;
import com.google.android.gms.internal.ep;
import com.google.android.gms.internal.fr;
import java.lang.ref.WeakReference;

/* compiled from: Unknown */
public final class a {
    final a AF;
    private int AH;
    int AI;
    private int AJ;
    private WeakReference<OnImageLoadedListener> AK;
    private WeakReference<ImageView> AL;
    private WeakReference<TextView> AM;
    private int AN;
    private boolean AO;
    private boolean AP;
    private int AQ;

    /* compiled from: Unknown */
    public static final class a {
        public final Uri uri;

        public a(Uri uri) {
            this.uri = uri;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof a)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (((a) obj).hashCode() == hashCode()) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return ep.hashCode(this.uri);
        }
    }

    private ea a(Drawable drawable, Drawable drawable2) {
        if (drawable == null) {
            drawable = null;
        } else if (drawable instanceof ea) {
            drawable = ((ea) drawable).dO();
        }
        return new ea(drawable, drawable2);
    }

    private void a(Drawable drawable, boolean z, boolean z2, boolean z3) {
        switch (this.AI) {
            case 1:
                if (!z2) {
                    OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.AK.get();
                    if (onImageLoadedListener != null) {
                        onImageLoadedListener.onImageLoaded(this.AF.uri, drawable, z3);
                        return;
                    }
                    return;
                }
                return;
            case 2:
                ImageView imageView = (ImageView) this.AL.get();
                if (imageView != null) {
                    a(imageView, drawable, z, z2, z3);
                    return;
                }
                return;
            case 3:
                TextView textView = (TextView) this.AM.get();
                if (textView != null) {
                    a(textView, this.AN, drawable, z, z2);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void a(ImageView imageView, Drawable drawable, boolean z, boolean z2, boolean z3) {
        if (z2 || z3) {
            Object obj = null;
        } else {
            int i = 1;
        }
        if (obj != null && (imageView instanceof ec)) {
            int dQ = ((ec) imageView).dQ();
            if (this.AH != 0 && dQ == this.AH) {
                return;
            }
        }
        boolean b = b(z, z2);
        Drawable a = !b ? drawable : a(imageView.getDrawable(), drawable);
        imageView.setImageDrawable(a);
        if (imageView instanceof ec) {
            ec ecVar = (ec) imageView;
            ecVar.d(!z3 ? null : this.AF.uri);
            ecVar.N(obj == null ? 0 : this.AH);
        }
        if (b) {
            ((ea) a).startTransition(250);
        }
    }

    private void a(TextView textView, int i, Drawable drawable, boolean z, boolean z2) {
        boolean b = b(z, z2);
        Drawable[] compoundDrawables = !fr.eO() ? textView.getCompoundDrawables() : textView.getCompoundDrawablesRelative();
        Drawable a = !b ? drawable : a(compoundDrawables[i], drawable);
        Drawable drawable2 = i != 0 ? compoundDrawables[0] : a;
        Drawable drawable3 = i != 1 ? compoundDrawables[1] : a;
        Drawable drawable4 = i != 2 ? compoundDrawables[2] : a;
        Drawable drawable5 = i != 3 ? compoundDrawables[3] : a;
        if (fr.eO()) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable2, drawable3, drawable4, drawable5);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable2, drawable3, drawable4, drawable5);
        }
        if (b) {
            ((ea) a).startTransition(250);
        }
    }

    private boolean b(boolean z, boolean z2) {
        return (this.AO && !z2) ? !z || this.AP : false;
    }

    void a(Context context, Bitmap bitmap, boolean z) {
        ed.d(bitmap);
        if ((this.AQ & 1) != 0) {
            bitmap = eb.a(bitmap);
        }
        a(new BitmapDrawable(context.getResources(), bitmap), z, false, true);
    }

    void b(Context context, boolean z) {
        Drawable drawable = null;
        if (this.AH != 0) {
            Resources resources = context.getResources();
            drawable = resources.getDrawable(this.AH);
            if ((this.AQ & 1) != 0) {
                drawable = eb.a(resources, drawable);
            }
        }
        a(drawable, z, false, false);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof a)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (((a) obj).hashCode() == hashCode()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.AJ;
    }
}
