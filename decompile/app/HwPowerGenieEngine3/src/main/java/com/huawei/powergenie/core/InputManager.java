package com.huawei.powergenie.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.Event;
import com.huawei.powergenie.integration.eventhub.EventListener;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.util.ArrayList;

public class InputManager implements EventListener {
    private InputDispatcher mDispatcher;
    private boolean mHookEventChannelOk = false;
    private final ArrayList<InputListener> mInputListeners = new ArrayList();

    public interface InputListener {
        void onInputHookEvent(HookEvent hookEvent);

        void onInputMsgEvent(MsgEvent msgEvent);
    }

    static final class EventCreator {
        private static final ArrayList<HookEvent> mFreeHookEvents = new ArrayList();
        private static final ArrayList<MsgEvent> mFreeMsgEvents = new ArrayList();

        EventCreator() {
        }

        protected static MsgEvent obtain(MsgEvent evt) {
            MsgEvent msgEvent = null;
            synchronized (mFreeMsgEvents) {
                if (mFreeMsgEvents.size() > 0) {
                    msgEvent = (MsgEvent) mFreeMsgEvents.remove(0);
                }
            }
            if (msgEvent == null) {
                Log.i("InputManager", "new msg event");
                return new MsgEvent(evt.getEventId(), evt.getIntent());
            }
            msgEvent.resetAs(evt.getEventId(), evt.getIntent());
            return msgEvent;
        }

        protected static HookEvent obtain(HookEvent evt) {
            HookEvent hookEvent = null;
            synchronized (mFreeHookEvents) {
                if (mFreeHookEvents.size() > 0) {
                    hookEvent = (HookEvent) mFreeHookEvents.remove(0);
                }
            }
            if (hookEvent == null) {
                Log.i("InputManager", "new hook event");
                hookEvent = new HookEvent(evt.getEventId());
            }
            hookEvent.resetAs(evt);
            return hookEvent;
        }

        protected static void recycle(MsgEvent evt) {
            synchronized (mFreeMsgEvents) {
                if (mFreeMsgEvents.size() < 3) {
                    mFreeMsgEvents.add(evt);
                }
            }
        }

        protected static void recycle(HookEvent evt) {
            synchronized (mFreeHookEvents) {
                if (mFreeHookEvents.size() < 5) {
                    mFreeHookEvents.add(evt);
                }
            }
        }
    }

    final class InputDispatcher extends Handler {
        public InputDispatcher(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    MsgEvent evtMsg = msg.obj;
                    for (InputListener listener : InputManager.this.mInputListeners) {
                        listener.onInputMsgEvent(evtMsg);
                    }
                    EventCreator.recycle(evtMsg);
                    return;
                case 101:
                    HookEvent evtHook = msg.obj;
                    for (InputListener listener2 : InputManager.this.mInputListeners) {
                        listener2.onInputHookEvent(evtHook);
                    }
                    EventCreator.recycle(evtHook);
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
        switch (evt.getType()) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                putQueue(EventCreator.obtain((MsgEvent) evt));
                return;
            case NativeAdapter.PLATFORM_HI /*2*/:
                if (!this.mHookEventChannelOk) {
                    this.mHookEventChannelOk = true;
                    Log.e("InputManager", "hook event channel is ok!");
                }
                putQueue(EventCreator.obtain((HookEvent) evt));
                return;
            default:
                return;
        }
    }

    private void putQueue(HookEvent evt) {
        this.mDispatcher.sendMessageDelayed(this.mDispatcher.obtainMessage(101, evt), 0);
    }

    private void putQueue(MsgEvent evt) {
        this.mDispatcher.sendMessageDelayed(this.mDispatcher.obtainMessage(100, evt), 0);
    }

    public void registerListener(InputListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mInputListeners) {
            if (!this.mInputListeners.contains(listener)) {
                this.mInputListeners.add(listener);
            }
        }
    }
}
