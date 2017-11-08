package com.android.systemui.recents;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.systemui.SystemUIApplication;

public class RecentsSystemUserService extends Service {
    public void onCreate() {
        super.onCreate();
    }

    public IBinder onBind(Intent intent) {
        Recents recents = (Recents) ((SystemUIApplication) getApplication()).getComponent(Recents.class);
        if (recents != null) {
            return recents.getSystemUserCallbacks();
        }
        return null;
    }
}
