package com.android.dialer.voicemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

class WiredHeadsetManager {
    private static final String TAG = WiredHeadsetManager.class.getSimpleName();
    private Context mContext;
    private boolean mIsPluggedIn;
    private Listener mListener;
    private final WiredHeadsetBroadcastReceiver mReceiver = new WiredHeadsetBroadcastReceiver();

    interface Listener {
        void onHeadsetPluggedBecomingNoisy();

        void onWiredHeadsetPluggedInChanged(boolean z, boolean z2);
    }

    private class WiredHeadsetBroadcastReceiver extends BroadcastReceiver {
        private WiredHeadsetBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.HEADSET_PLUG".equals(intent.getAction())) {
                boolean isPluggedIn = intent.getIntExtra("state", 0) == 1;
                Log.v(WiredHeadsetManager.TAG, "ACTION_HEADSET_PLUG event, plugged in: " + isPluggedIn);
                WiredHeadsetManager.this.onHeadsetPluggedInChanged(isPluggedIn);
            } else if ("android.media.AUDIO_BECOMING_NOISY".equals(intent.getAction())) {
                WiredHeadsetManager.this.onHeadsetPluggedBecomingNoisy();
            }
        }
    }

    WiredHeadsetManager(Context context) {
        this.mContext = context;
        this.mIsPluggedIn = ((AudioManager) context.getSystemService("audio")).isWiredHeadsetOn();
    }

    void setListener(Listener listener) {
        this.mListener = listener;
    }

    boolean isPluggedIn() {
        return this.mIsPluggedIn;
    }

    void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    private void onHeadsetPluggedInChanged(boolean isPluggedIn) {
        if (this.mIsPluggedIn != isPluggedIn) {
            Log.v(TAG, "onHeadsetPluggedInChanged, mIsPluggedIn: " + this.mIsPluggedIn + " -> " + isPluggedIn);
            boolean oldIsPluggedIn = this.mIsPluggedIn;
            this.mIsPluggedIn = isPluggedIn;
            if (this.mListener != null) {
                this.mListener.onWiredHeadsetPluggedInChanged(oldIsPluggedIn, this.mIsPluggedIn);
            }
        }
    }

    private void onHeadsetPluggedBecomingNoisy() {
        if (this.mListener != null) {
            this.mListener.onHeadsetPluggedBecomingNoisy();
        }
    }
}
