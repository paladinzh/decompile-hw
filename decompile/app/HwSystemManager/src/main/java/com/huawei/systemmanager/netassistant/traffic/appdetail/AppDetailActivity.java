package com.huawei.systemmanager.netassistant.traffic.appdetail;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Switch;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.cardmanager.SimCardMethod;
import com.huawei.netassistant.ui.view.NoScrollViewPager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.slideview.SlidingUpPanelLayout;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.BackgroundInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.MobileDataInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.NoTrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.NormalTextInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.RoamingInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.TitleInfo;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.WifiInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AppDetailActivity extends HsmActivity implements OnClickListener {
    private static final String TAG = AppDetailActivity.class.getSimpleName();
    private AppDetailAdapter mAppDetailAdapter;
    private String mImsi;
    private View mLine;
    private ListView mListView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private int mSubId = -1;
    private SubTabWidget mSubTabWidget;
    private int mSubwidthport;
    private Map<String, BaseInfo> mTaskMap = new HashMap();
    private View mTrafficInfoView;
    private int mUid;
    private View mUpperView;

    private class HelpAsynTask extends AsyncTask<Void, Void, Void> {
        private List<BaseInfo> mHelpAsynTaskList;
        private String mHelpSyncTask;

        public HelpAsynTask(String task) {
            this.mHelpSyncTask = task;
        }

        protected Void doInBackground(Void... voids) {
            if (TextUtils.equals(this.mHelpSyncTask, "1")) {
                initList();
                return null;
            }
            BaseInfo info = (BaseInfo) AppDetailActivity.this.mTaskMap.get(this.mHelpSyncTask);
            if (info == null || !info.isEnable()) {
                return null;
            }
            info.setChecked(!info.isChecked());
            HsmStat.statE(Events.E_NETASSISTANT_APP_DETAIL_CHANGE);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            if (TextUtils.equals(this.mHelpSyncTask, "1")) {
                AppDetailActivity.this.mAppDetailAdapter.swapData(this.mHelpAsynTaskList);
            } else {
                AppDetailActivity.this.mAppDetailAdapter.notifyDataSetChanged();
            }
        }

        private void initList() {
            this.mHelpAsynTaskList = new LinkedList();
            this.mHelpAsynTaskList.add(new TitleInfo((int) R.string.net_assistant_traffic_list_app_new));
            if (!Utility.isWifiOnlyMode()) {
                this.mHelpAsynTaskList.add(new MobileDataInfo(AppDetailActivity.this.mUid));
            }
            if (NetAppUtils.isRemovableUid(AppDetailActivity.this.mUid)) {
                this.mHelpAsynTaskList.add(new WifiInfo(AppDetailActivity.this.mUid));
            }
            if (!Utility.isWifiOnlyMode()) {
                this.mHelpAsynTaskList.add(new BackgroundInfo(AppDetailActivity.this.mUid));
                this.mHelpAsynTaskList.add(new RoamingInfo(AppDetailActivity.this.mUid));
            }
            if (CustomizeManager.getInstance().isFeatureEnabled(30)) {
                this.mHelpAsynTaskList.add(new NoTrafficInfo(AppDetailActivity.this.mImsi, AppDetailActivity.this.mUid));
            }
            for (BaseInfo baseInfo : this.mHelpAsynTaskList) {
                if (!TextUtils.isEmpty(baseInfo.getTask())) {
                    AppDetailActivity.this.mTaskMap.put(baseInfo.getTask(), baseInfo);
                }
            }
            List<BaseInfo> packageName = AppDetailActivity.this.createPackageName();
            if (packageName.size() > 0) {
                HwLog.i(AppDetailActivity.TAG, "doInBackground , size is  " + packageName.size());
                this.mHelpAsynTaskList.add(new TitleInfo((int) R.string.net_assistant_more_application));
                this.mHelpAsynTaskList.addAll(packageName);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netassistant_app_detail);
        if (!initData()) {
            finish();
        }
    }

    protected void onStart() {
        super.onStart();
        new HelpAsynTask("1").execute(new Void[0]);
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    private boolean initData() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        this.mUid = intent.getIntExtra("uid", 0);
        HwLog.i(TAG, "UID is  " + this.mUid);
        this.mImsi = intent.getStringExtra(Constant.EXTRA_IMSI);
        if (TextUtils.isEmpty(this.mImsi)) {
            this.mImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        }
        this.mSubId = intent.getIntExtra(Constant.EXTRA_SUBID, -1);
        initViews(intent.getIntExtra(Constant.EXTRA_ACTIVITY_FROM, 0));
        initTitle(intent);
        return true;
    }

    private void initViews(int from) {
        Bundle bundle;
        SubTab subTab;
        boolean isLand;
        this.mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (HSMConst.isSupportSubfiled(null)) {
            HSMConst.setCfgForSlidingUp(getIntent(), this.mSlidingUpPanelLayout);
        }
        NoScrollViewPager viewPager = (NoScrollViewPager) findViewById(R.id.pager);
        this.mSubTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        boolean flag = TextUtils.isEmpty(this.mImsi);
        SubTabFragmentPagerAdapter subTabFragmentPagerAdapter = new SubTabFragmentPagerAdapter(this, viewPager, this.mSubTabWidget);
        if (!Utility.isWifiOnlyMode()) {
            boolean z;
            bundle = AppDetailLineChartFragment.newBundle(0, this.mUid, this.mImsi, from, this.mSubId);
            subTab = this.mSubTabWidget.newSubTab(getString(R.string.net_assistant_app_detal_title_mobile));
            subTab.setSubTabId(R.id.systemmanager_net_assistant_app_detal_title_mobile);
            AppDetailLineChartFragment frag1 = new AppDetailLineChartFragment();
            if (flag) {
                z = false;
            } else {
                z = true;
            }
            subTabFragmentPagerAdapter.addSubTab(subTab, frag1, bundle, z);
        }
        bundle = AppDetailLineChartFragment.newBundle(1, this.mUid, this.mImsi, from, this.mSubId);
        subTab = this.mSubTabWidget.newSubTab(getString(R.string.net_assistant_app_detal_title_wifi));
        subTab.setSubTabId(R.id.systemmanager_net_assistant_app_detal_title_wifi);
        subTabFragmentPagerAdapter.addSubTab(subTab, new AppDetailLineChartFragment(), bundle, flag);
        if (Utility.isWifiOnlyMode() && subTabFragmentPagerAdapter.getCount() == 1) {
            this.mSubTabWidget.setVisibility(8);
        }
        this.mLine = findViewById(R.id.line_land);
        this.mUpperView = findViewById(R.id.sliding_layout_upperview);
        this.mTrafficInfoView = findViewById(R.id.traffic_info_view);
        this.mListView = (ListView) findViewById(R.id.listview);
        this.mAppDetailAdapter = new AppDetailAdapter(this, this);
        this.mListView.setAdapter(this.mAppDetailAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                BaseInfo info = AppDetailActivity.this.mAppDetailAdapter.getItem(pos);
                if (info != null) {
                    new HelpAsynTask(info.getTask()).execute(new Void[0]);
                }
            }
        });
        if (2 == getResources().getConfiguration().orientation) {
            isLand = true;
        } else {
            isLand = false;
        }
        if (isLand) {
            initScreenOrientation(getResources().getConfiguration());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initScreenOrientation(newConfig);
    }

    private void initScreenOrientation(Configuration newConfig) {
        int i = 0;
        int i2 = -1;
        boolean isLand = 2 == newConfig.orientation;
        View view = this.mLine;
        if (!isLand) {
            i = 8;
        }
        view.setVisibility(i);
        LayoutParams sp = (LayoutParams) this.mSubTabWidget.mSubTabContentView.getLayoutParams();
        if (this.mSubwidthport == 0) {
            this.mSubwidthport = sp.width;
        }
        int subtabwidth = getResources().getDimensionPixelSize(R.dimen.net_assistant_subtab_width);
        if (!isLand) {
            subtabwidth = this.mSubwidthport;
        }
        sp.width = subtabwidth;
        this.mSubTabWidget.mSubTabContentView.setLayoutParams(sp);
        ViewGroup.LayoutParams up = this.mUpperView.getLayoutParams();
        if (isLand) {
            i = -1;
        } else {
            i = getResources().getDimensionPixelSize(R.dimen.net_assistant_app_detail_upperview_height);
        }
        up.height = i;
        LayoutParams tp = (LayoutParams) this.mTrafficInfoView.getLayoutParams();
        if (!isLand) {
            i2 = getResources().getDimensionPixelSize(R.dimen.net_assistant_app_detail_traffic_info_view_height);
        }
        tp.height = i2;
    }

    public void onClick(View v) {
        if (v instanceof Switch) {
            new HelpAsynTask((String) v.getTag()).execute(new Void[0]);
        }
    }

    private boolean isLogUploadAppPackageName(String pkgname) {
        return "com.huawei.logupload".equals(pkgname);
    }

    private List<BaseInfo> createPackageName() {
        List<BaseInfo> packageName = new LinkedList();
        String[] apps = getPackageManager().getPackagesForUid(this.mUid);
        if (apps == null || apps.length <= 1) {
            if (SpecialUid.isSystemAccount(this.mUid)) {
                String[] details = getResources().getStringArray(R.array.netassistant_app_detail_system_account);
                for (String normalTextInfo : details) {
                    packageName.add(new NormalTextInfo(normalTextInfo));
                }
            }
            return packageName;
        }
        for (String pkg : apps) {
            if (!isLogUploadAppPackageName(pkg)) {
                packageName.add(new NormalTextInfo(HsmPackageManager.getInstance().getLabel(pkg)));
            }
        }
        return packageName;
    }

    public static Intent getIntent(int type, String label, int uid) {
        return getIntent(type, label, uid, SimCardManager.getInstance().getPreferredDataSubscriberId());
    }

    public static Intent getIntent(int type, String label, int uid, String imsi) {
        Intent intent = new Intent();
        intent.putExtra(Constant.EXTRA_ACTIVITY_FROM, type);
        intent.putExtra(Constant.EXTRA_APP_LABEL, label);
        intent.putExtra("uid", uid);
        intent.putExtra(Constant.EXTRA_IMSI, imsi);
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_ID, label);
        HsmStat.statE((int) Events.E_NETASSISTANT_APP_DETAIL, statParam);
        return intent;
    }

    private void initTitle(Intent intent) {
        String str = intent.getStringExtra(Constant.EXTRA_APP_LABEL);
        if (TextUtils.isEmpty(str)) {
            String pkgName = intent.getStringExtra("package");
            if (!TextUtils.isEmpty(pkgName)) {
                str = HsmPackageManager.getInstance().getLabel(pkgName);
            }
        }
        if (!(TextUtils.isEmpty(str) || TextUtils.isEmpty(this.mImsi) || !SimCardManager.getInstance().isPhoneSupportDualCard() || SimCardMethod.getSimCardSlotNum(this, this.mImsi) + 1 == 0)) {
            str = getString(R.string.app_detail_title, new Object[]{str, Integer.valueOf(SimCardMethod.getSimCardSlotNum(this, this.mImsi) + 1)});
        }
        setTitle(str);
    }
}
