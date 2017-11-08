package com.android.dialer.voicemail;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Space;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.dialer.calllog.CallLogAsyncTaskUtil.CallLogAsyncTaskListener;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter.PlaybackView;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@NotThreadSafe
public class VoicemailPlaybackLayout extends LinearLayout implements PlaybackView, CallLogAsyncTaskListener {
    private static final String TAG = VoicemailPlaybackLayout.class.getSimpleName();
    private ImageView mCallButton;
    private final OnClickListener mCallButtonListener;
    private Context mContext;
    private ImageView mDeleteButton;
    private final OnClickListener mDeleteButtonListener;
    private boolean mIsPlaying;
    private final OnClickListener mMsgButtonListener;
    private String mNumber;
    private SeekBar mPlaybackSeek;
    private ImageView mPlaybackSpeakerphone;
    private TextView mPositionText;
    private PositionUpdater mPositionUpdater;
    private VoicemailPlaybackPresenter mPresenter;
    private final OnSeekBarChangeListener mSeekBarChangeListener;
    private ImageView mSendMsgButton;
    private final OnClickListener mSpeakerphoneListener;
    private ImageView mStartStopButton;
    private final OnClickListener mStartStopButtonListener;
    private TextView mStateText;
    private TextView mTotalDurationText;
    private String mTranscription;
    private ImageView mTranslateButton;
    private final OnClickListener mTranslateButtonListener;
    private Space mTranslateSpace;
    private TextView mTranslateTextView;
    private Uri mVoicemailUri;

    @ThreadSafe
    private final class PositionUpdater implements Runnable {
        private int mDurationMs;
        private final ScheduledExecutorService mExecutorService;
        private final Object mLock = new Object();
        @GuardedBy("mLock")
        private ScheduledFuture<?> mScheduledFuture;
        private Runnable mUpdateClipPositionRunnable = new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                synchronized (PositionUpdater.this.mLock) {
                    if (PositionUpdater.this.mScheduledFuture == null || VoicemailPlaybackLayout.this.mPresenter == null) {
                    } else {
                        int currentPositionMs = VoicemailPlaybackLayout.this.mPresenter.getMediaPlayerPosition();
                        Log.d(VoicemailPlaybackLayout.TAG, PositionUpdater.this + ",setClipPosition : " + currentPositionMs);
                        VoicemailPlaybackLayout.this.setClipPosition(currentPositionMs, PositionUpdater.this.mDurationMs);
                    }
                }
            }
        };

        public PositionUpdater(int durationMs, ScheduledExecutorService executorService) {
            this.mDurationMs = durationMs;
            this.mExecutorService = executorService;
        }

        public void run() {
            VoicemailPlaybackLayout.this.post(this.mUpdateClipPositionRunnable);
        }

        public void startUpdating() {
            synchronized (this.mLock) {
                cancelPendingRunnables();
                this.mScheduledFuture = this.mExecutorService.scheduleAtFixedRate(this, 0, 33, TimeUnit.MILLISECONDS);
                Log.d(VoicemailPlaybackLayout.TAG, "startUpdating,mScheduledFuture : " + this.mScheduledFuture);
            }
        }

        public void stopUpdating() {
            synchronized (this.mLock) {
                cancelPendingRunnables();
            }
        }

        private void cancelPendingRunnables() {
            Log.d(VoicemailPlaybackLayout.TAG, "cancelPendingRunnables,mScheduledFuture : " + this.mScheduledFuture);
            if (this.mScheduledFuture != null) {
                this.mScheduledFuture.cancel(true);
                this.mScheduledFuture = null;
            }
            VoicemailPlaybackLayout.this.removeCallbacks(this.mUpdateClipPositionRunnable);
        }
    }

    public VoicemailPlaybackLayout(Context context) {
        this(context, null);
    }

    public VoicemailPlaybackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSeekBarChangeListener = new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(VoicemailPlaybackLayout.TAG, "onStartTrackingTouch");
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mPresenter.pausePlaybackForSeeking();
                    VoicemailPlaybackLayout.this.mPresenter.autoCollapse(false);
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(VoicemailPlaybackLayout.TAG, "onStopTrackingTouch,seekBar.getProgress():" + seekBar.getProgress());
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mPresenter.resumePlaybackAfterSeeking(seekBar.getProgress());
                }
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(VoicemailPlaybackLayout.TAG, "onProgressChanged,progress : " + progress + ", fromUser : " + fromUser + ",max : " + seekBar.getMax());
                VoicemailPlaybackLayout.this.setClipPosition(progress, seekBar.getMax());
                if (fromUser) {
                    VoicemailPlaybackLayout.this.mPresenter.seek(progress);
                }
            }
        };
        this.mSpeakerphoneListener = new OnClickListener() {
            public void onClick(View v) {
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mPresenter.toggleSpeakerphone();
                    VoicemailPlaybackLayout.this.mPresenter.autoCollapse(false);
                    StatisticalHelper.report(5039);
                }
            }
        };
        this.mStartStopButtonListener = new OnClickListener() {
            public void onClick(View view) {
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    if (VoicemailPlaybackLayout.this.mIsPlaying) {
                        VoicemailPlaybackLayout.this.mPresenter.pausePlayback();
                        StatisticalHelper.report(5038);
                    } else {
                        VoicemailPlaybackLayout.this.mPresenter.resumePlayback();
                        StatisticalHelper.report(5037);
                    }
                    VoicemailPlaybackLayout.this.mPresenter.autoCollapse(false);
                }
            }
        };
        this.mDeleteButtonListener = new OnClickListener() {
            public void onClick(View view) {
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mPresenter.pausePlayback();
                    VoicemailPlaybackLayout.this.mPresenter.onVoicemailDeleted();
                    VoicemailPlaybackLayout.this.mPresenter.autoCollapse(false);
                    StatisticalHelper.report(5042);
                }
            }
        };
        this.mMsgButtonListener = new OnClickListener() {
            public void onClick(View view) {
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mPresenter.sendMsg(VoicemailPlaybackLayout.this.mNumber);
                    StatisticalHelper.report(5041);
                }
            }
        };
        this.mCallButtonListener = new OnClickListener() {
            public void onClick(View view) {
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mPresenter.call(VoicemailPlaybackLayout.this.mNumber);
                    StatisticalHelper.report(5040);
                }
            }
        };
        this.mTranslateButtonListener = new OnClickListener() {
            public void onClick(View view) {
                if (VoicemailPlaybackLayout.this.mPresenter != null) {
                    VoicemailPlaybackLayout.this.mTranslateTextView.setText(VoicemailPlaybackLayout.this.mTranscription);
                    VoicemailPlaybackLayout.this.mTranslateTextView.setVisibility(0);
                }
            }
        };
        this.mIsPlaying = false;
        this.mContext = context;
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.voicemail_playback_layout, this);
    }

    public void setPresenter(VoicemailPlaybackPresenter presenter, Uri voicemailUri, String number, String transcription) {
        this.mPresenter = presenter;
        this.mVoicemailUri = voicemailUri;
        this.mNumber = number;
        this.mTranscription = transcription;
        setTranslateButtonVisible();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPlaybackSeek = (SeekBar) findViewById(R.id.playback_seek);
        this.mStartStopButton = (ImageView) findViewById(R.id.playback_start_stop);
        this.mPlaybackSpeakerphone = (ImageView) findViewById(R.id.playback_speakerphone);
        this.mDeleteButton = (ImageView) findViewById(R.id.delete_voicemail);
        this.mStateText = (TextView) findViewById(R.id.playback_state_text);
        this.mPositionText = (TextView) findViewById(R.id.playback_position_text);
        this.mTotalDurationText = (TextView) findViewById(R.id.total_duration_text);
        this.mSendMsgButton = (ImageView) findViewById(R.id.msg);
        this.mCallButton = (ImageView) findViewById(R.id.call);
        this.mTranslateTextView = (TextView) findViewById(R.id.playback_translate_text);
        this.mTranslateButton = (ImageView) findViewById(R.id.translate);
        this.mTranslateSpace = (Space) findViewById(R.id.translate_space);
        this.mPlaybackSeek.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
        this.mStartStopButton.setOnClickListener(this.mStartStopButtonListener);
        this.mPlaybackSpeakerphone.setOnClickListener(this.mSpeakerphoneListener);
        this.mDeleteButton.setOnClickListener(this.mDeleteButtonListener);
        this.mSendMsgButton.setOnClickListener(this.mMsgButtonListener);
        this.mCallButton.setOnClickListener(this.mCallButtonListener);
        this.mTranslateButton.setOnClickListener(this.mTranslateButtonListener);
        ViewUtil.setStateListIcon(this.mContext, this.mPlaybackSeek, false);
        ViewUtil.setStateListIcon(this.mContext, this.mTranslateButton, false);
        if (!CommonUtilMethods.getIsHaveEarpiece()) {
            setSpeakerphoneEnable(false);
        }
    }

    private void setTranslateButtonVisible() {
        this.mTranslateButton.setVisibility(8);
        this.mTranslateSpace.setVisibility(8);
        this.mTranslateTextView.setVisibility(8);
    }

    public void onPlaybackStarted(int duration, ScheduledExecutorService executorService) {
        this.mIsPlaying = true;
        this.mStartStopButton.setImageResource(R.drawable.ic_contacts_pause_voicemail_calllog);
        this.mStartStopButton.setContentDescription(getString(R.string.content_description_pause));
        if (this.mPresenter != null) {
            onSpeakerphoneOn(this.mPresenter.isSpeakerphoneOn());
        }
        onPositionUpdaterCancle();
        this.mPositionUpdater = new PositionUpdater(duration, executorService);
        this.mPositionUpdater.startUpdating();
        Log.d(TAG, "new mPositionUpdater : " + this.mPositionUpdater);
    }

    public void onPlaybackStopped() {
        this.mIsPlaying = false;
        this.mStartStopButton.setImageResource(R.drawable.ic_contacts_play_voicemail);
        this.mStartStopButton.setContentDescription(getString(R.string.notification_action_voicemail_play));
        Log.d(TAG, "onPlaybackStopped,mPositionUpdater : " + this.mPositionUpdater);
        if (this.mPositionUpdater != null) {
            this.mPositionUpdater.stopUpdating();
            this.mPositionUpdater = null;
        }
    }

    public void onPlaybackError() {
        if (this.mPositionUpdater != null) {
            this.mPositionUpdater.stopUpdating();
        }
        disableUiElements();
        this.mStateText.setText(getString(R.string.voicemail_playback_error));
    }

    public void onSpeakerphoneOn(boolean on) {
        if (on) {
            this.mPlaybackSpeakerphone.setImageResource(R.drawable.ic_contacts_earpiece);
            this.mPlaybackSpeakerphone.setContentDescription(getString(R.string.content_description_earpiece));
            return;
        }
        this.mPlaybackSpeakerphone.setImageResource(R.drawable.ic_public_sound);
        this.mPlaybackSpeakerphone.setContentDescription(getString(R.string.content_description_speakerphone));
        setSpeakerphoneEnable(true);
    }

    public void setSpeakerphoneEnable(boolean enable) {
        this.mPlaybackSpeakerphone.setEnabled(enable);
    }

    public void setClipPosition(int positionMs, int durationMs) {
        int seekBarPositionMs = Math.max(0, positionMs);
        int seekBarMax = Math.max(seekBarPositionMs, durationMs);
        if (this.mPlaybackSeek.getMax() != seekBarMax) {
            this.mPlaybackSeek.setMax(seekBarMax);
        }
        this.mPlaybackSeek.setProgress(seekBarPositionMs);
        this.mPositionText.setText(formatAsMinutesAndSeconds(seekBarPositionMs));
        this.mTotalDurationText.setText(formatAsMinutesAndSeconds(durationMs));
        this.mPositionText.setContentDescription(formatDurationDescription(seekBarPositionMs));
        this.mTotalDurationText.setContentDescription(formatDurationDescription(durationMs));
    }

    private String formatDurationDescription(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds -= minutes * 60;
        if (minutes > 99) {
            minutes = 99;
        }
        String min = String.format(this.mContext.getResources().getQuantityText(R.plurals.callDetailsDurationFormatHours_Minutes, minutes).toString(), new Object[]{Integer.valueOf(minutes)});
        String sec = String.format(this.mContext.getResources().getQuantityText(R.plurals.callDetailsDurationFormatHours_Seconds, seconds).toString(), new Object[]{Integer.valueOf(seconds)});
        if (minutes < 1) {
            return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", "", sec});
        }
        return this.mContext.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", min, sec});
    }

    public void setSuccess() {
        this.mStateText.setText(null);
    }

    public void setIsFetchingContent() {
        disableUiElements();
        this.mStateText.setText(getString(R.string.voicemail_fetching_content2));
    }

    public void setFetchContentTimeout() {
        this.mStartStopButton.setEnabled(true);
        this.mStateText.setText(getString(R.string.voicemail_fetching_timout2));
    }

    public int getDesiredClipPosition() {
        return this.mPlaybackSeek.getProgress();
    }

    public void disableUiElements() {
        this.mStartStopButton.setEnabled(false);
        resetSeekBar();
    }

    public void enableUiElements() {
        this.mDeleteButton.setEnabled(true);
        this.mStartStopButton.setEnabled(true);
        this.mPlaybackSeek.setEnabled(true);
    }

    public void resetSeekBar() {
        this.mPlaybackSeek.setProgress(0);
        this.mPlaybackSeek.setEnabled(false);
    }

    private String getString(int resId) {
        return this.mContext.getString(resId);
    }

    private String formatAsMinutesAndSeconds(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds -= minutes * 60;
        if (minutes > 99) {
            minutes = 99;
        }
        return String.format("%02d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)});
    }

    private void onPositionUpdaterCancle() {
        if (this.mPositionUpdater != null) {
            this.mPositionUpdater.stopUpdating();
            this.mPositionUpdater = null;
        }
    }

    @VisibleForTesting
    public String getStateText() {
        return this.mStateText.getText().toString();
    }

    public void onDeleteVoicemail() {
        this.mPresenter.onVoicemailDeletedInDatabase();
    }
}
