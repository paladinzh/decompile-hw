package com.huawei.rcs.utils.map.impl;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.android.mms.attachment.ui.mediapicker.MapMediaChooser;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.map.abs.RcsMapFragment;
import com.huawei.rcs.utils.map.abs.RcsMapFragment.AddressData;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RcsGaodeLocationFragment extends RcsMapFragment implements OnClickListener, LocationSource, AMapLocationListener {
    public static final String UNKNOWN_ADDRESS_NAME = new String(new byte[]{(byte) -26, (byte) -100, (byte) -86, (byte) -25, (byte) -97, (byte) -91, (byte) -28, (byte) -67, (byte) -115, (byte) -25, (byte) -67, (byte) -82}, Charset.forName("utf-8"));
    OnCameraChangeListener cameraChangeListener = new OnCameraChangeListener() {
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            if (RcsGaodeLocationFragment.this.lastLatLng == null || !RcsGaodeLocationFragment.this.isLatlngSame(RcsGaodeLocationFragment.this.lastLatLng, cameraPosition.target)) {
                RcsGaodeLocationFragment.this.lastLatLng = cameraPosition.target;
                if (RcsGaodeLocationFragment.this.isCickMove) {
                    RcsGaodeLocationFragment.this.showActionbar(true);
                    RcsGaodeLocationFragment.this.isCickMove = false;
                    return;
                }
                if (RcsGaodeLocationFragment.this.isLandOrFullScreen()) {
                    if (RcsGaodeLocationFragment.this.isNetworkAvailable()) {
                        if (!RcsGaodeLocationFragment.this.isSearchRet) {
                            RcsGaodeLocationFragment.this.showLoadingView(false);
                        }
                        RcsGaodeLocationFragment.this.showActionbar(false);
                        RcsGaodeLocationFragment.this.mListView.setVisibility(8);
                        RcsGaodeLocationFragment.this.mListBar.setVisibility(0);
                    } else {
                        RcsGaodeLocationFragment.this.showNeterrorView(false);
                        RcsGaodeLocationFragment.this.showActionbar(false);
                        RcsGaodeLocationFragment.this.mListView.setVisibility(8);
                        RcsGaodeLocationFragment.this.isSearchRet = false;
                        return;
                    }
                } else if (RcsGaodeLocationFragment.this.isNetworkAvailable()) {
                    RcsGaodeLocationFragment.this.showLoadingView(false);
                } else {
                    RcsGaodeLocationFragment.this.showNeterrorView(false);
                    return;
                }
                RcsGaodeLocationFragment.this.mGeocodeSearch.getFromLocationAsyn(new RegeocodeQuery(new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude), 1000.0f, GeocodeSearch.AMAP));
            }
        }

        public void onCameraChange(CameraPosition cameraPosition) {
            RcsGaodeLocationFragment.this.showActionbar(false);
        }
    };
    OnGeocodeSearchListener geocodeSearchListener = new OnGeocodeSearchListener() {
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            if (i == 1000) {
                RcsGaodeLocationFragment.this.mCity = regeocodeResult.getRegeocodeAddress().getCityCode();
                RcsGaodeLocationFragment.this.mAddressList.clear();
                String addressTitle = regeocodeResult.getRegeocodeAddress().getProvince() + regeocodeResult.getRegeocodeAddress().getCity() + regeocodeResult.getRegeocodeAddress().getDistrict();
                AddressData item1 = new AddressData();
                item1.subTitle = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                item1.title = addressTitle;
                item1.latitude = regeocodeResult.getRegeocodeQuery().getPoint().getLatitude();
                item1.longitude = regeocodeResult.getRegeocodeQuery().getPoint().getLongitude();
                RcsGaodeLocationFragment.this.mLookAddress = item1;
                List<PoiItem> poiList = regeocodeResult.getRegeocodeAddress().getPois();
                for (int n = 0; n < poiList.size(); n++) {
                    AddressData item = new AddressData();
                    item.title = ((PoiItem) poiList.get(n)).getTitle();
                    item.subTitle = ((PoiItem) poiList.get(n)).getSnippet();
                    item.latitude = ((PoiItem) poiList.get(n)).getLatLonPoint().getLatitude();
                    item.longitude = ((PoiItem) poiList.get(n)).getLatLonPoint().getLongitude();
                    RcsGaodeLocationFragment.this.mAddressList.add(item);
                }
                if (RcsGaodeLocationFragment.this.isSearchRet) {
                    RcsGaodeLocationFragment.this.mLookAddress = RcsGaodeLocationFragment.this.mSendAddress;
                    RcsGaodeLocationFragment.this.isSearchRet = false;
                } else {
                    RcsGaodeLocationFragment.this.mSendAddress = RcsGaodeLocationFragment.this.mLookAddress;
                }
                RcsGaodeLocationFragment.this.showAddressView(RcsGaodeLocationFragment.this.mLookAddress);
                if (RcsGaodeLocationFragment.this.isLandOrFullScreen()) {
                    int i2;
                    if (RcsGaodeLocationFragment.this.isInLandscape()) {
                        RcsGaodeLocationFragment rcsGaodeLocationFragment = RcsGaodeLocationFragment.this;
                        if (RcsGaodeLocationFragment.this.mAddressList.size() > 1) {
                            i2 = 1;
                        } else {
                            i2 = RcsGaodeLocationFragment.this.mAddressList.size();
                        }
                        rcsGaodeLocationFragment.setListViewHeightBasedOnChildren(i2, true);
                    } else {
                        RcsGaodeLocationFragment rcsGaodeLocationFragment2 = RcsGaodeLocationFragment.this;
                        if (RcsGaodeLocationFragment.this.mAddressList.size() > 3) {
                            i2 = 3;
                        } else {
                            i2 = RcsGaodeLocationFragment.this.mAddressList.size();
                        }
                        rcsGaodeLocationFragment2.setListViewHeightBasedOnChildren(i2, true);
                    }
                    if (TextUtils.isEmpty(addressTitle)) {
                        RcsGaodeLocationFragment.this.showActionbar(false);
                    } else {
                        RcsGaodeLocationFragment.this.showActionbar(true);
                    }
                    RcsGaodeLocationFragment.this.mAdapter.setItemCount(RcsGaodeLocationFragment.this.mAddressList.size());
                    RcsGaodeLocationFragment.this.mListBar.setVisibility(8);
                    RcsGaodeLocationFragment.this.mListView.setVisibility(0);
                    RcsGaodeLocationFragment.this.mAdapter.clickposition = -1;
                    RcsGaodeLocationFragment.this.mAdapter.notifyDataSetChanged();
                }
            } else if (RcsGaodeLocationFragment.this.isLandOrFullScreen()) {
                RcsGaodeLocationFragment.this.showActionbar(false);
                RcsGaodeLocationFragment.this.showLoadingView(true);
                RcsGaodeLocationFragment.this.mListBar.setVisibility(8);
                RcsGaodeLocationFragment.this.mListView.setVisibility(4);
                RcsGaodeLocationFragment.this.isSearchRet = false;
            } else {
                RcsGaodeLocationFragment.this.showLoadingView(true);
            }
        }

        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        }
    };
    private boolean isCickMove = false;
    private boolean isSearchRet = false;
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (RcsGaodeLocationFragment.this.mAddressView.getVisibility() == 0) {
                RcsGaodeLocationFragment.this.mAddressImage.setImageResource(R.drawable.ic_sms_location);
                RcsGaodeLocationFragment.this.mSubTextView.setTextColor(RcsGaodeLocationFragment.this.getResources().getColor(R.color.attachment_tab_button_unselected));
            }
            RcsGaodeLocationFragment.this.mAdapter.clickposition = position;
            RcsGaodeLocationFragment.this.mAdapter.notifyDataSetChanged();
            RcsGaodeLocationFragment.this.mSendAddress = (AddressData) RcsGaodeLocationFragment.this.mAddressList.get(position);
            RcsGaodeLocationFragment.this.isCickMove = true;
            RcsGaodeLocationFragment.this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(RcsGaodeLocationFragment.this.mSendAddress.latitude, RcsGaodeLocationFragment.this.mSendAddress.longitude), 15.0f), 500, null);
        }
    };
    private LatLng lastLatLng;
    private AbstractEmuiActionBar mActionBar;
    private RcsMapAdapter mAdapter;
    private ImageView mAddressImage;
    private ArrayList<AddressData> mAddressList = new ArrayList();
    private View mAddressView;
    private String mCity;
    private ImageButton mCurrentLocation;
    private View mDisplayView;
    private GeocodeSearch mGeocodeSearch;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10001:
                case 10002:
                    if (RcsGaodeLocationFragment.this.isNetworkAvailable()) {
                        RcsGaodeLocationFragment.this.showLoadingView(false);
                        if (RcsGaodeLocationFragment.this.isLandOrFullScreen()) {
                            RcsGaodeLocationFragment.this.mListBar.setVisibility(0);
                        }
                        RcsGaodeLocationFragment.this.mLoadingView.setOnClickListener(null);
                        if (RcsGaodeLocationFragment.this.lastLatLng == null && RcsGaodeLocationFragment.this.mMap.getCameraPosition() != null) {
                            RcsGaodeLocationFragment.this.lastLatLng = RcsGaodeLocationFragment.this.mMap.getCameraPosition().target;
                        }
                        if (RcsGaodeLocationFragment.this.lastLatLng != null) {
                            RcsGaodeLocationFragment.this.mGeocodeSearch.getFromLocationAsyn(new RegeocodeQuery(new LatLonPoint(RcsGaodeLocationFragment.this.lastLatLng.latitude, RcsGaodeLocationFragment.this.lastLatLng.longitude), 1000.0f, GeocodeSearch.AMAP));
                            return;
                        }
                        Log.e("RcsGaodeleMapFragment", "handleMessage lastLatLng is null ");
                        return;
                    }
                    RcsGaodeLocationFragment.this.showNeterrorView(false);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsFullScreen = false;
    private ProgressBar mListBar;
    private RcsMapListView mListView;
    private ProgressBar mLoadingBar;
    private ImageView mLoadingImageView;
    private TextView mLoadingTextView;
    private View mLoadingView;
    private AMapLocationClientOption mLocationOption;
    private AddressData mLookAddress;
    private AMap mMap;
    private TextureMapView mMapView;
    private View mMyCurrentLocationContainer;
    private View mNearbyItem;
    private TextView mNetErrorTextView;
    private View mNetErrorView;
    private View mNetSettingView;
    private OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            if (!RcsGaodeLocationFragment.this.mIsFullScreen) {
                RcsGaodeLocationFragment.this.setRequestHeight(RcsGaodeLocationFragment.this.mDisplayView.getHeight());
            }
            RcsGaodeLocationFragment.this.mTitleTextView.getViewTreeObserver().removeOnGlobalLayoutListener(RcsGaodeLocationFragment.this.mOnGlobalLayoutListener);
        }
    };
    private ScrollView mRequestView;
    private SearchView mSearchView;
    private AddressData mSendAddress;
    private TextView mSubTextView;
    private TextView mTitleTextView;
    private String mType;
    OnMapLoadedListener mapLoadedListener = new OnMapLoadedListener() {
        public void onMapLoaded() {
            if (RcsGaodeLocationFragment.this.mType == null || !RcsGaodeLocationFragment.this.mType.equals("look")) {
                RcsGaodeLocationFragment.this.addMarker();
                if (RcsGaodeLocationFragment.this.isLandOrFullScreen()) {
                    RcsGaodeLocationFragment.this.setRquestStartHeight(2);
                    return;
                }
                return;
            }
            RcsGaodeLocationFragment.this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(RcsGaodeLocationFragment.this.lastLatLng, 15.0f));
            RcsGaodeLocationFragment.this.addMarker(RcsGaodeLocationFragment.this.lastLatLng);
        }
    };
    private AMapLocationClient mlocationClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        this.mType = intent.getStringExtra(NumberInfo.TYPE_KEY);
        if (this.mType != null && this.mType.equals("look")) {
            Bundle bundle = intent.getExtras();
            this.mLookAddress = new AddressData();
            if (bundle != null) {
                this.mLookAddress.latitude = Double.valueOf(bundle.getString("latitude")).doubleValue();
                this.mLookAddress.longitude = Double.valueOf(bundle.getString("longitude")).doubleValue();
                this.mLookAddress.title = bundle.getString("title");
                this.mLookAddress.subTitle = bundle.getString("subtitle");
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = inflater.inflate(R.layout.rcs_map_fragment, viewGroup, false);
        init(view);
        this.mMapView.onCreate(bundle);
        return view;
    }

    public void onResume() {
        super.onResume();
        this.mMapView.onResume();
        if (this.mMap != null) {
            this.mMap.runOnDrawFrame();
        }
    }

    public void onPause() {
        super.onPause();
        this.mMapView.onPause();
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.mMapView.onLowMemory();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mMapView.onSaveInstanceState(outState);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMapView.onDestroy();
        deactivate();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mMediaPicker != null && (this.mMediaPicker.getSelectedChooser() instanceof MapMediaChooser)) {
            ((MapMediaChooser) this.mMediaPicker.getSelectedChooser()).setAmapAddressData(this.mLookAddress);
        }
    }

    private boolean isChangeScreendMove() {
        if (this.mMediaPicker == null || !(this.mMediaPicker.getSelectedChooser() instanceof MapMediaChooser)) {
            return false;
        }
        MapMediaChooser mapMediaChooser = (MapMediaChooser) this.mMediaPicker.getSelectedChooser();
        if (mapMediaChooser == null || mapMediaChooser.getAmapAddressData() == null) {
            return false;
        }
        AddressData addressData = mapMediaChooser.getAmapAddressData();
        LatLng addressDataLatLng = new LatLng(addressData.latitude, addressData.longitude);
        this.isSearchRet = true;
        this.mSendAddress = addressData;
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(addressDataLatLng, 15.0f));
        mapMediaChooser.setAmapAddressData(null);
        return true;
    }

    private void addMarker() {
        this.mMap.clear();
        ImageView imageView = new ImageView(getActivity());
        Bitmap bitmap = RcsMapFragment.drawableToBitmap(getContext().getDrawable(R.drawable.ic_sms_location_checked));
        imageView.setImageBitmap(bitmap);
        imageView.setPadding(0, 0, 0, bitmap.getHeight() / 2);
        this.mMapView.addView(imageView, new LayoutParams(-2, -2, 17));
    }

    private void addMarker(LatLng latLng) {
        this.mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        Bitmap bitmap = RcsMapFragment.drawableToBitmap(getContext().getDrawable(R.drawable.ic_sms_location_checked));
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        this.mMap.addMarker(markerOptions);
    }

    public void updateActionBar(AbstractEmuiActionBar actionBar) {
        super.updateActionBar(actionBar);
        setActionbar(actionBar);
        showActionbar(false);
    }

    private void showAddressView(AddressData addData) {
        if (addData != null) {
            if (TextUtils.isEmpty(addData.title)) {
                this.mTitleTextView.setText(UNKNOWN_ADDRESS_NAME);
                this.mSubTextView.setText(UNKNOWN_ADDRESS_NAME);
            } else {
                this.mTitleTextView.setText(addData.title);
                this.mSubTextView.setText(addData.subTitle);
            }
        }
        this.mAddressImage.setImageResource(R.drawable.ic_sms_location_checked);
        this.mSubTextView.setTextColor(getResources().getColor(R.color.attachment_tab_button_selected));
        this.mLoadingView.setVisibility(8);
        this.mAddressView.setVisibility(0);
        this.mNetErrorView.setVisibility(8);
        this.mTitleTextView.getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
    }

    private void showLoadingView(boolean isFailed) {
        this.mLoadingView.setVisibility(0);
        this.mAddressView.setVisibility(8);
        this.mNetErrorView.setVisibility(8);
        if (isFailed) {
            this.mLoadingBar.setVisibility(4);
            this.mLoadingImageView.setImageResource(R.drawable.ic_public_fail_loadmap);
            this.mLoadingTextView.setText(R.string.rcs_loading_location_error);
            this.mLoadingView.setOnClickListener(this);
            return;
        }
        this.mLoadingView.setOnClickListener(null);
        this.mLoadingBar.setVisibility(0);
        this.mLoadingImageView.setImageResource(R.drawable.ic_sms_location);
        this.mLoadingTextView.setText(R.string.rcs_loading_location);
    }

    private void showNeterrorView(boolean isLinking) {
        this.mLoadingView.setVisibility(8);
        this.mAddressView.setVisibility(8);
        this.mNetErrorView.setVisibility(0);
        if (isLinking) {
            this.mNetErrorTextView.setText(R.string.rcs_netlink_text);
        } else {
            this.mNetErrorTextView.setText(R.string.rcs_neterror_text);
        }
    }

    private boolean isLandOrFullScreen() {
        if (isInLandscape()) {
            return true;
        }
        if (this.mMediaPicker == null || !(this.mMediaPicker.getSelectedChooser() instanceof MapMediaChooser)) {
            return false;
        }
        return this.mMediaPicker.isFullScreen();
    }

    private void init(View view) {
        this.mMapView = (TextureMapView) view.findViewById(R.id.map);
        this.mMap = this.mMapView.getMap();
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            this.mType = intent.getStringExtra(NumberInfo.TYPE_KEY);
        }
        if (this.mType == null || !this.mType.equals("look")) {
            this.mRequestView = (ScrollView) view.findViewById(R.id.request_view);
            this.mRequestView.setVisibility(0);
            this.mDisplayView = view.findViewById(R.id.display_view);
            this.mNearbyItem = view.findViewById(R.id.nearby_item);
            this.mListBar = (ProgressBar) view.findViewById(R.id.nearby_bar);
            this.mLoadingView = view.findViewById(R.id.loading_view);
            this.mAddressView = view.findViewById(R.id.address_view);
            this.mAddressImage = (ImageView) view.findViewById(R.id.address_image);
            this.mNetErrorView = view.findViewById(R.id.neterror_view);
            this.mTitleTextView = (TextView) view.findViewById(R.id.text_title);
            this.mSubTextView = (TextView) view.findViewById(R.id.text_sub);
            this.mLoadingImageView = (ImageView) view.findViewById(R.id.loading_image);
            this.mLoadingTextView = (TextView) view.findViewById(R.id.loading_text);
            this.mLoadingBar = (ProgressBar) view.findViewById(R.id.loading_bar);
            this.mNetErrorTextView = (TextView) view.findViewById(R.id.neterror_text);
            this.mNetSettingView = view.findViewById(R.id.setting_network);
            this.mNetSettingView.setOnClickListener(this);
            this.mNetErrorTextView.setOnClickListener(this);
            this.mAddressView.setOnClickListener(this);
            this.mSearchView = (SearchView) view.findViewById(R.id.searchview);
            this.mSearchView.setSubmitButtonEnabled(true);
            this.mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Intent intent = new Intent();
                        intent.putExtra("city", RcsGaodeLocationFragment.this.mCity);
                        intent.setClass(RcsGaodeLocationFragment.this.getActivity(), RcsMapSearchActivity.class);
                        RcsGaodeLocationFragment.this.startActivityForResult(intent, 0);
                    }
                }
            });
            this.mListView = (RcsMapListView) view.findViewById(R.id.nearby_list);
            this.mAdapter = new RcsMapAdapter(getContext(), this.mAddressList);
            this.mListView.setAdapter(this.mAdapter);
            this.mListView.setOnItemClickListener(this.itemClickListener);
            if (isNetworkAvailable()) {
                showLoadingView(false);
            } else {
                showNeterrorView(false);
            }
            if (isLandOrFullScreen()) {
                this.mNearbyItem.setVisibility(0);
                if (isNetworkAvailable()) {
                    this.mListBar.setVisibility(0);
                }
                if (getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                    RcsGroupChatComposeMessageFragment fragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(getActivity(), "Mms_UI_GCCMF");
                    if (fragment != null) {
                        setActionbar(fragment.getGroupActionBar());
                        fragment.setMapClickCallback(this);
                    }
                } else {
                    ComposeMessageFragment fragment2 = (ComposeMessageFragment) FragmentTag.getFragmentByTag(getActivity(), "Mms_UI_CMF");
                    if (fragment2 != null) {
                        setActionbar(fragment2.getActionbar());
                        fragment2.setMapClickCallback(this);
                    }
                }
            }
            setUpMap(view);
            return;
        }
        this.mActionBar = createEmuiActionBar(view);
        this.mActionBar.setTitle(getString(R.string.attach_map_location));
        this.mActionBar.setStartIcon(true, R.drawable.ic_public_back, (OnClickListener) new OnClickListener() {
            public void onClick(View arg0) {
                RcsGaodeLocationFragment.this.getActivity().onBackPressed();
            }
        });
        this.mMap.setOnMapLoadedListener(this.mapLoadedListener);
        this.mMap.getUiSettings().setZoomControlsEnabled(false);
        view.findViewById(R.id.receive_view).setVisibility(0);
        TextView titleView = (TextView) view.findViewById(R.id.receive_title);
        TextView subView = (TextView) view.findViewById(R.id.receive_sub);
        ((Button) view.findViewById(R.id.receive_navi)).setOnClickListener(this);
        if (intent != null && intent.getExtras() != null) {
            Bundle b = intent.getExtras();
            this.lastLatLng = new LatLng(Double.valueOf(b.getString("latitude")).doubleValue(), Double.valueOf(b.getString("longitude")).doubleValue());
            titleView.setText(b.getString("title"));
            subView.setText(b.getString("subtitle"));
        }
    }

    public void setRquestStartHeight(int NumHeaders) {
        int headerHeight = this.mDisplayView.getHeight();
        this.mNearbyItem.measure(0, 0);
        setRequestHeight((headerHeight * NumHeaders) + this.mNearbyItem.getMeasuredHeight());
    }

    public void setRequestHeight(int height) {
        this.mRequestView.getLayoutParams().height = height;
        this.mRequestView.requestLayout();
    }

    public void showFullView() {
        this.mIsFullScreen = true;
        this.mMap.getUiSettings().setZoomGesturesEnabled(true);
        this.mMap.getUiSettings().setScrollGesturesEnabled(true);
        this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
        setMyLocationButtonEnabled(true);
        this.mSearchView.setVisibility(0);
        this.mNearbyItem.setVisibility(0);
        setRquestStartHeight(2);
        if (this.lastLatLng == null) {
            return;
        }
        if (isNetworkAvailable()) {
            this.mListBar.setVisibility(0);
            this.mGeocodeSearch.getFromLocationAsyn(new RegeocodeQuery(new LatLonPoint(this.lastLatLng.latitude, this.lastLatLng.longitude), 1000.0f, GeocodeSearch.AMAP));
            return;
        }
        showNeterrorView(false);
        this.mListBar.setVisibility(8);
    }

    public void hintFullView() {
        this.mIsFullScreen = false;
        this.mMap.getUiSettings().setZoomGesturesEnabled(false);
        this.mMap.getUiSettings().setScrollGesturesEnabled(false);
        this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
        setMyLocationButtonEnabled(false);
        this.mSearchView.setVisibility(8);
        this.mNearbyItem.setVisibility(8);
        this.mListView.setVisibility(8);
        this.mListBar.setVisibility(8);
        setRequestHeight(this.mDisplayView.getHeight());
        if (this.mAdapter.clickposition != -1) {
            showAddressView(this.mSendAddress);
        }
    }

    private void setUpMap(View view) {
        this.mGeocodeSearch = new GeocodeSearch(getActivity());
        this.mGeocodeSearch.setOnGeocodeSearchListener(this.geocodeSearchListener);
        this.mMap.setOnCameraChangeListener(this.cameraChangeListener);
        this.mMap.setOnMapLoadedListener(this.mapLoadedListener);
        this.mMap.setLocationSource(this);
        this.mMap.setMyLocationEnabled(true);
        addMyLocationButton(view);
        if (isLandOrFullScreen()) {
            this.mMap.getUiSettings().setAllGesturesEnabled(false);
            this.mMap.getUiSettings().setZoomGesturesEnabled(true);
            this.mMap.getUiSettings().setScrollGesturesEnabled(true);
            this.mMap.setOnCameraChangeListener(this.cameraChangeListener);
            setMyLocationButtonEnabled(true);
            this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
            this.mMap.getUiSettings().setZoomControlsEnabled(false);
            return;
        }
        this.mMap.getUiSettings().setAllGesturesEnabled(false);
        this.mMap.setOnCameraChangeListener(this.cameraChangeListener);
        setMyLocationButtonEnabled(false);
        this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
        this.mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            String latitude = data.getStringExtra("latitude");
            String longitude = data.getStringExtra("longitude");
            String title = data.getStringExtra("title");
            String subTitle = data.getStringExtra("subtitle");
            LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            AddressData addressData = new AddressData();
            addressData.latitude = Double.valueOf(latitude).doubleValue();
            addressData.longitude = Double.valueOf(longitude).doubleValue();
            addressData.title = title;
            addressData.subTitle = subTitle;
            showAddressView(addressData);
            this.mSendAddress = addressData;
            this.mLookAddress = addressData;
            this.isSearchRet = true;
            this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        }
    }

    public void setListViewHeightBasedOnChildren(int itemNums, boolean ismove) {
        ListAdapter listAdapter = this.mListView.getAdapter();
        if (listAdapter != null) {
            int totalHeight = 0;
            for (int i = 0; i < itemNums; i++) {
                View listItem = listAdapter.getView(i, null, this.mListView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            int listHeight = totalHeight + (this.mListView.getDividerHeight() * (itemNums - 1));
            int headerHeight = this.mDisplayView.getHeight();
            this.mNearbyItem.measure(0, 0);
            final int endValue = (listHeight + headerHeight) + this.mNearbyItem.getMeasuredHeight();
            if (ismove) {
                final int startValue = this.mRequestView.getLayoutParams().height;
                if (endValue != startValue) {
                    ValueAnimator mAnimator = ValueAnimator.ofInt(new int[]{0, 100});
                    mAnimator.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (animation.getAnimatedValue() == null) {
                                RcsGaodeLocationFragment.this.setRequestHeight(endValue);
                                return;
                            }
                            float fraction = ((float) ((Integer) animation.getAnimatedValue()).intValue()) / 100.0f;
                            IntEvaluator mEvaluator = new IntEvaluator();
                            if (RcsGaodeLocationFragment.this.mMediaPicker == null || RcsGaodeLocationFragment.this.mMediaPicker.isFullScreen()) {
                                RcsGaodeLocationFragment.this.mRequestView.getLayoutParams().height = mEvaluator.evaluate(fraction, Integer.valueOf(startValue), Integer.valueOf(endValue)).intValue();
                                RcsGaodeLocationFragment.this.mRequestView.requestLayout();
                                return;
                            }
                            animation.cancel();
                        }
                    });
                    mAnimator.setDuration(500);
                    mAnimator.setTarget(this);
                    mAnimator.start();
                } else {
                    return;
                }
            }
            setRequestHeight(endValue);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rcs_my_location:
                if (this.mlocationClient == null) {
                    this.mlocationClient = new AMapLocationClient(getActivity());
                    this.mLocationOption = new AMapLocationClientOption();
                    this.mlocationClient.setLocationListener(this);
                    this.mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
                    this.mLocationOption.setNeedAddress(true);
                    this.mLocationOption.setInterval(1000);
                    this.mLocationOption.setGpsFirst(true);
                    this.mlocationClient.setLocationOption(this.mLocationOption);
                    this.mlocationClient.startLocation();
                    return;
                }
                return;
            case R.id.loading_view:
                this.mHandler.sendEmptyMessage(10001);
                return;
            case R.id.setting_network:
                startActivity(new Intent("android.settings.AIRPLANE_MODE_SETTINGS"));
                return;
            case R.id.neterror_text:
                showNeterrorView(true);
                this.mHandler.sendEmptyMessageDelayed(10002, 500);
                return;
            case R.id.address_view:
                if (!isLandOrFullScreen()) {
                    this.mSendAddress = this.mLookAddress;
                    okClick();
                    return;
                } else if (this.mAddressView.getVisibility() == 0 && this.mLookAddress != null) {
                    this.mAddressImage.setImageResource(R.drawable.ic_sms_location_checked);
                    this.mSubTextView.setTextColor(getResources().getColor(R.color.attachment_tab_button_selected));
                    this.mAdapter.clickposition = -1;
                    this.mAdapter.notifyDataSetChanged();
                    this.isCickMove = true;
                    this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(this.mLookAddress.latitude, this.mLookAddress.longitude), 15.0f), 500, null);
                    return;
                } else {
                    return;
                }
            case R.id.receive_navi:
                NaviPara para = new NaviPara();
                para.setTargetPoint(this.lastLatLng);
                try {
                    if (chekApkExist("com.autonavi.minimap")) {
                        AMapUtils.openAMapNavi(para, getActivity());
                        return;
                    }
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://m.amap.com/?to=" + this.lastLatLng.latitude + "," + this.lastLatLng.longitude)));
                    return;
                } catch (AMapException e) {
                    Log.e("RcsGaodeleMapFragment", "onClick error");
                    return;
                }
            default:
                return;
        }
    }

    public void okClick() {
        super.okClick();
        if (this.mSendAddress != null) {
            addMarker(new LatLng(this.mSendAddress.latitude, this.mSendAddress.longitude));
            this.mMap.getMapScreenShot(new OnMapScreenShotListener() {
                public void onMapScreenShot(Bitmap arg0, int arg1) {
                }

                public void onMapScreenShot(Bitmap bitmap) {
                    RcsGaodeLocationFragment.this.saveSnapshotPic(bitmap, RcsGaodeLocationFragment.this.mSendAddress);
                }
            });
        }
    }

    private boolean isLatlngSame(LatLng l1, LatLng l2) {
        if (Double.compare(l1.latitude, l2.latitude) == 0 && Double.compare(l1.longitude, l2.longitude) == 0) {
            return true;
        }
        return false;
    }

    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation == null) {
            return;
        }
        if (amapLocation.getErrorCode() == 0) {
            double latitude = amapLocation.getLatitude();
            double longitude = amapLocation.getLongitude();
            this.mCity = amapLocation.getCity();
            this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f), 500, null);
            deactivate();
            return;
        }
        Log.e("RcsGaodeleMapFragment", "onLocationChanged error");
    }

    public void activate(OnLocationChangedListener listener) {
        if (!isChangeScreendMove() && this.mlocationClient == null) {
            this.mlocationClient = new AMapLocationClient(getActivity());
            this.mLocationOption = new AMapLocationClientOption();
            this.mlocationClient.setLocationListener(this);
            this.mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            this.mLocationOption.setNeedAddress(true);
            this.mLocationOption.setInterval(1000);
            this.mLocationOption.setGpsFirst(true);
            this.mlocationClient.setLocationOption(this.mLocationOption);
            this.mlocationClient.startLocation();
        }
    }

    public void deactivate() {
        if (this.mlocationClient != null) {
            this.mlocationClient.stopLocation();
            this.mlocationClient.onDestroy();
        }
        this.mlocationClient = null;
    }

    private void addMyLocationButton(View view) {
        this.mMyCurrentLocationContainer = view.findViewById(R.id.my_location_container);
        this.mCurrentLocation = (ImageButton) view.findViewById(R.id.rcs_my_location);
        this.mCurrentLocation.setOnClickListener(this);
    }

    private void setMyLocationButtonEnabled(boolean enabled) {
        if (this.mMyCurrentLocationContainer != null) {
            if (enabled) {
                this.mMyCurrentLocationContainer.setVisibility(0);
            } else {
                this.mMyCurrentLocationContainer.setVisibility(8);
            }
        }
    }
}
