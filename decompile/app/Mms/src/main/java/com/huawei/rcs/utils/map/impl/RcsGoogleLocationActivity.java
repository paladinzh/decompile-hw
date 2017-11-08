package com.huawei.rcs.utils.map.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SuppressLint({"SetJavaScriptEnabled"})
public class RcsGoogleLocationActivity extends Activity implements LocationListener {
    private static String address = "";
    public final int SEARCH_ADDRESS = 10000;
    ImageView btnCancel;
    ImageButton btnSend;
    private Location lastLocation;
    private LocationManager locationMgr;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10000:
                    RcsGoogleLocationActivity.address = (String) msg.obj;
                    if (TextUtils.isEmpty(RcsGoogleLocationActivity.address)) {
                        MLog.e("LocationActivity", "locationAddress is null:");
                        Toast.makeText(RcsGoogleLocationActivity.this, RcsGoogleLocationActivity.this.getString(R.string.rcs_obtain_location_fail), 0).show();
                        RcsGoogleLocationActivity.this.finish();
                        return;
                    }
                    RcsGoogleLocationActivity.this.btnSend.setEnabled(true);
                    RcsGoogleLocationActivity.this.btnSend.setClickable(true);
                    return;
                default:
                    return;
            }
        }
    };
    private String provider;
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.rcs_map_v3);
        setupWebView();
        setRequestedOrientation(1);
    }

    protected void onStart() {
        super.onStart();
        initMap();
    }

    protected void onStop() {
        if (this.locationMgr != null) {
            this.locationMgr.removeUpdates(this);
        }
        super.onStop();
    }

    private void initMap() {
        this.btnSend = (ImageButton) findViewById(R.id.btn_send);
        this.btnSend.setBackground(ResEx.self().getStateListDrawable(this, R.drawable.rcs_ic_send_msg_white));
        this.btnCancel = (ImageView) findViewById(R.id.btn_cancel);
        Intent intent = getIntent();
        String type = intent.getStringExtra(NumberInfo.TYPE_KEY);
        this.btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                RcsGoogleLocationActivity.this.finish();
            }
        });
        if (type == null) {
            return;
        }
        if (type.equals("select")) {
            this.btnSend.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    RcsGoogleLocationActivity.this.gotoChatPage();
                }
            });
            this.btnSend.setVisibility(0);
            this.btnSend.setEnabled(false);
            if (initLocationProvider()) {
                requestLocUpdate();
                return;
            }
            return;
        }
        this.btnSend.setVisibility(8);
        if (this.locationMgr != null) {
            this.locationMgr.removeUpdates(this);
        }
        Bundle b = intent.getExtras();
        if (b != null) {
            String latitude_str = b.getString("latitude");
            String longitude_str = b.getString("longitude");
            final double latitude = Double.valueOf(latitude_str).doubleValue();
            final double longitude = Double.valueOf(longitude_str).doubleValue();
            this.webView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    RcsGoogleLocationActivity.this.webView.loadUrl("javascript:initmap('" + latitude + "','" + longitude + "')");
                }
            });
        }
    }

    private void setupWebView() {
        this.webView = (WebView) findViewById(R.id.webview01);
        this.webView.loadUrl("file:///android_asset/map_v3.html");
    }

    private void requestLocUpdate() {
        if (this.locationMgr.isProviderEnabled(GeocodeSearch.GPS)) {
            this.provider = GeocodeSearch.GPS;
        }
        if (this.locationMgr.isProviderEnabled("network")) {
            this.provider = "network";
        }
        this.lastLocation = this.locationMgr.getLastKnownLocation(this.provider);
        this.locationMgr.requestLocationUpdates(this.provider, 5000, 5.0f, this);
    }

    private void dissmiss() {
        Log.d("LocationActivity", "dissmiss for findbugs");
    }

    private boolean initLocationProvider() {
        this.locationMgr = (LocationManager) getSystemService(NetUtil.REQ_QUERY_LOCATION);
        if (!this.locationMgr.isProviderEnabled("network")) {
            Builder dialog = new Builder(this);
            dialog.setTitle(getResources().getString(R.string.warning_gps));
            dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    RcsGoogleLocationActivity.this.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                }
            });
            dialog.setNegativeButton(getString(R.string.gps_ignore), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    RcsGoogleLocationActivity.this.dissmiss();
                }
            });
            dialog.show();
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(1);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        this.provider = this.locationMgr.getBestProvider(criteria, true);
        if (this.provider != null) {
            return true;
        }
        if (this.locationMgr.isProviderEnabled(GeocodeSearch.GPS)) {
            this.provider = GeocodeSearch.GPS;
            return true;
        } else if (!this.locationMgr.isProviderEnabled("network")) {
            return false;
        } else {
            this.provider = "network";
            return true;
        }
    }

    public void onLocationChanged(Location location) {
        this.lastLocation = location;
        Double longitude = Double.valueOf(this.lastLocation.getLongitude());
        Double latitude = Double.valueOf(this.lastLocation.getLatitude());
        this.locationMgr.removeUpdates(this);
        this.webView.loadUrl("javascript:initmap('" + latitude + "','" + longitude + "')");
        updateWithNewLocation(this.lastLocation);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void gotoChatPage() {
        if (this.lastLocation != null) {
            Intent intent = new Intent();
            intent.putExtra("y", this.lastLocation.getLongitude());
            intent.putExtra("x", this.lastLocation.getLatitude());
            intent.putExtra("city", address);
            intent.putExtra("address", address);
            setResult(-1, intent);
            finish();
            return;
        }
        Log.d("LocationActivity", "get location info error");
    }

    private String getLocationAddress(Location location) {
        String add = "";
        StringBuilder buf = new StringBuilder();
        try {
            List<Address> addresses = new Geocoder(getBaseContext(), Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = (Address) addresses.get(0);
                int maxLine = address.getMaxAddressLineIndex();
                for (int i = 1; i <= maxLine; i++) {
                    buf.append(address.getAddressLine(i));
                }
            }
            return buf.toString();
        } catch (IOException e) {
            add = "";
            e.printStackTrace();
            MLog.e("LocationActivity", "getLocationAddress exception:" + e);
            return add;
        }
    }

    private void updateWithNewLocation(final Location location) {
        if (location != null) {
            new Thread(new Runnable() {
                public void run() {
                    String addressName = "";
                    for (int count = 0; count != 4; count++) {
                        addressName = RcsGoogleLocationActivity.this.getLocationAddress(location);
                        if (!"".equals(addressName)) {
                            break;
                        }
                    }
                    Message msg = new Message();
                    msg.what = 10000;
                    msg.obj = addressName;
                    RcsGoogleLocationActivity.this.mHandler.sendMessage(msg);
                }
            }).start();
        }
    }
}
