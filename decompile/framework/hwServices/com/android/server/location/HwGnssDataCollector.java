package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import huawei.android.debug.HwDBGSwitchController;
import java.util.ArrayList;

public class HwGnssDataCollector {
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int GNSS_DATA_COLLECT_EVENT = 74;
    private static final int NTP_SERVER_IP_COLLECT = 0;
    private static final String TAG = "HwGnssLog_DataCollector";
    private static final int UPDATE_LOCATION = 1;
    private static final int UPDATE_NTP_SERVER_INFO = 0;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private Context mContext;
    private GeolocationCollectManager mGeolocationCollectManager = null;
    private HwGnssDataCollectorHandler mHandler;
    private HandlerThread mThread;

    class HwGnssDataCollectorHandler extends Handler {
        private ArrayList list;

        HwGnssDataCollectorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            this.list = (ArrayList) msg.obj;
            switch (msg.what) {
                case 0:
                    HwGnssDataCollector.this.handlerNtpServerInfo((String) this.list.get(0));
                    return;
                case 1:
                    HwGnssDataCollector.this.handlerGeoLocationInfo((Location) this.list.get(0), ((Long) this.list.get(1)).longValue(), (String) this.list.get(2));
                    return;
                default:
                    if (HwGnssDataCollector.DEBUG) {
                        Log.d(HwGnssDataCollector.TAG, "====handleMessage: msg.what = " + msg.what + "====");
                        return;
                    }
                    return;
            }
        }
    }

    HwGnssDataCollector(HandlerThread thread, Context context) {
        this.mThread = thread;
        this.mContext = context;
        this.mHandler = new HwGnssDataCollectorHandler(this.mThread.getLooper());
        this.mGeolocationCollectManager = new GeolocationCollectManager(this.mContext);
    }

    public void updateNtpServerInfo(String address) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(address);
        msg.what = 0;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateLocation(Location location, long time, String provider) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(location);
        list.add(Long.valueOf(time));
        list.add(provider);
        msg.what = 1;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    private void handlerNtpServerInfo(String address) {
        new NtpIpCollector(this.mContext).uploadNtpServerIp(GNSS_DATA_COLLECT_EVENT, 0, address);
    }

    private void handlerGeoLocationInfo(Location location, long time, String provider) {
        if (this.mGeolocationCollectManager != null) {
            this.mGeolocationCollectManager.setGeoLocationInfo(location, time, provider);
        }
    }
}
