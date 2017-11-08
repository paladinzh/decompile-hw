package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Rect;
import android.os.Vibrator;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwOnTriggerCallback;
import com.huawei.keyguard.HwUnlockConstants$StateViewSubType;
import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;

public class HwStateViewSub extends HwLockScreenSub implements HwUnlockInterface$ConditionCallback {
    protected ArrayList<HwUnlockIntent> mIntents = new ArrayList();
    private boolean mIsUnlocker = false;
    private HwUnlockConstants$StateViewSubType mState;
    private Rect mTouchResponseRect;
    private HwUnlockInterface$HwOnTriggerCallback mUnlockListener;

    public HwStateViewSub(Context context, NamedNodeMap attrs, HwUnlockConstants$StateViewSubType type, HwUnlockInterface$HwOnTriggerCallback callback) {
        int i = 0;
        super(context, attrs);
        this.mState = type;
        this.mUnlockListener = callback;
        if (type != HwUnlockConstants$StateViewSubType.NORMAL) {
            i = 4;
        }
        setVisibility(i);
    }

    public void setTouchRect(String x, String y, String w, String h) {
        int l = Integer.parseInt(x);
        int t = Integer.parseInt(y);
        this.mTouchResponseRect = new Rect(l, t, l + Integer.parseInt(w), t + Integer.parseInt(h));
    }

    public HwUnlockConstants$StateViewSubType getStateType() {
        return this.mState;
    }

    public boolean isInPressArea(int x, int y) {
        boolean result = false;
        if (this.mTouchResponseRect.contains(x, y)) {
            result = true;
        }
        if (result) {
            ((Vibrator) getContext().getSystemService("vibrator")).vibrate(40);
        }
        return result;
    }

    public void setUnlocker(boolean unlocker) {
        this.mIsUnlocker = unlocker;
    }

    public void addIntent(HwUnlockIntent intent) {
        this.mIntents.add(intent);
    }

    protected void parserSpecialAttributes(NamedNodeMap attrs) {
    }

    public void refreshCondition(String exp, boolean result) {
        if (result && this.mIsUnlocker) {
            for (int i = 0; i < this.mIntents.size(); i++) {
                boolean value = ((HwUnlockIntent) this.mIntents.get(i)).getCondition();
                String type = ((HwUnlockIntent) this.mIntents.get(i)).getIntentType();
                if (value) {
                    HwPropertyManager.getInstance().setTriggerFlag(true);
                    this.mUnlockListener.onTrigger(type);
                }
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mIntents != null) {
            this.mIntents.clear();
        }
    }
}
