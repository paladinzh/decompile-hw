package com.huawei.keyguard.view.charge.e40;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.android.keyguard.R$id;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;

public class ChargeEffectsLayerViewE40 extends FrameLayout {
    private ChargeAnimView mChargeAnimView;

    public ChargeEffectsLayerViewE40(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mChargeAnimView = (ChargeAnimView) findViewById(R$id.charge_anim);
        if (this.mChargeAnimView != null) {
            this.mChargeAnimView.setChargeEffectsLayerView(this);
        }
        Drawable drawable = KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper();
        if (drawable instanceof BokehDrawable) {
            Bitmap map = ((BokehDrawable) drawable).getBlurBitmap();
            if (map != null) {
                setBackground(new BitmapDrawable(this.mContext.getResources(), map));
            }
        } else if (drawable instanceof BitmapDrawable) {
            setBackground(drawable);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable background = getBackground();
        if (background != null) {
            background.setAlpha((int) (getAlpha() * 225.0f));
            background.draw(canvas);
        }
    }

    public void startAnim() {
        setVisibility(0);
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(1000);
        alpha.setInterpolator(new LinearInterpolator());
        startAnimation(alpha);
    }
}
