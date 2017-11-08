package com.huawei.mms.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.rcs.RcsCommonConfig;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitor;
import com.huawei.mms.ui.SpandLinkMovementMethod.SpandTouchMonitorEx;
import com.huawei.mms.util.SafetySmsParser;
import com.huawei.mms.util.TextSpan;
import com.huawei.rcs.ui.RcsSpandTextView;
import java.util.List;

public class SpandTextView extends TextView implements SpandTouchMonitorEx {
    private static int DOUBLE_TAP_MIN_TIME = ViewConfiguration.getDoubleTapMinTime();
    private static int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static int sDoubleTapSlopSquare = 0;
    private ClickableSpan mClickedSpan;
    private CryptoSpandTextView mCryptoSpandTextView = new CryptoSpandTextView();
    private int mDeltaPos = 0;
    private long mDownTime = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    if (SpandTextView.this.mSpanTouchMonitor != null && !SpandTextView.this.mIsLongPress && !SpandTextView.this.mIsLinkPress) {
                        SpandTextView.this.mSpanTouchMonitor.onTouchOutsideSpanText();
                        return;
                    }
                    return;
                case 102:
                    SpandTextView.this.mIsLinkPress = true;
                    if (SpandTextView.this.mClickedSpan != null && !SpandTextView.this.mIsLongPress) {
                        SpandTextView.this.mClickedSpan.onClick(SpandTextView.this);
                        return;
                    }
                    return;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    if (SpandTextView.this.mClickedSpan == null) {
                        return;
                    }
                    if (SpandTextView.this.mClickedSpan instanceof AddressClickableSpan) {
                        ((AddressClickableSpan) SpandTextView.this.mClickedSpan).onPress(SpandTextView.this);
                        return;
                    } else if (SpandTextView.this.mClickedSpan instanceof DateClickableSpan) {
                        ((DateClickableSpan) SpandTextView.this.mClickedSpan).onPress(SpandTextView.this);
                        return;
                    } else {
                        return;
                    }
                default:
                    throw new RuntimeException("Unknown message " + msg);
            }
        }
    };
    private RcsSpandTextView mHwCustSpandTextView;
    private boolean mInSelectionMode = false;
    private boolean mIsClickIntercepted = false;
    private boolean mIsLinkPress = false;
    private boolean mIsLongPress = false;
    private int mPrevPosX = -100;
    private int mPrevPosY = -100;
    private long mPrevUpTime = 0;
    private SelectionDoneCallback mSelectionDoneCallBack = null;
    List<TextSpan> mSpanList = null;
    SpandTouchMonitor mSpanTouchMonitor = null;
    private SpandLinkMovementMethod mSpandLinkMovementMethod;
    SpannableStringBuilder mStrBuilder = new SpannableStringBuilder();

    public interface SelectionDoneCallback {
        void onSelectionDone();
    }

    public RcsSpandTextView getHwCust() {
        return this.mHwCustSpandTextView;
    }

    public SpandTextView(Context context) {
        super(context);
        init(context);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCustSpandTextView == null) {
            this.mHwCustSpandTextView = new RcsSpandTextView();
        }
    }

    public SpandTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCustSpandTextView == null) {
            this.mHwCustSpandTextView = new RcsSpandTextView();
        }
    }

    public SpandTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCustSpandTextView == null) {
            this.mHwCustSpandTextView = new RcsSpandTextView();
        }
    }

    private void init(Context context) {
        this.mSpandLinkMovementMethod = new SpandLinkMovementMethod(context, this);
        checkConfiguration(context);
    }

    private static void checkConfiguration(Context context) {
        if (sDoubleTapSlopSquare == 0) {
            float doubleTapTouchSlop = (float) ViewConfiguration.get(context).getScaledDoubleTapSlop();
            sDoubleTapSlopSquare = (int) (doubleTapTouchSlop * doubleTapTouchSlop);
        }
    }

    public void setSpandTouchMonitor(SpandTouchMonitor realMonitor) {
        this.mSpanTouchMonitor = realMonitor;
    }

    public void rsetSpanList() {
        this.mSpanList = null;
    }

    public void setText(CharSequence text, List<TextSpan> textSpan) {
        this.mSpanList = textSpan;
        super.setText(text);
        if (this.mHwCustSpandTextView != null) {
            this.mHwCustSpandTextView.setSpanList(this.mSpanList);
        }
        this.mCryptoSpandTextView.setEncryptSpanList(this.mSpanList);
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        CharSequence sequence = getText();
        if (!TextUtils.isEmpty(sequence)) {
            if (this.mSpanList == null || this.mSpanList.size() == 0) {
                setMovementMethod(this.mSpandLinkMovementMethod.resetSpandLinkMovementMethod(getContext(), this));
                return;
            }
            this.mStrBuilder.clear();
            if (this.mHwCustSpandTextView == null || !this.mHwCustSpandTextView.isRcsSwitchOn()) {
                this.mStrBuilder.append(text);
            } else {
                this.mStrBuilder = new SpannableStringBuilder(text);
            }
            for (TextSpan span : this.mSpanList) {
                ClickableSpan spanClick = createSpandClickable(getContext(), span, sequence);
                if (spanClick != null) {
                    int start = span.getStart();
                    int end = span.getEnd();
                    if (end > this.mStrBuilder.length()) {
                        end = this.mStrBuilder.length();
                    }
                    if (start > end) {
                        start = end;
                    }
                    this.mStrBuilder.setSpan(spanClick, start, end, 33);
                }
            }
            SafetySmsParser.getInstance().appendSafetySmsSpan(this.mStrBuilder, this.mSpanList, getContext());
            SafetySmsParser.getInstance().appendRiskSpan(this.mStrBuilder, this.mSpanList);
            super.setText(this.mStrBuilder, type);
            setMovementMethod(this.mSpandLinkMovementMethod.resetSpandLinkMovementMethod(getContext(), this));
        }
    }

    public static ClickableSpan createSpandClickable(Context context, TextSpan span, CharSequence sequence) {
        if (span.getSpanType() == 1) {
            return new TelephoneClickableSpan(context, span.getUrl());
        }
        if (span.getSpanType() == 2) {
            return new EmailClickableSpan(context, span.getUrl(), sequence);
        }
        if (span.getSpanType() == 0) {
            return new BrowserClickableSpan(context, span.getUrl(), sequence);
        }
        if (span.getSpanType() == 3) {
            return new AddressClickableSpan(context, span.getUrl());
        }
        if (span.getSpanType() == 4) {
            return new DateClickableSpan(context, span, sequence);
        }
        if (span.getSpanType() == -6) {
            return new RiskUrlClickableSpan(context, span.getUrl(), sequence);
        }
        if (span.getSpanType() == -7) {
            return new UnoffcialUrlClickableSpan(context, span.getUrl(), sequence);
        }
        return null;
    }

    public void onTouchLink(ClickableSpan span) {
        this.mClickedSpan = span;
        boolean hadTapMessage = this.mHandler.hasMessages(102);
        if (hadTapMessage) {
            this.mHandler.removeMessages(102);
        }
        if (!hadTapMessage || this.mDeltaPos >= sDoubleTapSlopSquare) {
            this.mHandler.sendEmptyMessageDelayed(102, (long) DOUBLE_TAP_TIMEOUT);
        } else {
            onDoubleTapUp(true);
        }
    }

    public void onPressureLink(ClickableSpan span) {
        this.mClickedSpan = span;
        if (this.mHandler.hasMessages(OfflineMapStatus.EXCEPTION_SDCARD)) {
            this.mHandler.removeMessages(OfflineMapStatus.EXCEPTION_SDCARD);
        }
        this.mHandler.sendEmptyMessage(OfflineMapStatus.EXCEPTION_SDCARD);
    }

    public SpandTextView getTextView() {
        return this;
    }

    public void onTouchOutsideSpanText() {
        boolean hadTapMessage = this.mHandler.hasMessages(101);
        if (hadTapMessage) {
            this.mHandler.removeMessages(101);
        }
        if (!hadTapMessage || this.mDeltaPos >= sDoubleTapSlopSquare) {
            this.mHandler.sendEmptyMessageDelayed(101, (long) DOUBLE_TAP_TIMEOUT);
        } else {
            onDoubleTapUp(false);
        }
    }

    public boolean onDoubleTapUp(boolean isLink) {
        return this.mSpanTouchMonitor != null ? this.mSpanTouchMonitor.onDoubleTapUp(isLink) : false;
    }

    public boolean performClick() {
        return true;
    }

    public boolean performLongClick() {
        this.mIsLongPress = true;
        return super.performLongClick();
    }

    private void onSpanTextTouchUp() {
        if (!this.mIsLongPress) {
            this.mCryptoSpandTextView.onSpandTextViewClickEvent(this.mDeltaPos < sDoubleTapSlopSquare);
        }
    }

    public void onSpanTextPressed(boolean pressed) {
        setSelected(pressed);
        setPressed(pressed);
    }

    public boolean isEditTextClickable() {
        return this.mSpanTouchMonitor == null ? true : this.mSpanTouchMonitor.isEditTextClickable();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mSpanTouchMonitor == null) {
            return super.onTouchEvent(event);
        }
        if (!this.mSpanTouchMonitor.isEditTextClickable() || this.mIsClickIntercepted) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                this.mSpanTouchMonitor.onSpanTextPressed(true);
                this.mDownTime = event.getDownTime();
                int posX = (int) event.getX();
                int posY = (int) event.getY();
                long deltaTime = this.mDownTime - this.mPrevUpTime;
                if (deltaTime >= ((long) DOUBLE_TAP_TIMEOUT) || deltaTime <= ((long) DOUBLE_TAP_MIN_TIME)) {
                    this.mDeltaPos = sDoubleTapSlopSquare;
                } else {
                    int deltaX = posX - this.mPrevPosX;
                    int deltaY = posY - this.mPrevPosY;
                    this.mDeltaPos = (deltaX * deltaX) + (deltaY * deltaY);
                }
                this.mPrevPosX = (int) event.getX();
                this.mPrevPosY = (int) event.getY();
                this.mIsLongPress = false;
                this.mIsLinkPress = false;
                break;
            case 1:
                this.mSpanTouchMonitor.onSpanTextPressed(false);
                this.mPrevUpTime = event.getEventTime();
                onSpanTextTouchUp();
                break;
            case 3:
            case 4:
                this.mSpanTouchMonitor.onSpanTextPressed(false);
                this.mPrevUpTime = event.getEventTime();
                break;
        }
        super.onTouchEvent(event);
        return true;
    }

    public void switchToSelectionMode() {
        setMovementMethod(ArrowKeyMovementMethod.getInstance());
        setLongClickable(true);
        this.mInSelectionMode = true;
        setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                Selection.setSelection((Spannable) SpandTextView.this.getText(), 0, SpandTextView.this.getText().length());
                return false;
            }
        });
    }

    public void switchToSpanClickMode() {
        if (this.mInSelectionMode) {
            this.mInSelectionMode = false;
            if (this.mSelectionDoneCallBack != null) {
                this.mSelectionDoneCallBack.onSelectionDone();
                this.mSelectionDoneCallBack = null;
            }
            post(new Runnable() {
                public void run() {
                    SpandTextView.this.setMovementMethod(SpandTextView.this.mSpandLinkMovementMethod.resetSpandLinkMovementMethod(SpandTextView.this.getContext(), SpandTextView.this));
                    long now = SystemClock.uptimeMillis();
                    super.onTouchEvent(MotionEvent.obtain(now, now, 1, 0.0f, 0.0f, 0));
                }
            });
        }
    }

    protected void stopTextActionMode() {
        super.stopTextActionMode();
        switchToSpanClickMode();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            switchToSpanClickMode();
        }
    }

    public CryptoSpandTextView getCryptoSpandTextView() {
        return this.mCryptoSpandTextView;
    }

    public void setIsClickIntercepted(boolean isIntercepted) {
        this.mIsClickIntercepted = isIntercepted;
    }
}
