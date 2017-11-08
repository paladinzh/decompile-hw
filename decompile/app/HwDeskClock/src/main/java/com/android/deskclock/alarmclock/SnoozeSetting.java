package com.android.deskclock.alarmclock;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import com.android.deskclock.R;

public class SnoozeSetting extends DialogPreference {
    private View mView;

    public SnoozeSetting(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.setting_dialog);
        createActionButtons();
    }

    public void createActionButtons() {
        setPositiveButtonText(17039370);
        setNegativeButtonText(17039360);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.mView = view;
        this.mView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldRight != right) {
                    SnoozeSetting.this.initTextLocation();
                }
            }
        });
    }

    private void initTextLocation() {
    }

    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }
}
