package com.android.server.ethernet;

import android.content.Context;
import android.net.EthernetManager;
import android.net.IEthernetServiceListener;
import android.net.InterfaceConfiguration;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.StaticIpConfiguration;
import android.net.ip.IpManager;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;

class EthernetNetworkFactory {
    private static final boolean DBG = true;
    private static final int NETWORK_SCORE = 70;
    private static final String NETWORK_TYPE = "Ethernet";
    private static final String TAG = "EthernetNetworkFactory";
    private static String mIface = "";
    private static String mIfaceMatch = "";
    private static boolean mLinkUp;
    private Context mContext;
    private EthernetManager mEthernetManager;
    private LocalNetworkFactory mFactory;
    private String mHwAddr;
    private InterfaceObserver mInterfaceObserver;
    private IpManager mIpManager;
    private Thread mIpProvisioningThread;
    private LinkProperties mLinkProperties = new LinkProperties();
    private final RemoteCallbackList<IEthernetServiceListener> mListeners;
    private INetworkManagementService mNMService;
    private NetworkAgent mNetworkAgent;
    private NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");

    private class InterfaceObserver extends BaseNetworkObserver {
        private InterfaceObserver() {
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            EthernetNetworkFactory.this.updateInterfaceState(iface, up);
        }

        public void interfaceAdded(String iface) {
            EthernetNetworkFactory.this.maybeTrackInterface(iface);
        }

        public void interfaceRemoved(String iface) {
            EthernetNetworkFactory.this.stopTrackingInterface(iface);
        }
    }

    private class LocalNetworkFactory extends NetworkFactory {
        LocalNetworkFactory(String name, Context context, Looper looper) {
            super(looper, context, name, new NetworkCapabilities());
        }

        protected void startNetwork() {
            EthernetNetworkFactory.this.onRequestNetwork();
        }

        protected void stopNetwork() {
        }
    }

    EthernetNetworkFactory(RemoteCallbackList<IEthernetServiceListener> listeners) {
        initNetworkCapabilities();
        this.mListeners = listeners;
    }

    private void stopIpManagerLocked() {
        if (this.mIpManager != null) {
            this.mIpManager.shutdown();
            this.mIpManager = null;
        }
    }

    private void stopIpProvisioningThreadLocked() {
        stopIpManagerLocked();
        if (this.mIpProvisioningThread != null) {
            this.mIpProvisioningThread.interrupt();
            this.mIpProvisioningThread = null;
        }
    }

    private void updateInterfaceState(String iface, boolean up) {
        if (mIface.equals(iface)) {
            Log.d(TAG, "updateInterface: " + iface + " link " + (up ? "up" : "down"));
            synchronized (this) {
                int i;
                mLinkUp = up;
                this.mNetworkInfo.setIsAvailable(up);
                if (!up) {
                    this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
                    stopIpProvisioningThreadLocked();
                }
                updateAgent();
                LocalNetworkFactory localNetworkFactory = this.mFactory;
                if (up) {
                    i = NETWORK_SCORE;
                } else {
                    i = -1;
                }
                localNetworkFactory.setScoreFilter(i);
            }
        }
    }

    private void setInterfaceUp(String iface) {
        try {
            this.mNMService.setInterfaceUp(iface);
            InterfaceConfiguration config = this.mNMService.getInterfaceConfig(iface);
            if (config == null) {
                Log.e(TAG, "Null iterface config for " + iface + ". Bailing out.");
                return;
            }
            synchronized (this) {
                if (isTrackingInterface()) {
                    Log.e(TAG, "Interface unexpectedly changed from " + iface + " to " + mIface);
                    this.mNMService.setInterfaceDown(iface);
                } else {
                    setInterfaceInfoLocked(iface, config.getHardwareAddress());
                    this.mNetworkInfo.setIsAvailable(DBG);
                    this.mNetworkInfo.setExtraInfo(this.mHwAddr);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error upping interface " + mIface + ": " + e);
        }
    }

    private boolean maybeTrackInterface(String iface) {
        if (!iface.matches(mIfaceMatch) || isTrackingInterface()) {
            return false;
        }
        Log.d(TAG, "Started tracking interface " + iface);
        setInterfaceUp(iface);
        return DBG;
    }

    private void stopTrackingInterface(String iface) {
        if (iface.equals(mIface)) {
            Log.d(TAG, "Stopped tracking interface " + iface);
            synchronized (this) {
                stopIpProvisioningThreadLocked();
                setInterfaceInfoLocked("", null);
                this.mNetworkInfo.setExtraInfo(null);
                mLinkUp = false;
                this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
                updateAgent();
                this.mNetworkAgent = null;
                this.mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");
                this.mLinkProperties = new LinkProperties();
            }
        }
    }

    private boolean setStaticIpAddress(StaticIpConfiguration staticConfig) {
        if (staticConfig.ipAddress == null || staticConfig.gateway == null || staticConfig.dnsServers.size() <= 0) {
            Log.e(TAG, "Invalid static IP configuration.");
        } else {
            try {
                Log.i(TAG, "Applying static IPv4 configuration to " + mIface + ": " + staticConfig);
                InterfaceConfiguration config = this.mNMService.getInterfaceConfig(mIface);
                config.setLinkAddress(staticConfig.ipAddress);
                this.mNMService.setInterfaceConfig(mIface, config);
                return DBG;
            } catch (Exception e) {
                Log.e(TAG, "Setting static IP address failed: " + e.getMessage());
            }
        }
        return false;
    }

    public void updateAgent() {
        synchronized (this) {
            if (this.mNetworkAgent == null) {
                return;
            }
            int i;
            Log.i(TAG, "Updating mNetworkAgent with: " + this.mNetworkCapabilities + ", " + this.mNetworkInfo + ", " + this.mLinkProperties);
            this.mNetworkAgent.sendNetworkCapabilities(this.mNetworkCapabilities);
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
            NetworkAgent networkAgent = this.mNetworkAgent;
            if (mLinkUp) {
                i = NETWORK_SCORE;
            } else {
                i = 0;
            }
            networkAgent.sendNetworkScore(i);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRequestNetwork() {
        synchronized (this) {
            if (this.mIpProvisioningThread != null) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void start(Context context, Handler target) {
        this.mNMService = Stub.asInterface(ServiceManager.getService("network_management"));
        this.mEthernetManager = (EthernetManager) context.getSystemService("ethernet");
        mIfaceMatch = context.getResources().getString(17039411);
        this.mFactory = new LocalNetworkFactory(NETWORK_TYPE, context, target.getLooper());
        this.mFactory.setCapabilityFilter(this.mNetworkCapabilities);
        this.mFactory.setScoreFilter(-1);
        this.mFactory.register();
        this.mContext = context;
        this.mInterfaceObserver = new InterfaceObserver();
        try {
            this.mNMService.registerObserver(this.mInterfaceObserver);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not register InterfaceObserver " + e);
        }
        try {
            String[] ifaces = this.mNMService.listInterfaces();
            int i = 0;
            int length = ifaces.length;
            while (i < length) {
                String iface = ifaces[i];
                synchronized (this) {
                    if (maybeTrackInterface(iface)) {
                        if (this.mNMService.getInterfaceConfig(iface).hasFlag("running")) {
                            updateInterfaceState(iface, DBG);
                        }
                    }
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "Could not get list of interfaces " + e2);
        }
    }

    public synchronized void stop() {
        stopIpProvisioningThreadLocked();
        this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
        mLinkUp = false;
        updateAgent();
        this.mLinkProperties = new LinkProperties();
        this.mNetworkAgent = null;
        setInterfaceInfoLocked("", null);
        this.mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");
        this.mFactory.unregister();
    }

    private void initNetworkCapabilities() {
        this.mNetworkCapabilities = new NetworkCapabilities();
        this.mNetworkCapabilities.addTransportType(3);
        this.mNetworkCapabilities.addCapability(12);
        this.mNetworkCapabilities.addCapability(13);
        this.mNetworkCapabilities.setLinkUpstreamBandwidthKbps(100000);
        this.mNetworkCapabilities.setLinkDownstreamBandwidthKbps(100000);
    }

    public synchronized boolean isTrackingInterface() {
        return TextUtils.isEmpty(mIface) ? false : DBG;
    }

    private void setInterfaceInfoLocked(String iface, String hwAddr) {
        boolean oldAvailable = isTrackingInterface();
        mIface = iface;
        this.mHwAddr = hwAddr;
        boolean available = isTrackingInterface();
        if (oldAvailable != available) {
            int n = this.mListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((IEthernetServiceListener) this.mListeners.getBroadcastItem(i)).onAvailabilityChanged(available);
                } catch (RemoteException e) {
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    synchronized void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        if (isTrackingInterface()) {
            pw.println("Tracking interface: " + mIface);
            pw.increaseIndent();
            pw.println("MAC address: " + this.mHwAddr);
            pw.println("Link state: " + (mLinkUp ? "up" : "down"));
            pw.decreaseIndent();
        } else {
            pw.println("Not tracking any interface");
        }
        pw.println();
        pw.println("NetworkInfo: " + this.mNetworkInfo);
        pw.println("LinkProperties: " + this.mLinkProperties);
        pw.println("NetworkAgent: " + this.mNetworkAgent);
        if (this.mIpManager != null) {
            pw.println("IpManager:");
            pw.increaseIndent();
            this.mIpManager.dump(fd, pw, args);
            pw.decreaseIndent();
        }
    }
}
