package com.android.systemui.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.settings.ToggleSlider.Listener;
import java.util.ArrayList;

public class BrightnessController implements Listener {
    private boolean mAutomatic;
    private final boolean mAutomaticAvailable;
    private final BrightnessObserver mBrightnessObserver;
    private ArrayList<BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList();
    private final Context mContext;
    private final ToggleSlider mControl;
    private boolean mExternalChange;
    private final Handler mHandler;
    private final ImageView mIcon;
    private boolean mListening;
    private final int mMaximumBacklight;
    private final int mMinimumBacklight;
    private final IPowerManager mPower;
    private final CurrentUserTracker mUserTracker;

    private class BrightnessObserver extends ContentObserver {
        private final Uri BRIGHTNESS_ADJ_URI = System.getUriFor("screen_auto_brightness_adj");
        private final Uri BRIGHTNESS_MODE_URI = System.getUriFor("screen_brightness_mode");
        private final Uri BRIGHTNESS_URI = System.getUriFor("screen_brightness");

        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (!selfChange) {
                try {
                    BrightnessController.this.mExternalChange = true;
                    if (this.BRIGHTNESS_MODE_URI.equals(uri)) {
                        BrightnessController.this.updateMode();
                        BrightnessController.this.updateSlider();
                    } else if (this.BRIGHTNESS_URI.equals(uri) && !BrightnessController.this.mAutomatic) {
                        BrightnessController.this.updateSlider();
                    } else if (this.BRIGHTNESS_ADJ_URI.equals(uri) && BrightnessController.this.mAutomatic) {
                        BrightnessController.this.updateSlider();
                    } else {
                        BrightnessController.this.updateMode();
                        BrightnessController.this.updateSlider();
                    }
                    for (BrightnessStateChangeCallback cb : BrightnessController.this.mChangeCallbacks) {
                        cb.onBrightnessLevelChanged();
                    }
                } finally {
                    BrightnessController.this.mExternalChange = false;
                }
            }
        }

        public void startObserving() {
            ContentResolver cr = BrightnessController.this.mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(this.BRIGHTNESS_MODE_URI, false, this, -1);
            cr.registerContentObserver(this.BRIGHTNESS_URI, false, this, -1);
            cr.registerContentObserver(this.BRIGHTNESS_ADJ_URI, false, this, -1);
        }

        public void stopObserving() {
            BrightnessController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public interface BrightnessStateChangeCallback {
        void onBrightnessLevelChanged();
    }

    public BrightnessController(Context context, ImageView icon, ToggleSlider control) {
        this.mContext = context;
        this.mIcon = icon;
        this.mControl = control;
        this.mHandler = new Handler();
        this.mUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int newUserId) {
                BrightnessController.this.updateMode();
                BrightnessController.this.updateSlider();
            }
        };
        this.mBrightnessObserver = new BrightnessObserver(this.mHandler);
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
        this.mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();
        this.mAutomaticAvailable = context.getResources().getBoolean(17956900);
        this.mPower = Stub.asInterface(ServiceManager.getService("power"));
    }

    public void onInit(ToggleSlider control) {
    }

    public void registerCallbacks() {
        if (!this.mListening) {
            this.mBrightnessObserver.startObserving();
            this.mUserTracker.startTracking();
            updateMode();
            updateSlider();
            this.mControl.setOnChangedListener(this);
            this.mListening = true;
        }
    }

    public void unregisterCallbacks() {
        if (this.mListening) {
            this.mBrightnessObserver.stopObserving();
            this.mUserTracker.stopTracking();
            this.mControl.setOnChangedListener(null);
            this.mListening = false;
        }
    }

    public void onChanged(ToggleSlider view, boolean tracking, boolean automatic, int value, boolean stopTracking) {
        updateIcon(this.mAutomatic);
        if (!this.mExternalChange) {
            if (this.mAutomatic) {
                final float adj = (((float) value) / 1024.0f) - 1.0f;
                if (stopTracking) {
                    MetricsLogger.action(this.mContext, 219, value);
                }
                setBrightnessAdj(adj);
                if (!tracking) {
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            System.putFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_auto_brightness_adj", adj, -2);
                        }
                    });
                }
            } else {
                final int val = value + this.mMinimumBacklight;
                if (stopTracking) {
                    MetricsLogger.action(this.mContext, 218, val);
                }
                setBrightness(val);
                if (!tracking) {
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            System.putIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness", val, -2);
                        }
                    });
                }
            }
            for (BrightnessStateChangeCallback cb : this.mChangeCallbacks) {
                cb.onBrightnessLevelChanged();
            }
        }
    }

    private void setBrightness(int brightness) {
        try {
            this.mPower.setTemporaryScreenBrightnessSettingOverride(brightness);
        } catch (RemoteException e) {
        }
    }

    private void setBrightnessAdj(float adj) {
        try {
            this.mPower.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(adj);
        } catch (RemoteException e) {
        }
    }

    private void updateIcon(boolean automatic) {
        if (this.mIcon != null) {
            ImageView imageView = this.mIcon;
            if (automatic) {
                imageView.setImageResource(R.drawable.ic_qs_brightness_auto_off);
            } else {
                imageView.setImageResource(R.drawable.ic_qs_brightness_auto_off);
            }
        }
    }

    private void updateMode() {
        boolean z = false;
        if (this.mAutomaticAvailable) {
            if (System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2) != 0) {
                z = true;
            }
            this.mAutomatic = z;
            updateIcon(this.mAutomatic);
            return;
        }
        this.mControl.setChecked(false);
        updateIcon(false);
    }

    private void updateSlider() {
        if (this.mAutomatic) {
            float value = System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, -2);
            this.mControl.setMax(2048);
            this.mControl.setValue((int) (((1.0f + value) * 2048.0f) / 2.0f));
            return;
        }
        int value2 = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mMaximumBacklight, -2);
        this.mControl.setMax(this.mMaximumBacklight - this.mMinimumBacklight);
        this.mControl.setValue(value2 - this.mMinimumBacklight);
    }
}
