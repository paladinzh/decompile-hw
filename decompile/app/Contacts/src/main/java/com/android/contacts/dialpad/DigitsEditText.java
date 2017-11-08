package com.android.contacts.dialpad;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.google.android.gms.R;

public class DigitsEditText extends EditText {
    private static float CHANGING_FACTOR = 2.0f;
    private static float MIN_SCALE_FACTOR = 0.5926f;
    private static float MIN_SCALE_FACTOR_HINTTEXT = 0.562f;
    private Rect mBounds;
    private float mDensityFactor = getResources().getDisplayMetrics().density;
    private float mMaxFontSize;
    private float mMinFontSize;
    private float mMinFontSizeHintText;
    private Paint mPaint;
    private float mPrefferedHintTextSize;

    public DigitsEditText(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        setInputType(getInputType() | 524288);
        setShowSoftInputOnFocus(false);
        this.mMaxFontSize = context.getResources().getDimension(R.dimen.dialer_num_text_size);
        this.mPrefferedHintTextSize = context.getResources().getDimension(R.dimen.emergency_hint_text_size);
        this.mPaint = new Paint();
        this.mBounds = new Rect();
        this.mMinFontSize = this.mMaxFontSize * MIN_SCALE_FACTOR;
        this.mMinFontSizeHintText = this.mPrefferedHintTextSize * MIN_SCALE_FACTOR_HINTTEXT;
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        if (imm != null && imm.isActive(this)) {
            imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        if (imm != null && imm.isActive(this)) {
            imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
        }
        return ret;
    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (event.getEventType() != 8) {
            super.sendAccessibilityEventUnchecked(event);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        adjustFontSize();
    }

    public void adjustFontSize() {
        int width = (getWidth() - getPaddingLeft()) - getPaddingRight();
        if (getResources().getConfiguration().orientation == 2) {
            width = (getContext().getResources().getDisplayMetrics().widthPixels - getResources().getDimensionPixelSize(R.dimen.dialpad_huawei_header_edit_text_margin_start)) - getResources().getDimensionPixelSize(R.dimen.dialpad_huawei_header_edit_text_margin_end);
        }
        String text = getText().toString();
        int textlength = text.length();
        if (width <= 0) {
            return;
        }
        float currentSize;
        if (textlength > 0) {
            currentSize = getTextSize();
            this.mPaint.setTextSize(currentSize);
            this.mPaint.getTextBounds(text, 0, textlength, this.mBounds);
            if (width < this.mBounds.width() && currentSize > this.mMinFontSize) {
                do {
                    currentSize -= CHANGING_FACTOR;
                    this.mPaint.setTextSize(currentSize);
                    this.mPaint.getTextBounds(text, 0, textlength, this.mBounds);
                    if (width >= this.mBounds.width()) {
                        break;
                    }
                } while (currentSize > this.mMinFontSize);
            } else {
                while (width > this.mBounds.width() && currentSize < this.mMaxFontSize) {
                    currentSize += CHANGING_FACTOR;
                    this.mPaint.setTextSize(currentSize);
                    this.mPaint.getTextBounds(text, 0, textlength, this.mBounds);
                }
                if (currentSize > this.mMinFontSize) {
                    currentSize -= CHANGING_FACTOR;
                }
            }
            setTextSize(1, currentSize / this.mDensityFactor);
            return;
        }
        CharSequence hint = getHint();
        if (hint != null) {
            String hintText = hint.toString();
            textlength = hint.length();
            if (textlength > 0) {
                currentSize = this.mPrefferedHintTextSize;
                this.mPaint.setTextSize(currentSize);
                this.mPaint.getTextBounds(hintText, 0, textlength, this.mBounds);
                if (width < this.mBounds.width() && currentSize > this.mMinFontSizeHintText) {
                    do {
                        this.mPaint.setTextSize(currentSize);
                        currentSize -= CHANGING_FACTOR;
                        this.mPaint.getTextBounds(hintText, 0, textlength, this.mBounds);
                        if (width >= this.mBounds.width()) {
                            break;
                        }
                    } while (currentSize > this.mMinFontSizeHintText);
                }
                setTextSize(1, currentSize / this.mDensityFactor);
            }
        }
    }
}
