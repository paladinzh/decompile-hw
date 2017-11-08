package com.huawei.netassistant.util;

import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.util.HwLog;

public class NetworkSessionUtil {
    private static final String TAG = "NetworkSessionUtil";
    private INetworkStatsService mStatsService = Stub.asInterface(ServiceManager.getService("netstats"));
    private INetworkStatsSession mStatsSession;

    public void closeSession() {
        if (this.mStatsSession != null) {
            try {
                this.mStatsSession.close();
            } catch (RemoteException localRemoteException) {
                HwLog.e(TAG, "Remote exception:", localRemoteException);
            } catch (Exception localException) {
                HwLog.e(TAG, "Unknown exception:", localException);
            }
        }
    }

    public void resetNetStatsService() {
        this.mStatsService = Stub.asInterface(ServiceManager.getService("netstats"));
    }

    public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid, int set, int tag, int fields) throws RemoteException {
        if (this.mStatsSession == null) {
            openSession();
        }
        if (this.mStatsSession != null) {
            return this.mStatsSession.getHistoryForUid(template, uid, set, tag, fields);
        }
        return null;
    }

    public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end, boolean includeTags) throws RemoteException {
        if (this.mStatsSession == null) {
            openSession();
        }
        if (this.mStatsSession != null) {
            return this.mStatsSession.getSummaryForAllUid(template, start, end, includeTags);
        }
        return null;
    }

    public NetworkStatsHistory getHistoryForNetwork(NetworkTemplate template, int fields) throws RemoteException {
        if (this.mStatsSession == null) {
            openSession();
        }
        if (this.mStatsSession != null) {
            return this.mStatsSession.getHistoryForNetwork(template, fields);
        }
        return null;
    }

    public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) throws RemoteException {
        if (this.mStatsSession == null) {
            openSession();
        }
        if (this.mStatsSession != null) {
            return this.mStatsSession.getSummaryForNetwork(template, start, end);
        }
        return null;
    }

    public void advisePersistThreshold(long thresholdBytes) {
        try {
            this.mStatsService.advisePersistThreshold(thresholdBytes);
        } catch (RemoteException localRemoteException) {
            HwLog.e(TAG, "Remote exception:", localRemoteException);
        } catch (Exception exception) {
            HwLog.e(TAG, "Exception:", exception);
        }
    }

    public void openSession() {
        try {
            this.mStatsSession = this.mStatsService.openSession();
        } catch (RemoteException localRemoteException) {
            HwLog.e(TAG, "Remote exception:", localRemoteException);
        } catch (Exception exception) {
            HwLog.e(TAG, "Exception:", exception);
        }
    }

    public void forceUpdate() {
        try {
            this.mStatsService.forceUpdate();
        } catch (RemoteException localRemoteException) {
            HwLog.e(TAG, "Remote exception:", localRemoteException);
        } catch (Exception exception) {
            HwLog.e(TAG, "Exception:", exception);
        }
    }
}
