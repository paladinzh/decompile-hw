package com.loc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* compiled from: Unknown */
final class ds extends BroadcastReceiver {
    ds(db dbVar) {
    }

    public final void onReceive(Context context, Intent intent) {
        if (intent != null) {
            try {
                String action = intent.getAction();
                if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                    dl.c = false;
                }
                if (action.equals("android.intent.action.MEDIA_UNMOUNTED")) {
                    dl.c = true;
                }
                if (action.equals("android.intent.action.MEDIA_EJECT")) {
                    dl.c = true;
                }
            } catch (Exception e) {
            }
        }
    }
}
