package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.SystemProperties;
import android.view.IWindowManager.Stub;

public abstract class AbsWindowManagerService extends Stub {
    public static final int TOP_LAYER = 400000;
    protected static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    Configuration mCurNaviConfiguration;
    protected boolean mIgnoreFrozen = false;
    public int mLazyModeOn;

    public int getLazyMode() {
        return 0;
    }

    public void setLazyMode(int lazyMode) {
    }

    protected void setCropOnSingleHandMode(int singleHandleMode, boolean isMultiWindowApp, int dw, int dh, Rect crop) {
    }

    protected void hwProcessOnMatrix(int rotation, int width, int height, Rect frame, Matrix outMatrix) {
    }

    public boolean isCoverOpen() {
        return true;
    }

    public void setCoverManagerState(boolean isCoverOpen) {
    }

    public void freezeOrThawRotation(int rotation) {
    }

    protected void sendUpdateAppOpsState() {
    }

    protected void setAppOpHideHook(WindowState win, boolean visible) {
    }

    protected void setAppOpVisibilityLwHook(WindowState win, int mode) {
    }

    protected void setVisibleFromParent(WindowState win) {
    }

    public void setNaviBarFlag() {
    }

    public void setFocusedAppForNavi(IBinder token) {
    }

    protected void updateInputImmersiveMode() {
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
    }

    public Configuration getCurNaviConfiguration() {
        return null;
    }

    protected void addWindowReport(WindowState win, int mode) {
    }

    protected void removeWindowReport(WindowState win) {
    }

    protected void updateAppOpsStateReport(int ops, String packageName) {
    }

    public int getNsdWindowInfo(IBinder token) {
        return 0;
    }

    public String getNsdWindowTitle(IBinder token) {
        return null;
    }

    protected void checkKeyguardDismissDoneLocked() {
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
    }

    public boolean isSplitMode() {
        return false;
    }

    public void setSplittable(boolean splittable) {
    }

    public int getLayerIndex(String appName, int windowType) {
        return 0;
    }

    public void showWallpaperIfNeed(WindowState w) {
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, int pid, String processName) {
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, String componentName) {
    }
}
