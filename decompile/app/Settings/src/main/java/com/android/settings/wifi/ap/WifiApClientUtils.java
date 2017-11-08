package com.android.settings.wifi.ap;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import com.android.settings.MLog;
import com.huawei.android.net.wifi.WifiManagerEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WifiApClientUtils {
    private static WifiApClientUtils mInstance;
    private AllowedDevices mAllowedDevices;
    private ConnectionTime mConnectionTime = new ConnectionTime();
    private WifiManager mWifiManager;

    private static class AllowedDevices {
        private List<WifiApClientInfo> mInfoList;
        private boolean needPersist;

        public AllowedDevices(Context context) {
            loadAllowedDevices(context);
        }

        public List<WifiApClientInfo> getAllowedList() {
            return this.mInfoList;
        }

        public void addAllowedDevice(Context context, WifiApClientInfo info) {
            int i = 0;
            while (i < this.mInfoList.size()) {
                if (info.isMACEquals((WifiApClientInfo) this.mInfoList.get(i))) {
                    this.mInfoList.remove(i);
                } else {
                    i++;
                }
            }
            if (info.getId() == -1) {
                info.setId(getNextId());
            }
            this.mInfoList.add(info);
            this.needPersist = true;
        }

        private int getNextId() {
            int maxId = -1;
            for (int i = 0; i < this.mInfoList.size(); i++) {
                WifiApClientInfo info = (WifiApClientInfo) this.mInfoList.get(i);
                if (maxId < info.getId()) {
                    maxId = info.getId();
                }
            }
            return maxId + 1;
        }

        public void editAllowedDevice(Context context, WifiApClientInfo info) {
            int i = 0;
            while (i < this.mInfoList.size()) {
                WifiApClientInfo clientInfo = (WifiApClientInfo) this.mInfoList.get(i);
                if (clientInfo.getId() == info.getId()) {
                    clientInfo.setDeviceName(info.getDeviceName());
                    clientInfo.setMAC(info.getMAC());
                } else if (info.isMACEquals(clientInfo)) {
                    this.mInfoList.remove(i);
                }
                i++;
            }
            this.needPersist = true;
        }

        public void removeAllowedDevice(Context context, WifiApClientInfo info) {
            for (int i = 0; i < this.mInfoList.size(); i++) {
                if (((WifiApClientInfo) this.mInfoList.get(i)).getId() == info.getId()) {
                    this.mInfoList.remove(i);
                    break;
                }
            }
            this.needPersist = true;
        }

        public boolean isAllowAllDevices(Context context) {
            return System.getInt(context.getContentResolver(), "allow_all_devices_connection", 0) == 1;
        }

        public void allowAllDevices(Context context, boolean bAllowed) {
            System.putInt(context.getContentResolver(), "allow_all_devices_connection", bAllowed ? 1 : 0);
        }

        public boolean isDeviceAllowed(WifiApClientInfo info) {
            for (int i = 0; i < this.mInfoList.size(); i++) {
                if (info.isMACEquals((WifiApClientInfo) this.mInfoList.get(i))) {
                    return true;
                }
            }
            return false;
        }

        public boolean canAddDevice() {
            return this.mInfoList.size() < 8;
        }

        public WifiApClientInfo getAllowedDevice(WifiApClientInfo info) {
            if (info.getMAC() == null) {
                return null;
            }
            for (WifiApClientInfo clientinfo : this.mInfoList) {
                if (clientinfo.isMACEquals(info)) {
                    return clientinfo;
                }
            }
            return null;
        }

        public void saveAllowedDevices(Context context) {
            if (this.needPersist) {
                this.needPersist = false;
                new AsyncTask<Context, Object, Object>() {
                    protected Object doInBackground(Context... params) {
                        AllowedDevicesParser.saveAllowedDevices(params[0], new ArrayList(AllowedDevices.this.mInfoList));
                        return null;
                    }
                }.execute(new Context[]{context});
            }
        }

        private void loadAllowedDevices(Context context) {
            if (this.mInfoList != null) {
                this.mInfoList.clear();
            }
            this.mInfoList = AllowedDevicesParser.loadAllowedDevices(context);
        }

        public boolean isNeedPersist() {
            return this.needPersist;
        }
    }

    private static class ConnectionParser {
        private ConnectionParser() {
        }

        public List<WifiApClientInfo> parse(List<String> connectionInfo, ConnectionTime connectionTime, AllowedDevices allowedDevices) {
            if (connectionInfo == null) {
                return null;
            }
            List<WifiApClientInfo> connectedList = new ArrayList();
            for (String item : connectionInfo) {
                WifiApClientInfo info = parseConnectedItem(item, connectionTime, allowedDevices);
                if (info != null) {
                    connectedList.add(info);
                }
            }
            return connectedList;
        }

        private WifiApClientInfo parseConnectedItem(String item, ConnectionTime connectionTime, AllowedDevices allowedDevices) {
            if (item == null) {
                return null;
            }
            WifiApClientInfo info = new WifiApClientInfo();
            String[] deviceInfos = item.split(" ");
            for (int i = 0; i < deviceInfos.length; i++) {
                if (deviceInfos[i] != null) {
                    if (deviceInfos[i].startsWith("MAC=")) {
                        String strMac = deviceInfos[i].substring("MAC=".length());
                        info.setMAC(strMac);
                        info.setConnectedTime(connectionTime.getConnectedTime(strMac));
                    } else if (deviceInfos[i].startsWith("IP=")) {
                        info.setIP(deviceInfos[i].substring("IP=".length()));
                    } else if (deviceInfos[i].startsWith("DEVICE=")) {
                        info.setDeviceName(deviceInfos[i].substring("DEVICE=".length()));
                    }
                }
            }
            checkDevicesAllowed(info, allowedDevices);
            return info;
        }

        private void checkDevicesAllowed(WifiApClientInfo info, AllowedDevices allowedDevices) {
            info.setId(-1);
            WifiApClientInfo allowedDevice = allowedDevices.getAllowedDevice(info);
            if (allowedDevice != null) {
                info.setId(allowedDevice.getId());
                CharSequence deviceName = allowedDevice.getDeviceName();
                if (deviceName != null) {
                    info.setDeviceName(deviceName);
                }
            }
        }
    }

    private static class ConnectionTime {
        private HashMap<String, Long> mConnectedTimeList = new HashMap();

        public void addConnectedDevice(String mac, String strConnectedTime) {
            if (mac != null && !mac.equals("")) {
                Long connectedTime = Long.valueOf(SystemClock.elapsedRealtime());
                this.mConnectedTimeList.put(mac.toLowerCase(Locale.US), connectedTime);
            }
        }

        public void removeConnectedDevice(String mac) {
            Log.i("WifiApClientUtils", "removeConnectedDevice");
            if (mac != null && !mac.equals("")) {
                this.mConnectedTimeList.remove(mac.toLowerCase(Locale.US));
            }
        }

        public void removeAllConnectedDevice() {
            this.mConnectedTimeList.clear();
        }

        public long getConnectedTime(String mac) {
            Long connectedTime = (Long) this.mConnectedTimeList.get(mac.toLowerCase(Locale.US));
            if (connectedTime == null) {
                return SystemClock.elapsedRealtime();
            }
            return connectedTime.longValue();
        }
    }

    private WifiApClientUtils(Context context) {
        this.mAllowedDevices = new AllowedDevices(context);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public static synchronized WifiApClientUtils getInstance(Context context) {
        WifiApClientUtils wifiApClientUtils;
        synchronized (WifiApClientUtils.class) {
            if (mInstance == null) {
                mInstance = new WifiApClientUtils(context);
            }
            wifiApClientUtils = mInstance;
        }
        return wifiApClientUtils;
    }

    public List<WifiApClientInfo> getAllowedList() {
        return this.mAllowedDevices.getAllowedList();
    }

    public void addAllowedDevice(Context context, WifiApClientInfo info) {
        this.mAllowedDevices.addAllowedDevice(context, info);
        setMacFilters(context);
    }

    public void editAllowedDevice(Context context, WifiApClientInfo info) {
        this.mAllowedDevices.editAllowedDevice(context, info);
        setMacFilters(context);
    }

    public void removeAllowedDevice(Context context, WifiApClientInfo info) {
        this.mAllowedDevices.removeAllowedDevice(context, info);
        setMacFilters(context);
    }

    public boolean isAllowAllDevices(Context context) {
        return this.mAllowedDevices.isAllowAllDevices(context);
    }

    public void allowAllDevices(Context context, boolean bAllowed) {
        this.mAllowedDevices.allowAllDevices(context, bAllowed);
        setMacFilters(context);
    }

    public boolean isDeviceAllowed(WifiApClientInfo info) {
        return this.mAllowedDevices.isDeviceAllowed(info);
    }

    public boolean canAddDevice() {
        return this.mAllowedDevices.canAddDevice();
    }

    public void addConnectedDevice(String mac, String connectedTime) {
        this.mConnectionTime.addConnectedDevice(mac, connectedTime);
    }

    public void removeConnectedDevice(String mac) {
        this.mConnectionTime.removeConnectedDevice(mac);
    }

    public void removeAllConnectedDevice() {
        this.mConnectionTime.removeAllConnectedDevice();
    }

    public List<WifiApClientInfo> getConnectedList() {
        if (this.mWifiManager.getWifiApState() != 13) {
            return null;
        }
        List list = null;
        try {
            list = WifiManagerEx.getApLinkedStaList(this.mWifiManager);
            MLog.d("WifiApClientUtils", "getApLinkedStaList succeed");
        } catch (Exception e) {
            MLog.e("WifiApClientUtils", "WifiManagerEx.getApLinkedStaList is not implemented");
            e.printStackTrace();
        }
        return new ConnectionParser().parse(list, this.mConnectionTime, this.mAllowedDevices);
    }

    public void disconnectDevice(WifiApClientInfo info) {
        try {
            WifiManagerEx.setSoftapDisassociateSta(this.mWifiManager, getMacWithoutSplitter(info.getMAC()));
            MLog.d("WifiApClientUtils", "setSoftapDisassociateSta succeed");
        } catch (Exception e) {
            MLog.e("WifiApClientUtils", "WifiManagerEx.setSoftapDisassociateSta is not implemented");
            e.printStackTrace();
        }
    }

    public void setMacFilters(Context context) {
        String macFilter;
        if (isAllowAllDevices(context)) {
            macFilter = "MAC_MODE=0,MAC_CNT=0";
        } else {
            StringBuilder builder = new StringBuilder("MAC_MODE=2,MAC_CNT=");
            List<WifiApClientInfo> list = this.mAllowedDevices.getAllowedList();
            builder.append(list.size());
            for (WifiApClientInfo info : list) {
                builder.append(",MAC=");
                builder.append(getMacWithoutSplitter(info.getMAC()));
            }
            macFilter = builder.toString();
        }
        try {
            MLog.d("WifiApClientUtils", "setSoftapMacFilter:" + macFilter);
            WifiManagerEx.setSoftapMacFilter(this.mWifiManager, macFilter);
        } catch (Exception e) {
            MLog.e("WifiApClientUtils", "WifiManagerEx.setSoftapMacFilter is not implemented");
            e.printStackTrace();
        }
    }

    private String getMacWithoutSplitter(CharSequence mac) {
        if (mac == null) {
            Log.e("WifiApClientUtils", "getMacWithoutSplitter.mac is null!");
            mac = "";
        }
        return mac.toString().toLowerCase(Locale.US).replace(":", "");
    }

    public static boolean isMacValid(CharSequence mac) {
        if (mac == null || mac.length() != 17) {
            return false;
        }
        CharSequence defaultMac = "00:00:00:00:00:00";
        for (int i = 0; i < defaultMac.length(); i++) {
            if (defaultMac.charAt(i) == '0') {
                if (!isHexNumber(mac.charAt(i))) {
                    return false;
                }
            } else if (mac.charAt(i) != ':') {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexNumber(char c) {
        if (('0' > c || c > '9') && (('a' > c || c > 'f') && ('A' > c || c > 'F'))) {
            return false;
        }
        return true;
    }

    public void persistAllowedListIfChanged(Context context) {
        MLog.d("WifiApClientUtils", "try to save allowed list, need persist = " + this.mAllowedDevices.isNeedPersist());
        this.mAllowedDevices.saveAllowedDevices(context);
    }

    public boolean isSupportConnectManager() {
        boolean z = false;
        try {
            z = WifiManagerEx.isSupportConnectManager(this.mWifiManager);
            MLog.d("WifiApClientUtils", "isSupportConnectManager:" + z);
            return z;
        } catch (Exception e) {
            MLog.e("WifiApClientUtils", "WifiManagerEx.isSupportConnectManager is not implemented");
            e.printStackTrace();
            return z;
        }
    }

    public boolean isSupportChannel() {
        boolean z = false;
        try {
            z = WifiManagerEx.isSupportChannel(this.mWifiManager);
            MLog.d("WifiApClientUtils", "isSupportChannel:" + z);
            return z;
        } catch (Exception e) {
            MLog.e("WifiApClientUtils", "WifiManagerEx.isSupportChannel is not implemented");
            e.printStackTrace();
            return z;
        }
    }
}
