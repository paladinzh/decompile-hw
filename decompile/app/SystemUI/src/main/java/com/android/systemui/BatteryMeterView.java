package com.android.systemui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

public class BatteryMeterView extends ImageView implements BatteryStateChangeCallback, Tunable {
    private BatteryController mBatteryController;
    private final BatteryMeterDrawable mDrawable;
    private final String mSlotBattery;

    public BatteryMeterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray atts = context.obtainStyledAttributes(attrs, R$styleable.BatteryMeterView, defStyle, 0);
        this.mDrawable = new BatteryMeterDrawable(context, new Handler(), atts.getColor(0, context.getColor(R.color.batterymeter_frame_color)));
        atts.recycle();
        this.mSlotBattery = context.getString(17039407);
        setImageDrawable(this.mDrawable);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void onTuningChanged(String key, String newValue) {
        if ("icon_blacklist".equals(key)) {
            setVisibility(StatusBarIconController.getIconBlacklist(newValue).contains(this.mSlotBattery) ? 8 : 0);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mBatteryController != null) {
            this.mBatteryController.addStateChangedCallback(this);
        }
        if (this.mDrawable != null) {
            this.mDrawable.startListening();
        }
        TunerService.get(getContext()).addTunable((Tunable) this, "icon_blacklist");
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mBatteryController != null) {
            this.mBatteryController.removeStateChangedCallback(this);
        }
        if (this.mDrawable != null) {
            this.mDrawable.stopListening();
        }
        TunerService.get(getContext()).removeTunable(this);
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        int i;
        Context context = getContext();
        if (charging) {
            i = R.string.accessibility_battery_level_charging;
        } else {
            i = R.string.accessibility_battery_level;
        }
        setContentDescription(context.getString(i, new Object[]{Integer.valueOf(level)}));
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
    }
}
