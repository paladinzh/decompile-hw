package com.android.dialer.greeting.presenter;

import android.content.Context;
import android.media.AudioSystem;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Bundle;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.HwLog;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RecordPresenter implements OnErrorListener {
    private static final String TAG = RecordPresenter.class.getSimpleName();
    private static ScheduledExecutorService mScheduledExecutorService;
    private static RecordPresenter sInstance;
    private Context mContext;
    private int mInitialOrientation;
    private String mRecordFile;
    private int mRecordPosition;
    private MediaRecorder mRecorder;
    private RecordView mView;

    private class DeleteRecordFileTask implements Runnable {
        private DeleteRecordFileTask() {
        }

        public void run() {
            if (RecordPresenter.this.mRecordFile != null) {
                File tmpFile = new File(RecordPresenter.this.mRecordFile);
                if (tmpFile.exists()) {
                    HwLog.d(RecordPresenter.TAG, "DeleteRecordFileTask , delete success : " + tmpFile.delete());
                }
            }
        }
    }

    public interface RecordView {
        void onRecordAbort();

        void onRecordStarted(int i, ScheduledExecutorService scheduledExecutorService);

        void onRecordStop(String str, int i);

        void onShowError();

        void setClipPosition(int i, int i2);

        void setPresenter(RecordPresenter recordPresenter);
    }

    public static synchronized RecordPresenter getInstance(Context context, Bundle savedInstanceState) {
        RecordPresenter recordPresenter;
        synchronized (RecordPresenter.class) {
            if (sInstance == null) {
                sInstance = new RecordPresenter();
            }
            sInstance.init(context, savedInstanceState);
            recordPresenter = sInstance;
        }
        return recordPresenter;
    }

    protected void init(Context context, Bundle savedInstanceState) {
        this.mContext = context;
        if (this.mContext != null) {
            this.mInitialOrientation = this.mContext.getResources().getConfiguration().orientation;
        }
        if (savedInstanceState != null) {
            this.mRecordPosition = savedInstanceState.getInt("RECORD_POSITION");
        }
    }

    public void setRecordView(RecordView view) {
        this.mView = view;
        this.mView.setPresenter(this);
        if (this.mRecorder != null) {
            this.mView.setClipPosition(this.mRecordPosition, 450);
            this.mView.onRecordStarted(450, getScheduledExecutorServiceInstance());
            return;
        }
        this.mView.setClipPosition(0, 450);
    }

    public void startRecording() {
        if (this.mContext != null && !checkSystemAudio()) {
            if (this.mRecorder != null) {
                stopRecorder();
            }
            this.mRecordFile = getRecordFilePath();
            this.mRecorder = new MediaRecorder();
            try {
                this.mRecorder.setAudioSource(1);
                this.mRecorder.setOutputFormat(1);
                this.mRecorder.setOutputFile(this.mRecordFile);
                this.mRecorder.setAudioEncoder(1);
                this.mRecorder.prepare();
                this.mRecorder.start();
                this.mRecordPosition = 0;
                this.mView.onRecordStarted(450, getScheduledExecutorServiceInstance());
            } catch (IOException e) {
                HwLog.e(TAG, "mRecorder IOException");
            } catch (IllegalStateException e2) {
                HwLog.e(TAG, "mRecorder IllegalStateException");
            }
        }
    }

    private boolean checkSystemAudio() {
        if (!isSourceActive(1) && !isSourceActive(4) && !isSourceActive(5) && !isSourceActive(6) && !isSourceActive(7)) {
            return false;
        }
        this.mView.onShowError();
        return true;
    }

    private static boolean isSourceActive(int source) {
        return AudioSystem.isSourceActive(source);
    }

    public void stopRecording() {
        if (this.mRecorder != null) {
            stopRecorder();
        }
        this.mView.onRecordStop(this.mRecordFile, this.mRecordPosition / 30);
    }

    public void onPause() {
        if (this.mContext == null || this.mInitialOrientation == this.mContext.getResources().getConfiguration().orientation) {
            pausePresenter();
        } else {
            HwLog.d(TAG, "onPause: Orientation changed.");
        }
    }

    public void pausePresenter() {
        if (this.mRecorder != null) {
            stopRecorder();
            this.mView.onRecordAbort();
            deleteRecordFileInbackground();
        }
    }

    private void stopRecorder() {
        this.mRecorder.setOnErrorListener(null);
        this.mRecorder.setOnInfoListener(null);
        this.mRecorder.setPreviewDisplay(null);
        try {
            this.mRecorder.stop();
            this.mRecorder.reset();
            this.mRecorder.release();
        } catch (RuntimeException e) {
            onError(this.mRecorder, 0, 0);
        }
        this.mRecorder = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mView != null) {
            outState.putInt("RECORD_POSITION", this.mRecordPosition);
        }
    }

    public void onDestroy() {
        this.mContext = null;
        destoryExecutor();
    }

    private static synchronized void destoryExecutor() {
        synchronized (RecordPresenter.class) {
            if (mScheduledExecutorService != null) {
                mScheduledExecutorService.shutdown();
                mScheduledExecutorService = null;
            }
        }
    }

    public boolean isRecording() {
        return this.mRecorder != null;
    }

    public int getRecordPosition() {
        if (this.mRecorder == null) {
            return 0;
        }
        int i = this.mRecordPosition + 1;
        this.mRecordPosition = i;
        return i;
    }

    private String getRecordFilePath() {
        File rootFile = ContactPhotoUtils.getRootFilePath(this.mContext);
        return rootFile == null ? "" : rootFile.getAbsolutePath() + "/greeting_tmp.3gp";
    }

    private static synchronized ScheduledExecutorService getScheduledExecutorServiceInstance() {
        ScheduledExecutorService scheduledExecutorService;
        synchronized (RecordPresenter.class) {
            if (mScheduledExecutorService == null) {
                mScheduledExecutorService = Executors.newScheduledThreadPool(2);
            }
            scheduledExecutorService = mScheduledExecutorService;
        }
        return scheduledExecutorService;
    }

    private void deleteRecordFileInbackground() {
        getScheduledExecutorServiceInstance().execute(new DeleteRecordFileTask());
    }

    public void onError(MediaRecorder mr, int what, int extra) {
        if (mr != null) {
            try {
                mr.reset();
            } catch (IllegalStateException e) {
                HwLog.e(TAG, "stopRecord" + e);
            } catch (Exception e2) {
                HwLog.e(TAG, "stopRecord" + e2);
            }
        }
        this.mRecorder = null;
    }
}
