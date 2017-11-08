package com.android.server.wm;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Point;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import android.view.Display;
import android.view.DragEvent;
import android.view.InputChannel;
import android.view.SurfaceControl;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import java.util.ArrayList;

class DragState {
    private static final long ANIMATION_DURATION_MS = 500;
    private static final int DRAG_FLAGS_URI_ACCESS = 3;
    private static final int DRAG_FLAGS_URI_PERMISSIONS = 195;
    private Animation mAnimation;
    InputChannel mClientChannel;
    boolean mCrossProfileCopyAllowed;
    private final Interpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    float mCurrentX;
    float mCurrentY;
    ClipData mData;
    ClipDescription mDataDescription;
    DisplayContent mDisplayContent;
    InputApplicationHandle mDragApplicationHandle;
    boolean mDragInProgress;
    boolean mDragResult;
    InputWindowHandle mDragWindowHandle;
    int mFlags;
    DragInputEventReceiver mInputEventReceiver;
    IBinder mLocalWin;
    ArrayList<WindowState> mNotifiedWindows;
    float mOriginalAlpha;
    float mOriginalX;
    float mOriginalY;
    int mPid;
    InputChannel mServerChannel;
    final WindowManagerService mService;
    int mSourceUserId;
    SurfaceControl mSurfaceControl;
    WindowState mTargetWindow;
    float mThumbOffsetX;
    float mThumbOffsetY;
    IBinder mToken;
    int mTouchSource;
    final Transformation mTransformation = new Transformation();
    int mUid;

    private void sendDragStartedLw(com.android.server.wm.WindowState r12, float r13, float r14, android.content.ClipDescription r15) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        r4 = 0;
        r0 = r11.mDragInProgress;
        if (r0 == 0) goto L_0x002e;
    L_0x0005:
        r0 = r11.isValidDropTarget(r12);
        if (r0 == 0) goto L_0x002e;
    L_0x000b:
        r1 = 1;
        r8 = 0;
        r0 = r12;
        r2 = r13;
        r3 = r14;
        r5 = r15;
        r6 = r4;
        r7 = r4;
        r10 = obtainDragEvent(r0, r1, r2, r3, r4, r5, r6, r7, r8);
        r0 = r12.mClient;	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r0.dispatchDragEvent(r10);	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r0 = r11.mNotifiedWindows;	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r0.add(r12);	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r0 = android.os.Process.myPid();
        r1 = r12.mSession;
        r1 = r1.mPid;
        if (r0 == r1) goto L_0x002e;
    L_0x002b:
        r10.recycle();
    L_0x002e:
        return;
    L_0x002f:
        r9 = move-exception;
        r0 = "WindowManager";	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r1 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r1.<init>();	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r2 = "Unable to drag-start window ";	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r1 = r1.append(r2);	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r1 = r1.append(r12);	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r1 = r1.toString();	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        android.util.Slog.w(r0, r1);	 Catch:{ RemoteException -> 0x002f, all -> 0x0058 }
        r0 = android.os.Process.myPid();
        r1 = r12.mSession;
        r1 = r1.mPid;
        if (r0 == r1) goto L_0x002e;
    L_0x0054:
        r10.recycle();
        goto L_0x002e;
    L_0x0058:
        r0 = move-exception;
        r1 = android.os.Process.myPid();
        r2 = r12.mSession;
        r2 = r2.mPid;
        if (r1 == r2) goto L_0x0066;
    L_0x0063:
        r10.recycle();
    L_0x0066:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DragState.sendDragStartedLw(com.android.server.wm.WindowState, float, float, android.content.ClipDescription):void");
    }

    boolean notifyDropLw(float r24, float r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00fa in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r23 = this;
        r0 = r23;
        r3 = r0.mAnimation;
        if (r3 == 0) goto L_0x0008;
    L_0x0006:
        r3 = 0;
        return r3;
    L_0x0008:
        r0 = r24;
        r1 = r23;
        r1.mCurrentX = r0;
        r0 = r25;
        r1 = r23;
        r1.mCurrentY = r0;
        r0 = r23;
        r3 = r0.mDisplayContent;
        r0 = r24;
        r1 = r25;
        r9 = r3.getTouchableWinAtPointLocked(r0, r1);
        r0 = r23;
        r3 = r0.isWindowNotified(r9);
        if (r3 != 0) goto L_0x002f;
    L_0x0028:
        r3 = 0;
        r0 = r23;
        r0.mDragResult = r3;
        r3 = 1;
        return r3;
    L_0x002f:
        r3 = r9.getOwningUid();
        r8 = android.os.UserHandle.getUserId(r3);
        r2 = 0;
        r0 = r23;
        r3 = r0.mFlags;
        r3 = r3 & 256;
        if (r3 == 0) goto L_0x0063;
    L_0x0040:
        r0 = r23;
        r3 = r0.mFlags;
        r3 = r3 & 3;
        if (r3 == 0) goto L_0x0063;
    L_0x0048:
        r2 = new com.android.server.wm.DragAndDropPermissionsHandler;
        r0 = r23;
        r3 = r0.mData;
        r0 = r23;
        r4 = r0.mUid;
        r5 = r9.getOwningPackage();
        r0 = r23;
        r6 = r0.mFlags;
        r6 = r6 & 195;
        r0 = r23;
        r7 = r0.mSourceUserId;
        r2.<init>(r3, r4, r5, r6, r7, r8);
    L_0x0063:
        r0 = r23;
        r3 = r0.mSourceUserId;
        if (r3 == r8) goto L_0x0074;
    L_0x0069:
        r0 = r23;
        r3 = r0.mData;
        r0 = r23;
        r4 = r0.mSourceUserId;
        r3.fixUris(r4);
    L_0x0074:
        r21 = android.os.Process.myPid();
        r3 = r9.mClient;
        r22 = r3.asBinder();
        r0 = r23;
        r15 = r0.mData;
        r10 = 3;
        r13 = 0;
        r14 = 0;
        r17 = 0;
        r11 = r24;
        r12 = r25;
        r16 = r2;
        r19 = obtainDragEvent(r9, r10, r11, r12, r13, r14, r15, r16, r17);
        r3 = r9.mClient;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r19;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3.dispatchDragEvent(r0);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r23;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r0.mService;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r3.mH;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = 21;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r22;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3.removeMessages(r4, r0);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r23;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r0.mService;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r3.mH;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = 21;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r22;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r20 = r3.obtainMessage(r4, r0);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r23;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r0.mService;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r3.mH;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r0 = r20;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3.sendMessageDelayed(r0, r4);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = r9.mSession;
        r3 = r3.mPid;
        r0 = r21;
        if (r0 == r3) goto L_0x00cb;
    L_0x00c8:
        r19.recycle();
    L_0x00cb:
        r0 = r22;
        r1 = r23;
        r1.mToken = r0;
        r3 = 0;
        return r3;
    L_0x00d3:
        r18 = move-exception;
        r3 = "WindowManager";	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4.<init>();	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r5 = "can't send drop notification to win ";	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = r4.append(r5);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = r4.append(r9);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r4 = r4.toString();	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        android.util.Slog.w(r3, r4);	 Catch:{ RemoteException -> 0x00d3, all -> 0x00fb }
        r3 = 1;
        r4 = r9.mSession;
        r4 = r4.mPid;
        r0 = r21;
        if (r0 == r4) goto L_0x00fa;
    L_0x00f7:
        r19.recycle();
    L_0x00fa:
        return r3;
    L_0x00fb:
        r3 = move-exception;
        r4 = r9.mSession;
        r4 = r4.mPid;
        r0 = r21;
        if (r0 == r4) goto L_0x0107;
    L_0x0104:
        r19.recycle();
    L_0x0107:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DragState.notifyDropLw(float, float):boolean");
    }

    DragState(WindowManagerService service, IBinder token, SurfaceControl surface, int flags, IBinder localWin) {
        this.mService = service;
        this.mToken = token;
        this.mSurfaceControl = surface;
        this.mFlags = flags;
        this.mLocalWin = localWin;
        this.mNotifiedWindows = new ArrayList();
    }

    void reset() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.destroy();
        }
        this.mSurfaceControl = null;
        this.mFlags = 0;
        this.mLocalWin = null;
        this.mToken = null;
        this.mData = null;
        this.mThumbOffsetY = 0.0f;
        this.mThumbOffsetX = 0.0f;
        this.mNotifiedWindows = null;
    }

    void register(Display display) {
        if (this.mClientChannel != null) {
            Slog.e("WindowManager", "Duplicate register of drag input channel");
            return;
        }
        this.mDisplayContent = this.mService.getDisplayContentLocked(display.getDisplayId());
        InputChannel[] channels = InputChannel.openInputChannelPair("drag");
        this.mServerChannel = channels[0];
        this.mClientChannel = channels[1];
        this.mService.mInputManager.registerInputChannel(this.mServerChannel, null);
        WindowManagerService windowManagerService = this.mService;
        windowManagerService.getClass();
        this.mInputEventReceiver = new DragInputEventReceiver(this.mClientChannel, this.mService.mH.getLooper());
        this.mDragApplicationHandle = new InputApplicationHandle(null);
        this.mDragApplicationHandle.name = "drag";
        this.mDragApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle = new InputWindowHandle(this.mDragApplicationHandle, null, display.getDisplayId());
        this.mDragWindowHandle.name = "drag";
        this.mDragWindowHandle.inputChannel = this.mServerChannel;
        this.mDragWindowHandle.layer = getDragLayerLw();
        this.mDragWindowHandle.layoutParamsFlags = 0;
        this.mDragWindowHandle.layoutParamsType = 2016;
        this.mDragWindowHandle.dispatchingTimeoutNanos = 5000000000L;
        this.mDragWindowHandle.visible = true;
        this.mDragWindowHandle.canReceiveKeys = false;
        this.mDragWindowHandle.hasFocus = true;
        this.mDragWindowHandle.hasWallpaper = false;
        this.mDragWindowHandle.paused = false;
        this.mDragWindowHandle.ownerPid = Process.myPid();
        this.mDragWindowHandle.ownerUid = Process.myUid();
        this.mDragWindowHandle.inputFeatures = 0;
        this.mDragWindowHandle.scaleFactor = 1.0f;
        this.mDragWindowHandle.touchableRegion.setEmpty();
        this.mDragWindowHandle.frameLeft = 0;
        this.mDragWindowHandle.frameTop = 0;
        Point p = new Point();
        display.getRealSize(p);
        this.mDragWindowHandle.frameRight = p.x;
        this.mDragWindowHandle.frameBottom = p.y;
        this.mService.pauseRotationLocked();
    }

    void unregister() {
        if (this.mClientChannel == null) {
            Slog.e("WindowManager", "Unregister of nonexistent drag input channel");
            return;
        }
        this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
        this.mInputEventReceiver.dispose();
        this.mInputEventReceiver = null;
        this.mClientChannel.dispose();
        this.mServerChannel.dispose();
        this.mClientChannel = null;
        this.mServerChannel = null;
        this.mDragWindowHandle = null;
        this.mDragApplicationHandle = null;
        this.mService.resumeRotationLocked();
    }

    int getDragLayerLw() {
        return (this.mService.mPolicy.windowTypeToLayerLw(2016) * 10000) + 1000;
    }

    void broadcastDragStartedLw(float touchX, float touchY) {
        ClipDescription clipDescription = null;
        this.mCurrentX = touchX;
        this.mOriginalX = touchX;
        this.mCurrentY = touchY;
        this.mOriginalY = touchY;
        if (this.mData != null) {
            clipDescription = this.mData.getDescription();
        }
        this.mDataDescription = clipDescription;
        this.mNotifiedWindows.clear();
        this.mDragInProgress = true;
        this.mSourceUserId = UserHandle.getUserId(this.mUid);
        try {
            boolean z;
            if (((IUserManager) ServiceManager.getService("user")).getUserRestrictions(this.mSourceUserId).getBoolean("no_cross_profile_copy_paste")) {
                z = false;
            } else {
                z = true;
            }
            this.mCrossProfileCopyAllowed = z;
        } catch (RemoteException e) {
            Slog.e("WindowManager", "Remote Exception calling UserManager: " + e);
            this.mCrossProfileCopyAllowed = false;
        }
        WindowList windows = this.mDisplayContent.getWindowList();
        int N = windows.size();
        for (int i = 0; i < N; i++) {
            sendDragStartedLw((WindowState) windows.get(i), touchX, touchY, this.mDataDescription);
        }
    }

    private boolean isValidDropTarget(WindowState targetWin) {
        boolean z = true;
        if (targetWin == null || !targetWin.isPotentialDragTarget()) {
            return false;
        }
        if (((this.mFlags & 256) == 0 || !targetWindowSupportsGlobalDrag(targetWin)) && this.mLocalWin != targetWin.mClient.asBinder()) {
            return false;
        }
        if (!(this.mCrossProfileCopyAllowed || this.mSourceUserId == UserHandle.getUserId(targetWin.getOwningUid()))) {
            z = false;
        }
        return z;
    }

    private boolean targetWindowSupportsGlobalDrag(WindowState targetWin) {
        if (targetWin.mAppToken == null || targetWin.mAppToken.targetSdk >= 24) {
            return true;
        }
        return false;
    }

    void sendDragStartedIfNeededLw(WindowState newWin) {
        if (this.mDragInProgress && !isWindowNotified(newWin)) {
            sendDragStartedLw(newWin, this.mCurrentX, this.mCurrentY, this.mDataDescription);
        }
    }

    private boolean isWindowNotified(WindowState newWin) {
        for (WindowState ws : this.mNotifiedWindows) {
            if (ws == newWin) {
                return true;
            }
        }
        return false;
    }

    private void broadcastDragEndedLw() {
        int myPid = Process.myPid();
        for (WindowState ws : this.mNotifiedWindows) {
            float x = 0.0f;
            float y = 0.0f;
            if (!this.mDragResult && ws.mSession.mPid == this.mPid) {
                x = this.mCurrentX;
                y = this.mCurrentY;
            }
            DragEvent evt = DragEvent.obtain(4, x, y, null, null, null, null, this.mDragResult);
            try {
                ws.mClient.dispatchDragEvent(evt);
            } catch (RemoteException e) {
                Slog.w("WindowManager", "Unable to drag-end window " + ws);
            }
            if (myPid != ws.mSession.mPid) {
                evt.recycle();
            }
        }
        this.mNotifiedWindows.clear();
        this.mDragInProgress = false;
    }

    void endDragLw() {
        if (this.mAnimation == null) {
            if (this.mDragResult) {
                cleanUpDragLw();
                return;
            }
            this.mAnimation = createReturnAnimationLocked();
            this.mService.scheduleAnimationLocked();
        }
    }

    void cancelDragLw() {
        if (this.mAnimation == null) {
            this.mAnimation = createCancelAnimationLocked();
            this.mService.scheduleAnimationLocked();
        }
    }

    private void cleanUpDragLw() {
        broadcastDragEndedLw();
        if (isFromSource(8194)) {
            this.mService.restorePointerIconLocked(this.mDisplayContent, this.mCurrentX, this.mCurrentY);
        }
        unregister();
        reset();
        this.mService.mDragState = null;
        this.mService.mInputMonitor.updateInputWindowsLw(true);
    }

    void notifyMoveLw(float x, float y) {
        if (this.mAnimation == null) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.setPosition(x - this.mThumbOffsetX, y - this.mThumbOffsetY);
                notifyLocationLw(x, y);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    void notifyLocationLw(float x, float y) {
        WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
        if (!(touchedWin == null || isWindowNotified(touchedWin))) {
            touchedWin = null;
        }
        try {
            DragEvent evt;
            int myPid = Process.myPid();
            if (!(touchedWin == this.mTargetWindow || this.mTargetWindow == null)) {
                evt = obtainDragEvent(this.mTargetWindow, 6, 0.0f, 0.0f, null, null, null, null, false);
                this.mTargetWindow.mClient.dispatchDragEvent(evt);
                if (myPid != this.mTargetWindow.mSession.mPid) {
                    evt.recycle();
                }
            }
            if (touchedWin != null) {
                evt = obtainDragEvent(touchedWin, 2, x, y, null, null, null, null, false);
                touchedWin.mClient.dispatchDragEvent(evt);
                if (myPid != touchedWin.mSession.mPid) {
                    evt.recycle();
                }
            }
        } catch (RemoteException e) {
            Slog.w("WindowManager", "can't send drag notification to windows");
        }
        this.mTargetWindow = touchedWin;
    }

    private static DragEvent obtainDragEvent(WindowState win, int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        return DragEvent.obtain(action, win.translateToWindowX(x), win.translateToWindowY(y), localState, description, data, dragAndDropPermissions, result);
    }

    boolean stepAnimationLocked(long currentTimeMs) {
        if (this.mAnimation == null) {
            return false;
        }
        this.mTransformation.clear();
        if (this.mAnimation.getTransformation(currentTimeMs, this.mTransformation)) {
            this.mTransformation.getMatrix().postTranslate(this.mCurrentX - this.mThumbOffsetX, this.mCurrentY - this.mThumbOffsetY);
            float[] tmpFloats = this.mService.mTmpFloats;
            this.mTransformation.getMatrix().getValues(tmpFloats);
            this.mSurfaceControl.setPosition(tmpFloats[2], tmpFloats[5]);
            this.mSurfaceControl.setAlpha(this.mTransformation.getAlpha());
            this.mSurfaceControl.setMatrix(tmpFloats[0], tmpFloats[3], tmpFloats[1], tmpFloats[4]);
            return true;
        }
        cleanUpDragLw();
        return false;
    }

    private Animation createReturnAnimationLocked() {
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(new TranslateAnimation(0.0f, this.mOriginalX - this.mCurrentX, 0.0f, this.mOriginalY - this.mCurrentY));
        set.addAnimation(new AlphaAnimation(this.mOriginalAlpha, this.mOriginalAlpha / 2.0f));
        set.setDuration(500);
        set.setInterpolator(this.mCubicEaseOutInterpolator);
        set.initialize(0, 0, 0, 0);
        set.start();
        return set;
    }

    private Animation createCancelAnimationLocked() {
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, this.mThumbOffsetX, this.mThumbOffsetY));
        set.addAnimation(new AlphaAnimation(this.mOriginalAlpha, 0.0f));
        set.setDuration(500);
        set.setInterpolator(this.mCubicEaseOutInterpolator);
        set.initialize(0, 0, 0, 0);
        set.start();
        return set;
    }

    private boolean isFromSource(int source) {
        return (this.mTouchSource & source) == source;
    }

    void overridePointerIconLw(int touchSource) {
        this.mTouchSource = touchSource;
        if (isFromSource(8194)) {
            InputManager.getInstance().setPointerIconType(1021);
        }
    }
}
