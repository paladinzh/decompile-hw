package android.media;

import android.util.Log;

public class HwMediaRecorderDummy implements IHwMediaRecorder {
    private static final String TAG = "HwMediaRecorderDummy";
    private static IHwMediaRecorder mHwMediaRecoder = new HwMediaRecorderDummy();
    IAudioService mAudioService = null;

    private HwMediaRecorderDummy() {
    }

    public static IHwMediaRecorder getDefault() {
        return mHwMediaRecoder;
    }

    public void sendStateChangedIntent(int state) {
        Log.w(TAG, "dummy sendStateChangedIntent, state=" + state);
    }

    public void checkRecordActive() {
        Log.w(TAG, "dummy checkRecordActive");
    }
}
