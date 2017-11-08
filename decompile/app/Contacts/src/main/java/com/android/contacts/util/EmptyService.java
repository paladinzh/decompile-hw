package com.android.contacts.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EmptyService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }
}
