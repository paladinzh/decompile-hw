package com.android.systemui.statusbar.car;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager.LayoutParams;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.SystemServicesProxy.TaskStackListener;
import com.android.systemui.statusbar.car.CarBatteryController.BatteryViewHandler;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.policy.BatteryController;

public class CarStatusBar extends PhoneStatusBar implements BatteryViewHandler {
    private CarBatteryController mCarBatteryController;
    private CarNavigationBarView mCarNavigationBar;
    private CarNavigationBarController mController;
    private FullscreenUserSwitcher mFullscreenUserSwitcher;
    private BroadcastReceiver mPackageChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getData() != null && CarStatusBar.this.mController != null) {
                CarStatusBar.this.mController.onPackageChange(intent.getData().getSchemeSpecificPart());
            }
        }
    };
    private TaskStackListenerImpl mTaskStackListener;

    private class TaskStackListenerImpl extends TaskStackListener {
        private TaskStackListenerImpl() {
        }

        public void onTaskStackChanged() {
            CarStatusBar.this.mController.taskChanged(Recents.getSystemServices().getRunningTask().baseActivity.getPackageName());
        }
    }

    public void start() {
        super.start();
        this.mTaskStackListener = new TaskStackListenerImpl();
        SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mTaskStackListener);
        registerPackageChangeReceivers();
        this.mCarBatteryController.startListening();
    }

    public void destroy() {
        this.mCarBatteryController.stopListening();
        super.destroy();
    }

    protected PhoneStatusBarView makeStatusBarView() {
        return super.makeStatusBarView();
    }

    protected BatteryController createBatteryController() {
        this.mCarBatteryController = new CarBatteryController(this.mContext);
        this.mCarBatteryController.addBatteryViewHandler(this);
        return this.mCarBatteryController;
    }

    protected void addNavigationBar() {
        LayoutParams lp = new LayoutParams(-1, -1, 2019, 25428072, -3);
        lp.setTitle("CarNavigationBar");
        lp.windowAnimations = 0;
        this.mWindowManager.addView(this.mNavigationBarView, lp);
    }

    protected void createNavigationBarView(Context context) {
        if (this.mNavigationBarView == null) {
            this.mCarNavigationBar = (CarNavigationBarView) View.inflate(context, R.layout.car_navigation_bar, null);
            this.mController = new CarNavigationBarController(context, this.mCarNavigationBar, this);
            this.mNavigationBarView = this.mCarNavigationBar;
        }
    }

    public void showBatteryView() {
    }

    public void hideBatteryView() {
    }

    private void registerPackageChangeReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mPackageChangeReceiver, filter);
    }

    protected void repositionNavigationBar() {
    }

    protected void createUserSwitcher() {
        if (this.mUserSwitcherController.useFullscreenUserSwitcher()) {
            this.mFullscreenUserSwitcher = new FullscreenUserSwitcher(this, this.mUserSwitcherController, (ViewStub) this.mStatusBarWindow.findViewById(R.id.fullscreen_user_switcher_stub));
        } else {
            super.createUserSwitcher();
        }
    }

    public void userSwitched(int newUserId) {
        super.userSwitched(newUserId);
        if (this.mFullscreenUserSwitcher != null) {
            this.mFullscreenUserSwitcher.onUserSwitched(newUserId);
        }
    }

    public void updateKeyguardState(boolean goingToFullShade, boolean fromShadeLocked) {
        super.updateKeyguardState(goingToFullShade, fromShadeLocked);
        if (this.mFullscreenUserSwitcher == null) {
            return;
        }
        if (this.mState == 3) {
            this.mFullscreenUserSwitcher.show();
        } else {
            this.mFullscreenUserSwitcher.hide();
        }
    }

    public void updateStatusBar() {
    }

    public void requestUpdateNavigationBar(boolean minNaviBar) {
    }

    public void showScreenPinningDialog(int taskId, boolean allowCancel) {
    }

    public void hideDropbackView() {
    }

    public void showDropbackView() {
    }

    public boolean getDropbackViewHideStatus() {
        return false;
    }

    public void setBokehChangeStatus(boolean change) {
    }

    public boolean getFpUnlockingStatus() {
        return false;
    }

    public boolean getFastUnlockMode() {
        return false;
    }

    public KeyguardStatusBarView getKeyguardStatusBarView() {
        return null;
    }

    public View getCoverStatusBarView() {
        return null;
    }

    public void showNotificationToast(boolean shouldShowNoDetails) {
    }

    public void hideNotificationToast() {
    }

    public boolean isFullscreenBouncer() {
        return false;
    }
}
