package com.android.systemui.statusbar.phone;

import android.app.PendingIntent;
import android.content.Intent;

public interface ActivityStarter {

    public interface Callback {
        void onActivityStarted(int i);
    }

    void preventNextAnimation();

    void startActivity(Intent intent, boolean z);

    void startActivity(Intent intent, boolean z, Callback callback);

    void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent);
}
