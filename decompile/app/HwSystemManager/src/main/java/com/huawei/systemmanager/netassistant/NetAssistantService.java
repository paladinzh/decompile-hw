package com.huawei.systemmanager.netassistant;

import android.annotation.TargetApi;
import android.util.ArrayMap;
import com.huawei.systemmanager.SubService;
import com.huawei.systemmanager.SubService.HsmBinder;
import com.huawei.systemmanager.Task;
import com.huawei.systemmanager.netassistant.task.ForegroundAppChangeMonitor;
import com.huawei.systemmanager.netassistant.task.PackageChangeMonitor;
import com.huawei.systemmanager.netassistant.task.ServiceStateMonitor;
import com.huawei.systemmanager.util.HwLog;

@TargetApi(22)
public class NetAssistantService extends SubService {
    private static final String TAG = "NetAssistantService";
    private final Class<Task>[] TASKS = new Class[]{ServiceStateMonitor.class, PackageChangeMonitor.class, ForegroundAppChangeMonitor.class};
    private NetAssistantBinder mBinder;
    private ArrayMap<String, Task> mTasks = new ArrayMap();

    public void onCreate() {
        super.onCreate();
        HwLog.i("NetAssistantService", "create NetAssistant Service");
        for (Class newInstance : this.TASKS) {
            Task task = null;
            try {
                task = (Task) newInstance.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
            if (task != null) {
                this.mTasks.put(task.getName(), task);
                task.init();
            }
        }
        this.mBinder = new NetAssistantBinder((ServiceStateMonitor) this.mTasks.get(ServiceStateMonitor.TAG));
        registerListeners();
    }

    public void onDestroy() {
        unRegisterListeners();
        super.onDestroy();
    }

    private void registerListeners() {
        for (Task task : this.mTasks.values()) {
            task.registerListener();
        }
    }

    private void unRegisterListeners() {
        for (Task task : this.mTasks.values()) {
            task.unRegisterListener();
        }
    }

    public HsmBinder onBind() {
        return new HsmBinder("com.huawei.netassistant.binder.notificationcallbackbinder", this.mBinder);
    }
}
