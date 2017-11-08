package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.os.SomeArgs;
import com.android.systemui.recents.IRecentsNonSystemUserCallbacks.Stub;

public class RecentsImplProxy extends Stub {
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            SomeArgs args;
            RecentsImpl -get0;
            switch (msg.what) {
                case 1:
                    RecentsImplProxy.this.mImpl.preloadRecents();
                    break;
                case 2:
                    RecentsImplProxy.this.mImpl.cancelPreloadingRecents();
                    break;
                case 3:
                    boolean z2;
                    args = msg.obj;
                    RecentsImpl -get02 = RecentsImplProxy.this.mImpl;
                    boolean z3 = args.argi1 != 0;
                    boolean z4 = args.argi2 != 0;
                    boolean z5 = args.argi3 != 0;
                    if (args.argi4 != 0) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    if (args.argi5 == 0) {
                        z = false;
                    }
                    -get02.showRecents(z3, z4, z5, z2, z, args.argi6);
                    break;
                case 4:
                    -get0 = RecentsImplProxy.this.mImpl;
                    boolean z6 = msg.arg1 != 0;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    -get0.hideRecents(z6, z);
                    break;
                case 5:
                    RecentsImplProxy.this.mImpl.toggleRecents(((SomeArgs) msg.obj).argi1);
                    break;
                case 6:
                    RecentsImplProxy.this.mImpl.onConfigurationChanged();
                    break;
                case 7:
                    args = (SomeArgs) msg.obj;
                    -get0 = RecentsImplProxy.this.mImpl;
                    int i = args.argi1;
                    int i2 = args.argi2;
                    args.argi3 = 0;
                    -get0.dockTopTask(i, i2, 0, (Rect) args.arg1);
                    break;
                case 8:
                    RecentsImplProxy.this.mImpl.onDraggingInRecents(((Float) msg.obj).floatValue());
                    break;
                case 9:
                    RecentsImplProxy.this.mImpl.onDraggingInRecentsEnded(((Float) msg.obj).floatValue());
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private RecentsImpl mImpl;

    public RecentsImplProxy(RecentsImpl recentsImpl) {
        this.mImpl = recentsImpl;
    }

    public void preloadRecents() throws RemoteException {
        this.mHandler.sendEmptyMessage(1);
    }

    public void cancelPreloadingRecents() throws RemoteException {
        this.mHandler.sendEmptyMessage(2);
    }

    public void showRecents(boolean triggeredFromAltTab, boolean draggingInRecents, boolean animate, boolean reloadTasks, boolean fromHome, int growTarget) throws RemoteException {
        int i;
        int i2 = 1;
        SomeArgs args = SomeArgs.obtain();
        if (triggeredFromAltTab) {
            i = 1;
        } else {
            i = 0;
        }
        args.argi1 = i;
        if (draggingInRecents) {
            i = 1;
        } else {
            i = 0;
        }
        args.argi2 = i;
        if (animate) {
            i = 1;
        } else {
            i = 0;
        }
        args.argi3 = i;
        if (reloadTasks) {
            i = 1;
        } else {
            i = 0;
        }
        args.argi4 = i;
        if (!fromHome) {
            i2 = 0;
        }
        args.argi5 = i2;
        args.argi6 = growTarget;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, args));
    }

    public void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
        int i;
        int i2 = 1;
        Handler handler = this.mHandler;
        Handler handler2 = this.mHandler;
        if (triggeredFromAltTab) {
            i = 1;
        } else {
            i = 0;
        }
        if (!triggeredFromHomeKey) {
            i2 = 0;
        }
        handler.sendMessage(handler2.obtainMessage(4, i, i2));
    }

    public void toggleRecents(int growTarget) throws RemoteException {
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = growTarget;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, args));
    }

    public void onConfigurationChanged() throws RemoteException {
        this.mHandler.sendEmptyMessage(6);
    }

    public void dockTopTask(int topTaskId, int dragMode, int stackCreateMode, Rect initialBounds) throws RemoteException {
        SomeArgs args = SomeArgs.obtain();
        args.argi1 = topTaskId;
        args.argi2 = dragMode;
        args.argi3 = stackCreateMode;
        args.arg1 = initialBounds;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, args));
    }

    public void onDraggingInRecents(float distanceFromTop) throws RemoteException {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(8, Float.valueOf(distanceFromTop)));
    }

    public void onDraggingInRecentsEnded(float velocity) throws RemoteException {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, Float.valueOf(velocity)));
    }
}
