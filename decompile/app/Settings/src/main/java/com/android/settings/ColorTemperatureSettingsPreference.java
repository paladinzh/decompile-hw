package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.HwCustDisplaySettingsImpl.ColorTemperatureCallback;

public class ColorTemperatureSettingsPreference extends SeekBarDialogPreference implements OnSeekBarChangeListener, ColorTemperatureCallback, OnCheckedChangeListener {
    private static String COLOR_TEMPERATURE = "color_temperature";
    private static String COLOR_TEMPERATURE_MODE = "color_temperature_mode";
    private static int COLOR_TEMPERATURE_MODE_DEFAULT = 1;
    private static int COLOR_TEMPERATURE_MODE_MANUAL = 0;
    private static final int DEFAULT_TEMPERATURE = ((MAXINUM_TEMPERATURE + 1) / 2);
    private static final int MAXINUM_TEMPERATURE = SystemProperties.getInt("ro.config.mtk_color_maxvalue", 255);
    private CheckBox mCheckBox;
    private boolean mIsDefultSetChecked = true;
    private boolean mIsOldDefultSelected;
    private int mOldColorTemperature;
    private boolean mRestoredOldState = false;
    private SeekBar mSeekBar;
    private boolean pauseModeChecked = true;
    private int pauseSeekprocess = -1;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                int -get0;
                ColorTemperatureSettingsPreference colorTemperatureSettingsPreference = ColorTemperatureSettingsPreference.this;
                if (ColorTemperatureSettingsPreference.this.mIsDefultSetChecked) {
                    -get0 = ColorTemperatureSettingsPreference.DEFAULT_TEMPERATURE;
                } else {
                    -get0 = ColorTemperatureSettingsPreference.this.mSeekBar.getProgress();
                }
                colorTemperatureSettingsPreference.setColorTemperature(-get0);
                ColorTemperatureSettingsPreference.this.setMode(ColorTemperatureSettingsPreference.this.mIsDefultSetChecked ? 1 : 0);
                ColorTemperatureSettingsPreference.this.mRestoredOldState = true;
            }
        }
    };

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean mIsDefaultSelected;
        boolean mIsOldDefaultSelected;
        int oldProgress;
        int progress;

        public SavedState(Parcel source) {
            boolean z;
            boolean z2 = true;
            super(source);
            if (source.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mIsDefaultSelected = z;
            this.progress = source.readInt();
            if (source.readInt() != 1) {
                z2 = false;
            }
            this.mIsOldDefaultSelected = z2;
            this.oldProgress = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            super.writeToParcel(dest, flags);
            if (this.mIsDefaultSelected) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.progress);
            if (!this.mIsOldDefaultSelected) {
                i2 = 0;
            }
            dest.writeInt(i2);
            dest.writeInt(this.oldProgress);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public ColorTemperatureSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(2130968929);
    }

    protected void onBindDialogView(View view) {
        int i;
        super.onBindDialogView(view);
        this.mSeekBar = SeekBarDialogPreference.getSeekBar(view);
        this.mSeekBar.setMax(MAXINUM_TEMPERATURE);
        this.mOldColorTemperature = getColorTemperature();
        this.mSeekBar.setProgress(this.mOldColorTemperature);
        this.mCheckBox = (CheckBox) view.findViewById(2131886896);
        this.mCheckBox.setOnCheckedChangeListener(this);
        this.mIsOldDefultSelected = isColorTemperDefaultMode();
        this.mCheckBox.setChecked(this.mIsOldDefultSelected);
        this.mSeekBar.setOnSeekBarChangeListener(this);
        this.mIsDefultSetChecked = this.mIsOldDefultSelected;
        if (this.mIsDefultSetChecked) {
            i = DEFAULT_TEMPERATURE;
        } else {
            i = this.mOldColorTemperature;
        }
        setColorTemperature(i);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction("android.intent.action.SCREEN_OFF");
        iFilter.addAction("android.intent.action.SCREEN_ON");
        getContext().registerReceiver(this.receiver, iFilter);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        setColorTemperature(progress);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int i;
        if (isChecked) {
            i = COLOR_TEMPERATURE_MODE_DEFAULT;
        } else {
            i = COLOR_TEMPERATURE_MODE_MANUAL;
        }
        setMode(i);
        this.mIsDefultSetChecked = isChecked;
        if (isChecked) {
            i = DEFAULT_TEMPERATURE;
        } else {
            i = this.mSeekBar.getProgress();
        }
        setColorTemperature(i);
        if (isChecked) {
            this.mSeekBar.setProgress(DEFAULT_TEMPERATURE);
        }
    }

    private int getColorTemperature() {
        return System.getIntForUser(getContext().getContentResolver(), COLOR_TEMPERATURE, DEFAULT_TEMPERATURE, UserHandle.myUserId());
    }

    private boolean isColorTemperDefaultMode() {
        if (System.getIntForUser(getContext().getContentResolver(), COLOR_TEMPERATURE_MODE, COLOR_TEMPERATURE_MODE_DEFAULT, UserHandle.myUserId()) == COLOR_TEMPERATURE_MODE_DEFAULT) {
            return true;
        }
        return false;
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        ContentResolver resolver = getContext().getContentResolver();
        if (positiveResult) {
            if (this.mIsDefultSetChecked) {
                System.putIntForUser(resolver, COLOR_TEMPERATURE, DEFAULT_TEMPERATURE, UserHandle.myUserId());
            } else {
                System.putIntForUser(resolver, COLOR_TEMPERATURE, this.mSeekBar.getProgress(), UserHandle.myUserId());
            }
            System.putIntForUser(getContext().getContentResolver(), COLOR_TEMPERATURE_MODE, this.mIsDefultSetChecked ? 1 : 0, UserHandle.myUserId());
        } else {
            restoreOldState();
        }
        try {
            getContext().unregisterReceiver(this.receiver);
        } catch (Exception e) {
            Log.w("ColorTemperatureSettingsPreference", "Can not unregister receiver.");
        }
    }

    private void restoreOldState() {
        if (!this.mRestoredOldState) {
            int i;
            if (this.mIsOldDefultSelected) {
                i = DEFAULT_TEMPERATURE;
            } else {
                i = this.mOldColorTemperature;
            }
            setColorTemperature(i);
            this.mRestoredOldState = true;
        }
    }

    private void setColorTemperature(int colorTemperature) {
        try {
            IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
            power.getClass().getMethod("setColorTemperature", new Class[]{Integer.TYPE}).invoke(power, new Object[]{Integer.valueOf(colorTemperature)});
        } catch (Exception doe) {
            Log.e("ColorTemperatureSettingsPreference", doe.getMessage());
        }
    }

    private void setMode(int mode) {
        if (mode == COLOR_TEMPERATURE_MODE_DEFAULT) {
            if (this.mSeekBar != null) {
                this.mSeekBar.setEnabled(false);
            }
        } else if (this.mSeekBar != null) {
            this.mSeekBar.setEnabled(true);
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.mIsDefaultSelected = this.mCheckBox.isChecked();
        myState.progress = this.mSeekBar.getProgress();
        myState.mIsOldDefaultSelected = this.mIsOldDefultSelected;
        myState.oldProgress = this.mOldColorTemperature;
        restoreOldState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        int i;
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.mOldColorTemperature = myState.oldProgress;
        this.mIsOldDefultSelected = myState.mIsOldDefaultSelected;
        setMode(myState.mIsDefaultSelected ? 1 : 0);
        if (myState.mIsDefaultSelected) {
            i = DEFAULT_TEMPERATURE;
        } else {
            i = myState.progress;
        }
        setColorTemperature(i);
    }

    public void onPause() {
        if (!(!(getDialog() != null ? getDialog().isShowing() : false) || this.mSeekBar == null || this.mCheckBox == null)) {
            this.pauseSeekprocess = this.mSeekBar.getProgress();
            this.pauseModeChecked = this.mIsDefultSetChecked;
        }
    }

    public void onResume() {
        boolean isShowing;
        if (getDialog() != null) {
            isShowing = getDialog().isShowing();
        } else {
            isShowing = false;
        }
        if (isShowing) {
            this.mRestoredOldState = false;
            if (!(this.mSeekBar == null || this.pauseSeekprocess < 0 || this.mCheckBox == null)) {
                int i;
                this.mSeekBar.setProgress(this.pauseSeekprocess);
                if (this.pauseModeChecked) {
                    i = DEFAULT_TEMPERATURE;
                } else {
                    i = this.pauseSeekprocess;
                }
                setColorTemperature(i);
                if (this.pauseModeChecked) {
                    i = 1;
                } else {
                    i = 0;
                }
                setMode(i);
            }
        }
    }
}
