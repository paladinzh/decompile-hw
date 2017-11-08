package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.android.keyguard.R$plurals;
import com.android.keyguard.R$string;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenReal;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenView;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwOnTriggerCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LockScreenCallback;
import com.android.keyguard.hwlockscreen.LockScreenCallbackImpl;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.amazinglockscreen.HwUnlocker.EndPoint;
import com.huawei.keyguard.amazinglockscreen.data.HwResManager;
import com.huawei.keyguard.amazinglockscreen.data.UnlockAppManager;
import com.huawei.keyguard.clock.ClockHelper;
import com.huawei.keyguard.clock.ClockHelper.DualClockCallback;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.events.CallLogMonitor;
import com.huawei.keyguard.events.CallLogMonitor.CallLogInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.MessageMonitor;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.theme.HwThemeParser;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.MusicUtils;
import java.util.ArrayList;
import java.util.Calendar;
import org.w3c.dom.Document;

public class AmazingLockScreen extends ViewGroup implements HwUnlockInterface$HwLockScreenReal, HwUnlockInterface$HwOnTriggerCallback {
    private ClockHelper mClockHelper;
    private int mDownX;
    private int mDownY;
    private DualClockCallback mDualClockCallback = new DualClockCallback() {
        protected void onDualClockUpdate(boolean needDualClock) {
            AmazingLockScreen.this.mNeedDualClock = needDualClock;
            AmazingLockScreen.this.updateDualClockVisibility(AmazingLockScreen.this.mNeedDualClock);
            AmazingLockScreen.this.onTimeChanged();
        }
    };
    private float mFractionUnlock;
    private float mFractionUnlockMove;
    private int mFrameRate = 33;
    GestureDetector mGestureDetector = new GestureDetector(getContext().getApplicationContext(), new SimpleGestureListener());
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    AmazingLockScreen.this.invalidate();
                    if (AmazingLockScreen.this.mLockScreenCallback != null && AmazingLockScreen.this.mLockScreenCallback.isScreenOn()) {
                        AmazingLockScreen.this.mHandler.sendEmptyMessageDelayed(2001, (long) (1000 / AmazingLockScreen.this.mFrameRate));
                        break;
                    }
                    AmazingLockScreen.this.mHandler.removeMessages(2001);
                    break;
                    break;
                case 2002:
                    if (!HwKeyguardUpdateMonitor.getInstance(AmazingLockScreen.this.getContext()).isOccluded()) {
                        if (!AmazingLockScreen.this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                            View v = AmazingLockScreen.this.getRootView().findViewById(R$id.keyguard_host_view);
                            if (v != null) {
                                v.setBackground(null);
                            }
                        }
                        MusicUtils.setMusicState(0);
                        HwKeyguardPolicy.getInst().dismiss();
                        break;
                    }
                    HwLog.w("AmazingLockScreen", "unlock break because of lockscreen occluded");
                    return;
            }
        }
    };
    private HwMusicController mHwMusicController;
    public ArrayList<HwVirtualButton> mHwVirtualButtonArray = new ArrayList();
    private boolean mIsClickedKey;
    private boolean mIsKeyDown;
    private boolean mIsOnTriggered = false;
    private boolean mIsUnlock;
    private LockPatternUtils mLockPatternUtils;
    private HwUnlockInterface$LockScreenCallback mLockScreenCallback;
    private boolean mNeedDualClock;
    private HwPropertyManager mPropertyManager = HwPropertyManager.getInstance();
    private AlphaAnimation mScreenFadeAnim;
    private boolean mSlideEnabled = false;
    private HwVirtualButton mTouchButton;
    private Point mTouchPoint = new Point(0, 0);
    private HwUnlocker mTouchUnlocker;
    private int mUnlockDistance = -1;
    private ArrayList<HwUnlocker> mUnlockerArray;
    HwUpdateCallback mUpdateCallback = new HwUpdateCallback() {
        public void onNewMessageChange(MessageInfo info) {
            int count = 0;
            if (info == null) {
                HwLog.i("AmazingLockScreen", "onNewMessageChange info is null - no change happened");
            } else {
                count = info.getUnReadCount();
            }
            UnlockAppManager.getInstance().addIntent("message", MessageMonitor.getMmsIntent(count));
            HwLog.i("AmazingLockScreen", "onNewMessageChange missedCount=" + count);
            if (count > 0) {
                String messageText;
                if (count < 100) {
                    messageText = AmazingLockScreen.this.mContext.getResources().getQuantityString(R$plurals.missed_message_tip, count, new Object[]{Integer.valueOf(count)});
                } else {
                    messageText = AmazingLockScreen.this.mContext.getString(R$string.missed_message_99tip);
                }
                AmazingLockScreen.this.mPropertyManager.updateMessageCount(messageText, count);
            }
        }

        public void onCalllogChange(CallLogInfo info) {
            int count = 0;
            if (info == null) {
                HwLog.i("AmazingLockScreen", "onCalllogChange info is null - no change happened");
            } else {
                count = info.getMissedcount();
            }
            UnlockAppManager.getInstance().addIntent("calllog", CallLogMonitor.getCallLogIntent(count));
            HwLog.i("AmazingLockScreen", "onCalllogChange missedCount = " + count);
            if (count > 0) {
                String callText;
                if (count < 100) {
                    callText = AmazingLockScreen.this.mContext.getResources().getQuantityString(R$plurals.missed_call_tip, count, new Object[]{Integer.valueOf(count)});
                } else {
                    callText = AmazingLockScreen.this.mContext.getString(R$string.missed_call_99tip);
                }
                AmazingLockScreen.this.mPropertyManager.updateCallCount(callText, count);
            }
        }
    };
    private float mWidthPx = 0.0f;
    private float mXdpi = 0.0f;

    private class SimpleGestureListener extends SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            if (e.getActionMasked() != 0) {
                return false;
            }
            HwLog.d("AmazingLockScreen", "testTime SimpleGestureListener onDown action = " + e.getAction());
            int x = (int) e.getX();
            int y = (int) e.getY();
            AmazingLockScreen.this.mTouchPoint = new Point(x, y);
            AmazingLockScreen.this.refreshButtonState(x, y);
            AmazingLockScreen.this.refreshTouchUnlock(x, y);
            if (AmazingLockScreen.this.mIsClickedKey && AmazingLockScreen.this.mTouchUnlocker != null) {
                AmazingLockScreen.this.mPropertyManager.updateUnlockerState(AmazingLockScreen.this.mTouchUnlocker.getName() + "_state", 1);
            }
            return true;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            HwLog.d("AmazingLockScreen", "testTime SimpleGestureListener onDoubleTapEvent  action = " + e.getAction());
            switch (e.getAction()) {
                case 1:
                    if (AmazingLockScreen.this.mTouchButton != null) {
                        AmazingLockScreen.this.refreshMusicControlState(AmazingLockScreen.this.mTouchButton.getButtonName());
                        AmazingLockScreen.this.mTouchButton.triggerCommand(e.getAction(), true);
                        AmazingLockScreen.this.mTouchButton = null;
                    }
                    AmazingLockScreen.this.dealActionUpEvent((int) e.getX(0), (int) e.getY(0), e.getActionIndex());
                    break;
                case 2:
                    if (AmazingLockScreen.this.mIsClickedKey && AmazingLockScreen.this.mTouchUnlocker != null) {
                        AmazingLockScreen.this.refreshUnlockMoveState((int) e.getX(0), (int) e.getY(0));
                        break;
                    }
            }
            return true;
        }
    }

    public AmazingLockScreen(Context context, Document document) {
        super(context, null, 0);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(393216);
        init(document);
        initScreenAnimation();
        this.mSlideEnabled = HwThemeParser.getInstance().getSlideInAmazeFlag();
        this.mFractionUnlock = getResources().getDimension(R$dimen.slide_unlock_distance);
        this.mFractionUnlockMove = getResources().getDimension(R$dimen.slide_unlock_distance_move);
        this.mClockHelper = new ClockHelper(context, this.mDualClockCallback);
        this.mNeedDualClock = this.mClockHelper.needDualClock(context);
        HwLog.i("AmazingLockScreen", "mNeedDualClock=" + this.mNeedDualClock);
        this.mXdpi = HwUnlockUtils.getRealXdpi(getContext());
    }

    public void setLockScreenCallback(HwUnlockInterface$LockScreenCallback callback) {
        this.mLockScreenCallback = callback;
    }

    public void addTriggerButton(HwVirtualButton button) {
        this.mHwVirtualButtonArray.add(button);
    }

    public void setFrameRate(int frameRate) {
        this.mFrameRate = frameRate;
    }

    public void setMusicControl(HwMusicController musicControl) {
        this.mHwMusicController = musicControl;
    }

    public void onPhoneStateChanged() {
    }

    public void onBatteryInfoChanged() {
        if (this.mLockScreenCallback != null && this.mPropertyManager != null) {
            BatteryStateInfo bsi = BatteryStateInfo.getInst();
            this.mPropertyManager.updateBatteryInfo(bsi.getBatteryInfo(this.mContext), bsi.showBatteryInfo(), bsi.getChargeLevel(), bsi.getChargePercent(this.mContext));
        }
    }

    public void onTimeChanged() {
        updateTime();
    }

    public void onOwnerInfoChanged() {
        if (this.mLockScreenCallback != null && this.mPropertyManager != null) {
            if (this.mLockScreenCallback instanceof LockScreenCallbackImpl) {
                ((LockScreenCallbackImpl) this.mLockScreenCallback).refreshLockScreenInfo();
            }
            this.mPropertyManager.updateOwnerInfo(this.mLockScreenCallback.getOwnerInfo(), this.mLockScreenCallback.isShowOwnerInfo());
        }
    }

    public void onUnlockTipChanged() {
        if (this.mSlideEnabled) {
            this.mPropertyManager.updateUnlockTip(getContext().getString(R$string.slide_to_unlock));
            return;
        }
        this.mPropertyManager.updateUnlockTip(getContext().getString(R$string.drag_to_unlock));
    }

    public boolean needsInput() {
        return false;
    }

    public void onResume() {
        HwViewProperty.setUP(true);
        if (this.mPropertyManager == null) {
            this.mPropertyManager = HwPropertyManager.getInstance();
            this.mPropertyManager.setLanguage(getContext().getResources().getConfiguration().locale.getLanguage());
        }
        onBatteryInfoChanged();
        updateDualClockVisibility(this.mNeedDualClock);
        onTimeChanged();
        onOwnerInfoChanged();
        onUnlockTipChanged();
        HwUpdateMonitor.getInstance(this.mContext).sendUpdates(this.mUpdateCallback);
        this.mPropertyManager.updatePressState(true);
        this.mPropertyManager.updateStart(true);
        this.mIsOnTriggered = false;
        if (this.mHwMusicController != null) {
            this.mHwMusicController.refreshMusicInfo();
        }
        if (this.mLockScreenCallback != null && this.mLockScreenCallback.isScreenOn()) {
            refreshFrame(this.mFrameRate);
        }
    }

    public void onTrigger(String intentType) {
        if (!this.mIsOnTriggered) {
            this.mIsOnTriggered = true;
            Intent intent = UnlockAppManager.getInstance().getIntent(intentType);
            if (intent != null) {
                HwKeyguardPolicy.getInst().startActivity(intent, true);
                return;
            }
            HwLog.w("AmazingLockScreen", "Target intent is null and dismiss Keyguard directly");
            HwKeyguardPolicy.getInst().dismiss();
        }
    }

    protected void onAttachedToWindow() {
        HwLog.d("AmazingLockScreen", "on attached to window");
        super.onAttachedToWindow();
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
        this.mClockHelper.registerDualClockListeners();
        onBatteryInfoChanged();
        updateDualClockVisibility(this.mNeedDualClock);
        onTimeChanged();
        onOwnerInfoChanged();
        onUnlockTipChanged();
        this.mPropertyManager.updatePressState(true);
        this.mPropertyManager.updateStart(true);
        refreshFrame(this.mFrameRate);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPropertyManager != null) {
            this.mPropertyManager.unregisterCallbaks();
            this.mPropertyManager = null;
        }
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
        this.mClockHelper.unregisterDualClockListeners();
        UnlockAppManager.clear();
        HwResManager.getInstance().clearCache();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2001);
            this.mHandler = null;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof HwUnlockInterface$HwLockScreenView) {
                ((HwUnlockInterface$HwLockScreenView) child).layout();
            }
        }
    }

    private void init(Document document) {
        this.mUnlockerArray = new ArrayList();
        HwViewProperty.setUP(true);
        HwPropertyManager.clean();
        this.mPropertyManager = HwPropertyManager.getInstance();
        this.mPropertyManager.setLanguage(getContext().getResources().getConfiguration().locale.getLanguage());
        new HwInflater(getContext()).parseLayoutFromXml(document, this);
    }

    private void updateTime() {
        Calendar cal1;
        char[] digitCharsForTime;
        if (this.mPropertyManager == null) {
            this.mPropertyManager = HwPropertyManager.getInstance();
        }
        if (this.mNeedDualClock) {
            cal1 = this.mClockHelper.getDefaultCalendar();
            digitCharsForTime = this.mClockHelper.getDigitCharForTime(cal1);
            this.mPropertyManager.updateDefaultTime(String.valueOf(digitCharsForTime[0]), String.valueOf(digitCharsForTime[1]), String.valueOf(digitCharsForTime[2]), String.valueOf(digitCharsForTime[3]), this.mClockHelper.getAmpmString(cal1), this.mClockHelper.isShowAmpm());
            this.mPropertyManager.updateDefaultDate(this.mClockHelper.getDateString(cal1.getTimeZone(), false));
            Calendar cal2 = this.mClockHelper.getRoamingCalendar();
            digitCharsForTime = this.mClockHelper.getDigitCharForTime(cal2);
            this.mPropertyManager.updateRoamingTime(String.valueOf(digitCharsForTime[0]), String.valueOf(digitCharsForTime[1]), String.valueOf(digitCharsForTime[2]), String.valueOf(digitCharsForTime[3]), this.mClockHelper.getAmpmString(cal2), this.mClockHelper.isShowAmpm());
            this.mPropertyManager.updateRoamingDate(this.mClockHelper.getDateString(cal2.getTimeZone(), false));
        }
        cal1 = this.mClockHelper.getRoamingCalendar();
        digitCharsForTime = this.mClockHelper.getDigitCharForTime(cal1);
        this.mPropertyManager.updateTime(String.valueOf(digitCharsForTime[0]), String.valueOf(digitCharsForTime[1]), String.valueOf(digitCharsForTime[2]), String.valueOf(digitCharsForTime[3]), this.mClockHelper.getAmpmString(cal1), this.mClockHelper.isShowAmpm());
        this.mPropertyManager.updateDate(this.mClockHelper.getDateString(cal1.getTimeZone(), true));
    }

    private void updateDualClockVisibility(boolean needDualClock) {
        if (this.mPropertyManager == null) {
            this.mPropertyManager = HwPropertyManager.getInstance();
        }
        this.mPropertyManager.updateDualClockVisibility(needDualClock);
    }

    private void initScreenAnimation() {
        this.mScreenFadeAnim = new AlphaAnimation(1.0f, 1.0f);
        this.mScreenFadeAnim.setInterpolator(new DecelerateInterpolator());
        this.mScreenFadeAnim.setDuration(500);
    }

    public void addUnlocker(HwUnlocker unlocker) {
        this.mUnlockerArray.add(unlocker);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.mDownX = (int) event.getX();
                this.mDownY = (int) event.getY();
                this.mIsUnlock = false;
                this.mIsKeyDown = true;
                updatePressState(false);
                break;
            case 1:
            case 3:
                updatePressState(true);
                break;
        }
        if (this.mPropertyManager == null) {
            this.mPropertyManager = HwPropertyManager.getInstance();
        }
        if (this.mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        int action = event.getAction();
        HwLog.d("AmazingLockScreen", "mSlideEnabled  =" + this.mSlideEnabled);
        if (this.mSlideEnabled) {
            dealSlideAmazing(event);
        }
        switch (event.getActionMasked() & action) {
            case 1:
            case 6:
                if (this.mTouchButton != null) {
                    if (this.mTouchButton.getAmazingButtonRect().contains((int) event.getX(), (int) event.getY())) {
                        refreshMusicControlState(this.mTouchButton.getButtonName());
                    } else {
                        reStorehMusicControlState(this.mTouchButton.getButtonName());
                    }
                    this.mTouchButton = null;
                }
                dealActionUpEvent((int) event.getX(0), (int) event.getY(0), event.getActionIndex());
                break;
            case 2:
                if (this.mIsClickedKey && this.mTouchUnlocker != null) {
                    refreshUnlockMoveState((int) event.getX(0), (int) event.getY(0));
                    break;
                }
        }
        return true;
    }

    private void refreshFrame(int frameRate) {
        if (frameRate > 0) {
            if (frameRate > 60) {
                frameRate = 60;
            }
            this.mFrameRate = frameRate;
            int interval = 1000 / frameRate;
            if (this.mHandler == null) {
                HwLog.w("AmazingLockScreen", "refreshFrame with mHandler null!");
                return;
            }
            Message message = this.mHandler.obtainMessage();
            message.what = 2001;
            message.arg1 = interval;
            this.mHandler.sendMessage(message);
        }
    }

    private void refreshButtonState(int x, int y) {
        for (HwVirtualButton button : this.mHwVirtualButtonArray) {
            if (button.getVisible() && button.getAmazingButtonRect().contains(x, y)) {
                this.mTouchButton = button;
                if (this.mTouchButton.getButtonName().equals("music_prev")) {
                    this.mPropertyManager.updateMusicPrevState(1);
                    return;
                } else if (this.mTouchButton.getButtonName().equals("music_next")) {
                    this.mPropertyManager.updateMusicNextState(1);
                    return;
                } else if (this.mTouchButton.getButtonName().equals("music_play")) {
                    this.mPropertyManager.updateMusicPlayState(1);
                    return;
                } else if (this.mTouchButton.getButtonName().equals("music_pause")) {
                    this.mPropertyManager.updateMusicPauseState(1);
                    return;
                } else {
                    return;
                }
            }
        }
    }

    private void refreshTouchUnlock(int x, int y) {
        for (HwUnlocker unlocker : this.mUnlockerArray) {
            if (unlocker.getVisible() && unlocker.getStartPointRect().contains(x, y)) {
                this.mTouchUnlocker = unlocker;
                this.mIsClickedKey = true;
                return;
            }
        }
    }

    private void refreshUnlockMoveState(int movePointX, int movePointY) {
        float scalePara = AmazingUtils.getScalePara();
        int movex = (int) (((float) (movePointX - this.mTouchPoint.x)) / scalePara);
        int movey = (int) (((float) (movePointY - this.mTouchPoint.y)) / scalePara);
        int maxMoveX = this.mTouchUnlocker.getMaxMoveX();
        int maxMoveY = this.mTouchUnlocker.getMaxMoveY();
        int minMoveX = this.mTouchUnlocker.getMinMoveX();
        int minMoveY = this.mTouchUnlocker.getMinMoveY();
        if (this.mPropertyManager == null) {
            this.mPropertyManager = HwPropertyManager.getInstance();
        }
        if (maxMoveX != minMoveX && movex > minMoveX && movex < maxMoveX) {
            this.mPropertyManager.updateUnlockerMoveX(this.mTouchUnlocker.getName() + "_moveX", movex);
        }
        if (maxMoveY != minMoveY && movey > minMoveY && movey < maxMoveY) {
            this.mPropertyManager.updateUnlockerMoveY(this.mTouchUnlocker.getName() + "_moveY", movey);
        }
        for (EndPoint endPoint : this.mTouchUnlocker.getEndPoint()) {
            if (endPoint.getRect().contains(movePointX, movePointY)) {
                this.mPropertyManager.updateUnlockerState(this.mTouchUnlocker.getName() + "_state", 2);
                return;
            }
        }
    }

    private void refreshMusicControlState(String buttonName) {
        if (buttonName != null && !buttonName.isEmpty()) {
            if (this.mPropertyManager == null) {
                this.mPropertyManager = HwPropertyManager.getInstance();
            }
            if (buttonName.equals("music_prev")) {
                this.mPropertyManager.updateMusicPrevState(0);
                if (this.mHwMusicController != null) {
                    this.mHwMusicController.setPrev();
                }
            } else if (buttonName.equals("music_next")) {
                this.mPropertyManager.updateMusicNextState(0);
                if (this.mHwMusicController != null) {
                    this.mHwMusicController.setNext();
                }
            } else if (buttonName.equals("music_play")) {
                this.mPropertyManager.updateMusicPlayState(0);
                this.mPropertyManager.updateMusicState(1);
                if (this.mHwMusicController != null) {
                    this.mHwMusicController.setPlayOrPause();
                }
            } else if (buttonName.equals("music_pause")) {
                this.mPropertyManager.updateMusicPauseState(0);
                this.mPropertyManager.updateMusicState(0);
                if (this.mHwMusicController != null) {
                    this.mHwMusicController.setPlayOrPause();
                }
            }
        }
    }

    private void reStorehMusicControlState(String buttonName) {
        if (buttonName != null && !buttonName.isEmpty()) {
            if (this.mPropertyManager == null) {
                this.mPropertyManager = HwPropertyManager.getInstance();
            }
            if (buttonName.equals("music_prev")) {
                this.mPropertyManager.updateMusicPrevState(0);
            } else if (buttonName.equals("music_next")) {
                this.mPropertyManager.updateMusicNextState(0);
            }
        }
    }

    private void dealActionUpEvent(int eventX, int eventY, int actionIndex) {
        if (this.mIsClickedKey && actionIndex == 0 && this.mTouchUnlocker != null) {
            HwPropertyManager.getInstance().updateUnlockerState(this.mTouchUnlocker.getName() + "_state", 0);
            this.mIsClickedKey = false;
            for (EndPoint endPoint : this.mTouchUnlocker.getEndPoint()) {
                HwLog.d("AmazingLockScreen", "endPoint.getRect().contains(eventX,eventY) is " + endPoint.getRect().contains(eventX, eventY));
                if (endPoint.getRect().contains(eventX, eventY)) {
                    MusicUtils.setMusicState(0);
                    Intent intent = UnlockAppManager.getInstance().getIntent(endPoint.getIntentType());
                    if (intent != null) {
                        HwKeyguardPolicy.getInst().startActivity(intent, true);
                    } else {
                        HwKeyguardPolicy.getInst().dismiss();
                    }
                    return;
                }
            }
            HwPropertyManager.getInstance().updateUnlockerMoveX(this.mTouchUnlocker.getName() + "_moveX", 0);
            HwPropertyManager.getInstance().updateUnlockerMoveY(this.mTouchUnlocker.getName() + "_moveY", 0);
            this.mTouchUnlocker = null;
        }
    }

    public void setClickKey(boolean value) {
        this.mLockScreenCallback.setClickKey(value ? 1 : 0);
    }

    public void updatePressState(boolean b) {
        boolean isUp = HwViewProperty.isUP();
        if (!isUp) {
            HwViewProperty.setUP(true);
        }
        this.mPropertyManager.updatePressState(b);
        HwViewProperty.setUP(isUp);
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        if (this.mSlideEnabled) {
            this.mLockPatternUtils = lockPatternUtils;
        }
    }

    private void dealSlideAmazing(MotionEvent event) {
        if (this.mUnlockDistance == -1) {
            if (this.mXdpi * 2.3f > ((float) getWidth()) || this.mXdpi == -1.0f) {
                this.mWidthPx = (float) getWidth();
            } else {
                this.mWidthPx = this.mXdpi * 2.3f;
            }
            this.mUnlockDistance = (int) (this.mFractionUnlock * this.mWidthPx);
        }
        switch (event.getAction() & event.getActionMasked()) {
            case 1:
            case 6:
                if (!this.mIsUnlock && this.mIsKeyDown) {
                    dealActionUpEvent(this.mUnlockDistance, event);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void dealActionUpEvent(int distance, MotionEvent event) {
        if (event.getActionIndex() == 0) {
            int y = (int) event.getY();
            int lengthX = Math.abs(((int) event.getX()) - this.mDownX);
            int lengthY = Math.abs(y - this.mDownY);
            if ((lengthX * lengthX) + (lengthY * lengthY) > distance * distance) {
                HwLog.d("AmazingLockScreen", "dealActionUpEvent to unlock");
                this.mIsUnlock = true;
                this.mIsKeyDown = false;
                this.mHandler.sendEmptyMessage(2002);
            } else if (this.mUnlockDistance == distance) {
                this.mIsKeyDown = false;
            }
        }
    }
}
