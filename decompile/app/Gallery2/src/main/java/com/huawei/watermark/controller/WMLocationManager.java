package com.huawei.watermark.controller;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.huawei.watermark.WatermarkDelegate.LocationSettingDelegate;
import com.huawei.watermark.decoratorclass.WMLog;

public class WMLocationManager {
    private Context mContext;
    private LocationChangedListener mLocationChangedListener;
    LocationListener[] mLocationListeners = new LocationListener[]{new LocationListener(GeocodeSearch.GPS), new LocationListener("network")};
    private LocationManager mLocationManager;
    private LocationSettingDelegate mLocationSettingDelegate;
    private boolean mRecordLocation;
    private boolean misInitialize = false;

    public interface LocationChangedListener {
        void onLocationChanged();
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        String mProvider;
        boolean mValid = false;

        public LocationListener(String provider) {
            this.mProvider = provider;
            this.mLastLocation = new Location(this.mProvider);
        }

        public void onLocationChanged(Location newLocation) {
            if (newLocation.getLatitude() != 0.0d || newLocation.getLongitude() != 0.0d) {
                if (!this.mValid) {
                    WMLog.d("WMLocationManager", "Got first location.");
                }
                WMLog.d("WMLocationManager", "onLocationChanged");
                this.mLastLocation.set(newLocation);
                this.mValid = true;
                if (WMLocationManager.this.mLocationChangedListener != null) {
                    WMLocationManager.this.mLocationChangedListener.onLocationChanged();
                }
            }
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            this.mValid = false;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case 0:
                case 1:
                    this.mValid = false;
                    return;
                default:
                    return;
            }
        }

        public Location current() {
            return this.mValid ? this.mLastLocation : null;
        }
    }

    public WMLocationManager(Context context) {
        this.mContext = context;
    }

    public void setLocationSettingDelegate(LocationSettingDelegate temp) {
        this.mLocationSettingDelegate = temp;
    }

    public LocationSettingDelegate getLocationSettingDelegate() {
        return this.mLocationSettingDelegate;
    }

    public void resume() {
        if (!this.misInitialize) {
            if (this.mLocationSettingDelegate != null) {
                recordLocation(this.mLocationSettingDelegate.getGPSMenuSetting());
            }
            this.misInitialize = true;
        }
    }

    public void pause() {
        recordLocation(false);
        this.misInitialize = false;
    }

    public void locationSettingStatusChanged(boolean on) {
        recordLocation(on);
    }

    public Location getCurrentLocation() {
        if (!this.mRecordLocation) {
            return null;
        }
        for (LocationListener current : this.mLocationListeners) {
            Location l = current.current();
            if (l != null) {
                return l;
            }
        }
        WMLog.d("WMLocationManager", "No location received yet.");
        return null;
    }

    private void recordLocation(final boolean recordLocation) {
        ((Activity) this.mContext).runOnUiThread(new Runnable() {
            public void run() {
                if (WMLocationManager.this.mRecordLocation != recordLocation) {
                    WMLocationManager.this.mRecordLocation = recordLocation;
                    if (recordLocation) {
                        WMLocationManager.this.startReceivingLocationUpdates();
                    } else {
                        WMLocationManager.this.stopReceivingLocationUpdates();
                    }
                }
            }
        });
    }

    private void startReceivingLocationUpdates() {
        if (this.mContext != null) {
            if (this.mLocationManager == null) {
                this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            }
            if (this.mLocationManager != null) {
                try {
                    this.mLocationManager.requestLocationUpdates("network", 1000, 0.0f, this.mLocationListeners[1]);
                } catch (SecurityException ex) {
                    WMLog.i("WMLocationManager", "fail to request location update, ignore", ex);
                } catch (IllegalArgumentException ex2) {
                    WMLog.d("WMLocationManager", "provider does not exist " + ex2.getMessage());
                }
                try {
                    this.mLocationManager.requestLocationUpdates(GeocodeSearch.GPS, 1000, 0.0f, this.mLocationListeners[0]);
                } catch (SecurityException ex3) {
                    WMLog.i("WMLocationManager", "fail to request location update, ignore", ex3);
                } catch (IllegalArgumentException ex22) {
                    WMLog.d("WMLocationManager", "provider does not exist " + ex22.getMessage());
                }
                WMLog.d("WMLocationManager", "startReceivingLocationUpdates");
            }
        }
    }

    private void stopReceivingLocationUpdates() {
        if (this.mLocationManager != null) {
            for (android.location.LocationListener removeUpdates : this.mLocationListeners) {
                try {
                    this.mLocationManager.removeUpdates(removeUpdates);
                } catch (Exception ex) {
                    WMLog.i("WMLocationManager", "fail to remove location listners, ignore", ex);
                }
            }
            WMLog.d("WMLocationManager", "stopReceivingLocationUpdates");
        }
    }

    public void setLocationChangedListener(LocationChangedListener listener) {
        this.mLocationChangedListener = listener;
    }
}
