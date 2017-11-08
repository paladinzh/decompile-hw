package com.huawei.systemmanager.netassistant.traffic.appdetail;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.collect.Lists;
import com.huawei.netassistant.common.ParcelableDailyTrafficItem;
import com.huawei.netassistant.service.NetAssistantManager;
import com.huawei.netassistant.ui.MyTextView;
import com.huawei.netassistant.ui.NetTrafficLineChartFragment;
import com.huawei.netassistant.ui.view.ChartData;
import com.huawei.netassistant.ui.view.TrafficLineChartView;
import com.huawei.netassistant.ui.view.TrafficLineChartView.ChartSizeChangeListener;
import com.huawei.netassistant.ui.view.TrafficLineChartView.ClickPointListener;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class AppDetailLineChartFragment extends Fragment implements OnItemSelectedListener {
    private static final int DATA_SIZE_MAX = 7;
    private static final int DATA_SIZE_MIN = 1;
    public static final String HW_SHOW_4_5G_FOR_MCC = "hw_show_4_5G_for_mcc";
    public static final String HW_SHOW_LTE = "hw_show_lte";
    private static final int MAX_NUM = 5;
    private static final int MIN_NUM = 0;
    private static final int NUM_SIZE = 5;
    private static final String TAG = AppDetailLineChartFragment.class.getSimpleName();
    private static final int TYPE_FIRST = 1;
    private static final int TYPE_LAST = 3;
    private static final int TYPE_MIDDLE = 2;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_WIFI = 1;
    private int mActivityFrom;
    ChartSizeChangeListener mChartSizeChangeListener = new ChartSizeChangeListener() {
        public void onChartSizeChanged(int xWidth) {
            ChartData lastData = AppDetailLineChartFragment.this.mTrafficLineChartView.getLastData();
            if (lastData != null) {
                AppDetailLineChartFragment.this.mDataTextView.udpateXWidth(xWidth, (int) lastData.getX());
                AppDetailLineChartFragment.this.initLastData();
            }
        }
    };
    ClickPointListener mClickPointListener = new ClickPointListener() {
        public void onPointClick(ChartData data) {
            AppDetailLineChartFragment.this.mDataTextView.setText(data.getText());
            AppDetailLineChartFragment.this.mDataTextView.setXY(AppDetailLineChartFragment.this.xWidth, (int) data.getX(), (int) data.getY(), 2);
            AppDetailLineChartFragment.this.mDataTextView.setBackground(AppDetailLineChartFragment.this.getResources().getDrawable(R.drawable.power_time_picker_battery_new));
        }

        public void onLastPointClick(ChartData data) {
            AppDetailLineChartFragment.this.mDataTextView.setText(data.getText());
            AppDetailLineChartFragment.this.mDataTextView.setXY(AppDetailLineChartFragment.this.xWidth, (int) data.getX(), (int) data.getY(), 3);
            AppDetailLineChartFragment.this.mDataTextView.setBackground(AppDetailLineChartFragment.this.getResources().getDrawable(R.drawable.power_time_picker_right_battery_new));
        }

        public void onFstPointClick(ChartData data) {
            AppDetailLineChartFragment.this.mDataTextView.setText(data.getText());
            AppDetailLineChartFragment.this.mDataTextView.setXY(AppDetailLineChartFragment.this.xWidth, (int) data.getX(), (int) data.getY(), 1);
            AppDetailLineChartFragment.this.mDataTextView.setBackground(AppDetailLineChartFragment.this.getResources().getDrawable(R.drawable.power_time_picker_left_battery_new));
        }
    };
    private MyTextView mDataTextView;
    HorizontalScrollView mHorizontalScrollView = null;
    private String mImsi;
    private int mPeriod;
    private Spinner mSpinner;
    private int mSubId = -1;
    TrafficLineChartView mTrafficLineChartView = null;
    private TextView mTxtAppUsedContent;
    private int mType;
    private int mUid;
    private TextView mYNumtext0;
    private TextView mYNumtext1;
    private TextView mYNumtext2;
    private TextView mYNumtext3;
    private TextView mYNumtext4;
    private TextView mYNumtext5;
    private int xWidth;

    private class AppDetailContentTask extends AsyncTask<Void, Void, long[]> {
        protected long[] doInBackground(Void... params) {
            long endTime = DateUtil.getCurrentTimeMills();
            long startTime = getStartTime(AppDetailLineChartFragment.this.mPeriod, endTime);
            long foreground = NetAssistantManager.getForegroundBytes(AppDetailLineChartFragment.this.getTemplate(), AppDetailLineChartFragment.this.mUid, startTime, endTime);
            long background = NetAssistantManager.getBackgroundBytes(AppDetailLineChartFragment.this.getTemplate(), AppDetailLineChartFragment.this.mUid, startTime, endTime);
            return new long[]{foreground, background};
        }

        private long getStartTime(int mPeriod, long endTime) {
            if (mPeriod == Constant.PERIOD_ARRAY[0] || mPeriod == Constant.PERIOD_ARRAY[2]) {
                return endTime - SpaceConst.LARGE_FILE_EXCEED_INTERVAL_TIME;
            }
            if (mPeriod == Constant.PERIOD_ARRAY[1] || mPeriod == Constant.PERIOD_ARRAY[3]) {
                return endTime - 86400000;
            }
            return endTime;
        }

        protected void onPostExecute(long[] result) {
            super.onPostExecute(result);
            if (AppDetailLineChartFragment.this.mTxtAppUsedContent != null && AppDetailLineChartFragment.this.isAdded()) {
                AppDetailLineChartFragment.this.mTxtAppUsedContent.setText(AppDetailLineChartFragment.this.getString(R.string.net_assistant_app_detail_traffic_content, new Object[]{CommonMethodUtil.formatBytes(AppDetailLineChartFragment.this.getActivity(), result[0] + result[1]), CommonMethodUtil.formatBytes(AppDetailLineChartFragment.this.getActivity(), result[0]), CommonMethodUtil.formatBytes(AppDetailLineChartFragment.this.getActivity(), result[1])}));
            }
        }
    }

    private class TrafficDataTask extends AsyncTask<String, Void, List<ParcelableDailyTrafficItem>> {
        List<ParcelableDailyTrafficItem> items;

        private TrafficDataTask() {
            this.items = Lists.newArrayList();
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected List<ParcelableDailyTrafficItem> doInBackground(String... params) {
            NetworkTemplate template = AppDetailLineChartFragment.this.getTemplate();
            if (AppDetailLineChartFragment.this.mPeriod == Constant.PERIOD_ARRAY[0] || AppDetailLineChartFragment.this.mPeriod == Constant.PERIOD_ARRAY[2]) {
                this.items = NetAssistantManager.getMonthPerDayTraffic(template, AppDetailLineChartFragment.this.mUid);
            } else {
                this.items = NetAssistantManager.getDayPerHourTraffic(template, AppDetailLineChartFragment.this.mUid);
            }
            return this.items;
        }

        protected void onPostExecute(List<ParcelableDailyTrafficItem> result) {
            super.onPostExecute(result);
            if (result != null && AppDetailLineChartFragment.this.isAdded()) {
                AppDetailLineChartFragment.this.mTrafficLineChartView.setData(result);
                AppDetailLineChartFragment.this.mTrafficLineChartView.requestLayout();
                AppDetailLineChartFragment.this.mHorizontalScrollView.requestLayout();
                AppDetailLineChartFragment.this.initYNumText(result);
            }
        }
    }

    public static Bundle newBundle(int type, int uid, String imsi, int from, int subId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_TYPE, type);
        bundle.putInt("uid", uid);
        bundle.putString(Constant.EXTRA_IMSI, imsi);
        bundle.putInt(Constant.EXTRA_ACTIVITY_FROM, from);
        bundle.putInt(Constant.EXTRA_SUBID, subId);
        return bundle;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mType = bundle.getInt(Constant.EXTRA_TYPE);
            this.mUid = bundle.getInt("uid");
            this.mImsi = bundle.getString(Constant.EXTRA_IMSI);
            this.mActivityFrom = bundle.getInt(Constant.EXTRA_ACTIVITY_FROM);
            this.mSubId = bundle.getInt(Constant.EXTRA_SUBID, -1);
            if (this.mActivityFrom == 1 && this.mType == 0) {
                this.mPeriod = Constant.PERIOD_ARRAY[2];
            } else {
                this.mPeriod = Constant.PERIOD_ARRAY[0];
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.app_detail_linechart_fragment, null);
        this.mTrafficLineChartView = (TrafficLineChartView) v.findViewById(R.id.linechart_view);
        this.mHorizontalScrollView = (HorizontalScrollView) v.findViewById(R.id.horizon_scroll_view);
        this.mSpinner = (Spinner) v.findViewById(R.id.spinner);
        this.mTxtAppUsedContent = (TextView) v.findViewById(R.id.app_used_content);
        this.mTrafficLineChartView.addClickPointListener(this.mClickPointListener);
        this.mTrafficLineChartView.addChartSizeChangeListener(this.mChartSizeChangeListener);
        this.mDataTextView = (MyTextView) v.findViewById(R.id.data_textview);
        this.mYNumtext0 = (TextView) v.findViewById(R.id.y_text_0);
        this.mYNumtext1 = (TextView) v.findViewById(R.id.y_text_1);
        this.mYNumtext2 = (TextView) v.findViewById(R.id.y_text_2);
        this.mYNumtext3 = (TextView) v.findViewById(R.id.y_text_3);
        this.mYNumtext4 = (TextView) v.findViewById(R.id.y_text_4);
        this.mYNumtext5 = (TextView) v.findViewById(R.id.y_text_5);
        return v;
    }

    public void onResume() {
        ArrayAdapter<String> adapter;
        super.onResume();
        String[] array;
        int[] num;
        int i;
        if (this.mType == 0) {
            array = getResources().getStringArray(R.array.netassistant_app_detail_spinner_content_1);
            num = getResources().getIntArray(R.array.netassistant_app_detail_spinner_content_num);
            for (i = 0; i < array.length; i++) {
                array[i] = String.format(array[i], new Object[]{Utility.getLocaleNumber(num[i])});
                array[i] = replace4GString(getActivity(), array[i]);
            }
            adapter = new ArrayAdapter(getActivity(), 17367049, array);
        } else {
            array = getResources().getStringArray(R.array.netassistant_app_detail_wifi_spinner_content_1);
            num = getResources().getIntArray(R.array.netassistant_app_detail_wifi_spinner_content_num);
            for (i = 0; i < array.length; i++) {
                array[i] = String.format(array[i], new Object[]{Utility.getLocaleNumber(num[i])});
            }
            adapter = new ArrayAdapter(getActivity(), 17367049, array);
        }
        this.mSpinner.setAdapter(adapter);
        this.mSpinner.setOnItemSelectedListener(this);
        if (this.mType == 0) {
            this.mSpinner.setSelection(this.mPeriod);
        }
        new TrafficDataTask().execute(new String[0]);
        new AppDetailContentTask().execute(new Void[0]);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mTrafficLineChartView.removeClickPointListener();
        this.mTrafficLineChartView.removeChartSizeChangeListener();
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        this.mPeriod = Constant.PERIOD_ARRAY[position];
        new TrafficDataTask().execute(new String[0]);
        new AppDetailContentTask().execute(new Void[0]);
        HwLog.d(TAG, "spinner select to " + this.mPeriod);
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private NetworkTemplate getTemplate() {
        if (this.mType != 0) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        }
        if (this.mPeriod == Constant.PERIOD_ARRAY[0] || this.mPeriod == Constant.PERIOD_ARRAY[1]) {
            return NetworkTemplate.buildTemplateMobileAll(this.mImsi);
        }
        return NetworkTemplate.buildTemplateMobile4g(this.mImsi);
    }

    private void initYNumText(List<ParcelableDailyTrafficItem> items) {
        long result = 0;
        int N = items.size();
        for (int i = 0; i < N; i++) {
            if (result < ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic) {
                result = ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic;
            }
        }
        float maxNum = (float) NetTrafficLineChartFragment.formatFileSize(result);
        float disData = maxNum / 5.0f;
        int[] dataNums = new int[]{0, (int) (Utility.ALPHA_MAX * disData), (int) (2.0f * disData), (int) (3.0f * disData), (int) (4.0f * disData), (int) (5.0f * disData)};
        this.mYNumtext0.setText(Utility.getLocaleNumber(dataNums[0]));
        this.mYNumtext1.setText(Utility.getLocaleNumber(dataNums[1]));
        this.mYNumtext2.setText(Utility.getLocaleNumber(dataNums[2]));
        this.mYNumtext3.setText(Utility.getLocaleNumber(dataNums[3]));
        this.mYNumtext4.setText(Utility.getLocaleNumber(dataNums[4]));
        this.mYNumtext5.setText(Utility.getLocaleNumber(dataNums[5]));
        if (maxNum < 5.0f) {
            this.mYNumtext1.setVisibility(4);
            this.mYNumtext2.setVisibility(4);
            this.mYNumtext3.setVisibility(4);
            this.mYNumtext4.setVisibility(4);
        }
        if (maxNum == 0.0f) {
            this.mYNumtext5.setVisibility(4);
        }
        initLastData();
    }

    private void initLastData() {
        ChartData lastData = this.mTrafficLineChartView.getLastData();
        int dataSize = this.mTrafficLineChartView.getDataSize();
        this.xWidth = this.mTrafficLineChartView.getXWidth();
        if (lastData != null) {
            this.mDataTextView.setText(lastData.getText());
            if (1 == dataSize) {
                this.mDataTextView.setXY(this.xWidth, (int) lastData.getX(), (int) lastData.getY(), 1);
                this.mDataTextView.setBackground(getResources().getDrawable(R.drawable.power_time_picker_left_battery_new));
            } else if (1 >= dataSize || dataSize >= 7) {
                this.mDataTextView.setXY(this.xWidth, (int) lastData.getX(), (int) lastData.getY(), 3);
                this.mDataTextView.setBackground(getResources().getDrawable(R.drawable.power_time_picker_right_battery_new));
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        AppDetailLineChartFragment.this.mHorizontalScrollView.scrollBy(AppDetailLineChartFragment.this.mHorizontalScrollView.getChildAt(0).getWidth(), 0);
                    }
                }, 50);
            } else {
                this.mDataTextView.setXY(this.xWidth, (int) lastData.getX(), (int) lastData.getY(), 2);
                this.mDataTextView.setBackground(getResources().getDrawable(R.drawable.power_time_picker_battery_new));
            }
        }
    }

    private boolean isConfigCountry(Context context, String configEntry, String mccmnc) {
        if (context == null) {
            return false;
        }
        String configString = System.getString(context.getContentResolver(), configEntry);
        if (TextUtils.isEmpty(configString)) {
            return false;
        }
        if ("ALL".equals(configString)) {
            return true;
        }
        boolean result = false;
        if (!TextUtils.isEmpty(mccmnc)) {
            String[] custValues = configString.trim().split(SqlMarker.SQL_END);
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (mccmnc.startsWith(custValues[i]) || mccmnc.equalsIgnoreCase(custValues[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        return result;
    }

    private String replace4GString(Activity activity, String str4G) {
        Context context = activity.getApplicationContext();
        if (context == null) {
            return str4G;
        }
        TelephonyManager telManager = TelephonyManager.from(context);
        if (telManager == null) {
            return str4G;
        }
        String mccmnc;
        if (this.mSubId == -1) {
            mccmnc = telManager.getSimOperator();
        } else {
            mccmnc = telManager.getSimOperator(this.mSubId);
        }
        boolean isLTE = isConfigCountry(context, "hw_show_lte", mccmnc);
        boolean is4_5G = isConfigCountry(context, "hw_show_4_5G_for_mcc", mccmnc);
        if (isLTE) {
            return str4G.replace("4G", "LTE");
        }
        if (is4_5G) {
            return str4G.replace("4G", "4.5G");
        }
        return str4G;
    }
}
