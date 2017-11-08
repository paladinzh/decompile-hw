package cn.com.xy.sms.sdk;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.amap.api.services.geocoder.GeocodeSearch;
import java.util.HashSet;

public class XyLoactionManager implements LocationListener {
    public static final double DOUBLE_PRECISION = 1.0E-8d;
    public static final int DO_SEND_MAP_QUERY_URL = 4102;
    public static final int LOCATION_SERVICE_DISTANCE = 0;
    public static final int LOCATION_SERVICE_INTERVAL = 0;
    public static final int QUERY_REQUEST_ERROR = 4100;
    private static final HashSet<String> requestMapSet = new HashSet();
    private Handler mHandler = null;
    private LocationManager mLocationManager = null;
    private Handler mTimeOutHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimeOutRunnable = new Runnable() {
        public void run() {
            try {
                if (XyLoactionManager.this.mLocationManager != null) {
                    XyLoactionManager.this.mLocationManager.removeUpdates(XyLoactionManager.this);
                }
                if (XyLoactionManager.this.provider != null) {
                    XyLoactionManager.requestMapSet.remove(XyLoactionManager.this.provider);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    };
    private String provider = null;

    private XyLoactionManager(Context ctx, Handler handler) {
        this.mHandler = handler;
        this.mLocationManager = (LocationManager) ctx.getSystemService(NetUtil.REQ_QUERY_LOCATION);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized Location getLocation(Context ctx, Handler handler) {
        synchronized (XyLoactionManager.class) {
            if (ctx == null) {
                return null;
            }
            try {
                Location location = new XyLoactionManager(ctx, handler).getGPSLocation();
                if (location == null) {
                    location = new XyLoactionManager(ctx, handler).getNetWorkLocation();
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("XyLocationManager onLocationChanged error: " + e.getMessage(), e);
                return null;
            }
        }
    }

    private void sendCommand(String provider, Bundle bd) {
        try {
            this.mLocationManager.sendExtraCommand(provider, "force_xtra_injection", bd);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Location getGPSLocation() {
        if (this.mLocationManager == null) {
            return null;
        }
        this.provider = GeocodeSearch.GPS;
        Location location = null;
        if (this.mLocationManager.isProviderEnabled(GeocodeSearch.GPS)) {
            sendCommand(GeocodeSearch.GPS, new Bundle());
            if (requestMapSet.contains(GeocodeSearch.GPS)) {
                this.mLocationManager.requestLocationUpdates(GeocodeSearch.GPS, 0, 0.0f, this);
                requestMapSet.add(GeocodeSearch.GPS);
                this.mTimeOutHandler.postDelayed(this.mTimeOutRunnable, 30000);
            }
            location = this.mLocationManager.getLastKnownLocation(GeocodeSearch.GPS);
        }
        if (isValidLocaiton(GeocodeSearch.GPS, location)) {
            sendMsgToHandler(location, this.mHandler);
        } else {
            location = null;
        }
        return location;
    }

    public Location getNetWorkLocation() {
        if (this.mLocationManager == null) {
            return null;
        }
        this.provider = "network";
        Location location = null;
        if (this.mLocationManager.isProviderEnabled("network")) {
            sendCommand("network", new Bundle());
            if (requestMapSet.contains("network")) {
                this.mLocationManager.requestLocationUpdates("network", 0, 0.0f, this);
                requestMapSet.add("network");
                this.mTimeOutHandler.postDelayed(this.mTimeOutRunnable, 5000);
            }
            this.mLocationManager.requestLocationUpdates("network", 0, 0.0f, this);
            location = this.mLocationManager.getLastKnownLocation("network");
        }
        if (isValidLocaiton("network", location)) {
            sendMsgToHandler(location, this.mHandler);
        } else {
            location = null;
        }
        return location;
    }

    private static void sendMsgToHandler(Location location, Handler handler) {
        if (handler != null) {
            if (location == null) {
                try {
                    handler.obtainMessage(4100).sendToTarget();
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("XyLocationManager onLocationChanged error: " + e.getMessage(), e);
                }
            } else {
                Message msg = handler.obtainMessage(4102);
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", location.getLatitude());
                bundle.putDouble("longitude", location.getLongitude());
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }
    }

    private static boolean isValidLocaiton(String provider, Location location) {
        if (location == null) {
            return false;
        }
        return isValidLocaiton(provider, location.getLatitude(), location.getLongitude());
    }

    private static boolean isValidLocaiton(String provider, double lon, double lat) {
        return true;
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    public void onProviderEnabled(String arg0) {
    }

    public void onProviderDisabled(String arg0) {
    }

    public void onLocationChanged(Location loc) {
        try {
            if (this.mLocationManager != null) {
                this.mLocationManager.removeUpdates(this);
                this.mTimeOutHandler.removeCallbacks(this.mTimeOutRunnable);
            }
            if (this.provider != null) {
                requestMapSet.remove(this.provider);
            }
            sendMsgToHandler(loc, this.mHandler);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("XyLocationManager onLocationChanged error: " + e.getMessage(), e);
        }
    }
}
