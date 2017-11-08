package com.huawei.hwid.api.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.cloudservice.b.a;
import com.huawei.hwid.core.d.b.e;

class q implements ServiceConnection {
    final /* synthetic */ o a;

    q(o oVar) {
        this.a = oVar;
    }

    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        boolean z = false;
        e.b("CloudAccountServiceHandle", "onServiceConnected---");
        int i = 0;
        while (i < 20) {
            this.a.a = a.a(iBinder);
            try {
                if (this.a.a == null) {
                    Thread.sleep(200);
                    i++;
                }
            } catch (InterruptedException e) {
                e.b("CloudAccountServiceHandle", "service cannot connected");
            }
        }
        try {
            this.a.a.a(this.a.e, this.a.f, this.a.h);
        } catch (RemoteException e2) {
            e.d("CloudAccountServiceHandle", "remote exception");
        }
        String str = "CloudAccountServiceHandle";
        StringBuilder append = new StringBuilder().append("onServiceConnected---mICloudAccount=");
        if (this.a.a != null) {
            z = true;
        }
        e.b(str, append.append(z).toString());
    }

    public void onServiceDisconnected(ComponentName componentName) {
        e.b("CloudAccountServiceHandle", "onServiceDisconnected");
        this.a.a = null;
    }
}
