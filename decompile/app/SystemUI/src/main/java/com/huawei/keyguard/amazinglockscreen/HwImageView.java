package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenView;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ImageCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LayoutCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$MaskCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$VisibilityCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import com.huawei.keyguard.amazinglockscreen.data.HwResManager;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class HwImageView extends ImageView implements HwUnlockInterface$HwLockScreenView, HwUnlockInterface$ImageCallback, HwUnlockInterface$LayoutCallback, HwUnlockInterface$VisibilityCallback, HwUnlockInterface$ConditionCallback, HwUnlockInterface$MaskCallback {
    private static final int[] TIME_DIGIT_DRAWABLE_ID = new int[]{R$drawable.number_0, R$drawable.number_1, R$drawable.number_2, R$drawable.number_3, R$drawable.number_4, R$drawable.number_5, R$drawable.number_6, R$drawable.number_7, R$drawable.number_8, R$drawable.number_9};
    private boolean mAnimationStart;
    private String mCommandName;
    private HwViewProperty mContent;
    private AnimationDrawable mFrameAnimation;
    private HwViewProperty mH;
    private Handler mHandler;
    private ArrayList<HwAnimationSet> mHwAnimations;
    private String mId;
    private String mImageName;
    private Bitmap mMaskBitmap;
    private Paint mMaskPaint;
    private HwViewProperty mMaskX;
    private HwViewProperty mMaskY;
    private String mMasktype;
    private PorterDuffXfermode mPorterDuffXfermode;
    private PowerManager mPowerManager;
    private HwViewProperty mVisiblity;
    private HwViewProperty mW;
    private HwViewProperty mX;
    private HwViewProperty mY;

    public HwImageView(Context context) {
        super(context);
        this.mAnimationStart = false;
        this.mPowerManager = null;
        this.mMasktype = BuildConfig.FLAVOR;
        this.mImageName = BuildConfig.FLAVOR;
        this.mCommandName = BuildConfig.FLAVOR;
        this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        this.mHwAnimations = new ArrayList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (HwImageView.this.mAnimationStart) {
                            HwImageView.this.clearAnimation();
                            if (HwImageView.this.mFrameAnimation != null) {
                                HwImageView.this.mFrameAnimation.stop();
                            }
                            Animation anim = msg.obj;
                            if (anim != null && HwImageView.this.mPowerManager.isScreenOn()) {
                                HwImageView.this.startAnimation(anim);
                            }
                            if (HwImageView.this.mFrameAnimation != null && HwImageView.this.mPowerManager.isScreenOn()) {
                                HwImageView.this.mFrameAnimation.start();
                            }
                            if (!HwImageView.this.mPowerManager.isScreenOn()) {
                                HwImageView.this.mAnimationStart = false;
                                return;
                            }
                            return;
                        }
                        return;
                    case 1:
                        HwImageView.this.clearAnimation();
                        if (HwImageView.this.mFrameAnimation != null) {
                            HwImageView.this.mFrameAnimation.selectDrawable(0);
                            HwImageView.this.mFrameAnimation.stop();
                            return;
                        }
                        return;
                    case 256:
                        Bitmap bm = HwResManager.getInstance().getSourceBitmap(msg.obj.toString(), HwImageView.this.mContext.getApplicationContext(), HwImageView.this.mHandler, false);
                        if (bm != null) {
                            byte[] chunk = bm.getNinePatchChunk();
                            if (NinePatch.isNinePatchChunk(chunk)) {
                                HwImageView.this.setBackgroundDrawable(new NinePatchDrawable(HwImageView.this.getContext().getResources(), new NinePatch(bm, chunk, null)));
                            } else if (msg.arg1 == 0) {
                                HwImageView.this.setImageBitmap(bm);
                            } else {
                                HwImageView.this.mMaskBitmap = bm;
                                HwImageView.this.mMaskPaint = new Paint();
                                HwImageView.this.mMaskPaint.setFilterBitmap(false);
                                HwImageView.this.setMaskMode();
                            }
                        }
                        HwImageView.this.invalidate();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void setMaskMode() {
        Mode mode = Mode.DST_ATOP;
        if (this.mMasktype != null) {
            try {
                mode = Mode.valueOf(this.mMasktype);
            } catch (IllegalArgumentException e) {
                HwLog.w("HwImageView", "Illegal masktype parameter!");
            }
        }
        this.mPorterDuffXfermode = new PorterDuffXfermode(mode);
    }

    public void setContent(String strContent) {
        if (strContent.contains("system.")) {
            this.mContent = new HwViewProperty(getContext(), strContent, HwUnlockConstants$ViewPropertyType.TYPE_DRAWABLE, this);
        } else {
            setImageBitmapSrc(strContent, false);
        }
    }

    public void setMaskType(String type) {
        this.mMasktype = type;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setImageBitmapSrc(String src) {
        setImageBitmapSrc(src, false);
    }

    public void setImageBitmapSrc(String src, boolean isMask) {
        Bitmap bm = HwResManager.getInstance().getSourceBitmap(src, this.mContext.getApplicationContext(), this.mHandler, isMask);
        if (bm != null) {
            byte[] chunk = bm.getNinePatchChunk();
            if (NinePatch.isNinePatchChunk(chunk)) {
                setBackgroundDrawable(new NinePatchDrawable(getContext().getResources(), new NinePatch(bm, chunk, null)));
            } else if (isMask) {
                this.mMaskBitmap = bm;
                this.mMaskPaint = new Paint();
                this.mMaskPaint.setFilterBitmap(false);
                setMaskMode();
            } else {
                setImageBitmap(bm);
            }
        }
    }

    public void setLayout(String x, String y, String w, String h) {
        this.mX = new HwViewProperty(getContext(), x, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
        this.mY = new HwViewProperty(getContext(), y, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
        this.mW = new HwViewProperty(getContext(), w, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
        this.mH = new HwViewProperty(getContext(), h, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
    }

    public void onLayoutChanged() {
        layout();
    }

    public void layout() {
        int l = ((Integer) this.mX.getValue()).intValue();
        int t = ((Integer) this.mY.getValue()).intValue();
        layout(l, t, l + ((Integer) this.mW.getValue()).intValue(), t + ((Integer) this.mH.getValue()).intValue());
    }

    public void setVisiblityProp(String visible) {
        this.mVisiblity = new HwViewProperty(getContext(), visible, HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY, this);
        refreshVisibility(((Boolean) this.mVisiblity.getValue()).booleanValue());
    }

    public void refreshVisibility(boolean result) {
        int i;
        if (result) {
            startAnimation();
        } else {
            stopAnimation();
        }
        if (result) {
            i = 0;
        } else {
            i = 4;
        }
        setVisibility(i);
    }

    public void addHwAnimation(final HwAnimationSet animation) {
        HwAnimationSet animationSet = animation;
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (-1 != animation.mInterval) {
                    if (HwImageView.this.mHandler.hasMessages(0)) {
                        HwImageView.this.mHandler.removeMessages(0);
                    }
                    Message msg = Message.obtain();
                    msg.obj = animation;
                    msg.what = 0;
                    HwImageView.this.mHandler.sendMessageDelayed(msg, animation.mInterval);
                }
            }
        });
        this.mHwAnimations.add(animation);
    }

    public void setFrameAnimation(AnimationDrawable frameAnimation) {
        this.mFrameAnimation = frameAnimation;
        setBackgroundDrawable(this.mFrameAnimation);
    }

    public void setMaskPositionX(String x) {
        this.mMaskX = new HwViewProperty(getContext(), x, HwUnlockConstants$ViewPropertyType.TYPE_MASK, this);
    }

    public void setMaskPositionY(String y) {
        this.mMaskY = new HwViewProperty(getContext(), y, HwUnlockConstants$ViewPropertyType.TYPE_MASK, this);
    }

    public void onMaskChanged() {
        invalidate();
    }

    public void setImageName(String name) {
        this.mImageName = name;
    }

    public String getImageName() {
        return this.mImageName;
    }

    public void refreshTrigger(boolean value) {
        int i;
        HwLog.d("HwImageView", "refreshTrigger condition:  value : " + value);
        if (value && !this.mAnimationStart) {
            startAnimation();
        } else if (!value) {
            stopAnimation();
        }
        if (value) {
            i = 0;
        } else {
            i = 4;
        }
        setVisibility(i);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mMaskBitmap == null) {
            super.onDraw(canvas);
            return;
        }
        Matrix matrix;
        try {
            Field field = ImageView.class.getDeclaredField("mDrawMatrix");
            field.setAccessible(true);
            matrix = (Matrix) field.get(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            matrix = null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            matrix = null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            matrix = null;
        }
        if (getDrawable() != null) {
            Bitmap img = ((BitmapDrawable) getDrawable()).getBitmap();
            if (img != null) {
                int sc = canvas.saveLayer(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), null, 31);
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
                if (matrix != null) {
                    canvas.concat(matrix);
                }
                canvas.drawBitmap(img, 0.0f, 0.0f, this.mMaskPaint);
                this.mMaskPaint.setXfermode(this.mPorterDuffXfermode);
                int mx = 0;
                int my = 0;
                if (this.mMaskX != null) {
                    mx = ((Integer) this.mMaskX.getValue()).intValue();
                }
                if (this.mMaskY != null) {
                    my = ((Integer) this.mMaskY.getValue()).intValue();
                }
                float scale = AmazingUtils.getScalePara();
                if (!(scale == 1.0f || scale == 0.0f)) {
                    my = (int) (((float) my) / scale);
                    mx = (int) (((float) mx) / scale);
                }
                Canvas canvas2 = canvas;
                canvas2.translate((float) mx, (float) my);
                canvas.drawBitmap(this.mMaskBitmap, 0.0f, 0.0f, this.mMaskPaint);
                this.mMaskPaint.setXfermode(null);
                canvas.restoreToCount(sc);
            }
        }
    }

    protected void onDetachedFromWindow() {
        setBackground(null);
        setImageDrawable(null);
        this.mMaskPaint = null;
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        clearAnimation();
        if (this.mFrameAnimation != null) {
            int imageCnt = this.mFrameAnimation.getNumberOfFrames();
            for (int i = 0; i < imageCnt; i++) {
                Drawable drawable = this.mFrameAnimation.getFrame(i);
                if (drawable instanceof HwAsyncDrawable) {
                    ((HwAsyncDrawable) drawable).releaseBitmap();
                }
            }
            this.mFrameAnimation.stop();
        }
        if (this.mHwAnimations != null) {
            for (HwAnimationSet hwAnimationSet : this.mHwAnimations) {
                hwAnimationSet.cancel();
            }
            this.mHwAnimations.clear();
        }
        super.onDetachedFromWindow();
    }

    public void refreshCondition(String strValue, boolean value) {
        HwLog.d("HwImageView", "condition: " + strValue + " value : " + value);
        if (value && !this.mAnimationStart) {
            startAnimation();
        } else if (!value) {
            stopAnimation();
        }
    }

    private void startAnimation() {
        if (this.mHandler.hasMessages(0)) {
            this.mHandler.removeMessages(0);
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        for (int i = 0; i < this.mHwAnimations.size(); i++) {
            HwAnimationSet anim = (HwAnimationSet) this.mHwAnimations.get(i);
            this.mAnimationStart = true;
            Message msg = Message.obtain();
            msg.obj = anim;
            msg.what = 0;
            this.mHandler.sendMessageDelayed(msg, anim.mDelay);
        }
        if (this.mFrameAnimation != null && !this.mAnimationStart) {
            this.mAnimationStart = true;
            msg = Message.obtain();
            msg.what = 0;
            this.mHandler.sendMessage(msg);
        }
    }

    private void stopAnimation() {
        if (this.mHandler.hasMessages(0)) {
            this.mHandler.removeMessages(0);
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        this.mAnimationStart = false;
        Message msg = Message.obtain();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }
}
