package com.huawei.powergenie.core;

import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.integration.adapter.pged.KStateInterface;
import com.huawei.powergenie.integration.adapter.pged.KStateMonitor;
import com.huawei.powergenie.integration.adapter.pged.PgedAdapterFactory;
import java.util.ArrayList;

public final class KStateManager extends BaseService {
    private ArrayList<Integer> mAllKStateActions = new ArrayList();
    private final ICoreContext mICoreContext;
    private KStateInterface mKStateAdapter;
    private boolean mMonitoring = false;

    private class KStateMonitorImpl implements KStateMonitor {
        private KStateMonitorImpl() {
        }

        public void onKStateEvent(int type, int value1, String value2, ArrayList<Integer> value3) {
            int actionId = 0;
            int pid = 0;
            ArrayList uids = null;
            String value = "";
            ArrayList<Integer> uids2;
            switch (type) {
                case 8:
                    if (!"KILLED".equals(value2)) {
                        if ("NET".equals(value2)) {
                            actionId = 261;
                            pid = value1;
                            value = value2;
                            uids2 = value3;
                            break;
                        }
                    }
                    actionId = 260;
                    pid = value1;
                    value = value2;
                    uids = value3;
                    break;
                    break;
                case 16:
                    actionId = 274;
                    pid = value1;
                    value = value2;
                    uids2 = value3;
                    break;
                default:
                    Log.w("KStateManager", "unknown kernel state action !");
                    return;
            }
            if (KStateManager.this.mAllKStateActions.contains(Integer.valueOf(actionId))) {
                KStateManager.this.notifyPowerActionChanged(KStateManager.this.mICoreContext, KStateManager.this.createKStateAction(actionId, pid, uids, value));
            } else {
                Log.w("KStateManager", "discards the kstate action: " + actionId);
            }
        }
    }

    public KStateManager(ICoreContext context) {
        this.mICoreContext = context;
    }

    public void start() {
        if (this.mKStateAdapter == null) {
            this.mKStateAdapter = PgedAdapterFactory.getKStateAdapter(new KStateMonitorImpl());
        }
        if (!this.mMonitoring) {
            if (this.mKStateAdapter.registerHwPgedListener()) {
                this.mMonitoring = true;
            } else {
                Log.e("KStateManager", "the hwpged does not exist or crashed now!");
            }
        }
    }

    public void addMonitorAction(int actionId) {
        Log.i("KStateManager", "monitor kstate action: " + actionId);
        if (!this.mAllKStateActions.contains(Integer.valueOf(actionId))) {
            this.mAllKStateActions.add(Integer.valueOf(actionId));
            if (actionId == 260 || actionId == 261) {
                this.mKStateAdapter.openKState(8);
            } else if (actionId != 262 && actionId == 274) {
                this.mKStateAdapter.openKState(16);
            }
        }
    }

    public void removeMonitorAction(int actionId) {
        Log.i("KStateManager", "remove kstate action: " + actionId);
        this.mAllKStateActions.remove(Integer.valueOf(actionId));
        if (actionId == 260 || actionId == 261) {
            if (!this.mAllKStateActions.contains(Integer.valueOf(260)) && !this.mAllKStateActions.contains(Integer.valueOf(261))) {
                this.mKStateAdapter.closeKState(8);
            }
        } else if (actionId != 262 && actionId == 274) {
            this.mKStateAdapter.closeKState(16);
        }
    }

    private KStateAction createKStateAction(int actionId, int pid, ArrayList<Integer> uids, String value) {
        KStateAction kAction = KStateAction.obtain();
        if (kAction != null) {
            kAction.resetAs(actionId, pid, uids, value);
        }
        return kAction;
    }
}
