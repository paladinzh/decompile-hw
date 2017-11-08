package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.wifi.HuaweiApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwNetworkManagementService extends NetworkManagementService {
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    private static final String AD_APKDL_STRATEGY = "com.huawei.permission.AD_APKDL_STRATEGY";
    private static final String AD_APKDL_STRATEGY_PERMISSION = "com.huawei.permission.AD_APKDL_STRATEGY";
    private static final int AD_STRATEGY = 0;
    private static final int APK_DL_STRATEGY = 1;
    private static final String ARG_ADD = "add";
    private static final String ARG_CLEAR = "clear";
    private static final String ARG_IP_WHITELIST = "ipwhitelist";
    private static final String ARG_SET = "set";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String CMD_NET_FILTER = "net_filter";
    private static final int CODE_AD_DEBUG = 1019;
    private static final int CODE_CLEAN_AD_STRATEGY = 1018;
    private static final int CODE_CLEAR_AD_APKDL_STRATEGY = 1103;
    private static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    private static final int CODE_GET_AD_KEY_LIST = 1016;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_WIFI_DNS_STAT = 1011;
    private static final int CODE_PRINT_AD_APKDL_STRATEGY = 1104;
    private static final int CODE_REMOVE_LEGACYROUTE_TO_HOST = 1015;
    private static final int CODE_SET_AD_STRATEGY = 1017;
    private static final int CODE_SET_AD_STRATEGY_RULE = 1101;
    private static final int CODE_SET_APK_DL_STRATEGY = 1102;
    private static final int CODE_SET_APK_DL_URL_USER_RESULT = 1105;
    private static final int CODE_SET_AP_CONFIGRATION_HW = 1008;
    private static final int CODE_SET_NETWORK_ACCESS_WHITELIST = 1106;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_SOFTAP_TX_POWER = 1009;
    private static final int DEFAULT_ISMCOEX_WIFI_AP_CHANNEL = 11;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final int DEFAULT_WIFI_AP_MAX_CONNECTIONS = 8;
    private static final String DESCRIPTOR = "android.net.wifi.INetworkManager";
    private static final String DESCRIPTOR_ADCLEANER_MANAGER_Ex = "android.os.AdCleanerManagerEx";
    private static final String DESCRIPTOR_HW_AD_CLEANER = "android.view.HwAdCleaner";
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String EXTRA_CURRENT_TIME = "currentTime";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_STA_INFO = "macInfo";
    private static final String HEX_STR = "0123456789ABCDEF";
    private static final int HSM_TRANSACT_CODE = 201;
    private static final String INTENT_APKDL_URL_DETECTED = "com.android.intent.action.apkdl_url_detected";
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final int PER_STRATEGY_SIZE = 470;
    private static final int PER_UID_LIST_SIZE = 50;
    private static final String TAG = HwNetworkManagementService.class.getSimpleName();
    private Map<String, List<String>> mAdIdMap = new HashMap();
    private Map<String, List<String>> mAdViewMap = new HashMap();
    private int mChannel;
    private AtomicInteger mCmdId;
    private NativeDaemonConnector mConnector;
    private Context mContext;
    private HuaweiApConfiguration mHwApConfig;
    private int mLinkedStaCount = 0;
    private String mSoftapIface;
    private String mWlanIface;
    private Pattern p = Pattern.compile("^.*max=([0-9]+);idx=([0-9]+);(.*)$");
    private HashMap<String, Long> startTimeMap = new HashMap();
    private StringBuffer urlBuffer = new StringBuffer();

    static class NetdResponseCode {
        public static final int ApLinkedStaListChangeHISI = 651;
        public static final int ApLinkedStaListChangeQCOM = 901;
        public static final int HwDnsStat = 130;
        public static final int SoftapDhcpListResult = 122;
        public static final int SoftapListResult = 121;

        NetdResponseCode() {
        }
    }

    public HwNetworkManagementService(Context context, String socket) {
        super(context, socket);
        this.mContext = context;
        this.mCmdId = new AtomicInteger(0);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 201) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            return executeHsmCommand(data, reply);
        } else if (code == 1005) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            List<String> result = getApLinkedStaList();
            reply.writeNoException();
            reply.writeStringList(result);
            return true;
        } else if (code == 1006) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_MACFILTER");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setSoftapMacFilter(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == 1007) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_DISASSOCIATESTA");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setSoftapDisassociateSta(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == 1008) {
            Slog.d(TAG, "code == CODE_SET_AP_CONFIGRATION_HW");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            this.mWlanIface = data.readString();
            this.mSoftapIface = data.readString();
            reply.writeNoException();
            setAccessPointHw(this.mWlanIface, this.mSoftapIface);
            return true;
        } else if (code == 1009) {
            Slog.d(TAG, "code == CODE_SET_SOFTAP_TX_POWER");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            setWifiTxPower(data.readString());
            reply.writeNoException();
            return true;
        } else if (code == 1011) {
            Slog.d(TAG, "code == CODE_GET_WIFI_DNS_STAT");
            data.enforceInterface(DESCRIPTOR);
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
            String stats = getWiFiDnsStats(data.readInt());
            reply.writeNoException();
            reply.writeString(stats);
            return true;
        } else if (code == CODE_SET_AD_STRATEGY) {
            Slog.d(TAG, "code == CODE_SET_AD_STRATEGY");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.AD_APKDL_STRATEGY", "permission denied");
            needReset = data.readInt() > 0;
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, needReset: " + needReset);
            if (needReset) {
                this.mAdViewMap.clear();
                this.mAdIdMap.clear();
            }
            size = data.readInt();
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdViewMap size: " + size);
            if (size > 0) {
                for (i = 0; i < size; i++) {
                    key = data.readString();
                    value = data.createStringArrayList();
                    Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdViewMap key: " + key + ", at " + i);
                    this.mAdViewMap.put(key, value);
                }
            }
            size = data.readInt();
            Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdIdMap size: " + size);
            if (size > 0) {
                for (i = 0; i < size; i++) {
                    key = data.readString();
                    value = data.createStringArrayList();
                    Slog.d(TAG, "CODE_SET_AD_STRATEGY, mAdIdMap key: " + key + ", at " + i);
                    this.mAdIdMap.put(key, value);
                }
            }
            reply.writeNoException();
            return true;
        } else if (code == CODE_CLEAN_AD_STRATEGY) {
            Slog.d(TAG, "code == CODE_CLEAN_AD_STRATEGY");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.AD_APKDL_STRATEGY", "permission denied");
            int flag = data.readInt();
            Slog.d(TAG, "CODE_CLEAN_AD_STRATEGY, flag: " + flag);
            if (1 == flag) {
                this.mAdViewMap.clear();
                this.mAdIdMap.clear();
            } else if (flag == 0) {
                ArrayList<String> adAppList = data.createStringArrayList();
                Slog.d(TAG, "CODE_CLEAN_AD_STRATEGY adAppList: ");
                if (adAppList != null) {
                    for (i = 0; i < adAppList.size(); i++) {
                        String adAppName = (String) adAppList.get(i);
                        Slog.d(TAG, i + " = " + adAppName);
                        if (this.mAdViewMap.containsKey(adAppName)) {
                            this.mAdViewMap.remove(adAppName);
                        }
                        if (this.mAdIdMap.containsKey(adAppName)) {
                            this.mAdIdMap.remove(adAppName);
                        }
                    }
                }
            }
            reply.writeNoException();
            return true;
        } else if (code == CODE_GET_AD_KEY_LIST) {
            Slog.d(TAG, "code == CODE_GET_AD_KEY_LIST");
            data.enforceInterface(DESCRIPTOR_HW_AD_CLEANER);
            String appName = data.readString();
            if (appName == null || !this.mAdViewMap.containsKey(appName)) {
                try {
                    reply.writeStringList(new ArrayList());
                    Slog.d(TAG, "appName = " + appName + "  is not in the mAdViewMap! reply none");
                } catch (Exception e) {
                    Slog.d(TAG, "---------err: Exception ");
                    e.printStackTrace();
                }
            } else {
                reply.writeStringList((List) this.mAdViewMap.get(appName));
                Slog.d(TAG, "appName = " + appName + "  is in the mAdViewMap!");
            }
            if (appName == null || !this.mAdIdMap.containsKey(appName)) {
                reply.writeStringList(new ArrayList());
                Slog.d(TAG, "appName = " + appName + "  is not in the mAdIdMap! reply none");
                reply.writeNoException();
                return true;
            }
            reply.writeStringList((List) this.mAdIdMap.get(appName));
            Slog.d(TAG, "appName = " + appName + "  is in the mAdIdMap !");
            reply.writeNoException();
            return true;
        } else if (code == CODE_AD_DEBUG) {
            Set<String> keysSet;
            List<String> keysList;
            List<String> value;
            Slog.d(TAG, "code == CODE_AD_DEBUG");
            data.enforceInterface(DESCRIPTOR_ADCLEANER_MANAGER_Ex);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.AD_APKDL_STRATEGY", "permission denied");
            data.readInt();
            int j = 0;
            StringBuffer print = new StringBuffer();
            if (this.mAdViewMap.isEmpty()) {
                print.append("mAdViewMap is empty!");
            } else {
                print.append("\n---------------- mAdViewMap is as followed ---------------\n");
                keysSet = this.mAdViewMap.keySet();
                keysList = new ArrayList();
                for (String keyString : keysSet) {
                    keysList.add(keyString);
                }
                for (i = 0; i < this.mAdViewMap.size(); i++) {
                    key = (String) keysList.get(i);
                    value = (List) this.mAdViewMap.get(key);
                    print.append("\n(" + i + ") apkName = " + key + "\n");
                    for (j = 
/*
Method generation error in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r15_6 'j' int) = (r15_0 'j' int), (r15_9 'j' int) binds: {(r15_0 'j' int)=B:218:0x057f, (r15_9 'j' int)=B:108:0x057c} in method: com.android.server.HwNetworkManagementService.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:190)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:128)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:328)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:265)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:228)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:530)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:514)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 50 more

*/

                    private boolean executeHsmCommand(Parcel data, Parcel reply) {
                        try {
                            String cmd = data.readString();
                            Object[] args = data.readArray(null);
                            if (this.mConnector != null) {
                                int i;
                                if (this.mConnector.execute(cmd, args).isClassOk()) {
                                    i = 1;
                                } else {
                                    i = 0;
                                }
                                reply.writeInt(i);
                            }
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    public void setConnector(NativeDaemonConnector connector) {
                        this.mConnector = connector;
                    }

                    private String getChannel(WifiConfiguration wifiConfig) {
                        if (wifiConfig.apBand == 0 && SystemProperties.getBoolean(ISM_COEX_ON, false)) {
                            this.mChannel = 11;
                        } else {
                            this.mChannel = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_channel", 0);
                            if (this.mChannel != 0 && (wifiConfig.apBand != 0 || this.mChannel <= 14)) {
                                if (wifiConfig.apBand == 1 && this.mChannel < 34) {
                                }
                            }
                            this.mChannel = wifiConfig.apChannel;
                        }
                        Slog.d(TAG, "channel=" + this.mChannel);
                        return String.valueOf(this.mChannel);
                    }

                    private String getMaxscb() {
                        int maxscb = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
                        Slog.d(TAG, "maxscb=" + maxscb);
                        return String.valueOf(maxscb);
                    }

                    private static String getSecurityType(WifiConfiguration wifiConfig) {
                        switch (wifiConfig.getAuthType()) {
                            case 1:
                                return "wpa-psk";
                            case 4:
                                return "wpa2-psk";
                            default:
                                return "open";
                        }
                    }

                    public String getIgnorebroadcastssid() {
                        String iIgnorebroadcastssidStr = "broadcast";
                        if (1 == Systemex.getInt(this.mContext.getContentResolver(), "show_broadcast_ssid_config", 0)) {
                            iIgnorebroadcastssidStr = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_ignorebroadcastssid", 0) == 0 ? "broadcast" : "hidden";
                            Slog.d(TAG, "iIgnorebroadcastssidStr=" + iIgnorebroadcastssidStr);
                        }
                        return iIgnorebroadcastssidStr;
                    }

                    public void startAccessPointWithChannel(WifiConfiguration wifiConfig, String wlanIface) {
                        if (wifiConfig != null) {
                            try {
                                this.mConnector.execute("softap", new Object[]{ARG_SET, wlanIface, wifiConfig.SSID, getIgnorebroadcastssid(), getChannel(wifiConfig), getSecurityType(wifiConfig), new SensitiveArg(wifiConfig.preSharedKey), getMaxscb()});
                                this.mConnector.execute("softap", new Object[]{"startap"});
                            } catch (NativeDaemonConnectorException e) {
                                throw e.rethrowAsParcelableException();
                            }
                        }
                    }

                    public void sendDataSpeedSlowMessage(String[] cooked, String raw) {
                        if (cooked.length < 2 || !cooked[1].equals("sourceAddress")) {
                            String msg1 = String.format("Invalid event from daemon (%s)", new Object[]{raw});
                            Slog.d(TAG, "receive DataSpeedSlowDetected,return error 1");
                            throw new IllegalStateException(msg1);
                        }
                        int sourceAddress = Integer.parseInt(cooked[2]);
                        NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
                        Slog.d(TAG, "onEvent receive DataSpeedSlowDetected");
                        if (mobileNetinfo != null && mobileNetinfo.isConnected()) {
                            Slog.d(TAG, "onEvent receive DataSpeedSlowDetected,mobile network is connected!");
                            Intent chrIntent = new Intent("com.android.intent.action.data_speed_slow");
                            chrIntent.putExtra("sourceAddress", sourceAddress);
                            this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                        }
                    }

                    public void sendWebStatMessage(String[] cooked, String raw) {
                        if (cooked.length < 20 || !cooked[1].equals("ReportType")) {
                            throw new IllegalStateException(String.format("Invalid event from daemon (%s)", new Object[]{raw}));
                        }
                        try {
                            NetworkInfo mobileNetinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
                            Slog.d(TAG, "onEvent receive Web Stat Report:" + raw);
                            if (mobileNetinfo != null && mobileNetinfo.isConnected()) {
                                Intent chrIntent = new Intent("com.android.intent.action.web_stat_report");
                                chrIntent.putExtra("ReportType", Integer.parseInt(cooked[2]));
                                chrIntent.putExtra("RTT", Integer.parseInt(cooked[3]));
                                chrIntent.putExtra("WebDelay", Integer.parseInt(cooked[4]));
                                chrIntent.putExtra("SuccNum", Integer.parseInt(cooked[5]));
                                chrIntent.putExtra("FailNum", Integer.parseInt(cooked[6]));
                                chrIntent.putExtra("NoAckNum", Integer.parseInt(cooked[7]));
                                chrIntent.putExtra("TotalNum", Integer.parseInt(cooked[8]));
                                chrIntent.putExtra("TcpTotalNum", Integer.parseInt(cooked[9]));
                                chrIntent.putExtra("DelayL1", Integer.parseInt(cooked[10]));
                                chrIntent.putExtra("DelayL2", Integer.parseInt(cooked[11]));
                                chrIntent.putExtra("DelayL3", Integer.parseInt(cooked[12]));
                                chrIntent.putExtra("DelayL4", Integer.parseInt(cooked[13]));
                                chrIntent.putExtra("DelayL5", Integer.parseInt(cooked[14]));
                                chrIntent.putExtra("DelayL6", Integer.parseInt(cooked[15]));
                                chrIntent.putExtra("RTTL1", Integer.parseInt(cooked[16]));
                                chrIntent.putExtra("RTTL2", Integer.parseInt(cooked[17]));
                                chrIntent.putExtra("RTTL3", Integer.parseInt(cooked[18]));
                                chrIntent.putExtra("RTTL4", Integer.parseInt(cooked[19]));
                                chrIntent.putExtra("RTTL5", Integer.parseInt(cooked[20]));
                                this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                            }
                        } catch (Exception e) {
                            Slog.e(TAG, "Web Stat Report Send Broadcast Fail.");
                        }
                    }

                    public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
                        Slog.d(TAG, "handleApLinkedStaListChange is called");
                        if ("STA_JOIN".equals(cooked[1]) || "STA_LEAVE".equals(cooked[1])) {
                            Slog.d(TAG, "Got sta list change event:" + cooked[1]);
                            notifyApLinkedStaListChange(cooked[1], cooked[4]);
                            return true;
                        }
                        throw new IllegalStateException(String.format("ApLinkedStaListChange: Invalid event from daemon (%s)", new Object[]{raw}));
                    }

                    public List<String> getApLinkedStaList() {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        try {
                            List<String> mDhcpList = getApLinkedDhcpList();
                            Slog.d(TAG, "getApLinkedStaList: softap assoclist");
                            String[] macList = this.mConnector.doListCommand("softap", 121, new Object[]{"assoclist"});
                            if (macList == null) {
                                Slog.e(TAG, "getApLinkedStaList Error: doListCommand return NULL");
                                this.mLinkedStaCount = 0;
                                return null;
                            }
                            this.mLinkedStaCount = macList.length;
                            List<String> infoList = new ArrayList();
                            for (String mac : macList) {
                                String mac2 = getApLinkedStaInfo(mac2, mDhcpList);
                                Slog.d(TAG, "getApLinkedStaList ApLinkedStaInfo = " + mac2);
                                infoList.add(mac2);
                            }
                            Slog.d(TAG, "getApLinkedStaList, info size=" + infoList.size());
                            return infoList;
                        } catch (NativeDaemonConnectorException e) {
                            throw new IllegalStateException("Cannot communicate with native daemon to get linked stations list");
                        }
                    }

                    public void setSoftapMacFilter(String macFilter) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        try {
                            Slog.d(TAG, "setSoftapMacFilter:" + String.format("softap setmacfilter " + macFilter, new Object[0]));
                            this.mConnector.doCommand("softap", new Object[]{"setmacfilter", macFilter});
                        } catch (NativeDaemonConnectorException e) {
                            throw new IllegalStateException("Cannot communicate with native daemon to set MAC Filter");
                        }
                    }

                    public void setSoftapDisassociateSta(String mac) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        try {
                            Slog.d(TAG, "setSoftapDisassociateSta:" + String.format("softap disassociatesta " + mac, new Object[0]));
                            this.mConnector.doCommand("softap", new Object[]{"disassociatesta", mac});
                        } catch (NativeDaemonConnectorException e) {
                            throw new IllegalStateException("Cannot communicate with native daemon to disassociate a station");
                        }
                    }

                    public void setAccessPointHw(String wlanIface, String softapIface) throws IllegalStateException {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_NETWORK_STATE", "NetworkManagementService");
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", "NetworkManagementService");
                        HuaweiApConfiguration hwApConfig = new HuaweiApConfiguration();
                        hwApConfig.channel = this.mChannel;
                        hwApConfig.maxScb = Secure.getInt(this.mContext.getContentResolver(), "wifi_ap_maxscb", 8);
                        try {
                            String str = String.format("softap sethw " + wlanIface + " " + softapIface + " %d %d", new Object[]{Integer.valueOf(hwApConfig.channel), Integer.valueOf(hwApConfig.maxScb)});
                            this.mConnector.doCommand("softap", new Object[]{"sethw", wlanIface, softapIface, String.valueOf(hwApConfig.channel), String.valueOf(hwApConfig.maxScb)});
                            Slog.d(TAG, "setAccessPointHw command: " + str);
                        } catch (NativeDaemonConnectorException e) {
                            throw new IllegalStateException("Error communicating to native daemon to set soft AP", e);
                        }
                    }

                    private List<String> getApLinkedDhcpList() {
                        try {
                            Slog.d(TAG, "getApLinkedDhcpList: softap getdhcplease");
                            String[] dhcpleaseList = this.mConnector.doListCommand("softap", 122, new Object[]{"getdhcplease"});
                            if (dhcpleaseList == null) {
                                Slog.e(TAG, "getApLinkedDhcpList Error: doListCommand return NULL");
                                return null;
                            }
                            List<String> mDhcpList = new ArrayList();
                            for (String dhcplease : dhcpleaseList) {
                                Slog.d(TAG, "getApLinkedDhcpList dhcpList = " + dhcplease);
                                mDhcpList.add(dhcplease);
                            }
                            Slog.d(TAG, "getApLinkedDhcpList: mDhcpList size=" + mDhcpList.size());
                            return mDhcpList;
                        } catch (NativeDaemonConnectorException e) {
                            Slog.e(TAG, "Cannot communicate with native daemon to get dhcp lease information");
                            return null;
                        }
                    }

                    private String getApLinkedStaInfo(String mac, List<String> mDhcpList) {
                        String ApLinkedStaInfo = String.format("MAC=%s", new Object[]{mac});
                        mac = mac.toLowerCase();
                        if (mDhcpList != null) {
                            for (String dhcplease : mDhcpList) {
                                if (dhcplease.contains(mac)) {
                                    if (4 <= dhcplease.split(" ").length) {
                                        Slog.d(TAG, "getApLinkedStaInfo: dhcplease token");
                                        ApLinkedStaInfo = String.format(ApLinkedStaInfo + " IP=%s DEVICE=%s", new Object[]{Tokens[2], Tokens[3]});
                                    }
                                }
                            }
                        }
                        return ApLinkedStaInfo;
                    }

                    private void notifyApLinkedStaListChange(String event, String macStr) {
                        String action = null;
                        long mCurrentTime = System.currentTimeMillis();
                        if ("STA_JOIN".equals(event)) {
                            action = ACTION_WIFI_AP_STA_JOIN;
                            this.mLinkedStaCount++;
                        } else if ("STA_LEAVE".equals(event)) {
                            action = ACTION_WIFI_AP_STA_LEAVE;
                            this.mLinkedStaCount--;
                        }
                        if (this.mLinkedStaCount < 0 || this.mLinkedStaCount > 8) {
                            Slog.e(TAG, "mLinkedStaCount over flow, need synchronize. value = " + this.mLinkedStaCount);
                            try {
                                String[] macList = this.mConnector.doListCommand(String.format("softap assoclist", new Object[0]), 121, new Object[0]);
                                if (macList == null) {
                                    this.mLinkedStaCount = 0;
                                } else {
                                    this.mLinkedStaCount = macList.length;
                                }
                            } catch (NativeDaemonConnectorException e) {
                                Slog.e(TAG, "Cannot communicate with native daemon to get linked stations list");
                                this.mLinkedStaCount = 0;
                            }
                        }
                        Slog.e(TAG, "send broadcast, event=" + event + ", extraInfo: " + String.format("MAC=%s TIME=%d STACNT=%d", new Object[]{macStr, Long.valueOf(mCurrentTime), Integer.valueOf(this.mLinkedStaCount)}));
                        Intent broadcast = new Intent(action);
                        broadcast.putExtra(EXTRA_STA_INFO, macStr);
                        broadcast.putExtra(EXTRA_CURRENT_TIME, mCurrentTime);
                        broadcast.putExtra(EXTRA_STA_COUNT, this.mLinkedStaCount);
                        this.mContext.sendBroadcast(broadcast, "android.permission.ACCESS_WIFI_STATE");
                    }

                    public void setWifiTxPower(String reduceCmd) {
                        Slog.d(TAG, "setWifiTxPower " + reduceCmd);
                        try {
                            this.mConnector.execute("softap", new Object[]{reduceCmd});
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    }

                    private String getWiFiDnsStats(int netid) {
                        StringBuffer buf = new StringBuffer();
                        try {
                            String[] stats = this.mConnector.doListCommand("resolver", 130, new Object[]{"getdnsstat", Integer.valueOf(netid)});
                            if (stats != null) {
                                for (int i = 0; i < stats.length; i++) {
                                    buf.append(stats[i]);
                                    if (i < stats.length - 1) {
                                        buf.append(";");
                                    }
                                }
                            }
                        } catch (NativeDaemonConnectorException e) {
                            Slog.e(TAG, "Cannot communicate with native daemon to get wifi dns stats");
                        }
                        return buf.toString();
                    }

                    private String strToHexStr(String str) {
                        if (str == null) {
                            return null;
                        }
                        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
                        StringBuilder sb = new StringBuilder(bytes.length * 2);
                        for (int i = 0; i < bytes.length; i++) {
                            sb.append("0123456789ABCDEF".charAt((bytes[i] & 240) >> 4));
                            sb.append("0123456789ABCDEF".charAt((bytes[i] & 15) >> 0));
                        }
                        return sb.toString();
                    }

                    private String hexStrToStr(String hexStr) {
                        if (hexStr == null) {
                            return null;
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexStr.length() / 2);
                        for (int i = 0; i < hexStr.length(); i += 2) {
                            baos.write(("0123456789ABCDEF".indexOf(hexStr.charAt(i)) << 4) | "0123456789ABCDEF".indexOf(hexStr.charAt(i + 1)));
                        }
                        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
                    }

                    private ArrayList<String> convertPkgNameToUid(String[] pkgName) {
                        if (pkgName != null) {
                            Slog.d(TAG, "convertPkgNameToUid, pkgName=" + Arrays.asList(pkgName));
                        }
                        ArrayList<String> uidList = new ArrayList();
                        if (pkgName != null && pkgName.length > 0) {
                            int userCount = UserManager.get(this.mContext).getUserCount();
                            List<UserInfo> users = UserManager.get(this.mContext).getUsers();
                            PackageManager pm = this.mContext.getPackageManager();
                            StringBuilder appUidBuilder = new StringBuilder();
                            int uidCount = 0;
                            for (String pkg : pkgName) {
                                for (int n = 0; n < userCount; n++) {
                                    try {
                                        int uid = pm.getPackageUidAsUser(pkg, ((UserInfo) users.get(n)).id);
                                        Slog.d(TAG, "convertPkgNameToUid, pkg=" + pkg + ", uid=" + uid + ", under user.id=" + ((UserInfo) users.get(n)).id);
                                        uidCount++;
                                        if (uidCount % 50 == 0) {
                                            appUidBuilder.append(uid);
                                            appUidBuilder.append(";");
                                            uidList.add(appUidBuilder.toString());
                                            appUidBuilder = new StringBuilder();
                                        } else {
                                            appUidBuilder.append(uid);
                                            appUidBuilder.append(";");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Slog.e(TAG, "convertPkgNameToUid, skip unknown packages!");
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(appUidBuilder.toString())) {
                                uidList.add(appUidBuilder.toString());
                            }
                        }
                        return uidList;
                    }

                    private void setAdFilterRules(String adStrategy, boolean needReset) {
                        Slog.d(TAG, "setAdFilterRules, adStrategy=" + adStrategy + ", needReset=" + needReset);
                        String operation = needReset ? "reset" : "not_reset";
                        int count = 0;
                        int strategyLen = 0;
                        if (adStrategy != null) {
                            strategyLen = adStrategy.length();
                            count = strategyLen / PER_STRATEGY_SIZE;
                            if (strategyLen % PER_STRATEGY_SIZE != 0) {
                                count++;
                            }
                            Slog.d(TAG, "setAdFilterRules, adStrategy len=" + strategyLen + ", divided count=" + count);
                        }
                        int cmdId = this.mCmdId.incrementAndGet();
                        try {
                            Slog.d(TAG, "setAdFilterRules, count=" + count + ", cmdId=" + cmdId);
                            this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_rule", operation, Integer.valueOf(cmdId), Integer.valueOf(count)});
                            if (strategyLen == 0) {
                                Slog.d(TAG, "setAdFilterRules, adStrategy is null!");
                                return;
                            }
                            int i = 1;
                            while (adStrategy.length() > 0) {
                                if (adStrategy.length() > PER_STRATEGY_SIZE) {
                                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy.substring(0, PER_STRATEGY_SIZE).length() + ", seq=" + i + ", cmdId=" + cmdId);
                                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategyTmp});
                                    adStrategy = adStrategy.substring(PER_STRATEGY_SIZE);
                                    i++;
                                } else {
                                    Slog.d(TAG, "setAdFilterRules, adStrategy len=" + adStrategy.length() + ", seq=" + i + ", cmdId=" + cmdId);
                                    this.mConnector.execute("hwfilter", new Object[]{"set_ad_strategy_buf", Integer.valueOf(cmdId), Integer.valueOf(i), adStrategy});
                                    return;
                                }
                            }
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    }

                    private void setApkDlFilterRules(ArrayList<String> appUidList, boolean needReset) {
                        Slog.d(TAG, "setApkDlFilterRules, appUidList=" + appUidList + ", needReset=" + needReset);
                        String operation = needReset ? "reset" : "not_reset";
                        if (appUidList != null) {
                            try {
                                if (appUidList.size() > 0) {
                                    for (int i = 0; i < appUidList.size(); i++) {
                                        if (i == 0) {
                                            this.mConnector.execute("hwfilter", new Object[]{"set_apkdl_strategy_rule", appUidList.get(i), operation});
                                        } else {
                                            this.mConnector.execute("hwfilter", new Object[]{"set_apkdl_strategy_rule", appUidList.get(i), "not_reset"});
                                        }
                                    }
                                    return;
                                }
                            } catch (NativeDaemonConnectorException e) {
                                throw e.rethrowAsParcelableException();
                            }
                        }
                        this.mConnector.execute("hwfilter", new Object[]{"set_apkdl_strategy_rule", null, operation});
                    }

                    private void clearAdOrApkDlFilterRules(ArrayList<String> appUidList, boolean needReset, int strategy) {
                        Slog.d(TAG, "clearApkDlFilterRules, appUidList=" + appUidList + ", needReset=" + needReset + ", strategy=" + strategy);
                        String operation = needReset ? "reset" : "not_reset";
                        int i;
                        if (strategy == 0) {
                            if (appUidList != null) {
                                try {
                                    if (appUidList.size() > 0) {
                                        for (i = 0; i < appUidList.size(); i++) {
                                            if (i == 0) {
                                                this.mConnector.execute("hwfilter", new Object[]{"clear_ad_strategy_rule", appUidList.get(i), operation});
                                            } else {
                                                this.mConnector.execute("hwfilter", new Object[]{"clear_ad_strategy_rule", appUidList.get(i), "not_reset"});
                                            }
                                        }
                                        return;
                                    }
                                } catch (NativeDaemonConnectorException e) {
                                    throw e.rethrowAsParcelableException();
                                }
                            }
                            this.mConnector.execute("hwfilter", new Object[]{"clear_ad_strategy_rule", null, operation});
                        } else if (1 != strategy) {
                        } else {
                            if (appUidList == null || appUidList.size() <= 0) {
                                this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", null, operation});
                                return;
                            }
                            for (i = 0; i < appUidList.size(); i++) {
                                if (i == 0) {
                                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i), operation});
                                } else {
                                    this.mConnector.execute("hwfilter", new Object[]{"clear_apkdl_strategy_rule", appUidList.get(i), "not_reset"});
                                }
                            }
                        }
                    }

                    private void printAdOrApkDlFilterRules(int strategy) {
                        Slog.d(TAG, "printAdOrApkDlFilterRules, strategy=" + strategy);
                        if (strategy == 0) {
                            try {
                                this.mConnector.execute("hwfilter", new Object[]{"output_ad_strategy_rule"});
                            } catch (NativeDaemonConnectorException e) {
                                throw e.rethrowAsParcelableException();
                            }
                        } else if (1 == strategy) {
                            this.mConnector.execute("hwfilter", new Object[]{"output_apkdl_strategy_rule"});
                        }
                    }

                    private void setApkDlUrlUserResult(String downloadId, boolean result) {
                        Slog.d(TAG, "setApkDlUrlUserResult, downloadId=" + downloadId + ", result=" + result);
                        String operation = result ? "allow" : "reject";
                        try {
                            this.mConnector.execute("hwfilter", new Object[]{"apkdl_callback", downloadId, operation});
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    }

                    public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
                        Slog.d(TAG, "receive report_apkdl_event, raw=" + raw);
                        if (cooked.length < 4) {
                            String errorMessage = String.format("Invalid event from daemon (%s)", new Object[]{raw});
                            Slog.d(TAG, "receive report_apkdl_event, return error");
                            throw new IllegalStateException(errorMessage);
                        }
                        long startTime = SystemClock.elapsedRealtime();
                        String downloadId = cooked[1];
                        String uid = cooked[2];
                        if (!this.startTimeMap.containsKey(downloadId)) {
                            this.startTimeMap.put(downloadId, Long.valueOf(startTime));
                        }
                        Matcher m = this.p.matcher(cooked[3]);
                        if (!m.matches() || m.groupCount() < 3) {
                            String url = hexStrToStr(cooked[3]);
                            Slog.d(TAG, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
                            Intent intent = new Intent(INTENT_APKDL_URL_DETECTED);
                            intent.putExtra("startTime", startTime);
                            intent.putExtra("downloadId", downloadId);
                            intent.putExtra("uid", uid);
                            intent.putExtra("url", url);
                            this.mContext.sendBroadcast(intent, "com.huawei.permission.AD_APKDL_STRATEGY");
                            return;
                        }
                        int max = Integer.parseInt(m.group(1));
                        int idx = Integer.parseInt(m.group(2));
                        String subUrl = m.group(3);
                        if (idx == 1) {
                            this.urlBuffer = new StringBuffer();
                            this.urlBuffer.append(subUrl);
                        } else {
                            this.urlBuffer.append(subUrl);
                        }
                        if (max == idx) {
                            url = hexStrToStr(this.urlBuffer.toString());
                            Slog.d(TAG, "onEvent receive report_apkdl_event, startTime=" + startTime + ", downloadId=" + downloadId + ", uid=" + uid + ", url=" + url);
                            intent = new Intent(INTENT_APKDL_URL_DETECTED);
                            intent.putExtra("startTime", (Serializable) this.startTimeMap.get(downloadId));
                            intent.putExtra("downloadId", downloadId);
                            intent.putExtra("uid", uid);
                            intent.putExtra("url", url);
                            this.mContext.sendBroadcast(intent, "com.huawei.permission.AD_APKDL_STRATEGY");
                        }
                    }

                    public void systemReady() {
                        super.systemReady();
                        initNetworkAccessWhitelist();
                    }

                    private void initNetworkAccessWhitelist() {
                        final List<String> networkAccessWhitelist = HwDeviceManager.getList(9);
                        if (networkAccessWhitelist != null && !networkAccessWhitelist.isEmpty()) {
                            Slog.d(TAG, "networkAccessWhitelist has been set");
                            new Thread() {
                                public void run() {
                                    HwNetworkManagementService.this.setNetworkAccessWhitelist(networkAccessWhitelist);
                                }
                            }.start();
                        }
                    }

                    public void setNetworkAccessWhitelist(List<String> addrList) {
                        if (addrList != null) {
                            try {
                                if (!addrList.isEmpty()) {
                                    int size = addrList.size();
                                    Slog.d(TAG, "set ipwhitelist:" + ((String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_SET, addrList.get(0)}).get(0)));
                                    for (int i = 1; i < size; i++) {
                                        Slog.d(TAG, "add ipwhitelist:" + ((String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_ADD, addrList.get(i)}).get(0)));
                                    }
                                    return;
                                }
                            } catch (NullPointerException npe) {
                                Slog.e(TAG, "runNetFilterCmd:", npe);
                                return;
                            } catch (NativeDaemonConnectorException nde) {
                                Slog.e(TAG, "runNetFilterCmd:", nde);
                                return;
                            }
                        }
                        Slog.d(TAG, "clear ipwhitelist:" + ((String) this.mConnector.doCommand(CMD_NET_FILTER, new Object[]{ARG_IP_WHITELIST, ARG_CLEAR}).get(0)));
                    }

                    private void removeLegacyRouteForNetId(int netId, RouteInfo routeInfo, int uid) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
                        Command cmd = new Command("network", new Object[]{"route", "legacy", Integer.valueOf(uid), "remove", Integer.valueOf(netId)});
                        LinkAddress la = routeInfo.getDestinationLinkAddress();
                        cmd.appendArg(routeInfo.getInterface());
                        cmd.appendArg(la.getAddress().getHostAddress() + "/" + la.getPrefixLength());
                        if (routeInfo.hasGateway()) {
                            cmd.appendArg(routeInfo.getGateway().getHostAddress());
                        }
                        try {
                            this.mConnector.execute(cmd);
                        } catch (NativeDaemonConnectorException e) {
                            throw e.rethrowAsParcelableException();
                        }
                    }

                    public boolean closeSocketsForUid(int uid) {
                        try {
                            this.mNetdService.socketDestroy(new UidRange[]{new UidRange(uid, uid)}, new int[0]);
                            return true;
                        } catch (Exception e) {
                            Slog.e(TAG, "Error closing sockets for uid " + uid + ": " + e);
                            return false;
                        }
                    }
                }
