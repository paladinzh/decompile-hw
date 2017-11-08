package com.huawei.rcs.utils.map.impl;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.messaging.util.OsUtil;
import com.android.mms.attachment.ui.mediapicker.MapMediaChooser;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
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
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.map.abs.RcsMapFragment;
import com.huawei.rcs.utils.map.abs.RcsMapFragment.AddressData;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RcsGoogleMapFragment extends RcsMapFragment implements OnMapReadyCallback, LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    private View addressView;
    OnCameraChangeListener cameraChangeListener = new OnCameraChangeListener() {
        public void onCameraChange(CameraPosition cameraPosition) {
            RcsGoogleMapFragment.this.showActionbar(false);
            RcsGoogleMapFragment.this.mLatLng = cameraPosition.target;
            if (RcsGoogleMapFragment.this.isNetworkAvailable()) {
                RcsGoogleMapFragment.this.showLoadingView(false);
                RcsGoogleMapFragment.this.updateWithNewLocation(RcsGoogleMapFragment.this.mLatLng);
                return;
            }
            RcsGoogleMapFragment.this.showNeterrorView(false);
        }
    };
    private ProgressBar loadingBar;
    OnClickListener loadingFailedClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            RcsGoogleMapFragment.this.mHandler.sendEmptyMessage(10004);
        }
    };
    private ImageView loadingImageView;
    private TextView loadingTextView;
    private View loadingView;
    private AddressData lookAddress;
    private AbstractEmuiActionBar mActionBar;
    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10001:
                    AddressData addressOne = msg.obj;
                    if (addressOne != null) {
                        if (RcsGoogleMapFragment.this.isLatlngSame(new LatLng(addressOne.latitude, addressOne.longitude), RcsGoogleMapFragment.this.mLatLng)) {
                            RcsGoogleMapFragment.this.showAddressView(addressOne.title, addressOne.subTitle);
                            RcsGoogleMapFragment.this.showActionbar(true);
                            RcsGoogleMapFragment.this.lookAddress = addressOne;
                            return;
                        }
                        return;
                    }
                    RcsGoogleMapFragment.this.showLoadingView(true);
                    return;
                case 10002:
                    if (RcsGoogleMapFragment.this.isNetworkAvailable()) {
                        RcsGoogleMapFragment.this.showLoadingView(false);
                        RcsGoogleMapFragment.this.updateWithNewLocation(RcsGoogleMapFragment.this.mLatLng);
                        return;
                    }
                    RcsGoogleMapFragment.this.showNeterrorView(false);
                    return;
                case 10003:
                    final LatLng latLng = msg.obj;
                    new Thread(new Runnable() {
                        public void run() {
                            if (RcsGoogleMapFragment.this.isLatlngSame(latLng, RcsGoogleMapFragment.this.mLatLng)) {
                                AddressData addressOne = RcsGoogleMapFragment.this.getLocationbyAddress(latLng);
                                if (RcsGoogleMapFragment.this.isLatlngSame(latLng, RcsGoogleMapFragment.this.mLatLng)) {
                                    Message msg = new Message();
                                    msg.what = 10001;
                                    msg.obj = addressOne;
                                    RcsGoogleMapFragment.this.mHandler.sendMessage(msg);
                                }
                            }
                        }
                    }).start();
                    return;
                case 10004:
                    if (RcsGoogleMapFragment.this.isNetworkAvailable()) {
                        RcsGoogleMapFragment.this.showLoadingView(false);
                        RcsGoogleMapFragment.this.updateWithNewLocation(RcsGoogleMapFragment.this.mLatLng);
                        RcsGoogleMapFragment.this.loadingView.setOnClickListener(null);
                        return;
                    }
                    RcsGoogleMapFragment.this.showNeterrorView(false);
                    return;
                default:
                    return;
            }
        }
    };
    private LatLng mLatLng;
    private Location mLocation;
    private GoogleMap mMap;
    private View mMyCurrentLocation;
    private View mMyCurrentLocationContainer;
    private MapView mapView;
    private TextView netErrorTextView;
    private View netErrorView;
    private View netSettingView;
    OnClickListener sendClickListener = new OnClickListener() {
        public void onClick(View arg0) {
            RcsGoogleMapFragment.this.okClick();
        }
    };
    private TextView subTextView;
    private TextView titleTextView;
    private String type;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());
        Intent intent = getActivity().getIntent();
        this.type = intent.getStringExtra(NumberInfo.TYPE_KEY);
        if (this.type != null && this.type.equals("look")) {
            Bundle bundle = intent.getExtras();
            this.lookAddress = new AddressData();
            if (bundle != null) {
                this.lookAddress.latitude = Double.valueOf(bundle.getString("latitude")).doubleValue();
                this.lookAddress.longitude = Double.valueOf(bundle.getString("longitude")).doubleValue();
                this.lookAddress.title = bundle.getString("title");
                this.lookAddress.subTitle = bundle.getString("subtitle");
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = inflater.inflate(R.layout.rcs_google_map_fragment, viewGroup, false);
        init(view);
        this.mapView.onCreate(bundle);
        return view;
    }

    public void onMapReady(GoogleMap map) {
        this.mMap = map;
        if (OsUtil.hasLocationPermission()) {
            this.mMap.setMyLocationEnabled(true);
        }
        if (this.type == null || !this.type.equals("look")) {
            addMarker();
            setUpMap();
            if (this.mLatLng == null) {
                this.mLatLng = this.mMap.getCameraPosition().target;
            } else {
                this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 15.0f));
            }
            if (this.mGoogleApiClient == null) {
                this.mGoogleApiClient = new Builder(getActivity()).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            }
            if (!this.mGoogleApiClient.isConnected()) {
                this.mGoogleApiClient.connect();
            }
            return;
        }
        LatLng latlng = new LatLng(this.lookAddress.latitude, this.lookAddress.longitude);
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15.0f));
        addMarker(latlng);
    }

    public void okClick() {
        super.okClick();
        if (this.lookAddress != null) {
            this.mMap.snapshot(new SnapshotReadyCallback() {
                public void onSnapshotReady(Bitmap snapshot) {
                    RcsGoogleMapFragment.this.saveSnapshotPic(snapshot, RcsGoogleMapFragment.this.lookAddress);
                }
            });
        }
    }

    public void updateActionBar(AbstractEmuiActionBar actionBar) {
        super.updateActionBar(actionBar);
        setActionbar(actionBar);
        if (isInLandscape()) {
            showActionbar(false);
            return;
        }
        if (this.addressView.getVisibility() != 0) {
            showActionbar(false);
        } else {
            showActionbar(true);
        }
    }

    public void onLocationChanged(Location location) {
        this.mLocation = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, (LocationListener) this);
        this.mLatLng = changtoLatlng(this.mLocation);
        this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 15.0f), VTMCDataCache.MAXSIZE, null);
    }

    public LatLng changtoLatlng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void onConnectionFailed(ConnectionResult arg0) {
    }

    public void onConnected(Bundle arg0) {
        if (isNeedConnected()) {
            this.mLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
            if (this.mLocation != null) {
                this.mLatLng = changtoLatlng(this.mLocation);
                this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 15.0f));
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, createLocationRequest(), (LocationListener) this);
            }
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(100);
        return mLocationRequest;
    }

    public void onConnectionSuspended(int arg0) {
        this.mGoogleApiClient.connect();
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.mapView.onLowMemory();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mapView.onSaveInstanceState(outState);
    }

    private boolean isNeedConnected() {
        if (this.mMediaPicker == null || !(this.mMediaPicker.getSelectedChooser() instanceof MapMediaChooser)) {
            return true;
        }
        MapMediaChooser mapMediaChooser = (MapMediaChooser) this.mMediaPicker.getSelectedChooser();
        if (mapMediaChooser == null || mapMediaChooser.getLatLng() == null) {
            return true;
        }
        this.mLatLng = mapMediaChooser.getLatLng();
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(this.mLatLng, 15.0f));
        mapMediaChooser.setLatLng(null);
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mMediaPicker != null && (this.mMediaPicker.getSelectedChooser() instanceof MapMediaChooser)) {
            ((MapMediaChooser) this.mMediaPicker.getSelectedChooser()).setLatLng(this.mLatLng);
        }
    }

    public void onResume() {
        super.onResume();
        this.mapView.onResume();
    }

    public void onPause() {
        super.onPause();
        this.mapView.onPause();
    }

    public void onDestroy() {
        if (this.mGoogleApiClient != null && this.mGoogleApiClient.isConnected()) {
            this.mGoogleApiClient.disconnect();
            this.mGoogleApiClient = null;
        }
        super.onDestroy();
        this.mapView.onDestroy();
    }

    private void addMarker() {
        this.mMap.clear();
        ImageView imageView = new ImageView(getActivity());
        Bitmap bitmap = RcsMapFragment.drawableToBitmap(getContext().getDrawable(R.drawable.ic_sms_location_checked));
        imageView.setImageBitmap(bitmap);
        imageView.setPadding(0, 0, 0, bitmap.getHeight() / 2);
        this.mapView.addView(imageView, new LayoutParams(-2, -2, 17));
    }

    private void addMarker(LatLng latLng) {
        this.mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        Bitmap bitmap = RcsMapFragment.drawableToBitmap(getContext().getDrawable(R.drawable.ic_sms_location_checked));
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        this.mMap.addMarker(markerOptions);
    }

    public void showFullView() {
        if (this.mMap != null) {
            this.mMap.getUiSettings().setZoomGesturesEnabled(true);
            this.mMap.getUiSettings().setScrollGesturesEnabled(true);
            this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
            setCurrentLocationButtonEnabled(true);
            this.addressView.setOnClickListener(null);
        }
    }

    public void hintFullView() {
        if (this.mMap != null) {
            this.mMap.getUiSettings().setZoomGesturesEnabled(false);
            this.mMap.getUiSettings().setScrollGesturesEnabled(false);
            this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
            setCurrentLocationButtonEnabled(false);
            this.addressView.setOnClickListener(this.sendClickListener);
        }
    }

    private void showAddressView(String title, String subTitle) {
        if (!(title == null || title.equals(""))) {
            this.titleTextView.setText(title);
        }
        if (!(subTitle == null || subTitle.equals(""))) {
            this.subTextView.setText(subTitle);
        }
        this.loadingView.setVisibility(8);
        this.addressView.setVisibility(0);
        this.netErrorView.setVisibility(8);
    }

    private void showLoadingView(boolean isFailed) {
        this.loadingView.setVisibility(0);
        this.addressView.setVisibility(8);
        this.netErrorView.setVisibility(8);
        if (isFailed) {
            this.loadingBar.setVisibility(4);
            this.loadingImageView.setImageResource(R.drawable.ic_public_fail_loadmap);
            this.loadingTextView.setText(R.string.rcs_loading_location_error);
            this.loadingView.setOnClickListener(this.loadingFailedClickListener);
            return;
        }
        this.loadingBar.setVisibility(0);
        this.loadingImageView.setImageResource(R.drawable.ic_sms_location);
        this.loadingTextView.setText(R.string.rcs_loading_location);
    }

    private void showNeterrorView(boolean isLinking) {
        this.loadingView.setVisibility(8);
        this.addressView.setVisibility(8);
        this.netErrorView.setVisibility(0);
        if (isLinking) {
            this.netErrorTextView.setText(R.string.rcs_netlink_text);
        } else {
            this.netErrorTextView.setText(R.string.rcs_neterror_text);
        }
    }

    private void init(View view) {
        this.mapView = (MapView) view.findViewById(R.id.map);
        this.loadingView = view.findViewById(R.id.loading_view);
        this.addressView = view.findViewById(R.id.address_view);
        this.netErrorView = view.findViewById(R.id.neterror_view);
        this.titleTextView = (TextView) view.findViewById(R.id.text_title);
        this.subTextView = (TextView) view.findViewById(R.id.text_sub);
        this.mMyCurrentLocationContainer = view.findViewById(R.id.my_location_container);
        this.mMyCurrentLocation = view.findViewById(R.id.rcs_my_location);
        this.mMyCurrentLocation.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (RcsGoogleMapFragment.this.mGoogleApiClient != null && RcsGoogleMapFragment.this.mGoogleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(RcsGoogleMapFragment.this.mGoogleApiClient, RcsGoogleMapFragment.this.createLocationRequest(), RcsGoogleMapFragment.this);
                }
            }
        });
        if (this.mapView != null) {
            this.mapView.getMapAsync(this);
        }
        if (this.type == null || !this.type.equals("look")) {
            this.loadingImageView = (ImageView) view.findViewById(R.id.loading_image);
            this.loadingTextView = (TextView) view.findViewById(R.id.loading_text);
            this.loadingBar = (ProgressBar) view.findViewById(R.id.loading_bar);
            this.netErrorTextView = (TextView) view.findViewById(R.id.neterror_text);
            this.netSettingView = view.findViewById(R.id.setting_network);
            this.netSettingView.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    RcsGoogleMapFragment.this.startActivity(new Intent("android.settings.AIRPLANE_MODE_SETTINGS"));
                }
            });
            this.netErrorTextView.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    RcsGoogleMapFragment.this.showNeterrorView(true);
                    RcsGoogleMapFragment.this.mHandler.sendEmptyMessageDelayed(10002, 500);
                }
            });
            if (!isInLandscape() && (this.mMediaPicker == null || !this.mMediaPicker.isFullScreen())) {
                this.addressView.setOnClickListener(this.sendClickListener);
                return;
            } else if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                RcsGroupChatComposeMessageFragment fragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
                if (fragment != null) {
                    setActionbar(fragment.getGroupActionBar());
                    fragment.setMapClickCallback(this);
                    return;
                }
                return;
            } else {
                ComposeMessageFragment fragment2 = (ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
                if (fragment2 != null) {
                    setActionbar(fragment2.getActionbar());
                    fragment2.setMapClickCallback(this);
                    return;
                }
                return;
            }
        }
        this.mActionBar = createEmuiActionBar(view);
        this.mActionBar.setTitle(getString(R.string.attach_map_location));
        this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View arg0) {
                RcsGoogleMapFragment.this.getActivity().onBackPressed();
            }
        });
        Button navibtn = (Button) view.findViewById(R.id.navi);
        navibtn.setVisibility(0);
        navibtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (RcsGoogleMapFragment.this.chekApkExist("com.google.android.apps.maps")) {
                        Intent mapIntent = new Intent("android.intent.action.VIEW", Uri.parse("google.navigation:q=" + RcsGoogleMapFragment.this.lookAddress.latitude + "," + RcsGoogleMapFragment.this.lookAddress.longitude));
                        mapIntent.setPackage("com.google.android.apps.maps");
                        RcsGoogleMapFragment.this.startActivity(mapIntent);
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setAction("com.google.android.apps.maps");
                    intent.setData(Uri.parse("http://www.maps.google.com/maps?f=q&q=" + RcsGoogleMapFragment.this.lookAddress.latitude + "," + RcsGoogleMapFragment.this.lookAddress.longitude));
                    RcsGoogleMapFragment.this.startActivity(intent);
                } catch (RuntimeException e) {
                    MLog.e("RcsGoogleMapFragment", "RuntimeException: start google map failed");
                } catch (Exception e2) {
                    MLog.e("RcsGoogleMapFragment", "start google map failed");
                }
            }
        });
        showAddressView(this.lookAddress.title, this.lookAddress.subTitle);
    }

    private void setUpMap() {
        if (isInLandscape() || (this.mMediaPicker != null && this.mMediaPicker.isFullScreen())) {
            this.mMap.getUiSettings().setAllGesturesEnabled(false);
            this.mMap.getUiSettings().setZoomGesturesEnabled(true);
            this.mMap.getUiSettings().setScrollGesturesEnabled(true);
            this.mMap.setOnCameraChangeListener(this.cameraChangeListener);
            this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
            setCurrentLocationButtonEnabled(true);
            return;
        }
        this.mMap.getUiSettings().setAllGesturesEnabled(false);
        this.mMap.setOnCameraChangeListener(this.cameraChangeListener);
        this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
        setCurrentLocationButtonEnabled(false);
    }

    private void updateWithNewLocation(LatLng latlng) {
        this.mHandler.removeMessages(10003);
        Message message = this.mHandler.obtainMessage(10003);
        message.obj = latlng;
        this.mHandler.sendMessageDelayed(message, 500);
    }

    private AddressData getLocationbyAddress(LatLng latLng) {
        String titleBuf = "";
        StringBuilder subBuf = new StringBuilder();
        if (getActivity() == null) {
            return null;
        }
        try {
            List<Address> addresses = new Geocoder(getActivity(), Locale.getDefault()).getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = (Address) addresses.get(0);
                int maxLine = address.getMaxAddressLineIndex();
                for (int i = 0; i <= maxLine; i++) {
                    subBuf.append(address.getAddressLine(i)).append(" ");
                }
                if (address.getCountryName() != null) {
                    titleBuf = titleBuf + address.getCountryName();
                }
                if (address.getAdminArea() != null) {
                    titleBuf = titleBuf + address.getAdminArea();
                }
            }
            if (subBuf.toString().equals("")) {
                MLog.e("RcsGoogleMapFragment", "getLocationbyAddress subBuf is empty, return null");
                return null;
            }
            AddressData oneAddress = new AddressData();
            oneAddress.latitude = latLng.latitude;
            oneAddress.longitude = latLng.longitude;
            oneAddress.title = titleBuf;
            oneAddress.subTitle = subBuf.toString().substring(0, subBuf.length() - 1);
            return oneAddress;
        } catch (IOException e) {
            MLog.e("RcsGoogleMapFragment", "getLocationbyAddress occur an IOException.");
            return null;
        }
    }

    private boolean isLatlngSame(LatLng l1, LatLng l2) {
        if (Double.compare(l1.latitude, l2.latitude) == 0 && Double.compare(l1.longitude, l2.longitude) == 0) {
            return true;
        }
        return false;
    }

    private void setCurrentLocationButtonEnabled(boolean isEnabled) {
        if (this.mMyCurrentLocationContainer != null) {
            if (isEnabled) {
                this.mMyCurrentLocationContainer.setVisibility(0);
            } else {
                this.mMyCurrentLocationContainer.setVisibility(8);
            }
        }
    }
}
