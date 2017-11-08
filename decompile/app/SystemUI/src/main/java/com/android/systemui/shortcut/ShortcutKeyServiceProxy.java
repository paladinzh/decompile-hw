package com.android.systemui.shortcut;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.policy.IShortcutService.Stub;

public class ShortcutKeyServiceProxy extends Stub {
    private Callbacks mCallbacks;
    private final Handler mHandler = new H();
    private final Object mLock = new Object();

    public interface Callbacks {
        void onShortcutKeyPressed(long j);
    }

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ShortcutKeyServiceProxy.this.mCallbacks.onShortcutKeyPressed(((Long) msg.obj).longValue());
                    return;
                default:
                    return;
            }
        }
    }

    public ShortcutKeyServiceProxy(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void notifyShortcutKeyPressed(long shortcutCode) throws RemoteException {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1, Long.valueOf(shortcutCode)).sendToTarget();
        }
    }
}
