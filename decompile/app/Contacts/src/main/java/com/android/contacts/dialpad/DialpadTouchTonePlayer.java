package com.android.contacts.dialpad;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import com.android.contacts.util.HwLog;
import com.android.phone.common.HapticFeedback;
import com.google.android.gms.R;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DialpadTouchTonePlayer {
    private static final int[][] TONE_LIST = new int[][]{new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 11}, new int[]{R.raw.phone_key_pressed_piano_1, R.raw.phone_key_pressed_piano_2, R.raw.phone_key_pressed_piano_3, R.raw.phone_key_pressed_piano_4, R.raw.phone_key_pressed_piano_5, R.raw.phone_key_pressed_piano_6, R.raw.phone_key_pressed_piano_7, R.raw.phone_key_pressed_piano_8, R.raw.phone_key_pressed_piano_9, R.raw.phone_key_pressed_piano_star, R.raw.phone_key_pressed_piano_0, R.raw.phone_key_pressed_piano_pound}};
    private AudioManager mAudioMgr;
    private Context mContext;
    private int mDialpadTouchToneType;
    private ExecutorService mExecutorService = null;
    private boolean mIgnoreSilentMode = false;
    private MediaPlayer mMediaPlayer = null;
    private SoundPool mSoundPool = null;
    private ArrayList<Integer> mSoundPoolIdSet = null;
    SoundPoolLoadListener mSoundPoolLoadListener = new SoundPoolLoadListener() {
        public void onSoundPoolLoadFinished(SoundPool soundPool, ArrayList<Integer> soundPoolIdSet) {
            synchronized (DialpadTouchTonePlayer.this.mSyncLock) {
                if (DialpadTouchTonePlayer.this.mExecutorService != null) {
                    DialpadTouchTonePlayer.this.mSoundPool = soundPool;
                    DialpadTouchTonePlayer.this.mSoundPoolIdSet = soundPoolIdSet;
                } else if (soundPool != null) {
                    soundPool.release();
                }
            }
        }

        public void onSoundPoolLoadCanceled(SoundPool soundPool, ArrayList<Integer> arrayList) {
            synchronized (DialpadTouchTonePlayer.this.mSyncLock) {
                if (soundPool != null) {
                    soundPool.release();
                }
            }
        }
    };
    private SoundPoolRunnable mSoundPoolRunnable = null;
    private final Object mSyncLock = new Object();
    private ToneGenerator mToneGenerator = null;
    private TonePlayerHandler mTonePlayerHandler;
    private HandlerThread mTonePlayerThread;

    private interface SoundPoolLoadListener {
        void onSoundPoolLoadCanceled(SoundPool soundPool, ArrayList<Integer> arrayList);

        void onSoundPoolLoadFinished(SoundPool soundPool, ArrayList<Integer> arrayList);
    }

    private static class SoundPoolRunnable implements Runnable, OnLoadCompleteListener {
        private boolean mCanceled;
        private WeakReference<Context> mContext;
        private SoundPoolLoadListener mListener;
        private SoundPool mSoundPool;
        private ArrayList<Integer> mSoundPoolIdSet;
        private Object mSyncLock;

        private SoundPoolRunnable(Context context, SoundPoolLoadListener listener) {
            this.mCanceled = false;
            this.mSoundPool = null;
            this.mSoundPoolIdSet = new ArrayList();
            this.mContext = new WeakReference(context);
            this.mListener = listener;
            this.mSyncLock = new Object();
        }

        public synchronized void cancel() {
            if (!this.mCanceled) {
                this.mCanceled = true;
                synchronized (this.mSyncLock) {
                    this.mSyncLock.notifyAll();
                }
            }
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            int key = DialpadTouchTonePlayer.TONE_LIST[1].length;
            if (this.mSoundPoolIdSet != null && this.mSoundPoolIdSet.size() == key && this.mSoundPoolIdSet.get(key - 1) != null && sampleId == ((Integer) this.mSoundPoolIdSet.get(key - 1)).intValue()) {
                this.mListener.onSoundPoolLoadFinished(this.mSoundPool, this.mSoundPoolIdSet);
                this.mListener = null;
                this.mSoundPool = null;
                this.mSoundPoolIdSet = null;
            }
        }

        public void run() {
            synchronized (this.mSyncLock) {
                try {
                    this.mSyncLock.wait(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (this.mCanceled) {
                this.mListener.onSoundPoolLoadCanceled(null, null);
                this.mListener = null;
                return;
            }
            this.mSoundPool = new SoundPool(10, 8, 0);
            if (this.mCanceled) {
                this.mListener.onSoundPoolLoadCanceled(this.mSoundPool, null);
                this.mListener = null;
                return;
            }
            this.mSoundPool.setOnLoadCompleteListener(this);
            for (int load : DialpadTouchTonePlayer.TONE_LIST[1]) {
                if (this.mCanceled) {
                    this.mListener.onSoundPoolLoadCanceled(this.mSoundPool, this.mSoundPoolIdSet);
                    this.mListener = null;
                    return;
                }
                if (this.mContext.get() != null) {
                    this.mSoundPoolIdSet.add(Integer.valueOf(this.mSoundPool.load((Context) this.mContext.get(), load, 1)));
                }
            }
        }
    }

    private class TonePlayerHandler extends Handler {
        public TonePlayerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            if (msg.what == 4097 || msg.what == 4098) {
                int key = msg.arg1;
                if (DialpadTouchTonePlayer.this.mDialpadTouchToneType != 0 && (!DialpadTouchTonePlayer.this.isSilentMode() || DialpadTouchTonePlayer.this.mIgnoreSilentMode)) {
                    if (msg.what != 4097) {
                        z = true;
                    }
                    playKeyTone(key, z);
                }
                if (msg.arg2 == 1) {
                    try {
                        ((HapticFeedback) msg.obj).vibrate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == 4099) {
                DialpadTouchTonePlayer.this.stopInner();
            } else if (msg.what == 4100) {
                try {
                    ((HapticFeedback) msg.obj).lonVibrate();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void playKeyTone(int key, boolean isInfinite) {
            int tone = DialpadTouchTonePlayer.TONE_LIST[DialpadTouchTonePlayer.this.mDialpadTouchToneType - 1][key];
            if (DialpadTouchTonePlayer.this.mDialpadTouchToneType == 1) {
                synchronized (DialpadTouchTonePlayer.this.mSyncLock) {
                    int i;
                    if (DialpadTouchTonePlayer.this.mToneGenerator == null) {
                        try {
                            DialpadTouchTonePlayer.this.mToneGenerator = new ToneGenerator(8, 80);
                        } catch (RuntimeException e) {
                            HwLog.w("DialpadTouchTonePlayer", "Exception caught while creating local tone generator: " + e);
                            DialpadTouchTonePlayer.this.mToneGenerator = null;
                            return;
                        }
                    }
                    ToneGenerator -get9 = DialpadTouchTonePlayer.this.mToneGenerator;
                    if (isInfinite) {
                        i = -1;
                    } else {
                        i = 100;
                    }
                    -get9.startTone(tone, i);
                }
            } else if (DialpadTouchTonePlayer.this.mDialpadTouchToneType == 2) {
                synchronized (DialpadTouchTonePlayer.this.mSyncLock) {
                    if (DialpadTouchTonePlayer.this.mSoundPool == null || DialpadTouchTonePlayer.this.mSoundPoolIdSet.get(key) == null) {
                    } else {
                        DialpadTouchTonePlayer.this.mSoundPool.play(((Integer) DialpadTouchTonePlayer.this.mSoundPoolIdSet.get(key)).intValue(), 1.0f, 1.0f, 0, 0, 1.0f);
                        if (DialpadTouchTonePlayer.this.mMediaPlayer != null) {
                            DialpadTouchTonePlayer.this.mMediaPlayer.release();
                            DialpadTouchTonePlayer.this.mMediaPlayer = null;
                        }
                    }
                }
            }
            DialpadTouchTonePlayer.this.mMediaPlayer.start();
        }
    }

    public DialpadTouchTonePlayer(Context context) {
        this.mContext = context;
        refreshToneType();
        this.mTonePlayerThread = new HandlerThread("tone_playback_thread");
        this.mTonePlayerThread.start();
        this.mTonePlayerHandler = new TonePlayerHandler(this.mTonePlayerThread.getLooper());
    }

    private void initPlayer() {
        switch (this.mDialpadTouchToneType) {
            case 0:
                return;
            case 1:
                initDTMFToneGenerator();
                return;
            case 2:
                initMediaPlayer();
                initSoundPool();
                return;
            default:
                if (HwLog.HWFLOW) {
                    HwLog.i("DialpadTouchTonePlayer", "invalidate dialpad touch tone type: " + this.mDialpadTouchToneType);
                    return;
                }
                return;
        }
    }

    private void initMediaPlayer() {
        if (this.mMediaPlayer == null) {
            this.mMediaPlayer = new MediaPlayer();
        }
    }

    private void initSoundPool() {
        if (this.mSoundPoolRunnable == null) {
            this.mExecutorService = Executors.newCachedThreadPool();
            this.mSoundPoolRunnable = new SoundPoolRunnable(this.mContext, this.mSoundPoolLoadListener);
            this.mExecutorService.execute(this.mSoundPoolRunnable);
        }
    }

    private void initDTMFToneGenerator() {
        if (this.mToneGenerator == null) {
            new Thread() {
                public void run() {
                    synchronized (DialpadTouchTonePlayer.this.mSyncLock) {
                        try {
                            DialpadTouchTonePlayer.this.mToneGenerator = new ToneGenerator(8, 60);
                        } catch (RuntimeException e) {
                            HwLog.w("DialpadTouchTonePlayer", "Exception caught while creating local tone generator: " + e);
                            DialpadTouchTonePlayer.this.mToneGenerator = null;
                        }
                    }
                }
            }.start();
        }
    }

    public void refreshToneType() {
        ContentResolver cr = this.mContext.getContentResolver();
        int dtmfTone = System.getInt(cr, "dtmf_tone", 1);
        int customTone = System.getInt(cr, "dialpad_touch_tone_type", 1);
        if (dtmfTone != 0) {
            dtmfTone = customTone;
        }
        this.mDialpadTouchToneType = dtmfTone;
        initPlayer();
    }

    public void playback(int key, boolean vibrate, boolean isInfinite, HapticFeedback haptic) {
        int i = 4098;
        if (!(key < 0 || this.mTonePlayerHandler.hasMessages(4097) || this.mTonePlayerHandler.hasMessages(4098))) {
            Message msg = new Message();
            if (!isInfinite) {
                i = 4097;
            }
            msg.what = i;
            msg.arg1 = key;
            msg.obj = haptic;
            if (vibrate) {
                i = 1;
            } else {
                i = 0;
            }
            msg.arg2 = i;
            this.mTonePlayerHandler.sendMessage(msg);
        }
    }

    public void playVibrate(HapticFeedback haptic) {
        this.mTonePlayerHandler.removeMessages(4100);
        Message msg = this.mTonePlayerHandler.obtainMessage(4100);
        msg.obj = haptic;
        this.mTonePlayerHandler.sendMessage(msg);
    }

    public void stop() {
        Message msg = this.mTonePlayerHandler.obtainMessage();
        msg.what = 4099;
        this.mTonePlayerHandler.sendMessage(msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopInner() {
        if (this.mDialpadTouchToneType != 0) {
            synchronized (this.mSyncLock) {
                if (this.mToneGenerator == null) {
                    HwLog.w("DialpadTouchTonePlayer", "stopTone: mToneGenerator == null");
                    return;
                }
                this.mToneGenerator.stopTone();
            }
        }
    }

    public void release(boolean quitPlayerThread) {
        new Thread(new Runnable() {
            public void run() {
                synchronized (DialpadTouchTonePlayer.this.mSyncLock) {
                    if (DialpadTouchTonePlayer.this.mToneGenerator != null) {
                        DialpadTouchTonePlayer.this.mToneGenerator.release();
                        DialpadTouchTonePlayer.this.mToneGenerator = null;
                    }
                }
            }
        }).start();
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        synchronized (this.mSyncLock) {
            if (this.mExecutorService != null) {
                this.mSoundPoolRunnable.cancel();
                this.mSoundPoolRunnable = null;
                this.mExecutorService.shutdown();
                this.mExecutorService = null;
            }
            if (this.mSoundPool != null) {
                this.mSoundPool.release();
                this.mSoundPool = null;
            }
            if (this.mSoundPoolIdSet != null) {
                this.mSoundPoolIdSet.clear();
            }
        }
        if (quitPlayerThread) {
            this.mTonePlayerThread.quit();
        }
    }

    private boolean isSilentMode() {
        int ringerMode = 0;
        if (this.mAudioMgr == null && this.mContext != null) {
            this.mAudioMgr = (AudioManager) this.mContext.getSystemService("audio");
        }
        if (this.mAudioMgr != null) {
            ringerMode = this.mAudioMgr.getRingerMode();
        }
        return ringerMode == 0 || ringerMode == 1;
    }
}
