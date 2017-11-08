package com.huawei.keyguard.wallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import com.huawei.anim.visualeffect.SensorEffect;
import com.huawei.anim.visualeffect.SensorEffect.Listener;
import com.huawei.anim.visualeffect.parallax.ParallaxEffect;

public class DrawableFrame extends FrameLayout {
    private static final float[] RESET_ARRAY = new float[]{0.5f, 0.5f};
    static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private final Drawable mBackgroundDrawable = new Drawable() {
        public void draw(Canvas canvas) {
            if (DrawableFrame.this.mCustomBackground != null) {
                Rect bounds = DrawableFrame.this.mCustomBackground.getBounds();
                if (bounds.width() == 0) {
                    DrawableFrame.this.computeCustomBackgroundBounds();
                }
                int vWidth = DrawableFrame.this.getWidth();
                int vHeight = DrawableFrame.this.getHeight();
                int restore = canvas.save();
                canvas.scale(DrawableFrame.this.mCanvasScaleX, DrawableFrame.this.mCanvasScaleY, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
                if (!DrawableFrame.this.mIsWallpaperClamp) {
                    canvas.translate(((float) (-(bounds.width() - vWidth))) / 2.0f, ((float) (-(bounds.height() - vHeight))) / 2.0f);
                }
                if (DrawableFrame.this.mParallaxEffect != null) {
                    Canvas canvas2 = canvas;
                    canvas2.translate(-((float) ((DrawableFrame.this.mParallaxOffsetX - ((double) DrawableFrame.RESET_ARRAY[0])) * (((double) bounds.width()) * 0.10000000000000009d))), -((float) ((DrawableFrame.this.mParallaxOffsetY - ((double) DrawableFrame.RESET_ARRAY[1])) * (((double) bounds.height()) * 0.10000000000000009d))));
                    canvas.scale(1.1f, 1.1f, ((float) canvas.getWidth()) / 2.0f, ((float) canvas.getHeight()) / 2.0f);
                }
                DrawableFrame.this.mCustomBackground.draw(canvas);
                DrawableFrame.this.mMaskDrawable.draw(canvas);
                canvas.restoreToCount(restore);
                return;
            }
            DrawableFrame.this.mBlackDrawable.draw(canvas);
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter cf) {
        }

        public int getOpacity() {
            return -3;
        }
    };
    private Drawable mBlackDrawable = new ColorDrawable(-16776961);
    private float mCanvasScaleX = 1.0f;
    private float mCanvasScaleY = 1.0f;
    private Drawable mCustomBackground;
    private boolean mIsInPowerSave = false;
    private boolean mIsWallpaperClamp = false;
    Drawable mMaskDrawable = new ColorDrawable(-16777216);
    private SensorEffect mParallaxEffect;
    private final Listener mParallaxListener = new Listener() {
        public void onChanged(int type, float[] values) {
            if (type == 500) {
                DrawableFrame.this.mParallaxOffsetX = (double) values[0];
                DrawableFrame.this.mParallaxOffsetY = (double) values[1];
                DrawableFrame.this.invalidate();
            }
        }
    };
    private double mParallaxOffsetX = ((double) RESET_ARRAY[0]);
    private double mParallaxOffsetY = ((double) RESET_ARRAY[1]);
    private boolean mParallaxOpen = false;
    private ColorFilter mPowerSaveFilter = null;

    public DrawableFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private synchronized void setupParallaxEffect(Context context) {
        this.mParallaxEffect = new ParallaxEffect(context);
    }

    private void computeCustomBackgroundBounds() {
        if (this.mCustomBackground != null && isLaidOut()) {
            int bgWidth = this.mCustomBackground.getIntrinsicWidth();
            int bgHeight = this.mCustomBackground.getIntrinsicHeight();
            int vWidth = getWidth();
            int vHeight = getHeight();
            float bgAspect = ((float) bgWidth) / ((float) bgHeight);
            if (bgAspect > ((float) vWidth) / ((float) vHeight)) {
                this.mCustomBackground.setBounds(0, 0, (int) (((float) vHeight) * bgAspect), vHeight);
                this.mMaskDrawable.setBounds(0, 0, (int) (((float) vHeight) * bgAspect), vHeight);
            } else {
                this.mCustomBackground.setBounds(0, 0, vWidth, (int) (((float) vWidth) / bgAspect));
                this.mMaskDrawable.setBounds(0, 0, vWidth, (int) (((float) vWidth) / bgAspect));
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeCustomBackgroundBounds();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
        this.mMaskDrawable.setAlpha(51);
    }

    private void init() {
        setBackground(this.mBackgroundDrawable);
        if (this.mParallaxOpen) {
            setupParallaxEffect(this.mContext);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}
