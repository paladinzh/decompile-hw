package com.huawei.keyguard.cover;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.huawei.coverscreen.HwCustCoverScreen;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.R$layout;
import com.huawei.android.media.AudioManagerEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.monitor.RadarUtil;
import com.huawei.keyguard.util.HwLog;

public class CoverScreen extends RelativeLayout implements KeyguardSecurityView {
    private AudioManager mAudioManager;
    private CoverHome mCoverHome;
    private int mDownX;
    private int mDownY;
    private boolean mIsKeyDown;
    private boolean mIsPixelCoverEnable;
    private KeyguardSecurityCallback mKeyguardsecuritycallback;
    private long mLastTimePokeWakeCalled;
    private Rect mRect;
    private TelephonyManager mTelephonyManager;

    public CoverScreen(Context context) {
        this(context, null);
    }

    public CoverScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLastTimePokeWakeCalled = 0;
        this.mIsPixelCoverEnable = false;
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(393216);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initLockViews();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (interceptMediaKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean interceptMediaKey(KeyEvent event) {
        int i = -1;
        int keyCode = event.getKeyCode();
        if (event.getAction() != 0) {
            if (event.getAction() == 1) {
                switch (keyCode) {
                    case 24:
                    case 25:
                    case 164:
                        return true;
                    case 79:
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case 90:
                    case 91:
                    case 126:
                    case 127:
                    case 130:
                        handleMediaKeyEvent(event);
                        return true;
                    default:
                        break;
                }
            }
        }
        switch (keyCode) {
            case 24:
            case 25:
            case 164:
                synchronized (this) {
                    if (this.mAudioManager == null) {
                        this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
                    }
                }
                if (this.mAudioManager.isMusicActive()) {
                    int i2;
                    AudioManager audioManager = this.mAudioManager;
                    if (keyCode == 24) {
                        i2 = 1;
                    } else {
                        i2 = -1;
                    }
                    audioManager.adjustStreamVolume(3, i2, 0);
                } else if (AudioManagerEx.isFMActive(this.mAudioManager)) {
                    AudioManager audioManager2 = this.mAudioManager;
                    int i3 = AudioManagerEx.STREAM_FM;
                    if (keyCode == 24) {
                        i = 1;
                    }
                    audioManager2.adjustStreamVolume(i3, i, 0);
                    HwLog.d("KeyguardViewBase", "interceptMediaKey fm volume ");
                }
                return true;
            case 79:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 130:
                break;
            case 85:
            case 126:
            case 127:
                if (this.mTelephonyManager == null) {
                    this.mTelephonyManager = (TelephonyManager) getContext().getSystemService("phone");
                }
                if (!(this.mTelephonyManager == null || this.mTelephonyManager.getCallState() == 0)) {
                    return true;
                }
        }
        return false;
    }

    void handleMediaKeyEvent(KeyEvent keyEvent) {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
        }
        if (this.mAudioManager != null) {
            try {
                this.mAudioManager.dispatchMediaKeyEvent(keyEvent);
                return;
            } catch (Exception e) {
                HwLog.e("KeyguardViewBase", "dispatchMediaKeyEvent threw exception " + e);
                RadarUtil.uploadPlayMusicError(getContext(), "dispatchMediaKeyEvent threw exception: " + e.toString());
                return;
            }
        }
        HwLog.w("KeyguardViewBase", "Unable to find IAudioService for media key event");
    }

    private void initLockViews() {
        this.mRect = CoverViewManager.getCoverWindowSize(this.mContext);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (inflater != null) {
            this.mCoverHome = (CoverHome) inflater.inflate(R$layout.cover_home, null);
        }
        if (this.mCoverHome == null) {
            HwLog.i("CoverScreen", "initLockViews, mCoverHome is null!");
            return;
        }
        this.mCoverHome.setCallback(this);
        addView(this.mCoverHome);
        LayoutParams layoutParams = (LayoutParams) this.mCoverHome.getLayoutParams();
        layoutParams.topMargin = this.mRect.top;
        layoutParams.width = this.mRect.width();
        layoutParams.height = this.mRect.height();
        layoutParams.leftMargin = this.mRect.left;
        this.mCoverHome.setLayoutParams(layoutParams);
        if (!this.mIsPixelCoverEnable) {
            HwCustCoverScreen hwCustCoverScreen = (HwCustCoverScreen) HwCustUtils.createObj(HwCustCoverScreen.class, new Object[]{getContext()});
            if (hwCustCoverScreen != null) {
                hwCustCoverScreen.initLockScreen(this, this.mCoverHome);
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                this.mDownX = (int) ev.getX();
                this.mDownY = (int) ev.getY();
                this.mIsKeyDown = true;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & event.getActionMasked()) {
            case 1:
            case 6:
                if (this.mIsKeyDown && event.getActionIndex() == 0) {
                    this.mIsKeyDown = false;
                    int y = (int) event.getY();
                    int lengthX = Math.abs(((int) event.getX()) - this.mDownX);
                    int lengthY = Math.abs(y - this.mDownY);
                    HwLog.d("CoverScreen", "ACTION_UP onTouchEvent x=" + ((int) event.getX()) + " y=" + ((int) event.getY()));
                    if (!this.mRect.contains(this.mDownX, this.mDownY) && (lengthX * lengthX) + (lengthY * lengthY) > 40000) {
                        HwLog.d("CoverScreen", "action_up ontouchevent removeScreen");
                        CoverViewManager.getInstance(this.mContext).removeCoverScreen();
                        break;
                    }
                }
        }
        return true;
    }

    public void onGrabbedStateChange() {
        if (this.mKeyguardsecuritycallback != null && System.currentTimeMillis() - this.mLastTimePokeWakeCalled > 2000) {
            this.mKeyguardsecuritycallback.userActivity();
            this.mLastTimePokeWakeCalled = System.currentTimeMillis();
        }
    }

    protected void onAttachedToWindow() {
        HwLog.i("CoverScreen", " CoverScreen onAttachedToWindow");
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        HwLog.i("CoverScreen", " CoverScreen onDetachedFromWindow");
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).clearNotifications();
    }

    public boolean needsInput() {
        return false;
    }

    public void onPause() {
    }

    public void onResume(int arg0) {
    }

    public void setKeyguardCallback(KeyguardSecurityCallback arg0) {
        this.mKeyguardsecuritycallback = arg0;
    }

    public void setLockPatternUtils(LockPatternUtils arg0) {
    }

    public void showPromptReason(int reason) {
    }

    public void showMessage(String message, int color) {
    }

    public void startAppearAnimation() {
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return false;
    }

    public boolean startRevertAnimation(Runnable finishRunnable) {
        return false;
    }
}
