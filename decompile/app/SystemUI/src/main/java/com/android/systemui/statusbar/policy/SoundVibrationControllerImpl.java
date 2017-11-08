package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Log;
import com.android.systemui.statusbar.policy.SoundVibrationController.RingModeChangeCallback;
import java.util.ArrayList;

public class SoundVibrationControllerImpl extends BroadcastReceiver implements SoundVibrationController {
    private AudioManager mAudioManager;
    Context mContext;
    private final H mHandler = new H();
    private boolean mIsVibrationEnable;
    private int mRingMode;
    private ArrayList<RingModeChangeCallback> mRingModeChangeCallbacks = new ArrayList();
    private Object mSyncObject = new Object();
    private ContentObserver mVibrationStateChangedObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            SoundVibrationControllerImpl.this.updateVibrationState();
        }
    };

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ringModeChanged();
                    return;
                default:
                    return;
            }
        }

        private void ringModeChanged() {
            int ringMode = SoundVibrationControllerImpl.this.getRingMode();
            boolean vibrationEnable = SoundVibrationControllerImpl.this.isVibrationEnable();
            ArrayList<RingModeChangeCallback> tempList = new ArrayList();
            synchronized (SoundVibrationControllerImpl.this.mSyncObject) {
                tempList.addAll(SoundVibrationControllerImpl.this.mRingModeChangeCallbacks);
            }
            for (RingModeChangeCallback cb : tempList) {
                cb.onRingModeChanged(ringMode, vibrationEnable);
            }
        }
    }

    public SoundVibrationControllerImpl(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        init();
    }

    private void init() {
        boolean z;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.RINGER_MODE_CHANGED");
        filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        this.mContext.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("vibirate_when_silent"), true, this.mVibrationStateChangedObserver);
        this.mRingMode = this.mAudioManager.getRingerModeInternal();
        if (1 == Global.getInt(this.mContext.getContentResolver(), "vibirate_when_silent", 0)) {
            z = true;
        } else {
            z = false;
        }
        this.mIsVibrationEnable = z;
    }

    public void addRingModeChangedCallback(RingModeChangeCallback callback) {
        synchronized (this.mSyncObject) {
            this.mRingModeChangeCallbacks.add(callback);
        }
        this.mHandler.sendEmptyMessage(1);
    }

    public void removeRingModeChangedCallback(RingModeChangeCallback callback) {
        synchronized (this.mSyncObject) {
            this.mRingModeChangeCallbacks.remove(callback);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.e("SoundVibrationControllerImpl", "onReceive: intent or action is null!");
            return;
        }
        String action = intent.getAction();
        if ("android.media.RINGER_MODE_CHANGED".equals(action) || "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(action)) {
            updateRingMode();
        }
    }

    public int getRingMode() {
        return this.mRingMode;
    }

    public void setRingMode(int mode) {
        this.mRingMode = mode;
        this.mAudioManager.setRingerModeInternal(mode);
    }

    private void updateRingMode() {
        this.mRingMode = this.mAudioManager.getRingerModeInternal();
        if (1 == this.mRingMode) {
            setVibrationState(true);
        }
        this.mHandler.sendEmptyMessage(1);
    }

    public boolean isVibrationEnable() {
        return this.mIsVibrationEnable;
    }

    private void updateVibrationState() {
        boolean z = false;
        if (1 == Global.getInt(this.mContext.getContentResolver(), "vibirate_when_silent", 0)) {
            z = true;
        }
        this.mIsVibrationEnable = z;
        this.mHandler.sendEmptyMessage(1);
    }

    public void setVibrationState(boolean enable) {
        Global.putInt(this.mContext.getContentResolver(), "vibirate_when_silent", enable ? 1 : 0);
    }
}
