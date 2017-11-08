package com.android.systemui.settings;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.systemui.R;

public class HwCustToggleSliderImpl extends HwCustToggleSlider {
    private TextView mLabel;
    private Sensor mSensorLight;
    private SensorManager mSensors;
    private CompoundButton mToggle;

    public void isNotShowBrightnessSwitch(View view, Context context) {
        this.mSensors = (SensorManager) context.getSystemService("sensor");
        this.mSensorLight = this.mSensors.getDefaultSensor(5);
        this.mToggle = (CompoundButton) view.findViewById(R.id.toggle);
        this.mLabel = (TextView) view.findViewById(R.id.label);
        if (this.mSensorLight == null) {
            this.mToggle.setVisibility(8);
            this.mLabel.setVisibility(8);
        }
    }
}
