package com.android.mms.util;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.Telephony.Mms.Rate;
import android.support.v4.content.LocalBroadcastManager;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;

public class RateController {
    private static RateController sInstance;
    private static boolean sMutexLock;
    private int mAnswer;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.android.mms.RATE_LIMIT_CONFIRMED".equals(intent.getAction())) {
                synchronized (RateController.this) {
                    RateController.this.mAnswer = intent.getBooleanExtra("answer", false) ? 1 : 2;
                    RateController.this.notifyAll();
                }
            }
        }
    };
    private final Context mContext;

    private RateController(Context context) {
        this.mContext = context;
    }

    public static synchronized void init(Context context) {
        synchronized (RateController.class) {
            if (sInstance != null) {
                MLog.w("RateController", "Already initialized.");
                return;
            }
            sInstance = new RateController(context);
        }
    }

    public static RateController getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        throw new IllegalStateException("Uninitialized.");
    }

    public final void update() {
        ContentValues values = new ContentValues(1);
        values.put("sent_time", Long.valueOf(System.currentTimeMillis()));
        SqliteWrapper.insert(this.mContext, this.mContext.getContentResolver(), Rate.CONTENT_URI, values);
    }

    public final boolean isLimitSurpassed() {
        Cursor c = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), Rate.CONTENT_URI, new String[]{"COUNT(*) AS rate"}, "sent_time>" + (System.currentTimeMillis() - 3600000), null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    boolean z = c.getInt(0) >= 250;
                    c.close();
                    return z;
                }
                c.close();
            } catch (Throwable th) {
                c.close();
            }
        }
        return false;
    }

    public static synchronized void setMutexLocked(boolean locked) {
        synchronized (RateController.class) {
            sMutexLock = locked;
        }
    }

    public static synchronized boolean isMutexLocked() {
        boolean z;
        synchronized (RateController.class) {
            z = sMutexLock;
        }
        return z;
    }

    public synchronized boolean isAllowedByUser() {
        boolean z = true;
        synchronized (this) {
            while (isMutexLocked()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            setMutexLocked(true);
            LocalBroadcastManager.getInstance(this.mContext).registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.android.mms.RATE_LIMIT_CONFIRMED"));
            this.mAnswer = 0;
            try {
                Intent intent = new Intent("com.android.mms.RATE_LIMIT_SURPASSED");
                intent.addFlags(268435456);
                this.mContext.startActivity(intent);
                if (waitForAnswer() != 1) {
                    z = false;
                }
            } finally {
                LocalBroadcastManager.getInstance(this.mContext).unregisterReceiver(this.mBroadcastReceiver);
                setMutexLocked(false);
                notifyAll();
            }
        }
        return z;
    }

    private synchronized int waitForAnswer() {
        int t = 0;
        while (this.mAnswer == 0 && t < 20000) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
            }
            t += 1000;
        }
        return this.mAnswer;
    }
}
