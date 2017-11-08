package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsConfig;
import com.huawei.mms.util.ActivityExWrapper;
import com.huawei.mms.util.TouchForceManagerWrapper;

public class SpandLinkMovementMethod implements MovementMethod {
    private ActivityExWrapper mActivityExWrapper;
    boolean mChangeOldText = true;
    Context mContext;
    private ClickableSpan mCurrentClickableSpan;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 2009:
                    final View parent = SpandLinkMovementMethod.this.getParrentView();
                    if (parent instanceof ViewGroup) {
                        ((Activity) SpandLinkMovementMethod.this.mContext).runOnUiThread(new Runnable() {
                            public void run() {
                                ((ViewGroup) parent).removeView(SpandLinkMovementMethod.this.mPreviewBackground);
                                SpandLinkMovementMethod.this.resetView();
                                SpandLinkMovementMethod.this.mInPrepareMode = false;
                            }
                        });
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mInPrepareMode = false;
    boolean mIsConsumedByPress = false;
    private boolean mIsSupportPressure = false;
    private long mLongClickTime = ((long) ViewConfiguration.getLongPressTimeout());
    private CharSequence mOldText;
    private TextView mPreTextView;
    private View mPreviewBackground;
    SpandTouchMonitor mSpanTouchMonitor = null;
    private SpandTextView mSpandTextView;
    private SpannableStringBuilder mStrBuilder = new SpannableStringBuilder();
    long mTickClickDown = 0;
    private TouchForceManagerWrapper mTouchForceManagerWrapper;
    private View mViewParrent;
    MovementMethod target = LinkMovementMethod.getInstance();

    public interface SpandTouchMonitor {
        boolean isEditTextClickable();

        boolean onDoubleTapUp(boolean z);

        void onSpanTextPressed(boolean z);

        void onTouchLink(ClickableSpan clickableSpan);

        void onTouchOutsideSpanText();
    }

    public interface SpandTouchMonitorEx extends SpandTouchMonitor {
        SpandTextView getTextView();

        void onPressureLink(ClickableSpan clickableSpan);
    }

    public SpandLinkMovementMethod(Context context, SpandTouchMonitor touchOutsideHandler) {
        this.mSpanTouchMonitor = touchOutsideHandler;
        this.mContext = context;
        this.mPreviewBackground = getPreBackgroundView(context);
        if (this.mSpanTouchMonitor instanceof SpandTouchMonitorEx) {
            this.mSpandTextView = ((SpandTouchMonitorEx) this.mSpanTouchMonitor).getTextView();
            this.mPreTextView = new TextView(this.mContext);
            this.mPreTextView.setTextColor(-1);
            initPreTextView();
        }
        this.mTouchForceManagerWrapper = new TouchForceManagerWrapper(this.mContext);
        try {
            this.mIsSupportPressure = this.mTouchForceManagerWrapper.isSupportForce();
        } catch (Exception e) {
            Log.d("SpandLinkMovementMethod", e.getMessage());
        }
        if (this.mIsSupportPressure) {
            this.mLongClickTime = 800;
        }
        this.mActivityExWrapper = new ActivityExWrapper((Activity) this.mContext);
    }

    public SpandLinkMovementMethod resetSpandLinkMovementMethod(Context context, SpandTouchMonitor touchOutsideHandler) {
        this.mSpanTouchMonitor = touchOutsideHandler;
        this.mContext = context;
        this.mPreviewBackground = getPreBackgroundView(context);
        if (this.mSpanTouchMonitor instanceof SpandTouchMonitorEx) {
            this.mSpandTextView = ((SpandTouchMonitorEx) this.mSpanTouchMonitor).getTextView();
            if (this.mPreTextView == null) {
                this.mPreTextView = new TextView(this.mContext);
                this.mPreTextView.setTextColor(-1);
            }
            initPreTextView();
        }
        return this;
    }

    private void initPreTextView() {
        this.mPreTextView.setTextSize(0, this.mSpandTextView.getTextSize());
        if (this.mChangeOldText) {
            this.mOldText = this.mSpandTextView.getText();
            this.mPreTextView.setText(this.mOldText);
        }
        int[] locationInWindow = new int[2];
        this.mSpandTextView.getLocationOnScreen(locationInWindow);
        LayoutParams params = new LayoutParams(this.mSpandTextView.getWidth(), this.mSpandTextView.getHeight());
        params.leftMargin = locationInWindow[0];
        params.topMargin = locationInWindow[1];
        this.mPreTextView.setLayoutParams(params);
        this.mPreTextView.setLineSpacing(0.0f, this.mSpandTextView.getLineSpacingMultiplier());
    }

    public void initialize(TextView widget, Spannable text) {
        this.target.initialize(widget, text);
    }

    public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event) {
        this.target.onKeyDown(widget, text, keyCode, event);
        return false;
    }

    public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event) {
        this.target.onKeyUp(widget, text, keyCode, event);
        return false;
    }

    public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
        this.target.onKeyOther(view, text, event);
        return false;
    }

    public void onTakeFocus(TextView widget, Spannable text, int direction) {
        this.target.onTakeFocus(widget, text, direction);
    }

    public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event) {
        this.target.onTrackballEvent(widget, text, event);
        return false;
    }

    public boolean onGenericMotionEvent(TextView widget, Spannable text, MotionEvent event) {
        this.target.onGenericMotionEvent(widget, text, event);
        return false;
    }

    public boolean canSelectArbitrarily() {
        this.target.canSelectArbitrarily();
        return false;
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        boolean ret = false;
        int x = (((int) event.getX()) - widget.getTotalPaddingLeft()) + widget.getScrollX();
        int y = (((int) event.getY()) - widget.getTotalPaddingTop()) + widget.getScrollY();
        Layout layout = widget.getLayout();
        int off = layout.getOffsetForHorizontal(layout.getLineForVertical(y), (float) x);
        boolean isNotLongPress = true;
        ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
        if (action == 0) {
            this.mActivityExWrapper.run("setScreenShotFromDecorView");
            initParams();
            this.mIsConsumedByPress = false;
            this.mSpanTouchMonitor.onSpanTextPressed(true);
            this.mTickClickDown = System.currentTimeMillis();
            if (link.length > 0) {
                this.mCurrentClickableSpan = link[0];
            }
        } else if (action == 2) {
            isNotLongPress = System.currentTimeMillis() < this.mTickClickDown + this.mLongClickTime;
            ret = checkPressureAction(event, false, this.mCurrentClickableSpan);
        } else if (action == 1) {
            exitPreviewMode();
            this.mSpanTouchMonitor.onSpanTextPressed(false);
            isNotLongPress = true;
            if (link.length > 0 && !this.mIsConsumedByPress) {
                this.mSpanTouchMonitor.onTouchLink(link[0]);
            }
            if (null == null && !this.mIsConsumedByPress) {
                this.mSpanTouchMonitor.onTouchOutsideSpanText();
            }
            ret = true;
            this.mIsConsumedByPress = false;
        } else if (action == 3 || action == 4) {
            exitPreviewMode();
            this.mSpanTouchMonitor.onSpanTextPressed(false);
            this.mIsConsumedByPress = false;
        } else {
            exitPreviewMode();
        }
        if (!isNotLongPress) {
            exitPreviewMode();
        }
        return ret;
    }

    private boolean checkPressureAction(MotionEvent event, boolean ret, ClickableSpan clickableSpan) {
        if (!this.mIsSupportPressure) {
            return ret;
        }
        float currentPressure = event.getPressure();
        float level1PrePressure = MmsConfig.getPrePressureThreshold();
        float level1Pressure = MmsConfig.getPressureThreshold();
        boolean isReadyToResponsePressure = System.currentTimeMillis() > this.mTickClickDown + 200;
        boolean isReadyToResponsePrePressure = System.currentTimeMillis() > this.mTickClickDown + 50;
        if (currentPressure <= level1PrePressure || currentPressure >= level1Pressure || this.mIsConsumedByPress || clickableSpan == null || !isReadyToResponsePrePressure) {
            if (currentPressure > level1Pressure && !this.mIsConsumedByPress && clickableSpan != null && isReadyToResponsePressure && (this.mSpanTouchMonitor instanceof SpandTouchMonitorEx) && isSpecialClickSpan(clickableSpan)) {
                this.mPreviewBackground.setBackgroundColor(Color.argb(178, 0, 0, 0));
                delayExitPreviewMode();
                this.mIsConsumedByPress = true;
                ret = true;
                ((SpandTouchMonitorEx) this.mSpanTouchMonitor).onPressureLink(clickableSpan);
            }
        } else if ((this.mSpanTouchMonitor instanceof SpandTouchMonitorEx) && isSpecialClickSpan(clickableSpan)) {
            enterPrepareMode(clickableSpan);
            updateByProgress(event, 178);
            ret = true;
        }
        return ret;
    }

    private boolean isSpecialClickSpan(ClickableSpan clickableSpan) {
        if ((clickableSpan instanceof AddressClickableSpan) || (clickableSpan instanceof DateClickableSpan) || (clickableSpan instanceof BrowserClickableSpan)) {
            return true;
        }
        return false;
    }

    private void initParams() {
        resetView();
    }

    private void resetView() {
        this.mPreviewBackground.setBackgroundColor(0);
        if (this.mPreTextView != null) {
            this.mPreTextView.setScaleX(ContentUtil.FONT_SIZE_NORMAL);
            this.mPreTextView.setScaleY(ContentUtil.FONT_SIZE_NORMAL);
        }
    }

    private void updateByProgress(MotionEvent event, int backgroundColor) {
        float progress = event.getPressure() * 0.5f;
        if (progress < 0.0f) {
            progress = 0.0f;
        }
        if (progress > ContentUtil.FONT_SIZE_NORMAL) {
            progress = ContentUtil.FONT_SIZE_NORMAL;
        }
        int alpha = (int) ((6.0f * progress) * ((float) backgroundColor));
        if (alpha > 178) {
            alpha = 178;
        }
        this.mPreTextView.getLocationOnScreen(new int[2]);
        this.mPreviewBackground.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        float scale = progress + ContentUtil.FONT_SIZE_NORMAL;
        if (scale > 1.1f) {
            scale = 1.1f;
        }
        this.mPreTextView.setScaleX(scale);
        this.mPreTextView.setScaleY(scale);
    }

    private void enterPrepareMode(ClickableSpan clickablespan) {
        if (!this.mInPrepareMode) {
            this.mInPrepareMode = true;
            if (!(this.mSpandTextView == null || this.mPreTextView == null)) {
                changeTextColor(clickablespan);
                changePreTextViewStyle();
            }
            View parent = getParrentView();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).addView(this.mPreviewBackground);
                if (this.mPreTextView != null) {
                    this.mPreTextView.setTextColor(-1);
                    ((ViewGroup) parent).addView(this.mPreTextView);
                }
            }
        }
    }

    private void changeTextColor(ClickableSpan clickableSpan) {
        if (clickableSpan != null) {
            this.mStrBuilder.clear();
            this.mStrBuilder.append(this.mOldText);
            int start = this.mStrBuilder.getSpanStart(clickableSpan);
            int end = this.mStrBuilder.getSpanEnd(clickableSpan);
            if (start >= 0 || end >= 0) {
                if (end > this.mStrBuilder.length()) {
                    end = this.mStrBuilder.length();
                }
                if (start > end) {
                    start = end;
                }
                if (end > this.mStrBuilder.length()) {
                    end = this.mStrBuilder.length();
                }
                if (start > end) {
                    start = end;
                }
                this.mStrBuilder.clearSpans();
                if (start > 0) {
                    this.mStrBuilder.setSpan(new ForegroundColorSpan(0), 0, start, 33);
                }
                TextPaint textPaint = new TextPaint();
                textPaint.setColor(-1);
                clickableSpan.updateDrawState(textPaint);
                this.mStrBuilder.setSpan(clickableSpan, start, end, 33);
                if (end < this.mStrBuilder.length()) {
                    this.mStrBuilder.setSpan(new ForegroundColorSpan(0), end, this.mStrBuilder.length(), 33);
                }
                this.mPreTextView.setText(this.mStrBuilder);
                this.mStrBuilder.clear();
                this.mStrBuilder.append(this.mOldText);
                this.mStrBuilder.removeSpan(clickableSpan);
                this.mStrBuilder.setSpan(new ForegroundColorSpan(0), start, end, 33);
                this.mChangeOldText = false;
                this.mSpandTextView.setText(this.mStrBuilder);
                this.mChangeOldText = true;
            }
        }
    }

    private void changePreTextViewStyle() {
        float textSize = this.mSpandTextView.getTextSize();
        int[] locationInWindow = new int[2];
        this.mSpandTextView.getLocationOnScreen(locationInWindow);
        this.mPreTextView.setTextSize(0, textSize);
        LayoutParams params = new LayoutParams(this.mSpandTextView.getWidth(), this.mSpandTextView.getHeight());
        params.leftMargin = locationInWindow[0];
        params.topMargin = locationInWindow[1];
        this.mPreTextView.setLayoutParams(params);
    }

    private void delayExitPreviewMode() {
        if (this.mInPrepareMode) {
            this.mInPrepareMode = false;
            View parent = getParrentView();
            if ((parent instanceof ViewGroup) && this.mPreTextView != null) {
                ((ViewGroup) parent).removeView(this.mPreTextView);
            }
            this.mSpandTextView.setText(this.mOldText);
            this.mHandler.sendEmptyMessageDelayed(2009, 400);
        }
    }

    public void exitPreviewMode() {
        if (this.mInPrepareMode) {
            View parent = getParrentView();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mPreviewBackground);
                if (this.mPreTextView != null) {
                    ((ViewGroup) parent).removeView(this.mPreTextView);
                }
            }
            this.mSpandTextView.setText(this.mOldText);
            resetView();
            this.mInPrepareMode = false;
        }
    }

    private View getParrentView() {
        if (this.mViewParrent == null) {
            this.mViewParrent = ((Activity) this.mContext).getWindow().getDecorView();
        }
        return this.mViewParrent;
    }

    private View getPreBackgroundView(Context context) {
        if (this.mPreviewBackground == null) {
            this.mPreviewBackground = new View(context);
            this.mPreviewBackground.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        }
        return this.mPreviewBackground;
    }
}
