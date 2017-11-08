package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class EmergencyCarrierArea extends AlphaOptimizedLinearLayout {
    private CarrierText mCarrierText;
    private EmergencyButton mEmergencyButton;

    public EmergencyCarrierArea(Context context) {
        super(context);
    }

    public EmergencyCarrierArea(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCarrierText = (CarrierText) findViewById(R$id.carrier_text);
        this.mEmergencyButton = (EmergencyButton) findViewById(R$id.emergency_call_button);
        this.mEmergencyButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (EmergencyCarrierArea.this.mCarrierText.getVisibility() != 0) {
                    return false;
                }
                switch (event.getAction()) {
                    case 0:
                        EmergencyCarrierArea.this.mCarrierText.animate().alpha(0.0f);
                        break;
                    case 1:
                        EmergencyCarrierArea.this.mCarrierText.animate().alpha(1.0f);
                        break;
                }
                return false;
            }
        });
    }

    public void setCarrierTextVisible(boolean visible) {
        this.mCarrierText.setVisibility(visible ? 0 : 8);
    }
}
