package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.time.HwStatusBarClock;
import com.android.systemui.utils.HwLog;

public class KeyguardStatusBarView extends RelativeLayout implements BatteryStateChangeCallback {
    private HwStatusBarClock mClock;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mKeyguardUserSwitcherShowing;
    private ImageView mMultiUserAvatar;
    private MultiUserSwitch mMultiUserSwitch;
    private View mSystemIconsContainer;
    private View mSystemIconsSuperContainer;
    private int mSystemIconsSwitcherHiddenExpandedMargin;

    public KeyguardStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsSuperContainer = findViewById(R.id.system_icons_super_container);
        this.mSystemIconsContainer = findViewById(R.id.system_icons_container);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) findViewById(R.id.multi_user_avatar);
        this.mClock = (HwStatusBarClock) findViewById(R.id.clock);
        loadDimens();
        updateUserSwitcher();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MarginLayoutParams lp = (MarginLayoutParams) this.mMultiUserAvatar.getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.multi_user_avatar_keyguard_size);
        lp.height = dimensionPixelSize;
        lp.width = dimensionPixelSize;
        this.mMultiUserAvatar.setLayoutParams(lp);
        lp = (MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.multi_user_switch_width_keyguard);
        lp.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.multi_user_switch_keyguard_margin));
        this.mMultiUserSwitch.setLayoutParams(lp);
        lp = (MarginLayoutParams) this.mSystemIconsSuperContainer.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height);
        lp.setMarginStart(getResources().getDimensionPixelSize(R.dimen.system_icons_super_container_margin_start));
        this.mSystemIconsSuperContainer.setLayoutParams(lp);
        this.mSystemIconsSuperContainer.setPaddingRelative(this.mSystemIconsSuperContainer.getPaddingStart(), this.mSystemIconsSuperContainer.getPaddingTop(), getResources().getDimensionPixelSize(R.dimen.system_icons_keyguard_padding_end), this.mSystemIconsSuperContainer.getPaddingBottom());
        lp = (MarginLayoutParams) this.mSystemIconsContainer.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        this.mSystemIconsContainer.setLayoutParams(lp);
        lp = (MarginLayoutParams) getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height_keyguard);
        setLayoutParams(lp);
    }

    private void loadDimens() {
        this.mSystemIconsSwitcherHiddenExpandedMargin = getResources().getDimensionPixelSize(R.dimen.system_icons_switcher_hidden_expanded_margin);
    }

    private void updateVisibilities() {
        if (this.mMultiUserSwitch.getParent() != this && !this.mKeyguardUserSwitcherShowing) {
            if (this.mMultiUserSwitch.getParent() != null) {
                getOverlay().remove(this.mMultiUserSwitch);
            }
            addView(this.mMultiUserSwitch, 0);
        } else if (this.mMultiUserSwitch.getParent() == this && this.mKeyguardUserSwitcherShowing) {
            removeView(this.mMultiUserSwitch);
        }
    }

    private void updateSystemIconsLayoutParams() {
        LayoutParams lp = (LayoutParams) this.mSystemIconsSuperContainer.getLayoutParams();
        int marginEnd = this.mKeyguardUserSwitcherShowing ? this.mSystemIconsSwitcherHiddenExpandedMargin : 0;
        if (marginEnd != lp.getMarginEnd()) {
            lp.setMarginEnd(marginEnd);
            this.mSystemIconsSuperContainer.setLayoutParams(lp);
        }
    }

    private void updateUserSwitcher() {
        boolean keyguardSwitcherAvailable = this.mKeyguardUserSwitcher != null;
        this.mMultiUserSwitch.setClickable(keyguardSwitcherAvailable);
        this.mMultiUserSwitch.setFocusable(keyguardSwitcherAvailable);
        this.mMultiUserSwitch.setKeyguardMode(keyguardSwitcherAvailable);
    }

    public void setUserSwitcherController(UserSwitcherController controller) {
        this.mMultiUserSwitch.setUserSwitcherController(controller);
    }

    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(new OnUserInfoChangedListener() {
            public void onUserInfoChanged(String name, Drawable picture) {
                KeyguardStatusBarView.this.mMultiUserAvatar.setImageDrawable(picture);
            }
        });
    }

    public void setQSPanel(QSPanel qsp) {
        this.mMultiUserSwitch.setQsPanel(qsp);
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        this.mMultiUserSwitch.setKeyguardUserSwitcher(keyguardUserSwitcher);
        updateUserSwitcher();
    }

    public void setKeyguardUserSwitcherShowing(boolean showing, boolean animate) {
        this.mKeyguardUserSwitcherShowing = showing;
        if (animate) {
            animateNextLayoutChange();
        }
        updateVisibilities();
        updateSystemIconsLayoutParams();
    }

    private void animateNextLayoutChange() {
        final int systemIconsCurrentX = this.mSystemIconsSuperContainer.getLeft();
        final boolean userSwitcherVisible = this.mMultiUserSwitch.getParent() == this;
        getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                KeyguardStatusBarView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean userSwitcherHiding = userSwitcherVisible ? KeyguardStatusBarView.this.mMultiUserSwitch.getParent() != KeyguardStatusBarView.this : false;
                KeyguardStatusBarView.this.mSystemIconsSuperContainer.setX((float) systemIconsCurrentX);
                KeyguardStatusBarView.this.mSystemIconsSuperContainer.animate().translationX(0.0f).setDuration(400).setStartDelay((long) (userSwitcherHiding ? 300 : 0)).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
                if (userSwitcherHiding) {
                    KeyguardStatusBarView.this.getOverlay().add(KeyguardStatusBarView.this.mMultiUserSwitch);
                    KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
                        public void run() {
                            KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(1.0f);
                            KeyguardStatusBarView.this.getOverlay().remove(KeyguardStatusBarView.this.mMultiUserSwitch);
                        }
                    }).start();
                } else {
                    KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(0.0f);
                    KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(1.0f).setDuration(300).setStartDelay(200).setInterpolator(Interpolators.ALPHA_IN);
                }
                return true;
            }
        });
    }

    public void setVisibility(int visibility) {
        if (visibility != getVisibility()) {
            HwLog.i("KeyguardStatusBarView", "setVisibility: " + visibility + ", alpha=" + getAlpha());
        }
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

    public void setClockVisible(boolean show) {
        if (this.mClock != null) {
            this.mClock.setVisibility(show ? 0 : 8);
        }
    }
}
