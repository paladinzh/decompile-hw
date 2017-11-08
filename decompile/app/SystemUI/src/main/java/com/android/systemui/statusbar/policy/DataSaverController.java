package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.NetworkPolicyManager;
import android.os.Handler;
import android.os.RemoteException;
import java.util.ArrayList;

public class DataSaverController {
    private final Handler mHandler = new Handler();
    private final ArrayList<Listener> mListeners = new ArrayList();
    private final INetworkPolicyListener mPolicyListener = new Stub() {
        public void onUidRulesChanged(int i, int i1) throws RemoteException {
        }

        public void onMeteredIfacesChanged(String[] strings) throws RemoteException {
        }

        public void onRestrictBackgroundChanged(final boolean isDataSaving) throws RemoteException {
            DataSaverController.this.mHandler.post(new Runnable() {
                public void run() {
                    DataSaverController.this.handleRestrictBackgroundChanged(isDataSaving);
                }
            });
        }

        public void onRestrictBackgroundWhitelistChanged(int uid, boolean whitelisted) {
        }

        public void onRestrictBackgroundBlacklistChanged(int uid, boolean blacklisted) {
        }
    };
    private final NetworkPolicyManager mPolicyManager;

    public interface Listener {
        void onDataSaverChanged(boolean z);
    }

    public DataSaverController(Context context) {
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    private void handleRestrictBackgroundChanged(boolean isDataSaving) {
        synchronized (this.mListeners) {
            for (int i = 0; i < this.mListeners.size(); i++) {
                ((Listener) this.mListeners.get(i)).onDataSaverChanged(isDataSaving);
            }
        }
    }

    public void addListener(Listener listener) {
        synchronized (this.mListeners) {
            this.mListeners.add(listener);
            if (this.mListeners.size() == 1) {
                this.mPolicyManager.registerListener(this.mPolicyListener);
            }
        }
        listener.onDataSaverChanged(isDataSaverEnabled());
    }

    public void remListener(Listener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
            if (this.mListeners.size() == 0) {
                this.mPolicyManager.unregisterListener(this.mPolicyListener);
            }
        }
    }

    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    public void setDataSaverEnabled(boolean enabled) {
        this.mPolicyManager.setRestrictBackground(enabled);
        try {
            this.mPolicyListener.onRestrictBackgroundChanged(enabled);
        } catch (RemoteException e) {
        }
    }
}
