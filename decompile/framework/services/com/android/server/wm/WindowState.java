package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.rog.AppRogInfo;
import android.rog.IRogManager;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.IWindowFocusObserver;
import android.view.IWindowId;
import android.view.IWindowId.Stub;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwWindowStateAnimator;
import com.android.server.LocalServices;
import com.android.server.input.InputWindowHandle;
import com.android.server.power.IHwShutdownThread;
import java.io.PrintWriter;

public final class WindowState implements android.view.WindowManagerPolicy.WindowState {
    static final boolean DEBUG_DISABLE_SAVING_SURFACES = false;
    public static final int LOW_RESOLUTION_COMPOSITION_OFF = 1;
    public static final int LOW_RESOLUTION_COMPOSITION_ON = 2;
    public static final int LOW_RESOLUTION_FEATURE_OFF = 0;
    static final int MINIMUM_VISIBLE_HEIGHT_IN_DP = 36;
    static final int MINIMUM_VISIBLE_WIDTH_IN_DP = 42;
    static final int RESIZE_HANDLE_WIDTH_IN_DP = 30;
    static final boolean SHOW_TRANSACTIONS = false;
    static final String TAG = "WindowManager";
    static final Region sEmptyRegion = new Region();
    private static final Rect sTmpRect = new Rect();
    boolean mAnimateReplacingWindow = false;
    boolean mAnimatingExit;
    private boolean mAnimatingWithSavedSurface;
    boolean mAppDied;
    boolean mAppFreezing;
    final int mAppOp;
    public boolean mAppOpVisibility = true;
    AppWindowToken mAppToken;
    boolean mAttachedHidden;
    public final WindowState mAttachedWindow;
    public final LayoutParams mAttrs = new LayoutParams();
    final int mBaseLayer;
    private boolean mCanCarryColors;
    public final WindowList mChildWindows = new WindowList();
    final IWindow mClient;
    InputChannel mClientChannel;
    final Rect mCompatFrame = new Rect();
    private boolean mConfigHasChanged;
    final Rect mContainingFrame = new Rect();
    boolean mContentChanged;
    final Rect mContentFrame = new Rect();
    final Rect mContentInsets = new Rect();
    boolean mContentInsetsChanged;
    final Context mContext;
    private DeadWindowEventReceiver mDeadWindowEventReceiver;
    final DeathRecipient mDeathRecipient;
    final Rect mDecorFrame = new Rect();
    boolean mDestroying;
    DisplayContent mDisplayContent;
    final Rect mDisplayFrame = new Rect();
    boolean mDragResizing;
    boolean mDragResizingChangeReported;
    WakeLock mDrawLock;
    boolean mEnforceSizeCompat;
    RemoteCallbackList<IWindowFocusObserver> mFocusCallbacks;
    int mForceCompatMode = 0;
    final Rect mFrame = new Rect();
    final Rect mGivenContentInsets = new Rect();
    boolean mGivenInsetsPending;
    final Region mGivenTouchableRegion = new Region();
    final Rect mGivenVisibleInsets = new Rect();
    float mGlobalScale = 1.0f;
    float mHScale = 1.0f;
    boolean mHasSurface = false;
    boolean mHaveFrame;
    boolean mInRelayout;
    InputChannel mInputChannel;
    final InputWindowHandle mInputWindowHandle;
    final Rect mInsetFrame = new Rect();
    float mInvGlobalScale = 1.0f;
    final boolean mIsFloatingLayer;
    final boolean mIsImWindow;
    final boolean mIsWallpaper;
    private boolean mJustMovedInStack;
    final Rect mLastContentInsets = new Rect();
    final Rect mLastFrame = new Rect();
    int mLastFreezeDuration;
    float mLastHScale = 1.0f;
    final Rect mLastOutsets = new Rect();
    final Rect mLastOverscanInsets = new Rect();
    int mLastRequestedHeight;
    int mLastRequestedWidth;
    final Rect mLastStableInsets = new Rect();
    CharSequence mLastTitle;
    float mLastVScale = 1.0f;
    final Rect mLastVisibleInsets = new Rect();
    int mLayer;
    final boolean mLayoutAttached;
    boolean mLayoutNeeded;
    int mLayoutSeq = -1;
    private Configuration mMergedConfiguration = new Configuration();
    boolean mMovedByResize;
    boolean mNotOnAppsDisplay = false;
    boolean mObscured;
    boolean mOrientationChanging;
    final Rect mOutsetFrame = new Rect();
    final Rect mOutsets = new Rect();
    boolean mOutsetsChanged = false;
    final Rect mOverscanFrame = new Rect();
    final Rect mOverscanInsets = new Rect();
    boolean mOverscanInsetsChanged;
    final int mOwnerUid;
    final Rect mParentFrame = new Rect();
    final WindowManagerPolicy mPolicy;
    boolean mPolicyVisibility = true;
    boolean mPolicyVisibilityAfterAnim = true;
    boolean mRebuilding;
    boolean mRelayoutCalled;
    boolean mRemoveOnExit;
    boolean mRemoved;
    boolean mReplacingRemoveRequested = false;
    WindowState mReplacingWindow = null;
    int mRequestedHeight;
    int mRequestedWidth;
    int mResizeMode;
    boolean mResizedWhileGone = false;
    private boolean mResizedWhileNotDragResizing;
    private boolean mResizedWhileNotDragResizingReported;
    WindowToken mRootToken;
    int mSeq;
    final WindowManagerService mService;
    final Session mSession;
    private boolean mShowToOwnerOnly;
    final Point mShownPosition = new Point();
    boolean mSkipEnterAnimationForSeamlessReplacement = false;
    final Rect mStableFrame = new Rect();
    final Rect mStableInsets = new Rect();
    boolean mStableInsetsChanged;
    String mStringNameCache;
    final int mSubLayer;
    private boolean mSurfaceSaved = false;
    int mSystemUiVisibility;
    AppWindowToken mTargetAppToken;
    private final Configuration mTmpConfig = new Configuration();
    final Matrix mTmpMatrix = new Matrix();
    private final Rect mTmpRect = new Rect();
    WindowToken mToken;
    int mTouchableInsets = 0;
    boolean mTurnOnScreen;
    float mVScale = 1.0f;
    int mViewVisibility;
    final Rect mVisibleFrame = new Rect();
    final Rect mVisibleInsets = new Rect();
    boolean mVisibleInsetsChanged;
    int mWallpaperDisplayOffsetX = Integer.MIN_VALUE;
    int mWallpaperDisplayOffsetY = Integer.MIN_VALUE;
    boolean mWallpaperVisible;
    float mWallpaperX = -1.0f;
    float mWallpaperXStep = -1.0f;
    float mWallpaperY = -1.0f;
    float mWallpaperYStep = -1.0f;
    boolean mWasExiting;
    boolean mWasVisibleBeforeClientHidden;
    boolean mWillReplaceWindow = false;
    final WindowStateAnimator mWinAnimator;
    final IWindowId mWindowId;
    boolean mWindowRemovalAllowed;
    int mXOffset;
    int mYOffset;

    private final class DeadWindowEventReceiver extends InputEventReceiver {
        DeadWindowEventReceiver(InputChannel inputChannel) {
            super(inputChannel, WindowState.this.mService.mH.getLooper());
        }

        public void onInputEvent(InputEvent event) {
            finishInputEvent(event, true);
        }
    }

    private class DeathRecipient implements android.os.IBinder.DeathRecipient {
        private DeathRecipient() {
        }

        public void binderDied() {
            try {
                synchronized (WindowState.this.mService.mWindowMap) {
                    WindowState win = WindowState.this.mService.windowForClientLocked(WindowState.this.mSession, WindowState.this.mClient, false);
                    Slog.i(WindowState.TAG, "WIN DEATH: " + win);
                    if (win != null) {
                        WindowState.this.mService.removeWindowLocked(win, WindowState.this.shouldKeepVisibleDeadAppWindow());
                        if (win.mAttrs.type == 2034) {
                            TaskStack stack = (TaskStack) WindowState.this.mService.mStackIdToStack.get(3);
                            if (stack != null) {
                                stack.resetDockedStackToMiddle();
                            }
                            WindowState.this.mService.setDockedStackResizing(false);
                        }
                    } else if (WindowState.this.mHasSurface) {
                        Slog.e(WindowState.TAG, "!!! LEAK !!! Window removed but surface still valid.");
                        WindowState.this.mService.removeWindowLocked(WindowState.this);
                    }
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    WindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState attachedWindow, int appOp, int seq, LayoutParams a, int viewVisibility, DisplayContent displayContent, int forceCompatFlag) {
        boolean isRogEnable;
        this.mService = service;
        this.mSession = s;
        this.mClient = c;
        this.mAppOp = appOp;
        this.mToken = token;
        this.mOwnerUid = s.mUid;
        this.mWindowId = new Stub() {
            public void registerFocusObserver(IWindowFocusObserver observer) {
                WindowState.this.registerFocusObserver(observer);
            }

            public void unregisterFocusObserver(IWindowFocusObserver observer) {
                WindowState.this.unregisterFocusObserver(observer);
            }

            public boolean isFocused() {
                return WindowState.this.isFocused();
            }
        };
        this.mAttrs.copyFrom(a);
        this.mViewVisibility = viewVisibility;
        this.mDisplayContent = displayContent;
        this.mPolicy = this.mService.mPolicy;
        this.mContext = this.mService.mContext;
        WindowState windowState = this;
        DeathRecipient deathRecipient = new DeathRecipient();
        this.mSeq = seq;
        if ((this.mAttrs.privateFlags & 128) == 0) {
            isRogEnable = isRogEnable();
        } else {
            isRogEnable = true;
        }
        this.mEnforceSizeCompat = isRogEnable;
        try {
            c.asBinder().linkToDeath(deathRecipient, 0);
            this.mDeathRecipient = deathRecipient;
            if (this.mAttrs.type < 1000 || this.mAttrs.type > 1999) {
                this.mBaseLayer = (this.mPolicy.windowTypeToLayerLw(a.type) * 10000) + 1000;
                this.mSubLayer = 0;
                this.mAttachedWindow = null;
                this.mLayoutAttached = false;
                isRogEnable = this.mAttrs.type != 2011 ? this.mAttrs.type == 2012 : true;
                this.mIsImWindow = isRogEnable;
                this.mIsWallpaper = this.mAttrs.type == 2013;
                this.mIsFloatingLayer = !this.mIsImWindow ? this.mIsWallpaper : true;
            } else {
                this.mBaseLayer = (this.mPolicy.windowTypeToLayerLw(attachedWindow.mAttrs.type) * 10000) + 1000;
                this.mSubLayer = this.mPolicy.subWindowTypeToLayerLw(a.type);
                this.mAttachedWindow = attachedWindow;
                WindowList childWindows = this.mAttachedWindow.mChildWindows;
                int numChildWindows = childWindows.size();
                if (numChildWindows == 0) {
                    childWindows.add(this);
                } else {
                    boolean added = false;
                    for (int i = 0; i < numChildWindows; i++) {
                        int childSubLayer = ((WindowState) childWindows.get(i)).mSubLayer;
                        if (this.mSubLayer < childSubLayer || (this.mSubLayer == childSubLayer && childSubLayer < 0)) {
                            childWindows.add(i, this);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        childWindows.add(this);
                    }
                }
                this.mLayoutAttached = this.mAttrs.type != 1003;
                isRogEnable = attachedWindow.mAttrs.type != 2011 ? attachedWindow.mAttrs.type == 2012 : true;
                this.mIsImWindow = isRogEnable;
                this.mIsWallpaper = attachedWindow.mAttrs.type == 2013;
                if (this.mIsImWindow) {
                    isRogEnable = true;
                } else {
                    isRogEnable = this.mIsWallpaper;
                }
                this.mIsFloatingLayer = isRogEnable;
            }
            WindowState appWin = this;
            while (appWin.isChildWindow()) {
                appWin = appWin.mAttachedWindow;
            }
            if (forceCompatFlag == -3) {
                this.mForceCompatMode = appWin.mForceCompatMode;
            } else {
                this.mForceCompatMode = forceCompatFlag;
            }
            WindowToken appToken = appWin.mToken;
            while (appToken.appWindowToken == null) {
                WindowToken parent = (WindowToken) this.mService.mTokenMap.get(appToken.token);
                if (parent == null || appToken == parent) {
                    break;
                }
                appToken = parent;
            }
            this.mRootToken = appToken;
            this.mAppToken = appToken.appWindowToken;
            if (this.mAppToken != null) {
                this.mNotOnAppsDisplay = displayContent != getDisplayContent();
                if (this.mAppToken.showForAllUsers) {
                    LayoutParams layoutParams = this.mAttrs;
                    layoutParams.flags |= DumpState.DUMP_FROZEN;
                }
            }
            IHwWindowStateAnimator iwsa = HwServiceFactory.getHuaweiWindowStateAnimator();
            if (iwsa != null) {
                this.mWinAnimator = iwsa.getInstance(this);
            } else {
                this.mWinAnimator = new WindowStateAnimator(this);
            }
            this.mWinAnimator.mAlpha = a.alpha;
            this.mRequestedWidth = 0;
            this.mRequestedHeight = 0;
            this.mLastRequestedWidth = 0;
            this.mLastRequestedHeight = 0;
            this.mXOffset = 0;
            this.mYOffset = 0;
            this.mLayer = 0;
            this.mInputWindowHandle = new InputWindowHandle(this.mAppToken != null ? this.mAppToken.mInputApplicationHandle : null, this, displayContent.getDisplayId());
        } catch (RemoteException e) {
            this.mDeathRecipient = null;
            this.mAttachedWindow = null;
            this.mLayoutAttached = false;
            this.mIsImWindow = false;
            this.mIsWallpaper = false;
            this.mIsFloatingLayer = false;
            this.mBaseLayer = 0;
            this.mSubLayer = 0;
            this.mInputWindowHandle = null;
            this.mWinAnimator = null;
        }
    }

    void attach() {
        this.mSession.windowAddedLocked();
    }

    public int getOwningUid() {
        return this.mOwnerUid;
    }

    public String getOwningPackage() {
        return this.mAttrs.packageName;
    }

    private void subtractInsets(Rect frame, Rect layoutFrame, Rect insetFrame, Rect displayFrame) {
        frame.inset(Math.max(0, insetFrame.left - Math.max(layoutFrame.left, displayFrame.left)), Math.max(0, insetFrame.top - Math.max(layoutFrame.top, displayFrame.top)), Math.max(0, Math.min(layoutFrame.right, displayFrame.right) - insetFrame.right), Math.max(0, Math.min(layoutFrame.bottom, displayFrame.bottom) - insetFrame.bottom));
    }

    public void computeFrameLw(Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf, Rect sf, Rect osf) {
        if (!this.mWillReplaceWindow || (!this.mAnimatingExit && this.mReplacingRemoveRequested)) {
            Rect layoutDisplayFrame;
            Rect layoutContainingFrame;
            int layoutXDiff;
            int layoutYDiff;
            this.mHaveFrame = true;
            Task task = getTask();
            boolean fullscreenTask = !isInMultiWindowMode();
            boolean isFloating = task != null ? task.isFloating() : false;
            if (fullscreenTask) {
                this.mInsetFrame.setEmpty();
            } else {
                task.getTempInsetBounds(this.mInsetFrame);
            }
            if (fullscreenTask || layoutInParentFrame()) {
                this.mContainingFrame.set(pf);
                this.mDisplayFrame.set(df);
                layoutDisplayFrame = df;
                layoutContainingFrame = pf;
                layoutXDiff = 0;
                layoutYDiff = 0;
            } else {
                task.getBounds(this.mContainingFrame);
                if (!(this.mAppToken == null || this.mAppToken.mFrozenBounds.isEmpty())) {
                    Rect frozen = (Rect) this.mAppToken.mFrozenBounds.peek();
                    this.mContainingFrame.right = this.mContainingFrame.left + frozen.width();
                    this.mContainingFrame.bottom = this.mContainingFrame.top + frozen.height();
                }
                WindowState imeWin = this.mService.mInputMethodWindow;
                if (imeWin != null && imeWin.isVisibleNow() && this.mService.mInputMethodTarget == this) {
                    if (!isFloating || this.mContainingFrame.bottom <= cf.bottom) {
                        if (this.mContainingFrame.bottom > pf.bottom) {
                            this.mContainingFrame.bottom = pf.bottom;
                        }
                    } else {
                        Rect rect = this.mContainingFrame;
                        rect.top -= this.mContainingFrame.bottom - cf.bottom;
                    }
                }
                if (isFloating && this.mContainingFrame.isEmpty()) {
                    this.mContainingFrame.set(cf);
                }
                this.mDisplayFrame.set(this.mContainingFrame);
                layoutXDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.left - this.mContainingFrame.left : 0;
                layoutYDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.top - this.mContainingFrame.top : 0;
                layoutContainingFrame = !this.mInsetFrame.isEmpty() ? this.mInsetFrame : this.mContainingFrame;
                this.mTmpRect.set(0, 0, this.mDisplayContent.getDisplayInfo().logicalWidth, this.mDisplayContent.getDisplayInfo().logicalHeight);
                subtractInsets(this.mDisplayFrame, layoutContainingFrame, df, this.mTmpRect);
                if (!layoutInParentFrame()) {
                    subtractInsets(this.mContainingFrame, layoutContainingFrame, pf, this.mTmpRect);
                    subtractInsets(this.mInsetFrame, layoutContainingFrame, pf, this.mTmpRect);
                }
                layoutDisplayFrame = df;
                df.intersect(layoutContainingFrame);
            }
            int pw = this.mContainingFrame.width();
            int ph = this.mContainingFrame.height();
            if (!this.mParentFrame.equals(pf)) {
                this.mParentFrame.set(pf);
                this.mContentChanged = true;
            }
            if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
                this.mLastRequestedWidth = this.mRequestedWidth;
                this.mLastRequestedHeight = this.mRequestedHeight;
                this.mContentChanged = true;
            }
            this.mOverscanFrame.set(of);
            this.mContentFrame.set(cf);
            this.mVisibleFrame.set(vf);
            this.mDecorFrame.set(dcf);
            this.mStableFrame.set(sf);
            boolean hasOutsets = osf != null;
            if (hasOutsets) {
                this.mOutsetFrame.set(osf);
            }
            int fw = this.mFrame.width();
            int fh = this.mFrame.height();
            applyGravityAndUpdateFrame(layoutContainingFrame, layoutDisplayFrame);
            if (hasOutsets) {
                this.mOutsets.set(Math.max(this.mContentFrame.left - this.mOutsetFrame.left, 0), Math.max(this.mContentFrame.top - this.mOutsetFrame.top, 0), Math.max(this.mOutsetFrame.right - this.mContentFrame.right, 0), Math.max(this.mOutsetFrame.bottom - this.mContentFrame.bottom, 0));
            } else {
                this.mOutsets.set(0, 0, 0, 0);
            }
            if (isFloating && !this.mFrame.isEmpty()) {
                int height = Math.min(this.mFrame.height(), this.mContentFrame.height());
                int width = Math.min(this.mContentFrame.width(), this.mFrame.width());
                DisplayMetrics displayMetrics = getDisplayContent().getDisplayMetrics();
                int minVisibleHeight = WindowManagerService.dipToPixel(36, displayMetrics);
                int minVisibleWidth = WindowManagerService.dipToPixel(42, displayMetrics);
                int top = Math.max(this.mContentFrame.top, Math.min(this.mFrame.top, this.mContentFrame.bottom - minVisibleHeight));
                int left = Math.max((this.mContentFrame.left + minVisibleWidth) - width, Math.min(this.mFrame.left, this.mContentFrame.right - minVisibleWidth));
                this.mFrame.set(left, top, left + width, top + height);
                this.mContentFrame.set(this.mFrame);
                this.mVisibleFrame.set(this.mContentFrame);
                this.mStableFrame.set(this.mContentFrame);
            } else if (this.mAttrs.type == 2034) {
                this.mDisplayContent.getDockedDividerController().positionDockedStackedDivider(this.mFrame);
                this.mContentFrame.set(this.mFrame);
                if (!this.mFrame.equals(this.mLastFrame)) {
                    this.mMovedByResize = true;
                }
            } else {
                this.mContentFrame.set(Math.max(this.mContentFrame.left, this.mFrame.left), Math.max(this.mContentFrame.top, this.mFrame.top), Math.min(this.mContentFrame.right, this.mFrame.right), Math.min(this.mContentFrame.bottom, this.mFrame.bottom));
                this.mVisibleFrame.set(Math.max(this.mVisibleFrame.left, this.mFrame.left), Math.max(this.mVisibleFrame.top, this.mFrame.top), Math.min(this.mVisibleFrame.right, this.mFrame.right), Math.min(this.mVisibleFrame.bottom, this.mFrame.bottom));
                this.mStableFrame.set(Math.max(this.mStableFrame.left, this.mFrame.left), Math.max(this.mStableFrame.top, this.mFrame.top), Math.min(this.mStableFrame.right, this.mFrame.right), Math.min(this.mStableFrame.bottom, this.mFrame.bottom));
            }
            if (fullscreenTask && !isFloating) {
                this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, 0), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, 0), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, 0), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, 0));
            }
            if (this.mAttrs.type == 2034) {
                this.mStableInsets.set(Math.max(this.mStableFrame.left - this.mDisplayFrame.left, 0), Math.max(this.mStableFrame.top - this.mDisplayFrame.top, 0), Math.max(this.mDisplayFrame.right - this.mStableFrame.right, 0), Math.max(this.mDisplayFrame.bottom - this.mStableFrame.bottom, 0));
                this.mContentInsets.setEmpty();
                this.mVisibleInsets.setEmpty();
            } else {
                int i;
                int i2;
                if (getDisplayContent() != null) {
                    getDisplayContent().getLogicalDisplayRect(this.mTmpRect);
                } else {
                    this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
                }
                boolean overrideRightInset = !fullscreenTask && this.mFrame.right > this.mTmpRect.right;
                boolean overrideBottomInset = !fullscreenTask && this.mFrame.bottom > this.mTmpRect.bottom;
                Rect rect2 = this.mContentInsets;
                int i3 = this.mContentFrame.left - this.mFrame.left;
                int i4 = this.mContentFrame.top - this.mFrame.top;
                if (overrideRightInset) {
                    i = this.mTmpRect.right - this.mContentFrame.right;
                } else {
                    i = this.mFrame.right - this.mContentFrame.right;
                }
                if (overrideBottomInset) {
                    i2 = this.mTmpRect.bottom - this.mContentFrame.bottom;
                } else {
                    i2 = this.mFrame.bottom - this.mContentFrame.bottom;
                }
                rect2.set(i3, i4, i, i2);
                rect2 = this.mVisibleInsets;
                i3 = this.mVisibleFrame.left - this.mFrame.left;
                i4 = this.mVisibleFrame.top - this.mFrame.top;
                if (overrideRightInset) {
                    i = this.mTmpRect.right - this.mVisibleFrame.right;
                } else {
                    i = this.mFrame.right - this.mVisibleFrame.right;
                }
                if (overrideBottomInset) {
                    i2 = this.mTmpRect.bottom - this.mVisibleFrame.bottom;
                } else {
                    i2 = this.mFrame.bottom - this.mVisibleFrame.bottom;
                }
                rect2.set(i3, i4, i, i2);
                rect2 = this.mStableInsets;
                i3 = Math.max(this.mStableFrame.left - this.mFrame.left, 0);
                i4 = Math.max(this.mStableFrame.top - this.mFrame.top, 0);
                if (overrideRightInset) {
                    i = Math.max(this.mTmpRect.right - this.mStableFrame.right, 0);
                } else {
                    i = Math.max(this.mFrame.right - this.mStableFrame.right, 0);
                }
                if (overrideBottomInset) {
                    i2 = Math.max(this.mTmpRect.bottom - this.mStableFrame.bottom, 0);
                } else {
                    i2 = Math.max(this.mFrame.bottom - this.mStableFrame.bottom, 0);
                }
                rect2.set(i3, i4, i, i2);
            }
            this.mFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mCompatFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mContentFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mVisibleFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mStableFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mCompatFrame.set(this.mFrame);
            if (this.mEnforceSizeCompat) {
                Rect rect3 = new Rect(this.mContentInsets);
                this.mOverscanInsets.scale(this.mInvGlobalScale);
                this.mContentInsets.scale(this.mInvGlobalScale);
                this.mVisibleInsets.scale(this.mInvGlobalScale);
                this.mStableInsets.scale(this.mInvGlobalScale);
                this.mOutsets.scale(this.mInvGlobalScale);
                this.mCompatFrame.scale(this.mInvGlobalScale);
                adjustContentsInsetsInCompatMode(rect3);
            }
            if (this.mIsWallpaper && !(fw == this.mFrame.width() && fh == this.mFrame.height())) {
                DisplayContent displayContent = getDisplayContent();
                if (displayContent != null) {
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    this.mService.mWallpaperControllerLocked.updateWallpaperOffset(this, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                }
            }
        }
    }

    private void adjustContentsInsetsInCompatMode(Rect nonCompatContentInsets) {
        if ((this.mFrame.height() - nonCompatContentInsets.top) - nonCompatContentInsets.bottom != ((int) ((((float) ((this.mCompatFrame.height() - this.mContentInsets.top) - this.mContentInsets.bottom)) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA))) {
            Rect rect = this.mContentInsets;
            rect.bottom--;
            rect = this.mVisibleInsets;
            rect.bottom--;
        }
    }

    public Rect getFrameLw() {
        return this.mFrame;
    }

    public Point getShownPositionLw() {
        return this.mShownPosition;
    }

    public Rect getDisplayFrameLw() {
        return this.mDisplayFrame;
    }

    public Rect getOverscanFrameLw() {
        return this.mOverscanFrame;
    }

    public Rect getContentFrameLw() {
        return this.mContentFrame;
    }

    public Rect getVisibleFrameLw() {
        return this.mVisibleFrame;
    }

    public boolean getGivenInsetsPendingLw() {
        return this.mGivenInsetsPending;
    }

    public Rect getGivenContentInsetsLw() {
        return this.mGivenContentInsets;
    }

    public Rect getGivenVisibleInsetsLw() {
        return this.mGivenVisibleInsets;
    }

    public LayoutParams getAttrs() {
        return this.mAttrs;
    }

    public boolean getNeedsMenuLw(android.view.WindowManagerPolicy.WindowState bottom) {
        boolean z = true;
        int index = -1;
        android.view.WindowManagerPolicy.WindowState ws = this;
        WindowList windows = getWindowList();
        while (ws.mAttrs.needsMenuKey == 0) {
            if (ws == bottom) {
                return false;
            }
            if (index < 0) {
                index = windows.indexOf(ws);
            }
            index--;
            if (index < 0) {
                return false;
            }
            WindowState ws2 = (WindowState) windows.get(index);
        }
        if (ws.mAttrs.needsMenuKey != 1) {
            z = false;
        }
        return z;
    }

    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    public int getSurfaceLayer() {
        return this.mLayer;
    }

    public int getBaseType() {
        WindowState win = this;
        while (win.isChildWindow()) {
            win = win.mAttachedWindow;
        }
        return win.mAttrs.type;
    }

    public IApplicationToken getAppToken() {
        return this.mAppToken != null ? this.mAppToken.appToken : null;
    }

    public boolean isVoiceInteraction() {
        return this.mAppToken != null ? this.mAppToken.voiceInteraction : false;
    }

    boolean setInsetsChanged() {
        int i;
        int i2 = 0;
        this.mOverscanInsetsChanged = (this.mLastOverscanInsets.equals(this.mOverscanInsets) ? 0 : 1) | this.mOverscanInsetsChanged;
        boolean z = this.mContentInsetsChanged;
        if (this.mLastContentInsets.equals(this.mContentInsets)) {
            i = 0;
        } else {
            i = 1;
        }
        this.mContentInsetsChanged = i | z;
        z = this.mVisibleInsetsChanged;
        if (this.mLastVisibleInsets.equals(this.mVisibleInsets)) {
            i = 0;
        } else {
            i = 1;
        }
        this.mVisibleInsetsChanged = i | z;
        z = this.mStableInsetsChanged;
        if (this.mLastStableInsets.equals(this.mStableInsets)) {
            i = 0;
        } else {
            i = 1;
        }
        this.mStableInsetsChanged = i | z;
        boolean z2 = this.mOutsetsChanged;
        if (!this.mLastOutsets.equals(this.mOutsets)) {
            i2 = 1;
        }
        this.mOutsetsChanged = z2 | i2;
        if (this.mOverscanInsetsChanged || this.mContentInsetsChanged || this.mVisibleInsetsChanged) {
            return true;
        }
        return this.mOutsetsChanged;
    }

    public DisplayContent getDisplayContent() {
        if (this.mAppToken == null || this.mNotOnAppsDisplay) {
            return this.mDisplayContent;
        }
        TaskStack stack = getStack();
        return stack == null ? this.mDisplayContent : stack.getDisplayContent();
    }

    public DisplayInfo getDisplayInfo() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent != null) {
            return displayContent.getDisplayInfo();
        }
        return null;
    }

    public int getDisplayId() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return -1;
        }
        return displayContent.getDisplayId();
    }

    Task getTask() {
        return this.mAppToken != null ? this.mAppToken.mTask : null;
    }

    TaskStack getStack() {
        TaskStack taskStack = null;
        Task task = getTask();
        if (task != null && task.mStack != null) {
            return task.mStack;
        }
        if (this.mAttrs.type >= IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && this.mDisplayContent != null) {
            taskStack = this.mDisplayContent.getHomeStack();
        }
        return taskStack;
    }

    void getVisibleBounds(Rect bounds) {
        Task task = getTask();
        boolean cropWindowsToStackBounds = task != null ? task.cropWindowsToStackBounds() : false;
        bounds.setEmpty();
        this.mTmpRect.setEmpty();
        if (cropWindowsToStackBounds) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
            } else {
                cropWindowsToStackBounds = false;
            }
        }
        bounds.set(this.mVisibleFrame);
        if (cropWindowsToStackBounds) {
            bounds.intersect(this.mTmpRect);
        }
        if (bounds.isEmpty()) {
            bounds.set(this.mFrame);
            if (cropWindowsToStackBounds) {
                bounds.intersect(this.mTmpRect);
            }
        }
    }

    public long getInputDispatchingTimeoutNanos() {
        if (this.mAppToken != null) {
            return this.mAppToken.inputDispatchingTimeoutNanos;
        }
        return 5000000000L;
    }

    public boolean hasAppShownWindows() {
        if (this.mAppToken != null) {
            return !this.mAppToken.firstWindowDrawn ? this.mAppToken.startingDisplayed : true;
        } else {
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        if (dsdx < 0.99999f || dsdx > 1.00001f || dtdy < 0.99999f || dtdy > 1.00001f || dtdx < -1.0E-6f || dtdx > 1.0E-6f || dsdy < -1.0E-6f || dsdy > 1.0E-6f) {
            return false;
        }
        return true;
    }

    void prelayout() {
        if (!this.mEnforceSizeCompat) {
            this.mInvGlobalScale = 1.0f;
            this.mGlobalScale = 1.0f;
        } else if (isRogEnable()) {
            float rogScale = getRogScale();
            this.mGlobalScale = rogScale;
            this.mInvGlobalScale = 1.0f / rogScale;
        } else if (this.mForceCompatMode == 1) {
            this.mGlobalScale = this.mService.mForceCompatibleScreenScale;
            this.mInvGlobalScale = 1.0f / this.mGlobalScale;
        } else {
            this.mGlobalScale = this.mService.mCompatibleScreenScale;
            this.mInvGlobalScale = 1.0f / this.mGlobalScale;
        }
    }

    private boolean isVisibleUnchecked() {
        if (!this.mHasSurface || !this.mPolicyVisibility || this.mAttachedHidden || this.mAnimatingExit || this.mDestroying) {
            return false;
        }
        return this.mIsWallpaper ? this.mWallpaperVisible : true;
    }

    public boolean isVisibleLw() {
        return (this.mAppToken == null || !this.mAppToken.hiddenRequested) ? isVisibleUnchecked() : false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVisibleOrBehindKeyguardLw() {
        if (this.mRootToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return false;
        }
        AppWindowToken atoken = this.mAppToken;
        boolean animating = (atoken == null || atoken.mAppAnimator.animation == null) ? false : true;
        if (!(!this.mHasSurface || this.mDestroying || this.mAnimatingExit)) {
            if (atoken != null) {
                if (atoken.hiddenRequested) {
                }
            }
            if (!((this.mAttachedHidden || this.mViewVisibility != 0 || this.mRootToken.hidden) && this.mWinAnimator.mAnimation == null)) {
                animating = true;
            }
            return animating;
        }
        animating = false;
        return animating;
    }

    public boolean isWinVisibleLw() {
        if (this.mAppToken == null || !this.mAppToken.hiddenRequested || this.mAppToken.mAppAnimator.animating) {
            return isVisibleUnchecked();
        }
        return false;
    }

    boolean isVisibleNow() {
        if (!this.mRootToken.hidden || this.mAttrs.type == 3) {
            return isVisibleUnchecked();
        }
        return false;
    }

    boolean isPotentialDragTarget() {
        if (!isVisibleNow() || this.mRemoved || this.mInputChannel == null || this.mInputWindowHandle == null) {
            return false;
        }
        return true;
    }

    boolean isVisibleOrAdding() {
        AppWindowToken atoken = this.mAppToken;
        if ((!this.mHasSurface && (this.mRelayoutCalled || this.mViewVisibility != 0)) || !this.mPolicyVisibility || this.mAttachedHidden) {
            return false;
        }
        if ((atoken != null && atoken.hiddenRequested) || this.mAnimatingExit || this.mDestroying) {
            return false;
        }
        return true;
    }

    boolean isOnScreen() {
        return this.mPolicyVisibility ? isOnScreenIgnoringKeyguard() : false;
    }

    boolean isOnScreenIgnoringKeyguard() {
        boolean z = true;
        if (!this.mHasSurface || this.mDestroying) {
            return false;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken != null) {
            if ((this.mAttachedHidden || atoken.hiddenRequested) && this.mWinAnimator.mAnimation == null && atoken.mAppAnimator.animation == null) {
                z = false;
            }
            return z;
        }
        if (this.mAttachedHidden && this.mWinAnimator.mAnimation == null) {
            z = false;
        }
        return z;
    }

    boolean mightAffectAllDrawn(boolean visibleOnly) {
        boolean isViewVisible = ((this.mAppToken == null || !this.mAppToken.clientHidden) && this.mViewVisibility == 0) ? !this.mWindowRemovalAllowed : false;
        if (((!isOnScreenIgnoringKeyguard() || (visibleOnly && !isViewVisible)) && this.mWinAnimator.mAttrType != 1) || this.mAnimatingExit || this.mDestroying) {
            return false;
        }
        return true;
    }

    boolean isInteresting() {
        if (this.mAppToken == null || this.mAppDied) {
            return false;
        }
        if (this.mAppToken.mAppAnimator.freezingScreen && this.mAppFreezing) {
            return false;
        }
        return true;
    }

    boolean isReadyForDisplay() {
        boolean z = true;
        if (this.mRootToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return false;
        }
        if (!this.mHasSurface || !this.mPolicyVisibility || this.mDestroying) {
            z = false;
        } else if ((this.mAttachedHidden || this.mViewVisibility != 0 || this.mRootToken.hidden) && this.mWinAnimator.mAnimation == null && (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null)) {
            z = false;
        }
        return z;
    }

    boolean isReadyForDisplayIgnoringKeyguard() {
        boolean z = false;
        if (this.mRootToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return false;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken == null && !this.mPolicyVisibility) {
            return false;
        }
        if (this.mHasSurface && !this.mDestroying) {
            if ((!this.mAttachedHidden && this.mViewVisibility == 0 && !this.mRootToken.hidden) || this.mWinAnimator.mAnimation != null) {
                z = true;
            } else if (!(atoken == null || atoken.mAppAnimator.animation == null || this.mWinAnimator.isDummyAnimation())) {
                z = true;
            }
        }
        return z;
    }

    public boolean isDisplayedLw() {
        AppWindowToken atoken = this.mAppToken;
        if (!isDrawnLw() || !this.mPolicyVisibility) {
            return false;
        }
        if ((!this.mAttachedHidden && (atoken == null || !atoken.hiddenRequested)) || this.mWinAnimator.mAnimating) {
            return true;
        }
        if (atoken == null || atoken.mAppAnimator.animation == null) {
            return false;
        }
        return true;
    }

    public boolean isAnimatingLw() {
        if (this.mWinAnimator.mAnimation == null) {
            return (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null) ? false : true;
        } else {
            return true;
        }
    }

    public boolean isGoneForLayoutLw() {
        AppWindowToken atoken = this.mAppToken;
        if (this.mViewVisibility == 8 || !this.mRelayoutCalled || ((atoken == null && this.mRootToken.hidden) || ((atoken != null && atoken.hiddenRequested) || this.mAttachedHidden || (this.mAnimatingExit && !isAnimatingLw())))) {
            return true;
        }
        return this.mDestroying;
    }

    public boolean isDrawFinishedLw() {
        if (!this.mHasSurface || this.mDestroying) {
            return false;
        }
        if (this.mWinAnimator.mDrawState == 2 || this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4) {
            return true;
        }
        return false;
    }

    public boolean isDrawnLw() {
        if (!this.mHasSurface || this.mDestroying) {
            return false;
        }
        if (this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4) {
            return true;
        }
        return false;
    }

    boolean isOpaqueDrawn() {
        if (((!this.mIsWallpaper && this.mAttrs.format == -1) || (this.mIsWallpaper && this.mWallpaperVisible)) && isDrawnLw() && this.mWinAnimator.mAnimation == null) {
            return this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null;
        } else {
            return false;
        }
    }

    boolean hasMoved() {
        if (!this.mHasSurface || ((!this.mContentChanged && !this.mMovedByResize) || this.mAnimatingExit || !this.mService.okToDisplay() || (this.mFrame.top == this.mLastFrame.top && this.mFrame.left == this.mLastFrame.left))) {
            return false;
        }
        if (this.mAttachedWindow == null || !this.mAttachedWindow.hasMoved()) {
            return true;
        }
        return false;
    }

    boolean isObscuringFullscreen(DisplayInfo displayInfo) {
        Task task = getTask();
        if ((task == null || task.mStack == null || task.mStack.isFullscreen()) && isOpaqueDrawn() && isFrameFullscreen(displayInfo)) {
            return true;
        }
        return false;
    }

    boolean isFrameFullscreen(DisplayInfo displayInfo) {
        if (this.mFrame.left > 0 || this.mFrame.top > 0 || this.mFrame.right < displayInfo.appWidth || this.mFrame.bottom < displayInfo.appHeight) {
            return false;
        }
        return true;
    }

    boolean isConfigChanged() {
        getMergedConfig(this.mTmpConfig);
        boolean configChanged = !this.mMergedConfiguration.equals(Configuration.EMPTY) ? this.mTmpConfig.diff(this.mMergedConfiguration) != 0 : true;
        if ((this.mAttrs.privateFlags & 1024) == 0) {
            return configChanged;
        }
        this.mConfigHasChanged |= configChanged;
        return this.mConfigHasChanged;
    }

    boolean isAdjustedForMinimizedDock() {
        if (this.mAppToken == null || this.mAppToken.mTask == null) {
            return false;
        }
        return this.mAppToken.mTask.mStack.isAdjustedForMinimizedDock();
    }

    void removeLocked() {
        disposeInputChannel();
        if (isChildWindow()) {
            this.mAttachedWindow.mChildWindows.remove(this);
        }
        this.mWinAnimator.destroyDeferredSurfaceLocked();
        this.mWinAnimator.destroySurfaceLocked();
        this.mSession.windowRemovedLocked();
        try {
            this.mClient.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
        } catch (RuntimeException e) {
        }
    }

    void setHasSurface(boolean hasSurface) {
        this.mHasSurface = hasSurface;
    }

    int getAnimLayerAdjustment() {
        if (this.mTargetAppToken != null) {
            return this.mTargetAppToken.mAppAnimator.animLayerAdjustment;
        }
        if (this.mAppToken != null) {
            return this.mAppToken.mAppAnimator.animLayerAdjustment;
        }
        return 0;
    }

    void scheduleAnimationIfDimming() {
        if (this.mDisplayContent != null) {
            DimLayerUser dimLayerUser = getDimLayerUser();
            if (dimLayerUser != null && this.mDisplayContent.mDimLayerController.isDimming(dimLayerUser, this.mWinAnimator)) {
                this.mService.scheduleAnimationLocked();
            }
        }
    }

    void notifyMovedInStack() {
        this.mJustMovedInStack = true;
    }

    boolean hasJustMovedInStack() {
        return this.mJustMovedInStack;
    }

    void resetJustMovedInStack() {
        this.mJustMovedInStack = false;
    }

    void openInputChannel(InputChannel outInputChannel) {
        if (this.mInputChannel != null) {
            throw new IllegalStateException("Window already has an input channel.");
        }
        InputChannel[] inputChannels = InputChannel.openInputChannelPair(makeInputChannelName());
        this.mInputChannel = inputChannels[0];
        this.mClientChannel = inputChannels[1];
        this.mInputWindowHandle.inputChannel = inputChannels[0];
        if (outInputChannel != null) {
            this.mClientChannel.transferTo(outInputChannel);
            this.mClientChannel.dispose();
            this.mClientChannel = null;
        } else {
            this.mDeadWindowEventReceiver = new DeadWindowEventReceiver(this.mClientChannel);
        }
        this.mService.mInputManager.registerInputChannel(this.mInputChannel, this.mInputWindowHandle);
    }

    void disposeInputChannel() {
        if (this.mDeadWindowEventReceiver != null) {
            this.mDeadWindowEventReceiver.dispose();
            this.mDeadWindowEventReceiver = null;
        }
        if (this.mInputChannel != null) {
            this.mService.mInputManager.unregisterInputChannel(this.mInputChannel);
            this.mInputChannel.dispose();
            this.mInputChannel = null;
        }
        if (this.mClientChannel != null) {
            this.mClientChannel.dispose();
            this.mClientChannel = null;
        }
        this.mInputWindowHandle.inputChannel = null;
    }

    void applyDimLayerIfNeeded() {
        AppWindowToken token = this.mAppToken;
        if (token == null || !token.removed) {
            if (!this.mAnimatingExit && this.mAppDied) {
                this.mDisplayContent.mDimLayerController.applyDimAbove(getDimLayerUser(), this.mWinAnimator);
            } else if (!((this.mAttrs.flags & 2) == 0 || this.mDisplayContent == null || this.mAnimatingExit || !isVisibleUnchecked())) {
                this.mDisplayContent.mDimLayerController.applyDimBehind(getDimLayerUser(), this.mWinAnimator);
            }
        }
    }

    DimLayerUser getDimLayerUser() {
        Task task = getTask();
        if (task != null) {
            return task;
        }
        return getStack();
    }

    void maybeRemoveReplacedWindow() {
        if (this.mAppToken != null) {
            for (int i = this.mAppToken.allAppWindows.size() - 1; i >= 0; i--) {
                WindowState win = (WindowState) this.mAppToken.allAppWindows.get(i);
                if (win.mWillReplaceWindow && win.mReplacingWindow == this && hasDrawnLw()) {
                    if (win.isDimming()) {
                        win.transferDimToReplacement();
                    }
                    win.mWillReplaceWindow = false;
                    boolean animateReplacingWindow = win.mAnimateReplacingWindow;
                    win.mAnimateReplacingWindow = false;
                    win.mReplacingRemoveRequested = false;
                    win.mReplacingWindow = null;
                    this.mSkipEnterAnimationForSeamlessReplacement = false;
                    if (win.mAnimatingExit || !animateReplacingWindow) {
                        this.mService.removeWindowInnerLocked(win);
                    }
                }
            }
        }
    }

    void setDisplayLayoutNeeded() {
        if (this.mDisplayContent != null) {
            this.mDisplayContent.layoutNeeded = true;
        }
    }

    boolean inDockedWorkspace() {
        Task task = getTask();
        return task != null ? task.inDockedWorkspace() : false;
    }

    boolean inPinnedWorkspace() {
        Task task = getTask();
        return task != null ? task.inPinnedWorkspace() : false;
    }

    boolean isDockedInEffect() {
        Task task = getTask();
        return task != null ? task.isDockedInEffect() : false;
    }

    void applyScrollIfNeeded() {
        Task task = getTask();
        if (task != null) {
            task.applyScrollToWindowIfNeeded(this);
        }
    }

    void applyAdjustForImeIfNeeded() {
        Task task = getTask();
        if (task != null && task.mStack != null && task.mStack.isAdjustedForIme()) {
            task.mStack.applyAdjustForImeIfNeeded(task);
        }
    }

    int getTouchableRegion(Region region, int flags) {
        boolean modal = false;
        if ((flags & 40) == 0) {
            modal = true;
        }
        if (!modal || this.mAppToken == null) {
            getTouchableRegion(region);
        } else {
            flags |= 32;
            DimLayerUser dimLayerUser = getDimLayerUser();
            if (dimLayerUser != null) {
                dimLayerUser.getDimBounds(this.mTmpRect);
            } else {
                getVisibleBounds(this.mTmpRect);
            }
            if (inFreeformWorkspace()) {
                int delta = WindowManagerService.dipToPixel(30, getDisplayContent().getDisplayMetrics());
                this.mTmpRect.inset(-delta, -delta);
            }
            region.set(this.mTmpRect);
            cropRegionToStackBoundsIfNeeded(region);
        }
        return flags;
    }

    void checkPolicyVisibilityChange() {
        if (this.mPolicyVisibility != this.mPolicyVisibilityAfterAnim) {
            this.mPolicyVisibility = this.mPolicyVisibilityAfterAnim;
            setDisplayLayoutNeeded();
            if (!this.mPolicyVisibility) {
                if (this.mService.mCurrentFocus == this) {
                    this.mService.mFocusMayChange = true;
                }
                this.mService.enableScreenIfNeededLocked();
            }
        }
    }

    void setRequestedSize(int requestedWidth, int requestedHeight) {
        if (this.mRequestedWidth != requestedWidth || this.mRequestedHeight != requestedHeight) {
            this.mLayoutNeeded = true;
            this.mRequestedWidth = requestedWidth;
            this.mRequestedHeight = requestedHeight;
        }
    }

    void prepareWindowToDisplayDuringRelayout(Configuration outConfig) {
        if ((this.mAttrs.softInputMode & 240) == 16) {
            this.mLayoutNeeded = true;
        }
        if (isDrawnLw() && this.mService.okToDisplay()) {
            this.mWinAnimator.applyEnterAnimationLocked();
        }
        if ((this.mAttrs.flags & 2097152) != 0) {
            this.mTurnOnScreen = true;
        }
        if (isConfigChanged()) {
            outConfig.setTo(updateConfiguration());
        }
    }

    void adjustStartingWindowFlags() {
        if (this.mAttrs.type == 1 && this.mAppToken != null && this.mAppToken.startingWindow != null) {
            LayoutParams sa = this.mAppToken.startingWindow.mAttrs;
            sa.flags = (sa.flags & -4718594) | (this.mAttrs.flags & 4718593);
        }
    }

    void setWindowScale(int requestedWidth, int requestedHeight) {
        boolean scaledWindow = false;
        float f = 1.0f;
        if ((this.mAttrs.flags & DumpState.DUMP_KEYSETS) != 0) {
            scaledWindow = true;
        }
        if (scaledWindow) {
            float f2;
            if (this.mAttrs.width != requestedWidth) {
                f2 = ((float) this.mAttrs.width) / ((float) requestedWidth);
            } else {
                f2 = 1.0f;
            }
            this.mHScale = f2;
            if (this.mAttrs.height != requestedHeight) {
                f = ((float) this.mAttrs.height) / ((float) requestedHeight);
            }
            this.mVScale = f;
            return;
        }
        this.mVScale = 1.0f;
        this.mHScale = 1.0f;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shouldKeepVisibleDeadAppWindow() {
        boolean z = false;
        if (!isWinVisibleLw() || this.mAppToken == null || this.mAppToken.clientHidden || this.mAttrs.token != this.mClient.asBinder() || this.mAttrs.type == 3) {
            return false;
        }
        TaskStack stack = getStack();
        if (stack != null) {
            z = StackId.keepVisibleDeadAppWindowOnScreen(stack.mStackId);
        }
        return z;
    }

    boolean canReceiveKeys() {
        if (!isVisibleOrAdding() || this.mViewVisibility != 0 || this.mRemoveOnExit || (this.mAttrs.flags & 8) != 0) {
            return false;
        }
        if ((this.mAppToken == null || this.mAppToken.windowsAreFocusable()) && !isAdjustedForMinimizedDock()) {
            return true;
        }
        return false;
    }

    public boolean hasDrawnLw() {
        return this.mWinAnimator.mDrawState == 4;
    }

    public boolean showLw(boolean doAnimation) {
        return showLw(doAnimation, true);
    }

    boolean showLw(boolean doAnimation, boolean requestAnim) {
        if (isHiddenFromUserLocked() || !this.mAppOpVisibility) {
            return false;
        }
        if (this.mPolicyVisibility && this.mPolicyVisibilityAfterAnim) {
            return false;
        }
        if (doAnimation) {
            if (!this.mService.okToDisplay()) {
                doAnimation = false;
            } else if (this.mPolicyVisibility && this.mWinAnimator.mAnimation == null) {
                doAnimation = false;
            }
        }
        this.mPolicyVisibility = true;
        this.mPolicyVisibilityAfterAnim = true;
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(1, true);
        }
        if (requestAnim) {
            this.mService.scheduleAnimationLocked();
        }
        return true;
    }

    public boolean hideLw(boolean doAnimation) {
        return hideLw(doAnimation, true);
    }

    boolean hideLw(boolean doAnimation, boolean requestAnim) {
        boolean current;
        if (doAnimation && !this.mService.okToDisplay()) {
            doAnimation = false;
        }
        if (doAnimation) {
            current = this.mPolicyVisibilityAfterAnim;
        } else {
            current = this.mPolicyVisibility;
        }
        if (!current) {
            return false;
        }
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (this.mWinAnimator.mAnimation == null) {
                doAnimation = false;
            }
        }
        if (doAnimation) {
            this.mPolicyVisibilityAfterAnim = false;
        } else {
            this.mPolicyVisibilityAfterAnim = false;
            this.mPolicyVisibility = false;
            this.mService.enableScreenIfNeededLocked();
            if (this.mService.mCurrentFocus == this) {
                this.mService.mFocusMayChange = true;
            }
        }
        if (requestAnim) {
            this.mService.scheduleAnimationLocked();
        }
        return true;
    }

    public void setAppOpVisibilityLw(boolean state) {
        if (this.mAppOpVisibility != state) {
            this.mAppOpVisibility = state;
            if (state) {
                showLw(true, true);
            } else {
                hideLw(true, true);
            }
        }
    }

    public void pokeDrawLockLw(long timeout) {
        if (isVisibleOrAdding()) {
            if (this.mDrawLock == null) {
                this.mDrawLock = this.mService.mPowerManager.newWakeLock(128, "Window:" + getWindowTag());
                this.mDrawLock.setReferenceCounted(false);
                this.mDrawLock.setWorkSource(new WorkSource(this.mOwnerUid, this.mAttrs.packageName));
            }
            this.mDrawLock.acquire(timeout);
        }
    }

    public boolean isAlive() {
        return this.mClient.asBinder().isBinderAlive();
    }

    boolean isClosing() {
        return !this.mAnimatingExit ? this.mService.mClosingApps.contains(this.mAppToken) : true;
    }

    boolean isAnimatingWithSavedSurface() {
        return this.mAnimatingWithSavedSurface;
    }

    boolean isAnimatingInvisibleWithSavedSurface() {
        if (this.mAnimatingWithSavedSurface) {
            return this.mViewVisibility == 0 ? this.mWindowRemovalAllowed : true;
        } else {
            return false;
        }
    }

    public void setVisibleBeforeClientHidden() {
        this.mWasVisibleBeforeClientHidden = (this.mViewVisibility != 0 ? this.mAnimatingWithSavedSurface : 1) | this.mWasVisibleBeforeClientHidden;
    }

    public void clearVisibleBeforeClientHidden() {
        this.mWasVisibleBeforeClientHidden = false;
    }

    public boolean wasVisibleBeforeClientHidden() {
        return this.mWasVisibleBeforeClientHidden;
    }

    private boolean shouldSaveSurface() {
        if (this.mWinAnimator.mSurfaceController == null) {
            return false;
        }
        if ((this.mAttrs.hwFlags & 2) == 2) {
            Slog.v(TAG, "dont Save surface because FLAG_DESTORY_SURFACE");
            return false;
        } else if (!this.mWasVisibleBeforeClientHidden || (this.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0 || SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false) || SystemProperties.getBoolean("ro.config.hw_disable_surface", true) || ActivityManager.isLowRamDeviceStatic()) {
            return false;
        } else {
            Task task = getTask();
            if (task == null || task.inHomeStack()) {
                return false;
            }
            AppWindowToken taskTop = task.getTopVisibleAppToken();
            if ((taskTop == null || taskTop == this.mAppToken) && !this.mResizedWhileGone) {
                return this.mAppToken.shouldSaveSurface();
            }
            return false;
        }
    }

    void destroyOrSaveSurface() {
        this.mSurfaceSaved = shouldSaveSurface();
        if (this.mSurfaceSaved) {
            this.mSession.setTransparentRegion(this.mClient, sEmptyRegion);
            this.mWinAnimator.hide("saved surface");
            this.mWinAnimator.mDrawState = 0;
            setHasSurface(false);
            if (this.mWinAnimator.mSurfaceController != null) {
                this.mWinAnimator.mSurfaceController.disconnectInTransaction();
            }
            this.mAnimatingWithSavedSurface = false;
        } else {
            this.mWinAnimator.destroySurfaceLocked();
        }
        this.mAnimatingExit = false;
    }

    void destroySavedSurface() {
        if (this.mSurfaceSaved) {
            this.mWinAnimator.destroySurfaceLocked();
        }
        this.mWasVisibleBeforeClientHidden = false;
    }

    void restoreSavedSurface() {
        if (this.mSurfaceSaved) {
            this.mSurfaceSaved = false;
            if (this.mWinAnimator.mSurfaceController != null) {
                setHasSurface(true);
                this.mWinAnimator.mDrawState = 3;
                this.mAnimatingWithSavedSurface = true;
            } else {
                Slog.wtf(TAG, "Failed to restore saved surface: surface gone! " + this);
            }
        }
    }

    boolean canRestoreSurface() {
        return this.mWasVisibleBeforeClientHidden ? this.mSurfaceSaved : false;
    }

    boolean hasSavedSurface() {
        return this.mSurfaceSaved;
    }

    void clearHasSavedSurface() {
        this.mSurfaceSaved = false;
        this.mAnimatingWithSavedSurface = false;
        if (this.mWasVisibleBeforeClientHidden) {
            this.mAppToken.destroySavedSurfaces();
        }
    }

    boolean clearAnimatingWithSavedSurface() {
        if (!this.mAnimatingWithSavedSurface) {
            return false;
        }
        this.mAnimatingWithSavedSurface = false;
        return true;
    }

    public boolean isDefaultDisplay() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return false;
        }
        return displayContent.isDefaultDisplay;
    }

    public boolean isDimming() {
        DimLayerUser dimLayerUser = getDimLayerUser();
        if (dimLayerUser == null || this.mDisplayContent == null) {
            return false;
        }
        return this.mDisplayContent.mDimLayerController.isDimming(dimLayerUser, this.mWinAnimator);
    }

    public void setShowToOwnerOnlyLocked(boolean showToOwnerOnly) {
        this.mShowToOwnerOnly = showToOwnerOnly;
    }

    boolean isHiddenFromUserLocked() {
        boolean z = false;
        WindowState win = this;
        while (win.isChildWindow()) {
            win = win.mAttachedWindow;
        }
        if (win.mAttrs.type < IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && win.mAppToken != null && win.mAppToken.showForAllUsers && win.mFrame.left <= win.mDisplayFrame.left && win.mFrame.top <= win.mDisplayFrame.top && win.mFrame.right >= win.mStableFrame.right && win.mFrame.bottom >= win.mStableFrame.bottom) {
            return false;
        }
        if (win.mShowToOwnerOnly && !this.mService.isCurrentProfileLocked(UserHandle.getUserId(win.mOwnerUid))) {
            z = true;
        }
        return z;
    }

    private static void applyInsets(Region outRegion, Rect frame, Rect inset) {
        outRegion.set(frame.left + inset.left, frame.top + inset.top, frame.right - inset.right, frame.bottom - inset.bottom);
    }

    void getTouchableRegion(Region outRegion) {
        Rect frame = this.mFrame;
        switch (this.mTouchableInsets) {
            case 1:
                applyInsets(outRegion, frame, this.mGivenContentInsets);
                break;
            case 2:
                applyInsets(outRegion, frame, this.mGivenVisibleInsets);
                break;
            case 3:
                outRegion.set(this.mGivenTouchableRegion);
                outRegion.translate(frame.left, frame.top);
                break;
            default:
                outRegion.set(frame);
                break;
        }
        cropRegionToStackBoundsIfNeeded(outRegion);
    }

    void cropRegionToStackBoundsIfNeeded(Region region) {
        Task task = getTask();
        if (task != null && task.cropWindowsToStackBounds()) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
                region.op(this.mTmpRect, Op.INTERSECT);
            }
        }
    }

    WindowList getWindowList() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return null;
        }
        return displayContent.getWindowList();
    }

    public void reportFocusChangedSerialized(boolean focused, boolean inTouchMode) {
        try {
            this.mClient.windowFocusChanged(focused, inTouchMode);
        } catch (RemoteException e) {
        }
        if (this.mFocusCallbacks != null) {
            int N = this.mFocusCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IWindowFocusObserver obs = (IWindowFocusObserver) this.mFocusCallbacks.getBroadcastItem(i);
                if (focused) {
                    try {
                        obs.focusGained(this.mWindowId.asBinder());
                    } catch (RemoteException e2) {
                    }
                } else {
                    obs.focusLost(this.mWindowId.asBinder());
                }
            }
            this.mFocusCallbacks.finishBroadcast();
        }
    }

    private Configuration updateConfiguration() {
        boolean configChanged = isConfigChanged();
        getMergedConfig(this.mMergedConfiguration);
        this.mConfigHasChanged = false;
        return this.mMergedConfiguration;
    }

    private void getMergedConfig(Configuration outConfig) {
        if (this.mAppToken == null || this.mAppToken.mFrozenMergedConfig.size() <= 0) {
            Configuration overrideConfig;
            Task task = getTask();
            if (task != null) {
                overrideConfig = task.mOverrideConfig;
            } else {
                overrideConfig = Configuration.EMPTY;
            }
            outConfig.setTo(this.mService.mCurConfiguration);
            if (overrideConfig != Configuration.EMPTY) {
                outConfig.updateFrom(overrideConfig);
            }
            return;
        }
        outConfig.setTo((Configuration) this.mAppToken.mFrozenMergedConfig.peek());
    }

    void reportResized() {
        Trace.traceBegin(32, "wm.reportResized_" + getWindowTag());
        try {
            Rect frame;
            final Configuration updateConfiguration = isConfigChanged() ? updateConfiguration() : null;
            if (this.mEnforceSizeCompat) {
                frame = this.mCompatFrame;
            } else {
                frame = this.mFrame;
            }
            final Rect overscanInsets = this.mLastOverscanInsets;
            final Rect contentInsets = this.mLastContentInsets;
            final Rect visibleInsets = this.mLastVisibleInsets;
            final Rect stableInsets = this.mLastStableInsets;
            final Rect outsets = this.mLastOutsets;
            final boolean reportDraw = this.mWinAnimator.mDrawState == 1;
            if (this.mAttrs.type == 3 || !(this.mClient instanceof IWindow.Stub)) {
                dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, updateConfiguration);
            } else {
                this.mService.mH.post(new Runnable() {
                    public void run() {
                        try {
                            WindowState.this.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, updateConfiguration);
                        } catch (RemoteException e) {
                        }
                    }
                });
            }
            if (this.mService.mAccessibilityController != null && getDisplayId() == 0) {
                this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            this.mOverscanInsetsChanged = false;
            this.mContentInsetsChanged = false;
            this.mVisibleInsetsChanged = false;
            this.mStableInsetsChanged = false;
            this.mOutsetsChanged = false;
            this.mResizedWhileNotDragResizingReported = true;
            this.mWinAnimator.mSurfaceResized = false;
        } catch (RemoteException e) {
            this.mOrientationChanging = false;
            this.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
            this.mOverscanInsetsChanged = false;
            this.mContentInsetsChanged = false;
            this.mVisibleInsetsChanged = false;
            this.mStableInsetsChanged = false;
            this.mWinAnimator.mSurfaceResized = false;
            Slog.w(TAG, "Failed to report 'resized' to the client of " + this + ", removing this window.");
            this.mService.mPendingRemove.add(this);
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
        Trace.traceEnd(32);
    }

    Rect getBackdropFrame(Rect frame) {
        boolean isDragResizeChanged = !isDragResizing() ? isDragResizeChanged() : true;
        if (StackId.useWindowFrameForBackdrop(getStackId()) || !isDragResizeChanged) {
            return frame;
        }
        DisplayInfo displayInfo = getDisplayInfo();
        this.mTmpRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        return this.mTmpRect;
    }

    public int getStackId() {
        TaskStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mStackId;
    }

    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig) throws RemoteException {
        this.mClient.resized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, newConfig, getBackdropFrame(frame), !isDragResizeChanged() ? this.mResizedWhileNotDragResizing : true, this.mPolicy.isNavBarForcedShownLw(this));
        this.mDragResizingChangeReported = true;
    }

    public void registerFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            if (this.mFocusCallbacks == null) {
                this.mFocusCallbacks = new RemoteCallbackList();
            }
            this.mFocusCallbacks.register(observer);
        }
    }

    public void unregisterFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            if (this.mFocusCallbacks != null) {
                this.mFocusCallbacks.unregister(observer);
            }
        }
    }

    public boolean isFocused() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            z = this.mService.mCurrentFocus == this;
        }
        return z;
    }

    boolean inFreeformWorkspace() {
        Task task = getTask();
        return task != null ? task.inFreeformWorkspace() : false;
    }

    public boolean isInMultiWindowMode() {
        Task task = getTask();
        if (task == null || task.isFullscreen()) {
            return false;
        }
        return true;
    }

    boolean isDragResizeChanged() {
        return this.mDragResizing != computeDragResizing();
    }

    boolean isDragResizingChangeReported() {
        return this.mDragResizingChangeReported;
    }

    void resetDragResizingChangeReported() {
        this.mDragResizingChangeReported = false;
    }

    void setResizedWhileNotDragResizing(boolean resizedWhileNotDragResizing) {
        this.mResizedWhileNotDragResizing = resizedWhileNotDragResizing;
        this.mResizedWhileNotDragResizingReported = !resizedWhileNotDragResizing;
    }

    boolean isResizedWhileNotDragResizing() {
        return this.mResizedWhileNotDragResizing;
    }

    boolean isResizedWhileNotDragResizingReported() {
        return this.mResizedWhileNotDragResizingReported;
    }

    int getResizeMode() {
        return this.mResizeMode;
    }

    boolean computeDragResizing() {
        boolean z = false;
        Task task = getTask();
        if (task == null || this.mAttrs.width != -1 || this.mAttrs.height != -1) {
            return false;
        }
        if (task.isDragResizing()) {
            return true;
        }
        if (!((!this.mDisplayContent.mDividerControllerLocked.isResizing() && (this.mAppToken == null || this.mAppToken.mFrozenBounds.isEmpty())) || task.inFreeformWorkspace() || isGoneForLayoutLw())) {
            z = true;
        }
        return z;
    }

    void setDragResizing() {
        boolean resizing = computeDragResizing();
        if (resizing != this.mDragResizing) {
            this.mDragResizing = resizing;
            Task task = getTask();
            if (task == null || !task.isDragResizing()) {
                int i;
                if (this.mDragResizing && this.mDisplayContent.mDividerControllerLocked.isResizing()) {
                    i = 1;
                } else {
                    i = 0;
                }
                this.mResizeMode = i;
            } else {
                this.mResizeMode = task.getDragResizeMode();
            }
        }
    }

    boolean isDragResizing() {
        return this.mDragResizing;
    }

    boolean isDockedResizing() {
        return this.mDragResizing && getResizeMode() == 1;
    }

    void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        int i = 0;
        TaskStack stack = getStack();
        pw.print(prefix);
        pw.print("mDisplayId=");
        pw.print(getDisplayId());
        if (stack != null) {
            pw.print(" stackId=");
            pw.print(stack.mStackId);
        }
        if (this.mNotOnAppsDisplay) {
            pw.print(" mNotOnAppsDisplay=");
            pw.print(this.mNotOnAppsDisplay);
        }
        pw.print(" mSession=");
        pw.print(this.mSession);
        pw.print(" mClient=");
        pw.println(this.mClient.asBinder());
        pw.print(prefix);
        pw.print("mOwnerUid=");
        pw.print(this.mOwnerUid);
        pw.print(" mShowToOwnerOnly=");
        pw.print(this.mShowToOwnerOnly);
        pw.print(" package=");
        pw.print(this.mAttrs.packageName);
        pw.print(" appop=");
        pw.println(AppOpsManager.opToName(this.mAppOp));
        pw.print(prefix);
        pw.print("mAttrs=");
        pw.println(this.mAttrs);
        pw.print(prefix);
        pw.print("Requested w=");
        pw.print(this.mRequestedWidth);
        pw.print(" h=");
        pw.print(this.mRequestedHeight);
        pw.print(" mLayoutSeq=");
        pw.println(this.mLayoutSeq);
        if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
            pw.print(prefix);
            pw.print("LastRequested w=");
            pw.print(this.mLastRequestedWidth);
            pw.print(" h=");
            pw.println(this.mLastRequestedHeight);
        }
        if (isChildWindow() || this.mLayoutAttached) {
            pw.print(prefix);
            pw.print("mAttachedWindow=");
            pw.print(this.mAttachedWindow);
            pw.print(" mLayoutAttached=");
            pw.println(this.mLayoutAttached);
        }
        if (this.mIsImWindow || this.mIsWallpaper || this.mIsFloatingLayer) {
            pw.print(prefix);
            pw.print("mIsImWindow=");
            pw.print(this.mIsImWindow);
            pw.print(" mIsWallpaper=");
            pw.print(this.mIsWallpaper);
            pw.print(" mIsFloatingLayer=");
            pw.print(this.mIsFloatingLayer);
            pw.print(" mWallpaperVisible=");
            pw.println(this.mWallpaperVisible);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mBaseLayer=");
            pw.print(this.mBaseLayer);
            pw.print(" mSubLayer=");
            pw.print(this.mSubLayer);
            pw.print(" mAnimLayer=");
            pw.print(this.mLayer);
            pw.print("+");
            if (this.mTargetAppToken != null) {
                i = this.mTargetAppToken.mAppAnimator.animLayerAdjustment;
            } else if (this.mAppToken != null) {
                i = this.mAppToken.mAppAnimator.animLayerAdjustment;
            }
            pw.print(i);
            pw.print("=");
            pw.print(this.mWinAnimator.mAnimLayer);
            pw.print(" mLastLayer=");
            pw.println(this.mWinAnimator.mLastLayer);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mToken=");
            pw.println(this.mToken);
            pw.print(prefix);
            pw.print("mRootToken=");
            pw.println(this.mRootToken);
            if (this.mAppToken != null) {
                pw.print(prefix);
                pw.print("mAppToken=");
                pw.println(this.mAppToken);
                pw.print(prefix);
                pw.print(" isAnimatingWithSavedSurface()=");
                pw.print(isAnimatingWithSavedSurface());
                pw.print(" mAppDied=");
                pw.println(this.mAppDied);
            }
            if (this.mTargetAppToken != null) {
                pw.print(prefix);
                pw.print("mTargetAppToken=");
                pw.println(this.mTargetAppToken);
            }
            pw.print(prefix);
            pw.print("mViewVisibility=0x");
            pw.print(Integer.toHexString(this.mViewVisibility));
            pw.print(" mHaveFrame=");
            pw.print(this.mHaveFrame);
            pw.print(" mObscured=");
            pw.println(this.mObscured);
            pw.print(prefix);
            pw.print("mSeq=");
            pw.print(this.mSeq);
            pw.print(" mSystemUiVisibility=0x");
            pw.println(Integer.toHexString(this.mSystemUiVisibility));
        }
        if (!(this.mPolicyVisibility && this.mPolicyVisibilityAfterAnim && this.mAppOpVisibility && !this.mAttachedHidden)) {
            pw.print(prefix);
            pw.print("mPolicyVisibility=");
            pw.print(this.mPolicyVisibility);
            pw.print(" mPolicyVisibilityAfterAnim=");
            pw.print(this.mPolicyVisibilityAfterAnim);
            pw.print(" mAppOpVisibility=");
            pw.print(this.mAppOpVisibility);
            pw.print(" mAttachedHidden=");
            pw.println(this.mAttachedHidden);
        }
        if (!this.mRelayoutCalled || this.mLayoutNeeded) {
            pw.print(prefix);
            pw.print("mRelayoutCalled=");
            pw.print(this.mRelayoutCalled);
            pw.print(" mLayoutNeeded=");
            pw.println(this.mLayoutNeeded);
        }
        if (!(this.mXOffset == 0 && this.mYOffset == 0)) {
            pw.print(prefix);
            pw.print("Offsets x=");
            pw.print(this.mXOffset);
            pw.print(" y=");
            pw.println(this.mYOffset);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mGivenContentInsets=");
            this.mGivenContentInsets.printShortString(pw);
            pw.print(" mGivenVisibleInsets=");
            this.mGivenVisibleInsets.printShortString(pw);
            pw.println();
            if (this.mTouchableInsets != 0 || this.mGivenInsetsPending) {
                pw.print(prefix);
                pw.print("mTouchableInsets=");
                pw.print(this.mTouchableInsets);
                pw.print(" mGivenInsetsPending=");
                pw.println(this.mGivenInsetsPending);
                Region region = new Region();
                getTouchableRegion(region);
                pw.print(prefix);
                pw.print("touchable region=");
                pw.println(region);
            }
            pw.print(prefix);
            pw.print("mMergedConfiguration=");
            pw.println(this.mMergedConfiguration);
        }
        pw.print(prefix);
        pw.print("mHasSurface=");
        pw.print(this.mHasSurface);
        pw.print(" mShownPosition=");
        this.mShownPosition.printShortString(pw);
        pw.print(" isReadyForDisplay()=");
        pw.print(isReadyForDisplay());
        pw.print(" hasSavedSurface()=");
        pw.print(hasSavedSurface());
        pw.print(" mWindowRemovalAllowed=");
        pw.println(this.mWindowRemovalAllowed);
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mFrame=");
            this.mFrame.printShortString(pw);
            pw.print(" last=");
            this.mLastFrame.printShortString(pw);
            pw.println();
        }
        if (this.mEnforceSizeCompat) {
            pw.print(prefix);
            pw.print("mCompatFrame=");
            this.mCompatFrame.printShortString(pw);
            pw.println();
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("Frames: containing=");
            this.mContainingFrame.printShortString(pw);
            pw.print(" parent=");
            this.mParentFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    display=");
            this.mDisplayFrame.printShortString(pw);
            pw.print(" overscan=");
            this.mOverscanFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    content=");
            this.mContentFrame.printShortString(pw);
            pw.print(" visible=");
            this.mVisibleFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    decor=");
            this.mDecorFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    outset=");
            this.mOutsetFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("Cur insets: overscan=");
            this.mOverscanInsets.printShortString(pw);
            pw.print(" content=");
            this.mContentInsets.printShortString(pw);
            pw.print(" visible=");
            this.mVisibleInsets.printShortString(pw);
            pw.print(" stable=");
            this.mStableInsets.printShortString(pw);
            pw.print(" surface=");
            this.mAttrs.surfaceInsets.printShortString(pw);
            pw.print(" outsets=");
            this.mOutsets.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("Lst insets: overscan=");
            this.mLastOverscanInsets.printShortString(pw);
            pw.print(" content=");
            this.mLastContentInsets.printShortString(pw);
            pw.print(" visible=");
            this.mLastVisibleInsets.printShortString(pw);
            pw.print(" stable=");
            this.mLastStableInsets.printShortString(pw);
            pw.print(" physical=");
            this.mLastOutsets.printShortString(pw);
            pw.print(" outset=");
            this.mLastOutsets.printShortString(pw);
            pw.println();
        }
        pw.print(prefix);
        pw.print(this.mWinAnimator);
        pw.println(":");
        this.mWinAnimator.dump(pw, prefix + "  ", dumpAll);
        if (this.mAnimatingExit || this.mRemoveOnExit || this.mDestroying || this.mRemoved) {
            pw.print(prefix);
            pw.print("mAnimatingExit=");
            pw.print(this.mAnimatingExit);
            pw.print(" mRemoveOnExit=");
            pw.print(this.mRemoveOnExit);
            pw.print(" mDestroying=");
            pw.print(this.mDestroying);
            pw.print(" mRemoved=");
            pw.println(this.mRemoved);
        }
        if (this.mOrientationChanging || this.mAppFreezing || this.mTurnOnScreen) {
            pw.print(prefix);
            pw.print("mOrientationChanging=");
            pw.print(this.mOrientationChanging);
            pw.print(" mAppFreezing=");
            pw.print(this.mAppFreezing);
            pw.print(" mTurnOnScreen=");
            pw.println(this.mTurnOnScreen);
        }
        if (this.mLastFreezeDuration != 0) {
            pw.print(prefix);
            pw.print("mLastFreezeDuration=");
            TimeUtils.formatDuration((long) this.mLastFreezeDuration, pw);
            pw.println();
        }
        if (!(this.mHScale == 1.0f && this.mVScale == 1.0f)) {
            pw.print(prefix);
            pw.print("mHScale=");
            pw.print(this.mHScale);
            pw.print(" mVScale=");
            pw.println(this.mVScale);
        }
        if (!(this.mWallpaperX == -1.0f && this.mWallpaperY == -1.0f)) {
            pw.print(prefix);
            pw.print("mWallpaperX=");
            pw.print(this.mWallpaperX);
            pw.print(" mWallpaperY=");
            pw.println(this.mWallpaperY);
        }
        if (!(this.mWallpaperXStep == -1.0f && this.mWallpaperYStep == -1.0f)) {
            pw.print(prefix);
            pw.print("mWallpaperXStep=");
            pw.print(this.mWallpaperXStep);
            pw.print(" mWallpaperYStep=");
            pw.println(this.mWallpaperYStep);
        }
        if (!(this.mWallpaperDisplayOffsetX == Integer.MIN_VALUE && this.mWallpaperDisplayOffsetY == Integer.MIN_VALUE)) {
            pw.print(prefix);
            pw.print("mWallpaperDisplayOffsetX=");
            pw.print(this.mWallpaperDisplayOffsetX);
            pw.print(" mWallpaperDisplayOffsetY=");
            pw.println(this.mWallpaperDisplayOffsetY);
        }
        if (this.mDrawLock != null) {
            pw.print(prefix);
            pw.println("mDrawLock=" + this.mDrawLock);
        }
        if (isDragResizing()) {
            pw.print(prefix);
            pw.println("isDragResizing=" + isDragResizing());
        }
        if (computeDragResizing()) {
            pw.print(prefix);
            pw.println("computeDragResizing=" + computeDragResizing());
        }
    }

    String makeInputChannelName() {
        return Integer.toHexString(System.identityHashCode(this)) + " " + getWindowTag();
    }

    CharSequence getWindowTag() {
        CharSequence tag = this.mAttrs.getTitle();
        if (tag == null || tag.length() <= 0) {
            return this.mAttrs.packageName;
        }
        return tag;
    }

    public String toString() {
        CharSequence title = getWindowTag();
        if (this.mStringNameCache != null && this.mLastTitle == title) {
            if (this.mWasExiting != this.mAnimatingExit) {
            }
            return this.mStringNameCache;
        }
        this.mLastTitle = title;
        this.mWasExiting = this.mAnimatingExit;
        this.mStringNameCache = "Window{" + Integer.toHexString(System.identityHashCode(this)) + " u" + UserHandle.getUserId(this.mSession.mUid) + " " + this.mLastTitle + (this.mAnimatingExit ? " EXITING}" : "}");
        return this.mStringNameCache;
    }

    void transformClipRectFromScreenToSurfaceSpace(Rect clipRect) {
        if (this.mHScale >= 0.0f) {
            clipRect.left = (int) (((float) clipRect.left) / this.mHScale);
            clipRect.right = (int) Math.ceil((double) (((float) clipRect.right) / this.mHScale));
        }
        if (this.mVScale >= 0.0f) {
            clipRect.top = (int) (((float) clipRect.top) / this.mVScale);
            clipRect.bottom = (int) Math.ceil((double) (((float) clipRect.bottom) / this.mVScale));
        }
    }

    void applyGravityAndUpdateFrame(Rect containingFrame, Rect displayFrame) {
        int w;
        int h;
        float x;
        float y;
        int pw = containingFrame.width();
        int ph = containingFrame.height();
        Task task = getTask();
        boolean nonFullscreenTask = isInMultiWindowMode();
        boolean fitToDisplay = (task == null || !nonFullscreenTask) ? true : isChildWindow() && !((this.mAttrs.flags & 512) != 0);
        if ((this.mAttrs.flags & DumpState.DUMP_KEYSETS) != 0) {
            if (this.mAttrs.width < 0) {
                w = pw;
            } else if (this.mEnforceSizeCompat) {
                w = (int) ((((float) this.mAttrs.width) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            } else {
                w = this.mAttrs.width;
            }
            if (this.mAttrs.height < 0) {
                h = ph;
            } else if (this.mEnforceSizeCompat) {
                h = (int) ((((float) this.mAttrs.height) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            } else {
                h = this.mAttrs.height;
            }
        } else {
            if (this.mAttrs.width == -1) {
                w = pw;
            } else if (!this.mEnforceSizeCompat || isRogEnable()) {
                w = this.mRequestedWidth;
            } else {
                w = (int) ((((float) this.mRequestedWidth) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            }
            if (this.mAttrs.height == -1) {
                h = ph;
            } else if (!this.mEnforceSizeCompat || isRogEnable()) {
                h = this.mRequestedHeight;
            } else {
                h = (int) ((((float) this.mRequestedHeight) * this.mGlobalScale) + TaskPositioner.RESIZING_HINT_ALPHA);
            }
        }
        if (!this.mEnforceSizeCompat || isRogEnable()) {
            x = (float) this.mAttrs.x;
            y = (float) this.mAttrs.y;
        } else {
            x = ((float) this.mAttrs.x) * this.mGlobalScale;
            y = ((float) this.mAttrs.y) * this.mGlobalScale;
        }
        if (nonFullscreenTask && !layoutInParentFrame()) {
            w = Math.min(w, pw);
            h = Math.min(h, ph);
        }
        Gravity.apply(this.mAttrs.gravity, w, h, containingFrame, (int) ((this.mAttrs.horizontalMargin * ((float) pw)) + x), (int) ((this.mAttrs.verticalMargin * ((float) ph)) + y), this.mFrame);
        if (fitToDisplay) {
            Gravity.applyDisplay(this.mAttrs.gravity, displayFrame, this.mFrame);
        }
        this.mCompatFrame.set(this.mFrame);
        if (this.mEnforceSizeCompat) {
            this.mCompatFrame.scale(this.mInvGlobalScale);
        }
    }

    boolean isChildWindow() {
        return this.mAttachedWindow != null;
    }

    boolean layoutInParentFrame() {
        return isChildWindow() && (this.mAttrs.privateFlags & DumpState.DUMP_INSTALLS) != 0;
    }

    void setReplacing(boolean animate) {
        if ((this.mAttrs.privateFlags & DumpState.DUMP_VERSION) == 0 && this.mAttrs.type != 3) {
            this.mWillReplaceWindow = true;
            this.mReplacingWindow = null;
            this.mAnimateReplacingWindow = animate;
        }
    }

    void resetReplacing() {
        this.mWillReplaceWindow = false;
        this.mReplacingWindow = null;
        this.mAnimateReplacingWindow = false;
    }

    void requestUpdateWallpaperIfNeeded() {
        if (this.mDisplayContent != null && (this.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
            DisplayContent displayContent = this.mDisplayContent;
            displayContent.pendingLayoutChanges |= 4;
            this.mDisplayContent.layoutNeeded = true;
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
    }

    float translateToWindowX(float x) {
        float winX = x - ((float) this.mFrame.left);
        if (this.mEnforceSizeCompat) {
            return winX * this.mGlobalScale;
        }
        return winX;
    }

    float translateToWindowY(float y) {
        float winY = y - ((float) this.mFrame.top);
        if (this.mEnforceSizeCompat) {
            return winY * this.mGlobalScale;
        }
        return winY;
    }

    void transferDimToReplacement() {
        boolean z = false;
        DimLayerUser dimLayerUser = getDimLayerUser();
        if (dimLayerUser != null && this.mDisplayContent != null) {
            DimLayerController dimLayerController = this.mDisplayContent.mDimLayerController;
            WindowStateAnimator windowStateAnimator = this.mReplacingWindow.mWinAnimator;
            if ((this.mAttrs.flags & 2) != 0) {
                z = true;
            }
            dimLayerController.applyDim(dimLayerUser, windowStateAnimator, z);
        }
    }

    boolean shouldBeReplacedWithChildren() {
        return isChildWindow() || this.mAttrs.type == 2;
    }

    public boolean canCarryColors() {
        return this.mCanCarryColors;
    }

    public void setCanCarryColors(boolean carry) {
        this.mCanCarryColors = carry;
    }

    private AppRogInfo getAppRogInfo() {
        IRogManager rogManager = (IRogManager) LocalServices.getService(IRogManager.class);
        if (rogManager != null) {
            return rogManager.getSpecifiedAppRogInfo(this.mAttrs.packageName);
        }
        return null;
    }

    boolean isRogEnable() {
        return (this.mAttrs.privateFlags & DumpState.DUMP_DEXOPT) != 0;
    }

    private boolean isStatusBarWindow() {
        if (this.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || this.mAttrs.type == 2014 || this.mAttrs.type == 2019 || this.mAttrs.type == 2024) {
            return true;
        }
        return false;
    }

    int getLowResolutionMode() {
        return (!isStatusBarWindow() && ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getPackageScreenCompatMode(getOwningPackage()) == 0) ? 0 : 2;
    }

    float getRogScale() {
        AppRogInfo rogInfo = getAppRogInfo();
        return rogInfo != null ? rogInfo.mRogScale : 1.0f;
    }
}
