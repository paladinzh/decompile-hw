package com.android.settings.accessibility;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Vibrator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PersistentNotificationScheduler extends ScheduledThreadPoolExecutor {
    private static Context mContext;
    private static PersistentNotificationScheduler mPersistentNotificationScheduler;
    private static final Runnable mRunnable = new PersistentNotificationTask();

    private static class PersistentNotificationTask implements Runnable {
        private PersistentNotificationTask() {
        }

        public void run() {
            playNotificationSoundAndVibrate();
        }

        private void playNotificationSoundAndVibrate() {
            AudioManager audioManager = (AudioManager) PersistentNotificationScheduler.mContext.getSystemService("audio");
            int ringerState = 0;
            if (audioManager != null) {
                ringerState = audioManager.getRingerMode();
            }
            Vibrator vibrator = (Vibrator) PersistentNotificationScheduler.mContext.getSystemService("vibrator");
            switch (ringerState) {
                case 1:
                    vibrator.vibrate(500);
                    return;
                case 2:
                    Ringtone r = RingtoneManager.getRingtone(PersistentNotificationScheduler.mContext, RingtoneManager.getDefaultUri(2));
                    if (r != null) {
                        r.play();
                    }
                    vibrator.vibrate(500);
                    return;
                default:
                    return;
            }
        }
    }

    private PersistentNotificationScheduler() {
        super(1);
    }

    public static synchronized PersistentNotificationScheduler getInstance(Context context) {
        PersistentNotificationScheduler persistentNotificationScheduler;
        synchronized (PersistentNotificationScheduler.class) {
            mContext = context;
            if (mPersistentNotificationScheduler == null || mPersistentNotificationScheduler.isShutdown()) {
                mPersistentNotificationScheduler = new PersistentNotificationScheduler();
            }
            persistentNotificationScheduler = mPersistentNotificationScheduler;
        }
        return persistentNotificationScheduler;
    }

    public void scheduleIndefiniteTimer(long interval) {
        scheduleWithFixedDelay(mRunnable, interval, interval, TimeUnit.SECONDS);
    }
}
