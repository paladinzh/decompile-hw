package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$VisibilityCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;

public class HwVirtualButton implements HwUnlockInterface$VisibilityCallback {
    private ArrayList<ButtonInfo> mButtonInfo = new ArrayList();
    private String mButtonName;
    private Context mContext;
    private String mTargetValue;
    private Rect mTouchResponseRect;
    private int mTriggerAction;
    private String mTriggerName;
    private boolean mVisible;
    private String mVisibleProperty;
    private HwViewProperty mVisiblity;

    static class ButtonInfo {
        private String mTargetValue;
        private int mTriggerAction;
        private View mView;
        private String mVisibleProperty;

        ButtonInfo() {
        }
    }

    public HwVirtualButton(Context context) {
        this.mContext = context;
        this.mTouchResponseRect = new Rect(0, 0, 0, 0);
    }

    public String getButtonName() {
        return this.mButtonName;
    }

    public void setButtonName(String name) {
        this.mButtonName = name;
    }

    public void setTriggerAction(String actionName) {
        if ("down".equalsIgnoreCase(actionName)) {
            this.mTriggerAction = 0;
        } else if ("up".equalsIgnoreCase(actionName)) {
            this.mTriggerAction = 1;
        } else if ("move".equalsIgnoreCase(actionName)) {
            this.mTriggerAction = 2;
        } else if ("cancel".equalsIgnoreCase(actionName)) {
            this.mTriggerAction = 3;
        } else if ("double".equalsIgnoreCase(actionName)) {
            this.mTriggerAction = 1001;
        }
    }

    public void setTriggerName(String triggerName) {
        this.mTriggerName = triggerName;
    }

    public void setTouchRect(String x, String y, String w, String h) {
        float scale = AmazingUtils.getScalePara();
        if (scale != 1.0f) {
            x = ((int) (((float) Integer.parseInt(x)) * scale)) + BuildConfig.FLAVOR;
            y = ((int) (((float) Integer.parseInt(y)) * scale)) + BuildConfig.FLAVOR;
            w = ((int) (((float) Integer.parseInt(w)) * scale)) + BuildConfig.FLAVOR;
            h = ((int) (((float) Integer.parseInt(h)) * scale)) + BuildConfig.FLAVOR;
        }
        int l = Integer.parseInt(x);
        int t = Integer.parseInt(y);
        this.mTouchResponseRect = new Rect(l, t, l + Integer.parseInt(w), t + Integer.parseInt(h));
    }

    public void setVisiblityProp(String visible) {
        this.mVisibleProperty = visible;
        this.mVisiblity = new HwViewProperty(this.mContext, visible, HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY, this);
        refreshVisibility(((Boolean) this.mVisiblity.getValue()).booleanValue());
    }

    public String getVisibityProp() {
        return this.mVisibleProperty;
    }

    public void refreshVisibility(boolean visible) {
        this.mVisible = visible;
    }

    public boolean getVisible() {
        return this.mVisible;
    }

    public void setTargetValue(String targetValue) {
        this.mTargetValue = targetValue;
    }

    public Rect getAmazingButtonRect() {
        return this.mTouchResponseRect;
    }

    public void triggerCommand(int action, boolean doubleClick) {
        int size = this.mButtonInfo.size();
        for (int i = 0; i < size; i++) {
            ButtonInfo buttonData = (ButtonInfo) this.mButtonInfo.get(i);
            int triggerAction = buttonData.mTriggerAction;
            if (doubleClick) {
                action = 1001;
            }
            View view = buttonData.mView;
            if (action == triggerAction) {
                if (view instanceof HwImageView) {
                    String targetValue = buttonData.mTargetValue;
                    HwImageView hwImageView = (HwImageView) view;
                    if ("play".equalsIgnoreCase(targetValue)) {
                        hwImageView.refreshTrigger(true);
                    } else if ("stop".equalsIgnoreCase(targetValue)) {
                        hwImageView.refreshTrigger(false);
                    }
                    hwImageView.setVisiblityProp(buttonData.mVisibleProperty);
                } else if (view instanceof HwTextView) {
                    ((HwTextView) view).setVisiblityProp(buttonData.mVisibleProperty);
                } else if (view instanceof HwMusicController) {
                    HwMusicController musicController = (HwMusicController) view;
                    if (musicController.getVisiblityProp()) {
                        musicController.refreshVisibility(false);
                    } else {
                        musicController.refreshVisibility(true);
                    }
                }
            }
        }
    }

    public void addView(View view, String visisbility) {
        if (view != null) {
            ButtonInfo buttonInfo = new ButtonInfo();
            buttonInfo.mTriggerAction = this.mTriggerAction;
            buttonInfo.mView = view;
            buttonInfo.mTargetValue = this.mTargetValue;
            buttonInfo.mVisibleProperty = visisbility;
            this.mButtonInfo.add(buttonInfo);
        }
    }
}
