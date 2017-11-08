package com.common.imageloader.core.display;

import android.graphics.Bitmap;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import com.common.imageloader.core.assist.LoadedFrom;
import com.common.imageloader.core.display.RoundedBitmapDisplayer.RoundedDrawable;
import com.common.imageloader.core.imageaware.ImageAware;
import com.common.imageloader.core.imageaware.ImageViewAware;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;

public class RoundedVignetteBitmapDisplayer extends RoundedBitmapDisplayer {

    protected static class RoundedVignetteDrawable extends RoundedDrawable {
        RoundedVignetteDrawable(Bitmap bitmap, int cornerRadius, int margin) {
            super(bitmap, cornerRadius, margin);
        }

        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            RadialGradient vignette = new RadialGradient(this.mRect.centerX(), (this.mRect.centerY() * Utility.ALPHA_MAX) / 0.7f, this.mRect.centerX() * 1.3f, new int[]{0, 0, GlobalContext.getContext().getResources().getColor(R.color.emui_list_secondray_text)}, new float[]{0.0f, 0.7f, Utility.ALPHA_MAX}, TileMode.CLAMP);
            Matrix oval = new Matrix();
            oval.setScale(Utility.ALPHA_MAX, 0.7f);
            vignette.setLocalMatrix(oval);
            this.paint.setShader(new ComposeShader(this.bitmapShader, vignette, Mode.SRC_OVER));
        }
    }

    public RoundedVignetteBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
        super(cornerRadiusPixels, marginPixels);
    }

    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (imageAware instanceof ImageViewAware) {
            imageAware.setImageDrawable(new RoundedVignetteDrawable(bitmap, this.cornerRadius, this.margin));
            return;
        }
        throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
    }
}
