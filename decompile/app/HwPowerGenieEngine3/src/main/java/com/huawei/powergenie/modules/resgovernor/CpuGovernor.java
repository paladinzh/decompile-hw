package com.huawei.powergenie.modules.resgovernor;

import android.util.Log;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.PowerAction;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CpuGovernor extends BaseModule {
    private CpuGovernorPolicy mCpuGovPolicy;

    public void onCreate() {
        super.onCreate();
        this.mCpuGovPolicy = getCpuPolicy();
    }

    public void onStart() {
        super.onStart();
        this.mCpuGovPolicy.handleStart();
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        int actionId = action.getActionId();
        if (actionId == 350) {
            this.mCpuGovPolicy.handlePowerMode(action.getExtraInt());
            return true;
        } else if (actionId == 361) {
            this.mCpuGovPolicy.handleVRMode(action.getExtraBoolean());
            return true;
        } else {
            long expireMs = System.currentTimeMillis() - action.getTimeStamp();
            if (expireMs >= 8000) {
                if (actionId == 251) {
                    Log.w("CpuGovernor", "action:" + actionId + "delay " + expireMs + "ms");
                } else {
                    Log.w("CpuGovernor", "discards action:" + actionId + " timeout:" + expireMs + "ms");
                    return true;
                }
            }
            if (319 == actionId && ((IDeviceState) getCoreContext().getService("device")).isCharging()) {
                Log.d("CpuGovernor", "it is charging now, do not handle battery low event!");
                return true;
            }
            this.mCpuGovPolicy.handleCpuAction(action, getActionSubFlag(action));
            return true;
        }
    }

    private int getActionSubFlag(PowerAction action) {
        switch (action.getActionId()) {
            case 210:
            case 221:
            case 238:
            case 246:
            case 267:
                return 1;
            case 211:
            case 239:
            case 244:
            case 247:
            case 268:
                return 2;
            case 234:
            case 236:
                return action.getExtraBoolean() ? 1 : 2;
            default:
                return action.getSubFlag();
        }
    }

    private CpuGovernorPolicy getCpuPolicy() {
        if (getCoreContext().isHisiPlatform()) {
            return K3V3CpuGovernorPolicy.getInstance(getCoreContext(), this);
        }
        return MsmCpuGovernorPolicy.getInstance(getCoreContext(), this);
    }

    protected void registerActions(ArrayList<Integer> actionIds) {
        removeAllActions();
        if (actionIds != null) {
            for (Integer actId : actionIds) {
                addAction(actId.intValue());
                addEndAction(actId.intValue());
            }
        }
        addAction(350);
        addAction(361);
        addAction(251);
        addAction(319);
        addAction(320);
        addAction(208);
    }

    private void addEndAction(int actionId) {
        Integer endID = (Integer) PowerAction.mSubActionMap.get(Integer.valueOf(actionId));
        if (endID != null) {
            addAction(endID.intValue());
        }
    }

    public boolean dump(PrintWriter pw, String[] args) {
        pw.println("\nPGMODULE CpuGovernor: ");
        this.mCpuGovPolicy.dump(pw, args);
        return true;
    }
}
