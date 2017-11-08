package com.android.dialer.voicemail;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.telecom.CallAudioState;
import android.util.Log;
import com.android.contacts.hap.CommonUtilMethods;
import java.util.concurrent.RejectedExecutionException;

final class VoicemailAudioManager implements OnAudioFocusChangeListener, Listener {
    private static final String TAG = VoicemailAudioManager.class.getSimpleName();
    private AudioManager mAudioManager;
    private CallAudioState mCallAudioState = getInitialAudioState();
    private VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;
    private boolean mWasSpeakerOn;
    private WiredHeadsetManager mWiredHeadsetManager;

    public VoicemailAudioManager(Context context, VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;
        this.mWiredHeadsetManager = new WiredHeadsetManager(context);
        this.mWiredHeadsetManager.setListener(this);
        Log.i(TAG, "Initial audioState = " + this.mCallAudioState);
    }

    public void requestAudioFocus() {
        if (this.mAudioManager.requestAudioFocus(this, 0, 2) != 1) {
            throw new RejectedExecutionException("Could not capture audio focus.");
        }
    }

    public void abandonAudioFocus() {
        this.mAudioManager.abandonAudioFocus(this);
    }

    public void onAudioFocusChange(int focusChange) {
        boolean z = true;
        Log.d(TAG, "onAudioFocusChange: focusChange=" + focusChange);
        VoicemailPlaybackPresenter voicemailPlaybackPresenter = this.mVoicemailPlaybackPresenter;
        if (focusChange != 1) {
            z = false;
        }
        voicemailPlaybackPresenter.onAudioFocusChange(z);
    }

    public void onWiredHeadsetPluggedInChanged(boolean oldIsPluggedIn, boolean newIsPluggedIn) {
        boolean z = true;
        Log.i(TAG, "wired headset was plugged in changed: " + oldIsPluggedIn + " -> " + newIsPluggedIn);
        if (oldIsPluggedIn != newIsPluggedIn && this.mVoicemailPlaybackPresenter.isPlaying()) {
            int newRoute;
            if (newIsPluggedIn) {
                newRoute = 4;
                if (!CommonUtilMethods.getIsHaveEarpiece()) {
                    this.mVoicemailPlaybackPresenter.setSpeakerphoneEnable(true);
                }
            } else {
                if (!CommonUtilMethods.getIsHaveEarpiece()) {
                    this.mVoicemailPlaybackPresenter.setSpeakerphoneEnable(false);
                }
                newRoute = this.mWasSpeakerOn ? 8 : CommonUtilMethods.getIsHaveEarpiece() ? 1 : 8;
            }
            VoicemailPlaybackPresenter voicemailPlaybackPresenter = this.mVoicemailPlaybackPresenter;
            if (newRoute != 8) {
                z = false;
            }
            voicemailPlaybackPresenter.setSpeakerphoneOn(z);
            setSystemAudioState(new CallAudioState(false, newRoute, calculateSupportedRoutes()));
        }
    }

    public void onHeadsetPluggedBecomingNoisy() {
        if (this.mVoicemailPlaybackPresenter.isPlaying()) {
            this.mVoicemailPlaybackPresenter.pausePlayback();
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        setAudioRoute(on ? 8 : 5);
    }

    public boolean isWiredHeadsetPluggedIn() {
        return this.mWiredHeadsetManager.isPluggedIn();
    }

    public void registerReceivers() {
        this.mWiredHeadsetManager.registerReceiver();
    }

    public void unregisterReceivers() {
        this.mWiredHeadsetManager.unregisterReceiver();
    }

    void setAudioRoute(int route) {
        Log.v(TAG, "setAudioRoute, route: " + CallAudioState.audioRouteToString(route));
        int newRoute = selectWiredOrEarpiece(route, this.mCallAudioState.getSupportedRouteMask());
        if ((this.mCallAudioState.getSupportedRouteMask() | newRoute) == 0) {
            Log.w(TAG, "Asking to set to a route that is unsupported: " + newRoute);
            return;
        }
        if (this.mCallAudioState.getRoute() != newRoute) {
            this.mWasSpeakerOn = newRoute == 8;
            setSystemAudioState(new CallAudioState(false, newRoute, this.mCallAudioState.getSupportedRouteMask()));
        }
    }

    private CallAudioState getInitialAudioState() {
        int supportedRouteMask = calculateSupportedRoutes();
        return new CallAudioState(false, selectWiredOrEarpiece(5, supportedRouteMask), supportedRouteMask);
    }

    private int calculateSupportedRoutes() {
        if (this.mWiredHeadsetManager.isPluggedIn()) {
            return 12;
        }
        if (CommonUtilMethods.getIsHaveEarpiece()) {
            return 9;
        }
        return 8;
    }

    private int selectWiredOrEarpiece(int route, int supportedRouteMask) {
        if (route != 5) {
            return route;
        }
        route = supportedRouteMask & 5;
        if (route != 0) {
            return route;
        }
        Log.wtf(TAG, "One of wired headset or earpiece should always be valid.");
        if (CommonUtilMethods.getIsHaveEarpiece()) {
            return 1;
        }
        return 8;
    }

    private void setSystemAudioState(CallAudioState callAudioState) {
        CallAudioState oldAudioState = this.mCallAudioState;
        this.mCallAudioState = callAudioState;
        Log.i(TAG, "setSystemAudioState: changing from " + oldAudioState + " to " + this.mCallAudioState);
        if (this.mCallAudioState.getRoute() == 8) {
            turnOnSpeaker(true);
        } else if (this.mCallAudioState.getRoute() == 1 || this.mCallAudioState.getRoute() == 4) {
            turnOnSpeaker(false);
        }
    }

    private void turnOnSpeaker(boolean on) {
        if (this.mAudioManager.isSpeakerphoneOn() != on) {
            Log.i(TAG, "turning speaker phone on: " + on);
            this.mAudioManager.setSpeakerphoneOn(on);
        }
    }
}
