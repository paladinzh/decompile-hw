package com.android.dialer.voicemail;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.common.io.MoreCloseables;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@VisibleForTesting
@NotThreadSafe
public class VoicemailPlaybackPresenter implements OnPreparedListener, OnCompletionListener, OnErrorListener {
    private static final String CLIP_POSITION_KEY = (VoicemailPlaybackPresenter.class.getName() + ".CLIP_POSITION_KEY");
    private static final String[] HAS_CONTENT_PROJECTION = new String[]{"has_content", "duration"};
    private static final String IS_FETCHING_KEY = (VoicemailPlaybackPresenter.class.getName() + ".IS_FETCHING_KEY");
    private static final String IS_PLAYING_STATE_KEY = (VoicemailPlaybackPresenter.class.getName() + ".IS_PLAYING_STATE_KEY");
    private static final String IS_PREPARED_KEY = (VoicemailPlaybackPresenter.class.getName() + ".IS_PREPARED");
    private static final String IS_SPEAKERPHONE_ON_KEY = (VoicemailPlaybackPresenter.class.getName() + ".IS_SPEAKER_PHONE_ON");
    private static final String IS_TIMEOUT_KEY = (VoicemailPlaybackPresenter.class.getName() + ".IS_TIMEOUT_KEY");
    private static final String VOICEMAIL_URI_KEY = (VoicemailPlaybackPresenter.class.getName() + ".VOICEMAIL_URI");
    private static ScheduledExecutorService mScheduledExecutorService;
    private static VoicemailPlaybackPresenter sInstance;
    private static VoicemailPlaybackPresenter sInstance2;
    private Activity mActivity;
    private final List<FetchResultHandler> mArchiveResultHandlers = new ArrayList();
    protected AsyncTaskExecutor mAsyncTaskExecutor;
    private AutoCollapseHandler mAutoCollaseHandler = new AutoCollapseHandler();
    protected Context mContext;
    private final AtomicInteger mDuration = new AtomicInteger(0);
    private FetchResultHandler mFetchResultHandler;
    private Handler mHandler = new Handler();
    private int mInitialOrientation;
    private boolean mIsFetching;
    private boolean mIsPlaying;
    private boolean mIsPrepared;
    private boolean mIsSpeakerphoneOn;
    private boolean mIsTimeOut;
    protected MediaPlayer mMediaPlayer;
    private OnAutoCollapseListener mOnAutoCollapseListener;
    private OnVoicemailDeletedListener mOnVoicemailDeletedListener;
    private int mPosition;
    private WakeLock mProximityWakeLock;
    private boolean mShouldResumePlaybackAfterSeeking;
    private PlaybackView mView;
    private VoicemailAudioManager mVoicemailAudioManager;
    protected Uri mVoicemailUri;

    public interface OnAutoCollapseListener {
        void onAutoCollapse();
    }

    public interface OnVoicemailDeletedListener {
        void onVoicemailDeleted(Uri uri);

        void onVoicemailDeletedInDatabase();
    }

    public interface PlaybackView {
        void enableUiElements();

        int getDesiredClipPosition();

        void onPlaybackError();

        void onPlaybackStarted(int i, ScheduledExecutorService scheduledExecutorService);

        void onPlaybackStopped();

        void onSpeakerphoneOn(boolean z);

        void resetSeekBar();

        void setClipPosition(int i, int i2);

        void setFetchContentTimeout();

        void setIsFetchingContent();

        void setPresenter(VoicemailPlaybackPresenter voicemailPlaybackPresenter, Uri uri, String str, String str2);

        void setSpeakerphoneEnable(boolean z);

        void setSuccess();
    }

    protected interface OnContentCheckedListener {
        void onContentChecked(boolean z);
    }

    private class AutoCollapseHandler extends Handler {
        private AutoCollapseHandler() {
        }

        public void handleMessage(Message msg) {
            if (VoicemailPlaybackPresenter.this.mOnAutoCollapseListener != null) {
                VoicemailPlaybackPresenter.this.mOnAutoCollapseListener.onAutoCollapse();
            }
        }

        private void start() {
            stop();
            sendEmptyMessageDelayed(0, 5000);
        }

        private void stop() {
            removeMessages(0);
        }
    }

    @ThreadSafe
    private class FetchResultHandler extends ContentObserver implements Runnable {
        private final Handler mFetchResultHandler;
        private AtomicBoolean mIsWaitingForResult = new AtomicBoolean(true);
        private final int mRequestCode;
        private final Uri mVoicemailUri;

        public FetchResultHandler(Handler handler, Uri uri, int code) {
            super(handler);
            this.mFetchResultHandler = handler;
            this.mRequestCode = code;
            this.mVoicemailUri = uri;
            if (VoicemailPlaybackPresenter.this.mContext != null) {
                VoicemailPlaybackPresenter.this.mContext.getContentResolver().registerContentObserver(this.mVoicemailUri, false, this);
                this.mFetchResultHandler.postDelayed(this, 20000);
                VoicemailPlaybackPresenter.this.mIsFetching = true;
            }
        }

        public void run() {
            if (this.mIsWaitingForResult.getAndSet(false) && VoicemailPlaybackPresenter.this.mContext != null) {
                VoicemailPlaybackPresenter.this.mContext.getContentResolver().unregisterContentObserver(this);
                VoicemailPlaybackPresenter.this.mIsFetching = false;
                if (VoicemailPlaybackPresenter.this.mView != null) {
                    VoicemailPlaybackPresenter.this.mView.setFetchContentTimeout();
                    VoicemailPlaybackPresenter.this.mIsTimeOut = true;
                }
            }
        }

        public void destroy() {
            if (this.mIsWaitingForResult.getAndSet(false) && VoicemailPlaybackPresenter.this.mContext != null) {
                VoicemailPlaybackPresenter.this.mContext.getContentResolver().unregisterContentObserver(this);
                this.mFetchResultHandler.removeCallbacks(this);
                VoicemailPlaybackPresenter.this.mIsFetching = false;
            }
        }

        public void onChange(boolean selfChange) {
            VoicemailPlaybackPresenter.this.mAsyncTaskExecutor.submit(Tasks.CHECK_CONTENT_AFTER_CHANGE, new AsyncTask<Void, Void, Boolean>() {
                public Boolean doInBackground(Void... params) {
                    return Boolean.valueOf(VoicemailPlaybackPresenter.this.queryHasContent(FetchResultHandler.this.mVoicemailUri));
                }

                public void onPostExecute(Boolean hasContent) {
                    if (hasContent.booleanValue() && VoicemailPlaybackPresenter.this.mContext != null && FetchResultHandler.this.mIsWaitingForResult.getAndSet(false)) {
                        VoicemailPlaybackPresenter.this.mContext.getContentResolver().unregisterContentObserver(FetchResultHandler.this);
                        VoicemailPlaybackPresenter.this.mIsFetching = false;
                        VoicemailPlaybackPresenter.this.prepareContent();
                        HwLog.d("VoicemailPlaybackPresenter", "FetchResultHandler:onChange , hasContent : " + hasContent + " , mRequestCode : " + FetchResultHandler.this.mRequestCode);
                    }
                }
            }, new Void[0]);
        }
    }

    public enum Tasks {
        CHECK_FOR_CONTENT,
        CHECK_CONTENT_AFTER_CHANGE
    }

    public static synchronized VoicemailPlaybackPresenter getInstance(Activity activity, Bundle savedInstanceState) {
        VoicemailPlaybackPresenter voicemailPlaybackPresenter;
        synchronized (VoicemailPlaybackPresenter.class) {
            if (sInstance == null) {
                sInstance = new VoicemailPlaybackPresenter(activity);
            }
            sInstance.init(activity, savedInstanceState);
            voicemailPlaybackPresenter = sInstance;
        }
        return voicemailPlaybackPresenter;
    }

    public static synchronized VoicemailPlaybackPresenter getInstance2(Activity activity, Bundle savedInstanceState) {
        VoicemailPlaybackPresenter voicemailPlaybackPresenter;
        synchronized (VoicemailPlaybackPresenter.class) {
            if (sInstance2 == null) {
                sInstance2 = new VoicemailPlaybackPresenter(activity);
            }
            sInstance2.init(activity, savedInstanceState);
            voicemailPlaybackPresenter = sInstance2;
        }
        return voicemailPlaybackPresenter;
    }

    public VoicemailPlaybackPresenter(Activity activity) {
        Context context = activity.getApplicationContext();
        this.mAsyncTaskExecutor = AsyncTaskExecutors.createAsyncTaskExecutor();
        this.mVoicemailAudioManager = new VoicemailAudioManager(context, this);
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        if (powerManager.isWakeLockLevelSupported(32)) {
            this.mProximityWakeLock = powerManager.newWakeLock(32, "VoicemailPlaybackPresenter");
        }
    }

    public void init(Activity activity, Bundle savedInstanceState) {
        this.mActivity = activity;
        this.mContext = activity;
        this.mInitialOrientation = this.mContext.getResources().getConfiguration().orientation;
        if (savedInstanceState != null) {
            this.mVoicemailUri = (Uri) savedInstanceState.getParcelable(VOICEMAIL_URI_KEY);
            this.mIsPrepared = savedInstanceState.getBoolean(IS_PREPARED_KEY);
            this.mPosition = savedInstanceState.getInt(CLIP_POSITION_KEY, 0);
            this.mIsPlaying = savedInstanceState.getBoolean(IS_PLAYING_STATE_KEY, false);
            this.mIsSpeakerphoneOn = savedInstanceState.getBoolean(IS_SPEAKERPHONE_ON_KEY, false);
            this.mIsFetching = savedInstanceState.getBoolean(IS_FETCHING_KEY, false);
            this.mIsTimeOut = savedInstanceState.getBoolean(IS_TIMEOUT_KEY, false);
        }
        if (!(CommonUtilMethods.getIsHaveEarpiece() || this.mVoicemailAudioManager.isWiredHeadsetPluggedIn())) {
            this.mIsSpeakerphoneOn = true;
        }
        if (this.mMediaPlayer == null) {
            this.mIsPrepared = false;
            this.mIsPlaying = false;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mView != null) {
            outState.putParcelable(VOICEMAIL_URI_KEY, this.mVoicemailUri);
            outState.putBoolean(IS_PREPARED_KEY, this.mIsPrepared);
            outState.putInt(CLIP_POSITION_KEY, this.mView.getDesiredClipPosition());
            outState.putBoolean(IS_PLAYING_STATE_KEY, this.mIsPlaying);
            outState.putBoolean(IS_SPEAKERPHONE_ON_KEY, this.mIsSpeakerphoneOn);
            outState.putBoolean(IS_FETCHING_KEY, this.mIsFetching);
            outState.putBoolean(IS_TIMEOUT_KEY, this.mIsTimeOut);
        }
    }

    public void setPlaybackView(PlaybackView view, Uri voicemailUri, String number, String transcription, boolean startPlayingImmediately) {
        this.mView = view;
        this.mView.setPresenter(this, voicemailUri, number, transcription);
        if (this.mMediaPlayer != null && this.mIsPrepared && voicemailUri.equals(this.mVoicemailUri)) {
            Log.d("VoicemailPlaybackPresenter", "setPlaybackView mIsPrepared mPositioin : " + this.mPosition);
            this.mPosition = this.mMediaPlayer.getCurrentPosition();
            onPrepared(this.mMediaPlayer);
        } else {
            if (voicemailUri.equals(this.mVoicemailUri)) {
                this.mView.onSpeakerphoneOn(this.mIsSpeakerphoneOn);
                if (restoreFetchingState()) {
                    return;
                }
            }
            this.mVoicemailUri = voicemailUri;
            this.mPosition = 0;
            if (CommonUtilMethods.getIsHaveEarpiece() || this.mVoicemailAudioManager.isWiredHeadsetPluggedIn()) {
                setSpeakerphoneOn(false);
                this.mVoicemailAudioManager.setSpeakerphoneOn(false);
                this.mView.setSpeakerphoneEnable(true);
            } else {
                setSpeakerphoneOn(true);
                this.mVoicemailAudioManager.setSpeakerphoneOn(true);
            }
            if (this.mFetchResultHandler != null) {
                this.mFetchResultHandler.destroy();
            }
            checkForContent(new OnContentCheckedListener() {
                public void onContentChecked(boolean hasContent) {
                    if (hasContent) {
                        Log.d("VoicemailPlaybackPresenter", "setPlaybackView checkForContent hasContent prepareContent");
                        VoicemailPlaybackPresenter.this.prepareContent();
                    } else if (VoicemailPlaybackPresenter.this.mView != null) {
                        Log.d("VoicemailPlaybackPresenter", "setPlaybackView checkForContent hasContent NO");
                        VoicemailPlaybackPresenter.this.mView.resetSeekBar();
                        VoicemailPlaybackPresenter.this.mView.setClipPosition(0, VoicemailPlaybackPresenter.this.mDuration.get());
                    }
                }
            });
            if (startPlayingImmediately) {
                this.mIsPlaying = startPlayingImmediately;
            }
        }
    }

    private boolean restoreFetchingState() {
        if (this.mIsFetching) {
            this.mView.setIsFetchingContent();
            return true;
        } else if (!this.mIsTimeOut) {
            return false;
        } else {
            this.mView.setFetchContentTimeout();
            return true;
        }
    }

    public void resetAll() {
        pausePresenter(true);
        if (this.mView != null) {
            Log.d("VoicemailPlaybackPresenter", "reset ui.");
            this.mView.enableUiElements();
            this.mView.setSuccess();
        }
        this.mDuration.set(0);
        this.mView = null;
        this.mVoicemailUri = null;
        this.mIsFetching = false;
        this.mIsTimeOut = false;
    }

    public void pausePresenter(boolean reset) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            Log.d("VoicemailPlaybackPresenter", "mMediaPlayer,release : " + this.mMediaPlayer);
            this.mMediaPlayer = null;
        }
        disableProximitySensor(false);
        this.mIsPrepared = false;
        this.mIsPlaying = false;
        Log.d("VoicemailPlaybackPresenter", "pausePresenter mIsPlaying false,reset : " + reset);
        if (reset) {
            this.mPosition = 0;
        }
        if (this.mView != null) {
            this.mView.onPlaybackStopped();
            if (reset) {
                this.mView.setClipPosition(0, this.mDuration.get());
            } else {
                this.mPosition = this.mView.getDesiredClipPosition();
            }
        }
        this.mAutoCollaseHandler.stop();
    }

    public void onResume() {
        this.mVoicemailAudioManager.registerReceivers();
        this.mVoicemailAudioManager.setSpeakerphoneOn(this.mIsSpeakerphoneOn);
    }

    public void onPause(Activity activity) {
        Log.d("VoicemailPlaybackPresenter", "onPause");
        if (this.mActivity == activity) {
            this.mVoicemailAudioManager.unregisterReceivers();
        }
        this.mVoicemailAudioManager.abandonAudioFocus();
        if (this.mContext == null || !this.mIsPrepared || this.mInitialOrientation == this.mContext.getResources().getConfiguration().orientation) {
            pausePresenter(false);
            if (this.mActivity != null) {
                this.mActivity.getWindow().clearFlags(128);
            }
            return;
        }
        Log.d("VoicemailPlaybackPresenter", "onPause: Orientation changed.");
    }

    public synchronized void onDestroy(Activity activity) {
        if (activity == this.mActivity) {
            this.mActivity = null;
            this.mContext = null;
        }
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            clearScheduledExecutorService();
        }
        if (!this.mArchiveResultHandlers.isEmpty()) {
            for (FetchResultHandler fetchResultHandler : this.mArchiveResultHandlers) {
                fetchResultHandler.destroy();
            }
            this.mArchiveResultHandlers.clear();
        }
        if (!(this.mFetchResultHandler == null || this.mIsFetching)) {
            this.mFetchResultHandler.destroy();
            this.mFetchResultHandler = null;
        }
    }

    protected void checkForContent(final OnContentCheckedListener callback) {
        this.mAsyncTaskExecutor.submit(Tasks.CHECK_FOR_CONTENT, new AsyncTask<Void, Void, Boolean>() {
            public Boolean doInBackground(Void... params) {
                return Boolean.valueOf(VoicemailPlaybackPresenter.this.queryHasContent(VoicemailPlaybackPresenter.this.mVoicemailUri));
            }

            public void onPostExecute(Boolean hasContent) {
                callback.onContentChecked(hasContent.booleanValue());
            }
        }, new Void[0]);
    }

    private boolean queryHasContent(Uri voicemailUri) {
        if (voicemailUri == null || this.mContext == null) {
            return false;
        }
        Cursor cursor = this.mContext.getContentResolver().query(voicemailUri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    HwLog.d("VoicemailPlaybackPresenter", "queryHasContent , duration : " + cursor.getInt(cursor.getColumnIndex("duration")));
                    boolean z = cursor.getInt(cursor.getColumnIndex("has_content")) == 1;
                    MoreCloseables.closeQuietly(cursor);
                    return z;
                }
            } catch (Throwable th) {
                MoreCloseables.closeQuietly(cursor);
            }
        }
        MoreCloseables.closeQuietly(cursor);
        return false;
    }

    protected boolean requestContent(int code) {
        if (this.mContext == null || this.mVoicemailUri == null) {
            return false;
        }
        FetchResultHandler tempFetchResultHandler = new FetchResultHandler(new Handler(), this.mVoicemailUri, code);
        switch (code) {
            case 1:
                this.mArchiveResultHandlers.add(tempFetchResultHandler);
                break;
            default:
                if (this.mFetchResultHandler != null) {
                    this.mFetchResultHandler.destroy();
                }
                this.mView.setIsFetchingContent();
                this.mFetchResultHandler = tempFetchResultHandler;
                break;
        }
        this.mContext.sendBroadcast(new Intent("android.intent.action.FETCH_VOICEMAIL", this.mVoicemailUri));
        return true;
    }

    protected void prepareContent() {
        if (this.mView != null && this.mContext != null) {
            Log.d("VoicemailPlaybackPresenter", "prepareContent");
            if (this.mMediaPlayer != null) {
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            }
            this.mIsPrepared = false;
            try {
                this.mMediaPlayer = new MediaPlayer();
                Log.d("VoicemailPlaybackPresenter", "new mMediaPlayer : " + this.mMediaPlayer);
                this.mMediaPlayer.setOnPreparedListener(this);
                this.mMediaPlayer.setOnErrorListener(this);
                this.mMediaPlayer.setOnCompletionListener(this);
                this.mMediaPlayer.reset();
                this.mMediaPlayer.setDataSource(this.mContext, this.mVoicemailUri);
                this.mMediaPlayer.setAudioStreamType(0);
                this.mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                handleError(e);
            }
        }
    }

    public void onPrepared(MediaPlayer mp) {
        if (this.mView != null) {
            Log.d("VoicemailPlaybackPresenter", "onPrepared, mMediaPlayer.getDuration() : " + this.mMediaPlayer.getDuration());
            this.mIsPrepared = true;
            this.mDuration.set(this.mMediaPlayer.getDuration());
            Log.d("VoicemailPlaybackPresenter", "onPrepared: mPosition=" + this.mPosition);
            this.mView.setClipPosition(this.mPosition, this.mDuration.get());
            this.mView.enableUiElements();
            this.mView.setSuccess();
            this.mIsTimeOut = false;
            this.mMediaPlayer.seekTo(this.mPosition);
            if (this.mIsPlaying) {
                resumePlayback();
            } else {
                pausePlayback();
            }
        }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        handleError(new IllegalStateException("MediaPlayer error listener invoked: " + extra));
        return true;
    }

    protected void handleError(Exception e) {
        Log.d("VoicemailPlaybackPresenter", "handleError: Could not play voicemail " + e);
        if (this.mIsPrepared) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mIsPrepared = false;
        }
        if (this.mView != null) {
            this.mView.onPlaybackError();
        }
        this.mPosition = 0;
        this.mIsPlaying = false;
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        pausePlayback();
        this.mPosition = 0;
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.seekTo(this.mPosition);
        }
        if (this.mView != null) {
            this.mView.setClipPosition(0, this.mDuration.get());
        }
        this.mAutoCollaseHandler.start();
    }

    public void onAudioFocusChange(boolean gainedFocus) {
        Log.d("VoicemailPlaybackPresenter", "onAudioFocusChange, gainedFocus : " + gainedFocus + "; mIsPlaying : " + this.mIsPlaying);
        if (this.mIsPlaying != gainedFocus) {
            if (this.mIsPlaying) {
                pausePlayback();
            } else {
                resumePlayback();
            }
        }
    }

    public void resumePlayback() {
        if (this.mView != null) {
            if (this.mIsPrepared) {
                this.mIsPlaying = true;
                if (!(this.mMediaPlayer == null || this.mMediaPlayer.isPlaying())) {
                    this.mPosition = Math.max(0, Math.min(this.mPosition, this.mDuration.get()));
                    this.mMediaPlayer.seekTo(this.mPosition);
                    try {
                        this.mVoicemailAudioManager.requestAudioFocus();
                        this.mMediaPlayer.start();
                        Log.d("VoicemailPlaybackPresenter", "mMediaPlayer.start : " + this.mMediaPlayer);
                        setSpeakerphoneOn(this.mIsSpeakerphoneOn);
                    } catch (RejectedExecutionException e) {
                        handleError(e);
                    }
                }
                Log.d("VoicemailPlaybackPresenter", "Resumed playback at " + this.mPosition + ".");
                this.mView.onPlaybackStarted(this.mDuration.get(), getScheduledExecutorServiceInstance());
                return;
            }
            checkForContent(new OnContentCheckedListener() {
                public void onContentChecked(boolean hasContent) {
                    if (hasContent) {
                        VoicemailPlaybackPresenter.this.mIsPlaying = true;
                        VoicemailPlaybackPresenter.this.prepareContent();
                        return;
                    }
                    VoicemailPlaybackPresenter.this.mIsPlaying = VoicemailPlaybackPresenter.this.requestContent(0);
                }
            });
        }
    }

    public void pausePlayback() {
        int i = 0;
        if (this.mIsPrepared) {
            this.mIsPlaying = false;
            if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
                this.mMediaPlayer.pause();
            }
            if (this.mMediaPlayer != null) {
                i = this.mMediaPlayer.getCurrentPosition();
            }
            this.mPosition = i;
            Log.d("VoicemailPlaybackPresenter", "Paused playback at " + this.mPosition + ".");
            if (this.mView != null) {
                this.mView.onPlaybackStopped();
            }
            this.mVoicemailAudioManager.abandonAudioFocus();
            if (this.mActivity != null) {
                this.mActivity.getWindow().clearFlags(128);
            }
            disableProximitySensor(true);
        }
    }

    public void pausePlaybackForSeeking() {
        if (this.mMediaPlayer != null) {
            this.mShouldResumePlaybackAfterSeeking = this.mMediaPlayer.isPlaying();
        }
        pausePlayback();
    }

    public void resumePlaybackAfterSeeking(int desiredPosition) {
        this.mPosition = desiredPosition;
        if (this.mShouldResumePlaybackAfterSeeking) {
            this.mShouldResumePlaybackAfterSeeking = false;
            resumePlayback();
        }
    }

    public void seek(int position) {
        this.mPosition = position;
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.seekTo(this.mPosition);
        }
    }

    private void enableProximitySensor() {
        if (this.mProximityWakeLock != null && !this.mIsSpeakerphoneOn && this.mIsPrepared && this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            if (this.mProximityWakeLock.isHeld()) {
                Log.i("VoicemailPlaybackPresenter", "Proximity wake lock already acquired");
            } else {
                Log.i("VoicemailPlaybackPresenter", "Acquiring proximity wake lock");
                this.mProximityWakeLock.acquire();
            }
        }
    }

    private void disableProximitySensor(boolean waitForFarState) {
        if (this.mProximityWakeLock != null) {
            if (this.mProximityWakeLock.isHeld()) {
                Log.i("VoicemailPlaybackPresenter", "Releasing proximity wake lock");
                this.mProximityWakeLock.release(waitForFarState ? 1 : 0);
            } else {
                Log.i("VoicemailPlaybackPresenter", "Proximity wake lock already released");
            }
        }
    }

    public void toggleSpeakerphone() {
        boolean z;
        boolean z2 = false;
        VoicemailAudioManager voicemailAudioManager = this.mVoicemailAudioManager;
        if (this.mIsSpeakerphoneOn) {
            z = false;
        } else {
            z = true;
        }
        voicemailAudioManager.setSpeakerphoneOn(z);
        if (!this.mIsSpeakerphoneOn) {
            z2 = true;
        }
        setSpeakerphoneOn(z2);
    }

    public void setSpeakerphoneOn(boolean on) {
        if (this.mView != null) {
            this.mView.onSpeakerphoneOn(on);
            this.mIsSpeakerphoneOn = on;
            if (this.mIsPlaying) {
                if (on || this.mVoicemailAudioManager.isWiredHeadsetPluggedIn()) {
                    disableProximitySensor(false);
                    if (this.mIsPrepared && this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
                        this.mActivity.getWindow().addFlags(128);
                    }
                } else {
                    enableProximitySensor();
                    if (this.mActivity != null) {
                        this.mActivity.getWindow().clearFlags(128);
                    }
                }
            }
        }
    }

    public void setSpeakerphoneEnable(boolean enable) {
        if (this.mView != null) {
            this.mView.setSpeakerphoneEnable(enable);
        }
    }

    public void setOnAutoCollapseListener(OnAutoCollapseListener listener) {
        this.mOnAutoCollapseListener = listener;
    }

    public void setOnVoicemailDeletedListener(OnVoicemailDeletedListener listener) {
        this.mOnVoicemailDeletedListener = listener;
    }

    public int getMediaPlayerPosition() {
        if (this.mMediaPlayer == null) {
            Log.d("VoicemailPlaybackPresenter", "getMediaPlayerPosition = null");
        } else {
            Log.d("VoicemailPlaybackPresenter", "getMediaPlayerPosition,mIsPrepared:" + this.mIsPrepared + ",p:" + this.mMediaPlayer.getCurrentPosition() + ",mMediaPlayer : " + this.mMediaPlayer);
        }
        return (!this.mIsPrepared || this.mMediaPlayer == null) ? 0 : this.mMediaPlayer.getCurrentPosition();
    }

    void onVoicemailDeleted() {
        if (this.mOnVoicemailDeletedListener != null) {
            this.mOnVoicemailDeletedListener.onVoicemailDeleted(this.mVoicemailUri);
        }
    }

    void onVoicemailDeletedInDatabase() {
        if (this.mOnVoicemailDeletedListener != null) {
            this.mOnVoicemailDeletedListener.onVoicemailDeletedInDatabase();
        }
    }

    private static synchronized ScheduledExecutorService getScheduledExecutorServiceInstance() {
        ScheduledExecutorService scheduledExecutorService;
        synchronized (VoicemailPlaybackPresenter.class) {
            if (mScheduledExecutorService == null) {
                mScheduledExecutorService = Executors.newScheduledThreadPool(2);
            }
            scheduledExecutorService = mScheduledExecutorService;
        }
        return scheduledExecutorService;
    }

    private static void clearScheduledExecutorService() {
        mScheduledExecutorService = null;
    }

    public void sendMsg(String number) {
        if (!TextUtils.isEmpty(number)) {
            try {
                this.mActivity.startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", number, null)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this.mActivity, R.string.quickcontact_missing_app_Toast, 0).show();
            }
        }
    }

    public void call(String number) {
        if (!TextUtils.isEmpty(number) && this.mActivity != null) {
            Uri numberUri = Uri.fromParts("tel", number, null);
            if (SimFactoryManager.isDualSim()) {
                this.mActivity.startActivity(new Intent("com.android.contacts.action.CHOOSE_SUB", numberUri));
                this.mActivity.overridePendingTransition(0, 0);
            } else {
                CommonUtilMethods.dialNumber(this.mActivity, numberUri, 0, true, false);
            }
        }
    }

    public void autoCollapse(boolean isStart) {
        if (isStart) {
            this.mAutoCollaseHandler.start();
        } else {
            this.mAutoCollaseHandler.stop();
        }
    }

    @VisibleForTesting
    public boolean isPlaying() {
        return this.mIsPlaying;
    }

    @VisibleForTesting
    public boolean isSpeakerphoneOn() {
        return this.mIsSpeakerphoneOn;
    }

    @VisibleForTesting
    public static void clearInstance() {
        sInstance = null;
    }
}
