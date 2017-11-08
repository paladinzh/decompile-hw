package com.huawei.cspcommon.ex;

import android.content.AsyncQueryHandler;
import android.content.AsyncQueryHandler.WorkerHandler;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;

public class AsyncQueryHandlerEx extends AsyncQueryHandler {
    private ArrayList<Message> mPedingResults = new ArrayList();

    protected class WorkHandlerEX extends WorkerHandler {
        public WorkHandlerEX(Looper looper) {
            super(AsyncQueryHandlerEx.this, looper);
        }

        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
            } catch (Exception e) {
                ErrorMonitor.reportErrorInfo(2, getClass().getName() + ", handleMessage fail " + msg);
            }
        }
    }

    public AsyncQueryHandlerEx(ContentResolver cr) {
        super(cr);
    }

    private void removePendingResult(Message msg) {
        synchronized (this.mPedingResults) {
            this.mPedingResults.remove(msg);
        }
    }

    protected Handler createHandler(Looper looper) {
        return new WorkHandlerEX(looper);
    }

    public void handleMessage(Message msg) {
        try {
            removePendingResult(msg);
            super.handleMessage(msg);
        } catch (Exception e) {
            ErrorMonitor.reportErrorInfo(2, getClass().getName() + ", handleMessage fail " + msg);
        }
    }
}
