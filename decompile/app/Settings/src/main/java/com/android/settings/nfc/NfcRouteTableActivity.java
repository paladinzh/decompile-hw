package com.android.settings.nfc;

import android.content.Intent;
import com.android.settings.SettingsActivity;
import java.util.HashMap;

public class NfcRouteTableActivity extends SettingsActivity {
    public Intent getIntent() {
        Intent intent = super.getIntent();
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", NfcRouteTable.class.getName());
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (NfcRouteTable.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        NfcRouteTable.reflectInvokeNxpNfcAdapter("updateServiceState", new HashMap());
    }

    public void onBackPressed() {
        super.onBackPressed();
        NfcRouteTable.reflectInvokeNxpNfcAdapter("updateServiceState", new HashMap());
    }
}
