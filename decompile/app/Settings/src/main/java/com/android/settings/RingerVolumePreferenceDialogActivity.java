package com.android.settings;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.SeekBarVolumizer;
import android.preference.VolumePreference;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.huawei.cust.HwCustUtils;

public class RingerVolumePreferenceDialogActivity extends SettingsDrawerActivity {
    private static final int[] CHECKBOX_VIEW_ID = new int[]{2131886874, 2131886878, 2131886878, 2131886885, 2131886889};
    private static final int[] SEEKBAR_ID = new int[]{2131886875, 2131886879, 2131886879, 2131886886, 2131886891};
    private static final int[] SEEKBAR_MUTED_RES_ID = new int[]{2130838206, 2130838202, 2130838200, 2130838198, 2130838204};
    private static final int[] SEEKBAR_TYPE = new int[]{3, 2, 5, 4, 0};
    private static final int[] SEEKBAR_UNMUTED_RES_ID = new int[]{2130838205, 2130838201, 2130838199, 2130838197, 2130838204};
    private static Preference ringVolumn;
    private AudioManager mAudioManager;
    private ImageView[] mCheckBoxes = new ImageView[SEEKBAR_MUTED_RES_ID.length];
    private HwCustRingerVolumePreferenceDialogActivity mCust;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            RingerVolumePreferenceDialogActivity.this.updateSlidersAndMutedStates();
        }
    };
    private int mHideSectionIndex = -1;
    private BroadcastReceiver mRingModeChangedReceiver;
    private VolumePreference mRingerVolumePreference;
    private RingerVolumeSeekBarVolumizer[] mSeekBarVolumizer;
    private SeekBar[] mSeekBars = new SeekBar[SEEKBAR_ID.length];
    private final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();

    public interface Callback {
        void onSampleStarting(SeekBarVolumizer seekBarVolumizer);
    }

    private final class VolumePreferenceCallback implements Callback {
        private SeekBarVolumizer mCurrent;

        private VolumePreferenceCallback() {
        }

        public void onSampleStarting(SeekBarVolumizer sbv) {
            if (!(this.mCurrent == null || this.mCurrent == sbv)) {
                this.mCurrent.stopSample();
            }
            this.mCurrent = sbv;
        }
    }

    public static void setSilent(Preference ring_volumn) {
        ringVolumn = ring_volumn;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968926);
        LinearLayout voiceVolumeLayout = (LinearLayout) findViewById(2131886888);
        this.mCust = (HwCustRingerVolumePreferenceDialogActivity) HwCustUtils.createObj(HwCustRingerVolumePreferenceDialogActivity.class, new Object[]{this});
        Window win = getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        int i;
        this.mRingerVolumePreference = new VolumePreference(this, null);
        this.mRingerVolumePreference.setPreferenceId(2131886107);
        View view = findViewById(2131886887);
        if (null != null) {
            int masterVolumeId;
            if (Utils.isVoiceCapable(this)) {
                masterVolumeId = 1;
            } else {
                masterVolumeId = 0;
            }
            LinearLayout volumeContainer = (LinearLayout) view.findViewById(2131886887);
            int count = volumeContainer.getChildCount();
            for (i = 0; i < count; i++) {
                if (masterVolumeId != i / 2) {
                    volumeContainer.getChildAt(i).setVisibility(8);
                }
            }
        } else if (Utils.isVoiceCapable(this)) {
            this.mHideSectionIndex = 2;
        } else {
            this.mHideSectionIndex = 1;
            ((TextView) view.findViewById(2131886877)).setText(2131625124);
            ((ImageView) view.findViewById(2131886878)).setContentDescription(getResources().getString(2131625128));
        }
        this.mRingerVolumePreference.setStreamType(2);
        this.mSeekBarVolumizer = new RingerVolumeSeekBarVolumizer[SEEKBAR_ID.length];
        this.mAudioManager = (AudioManager) getSystemService("audio");
        i = 0;
        while (i < SEEKBAR_ID.length) {
            if (null != null || i != this.mHideSectionIndex) {
                SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
                this.mSeekBars[i] = seekBar;
                final int j = i;
                this.mSeekBarVolumizer[i] = createSeekBarVolumizer(seekBar, SEEKBAR_TYPE[i], this.mVolumeCallback);
                seekBar.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        for (RingerVolumeSeekBarVolumizer vol : RingerVolumePreferenceDialogActivity.this.mSeekBarVolumizer) {
                            if (vol != null) {
                                try {
                                    if (vol != RingerVolumePreferenceDialogActivity.this.mSeekBarVolumizer[j] && vol.isSamplePlaying()) {
                                        vol.stopSample();
                                        Log.d("RingerVolumePreferenceActivity" + v.getId(), "vol.stopSample()");
                                    }
                                } catch (IllegalStateException ex) {
                                    Log.w("RingerVolumePreferenceActivity", "IllegalStateException");
                                    ex.printStackTrace();
                                }
                            }
                        }
                        return false;
                    }
                });
                if (SEEKBAR_TYPE[i] == 0) {
                    initVoiceSeekbar(seekBar, i);
                }
                this.mSeekBarVolumizer[i].start();
            }
            i++;
        }
        int silentableStreams = System.getInt(getContentResolver(), "mode_ringer_streams_affected", 36);
        for (i = 0; i < this.mCheckBoxes.length; i++) {
            if (i != this.mHideSectionIndex) {
                this.mCheckBoxes[i] = (ImageView) view.findViewById(CHECKBOX_VIEW_ID[i]);
            }
        }
        updateSlidersAndMutedStates();
        if (this.mRingModeChangedReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.media.RINGER_MODE_CHANGED");
            filter.addAction("android.media.VOLUME_CHANGED_ACTION");
            this.mRingModeChangedReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("android.media.RINGER_MODE_CHANGED".equals(action) || "android.media.VOLUME_CHANGED_ACTION".equals(action)) {
                        RingerVolumePreferenceDialogActivity.this.mHandler.sendMessage(RingerVolumePreferenceDialogActivity.this.mHandler.obtainMessage(101, intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1), 0));
                    }
                }
            };
            registerReceiver(this.mRingModeChangedReceiver, filter);
        }
    }

    public void onResume() {
        super.onResume();
        initializeViews();
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(getBaseContext());
        super.onPause();
        for (RingerVolumeSeekBarVolumizer vol : this.mSeekBarVolumizer) {
            if (vol != null) {
                vol.stop();
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            if (this.mSeekBars[i] != null) {
                ItemUseStat.getInstance().handleClick(getBaseContext(), 2, ItemUseStat.KEY_VOLUME_STYLE[i], this.mSeekBars[i].getProgress());
            }
        }
        if (this.mRingModeChangedReceiver != null) {
            unregisterReceiver(this.mRingModeChangedReceiver);
            this.mRingModeChangedReceiver = null;
        }
        if (ringVolumn != null) {
            ringVolumn.setEnabled(true);
        }
    }

    private Uri getMediaVolumeUri(Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + 2131296258);
    }

    private boolean isZenMuted(int streamType) {
        int zenMode = ((NotificationManager) getSystemService(NotificationManager.class)).getZenMode();
        if ((zenMode == 3 && streamType == 2) || zenMode == 2) {
            return true;
        }
        return false;
    }

    private void updateSlidersAndMutedStates() {
        int i = 0;
        while (i < SEEKBAR_TYPE.length) {
            int streamType = SEEKBAR_TYPE[i];
            if (isZenMuted(streamType) && this.mSeekBars[i] != null) {
                this.mSeekBars[i].setEnabled(true);
            }
            boolean muted = this.mAudioManager.isStreamMute(streamType);
            if (this.mCheckBoxes[i] != null) {
                if ((streamType == 2 || streamType == 5) && SoundSettingsHwBase.getRingerMode(this.mAudioManager) == 1) {
                    this.mCheckBoxes[i].setImageResource(2130838203);
                } else {
                    this.mCheckBoxes[i].setImageResource(muted ? SEEKBAR_MUTED_RES_ID[i] : SEEKBAR_UNMUTED_RES_ID[i]);
                }
            }
            if (this.mSeekBars[i] != null) {
                int volume = this.mAudioManager.getStreamVolume(streamType);
                if (SEEKBAR_TYPE[i] == 0) {
                    this.mSeekBars[i].setProgress(volume);
                } else if (volume == 0) {
                    this.mSeekBars[i].setProgress(volume + 1);
                    this.mSeekBars[i].setProgress(volume);
                } else {
                    this.mSeekBars[i].setProgress(volume);
                }
            }
            i++;
        }
    }

    private void initVoiceSeekbar(SeekBar seekBar, int index) {
        final RingerVolumeSeekBarVolumizer vol = this.mSeekBarVolumizer[index];
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                vol.onStopTrackingTouch(seekBar);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                vol.onStartTrackingTouch(seekBar);
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    progress = 1;
                    seekBar.setProgress(1);
                }
                vol.onProgressChanged(seekBar, progress, fromUser);
            }
        });
        seekBar.setProgress(this.mAudioManager.getStreamVolume(SEEKBAR_TYPE[index]));
    }

    private RingerVolumeSeekBarVolumizer createSeekBarVolumizer(SeekBar mSeekBar, int mStream, final Callback mCallback) {
        final int i = mStream;
        RingerVolumeSeekBarVolumizer volumizer = new RingerVolumeSeekBarVolumizer(this, mStream, mStream == 3 ? getMediaVolumeUri(this) : null, new android.preference.SeekBarVolumizer.Callback() {
            public void onSampleStarting(SeekBarVolumizer sbv) {
                if (mCallback != null) {
                    mCallback.onSampleStarting(sbv);
                }
            }

            public void onMuted(boolean muted, boolean muted2) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            }
        }) {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                super.onProgressChanged(seekBar, progress, fromTouch);
                if (RingerVolumePreferenceDialogActivity.this.mCust != null) {
                    RingerVolumePreferenceDialogActivity.this.mCust.saveAfterChangeVolume(i, progress);
                }
            }
        };
        volumizer.setSeekBar(mSeekBar);
        return volumizer;
    }
}
