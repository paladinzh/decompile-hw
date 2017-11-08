package com.android.settings.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class ScreenWakerService extends Service {
    private Runnable mKeepOnScreenTask = new KeepOnScreenTask();
    private int mTimeout = 10000;
    private WakeLock mWakeLock;

    private class KeepOnScreenTask implements Runnable {
        private KeepOnScreenTask() {
        }

        public void run() {
            ScreenWakerService.this.mWakeLock.acquire();
            try {
                Thread.currentThread();
                Thread.sleep((long) ScreenWakerService.this.mTimeout);
                if (ScreenWakerService.this.mWakeLock.isHeld()) {
                    ScreenWakerService.this.mWakeLock.release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (ScreenWakerService.this.mWakeLock.isHeld()) {
                    ScreenWakerService.this.mWakeLock.release();
                }
            } catch (Throwable th) {
                if (ScreenWakerService.this.mWakeLock.isHeld()) {
                    ScreenWakerService.this.mWakeLock.release();
                }
            }
            ScreenWakerService.this.stopSelf();
        }
    }

    public void onDestroy() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(805306374, "BluetoothScreenWakerService");
        }
        if (this.mWakeLock.isHeld()) {
            return 2;
        }
        if (intent != null) {
            this.mTimeout = intent.getIntExtra("timeout", 10000);
        }
        new Thread(this.mKeepOnScreenTask).start();
        return 2;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
