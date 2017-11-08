package com.huawei.netassistant.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.netassistant.common.ParcelableDailyTrafficItem;
import com.huawei.netassistant.service.NetAssistantManager;
import com.huawei.netassistant.ui.view.ChartData;
import com.huawei.netassistant.ui.view.NetAssistantDialogManager;
import com.huawei.netassistant.ui.view.TrafficLineChartView;
import com.huawei.netassistant.ui.view.TrafficLineChartView.ChartSizeChangeListener;
import com.huawei.netassistant.ui.view.TrafficLineChartView.ClickPointListener;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.comm.widget.slideview.SlidingUpPanelLayout;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.FstPackageSetActivity;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.NetState;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.ShareCfg;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo.TrafficData;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficState;
import com.huawei.systemmanager.netassistant.ui.mainpage.MainListFragment;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class NetTrafficLineChartFragment extends Fragment implements OnClickListener {
    public static final String ARG_SUBID = "arg_msg";
    private static final int DATA_SIZE_MAX = 7;
    private static final int DATA_SIZE_MIN = 1;
    private static final int MAX_NUM = 5;
    private static final int MIN_NUM = 0;
    private static final int MSG_REFRESH_VIEW_ACTION = 201;
    private static final int MSG_REFRESH_VIEW_DELAY_TIME = 50;
    private static final int NUM_SIZE = 5;
    private static final String TAG = "NetTrafficLineChartFragment";
    private static final int TYPE_FIRST = 1;
    private static final int TYPE_LAST = 3;
    private static final int TYPE_MIDDLE = 2;
    boolean isNetAssistantEnable;
    ChartSizeChangeListener mChartSizeChangeListener = new ChartSizeChangeListener() {
        public void onChartSizeChanged(int xWidth) {
            ChartData lastData = NetTrafficLineChartFragment.this.mTrafficLineChartView.getLastData();
            if (lastData != null) {
                NetTrafficLineChartFragment.this.mDataTextView.udpateXWidth(xWidth, (int) lastData.getX());
                NetTrafficLineChartFragment.this.initLastData();
            }
        }
    };
    ClickPointListener mClickPointListener = new ClickPointListener() {
        public void onPointClick(ChartData data) {
            NetTrafficLineChartFragment.this.mDataTextView.setText(data.getText());
            NetTrafficLineChartFragment.this.mDataTextView.setXY(NetTrafficLineChartFragment.this.xWidth, (int) data.getX(), (int) data.getY(), 2);
            NetTrafficLineChartFragment.this.mDataTextView.setBackground(NetTrafficLineChartFragment.this.getResources().getDrawable(R.drawable.power_time_picker_battery_new));
        }

        public void onLastPointClick(ChartData data) {
            NetTrafficLineChartFragment.this.mDataTextView.setText(data.getText());
            NetTrafficLineChartFragment.this.mDataTextView.setXY(NetTrafficLineChartFragment.this.xWidth, (int) data.getX(), (int) data.getY(), 3);
            NetTrafficLineChartFragment.this.mDataTextView.setBackground(NetTrafficLineChartFragment.this.getResources().getDrawable(R.drawable.power_time_picker_right_battery_new));
        }

        public void onFstPointClick(ChartData data) {
            NetTrafficLineChartFragment.this.mDataTextView.setText(data.getText());
            NetTrafficLineChartFragment.this.mDataTextView.setXY(NetTrafficLineChartFragment.this.xWidth, (int) data.getX(), (int) data.getY(), 1);
            NetTrafficLineChartFragment.this.mDataTextView.setBackground(NetTrafficLineChartFragment.this.getResources().getDrawable(R.drawable.power_time_picker_left_battery_new));
        }
    };
    private ViewGroup mContainer;
    MyTextView mDataTextView;
    TextView mDataTypeTxt = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 201:
                    new PackageUpdateTask(HsmSubsciptionManager.getImsi(NetTrafficLineChartFragment.this.mSubId)).execute(new Void[0]);
                    return;
                default:
                    return;
            }
        }
    };
    HorizontalScrollView mHorizontalScrollView = null;
    TextView mNetLeftData;
    TextView mNetLeftDataUnit;
    TextView mNetLeftTitle;
    ImageView mNetModifyImg;
    Button mNetPackageSettingBtn;
    TextView mNetUnSetTip;
    TextView mNetUsedData;
    TextView mNetUsedDataUnit;
    View mNoticeTxt = null;
    private LinearLayout mPkgInfoView;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                HwLog.w(NetTrafficLineChartFragment.TAG, "receive message error , context == null or intent == null");
                return;
            }
            HwLog.i(NetTrafficLineChartFragment.TAG, "receive the message " + intent.getAction());
            if (ShareCfg.RECEIVE_REFRESH_TRAFFIC_ACTION.equals(intent.getAction())) {
                NetTrafficLineChartFragment.this.mHandler.removeMessages(201);
                NetTrafficLineChartFragment.this.mHandler.sendEmptyMessageDelayed(201, 50);
            }
        }
    };
    int mSubId;
    TrafficLineChartView mTrafficLineChartView = null;
    TextView mYNumtext0;
    TextView mYNumtext1;
    TextView mYNumtext2;
    TextView mYNumtext3;
    TextView mYNumtext4;
    TextView mYNumtext5;
    private int xWidth;

    private class PackageUpdateTask extends AsyncTask<Void, Void, ITrafficInfo> {
        String imsi;

        public PackageUpdateTask(String im) {
            this.imsi = im;
        }

        protected ITrafficInfo doInBackground(Void... params) {
            int yearMonth = DateUtil.getYearMonth(this.imsi);
            int trafficState = TrafficState.getCurrentTrafficState(this.imsi);
            HwLog.i(NetTrafficLineChartFragment.TAG, "Traffic state = " + trafficState);
            return ITrafficInfo.create(this.imsi, yearMonth, trafficState);
        }

        protected void onPostExecute(ITrafficInfo trafficInfo) {
            if (NetTrafficLineChartFragment.this.isAdded()) {
                int curType = trafficInfo.getType();
                if (302 == curType || 303 == curType) {
                    NetTrafficLineChartFragment.this.mNetModifyImg.setVisibility(8);
                } else {
                    NetTrafficLineChartFragment.this.mNetModifyImg.setVisibility(0);
                }
                if (UserHandle.myUserId() == 0) {
                    NetTrafficLineChartFragment.this.mNetPackageSettingBtn.setVisibility(0);
                    refreshPackageView(trafficInfo.getTrafficData());
                } else {
                    NetTrafficLineChartFragment.this.mPkgInfoView.setVisibility(8);
                    NetTrafficLineChartFragment.this.mDataTypeTxt.setVisibility(8);
                    NetTrafficLineChartFragment.this.mNetPackageSettingBtn.setVisibility(8);
                }
            }
        }

        private void refreshPackageView(TrafficData td) {
            if (NatSettingManager.hasPackageSet(this.imsi)) {
                NetTrafficLineChartFragment.this.mNetUnSetTip.setVisibility(8);
                NetTrafficLineChartFragment.this.mPkgInfoView.setVisibility(0);
                NetTrafficLineChartFragment.this.mDataTypeTxt.setVisibility(0);
                if (td.isOverData()) {
                    NetTrafficLineChartFragment.this.mNetLeftTitle.setText(R.string.net_over_title_text);
                } else {
                    NetTrafficLineChartFragment.this.mNetLeftTitle.setText(R.string.net_left_title_text);
                }
                NetTrafficLineChartFragment.this.mNetLeftData.setText(td.getTrafficLeftData());
                NetTrafficLineChartFragment.this.mNetLeftDataUnit.setText(td.getTrafficLeftUnit());
                NetTrafficLineChartFragment.this.mNetUsedData.setText(td.getTrafficUsedData());
                NetTrafficLineChartFragment.this.mNetUsedDataUnit.setText(td.getTrafficUsedUnit());
                NetTrafficLineChartFragment.this.mDataTypeTxt.setText(td.getTrafficStateMessage());
                NetTrafficLineChartFragment.this.mNetPackageSettingBtn.setText(NetTrafficLineChartFragment.this.getResources().getString(R.string.net_assistant_fix_package));
            } else {
                NetTrafficLineChartFragment.this.mNetUnSetTip.setText(R.string.net_assistant_no_package);
                NetTrafficLineChartFragment.this.mPkgInfoView.setVisibility(8);
                NetTrafficLineChartFragment.this.mDataTypeTxt.setVisibility(8);
                NetTrafficLineChartFragment.this.mNetPackageSettingBtn.setText(NetTrafficLineChartFragment.this.getResources().getString(R.string.net_assistant_set_package));
            }
            if (!NetTrafficLineChartFragment.this.isNetAssistantEnable) {
                NetTrafficLineChartFragment.this.mNetPackageSettingBtn.setVisibility(8);
            }
        }
    }

    private class TrafficDataTask extends AsyncTask<String, Void, List<ParcelableDailyTrafficItem>> {
        private TrafficDataTask() {
        }

        protected List<ParcelableDailyTrafficItem> doInBackground(String... params) {
            List<ParcelableDailyTrafficItem> items = null;
            try {
                items = NetAssistantManager.getMonthTrafficDailyDetailList(HsmSubsciptionManager.getImsi(NetTrafficLineChartFragment.this.mSubId)).subList(0, NetAssistantManager.getNetworkUsageDays(HsmSubsciptionManager.getImsi(NetTrafficLineChartFragment.this.mSubId)) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return items;
        }

        protected void onPostExecute(List<ParcelableDailyTrafficItem> result) {
            super.onPostExecute(result);
            if (result != null && NetTrafficLineChartFragment.this.isAdded() && result.size() > 0) {
                ParcelableDailyTrafficItem item = (ParcelableDailyTrafficItem) result.get(result.size() - 1);
                Context mContext = NetTrafficLineChartFragment.this.getActivity();
                if (mContext != null) {
                    if (mContext.getResources().getBoolean(R.bool.net_is_use_today)) {
                        item.mDate = mContext.getResources().getString(R.string.net_assistant_line_chart_today);
                    }
                    NetTrafficLineChartFragment.this.mTrafficLineChartView.setData(result);
                    NetTrafficLineChartFragment.this.mTrafficLineChartView.requestLayout();
                    NetTrafficLineChartFragment.this.mNoticeTxt.setVisibility(0);
                    NetTrafficLineChartFragment.this.mHorizontalScrollView.setVisibility(0);
                    NetTrafficLineChartFragment.this.mHorizontalScrollView.requestLayout();
                    NetTrafficLineChartFragment.this.initYNumText(result);
                }
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mSubId = bundle.getInt(ARG_SUBID);
            this.isNetAssistantEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.net_assistant_line_chart_fragment, container, false);
        this.mContainer = container;
        Activity activity = getActivity();
        if (HSMConst.isSupportSubfiled(null) && activity != null) {
            Intent intent = activity.getIntent();
            if (v instanceof SlidingUpPanelLayout) {
                HSMConst.setCfgForSlidingUp(intent, (SlidingUpPanelLayout) v);
            }
        }
        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mPkgInfoView = (LinearLayout) view.findViewById(R.id.package_info_view);
        this.mNetUnSetTip = (TextView) view.findViewById(R.id.net_unset_tip);
        this.mNetLeftData = (TextView) view.findViewById(R.id.net_left);
        this.mNetLeftTitle = (TextView) view.findViewById(R.id.net_left_title);
        this.mNetLeftDataUnit = (TextView) view.findViewById(R.id.net_left_unit);
        this.mNetUsedData = (TextView) view.findViewById(R.id.net_used);
        this.mNetUsedDataUnit = (TextView) view.findViewById(R.id.net_used_unit);
        this.mNetModifyImg = (ImageView) view.findViewById(R.id.net_modify);
        this.mTrafficLineChartView = (TrafficLineChartView) view.findViewById(R.id.linechart_view);
        this.mTrafficLineChartView.addClickPointListener(this.mClickPointListener);
        this.mTrafficLineChartView.addChartSizeChangeListener(this.mChartSizeChangeListener);
        this.mDataTextView = (MyTextView) view.findViewById(R.id.data_textview);
        this.mHorizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizon_scroll_view);
        this.mNoticeTxt = view.findViewById(R.id.txt_tips);
        this.mDataTypeTxt = (TextView) view.findViewById(R.id.txt_data_type);
        this.mNetPackageSettingBtn = (Button) view.findViewById(R.id.net_package_set_button);
        this.mNetModifyImg.setOnClickListener(this);
        this.mYNumtext0 = (TextView) view.findViewById(R.id.y_text_0);
        this.mYNumtext1 = (TextView) view.findViewById(R.id.y_text_1);
        this.mYNumtext2 = (TextView) view.findViewById(R.id.y_text_2);
        this.mYNumtext3 = (TextView) view.findViewById(R.id.y_text_3);
        this.mYNumtext4 = (TextView) view.findViewById(R.id.y_text_4);
        this.mYNumtext5 = (TextView) view.findViewById(R.id.y_text_5);
        this.mNetPackageSettingBtn.setOnClickListener(this);
        if (HsmSubsciptionManager.isMultiSubs()) {
            MainListFragment fragment = (MainListFragment) getChildFragmentManager().findFragmentById(R.id.list_items);
            if (fragment != null) {
                fragment.updateCardMessage(this.mSubId);
            } else {
                HwLog.w(TAG, "onViewCreated MainListFragment is null");
            }
        }
        initScreenOrientation(getResources().getConfiguration().orientation);
        GlobalContext.getContext().registerReceiver(this.mReceiver, new IntentFilter(ShareCfg.RECEIVE_REFRESH_TRAFFIC_ACTION), "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initScreenOrientation(newConfig.orientation);
    }

    public void onResume() {
        super.onResume();
        new PackageUpdateTask(HsmSubsciptionManager.getImsi(this.mSubId)).execute(new Void[0]);
        new TrafficDataTask().execute(new String[0]);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mTrafficLineChartView.removeClickPointListener();
        this.mTrafficLineChartView.removeChartSizeChangeListener();
        GlobalContext.getContext().unregisterReceiver(this.mReceiver);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    public void onClick(View v) {
        String imsi = HsmSubsciptionManager.getImsi(this.mSubId);
        if (v.getId() == this.mNetPackageSettingBtn.getId()) {
            if (!NatSettingManager.hasPackageSet(imsi)) {
                HsmStat.statE(Events.E_NETASSISTANT_PACKAGE_INIT);
                Intent intent = new Intent();
                intent.setClass(getActivity(), FstPackageSetActivity.class);
                intent.putExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI, imsi);
                startActivity(intent);
            } else if (NetState.isCurrentNetActive()) {
                HsmStat.statE(Events.E_NETASSISTANT_MMS_ADJUST);
                NetAssistantDialogManager.createPromptMessageDialog(getActivity(), imsi, this.mContainer);
                HsmStat.statE(90);
            } else {
                ToastUtils.toastLongMsg((int) R.string.msm_adjust_no_network);
            }
        } else if (v.getId() == this.mNetModifyImg.getId()) {
            HsmStat.statE(Events.E_NETASSISTANT_MANUAL_ADJUST);
            NetAssistantDialogManager.createManualAdjustSettingDialog(getActivity(), imsi, new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    new PackageUpdateTask(HsmSubsciptionManager.getImsi(NetTrafficLineChartFragment.this.mSubId)).execute(new Void[0]);
                }
            }, this.mContainer);
        }
    }

    private void initScreenOrientation(int orientation) {
    }

    private void initYNumText(List<ParcelableDailyTrafficItem> items) {
        long result = 0;
        int N = items.size();
        for (int i = 0; i < N; i++) {
            if (result < ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic) {
                result = ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic;
            }
        }
        float maxNum = (float) formatFileSize(result);
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
                        NetTrafficLineChartFragment.this.mHorizontalScrollView.scrollBy(NetTrafficLineChartFragment.this.mHorizontalScrollView.getChildAt(0).getWidth(), 0);
                    }
                }, 50);
            } else {
                this.mDataTextView.setXY(this.xWidth, (int) lastData.getX(), (int) lastData.getY(), 2);
                this.mDataTextView.setBackground(getResources().getDrawable(R.drawable.power_time_picker_battery_new));
            }
        }
    }

    public static long formatFileSize(long number) {
        long result = number;
        if (number > 900) {
            result = number / 1024;
        }
        if (result > 900) {
            result /= 1024;
        }
        if (result > 900) {
            result /= 1024;
        }
        if (result > 900) {
            result /= 1024;
        }
        if (result > 900) {
            return result / 1024;
        }
        return result;
    }
}
