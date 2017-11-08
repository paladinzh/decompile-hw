package com.huawei.thermal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.huawei.thermal.event.Event;
import com.huawei.thermal.eventhub.EventListener;
import java.util.ArrayList;
import java.util.Iterator;

public class InputManager implements EventListener {
    private InputDispatcher mDispatcher;
    private boolean mHookEventChannelOk = false;
    private final ArrayList<InputListener> mInputListeners = new ArrayList();

    final class InputDispatcher extends Handler {
        public InputDispatcher(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    Event evt = msg.obj;
                    Iterator it = InputManager.this.mInputListeners.iterator();
                    while (it.hasNext()) {
                        ((InputListener) it.next()).onInputEvent(evt);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected InputManager(Looper looper) {
        this.mDispatcher = new InputDispatcher(looper);
    }

    public void handleEvent(Event evt) {
        if (!this.mHookEventChannelOk && evt.getType() == 2) {
            this.mHookEventChannelOk = true;
            Log.e("InputManager", "hook event channel is ok!");
        }
        putQueue(evt);
    }

    private void putQueue(Event evt) {
        this.mDispatcher.sendMessageDelayed(this.mDispatcher.obtainMessage(100, evt), 0);
    }

    public void registerListener(InputListener listener) {
        if (listener != null) {
            synchronized (this.mInputListeners) {
                if (!this.mInputListeners.contains(listener)) {
                    this.mInputListeners.add(listener);
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }
}
