package com.android.contacts.hap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepContactsAliveService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }
}
