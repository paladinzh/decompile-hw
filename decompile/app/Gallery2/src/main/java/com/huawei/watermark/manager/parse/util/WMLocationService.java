package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.ConditionVariable;
import android.util.Log;
import com.android.gallery3d.R;
import com.huawei.watermark.WatermarkDelegate.LocationSettingDelegate;
import com.huawei.watermark.controller.WMLocationManager;
import com.huawei.watermark.controller.WMLocationManager.LocationChangedListener;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.util.IHWPoiSearch.OnPoiSearchCallback;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMFileUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class WMLocationService {
    public static final String AUTHORITY = "com.huawei.android.weather";
    public static final String CITYNOWSELECTED = "10";
    public static final String CITYTYPE = "city_type";
    public static final String CITY_NAME = "city_name";
    public static final String CITY_NATIVE = "city_native";
    public static final String TABLE_NAME = "cityInfo";
    private static Location loc = null;
    private Location lastLocation = null;
    private List<Address> mAddresses = new ArrayList();
    private boolean mCanStart;
    private Context mContext;
    private boolean mIsLocationSuccess = false;
    private LocationChangedListener mLocationChangedListener = new LocationChangedListener() {
        public void onLocationChanged() {
            if (!WMLocationService.this.mIsLocationSuccess) {
                WMLocationService.this.startLocationUpdateThread();
            }
        }
    };
    private List<LocationEventListener> mLocationEventListeners = new ArrayList();
    private List<LocationUpdateCallback> mLocationUpdateCallbacks = new ArrayList();
    private LocationUpdateThread mLocationUpdateThread;
    private OnPoiSearchCallback mOnPoiSearchCallback = new OnPoiSearchCallback() {
        public void onPoiSearched(List<Address> address) {
            WMLocationService.this.mPoiAddresses = address;
            WMLocationService.this.reportCallbacks();
        }
    };
    private List<Address> mPoiAddresses = new ArrayList();
    private Object mSyncObj = new Object();
    private WMLocationManager mWMLocationManager;

    public interface LocationUpdateCallback {
        void onAddressReport(List<Address> list);
    }

    private class LocationUpdateThread extends Thread {
        private boolean mCancel;
        private ConditionVariable mSig = new ConditionVariable();

        public LocationUpdateThread() {
            setName("LocationUpdateThread");
        }

        @SuppressWarnings({"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
        public void run() {
            WMLocationService.loc = serachLocation();
            if (WMLocationService.loc == null) {
                WMLocationService.this.onLocationFailed();
                WMLocationService.this.mLocationUpdateThread = null;
                return;
            }
            WMLocationService.this.onLocationSuccess(WMLocationService.loc.getLongitude(), WMLocationService.loc.getLatitude());
            if (this.mCancel) {
                WMLocationService.this.mLocationUpdateThread = null;
                return;
            }
            if (WMBaseUtil.isChinaProductLocaleRegion() && !WMLocationService.loc.equals(WMLocationService.this.lastLocation)) {
                WMLocationService.this.searchPoi(WMLocationService.loc);
                WMLocationService.this.lastLocation = WMLocationService.loc;
            }
            if (this.mCancel) {
                WMLocationService.this.mLocationUpdateThread = null;
                return;
            }
            Locale currentLocale = WMBaseUtil.isSimplifiedOrTraditionalChineseLanguage(Locale.getDefault()) ? Locale.getDefault() : Locale.US;
            if (WMLocationService.this.mContext == null) {
                WMLocationService.this.mLocationUpdateThread = null;
                return;
            }
            WMLocationService.this.getAddresses(System.currentTimeMillis(), new Geocoder(WMLocationService.this.mContext, currentLocale));
            if (this.mCancel) {
                WMLocationService.this.mLocationUpdateThread = null;
            } else if (WMLocationService.this.mContext == null) {
                WMLocationService.this.mLocationUpdateThread = null;
            } else {
                if (WMCollectionUtil.isEmptyCollection(WMLocationService.this.mAddresses)) {
                    Log.w("Watermark_location_service", "geocoder.getFromLocation got null addresses");
                    WMLocationService.this.mAddresses = WMLocationService.this.getAddressFromDB(WMLocationService.this.mContext);
                    if (!WMCollectionUtil.isEmptyCollection(WMLocationService.this.mAddresses)) {
                        ((Address) WMLocationService.this.mAddresses.get(0)).setLongitude(WMLocationService.loc.getLongitude());
                        ((Address) WMLocationService.this.mAddresses.get(0)).setLatitude(WMLocationService.loc.getLatitude());
                        Log.w("Watermark_location_service", "getAddressFromDB success");
                    }
                }
                if (isCancelOrEmptyCollection()) {
                    WMLocationService.this.mLocationUpdateThread = null;
                    return;
                }
                WMLocationService.this.reportCallbacks();
                WMLocationService.this.mLocationUpdateThread = null;
            }
        }

        private boolean isCancelOrEmptyCollection() {
            return (this.mCancel || WMCollectionUtil.isEmptyCollection(WMLocationService.this.mAddresses)) ? true : WMCollectionUtil.isEmptyCollection(WMLocationService.this.mLocationUpdateCallbacks);
        }

        private Location serachLocation() {
            if (WMLocationService.this.mContext == null || WMLocationService.this.mWMLocationManager == null) {
                return null;
            }
            return WMLocationService.this.mWMLocationManager.getCurrentLocation();
        }

        public void cancel() {
            this.mCancel = true;
            this.mSig.open();
        }
    }

    public WMLocationService(Context mContext) {
        this.mContext = mContext;
        this.mWMLocationManager = new WMLocationManager(this.mContext);
    }

    public void addLocationEventListener(LocationEventListener listener) {
        if (this.mLocationEventListeners != null && !this.mLocationEventListeners.contains(listener)) {
            this.mLocationEventListeners.add(listener);
        }
    }

    private void onLocationSuccess(double longitude, double latitude) {
        Log.d("Watermark_location_service", "onLocationSuccess");
        this.mIsLocationSuccess = true;
        if (this.mLocationEventListeners != null) {
            for (LocationEventListener listener : this.mLocationEventListeners) {
                listener.onLocationSuccess(longitude, latitude);
            }
        }
    }

    private void onLocationFailed() {
        Log.d("Watermark_location_service", "onLocationFailed");
        if (this.mLocationEventListeners != null) {
            for (LocationEventListener listener : this.mLocationEventListeners) {
                listener.onLocationFailed();
            }
        }
    }

    public void setLocationSettingDelegate(LocationSettingDelegate temp) {
        if (this.mWMLocationManager != null) {
            this.mWMLocationManager.setLocationSettingDelegate(temp);
        }
    }

    public LocationSettingDelegate getLocationSettingDelegate() {
        if (this.mWMLocationManager == null) {
            return null;
        }
        return this.mWMLocationManager.getLocationSettingDelegate();
    }

    public void locationSettingStatusChanged(boolean on) {
        if (this.mWMLocationManager != null) {
            this.mWMLocationManager.locationSettingStatusChanged(on);
        }
    }

    public void start() {
        this.mCanStart = true;
        if (this.mWMLocationManager != null) {
            this.mWMLocationManager.resume();
        }
        reportCallbacks();
        startLocationUpdateThread();
        this.mWMLocationManager.setLocationChangedListener(this.mLocationChangedListener);
    }

    public void release() {
        this.mCanStart = false;
        this.mIsLocationSuccess = false;
        if (this.mWMLocationManager != null) {
            this.mWMLocationManager.pause();
        }
        if (this.mLocationUpdateThread != null) {
            this.mLocationUpdateThread.cancel();
        }
        if (this.mWMLocationManager != null) {
            this.mWMLocationManager.setLocationChangedListener(null);
        }
        synchronized (this.mSyncObj) {
            if (!WMCollectionUtil.isEmptyCollection(this.mLocationUpdateCallbacks)) {
                this.mLocationUpdateCallbacks.clear();
            }
        }
        this.mLocationUpdateThread = null;
        if (!WMCollectionUtil.isEmptyCollection(this.mAddresses)) {
            this.mAddresses.clear();
        }
        if (!WMCollectionUtil.isEmptyCollection(this.mPoiAddresses)) {
            this.mPoiAddresses.clear();
        }
    }

    private void getAddresses(long time, Geocoder geocoder) {
        try {
            Collection addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (!WMCollectionUtil.isEmptyCollection(addresses)) {
                this.mAddresses = toTrimCityAddressArray(addresses, this.mContext);
            }
        } catch (IOException e) {
            Log.w("Watermark_location_service", "geocoder.getFromLocation got exception : " + e.getMessage());
        }
        Log.d("Watermark_location_service", "geocoder.getFromLocation cost : " + (System.currentTimeMillis() - time));
    }

    public List<Address> getAddressFromDB(Context context) {
        String cityName = getDefaultCityFormDB(context);
        if (WMStringUtil.isEmptyString(cityName)) {
            return null;
        }
        List<Address> addrs = new ArrayList();
        Address address = new Address(Locale.getDefault());
        address.setLocality(cityName);
        addrs.add(address);
        return addrs;
    }

    private static String getLocationStrFromAddress(Address item) {
        if (item == null) {
            return null;
        }
        String location = item.getLocality();
        if (WMStringUtil.isEmptyString(location)) {
            return item.getSubAdminArea();
        }
        return location;
    }

    public static List<String> toStringArray(List<Address> address, Context context) {
        if (WMCollectionUtil.isEmptyCollection((Collection) address)) {
            return null;
        }
        List<String> ret = new ArrayList();
        for (Address item : address) {
            String addrStr = getLocationStrFromAddress(item);
            if (!WMStringUtil.isEmptyString(addrStr)) {
                ret.add(trimCity(addrStr, context));
            }
        }
        return ret;
    }

    private List<Address> toTrimCityAddressArray(List<Address> address, Context context) {
        if (WMCollectionUtil.isEmptyCollection((Collection) address)) {
            return null;
        }
        List<Address> ret = new ArrayList();
        for (Address item : address) {
            String addrStr = getLocationStrFromAddress(item);
            if (!WMStringUtil.isEmptyString(addrStr)) {
                item.setLocality(trimCity(addrStr, context));
                ret.add(item);
            }
        }
        return ret;
    }

    private static String trimCity(String cityInfo, Context context) {
        String city = context.getString(R.string.city);
        if (WMStringUtil.isEmptyString(cityInfo)) {
            WMLog.d("Watermark_location_service", "WMLocationService trimCity cityInfo isEmptyString");
            return cityInfo;
        } else if (WMStringUtil.isEmptyString(city)) {
            WMLog.d("Watermark_location_service", "WMLocationService trimCity city isEmptyString");
            return cityInfo;
        } else if (cityInfo.length() <= city.length()) {
            WMLog.d("Watermark_location_service", "WMLocationService trimCity cityInfo is shorter than city");
            return cityInfo;
        } else {
            if (city.equals(cityInfo.substring(cityInfo.length() - city.length()))) {
                cityInfo = cityInfo.substring(0, cityInfo.length() - city.length());
            }
            return cityInfo;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getDefaultCityFormDB(Context context) {
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(Uri.parse("content://com.huawei.android.weather/cityInfo"), null, "city_type = 10", null, null);
            if (closeable == null || closeable.getCount() == 0) {
                WMFileUtil.closeSilently(closeable);
                return null;
            }
            closeable.moveToFirst();
            String city_name = closeable.getString(closeable.getColumnIndex(CITY_NAME));
            String city_native = closeable.getString(closeable.getColumnIndex(CITY_NATIVE));
            if (WMStringUtil.isEmptyString(city_native)) {
                WMFileUtil.closeSilently(closeable);
                return city_name;
            }
            WMFileUtil.closeSilently(closeable);
            return city_native;
        } catch (Exception ex) {
            WMLog.e("Watermark_location_service", "getCityInfoFromUserAddedCityList ex =" + ex.getMessage());
            return null;
        } catch (Throwable th) {
            WMFileUtil.closeSilently(closeable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void reportCallbacks() {
        if (this.mCanStart) {
            synchronized (this.mSyncObj) {
                if (WMCollectionUtil.isEmptyCollection(this.mLocationUpdateCallbacks)) {
                    return;
                }
                Collection addrs = new ArrayList();
                if (!WMCollectionUtil.isEmptyCollection(this.mAddresses)) {
                    addrs.addAll(this.mAddresses);
                }
                if (!WMCollectionUtil.isEmptyCollection(this.mPoiAddresses)) {
                    addrs.addAll(this.mPoiAddresses);
                }
                if (!WMCollectionUtil.isEmptyCollection(addrs)) {
                    for (LocationUpdateCallback locationUpdateCallback : this.mLocationUpdateCallbacks) {
                        locationUpdateCallback.onAddressReport(addrs);
                    }
                }
            }
        }
    }

    private void reportCallback(LocationUpdateCallback locationUpdateCallback) {
        if (this.mCanStart) {
            List<Address> addrs = new ArrayList();
            if (!WMCollectionUtil.isEmptyCollection(this.mAddresses)) {
                addrs.addAll(this.mAddresses);
            }
            if (!WMCollectionUtil.isEmptyCollection(this.mPoiAddresses)) {
                addrs.addAll(this.mPoiAddresses);
            }
            locationUpdateCallback.onAddressReport(addrs);
        }
    }

    private void searchPoi(Location loc) {
        if (this.mContext != null) {
            new KayRuckerPoiSearch(this.mContext).poiSearch(loc, this.mOnPoiSearchCallback);
        }
    }

    public void addLocationUpdateCallback(LocationUpdateCallback locationUpdateCallback) {
        synchronized (this.mSyncObj) {
            if (WMCollectionUtil.isEmptyCollection(this.mLocationUpdateCallbacks)) {
                this.mLocationUpdateCallbacks = new ArrayList();
            }
            this.mLocationUpdateCallbacks.add(locationUpdateCallback);
        }
        if (!WMCollectionUtil.isEmptyCollection(this.mAddresses)) {
            reportCallback(locationUpdateCallback);
        }
        startLocationUpdateThread();
    }

    public void startLocationUpdateThread() {
        if (this.mLocationUpdateThread == null && this.mCanStart) {
            this.mLocationUpdateThread = new LocationUpdateThread();
            this.mLocationUpdateThread.start();
        }
    }
}
