package com.android.settings.nfc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settingslib.RestrictedPreference;

public class NfcEnabler implements OnPreferenceChangeListener {
    private final RestrictedPreference mAndroidBeam;
    private boolean mBeamDisallowedBySystem;
    private final NfcAdapter mNfcAdapter;
    private final SwitchPreference mSwitch;

    /* renamed from: com.android.settings.nfc.NfcEnabler$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ NfcEnabler this$0;

        public void onReceive(Context context, Intent intent) {
            if ("android.nfc.action.ADAPTER_STATE_CHANGED".equals(intent.getAction())) {
                this.this$0.handleNfcStateChanged(intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1));
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean desiredState = ((Boolean) value).booleanValue();
        this.mSwitch.setEnabled(false);
        if (desiredState) {
            this.mNfcAdapter.enable();
        } else {
            this.mNfcAdapter.disable();
        }
        return false;
    }

    private void handleNfcStateChanged(int newState) {
        switch (newState) {
            case 1:
                this.mSwitch.setChecked(false);
                this.mSwitch.setEnabled(true);
                this.mAndroidBeam.setEnabled(false);
                this.mAndroidBeam.setSummary(2131624897);
                return;
            case 2:
                this.mSwitch.setChecked(true);
                this.mSwitch.setEnabled(false);
                this.mAndroidBeam.setEnabled(false);
                return;
            case 3:
                this.mSwitch.setChecked(true);
                this.mSwitch.setEnabled(true);
                if (this.mBeamDisallowedBySystem) {
                    this.mAndroidBeam.setDisabledByAdmin(null);
                    this.mAndroidBeam.setEnabled(false);
                } else {
                    this.mAndroidBeam.checkRestrictionAndSetDisabled("no_outgoing_beam");
                }
                if (this.mNfcAdapter.isNdefPushEnabled() && this.mAndroidBeam.isEnabled()) {
                    this.mAndroidBeam.setSummary(2131624895);
                    return;
                } else {
                    this.mAndroidBeam.setSummary(2131624896);
                    return;
                }
            case 4:
                this.mSwitch.setChecked(false);
                this.mSwitch.setEnabled(false);
                this.mAndroidBeam.setEnabled(false);
                return;
            default:
                return;
        }
    }
}
