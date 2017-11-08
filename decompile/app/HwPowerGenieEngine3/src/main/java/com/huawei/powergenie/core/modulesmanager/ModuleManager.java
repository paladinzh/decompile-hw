package com.huawei.powergenie.core.modulesmanager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.KStateManager;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.Watchdog;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public final class ModuleManager extends BaseService {
    private static final boolean DBG_ACT = (Log.isLoggable("ACT", 2));
    private final HashMap<Integer, Integer> mActionOccurNum = new HashMap();
    private ActionsHandler mActionsHandler;
    private Looper mActionsHandlerLooper;
    private boolean mAllModulesReady = false;
    private final ArrayList<Integer> mAllRegisterActions = new ArrayList();
    private final ICoreContext mICoreContext;
    private final ArrayList<PowerAction> mPendingAction = new ArrayList();
    private final HashMap<Integer, ArrayList<Integer>> mRegisterActions = new HashMap();
    private final HashMap<BaseService, ArrayList<Integer>> mRegisterActionsForServices = new HashMap();
    private final HashMap<Integer, ArrayList<Integer>> mRegisterActionsFrequent = new HashMap();
    private final HashMap<Integer, ArrayList<Integer>> mRegisterKStateActions = new HashMap();
    private ArrayList<BaseModule> mRunningModules;
    private long mSpentTime;

    private final class ActionsHandler extends Handler {
        public ActionsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ModuleManager.this.initAllModules();
                    break;
                case 101:
                    PowerAction action = msg.obj;
                    ModuleManager.this.dispatchPowerActionToModules(action);
                    ModuleManager.this.dispatchPowerActionToServices(action);
                    action.recycle();
                    break;
                case 102:
                    synchronized (ModuleManager.this.mAllRegisterActions) {
                        for (PowerAction pendingAction : ModuleManager.this.mPendingAction) {
                            ModuleManager.this.dispatchPowerActionToModules(pendingAction);
                            ModuleManager.this.dispatchPowerActionToServices(pendingAction);
                            pendingAction.recycle();
                        }
                        ModuleManager.this.mPendingAction.clear();
                    }
                    break;
                case 103:
                    break;
                case 104:
                    getLooper().quit();
                    break;
                default:
                    Log.w("ModuleManager", "what=" + msg.what);
                    return;
            }
        }
    }

    public ModuleManager(ICoreContext context) {
        this.mICoreContext = context;
    }

    public void start() {
        HandlerThread thread = new HandlerThread("actions handler", 10);
        thread.start();
        this.mActionsHandlerLooper = thread.getLooper();
        this.mActionsHandler = new ActionsHandler(this.mActionsHandlerLooper);
        this.mActionsHandler.sendEmptyMessage(100);
        Watchdog.getInstance().addThread(this.mActionsHandler);
    }

    private void initAllModules() {
        long start = SystemClock.uptimeMillis();
        ModuleLifecycle modulesHandler = new ModuleLifecycle(this.mICoreContext.getContext());
        modulesHandler.loadAll(this.mICoreContext);
        modulesHandler.handleStart();
        this.mRunningModules = modulesHandler.getRunningModules();
        this.mAllModulesReady = true;
        if (this.mPendingAction.size() > 0) {
            this.mActionsHandler.sendEmptyMessage(102);
        }
        Log.i("ModuleManager", "init all modules expend: " + (SystemClock.uptimeMillis() - start) + "ms");
    }

    public Looper getActionsHandlerLooper() {
        return this.mActionsHandlerLooper;
    }

    public synchronized void addAction(int modId, int actionId) {
        HashMap<Integer, ArrayList<Integer>> targetActions = this.mRegisterActions;
        if (isKStateAction(actionId)) {
            targetActions = this.mRegisterKStateActions;
        }
        ArrayList<Integer> actions = (ArrayList) targetActions.get(Integer.valueOf(modId));
        if (actions == null) {
            actions = new ArrayList();
            actions.add(Integer.valueOf(actionId));
            targetActions.put(Integer.valueOf(modId), actions);
        } else if (!actions.contains(Integer.valueOf(actionId))) {
            actions.add(Integer.valueOf(actionId));
        }
        synchronized (this.mAllRegisterActions) {
            if (!this.mAllRegisterActions.contains(Integer.valueOf(actionId))) {
                this.mAllRegisterActions.add(Integer.valueOf(actionId));
            }
        }
        if (isKStateAction(actionId)) {
            ((KStateManager) this.mICoreContext.getService("kstate")).addMonitorAction(actionId);
        }
    }

    public synchronized void removeAction(int modId, int actionId) {
        ArrayList<Integer> actions = (ArrayList) this.mRegisterActions.get(Integer.valueOf(modId));
        if (actions != null) {
            actions.remove(Integer.valueOf(actionId));
        }
        actions = (ArrayList) this.mRegisterKStateActions.get(Integer.valueOf(modId));
        if (actions != null) {
            actions.remove(Integer.valueOf(actionId));
        }
        if (isKStateAction(actionId) && noKStateAction(actionId)) {
            ((KStateManager) this.mICoreContext.getService("kstate")).removeMonitorAction(actionId);
        }
    }

    public synchronized void removeAllActions(int modId) {
        Log.d("ModuleManager", "remove all actions for mod: " + modId);
        ArrayList<Integer> actions = (ArrayList) this.mRegisterActions.remove(Integer.valueOf(modId));
        actions = (ArrayList) this.mRegisterKStateActions.remove(Integer.valueOf(modId));
        if (actions != null) {
            KStateManager kstate = (KStateManager) this.mICoreContext.getService("kstate");
            for (Integer actId : actions) {
                if (noKStateAction(actId.intValue())) {
                    kstate.removeMonitorAction(actId.intValue());
                }
            }
        }
    }

    private boolean noKStateAction(int actionId) {
        for (Entry entry : this.mRegisterKStateActions.entrySet()) {
            if (((ArrayList) entry.getValue()).contains(Integer.valueOf(actionId))) {
                return false;
            }
        }
        return true;
    }

    private boolean isKStateAction(int actionId) {
        switch (actionId) {
            case 260:
            case 261:
            case 274:
                return true;
            default:
                return false;
        }
    }

    private void dispatchPowerActionToModules(PowerAction action) {
        if (this.mRunningModules == null) {
            Log.w("ModuleManager", "no module running.");
            return;
        }
        if (DBG_ACT) {
            this.mSpentTime = SystemClock.uptimeMillis();
        }
        int actionId = action.getActionId();
        HashMap<Integer, ArrayList<Integer>> targetActions = this.mRegisterActions;
        for (BaseModule module : this.mRunningModules) {
            if (isKStateAction(actionId)) {
                targetActions = this.mRegisterKStateActions;
            }
            ArrayList<Integer> actionIds = (ArrayList) targetActions.get(Integer.valueOf(module.getModId()));
            if (actionIds != null && actionIds.contains(Integer.valueOf(actionId))) {
                module.handleAction(action);
            }
        }
        if (DBG_ACT) {
            Log.i("ModuleManager", "To modules spent: " + (SystemClock.uptimeMillis() - this.mSpentTime) + "ms for action:" + action.getActionId());
        }
    }

    private void dispatchPowerActionToServices(PowerAction action) {
        if (DBG_ACT) {
            this.mSpentTime = SystemClock.uptimeMillis();
        }
        int actionId = action.getActionId();
        for (Entry entry : this.mRegisterActionsForServices.entrySet()) {
            if (((ArrayList) entry.getValue()).contains(Integer.valueOf(actionId))) {
                ((BaseService) entry.getKey()).handleAction(action);
            }
        }
        if (DBG_ACT) {
            Log.i("ModuleManager", "To services spent: " + (SystemClock.uptimeMillis() - this.mSpentTime) + "ms for action:" + action.getActionId());
        }
    }

    public synchronized void addAction(BaseService service, int actionId) {
        if (service != null) {
            ArrayList<Integer> actions = (ArrayList) this.mRegisterActionsForServices.get(service);
            if (actions == null) {
                actions = new ArrayList();
                actions.add(Integer.valueOf(actionId));
                this.mRegisterActionsForServices.put(service, actions);
            } else if (!actions.contains(Integer.valueOf(actionId))) {
                actions.add(Integer.valueOf(actionId));
            }
            synchronized (this.mAllRegisterActions) {
                if (!this.mAllRegisterActions.contains(Integer.valueOf(actionId))) {
                    this.mAllRegisterActions.add(Integer.valueOf(actionId));
                }
            }
        }
    }

    public void putPowerAction(PowerAction action) {
        synchronized (this.mAllRegisterActions) {
            if (this.mAllRegisterActions.contains(Integer.valueOf(action.getActionId()))) {
                this.mActionsHandler.sendMessageDelayed(this.mActionsHandler.obtainMessage(101, action), 0);
            } else if (this.mAllModulesReady) {
                if (DBG_ACT) {
                    Log.d("ModuleManager", "not care the action: " + action);
                }
                action.recycle();
            } else {
                Log.d("ModuleManager", "delay to handle action: " + action);
                this.mPendingAction.add(action);
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        for (Entry entry : this.mActionOccurNum.entrySet()) {
            Integer num = (Integer) entry.getValue();
            pw.println("    action: " + ((Integer) entry.getKey()) + "  num: " + num);
        }
        if (this.mRunningModules == null) {
            Log.w("ModuleManager", "no module running.");
            return;
        }
        Integer modId = Integer.valueOf(-1);
        if (args.length == 0 || args[0].equals("modules")) {
            Log.d("ModuleManager", "dump all modules");
        } else if (args[0].equals("cpu")) {
            modId = Integer.valueOf(2);
        } else {
            Log.w("ModuleManager", "no support this " + args[0] + " mode.");
            return;
        }
        for (BaseModule module : this.mRunningModules) {
            if (modId.intValue() == -1 || module.getModId() == modId.intValue()) {
                module.dump(pw, args);
            }
        }
    }
}
