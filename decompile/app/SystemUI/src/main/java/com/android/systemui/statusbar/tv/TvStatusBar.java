package com.android.systemui.statusbar.tv;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.tv.pip.PipManager;

public class TvStatusBar extends BaseStatusBar {
    private int mLastDispatchedSystemUiVisibility = -1;
    int mSystemUiVisibility = 0;

    public void setIcon(String slot, StatusBarIcon icon) {
    }

    public void removeIcon(String slot) {
    }

    public void addNotification(StatusBarNotification notification, RankingMap ranking, Entry entry) {
    }

    protected void updateNotificationRanking(RankingMap ranking) {
    }

    public void removeNotification(String key, RankingMap ranking) {
    }

    public void disable(int state1, int state2, boolean animate) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateCollapsePanels(int flags) {
    }

    public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenStackBounds, Rect dockedStackBounds) {
    }

    public void topAppWindowChanged(boolean visible) {
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) {
    }

    public void setWindowState(int window, int state) {
    }

    public void buzzBeepBlinked() {
    }

    public void notificationLightOff() {
    }

    public void notificationLightPulse(int argb, int onMillis, int offMillis) {
    }

    protected void setAreThereNotifications() {
    }

    protected void updateNotifications() {
    }

    protected void toggleSplitScreenMode(int metricsDockAction, int metricsUndockAction) {
    }

    public void maybeEscalateHeadsUp() {
    }

    public boolean isPanelFullyCollapsed() {
        return false;
    }

    protected int getMaxKeyguardNotifications(boolean recompute) {
        return 0;
    }

    public void animateExpandSettingsPanel(String subPanel) {
    }

    protected void createAndAddWindows() {
    }

    protected void refreshLayout(int layoutDirection) {
    }

    public void onActivated(ActivatableNotificationView view) {
    }

    public void onActivationReset(ActivatableNotificationView view) {
    }

    public void showScreenPinningRequest(int taskId) {
    }

    public void appTransitionPending() {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionStarting(long startTime, long duration) {
    }

    public void appTransitionFinished() {
    }

    public void onCameraLaunchGestureDetected(int source) {
    }

    public void showTvPictureInPictureMenu() {
        PipManager.getInstance().showTvPictureInPictureMenu();
    }

    protected void updateHeadsUp(String key, Entry entry, boolean shouldPeek, boolean alertAgain) {
    }

    protected void setHeadsUpUser(int newUserId) {
    }

    protected boolean isSnoozedPackage(StatusBarNotification sbn) {
        return false;
    }

    public void addQsTile(ComponentName tile) {
    }

    public void remQsTile(ComponentName tile) {
    }

    public void clickTile(ComponentName tile) {
    }

    public void start() {
        super.start();
        putComponent(TvStatusBar.class, this);
    }

    public void updatePipVisibility(boolean visible) {
        if (visible) {
            this.mSystemUiVisibility |= 65536;
        } else {
            this.mSystemUiVisibility &= -65537;
        }
        notifyUiVisibilityChanged(this.mSystemUiVisibility);
    }

    public void updateRecentsVisibility(boolean visible) {
        if (visible) {
            this.mSystemUiVisibility |= 16384;
        } else {
            this.mSystemUiVisibility &= -16385;
        }
        notifyUiVisibilityChanged(this.mSystemUiVisibility);
    }

    private void notifyUiVisibilityChanged(int vis) {
        try {
            if (this.mLastDispatchedSystemUiVisibility != vis) {
                this.mWindowManagerService.statusBarVisibilityChanged(vis);
                this.mLastDispatchedSystemUiVisibility = vis;
            }
        } catch (RemoteException e) {
        }
    }

    public void hideNotificationToastIfShowing() {
    }
}
