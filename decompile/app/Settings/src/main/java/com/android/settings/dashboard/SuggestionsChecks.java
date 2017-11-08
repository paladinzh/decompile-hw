package com.android.settings.dashboard;

import android.app.AutomaticZenRule;
import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.app.IWallpaperManagerCallback.Stub;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.ims.ImsManager;
import com.android.settings.Settings.FingerprintEnrollSuggestionActivity;
import com.android.settings.Settings.FingerprintSuggestionActivity;
import com.android.settings.Settings.ScreenLockSuggestionActivity;
import com.android.settings.Settings.WallpaperSuggestionActivity;
import com.android.settings.Settings.WifiCallingSuggestionActivity;
import com.android.settings.Settings.ZenModeAutomationSuggestionActivity;
import com.android.settingslib.drawer.Tile;

public class SuggestionsChecks {
    private final IWallpaperManagerCallback mCallback = new Stub() {
        public void onWallpaperChanged() throws RemoteException {
        }

        public void onBlurWallpaperChanged() throws RemoteException {
        }
    };
    private final Context mContext;

    public SuggestionsChecks(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public boolean isSuggestionComplete(Tile suggestion) {
        String className = suggestion.intent.getComponent().getClassName();
        if (className.equals(ZenModeAutomationSuggestionActivity.class.getName())) {
            return hasEnabledZenAutoRules();
        }
        if (className.equals(WallpaperSuggestionActivity.class.getName())) {
            return hasWallpaperSet();
        }
        if (className.equals(WifiCallingSuggestionActivity.class.getName())) {
            return isWifiCallingUnavailableOrEnabled();
        }
        if (className.equals(FingerprintSuggestionActivity.class.getName())) {
            return isNotSingleFingerprintEnrolled();
        }
        if (className.equals(ScreenLockSuggestionActivity.class.getName()) || className.equals(FingerprintEnrollSuggestionActivity.class.getName())) {
            return isDeviceSecured();
        }
        return false;
    }

    private boolean isDeviceSecured() {
        return ((KeyguardManager) this.mContext.getSystemService(KeyguardManager.class)).isKeyguardSecure();
    }

    private boolean isNotSingleFingerprintEnrolled() {
        FingerprintManager manager = (FingerprintManager) this.mContext.getSystemService(FingerprintManager.class);
        if (manager == null || manager.getEnrolledFingerprints().size() != 1) {
            return true;
        }
        return false;
    }

    public boolean isWifiCallingUnavailableOrEnabled() {
        if (!ImsManager.isWfcEnabledByPlatform(this.mContext)) {
            return true;
        }
        boolean isNonTtyOrTtyOnVolteEnabled;
        if (ImsManager.isWfcEnabledByUser(this.mContext)) {
            isNonTtyOrTtyOnVolteEnabled = ImsManager.isNonTtyOrTtyOnVolteEnabled(this.mContext);
        } else {
            isNonTtyOrTtyOnVolteEnabled = false;
        }
        return isNonTtyOrTtyOnVolteEnabled;
    }

    private boolean hasEnabledZenAutoRules() {
        for (AutomaticZenRule rule : NotificationManager.from(this.mContext).getAutomaticZenRules().values()) {
            if (rule.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWallpaperSet() {
        boolean z = true;
        try {
            if (IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).getWallpaper(this.mCallback, 1, new Bundle(), this.mContext.getUserId()) == null) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }
}
