package com.android.settings;

import android.content.Context;
import android.net.Uri;
import android.preference.SeekBarVolumizer;
import android.preference.SeekBarVolumizer.Callback;
import android.widget.SeekBar;

public class RingerVolumeSeekBarVolumizer extends SeekBarVolumizer {
    public RingerVolumeSeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        super(context, streamType, defaultUri, callback);
    }

    protected void updateSeekBar() {
        super.updateSeekBar();
        SeekBar seekBar = getSeekBar();
        if (seekBar != null) {
            seekBar.setEnabled(true);
        }
    }
}
