package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Matrix;
import android.util.MathUtils;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import java.util.ArrayList;

public class NightModeController implements Tunable {
    public static final float[] IDENTITY_MATRIX = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] NIGHT_VALUES = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.754f, 0.0f, 0.0f, 0.0f, 0.0f, 0.516f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private boolean mAdjustTint;
    private float mAmount;
    private final Context mContext;
    private float[] mCustomMatrix;
    private boolean mIsAuto;
    private boolean mIsNight;
    private final ArrayList<Listener> mListeners;
    private boolean mListening;
    private final BroadcastReceiver mReceiver;
    private final boolean mUpdateMatrix;

    public interface Listener {
        void onNightModeChanged();
    }

    public NightModeController(Context context) {
        this(context, false);
    }

    public NightModeController(Context context, boolean updateMatrix) {
        this.mListeners = new ArrayList();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.TWILIGHT_CHANGED".equals(intent.getAction())) {
                    NightModeController.this.updateNightMode(intent);
                    NightModeController.this.updateCurrentMatrix();
                    for (int i = 0; i < NightModeController.this.mListeners.size(); i++) {
                        ((Listener) NightModeController.this.mListeners.get(i)).onNightModeChanged();
                    }
                }
            }
        };
        this.mContext = context;
        this.mUpdateMatrix = updateMatrix;
        TunerService.get(this.mContext).addTunable((Tunable) this, "tuner_night_mode_adjust_tint", "tuner_color_custom_values", "twilight_mode");
    }

    public void setNightMode(boolean isNight) {
        if (!this.mIsAuto) {
            TunerService.get(this.mContext).setValue("twilight_mode", isNight ? 1 : 0);
        } else if (this.mIsNight != isNight) {
            int i;
            TunerService tunerService = TunerService.get(this.mContext);
            String str = "twilight_mode";
            if (isNight) {
                i = 4;
            } else {
                i = 3;
            }
            tunerService.setValue(str, i);
        } else {
            TunerService.get(this.mContext).setValue("twilight_mode", 2);
        }
    }

    public void setAuto(boolean auto) {
        this.mIsAuto = auto;
        if (auto) {
            TunerService.get(this.mContext).setValue("twilight_mode", 2);
        } else {
            TunerService.get(this.mContext).setValue("twilight_mode", this.mIsNight ? 1 : 0);
        }
    }

    public void setAdjustTint(Boolean newValue) {
        TunerService.get(this.mContext).setValue("tuner_night_mode_adjust_tint", newValue.booleanValue() ? 1 : 0);
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
        listener.onNightModeChanged();
        updateListening();
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
        updateListening();
    }

    private void updateListening() {
        boolean shouldListen = this.mListeners.size() == 0 ? this.mUpdateMatrix ? this.mAdjustTint : false : true;
        if (shouldListen != this.mListening) {
            this.mListening = shouldListen;
            if (this.mListening) {
                this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.TWILIGHT_CHANGED"));
            } else {
                this.mContext.unregisterReceiver(this.mReceiver);
            }
        }
    }

    public boolean isEnabled() {
        if (!this.mListening) {
            updateNightMode(this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.TWILIGHT_CHANGED")));
        }
        return this.mIsNight;
    }

    public String getCustomValues() {
        return TunerService.get(this.mContext).getValue("tuner_color_custom_values");
    }

    public void setCustomValues(String values) {
        TunerService.get(this.mContext).setValue("tuner_color_custom_values", values);
    }

    public void onTuningChanged(String key, String newValue) {
        float[] fArr = null;
        boolean z = false;
        if ("tuner_color_custom_values".equals(key)) {
            if (newValue != null) {
                fArr = toValues(newValue);
            }
            this.mCustomMatrix = fArr;
            updateCurrentMatrix();
        } else if ("tuner_night_mode_adjust_tint".equals(key)) {
            boolean z2;
            if (newValue == null || Integer.parseInt(newValue) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mAdjustTint = z2;
            updateListening();
            updateCurrentMatrix();
        } else if ("twilight_mode".equals(key)) {
            if (newValue != null && Integer.parseInt(newValue) >= 2) {
                z = true;
            }
            this.mIsAuto = z;
        }
    }

    private void updateCurrentMatrix() {
        float f = 0.0f;
        if (!this.mUpdateMatrix) {
            return;
        }
        if ((!this.mAdjustTint || this.mAmount == 0.0f) && this.mCustomMatrix == null) {
            TunerService.get(this.mContext).setValue("accessibility_display_color_matrix", null);
            return;
        }
        float[] fArr = IDENTITY_MATRIX;
        float[] fArr2 = NIGHT_VALUES;
        if (this.mAdjustTint) {
            f = this.mAmount;
        }
        float[] values = scaleValues(fArr, fArr2, f);
        if (this.mCustomMatrix != null) {
            values = multiply(values, this.mCustomMatrix);
        }
        TunerService.get(this.mContext).setValue("accessibility_display_color_matrix", toString(values));
    }

    private void updateNightMode(Intent intent) {
        float floatExtra;
        boolean z = false;
        if (intent != null) {
            z = intent.getBooleanExtra("isNight", false);
        }
        this.mIsNight = z;
        if (intent != null) {
            floatExtra = intent.getFloatExtra("amount", 0.0f);
        } else {
            floatExtra = 0.0f;
        }
        this.mAmount = floatExtra;
    }

    private static float[] multiply(float[] matrix, float[] other) {
        if (matrix == null) {
            return other;
        }
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, matrix, 0, other, 0);
        return result;
    }

    private float[] scaleValues(float[] identityMatrix, float[] nightValues, float amount) {
        float[] values = new float[identityMatrix.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = MathUtils.lerp(identityMatrix[i], nightValues[i], amount);
        }
        return values;
    }

    public static String toString(float[] values) {
        StringBuilder builder = new StringBuilder();
        for (float append : values) {
            if (builder.length() != 0) {
                builder.append(',');
            }
            builder.append(append);
        }
        return builder.toString();
    }

    public static float[] toValues(String customValues) {
        String[] strValues = customValues.split(",");
        float[] values = new float[strValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Float.parseFloat(strValues[i]);
        }
        return values;
    }
}
