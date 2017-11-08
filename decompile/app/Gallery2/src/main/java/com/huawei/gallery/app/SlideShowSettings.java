package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.gallery3d.R;

public class SlideShowSettings extends Activity {
    private final int DEFAULT_INTERVAL = 3;
    private final int INTERVAL_MAX_VALUE = 10;
    private CheckBox mAddMusicCheckBox;
    private String mAudioPath;
    private boolean mIsMusicLaunched;
    private boolean mIsRadioGrpVisibile;
    private String mItemPath;
    private int mProgress = 3;
    OnCheckedChangeListener mRadioBtnChangeLister = new OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            SlideShowSettings.this.mSlidePrefId = checkedId;
        }
    };
    private TextView mSeekBarTxtView;
    private int mSlidePrefId = R.id.current_indx_radiobtn;
    private SeekBar mSlideTimeInterval;
    private TextView mStartFromTxtView;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.slide_settings_view);
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            if (extra.containsKey("radio_btn_visibility")) {
                this.mIsRadioGrpVisibile = extra.getBoolean("radio_btn_visibility", false);
            }
            if (extra.containsKey("item_path")) {
                this.mItemPath = extra.getString("item_path", "");
            }
        }
        RadioGroup slideRadioGp = (RadioGroup) findViewById(R.id.slide_radiogp);
        this.mStartFromTxtView = (TextView) findViewById(R.id.tv_start_from);
        this.mAddMusicCheckBox = (CheckBox) findViewById(R.id.ckBoxAddMusic);
        this.mSeekBarTxtView = (TextView) findViewById(R.id.tv_seekbar_value);
        this.mSlideTimeInterval = (SeekBar) findViewById(R.id.sb_time_interval);
        this.mSlideTimeInterval.setProgress(3);
        this.mSlideTimeInterval.setMax(10);
        if (this.mIsRadioGrpVisibile) {
            slideRadioGp.setVisibility(0);
            this.mStartFromTxtView.setVisibility(0);
        }
        this.mSeekBarTxtView.setText(String.valueOf(3) + " Sec");
        this.mSlideTimeInterval.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                SlideShowSettings.this.mProgress = progresValue;
                SlideShowSettings.this.mSeekBarTxtView.setText(String.valueOf(progresValue) + " Sec");
                if (progresValue < 3) {
                    SlideShowSettings.this.mSlideTimeInterval.setProgress(2);
                }
            }
        });
        slideRadioGp.setOnCheckedChangeListener(this.mRadioBtnChangeLister);
    }

    public void onConfirmation(View view) {
        if (this.mAddMusicCheckBox.isChecked()) {
            this.mIsMusicLaunched = true;
            onAddMusic();
            return;
        }
        sendDataForSlideShow();
    }

    private void sendDataForSlideShow() {
        Intent result = new Intent();
        result.putExtra(HwCustSlideShowPageImpl.KEY_INTERVAL, this.mProgress * 1000);
        result.putExtra("sel_id", this.mSlidePrefId);
        result.putExtra(HwCustSlideShowPageImpl.KEY_BCK_AUDIO, this.mAudioPath);
        result.putExtra("radio_btn_visibility", this.mIsRadioGrpVisibile);
        result.putExtra("item_path", this.mItemPath);
        setResult(-1, result);
        finish();
        overridePendingTransition(0, 0);
    }

    public void onCancel(View view) {
        finish();
        overridePendingTransition(0, 0);
    }

    private void onAddMusic() {
        startActivityForResult(new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI), 9090);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (9090 == requestCode) {
            if (-1 == resultCode && data != null) {
                this.mAudioPath = data.getData().toString();
            }
            sendDataForSlideShow();
        }
    }

    protected void onPause() {
        super.onPause();
        if (!this.mIsMusicLaunched) {
            finish();
        }
        overridePendingTransition(0, 0);
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
