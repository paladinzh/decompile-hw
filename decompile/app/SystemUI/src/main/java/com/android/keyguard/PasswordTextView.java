package com.android.keyguard;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.huawei.keyguard.view.effect.HintErrorEffect;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.Stack;

public class PasswordTextView extends View {
    private Interpolator mAppearInterpolator;
    private int mCharPadding;
    private Stack<CharState> mCharPool;
    private Interpolator mDisappearInterpolator;
    private int mDotSize;
    private final Paint mDrawPaint;
    private Interpolator mFastOutSlowInInterpolator;
    private final int mGravity;
    private HintErrorEffect mHintErrorEffect;
    private PowerManager mPM;
    private PasswdChangeListener mPasswdChangeListener;
    private boolean mShowPassword;
    private String mText;
    private ArrayList<CharState> mTextChars;
    private final int mTextHeightRaw;
    private UserActivityListener mUserActivityListener;

    public interface PasswdChangeListener {
        void onCharAppend();

        void onCharDel();
    }

    public interface UserActivityListener {
        void onUserActivity();
    }

    private class CharState {
        float currentDotSizeFactor;
        float currentTextSizeFactor;
        float currentTextTranslationY;
        float currentWidthFactor;
        boolean dotAnimationIsGrowing;
        Animator dotAnimator;
        AnimatorListener dotFinishListener;
        private AnimatorUpdateListener dotSizeUpdater;
        private Runnable dotSwapperRunnable;
        boolean isDotSwapPending;
        AnimatorListener removeEndListener;
        boolean textAnimationIsGrowing;
        ValueAnimator textAnimator;
        AnimatorListener textFinishListener;
        private AnimatorUpdateListener textSizeUpdater;
        ValueAnimator textTranslateAnimator;
        AnimatorListener textTranslateFinishListener;
        private AnimatorUpdateListener textTranslationUpdater;
        char whichChar;
        boolean widthAnimationIsGrowing;
        ValueAnimator widthAnimator;
        AnimatorListener widthFinishListener;
        private AnimatorUpdateListener widthUpdater;

        private CharState() {
            this.currentTextTranslationY = 1.0f;
            this.removeEndListener = new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animation) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animation) {
                    if (!this.mCancelled) {
                        PasswordTextView.this.mTextChars.remove(CharState.this);
                        PasswordTextView.this.mCharPool.push(CharState.this);
                        CharState.this.reset();
                        CharState.this.cancelAnimator(CharState.this.textTranslateAnimator);
                        CharState.this.textTranslateAnimator = null;
                    }
                }

                public void onAnimationStart(Animator animation) {
                    this.mCancelled = false;
                }
            };
            this.dotFinishListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    CharState.this.dotAnimator = null;
                }
            };
            this.textFinishListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    CharState.this.textAnimator = null;
                }
            };
            this.textTranslateFinishListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    CharState.this.textTranslateAnimator = null;
                }
            };
            this.widthFinishListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    CharState.this.widthAnimator = null;
                }
            };
            this.dotSizeUpdater = new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    CharState.this.currentDotSizeFactor = ((Float) animation.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.textSizeUpdater = new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    CharState.this.currentTextSizeFactor = ((Float) animation.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.textTranslationUpdater = new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    CharState.this.currentTextTranslationY = ((Float) animation.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.widthUpdater = new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    CharState.this.currentWidthFactor = ((Float) animation.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.dotSwapperRunnable = new Runnable() {
                public void run() {
                    CharState.this.performSwap();
                    CharState.this.isDotSwapPending = false;
                }
            };
        }

        void reset() {
            this.whichChar = '\u0000';
            this.currentTextSizeFactor = 0.0f;
            this.currentDotSizeFactor = 0.0f;
            this.currentWidthFactor = 0.0f;
            cancelAnimator(this.textAnimator);
            this.textAnimator = null;
            cancelAnimator(this.dotAnimator);
            this.dotAnimator = null;
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = null;
            this.currentTextTranslationY = 1.0f;
            removeDotSwapCallbacks();
        }

        void startRemoveAnimation(long startDelay, long widthDelay) {
            boolean dotNeedsAnimation = (this.currentDotSizeFactor <= 0.0f || this.dotAnimator != null) ? this.dotAnimator != null ? this.dotAnimationIsGrowing : false : true;
            boolean textNeedsAnimation = (this.currentTextSizeFactor <= 0.0f || this.textAnimator != null) ? this.textAnimator != null ? this.textAnimationIsGrowing : false : true;
            boolean widthNeedsAnimation = (this.currentWidthFactor <= 0.0f || this.widthAnimator != null) ? this.widthAnimator != null ? this.widthAnimationIsGrowing : false : true;
            if (dotNeedsAnimation) {
                startDotDisappearAnimation(startDelay);
            }
            if (textNeedsAnimation) {
                startTextDisappearAnimation(startDelay);
            }
            if (widthNeedsAnimation) {
                startWidthDisappearAnimation(widthDelay);
            }
        }

        void startAppearAnimation() {
            boolean dotNeedsAnimation = !PasswordTextView.this.mShowPassword ? this.dotAnimator == null || !this.dotAnimationIsGrowing : false;
            boolean textNeedsAnimation = PasswordTextView.this.mShowPassword ? this.textAnimator == null || !this.textAnimationIsGrowing : false;
            boolean widthNeedsAnimation = this.widthAnimator == null || !this.widthAnimationIsGrowing;
            if (dotNeedsAnimation) {
                startDotAppearAnimation(0);
            }
            if (textNeedsAnimation) {
                startTextAppearAnimation();
            }
            if (widthNeedsAnimation) {
                startWidthAppearAnimation();
            }
            if (PasswordTextView.this.mShowPassword) {
                postDotSwap(500);
            }
        }

        private void postDotSwap(long delay) {
            removeDotSwapCallbacks();
            PasswordTextView.this.postDelayed(this.dotSwapperRunnable, delay);
            this.isDotSwapPending = true;
        }

        private void removeDotSwapCallbacks() {
            PasswordTextView.this.removeCallbacks(this.dotSwapperRunnable);
            this.isDotSwapPending = false;
        }

        void swapToDotWhenAppearFinished() {
            removeDotSwapCallbacks();
            if (this.textAnimator != null) {
                postDotSwap(100 + (this.textAnimator.getDuration() - this.textAnimator.getCurrentPlayTime()));
            } else {
                performSwap();
            }
        }

        private void performSwap() {
            startTextDisappearAnimation(0);
            startDotAppearAnimation(30);
        }

        private void startWidthDisappearAnimation(long widthDelay) {
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = ValueAnimator.ofFloat(new float[]{this.currentWidthFactor, 0.0f});
            this.widthAnimator.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.addListener(this.removeEndListener);
            this.widthAnimator.setDuration((long) (this.currentWidthFactor * 160.0f));
            this.widthAnimator.setStartDelay(widthDelay);
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = false;
        }

        private void startTextDisappearAnimation(long startDelay) {
            cancelAnimator(this.textAnimator);
            this.textAnimator = ValueAnimator.ofFloat(new float[]{this.currentTextSizeFactor, 0.0f});
            this.textAnimator.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(PasswordTextView.this.mDisappearInterpolator);
            this.textAnimator.setDuration((long) (this.currentTextSizeFactor * 160.0f));
            this.textAnimator.setStartDelay(startDelay);
            this.textAnimator.start();
            this.textAnimationIsGrowing = false;
        }

        private void startDotDisappearAnimation(long startDelay) {
            cancelAnimator(this.dotAnimator);
            ValueAnimator animator = ValueAnimator.ofFloat(new float[]{this.currentDotSizeFactor, 0.0f});
            animator.addUpdateListener(this.dotSizeUpdater);
            animator.addListener(this.dotFinishListener);
            animator.setInterpolator(PasswordTextView.this.mDisappearInterpolator);
            animator.setDuration((long) (Math.min(this.currentDotSizeFactor, 1.0f) * 160.0f));
            animator.setStartDelay(startDelay);
            animator.start();
            this.dotAnimator = animator;
            this.dotAnimationIsGrowing = false;
        }

        private void startWidthAppearAnimation() {
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = ValueAnimator.ofFloat(new float[]{this.currentWidthFactor, 1.0f});
            this.widthAnimator.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.setDuration((long) ((1.0f - this.currentWidthFactor) * 160.0f));
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = true;
        }

        private void startTextAppearAnimation() {
            cancelAnimator(this.textAnimator);
            this.textAnimator = ValueAnimator.ofFloat(new float[]{this.currentTextSizeFactor, 1.0f});
            this.textAnimator.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(PasswordTextView.this.mAppearInterpolator);
            this.textAnimator.setDuration((long) ((1.0f - this.currentTextSizeFactor) * 160.0f));
            this.textAnimator.start();
            this.textAnimationIsGrowing = true;
            if (this.textTranslateAnimator == null) {
                this.textTranslateAnimator = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
                this.textTranslateAnimator.addUpdateListener(this.textTranslationUpdater);
                this.textTranslateAnimator.addListener(this.textTranslateFinishListener);
                this.textTranslateAnimator.setInterpolator(PasswordTextView.this.mAppearInterpolator);
                this.textTranslateAnimator.setDuration(160);
                this.textTranslateAnimator.start();
            }
        }

        private void startDotAppearAnimation(long delay) {
            cancelAnimator(this.dotAnimator);
            if (PasswordTextView.this.mShowPassword) {
                ValueAnimator growAnimator = ValueAnimator.ofFloat(new float[]{this.currentDotSizeFactor, 1.0f});
                growAnimator.addUpdateListener(this.dotSizeUpdater);
                growAnimator.setDuration((long) ((1.0f - this.currentDotSizeFactor) * 160.0f));
                growAnimator.addListener(this.dotFinishListener);
                growAnimator.setStartDelay(delay);
                growAnimator.start();
                this.dotAnimator = growAnimator;
            } else {
                ValueAnimator overShootAnimator = ValueAnimator.ofFloat(new float[]{this.currentDotSizeFactor, 1.5f});
                overShootAnimator.addUpdateListener(this.dotSizeUpdater);
                overShootAnimator.setInterpolator(PasswordTextView.this.mAppearInterpolator);
                overShootAnimator.setDuration(160);
                ValueAnimator settleBackAnimator = ValueAnimator.ofFloat(new float[]{1.5f, 1.0f});
                settleBackAnimator.addUpdateListener(this.dotSizeUpdater);
                settleBackAnimator.setDuration(160);
                settleBackAnimator.addListener(this.dotFinishListener);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(new Animator[]{overShootAnimator, settleBackAnimator});
                animatorSet.setStartDelay(delay);
                animatorSet.start();
                this.dotAnimator = animatorSet;
            }
            this.dotAnimationIsGrowing = true;
        }

        private void cancelAnimator(Animator animator) {
            if (animator != null) {
                animator.cancel();
            }
        }

        public float draw(Canvas canvas, float currentDrawPosition, int charHeight, float yPosition, float charLength) {
            boolean textVisible = this.currentTextSizeFactor > 0.0f;
            boolean dotVisible = this.currentDotSizeFactor > 0.0f;
            float charWidth = charLength * this.currentWidthFactor;
            if (textVisible) {
                float currYPosition = (((((float) charHeight) / 2.0f) * this.currentTextSizeFactor) + yPosition) + ((((float) charHeight) * this.currentTextTranslationY) * 0.8f);
                canvas.save();
                canvas.translate(currentDrawPosition + (charWidth / 2.0f), currYPosition);
                canvas.scale(this.currentTextSizeFactor, this.currentTextSizeFactor);
                canvas.drawText(Character.toString(this.whichChar), 0.0f, 0.0f, PasswordTextView.this.mDrawPaint);
                canvas.restore();
            }
            if (dotVisible) {
                canvas.save();
                canvas.translate(currentDrawPosition + (charWidth / 2.0f), yPosition);
                canvas.drawCircle(0.0f, 0.0f, ((float) (PasswordTextView.this.mDotSize / 2)) * this.currentDotSizeFactor, PasswordTextView.this.mDrawPaint);
                canvas.restore();
            }
            return (((float) PasswordTextView.this.mCharPadding) * this.currentWidthFactor) + charWidth;
        }
    }

    public PasswordTextView(Context context) {
        this(context, null);
    }

    public PasswordTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PasswordTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        boolean z = true;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHintErrorEffect = new HintErrorEffect(this);
        this.mTextChars = new ArrayList();
        this.mText = BuildConfig.FLAVOR;
        this.mCharPool = new Stack();
        this.mDrawPaint = new Paint();
        this.mPasswdChangeListener = null;
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.PasswordTextView);
        try {
            this.mTextHeightRaw = a.getInt(R$styleable.PasswordTextView_scaledTextSize, 0);
            this.mGravity = a.getInt(R$styleable.PasswordTextView_android_gravity, 17);
            this.mDotSize = a.getDimensionPixelSize(R$styleable.PasswordTextView_dotSize, getContext().getResources().getDimensionPixelSize(R$dimen.password_dot_size));
            this.mCharPadding = a.getDimensionPixelSize(R$styleable.PasswordTextView_charPadding, getContext().getResources().getDimensionPixelSize(R$dimen.password_char_padding));
            this.mDrawPaint.setFlags(129);
            this.mDrawPaint.setTextAlign(Align.CENTER);
            this.mDrawPaint.setColor(-1);
            this.mDrawPaint.setTypeface(Typeface.create("sans-serif-light", 0));
            if (System.getInt(this.mContext.getContentResolver(), "show_password", 1) != 1) {
                z = false;
            }
            this.mShowPassword = z;
            this.mAppearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
            this.mDisappearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563663);
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
            this.mPM = (PowerManager) this.mContext.getSystemService("power");
        } finally {
            a.recycle();
        }
    }

    protected void onDraw(Canvas canvas) {
        float currentDrawPosition;
        this.mHintErrorEffect.drawErrorEffect(canvas);
        float totalDrawingWidth = getDrawingWidth();
        if ((this.mGravity & 7) != 3) {
            currentDrawPosition = (((float) getWidth()) - totalDrawingWidth) / 2.0f;
        } else if ((this.mGravity & 8388608) == 0 || getLayoutDirection() != 1) {
            currentDrawPosition = (float) getPaddingLeft();
        } else {
            currentDrawPosition = ((float) (getWidth() - getPaddingRight())) - totalDrawingWidth;
        }
        int length = this.mTextChars.size();
        Rect bounds = getCharBounds();
        int charHeight = bounds.bottom - bounds.top;
        float yPosition = (float) ((((getHeight() - getPaddingBottom()) - getPaddingTop()) / 2) + getPaddingTop());
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        float charLength = (float) (bounds.right - bounds.left);
        for (int i = 0; i < length; i++) {
            currentDrawPosition += ((CharState) this.mTextChars.get(i)).draw(canvas, currentDrawPosition, charHeight, yPosition, charLength);
        }
    }

    public void showErrorEffect() {
        this.mHintErrorEffect.showErrEffect(true);
        if (!this.mHintErrorEffect.isShownErrEffect()) {
            invalidate();
        }
    }

    public void hideErrorEffect() {
        this.mHintErrorEffect.showErrEffect(false);
        if (this.mHintErrorEffect.isShownErrEffect()) {
            invalidate();
        }
    }

    public boolean isShownErrEffect() {
        return this.mHintErrorEffect.isShownErrEffect();
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    private Rect getCharBounds() {
        this.mDrawPaint.setTextSize(((float) this.mTextHeightRaw) * getResources().getDisplayMetrics().scaledDensity);
        Rect bounds = new Rect();
        this.mDrawPaint.getTextBounds("0", 0, 1, bounds);
        return bounds;
    }

    private float getDrawingWidth() {
        int width = 0;
        int length = this.mTextChars.size();
        Rect bounds = getCharBounds();
        int charLength = bounds.right - bounds.left;
        for (int i = 0; i < length; i++) {
            CharState charState = (CharState) this.mTextChars.get(i);
            if (i != 0) {
                width = (int) (((float) width) + (((float) this.mCharPadding) * charState.currentWidthFactor));
            }
            width = (int) (((float) width) + (((float) charLength) * charState.currentWidthFactor));
        }
        return (float) width;
    }

    public void append(char c) {
        CharState charState;
        int visibleChars = this.mTextChars.size();
        String textbefore = this.mText;
        this.mText += c;
        int newLength = this.mText.length();
        if (newLength > visibleChars) {
            charState = obtainCharState(c);
            this.mTextChars.add(charState);
        } else {
            charState = (CharState) this.mTextChars.get(newLength - 1);
            charState.whichChar = c;
        }
        if (this.mPasswdChangeListener != null) {
            this.mPasswdChangeListener.onCharAppend();
        }
        charState.startAppearAnimation();
        if (newLength > 1) {
            CharState previousState = (CharState) this.mTextChars.get(newLength - 2);
            if (previousState.isDotSwapPending) {
                previousState.swapToDotWhenAppearFinished();
            }
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(textbefore, textbefore.length(), 0, 1);
    }

    public void setUserActivityListener(UserActivityListener userActivitiListener) {
        this.mUserActivityListener = userActivitiListener;
    }

    private void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
        if (this.mUserActivityListener != null) {
            this.mUserActivityListener.onUserActivity();
        }
    }

    public void deleteLastChar() {
        int length = this.mText.length();
        String textbefore = this.mText;
        if (length > 0) {
            if (this.mPasswdChangeListener != null) {
                this.mPasswdChangeListener.onCharDel();
            }
            this.mText = this.mText.substring(0, length - 1);
            ((CharState) this.mTextChars.get(length - 1)).startRemoveAnimation(0, 0);
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(textbefore, textbefore.length() - 1, 1, 0);
    }

    public String getText() {
        return this.mText;
    }

    private CharState obtainCharState(char c) {
        CharState charState;
        if (this.mCharPool.isEmpty()) {
            charState = new CharState();
        } else {
            charState = (CharState) this.mCharPool.pop();
            charState.reset();
        }
        charState.whichChar = c;
        return charState;
    }

    public void reset(boolean animated, boolean announce) {
        String textbefore = this.mText;
        this.mText = BuildConfig.FLAVOR;
        int length = this.mTextChars.size();
        int middleIndex = (length - 1) / 2;
        for (int i = 0; i < length; i++) {
            CharState charState = (CharState) this.mTextChars.get(i);
            if (animated) {
                int delayIndex;
                if (i <= middleIndex) {
                    delayIndex = i * 2;
                } else {
                    delayIndex = (length - 1) - (((i - middleIndex) - 1) * 2);
                }
                charState.startRemoveAnimation(Math.min(((long) delayIndex) * 40, 200), Math.min(40 * ((long) (length - 1)), 200) + 160);
                charState.removeDotSwapCallbacks();
            } else {
                this.mCharPool.push(charState);
            }
        }
        if (!animated) {
            this.mTextChars.clear();
        }
        if (announce) {
            sendAccessibilityEventTypeViewTextChanged(textbefore, 0, textbefore.length(), 0);
        }
    }

    void sendAccessibilityEventTypeViewTextChanged(String beforeText, int fromIndex, int removedCount, int addedCount) {
        if (!AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            return;
        }
        if (isFocused() || (isSelected() && isShown())) {
            CharSequence beforeText2;
            if (!shouldSpeakPasswordsForAccessibility()) {
                beforeText2 = null;
            }
            AccessibilityEvent event = AccessibilityEvent.obtain(16);
            event.setFromIndex(fromIndex);
            event.setRemovedCount(removedCount);
            event.setAddedCount(addedCount);
            event.setBeforeText(beforeText2);
            event.setPassword(true);
            sendAccessibilityEventUnchecked(event);
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(PasswordTextView.class.getName());
        event.setPassword(true);
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        if (shouldSpeakPasswordsForAccessibility()) {
            CharSequence text = this.mText;
            if (!TextUtils.isEmpty(text)) {
                event.getText().add(text);
            }
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(PasswordTextView.class.getName());
        info.setPassword(true);
        if (shouldSpeakPasswordsForAccessibility()) {
            info.setText(this.mText);
        }
        info.setEditable(true);
        info.setInputType(16);
    }

    private boolean shouldSpeakPasswordsForAccessibility() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "speak_password", 0, -3) == 1;
    }

    public void setChangeListener(PasswdChangeListener l) {
        this.mPasswdChangeListener = l;
    }
}
