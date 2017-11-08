package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LockScreenCallback;
import com.huawei.keyguard.events.CallLogMonitor;
import com.huawei.keyguard.events.CallLogMonitor.CallLogInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.MessageMonitor;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.util.EventViewHelper;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;

public class EventNotifyView extends LinearLayout {
    private ArrowLayout mArrowLeft;
    private ArrowLayout mArrowRight;
    private int mCallNum;
    private TextView mCallNumText;
    private float mDownX;
    private float mDownY;
    private int mDragMode = 1;
    private ViewGroup mDragParent;
    private ShadowView mDragView;
    private float mFractionUnlockMms;
    private boolean mIsUnlock;
    private View mLayoutCall;
    private View mLayoutMms;
    private HwUnlockInterface$LockScreenCallback mLockScreenCallback;
    private int mMmsNum;
    private TextView mMmsNumText;
    private View mSeparatorView;
    private int mUnlockDistanceMms;
    HwUpdateCallback mUpdateCallback = new HwUpdateCallback() {
        public void onNewMessageChange(MessageInfo info) {
            if (info == null) {
                HwLog.i("EventNotifyView", "onNewMessageChange info is null - no change happened");
                return;
            }
            HwLog.i("EventNotifyView", "onNewMessageChange missedCount=" + info.getUnReadCount());
            EventNotifyView.this.onNewMmsUpdate(info.getUnReadCount());
        }

        public void onCalllogChange(CallLogInfo info) {
            if (info == null) {
                HwLog.i("EventNotifyView", "onCalllogChange info is null - no change happened");
                return;
            }
            HwLog.i("EventNotifyView", "onCalllogChange missedCount=" + info.getMissedcount());
            EventNotifyView.this.onMissCallUpdate(info.getMissedcount());
        }
    };

    private class DragListener implements OnTouchListener {
        Rect mTargetRect;
        View mTargetView;

        public DragListener(View view) {
            this.mTargetView = view;
            if (this.mTargetView == null) {
                HwLog.w("EventNotifyView", "DragListener mTargetView is null");
            }
            this.mTargetRect = new Rect();
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (EventNotifyView.this.mIsUnlock || this.mTargetView == null) {
                return false;
            }
            float x = event.getRawX();
            float y = event.getRawY();
            switch (event.getAction()) {
                case 0:
                    EventNotifyView.this.mDownX = x;
                    EventNotifyView.this.mDownY = y;
                    EventNotifyView.this.mDragView = ShadowView.createShadow(this.mTargetView);
                    this.mTargetView.getGlobalVisibleRect(this.mTargetRect);
                    EventNotifyView.this.mDragView.setTranslationX((float) this.mTargetRect.left);
                    EventNotifyView.this.mDragView.setTranslationY((float) this.mTargetRect.top);
                    EventNotifyView.this.onDrag(true);
                    break;
                case 1:
                case 3:
                    EventNotifyView.this.onDrag(false);
                    break;
                case 2:
                    if (EventNotifyView.this.mDragView != null) {
                        EventNotifyView.this.mDragView.setTranslationX((x - EventNotifyView.this.mDownX) + ((float) this.mTargetRect.left));
                        if (EventNotifyView.this.mDragMode == 2) {
                            EventNotifyView.this.mDragView.setTranslationY((y - EventNotifyView.this.mDownY) + ((float) this.mTargetRect.top));
                        }
                    }
                    if (EventNotifyView.this.mDragParent != null) {
                        if (EventNotifyView.this.mUnlockDistanceMms == 0) {
                            EventNotifyView.this.mUnlockDistanceMms = (int) (EventNotifyView.this.mFractionUnlockMms * ((float) EventNotifyView.this.mDragParent.getWidth()));
                            if (EventNotifyView.this.mDragMode == 2) {
                                EventNotifyView eventNotifyView = EventNotifyView.this;
                                eventNotifyView.mUnlockDistanceMms = eventNotifyView.mUnlockDistanceMms * EventNotifyView.this.mUnlockDistanceMms;
                            }
                        }
                        if (EventNotifyView.this.mDragMode == 2) {
                            EventNotifyView.this.mIsUnlock = (Math.abs(x - EventNotifyView.this.mDownX) * Math.abs(x - EventNotifyView.this.mDownX)) + (Math.abs(y - EventNotifyView.this.mDownY) * Math.abs(y - EventNotifyView.this.mDownY)) > ((float) EventNotifyView.this.mUnlockDistanceMms);
                        } else {
                            EventNotifyView.this.mIsUnlock = Math.abs(x - EventNotifyView.this.mDownX) > ((float) EventNotifyView.this.mUnlockDistanceMms);
                        }
                        if (EventNotifyView.this.mIsUnlock) {
                            EventNotifyView.this.onDrag(false);
                            EventNotifyView.this.mLockScreenCallback.onTrigger(EventNotifyView.this.getIntent(this.mTargetView), null);
                            break;
                        }
                    }
                    break;
            }
            return true;
        }
    }

    public EventNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(393216);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initLockViews();
    }

    private void initLockViews() {
        this.mCallNum = 0;
        this.mMmsNum = 0;
        this.mArrowLeft = (ArrowLayout) findViewById(R$id.arrowleft);
        this.mArrowRight = (ArrowLayout) findViewById(R$id.arrowright);
        this.mFractionUnlockMms = getResources().getDimension(R$dimen.event_unlock_distance);
        this.mLayoutMms = findViewById(R$id.event_mms);
        if (this.mLayoutMms != null) {
            this.mLayoutMms.setOnTouchListener(new DragListener(this.mLayoutMms));
        }
        this.mLayoutCall = findViewById(R$id.event_call);
        if (this.mLayoutCall != null) {
            this.mLayoutCall.setOnTouchListener(new DragListener(this.mLayoutCall));
        }
        this.mMmsNumText = (TextView) findViewById(R$id.mmsNum);
        this.mCallNumText = (TextView) findViewById(R$id.callNum);
        this.mSeparatorView = (ImageView) findViewById(R$id.separator);
    }

    private Intent getIntent(View view) {
        if (view == null) {
            HwLog.w("EventNotifyView", "getIntent view is null");
            return null;
        }
        int id = view.getId();
        if (id == R$id.event_mms) {
            return MessageMonitor.getMmsIntent(this.mMmsNum);
        }
        if (id == R$id.event_call) {
            return CallLogMonitor.getCallLogIntent(this.mCallNum);
        }
        HwLog.w("EventNotifyView", "getIntent view id = " + id);
        return null;
    }

    private void onDrag(boolean isDragging) {
        int i = 8;
        if (isDragging) {
            if (this.mLayoutCall != null && this.mLayoutCall.getVisibility() == 0) {
                this.mLayoutCall.setVisibility(4);
            }
            if (this.mLayoutMms != null && this.mLayoutMms.getVisibility() == 0) {
                this.mLayoutMms.setVisibility(4);
            }
            if (this.mSeparatorView != null && this.mSeparatorView.getVisibility() == 0) {
                this.mSeparatorView.setVisibility(4);
            }
            if (this.mArrowLeft != null) {
                this.mArrowLeft.stopAnimation();
            }
            if (this.mArrowRight != null) {
                this.mArrowRight.stopAnimation();
            }
            if (this.mDragParent != null) {
                this.mDragParent.addView(this.mDragView);
                return;
            }
            return;
        }
        if (!(this.mDragParent == null || this.mDragView == null)) {
            this.mDragParent.removeView(this.mDragView);
            this.mDragView.clearBitmap();
            this.mDragView = null;
        }
        if (this.mLayoutMms != null) {
            this.mLayoutMms.setVisibility(this.mMmsNum <= 0 ? 8 : 0);
        }
        if (this.mLayoutCall != null) {
            View view = this.mLayoutCall;
            if (this.mCallNum > 0) {
                i = 0;
            }
            view.setVisibility(i);
        }
        updateEventDisplay();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View root = getRootView();
        if (root != null) {
            this.mDragParent = (ViewGroup) root.findViewById(R$id.keyguard_host_view);
        }
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
        this.mDragParent = null;
    }

    private void updateEventDisplay() {
        if (this.mSeparatorView == null) {
            HwLog.w("EventNotifyView", "updateEventDisplay mSeparatorView is null");
            return;
        }
        if (this.mMmsNum == 0 || this.mCallNum == 0) {
            this.mSeparatorView.setVisibility(8);
        } else {
            this.mSeparatorView.setVisibility(0);
        }
        if (this.mMmsNum == 0 && this.mCallNum == 0) {
            setVisibility(8);
        } else {
            setVisibility(0);
        }
        invalidate();
    }

    public void onMissCallUpdate(int num) {
        if (this.mCallNumText == null || this.mLayoutCall == null) {
            HwLog.w("EventNotifyView", "onMissCallUpdate mCallView is null");
            return;
        }
        this.mCallNum = num;
        if (num <= 0) {
            this.mLayoutCall.setVisibility(8);
        } else {
            this.mLayoutCall.setVisibility(0);
            CharSequence numText = BuildConfig.FLAVOR + num;
            if (num > 99) {
                numText = "99+";
            }
            this.mCallNumText.setText(numText);
            this.mCallNumText.setContentDescription(EventViewHelper.getMissCallAccessibilityDescription(getContext(), num, false));
            addTextShadow(this.mCallNumText);
        }
        updateEventDisplay();
    }

    public void onNewMmsUpdate(int num) {
        if (this.mMmsNumText == null || this.mLayoutMms == null) {
            HwLog.w("EventNotifyView", "onMissCallUpdate mCallView is null");
            return;
        }
        this.mMmsNum = num;
        if (num <= 0) {
            this.mLayoutMms.setVisibility(8);
        } else {
            this.mLayoutMms.setVisibility(0);
            CharSequence numText = BuildConfig.FLAVOR + num;
            if (num > 99) {
                numText = "99+";
            }
            this.mMmsNumText.setText(numText);
            this.mMmsNumText.setContentDescription(EventViewHelper.getNewMmsAccessibilityDescription(getContext(), num, false));
            addTextShadow(this.mMmsNumText);
        }
        updateEventDisplay();
    }

    private void addTextShadow(TextView tv) {
        tv.setShadowLayer(1.0f, 0.0f, 3.0f, -1728053248);
    }
}
