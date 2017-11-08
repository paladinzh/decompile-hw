package com.huawei.android.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.MSimTelephonyConstants;
import android.util.Log;

public class AudioManagerEx {
    private static final int ENABLE_VOLUME_ADJUST_TOKEN = 1102;
    private static final int IS_ADJUST_VOLUME_ENABLE_TOKEN = 1101;
    public static final int STREAM_FM;
    public static final int STREAM_INCALL_MUSIC = 3;
    public static final int STREAM_VOICE_HELPER = 11;
    private static final String TAG = "AudioManagerEx";

    static {
        boolean supportFmStream = true;
        try {
            AudioSystem.class.getDeclaredField("STREAM_FM");
        } catch (NoSuchFieldException e) {
            supportFmStream = false;
        }
        if (supportFmStream) {
            STREAM_FM = 10;
        } else {
            STREAM_FM = 3;
        }
        Log.i(TAG, "STREAM_FM = " + STREAM_FM);
    }

    public static void setSpeakermediaOn(Context context, boolean on) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (on) {
            data.writeInt(1);
        } else {
            data.writeInt(0);
        }
        try {
            ServiceManager.getService("audio").transact(101, data, reply, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on setSpeakermediaOn in exception....");
        }
    }

    public static boolean isFMActive(AudioManager am) {
        if (1 == AudioSystem.getDeviceConnectionState(1048576, MSimTelephonyConstants.MY_RADIO_PLATFORM)) {
            return true;
        }
        return false;
    }

    public static boolean isAdjuseVolumeEnable() {
        int ret = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            ServiceManager.getService("audio").transact(1101, data, reply, 0);
            reply.readException();
            ret = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "add-on isAdjuseVolumeEnable in exception....", e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    public static void enableVolumeAdjust(boolean enable) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeStrongBinder(new Binder());
        if (enable) {
            data.writeInt(1);
        } else {
            data.writeInt(0);
        }
        try {
            ServiceManager.getService("audio").transact(ENABLE_VOLUME_ADJUST_TOKEN, data, reply, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "add-on enableVolumeAdjust in exception....");
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
