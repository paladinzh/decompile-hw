package com.huawei.keyguard.view.charge.e50;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.android.keyguard.R$id;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.view.charge.ChargingAnimController;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;

public class ChargeEffectsLayerViewE50 extends FrameLayout {
    private UnlockChargingView animateView;

    public ChargeEffectsLayerViewE50(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Drawable drawable = KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper();
        if (drawable instanceof BokehDrawable) {
            Bitmap map = ((BokehDrawable) drawable).getBlurBitmap();
            if (map != null) {
                setBackground(new BitmapDrawable(this.mContext.getResources(), map));
            }
        } else if (drawable instanceof BitmapDrawable) {
            setBackground(drawable);
        } else {
            setBackgroundColor(-16777216);
        }
        this.animateView = (UnlockChargingView) findViewById(R$id.unlock_charging_view);
        FrameLayout backgroundView = (FrameLayout) findViewById(R$id.charge_shadow_effect_layer_e50);
        if (backgroundView != null) {
            backgroundView.setBackgroundColor(-1090519040);
        }
        if (this.animateView != null) {
            this.animateView.setVisibility(0);
            this.animateView.startRender(450, BatteryStateInfo.getInst().getChargingMode(), BatteryStateInfo.getInst().getChargeLevel());
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

    public UnlockChargingView getAnimateView() {
        return this.animateView;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & event.getActionMasked()) {
            case 1:
            case 6:
                ChargingAnimController.getInst(this.mContext).removeChargingView();
                break;
        }
        return true;
    }
}
