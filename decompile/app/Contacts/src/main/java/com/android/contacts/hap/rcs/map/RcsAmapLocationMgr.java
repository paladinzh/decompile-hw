package com.android.contacts.hap.rcs.map;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.MapFragment;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;

public class RcsAmapLocationMgr implements RcsLocationMgr, LocationSource, AMapLocationListener {
    OnGeocodeSearchListener callLogGeoSearchListenr = new OnGeocodeSearchListener() {
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
            HwLog.i("RcsAmapLocationMgr", "callLogGeoSearchListenr address found rCode " + rCode);
            if (rCode == 1000 && result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null && result.getRegeocodeQuery() != null && result.getRegeocodeQuery().getPoint() != null && RcsAmapLocationMgr.this.mListener != null) {
                String addressName = result.getRegeocodeAddress().getFormatAddress();
                LatLonPoint latLonPoint = result.getRegeocodeQuery().getPoint();
                HwLog.i("RcsAmapLocationMgr", "callLogGeoSearchListener address return");
                RcsAmapLocationMgr.this.mListener.onLocationResult(latLonPoint.getLatitude(), latLonPoint.getLongitude(), addressName);
            }
        }

        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        }
    };
    private Activity mActivity;
    private AMap mAmap;
    private GeocodeSearch mGeocoderSearch;
    private boolean mIsDialayMapDirect;
    private LatLng mLatLng;
    private double mLatitude;
    private RcsLocationListener mListener;
    private AMapLocationClientOption mLocationOption;
    private double mLogitude;
    private MapFragment mMapFragment;
    private RcsPrecallLocationListener mPrecallListener;
    OnMapLoadedListener mapLoadedListener = new OnMapLoadedListener() {
        public void onMapLoaded() {
            HwLog.i("RcsAmapLocationMgr", "onMapLoaded");
            RcsAmapLocationMgr.this.mAmap.clear();
            RcsAmapLocationMgr.this.mAmap.getUiSettings().setZoomControlsEnabled(false);
            RcsAmapLocationMgr.this.mAmap.getUiSettings().setScaleControlsEnabled(false);
            RcsAmapLocationMgr.this.mAmap.getUiSettings().setAllGesturesEnabled(false);
            if (RcsAmapLocationMgr.this.mLatLng == null) {
                RcsAmapLocationMgr.this.mLatLng = new LatLng(360.0d, 360.0d);
            }
            RcsAmapLocationMgr.this.mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom(RcsAmapLocationMgr.this.mLatLng, 15.0f));
            RcsAmapLocationMgr.this.mAmap.addMarker(new MarkerOptions().position(RcsAmapLocationMgr.this.mLatLng));
            if (RcsAmapLocationMgr.this.mIsDialayMapDirect) {
                ContactsThreadPool.getInstance().execute(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            HwLog.e("RcsAmapLocationMgr", "InterruptedException " + e.getMessage());
                        }
                        RcsAmapLocationMgr.this.getAmapScreenShot();
                    }
                });
                RcsAmapLocationMgr.this.mIsDialayMapDirect = false;
            }
        }
    };
    private AMapLocationClient mlocationClient;
    OnGeocodeSearchListener precallGeoSearchListenr = new OnGeocodeSearchListener() {
        public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
            HwLog.i("RcsAmapLocationMgr", "precallGeoSearchListenr address found rCode " + rCode);
            if (!(rCode != 1000 || result == null || result.getRegeocodeAddress() == null || result.getRegeocodeAddress().getFormatAddress() == null || result.getRegeocodeQuery() == null || result.getRegeocodeQuery().getPoint() == null || RcsAmapLocationMgr.this.mPrecallListener == null)) {
                String addressName = result.getRegeocodeAddress().getFormatAddress();
                HwLog.i("RcsAmapLocationMgr", "precallGeoSearchListenr address return ");
                if (!RcsContactsUtils.isSettingsLocationOpen(RcsAmapLocationMgr.this.mActivity) || TextUtils.isEmpty(addressName)) {
                    RcsAmapLocationMgr.this.mPrecallListener.onLocationLatLngResult(360.0d, 360.0d);
                    return;
                }
                RcsAmapLocationMgr.this.mPrecallListener.onLocationLatLngResult(RcsAmapLocationMgr.this.mLatitude, RcsAmapLocationMgr.this.mLogitude);
                RcsAmapLocationMgr.this.mPrecallListener.onLocationAddressResult(addressName);
                try {
                    Thread.sleep(1000);
                    HwLog.i("RcsAmapLocationMgr", "delay 1000ms to snapshot map");
                } catch (InterruptedException e) {
                    HwLog.e("RcsAmapLocationMgr", "InterruptedException " + e.getMessage());
                }
                RcsAmapLocationMgr.this.getAmapScreenShot();
            }
        }

        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        }
    };

    public RcsAmapLocationMgr(Activity activity) {
        this.mActivity = activity;
        this.mGeocoderSearch = new GeocodeSearch(this.mActivity);
        HwLog.i("RcsAmapLocationMgr", "new RcsAmapLocationMgr");
    }

    public void setLocationListener(RcsLocationListener listener) {
        this.mListener = listener;
    }

    public void setPrecallLocationListener(RcsPrecallLocationListener listener) {
        this.mPrecallListener = listener;
    }

    public void setMapdisplayView(RelativeLayout mapDisplayView, Bundle bundle) {
        HwLog.i("RcsAmapLocationMgr", "precall setMapdisplayView ");
        this.mMapFragment = MapFragment.newInstance();
        FragmentTransaction frTransaction = this.mActivity.getFragmentManager().beginTransaction();
        frTransaction.replace(mapDisplayView.getId(), this.mMapFragment);
        frTransaction.commit();
        if (this.mAmap == null) {
            try {
                MapsInitializer.initialize(this.mActivity);
                this.mAmap = this.mMapFragment.getMap();
                if (this.mAmap != null) {
                    this.mAmap.setLocationSource(this);
                    this.mAmap.setOnMapLoadedListener(this.mapLoadedListener);
                    this.mAmap.getUiSettings().setZoomControlsEnabled(false);
                    this.mAmap.getUiSettings().setScaleControlsEnabled(false);
                    this.mAmap.getUiSettings().setAllGesturesEnabled(false);
                }
            } catch (Exception e) {
                HwLog.i("RcsAmapLocationMgr", "initMapFragment Exception");
            }
        }
    }

    public void startLoadMap(double lan, double lon) {
        this.mLatitude = lan;
        this.mLogitude = lon;
        this.mLatLng = new LatLng(this.mLatitude, this.mLogitude);
        if (this.mlocationClient == null) {
            this.mlocationClient = new AMapLocationClient(this.mActivity);
            this.mLocationOption = new AMapLocationClientOption();
            this.mlocationClient.setLocationListener(this);
            this.mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            this.mLocationOption.setNeedAddress(true);
            this.mLocationOption.setInterval(1000);
            this.mLocationOption.setGpsFirst(true);
            this.mlocationClient.setLocationOption(this.mLocationOption);
            HwLog.i("RcsAmapLocationMgr", "startLocation0");
            this.mlocationClient.startLocation();
            return;
        }
        HwLog.i("RcsAmapLocationMgr", "startLocation1");
        this.mlocationClient.startLocation();
    }

    public void getPrecallAddressText(double lat, double lon) {
        this.mGeocoderSearch.setOnGeocodeSearchListener(this.precallGeoSearchListenr);
        this.mGeocoderSearch.getFromLocationAsyn(new RegeocodeQuery(new LatLonPoint(lat, lon), 200.0f, GeocodeSearch.AMAP));
    }

    public void locationOnDestroy() {
        HwLog.i("RcsAmapLocationMgr", "locationOnDestroy");
        if (this.mlocationClient != null) {
            this.mlocationClient.stopLocation();
            this.mlocationClient.onDestroy();
        }
        this.mlocationClient = null;
    }

    public void activate(OnLocationChangedListener listener) {
        if (this.mlocationClient == null) {
            this.mlocationClient = new AMapLocationClient(this.mActivity);
            this.mLocationOption = new AMapLocationClientOption();
            this.mlocationClient.setLocationListener(this);
            this.mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            this.mLocationOption.setNeedAddress(true);
            this.mLocationOption.setInterval(1000);
            this.mLocationOption.setGpsFirst(true);
            this.mlocationClient.setLocationOption(this.mLocationOption);
        }
    }

    private void getAmapScreenShot() {
        HwLog.i("RcsAmapLocationMgr", "getAmapScreenShot");
        this.mAmap.getMapScreenShot(new OnMapScreenShotListener() {
            public void onMapScreenShot(Bitmap bitmap) {
                HwLog.i("RcsAmapLocationMgr", "onMapScreenShot 00 snapshot");
                RcsAmapLocationMgr.this.mPrecallListener.onLocatinonSnapShot(bitmap);
            }

            public void onMapScreenShot(Bitmap bitmap, int status) {
                HwLog.i("RcsAmapLocationMgr", "onMapScreenShot  status = " + status);
            }
        });
    }

    public void onLocationChanged(AMapLocation amapLocation) {
        HwLog.i("RcsAmapLocationMgr", "onLocationChanged  amapLocation");
        if (amapLocation != null) {
            if (!RcsContactsUtils.isSettingsLocationOpen(this.mActivity)) {
                deactivate();
            } else if (amapLocation.getErrorCode() == 0) {
                this.mLatitude = amapLocation.getLatitude();
                this.mLogitude = amapLocation.getLongitude();
                this.mLatLng = new LatLng(this.mLatitude, this.mLogitude);
                this.mAmap.clear();
                this.mAmap.getUiSettings().setZoomControlsEnabled(false);
                this.mAmap.getUiSettings().setScaleControlsEnabled(false);
                this.mAmap.getUiSettings().setAllGesturesEnabled(false);
                this.mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 15.0f));
                this.mAmap.addMarker(new MarkerOptions().position(this.mLatLng));
                deactivate();
                getPrecallAddressText(this.mLatitude, this.mLogitude);
            } else {
                HwLog.e("AmapErr", "error" + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo());
            }
        }
    }

    public void deactivate() {
        HwLog.i("RcsAmapLocationMgr", "deactivate mlocationClient =" + this.mlocationClient);
        if (this.mlocationClient != null) {
            this.mlocationClient.stopLocation();
            this.mlocationClient.onDestroy();
        }
        this.mlocationClient = null;
    }

    public void displayMap(double lat, double lon, boolean displayMapDirect) {
        HwLog.i("RcsAmapLocationMgr", "display map directly");
        this.mLatLng = new LatLng(lat, lon);
        this.mIsDialayMapDirect = displayMapDirect;
    }
}
