package cn.com.xy.sms.sdk.ui.popu.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.XyLoactionManager;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.dialog.GotoMyPositionDialog;
import cn.com.xy.sms.sdk.ui.settings.PermissionRequestActivity;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import com.android.mms.ui.PermissionCheckActivity;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseFragment;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class NearbyPointListFragment extends HwBaseFragment {
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 500;
    private static final double EARTH_RADIUS = 6378.137d;
    private static final String LOCATION_MODE = "location_mode";
    private static final String LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";
    public static final int REQUEST_NEAR_SITE = 1;
    public static final int REQUEST_PER_ENTER_BACK_LOCATION_SERVICE_SETTING = 2;
    public static final int REQUEST_PER_ENTER_FIRST = 0;
    public static final int REQUEST_PER_ENTER_FROM_OTHER = 1;
    public static final String REQUEST_TARGET_KEY = "request_target_key";
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private static final String TAG = "NearbyPointListFragment";
    private boolean isPrepared;
    public RelativeLayout mActionBar;
    private Activity mActivity;
    private String mAddress = null;
    private Location mFirstLoaction = null;
    private boolean mFirstQuery = true;
    private int mGetLoactionCount = 0;
    private int mGetLocationInterval = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!NearbyPointListFragment.this.isHidden() && NearbyPointListFragment.this.isPrepared) {
                switch (msg.what) {
                    case NearbyPoint.QUERY_RESULT_RECEIVE /*4097*/:
                        NearbyPointListFragment.this.analysisResult(msg.getData().getString(NearbyPoint.QUERY_RESULT));
                        if (NearbyPointListFragment.this.mTotal <= (NearbyPointListFragment.this.mPageNum + 1) * 10) {
                            NearbyPointListFragment.this.mloadMoreLinearLayout.setVisibility(8);
                            break;
                        }
                        NearbyPointListFragment.this.mLoadMoreImageView.setVisibility(0);
                        NearbyPointListFragment.this.mLoadMoreTextView.setText(NearbyPointListFragment.this.mActivity.getApplication().getString(R.string.duoqu_tip_load_more));
                        NearbyPointListFragment.this.mloadMoreLinearLayout.setEnabled(true);
                        break;
                    case NearbyPoint.QUERY_PARAM_ERROR /*4098*/:
                    case NearbyPoint.GET_QUERY_URL_FAILURE /*4099*/:
                    case 4100:
                        NearbyPointListFragment.this.setViewVisibility(8, 0, 8, 8);
                        break;
                    case NearbyPoint.DO_GET_LOCATION /*4101*/:
                        NearbyPointListFragment.this.requestLocation();
                        break;
                    case 4102:
                        NearbyPointListFragment.this.mLocationLatitude = msg.getData().getDouble("latitude");
                        NearbyPointListFragment.this.mLocationLongitude = msg.getData().getDouble("longitude");
                        if (NearbyPointListFragment.this.mFirstLoaction != null && NearbyPointListFragment.getDistance(NearbyPointListFragment.this.mFirstLoaction.getLatitude(), NearbyPointListFragment.this.mFirstLoaction.getLongitude(), NearbyPointListFragment.this.mLocationLatitude, NearbyPointListFragment.this.mLocationLongitude) > 100.0d) {
                            NearbyPointListFragment.this.mLoactionChange = true;
                        }
                        if (NearbyPointListFragment.this.mFirstQuery || NearbyPointListFragment.this.mLoactionChange) {
                            NearbyPointListFragment.this.mFirstQuery = false;
                            NearbyPointListFragment.this.doSendMapQueryUrl(NearbyPointListFragment.this.mLocationLongitude, NearbyPointListFragment.this.mLocationLatitude);
                            break;
                        }
                }
            }
        }
    };
    public ImageView mHeadBackView = null;
    private JSONObject mJSObject;
    private ArrayList<HashMap<String, Object>> mListItems = new ArrayList();
    private ListView mListView;
    private boolean mLoactionChange = false;
    private ProgressBar mLoadMoreImageView;
    private TextView mLoadMoreTextView;
    private double mLocationLatitude = -1.0d;
    private double mLocationLongitude = -1.0d;
    private int mLocationMode = 0;
    private TextView mNearRefreshTextView;
    private NearbyPoint mNearbyPoint = null;
    private LinearLayout mNearbyPointListLinearLayout;
    private NearbyPointListViewAdapter mNearbyPointListViewAdapter;
    private LinearLayout mNearbyPointLoadingLinearLayout;
    private LinearLayout mNearbyPointNetworkLoseLinearLayout;
    private LinearLayout mNearbyPointNotFindLinearLayout;
    private int mPageNum = 0;
    private String[] mPermissionArr = new String[]{LOCATION_PERMISSION};
    private long mRequestTimeMillis = 0;
    private int mStatu = 0;
    public TextView mTitleNameView = null;
    private int mTotal = 0;
    private LinearLayout mloadMoreLinearLayout;
    private View rootView;

    private class GetLocationThread extends Thread {
        private GetLocationThread() {
        }

        public void run() {
            try {
                Thread.sleep((long) NearbyPointListFragment.this.mGetLocationInterval);
                NearbyPointListFragment.this.mHandler.obtainMessage(NearbyPoint.DO_GET_LOCATION).sendToTarget();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class LoadMoreOnClickListener implements OnClickListener {
        private LoadMoreOnClickListener() {
        }

        public void onClick(View arg0) {
            NearbyPointListFragment.this.mLoadMoreImageView.setVisibility(8);
            NearbyPointListFragment.this.mLoadMoreTextView.setText(NearbyPointListFragment.this.mActivity.getApplication().getString(R.string.duoqu_tip_loading));
            NearbyPointListFragment.this.mloadMoreLinearLayout.setEnabled(false);
            NearbyPointListFragment nearbyPointListFragment = NearbyPointListFragment.this;
            nearbyPointListFragment.mPageNum = nearbyPointListFragment.mPageNum + 1;
            NearbyPointListFragment.this.mNearbyPoint.sendMapQueryUrl(NearbyPointListFragment.this.mAddress, NearbyPointListFragment.this.mLocationLatitude, NearbyPointListFragment.this.mLocationLongitude, NearbyPointListFragment.this.mPageNum);
        }
    }

    private class RetryOnClickListener implements OnClickListener {
        private RetryOnClickListener() {
        }

        public void onClick(View view) {
            NearbyPointListFragment.this.retryLoadNearData();
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.rootView == null) {
            this.rootView = inflater.inflate(R.layout.duoqu_nearby_point_list_fragment, null);
        }
        ViewGroup parent = (ViewGroup) this.rootView.getParent();
        if (parent != null) {
            parent.removeView(this.rootView);
        }
        return this.rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initListener();
        this.isPrepared = true;
        this.mLocationMode = getLocationMode(this.mActivity);
    }

    public void onResume() {
        super.onResume();
        checkPermission();
        if (this.mNearbyPointListLinearLayout.getVisibility() == 8 && this.mListItems.size() > 0) {
            this.mNearbyPointListLinearLayout.setVisibility(0);
        }
    }

    public void onDestroy() {
        this.mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void loasList(JSONObject jsonObject) {
        if (jsonObject != null) {
            this.mJSObject = jsonObject;
            if (this.isPrepared) {
                this.mAddress = jsonObject.optString("address");
                if (!StringUtils.isNull(this.mAddress)) {
                    this.mloadMoreLinearLayout.setOnClickListener(new LoadMoreOnClickListener());
                }
                String title = this.mJSObject.optString("menuName");
                if (StringUtils.isNull(title)) {
                    title = this.mActivity.getResources().getString(R.string.duoqu_nearby_point_list_title);
                }
                this.mTitleNameView.setText(title);
                retryLoadNearData();
            }
        }
    }

    private void initListener() {
        this.mHeadBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                NearbyPointListFragment.this.hideFragment();
            }
        });
        this.mNearRefreshTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (NearbyPointListFragment.this.mLocationMode <= 1) {
                    NearbyPointListFragment.this.redirectToLocationServiceSetting(NearbyPointListFragment.this.mActivity);
                } else {
                    NearbyPointListFragment.this.retryLoadNearData();
                }
            }
        });
    }

    private void initViews(View view) {
        this.mNearbyPoint = new NearbyPoint(this.mActivity, this.mHandler);
        this.mNearbyPointListViewAdapter = new NearbyPointListViewAdapter(this.mActivity, this.mListItems);
        this.mNearbyPointLoadingLinearLayout = (LinearLayout) view.findViewById(R.id.duoqu_ll_nearby_point_loading);
        this.mNearbyPointNotFindLinearLayout = (LinearLayout) view.findViewById(R.id.duoqu_ll_nearby_point_not_find);
        this.mNearbyPointNetworkLoseLinearLayout = (LinearLayout) view.findViewById(R.id.duoqu_ll_nearby_point_network_lose);
        this.mNearbyPointNetworkLoseLinearLayout.setOnClickListener(new RetryOnClickListener());
        this.mNearbyPointListLinearLayout = (LinearLayout) view.findViewById(R.id.duoqu_ll_nearby_point_list);
        this.mListView = (ListView) view.findViewById(R.id.duoqu_lv_nearby_point);
        View loadMoreView = this.mActivity.getLayoutInflater().inflate(R.layout.duoqu_nearby_point_list_bottom, null);
        this.mLoadMoreImageView = (ProgressBar) loadMoreView.findViewById(R.id.duoqu_iv_load_more);
        this.mLoadMoreTextView = (TextView) loadMoreView.findViewById(R.id.duoqu_tv_load_more);
        this.mloadMoreLinearLayout = (LinearLayout) loadMoreView.findViewById(R.id.duoqu_ll_load_more);
        this.mNearRefreshTextView = (TextView) view.findViewById(R.id.duoqu_tip_point_refresh);
        this.mListView.addFooterView(loadMoreView);
        this.mListView.setDivider(null);
        this.mListView.setAdapter(this.mNearbyPointListViewAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    if (NearbyPointListFragment.this.mListItems.size() > position) {
                        DuoquUtils.getSdkDoAction().openMap(NearbyPointListFragment.this.mActivity, (String) ((HashMap) NearbyPointListFragment.this.mListItems.get(position)).get("name"), (String) ((HashMap) NearbyPointListFragment.this.mListItems.get(position)).get("address"), ((Double) ((HashMap) NearbyPointListFragment.this.mListItems.get(position)).get("longitude")).doubleValue(), ((Double) ((HashMap) NearbyPointListFragment.this.mListItems.get(position)).get("latitude")).doubleValue());
                    }
                } catch (Throwable e) {
                    LogManager.e("XIAOYUAN", e.getMessage(), e);
                }
            }
        });
        this.mActionBar = (RelativeLayout) view.findViewById(R.id.action_bar);
        this.mHeadBackView = (ImageView) view.findViewById(R.id.duoqu_header_back);
        this.mTitleNameView = (TextView) view.findViewById(R.id.duoqu_title_name);
    }

    private void retryLoadNearData() {
        this.mFirstLoaction = null;
        this.mLoactionChange = false;
        this.mGetLoactionCount = 0;
        this.mGetLocationInterval = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
        this.mFirstQuery = true;
        getLocation(true);
    }

    private void doSendMapQueryUrl(double longitude, double latitude) {
        if (longitude <= 0.0d || latitude <= 0.0d) {
            setViewVisibility(8, 0, 8, 8);
        } else {
            this.mNearbyPoint.sendMapQueryUrl(this.mAddress, latitude, longitude, 0);
        }
    }

    private void setViewVisibility(int loadingVisibility, int notFindVisibility, int networkLoseVisibility, int nearbyPointListVisibility) {
        this.mNearbyPointLoadingLinearLayout.setVisibility(loadingVisibility);
        this.mNearbyPointNetworkLoseLinearLayout.setVisibility(networkLoseVisibility);
        this.mNearbyPointNotFindLinearLayout.setVisibility(notFindVisibility);
        this.mNearbyPointListLinearLayout.setVisibility(nearbyPointListVisibility);
        if (notFindVisibility == 0 || networkLoseVisibility == 0) {
            this.mPageNum = 0;
        }
        setNotFindTipView();
    }

    private ArrayList<HashMap<String, Object>> getListItems(String strQueryResult) {
        try {
            JSONObject jsonQueryResultObject = new JSONObject(strQueryResult);
            this.mTotal = jsonQueryResultObject.getInt("total");
            return NearbyPointList.getListItemsByJson(jsonQueryResultObject);
        } catch (Throwable ex) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment getListItems error " + ex.getMessage(), ex);
            return new ArrayList();
        }
    }

    private void analysisResult(String strQueryResult) {
        if (StringUtils.isNull(strQueryResult)) {
            setViewVisibility(8, 0, 8, 8);
            return;
        }
        try {
            JSONObject jsonQueryResultObject = new JSONObject(strQueryResult);
            if (jsonQueryResultObject.getInt("status") != 0) {
                throw new JSONException("Abnormal returns the result");
            } else if (jsonQueryResultObject.getInt("total") == 0) {
                throw new JSONException("Retrieve result data quantity to zero");
            } else {
                for (HashMap<String, Object> newListItem : getListItems(strQueryResult)) {
                    this.mListItems.add(newListItem);
                }
                if (this.mTotal > 10) {
                    this.mloadMoreLinearLayout.setVisibility(0);
                }
                this.mNearbyPointListViewAdapter.notifyDataSetChanged();
                setViewVisibility(8, 8, 8, 0);
            }
        } catch (Throwable ex) {
            setViewVisibility(8, 0, 8, 8);
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment getListItems error " + ex.getMessage(), ex);
        }
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        return NearbyPointList.getDistance(lat1, lng1, lat2, lng2);
    }

    private void requestLocation() {
        if (XyUtil.checkNetWork(this.mActivity.getApplicationContext(), 2) != 0) {
            setViewVisibility(8, 8, 0, 8);
            return;
        }
        String str = null;
        String locationLatitude = null;
        try {
            if (this.mJSObject != null) {
                str = this.mJSObject.optString("locationLongitude");
                locationLatitude = this.mJSObject.optString("locationLatitude");
            }
            if (StringUtils.isNull(str) || !StringUtils.isNull(locationLatitude)) {
                this.mLocationLongitude = -1.0d;
                this.mLocationLatitude = -1.0d;
                doGetLocation(this.mLocationLongitude, this.mLocationLatitude);
            }
            this.mLocationLongitude = Double.parseDouble(str);
            this.mLocationLatitude = Double.parseDouble(locationLatitude);
            doGetLocation(this.mLocationLongitude, this.mLocationLatitude);
        } catch (Throwable ex) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment requestLocation error " + ex.getMessage(), ex);
        }
    }

    private void setNotFindTipView() {
        if (this.mNearbyPointNotFindLinearLayout.getVisibility() == 0) {
            SpannableString spanString;
            String notFindStr = "";
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
            if (this.mLocationMode <= 1) {
                notFindStr = getString(R.string.duoqu_tip_nearby_point_not_find_chanage_mode);
                spanString = new SpannableString(getString(R.string.duoqu_tip_nearby_point_set_location));
            } else {
                notFindStr = getString(R.string.duoqu_tip_nearby_point_not_find);
                spanString = new SpannableString(getString(R.string.duoqu_tip_nearby_point_refresh));
            }
            spanBuilder.append(notFindStr);
            spanString.setSpan(new ForegroundColorSpan(-16776961), 0, spanString.length(), 33);
            spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 33);
            spanBuilder.append(spanString);
            this.mNearRefreshTextView.setText(spanBuilder);
        }
    }

    private void doGetLocation(double locationLongitude, double locationLatitude) {
        if (locationLongitude <= 0.0d || locationLatitude <= 0.0d) {
            try {
                Location location = XyLoactionManager.getLocation(this.mActivity.getApplicationContext(), this.mHandler);
                if (location == null) {
                    if (this.mGetLoactionCount < 20) {
                        this.mGetLoactionCount++;
                        this.mGetLocationInterval = VTMCDataCache.MAXSIZE;
                        getLocation(false);
                    } else {
                        setViewVisibility(8, 0, 8, 8);
                    }
                } else if (this.mGetLoactionCount == 0) {
                    this.mGetLoactionCount++;
                    this.mGetLocationInterval = VTMCDataCache.MAXSIZE;
                    this.mFirstLoaction = location;
                    getLocation(false);
                } else if (this.mFirstLoaction != null && this.mGetLoactionCount < 10 && isEqualLocationPoint(this.mFirstLoaction.getLongitude(), location.getLongitude()) && isEqualLocationPoint(this.mFirstLoaction.getLatitude(), location.getLatitude())) {
                    this.mGetLoactionCount++;
                    getLocation(false);
                }
            } catch (Throwable ex) {
                setViewVisibility(8, 0, 8, 8);
                SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment doGetLocation error " + ex.getMessage(), ex);
            }
            return;
        }
        try {
            doSendMapQueryUrl(locationLongitude, locationLatitude);
        } catch (Throwable ex2) {
            setViewVisibility(8, 0, 8, 8);
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment 1doGetLocation error " + ex2.getMessage(), ex2);
        }
    }

    private void getLocation(boolean showLoading) {
        if (showLoading) {
            setViewVisibility(0, 8, 8, 8);
        }
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                NearbyPointListFragment.this.requestLocation();
            }
        }, (long) this.mGetLocationInterval);
    }

    private boolean isEqualLocationPoint(double point1, double point2) {
        if (Math.abs(point1 - point2) < 1.0E-7d) {
            return true;
        }
        return false;
    }

    private void hideFragment() {
        if (this.mActivity instanceof IXYSmartMessageActivity) {
            ((IXYSmartMessageActivity) this.mActivity).finshFragemnt(this);
        }
    }

    private void checkPermission() {
        if (this.mStatu == 2) {
            if (isOnLocationService()) {
                loasList(this.mJSObject);
            } else {
                hideFragment();
            }
        } else if (this.mStatu != 1) {
            this.mStatu = 1;
            try {
                if (!PermissionRequestActivity.appHasLocationPermission()) {
                    tryRequestPermission();
                } else if (isOnLocationService()) {
                    loasList(this.mJSObject);
                } else {
                    gotoPositionDialog();
                }
            } catch (Throwable e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment onResume error " + e.getMessage(), e);
            }
        }
    }

    private void gotoPositionDialog() {
        GotoMyPositionDialog.show(this.mActivity, new XyCallBack() {
            public void execute(Object... arg) {
                if (arg != null && arg.length >= 1 && arg[0] != null && (arg[0] instanceof Integer)) {
                    switch (((Integer) arg[0]).intValue()) {
                        case 0:
                            NearbyPointListFragment.this.hideFragment();
                            break;
                        case 1:
                            NearbyPointListFragment.this.redirectToLocationServiceSetting(NearbyPointListFragment.this.mActivity);
                            NearbyPointListFragment.this.mStatu = 2;
                            break;
                    }
                }
            }
        });
    }

    private void tryRequestPermission() {
        try {
            this.mRequestTimeMillis = SystemClock.elapsedRealtime();
            requestPermissions(this.mPermissionArr, 1);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment tryRequestPermission error " + e.getMessage(), e);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionRequestActivity.appHasLocationPermission()) {
                if (isOnLocationService()) {
                    loasList(this.mJSObject);
                } else {
                    gotoPositionDialog();
                }
                return;
            }
            if (SystemClock.elapsedRealtime() - this.mRequestTimeMillis >= AUTOMATED_RESULT_THRESHOLD_MILLLIS) {
                hideFragment();
            } else if (!PermissionCheckActivity.recheckUserRejectPermissions(this.mActivity, this.mPermissionArr)) {
                PermissionCheckActivity.gotoPackageSettings(this.mActivity);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (2 == requestCode) {
            if (resultCode == 0) {
                hideFragment();
            } else if (-1 == resultCode && PermissionRequestActivity.appHasLocationPermission()) {
                if (isOnLocationService()) {
                    loasList(this.mJSObject);
                } else {
                    gotoPositionDialog();
                }
            }
        }
    }

    private boolean isOnLocationService() {
        boolean z = false;
        try {
            if (getLocationMode(this.mActivity) != 0) {
                z = true;
            }
            return z;
        } catch (Throwable th) {
            return true;
        }
    }

    private int getLocationMode(Context context) {
        if (context != null) {
            try {
                return Secure.getInt(context.getContentResolver(), LOCATION_MODE, 0);
            } catch (Exception e) {
            }
        }
        return -1;
    }

    private void redirectToLocationServiceSetting(Context ctx) {
        try {
            Intent it = new Intent();
            it.setPackage("com.android.settings");
            it.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
            ctx.startActivity(it);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointListFragment tryRequestPermission error " + e.getMessage(), e);
        }
    }

    private void setActionBarHeight() {
        if (this.mActionBar != null) {
            TypedArray actionBarSizeTypeArray = this.mActivity.obtainStyledAttributes(new int[]{16843499});
            int actionBarHeight = (int) actionBarSizeTypeArray.getDimension(0, 0.0f);
            if (actionBarHeight != 0) {
                LayoutParams pp = this.mActionBar.getLayoutParams();
                pp.height = actionBarHeight;
                this.mActionBar.setLayoutParams(pp);
            }
            actionBarSizeTypeArray.recycle();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setActionBarHeight();
    }
}
