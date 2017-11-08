package com.huawei.powergenie.integration.eventhub;

import android.os.Process;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.adapter.RawEvent;

public class HookHub extends EventHub {
    private static final boolean DBG = Log.isLoggable("HookHub", 2);
    private static HookHub sInstance;
    private boolean mIsStart = false;
    private EventPollThread mPollThead;

    private class EventPollThread extends Thread {
        private boolean mExit = false;

        protected EventPollThread(String name) {
            super(name);
        }

        public void run() {
            Process.setThreadPriority(0);
            NativeAdapter.clearLog();
            Log.i("HookHub", "start reading -> thread:" + Process.myTid());
            RawEvent rawEvent = new RawEvent();
            HookEvent evt = new HookEvent(0);
            while (!this.mExit && NativeAdapter.readHookEvent(rawEvent) > 0) {
                if (HookHub.DBG) {
                    Log.i("HookHub", "read event: " + rawEvent);
                }
                evt.fill(rawEvent);
                HookHub.this.dispatchEvent(evt);
                rawEvent.reset();
            }
            Log.e("HookHub", "read thread exit. mExit: " + this.mExit);
        }
    }

    private HookHub() {
    }

    protected static EventHub getInstance() {
        EventHub eventHub;
        synchronized (HookHub.class) {
            if (sInstance == null) {
                sInstance = new HookHub();
            }
            eventHub = sInstance;
        }
        return eventHub;
    }

    protected boolean start() {
        if (this.mIsStart) {
            return true;
        }
        this.mIsStart = true;
        this.mPollThead = new EventPollThread("HookHub");
        this.mPollThead.start();
        return true;
    }
}
