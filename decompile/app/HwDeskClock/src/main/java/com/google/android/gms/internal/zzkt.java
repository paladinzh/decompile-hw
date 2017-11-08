package com.google.android.gms.internal;

import android.graphics.Canvas;
import android.graphics.Path;
import android.net.Uri;
import android.widget.ImageView;

/* compiled from: Unknown */
public final class zzkt extends ImageView {
    private Uri zzacu;
    private int zzacv;
    private int zzacw;
    private zza zzacx;
    private int zzacy;
    private float zzacz;

    /* compiled from: Unknown */
    public interface zza {
        Path zzl(int i, int i2);
    }

    protected void onDraw(Canvas canvas) {
        if (this.zzacx != null) {
            canvas.clipPath(this.zzacx.zzl(getWidth(), getHeight()));
        }
        super.onDraw(canvas);
        if (this.zzacw != 0) {
            canvas.drawColor(this.zzacw);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight;
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        switch (this.zzacy) {
            case 1:
                measuredHeight = getMeasuredHeight();
                i = (int) (((float) measuredHeight) * this.zzacz);
                break;
            case 2:
                i = getMeasuredWidth();
                measuredHeight = (int) (((float) i) / this.zzacz);
                break;
            default:
                return;
        }
        setMeasuredDimension(i, measuredHeight);
    }

    public void zzbv(int i) {
        this.zzacv = i;
    }

    public void zzj(Uri uri) {
        this.zzacu = uri;
    }

    public int zzog() {
        return this.zzacv;
    }
}
