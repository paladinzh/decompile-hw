package com.android.keyguard;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.keyguard.util.HwLog;

public class NumPadKey extends ViewGroup {
    static String[] sKlondike;
    private int mDigit;
    private TextView mDigitText;
    private boolean mEnableHaptics;
    private TextView mKlondikeText;
    private OnClickListener mListener;
    private PowerManager mPM;
    private PasswordTextView mTextView;
    private int mTextViewResId;

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            doHapticKeyClick();
        }
        return false;
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    public NumPadKey(Context context) {
        this(context, null);
    }

    public NumPadKey(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumPadKey(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, R$layout.keyguard_num_pad_key);
    }

    protected NumPadKey(Context context, AttributeSet attrs, int defStyle, int contentResource) {
        super(context, attrs, defStyle);
        this.mDigit = -1;
        this.mListener = new OnClickListener() {
            public void onClick(View thisView) {
                if (NumPadKey.this.mTextView == null && NumPadKey.this.mTextViewResId > 0) {
                    View v = NumPadKey.this.getRootView().findViewById(NumPadKey.this.mTextViewResId);
                    if (v != null && (v instanceof PasswordTextView)) {
                        NumPadKey.this.mTextView = (PasswordTextView) v;
                    }
                }
                if (NumPadKey.this.mTextView == null || !NumPadKey.this.mTextView.isEnabled()) {
                    HwLog.w("NumPadKey", "skip input.");
                } else {
                    NumPadKey.this.mTextView.append(Character.forDigit(NumPadKey.this.mDigit, 10));
                }
                NumPadKey.this.userActivity();
            }
        };
        setFocusable(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.NumPadKey);
        try {
            this.mDigit = a.getInt(R$styleable.NumPadKey_digit, this.mDigit);
            this.mTextViewResId = a.getResourceId(R$styleable.NumPadKey_textView, 0);
            setOnClickListener(this.mListener);
            setOnHoverListener(new LiftToActivateListener(context));
            setAccessibilityDelegate(new ObscureSpeechDelegate(context));
            this.mEnableHaptics = new LockPatternUtils(context).isTactileFeedbackEnabled();
            this.mPM = (PowerManager) this.mContext.getSystemService("power");
            ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(contentResource, this, true);
            this.mDigitText = (TextView) findViewById(R$id.digit_text);
            this.mDigitText.setText(Integer.toString(this.mDigit));
            this.mKlondikeText = (TextView) findViewById(R$id.klondike_text);
            if (this.mDigit >= 0) {
                if (sKlondike == null) {
                    sKlondike = getResources().getStringArray(R$array.lockscreen_num_pad_klondike);
                }
                if (sKlondike != null && sKlondike.length > this.mDigit) {
                    String klondike = sKlondike[this.mDigit];
                    if (klondike.length() > 0) {
                        this.mKlondikeText.setText(klondike);
                        if (this.mDigit == 0) {
                            this.mKlondikeText.setTextSize(16.0f);
                        }
                    } else {
                        this.mKlondikeText.setVisibility(4);
                    }
                }
            }
            a = context.obtainStyledAttributes(attrs, R.styleable.View);
            if (!a.hasValueOrEmpty(13)) {
                setBackground(this.mContext.getDrawable(R$drawable.ripple_drawable));
            }
            a.recycle();
            setContentDescription(this.mDigitText.getText().toString());
        } finally {
            a.recycle();
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ObscureSpeechDelegate.setAnnouncedHeadset(false);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int digitHeight = this.mDigitText.getMeasuredHeight();
        int klondikeHeight = this.mKlondikeText.getMeasuredHeight();
        int top = (getHeight() / 2) - ((digitHeight + klondikeHeight) / 2);
        int centerX = getWidth() / 2;
        int left = centerX - (this.mDigitText.getMeasuredWidth() / 2);
        int bottom = top + digitHeight;
        this.mDigitText.layout(left, top, this.mDigitText.getMeasuredWidth() + left, bottom);
        top = (int) (((float) bottom) - (((float) klondikeHeight) * 0.35f));
        left = centerX - (this.mKlondikeText.getMeasuredWidth() / 2);
        this.mKlondikeText.layout(left, top, this.mKlondikeText.getMeasuredWidth() + left, top + klondikeHeight);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }
}
