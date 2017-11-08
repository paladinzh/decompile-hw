package com.avast.android.shepherd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.avast.android.shepherd.obfuscated.x;

/* compiled from: Unknown */
public class ShepherdReferralReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        x.b("ReferralReceiver: onReceive");
        if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
            String stringExtra = intent.getStringExtra("referrer");
            if (stringExtra != null && stringExtra.length() != 0) {
                stringExtra = Uri.decode(stringExtra);
                x.b("ReferralReceiver: decoded referral: " + stringExtra);
                if (stringExtra != null && stringExtra.contains("=")) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Shepherd.BUNDLE_PARAMS_REFERRER_STRING_KEY, stringExtra);
                    Shepherd.updateParams(bundle);
                }
            }
        }
    }
}
