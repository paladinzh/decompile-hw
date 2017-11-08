package com.android.deskclock.stopwatch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.android.deskclock.R;
import com.android.util.Config;
import com.android.util.Log;

public class StopwatchService extends Service {
    private Context mContext;
    private int mRepeatPoolId;
    private int mRepeatStreamId;
    private boolean mSinglePlayState = false;
    private int mSinglePoolId;
    private SoundPool mSingleSoundPool;
    private int mSingleStreamId;
    private SoundPool mSoundPool;
    private WakeLock mWakeLock;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        Log.d("StopwatchService", "onCreate");
        this.mContext = this;
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.dRelease("StopwatchService", "onStartCommand");
        if (intent == null || intent.getAction() == null) {
            return 2;
        }
        String action = intent.getAction();
        if ("com.deskclock.stopwatch.soundpool.resume".equals(action)) {
            if (intent.getBooleanExtra("aquire_wakelock", false)) {
                wakeLockForce();
            }
            resumePool();
        } else if ("com.deskclock.stopwatch.soundpool.pause".equals(action)) {
            if (intent.getBooleanExtra("release_wakelock", false)) {
                releaseWakeLock();
            }
            pausePool();
        } else if ("com.deskclock.stopwatch.startService".equals(action)) {
            if (intent.getIntExtra("stopwatch_state", 0) == 1 && Config.clockTabIndex() == 2) {
                loadSoundPool();
            }
            wakeLockForce();
        } else if ("com.deskclock.stopwatch.soundpool.count".equals(action)) {
            if (this.mSinglePlayState) {
                playSingle();
            } else {
                loadSingleSoundPool();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    private void wakeLockForce() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(536870913, "stopwatch");
            if (!(this.mWakeLock == null || this.mWakeLock.isHeld())) {
                this.mWakeLock.acquire();
            }
        }
    }

    public void onDestroy() {
        Log.d("StopwatchService", "onDestroy");
        releaseWakeLock();
        super.onDestroy();
        stopSinglePool();
        releaseSinglePool();
        stopPool();
        releasePool();
    }

    private void loadSoundPool() {
        if (this.mSoundPool != null) {
            stopPool();
            releasePool();
        }
        this.mSoundPool = new SoundPool(1, 3, 1);
        this.mRepeatPoolId = this.mSoundPool.load(this.mContext, R.raw.stopwatch_tick, 1);
        this.mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.dRelease("StopwatchService", "loadSoundPool. sampleId = " + sampleId);
                if (sampleId == StopwatchService.this.mRepeatPoolId) {
                    StopwatchService.this.playPool();
                }
            }
        });
        Log.dRelease("StopwatchService", this.mSinglePoolId + " loadSoundPool. mRepeatPoolId = " + this.mRepeatPoolId);
    }

    private void loadSingleSoundPool() {
        this.mSinglePlayState = false;
        this.mSingleSoundPool = new SoundPool(1, 3, 1);
        this.mSinglePoolId = this.mSingleSoundPool.load(this.mContext, R.raw.records, 1);
        this.mSingleSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.dRelease("StopwatchService", "loadSingleSoundPool. sampleId = " + sampleId);
                if (sampleId == StopwatchService.this.mSinglePoolId) {
                    StopwatchService.this.playSingle();
                    StopwatchService.this.mSinglePlayState = true;
                }
            }
        });
        Log.dRelease("StopwatchService", "loadSingleSoundPool.");
    }

    private void playPool() {
        if (this.mSoundPool != null) {
            this.mRepeatStreamId = this.mSoundPool.play(this.mRepeatPoolId, 1.0f, 1.0f, 1, -1, 1.0f);
        }
        if (this.mRepeatStreamId == 0) {
            Log.w("StopwatchService", "can not play the repeat soundpool file.");
        }
        Log.dRelease("StopwatchService", "playPool.");
    }

    private void playSingle() {
        if (this.mSingleSoundPool != null) {
            this.mSingleStreamId = this.mSingleSoundPool.play(this.mSinglePoolId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
        if (this.mSingleStreamId == 0) {
            Log.w("StopwatchService", "can not play the single soundpool file.");
        }
        Log.dRelease("StopwatchService", "playSingle.");
    }

    private void resumePool() {
        if (this.mSoundPool == null) {
            loadSoundPool();
        } else if (this.mRepeatStreamId != 0) {
            this.mSoundPool.resume(this.mRepeatStreamId);
        }
        Log.dRelease("StopwatchService", "resumePool.");
    }

    private void pausePool() {
        if (!(this.mSoundPool == null || this.mRepeatStreamId == 0)) {
            this.mSoundPool.pause(this.mRepeatStreamId);
        }
        Log.dRelease("StopwatchService", "pausePool.");
    }

    private void stopPool() {
        if (!(this.mSoundPool == null || this.mRepeatStreamId == 0)) {
            this.mSoundPool.stop(this.mRepeatStreamId);
        }
        Log.dRelease("StopwatchService", "stopPool.");
    }

    private void stopSinglePool() {
        if (!(this.mSingleSoundPool == null || this.mSingleStreamId == 0)) {
            this.mSingleSoundPool.stop(this.mSingleStreamId);
            this.mSinglePlayState = false;
        }
        Log.dRelease("StopwatchService", "stopSinglePool.");
    }

    private void releaseSinglePool() {
        if (this.mSingleSoundPool != null) {
            this.mSingleSoundPool.release();
            this.mSingleSoundPool = null;
            this.mSinglePlayState = false;
        }
        Log.dRelease("StopwatchService", "releaseSinglePool.");
    }

    private void releasePool() {
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
        Log.dRelease("StopwatchService", "releasePool.");
    }
}
