package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.util.HwLog;

public abstract class DataSaverFWKApi {
    private String TAG = DataSaverFWKApi.class.getSimpleName();
    protected Context mContext;
    private final INetworkPolicyManager mIPolicyManager;
    final PackageManager mPm;
    private final INetworkPolicyListener mPolicyListener = new Stub() {
        public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
        }

        public void onRestrictBackgroundBlacklistChanged(int uid, boolean blacklisted) {
            DataSaverFWKApi.this.onBlacklistChanged(uid, blacklisted);
        }

        public void onRestrictBackgroundWhitelistChanged(int uid, boolean whitelisted) {
            DataSaverFWKApi.this.onWhitelistRefreshed(uid, whitelisted);
        }

        public void onMeteredIfacesChanged(String[] strings) throws RemoteException {
        }

        public void onRestrictBackgroundChanged(boolean enable) throws RemoteException {
            DataSaverFWKApi.this.onDataSaverStateChanged(enable);
        }
    };
    private final NetworkPolicyManager mPolicyManager;

    protected abstract void onBlacklistChanged(int i, boolean z);

    protected abstract void onDataSaverStateChanged(boolean z);

    protected abstract void onWhitelistRefreshed(int i, boolean z);

    public DataSaverFWKApi(Context context) {
        this.mContext = context;
        this.mPolicyManager = NetworkPolicyManager.from(context);
        this.mIPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));
        this.mPm = this.mContext.getPackageManager();
    }

    public void addRestrictBackgroundWhitelistedUid(int uid, String packageName) {
        try {
            this.mIPolicyManager.addRestrictBackgroundWhitelistedUid(uid);
        } catch (RemoteException e) {
            HwLog.w(this.TAG, "Can't reach policy manager", e);
        }
    }

    public void removeRestrictBackgroundWhitelistedUid(int uid, String packageName) {
        try {
            this.mIPolicyManager.removeRestrictBackgroundWhitelistedUid(uid);
        } catch (RemoteException e) {
            HwLog.w(this.TAG, "Can't reach policy manager", e);
        }
    }

    public int[] getRestrictBackgroundWhitelistedUids() {
        int[] uids = null;
        try {
            uids = this.mIPolicyManager.getRestrictBackgroundWhitelistedUids();
        } catch (RemoteException e) {
            HwLog.w(this.TAG, "Can't reach policy manager", e);
        }
        return uids;
    }

    public int[] getUidsWithPolicy() {
        int[] uids = null;
        try {
            uids = this.mIPolicyManager.getUidsWithPolicy(1);
        } catch (RemoteException e) {
            HwLog.w(this.TAG, "Can't reach policy manager", e);
        }
        return uids;
    }

    public void setDataSaverEnable(boolean enable) {
        this.mPolicyManager.setRestrictBackground(enable);
    }

    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    public void unRegisterListener() {
        this.mPolicyManager.unregisterListener(this.mPolicyListener);
    }

    public void registerListener() {
        this.mPolicyManager.registerListener(this.mPolicyListener);
    }
}
