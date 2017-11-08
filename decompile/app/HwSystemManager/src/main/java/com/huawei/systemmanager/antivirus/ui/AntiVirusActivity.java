package com.huawei.systemmanager.antivirus.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.android.app.ActionBarEx;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdConst;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import com.huawei.systemmanager.adblock.ui.model.AbsTxUrlsTask;
import com.huawei.systemmanager.adblock.ui.view.dlblock.DlBlockRecordListActivity;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.engine.CompetitorScan;
import com.huawei.systemmanager.antivirus.engine.IAntiVirusEngine;
import com.huawei.systemmanager.antivirus.logo.ILogoManager;
import com.huawei.systemmanager.antivirus.logo.LogoManagerFactory;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.AdvertiseEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.BaseEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.DlBlockEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.GlobalScanEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.NetQinEntity;
import com.huawei.systemmanager.antivirus.ui.AdvertiseAdapt.RiskPermEntity;
import com.huawei.systemmanager.antivirus.ui.view.GlobalScanProgressWrapper;
import com.huawei.systemmanager.antivirus.ui.view.IVirusScanProgressShow;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.misc.SystemManagerConst;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.RollingView;
import com.huawei.systemmanager.comm.widget.RollingView.OnNumberChangedListener;
import com.huawei.systemmanager.customize.FearureConfigration;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AntiVirusActivity extends HsmActivity implements OnNumberChangedListener {
    protected static final int MAX_ENGINE_INIT_PROGRESS = 6;
    protected static final int PERFORM_CLICK_DELAY_TIME = 100;
    static final int SCAN_MODE_GLOBAL = 1;
    static final int SCAN_MODE_QUICK = 0;
    private static final String TAG = "AntiVirusActivity";
    static final String sIS_GLOBAL_SCAN_FINISH = "is_global_scan_finish";
    protected int ZERO = 0;
    protected boolean isFirstScan = true;
    protected boolean isScanCancel = false;
    private OnItemClickListener mAdverItemCLicker = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Object item = view.getTag(R.id.convertview_tag_item);
            if (item != null) {
                if (item instanceof DlBlockEntity) {
                    AntiVirusActivity.this.startActivity(new Intent(AntiVirusActivity.this, DlBlockRecordListActivity.class));
                } else if (item instanceof GlobalScanEntity) {
                    AntiVirusActivity.this.startActivity(new Intent(AntiVirusActivity.this, AntiVirusGlobalScanActivity.class));
                } else {
                    if (item instanceof NetQinEntity) {
                        AntiVirusActivity.this.startActivity(new Intent(AntiVirusActivity.this, NetQinActivity.class));
                    }
                    Intent intent = new Intent(AntiVirusActivity.this, ScanResultListActivity.class);
                    if (item instanceof AdvertiseEntity) {
                        intent.putExtra(AntiVirusTools.RESULT_TYPE, AntiVirusTools.TYPE_ADVERTISE);
                        intent.putExtra(AntiVirusTools.RESULT_LIST, AntiVirusActivity.this.mScanResultsAdvertise);
                        HsmStat.statE(Events.E_ADVERTISE_ENTER);
                        if (!AntiVirusTools.isAbroad()) {
                            AdUtils.startAppListActivityForResult(AntiVirusActivity.this, AntiVirusTools.TYPE_ADVERTISE_CN);
                            return;
                        }
                    } else if (item instanceof RiskPermEntity) {
                        intent.putExtra(AntiVirusTools.RESULT_TYPE, AntiVirusTools.TYPE_RISKPERM);
                        intent.putExtra(AntiVirusTools.RESULT_LIST, AntiVirusActivity.this.mRiskPerm);
                        HsmStat.statE((int) Events.E_COMPETITOR_VIEW_LIST, HsmStatConst.PARAM_COUNT, String.valueOf(AntiVirusActivity.this.mRiskPerm.size()));
                    }
                    try {
                        AntiVirusActivity.this.startActivityForResult(intent, 10001);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    protected AdvertiseAdapt mAdvertiseAdapter = null;
    protected IAntiVirusEngine mAntiVirusEngine = null;
    private View mAntiVirusLandRef;
    protected int mAntiVirusStatus = -1;
    private RelativeLayout mAntivirusEnd;
    private RelativeLayout mBtnContainer;
    protected TextView mCircleExplain;
    protected RollingView mCircleNum;
    protected TextView mCircleUnit;
    protected ConnectivityManager mConnectivityManager = null;
    protected Context mContext = null;
    protected ArrayList<Map<String, Object>> mDangerItemList = null;
    protected AlertDialog mDialog = null;
    protected int mDlBlockSize = 0;
    protected Handler mHandler = new MyHandler(this);
    protected boolean mIsAutoStarted = false;
    protected boolean mIsBackFromList = false;
    protected boolean mIsCloudScan = false;
    protected boolean mIsCloudScanFail = false;
    private boolean mIsPercentTextSeparate;
    protected boolean mIsScanFinished = false;
    protected AtomicBoolean mIsScanning = new AtomicBoolean(false);
    private boolean mIsSupportOrientation;
    private View mLandDividerLine;
    private ViewGroup mLogoViewGroup;
    protected int mMoreItemCount;
    private FrameLayout mRadarLayout;
    RadarImageView mRiView;
    protected final ArrayList<ScanResultEntity> mRiskPerm = Lists.newArrayList();
    protected LinearLayout mScanAdvertiseLayout = null;
    protected Button mScanButton1 = null;
    protected LinearLayout mScanDangerItemsLayout = null;
    protected ListView mScanDangerItemsListView = null;
    protected TextView mScanInfo;
    protected ArrayList<Map<String, Object>> mScanItemList = null;
    private RelativeLayout mScanItemsInput;
    protected RelativeLayout mScanItemsLayout = null;
    protected ListView mScanItemsListView = null;
    protected int mScanMode = 0;
    protected IVirusScanProgressShow mScanProgressShow;
    protected ArrayList<ScanResultEntity> mScanResultsAdvertise = new ArrayList();
    protected Map<String, ScanResultEntity> mScanResultsBanUrl = new HashMap();
    protected ArrayList<ScanResultEntity> mScanResultsNotOfficial = new ArrayList();
    protected ArrayList<ScanResultEntity> mScanResultsRisk = new ArrayList();
    protected ArrayList<ScanResultEntity> mScanResultsVirus = new ArrayList();
    protected LinearLayout mScanSecurityItemsLayout = null;
    protected ListView mScanSecurityItemsListView = null;
    private View mScrollView;
    private LinearLayout mScrollviewContent;
    private LinearLayout mScrollviewContentAnim;
    protected ArrayList<Map<String, Object>> mSecurityItemList = null;
    protected OnClickListener mSettingClicker = new OnClickListener() {
        public void onClick(View v) {
            AntiVirusActivity.this.startNewActivity(v.getId());
        }
    };
    protected SharedPreferences mSharedPreferences;
    private View mUpperView;
    private RelativeLayout mVirusMain;
    private ScrollView sv;

    public class ItemsAdapter extends BaseAdapter {
        private Context mContext;
        private int mItemType;
        private ArrayList<Map<String, Object>> mList = new ArrayList();

        private class ViewHolder {
            Button checkInfoButton;
            TextView itemName;
            TextView itemResult;
            ImageView statusIcon;

            private ViewHolder() {
            }
        }

        public ItemsAdapter(Context context, int itemType) {
            this.mContext = context;
            this.mItemType = itemType;
        }

        public void setData(ArrayList<Map<String, Object>> list) {
            this.mList.clear();
            for (Map<String, Object> obj : list) {
                this.mList.add(obj);
            }
            notifyDataSetChanged();
        }

        public int getItemType() {
            return this.mItemType;
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int position) {
            return this.mList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(this.mContext).inflate(R.layout.virus_scan_item, parent, false);
                holder.statusIcon = (ImageView) convertView.findViewById(R.id.antivirus_scan_item_status_icon);
                holder.itemName = (TextView) convertView.findViewById(R.id.antivirus_scan_item_name);
                holder.itemResult = (TextView) convertView.findViewById(R.id.antivirus_scan_item_info);
                holder.checkInfoButton = (Button) convertView.findViewById(R.id.antivirus_check_item_info_button);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.itemName.setText((String) ((Map) this.mList.get(position)).get("name"));
            switch (this.mItemType) {
                case 0:
                    holder.itemResult.setText((String) ((Map) this.mList.get(position)).get("result"));
                    holder.itemResult.setVisibility(0);
                    holder.statusIcon.setVisibility(8);
                    holder.checkInfoButton.setVisibility(8);
                    break;
                case 1:
                    holder.itemResult.setVisibility(8);
                    if (((Integer) ((Map) this.mList.get(position)).get("icon")).intValue() == 0) {
                        holder.statusIcon.setImageResource(R.drawable.ic_public_arrow);
                        holder.statusIcon.setVisibility(0);
                        holder.checkInfoButton.setVisibility(8);
                        int resultType = ((Integer) ((Map) AntiVirusActivity.this.mDangerItemList.get(position)).get("type")).intValue();
                        Resources res = AntiVirusActivity.this.getResources();
                        if (res != null) {
                            if (AntiVirusTools.TYPE_VIRUS == resultType) {
                                holder.itemName.setTextColor(res.getColor(R.color.virus_scan_reselt_color_virus));
                            } else {
                                holder.itemName.setTextColor(res.getColor(R.color.emui_list_primary_text));
                            }
                        }
                        holder.checkInfoButton.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                ArrayList<ScanResultEntity> list;
                                int resultType = ((Integer) ((Map) AntiVirusActivity.this.mDangerItemList.get(position)).get("type")).intValue();
                                Intent intent = new Intent(AntiVirusActivity.this, ScanResultListActivity.class);
                                intent.putExtra(AntiVirusTools.RESULT_TYPE, resultType);
                                switch (resultType) {
                                    case 303:
                                        list = AntiVirusActivity.this.mScanResultsRisk;
                                        break;
                                    case 304:
                                        list = AntiVirusActivity.this.mScanResultsNotOfficial;
                                        break;
                                    case AntiVirusTools.TYPE_VIRUS /*305*/:
                                        list = AntiVirusActivity.this.mScanResultsVirus;
                                        break;
                                    default:
                                        list = new ArrayList();
                                        break;
                                }
                                intent.putExtra(AntiVirusTools.RESULT_LIST, list);
                                AntiVirusActivity.this.startActivityForResult(intent, 10001);
                            }
                        });
                        break;
                    }
                    holder.statusIcon.setImageResource(R.drawable.ic_scan_finish);
                    holder.statusIcon.setVisibility(0);
                    holder.checkInfoButton.setVisibility(8);
                    break;
            }
            return convertView;
        }
    }

    class MyHandler extends Handler {
        private WeakReference<AntiVirusActivity> mActivity = null;

        @SuppressLint({"HandlerLeak"})
        public MyHandler(AntiVirusActivity activity) {
            this.mActivity = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            AntiVirusActivity activity = (AntiVirusActivity) this.mActivity.get();
            if (activity == null) {
                super.handleMessage(msg);
                return;
            }
            switch (msg.what) {
                case 10:
                    activity.mHandler.removeMessages(19);
                    AntiVirusActivity.this.mScanProgressShow.play();
                    break;
                case 11:
                    ScanResultEntity result = msg.obj;
                    activity.handleScanningUiUpdate(activity.calculateProgress(msg.arg1), result);
                    AntiVirusActivity.this.mScanProgressShow.show(result.packageName);
                    break;
                case 12:
                case 15:
                case 30:
                    HwLog.i(AntiVirusActivity.TAG, "getScanMode() = " + AntiVirusActivity.this.getScanMode());
                    if (msg.what == 12 && AntiVirusActivity.this.getScanMode() == 0) {
                        HwLog.i(AntiVirusActivity.TAG, "AntiVirusTools.MSG_SCAN_FINISH");
                        AntiVirusActivity.this.sendBroadcast(new Intent(AntiVirusTools.ACTION_UPDATE_VIRUS_DATA), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                    }
                    AntiVirusActivity antiVirusActivity = AntiVirusActivity.this;
                    if (12 == msg.what) {
                        z = true;
                    }
                    antiVirusActivity.updateAdViews(z, AntiVirusActivity.this.mScanResultsBanUrl);
                    activity.updateScanFinishedState();
                    AntiVirusActivity.this.mAntiVirusEngine.onFreeMemory();
                    break;
                case 16:
                    activity.updateScanCancelState();
                    break;
                case 19:
                    int progress = msg.arg1;
                    activity.updateProgress(progress);
                    if (progress < 6) {
                        Message message = activity.mHandler.obtainMessage(19);
                        message.arg1 = msg.arg1 + 1;
                        activity.mHandler.sendMessageDelayed(message, 1000);
                        break;
                    }
                    break;
                case 23:
                    activity.handlerRiskPermItemUpdate((List) msg.obj);
                    break;
                case 24:
                    boolean doCloudScan = ((Boolean) msg.obj).booleanValue();
                    ILogoManager logoMgr = LogoManagerFactory.newInstance(AntiVirusActivity.this.mLogoViewGroup);
                    logoMgr.initView();
                    logoMgr.showLogo(doCloudScan);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public static class QuitRunnable implements Runnable {
        IAntiVirusEngine mEngine = null;

        public QuitRunnable(IAntiVirusEngine engine) {
            this.mEngine = engine;
        }

        public void run() {
            if (this.mEngine != null) {
                this.mEngine.onCancelScan();
                HwLog.i(AntiVirusActivity.TAG, "cancel scan successfully when back Antivirus activity");
            }
        }
    }

    private class ScanBtnClickListener implements OnClickListener {
        private ScanBtnClickListener() {
        }

        public void onClick(View view) {
            AntiVirusActivity.this.mSharedPreferences.edit().putBoolean(SystemManagerConst.ANTIVIRUS_PREFERENCE_FIRST_ENTRY, false).putLong(SystemManagerConst.ANTIVIRUS_SHARED_PREFENCE_TIME, System.currentTimeMillis()).commit();
            String buttonText = AntiVirusActivity.this.mScanButton1.getText().toString();
            Resources resource = AntiVirusActivity.this.getResources();
            String stopScanText = resource.getString(R.string.antivirus_button_stop_scan);
            String restartScanText = resource.getString(R.string.antivirus_button_rescan);
            String finishScanText = resource.getString(R.string.space_clean_complete);
            boolean isReScan = restartScanText.equals(buttonText);
            if (finishScanText.equals(buttonText)) {
                AntiVirusActivity.this.finishActivity();
            }
            String param;
            if (AntiVirusActivity.this.isFirstScan || isReScan) {
                if (!AntiVirusActivity.this.mIsScanning.get()) {
                    AntiVirusActivity.this.cleanData();
                    AntiVirusActivity.this.loadScanItems();
                    AntiVirusActivity.this.updateScanItemData();
                    AntiVirusActivity.this.updateScanStartState();
                    AntiVirusActivity.this.mRiView.startScan();
                    AntiVirusActivity.this.startScan();
                }
                if (AntiVirusActivity.this.isFirstScan) {
                    HsmStat.statE(82);
                    return;
                }
                param = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(AntiVirusActivity.this.getScanMode()));
                HsmStat.statE(83, param);
            } else if (stopScanText.equals(buttonText)) {
                AntiVirusActivity.this.mRiView.stopScan();
                AntiVirusActivity.this.mAntiVirusEngine.onCancelScan();
                param = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(AntiVirusActivity.this.getScanMode()));
                HsmStat.statE((int) Events.E_VIRUS_STOP_SCAN, param);
            }
        }
    }

    private class TxUrlsTask extends AbsTxUrlsTask {
        TxUrlsTask(Context context, boolean scanSuccess, Map<String, ScanResultEntity> scanResultsBanUrl) {
            super(context, scanSuccess, scanResultsBanUrl);
        }

        protected Integer[] doInBackground(Void... params) {
            AntiVirusActivity.this.mDlBlockSize = AdCheckUrlResult.getBlockRecordSize(AntiVirusActivity.this.getApplicationContext());
            return super.doInBackground(params);
        }

        protected void onTaskFinished(int totalAdCount, int checkedAdCount) {
            HwLog.i(AntiVirusActivity.TAG, "TxUrlsTask onTaskFinished");
            AntiVirusActivity.this.displayAdvertiseItemLayout(totalAdCount, checkedAdCount);
        }
    }

    protected void initScanModeData(View actionbarLayout) {
    }

    protected int getScanMode() {
        return this.mScanMode;
    }

    protected List<BaseEntity> getMoreItems(int totalAdCount, int checkedAdCount) {
        List<BaseEntity> list = Lists.newArrayList();
        if (!AntiVirusTools.isAbroad() && totalAdCount > 0) {
            list.add(new AdvertiseEntity(totalAdCount, checkedAdCount));
        }
        if (this.mRiskPerm.size() > 0) {
            list.add(new RiskPermEntity(this.mRiskPerm.size()));
        }
        if (hasNetQinTemp()) {
            list.add(new NetQinEntity());
        }
        if (AdUtils.isDlCheckEnable(getApplicationContext()) && this.mDlBlockSize > 0) {
            list.add(new DlBlockEntity());
        }
        this.mMoreItemCount = list.size();
        return list;
    }

    private boolean hasNetQinTemp() {
        return new File(new File(Environment.getDataDirectory(), "system"), "netqin.tmp").exists();
    }

    protected void setVirusScanProgressWrapper(ViewGroup viewGroup) {
        this.mScanProgressShow = new GlobalScanProgressWrapper(viewGroup);
        this.mScanProgressShow.initView();
    }

    private void startNewActivity(int viewId) {
        if (viewId == 16908296) {
            try {
                startActivity(new Intent(this, AntiVirusSettingsActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initAntiVirusEngine() {
        this.mAntiVirusEngine = AntiVirusEngineFactory.newInstance();
        this.mAntiVirusEngine.onInit(getApplicationContext());
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.isOwnerUser()) {
            this.mIsSupportOrientation = Utility.isSupportOrientation();
            this.mIsPercentTextSeparate = getResources().getBoolean(R.bool.is_percent_text_separate);
            setContentView(R.layout.virus_main);
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.systemmanager_module_title_virus);
            ActionBarEx.setEndIcon(actionBar, true, getDrawable(R.drawable.settings_menu_btn_selector), this.mSettingClicker);
            ActionBarEx.setEndContentDescription(getActionBar(), getString(R.string.net_assistant_setting_title));
            actionBar.show();
            this.mContext = getApplicationContext();
            initAntiVirusEngine();
            this.mScanItemList = new ArrayList();
            this.mDangerItemList = new ArrayList();
            this.mSecurityItemList = new ArrayList();
            this.mSharedPreferences = getSharedPreferences("systemmanagerscan", 0);
            this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
            loadScanItems();
            initViews();
            this.isFirstScan = true;
            Intent intent = getIntent();
            if (!(intent == null || intent.getBooleanExtra(FearureConfigration.AUTO_START, false))) {
                performClick();
            }
            return;
        }
        finish();
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    protected void onNewIntent(Intent intent) {
        this.mIsAutoStarted = false;
        super.onNewIntent(intent);
    }

    private LinearLayout initDangerItemsLayout() {
        if (this.mScanDangerItemsLayout != null) {
            return this.mScanDangerItemsLayout;
        }
        ViewStub stub = (ViewStub) findViewById(R.id.danger_items);
        if (stub != null) {
            stub.inflate();
        }
        this.mScanDangerItemsLayout = (LinearLayout) findViewById(R.id.antivirus_danger_items_layout);
        this.mScanDangerItemsListView = (ListView) findViewById(R.id.antivirus_danger_listview);
        this.mScanDangerItemsListView.setOverScrollMode(2);
        ItemsAdapter dangerAdapter = new ItemsAdapter(this.mContext, 1);
        dangerAdapter.setData(this.mDangerItemList);
        this.mScanDangerItemsListView.setAdapter(dangerAdapter);
        this.mScanDangerItemsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                if (1 == ((ItemsAdapter) arg0.getAdapter()).getItemType() && ((Integer) ((Map) AntiVirusActivity.this.mDangerItemList.get(position)).get("icon")).intValue() == 0) {
                    ArrayList<ScanResultEntity> list;
                    int resultType = ((Integer) ((Map) AntiVirusActivity.this.mDangerItemList.get(position)).get("type")).intValue();
                    Intent intent = new Intent(AntiVirusActivity.this, ScanResultListActivity.class);
                    intent.putExtra(AntiVirusTools.RESULT_TYPE, resultType);
                    switch (resultType) {
                        case 303:
                            list = AntiVirusActivity.this.mScanResultsRisk;
                            break;
                        case 304:
                            list = AntiVirusActivity.this.mScanResultsNotOfficial;
                            break;
                        case AntiVirusTools.TYPE_VIRUS /*305*/:
                            list = AntiVirusActivity.this.mScanResultsVirus;
                            break;
                        default:
                            list = new ArrayList();
                            break;
                    }
                    intent.putExtra(AntiVirusTools.RESULT_LIST, list);
                    AntiVirusActivity.this.startActivityForResult(intent, 10001);
                }
            }
        });
        return this.mScanDangerItemsLayout;
    }

    private void initListView() {
        this.mScanItemsLayout = (RelativeLayout) findViewById(R.id.antivirus_scan_items_layout);
        this.mScanItemsListView = (ListView) findViewById(R.id.antivirus_scan_listview);
        this.mRiView = (RadarImageView) findViewById(R.id.radar_view);
        ItemsAdapter scanAdapter = new ItemsAdapter(this.mContext, 0);
        scanAdapter.setData(this.mScanItemList);
        this.mScanItemsListView.setAdapter(scanAdapter);
        this.mScanItemsListView.setClickable(false);
        this.mScanItemsListView.setEnabled(false);
        checkAndShowLeaveDay();
    }

    private void checkAndShowLeaveDay() {
        this.mCircleNum = (RollingView) findViewById(R.id.antiVirus_totalNum_textview);
        this.mCircleNum.setOnNumberChangedListener(this);
        this.mCircleUnit = (TextView) findViewById(R.id.antiVirus_unit_textview);
        if (this.mIsPercentTextSeparate) {
            this.mCircleUnit.setVisibility(0);
        } else {
            this.mCircleUnit.setVisibility(8);
        }
        this.mCircleExplain = (TextView) findViewById(R.id.antiVirus_type_textview);
        this.mCircleNum.setNumberImmediately(0, this.mIsPercentTextSeparate);
        this.mCircleUnit.setText("%");
    }

    private void initViews() {
        this.mRadarLayout = (FrameLayout) findViewById(R.id.radar_layout);
        this.mBtnContainer = (RelativeLayout) findViewById(R.id.btn_container);
        this.mScrollView = (RelativeLayout) findViewById(R.id.scrollview);
        this.mUpperView = findViewById(R.id.sliding_layout_upperview);
        this.mAntiVirusLandRef = findViewById(R.id.antiVirus_land_ref);
        this.mLandDividerLine = findViewById(R.id.land_divider_line);
        this.mLogoViewGroup = (ViewGroup) findViewById(R.id.logo_container);
        this.mAntivirusEnd = (RelativeLayout) findViewById(R.id.antiVirus_end);
        this.mScanItemsInput = (RelativeLayout) findViewById(R.id.antivirus_scan_items_layout);
        this.mVirusMain = (RelativeLayout) findViewById(R.id.virus_main);
        this.mScrollviewContent = (LinearLayout) findViewById(R.id.scrollview_content);
        this.mScrollviewContentAnim = (LinearLayout) findViewById(R.id.scrollview_content_anim);
        this.sv = (ScrollView) findViewById(R.id.scrollview_fun);
        this.sv.setOverScrollMode(2);
        if (this.mIsSupportOrientation) {
            initScreenOrientation();
        }
        initListView();
        this.mScanButton1 = (Button) findViewById(R.id.scan);
        this.mScanButton1.setOnClickListener(new ScanBtnClickListener());
        setVirusScanProgressWrapper((ViewGroup) findViewById(R.id.progress_container));
    }

    private void loadScanItems() {
        this.mScanItemList.clear();
        loadScanItemData(0, AntiVirusTools.TYPE_VIRUS, R.string.software_type_virus, this.mContext.getString(R.string.virus_waiting_scan));
        loadScanItemData(1, 303, R.string.software_type_risk, null);
        loadScanItemData(2, 304, R.string.software_type_not_official, null);
        if (isNetworkConnected() && AntiVirusTools.isCloudScanSwitchOn(this.mContext)) {
            loadScanItemData(3, AntiVirusTools.TYPE_CLOUD_SCAN, R.string.virus_cloud_scan_title, null);
        }
    }

    private void loadScanItemData(int index, int type, int name, String result) {
        Map<String, Object> map = new HashMap();
        map.put("name", getResources().getString(name));
        map.put("type", Integer.valueOf(type));
        map.put("result", result);
        this.mScanItemList.add(index, map);
    }

    private void updateScanStartState() {
        this.mScanButton1.setText(R.string.antivirus_button_stop_scan);
        this.mScanItemsLayout.setVisibility(0);
        if (this.mScanDangerItemsLayout != null) {
            this.mScanDangerItemsLayout.setVisibility(8);
        }
        if (this.mScanSecurityItemsLayout != null) {
            this.mScanSecurityItemsLayout.setVisibility(8);
        }
        if (this.mScanAdvertiseLayout != null) {
            this.mScanAdvertiseLayout.setVisibility(8);
        }
        updateProgress(this.ZERO);
        this.mCircleNum.setTextColor(getResources().getColor(R.color.emui_list_primary_text));
        this.mCircleUnit.setTextColor(getResources().getColor(R.color.emui_list_primary_text));
        this.mCircleUnit.setText("%");
        if (this.mIsPercentTextSeparate) {
            this.mCircleUnit.setVisibility(0);
        } else {
            this.mCircleUnit.setVisibility(8);
        }
        this.mCircleExplain.setText(getString(R.string.antivus_scanning));
        int count = this.mScanItemList.size();
        for (int i = 0; i < count; i++) {
            ((Map) this.mScanItemList.get(i)).put("result", this.mContext.getString(R.string.virus_waiting_scan));
        }
        updateScanItemData();
    }

    private void updateScanItemListInfo(ScanResultEntity result) {
        recordBanUrlEntity(result);
        int index = 0;
        int appNumber = 0;
        switch (result.type) {
            case 303:
                this.mScanResultsRisk.add(result);
                index = 1;
                appNumber = this.mScanResultsRisk.size();
                break;
            case 304:
                this.mScanResultsNotOfficial.add(result);
                index = 2;
                appNumber = this.mScanResultsNotOfficial.size();
                break;
            case AntiVirusTools.TYPE_VIRUS /*305*/:
                this.mScanResultsVirus.add(result);
                index = 0;
                appNumber = this.mScanResultsVirus.size();
                break;
            case AntiVirusTools.TYPE_AD_BLOCK /*306*/:
                HwLog.i(TAG, "updateScanItemListInfo: Find an AD app, " + result.packageName);
                break;
            case AntiVirusTools.TYPE_ADVERTISE /*307*/:
                this.mScanResultsAdvertise.add(result);
                return;
            default:
                return;
        }
        ((Map) this.mScanItemList.get(index)).put("result", String.format(getResources().getString(R.string.scan_result_number1), new Object[]{Integer.valueOf(appNumber)}));
        updateScanItemData();
    }

    private void displayDangerItemLayout(int virusNum, int riskNum, int notOfficialNum) {
        initDangerItemsLayout();
        this.mDangerItemList.clear();
        this.mScanDangerItemsLayout.setVisibility(8);
        if (virusNum != 0) {
            loadDangerItemData(virusNum, AntiVirusTools.TYPE_VIRUS, R.plurals.scan_result_danger_virus_format, 0);
        }
        if (riskNum != 0) {
            loadDangerItemData(riskNum, 303, R.plurals.scan_result_danger_risk, 0);
        }
        if (notOfficialNum != 0) {
            loadDangerItemData(notOfficialNum, 304, R.plurals.scan_result_danger_not_official, 0);
        }
        ((ItemsAdapter) this.mScanDangerItemsListView.getAdapter()).setData(this.mDangerItemList);
    }

    private void loadDangerItemData(int num, int type, int stringId, int iconId) {
        Map<String, Object> map = new HashMap();
        String text = getResources().getQuantityString(stringId, num, new Object[]{Integer.valueOf(num)});
        map.put("type", Integer.valueOf(type));
        map.put("icon", Integer.valueOf(iconId));
        map.put("name", text);
        this.mDangerItemList.add(map);
    }

    private void updateProgress(int progress) {
        if (this.mCircleNum != null) {
            if (progress == 0) {
                this.mCircleNum.setNumberImmediately(0, this.mIsPercentTextSeparate);
            }
            this.mCircleNum.setNumberImmediately(progress, this.mIsPercentTextSeparate);
            this.mCircleUnit.setText("%");
        }
    }

    private void updateScanItemData() {
        ((ItemsAdapter) this.mScanItemsListView.getAdapter()).setData(this.mScanItemList);
    }

    private void updateScanCancelState() {
        this.isScanCancel = true;
        this.mScanButton1.setText(R.string.antivirus_button_rescan);
        updateScanCancelUI();
        updateScanItemData();
    }

    private void updateScanCancelUI() {
        int riskNum = this.mScanResultsRisk.size();
        int virusNum = this.mScanResultsVirus.size();
        int total = (riskNum + virusNum) + this.mScanResultsNotOfficial.size();
        if (total != 0) {
            if (riskNum != 0) {
                this.mCircleNum.setTextColor(-29670);
                this.mCircleUnit.setTextColor(-29670);
                this.mCircleExplain.setTextColor(-1);
            }
            if (virusNum != 0) {
                this.mCircleNum.setTextColor(-50641);
                this.mCircleUnit.setTextColor(-50641);
                this.mCircleExplain.setTextColor(-1);
            }
            this.mCircleNum.setNumberImmediately(total, this.mIsPercentTextSeparate);
            this.mCircleUnit.setText(getString(R.string.antivus_unit));
            this.mCircleExplain.setText(getString(R.string.antivirus_scan_uncomplete01));
        }
        if (this.mScanItemList != null) {
            int count = this.mScanItemList.size();
            for (int i = 0; i < count; i++) {
                ((Map) this.mScanItemList.get(i)).put("result", "");
            }
        }
        this.mScanProgressShow.cancel();
    }

    private void updateScanFinishedState() {
        this.mCircleNum.setNumberImmediately(100, this.mIsPercentTextSeparate);
        int virusNum = this.mScanResultsVirus.size();
        int riskNum = this.mScanResultsRisk.size();
        int notOfficialNum = this.mScanResultsNotOfficial.size();
        if ((virusNum + riskNum) + notOfficialNum != 0) {
            this.mAntiVirusStatus = 1;
        } else {
            this.mAntiVirusStatus = 0;
        }
        if (!this.isScanCancel) {
            updateStateInfo(virusNum, riskNum, notOfficialNum);
        }
    }

    private void checkAndShowNetworkDialog() {
        if (!this.mIsCloudScan || isNetworkConnected()) {
            this.mScanButton1.setText(R.string.space_clean_complete);
            return;
        }
        this.mScanButton1.setText(R.string.space_clean_complete);
        this.mIsCloudScanFail = true;
        showNoNetworkDialog();
    }

    private void updateStateInfo(int virusNum, int riskNum, int notOfficialNum) {
        int totalNum = (virusNum + riskNum) + notOfficialNum;
        if (this.mScanAdvertiseLayout != null) {
            this.mScanAdvertiseLayout.setVisibility(8);
        }
        if (totalNum != 0) {
            if (this.mScanDangerItemsLayout != null) {
                this.mScanDangerItemsLayout.setVisibility(8);
            }
            animationStepOne(true);
            displayDangerItemLayout(virusNum, riskNum, notOfficialNum);
        } else {
            if (this.mScanDangerItemsLayout != null) {
                this.mScanDangerItemsLayout.setVisibility(8);
            }
            animationStepOne(false);
        }
        if (AntiVirusTools.isAbroad()) {
            displayAdvertiseItemLayout(0, 0);
        }
    }

    private void displayAdvertiseItemLayout(int totalAdCount, int checkedAdCount) {
        int i = 0;
        HwLog.i(TAG, "displayAdvertiseItemLayout total=" + totalAdCount + ", checked=" + checkedAdCount);
        ensureAdverList();
        List<BaseEntity> list = getMoreItems(totalAdCount, checkedAdCount);
        if (this.mAdvertiseAdapter != null) {
            this.mAdvertiseAdapter.setData(list);
        }
        if (this.mScanAdvertiseLayout != null) {
            LinearLayout linearLayout = this.mScanAdvertiseLayout;
            if (this.mMoreItemCount <= 0 || !this.mIsScanFinished) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
    }

    private void ensureAdverList() {
        if (this.mAdvertiseAdapter == null) {
            ViewStub stub = (ViewStub) findViewById(R.id.advertise_items);
            if (stub != null) {
                stub.inflate();
            }
            this.mScanAdvertiseLayout = (LinearLayout) findViewById(R.id.antivirus_advertise_items_layout);
            ListView adverList = (ListView) findViewById(R.id.antivirus_advertise_listview);
            this.mAdvertiseAdapter = new AdvertiseAdapt(this);
            if (adverList != null) {
                adverList.setOverScrollMode(2);
                adverList.setAdapter(this.mAdvertiseAdapter);
                adverList.setOnItemClickListener(this.mAdverItemCLicker);
            }
        }
    }

    protected void animationStepOne(boolean isDanger) {
        final FrameLayout layout1 = (FrameLayout) findViewById(R.id.radar_layout);
        final LinearLayout layout2 = (LinearLayout) findViewById(R.id.progress_container);
        final RelativeLayout layout3 = (RelativeLayout) findViewById(R.id.antiVirus_end);
        final RelativeLayout btnContainer = (RelativeLayout) findViewById(R.id.btn_container);
        final ViewGroup scanListview = (ViewGroup) findViewById(R.id.antivirus_scan_listview);
        Animation animation_hide = AnimationUtils.loadAnimation(this, R.anim.virus_hide);
        final Animation animation_hide2 = AnimationUtils.loadAnimation(this, R.anim.virus_hide);
        final Animation animation_out = AnimationUtils.loadAnimation(this, R.anim.virus_out_bottom);
        final LayoutAnimationController anima = new LayoutAnimationController(animation_out, 0.2f);
        anima.setOrder(1);
        ImageView scanStatus = (ImageView) findViewById(R.id.antiVirus_end_icon);
        TextView scanStatusTitle = (TextView) findViewById(R.id.antiVirus_end_text_title);
        TextView scanStatusTime = (TextView) findViewById(R.id.antiVirus_end_text_time);
        Date todayTime = new Date();
        scanStatusTime.setText(getString(R.string.virus_end_texttime, new Object[]{DateUtils.formatDateTime(this.mContext, todayTime.getTime(), 17)}));
        if (isDanger) {
            scanStatus.setImageResource(R.drawable.img_danger);
            scanStatusTitle.setText(R.string.virus_end_texttitle_danger);
        } else {
            scanStatus.setImageResource(R.drawable.img_safe);
            scanStatusTitle.setText(R.string.virus_end_texttitle);
        }
        final boolean z = isDanger;
        animation_hide.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation a) {
                btnContainer.setAnimation(animation_out);
                scanListview.setLayoutAnimation(anima);
                scanListview.startLayoutAnimation();
                layout2.setAnimation(animation_hide2);
            }

            public void onAnimationRepeat(Animation a) {
            }

            public void onAnimationEnd(Animation a) {
                layout1.setVisibility(8);
                layout2.setVisibility(8);
                layout3.setVisibility(0);
                AntiVirusActivity.this.mScanItemsLayout.setVisibility(8);
                btnContainer.setVisibility(8);
                AntiVirusActivity.this.animationStepTwo(z);
            }
        });
        layout1.startAnimation(animation_hide);
    }

    protected void animationStepTwo(boolean isDanger) {
        RelativeLayout layout4 = (RelativeLayout) findViewById(R.id.antiVirus_end_icon_layout);
        final RelativeLayout layout5 = (RelativeLayout) findViewById(R.id.antiVirus_end_text);
        final RelativeLayout btnContainer = (RelativeLayout) findViewById(R.id.btn_container);
        Animation animation_show = AnimationUtils.loadAnimation(this, R.anim.virus_show);
        final Animation animation_in = AnimationUtils.loadAnimation(this, R.anim.virus_in_bottom);
        final LayoutAnimationController anima = new LayoutAnimationController(animation_in, 0.1f);
        this.mScanButton1.setText(R.string.space_clean_complete);
        final boolean z = isDanger;
        animation_show.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation a) {
                layout5.setAnimation(animation_in);
                if (AntiVirusActivity.this.mMoreItemCount > 0) {
                    AntiVirusActivity.this.mScanAdvertiseLayout.setVisibility(0);
                }
                if (z) {
                    AntiVirusActivity.this.mScanDangerItemsLayout.setVisibility(0);
                }
                AntiVirusActivity.this.mIsScanFinished = true;
                btnContainer.setVisibility(0);
                btnContainer.setAnimation(animation_in);
                AntiVirusActivity.this.mScrollviewContentAnim.setLayoutAnimation(anima);
                AntiVirusActivity.this.mScrollviewContentAnim.startLayoutAnimation();
            }

            public void onAnimationRepeat(Animation a) {
            }

            public void onAnimationEnd(Animation a) {
                AntiVirusActivity.this.checkAndShowNetworkDialog();
            }
        });
        layout4.startAnimation(animation_show);
    }

    protected void startScan() {
        this.isFirstScan = false;
        this.mIsCloudScanFail = false;
        AntiVirusTools.updateTimerRemindTimeStamp(this.mContext);
        new Thread(new Runnable() {
            public void run() {
                AntiVirusActivity.this.mIsScanning.set(true);
                Message msg = AntiVirusActivity.this.mHandler.obtainMessage(19);
                msg.arg1 = AntiVirusActivity.this.ZERO;
                AntiVirusActivity.this.mHandler.sendMessage(msg);
                AntiVirusActivity.this.mHandler.obtainMessage(23, AntiVirusActivity.this.getCompetitors()).sendToTarget();
                boolean z = AntiVirusActivity.this.isNetworkConnected() ? AntiVirusActivity.this.mIsCloudScan : false;
                Message logoMsg = AntiVirusActivity.this.mHandler.obtainMessage(24);
                logoMsg.obj = Boolean.valueOf(z);
                AntiVirusActivity.this.mHandler.sendMessage(logoMsg);
                switch (AntiVirusActivity.this.getScanMode()) {
                    case 0:
                        AntiVirusActivity.this.mAntiVirusEngine.onStartQuickScan(AntiVirusActivity.this.mContext, AntiVirusActivity.this.mHandler, z);
                        break;
                    case 1:
                        AntiVirusActivity.this.mAntiVirusEngine.onStartGlobalScan(AntiVirusActivity.this.mContext, AntiVirusActivity.this.mHandler, z);
                        break;
                }
                AntiVirusActivity.this.mIsScanning.set(false);
            }
        }, "AntiVirus_Scan").start();
    }

    private void handleScanningUiUpdate(int progress, ScanResultEntity result) {
        this.isScanCancel = false;
        updateProgress(progress);
        updateScanItemListInfo(result);
    }

    private void handlerRiskPermItemUpdate(List<ScanResultEntity> riskpermItems) {
        this.mRiskPerm.clear();
        if (!HsmCollections.isEmpty(riskpermItems)) {
            this.mRiskPerm.addAll(riskpermItems);
        }
    }

    private int calculateProgress(int progress) {
        return ((progress * 94) / 100) + 6;
    }

    private void cleanData() {
        this.mScanResultsVirus.clear();
        this.mScanResultsRisk.clear();
        this.mScanResultsNotOfficial.clear();
        this.mScanResultsAdvertise.clear();
        this.mScanResultsBanUrl.clear();
    }

    private void performClick() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (AntiVirusActivity.this.mScanButton1 != null) {
                    AntiVirusActivity.this.mScanButton1.performClick();
                }
            }
        }, 100);
    }

    protected void onDestroy() {
        cleanData();
        dimissDialog();
        if (this.mScanProgressShow != null) {
            this.mScanProgressShow.cancel();
        }
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (!(intent == null || !intent.getBooleanExtra(FearureConfigration.AUTO_START, false) || this.mIsAutoStarted)) {
            this.mIsAutoStarted = true;
            performClick();
        }
        this.mScanMode = mGetScanMode();
        this.mIsCloudScan = isCloudScan();
        if (this.isScanCancel || this.mIsBackFromList) {
            this.mIsBackFromList = false;
            if (!this.isScanCancel) {
                return;
            }
            return;
        }
        updateAdViews(false, null);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onRestart() {
        super.onRestart();
        int virusNum = this.mScanResultsVirus.size();
        int riskNum = this.mScanResultsRisk.size();
        int notOfficialNum = this.mScanResultsNotOfficial.size();
        int totalNum = (virusNum + riskNum) + notOfficialNum;
        if (this.mIsScanFinished) {
            displayDangerItemLayout(virusNum, riskNum, notOfficialNum);
            updateAdViews(false, null);
            if (totalNum > 0) {
                this.mScanDangerItemsLayout.setVisibility(0);
            } else {
                this.mScanDangerItemsLayout.setVisibility(8);
            }
            if (this.mMoreItemCount > 0) {
                this.mScanAdvertiseLayout.setVisibility(0);
            }
        }
    }

    protected void onStop() {
        super.onStop();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finishActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        finishActivity();
    }

    public void finishActivity() {
        boolean z = true;
        if (this.mIsScanning.get()) {
            new Thread(new QuitRunnable(this.mAntiVirusEngine), "QuitAntivirusThread").start();
        }
        AntiVirusTools.setAntiVirusStatus(this.mContext, this.mAntiVirusStatus);
        if (getScanMode() == 1) {
            String finishScanText = getResources().getString(R.string.space_clean_complete);
            Intent data = new Intent();
            boolean isFinish = finishScanText.equals(this.mScanButton1.getText().toString());
            String str = sIS_GLOBAL_SCAN_FINISH;
            if (!isFinish) {
                z = this.mIsCloudScanFail;
            }
            data.putExtra(str, z);
            setResult(-1, data);
        }
        dimissDialog();
        finish();
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        item.getItemId();
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        HwLog.d(TAG, "onActivityResult, requestCode:" + requestCode);
        this.mIsBackFromList = true;
        if (requestCode == AntiVirusTools.REQUEST_CODE_GLOBAL_SCAN) {
            if (data != null && data.getBooleanExtra(sIS_GLOBAL_SCAN_FINISH, false)) {
                finishActivity();
            }
        } else if (requestCode == AntiVirusTools.TYPE_ADVERTISE_CN) {
            if (data != null) {
                displayAdvertiseItemLayout(data.getIntExtra("totalCount", 0), data.getIntExtra(AdConst.BUNDLE_CHECKED_COUNT, 0));
            }
        } else if (data != null && requestCode == 10001) {
            int resultType = data.getIntExtra(AntiVirusTools.RESULT_TYPE, -1);
            ArrayList<ScanResultEntity> resultList = (ArrayList) data.getExtra(AntiVirusTools.RESULT_LIST);
            if (resultType != -1 && resultList != null) {
                switch (resultType) {
                    case 303:
                        changeListData(this.mScanResultsRisk, resultList);
                        break;
                    case 304:
                        changeListData(this.mScanResultsNotOfficial, resultList);
                        break;
                    case AntiVirusTools.TYPE_VIRUS /*305*/:
                        changeListData(this.mScanResultsVirus, resultList);
                        break;
                    case AntiVirusTools.TYPE_ADVERTISE /*307*/:
                        changeListData(this.mScanResultsAdvertise, resultList);
                        break;
                    case AntiVirusTools.TYPE_RISKPERM /*308*/:
                        changeListData(this.mRiskPerm, resultList);
                        break;
                }
            }
        }
    }

    private void changeListData(ArrayList<ScanResultEntity> original, ArrayList<ScanResultEntity> newList) {
        original.clear();
        for (ScanResultEntity entity : newList) {
            original.add(entity);
        }
    }

    public void onNumberChanged(int number) {
    }

    protected boolean useHsmActivityHelper() {
        return true;
    }

    private List<ScanResultEntity> getCompetitors() {
        List<HsmPkgInfo> competitors = CompetitorScan.getCompetitors(getApplicationContext());
        List<ScanResultEntity> results = Lists.newArrayList();
        for (HsmPkgInfo info : competitors) {
            ScanResultEntity entity = ScanResultEntity.createRiskPermItem(info);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mIsSupportOrientation) {
            initScreenOrientation();
        }
    }

    private void initScreenOrientation() {
        boolean isLand = getResources().getConfiguration().orientation == 2;
        Resources res = getResources();
        int displayMetricsWidth = Utility.getDisplayMetricsWidth();
        if (this.mAntivirusEnd == null) {
            this.mAntivirusEnd = (RelativeLayout) findViewById(R.id.antiVirus_end);
        } else {
            ViewGroup parent = (ViewGroup) this.mAntivirusEnd.getParent();
            if (parent != null) {
                parent.removeView(this.mAntivirusEnd);
            }
        }
        if (isLand) {
            this.mVirusMain.addView(this.mAntivirusEnd);
            LayoutParams endUp = (LayoutParams) this.mAntivirusEnd.getLayoutParams();
            endUp.width = (displayMetricsWidth * 5) / 12;
            endUp.height = -1;
            this.mAntivirusEnd.setLayoutParams(endUp);
            this.mLandDividerLine.setVisibility(0);
        } else {
            this.mScrollviewContent.addView(this.mAntivirusEnd, 0);
            LinearLayout.LayoutParams endUp2 = (LinearLayout.LayoutParams) this.mAntivirusEnd.getLayoutParams();
            endUp2.width = -1;
            endUp2.height = res.getDimensionPixelSize(R.dimen.virus_endUp_port_height);
            this.mAntivirusEnd.setLayoutParams(endUp2);
            this.mLandDividerLine.setVisibility(8);
        }
        LayoutParams inUp = (LayoutParams) this.mUpperView.getLayoutParams();
        inUp.width = isLand ? (displayMetricsWidth * 5) / 12 : -1;
        inUp.height = isLand ? -1 : res.getDimensionPixelSize(R.dimen.virus_inUp_port_height);
        this.mUpperView.setLayoutParams(inUp);
        LayoutParams inDown = (LayoutParams) this.mScanItemsInput.getLayoutParams();
        inDown.addRule(3, isLand ? -1 : R.id.sliding_layout_upperview);
        inDown.addRule(17, isLand ? R.id.sliding_layout_upperview : -1);
        this.mScanItemsInput.setLayoutParams(inDown);
        LayoutParams endDown = (LayoutParams) this.mScrollView.getLayoutParams();
        endDown.addRule(17, isLand ? R.id.antiVirus_end : -1);
        this.mScrollView.setLayoutParams(endDown);
        ((LayoutParams) this.mAntiVirusLandRef.getLayoutParams()).width = isLand ? (displayMetricsWidth * 5) / 12 : 0;
        LayoutParams btnLand = (LayoutParams) this.mBtnContainer.getLayoutParams();
        btnLand.addRule(17, isLand ? R.id.antiVirus_land_ref : -1);
        this.mBtnContainer.setLayoutParams(btnLand);
        LayoutParams logoLand = (LayoutParams) this.mLogoViewGroup.getLayoutParams();
        logoLand.addRule(17, isLand ? R.id.antiVirus_land_ref : -1);
        this.mLogoViewGroup.setLayoutParams(logoLand);
        this.sv.smoothScrollTo(0, 0);
    }

    private boolean isNetworkConnected() {
        if (this.mConnectivityManager.getActiveNetworkInfo() != null) {
            return this.mConnectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    private void showNoNetworkDialog() {
        if (this.mDialog == null) {
            Builder alertDialog = new Builder(this);
            alertDialog.setTitle(R.string.virus_set_network_dlg_title);
            alertDialog.setMessage(R.string.virus_set_network_dlg_message);
            alertDialog.setNegativeButton(getResources().getString(R.string.I_know), null);
            alertDialog.setPositiveButton(R.string.virus_set_network, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                    intent.addCategory("android.intent.category.DEFAULT");
                    try {
                        AntiVirusActivity.this.startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            try {
                this.mDialog = alertDialog.show();
            } catch (Exception e) {
                HwLog.e(TAG, "Dialog show error");
            }
        }
    }

    private void dimissDialog() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    private boolean isCloudScan() {
        return AntiVirusTools.isCloudScanSwitchOn(this.mContext);
    }

    private int mGetScanMode() {
        return AntiVirusTools.getScanMode(this.mContext);
    }

    private void recordBanUrlEntity(ScanResultEntity result) {
        if (301 != result.type && !result.mBanUrls.isEmpty()) {
            this.mScanResultsBanUrl.put(result.getPackageName(), result);
        }
    }

    private void updateAdViews(boolean scanSuccess, Map<String, ScanResultEntity> scanResultsBanUrl) {
        HwLog.i(TAG, "updateAdViews");
        new TxUrlsTask(this.mContext, scanSuccess, scanResultsBanUrl).executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    protected ArrayList<ScanResultEntity> getScanResultEntity() {
        ArrayList<ScanResultEntity> list = new ArrayList();
        list.addAll(this.mScanResultsRisk);
        list.addAll(this.mScanResultsVirus);
        list.addAll(this.mScanResultsNotOfficial);
        list.addAll(this.mScanResultsAdvertise);
        return list;
    }
}
