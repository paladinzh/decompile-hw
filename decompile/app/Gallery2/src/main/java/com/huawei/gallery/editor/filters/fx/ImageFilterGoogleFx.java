package com.huawei.gallery.editor.filters.fx;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilterFx;

public class ImageFilterGoogleFx extends ImageFilterFx {
    private Bitmap mFxBitmap = null;
    private int mFxBitmapId = 0;
    private FilterGoogleFxRepresentation mParameters = null;
    private Resources mResources = null;

    public void freeResources() {
        super.freeResources();
        if (this.mFxBitmap != null) {
            this.mFxBitmap.recycle();
        }
        this.mFxBitmap = null;
    }

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterGoogleFxRepresentation) {
            this.mParameters = (FilterGoogleFxRepresentation) representation;
        }
    }

    public Bitmap apply(Bitmap bitmap) {
        FilterGoogleFxRepresentation parameters = this.mParameters;
        if (parameters == null || this.mResources == null) {
            return bitmap;
        }
        int bitmapResourceId = parameters.getBitmapResource();
        if (bitmapResourceId == 0) {
            return bitmap;
        }
        if (this.mFxBitmap == null || this.mFxBitmapId != bitmapResourceId) {
            Options o = new Options();
            o.inScaled = false;
            this.mFxBitmapId = bitmapResourceId;
            if (this.mFxBitmapId != 0) {
                this.mFxBitmap = BitmapFactory.decodeResource(this.mResources, this.mFxBitmapId, o);
            } else {
                GalleryLog.w("ImageFilterGoogleFx", "bad resource for filter: " + this.mName);
            }
        }
        if (this.mFxBitmap == null) {
            return bitmap;
        }
        int fxw = this.mFxBitmap.getWidth();
        int fxh = this.mFxBitmap.getHeight();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int stride = w * 4;
        int max = stride * h;
        int increment = stride * 256;
        for (int i = 0; i < max; i += increment) {
            int start = i;
            int end = i + increment;
            if (end > max) {
                end = max;
            }
            if (!getEnvironment().needsStop()) {
                nativeApplyFilter(bitmap, w, h, this.mFxBitmap, fxw, fxh, start, end);
            }
        }
        return bitmap;
    }

    public void setResources(Resources resources) {
        this.mResources = resources;
    }
}
