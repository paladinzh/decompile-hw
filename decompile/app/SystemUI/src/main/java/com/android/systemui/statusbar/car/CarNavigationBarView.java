package com.android.systemui.statusbar.car;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.NavigationBarView;

class CarNavigationBarView extends NavigationBarView {
    private LinearLayout mLightsOutButtons;
    private LinearLayout mNavButtons;

    public CarNavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onFinishInflate() {
        this.mNavButtons = (LinearLayout) findViewById(R.id.nav_buttons);
        this.mLightsOutButtons = (LinearLayout) findViewById(R.id.lights_out);
    }

    public void addButton(CarNavigationButton button, CarNavigationButton lightsOutButton) {
        this.mNavButtons.addView(button);
        this.mLightsOutButtons.addView(lightsOutButton);
    }

    public void setDisabledFlags(int disabledFlags, boolean force) {
    }

    public void reorient() {
    }

    public View getCurrentView() {
        return this;
    }

    public void setNavigationIconHints(int hints, boolean force) {
    }
}
