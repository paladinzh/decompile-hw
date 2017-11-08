package com.android.mms.attachment.ui.mediapicker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.ImageView;
import com.android.mms.MmsApp;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.model.AudioModel;
import com.android.mms.model.AudioModel.AudioManagerFocusCallback;
import com.android.rcs.ui.RcsMessageUtils;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecorderManager implements AudioManagerFocusCallback {
    private static RecorderManager sRecorderManager;
    public ImageView mAudioAnimaImageView;
    private AnimationDrawable mAudioAnimationDrawable;
    private AudioModel mAudioModel;
    private MediaPlayer mAudioPlayer;
    private Handler mAudioStophandler = null;
    private boolean mIsPlaying = false;
    private ArrayList<Callback> mRecordCallbacks;
    private Handler mRecordControlHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    RecorderManager.this.notifyAllOnTimerChange((long) (RecorderManager.this.getRecordDuration() / 1000));
                    RecorderManager.this.mRecordControlHandler.removeMessages(100);
                    sendEmptyMessageDelayed(100, 50);
                    if (RemainingTimeCalculator.getInstance().timeRemaining() < 0) {
                        RecorderManager.this.notifyAllOnStorageFull(RemainingTimeCalculator.getInstance().currentLowerLimit());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    MediaRecorder mRecorder = null;
    File mSampleFile = null;
    int mSampleLength = 0;
    long mSampleStart = 0;
    int mState = 0;
    private StopPlayBroadcaseReceiver mStopPlayBroadcaseReceiver = new StopPlayBroadcaseReceiver();

    public interface Callback {
        void onError(int i);

        void onMemoryFull(int i);

        void onTimerChange(long j);
    }

    private class StopAudioHandler extends Handler {
        private String mAudioName;
        private int mPlayerObjId;

        public StopAudioHandler(int playerid, String audioname) {
            this.mAudioName = audioname;
            this.mPlayerObjId = playerid;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (RecorderManager.this.mAudioPlayer != null && RecorderManager.this.mAudioPlayer.isPlaying() && RecorderManager.this.mAudioPlayer.hashCode() == this.mPlayerObjId && RecorderManager.this.mAudioModel != null) {
                        String src = RecorderManager.this.mAudioModel.getSrc();
                        if (!((src == null && this.mAudioName == null) || src == null)) {
                            if (!src.equals(this.mAudioName)) {
                                return;
                            }
                        }
                        RecorderManager.this.stopAudio();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    class StopPlayBroadcaseReceiver extends BroadcastReceiver {
        StopPlayBroadcaseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || TextUtils.isEmpty(intent.getAction()) || !TextUtils.equals(intent.getAction(), "ACTION_COMPOSERESUME_STOPPLAYING"))) {
                RecorderManager.this.onAbandonAudioFocus();
            }
        }
    }

    public static RecorderManager getInstance(Context context) {
        RecorderManager recorderManager;
        synchronized (RecorderManager.class) {
            if (sRecorderManager == null) {
                sRecorderManager = new RecorderManager(context);
            }
            recorderManager = sRecorderManager;
        }
        return recorderManager;
    }

    public void initialRecorderManagerStatus(Context context) {
        this.mRecordCallbacks = new ArrayList();
        RemainingTimeCalculator.getInstance().setBitRate(5900);
    }

    public RecorderManager(Context context) {
        initialRecorderManagerStatus(context);
    }

    public void registerRecordCallback(Callback callback) {
        if (!this.mRecordCallbacks.contains(callback)) {
            this.mRecordCallbacks.add(callback);
        }
    }

    public void unregisterRecordCallback(Callback callback) {
        if (this.mRecordCallbacks.contains(callback)) {
            this.mRecordCallbacks.remove(callback);
        }
    }

    private synchronized void notifyAllOnErrorChange(int error) {
        for (Callback recordCAllback : this.mRecordCallbacks) {
            recordCAllback.onError(error);
        }
    }

    private synchronized void notifyAllOnTimerChange(long time) {
        MLog.i("RecorderManager", "notifyAllOnTimerChange, time = " + time);
        for (Callback recordCAllback : this.mRecordCallbacks) {
            recordCAllback.onTimerChange(time);
        }
    }

    private synchronized void notifyAllOnStorageFull(int type) {
        MLog.i("RecorderManager", "notifyAllOnStorageFull, type = " + type);
        for (Callback recordCAllback : this.mRecordCallbacks) {
            recordCAllback.onMemoryFull(type);
        }
    }

    public boolean isInPlayingstate() {
        return this.mIsPlaying;
    }

    public int sampleLength() {
        return this.mSampleLength / 1000;
    }

    public int sampleLengthMilliSecond() {
        return this.mSampleLength;
    }

    public void delete() {
        stop();
        if (!(this.mSampleFile == null || this.mSampleFile.delete())) {
            MLog.w("RecorderManager", "delete SampleFile failed");
        }
        this.mSampleFile = null;
        this.mSampleLength = 0;
    }

    public Uri getRecordUri() {
        return Uri.fromFile(this.mSampleFile);
    }

    public int getRecordDuration() {
        MediaPlayer mediaPlayer = null;
        try {
            mediaPlayer = MediaPlayer.create(MmsApp.getApplication().getApplicationContext(), getRecordUri());
            if (mediaPlayer == null) {
                return 0;
            }
            int duration = mediaPlayer.getDuration();
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            return duration;
        } finally {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        }
    }

    private boolean isAvailableSpace(Context mContext) {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return false;
        }
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long availableSpare = (((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize())) / 1024;
        MLog.d("RecorderManager", "availableSpare = " + availableSpare);
        if (availableSpare > 10240) {
            return true;
        }
        return false;
    }

    public void startRecording(int outputfileformat, String extension, Context context, long limitsize) {
        File sampleDir;
        stop();
        if (Environment.getExternalStorageDirectory().canWrite()) {
            sampleDir = new File(Environment.getExternalStorageDirectory() + "/Recordings");
            if (!(sampleDir.exists() || sampleDir.mkdirs())) {
                MLog.d("RecorderManager", "Audio create dir failed.");
            }
        } else {
            sampleDir = new File(Environment.getExternalStorageDirectory() + "/sdcard");
        }
        if (isAvailableSpace(context)) {
            try {
                this.mSampleFile = File.createTempFile("recording", extension, sampleDir);
                this.mRecorder = new MediaRecorder();
                this.mRecorder.setAudioSource(1);
                this.mRecorder.setOutputFormat(outputfileformat);
                this.mRecorder.setAudioEncoder(1);
                this.mRecorder.setOutputFile(this.mSampleFile.getAbsolutePath());
                try {
                    this.mRecorder.prepare();
                    try {
                        RemainingTimeCalculator.getInstance().reset();
                        if (limitsize > 0) {
                            RemainingTimeCalculator.getInstance().setFileSizeLimit(this.mSampleFile, limitsize);
                        }
                        this.mRecorder.start();
                        this.mSampleStart = System.currentTimeMillis();
                        this.mRecordControlHandler.sendEmptyMessage(100);
                        setState(1);
                        return;
                    } catch (RuntimeException e) {
                        AudioManager audioMngr = (AudioManager) context.getSystemService("audio");
                        boolean isInCall = audioMngr.getMode() != 2 ? audioMngr.getMode() == 3 : true;
                        if (isInCall) {
                            setError(3);
                        } else {
                            setError(2);
                        }
                        this.mRecorder.reset();
                        this.mRecorder.release();
                        this.mRecorder = null;
                        return;
                    }
                } catch (IOException e2) {
                    setError(2);
                    this.mRecorder.reset();
                    this.mRecorder.release();
                    this.mRecorder = null;
                    return;
                }
            } catch (IOException e3) {
                MLog.d("RecorderManager", "IOException e" + e3);
                setError(1);
                return;
            }
        }
        setError(5);
    }

    public void stopRecording(Context context) {
        if (this.mRecorder != null) {
            this.mRecordControlHandler.removeMessages(100);
            this.mRecorder.stop();
            this.mRecorder.release();
            this.mRecorder = null;
            if (context != null) {
                this.mSampleLength = RcsMessageUtils.getAudioFileDuration(context, this.mSampleFile);
            } else {
                this.mSampleLength = (int) (System.currentTimeMillis() - this.mSampleStart);
            }
            setState(0);
        }
    }

    public void stopRecording() {
        stopRecording(null);
    }

    public void stop() {
        stopRecording();
    }

    private void setState(int state) {
        if (state != this.mState) {
            this.mState = state;
        }
    }

    public int getState() {
        return this.mState;
    }

    private void setError(int error) {
        notifyAllOnErrorChange(error);
    }

    public void setAudioModel(AudioModel audioModel) {
        this.mAudioModel = audioModel;
        this.mAudioModel.setAudioManagerFocusCallback(this);
    }

    public void setAudioUri(Uri uri) {
        try {
            this.mAudioModel = new AudioModel(MmsApp.getApplication().getApplicationContext(), uri);
            this.mAudioModel.setAudioManagerFocusCallback(this);
        } catch (MmsException e) {
            MLog.e("RecorderManager", "setAudioUri MmsException");
        } catch (UnsupportContentTypeException e2) {
            MLog.e("RecorderManager", "setAudioUri UnsupportContentTypeException");
        }
        this.mAudioPlayer = MediaPlayer.create(MmsApp.getApplication().getApplicationContext(), uri);
    }

    public void playAudio() {
        registerStopPlayBroadcaseReceiver();
        if (this.mAudioPlayer != null) {
            if (this.mAudioPlayer.isPlaying()) {
                this.mAudioPlayer.stop();
                this.mAudioPlayer.release();
                if (this.mAudioModel != null) {
                    this.mAudioModel.abandonAudioFocus();
                }
            }
            this.mAudioPlayer = null;
        }
        if (this.mAudioModel != null) {
            try {
                this.mAudioPlayer = MediaPlayer.create(MmsApp.getApplication().getApplicationContext(), this.mAudioModel.getUri());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.mAudioPlayer != null) {
                this.mAudioPlayer.setAudioStreamType(3);
                this.mAudioPlayer.setLooping(false);
                this.mAudioPlayer.setOnCompletionListener(new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        RecorderManager.this.stopAudioAnimation();
                    }
                });
                this.mAudioPlayer.start();
                this.mAudioModel.requestAudioFocus();
                startAudioAnimation();
                if (this.mAudioStophandler != null) {
                    this.mAudioStophandler.removeMessages(2);
                    this.mAudioStophandler = null;
                }
                this.mAudioStophandler = new StopAudioHandler(this.mAudioPlayer.hashCode(), this.mAudioModel.getSrc());
                this.mAudioStophandler.sendMessageDelayed(this.mAudioStophandler.obtainMessage(2), (long) this.mAudioModel.getDuration());
            }
            this.mIsPlaying = true;
            return;
        }
        MLog.e("RecorderManager", "media of the mediaitem is null");
    }

    public void stopAudio() {
        unregisterStopPlayBroadcastReceiver();
        if (this.mAudioPlayer == null || !this.mAudioPlayer.isPlaying()) {
            MLog.e("RecorderManager", " mediaplayer is null or mediaplay is not playing");
        } else {
            this.mAudioPlayer.stop();
            this.mAudioPlayer.release();
            stopAudioAnimation();
            this.mIsPlaying = false;
        }
        if (this.mAudioModel != null) {
            this.mAudioModel.abandonAudioFocus();
        }
        this.mAudioPlayer = null;
    }

    private void startAudioAnimation() {
        this.mIsPlaying = true;
        if (this.mAudioAnimationDrawable == null && this.mAudioAnimaImageView != null) {
            this.mAudioAnimationDrawable = (AnimationDrawable) this.mAudioAnimaImageView.getBackground();
        }
        if (this.mAudioAnimationDrawable != null) {
            this.mAudioAnimationDrawable.start();
        }
    }

    private void stopAudioAnimation() {
        this.mIsPlaying = false;
        if (this.mAudioAnimationDrawable == null && this.mAudioAnimaImageView != null) {
            this.mAudioAnimationDrawable = (AnimationDrawable) this.mAudioAnimaImageView.getBackground();
        }
        if (this.mAudioAnimationDrawable != null) {
            this.mAudioAnimationDrawable.selectDrawable(0);
            this.mAudioAnimationDrawable.stop();
        }
    }

    public void onAbandonAudioFocus() {
        stopAudio();
        stopAudioAnimation();
    }

    private void registerStopPlayBroadcaseReceiver() {
        LocalBroadcastManager.getInstance(MmsApp.getApplication().getApplicationContext()).registerReceiver(this.mStopPlayBroadcaseReceiver, new IntentFilter("ACTION_COMPOSERESUME_STOPPLAYING"));
    }

    private void unregisterStopPlayBroadcastReceiver() {
        LocalBroadcastManager.getInstance(MmsApp.getApplication().getApplicationContext()).unregisterReceiver(this.mStopPlayBroadcaseReceiver);
    }

    public void clearCallbacks() {
        if (this.mRecordCallbacks != null) {
            this.mRecordCallbacks.clear();
        }
    }
}
