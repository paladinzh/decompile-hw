package com.huawei.permissionmanager.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.collect.Lists;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.DataChangeListener;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.HwPermissionInfo;
import com.huawei.permissionmanager.utils.PermissionCategory;
import com.huawei.permissionmanager.utils.PermissionMap;
import com.huawei.permissionmanager.utils.RecommendBaseItem;
import com.huawei.permissionmanager.utils.RecommendCfg;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionGroupItem;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItem;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItemBase;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionTagItem;
import com.huawei.permissionmanager.utils.SingleAppPermissions;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonSwitchController;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RecommendCallMethod;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendItem;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendParamException;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryInput;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryOutput;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.NormalStartupInfo;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;

public class SingleAppFragment extends Fragment implements OnClickListener, DataChangeListener {
    private static final /* synthetic */ int[] -com-huawei-permissionmanager-utils-PermissionCategorySwitchesValues = null;
    private static final int FIRST_POSITION_OF_LIST = 0;
    private static final int FIRST_TAG_POSITION = 0;
    private static final int HEADER_COUNT = 1;
    private static final String LOG_TAG = "SingleAppFragment";
    private static final long UPDATE_DIALOG_SHOW_TIME_MIN = 1500;
    private SingleAppAdapter mAdapter;
    OnCheckedChangeListener mAddviewSwitchClickListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            AddViewAppManager.getInstance(SingleAppFragment.this.mContext).setOpsMode(SingleAppFragment.this.mUid, SingleAppFragment.this.mPkgName, isChecked);
            new AsynctaskForUpdateCache().execute(new Void[0]);
        }
    };
    private CommonSwitchController mAddviewSwitcher;
    private CommonSwitchController mAppAwakedSwitcher;
    OnCheckedChangeListener mAwakedStartupSwitchClickListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (Utility.isOwnerUser()) {
                StartupDataMgrHelper.modifyAwakedStartupInfoStatus(SingleAppFragment.this.mContext, SingleAppFragment.this.mPkgName, isChecked);
            }
        }
    };
    private Builder mBuilder;
    private OnCheckedChangeListener mCfgSwitchListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PermissionItemBase base = (PermissionItemBase) buttonView.getTag(R.id.convertview_tag_item);
            if (base instanceof PermissionItem) {
                PermissionItem permissionItem = (PermissionItem) base;
                String permissionName = permissionItem.getPermissionName(SingleAppFragment.this.mContext);
                int permissionType = permissionItem.getPermissionType();
                HwLog.i(SingleAppFragment.LOG_TAG, "onCheckedChanged, name:" + permissionName + ", type:" + permissionType + ", checked:" + isChecked);
                if ((!Utility.isWifiOnlyMode() && !Utility.isDataOnlyMode()) || !ShareCfg.isPermissionFrozen(permissionType)) {
                    if (!isChecked && CommonFunctionUtil.isSmsPermission(permissionType) && CommonFunctionUtil.isDefaultSmsApp(SingleAppFragment.this.mContext, SingleAppFragment.this.mPkgName)) {
                        buttonView.setOnClickListener(null);
                        buttonView.setChecked(true);
                        buttonView.setOnCheckedChangeListener(SingleAppFragment.this.mCfgSwitchListener);
                        PermissionProhibitionDialogFragment.newInstance(permissionName, SingleAppFragment.this.mTitle, SingleAppFragment.this.mPkgName, SingleAppFragment.this.mUid, permissionType, 0).show(SingleAppFragment.this.getFragmentManager().beginTransaction(), "permission_pro_dialog");
                        return;
                    }
                    DBAdapter.setSinglePermissionAndSyncToSys(SingleAppFragment.this.mHwPermissionApps, SingleAppFragment.this.mContext, SingleAppFragment.this.mUid, SingleAppFragment.this.mPkgName, permissionType, isChecked ? 1 : 2, "single app set");
                    new AsynctaskForUpdateCache().execute(new Void[0]);
                    return;
                }
                return;
            }
            HwLog.w(SingleAppFragment.LOG_TAG, "unknown operation, set group item");
        }
    };
    private OnChildClickListener mChildClicker = new OnChildClickListener() {
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            SingleAppFragment.this.handlerClickItem((PermissionItemBase) v.getTag(R.id.convertview_tag_item));
            return false;
        }
    };
    private Context mContext = null;
    private boolean mDefaultMmsHedaderInited = false;
    private OnGroupClickListener mGroupClicker = new OnGroupClickListener() {
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            PermissionItemBase item = (PermissionItemBase) v.getTag(R.id.convertview_tag_item);
            if (item == null || (item instanceof PermissionGroupItem)) {
                return false;
            }
            SingleAppFragment.this.handlerClickItem(item);
            return false;
        }
    };
    private OnClickListener mGroupEndClicker = new OnClickListener() {
        public void onClick(View v) {
            SingleAppFragment.this.handlerClickItem((PermissionItemBase) v.getTag(R.id.convertview_tag_item));
        }
    };
    private boolean mHasRecommendPermission = false;
    private int mHeightOffSet = 0;
    private HwAppPermissions mHwPermissionApps = null;
    private View mLayout = null;
    private int mListViewPos = 0;
    private ExpandableListView mListview = null;
    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0) {
                SingleAppFragment.this.mListViewPos = SingleAppFragment.this.mListview.getFirstVisiblePosition();
                View topView = SingleAppFragment.this.mListview.getChildAt(0);
                if (topView != null) {
                    SingleAppFragment.this.mHeightOffSet = topView.getTop();
                }
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };
    private int mPermissionCfg = 0;
    private int mPermissionCode = 0;
    private ArrayList<PermissionItemBase> mPermissonItemList = new ArrayList();
    private String mPkgName = "";
    private List<RecommendItem> mRecommendList = new ArrayList();
    private SparseArray<RecommendBaseItem> mRecommendMap = new SparseArray();
    private List<HwPermissionInfo> mRequestPermissions;
    private OnCheckedChangeListener mStartupClicker = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (Utility.isOwnerUser()) {
                StartupDataMgrHelper.modifyNormalStartupInfoStatus(SingleAppFragment.this.mContext, SingleAppFragment.this.mPkgName, isChecked);
            }
        }
    };
    private CommonSwitchController mStartupSwitcher;
    private AppInfo mThisAppInfo = null;
    private String mTitle = "";
    private OnCheckedChangeListener mTrustAppClickListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SingleAppFragment.this.mTrusted = isChecked;
            AddViewAppManager.getInstance(SingleAppFragment.this.mContext).setOpsMode(SingleAppFragment.this.mUid, SingleAppFragment.this.mPkgName, isChecked);
            SingleAppAdapter.setGlobalSwitchStatus(SingleAppFragment.this.mTrusted);
            PermissionItem permissionItem;
            if (SingleAppFragment.this.mTrusted) {
                SingleAppFragment.this.mThisAppInfo.mTrust = 1;
                SingleAppFragment.this.mPermissionCfg = 0;
                for (PermissionItem item : SingleAppFragment.this.getTempPermList()) {
                    permissionItem = item;
                    SingleAppFragment.this.mHwPermissionApps.setSystemPermission(item.getPermissionType(), 1, false, "trust.");
                    SingleAppFragment.this.mPermissionCode = item.getPermissionType() | SingleAppFragment.this.mPermissionCode;
                }
            } else {
                SingleAppFragment.this.mThisAppInfo.mTrust = 0;
                ContentValues values = DBAdapter.getInitialConfig(SingleAppFragment.this.mContext, SingleAppFragment.this.mPkgName, SingleAppFragment.this.mUid, false);
                if (values == null) {
                    HwLog.w(SingleAppFragment.LOG_TAG, "onCheckedChanged values is null");
                    return;
                }
                Integer code = values.getAsInteger("permissionCode");
                Integer cfg = values.getAsInteger("permissionCfg");
                if (code == null || cfg == null) {
                    HwLog.w(SingleAppFragment.LOG_TAG, "onCheckedChanged code or cfg is null");
                    return;
                }
                SingleAppFragment.this.mPermissionCode = code.intValue();
                SingleAppFragment.this.mPermissionCfg = cfg.intValue();
                int compareCode = AppInfo.getComparePermissionCode(SingleAppFragment.this.mContext, SingleAppFragment.this.mPkgName);
                SingleAppFragment singleAppFragment = SingleAppFragment.this;
                singleAppFragment.mPermissionCode = singleAppFragment.mPermissionCode & compareCode;
                HwLog.i(SingleAppFragment.LOG_TAG, "set not trust, code:" + SingleAppFragment.this.mPermissionCode + ", cfg:" + SingleAppFragment.this.mPermissionCfg + ", mask:" + compareCode);
                for (PermissionItem item2 : SingleAppFragment.this.getTempPermList()) {
                    permissionItem = item2;
                    SingleAppFragment.this.mHwPermissionApps.setSystemPermission(item2.getPermissionType(), DBAdapter.getValue(item2.getPermissionType(), SingleAppFragment.this.mPermissionCode, SingleAppFragment.this.mPermissionCfg), false, "trust.");
                }
            }
            CommonFunctionUtil.uptateHsmPermissionsForTrust(SingleAppFragment.this.mContext, SingleAppFragment.this.mUid, SingleAppFragment.this.mPkgName, SingleAppFragment.this.mPermissionCode, SingleAppFragment.this.mPermissionCfg, SingleAppFragment.this.mTrusted);
            SingleAppFragment.this.updatePermissionItemList();
            SingleAppFragment.this.updateUI();
            buttonView.sendAccessibilityEvent(32768);
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_PKG;
            strArr[1] = SingleAppFragment.this.mPkgName;
            strArr[2] = HsmStatConst.PARAM_OP;
            strArr[3] = SingleAppFragment.this.mTrusted ? "1" : "0";
            HsmStat.statE(37, HsmStatConst.constructJsonParams(strArr));
        }
    };
    private CommonSwitchController mTrustSwitcher;
    private boolean mTrusted = false;
    private int mUid = 0;

    private class AsynctaskForUpdateCache extends AsyncTask<Void, Void, List<RecommendItem>> {
        private AsynctaskForUpdateCache() {
        }

        protected List<RecommendItem> doInBackground(Void... params) {
            DBAdapter.getInstance(SingleAppFragment.this.mContext).refreshAllCachedData("single app activity");
            List<RecommendItem> recommendList = getRecommendedList();
            SingleAppFragment.this.initGroupPermissionInfos();
            SingleAppFragment.this.mHwPermissionApps = HwAppPermissions.create(SingleAppFragment.this.mContext, SingleAppFragment.this.mPkgName);
            return recommendList;
        }

        private List<RecommendItem> getRecommendedList() {
            List<RecommendItem> recommendList = new ArrayList();
            try {
                return (List) RecommendQueryOutput.fromBundle(SingleAppFragment.this.mContext.getContentResolver().call(CloudProviderConst.CLOUD_AUTHORITY_URI, RecommendCallMethod.CALL_METHOD_QUERY_RECOMMEND, null, RecommendQueryInput.generateOnePkgMultiItemInput(6, SingleAppFragment.this.mPkgName, null))).get(SingleAppFragment.this.mPkgName);
            } catch (RecommendParamException e) {
                HwLog.e(SingleAppFragment.LOG_TAG, "error generateOnePkgMultiItemInput RecommendParamException");
                return recommendList;
            } catch (Exception e2) {
                HwLog.e(SingleAppFragment.LOG_TAG, "error generateOnePkgMultiItemInput Exception");
                return recommendList;
            }
        }

        protected void onPostExecute(List<RecommendItem> result) {
            super.onPostExecute(result);
            SingleAppFragment.this.convertRecommendInfo2Map(result);
            SingleAppFragment.this.mThisAppInfo = DBAdapter.getInstance(SingleAppFragment.this.mContext).getAppByPkgName(SingleAppFragment.this.mPkgName, "single app activity");
            if (SingleAppFragment.this.mThisAppInfo == null) {
                HwLog.e(SingleAppFragment.LOG_TAG, "The onResume mCurrentAppInfo is null");
                Activity activity = SingleAppFragment.this.getActivity();
                if (activity != null) {
                    activity.finish();
                }
                return;
            }
            SingleAppFragment.this.loadTheAppInfo();
            SingleAppFragment.this.setTrustFlag();
            SingleAppFragment.this.updatePermissionItemList();
            SingleAppFragment.this.hideProgressBar();
            SingleAppFragment.this.updateUI();
            SingleAppFragment.this.initGeneralPermission();
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-permissionmanager-utils-PermissionCategorySwitchesValues() {
        if (-com-huawei-permissionmanager-utils-PermissionCategorySwitchesValues != null) {
            return -com-huawei-permissionmanager-utils-PermissionCategorySwitchesValues;
        }
        int[] iArr = new int[PermissionCategory.values().length];
        try {
            iArr[PermissionCategory.BASIC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PermissionCategory.CALENDAR_GROUP.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PermissionCategory.CONTACT_GROUP.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PermissionCategory.GENERAL.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PermissionCategory.MOTION_HEALTH.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PermissionCategory.NONE.ordinal()] = 10;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PermissionCategory.OTHER.ordinal()] = 11;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PermissionCategory.PHONE_GROUP.ordinal()] = 5;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PermissionCategory.PRIVACY.ordinal()] = 6;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[PermissionCategory.PRIVATE.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[PermissionCategory.SECURITY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[PermissionCategory.SETTINGS.ordinal()] = 7;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[PermissionCategory.SMS_GROUP.ordinal()] = 8;
        } catch (NoSuchFieldError e13) {
        }
        -com-huawei-permissionmanager-utils-PermissionCategorySwitchesValues = iArr;
        return iArr;
    }

    protected void updatePermissionListForSpinner(int r1, int r2, com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItemBase r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.ui.SingleAppFragment.updatePermissionListForSpinner(int, int, com.huawei.permissionmanager.utils.SingleAppPermissionHelper$PermissionItemBase):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.ui.SingleAppFragment.updatePermissionListForSpinner(int, int, com.huawei.permissionmanager.utils.SingleAppPermissionHelper$PermissionItemBase):void");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        HwLog.v(LOG_TAG, "The SingleAppFragment will onCreate");
        this.mDefaultMmsHedaderInited = false;
        this.mContext = getActivity().getApplicationContext();
        initAppInfoFromIntent();
        if (GRuleManager.getInstance().shouldMonitor(getActivity(), MonitorScenario.SCENARIO_PERMISSION, this.mPkgName)) {
            clearNotificationIfNeeded();
            addIconAndTitle();
            DBAdapter.registerDataChangeListener(this);
            return;
        }
        HwLog.w(LOG_TAG, "should not monitor:" + this.mPkgName);
        getActivity().finish();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = inflater.inflate(R.layout.single_app_activity, null);
        this.mListview = (ExpandableListView) this.mLayout.findViewById(R.id.single_app_list);
        this.mListview.setOnChildClickListener(this.mChildClicker);
        this.mListview.setOnGroupClickListener(this.mGroupClicker);
        View headerLayout = LayoutInflater.from(this.mContext).inflate(R.layout.recommend_single_header, null);
        this.mListview.addHeaderView(headerLayout.findViewById(R.id.recommend_single_header_layout));
        this.mListview.setOnScrollListener(this.mOnScrollListener);
        initSetDefaultMmsHedader();
        this.mTrustSwitcher = new CommonSwitchController(headerLayout.findViewById(R.id.trust_app_layout), (Switch) headerLayout.findViewById(R.id.trust_single_app));
        LinearLayout startupLayout = (LinearLayout) headerLayout.findViewById(R.id.boot_startup_view);
        this.mStartupSwitcher = new CommonSwitchController(startupLayout, (Switch) startupLayout.findViewById(R.id.common_switch));
        this.mStartupSwitcher.setSupportMultiUser(false);
        LinearLayout appAwakedLayout = (LinearLayout) headerLayout.findViewById(R.id.app_awaked_view);
        this.mAppAwakedSwitcher = new CommonSwitchController(appAwakedLayout, (Switch) appAwakedLayout.findViewById(R.id.common_switch));
        this.mAppAwakedSwitcher.setSupportMultiUser(false);
        ((TextView) appAwakedLayout.findViewById(R.id.common_title)).setText(R.string.startupmgr_awaked_single_app_switch_title);
        LinearLayout addViewLayout = (LinearLayout) headerLayout.findViewById(R.id.addview_view);
        this.mAddviewSwitcher = new CommonSwitchController(addViewLayout, (Switch) addViewLayout.findViewById(R.id.common_switch));
        ((TextView) addViewLayout.findViewById(R.id.common_title)).setText(R.string.DropzoneAppTitle);
        return this.mLayout;
    }

    private void handlerClickItem(PermissionItemBase base) {
        if ((this.mTrustSwitcher == null || !this.mTrustSwitcher.isChecked()) && base != null && ((base instanceof PermissionItem) || (base instanceof PermissionGroupItem))) {
            CharSequence charSequence = null;
            String grpName = null;
            int permissionStatus = 0;
            int permissionType = -1;
            if (base instanceof PermissionItem) {
                PermissionItem permissionItem = (PermissionItem) base;
                charSequence = permissionItem.getPermissionName(this.mContext);
                HwLog.d(LOG_TAG, "permissionName: " + charSequence);
                permissionStatus = permissionItem.getStatus();
                permissionType = permissionItem.getPermissionType();
            } else if (base instanceof PermissionGroupItem) {
                PermissionGroupItem permissionItem2 = (PermissionGroupItem) base;
                charSequence = permissionItem2.getPermissionName(this.mContext);
                HwLog.d(LOG_TAG, "permissionName: " + charSequence);
                permissionStatus = permissionItem2.getStatus();
                grpName = permissionItem2.getGroupName();
            }
            if ((!Utility.isWifiOnlyMode() && !Utility.isDataOnlyMode()) || !ShareCfg.isPermissionFrozen(permissionType)) {
                int currentPosition = ShareLib.setDefaultSpinnerValue(permissionStatus);
                if ((CommonFunctionUtil.isSmsPermission(permissionType) || HwAppPermissions.isSmsGroup(r15)) && CommonFunctionUtil.isDefaultSmsApp(this.mContext, this.mPkgName)) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    PermissionProhibitionDialogFragment.newInstance(charSequence, this.mTitle, this.mPkgName, this.mUid, permissionType, currentPosition).show(ft, "permission_pro_dialog");
                    return;
                }
                OnDialogClickListener mOnDialogClickListener = new OnDialogClickListener(getActivity(), this.mPkgName, this.mUid, permissionType, base, currentPosition);
                CustomArrayAdapter<CharSequence> customArrayAdapter = new CustomArrayAdapter(this.mContext, this.mContext.getResources().getStringArray(R.array.permission_spinner_textarray));
                this.mBuilder = new Builder(getActivity());
                this.mBuilder.setTitle(charSequence).setIconAttribute(16843605).setNegativeButton(R.string.cancel, null).setSingleChoiceItems(customArrayAdapter, currentPosition, mOnDialogClickListener);
                this.mBuilder.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        HwLog.i(SingleAppFragment.LOG_TAG, "mBuilder onDismiss");
                        SingleAppFragment.this.mBuilder = null;
                        if (GRuleManager.getInstance().shouldMonitor(SingleAppFragment.this.mContext, MonitorScenario.SCENARIO_PERMISSION, SingleAppFragment.this.mPkgName)) {
                            new AsynctaskForUpdateCache().execute(new Void[0]);
                        }
                    }
                });
                this.mBuilder.show();
            }
        }
    }

    private void initSetDefaultMmsHedader() {
        if (!this.mDefaultMmsHedaderInited) {
            final String MMS_PACKAGE_NAME = CommonFunctionUtil.getHwOrginalSmsPackageName(this.mContext);
            if (!(MMS_PACKAGE_NAME == null || MMS_PACKAGE_NAME.equals(this.mPkgName) || !CommonFunctionUtil.isDefaultSmsApp(this.mContext, this.mPkgName))) {
                ViewStub viewStub = (ViewStub) this.mLayout.findViewById(R.id.viewstub_set_default_mms);
                View setDefaultMmsView = null;
                if (viewStub != null) {
                    setDefaultMmsView = viewStub.inflate();
                }
                if (setDefaultMmsView == null) {
                    HwLog.e(LOG_TAG, "error, can not find viewstub_set_default_mms");
                } else {
                    this.mDefaultMmsHedaderInited = true;
                    ((Button) this.mLayout.findViewById(R.id.default_mms_set_button)).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            CommonFunctionUtil.requestDefaultSmsAppActivity(SingleAppFragment.this.getActivity(), MMS_PACKAGE_NAME);
                        }
                    });
                }
            }
        }
    }

    public void onDestroy() {
        HwLog.v(LOG_TAG, "The SingleAppActivity will onDestroy");
        DBAdapter.unregisterDataChangeListener(this);
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        if (!GRuleManager.getInstance().shouldMonitor(getActivity(), MonitorScenario.SCENARIO_PERMISSION, this.mPkgName)) {
            return;
        }
        if (appRemoved()) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
            HwLog.w(LOG_TAG, "removed_app, finish.");
            return;
        }
        new AsynctaskForUpdateCache().execute(new Void[0]);
    }

    private boolean appRemoved() {
        boolean z = true;
        try {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm == null) {
                return false;
            }
            if (pm.getApplicationInfo(this.mPkgName, 0) != null) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            HwLog.w(LOG_TAG, "removed_app " + this.mPkgName);
            return true;
        } catch (Exception e2) {
            HwLog.w(LOG_TAG, "removed_app.", e2);
            return false;
        }
    }

    private void initGroupPermissionInfos() {
        try {
            PackageInfo pi = PackageManagerWrapper.getPackageInfo(this.mContext.getPackageManager(), this.mPkgName, 4096);
            if (pi != null) {
                this.mHwPermissionApps = new HwAppPermissions(this.mContext, pi);
            }
        } catch (NameNotFoundException e) {
            HwLog.e(LOG_TAG, "name not found:" + this.mPkgName);
        }
    }

    private void hideProgressBar() {
        View container = getView();
        if (container != null) {
            View progress = container.findViewById(R.id.progress_container);
            if (progress != null) {
                progress.setVisibility(8);
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (!GRuleManager.getInstance().shouldMonitor(getActivity(), MonitorScenario.SCENARIO_PERMISSION, this.mPkgName)) {
        }
    }

    private void initAppInfoFromIntent() {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            HwLog.e(LOG_TAG, "The intent is null");
            return;
        }
        this.mUid = intent.getIntExtra(ShareCfg.SINGLE_APP_UID, 0);
        this.mPkgName = intent.getStringExtra(ShareCfg.SINGLE_APP_PKGNAME);
        this.mTitle = intent.getStringExtra(ShareCfg.SINGLE_APP_LABEL);
        if (this.mUid == 0 || this.mTitle == null) {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(this.mPkgName);
            if (info != null) {
                this.mUid = info.mUid;
                this.mTitle = info.label();
            } else {
                this.mTitle = "";
            }
        }
        HwLog.d(LOG_TAG, "uid:" + this.mUid + " mPkgName:" + this.mPkgName + " mTitle:" + this.mTitle);
    }

    private void clearNotificationIfNeeded() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(1073741824);
        Intent intent = new Intent("com.huawei.permissionmanager.notification.private.delete");
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra("callerUid", this.mUid);
        this.mContext.sendBroadcast(intent);
    }

    private void loadTheAppInfo() {
        if (this.mThisAppInfo != null) {
            this.mTitle = this.mThisAppInfo.mAppLabel;
            this.mPermissionCode = this.mThisAppInfo.mPermissionCode;
            this.mPermissionCfg = this.mThisAppInfo.mPermissionCfg;
            this.mRequestPermissions = this.mThisAppInfo.mRequestPermissions;
        }
    }

    private void setTrustFlag() {
        boolean z;
        int i = 1;
        Context context = this.mContext;
        String str = this.mPkgName;
        int i2 = this.mUid;
        HwAppPermissions hwAppPermissions = this.mHwPermissionApps;
        if (1 == this.mThisAppInfo.mTrust) {
            z = true;
        } else {
            z = false;
        }
        this.mTrusted = CommonFunctionUtil.checkAppTrustStatus(context, str, i2, hwAppPermissions, z);
        AppInfo appInfo = this.mThisAppInfo;
        if (!this.mTrusted) {
            i = 0;
        }
        appInfo.mTrust = i;
        if (this.mTrustSwitcher != null) {
            this.mTrustSwitcher.updateCheckState(this.mTrusted);
            this.mTrustSwitcher.setOnCheckedChangeListener(this.mTrustAppClickListener);
        }
        SingleAppAdapter.setGlobalSwitchStatus(this.mTrusted);
    }

    private void addIconAndTitle() {
        getActivity().setTitle(this.mTitle);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(this.mTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    private void updatePermissionItemList() {
        this.mPermissonItemList.clear();
        initPermissionItems();
        List<PermissionItem> tempPermList = Lists.newArrayList();
        for (PermissionItemBase item : this.mPermissonItemList) {
            if (item instanceof PermissionItem) {
                tempPermList.add((PermissionItem) item);
            }
        }
        for (PermissionItem item2 : tempPermList) {
            PermissionItem pItem = item2;
            if (item2.getPermissionType() != 536870912) {
                int i;
                if (this.mHwPermissionApps.getSystemPermission(item2.getPermissionType())) {
                    i = 1;
                } else {
                    i = 2;
                }
                item2.setStatus(i);
                RecommendBaseItem recommendItem = (RecommendBaseItem) this.mRecommendMap.get(item2.getPermissionType());
                if (recommendItem != null && recommendItem.isCurrentPermissionHasRecommendStatus()) {
                    item2.changeToRecommend(recommendItem);
                }
            } else {
                item2.setStatus(AddViewAppManager.getInstance(this.mContext).getCurrentAppAddviewValue(this.mPkgName) ? 1 : 2);
            }
        }
        if (this.mPermissonItemList.size() == 0) {
            HwLog.e(LOG_TAG, "The mPermissonItemList is empty");
        }
    }

    private void updateUI() {
        initSetDefaultMmsHedader();
        View defaultAppHeader = this.mLayout.findViewById(R.id.default_app_header);
        if (defaultAppHeader != null) {
            if (CommonFunctionUtil.isDefaultSmsApp(getActivity(), this.mPkgName)) {
                defaultAppHeader.setVisibility(0);
            } else {
                defaultAppHeader.setVisibility(8);
            }
        }
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        } else {
            this.mAdapter = new SingleAppAdapter(this.mContext, this.mPermissonItemList, this.mThisAppInfo, this.mGroupEndClicker);
            this.mListview.setAdapter(this.mAdapter);
            for (Integer intValue : this.mAdapter.getExpandablePos()) {
                this.mListview.expandGroup(intValue.intValue());
            }
        }
        this.mListview.setSelectionFromTop(this.mListViewPos, this.mHeightOffSet);
    }

    private void initPermissionItems() {
        ArrayList<HwPermissionInfo> tempRequestWithAddView = new ArrayList();
        tempRequestWithAddView.addAll(this.mRequestPermissions);
        if (AddViewAppManager.getInstance(this.mContext).isCurrentAppShouldMonitor(this.mPkgName)) {
            tempRequestWithAddView.add(new HwPermissionInfo(ShareCfg.ADDVIEW_INDEX, 0, new String[]{""}, false));
        }
        sortPermissionItemsByIndex(tempRequestWithAddView);
        PermissionCategory lastCategory = PermissionCategory.NONE;
        SparseArray<PermissionItem> permissionItems = new SingleAppPermissions().getPermissionMaps();
        if (permissionItems.size() <= 0) {
            HwLog.e(LOG_TAG, "The appPermissionsMap is invalid!");
            return;
        }
        for (HwPermissionInfo info : tempRequestWithAddView) {
            PermissionItem permissionItem = (PermissionItem) permissionItems.get(info.mIndex);
            if (permissionItem != null) {
                PermissionCategory currentCategory = permissionItem.getCategory();
                if (lastCategory != currentCategory) {
                    this.mPermissonItemList.add(new PermissionTagItem(getIdByCategory(currentCategory)));
                    lastCategory = currentCategory;
                }
                if (permissionItem.getPermissionType() != 536870912) {
                    permissionItem.setListener(this.mCfgSwitchListener);
                } else {
                    permissionItem.setListener(this.mAddviewSwitchClickListener);
                }
                this.mPermissonItemList.add(permissionItem);
            }
        }
    }

    private void sortPermissionItemsByIndex(ArrayList<HwPermissionInfo> requestPermissions) {
        for (int j = 1; j < requestPermissions.size(); j++) {
            for (int i = 0; i < requestPermissions.size() - 1; i++) {
                if (((HwPermissionInfo) requestPermissions.get(i)).mIndex > ((HwPermissionInfo) requestPermissions.get(i + 1)).mIndex) {
                    HwPermissionInfo temp = (HwPermissionInfo) requestPermissions.get(i);
                    requestPermissions.set(i, (HwPermissionInfo) requestPermissions.get(i + 1));
                    requestPermissions.set(i + 1, temp);
                }
            }
        }
    }

    private int getIdByCategory(PermissionCategory category) {
        switch (-getcom-huawei-permissionmanager-utils-PermissionCategorySwitchesValues()[category.ordinal()]) {
            case 1:
                return R.string.BasicPermissionType;
            case 2:
                return R.string.permgrouplab_calendar;
            case 3:
                return R.string.permgrouplab_contacts;
            case 4:
                return R.string.google_basic_permission_management;
            case 5:
                return R.string.permgrouplab_phone;
            case 6:
                return R.string.PrivacyPermissionType;
            case 7:
                return R.string.SettingsPermissionType;
            case 8:
                return R.string.permgrouplab_sms;
            default:
                return R.string.other_permissions;
        }
    }

    protected void updatePermissionListForSMSGroup() {
        this.mThisAppInfo = DBAdapter.getInstance(this.mContext).getAppByPkgName(this.mPkgName, "updatePermissionListForSMSGroup");
        loadTheAppInfo();
        updatePermissionItemList();
        updateUI();
    }

    public void onClick(View v) {
        Switch view = (Switch) v.findViewById(R.id.common_switch);
        if (view != null) {
            view.performClick();
        }
    }

    private void initGeneralPermission() {
        try {
            initBootStartupManager();
            initAppAwakedManager();
            initAddviewManager();
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "initGeneralPermission Exception");
        }
    }

    private void initBootStartupManager() {
        if (this.mStartupSwitcher != null) {
            NormalStartupInfo startupInfo = StartupDataMgrHelper.querySingleNormalStartupInfo(this.mContext, this.mPkgName);
            if (startupInfo == null) {
                this.mStartupSwitcher.setVisibility(8);
                HwLog.d(LOG_TAG, "initBootStartupManager the current app is not bootstartup");
                return;
            }
            this.mStartupSwitcher.setVisibility(0);
            this.mStartupSwitcher.updateCheckState(startupInfo.getStatus());
            this.mStartupSwitcher.setOnCheckedChangeListener(this.mStartupClicker);
        }
    }

    private void initAppAwakedManager() {
        if (this.mAppAwakedSwitcher != null) {
            AwakedStartupInfo startupInfo = StartupDataMgrHelper.querySingleAwakedStartupInfo(this.mContext, this.mPkgName);
            if (startupInfo == null) {
                this.mAppAwakedSwitcher.setVisibility(8);
                HwLog.d(LOG_TAG, "initAppAwakedManager the current app is not app awaked");
                return;
            }
            this.mAppAwakedSwitcher.setVisibility(0);
            this.mAppAwakedSwitcher.updateCheckState(startupInfo.getStatus());
            this.mAppAwakedSwitcher.setOnCheckedChangeListener(this.mAwakedStartupSwitchClickListener);
        }
    }

    private void initAddviewManager() {
        if (this.mAddviewSwitcher != null) {
            this.mAddviewSwitcher.setVisibility(8);
        }
    }

    private void convertRecommendInfo2Map(List<RecommendItem> recommendList) {
        if (this.mRecommendList != null && this.mRecommendMap != null) {
            this.mRecommendList.clear();
            this.mRecommendMap.clear();
            this.mRecommendList.addAll(recommendList);
            SparseIntArray permissionIdMap = PermissionMap.getPermissionIdMap();
            if (permissionIdMap.size() != 0) {
                for (RecommendItem recommendItem : this.mRecommendList) {
                    int permissionType = permissionIdMap.get(recommendItem.getConfigItemId());
                    int recommendValue = RecommendCfg.getCfgFromRecommendVaule(recommendItem.getConfigType());
                    if (recommendValue != 0) {
                        this.mRecommendMap.put(permissionType, new RecommendBaseItem(true, recommendValue, recommendItem.getPercentage()));
                    }
                }
                if (this.mRecommendMap.size() > 0) {
                    this.mHasRecommendPermission = true;
                }
            }
        }
    }

    private List<PermissionItem> getTempPermList() {
        List<PermissionItem> tempList = Lists.newArrayList();
        for (PermissionItemBase item : this.mPermissonItemList) {
            if (item instanceof PermissionItem) {
                tempList.add((PermissionItem) item);
            }
        }
        return tempList;
    }

    public void onPermissionCfgChanged() {
        if (isResumed()) {
            new AsynctaskForUpdateCache().execute(new Void[0]);
        } else {
            HwLog.i(LOG_TAG, "view is background, don't update it.");
        }
    }
}
