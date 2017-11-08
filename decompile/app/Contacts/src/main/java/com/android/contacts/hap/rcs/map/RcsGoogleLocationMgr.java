package com.android.contacts.hap.rcs.map;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RcsGoogleLocationMgr implements RcsLocationMgr, OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, SnapshotReadyCallback, LocationListener {
    private static int MAX_RETURN_VALUE = 1;
    private Activity mActivity;
    private Geocoder mGeocoder = new Geocoder(this.mActivity);
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private boolean mIsDialayMapDirect;
    private LatLng mLatLng;
    private double mLatitude;
    private RcsLocationListener mListener;
    private double mLogitude;
    private MapFragment mMapFragment = MapFragment.newInstance();
    private RcsPrecallLocationListener mPrecallListener;

    public RcsGoogleLocationMgr(Activity activity) {
        this.mActivity = activity;
    }

    public void setLocationListener(RcsLocationListener listener) {
        this.mListener = listener;
    }

    public void setPrecallLocationListener(RcsPrecallLocationListener listener) {
        this.mPrecallListener = listener;
    }

    public void startLoadMap(double lat, double lon) {
        this.mLatitude = lat;
        this.mLogitude = lon;
        this.mLatLng = new LatLng(this.mLatitude, this.mLogitude);
        this.mMapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap map) {
        this.mGoogleMap = map;
        this.mGoogleMap.clear();
        this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 14.0f));
        this.mGoogleMap.addMarker(new MarkerOptions().position(this.mLatLng));
        this.mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        this.mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        this.mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
        this.mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (this.mIsDialayMapDirect) {
            ContactsThreadPool.getInstance().execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        HwLog.i("RcsGoogleLocationMgr", "delay 1000ms to snapshot map");
                    } catch (InterruptedException e) {
                        HwLog.e("RcsGoogleLocationMgr", "InterruptedException " + e.getMessage());
                    }
                    RcsGoogleLocationMgr.this.mGoogleMap.snapshot(RcsGoogleLocationMgr.this);
                }
            });
            this.mIsDialayMapDirect = false;
            return;
        }
        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new Builder(this.mActivity).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        }
        if (!this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.connect();
        }
    }

    public void onConnected(@Nullable Bundle arg0) {
        HwLog.d("RcsGoogleLocationMgr", "onConnected request location");
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, createLocationRequest(), (LocationListener) this);
    }

    public void onConnectionSuspended(int arg0) {
        HwLog.d("RcsGoogleLocationMgr", "onConnectionSuspended  connect");
        this.mGoogleApiClient.connect();
    }

    public void onConnectionFailed(@NonNull ConnectionResult arg0) {
        HwLog.d("RcsGoogleLocationMgr", "onConnectionFailed ");
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(100);
        return locationRequest;
    }

    public void onLocationChanged(Location location) {
        HwLog.i("RcsGoogleLocationMgr", " onLocationChanged ");
        if (this.mGoogleApiClient == null || !this.mGoogleApiClient.isConnected()) {
            HwLog.e("RcsGoogleLocationMgr", "googleApiClient null or not connected");
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, (LocationListener) this);
        if (RcsContactsUtils.isSettingsLocationOpen(this.mActivity)) {
            this.mLatitude = location.getLatitude();
            this.mLogitude = location.getLongitude();
            this.mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            this.mGoogleMap.clear();
            this.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 14.0f));
            this.mGoogleMap.addMarker(new MarkerOptions().position(this.mLatLng));
            HwLog.i("RcsGoogleLocationMgr", " onLocationChanged  getLocation");
            getPrecallAddressText(this.mLatitude, this.mLogitude);
        }
    }

    public void getPrecallAddressText(double lat, double lon) {
        final double d = lat;
        final double d2 = lon;
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                RcsGoogleLocationMgr.this.getPrecallAddressList(d, d2);
            }
        });
    }

    private void getPrecallAddressList(double lat, double lon) {
        String errorMsg = "";
        Object addressString = null;
        for (int i = 0; i < 3; i++) {
            try {
                List<Address> addresses = this.mGeocoder.getFromLocation(lat, lon, MAX_RETURN_VALUE);
                if (!(addresses == null || addresses.isEmpty() || this.mPrecallListener == null)) {
                    Address address = (Address) addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList();
                    int address_index = address.getMaxAddressLineIndex();
                    for (int j = 0; j < address_index; j++) {
                        String addressLine = address.getAddressLine(j);
                        if (!TextUtils.isEmpty(addressLine)) {
                            addressFragments.add(addressLine);
                        }
                    }
                    if (addressFragments.isEmpty() && !TextUtils.isEmpty(address.getFeatureName())) {
                        addressFragments.add(address.getFeatureName());
                    }
                    if (!TextUtils.isEmpty(address.getCountryName())) {
                        addressFragments.add(address.getCountryName());
                    }
                    if (addressFragments.isEmpty()) {
                        addressString = "";
                    } else {
                        addressString = TextUtils.join(", ", addressFragments);
                    }
                    HwLog.i("RcsGoogleLocationMgr", "precall address found");
                    break;
                }
            } catch (IOException ioException) {
                HwLog.e("RcsGoogleLocationMgr", "Catch network or other I/O problems." + " ioException " + ioException.getMessage());
            } catch (IllegalArgumentException illegalArgumentException) {
                HwLog.e("RcsGoogleLocationMgr", "Catch invalid latitude or longitude values." + " ., illegalArgumentException" + illegalArgumentException.getMessage());
            }
        }
        if (this.mGoogleApiClient != null && this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.disconnect();
        }
        if (this.mPrecallListener != null) {
            if (!RcsContactsUtils.isSettingsLocationOpen(this.mActivity) || TextUtils.isEmpty(addressString)) {
                this.mPrecallListener.onLocationLatLngResult(360.0d, 360.0d);
                return;
            }
            this.mPrecallListener.onLocationLatLngResult(this.mLatitude, this.mLogitude);
            this.mPrecallListener.onLocationAddressResult(addressString);
            try {
                Thread.sleep(1000);
                HwLog.i("RcsGoogleLocationMgr", "delay 1000ms to snapshot map");
            } catch (InterruptedException e) {
                HwLog.e("RcsGoogleLocationMgr", "InterruptedException " + e.getMessage());
            }
            this.mGoogleMap.snapshot(this);
        }
    }

    public void onSnapshotReady(Bitmap bitmap) {
        HwLog.d("RcsGoogleLocationMgr", " onSnapshotReady");
        if (this.mPrecallListener != null) {
            this.mPrecallListener.onLocatinonSnapShot(bitmap);
        } else {
            HwLog.e("RcsGoogleLocationMgr", "SnapshotReady but callback listener is null");
        }
    }

    public void locationOnDestroy() {
        if (this.mGoogleApiClient != null && this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.disconnect();
        }
    }

    public void setMapdisplayView(RelativeLayout mapDisplayView, Bundle bundle) {
        FragmentTransaction fraTransaction = this.mActivity.getFragmentManager().beginTransaction();
        fraTransaction.replace(mapDisplayView.getId(), this.mMapFragment);
        fraTransaction.commit();
    }

    public void displayMap(double lat, double lon, boolean displayMapDirect) {
        this.mLatLng = new LatLng(lat, lon);
        this.mIsDialayMapDirect = true;
        HwLog.i("RcsGoogleLocationMgr", "display Map directly");
        this.mMapFragment.getMapAsync(this);
    }
}
