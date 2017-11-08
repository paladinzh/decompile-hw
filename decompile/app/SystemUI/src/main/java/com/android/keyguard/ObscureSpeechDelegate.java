package com.android.keyguard;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

class ObscureSpeechDelegate extends AccessibilityDelegate {
    private static boolean sAnnouncedHeadset = false;
    private final AudioManager mAudioManager;
    private final ContentResolver mContentResolver;

    public ObscureSpeechDelegate(Context context) {
        this.mContentResolver = context.getContentResolver();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    public static synchronized void setAnnouncedHeadset(boolean announcedHeadsetValue) {
        synchronized (ObscureSpeechDelegate.class) {
            sAnnouncedHeadset = announcedHeadsetValue;
        }
    }

    public void sendAccessibilityEvent(View host, int eventType) {
        super.sendAccessibilityEvent(host, eventType);
        if (eventType == 32768 && !sAnnouncedHeadset && shouldObscureSpeech()) {
            sAnnouncedHeadset = true;
            host.announceForAccessibility(host.getContext().getString(17040553));
        }
    }

    public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(host, event);
        if (event.getEventType() != 16384 && shouldObscureSpeech()) {
            event.getText().clear();
            event.setContentDescription(host.getContext().getString(17040554));
        }
    }

    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        if (shouldObscureSpeech()) {
            Context ctx = host.getContext();
            info.setText(null);
            info.setContentDescription(ctx.getString(17040554));
        }
    }

    private boolean shouldObscureSpeech() {
        if (Secure.getIntForUser(this.mContentResolver, "speak_password", 0, -2) != 0 || this.mAudioManager.isWiredHeadsetOn() || this.mAudioManager.isBluetoothA2dpOn()) {
            return false;
        }
        return true;
    }
}
