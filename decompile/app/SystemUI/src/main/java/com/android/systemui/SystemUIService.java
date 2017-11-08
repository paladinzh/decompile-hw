package com.android.systemui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SystemUIService extends Service {
    public void onCreate() {
        super.onCreate();
        ((SystemUIApplication) getApplication()).startServicesIfNeeded();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i = 0;
        SystemUI[] services = ((SystemUIApplication) getApplication()).getServices();
        int length;
        if (args == null || args.length == 0) {
            length = services.length;
            while (i < length) {
                SystemUI ui = services[i];
                pw.println("dumping service: " + ui.getClass().getName());
                ui.dump(fd, pw, args);
                i++;
            }
            return;
        }
        String svc = args[0];
        for (SystemUI ui2 : services) {
            if (ui2.getClass().getName().endsWith(svc)) {
                ui2.dump(fd, pw, args);
            }
        }
    }
}
