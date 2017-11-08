package cn.com.xy.sms.sdk.ui.popu.web;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.XyLoactionManager;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.ui.settings.PermissionRequestActivity;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NearbyPointList extends HwBaseActivity {
    private static final double EARTH_RADIUS = 6378.137d;
    private static final String LTAG = NearbyPointList.class.getSimpleName();
    private static Typeface mMedium;
    private static Typeface mRegular;
    private String mAddress = null;
    private boolean mFirstIn = true;
    private Location mFirstLoaction = null;
    private boolean mFirstQuery = true;
    private int mGetLoactionCount = 0;
    private int mGetLocationInterval = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!NearbyPointList.this.isFinishing()) {
                switch (msg.what) {
                    case NearbyPoint.QUERY_RESULT_RECEIVE /*4097*/:
                        NearbyPointList.this.analysisResult(msg.getData().getString(NearbyPoint.QUERY_RESULT));
                        if (NearbyPointList.this.mTotal <= (NearbyPointList.this.mPageNum + 1) * 10) {
                            NearbyPointList.this.mloadMoreLinearLayout.setVisibility(8);
                            break;
                        }
                        NearbyPointList.this.mLoadMoreImageView.setVisibility(8);
                        NearbyPointList.this.mLoadMoreTextView.setTextColor(NearbyPointList.this.getResources().getColor(R.color.duoqu_web_nearby_point_load_more));
                        if (NearbyPointList.mMedium != null) {
                            NearbyPointList.this.mLoadMoreTextView.setTypeface(NearbyPointList.mMedium);
                        }
                        NearbyPointList.this.mloadMoreLinearLayout.setEnabled(true);
                        break;
                    case NearbyPoint.QUERY_PARAM_ERROR /*4098*/:
                    case NearbyPoint.GET_QUERY_URL_FAILURE /*4099*/:
                    case 4100:
                        NearbyPointList.this.setViewVisibility(8, 0, 8, 8);
                        break;
                    case NearbyPoint.DO_GET_LOCATION /*4101*/:
                        NearbyPointList.this.requestLocation();
                        break;
                    case 4102:
                        NearbyPointList.this.mLocationLongitude = msg.getData().getDouble("longitude");
                        NearbyPointList.this.mLocationLatitude = msg.getData().getDouble("latitude");
                        if (NearbyPointList.this.mFirstLoaction != null && NearbyPointList.getDistance(NearbyPointList.this.mFirstLoaction.getLatitude(), NearbyPointList.this.mFirstLoaction.getLongitude(), NearbyPointList.this.mLocationLatitude, NearbyPointList.this.mLocationLongitude) > 100.0d) {
                            NearbyPointList.this.mLoactionChange = true;
                        }
                        if (NearbyPointList.this.mFirstQuery || NearbyPointList.this.mLoactionChange) {
                            NearbyPointList.this.mFirstQuery = false;
                            NearbyPointList.this.doSendMapQueryUrl(NearbyPointList.this.mLocationLongitude, NearbyPointList.this.mLocationLatitude);
                            break;
                        }
                }
            }
        }
    };
    private ImageView mHeadBackView = null;
    private ArrayList<HashMap<String, Object>> mListItems = new ArrayList();
    private ListView mListView;
    private boolean mLoactionChange = false;
    private ProgressBar mLoadMoreImageView;
    private TextView mLoadMoreTextView;
    private double mLocationLatitude = -1.0d;
    private double mLocationLongitude = -1.0d;
    private int mLocationMode = 0;
    private NearbyPoint mNearbyPoint = null;
    private LinearLayout mNearbyPointListLinearLayout;
    private NearbyPointListViewAdapter mNearbyPointListViewAdapter;
    private LinearLayout mNearbyPointLoadingLinearLayout;
    private LinearLayout mNearbyPointNetworkLoseLinearLayout;
    private LinearLayout mNearbyPointNotFindLinearLayout;
    private TextView mNetworkSetting = null;
    private int mPageNum = 0;
    private TextView mTitleNameView = null;
    private int mTotal = 0;
    private LinearLayout mloadMoreLinearLayout;

    private class GetLocationThread extends Thread {
        private GetLocationThread() {
        }

        public void run() {
            try {
                Thread.sleep((long) NearbyPointList.this.mGetLocationInterval);
                NearbyPointList.this.mHandler.obtainMessage(NearbyPoint.DO_GET_LOCATION).sendToTarget();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class LoadMoreOnClickListener implements OnClickListener {
        private LoadMoreOnClickListener() {
        }

        @SuppressLint({"ResourceAsColor"})
        public void onClick(View arg0) {
            NearbyPointList.this.mLoadMoreImageView.setVisibility(0);
            NearbyPointList.this.mLoadMoreTextView.setTextColor(NearbyPointList.this.getResources().getColor(R.color.duoqu_web_nearby_point_load_more_loading));
            if (NearbyPointList.mRegular != null) {
                NearbyPointList.this.mLoadMoreTextView.setTypeface(NearbyPointList.mRegular);
            }
            NearbyPointList.this.mloadMoreLinearLayout.setEnabled(false);
            NearbyPointList nearbyPointList = NearbyPointList.this;
            nearbyPointList.mPageNum = nearbyPointList.mPageNum + 1;
            NearbyPointList.this.mNearbyPoint.sendMapQueryUrl(NearbyPointList.this.mAddress, NearbyPointList.this.mLocationLatitude, NearbyPointList.this.mLocationLongitude, NearbyPointList.this.mPageNum);
        }
    }

    private class NetSettingOnClickListener implements OnClickListener {
        private NetSettingOnClickListener() {
        }

        public void onClick(View arg0) {
            Exception e;
            try {
                Intent intent;
                if (VERSION.SDK_INT > 10) {
                    intent = new Intent("android.settings.SETTINGS");
                } else {
                    Intent intent2 = new Intent();
                    try {
                        intent2.setComponent(new ComponentName("com.android.settings", "com.android.settings.WirelessSettings"));
                        intent2.setAction("android.intent.action.VIEW");
                        intent = intent2;
                    } catch (Exception e2) {
                        e = e2;
                        intent = intent2;
                        SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList NetSettingOnClickListener error:" + e.getMessage(), e);
                    }
                }
                NearbyPointList.this.startActivity(intent);
            } catch (Exception e3) {
                e = e3;
                SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList NetSettingOnClickListener error:" + e.getMessage(), e);
            }
        }
    }

    private class RetryOnClickListener implements OnClickListener {
        private RetryOnClickListener() {
        }

        public void onClick(View view) {
            NearbyPointList.this.retryLoadNearData();
        }
    }

    static {
        mRegular = null;
        mMedium = null;
        try {
            mRegular = Typeface.create("HwChinese-regular", 0);
            mMedium = Typeface.create("HwChinese-medium", 0);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("init Typeface error:" + e.getMessage(), e);
        }
    }

    @SuppressLint({"NewApi"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duoqu_nearby_point_list);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayOptions(16);
        RelativeLayout actionBarLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.duoqu_web_action_bar, null);
        actionBar.setCustomView(actionBarLayout, new LayoutParams(-1, -1));
        actionBarLayout.setBackgroundColor(getResources().getColor(R.color.duoqu_actionbar_bg_color));
        this.mAddress = getIntent().getStringExtra("address");
        this.mNearbyPoint = new NearbyPoint(this, this.mHandler);
        this.mNearbyPointLoadingLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_loading);
        this.mNearbyPointNotFindLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_not_find);
        this.mNearbyPointNetworkLoseLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_network_lose);
        this.mNearbyPointNetworkLoseLinearLayout.setOnClickListener(new RetryOnClickListener());
        this.mNearbyPointListLinearLayout = (LinearLayout) findViewById(R.id.duoqu_ll_nearby_point_list);
        this.mListView = (ListView) findViewById(R.id.duoqu_lv_nearby_point);
        this.mNetworkSetting = (TextView) findViewById(R.id.duoqu_network_setting);
        this.mNetworkSetting.setOnClickListener(new NetSettingOnClickListener());
        this.mNearbyPointListViewAdapter = new NearbyPointListViewAdapter(this, this.mListItems);
        this.mHeadBackView = (ImageView) actionBarLayout.findViewById(R.id.duoqu_header_back);
        this.mTitleNameView = (TextView) actionBarLayout.findViewById(R.id.duoqu_title_name);
        CharSequence title = null;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("menuName")) {
            title = intent.getStringExtra("menuName");
        }
        if (StringUtils.isNull(title)) {
            title = getResources().getString(R.string.duoqu_nearby_point_list_title);
        }
        this.mTitleNameView.setText(title);
        View loadMoreView = getLayoutInflater().inflate(R.layout.duoqu_nearby_point_list_bottom, null);
        this.mLoadMoreImageView = (ProgressBar) loadMoreView.findViewById(R.id.duoqu_iv_load_more);
        this.mLoadMoreTextView = (TextView) loadMoreView.findViewById(R.id.duoqu_tv_load_more);
        this.mloadMoreLinearLayout = (LinearLayout) loadMoreView.findViewById(R.id.duoqu_ll_load_more);
        this.mloadMoreLinearLayout.setOnClickListener(new LoadMoreOnClickListener());
        this.mListView.addFooterView(loadMoreView);
        this.mListView.setDivider(null);
        this.mListView.setAdapter(this.mNearbyPointListViewAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    if (NearbyPointList.this.mListItems.size() > position) {
                        DuoquUtils.getSdkDoAction().openMap(NearbyPointList.this.getApplicationContext(), (String) ((HashMap) NearbyPointList.this.mListItems.get(position)).get("name"), (String) ((HashMap) NearbyPointList.this.mListItems.get(position)).get("address"), ((Double) ((HashMap) NearbyPointList.this.mListItems.get(position)).get("longitude")).doubleValue(), ((Double) ((HashMap) NearbyPointList.this.mListItems.get(position)).get("latitude")).doubleValue());
                    }
                } catch (Throwable e) {
                    SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList setOnItemClickListener error:" + e.getMessage(), e);
                }
            }
        });
        setTopStyle(this);
        initListener();
    }

    protected void onResume() {
        super.onResume();
        this.mLocationMode = PermissionRequestActivity.getLocationMode(this);
        if (this.mFirstIn || (this.mNearbyPointListLinearLayout.getVisibility() == 8 && this.mListItems.size() == 0)) {
            retryLoadNearData();
        } else if (this.mNearbyPointListLinearLayout.getVisibility() == 8 && this.mListItems.size() > 0) {
            this.mNearbyPointListLinearLayout.setVisibility(0);
        }
        this.mFirstIn = false;
    }

    private void setNotFindTipView() {
        if (this.mNearbyPointNotFindLinearLayout.getVisibility() == 0) {
        }
    }

    private void setTopStyle(Context context) {
        if (HwUiStyleUtils.isSuggestDarkStyle(context)) {
            this.mTitleNameView.setTextColor(ResEx.self().getCachedColor(R.color.title_color_primary_dark));
        }
    }

    void initListener() {
        this.mHeadBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                NearbyPointList.this.finish();
            }
        });
        this.mNearbyPointNotFindLinearLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (NearbyPointList.this.mLocationMode <= 1) {
                    PermissionRequestActivity.redirectToLocationServiceSetting(NearbyPointList.this);
                } else {
                    NearbyPointList.this.retryLoadNearData();
                }
            }
        });
    }

    protected void onDestroy() {
        this.mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void retryLoadNearData() {
        this.mGetLoactionCount = 0;
        this.mGetLocationInterval = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
        this.mFirstLoaction = null;
        this.mLoactionChange = false;
        this.mFirstQuery = true;
        getLocation(true);
    }

    private void getLocation(boolean showLoading) {
        if (showLoading) {
            setViewVisibility(0, 8, 8, 8);
        }
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                NearbyPointList.this.requestLocation();
            }
        }, (long) this.mGetLocationInterval);
    }

    private void doSendMapQueryUrl(double longitude, double latitude) {
        if (longitude <= 0.0d || latitude <= 0.0d) {
            setViewVisibility(8, 0, 8, 8);
        } else {
            this.mNearbyPoint.sendMapQueryUrl(this.mAddress, latitude, longitude, 0);
        }
    }

    private void doGetLocation(double locationLongitude, double locationLatitude) {
        if (locationLongitude <= 0.0d || locationLatitude <= 0.0d) {
            try {
                Location location = XyLoactionManager.getLocation(getApplicationContext(), this.mHandler);
                if (location == null) {
                    if (this.mGetLoactionCount < 20) {
                        this.mGetLocationInterval = VTMCDataCache.MAXSIZE;
                        this.mGetLoactionCount++;
                        getLocation(false);
                    } else {
                        setViewVisibility(8, 0, 8, 8);
                    }
                } else if (this.mGetLoactionCount == 0) {
                    this.mGetLocationInterval = VTMCDataCache.MAXSIZE;
                    this.mGetLoactionCount++;
                    this.mFirstLoaction = location;
                    getLocation(false);
                } else if (this.mFirstLoaction != null && this.mGetLoactionCount < 10 && isEqualLocationPoint(this.mFirstLoaction.getLongitude(), location.getLongitude()) && isEqualLocationPoint(this.mFirstLoaction.getLatitude(), location.getLatitude())) {
                    this.mGetLoactionCount++;
                    getLocation(false);
                }
            } catch (Exception ex) {
                setViewVisibility(8, 0, 8, 8);
                ex.printStackTrace();
            }
            return;
        }
        try {
            doSendMapQueryUrl(locationLongitude, locationLatitude);
        } catch (Throwable th) {
            setViewVisibility(8, 0, 8, 8);
        }
    }

    private boolean isEqualLocationPoint(double point1, double point2) {
        if (Math.abs(point1 - point2) < 1.0E-7d) {
            return true;
        }
        return false;
    }

    private void setViewVisibility(int loadingVisibility, int notFindVisibility, int networkLoseVisibility, int nearbyPointListVisibility) {
        this.mNearbyPointLoadingLinearLayout.setVisibility(loadingVisibility);
        this.mNearbyPointNotFindLinearLayout.setVisibility(notFindVisibility);
        this.mNearbyPointNetworkLoseLinearLayout.setVisibility(networkLoseVisibility);
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
            return getListItemsByJson(jsonQueryResultObject);
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearByPointList.getListItems", e);
            return new ArrayList();
        }
    }

    public static ArrayList<HashMap<String, Object>> getListItemsByJson(JSONObject jsonQueryResultObject) {
        ArrayList<HashMap<String, Object>> list = new ArrayList();
        try {
            JSONArray jsonResultsArray = jsonQueryResultObject.getJSONArray("results");
            for (int i = 0; i < jsonResultsArray.length(); i++) {
                JSONObject jsonObject = jsonResultsArray.getJSONObject(i);
                HashMap<String, Object> map = new HashMap();
                map.put("name", jsonObject.getString("name"));
                map.put("address", jsonObject.getString("address"));
                if (jsonObject.has("telephone")) {
                    map.put("phone", jsonObject.getString("telephone"));
                } else {
                    map.put("phone", "");
                }
                map.put("distance", Integer.valueOf(jsonObject.getJSONObject("detail_info").getInt("distance")));
                jsonObject = jsonObject.getJSONObject(NetUtil.REQ_QUERY_LOCATION);
                map.put("longitude", Double.valueOf(jsonObject.getDouble(Constant.LOACTION_LONGITUDE)));
                map.put("latitude", Double.valueOf(jsonObject.getDouble(Constant.LOACTION_LATITUDE)));
                list.add(map);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearByPointList.getListItemsByJson", e);
        }
        return list;
    }

    private void analysisResult(String strQueryResult) {
        if (StringUtils.isNull(strQueryResult)) {
            setViewVisibility(8, 0, 8, 8);
            return;
        }
        try {
            JSONObject jsonQueryResultObject = new JSONObject(strQueryResult);
            if (jsonQueryResultObject.getInt("status") != 0) {
                throw new JSONException("Abnormal returns the result in NearbyPointList");
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
        } catch (Throwable e) {
            e.printStackTrace();
            setViewVisibility(8, 0, 8, 8);
        }
    }

    private static double rad(double d) {
        return (3.141592653589793d * d) / 180.0d;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        return (EARTH_RADIUS * (2.0d * Math.asin(Math.sqrt(Math.pow(Math.sin((radLat1 - radLat2) / 2.0d), 2.0d) + ((Math.cos(radLat1) * Math.cos(radLat2)) * Math.pow(Math.sin((rad(lng1) - rad(lng2)) / 2.0d), 2.0d)))))) * 1000.0d;
    }

    private void requestLocation() {
        if (XyUtil.checkNetWork(getApplicationContext(), 2) != 0) {
            setViewVisibility(8, 8, 0, 8);
            return;
        }
        String locationLongitude = getIntent().getStringExtra("locationLongitude");
        String locationLatitude = getIntent().getStringExtra("locationLatitude");
        try {
            if (StringUtils.isNull(locationLongitude) || StringUtils.isNull(locationLatitude)) {
                this.mLocationLongitude = -1.0d;
                this.mLocationLatitude = -1.0d;
                doGetLocation(this.mLocationLongitude, this.mLocationLatitude);
            }
            this.mLocationLongitude = Double.parseDouble(locationLongitude);
            this.mLocationLatitude = Double.parseDouble(locationLatitude);
            doGetLocation(this.mLocationLongitude, this.mLocationLatitude);
        } catch (NumberFormatException e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList requestLocation error:" + e.getMessage(), e);
        }
    }

    public static void openNearSiteActivity(Context ctx, String address, String menuName) {
        try {
            Intent intent = new Intent();
            intent.setClass(ctx, NearbyPointList.class);
            intent.setFlags(131072);
            intent.putExtra("address", address);
            if (!StringUtils.isNull(menuName)) {
                intent.putExtra("menuName", menuName);
            }
            ctx.startActivity(intent);
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("NearbyPointList openNearSiteActivity error:" + e.getMessage(), e);
        }
    }
}
