package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;

public class HwSoftApManager extends SoftApManager {
    private static boolean DBG = HWFLOW;
    protected static final boolean HWFLOW;
    private static final int NT_CHINA_CMCC = 3;
    private static final int NT_CHINA_UT = 2;
    private static final int NT_FOREIGN = 1;
    private static final int NT_UNREG = 0;
    private static final String TAG = "HwSoftApManager";
    private int mDataSub = -1;
    private String mOperatorNumericSub0 = null;
    private String mOperatorNumericSub1 = null;
    private PhoneStateListener[] mPhoneStateListener;
    private int mServiceStateSub0 = 1;
    private int mServiceStateSub1 = 1;
    private TelephonyManager mTelephonyManager;
    private WifiChannelXmlParse mWifiChannelXmlParse = null;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public HwSoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService nmService, ConnectivityManager connectivityManager, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        super(context, looper, wifiNative, nmService, connectivityManager, countryCode, allowed2GChannels, listener);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwSoftApManager.this.mDataSub = intent.getIntExtra("subscription", -1);
            }
        }, new IntentFilter("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"));
        this.mDataSub = Global.getInt(context.getContentResolver(), "multi_sim_data_call", 0);
        registerPhoneStateListener(context);
    }

    private void registerPhoneStateListener(Context context) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mPhoneStateListener = new PhoneStateListener[2];
        for (int i = 0; i < 2; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListener[i], 1);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subId) {
        return new PhoneStateListener(subId) {
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    if (HwSoftApManager.DBG) {
                        Log.d(HwSoftApManager.TAG, "PhoneStateListener " + this.mSubId);
                    }
                    if (this.mSubId == 0) {
                        HwSoftApManager.this.mServiceStateSub0 = state.getDataRegState();
                        HwSoftApManager.this.mOperatorNumericSub0 = state.getOperatorNumeric();
                    } else if (this.mSubId == 1) {
                        HwSoftApManager.this.mServiceStateSub1 = state.getDataRegState();
                        HwSoftApManager.this.mOperatorNumericSub1 = state.getOperatorNumeric();
                    }
                }
            }
        };
    }

    private int getRegistedNetworkType() {
        int serviceState;
        String numeric;
        if (this.mDataSub == 0) {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        } else if (this.mDataSub != 1) {
            return 0;
        } else {
            serviceState = this.mServiceStateSub0;
            numeric = this.mOperatorNumericSub0;
        }
        Log.d(TAG, "isRegistedNetworkType mDataSub " + this.mDataSub + ", serviceState " + serviceState + " , numeric " + numeric);
        if (serviceState != 0 || (numeric != null && numeric.length() >= 5 && numeric.substring(0, 5).equals("99999"))) {
            return 0;
        }
        if (numeric == null || numeric.length() < 3 || !numeric.substring(0, 3).equals("460")) {
            return (numeric == null || numeric.equals("")) ? 0 : 1;
        } else {
            if ("46000".equals(this.mOperatorNumericSub0) || "46002".equals(this.mOperatorNumericSub0) || "46007".equals(this.mOperatorNumericSub0)) {
                return 3;
            }
            return 2;
        }
    }

    private String getCurrentBand() {
        String ret = null;
        String[] bandrst = HwTelephonyManagerInner.getDefault().queryServiceCellBand();
        if (bandrst != null) {
            if (bandrst.length < 2) {
                if (DBG) {
                    Log.d(TAG, "getCurrentBand bandrst error.");
                }
                return null;
            } else if ("GSM".equals(bandrst[0])) {
                switch (Integer.parseInt(bandrst[1])) {
                    case 0:
                        ret = "GSM850";
                        break;
                    case 1:
                        ret = "GSM900";
                        break;
                    case 2:
                        ret = "GSM1800";
                        break;
                    case 3:
                        ret = "GSM1900";
                        break;
                    default:
                        Log.e(TAG, "should not be here.");
                        break;
                }
            } else {
                ret = "CDMA".equals(bandrst[0]) ? "BC0" : bandrst[0] + bandrst[1];
            }
        }
        if (DBG) {
            Log.d(TAG, "getCurrentBand rst is " + ret);
        }
        return ret;
    }

    private ArrayList<Integer> getAllowed2GChannels(ArrayList<Integer> allowedChannels) {
        int networkType = getRegistedNetworkType();
        ArrayList<Integer> intersectChannels = new ArrayList();
        if (allowedChannels == null) {
            return null;
        }
        if (networkType == 3) {
            intersectChannels.add(Integer.valueOf(6));
        } else if (networkType == 2) {
            intersectChannels.add(Integer.valueOf(1));
            intersectChannels.add(Integer.valueOf(6));
        } else if (networkType == 1) {
            this.mWifiChannelXmlParse = WifiChannelXmlParse.getInstance();
            ArrayList<Integer> vaildChannels = this.mWifiChannelXmlParse.getValidChannels(getCurrentBand(), true);
            intersectChannels = (ArrayList) allowedChannels.clone();
            if (vaildChannels != null) {
                intersectChannels.retainAll(vaildChannels);
            }
            if (intersectChannels.size() == 0) {
                intersectChannels = allowedChannels;
            }
        } else {
            intersectChannels = allowedChannels;
        }
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("channels: ");
            for (Integer channel : intersectChannels) {
                sb.append(channel.toString()).append(",");
            }
            Log.d(TAG, "2G " + sb);
        }
        return intersectChannels;
    }

    private int[] getAllowed5GChannels(WifiNative wifiNative) {
        Exception e;
        int[] allowedChannels = wifiNative.getChannelsForBand(2);
        if (allowedChannels == null || allowedChannels.length <= 1) {
            return allowedChannels;
        }
        int i;
        int[] values = new int[allowedChannels.length];
        this.mWifiChannelXmlParse = WifiChannelXmlParse.getInstance();
        ArrayList<Integer> vaildChannels = this.mWifiChannelXmlParse.getValidChannels(getCurrentBand(), false);
        int counter = 0;
        if (vaildChannels != null) {
            i = 0;
            int counter2 = 0;
            while (i < allowedChannels.length) {
                try {
                    if (vaildChannels.contains(Integer.valueOf(ApConfigUtil.convertFrequencyToChannel(allowedChannels[i])))) {
                        counter = counter2 + 1;
                        try {
                            values[counter2] = allowedChannels[i];
                        } catch (Exception e2) {
                            e = e2;
                        }
                    } else {
                        counter = counter2;
                    }
                    i++;
                    counter2 = counter;
                } catch (Exception e3) {
                    e = e3;
                    counter = counter2;
                }
            }
            counter = counter2;
        }
        StringBuilder sb;
        if (counter != 0) {
            Log.d(TAG, "5G counter is 0");
            if (DBG) {
                sb = new StringBuilder();
                sb.append("allowedChannels channels: ");
                for (int convertFrequencyToChannel : allowedChannels) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(convertFrequencyToChannel)).append(",");
                }
                Log.d(TAG, "5G " + sb);
            }
            return allowedChannels;
        }
        int[] intersectChannels = new int[counter];
        for (i = 0; i < counter; i++) {
            intersectChannels[i] = values[i];
        }
        if (DBG) {
            sb = new StringBuilder();
            sb.append("allowedChannels channels: ");
            for (int convertFrequencyToChannel2 : allowedChannels) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(convertFrequencyToChannel2)).append(",");
            }
            sb.append("intersectChannels channels: ");
            for (i = 0; i < counter; i++) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(intersectChannels[i])).append(",");
            }
            Log.d(TAG, "5G " + sb.toString());
        }
        return intersectChannels;
        e.printStackTrace();
        if (counter != 0) {
            int[] intersectChannels2 = new int[counter];
            for (i = 0; i < counter; i++) {
                intersectChannels2[i] = values[i];
            }
            if (DBG) {
                sb = new StringBuilder();
                sb.append("allowedChannels channels: ");
                while (i < allowedChannels.length) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(convertFrequencyToChannel2)).append(",");
                }
                sb.append("intersectChannels channels: ");
                for (i = 0; i < counter; i++) {
                    sb.append(ApConfigUtil.convertFrequencyToChannel(intersectChannels2[i])).append(",");
                }
                Log.d(TAG, "5G " + sb.toString());
            }
            return intersectChannels2;
        }
        Log.d(TAG, "5G counter is 0");
        if (DBG) {
            sb = new StringBuilder();
            sb.append("allowedChannels channels: ");
            while (i < allowedChannels.length) {
                sb.append(ApConfigUtil.convertFrequencyToChannel(convertFrequencyToChannel2)).append(",");
            }
            Log.d(TAG, "5G " + sb);
        }
        return allowedChannels;
    }

    public int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        if (!wifiNative.isHalStarted()) {
            config.apBand = 0;
            config.apChannel = 6;
            return 0;
        } else if (config.apBand == 1 && countryCode == null) {
            Log.e(TAG, "5GHz band is not allowed without country code");
            return 2;
        } else {
            if (config.apChannel == 0) {
                config.apChannel = ApConfigUtil.chooseApChannel(config.apBand, getAllowed2GChannels(allowed2GChannels), getAllowed5GChannels(wifiNative));
                if (config.apChannel == -1) {
                    if (wifiNative.isGetChannelsForBandSupported()) {
                        Log.e(TAG, "Failed to get available channel.");
                        return 1;
                    }
                    config.apBand = 0;
                    config.apChannel = 6;
                }
            }
            if (DBG) {
                Log.d(TAG, "updateApChannelConfig apChannel: " + config.apChannel);
            }
            return 0;
        }
    }
}
