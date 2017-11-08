package com.huawei.powergenie.core.server;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.powergenie.api.ActionsExportMap;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.Watchdog;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public final class SdkService extends BaseService implements ISdkService {
    private static SocketServer mSocketServer;
    private BinderServer mBinderServer;
    private final ICoreContext mICoreContext;
    private PlugManager mPlugManager;
    private SdkThreadHandler mSdkHandler = null;

    private class SdkThreadHandler extends Handler {
        public SdkThreadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HashMap<String, String> data = msg.obj;
                    dispatchAction(msg.arg1, (String) data.get("pkg"), (String) data.get("extend1"), (String) data.get("extend2"));
                    return;
                default:
                    return;
            }
        }

        private void dispatchAction(int exportActionId, String pkg, String extend1, String extend2) {
            if (SdkService.this.mPlugManager != null) {
                SdkService.this.mPlugManager.handleActionInner(exportActionId, pkg, extend1, extend2);
            }
            if (SdkService.mSocketServer != null) {
                SdkService.mSocketServer.handleActionInner(exportActionId, pkg, extend1, extend2);
            }
            if (SdkService.this.mBinderServer != null) {
                SdkService.this.mBinderServer.handleActionInner(exportActionId, pkg, extend1, extend2);
            }
        }
    }

    public SdkService(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
    }

    public void start() {
        this.mPlugManager = new PlugManager(this.mICoreContext.getContext());
        this.mBinderServer = new BinderServer(this.mICoreContext);
        ServiceManager.addService("powergenius", this.mBinderServer);
        mSocketServer = new SocketServer();
        mSocketServer.start();
        HandlerThread handlerThread = new HandlerThread("sdk thread");
        handlerThread.start();
        this.mSdkHandler = new SdkThreadHandler(handlerThread.getLooper());
        addSdkActions();
        Watchdog.getInstance().addThread(this.mSdkHandler);
    }

    private void addSdkActions() {
        ArrayList<Integer> allActions = ActionsExportMap.getAllActions();
        if (allActions != null) {
            for (Integer action : allActions) {
                addAction(this.mICoreContext, action.intValue());
            }
        }
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        if (!this.mPlugManager.hasClients() && !mSocketServer.hasClients() && !this.mBinderServer.needAction()) {
            return true;
        }
        int actionId = action.getActionId();
        if (!mSocketServer.hasClients() && actionId == 224) {
            return true;
        }
        switch (action.getType()) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
            case 5:
                int exportActionId = ActionsExportMap.getExportActionID(actionId);
                if (!(exportActionId == -1 || this.mSdkHandler == null)) {
                    String extend1 = "";
                    String extend2 = "";
                    if (actionId == 224) {
                        extend1 = action.getExtraString();
                        extend2 = String.valueOf(action.getExtraInt());
                    } else if (actionId == 350) {
                        extend1 = String.valueOf(action.getExtraInt());
                    }
                    Message msg = this.mSdkHandler.obtainMessage(100);
                    msg.arg1 = exportActionId;
                    HashMap<String, String> data = new HashMap();
                    data.put("pkg", action.getPkgName());
                    data.put("extend1", extend1);
                    data.put("extend2", extend2);
                    msg.obj = data;
                    this.mSdkHandler.sendMessage(msg);
                }
                return true;
            default:
                Log.w("SdkService", "action type unknown!!!");
                return true;
        }
    }

    public void handleStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
        if (this.mBinderServer != null) {
            this.mBinderServer.handleStateChanged(stateType, eventType, pid, pkg, uid);
        }
    }
}
