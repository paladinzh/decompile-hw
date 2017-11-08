package com.huawei.systemmanager;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.huawei.systemmanager.SubService.HsmBinder;
import com.huawei.systemmanager.netassistant.NetAssistantService;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;

@TargetApi(19)
public class CoreService extends Service {
    public static final String BINDER_NAME = "binder_name";
    private static final String TAG = "CoreService";
    private final Class<? extends SubService>[] SERVICES = new Class[]{NetAssistantService.class};
    private final Map<String, IBinder> mBinderMap = new ArrayMap();
    private SubService[] mSubServices = new SubService[this.SERVICES.length];

    public void onCreate() {
        super.onCreate();
        startSubService();
    }

    private void startSubService() {
        HwLog.v(TAG, "Starting SubServices");
        for (int i = 0; i < this.SERVICES.length; i++) {
            try {
                this.mSubServices[i] = (SubService) this.SERVICES[i].newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
            if (this.mSubServices[i] != null) {
                this.mSubServices[i].onCreate();
                HsmBinder sysBinder = this.mSubServices[i].onBind();
                try {
                    ServiceManager.addService(sysBinder.getBinderName(), sysBinder.getBinder());
                    this.mBinderMap.put(sysBinder.getBinderName(), sysBinder.getBinder());
                } catch (SecurityException e3) {
                    e3.printStackTrace();
                    HwLog.e(TAG, "add binder:" + sysBinder.getBinderName() + " failed!");
                }
            }
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 2;
        }
        for (SubService service : this.mSubServices) {
            service.onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        for (SubService service : this.mSubServices) {
            service.onConfigurationChanged(newConfig);
        }
        super.onConfigurationChanged(newConfig);
    }

    public IBinder onBind(Intent intent) {
        if (intent == null) {
            return null;
        }
        String binderName = intent.getStringExtra("binder_name");
        if (TextUtils.isEmpty(binderName)) {
            return null;
        }
        return (IBinder) this.mBinderMap.get(binderName);
    }

    public void onDestroy() {
        for (SubService service : this.mSubServices) {
            service.onDestroy();
        }
        super.onDestroy();
    }
}
