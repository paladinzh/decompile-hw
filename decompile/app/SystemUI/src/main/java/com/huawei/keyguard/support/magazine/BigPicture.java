package com.huawei.keyguard.support.magazine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;

public class BigPicture {
    Bitmap mBitmap;
    private BokehDrawable mBokehDrawable = null;
    public BigPictureInfo mInfo = new BigPictureInfo();
    Bitmap mLandBitmap;

    public void set(BigPictureInfo info, Bitmap bmp) {
        set(info);
        set(bmp);
    }

    public void set(BigPictureInfo info) {
        if (info != null) {
            this.mInfo = info;
        }
    }

    public BigPictureInfo getBigPictureInfo() {
        return this.mInfo;
    }

    public String getPicPath() {
        return this.mInfo.getPicPath();
    }

    public void set(Bitmap bmp) {
        if (this.mBitmap != null) {
            this.mBitmap = null;
        }
        this.mBitmap = bmp;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public Bitmap getLandBitmap() {
        return this.mLandBitmap;
    }

    public void setLandBmp(Bitmap landbmp) {
        if (this.mLandBitmap != null) {
            this.mLandBitmap.recycle();
            this.mLandBitmap = null;
        }
        this.mLandBitmap = landbmp;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BigPicture : ").append(super.toString()).append("title = ").append(this.mInfo.getTitle()).append(", content = ").append(this.mInfo.getContent()).append(", Bitmap = ").append(this.mBitmap).append(", LandBitmap = ").append(this.mLandBitmap);
        return sb.toString();
    }

    public BokehDrawable getBokehDrawable(Context context) {
        if (this.mBokehDrawable != null) {
            return this.mBokehDrawable;
        }
        if (HwUnlockUtils.isLandscape(context)) {
            if (getLandBitmap() != null) {
                setBokehDrawable(BokehDrawable.create(context, getLandBitmap()));
                return this.mBokehDrawable;
            }
        } else if (getBitmap() != null) {
            setBokehDrawable(BokehDrawable.create(context, getBitmap()));
            return this.mBokehDrawable;
        }
        return null;
    }

    public void setBokehDrawable(BokehDrawable bokehDrawable) {
        this.mBokehDrawable = bokehDrawable;
        if (this.mBokehDrawable != null) {
            this.mBokehDrawable.setBitmapPath(getPicPath());
        }
    }

    public boolean isSameDrawable(Drawable dr, Context context) {
        boolean z = false;
        if (HwUnlockUtils.isLandscape(context)) {
            if (this.mLandBitmap != null && (dr instanceof BitmapDrawable)) {
                z = ((BitmapDrawable) dr).getBitmap().equals(this.mLandBitmap);
            }
            return z;
        }
        if (this.mBitmap != null && (dr instanceof BitmapDrawable)) {
            z = ((BitmapDrawable) dr).getBitmap().equals(this.mBitmap);
        }
        return z;
    }
}
