package com.huawei.powergenie.core;

import android.net.NetworkInfo;
import android.net.Uri;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.InputManager.InputListener;
import com.huawei.powergenie.core.modulesmanager.ModuleManager;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class BaseService implements InputListener {
    private String mLastPkg;
    private long mLastTimestamp;
    private ModuleManager mModMgmt = null;

    public void onInputMsgEvent(MsgEvent evt) {
    }

    public void onInputHookEvent(HookEvent evt) {
    }

    protected void notifyPowerActionChanged(ICoreContext context, PowerAction action) {
        if (action.getType() == 1) {
            putActionExtra((StateAction) action);
        }
        if (this.mModMgmt == null) {
            this.mModMgmt = (ModuleManager) context.getService("module");
        }
        this.mModMgmt.putPowerAction(action);
    }

    private void putActionExtra(StateAction action) {
        switch (action.getActionId()) {
            case 305:
            case 306:
            case 307:
                Uri data = action.getIntent().getData();
                if (data != null) {
                    String pkgName = data.getSchemeSpecificPart();
                    if (pkgName != null) {
                        action.putExtra(pkgName);
                        return;
                    }
                    return;
                }
                return;
            case 308:
                action.putExtra(action.getIntent().getIntExtra("level", 0));
                return;
            case 312:
                NetworkInfo networkInfo = (NetworkInfo) action.getIntent().getParcelableExtra("networkInfo");
                if (networkInfo != null) {
                    action.putExtra(networkInfo.getType());
                    return;
                }
                return;
            case 356:
                boolean pending = action.getIntent().getBooleanExtra("enable", true);
                ArrayList<String> applist = action.getIntent().getStringArrayListExtra("applist");
                int type = action.getIntent().getIntExtra("type", -1);
                action.putExtra(pending);
                action.putExtra((ArrayList) applist);
                action.putExtra(type);
                return;
            case 357:
                boolean state = action.getIntent().getBooleanExtra("ctrl_socket_status", true);
                String applistStr = action.getIntent().getStringExtra("ctrl_socket_list");
                action.putExtra(state);
                action.putExtra(applistStr);
                return;
            case 358:
                String pushType = action.getIntent().getStringExtra("pushType");
                String uri = action.getIntent().getStringExtra("uri");
                action.putExtra("pushType", pushType);
                action.putExtra("uri", uri);
                return;
            default:
                return;
        }
    }

    protected void addAction(ICoreContext context, int actionId) {
        if (this.mModMgmt == null) {
            this.mModMgmt = (ModuleManager) context.getService("module");
        }
        this.mModMgmt.addAction(this, actionId);
    }

    public boolean handleAction(PowerAction action) {
        if (action.getType() == 5) {
            ScenarioAction stAction = (ScenarioAction) action;
            if (stAction.getStateType() == 0) {
                if (stAction.getActionId() == 208 && this.mLastTimestamp == stAction.getTimeStamp() && this.mLastPkg != null && this.mLastPkg.equals(stAction.getPkgName())) {
                    return false;
                }
                this.mLastPkg = stAction.getPkgName();
                this.mLastTimestamp = stAction.getTimeStamp();
            }
        }
        return true;
    }

    public void dump(PrintWriter pw, String[] args) {
    }
}
