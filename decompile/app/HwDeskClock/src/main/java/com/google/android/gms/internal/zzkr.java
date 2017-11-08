package com.google.android.gms.internal;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.SystemClock;

/* compiled from: Unknown */
public final class zzkr extends Drawable implements Callback {
    private int mFrom;
    private long zzMS;
    private boolean zzabW;
    private int zzacd;
    private int zzace;
    private int zzacf;
    private int zzacg;
    private int zzach;
    private boolean zzaci;
    private zzb zzacj;
    private Drawable zzack;
    private Drawable zzacl;
    private boolean zzacm;
    private boolean zzacn;
    private boolean zzaco;
    private int zzacp;

    /* compiled from: Unknown */
    private static final class zza extends Drawable {
        private static final zza zzacq = new zza();
        private static final zza zzacr = new zza();

        /* compiled from: Unknown */
        private static final class zza extends ConstantState {
            private zza() {
            }

            public int getChangingConfigurations() {
                return 0;
            }

            public Drawable newDrawable() {
                return zza.zzacq;
            }
        }

        private zza() {
        }

        public void draw(Canvas canvas) {
        }

        public ConstantState getConstantState() {
            return zzacr;
        }

        public int getOpacity() {
            return -2;
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter cf) {
        }
    }

    /* compiled from: Unknown */
    static final class zzb extends ConstantState {
        int zzacs;
        int zzact;

        zzb(zzb zzb) {
            if (zzb != null) {
                this.zzacs = zzb.zzacs;
                this.zzact = zzb.zzact;
            }
        }

        public int getChangingConfigurations() {
            return this.zzacs;
        }

        public Drawable newDrawable() {
            return new zzkr(this);
        }
    }

    public zzkr(Drawable drawable, Drawable drawable2) {
        this(null);
        if (drawable == null) {
            drawable = zza.zzacq;
        }
        this.zzack = drawable;
        drawable.setCallback(this);
        zzb zzb = this.zzacj;
        zzb.zzact |= drawable.getChangingConfigurations();
        if (drawable2 == null) {
            drawable2 = zza.zzacq;
        }
        this.zzacl = drawable2;
        drawable2.setCallback(this);
        zzb = this.zzacj;
        zzb.zzact |= drawable2.getChangingConfigurations();
    }

    zzkr(zzb zzb) {
        this.zzacd = 0;
        this.zzacf = 255;
        this.zzach = 0;
        this.zzabW = true;
        this.zzacj = new zzb(zzb);
    }

    public boolean canConstantState() {
        boolean z = false;
        if (!this.zzacm) {
            if (!(this.zzack.getConstantState() == null || this.zzacl.getConstantState() == null)) {
                z = true;
            }
            this.zzacn = z;
            this.zzacm = true;
        }
        return this.zzacn;
    }

    public void draw(Canvas canvas) {
        int i = 1;
        switch (this.zzacd) {
            case 1:
                this.zzMS = SystemClock.uptimeMillis();
                this.zzacd = 2;
                i = 0;
                break;
            case 2:
                if ((this.zzMS < 0 ? 1 : 0) == 0) {
                    float uptimeMillis = ((float) (SystemClock.uptimeMillis() - this.zzMS)) / ((float) this.zzacg);
                    if (uptimeMillis < 1.0f) {
                        i = 0;
                    }
                    if (i != 0) {
                        this.zzacd = 0;
                    }
                    this.zzach = (int) ((Math.min(uptimeMillis, 1.0f) * ((float) (this.zzace - this.mFrom))) + ((float) this.mFrom));
                    break;
                }
                break;
        }
        int i2 = this.zzach;
        boolean z = this.zzabW;
        Drawable drawable = this.zzack;
        Drawable drawable2 = this.zzacl;
        if (i == 0) {
            if (z) {
                drawable.setAlpha(this.zzacf - i2);
            }
            drawable.draw(canvas);
            if (z) {
                drawable.setAlpha(this.zzacf);
            }
            if (i2 > 0) {
                drawable2.setAlpha(i2);
                drawable2.draw(canvas);
                drawable2.setAlpha(this.zzacf);
            }
            invalidateSelf();
            return;
        }
        if (!z || i2 == 0) {
            drawable.draw(canvas);
        }
        if (i2 == this.zzacf) {
            drawable2.setAlpha(this.zzacf);
            drawable2.draw(canvas);
        }
    }

    public int getChangingConfigurations() {
        return (super.getChangingConfigurations() | this.zzacj.zzacs) | this.zzacj.zzact;
    }

    public ConstantState getConstantState() {
        if (!canConstantState()) {
            return null;
        }
        this.zzacj.zzacs = getChangingConfigurations();
        return this.zzacj;
    }

    public int getIntrinsicHeight() {
        return Math.max(this.zzack.getIntrinsicHeight(), this.zzacl.getIntrinsicHeight());
    }

    public int getIntrinsicWidth() {
        return Math.max(this.zzack.getIntrinsicWidth(), this.zzacl.getIntrinsicWidth());
    }

    public int getOpacity() {
        if (!this.zzaco) {
            this.zzacp = Drawable.resolveOpacity(this.zzack.getOpacity(), this.zzacl.getOpacity());
            this.zzaco = true;
        }
        return this.zzacp;
    }

    public void invalidateDrawable(Drawable who) {
        if (zzlv.zzpQ()) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.invalidateDrawable(this);
            }
        }
    }

    public Drawable mutate() {
        if (!this.zzaci && super.mutate() == this) {
            if (canConstantState()) {
                this.zzack.mutate();
                this.zzacl.mutate();
                this.zzaci = true;
            } else {
                throw new IllegalStateException("One or more children of this LayerDrawable does not have constant state; this drawable cannot be mutated.");
            }
        }
        return this;
    }

    protected void onBoundsChange(Rect bounds) {
        this.zzack.setBounds(bounds);
        this.zzacl.setBounds(bounds);
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (zzlv.zzpQ()) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.scheduleDrawable(this, what, when);
            }
        }
    }

    public void setAlpha(int alpha) {
        if (this.zzach == this.zzacf) {
            this.zzach = alpha;
        }
        this.zzacf = alpha;
        invalidateSelf();
    }

    public void setColorFilter(ColorFilter cf) {
        this.zzack.setColorFilter(cf);
        this.zzacl.setColorFilter(cf);
    }

    public void startTransition(int durationMillis) {
        this.mFrom = 0;
        this.zzace = this.zzacf;
        this.zzach = 0;
        this.zzacg = durationMillis;
        this.zzacd = 1;
        invalidateSelf();
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (zzlv.zzpQ()) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.unscheduleDrawable(this, what);
            }
        }
    }

    public Drawable zzoe() {
        return this.zzacl;
    }
}
