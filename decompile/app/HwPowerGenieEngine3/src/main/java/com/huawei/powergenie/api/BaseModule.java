package com.huawei.powergenie.api;

import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.ScenarioAction;
import java.io.PrintWriter;

public abstract class BaseModule {
    private ICoreContext mICoreContext;
    private String mLastPkg;
    private long mLastTimestamp;
    private int mModId;

    public final void attach(ICoreContext context, int modId) {
        if (this.mICoreContext == null) {
            this.mICoreContext = context;
            this.mModId = modId;
        }
    }

    public ICoreContext getCoreContext() {
        return this.mICoreContext;
    }

    public int getModId() {
        return this.mModId;
    }

    public void onCreate() {
    }

    public void onStart() {
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

    public void addAction(int actionId) {
        this.mICoreContext.addAction(this.mModId, actionId);
    }

    public void removeAction(int actionId) {
        this.mICoreContext.removeAction(this.mModId, actionId);
    }

    public void removeAllActions() {
        this.mICoreContext.removeAllActions(this.mModId);
    }

    public int getPowerMode() {
        IPolicy policy = (IPolicy) this.mICoreContext.getService("policy");
        if (policy != null) {
            return policy.getPowerMode();
        }
        return 2;
    }

    public boolean dump(PrintWriter pw, String[] args) {
        return true;
    }
}
