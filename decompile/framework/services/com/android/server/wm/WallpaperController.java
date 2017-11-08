package com.android.server.wm;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.DisplayInfo;
import java.io.PrintWriter;
import java.util.ArrayList;

class WallpaperController {
    private static final String TAG = "WindowManager";
    private static final int WALLPAPER_DRAW_NORMAL = 0;
    private static final int WALLPAPER_DRAW_PENDING = 1;
    private static final long WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION = 500;
    private static final int WALLPAPER_DRAW_TIMEOUT = 2;
    private static final long WALLPAPER_TIMEOUT = 150;
    private static final long WALLPAPER_TIMEOUT_RECOVERY = 10000;
    private static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private WindowState mDeferredHideWallpaper = null;
    private final FindWallpaperTargetResult mFindResults = new FindWallpaperTargetResult();
    private int mLastWallpaperDisplayOffsetX = Integer.MIN_VALUE;
    private int mLastWallpaperDisplayOffsetY = Integer.MIN_VALUE;
    private long mLastWallpaperTimeoutTime;
    private float mLastWallpaperX = -1.0f;
    private float mLastWallpaperXStep = -1.0f;
    private float mLastWallpaperY = -1.0f;
    private float mLastWallpaperYStep = -1.0f;
    private WindowState mLowerWallpaperTarget = null;
    private final WindowManagerService mService;
    private WindowState mUpperWallpaperTarget = null;
    WindowState mWaitingOnWallpaper;
    private int mWallpaperAnimLayerAdjustment;
    private int mWallpaperDrawState = 0;
    private WindowState mWallpaperTarget = null;
    private final ArrayList<WindowToken> mWallpaperTokens = new ArrayList();

    private static final class FindWallpaperTargetResult {
        WindowState topWallpaper;
        int topWallpaperIndex;
        WindowState wallpaperTarget;
        int wallpaperTargetIndex;

        private FindWallpaperTargetResult() {
            this.topWallpaperIndex = 0;
            this.topWallpaper = null;
            this.wallpaperTargetIndex = 0;
            this.wallpaperTarget = null;
        }

        void setTopWallpaper(WindowState win, int index) {
            this.topWallpaper = win;
            this.topWallpaperIndex = index;
        }

        void setWallpaperTarget(WindowState win, int index) {
            this.wallpaperTarget = win;
            this.wallpaperTargetIndex = index;
        }

        void reset() {
            this.topWallpaperIndex = 0;
            this.topWallpaper = null;
            this.wallpaperTargetIndex = 0;
            this.wallpaperTarget = null;
        }
    }

    public WallpaperController(WindowManagerService service) {
        this.mService = service;
    }

    WindowState getWallpaperTarget() {
        return this.mWallpaperTarget;
    }

    WindowState getLowerWallpaperTarget() {
        return this.mLowerWallpaperTarget;
    }

    WindowState getUpperWallpaperTarget() {
        return this.mUpperWallpaperTarget;
    }

    boolean isWallpaperTarget(WindowState win) {
        return win == this.mWallpaperTarget;
    }

    boolean isBelowWallpaperTarget(WindowState win) {
        return this.mWallpaperTarget != null && this.mWallpaperTarget.mLayer >= win.mBaseLayer;
    }

    boolean isWallpaperVisible() {
        return isWallpaperVisible(this.mWallpaperTarget);
    }

    private boolean isWallpaperVisible(WindowState wallpaperTarget) {
        if (wallpaperTarget != null) {
            if (!wallpaperTarget.mObscured) {
                return true;
            }
            if (!(wallpaperTarget.mAppToken == null || wallpaperTarget.mAppToken.mAppAnimator.animation == null)) {
                return true;
            }
        }
        if (this.mUpperWallpaperTarget == null && this.mLowerWallpaperTarget == null) {
            return false;
        }
        return true;
    }

    boolean isWallpaperTargetAnimating() {
        if (this.mWallpaperTarget == null || !this.mWallpaperTarget.mWinAnimator.isAnimationSet() || this.mWallpaperTarget.mWinAnimator.isDummyAnimation()) {
            return false;
        }
        return true;
    }

    void updateWallpaperVisibility() {
        DisplayContent displayContent = this.mWallpaperTarget.getDisplayContent();
        if (displayContent != null) {
            boolean visible = isWallpaperVisible(this.mWallpaperTarget);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenNdx);
                if (token.hidden == visible) {
                    boolean z;
                    if (visible) {
                        z = false;
                    } else {
                        z = true;
                    }
                    token.hidden = z;
                    displayContent.layoutNeeded = true;
                }
                WindowList windows = token.windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    WindowState wallpaper = (WindowState) windows.get(wallpaperNdx);
                    if (visible) {
                        updateWallpaperOffset(wallpaper, dw, dh, false);
                    }
                    dispatchWallpaperVisibility(wallpaper, visible);
                }
            }
        }
    }

    void hideDeferredWallpapersIfNeeded() {
        if (this.mDeferredHideWallpaper != null) {
            hideWallpapers(this.mDeferredHideWallpaper);
            this.mDeferredHideWallpaper = null;
        }
    }

    void hideWallpapers(WindowState winGoingAway) {
        if (this.mWallpaperTarget != null && (this.mWallpaperTarget != winGoingAway || this.mLowerWallpaperTarget != null)) {
            return;
        }
        if (this.mService.mAppTransition.isRunning()) {
            this.mDeferredHideWallpaper = winGoingAway;
            return;
        }
        boolean wasDeferred = this.mDeferredHideWallpaper == winGoingAway;
        for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(i);
            for (int j = token.windows.size() - 1; j >= 0; j--) {
                WindowState wallpaper = (WindowState) token.windows.get(j);
                WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                if (!winAnimator.mLastHidden || wasDeferred) {
                    winAnimator.hide("hideWallpapers");
                    dispatchWallpaperVisibility(wallpaper, false);
                    DisplayContent displayContent = wallpaper.getDisplayContent();
                    if (displayContent != null) {
                        displayContent.pendingLayoutChanges |= 4;
                    }
                }
            }
            token.hidden = true;
        }
    }

    void dispatchWallpaperVisibility(WindowState wallpaper, boolean visible) {
        if (wallpaper.mWallpaperVisible == visible) {
            return;
        }
        if (this.mDeferredHideWallpaper == null || visible) {
            wallpaper.mWallpaperVisible = visible;
            try {
                wallpaper.mClient.dispatchAppVisibility(visible);
            } catch (RemoteException e) {
            }
        }
    }

    boolean updateWallpaperOffset(WindowState wallpaperWin, int dw, int dh, boolean sync) {
        boolean rawChanged = false;
        float wpx = this.mLastWallpaperX >= 0.0f ? this.mLastWallpaperX : 0.0f;
        float wpxs = this.mLastWallpaperXStep >= 0.0f ? this.mLastWallpaperXStep : -1.0f;
        int availw = (wallpaperWin.mFrame.right - wallpaperWin.mFrame.left) - dw;
        int offset = availw > 0 ? -((int) ((((float) availw) * wpx) + TaskPositioner.RESIZING_HINT_ALPHA)) : 0;
        if (this.mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
            offset += this.mLastWallpaperDisplayOffsetX;
        }
        boolean z = wallpaperWin.mXOffset != offset;
        if (z) {
            wallpaperWin.mXOffset = offset;
        }
        if (!(wallpaperWin.mWallpaperX == wpx && wallpaperWin.mWallpaperXStep == wpxs)) {
            wallpaperWin.mWallpaperX = wpx;
            wallpaperWin.mWallpaperXStep = wpxs;
            rawChanged = true;
        }
        float wpy = this.mLastWallpaperY >= 0.0f ? this.mLastWallpaperY : TaskPositioner.RESIZING_HINT_ALPHA;
        float wpys = this.mLastWallpaperYStep >= 0.0f ? this.mLastWallpaperYStep : -1.0f;
        int availh = (wallpaperWin.mFrame.bottom - wallpaperWin.mFrame.top) - dh;
        offset = availh > 0 ? -((int) ((((float) availh) * wpy) + TaskPositioner.RESIZING_HINT_ALPHA)) : 0;
        if (mUsingHwNavibar) {
            offset = 0;
        }
        if (this.mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            offset += this.mLastWallpaperDisplayOffsetY;
        }
        if (wallpaperWin.mYOffset != offset) {
            z = true;
            wallpaperWin.mYOffset = offset;
        }
        if (!(wallpaperWin.mWallpaperY == wpy && wallpaperWin.mWallpaperYStep == wpys)) {
            wallpaperWin.mWallpaperY = wpy;
            wallpaperWin.mWallpaperYStep = wpys;
            rawChanged = true;
        }
        if (rawChanged && (wallpaperWin.mAttrs.privateFlags & 4) != 0) {
            if (sync) {
                try {
                    this.mWaitingOnWallpaper = wallpaperWin;
                } catch (RemoteException e) {
                }
            }
            wallpaperWin.mClient.dispatchWallpaperOffsets(wallpaperWin.mWallpaperX, wallpaperWin.mWallpaperY, wallpaperWin.mWallpaperXStep, wallpaperWin.mWallpaperYStep, sync);
            if (sync && this.mWaitingOnWallpaper != null) {
                long start = SystemClock.uptimeMillis();
                if (this.mLastWallpaperTimeoutTime + 10000 < start) {
                    try {
                        this.mService.mWindowMap.wait(WALLPAPER_TIMEOUT);
                    } catch (InterruptedException e2) {
                    }
                    if (WALLPAPER_TIMEOUT + start < SystemClock.uptimeMillis()) {
                        Slog.i(TAG, "Timeout waiting for wallpaper to offset: " + wallpaperWin);
                        this.mLastWallpaperTimeoutTime = start;
                    }
                }
                this.mWaitingOnWallpaper = null;
            }
        }
        return z;
    }

    void setWindowWallpaperPosition(WindowState window, float x, float y, float xStep, float yStep) {
        if (window.mWallpaperX != x || window.mWallpaperY != y) {
            window.mWallpaperX = x;
            window.mWallpaperY = y;
            window.mWallpaperXStep = xStep;
            window.mWallpaperYStep = yStep;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    void setWindowWallpaperDisplayOffset(WindowState window, int x, int y) {
        if (window.mWallpaperDisplayOffsetX != x || window.mWallpaperDisplayOffsetY != y) {
            window.mWallpaperDisplayOffsetX = x;
            window.mWallpaperDisplayOffsetY = y;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    Bundle sendWindowWallpaperCommand(WindowState window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (window == this.mWallpaperTarget || window == this.mLowerWallpaperTarget || window == this.mUpperWallpaperTarget) {
            boolean doWait = sync;
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(curTokenNdx)).windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    try {
                        ((WindowState) windows.get(wallpaperNdx)).mClient.dispatchWallpaperCommand(action, x, y, z, extras, sync);
                        sync = false;
                    } catch (RemoteException e) {
                    }
                }
            }
            if (doWait) {
            }
        }
        return null;
    }

    void updateWallpaperOffsetLocked(WindowState changingTarget, boolean sync) {
        DisplayContent displayContent = changingTarget.getDisplayContent();
        if (displayContent != null) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            WindowState target = this.mWallpaperTarget;
            if (target != null) {
                if (target.mWallpaperX >= 0.0f) {
                    this.mLastWallpaperX = target.mWallpaperX;
                } else if (changingTarget.mWallpaperX >= 0.0f) {
                    this.mLastWallpaperX = changingTarget.mWallpaperX;
                }
                if (target.mWallpaperY >= 0.0f) {
                    this.mLastWallpaperY = target.mWallpaperY;
                } else if (changingTarget.mWallpaperY >= 0.0f) {
                    this.mLastWallpaperY = changingTarget.mWallpaperY;
                }
                if (target.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetX = target.mWallpaperDisplayOffsetX;
                } else if (changingTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetX = changingTarget.mWallpaperDisplayOffsetX;
                }
                if (target.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetY = target.mWallpaperDisplayOffsetY;
                } else if (changingTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetY = changingTarget.mWallpaperDisplayOffsetY;
                }
                if (target.mWallpaperXStep >= 0.0f) {
                    this.mLastWallpaperXStep = target.mWallpaperXStep;
                } else if (changingTarget.mWallpaperXStep >= 0.0f) {
                    this.mLastWallpaperXStep = changingTarget.mWallpaperXStep;
                }
                if (target.mWallpaperYStep >= 0.0f) {
                    this.mLastWallpaperYStep = target.mWallpaperYStep;
                } else if (changingTarget.mWallpaperYStep >= 0.0f) {
                    this.mLastWallpaperYStep = changingTarget.mWallpaperYStep;
                }
            }
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(curTokenNdx)).windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    WindowState wallpaper = (WindowState) windows.get(wallpaperNdx);
                    if (updateWallpaperOffset(wallpaper, dw, dh, sync)) {
                        WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                        winAnimator.computeShownFrameLocked();
                        winAnimator.setWallpaperOffset(wallpaper.mShownPosition);
                        sync = false;
                    }
                }
            }
        }
    }

    void clearLastWallpaperTimeoutTime() {
        this.mLastWallpaperTimeoutTime = 0;
    }

    void wallpaperCommandComplete(IBinder window) {
        if (this.mWaitingOnWallpaper != null && this.mWaitingOnWallpaper.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mWindowMap.notifyAll();
        }
    }

    void wallpaperOffsetsComplete(IBinder window) {
        if (this.mWaitingOnWallpaper != null && this.mWaitingOnWallpaper.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mWindowMap.notifyAll();
        }
    }

    int getAnimLayerAdjustment() {
        return this.mWallpaperAnimLayerAdjustment;
    }

    void setAnimLayerAdjustment(WindowState win, int adj) {
        if (win == this.mWallpaperTarget && this.mLowerWallpaperTarget == null) {
            this.mWallpaperAnimLayerAdjustment = adj;
            for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(i)).windows;
                for (int j = windows.size() - 1; j >= 0; j--) {
                    WindowState wallpaper = (WindowState) windows.get(j);
                    wallpaper.mWinAnimator.mAnimLayer = wallpaper.mLayer + adj;
                }
            }
        }
    }

    private void findWallpaperTarget(WindowList windows, FindWallpaperTargetResult result) {
        WindowAnimator winAnimator = this.mService.mAnimator;
        result.reset();
        WindowState w = null;
        int windowDetachedI = -1;
        boolean resetTopWallpaper = false;
        boolean inFreeformSpace = false;
        boolean replacing = false;
        for (int i = windows.size() - 1; i >= 0; i--) {
            w = (WindowState) windows.get(i);
            if (w.mAttrs.type != 2013) {
                resetTopWallpaper = true;
                if (w == winAnimator.mWindowDetachedWallpaper || w.mAppToken == null || !w.mAppToken.hidden || w.mAppToken.mAppAnimator.animation != null) {
                    if (!inFreeformSpace) {
                        TaskStack stack = w.getStack();
                        inFreeformSpace = stack != null && stack.mStackId == 2;
                    }
                    replacing = !replacing ? w.mWillReplaceWindow : true;
                    this.mService.showWallpaperIfNeed(w);
                    boolean hasWallpaper = (w.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0 ? w.mAppToken != null ? w.mWinAnimator.mKeyguardGoingAwayWithWallpaper : false : true;
                    if (hasWallpaper && w.isOnScreen() && (this.mWallpaperTarget == w || w.isDrawFinishedLw())) {
                        result.setWallpaperTarget(w, i);
                        if (w != this.mWallpaperTarget || !w.mWinAnimator.isAnimationSet()) {
                            break;
                        }
                    } else if (w == winAnimator.mWindowDetachedWallpaper) {
                        windowDetachedI = i;
                    }
                }
            } else if (result.topWallpaper == null || resetTopWallpaper) {
                result.setTopWallpaper(w, i);
                resetTopWallpaper = false;
            }
        }
        if (result.wallpaperTarget == null && windowDetachedI >= 0) {
            result.setWallpaperTarget(w, windowDetachedI);
        }
        if (result.wallpaperTarget != null) {
            return;
        }
        if (inFreeformSpace || (r3 && this.mWallpaperTarget != null)) {
            result.setWallpaperTarget(result.topWallpaper, result.topWallpaperIndex);
        }
    }

    private boolean updateWallpaperWindowsTarget(WindowList windows, FindWallpaperTargetResult result) {
        boolean targetChanged = false;
        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        if (this.mWallpaperTarget != wallpaperTarget && (this.mLowerWallpaperTarget == null || this.mLowerWallpaperTarget != wallpaperTarget)) {
            this.mLowerWallpaperTarget = null;
            this.mUpperWallpaperTarget = null;
            WindowState oldW = this.mWallpaperTarget;
            this.mWallpaperTarget = wallpaperTarget;
            targetChanged = true;
            if (!(wallpaperTarget == null || oldW == null)) {
                boolean oldAnim = oldW.isAnimatingLw();
                if (wallpaperTarget.isAnimatingLw() && oldAnim) {
                    int oldI = windows.indexOf(oldW);
                    if (oldI >= 0) {
                        if (wallpaperTarget.mAppToken != null && wallpaperTarget.mAppToken.hiddenRequested) {
                            this.mWallpaperTarget = oldW;
                            wallpaperTarget = oldW;
                            wallpaperTargetIndex = oldI;
                        } else if (wallpaperTargetIndex > oldI) {
                            this.mUpperWallpaperTarget = wallpaperTarget;
                            this.mLowerWallpaperTarget = oldW;
                            wallpaperTarget = oldW;
                            wallpaperTargetIndex = oldI;
                        } else {
                            this.mUpperWallpaperTarget = oldW;
                            this.mLowerWallpaperTarget = wallpaperTarget;
                        }
                    }
                }
            }
        } else if (!(this.mLowerWallpaperTarget == null || (this.mLowerWallpaperTarget.isAnimatingLw() && this.mUpperWallpaperTarget.isAnimatingLw()))) {
            this.mLowerWallpaperTarget = null;
            this.mUpperWallpaperTarget = null;
            this.mWallpaperTarget = wallpaperTarget;
            targetChanged = true;
        }
        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return targetChanged;
    }

    boolean updateWallpaperWindowsTargetByLayer(WindowList windows, FindWallpaperTargetResult result) {
        boolean visible;
        int i = 0;
        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        if (wallpaperTarget != null) {
            visible = true;
        } else {
            visible = false;
        }
        if (visible) {
            visible = isWallpaperVisible(wallpaperTarget);
            if (this.mLowerWallpaperTarget == null && wallpaperTarget.mAppToken != null) {
                i = wallpaperTarget.mAppToken.mAppAnimator.animLayerAdjustment;
            }
            this.mWallpaperAnimLayerAdjustment = i;
            int maxLayer = (this.mService.mPolicy.getMaxWallpaperLayer() * 10000) + 1000;
            while (wallpaperTargetIndex > 0) {
                WindowState wb = (WindowState) windows.get(wallpaperTargetIndex - 1);
                if (wb.mBaseLayer < maxLayer && wb.mAttachedWindow != wallpaperTarget && ((wallpaperTarget.mAttachedWindow == null || wb.mAttachedWindow != wallpaperTarget.mAttachedWindow) && (wb.mAttrs.type != 3 || wallpaperTarget.mToken == null || wb.mToken != wallpaperTarget.mToken))) {
                    break;
                }
                wallpaperTarget = wb;
                wallpaperTargetIndex--;
            }
        }
        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return visible;
    }

    boolean updateWallpaperWindowsPlacement(WindowList windows, WindowState wallpaperTarget, int wallpaperTargetIndex, boolean visible) {
        DisplayInfo displayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        boolean changed = false;
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenNdx);
            if (token.hidden == visible) {
                token.hidden = !visible;
                this.mService.getDefaultDisplayContentLocked().layoutNeeded = true;
            }
            WindowList tokenWindows = token.windows;
            for (int wallpaperNdx = tokenWindows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                WindowState wallpaper = (WindowState) tokenWindows.get(wallpaperNdx);
                if (visible) {
                    updateWallpaperOffset(wallpaper, dw, dh, false);
                }
                dispatchWallpaperVisibility(wallpaper, visible);
                wallpaper.mWinAnimator.mAnimLayer = wallpaper.mLayer + this.mWallpaperAnimLayerAdjustment;
                if (wallpaper == wallpaperTarget) {
                    wallpaperTargetIndex--;
                    if (wallpaperTargetIndex > 0) {
                        wallpaperTarget = (WindowState) windows.get(wallpaperTargetIndex - 1);
                    } else {
                        wallpaperTarget = null;
                    }
                } else {
                    int oldIndex = windows.indexOf(wallpaper);
                    if (oldIndex >= 0) {
                        windows.remove(oldIndex);
                        this.mService.mWindowsChanged = true;
                        if (oldIndex < wallpaperTargetIndex) {
                            wallpaperTargetIndex--;
                        }
                    }
                    int insertionIndex = 0;
                    if (visible && wallpaperTarget != null) {
                        int type = wallpaperTarget.mAttrs.type;
                        if ((wallpaperTarget.mAttrs.privateFlags & 1024) != 0 || type == 2029) {
                            insertionIndex = windows.indexOf(wallpaperTarget);
                        }
                    }
                    windows.add(insertionIndex, wallpaper);
                    this.mService.mWindowsChanged = true;
                    changed = true;
                }
            }
        }
        return changed;
    }

    boolean adjustWallpaperWindows() {
        this.mService.mWindowPlacerLocked.mWallpaperMayChange = false;
        WindowList windows = this.mService.getDefaultWindowListLocked();
        findWallpaperTarget(windows, this.mFindResults);
        boolean targetChanged = updateWallpaperWindowsTarget(windows, this.mFindResults);
        boolean visible = updateWallpaperWindowsTargetByLayer(windows, this.mFindResults);
        WindowState wallpaperTarget = this.mFindResults.wallpaperTarget;
        int wallpaperTargetIndex = this.mFindResults.wallpaperTargetIndex;
        if (wallpaperTarget != null || this.mFindResults.topWallpaper == null) {
            wallpaperTarget = wallpaperTargetIndex > 0 ? (WindowState) windows.get(wallpaperTargetIndex - 1) : null;
        } else {
            wallpaperTarget = this.mFindResults.topWallpaper;
            wallpaperTargetIndex = this.mFindResults.topWallpaperIndex + 1;
        }
        if (visible) {
            if (this.mWallpaperTarget.mWallpaperX >= 0.0f) {
                this.mLastWallpaperX = this.mWallpaperTarget.mWallpaperX;
                this.mLastWallpaperXStep = this.mWallpaperTarget.mWallpaperXStep;
            }
            if (this.mWallpaperTarget.mWallpaperY >= 0.0f) {
                this.mLastWallpaperY = this.mWallpaperTarget.mWallpaperY;
                this.mLastWallpaperYStep = this.mWallpaperTarget.mWallpaperYStep;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetX = this.mWallpaperTarget.mWallpaperDisplayOffsetX;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetY = this.mWallpaperTarget.mWallpaperDisplayOffsetY;
            }
        }
        return targetChanged ? updateWallpaperWindowsPlacement(windows, wallpaperTarget, wallpaperTargetIndex, visible) : updateWallpaperWindowsPlacement(windows, wallpaperTarget, wallpaperTargetIndex, visible);
    }

    boolean processWallpaperDrawPendingTimeout() {
        if (this.mWallpaperDrawState != 1) {
            return false;
        }
        this.mWallpaperDrawState = 2;
        return true;
    }

    boolean wallpaperTransitionReady() {
        boolean transitionReady = true;
        boolean wallpaperReady = true;
        for (int curTokenIndex = this.mWallpaperTokens.size() - 1; curTokenIndex >= 0 && wallpaperReady; curTokenIndex--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenIndex);
            int curWallpaperIndex = token.windows.size() - 1;
            while (curWallpaperIndex >= 0) {
                WindowState wallpaper = (WindowState) token.windows.get(curWallpaperIndex);
                if (!wallpaper.mWallpaperVisible || wallpaper.isDrawnLw()) {
                    curWallpaperIndex--;
                } else {
                    wallpaperReady = false;
                    if (this.mWallpaperDrawState != 2) {
                        transitionReady = false;
                    }
                    if (this.mWallpaperDrawState == 0) {
                        this.mWallpaperDrawState = 1;
                        this.mService.mH.removeMessages(39);
                        this.mService.mH.sendEmptyMessageDelayed(39, 500);
                    }
                }
            }
        }
        if (wallpaperReady) {
            this.mWallpaperDrawState = 0;
            this.mService.mH.removeMessages(39);
        }
        return transitionReady;
    }

    void addWallpaperToken(WindowToken token) {
        this.mWallpaperTokens.add(token);
    }

    void removeWallpaperToken(WindowToken token) {
        this.mWallpaperTokens.remove(token);
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mWallpaperTarget=");
        pw.println(this.mWallpaperTarget);
        if (!(this.mLowerWallpaperTarget == null && this.mUpperWallpaperTarget == null)) {
            pw.print(prefix);
            pw.print("mLowerWallpaperTarget=");
            pw.println(this.mLowerWallpaperTarget);
            pw.print(prefix);
            pw.print("mUpperWallpaperTarget=");
            pw.println(this.mUpperWallpaperTarget);
        }
        pw.print(prefix);
        pw.print("mLastWallpaperX=");
        pw.print(this.mLastWallpaperX);
        pw.print(" mLastWallpaperY=");
        pw.println(this.mLastWallpaperY);
        if (this.mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE || this.mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            pw.print(prefix);
            pw.print("mLastWallpaperDisplayOffsetX=");
            pw.print(this.mLastWallpaperDisplayOffsetX);
            pw.print(" mLastWallpaperDisplayOffsetY=");
            pw.println(this.mLastWallpaperDisplayOffsetY);
        }
    }

    void dumpTokens(PrintWriter pw, String prefix, boolean dumpAll) {
        if (!this.mWallpaperTokens.isEmpty()) {
            pw.println();
            pw.print(prefix);
            pw.println("Wallpaper tokens:");
            for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) this.mWallpaperTokens.get(i);
                pw.print(prefix);
                pw.print("Wallpaper #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
    }
}
