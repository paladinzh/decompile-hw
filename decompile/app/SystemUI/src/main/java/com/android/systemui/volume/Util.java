package com.android.systemui.volume;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController.PlaybackInfo;
import android.media.session.PlaybackState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

class Util {
    private static int[] AUDIO_MANAGER_FLAGS = new int[]{1, 16, 4, 2, 8, 2048, 128, 4096, 1024};
    private static String[] AUDIO_MANAGER_FLAG_NAMES = new String[]{"SHOW_UI", "VIBRATE", "PLAY_SOUND", "ALLOW_RINGER_MODES", "REMOVE_SOUND_AND_VIBRATE", "SHOW_VIBRATE_HINT", "SHOW_SILENT_HINT", "FROM_KEY", "SHOW_UI_WARNINGS"};
    private static final SimpleDateFormat HMMAA = new SimpleDateFormat("h:mm aa", Locale.US);

    private static java.lang.String bitFieldToString(int r1, int[] r2, java.lang.String[] r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.systemui.volume.Util.bitFieldToString(int, int[], java.lang.String[]):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.volume.Util.bitFieldToString(int, int[], java.lang.String[]):java.lang.String");
    }

    Util() {
    }

    public static String logTag(Class<?> c) {
        String tag = "vol." + c.getSimpleName();
        return tag.length() < 23 ? tag : tag.substring(0, 23);
    }

    public static String ringerModeToString(int ringerMode) {
        switch (ringerMode) {
            case 0:
                return "RINGER_MODE_SILENT";
            case 1:
                return "RINGER_MODE_VIBRATE";
            case 2:
                return "RINGER_MODE_NORMAL";
            default:
                return "RINGER_MODE_UNKNOWN_" + ringerMode;
        }
    }

    public static String mediaMetadataToString(MediaMetadata metadata) {
        return metadata.getDescription().toString();
    }

    public static String playbackInfoToString(PlaybackInfo info) {
        if (info == null) {
            return null;
        }
        String type = playbackInfoTypeToString(info.getPlaybackType());
        String vc = volumeProviderControlToString(info.getVolumeControl());
        return String.format("PlaybackInfo[vol=%s,max=%s,type=%s,vc=%s],atts=%s", new Object[]{Integer.valueOf(info.getCurrentVolume()), Integer.valueOf(info.getMaxVolume()), type, vc, info.getAudioAttributes()});
    }

    public static String playbackInfoTypeToString(int type) {
        switch (type) {
            case 1:
                return "LOCAL";
            case 2:
                return "REMOTE";
            default:
                return "UNKNOWN_" + type;
        }
    }

    public static String playbackStateStateToString(int state) {
        switch (state) {
            case 0:
                return "STATE_NONE";
            case 1:
                return "STATE_STOPPED";
            case 2:
                return "STATE_PAUSED";
            case 3:
                return "STATE_PLAYING";
            default:
                return "UNKNOWN_" + state;
        }
    }

    public static String volumeProviderControlToString(int control) {
        switch (control) {
            case 0:
                return "VOLUME_CONTROL_FIXED";
            case 1:
                return "VOLUME_CONTROL_RELATIVE";
            case 2:
                return "VOLUME_CONTROL_ABSOLUTE";
            default:
                return "VOLUME_CONTROL_UNKNOWN_" + control;
        }
    }

    public static String playbackStateToString(PlaybackState playbackState) {
        if (playbackState == null) {
            return null;
        }
        return playbackStateStateToString(playbackState.getState()) + " " + playbackState;
    }

    public static String audioManagerFlagsToString(int value) {
        return bitFieldToString(value, AUDIO_MANAGER_FLAGS, AUDIO_MANAGER_FLAG_NAMES);
    }

    private static CharSequence emptyToNull(CharSequence str) {
        return (str == null || str.length() == 0) ? null : str;
    }

    public static boolean setText(TextView tv, CharSequence text) {
        if (Objects.equals(emptyToNull(tv.getText()), emptyToNull(text))) {
            return false;
        }
        tv.setText(text);
        return true;
    }

    public static final void setVisOrGone(View v, boolean vis) {
        int i = 0;
        if (v != null) {
            if ((v.getVisibility() == 0) != vis) {
                if (!vis) {
                    i = 8;
                }
                v.setVisibility(i);
            }
        }
    }

    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        return telephony != null ? telephony.isVoiceCapable() : false;
    }
}
