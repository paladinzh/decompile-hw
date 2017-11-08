package com.android.systemui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.systemui.utils.HwLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SystemUISecondaryUserService extends Service {
    public void onCreate() {
        super.onCreate();
        ((SystemUIApplication) getApplication()).startSecondaryUserServicesIfNeeded();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i = 0;
        SystemUI[] services = ((SystemUIApplication) getApplication()).getServices();
        if (args != null) {
            try {
                if (args.length != 0) {
                    String svc = args[0];
                    for (SystemUI ui : services) {
                        SystemUI ui2;
                        if (ui2 != null && ui2.getClass().getName().endsWith(svc)) {
                            ui2.dump(fd, pw, args);
                        }
                    }
                    return;
                }
            } catch (Exception e) {
                HwLog.e("SystemUISecondaryUserService", "dump exception", e);
                return;
            }
        }
        int length = services.length;
        while (i < length) {
            ui2 = services[i];
            if (ui2 != null) {
                pw.println("dumping service: " + ui2.getClass().getName());
                ui2.dump(fd, pw, args);
            }
            i++;
        }
    }
}
