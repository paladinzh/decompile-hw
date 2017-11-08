package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.huawei.systemmanager.power.data.xml.RogueListPreset;

public class HandleBootCompleted implements IBroadcastHandler {

    private static class MakeSureDBReadyRunnable implements Runnable {
        private Context mCtx;

        MakeSureDBReadyRunnable(Context ctx) {
            this.mCtx = ctx;
        }

        public void run() {
            RogueListPreset.presetRoguePackage(this.mCtx);
        }
    }

    public void handleBroadcast(Context context, Intent intent) {
        makeSureDBReady(context);
    }

    private void makeSureDBReady(Context c) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new MakeSureDBReadyRunnable(c));
    }
}
