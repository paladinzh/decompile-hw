package com.android.deskclock.timer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import com.android.alarmclock.WorldAnalogClock;
import com.android.deskclock.R;
import com.android.deskclock.R$styleable;
import com.android.deskclock.timer.CommonHandler.MessageHandler;
import com.android.util.FormatTime;
import com.android.util.Log;
import java.util.Locale;

public class AdvancedNumberPicker extends LinearLayout implements MessageHandler {
    private static final char[] DIGIT_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int SHOW_INPUT_CONTROLS_DELAY_MILLIS = ViewConfiguration.getDoubleTapTimeout();
    public static final Formatter TWO_DIGIT_FORMATTER = new Formatter() {
        final Object[] mArgs = new Object[1];
        final StringBuilder mBuilder = new StringBuilder();
        final java.util.Formatter mFmt = new java.util.Formatter(this.mBuilder, Locale.US);

        public String format(int value) {
            this.mArgs[0] = Integer.valueOf(value);
            this.mBuilder.delete(0, this.mBuilder.length());
            this.mFmt.format("%02d", new Object[]{Integer.valueOf(value)});
            return this.mFmt.toString();
        }
    };
    private final float DATEPICKER_TEXT_SIZE_SMALL;
    private int SELECTOR_MIDDLE_ITEM_INDEX;
    private Rect clipBounds;
    private Paint drawPaint;
    private boolean isAddDescription;
    private final Scroller mAdjustScroller;
    private AdjustScrollerCommand mAdjustScrollerCommand;
    private boolean mAdjustScrollerOnUpEvent;
    private boolean mCheckBeginEditOnUpEvent;
    private final boolean mComputeMaxWidth;
    private Context mContext;
    private int mCurrentScrollOffset;
    private String mDescription;
    private final Animator mDimSelectorWheelAnimator;
    private final Scroller mFlingScroller;
    private final boolean mFlingable;
    private Formatter mFormatter;
    Handler mHandler;
    private int mInitialScrollOffset;
    private final TextView mInputText;
    private boolean mIsAccessibilityEnabled;
    private float mLastDownEventY;
    private float mLastMotionEventY;
    private int mMaxHeight;
    private int mMaxValue;
    private int mMaxWidth;
    private int mMaximumFlingVelocity;
    private final int mMinHeight;
    private int mMinValue;
    private final int mMinWidth;
    private int mMinimumFlingVelocity;
    private OnColorChangeListener mOnColorChangeListener;
    private OnScrollListener mOnScrollListener;
    private OnValueChangeListener mOnValueChangeListener;
    private int mPreviousScrollerY;
    private int mScrollState;
    private boolean mScrollWheelAndFadingEdgesInitialized;
    private int mSelectorElementHeight;
    private final SparseArray<String> mSelectorIndexToStringCache;
    private int[] mSelectorIndices;
    private int mSelectorTextGapHeight;
    private final Paint mSelectorWheelPaint;
    private int mSelectorWheelState;
    private final AnimatorSet mShowInputControlsAnimator;
    private final int mSolidColor;
    private final int mTextSize;
    private int mTouchSlop;
    private int mValue;
    private VelocityTracker mVelocityTracker;
    private boolean mWrapSelectorWheel;
    Locale sLocal;
    private Typeface typeface;

    public interface Formatter {
        String format(int i);
    }

    class AdjustScrollerCommand implements Runnable {
        AdjustScrollerCommand() {
        }

        public void run() {
            AdvancedNumberPicker.this.mPreviousScrollerY = 0;
            if (AdvancedNumberPicker.this.mInitialScrollOffset == AdvancedNumberPicker.this.mCurrentScrollOffset) {
                AdvancedNumberPicker.this.updateInputTextView();
                return;
            }
            int deltaY = AdvancedNumberPicker.this.mInitialScrollOffset - AdvancedNumberPicker.this.mCurrentScrollOffset;
            if (Math.abs(deltaY) > AdvancedNumberPicker.this.mSelectorElementHeight / 2) {
                deltaY += deltaY > 0 ? -AdvancedNumberPicker.this.mSelectorElementHeight : AdvancedNumberPicker.this.mSelectorElementHeight;
            }
            AdvancedNumberPicker.this.mAdjustScroller.startScroll(0, 0, 0, deltaY, 800);
            AdvancedNumberPicker.this.invalidate();
        }
    }

    public interface OnColorChangeListener {
        void onColorChange(AdvancedNumberPicker advancedNumberPicker);
    }

    public interface OnScrollListener {
        void onScrollStateChange(AdvancedNumberPicker advancedNumberPicker, int i);
    }

    public interface OnValueChangeListener {
        void onValueChange(AdvancedNumberPicker advancedNumberPicker, int i, int i2);
    }

    public AdvancedNumberPicker(Context context) {
        this(context, null);
    }

    public AdvancedNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.numberPickerStyle);
    }

    public AdvancedNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        boolean z;
        super(context, attrs, defStyle);
        this.SELECTOR_MIDDLE_ITEM_INDEX = 1;
        this.mContext = getContext();
        this.mFormatter = TWO_DIGIT_FORMATTER;
        this.mSelectorIndexToStringCache = new SparseArray();
        this.mSelectorIndices = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        this.mInitialScrollOffset = Integer.MIN_VALUE;
        this.mScrollState = 0;
        this.mIsAccessibilityEnabled = false;
        this.mHandler = new CommonHandler(this);
        this.drawPaint = new Paint();
        this.clipBounds = new Rect();
        this.sLocal = Locale.getDefault();
        AccessibilityManager accessibilityMgr = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (accessibilityMgr == null || !accessibilityMgr.isEnabled()) {
            z = false;
        } else {
            z = accessibilityMgr.isTouchExplorationEnabled();
        }
        this.mIsAccessibilityEnabled = z;
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R$styleable.NumberPicker, defStyle, 0);
        this.mSolidColor = attributesArray.getColor(0, 0);
        attributesArray.recycle();
        this.mFlingable = true;
        this.DATEPICKER_TEXT_SIZE_SMALL = getResources().getDimension(R.dimen.datepicker_text_size_small);
        this.mMinHeight = -1;
        try {
            this.mMaxHeight = ((int) getResources().getDimension(R.dimen.number_picker_input_high)) * (this.mSelectorIndices.length + 1);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        if (this.mMinHeight == -1 || this.mMaxHeight == -1 || this.mMinHeight <= this.mMaxHeight) {
            this.mMinWidth = 96;
            this.mMaxWidth = -1;
            if (this.mMinWidth == -1 || this.mMaxWidth == -1 || this.mMinWidth <= this.mMaxWidth) {
                if (this.mMaxWidth == Integer.MAX_VALUE) {
                    z = true;
                } else {
                    z = false;
                }
                this.mComputeMaxWidth = z;
                setWillNotDraw(false);
                setSelectorWheelState(0);
                ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.number_picker, this, true);
                this.mInputText = (TextView) findViewById(R.id.numberpicker_input);
                this.mTouchSlop = ViewConfiguration.getTapTimeout();
                ViewConfiguration configuration = ViewConfiguration.get(context);
                this.mTouchSlop = configuration.getScaledTouchSlop();
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;
                this.mTextSize = (int) this.mInputText.getTextSize();
                this.typeface = Typeface.create("HwChinese-light", 0);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setTextAlign(Align.CENTER);
                paint.setTextSize(this.DATEPICKER_TEXT_SIZE_SMALL);
                paint.setTypeface(this.typeface);
                this.mSelectorWheelPaint = paint;
                this.mDimSelectorWheelAnimator = ObjectAnimator.ofInt(this, "selectorPaintAlpha", new int[]{26, 60});
                this.mShowInputControlsAnimator = new AnimatorSet();
                this.mFlingScroller = new Scroller(getContext(), null, true);
                this.mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
                updateInputTextView();
                if (!this.mFlingable) {
                    return;
                }
                if (isInEditMode()) {
                    setSelectorWheelState(1);
                    return;
                }
                setSelectorWheelState(2);
                hideInputControls();
                return;
            }
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        throw new IllegalArgumentException("minHeight > maxHeight");
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int msrdWdth = getMeasuredWidth();
        int msrdHght = getMeasuredHeight();
        int inptTxtMsrdWdth = this.mInputText.getMeasuredWidth();
        int inptTxtMsrdHght = this.mInputText.getMeasuredHeight();
        int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        this.mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtLeft + inptTxtMsrdWdth, inptTxtTop + inptTxtMsrdHght);
        if (!this.mScrollWheelAndFadingEdgesInitialized) {
            this.mScrollWheelAndFadingEdgesInitialized = true;
            initializeSelectorWheel();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(makeMeasureSpec(widthMeasureSpec, this.mMaxWidth), makeMeasureSpec(heightMeasureSpec, this.mMaxHeight));
        setMeasuredDimension(resolveSizeAndStateRespectingMinSize(this.mMinWidth, getMeasuredWidth(), widthMeasureSpec), resolveSizeAndStateRespectingMinSize(this.mMinHeight, getMeasuredHeight(), heightMeasureSpec));
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || !this.mFlingable) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                float y = event.getY();
                this.mLastDownEventY = y;
                this.mLastMotionEventY = y;
                removeAllCallbacks();
                this.mShowInputControlsAnimator.cancel();
                this.mDimSelectorWheelAnimator.cancel();
                this.mCheckBeginEditOnUpEvent = false;
                this.mAdjustScrollerOnUpEvent = true;
                ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                if (this.mSelectorWheelState == 2) {
                    boolean scrollersFinished;
                    this.mSelectorWheelPaint.setAlpha(26);
                    if (this.mFlingScroller.isFinished()) {
                        scrollersFinished = this.mAdjustScroller.isFinished();
                    } else {
                        scrollersFinished = false;
                    }
                    if (!scrollersFinished) {
                        this.mFlingScroller.forceFinished(true);
                        this.mAdjustScroller.forceFinished(true);
                        onScrollStateChange(0);
                    }
                    this.mCheckBeginEditOnUpEvent = scrollersFinished;
                    this.mAdjustScrollerOnUpEvent = true;
                    hideInputControls();
                    return true;
                }
                this.mAdjustScrollerOnUpEvent = false;
                setSelectorWheelState(2);
                hideInputControls();
                return true;
            case 2:
                if (((int) Math.abs(event.getY() - this.mLastDownEventY)) > this.mTouchSlop) {
                    this.mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(1);
                    setSelectorWheelState(2);
                    hideInputControls();
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (!this.mIsAccessibilityEnabled) {
            return super.onHoverEvent(event);
        }
        this.mInputText.requestAccessibilityFocus();
        if (event.getAction() != 10) {
            sendAccessibilityEvent(32768);
        }
        return true;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(this.mInputText.getText());
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 103:
                this.mOnColorChangeListener.onColorChange(this);
                return;
            default:
                return;
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        switch (ev.getActionMasked()) {
            case 0:
                this.mHandler.sendEmptyMessage(103);
                break;
            case 1:
                withTouchActionUp();
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
                break;
            case 2:
                float currentMoveY = ev.getY();
                if ((this.mCheckBeginEditOnUpEvent || this.mScrollState != 1) && ((int) Math.abs(currentMoveY - this.mLastDownEventY)) > this.mTouchSlop) {
                    this.mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(1);
                }
                scrollBy(0, (int) (currentMoveY - this.mLastMotionEventY));
                invalidate();
                this.mLastMotionEventY = currentMoveY;
                break;
        }
        return true;
    }

    private void withTouchActionUp() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
        int initialVelocity = (int) velocityTracker.getYVelocity();
        if (Math.abs(initialVelocity) > this.mMinimumFlingVelocity) {
            fling(initialVelocity);
            onScrollStateChange(2);
        } else if (!this.mAdjustScrollerOnUpEvent) {
            postAdjustScrollerCommand(SHOW_INPUT_CONTROLS_DELAY_MILLIS);
        } else if (this.mFlingScroller.isFinished() && this.mAdjustScroller.isFinished()) {
            postAdjustScrollerCommand(0);
            onScrollStateChange(0);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                removeAllCallbacks();
                break;
            case 2:
                if (this.mSelectorWheelState == 2) {
                    removeAllCallbacks();
                    forceCompleteChangeCurrentByOneViaScroll();
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 23 || keyCode == 66) {
            removeAllCallbacks();
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 3 || action == 1) {
            removeAllCallbacks();
        }
        return super.dispatchTrackballEvent(event);
    }

    public void computeScroll() {
        if (this.mSelectorWheelState != 0) {
            Scroller scroller = this.mFlingScroller;
            if (scroller.isFinished()) {
                scroller = this.mAdjustScroller;
                if (scroller.isFinished()) {
                    return;
                }
            }
            scroller.computeScrollOffset();
            int currentScrollerY = scroller.getCurrY();
            if (this.mPreviousScrollerY == 0) {
                this.mPreviousScrollerY = scroller.getStartY();
            }
            scrollBy(0, currentScrollerY - this.mPreviousScrollerY);
            this.mPreviousScrollerY = currentScrollerY;
            if (scroller.isFinished()) {
                onScrollerFinished(scroller);
            } else {
                invalidate();
            }
        }
    }

    public void setEnabled(boolean enabled) {
    }

    public void scrollBy(int x, int y) {
        if (this.mSelectorWheelState != 0) {
            int[] selectorIndices = this.mSelectorIndices;
            if (!this.mWrapSelectorWheel && y > 0 && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] <= this.mMinValue) {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            } else if (this.mWrapSelectorWheel || y >= 0 || selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] < this.mMaxValue) {
                this.mCurrentScrollOffset += y;
                while (this.mCurrentScrollOffset - this.mInitialScrollOffset > this.mSelectorTextGapHeight) {
                    this.mCurrentScrollOffset -= this.mSelectorElementHeight;
                    decrementSelectorIndices(selectorIndices);
                    changeCurrent(selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX]);
                    if (!this.mWrapSelectorWheel && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] <= this.mMinValue) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                }
                while (this.mCurrentScrollOffset - this.mInitialScrollOffset < (-this.mSelectorTextGapHeight)) {
                    this.mCurrentScrollOffset += this.mSelectorElementHeight;
                    incrementSelectorIndices(selectorIndices);
                    changeCurrent(selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX]);
                    if (!this.mWrapSelectorWheel && selectorIndices[this.SELECTOR_MIDDLE_ITEM_INDEX] >= this.mMaxValue) {
                        this.mCurrentScrollOffset = this.mInitialScrollOffset;
                    }
                }
            } else {
                this.mCurrentScrollOffset = this.mInitialScrollOffset;
            }
        }
    }

    public int getSolidColor() {
        return this.mSolidColor;
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        this.mOnValueChangeListener = onValueChangedListener;
    }

    public void setValue(int value) {
        if (this.mValue != value) {
            if (value < this.mMinValue) {
                value = this.mWrapSelectorWheel ? this.mMaxValue : this.mMinValue;
            }
            if (value > this.mMaxValue) {
                value = this.mWrapSelectorWheel ? this.mMinValue : this.mMaxValue;
            }
            this.mValue = value;
            initializeSelectorWheelIndices();
            updateInputTextView();
            invalidate();
        }
    }

    private void tryComputeMaxWidth() {
        if (this.mComputeMaxWidth) {
            float maxDigitWidth = 0.0f;
            for (int i = 0; i <= 9; i++) {
                float digitWidth = this.mSelectorWheelPaint.measureText(String.valueOf(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            for (int current = this.mMaxValue; current > 0; current /= 10) {
                numberOfDigits++;
            }
            int maxTextWidth = ((int) (((float) numberOfDigits) * maxDigitWidth)) + (this.mInputText.getPaddingLeft() + this.mInputText.getPaddingRight());
            if (this.mMaxWidth != maxTextWidth) {
                if (maxTextWidth > this.mMinWidth) {
                    this.mMaxWidth = maxTextWidth;
                } else {
                    this.mMaxWidth = this.mMinWidth;
                }
                invalidate();
            }
        }
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        if (wrapSelectorWheel && this.mMaxValue - this.mMinValue < this.mSelectorIndices.length) {
            throw new IllegalStateException("Range less than selector items count.");
        } else if (wrapSelectorWheel != this.mWrapSelectorWheel) {
            this.mWrapSelectorWheel = wrapSelectorWheel;
        }
    }

    public int getValue() {
        return this.mValue;
    }

    public void setMinValue(int minValue) {
        boolean wrapSelectorWheel = false;
        if (this.mMinValue != minValue) {
            if (minValue < 0) {
                throw new IllegalArgumentException("minValue must be >= 0");
            }
            this.mMinValue = minValue;
            if (this.mMinValue > this.mValue) {
                this.mValue = this.mMinValue;
            }
            if (this.mMaxValue - this.mMinValue > this.mSelectorIndices.length) {
                wrapSelectorWheel = true;
            }
            setWrapSelectorWheel(wrapSelectorWheel);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
        }
    }

    public void setMaxValue(int maxValue) {
        boolean wrapSelectorWheel = false;
        if (this.mMaxValue != maxValue) {
            if (maxValue < 0) {
                throw new IllegalArgumentException("maxValue must be >= 0");
            }
            this.mMaxValue = maxValue;
            if (this.mMaxValue < this.mValue) {
                this.mValue = this.mMaxValue;
            }
            if (this.mMaxValue - this.mMinValue > this.mSelectorIndices.length) {
                wrapSelectorWheel = true;
            }
            setWrapSelectorWheel(wrapSelectorWheel);
            initializeSelectorWheelIndices();
            updateInputTextView();
            tryComputeMaxWidth();
        }
    }

    protected float getTopFadingEdgeStrength() {
        return 0.9f;
    }

    protected float getBottomFadingEdgeStrength() {
        return 0.9f;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mShowInputControlsAnimator.isRunning() || this.mSelectorWheelState != 2) {
            long drawTime = getDrawingTime();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (getChildAt(i).isShown()) {
                    drawChild(canvas, getChildAt(i), drawTime);
                }
            }
        }
    }

    public static String formatNumberWithLocale(int value) {
        return String.valueOf(value);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mSelectorWheelState != 0) {
            float x = ((float) (this.mRight - this.mLeft)) / 2.0f;
            float y = (float) this.mCurrentScrollOffset;
            int restoreCount = canvas.save();
            if (this.mSelectorWheelState == 1) {
                canvas.getClipBounds(this.clipBounds);
                this.clipBounds.inset(0, this.mSelectorElementHeight);
                canvas.clipRect(this.clipBounds);
            }
            this.drawPaint.setAntiAlias(true);
            this.drawPaint.setTextAlign(Align.CENTER);
            this.drawPaint.setColor(getResources().getColor(R.color.transparency_100_color));
            this.drawPaint.setAlpha(26);
            this.drawPaint.setTextSize(this.DATEPICKER_TEXT_SIZE_SMALL);
            this.drawPaint.setTypeface(this.typeface);
            int[] selectorIndices = this.mSelectorIndices;
            for (int i = 0; i < selectorIndices.length; i++) {
                String str = (String) this.mSelectorIndexToStringCache.get(selectorIndices[i]);
                try {
                    str = formatNumberWithLocale(Integer.parseInt(str));
                    if (this.isAddDescription && !str.endsWith(this.mDescription)) {
                        str = str + this.mDescription;
                    }
                } catch (NumberFormatException e) {
                }
                int tempSelectorValue = Integer.parseInt(str);
                if (i == this.SELECTOR_MIDDLE_ITEM_INDEX) {
                    canvas.drawText(FormatTime.formatNumber(tempSelectorValue), x, y, this.mSelectorWheelPaint);
                } else if (this.sLocal.toString().contains("ar")) {
                    canvas.drawText(String.format(this.sLocal, "%2d", new Object[]{Integer.valueOf(tempSelectorValue)}), x, y, this.drawPaint);
                } else {
                    canvas.drawText(FormatTime.formatNumber(tempSelectorValue), x, y, this.drawPaint);
                }
                y += (float) this.mSelectorElementHeight;
            }
            canvas.restoreToCount(restoreCount);
        }
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case Integer.MIN_VALUE:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), 1073741824);
            case 0:
                return MeasureSpec.makeMeasureSpec(maxSize, 1073741824);
            case 1073741824:
                return measureSpec;
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        if (minSize != -1) {
            return resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0);
        }
        return measuredSize;
    }

    private void initializeSelectorWheelIndices() {
        this.mSelectorIndexToStringCache.clear();
        int current = getValue();
        for (int i = 0; i < this.mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - this.SELECTOR_MIDDLE_ITEM_INDEX);
            if (this.mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            this.mSelectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(this.mSelectorIndices[i]);
        }
    }

    protected void changeCurrent(int current) {
        if (this.mValue != current) {
            if (this.mWrapSelectorWheel) {
                current = getWrappedSelectorIndex(current);
            }
            int previous = this.mValue;
            setValue(current);
            notifyChange(previous, current);
        }
    }

    private void forceCompleteChangeCurrentByOneViaScroll() {
        Scroller scroller = this.mFlingScroller;
        if (!scroller.isFinished()) {
            int yBeforeAbort = scroller.getCurrY();
            scroller.abortAnimation();
            scrollBy(0, scroller.getCurrY() - yBeforeAbort);
        }
    }

    private void setSelectorWheelState(int selectorWheelState) {
        this.mSelectorWheelState = selectorWheelState;
        if (selectorWheelState == 2) {
            this.mSelectorWheelPaint.setAlpha(26);
        }
        AccessibilityManager acm = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (acm == null) {
            Log.w("AdvancedNumberPicker", "get AccessibilityManager fail");
            return;
        }
        if (this.mFlingable && selectorWheelState == 2 && acm.isEnabled()) {
            try {
                acm.interrupt();
            } catch (Exception e) {
                Log.e("AdvancedNumberPicker", "interrupt warn." + e.getMessage());
            }
            this.mInputText.setContentDescription(this.mContext.getString(R.string.number_picker_increment_scroll_action));
            this.mInputText.sendAccessibilityEvent(4);
            this.mInputText.setContentDescription(null);
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        this.mSelectorTextGapHeight = (int) ((WorldAnalogClock.DEGREE_ONE_HOUR * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
        this.mSelectorElementHeight = this.mTextSize + this.mSelectorTextGapHeight;
        this.mInitialScrollOffset = (this.mInputText.getBaseline() + this.mInputText.getTop()) - (this.mSelectorElementHeight * this.SELECTOR_MIDDLE_ITEM_INDEX);
        this.mCurrentScrollOffset = this.mInitialScrollOffset;
        updateInputTextView();
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller != this.mFlingScroller) {
            updateInputTextView();
        } else if (this.mSelectorWheelState == 2) {
            postAdjustScrollerCommand(0);
            onScrollStateChange(0);
        } else {
            updateInputTextView();
        }
    }

    private void onScrollStateChange(int scrollState) {
        if (this.mScrollState != scrollState) {
            this.mScrollState = scrollState;
            if (this.mOnScrollListener != null) {
                this.mOnScrollListener.onScrollStateChange(this, scrollState);
            }
        }
    }

    private void fling(int velocityY) {
        this.mPreviousScrollerY = 0;
        if (velocityY > 0) {
            this.mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            this.mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }
        invalidate();
    }

    private void hideInputControls() {
        this.mShowInputControlsAnimator.cancel();
        this.mInputText.setVisibility(4);
    }

    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > this.mMaxValue) {
            return (this.mMinValue + ((selectorIndex - this.mMaxValue) % (this.mMaxValue - this.mMinValue))) - 1;
        }
        if (selectorIndex < this.mMinValue) {
            return (this.mMaxValue - ((this.mMinValue - selectorIndex) % (this.mMaxValue - this.mMinValue))) + 1;
        }
        return selectorIndex;
    }

    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex > this.mMaxValue) {
            nextScrollSelectorIndex = this.mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (this.mWrapSelectorWheel && nextScrollSelectorIndex < this.mMinValue) {
            nextScrollSelectorIndex = this.mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = this.mSelectorIndexToStringCache;
        if (((String) cache.get(selectorIndex)) == null) {
            String scrollSelectorValue;
            if (selectorIndex < this.mMinValue || selectorIndex > this.mMaxValue) {
                scrollSelectorValue = "";
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
            cache.put(selectorIndex, scrollSelectorValue);
        }
    }

    private String formatNumber(int value) {
        return this.mFormatter != null ? this.mFormatter.format(value) : String.valueOf(value);
    }

    private void updateInputTextView() {
        this.mInputText.setText(formatNumber(this.mValue));
        AccessibilityManager acm = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        if (this.mFlingable && acm != null && acm.isEnabled()) {
            this.mInputText.setContentDescription(this.mContext.getString(R.string.number_picker_increment_scroll_mode, new Object[]{this.mInputText.getText()}));
        }
    }

    private void notifyChange(int previous, int current) {
        if (this.mOnValueChangeListener != null) {
            this.mOnValueChangeListener.onValueChange(this, previous, this.mValue);
        }
    }

    private void removeAllCallbacks() {
        if (this.mAdjustScrollerCommand != null) {
            removeCallbacks(this.mAdjustScrollerCommand);
        }
    }

    private void postAdjustScrollerCommand(int delayMillis) {
        if (this.mAdjustScrollerCommand == null) {
            this.mAdjustScrollerCommand = new AdjustScrollerCommand();
        } else {
            removeCallbacks(this.mAdjustScrollerCommand);
        }
        postDelayed(this.mAdjustScrollerCommand, (long) delayMillis);
    }

    public void setmOnColorChangeListener(OnColorChangeListener mOnColorChangeListener) {
        this.mOnColorChangeListener = mOnColorChangeListener;
    }
}
