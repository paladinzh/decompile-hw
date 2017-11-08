package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VolumeBalancePreferenceDialogActivity extends Activity implements OnSeekBarChangeListener, Runnable {
    private static final int VOLUME_BALANCE_LEVEL = SystemProperties.getInt("ro.volume_balance_value", 10);
    private AudioManager mAudioManager;
    private Context mContext;
    private AlertDialog mDialog;
    private Handler mHandler = new Handler();
    private int mLastProgress = -1;
    private MediaPlayer mMediaPlayer;
    private int mOriginalStreamVolumeBalance;
    protected boolean positiveResult = false;

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        this.mAudioManager = (AudioManager) getApplicationContext().getSystemService("audio");
        this.mContext = getApplicationContext();
        this.mMediaPlayer = new MediaPlayer();
        try {
            this.mMediaPlayer.setDataSource(this.mContext, getMediaVolumeUri());
            this.mMediaPlayer.prepare();
        } catch (Exception e) {
        }
        showDialog(1);
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case 1:
                Builder builder = new Builder(this);
                LinearLayout view = (LinearLayout) ((LayoutInflater) getSystemService("layout_inflater")).inflate(2130968931, null);
                SeekBar mBalanceSeekBar = (SeekBar) view.findViewById(2131886907);
                if (mBalanceSeekBar != null) {
                    initSeekBar(mBalanceSeekBar);
                }
                builder.setView(view);
                builder.setTitle(2131629149);
                builder.setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        VolumeBalancePreferenceDialogActivity.this.positiveResult = true;
                        SystemProperties.set("persist.sys.sound_balance_value", String.valueOf(VolumeBalancePreferenceDialogActivity.this.mLastProgress));
                        VolumeBalancePreferenceDialogActivity.this.finish();
                    }
                });
                builder.setNegativeButton(17039360, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        VolumeBalancePreferenceDialogActivity.this.cancelActivity();
                    }
                });
                this.mDialog = builder.create();
                this.mDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        VolumeBalancePreferenceDialogActivity.this.cancelActivity();
                    }
                });
                this.mDialog.show();
                break;
        }
        return super.onCreateDialog(id, args);
    }

    protected void revertVolumeBalance() {
        postSetVolumeBalance(this.mOriginalStreamVolumeBalance);
    }

    private void initSeekBar(SeekBar mBalanceSeekBar) {
        mBalanceSeekBar.setMax(VOLUME_BALANCE_LEVEL * 2);
        this.mOriginalStreamVolumeBalance = SystemProperties.getInt("persist.sys.sound_balance_value", VOLUME_BALANCE_LEVEL);
        this.mLastProgress = this.mOriginalStreamVolumeBalance;
        mBalanceSeekBar.setProgress(this.mOriginalStreamVolumeBalance);
        mBalanceSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            postSetVolumeBalance(progress);
        }
    }

    private void postSetVolumeBalance(int progress) {
        this.mLastProgress = progress;
        this.mHandler.removeCallbacks(this);
        this.mHandler.post(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        stopSample();
        this.mMediaPlayer.release();
    }

    public void onStartTrackingTouch(SeekBar arg0) {
        startSample();
    }

    public void onStopTrackingTouch(SeekBar arg0) {
    }

    public void run() {
        int leftValue = VOLUME_BALANCE_LEVEL;
        int rightValue = VOLUME_BALANCE_LEVEL;
        if (this.mLastProgress > VOLUME_BALANCE_LEVEL) {
            leftValue = (VOLUME_BALANCE_LEVEL * 2) - this.mLastProgress;
        } else if (this.mLastProgress < VOLUME_BALANCE_LEVEL) {
            rightValue = this.mLastProgress;
        }
        SystemProperties.set("persist.sys.volume_value_left", String.valueOf(leftValue));
        SystemProperties.set("persist.sys.volume_value_right", String.valueOf(rightValue));
        this.mAudioManager.setParameters("SET_VOLUME_BALANCE=true");
    }

    private Uri getMediaVolumeUri() {
        return Uri.parse("android.resource://" + this.mContext.getPackageName() + "/" + 2131296258);
    }

    public boolean isSamplePlaying() {
        return this.mMediaPlayer != null ? this.mMediaPlayer.isPlaying() : false;
    }

    public void startSample() {
        if (!isSamplePlaying() && this.mMediaPlayer != null) {
            this.mMediaPlayer.start();
        }
    }

    public void stopSample() {
        if (isSamplePlaying() && this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
        }
    }

    public void cancelActivity() {
        if (!this.positiveResult) {
            revertVolumeBalance();
        }
        finish();
    }
}
