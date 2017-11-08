package com.huawei.keyguard.view.charge.e40;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.huawei.keyguard.util.Typefaces;
import java.util.Locale;

public class ChargeAnimView extends FrameLayout {
    private static final int[] COLORS = new int[]{-46014, -30208, -7812525};
    private static int[] sColors;
    private ValueAnimator mArcAnimator;
    private ChargeEffectsLayerViewE40 mChargeEffectsLayerViewE40;
    private float mFraction;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    ChargeAnimView.this.startAnim();
                    return;
                case 1:
                    ChargeAnimView.this.resetAnim();
                    return;
                case 2:
                    if (ChargeAnimView.this.mArcAnimator != null) {
                        ChargeAnimView.this.mArcAnimator.cancel();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {
        public void onRefreshBatteryInfo(BatteryStatus status) {
            ChargeAnimView.this.refreshBatteryInfo(status);
        }
    };
    private float mLevelPercent = 0.0f;
    private Paint mPaint = new Paint();
    private float[] mPiontStart = new float[]{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f};
    private AnimatorSet mPointsAnimator;
    private RectF mRectF;
    private TextView mTextView;
    private int sRepeatCount = 0;

    public ChargeAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = this.mContext.getResources();
        int lineWidth = res.getDimensionPixelSize(R$dimen.charge_line_width);
        this.mRectF = new RectF(((float) lineWidth) / 2.0f, ((float) lineWidth) / 2.0f, (float) (res.getDimensionPixelSize(R$dimen.charge_area_width) - lineWidth), (float) (res.getDimensionPixelSize(R$dimen.charge_area_height) - lineWidth));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth((float) lineWidth);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeCap(Cap.ROUND);
        this.mPaint.setColor(COLORS[0]);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(this.mRectF, 270.0f, 360.0f * this.mFraction, false, this.mPaint);
        drawPionts(canvas);
    }

    private void drawPionts(Canvas canvas) {
        if (canvas != null) {
            for (int i = 0; i < this.mPiontStart.length; i++) {
                if (this.mPiontStart[i] > 0.0f) {
                    canvas.drawArc(this.mRectF, this.mPiontStart[i], 0.1f, false, this.mPaint);
                }
            }
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTextView = (TextView) findViewById(R$id.charge_digital);
        Typeface t = Typefaces.get(getContext(), "/system/fonts/Roboto-Thin.ttf");
        if (this.mTextView != null && t != null) {
            this.mTextView.setTypeface(t);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
        reset();
    }

    private void startAnim() {
        setVisibility(0);
        AnimatorSet anim = new AnimatorSet();
        ValueAnimator arcAnim = getArcAnim();
        AnimatorSet pointsAnim = getPointsAnim();
        anim.playTogether(new Animator[]{arcAnim, pointsAnim});
        anim.start();
        startChargeLevelAnim();
        startChargeEffectsLayerAnim();
    }

    private void reset() {
        if (this.mArcAnimator != null) {
            if (this.mArcAnimator.isRunning()) {
                this.mArcAnimator.cancel();
            }
            this.mArcAnimator = null;
        }
        if (this.mPointsAnimator != null) {
            if (this.mPointsAnimator.isRunning()) {
                this.mPointsAnimator.cancel();
            }
            this.mPointsAnimator = null;
        }
        for (int i = 0; i < this.mPiontStart.length; i++) {
            this.mPiontStart[i] = -1.0f;
        }
        setVisibility(8);
        if (this.mChargeEffectsLayerViewE40 != null) {
            this.mChargeEffectsLayerViewE40.setVisibility(8);
        }
    }

    private void resetAnim() {
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setDuration(500);
        alpha.setInterpolator(new LinearInterpolator());
        startAnimation(alpha);
        if (this.mChargeEffectsLayerViewE40 != null) {
            this.mChargeEffectsLayerViewE40.startAnimation(alpha);
        }
        reset();
    }

    public ValueAnimator getArcAnim() {
        if (this.mArcAnimator == null) {
            PropertyValuesHolder xPvh = PropertyValuesHolder.ofFloat("x", new float[]{0.0f, 0.0f});
            this.mArcAnimator = ObjectAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{xPvh});
            this.mArcAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mArcAnimator.setDuration((long) getOneCircleTime());
            this.mArcAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animator) {
                    ChargeAnimView.this.mFraction = animator.getAnimatedFraction();
                    ChargeAnimView.this.invalidate();
                    if (ChargeAnimView.this.mFraction > ChargeAnimView.this.mLevelPercent) {
                        ChargeAnimView.this.mHandler.sendEmptyMessage(2);
                    }
                }
            });
        }
        return this.mArcAnimator;
    }

    private void startChargeLevelAnim() {
        View level = findViewById(R$id.charge_level);
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(800);
        alpha.setInterpolator(new LinearInterpolator());
        LayoutParams pa = new LayoutParams(-2, -2);
        String language = Locale.getDefault().getLanguage();
        int space = getResources().getDimensionPixelSize(R$dimen.charge_between_digital_percent);
        if (level != null && this.mTextView != null) {
            if (language.equals("tr") || language.equals("eu")) {
                level.setLayoutDirection(1);
                pa.leftMargin = space;
                pa.rightMargin = 0;
                this.mTextView.setLayoutParams(pa);
            } else {
                level.setLayoutDirection(0);
                pa.leftMargin = 0;
                pa.rightMargin = space;
                this.mTextView.setLayoutParams(pa);
            }
            level.setVisibility(0);
            level.startAnimation(alpha);
        }
    }

    private void startChargeEffectsLayerAnim() {
        if (this.mChargeEffectsLayerViewE40 != null) {
            this.mChargeEffectsLayerViewE40.startAnim();
        }
    }

    private AnimatorSet getPointsAnim() {
        if (this.mPointsAnimator == null) {
            this.mPointsAnimator = new AnimatorSet();
            ValueAnimator[] animators = new ValueAnimator[5];
            for (int i = 0; i < 5; i++) {
                final int index = i;
                PropertyValuesHolder xPvh = PropertyValuesHolder.ofFloat("x", new float[]{0.0f, 0.0f});
                ValueAnimator anim = ObjectAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{xPvh});
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration((long) getOneCircleTime());
                anim.setStartDelay(((long) index) * 100);
                anim.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ChargeAnimView.this.mPiontStart[index] = (360.0f * animation.getAnimatedFraction()) + 270.0f;
                        ChargeAnimView.this.invalidate();
                    }
                });
                anim.setRepeatCount(this.sRepeatCount);
                anim.setRepeatMode(1);
                animators[i] = anim;
            }
            this.mPointsAnimator.playTogether(animators);
            this.mPointsAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ChargeAnimView.this.mHandler.sendEmptyMessage(1);
                }

                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    ChargeAnimView.this.mHandler.sendEmptyMessage(1);
                }
            });
        }
        return this.mPointsAnimator;
    }

    private static int[] getColors() {
        if (sColors == null || sColors.length < 2) {
            return COLORS;
        }
        return sColors;
    }

    private int ave(int s, int d, float p) {
        return Math.round(((float) (d - s)) * p) + s;
    }

    private int interpColor(int[] colors, float unit) {
        if (unit <= 0.0f) {
            return colors[0];
        }
        if (unit >= 1.0f) {
            return colors[colors.length - 1];
        }
        float p = unit * ((float) (colors.length - 1));
        int i = (int) p;
        p -= (float) i;
        int c0 = colors[i];
        int c1 = colors[i + 1];
        return Color.argb(ave(Color.alpha(c0), Color.alpha(c1), p), ave(Color.red(c0), Color.red(c1), p), ave(Color.green(c0), Color.green(c1), p), ave(Color.blue(c0), Color.blue(c1), p));
    }

    private void refreshBatteryInfo(BatteryStatus status) {
        if (status != null) {
            int level = status.level;
            this.mLevelPercent = ((float) level) / 100.0f;
            this.mPaint.setColor(interpColor(getColors(), (((float) level) - 10.0f) / 80.0f));
            TextView textview = (TextView) findViewById(R$id.charge_digital);
            if (textview != null) {
                textview.setText(Integer.toString(level));
            }
            if (this.mArcAnimator == null) {
                this.mHandler.sendEmptyMessage(0);
            }
            invalidate();
        }
    }

    public void setChargeEffectsLayerView(ChargeEffectsLayerViewE40 view) {
        this.mChargeEffectsLayerViewE40 = view;
    }

    private int getOneCircleTime() {
        return 2100;
    }
}
