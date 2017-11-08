package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;

public class CoverStatusBarView extends RelativeLayout implements BatteryStateChangeCallback {
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private ImageView mMultiUserAvatar;
    private MultiUserSwitch mMultiUserSwitch;
    SignalClusterView mSignalCluster;
    private View mSystemIconsSuperContainer;
    private int mSystemIconsSwitcherHiddenExpandedMargin;

    public CoverStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsSuperContainer = findViewById(R.id.system_icons_super_container);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) findViewById(R.id.multi_user_avatar);
        loadDimens();
        updateUserSwitcher();
        this.mSignalCluster = (SignalClusterView) findViewById(R.id.signal_cluster);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUserInfoController(HwPhoneStatusBar.getInstance().mUserInfoController);
        this.mSignalCluster.setNetworkController(HwPhoneStatusBar.getInstance().mNetworkController);
        this.mSignalCluster.setSecurityController(HwPhoneStatusBar.getInstance().mSecurityController);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void loadDimens() {
        this.mSystemIconsSwitcherHiddenExpandedMargin = getResources().getDimensionPixelSize(R.dimen.system_icons_switcher_hidden_expanded_margin);
    }

    private void updateUserSwitcher() {
        boolean keyguardSwitcherAvailable = this.mKeyguardUserSwitcher != null;
        this.mMultiUserSwitch.setClickable(keyguardSwitcherAvailable);
        this.mMultiUserSwitch.setFocusable(keyguardSwitcherAvailable);
        this.mMultiUserSwitch.setKeyguardMode(keyguardSwitcherAvailable);
    }

    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(new OnUserInfoChangedListener() {
            public void onUserInfoChanged(String name, Drawable picture) {
                CoverStatusBarView.this.mMultiUserAvatar.setImageDrawable(picture);
            }
        });
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != 0) {
            this.mSystemIconsSuperContainer.animate().cancel();
            this.mMultiUserSwitch.animate().cancel();
            this.mMultiUserSwitch.setAlpha(1.0f);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
