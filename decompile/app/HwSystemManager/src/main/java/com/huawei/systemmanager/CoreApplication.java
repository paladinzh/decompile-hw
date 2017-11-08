package com.huawei.systemmanager;

import android.app.Application;
import android.content.Intent;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public class CoreApplication extends Application {
    public void onCreate() {
        super.onCreate();
        GlobalContext.setContext(this);
        startService(new Intent(this, CoreService.class));
    }
}
