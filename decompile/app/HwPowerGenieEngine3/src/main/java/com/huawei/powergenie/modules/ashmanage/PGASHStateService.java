package com.huawei.powergenie.modules.ashmanage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.powergenie.modules.apppower.hibernation.ASHManager;
import com.huawei.powergenie.modules.apppower.hibernation.ASHStateInterface;
import com.huawei.powergenie.modules.apppower.hibernation.IPGASHStateService.Stub;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PGASHStateService extends Service {
    private PGASHStateImpl mASHStateImpl;

    private class PGASHStateImpl extends Stub {
        private PGASHStateImpl() {
        }

        public Map getAppsState(List<String> packageNames) throws RemoteException {
            if (1000 != UserHandle.getAppId(Binder.getCallingUid())) {
                Log.e("PGASHStateService", "client has no permission. client uid:" + Binder.getCallingUid());
                return null;
            }
            HashMap<String, Integer> map = new HashMap();
            ASHStateInterface stateInterface = ASHManager.getPGDebugUI();
            if (stateInterface == null) {
                return null;
            }
            Map<String, AppStateRecord> smartHibernationApps = stateInterface.getApplicationMap();
            if (smartHibernationApps == null) {
                return null;
            }
            Iterator<String> it;
            if (packageNames == null) {
                it = smartHibernationApps.keySet().iterator();
            } else {
                it = packageNames.iterator();
            }
            while (it.hasNext()) {
                int state;
                String pkgName = (String) it.next();
                AppStateRecord app = (AppStateRecord) smartHibernationApps.get(pkgName);
                if (app == null) {
                    state = -3;
                } else {
                    state = (int) (((app.getDuration() / 1000) << 4) | ((long) PGASHStateService.this.stringStateToInt(app.getStateName())));
                }
                map.put(pkgName, Integer.valueOf(state));
            }
            return map;
        }

        public boolean hibernateApps(List<String> pkgNames) throws RemoteException {
            Log.i("PGASHStateService", "Service side, hibernateApps is called by uid:" + Binder.getCallingUid());
            boolean ret = true;
            if (1000 != UserHandle.getAppId(Binder.getCallingUid())) {
                Log.e("PGASHStateService", "client has no permission. client uid:" + Binder.getCallingUid());
                return false;
            }
            ASHStateInterface stateInterface = ASHManager.getPGDebugUI();
            if (stateInterface == null) {
                return false;
            }
            Map<String, AppStateRecord> smartHibernationApps = stateInterface.getApplicationMap();
            if (smartHibernationApps == null) {
                return false;
            }
            Iterator<String> it;
            if (pkgNames == null) {
                try {
                    it = smartHibernationApps.keySet().iterator();
                } catch (Exception e) {
                    Log.e("PGASHStateService", "Service side, requestHibernate call throws exception!");
                    e.printStackTrace();
                }
            } else {
                it = pkgNames.iterator();
            }
            while (it.hasNext()) {
                AppStateRecord app = (AppStateRecord) smartHibernationApps.get((String) it.next());
                if (app == null || !app.requestHibernate("debugui")) {
                    ret = false;
                }
            }
            return ret;
        }

        public boolean wakeupApps(List<String> pkgNames) throws RemoteException {
            Log.i("PGASHStateService", "Service side, wakeupApps is called by uid:" + Binder.getCallingUid());
            boolean ret = true;
            if (1000 != UserHandle.getAppId(Binder.getCallingUid())) {
                Log.e("PGASHStateService", "client has no permission. client uid:" + Binder.getCallingUid());
                return false;
            }
            ASHStateInterface stateInterface = ASHManager.getPGDebugUI();
            if (stateInterface == null) {
                return false;
            }
            Map<String, AppStateRecord> smartHibernationApps = stateInterface.getApplicationMap();
            if (smartHibernationApps == null) {
                return false;
            }
            Iterator<String> it;
            if (pkgNames == null) {
                try {
                    it = smartHibernationApps.keySet().iterator();
                } catch (Exception e) {
                    Log.e("PGASHStateService", "Service side, requestRunning call throws exception!");
                    e.printStackTrace();
                }
            } else {
                it = pkgNames.iterator();
            }
            while (it.hasNext()) {
                AppStateRecord app = (AppStateRecord) smartHibernationApps.get((String) it.next());
                if (app == null || !app.requestRunning("client")) {
                    ret = false;
                }
            }
            return ret;
        }
    }

    public IBinder onBind(Intent intent) {
        if (this.mASHStateImpl == null) {
            this.mASHStateImpl = new PGASHStateImpl();
        }
        return this.mASHStateImpl;
    }

    private int stringStateToInt(String stateStr) {
        int intState = 3;
        if (stateStr == null) {
            return 3;
        }
        if (stateStr.equals("running")) {
            intState = 0;
        } else if (stateStr.equals("doze")) {
            intState = 1;
        } else if (stateStr.equals("hibernation")) {
            intState = 2;
        }
        return intState;
    }
}
