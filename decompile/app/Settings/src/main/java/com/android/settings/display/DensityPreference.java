package com.android.settings.display;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.View;
import android.widget.EditText;
import com.android.settings.CustomEditTextPreference;
import com.android.settingslib.display.DisplayDensityUtils;

public class DensityPreference extends CustomEditTextPreference {
    public DensityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onAttached() {
        super.onAttached();
        setSummary(getContext().getString(2131627218, new Object[]{Integer.valueOf(getCurrentSwDp())}));
    }

    private int getCurrentSwDp() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return (int) (((float) Math.min(metrics.widthPixels, metrics.heightPixels)) / metrics.density);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = (EditText) view.findViewById(16908291);
        if (editText != null) {
            editText.setInputType(2);
            editText.setText(getCurrentSwDp() + "");
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            try {
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                DisplayDensityUtils.setForcedDisplayDensity(0, Math.max((Math.min(metrics.widthPixels, metrics.heightPixels) * 160) / Math.max(Integer.parseInt(getText()), 320), 120));
            } catch (Exception e) {
                Slog.e("DensityPreference", "Couldn't save density", e);
            }
        }
    }
}
