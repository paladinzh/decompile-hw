package com.android.systemui.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;

public class BrightnessDialog extends Activity {
    private BrightnessController mBrightnessController;
    private boolean mRegisted = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityManager.isUserAMonkey()) {
            finish();
            return;
        }
        Window window = getWindow();
        window.setGravity(48);
        window.clearFlags(2);
        window.requestFeature(1);
        setContentView(R.layout.quick_settings_brightness_dialog);
        this.mBrightnessController = new BrightnessController(this, (ImageView) findViewById(R.id.brightness_icon), (ToggleSlider) findViewById(R.id.brightness_slider));
    }

    protected void onStart() {
        super.onStart();
        if (!(this.mBrightnessController == null || this.mRegisted)) {
            this.mBrightnessController.registerCallbacks();
            this.mRegisted = true;
        }
        MetricsLogger.visible(this, 220);
    }

    protected void onStop() {
        super.onStop();
        MetricsLogger.hidden(this, 220);
        if (this.mBrightnessController != null && this.mRegisted) {
            this.mBrightnessController.unregisterCallbacks();
            this.mRegisted = false;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!(keyCode == 25 || keyCode == 24)) {
            if (keyCode == 164) {
            }
            return super.onKeyDown(keyCode, event);
        }
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
