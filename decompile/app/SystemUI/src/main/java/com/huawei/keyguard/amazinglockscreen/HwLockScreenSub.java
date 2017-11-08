package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenView;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LayoutCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$VisibilityCallback;
import com.huawei.keyguard.HwUnlockConstants$LockScreenSubType;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;

public abstract class HwLockScreenSub extends ViewGroup implements HwUnlockInterface$HwLockScreenView, HwUnlockInterface$LayoutCallback, HwUnlockInterface$VisibilityCallback, HwUnlockInterface$ConditionCallback {
    private boolean mAnimationStart = false;
    private HwViewProperty mH;
    private Handler mHandler;
    private ArrayList<HwAnimationSet> mHwAnimations;
    private PowerManager mPowerManager = null;
    protected HwUnlockConstants$LockScreenSubType mType;
    private HwViewProperty mVisiblity;
    private HwViewProperty mW;
    private HwViewProperty mX;
    private HwViewProperty mY;

    protected abstract void parserSpecialAttributes(NamedNodeMap namedNodeMap);

    public HwLockScreenSub(Context context, NamedNodeMap attrs) {
        super(context);
        parserView(attrs);
        this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        this.mHwAnimations = new ArrayList();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (HwLockScreenSub.this.mAnimationStart) {
                            HwLockScreenSub.this.clearAnimation();
                            Animation anim = msg.obj;
                            if (anim != null && HwLockScreenSub.this.mPowerManager.isScreenOn()) {
                                HwLockScreenSub.this.startAnimation(anim);
                                return;
                            } else if (!HwLockScreenSub.this.mPowerManager.isScreenOn()) {
                                HwLockScreenSub.this.mAnimationStart = false;
                                return;
                            } else {
                                return;
                            }
                        }
                        return;
                    case 1:
                        HwLockScreenSub.this.clearAnimation();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    protected void parserView(NamedNodeMap attrs) {
        parserLayoutAndVisibility(attrs);
        parserSpecialAttributes(attrs);
    }

    protected void onDetachedFromWindow() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        clearAnimation();
        if (this.mHwAnimations != null) {
            for (int i = 0; i < this.mHwAnimations.size(); i++) {
                ((HwAnimationSet) this.mHwAnimations.get(i)).cancel();
            }
            this.mHwAnimations.clear();
        }
        super.onDetachedFromWindow();
    }

    protected void parserLayoutAndVisibility(NamedNodeMap attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
            String name = attrs.item(i).getNodeName();
            String value = attrs.item(i).getNodeValue();
            if ("x".equalsIgnoreCase(name)) {
                this.mX = new HwViewProperty(getContext(), value, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
            } else if ("y".equalsIgnoreCase(name)) {
                this.mY = new HwViewProperty(getContext(), value, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
            } else if ("w".equalsIgnoreCase(name)) {
                this.mW = new HwViewProperty(getContext(), value, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
            } else if ("h".equalsIgnoreCase(name)) {
                this.mH = new HwViewProperty(getContext(), value, HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT, this);
            } else if ("visible".equalsIgnoreCase(name)) {
                this.mVisiblity = new HwViewProperty(getContext(), value, HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY, this);
            }
        }
    }

    public void onLayoutChanged() {
        layout();
    }

    public void layout() {
        int l = ((Integer) this.mX.getValue()).intValue();
        int t = ((Integer) this.mY.getValue()).intValue();
        layout(l, t, l + ((Integer) this.mW.getValue()).intValue(), t + ((Integer) this.mH.getValue()).intValue());
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof HwUnlockInterface$HwLockScreenView) {
                ((HwUnlockInterface$HwLockScreenView) view).layout();
            }
        }
    }

    public void refreshVisibility(boolean result) {
        setVisibility(result ? 0 : 4);
    }

    public void refreshCondition(String strValue, boolean value) {
        if (value && !this.mAnimationStart) {
            startAnimation();
        } else if (!value) {
            stopAnimation();
        }
    }

    private void startAnimation() {
        for (int i = 0; i < this.mHwAnimations.size(); i++) {
            HwAnimationSet anim = (HwAnimationSet) this.mHwAnimations.get(i);
            if (this.mHandler.hasMessages(0)) {
                this.mHandler.removeMessages(0);
            }
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
            }
            this.mAnimationStart = true;
            Message msg = Message.obtain();
            msg.obj = anim;
            msg.what = 0;
            this.mHandler.sendMessage(msg);
        }
    }

    private void stopAnimation() {
        if (this.mHandler.hasMessages(0)) {
            this.mHandler.removeMessages(0);
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        this.mAnimationStart = false;
        Message msg = Message.obtain();
        msg.what = 1;
        this.mHandler.sendMessage(msg);
    }
}
