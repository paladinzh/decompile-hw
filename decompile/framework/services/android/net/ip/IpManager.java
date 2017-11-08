package android.net.ip;

import android.content.Context;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.LinkProperties.ProvisioningChange;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.apf.ApfCapabilities;
import android.net.apf.ApfFilter;
import android.net.dhcp.DhcpClient;
import android.net.dhcp.HwDhcpClient;
import android.net.metrics.IpManagerEvent;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.net.NetlinkTracker;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Objects;
import java.util.StringJoiner;

public class IpManager extends StateMachine {
    private static final /* synthetic */ int[] -android-net-LinkProperties$ProvisioningChangeSwitchesValues = null;
    private static final String CLAT_PREFIX = "v4-";
    private static final int CMD_CONFIRM = 3;
    private static final int CMD_SET_MULTICAST_FILTER = 8;
    private static final int CMD_START = 2;
    private static final int CMD_STOP = 1;
    private static final int CMD_UPDATE_HTTP_PROXY = 7;
    private static final int CMD_UPDATE_TCP_BUFFER_SIZES = 6;
    private static final boolean DBG = false;
    public static final String DUMP_ARG = "ipmanager";
    private static final int EVENT_DHCPACTION_TIMEOUT = 10;
    private static final int EVENT_NETLINK_LINKPROPERTIES_CHANGED = 5;
    private static final int EVENT_PRE_DHCP_ACTION_COMPLETE = 4;
    private static final int EVENT_PROVISIONING_TIMEOUT = 9;
    private static final int MAX_LOG_RECORDS = 500;
    private static final boolean NO_CALLBACKS = false;
    private static final boolean SEND_CALLBACKS = true;
    private static final boolean VDBG = false;
    private static final Class[] sMessageClasses = new Class[]{IpManager.class, DhcpClient.class};
    private static final SparseArray<String> sWhatToString = MessageUtils.findMessageNames(sMessageClasses);
    private boolean forceDhcpDiscovery;
    private ApfFilter mApfFilter;
    protected final Callback mCallback;
    private final String mClatInterfaceName;
    private ProvisioningConfiguration mConfiguration;
    private final Context mContext;
    private final WakeupMessage mDhcpActionTimeoutAlarm;
    private DhcpClient mDhcpClient;
    private DhcpResults mDhcpResults;
    private int mDhcp_Flag;
    private ProxyInfo mHttpProxy;
    private final String mInterfaceName;
    private IpReachabilityMonitor mIpReachabilityMonitor;
    private LinkProperties mLinkProperties;
    private final LocalLog mLocalLog;
    private boolean mMulticastFiltering;
    private final NetlinkTracker mNetlinkTracker;
    private NetworkInterface mNetworkInterface;
    private final INetworkManagementService mNwService;
    private final WakeupMessage mProvisioningTimeoutAlarm;
    private long mStartTimeMillis;
    private final State mStartedState;
    private final State mStoppedState;
    private final State mStoppingState;
    private final String mTag;
    private String mTcpBufferSizes;

    public static class Callback {
        public void onPreDhcpAction() {
        }

        public void onPostDhcpAction() {
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
        }

        public void onProvisioningFailure(LinkProperties newLp) {
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
        }

        public void onReachabilityLost(String logMsg) {
        }

        public void onQuit() {
        }

        public void installPacketFilter(byte[] filter) {
        }

        public void setFallbackMulticastFilter(boolean enabled) {
        }

        public void setNeighborDiscoveryOffload(boolean enable) {
        }
    }

    private class LoggingCallbackWrapper extends Callback {
        private static final String PREFIX = "INVOKE ";
        private Callback mCallback;

        public LoggingCallbackWrapper(Callback callback) {
            this.mCallback = callback;
        }

        private void log(String msg) {
            IpManager.this.mLocalLog.log(PREFIX + msg);
        }

        public void onPreDhcpAction() {
            this.mCallback.onPreDhcpAction();
            log("onPreDhcpAction()");
        }

        public void onPostDhcpAction() {
            this.mCallback.onPostDhcpAction();
            log("onPostDhcpAction()");
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            this.mCallback.onNewDhcpResults(dhcpResults);
            log("onNewDhcpResults({" + dhcpResults + "})");
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            this.mCallback.onProvisioningSuccess(newLp);
            log("onProvisioningSuccess({" + newLp + "})");
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            this.mCallback.onProvisioningFailure(newLp);
            log("onProvisioningFailure({" + newLp + "})");
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            this.mCallback.onLinkPropertiesChange(newLp);
            log("onLinkPropertiesChange({" + newLp + "})");
        }

        public void onReachabilityLost(String logMsg) {
            this.mCallback.onReachabilityLost(logMsg);
            log("onReachabilityLost(" + logMsg + ")");
        }

        public void onQuit() {
            this.mCallback.onQuit();
            log("onQuit()");
        }

        public void installPacketFilter(byte[] filter) {
            this.mCallback.installPacketFilter(filter);
            log("installPacketFilter(byte[" + filter.length + "])");
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            this.mCallback.setFallbackMulticastFilter(enabled);
            log("setFallbackMulticastFilter(" + enabled + ")");
        }

        public void setNeighborDiscoveryOffload(boolean enable) {
            this.mCallback.setNeighborDiscoveryOffload(enable);
            log("setNeighborDiscoveryOffload(" + enable + ")");
        }
    }

    public static class ProvisioningConfiguration {
        private static final int DEFAULT_TIMEOUT_MS = 36000;
        ApfCapabilities mApfCapabilities;
        boolean mEnableIPv4 = IpManager.SEND_CALLBACKS;
        boolean mEnableIPv6 = IpManager.SEND_CALLBACKS;
        int mProvisioningTimeoutMs = DEFAULT_TIMEOUT_MS;
        int mRequestedPreDhcpActionMs;
        StaticIpConfiguration mStaticIpConfig;
        boolean mUsingIpReachabilityMonitor = IpManager.SEND_CALLBACKS;

        public static class Builder {
            private ProvisioningConfiguration mConfig = new ProvisioningConfiguration();

            public Builder withoutIPv4() {
                this.mConfig.mEnableIPv4 = false;
                return this;
            }

            public Builder withoutIPv6() {
                this.mConfig.mEnableIPv6 = false;
                return this;
            }

            public Builder withoutIpReachabilityMonitor() {
                this.mConfig.mUsingIpReachabilityMonitor = false;
                return this;
            }

            public Builder withPreDhcpAction() {
                this.mConfig.mRequestedPreDhcpActionMs = ProvisioningConfiguration.DEFAULT_TIMEOUT_MS;
                return this;
            }

            public Builder withPreDhcpAction(int dhcpActionTimeoutMs) {
                this.mConfig.mRequestedPreDhcpActionMs = dhcpActionTimeoutMs;
                return this;
            }

            public Builder withStaticConfiguration(StaticIpConfiguration staticConfig) {
                this.mConfig.mStaticIpConfig = staticConfig;
                return this;
            }

            public Builder withApfCapabilities(ApfCapabilities apfCapabilities) {
                this.mConfig.mApfCapabilities = apfCapabilities;
                return this;
            }

            public Builder withProvisioningTimeoutMs(int timeoutMs) {
                this.mConfig.mProvisioningTimeoutMs = timeoutMs;
                return this;
            }

            public ProvisioningConfiguration build() {
                return new ProvisioningConfiguration(this.mConfig);
            }
        }

        public ProvisioningConfiguration(ProvisioningConfiguration other) {
            this.mEnableIPv4 = other.mEnableIPv4;
            this.mEnableIPv6 = other.mEnableIPv6;
            this.mUsingIpReachabilityMonitor = other.mUsingIpReachabilityMonitor;
            this.mRequestedPreDhcpActionMs = other.mRequestedPreDhcpActionMs;
            this.mStaticIpConfig = other.mStaticIpConfig;
            this.mApfCapabilities = other.mApfCapabilities;
            this.mProvisioningTimeoutMs = other.mProvisioningTimeoutMs;
        }

        public String toString() {
            return new StringJoiner(", ", getClass().getSimpleName() + "{", "}").add("mEnableIPv4: " + this.mEnableIPv4).add("mEnableIPv6: " + this.mEnableIPv6).add("mUsingIpReachabilityMonitor: " + this.mUsingIpReachabilityMonitor).add("mRequestedPreDhcpActionMs: " + this.mRequestedPreDhcpActionMs).add("mStaticIpConfig: " + this.mStaticIpConfig).add("mApfCapabilities: " + this.mApfCapabilities).add("mProvisioningTimeoutMs: " + this.mProvisioningTimeoutMs).toString();
        }
    }

    class StartedState extends State {
        private boolean mDhcpActionInFlight;

        StartedState() {
        }

        public void enter() {
            IpManager.this.mStartTimeMillis = SystemClock.elapsedRealtime();
            IpManager.this.mApfFilter = ApfFilter.maybeCreate(IpManager.this.mConfiguration.mApfCapabilities, IpManager.this.mNetworkInterface, IpManager.this.mCallback, IpManager.this.mMulticastFiltering);
            if (IpManager.this.mApfFilter == null) {
                IpManager.this.mCallback.setFallbackMulticastFilter(IpManager.this.mMulticastFiltering);
            }
            if (IpManager.this.mConfiguration.mEnableIPv6) {
                IpManager.this.startIPv6();
            }
            if (IpManager.this.mConfiguration.mUsingIpReachabilityMonitor) {
                IpManager.this.mIpReachabilityMonitor = new IpReachabilityMonitor(IpManager.this.mContext, IpManager.this.mInterfaceName, new android.net.ip.IpReachabilityMonitor.Callback() {
                    public void notifyLost(InetAddress ip, String logMsg) {
                        IpManager.this.mCallback.onReachabilityLost(logMsg);
                    }
                });
            }
            if (IpManager.this.mConfiguration.mEnableIPv4 && !IpManager.this.startIPv4()) {
                IpManager.this.transitionTo(IpManager.this.mStoppingState);
            }
        }

        public void exit() {
            IpManager.this.mProvisioningTimeoutAlarm.cancel();
            stopDhcpAction();
            if (IpManager.this.mIpReachabilityMonitor != null) {
                IpManager.this.mIpReachabilityMonitor.stop();
                IpManager.this.mIpReachabilityMonitor = null;
            }
            if (IpManager.this.mDhcpClient != null) {
                IpManager.this.mDhcpClient.sendMessage(DhcpClient.CMD_STOP_DHCP);
                IpManager.this.mDhcpClient.doQuit();
            }
            if (IpManager.this.mApfFilter != null) {
                IpManager.this.mApfFilter.shutdown();
                IpManager.this.mApfFilter = null;
            }
            IpManager.this.resetLinkProperties();
        }

        private void ensureDhcpAction() {
            if (!this.mDhcpActionInFlight) {
                IpManager.this.mCallback.onPreDhcpAction();
                this.mDhcpActionInFlight = IpManager.SEND_CALLBACKS;
                IpManager.this.mDhcpActionTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) IpManager.this.mConfiguration.mRequestedPreDhcpActionMs));
            }
        }

        private void stopDhcpAction() {
            IpManager.this.mDhcpActionTimeoutAlarm.cancel();
            if (this.mDhcpActionInFlight) {
                IpManager.this.mCallback.onPostDhcpAction();
                this.mDhcpActionInFlight = false;
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(IpManager.this.mTag, "StartedState: CMD_STOP");
                    IpManager.this.transitionTo(IpManager.this.mStoppingState);
                    break;
                case 2:
                    Log.e(IpManager.this.mTag, "ALERT: START received in StartedState. Please fix caller.");
                    break;
                case 3:
                    if (IpManager.this.mIpReachabilityMonitor != null) {
                        IpManager.this.mIpReachabilityMonitor.probeAll();
                        break;
                    }
                    break;
                case 4:
                    if (IpManager.this.mDhcpClient != null) {
                        IpManager.this.mDhcpClient.sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE);
                        break;
                    }
                    break;
                case 5:
                    Log.d(IpManager.this.mTag, "StartedState: EVENT_NETLINK_LINKPROPERTIES_CHANGED");
                    if (!IpManager.this.handleLinkPropertiesUpdate(IpManager.SEND_CALLBACKS)) {
                        Log.w(IpManager.this.mTag, "we have lost provisioning, transitions to StoppingState");
                        IpManager.this.transitionTo(IpManager.this.mStoppingState);
                        break;
                    }
                    break;
                case 6:
                    IpManager.this.mTcpBufferSizes = (String) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(IpManager.SEND_CALLBACKS);
                    break;
                case 7:
                    IpManager.this.mHttpProxy = (ProxyInfo) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(IpManager.SEND_CALLBACKS);
                    break;
                case 8:
                    IpManager.this.mMulticastFiltering = ((Boolean) msg.obj).booleanValue();
                    if (IpManager.this.mApfFilter == null) {
                        IpManager.this.mCallback.setFallbackMulticastFilter(IpManager.this.mMulticastFiltering);
                        break;
                    }
                    IpManager.this.mApfFilter.setMulticastFilter(IpManager.this.mMulticastFiltering);
                    break;
                case 9:
                    Log.w(IpManager.this.mTag, "StartedState: EVENT_PROVISIONING_TIMEOUT");
                    IpManager.this.handleProvisioningFailure();
                    break;
                case 10:
                    Log.w(IpManager.this.mTag, "StartedState: EVENT_DHCPACTION_TIMEOUT");
                    stopDhcpAction();
                    break;
                case DhcpClient.CMD_PRE_DHCP_ACTION /*196611*/:
                    if (IpManager.this.mConfiguration.mRequestedPreDhcpActionMs <= 0) {
                        IpManager.this.sendMessage(4);
                        break;
                    }
                    ensureDhcpAction();
                    break;
                case DhcpClient.CMD_POST_DHCP_ACTION /*196612*/:
                    stopDhcpAction();
                    switch (msg.arg1) {
                        case 1:
                            IpManager.this.mDhcp_Flag = msg.arg2;
                            IpManager.this.handleIPv4Success((DhcpResults) msg.obj);
                            break;
                        case 2:
                            IpManager.this.mDhcp_Flag = msg.arg2;
                            IpManager.this.handleIPv4Failure();
                            break;
                        default:
                            Log.e(IpManager.this.mTag, "Unknown CMD_POST_DHCP_ACTION status:" + msg.arg1);
                            break;
                    }
                case DhcpClient.CMD_ON_QUIT /*196613*/:
                    Log.e(IpManager.this.mTag, "Unexpected CMD_ON_QUIT.");
                    IpManager.this.mDhcpClient = null;
                    break;
                case DhcpClient.CMD_CLEAR_LINKADDRESS /*196615*/:
                    IpManager.this.clearIPv4Address();
                    break;
                case DhcpClient.CMD_CONFIGURE_LINKADDRESS /*196616*/:
                    if (IpManager.this.setIPv4Address(msg.obj)) {
                        if (IpManager.this.mDhcpClient != null) {
                            IpManager.this.mDhcpClient.sendMessage(DhcpClient.EVENT_LINKADDRESS_CONFIGURED);
                            break;
                        }
                    }
                    Log.e(IpManager.this.mTag, "Failed to set IPv4 address!");
                    IpManager.this.dispatchCallback(ProvisioningChange.LOST_PROVISIONING, new LinkProperties(IpManager.this.mLinkProperties));
                    IpManager.this.transitionTo(IpManager.this.mStoppingState);
                    break;
                    break;
                case DhcpClient.CMD_TRY_CACHED_IP /*196618*/:
                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                    staticIpConfiguration.domains = "CMD_TRY_CACHED_IP";
                    IpManager.this.mCallback.onNewDhcpResults(new DhcpResults(staticIpConfiguration));
                    break;
                default:
                    return false;
            }
            return IpManager.SEND_CALLBACKS;
        }
    }

    class StoppedState extends State {
        StoppedState() {
        }

        public void enter() {
            try {
                IpManager.this.mNwService.disableIpv6(IpManager.this.mInterfaceName);
                IpManager.this.mNwService.clearInterfaceAddresses(IpManager.this.mInterfaceName);
            } catch (Exception e) {
                Log.e(IpManager.this.mTag, "Failed to clear addresses or disable IPv6" + e);
            }
            IpManager.this.resetLinkProperties();
            if (IpManager.this.mStartTimeMillis > 0) {
                IpManager.this.recordMetric(3);
                IpManager.this.mStartTimeMillis = 0;
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    IpManager.this.mConfiguration = (ProvisioningConfiguration) msg.obj;
                    IpManager.this.transitionTo(IpManager.this.mStartedState);
                    break;
                case 5:
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    break;
                case 6:
                    IpManager.this.mTcpBufferSizes = (String) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    break;
                case 7:
                    IpManager.this.mHttpProxy = (ProxyInfo) msg.obj;
                    IpManager.this.handleLinkPropertiesUpdate(false);
                    break;
                case 8:
                    IpManager.this.mMulticastFiltering = ((Boolean) msg.obj).booleanValue();
                    break;
                case DhcpClient.CMD_ON_QUIT /*196613*/:
                    Log.e(IpManager.this.mTag, "Unexpected CMD_ON_QUIT (already stopped).");
                    break;
                default:
                    return false;
            }
            return IpManager.SEND_CALLBACKS;
        }
    }

    class StoppingState extends State {
        StoppingState() {
        }

        public void enter() {
            if (IpManager.this.mDhcpClient == null) {
                IpManager.this.transitionTo(IpManager.this.mStoppedState);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case DhcpClient.CMD_ON_QUIT /*196613*/:
                    IpManager.this.mDhcpClient = null;
                    IpManager.this.transitionTo(IpManager.this.mStoppedState);
                    break;
                default:
                    IpManager.this.deferMessage(msg);
                    break;
            }
            return IpManager.SEND_CALLBACKS;
        }
    }

    public static class WaitForProvisioningCallback extends Callback {
        private LinkProperties mCallbackLinkProperties;

        public LinkProperties waitForProvisioning() {
            LinkProperties linkProperties;
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                linkProperties = this.mCallbackLinkProperties;
            }
            return linkProperties;
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            synchronized (this) {
                this.mCallbackLinkProperties = newLp;
                notify();
            }
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            synchronized (this) {
                this.mCallbackLinkProperties = null;
                notify();
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-net-LinkProperties$ProvisioningChangeSwitchesValues() {
        if (-android-net-LinkProperties$ProvisioningChangeSwitchesValues != null) {
            return -android-net-LinkProperties$ProvisioningChangeSwitchesValues;
        }
        int[] iArr = new int[ProvisioningChange.values().length];
        try {
            iArr[ProvisioningChange.GAINED_PROVISIONING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ProvisioningChange.LOST_PROVISIONING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ProvisioningChange.STILL_NOT_PROVISIONED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ProvisioningChange.STILL_PROVISIONED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-net-LinkProperties$ProvisioningChangeSwitchesValues = iArr;
        return iArr;
    }

    public IpManager(Context context, String ifName, Callback callback) throws IllegalArgumentException {
        this(context, ifName, callback, Stub.asInterface(ServiceManager.getService("network_management")));
    }

    public IpManager(Context context, String ifName, Callback callback, INetworkManagementService nwService) throws IllegalArgumentException {
        super(IpManager.class.getSimpleName() + "." + ifName);
        this.forceDhcpDiscovery = false;
        this.mStoppedState = new StoppedState();
        this.mStoppingState = new StoppingState();
        this.mStartedState = new StartedState();
        this.mDhcp_Flag = 0;
        this.mTag = getName();
        this.mContext = context;
        this.mInterfaceName = ifName;
        this.mClatInterfaceName = CLAT_PREFIX + ifName;
        this.mCallback = new LoggingCallbackWrapper(callback);
        this.mNwService = nwService;
        this.forceDhcpDiscovery = false;
        this.mNetlinkTracker = new NetlinkTracker(this.mInterfaceName, new com.android.server.net.NetlinkTracker.Callback() {
            public void update() {
                IpManager.this.sendMessage(5);
            }
        }) {
            public void interfaceAdded(String iface) {
                super.interfaceAdded(iface);
                if (IpManager.this.mClatInterfaceName.equals(iface)) {
                    IpManager.this.mCallback.setNeighborDiscoveryOffload(false);
                }
            }

            public void interfaceRemoved(String iface) {
                super.interfaceRemoved(iface);
                if (IpManager.this.mClatInterfaceName.equals(iface)) {
                    IpManager.this.mCallback.setNeighborDiscoveryOffload(IpManager.SEND_CALLBACKS);
                }
            }
        };
        this.mProvisioningTimeoutAlarm = new WakeupMessage(this.mContext, getHandler(), this.mTag + ".EVENT_PROVISIONING_TIMEOUT", 9);
        this.mDhcpActionTimeoutAlarm = new WakeupMessage(this.mContext, getHandler(), this.mTag + ".EVENT_DHCPACTION_TIMEOUT", 10);
        addState(this.mStoppedState);
        addState(this.mStartedState);
        addState(this.mStoppingState);
        setInitialState(this.mStoppedState);
        this.mLocalLog = new LocalLog(500);
        super.start();
        try {
            this.mNwService.registerObserver(this.mNetlinkTracker);
        } catch (RemoteException e) {
            Log.e(this.mTag, "Couldn't register NetlinkTracker: " + e.toString());
        }
        resetLinkProperties();
    }

    protected void onQuitting() {
        this.mCallback.onQuit();
    }

    public void shutdown() {
        stop();
        quit();
    }

    public static Builder buildProvisioningConfiguration() {
        return new Builder();
    }

    public void startProvisioning(ProvisioningConfiguration req) {
        getNetworkInterface();
        this.mCallback.setNeighborDiscoveryOffload(SEND_CALLBACKS);
        sendMessage(2, new ProvisioningConfiguration(req));
    }

    public void startProvisioning(StaticIpConfiguration staticIpConfig) {
        startProvisioning(buildProvisioningConfiguration().withStaticConfiguration(staticIpConfig).build());
    }

    public void startProvisioning() {
        startProvisioning(new ProvisioningConfiguration());
    }

    public void stop() {
        this.forceDhcpDiscovery = false;
        sendMessage(1);
    }

    public void confirmConfiguration() {
        sendMessage(3);
    }

    public void completedPreDhcpAction() {
        sendMessage(4);
    }

    public void setTcpBufferSizes(String tcpBufferSizes) {
        sendMessage(6, tcpBufferSizes);
    }

    public void setHttpProxy(ProxyInfo proxyInfo) {
        sendMessage(7, proxyInfo);
    }

    public void setMulticastFilter(boolean enabled) {
        sendMessage(8, Boolean.valueOf(enabled));
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println("APF dump:");
        pw.increaseIndent();
        ApfFilter apfFilter = this.mApfFilter;
        if (apfFilter != null) {
            apfFilter.dump(pw);
        } else {
            pw.println("No apf support");
        }
        pw.decreaseIndent();
        pw.println();
        pw.println("StateMachine dump:");
        pw.increaseIndent();
        this.mLocalLog.readOnlyLocalLog().dump(fd, pw, args);
        pw.decreaseIndent();
    }

    protected String getWhatToString(int what) {
        return (String) sWhatToString.get(what, "UNKNOWN: " + Integer.toString(what));
    }

    protected String getLogRecString(Message msg) {
        int i;
        String str = "%s/%d %d %d %s";
        Object[] objArr = new Object[5];
        objArr[0] = this.mInterfaceName;
        if (this.mNetworkInterface == null) {
            i = -1;
        } else {
            i = this.mNetworkInterface.getIndex();
        }
        objArr[1] = Integer.valueOf(i);
        objArr[2] = Integer.valueOf(msg.arg1);
        objArr[3] = Integer.valueOf(msg.arg2);
        objArr[4] = Objects.toString(msg.obj);
        String logLine = String.format(str, objArr);
        if (msg.what == DhcpClient.CMD_CONFIGURE_LINKADDRESS) {
            this.mLocalLog.log(getWhatToString(msg.what) + " " + this.mInterfaceName);
        } else {
            this.mLocalLog.log(getWhatToString(msg.what) + " " + logLine);
        }
        return logLine;
    }

    protected boolean recordLogRec(Message msg) {
        return msg.what != 5 ? SEND_CALLBACKS : false;
    }

    private void getNetworkInterface() {
        try {
            this.mNetworkInterface = NetworkInterface.getByName(this.mInterfaceName);
        } catch (Exception e) {
            Log.e(this.mTag, "ALERT: Failed to get interface object: ", e);
        }
    }

    private void resetLinkProperties() {
        this.mNetlinkTracker.clearLinkProperties();
        this.mConfiguration = null;
        this.mDhcpResults = null;
        this.mTcpBufferSizes = "";
        this.mHttpProxy = null;
        this.mLinkProperties = new LinkProperties();
        this.mLinkProperties.setInterfaceName(this.mInterfaceName);
    }

    private void recordMetric(int type) {
        if (this.mStartTimeMillis <= 0) {
            Log.wtf(this.mTag, "Start time undefined!");
        }
        IpManagerEvent.logEvent(type, this.mInterfaceName, SystemClock.elapsedRealtime() - this.mStartTimeMillis);
    }

    private static boolean isProvisioned(LinkProperties lp) {
        return !lp.isProvisioned() ? lp.hasIPv4Address() : SEND_CALLBACKS;
    }

    private static ProvisioningChange compareProvisioning(LinkProperties oldLp, LinkProperties newLp) {
        ProvisioningChange delta;
        boolean wasProvisioned = isProvisioned(oldLp);
        boolean isProvisioned = isProvisioned(newLp);
        if (!wasProvisioned && isProvisioned) {
            delta = ProvisioningChange.GAINED_PROVISIONING;
        } else if (wasProvisioned && isProvisioned) {
            delta = ProvisioningChange.STILL_PROVISIONED;
        } else if (wasProvisioned || isProvisioned) {
            delta = ProvisioningChange.LOST_PROVISIONING;
            Log.d("IpManager", "compareProvisioning: LOST_PROVISIONING because 1");
        } else {
            delta = ProvisioningChange.STILL_NOT_PROVISIONED;
        }
        if ((oldLp.hasIPv4Address() && !newLp.hasIPv4Address()) || (oldLp.isIPv6Provisioned() && !newLp.isIPv6Provisioned())) {
            delta = ProvisioningChange.LOST_PROVISIONING;
            Log.d("IpManager", "compareProvisioning: LOST_PROVISIONING because 2");
        }
        if (oldLp.hasGlobalIPv6Address() && oldLp.hasIPv6DefaultRoute() && !newLp.hasIPv6DefaultRoute()) {
            delta = ProvisioningChange.LOST_PROVISIONING;
            Log.d("IpManager", "compareProvisioning: LOST_PROVISIONING because 3");
        }
        if (delta != ProvisioningChange.LOST_PROVISIONING || !oldLp.hasIPv4Address() || !newLp.hasIPv4Address()) {
            return delta;
        }
        delta = ProvisioningChange.STILL_PROVISIONED;
        Log.d("IpManager", "compareProvisioning: network has IPv4 address, dont loss of provisioning");
        return delta;
    }

    private void dispatchCallback(ProvisioningChange delta, LinkProperties newLp) {
        switch (-getandroid-net-LinkProperties$ProvisioningChangeSwitchesValues()[delta.ordinal()]) {
            case 1:
                recordMetric(1);
                this.mCallback.onProvisioningSuccess(newLp);
                return;
            case 2:
                recordMetric(2);
                this.mCallback.onProvisioningFailure(newLp);
                return;
            default:
                this.mCallback.onLinkPropertiesChange(newLp);
                return;
        }
    }

    private ProvisioningChange setLinkProperties(LinkProperties newLp) {
        if (this.mApfFilter != null) {
            this.mApfFilter.setLinkProperties(newLp);
        }
        if (this.mIpReachabilityMonitor != null) {
            this.mIpReachabilityMonitor.updateLinkProperties(newLp);
        }
        ProvisioningChange delta = compareProvisioning(this.mLinkProperties, newLp);
        this.mLinkProperties = new LinkProperties(newLp);
        if (delta == ProvisioningChange.GAINED_PROVISIONING) {
            this.mProvisioningTimeoutAlarm.cancel();
        }
        return delta;
    }

    private boolean linkPropertiesUnchanged(LinkProperties newLp) {
        return Objects.equals(newLp, this.mLinkProperties);
    }

    private LinkProperties assembleLinkProperties() {
        LinkProperties newLp = new LinkProperties();
        newLp.setInterfaceName(this.mInterfaceName);
        LinkProperties netlinkLinkProperties = this.mNetlinkTracker.getLinkProperties();
        newLp.setLinkAddresses(netlinkLinkProperties.getLinkAddresses());
        for (RouteInfo route : netlinkLinkProperties.getRoutes()) {
            newLp.addRoute(route);
        }
        for (InetAddress dns : netlinkLinkProperties.getDnsServers()) {
            if (newLp.isReachable(dns)) {
                newLp.addDnsServer(dns);
            }
        }
        if (this.mDhcpResults != null) {
            for (RouteInfo route2 : this.mDhcpResults.getRoutes(this.mInterfaceName)) {
                newLp.addRoute(route2);
            }
            for (InetAddress dns2 : this.mDhcpResults.dnsServers) {
                if (newLp.isReachable(dns2)) {
                    newLp.addDnsServer(dns2);
                }
            }
            newLp.setDomains(this.mDhcpResults.domains);
            if (this.mDhcpResults.mtu != 0) {
                newLp.setMtu(this.mDhcpResults.mtu);
            }
        }
        if (!TextUtils.isEmpty(this.mTcpBufferSizes)) {
            newLp.setTcpBufferSizes(this.mTcpBufferSizes);
        }
        if (this.mHttpProxy != null) {
            newLp.setHttpProxy(this.mHttpProxy);
        }
        return newLp;
    }

    private boolean handleLinkPropertiesUpdate(boolean sendCallbacks) {
        boolean z = SEND_CALLBACKS;
        LinkProperties newLp = assembleLinkProperties();
        if (linkPropertiesUnchanged(newLp)) {
            return SEND_CALLBACKS;
        }
        ProvisioningChange delta = setLinkProperties(newLp);
        if (sendCallbacks) {
            dispatchCallback(delta, newLp);
        }
        if (delta == ProvisioningChange.LOST_PROVISIONING) {
            z = false;
        }
        return z;
    }

    private boolean setIPv4Address(LinkAddress address) {
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        ifcg.setLinkAddress(address);
        try {
            this.mNwService.setInterfaceConfig(this.mInterfaceName, ifcg);
            return SEND_CALLBACKS;
        } catch (Exception e) {
            Log.e(this.mTag, "IPv4 configuration failed: ", e);
            return false;
        }
    }

    private void clearIPv4Address() {
        try {
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            ifcg.setLinkAddress(new LinkAddress("0.0.0.0/0"));
            this.mNwService.setInterfaceConfig(this.mInterfaceName, ifcg);
        } catch (Exception e) {
            Log.e(this.mTag, "ALERT: Failed to clear IPv4 address on interface " + this.mInterfaceName, e);
        }
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        this.mDhcpResults = new DhcpResults(dhcpResults);
        LinkProperties newLp = assembleLinkProperties();
        ProvisioningChange delta = setLinkProperties(newLp);
        this.mCallback.onNewDhcpResults(dhcpResults);
        dispatchCallback(delta, newLp);
    }

    private void handleIPv4Failure() {
        clearIPv4Address();
        this.mDhcpResults = null;
        this.mCallback.onNewDhcpResults(null);
        handleProvisioningFailure();
    }

    private void handleProvisioningFailure() {
        LinkProperties newLp = assembleLinkProperties();
        ProvisioningChange delta = setLinkProperties(newLp);
        if (delta == ProvisioningChange.STILL_NOT_PROVISIONED) {
            delta = ProvisioningChange.LOST_PROVISIONING;
        }
        dispatchCallback(delta, newLp);
        if (delta == ProvisioningChange.LOST_PROVISIONING) {
            transitionTo(this.mStoppingState);
        }
    }

    private boolean startIPv4() {
        if (this.mConfiguration.mStaticIpConfig == null) {
            this.mDhcpClient = HwDhcpClient.makeHwDhcpClient(this.mContext, this, this.mInterfaceName);
            this.mDhcpClient.registerForPreDhcpNotification();
            this.mDhcpClient.sendMessage(DhcpClient.CMD_START_DHCP);
            if (this.mConfiguration.mProvisioningTimeoutMs > 0) {
                this.mProvisioningTimeoutAlarm.schedule(SystemClock.elapsedRealtime() + ((long) this.mConfiguration.mProvisioningTimeoutMs));
            }
        } else if (setIPv4Address(this.mConfiguration.mStaticIpConfig.ipAddress)) {
            handleIPv4Success(new DhcpResults(this.mConfiguration.mStaticIpConfig));
        } else {
            recordMetric(2);
            this.mCallback.onProvisioningFailure(new LinkProperties(this.mLinkProperties));
            return false;
        }
        return SEND_CALLBACKS;
    }

    private boolean startIPv6() {
        try {
            this.mNwService.setInterfaceIpv6PrivacyExtensions(this.mInterfaceName, SEND_CALLBACKS);
            this.mNwService.enableIpv6(this.mInterfaceName);
            return SEND_CALLBACKS;
        } catch (RemoteException re) {
            Log.e(this.mTag, "Unable to change interface settings: " + re);
            return false;
        } catch (IllegalStateException ie) {
            Log.e(this.mTag, "Unable to change interface settings: " + ie);
            return false;
        }
    }

    public int getDhcpFlag() {
        return this.mDhcp_Flag;
    }

    public void setForceDhcpDiscovery() {
        this.forceDhcpDiscovery = SEND_CALLBACKS;
    }

    public boolean isDhcpDiscoveryForced() {
        return this.forceDhcpDiscovery;
    }
}
