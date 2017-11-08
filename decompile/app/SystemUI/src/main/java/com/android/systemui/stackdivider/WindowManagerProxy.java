package com.android.systemui.stackdivider;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.GuardedBy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WindowManagerProxy {
    private static final WindowManagerProxy sInstance = new WindowManagerProxy();
    private float mDimLayerAlpha;
    private final Runnable mDimLayerRunnable = new Runnable() {
        public void run() {
            try {
                WindowManagerGlobal.getWindowManagerService().setResizeDimLayer(WindowManagerProxy.this.mDimLayerVisible, WindowManagerProxy.this.mDimLayerTargetStack, WindowManagerProxy.this.mDimLayerAlpha);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private int mDimLayerTargetStack;
    private boolean mDimLayerVisible;
    private final Runnable mDismissRunnable = new Runnable() {
        public void run() {
            try {
                ActivityManagerNative.getDefault().moveTasksToFullscreenStack(3, false);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to remove stack: " + e);
            }
        }
    };
    @GuardedBy("mDockedRect")
    private final Rect mDockedRect = new Rect();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Runnable mMaximizeRunnable = new Runnable() {
        public void run() {
            try {
                ActivityManagerNative.getDefault().resizeStack(3, null, true, true, false, -1);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mResizeRunnable = new Runnable() {
        public void run() {
            Rect rect = null;
            synchronized (WindowManagerProxy.this.mDockedRect) {
                WindowManagerProxy.this.mTmpRect1.set(WindowManagerProxy.this.mDockedRect);
                WindowManagerProxy.this.mTmpRect2.set(WindowManagerProxy.this.mTempDockedTaskRect);
                WindowManagerProxy.this.mTmpRect3.set(WindowManagerProxy.this.mTempDockedInsetRect);
                WindowManagerProxy.this.mTmpRect4.set(WindowManagerProxy.this.mTempOtherTaskRect);
                WindowManagerProxy.this.mTmpRect5.set(WindowManagerProxy.this.mTempOtherInsetRect);
            }
            try {
                IActivityManager iActivityManager = ActivityManagerNative.getDefault();
                Rect -get8 = WindowManagerProxy.this.mTmpRect1;
                Rect -get9 = WindowManagerProxy.this.mTmpRect2.isEmpty() ? null : WindowManagerProxy.this.mTmpRect2;
                Rect -get10 = WindowManagerProxy.this.mTmpRect3.isEmpty() ? null : WindowManagerProxy.this.mTmpRect3;
                Rect -get11 = WindowManagerProxy.this.mTmpRect4.isEmpty() ? null : WindowManagerProxy.this.mTmpRect4;
                if (!WindowManagerProxy.this.mTmpRect5.isEmpty()) {
                    rect = WindowManagerProxy.this.mTmpRect5;
                }
                iActivityManager.resizeDockedStack(-get8, -get9, -get10, -get11, rect);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mSetTouchableRegionRunnable = new Runnable() {
        public void run() {
            try {
                synchronized (WindowManagerProxy.this.mDockedRect) {
                    WindowManagerProxy.this.mTmpRect1.set(WindowManagerProxy.this.mTouchableRegion);
                }
                WindowManagerGlobal.getWindowManagerService().setDockedStackDividerTouchRegion(WindowManagerProxy.this.mTmpRect1);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to set touchable region: " + e);
            }
        }
    };
    private final Runnable mSwapRunnable = new Runnable() {
        public void run() {
            try {
                ActivityManagerNative.getDefault().swapDockedAndFullscreenStack();
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Rect mTempDockedInsetRect = new Rect();
    private final Rect mTempDockedTaskRect = new Rect();
    private final Rect mTempOtherInsetRect = new Rect();
    private final Rect mTempOtherTaskRect = new Rect();
    private final Rect mTmpRect1 = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final Rect mTmpRect3 = new Rect();
    private final Rect mTmpRect4 = new Rect();
    private final Rect mTmpRect5 = new Rect();
    @GuardedBy("mDockedRect")
    private final Rect mTouchableRegion = new Rect();

    public boolean isDimLayerVisible() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0072 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r5 = 1;
        r6 = 0;
        r0 = android.os.Parcel.obtain();
        r4 = android.os.Parcel.obtain();
        r7 = "android.view.IWindowManager";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r0.writeInterfaceToken(r7);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r7 = android.view.WindowManagerGlobal.getWindowManagerService();	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r7 = r7.asBinder();	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r8 = 1007; // 0x3ef float:1.411E-42 double:4.975E-321;	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r9 = 0;	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r7.transact(r8, r0, r4, r9);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r4.readException();	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r3 = r4.readInt();	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r7 = "WindowManagerProxy";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r8 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r8.<init>();	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r9 = "isDimLayerVisible = ";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r8 = r8.append(r9);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r8 = r8.append(r3);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r8 = r8.toString();	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        android.util.Log.i(r7, r8);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        if (r3 != r5) goto L_0x004c;
    L_0x0041:
        if (r0 == 0) goto L_0x0046;
    L_0x0043:
        r0.recycle();
    L_0x0046:
        if (r4 == 0) goto L_0x004b;
    L_0x0048:
        r4.recycle();
    L_0x004b:
        return r5;
    L_0x004c:
        r5 = r6;
        goto L_0x0041;
    L_0x004e:
        r2 = move-exception;
        r5 = "WindowManagerProxy";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r7 = "remote exception.";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        android.util.Log.e(r5, r7);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        if (r0 == 0) goto L_0x005d;
    L_0x005a:
        r0.recycle();
    L_0x005d:
        if (r4 == 0) goto L_0x0062;
    L_0x005f:
        r4.recycle();
    L_0x0062:
        return r6;
    L_0x0063:
        r1 = move-exception;
        r5 = "WindowManagerProxy";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        r7 = "remote remoteexception.";	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        android.util.Log.e(r5, r7);	 Catch:{ RemoteException -> 0x0063, Exception -> 0x004e, all -> 0x0078 }
        if (r0 == 0) goto L_0x0072;
    L_0x006f:
        r0.recycle();
    L_0x0072:
        if (r4 == 0) goto L_0x0077;
    L_0x0074:
        r4.recycle();
    L_0x0077:
        return r6;
    L_0x0078:
        r5 = move-exception;
        if (r0 == 0) goto L_0x007e;
    L_0x007b:
        r0.recycle();
    L_0x007e:
        if (r4 == 0) goto L_0x0083;
    L_0x0080:
        r4.recycle();
    L_0x0083:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.WindowManagerProxy.isDimLayerVisible():boolean");
    }

    private WindowManagerProxy() {
    }

    public static WindowManagerProxy getInstance() {
        return sInstance;
    }

    public void resizeDockedStack(Rect docked, Rect tempDockedTaskRect, Rect tempDockedInsetRect, Rect tempOtherTaskRect, Rect tempOtherInsetRect) {
        synchronized (this.mDockedRect) {
            this.mDockedRect.set(docked);
            if (tempDockedTaskRect != null) {
                this.mTempDockedTaskRect.set(tempDockedTaskRect);
            } else {
                this.mTempDockedTaskRect.setEmpty();
            }
            if (tempDockedInsetRect != null) {
                this.mTempDockedInsetRect.set(tempDockedInsetRect);
            } else {
                this.mTempDockedInsetRect.setEmpty();
            }
            if (tempOtherTaskRect != null) {
                this.mTempOtherTaskRect.set(tempOtherTaskRect);
            } else {
                this.mTempOtherTaskRect.setEmpty();
            }
            if (tempOtherInsetRect != null) {
                this.mTempOtherInsetRect.set(tempOtherInsetRect);
            } else {
                this.mTempOtherInsetRect.setEmpty();
            }
        }
        this.mExecutor.execute(this.mResizeRunnable);
    }

    public void dismissDockedStack() {
        this.mExecutor.execute(this.mDismissRunnable);
    }

    public void maximizeDockedStack() {
        this.mExecutor.execute(this.mMaximizeRunnable);
    }

    public void setResizing(final boolean resizing) {
        this.mExecutor.execute(new Runnable() {
            public void run() {
                try {
                    WindowManagerGlobal.getWindowManagerService().setDockedStackResizing(resizing);
                } catch (RemoteException e) {
                    Log.w("WindowManagerProxy", "Error calling setDockedStackResizing: " + e);
                }
            }
        });
    }

    public int getDockSide() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w("WindowManagerProxy", "Failed to get dock side: " + e);
            return -1;
        }
    }

    public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
        this.mDimLayerVisible = visible;
        this.mDimLayerTargetStack = targetStackId;
        this.mDimLayerAlpha = alpha;
        this.mExecutor.execute(this.mDimLayerRunnable);
    }

    public void swapTasks() {
        this.mExecutor.execute(this.mSwapRunnable);
    }

    public void setTouchRegion(Rect region) {
        synchronized (this.mDockedRect) {
            this.mTouchableRegion.set(region);
        }
        this.mExecutor.execute(this.mSetTouchableRegionRunnable);
    }
}
