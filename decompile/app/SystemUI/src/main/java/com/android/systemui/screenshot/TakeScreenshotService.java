package com.android.systemui.screenshot;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.MonitorReporter;
import java.util.concurrent.atomic.AtomicBoolean;

public class TakeScreenshotService extends Service {
    private static AtomicBoolean mRunning = new AtomicBoolean(false);
    private static GlobalScreenshot mScreenshot;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            final Messenger callback = msg.replyTo;
            Runnable finisher = new Runnable() {
                public void run() {
                    try {
                        callback.send(Message.obtain(null, 1));
                    } catch (RemoteException e) {
                        TakeScreenshotService.this.triggerRadarScreenshotFailed(e.getMessage());
                    }
                    TakeScreenshotService.mRunning.set(false);
                }
            };
            if (TakeScreenshotService.mRunning.get()) {
                HwLog.w("TakeScreenshotService", "takeScreenshot is running, ignore this request!!!");
            } else if (TakeScreenshotService.this.isUserUnlocked(TakeScreenshotService.this.getApplicationContext())) {
                if (TakeScreenshotService.mScreenshot == null) {
                    TakeScreenshotService.mScreenshot = new HwGlobalScreenshot(TakeScreenshotService.this);
                }
                GlobalScreenshot -get1;
                boolean z2;
                switch (msg.what) {
                    case 1:
                        TakeScreenshotService.mRunning.set(true);
                        -get1 = TakeScreenshotService.mScreenshot;
                        z2 = msg.arg1 > 0;
                        if (msg.arg2 <= 0) {
                            z = false;
                        }
                        -get1.takeScreenshot(finisher, z2, z);
                        break;
                    case 2:
                        TakeScreenshotService.mRunning.set(true);
                        -get1 = TakeScreenshotService.mScreenshot;
                        z2 = msg.arg1 > 0;
                        if (msg.arg2 <= 0) {
                            z = false;
                        }
                        -get1.takeScreenshotPartial(finisher, z2, z);
                        break;
                }
            } else {
                HwLog.w("TakeScreenshotService", "now isRestrictAsEncrypt, donot allow to take screenshot");
                finisher.run();
            }
        }
    };

    public IBinder onBind(Intent intent) {
        return new Messenger(this.mHandler).getBinder();
    }

    public boolean onUnbind(Intent intent) {
        if (mScreenshot != null) {
            mScreenshot.stopScreenshot();
        }
        return true;
    }

    private void triggerRadarScreenshotFailed(String errMsg) {
        MonitorReporter.doMonitor(MonitorReporter.createInfoIntent(907033005, MonitorReporter.createMapInfo((short) 0, errMsg)));
    }

    private boolean isUserUnlocked(Context context) {
        if (context != null) {
            return ((UserManager) context.getSystemService("user")).isUserUnlocked(ActivityManager.getCurrentUser());
        }
        HwLog.w("TakeScreenshotService", "isUserUnlocked(): given context is null! return false");
        return false;
    }
}
