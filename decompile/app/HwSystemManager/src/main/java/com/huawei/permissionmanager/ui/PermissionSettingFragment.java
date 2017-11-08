package com.huawei.permissionmanager.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.DataChangeListener;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.model.PermissionApps;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.PermissionMap;
import com.huawei.permissionmanager.utils.RecommendBaseItem;
import com.huawei.permissionmanager.utils.RecommendCfg;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RecommendCallMethod;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendItem;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendParamException;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryInput;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryOutput;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PermissionSettingFragment extends Fragment implements DataChangeListener {
    protected static final int HEADER_REFRESH = 2;
    protected static final int MSG_REFRESH_LIST = 1;
    private String LOG_TAG = "PermissionSettingFragment";
    private boolean isDefaultMmsHedaderInited = false;
    protected ListView mAppsListview = null;
    private OnCheckedChangeListener mCfgSwitchListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            AppInfoWrapperForSinglePermission info = (AppInfoWrapperForSinglePermission) buttonView.getTag(R.id.convertview_tag_item);
            if (info == null) {
                HwLog.w(PermissionSettingFragment.this.LOG_TAG, "onCheckedChanged, get tag null.");
                return;
            }
            String permissionName = PermissionSettingFragment.this.mPermissionObject.getmPermissionNames();
            int type = info.mPermissionType;
            String pkg = info.mPkgName;
            HwLog.i(PermissionSettingFragment.this.LOG_TAG, "onCheck " + isChecked + ", pkg:" + pkg + ", type:" + type);
            if (1048576 == type) {
                HsmStat.statPerssmisonSettingFragmentAction(isChecked ? "a" : "f", type, pkg);
            }
            if (!isChecked && CommonFunctionUtil.isSmsPermission(type) && CommonFunctionUtil.isDefaultSmsApp(PermissionSettingFragment.this.mContext, pkg)) {
                buttonView.setOnClickListener(null);
                buttonView.setChecked(true);
                buttonView.setOnCheckedChangeListener(PermissionSettingFragment.this.mCfgSwitchListener);
                PermissionProhibitionDialogFragment.newInstance(permissionName, info.mLabel, pkg, info.mUid, type, 0).show(PermissionSettingFragment.this.getFragmentManager().beginTransaction(), "permission_pro_dialog");
                return;
            }
            info.mPermissionStatus = isChecked ? 1 : 2;
            if (MPermissionUtil.isClassAType(type) || MPermissionUtil.isClassBType(type)) {
                HwAppPermissions.create(PermissionSettingFragment.this.mContext, pkg).setSystemPermission(type, isChecked ? 1 : 2, false, "user setting");
            } else if (MPermissionUtil.isClassEType(type)) {
                DBAdapter.setSinglePermission(PermissionSettingFragment.this.mContext, info.mUid, pkg, type, isChecked ? 1 : 2);
            } else {
                HwLog.w(PermissionSettingFragment.this.LOG_TAG, "onCheckedChanged,unexceped type:" + type);
            }
            new AsynctaskUpdateDB().execute(new Void[0]);
        }
    };
    protected Context mContext;
    private RelativeLayout mEmpty = null;
    private DefListener mExternalStorageListener = new DefListener() {
        public void onExternalChanged(String[] packages, boolean available) {
            if (packages != null && packages.length != 0) {
                new AsynctaskUpdateDB().execute(new Void[0]);
            }
        }
    };
    protected String mGroupName = null;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    PermissionSettingFragment.this.mPermissionSettingAdapter.notifyDataSetChanged();
                    return;
                case 2:
                    PermissionSettingFragment.this.mHeader.setText(PermissionSettingFragment.this.mPermissionObject.getmPermissionDescriptions(PermissionSettingFragment.this.getForbidCount()));
                    return;
                default:
                    return;
            }
        }
    };
    protected TextView mHeader = null;
    private LinearLayout mHeaderView = null;
    private int mHeightOffSet = 0;
    protected ImageView mImageView = null;
    private View mLayout = null;
    protected int mListViewPos = 0;
    OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            onListItemClick(position - PermissionSettingFragment.this.mAppsListview.getHeaderViewsCount());
        }

        private void onListItemClick(int itemPosition) {
            if (itemPosition < PermissionSettingFragment.this.mPermissonAppsList.size()) {
                AppInfoWrapperForSinglePermission appInfoWrapper = (AppInfoWrapperForSinglePermission) PermissionSettingFragment.this.mPermissonAppsList.get(itemPosition);
                String mPkgName = appInfoWrapper.mPkgName;
                int mUid = appInfoWrapper.mUid;
                int permissionType = appInfoWrapper.mPermissionType;
                if (PermissionSettingFragment.this.mContext != null) {
                    String permissionName = PermissionSettingFragment.this.mPermissionObject.getmPermissionNames();
                    int currentPosition = ShareLib.setDefaultSpinnerValue(appInfoWrapper.mPermissionStatus);
                    HwLog.d(PermissionSettingFragment.this.LOG_TAG, " itemPosition:" + itemPosition + " mPkgName:" + mPkgName + " mUid:" + mUid + " permissionType:" + permissionType);
                    if (CommonFunctionUtil.isSmsPermission(permissionType) && CommonFunctionUtil.isDefaultSmsApp(PermissionSettingFragment.this.mContext, mPkgName)) {
                        PermissionProhibitionDialogFragment.newInstance(permissionName, appInfoWrapper.mLabel, mPkgName, mUid, permissionType, currentPosition).show(PermissionSettingFragment.this.getFragmentManager().beginTransaction(), "permission_pro_dialog");
                    }
                }
            }
        }
    };
    private PermissionApps mPermissionApps = null;
    protected Permission mPermissionObject = null;
    protected BaseAdapter mPermissionSettingAdapter = null;
    protected PermissionTableManager mPermissionTableManager = null;
    protected int mPermissionType;
    protected ArrayList<AppInfoWrapperForSinglePermission> mPermissonAppsList = null;
    private boolean mRecommendAvailable = false;
    private ProgressDialog mWaitingDialog = null;
    protected TextView tvNoneAppInfo;

    private class AsynctaskForChange extends AsyncTask<Void, Void, Map<String, RecommendBaseItem>> {
        private AsynctaskForChange() {
        }

        protected Map<String, RecommendBaseItem> doInBackground(Void... params) {
            if (PermissionSettingFragment.this.mGroupName != null) {
                PermissionSettingFragment.this.mPermissionApps = PermissionApps.create(PermissionSettingFragment.this.mContext, PermissionSettingFragment.this.mGroupName);
            }
            return PermissionSettingFragment.this.getRecommendInfoForCurrentPermission();
        }

        protected void onPostExecute(Map<String, RecommendBaseItem> result) {
            PermissionSettingFragment.this.updatePermissionAppsList(result, PermissionSettingFragment.this.mPermissionApps);
            PermissionSettingFragment.this.mPermissionSettingAdapter.notifyDataSetChanged();
            PermissionSettingFragment.this.invalidateOptionsMenu();
        }
    }

    private class AsynctaskUpdateDB extends AsyncTask<Void, Void, Map<String, RecommendBaseItem>> {
        private AsynctaskUpdateDB() {
        }

        protected Map<String, RecommendBaseItem> doInBackground(Void... params) {
            DBAdapter.getInstance(PermissionSettingFragment.this.mContext).refreshAllCachedData("permission setting activity " + this);
            if (PermissionSettingFragment.this.mGroupName != null) {
                PermissionSettingFragment.this.mPermissionApps = PermissionApps.create(PermissionSettingFragment.this.mContext, PermissionSettingFragment.this.mGroupName);
            }
            return PermissionSettingFragment.this.getRecommendInfoForCurrentPermission();
        }

        protected void onPostExecute(Map<String, RecommendBaseItem> result) {
            super.onPostExecute(result);
            PermissionSettingFragment.this.updatePermissionAppsList(result, PermissionSettingFragment.this.mPermissionApps);
            PermissionSettingFragment.this.updateUI();
            PermissionSettingFragment.this.onHeaderChanged();
            PermissionSettingFragment.this.invalidateOptionsMenu();
        }
    }

    private class AsynctaskUpdatePermissionCfg extends AsyncTask<Integer, Void, Map<String, RecommendBaseItem>> {
        private AsynctaskUpdatePermissionCfg() {
        }

        protected Map<String, RecommendBaseItem> doInBackground(Integer... operations) {
            Integer operation = operations[0];
            DBAdapter dbAdapter = DBAdapter.getInstance(PermissionSettingFragment.this.mContext);
            if (PermissionSettingFragment.this.mGroupName != null) {
                PermissionSettingFragment.this.mPermissionApps = PermissionApps.create(PermissionSettingFragment.this.mContext, PermissionSettingFragment.this.mGroupName);
            }
            dbAdapter.setPermissionForAllApp(PermissionSettingFragment.this.mPermissionObject, PermissionSettingFragment.this.mPermissionType, operation.intValue(), PermissionSettingFragment.this.mPermissionApps);
            dbAdapter.refreshAllCachedData("permission setting activity " + this);
            return PermissionSettingFragment.this.getRecommendInfoForCurrentPermission();
        }

        protected void onPostExecute(Map<String, RecommendBaseItem> result) {
            PermissionSettingFragment.this.updatePermissionAppsList(result, PermissionSettingFragment.this.mPermissionApps);
            PermissionSettingFragment.this.updateUI();
            PermissionSettingFragment.this.onHeaderChanged();
            PermissionSettingFragment.this.hideWaitingDialog();
            PermissionSettingFragment.this.invalidateOptionsMenu();
        }
    }

    public void onResume() {
        super.onResume();
        this.mAppsListview.setSelection(this.mListViewPos);
        new AsynctaskUpdateDB().execute(new Void[0]);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.isDefaultMmsHedaderInited = false;
        this.mContext = getActivity().getApplicationContext();
        this.mPermissionTableManager = PermissionTableManager.getInstance(this.mContext);
        this.mPermissonAppsList = new ArrayList();
        if (getActivity().getIntent() != null) {
            Bundle bundle = getActivity().getIntent().getExtras();
            if (bundle != null) {
                this.mPermissionType = bundle.getInt("permissionType");
                if (MPermissionUtil.isClassAType(this.mPermissionType) || MPermissionUtil.isClassBType(this.mPermissionType)) {
                    this.mGroupName = (String) MPermissionUtil.typeToPermGroup.get(this.mPermissionType);
                    this.mPermissionApps = PermissionApps.create(this.mContext, this.mGroupName);
                }
                HwLog.i(this.LOG_TAG, "mPermissionType = " + this.mPermissionType + ", group name:" + this.mGroupName);
                this.mPermissionObject = this.mPermissionTableManager.getPermissionObjectByPermissionType(this.mPermissionType);
                if (this.mPermissionObject != null) {
                    addIconAndTitle();
                    updatePermissionAppsList(new HashMap(), this.mPermissionApps);
                    HsmPackageManager.registerListener(this.mExternalStorageListener);
                    DBAdapter.registerDataChangeListener(this);
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = inflater.inflate(R.layout.permission_cfg_list, null);
        this.mAppsListview = (ListView) this.mLayout.findViewById(16908298);
        this.mAppsListview.setItemsCanFocus(false);
        this.mAppsListview.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    PermissionSettingFragment.this.mListViewPos = PermissionSettingFragment.this.mAppsListview.getFirstVisiblePosition();
                    View topView = PermissionSettingFragment.this.mAppsListview.getChildAt(0);
                    if (topView != null) {
                        PermissionSettingFragment.this.mHeightOffSet = topView.getTop();
                    }
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        this.mEmpty = (RelativeLayout) this.mLayout.findViewById(16908292);
        initSetDefaultMmsHedader(this.mLayout);
        this.mHeaderView = (LinearLayout) this.mLayout.findViewById(R.id.permission_header_view);
        this.mHeader = (TextView) this.mHeaderView.findViewById(R.id.permission_description_text);
        this.tvNoneAppInfo = (TextView) this.mLayout.findViewById(R.id.empty_explain);
        this.tvNoneAppInfo.setText(this.mPermissionObject.getPermissionNoneAppTrips());
        this.mImageView = (ImageView) this.mLayout.findViewById(R.id.no_app_icon);
        this.mImageView.setImageDrawable(getResources().getDrawable(ShareLib.getNoAppIconId(this.mPermissionType)));
        updateUI();
        onHeaderChanged();
        return this.mLayout;
    }

    public void onPause() {
        hideWaitingDialog();
        super.onPause();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (((!Utility.isWifiOnlyMode() && !Utility.isDataOnlyMode()) || !ShareCfg.isPermissionFrozen(this.mPermissionType)) && this.mPermissionSettingAdapter != null && !this.mPermissionSettingAdapter.isEmpty()) {
            inflater.inflate(R.menu.permission_setall_menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (this.mPermissionSettingAdapter != null) {
            boolean enabled = !this.mPermissionSettingAdapter.isEmpty();
            setMenuItemEnabled(menu.findItem(R.id.restrict_all), enabled);
            setMenuItemEnabled(menu.findItem(R.id.allow_all), enabled);
        }
        MenuItem restrictMenu = menu.findItem(R.id.restrict_all);
        if (restrictMenu != null) {
            SpannableString menuTitle = new SpannableString(getResources().getString(R.string.cancel_all));
            menuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.hsm_forbidden)), 0, menuTitle.length(), 0);
            restrictMenu.setTitle(menuTitle);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                getActivity().finish();
                break;
            case R.id.allow_all:
                updateAllCfg(1);
                break;
            case R.id.restrict_all:
                updateAllCfg(2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSetDefaultMmsHedader(View view) {
        if (!this.isDefaultMmsHedaderInited) {
            final String MMS_PACKAGE_NAME = CommonFunctionUtil.getHwOrginalSmsPackageName(getActivity());
            if (!(MMS_PACKAGE_NAME == null || !CommonFunctionUtil.isSmsPermission(this.mPermissionType) || CommonFunctionUtil.isDefaultSmsApp(getActivity(), MMS_PACKAGE_NAME))) {
                ViewStub viewStub = (ViewStub) view.findViewById(R.id.viewstub_set_default_mms);
                View setDefaultMmsView = null;
                if (viewStub != null) {
                    setDefaultMmsView = viewStub.inflate();
                }
                if (setDefaultMmsView == null) {
                    HwLog.e(this.LOG_TAG, "error, can not find viewstub_set_default_mms");
                } else {
                    this.isDefaultMmsHedaderInited = true;
                    ((Button) view.findViewById(R.id.default_mms_set_button)).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            CommonFunctionUtil.requestDefaultSmsAppActivity(PermissionSettingFragment.this.getActivity(), MMS_PACKAGE_NAME);
                        }
                    });
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        DBAdapter.unregisterDataChangeListener(this);
        HsmPackageManager.unregisterListener(this.mExternalStorageListener);
    }

    protected void updateUI() {
        int i = 1;
        int i2 = -1;
        if (this.mPermissonAppsList != null) {
            int i3;
            if (this.mPermissonAppsList.size() > 0) {
                this.mHeaderView.setVisibility(0);
            } else {
                this.mHeaderView.setVisibility(8);
            }
            initSetDefaultMmsHedader(this.mLayout);
            String MMS_PACKAGE_NAME = CommonFunctionUtil.getHwOrginalSmsPackageName(getActivity());
            View defaultAppHeader = this.mLayout.findViewById(R.id.default_app_header);
            if (defaultAppHeader != null) {
                if (MMS_PACKAGE_NAME == null || !CommonFunctionUtil.isSmsPermission(this.mPermissionType) || CommonFunctionUtil.isDefaultSmsApp(getActivity(), MMS_PACKAGE_NAME)) {
                    defaultAppHeader.setVisibility(8);
                } else {
                    defaultAppHeader.setVisibility(0);
                }
            }
            this.mPermissionSettingAdapter = new PermissionSettingAdapter(getActivity(), this.mPermissonAppsList, this.mPermissionObject, this.mCfgSwitchListener);
            this.mListViewPos = this.mAppsListview.getFirstVisiblePosition();
            this.mAppsListview.setAdapter(this.mPermissionSettingAdapter);
            this.mAppsListview.setOnItemClickListener(this.mOnItemClickListener);
            this.mAppsListview.setSelectionFromTop(this.mListViewPos, this.mHeightOffSet);
            if ((Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) && this.mPermissonAppsList.size() > 0 && ShareCfg.isPermissionFrozen(((AppInfoWrapperForSinglePermission) this.mPermissonAppsList.get(this.mListViewPos)).mPermissionType)) {
                this.mAppsListview.setEnabled(false);
                this.mAppsListview.setClickable(false);
            }
            boolean noneData = this.mPermissonAppsList.size() == 0;
            LayoutParams mAppsListviewParams = (LayoutParams) this.mAppsListview.getLayoutParams();
            if (noneData) {
                i3 = 0;
            } else {
                i3 = -1;
            }
            mAppsListviewParams.height = i3;
            if (noneData) {
                i3 = 0;
            } else {
                i3 = 1;
            }
            mAppsListviewParams.weight = (float) i3;
            LayoutParams mEmptyParams = (LayoutParams) this.mEmpty.getLayoutParams();
            if (!noneData) {
                i2 = 0;
            }
            mEmptyParams.height = i2;
            if (!noneData) {
                i = 0;
            }
            mEmptyParams.weight = (float) i;
        }
    }

    protected void executePermissionCfgUpdate(int nCfgCode) {
        HwLog.d(this.LOG_TAG, "updateAllCfg: try to set permission cfg to " + nCfgCode);
        new AsynctaskUpdatePermissionCfg().execute(new Integer[]{Integer.valueOf(nCfgCode)});
    }

    private void addIconAndTitle() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(this.mPermissionObject.getmPermissionNames());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        getActivity().setTitle(this.mPermissionObject.getmPermissionNames());
    }

    public void notifyPermissionSettingAdapterDataChange() {
        this.mPermissionSettingAdapter.notifyDataSetChanged();
    }

    private Map<String, RecommendBaseItem> getRecommendInfoForCurrentPermission() {
        Map<String, RecommendBaseItem> recommendMap = new HashMap();
        SparseIntArray permissionIdMap = PermissionMap.getPermissionIdMap();
        if (permissionIdMap.size() == 0) {
            return recommendMap;
        }
        int index = permissionIdMap.indexOfValue(this.mPermissionType);
        if (-1 == index) {
            return recommendMap;
        }
        if (this.mContext == null) {
            HwLog.e(this.LOG_TAG, "The mContext is null!");
            return recommendMap;
        }
        int permissionItemId = permissionIdMap.keyAt(index);
        Map<String, List<RecommendItem>> result = null;
        try {
            result = RecommendQueryOutput.fromBundle(this.mContext.getContentResolver().call(CloudProviderConst.CLOUD_AUTHORITY_URI, RecommendCallMethod.CALL_METHOD_QUERY_RECOMMEND, null, RecommendQueryInput.generateMultiPkgOneItemInput(6, null, permissionItemId)));
        } catch (RecommendParamException e) {
            HwLog.e(this.LOG_TAG, "error generateMultiPkgOneItemInput RecommendParamException");
        } catch (Exception e2) {
            HwLog.e(this.LOG_TAG, "error generateMultiPkgOneItemInput Exception");
        }
        if (result == null || result.isEmpty()) {
            return recommendMap;
        }
        for (Entry<String, List<RecommendItem>> entry : result.entrySet()) {
            List<RecommendItem> listRecommendItem = (List) entry.getValue();
            if (!(listRecommendItem == null || listRecommendItem.isEmpty())) {
                RecommendItem recommendItem = (RecommendItem) ((List) entry.getValue()).get(0);
                int recommendValue = RecommendCfg.getCfgFromRecommendVaule(recommendItem.getConfigType());
                if (recommendValue != 0) {
                    recommendMap.put((String) entry.getKey(), new RecommendBaseItem(true, recommendValue, recommendItem.getPercentage()));
                }
            }
        }
        return recommendMap;
    }

    public void onPermissionCfgChanged() {
    }

    protected void updatePermissionAppsList(Map<String, RecommendBaseItem> recommendMap, PermissionApps pas) {
        this.mRecommendAvailable = AppInfoWrapperForSinglePermission.updatePureAppInfoWrapperList(this.mContext, this.mPermissionObject, this.mPermissonAppsList, recommendMap, pas);
    }

    void onHeaderChanged() {
        this.mHandler.sendEmptyMessageDelayed(2, 0);
    }

    private int getForbidCount() {
        int forbidCount = 0;
        if (this.mPermissonAppsList == null || this.mPermissionObject == null) {
            return 0;
        }
        for (AppInfoWrapperForSinglePermission appInfo : this.mPermissonAppsList) {
            if (2 == appInfo.mPermissionStatus) {
                forbidCount++;
            }
        }
        return forbidCount;
    }

    protected void updateAllCfg(final int nCfgCode) {
        String strDialogTitle;
        String strDialogMsg;
        String strPositiveBtnText;
        if (1 == nCfgCode) {
            strDialogTitle = getString(R.string.allow_all);
            strDialogMsg = getString(R.string.permissionmgr_alert_allowall_on_permission);
            strPositiveBtnText = getString(R.string.allow_all);
        } else {
            strDialogTitle = getString(R.string.permissionmgr_alert_retrictall_on_permission_title);
            strDialogMsg = getString(R.string.permissionmgr_alert_retrictall_on_permission_new);
            strPositiveBtnText = getString(R.string.cancel_all);
        }
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setTitle(strDialogTitle);
        dialogBuilder.setMessage(strDialogMsg);
        dialogBuilder.setNegativeButton(R.string.cancel, null);
        dialogBuilder.setPositiveButton(strPositiveBtnText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                PermissionSettingFragment.this.executePermissionCfgUpdate(nCfgCode);
                PermissionSettingFragment.this.showWaitingDialog(R.string.harassmentInterception_wait);
            }
        });
        AlertDialog dialog = dialogBuilder.show();
        if (2 == nCfgCode) {
            dialog.getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
        }
    }

    protected void showWaitingDialog(int StringId) {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(getActivity(), "", getResources().getString(StringId), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
        }
    }

    protected void hideWaitingDialog() {
        if (this.mWaitingDialog != null) {
            if (this.mWaitingDialog.isShowing()) {
                this.mWaitingDialog.dismiss();
            }
            this.mWaitingDialog = null;
        }
    }

    protected void updatePermissionAppListForSpinner(int permissionType, int uid, String pkgName, int permissionOperation) {
        for (AppInfoWrapperForSinglePermission appInfoWrapper : this.mPermissonAppsList) {
            if (uid == appInfoWrapper.mUid && pkgName.equals(appInfoWrapper.mPkgName)) {
                appInfoWrapper.mPermissionStatus = permissionOperation;
                DBAdapter.getInstance(this.mContext).updateApplist(uid, appInfoWrapper.mPkgName, false);
            }
        }
    }

    private void setMenuItemEnabled(MenuItem mi, boolean enabled) {
        if (mi != null) {
            mi.setEnabled(enabled);
        }
    }

    private void invalidateOptionsMenu() {
        Activity activity = getActivity();
        if (activity == null) {
            HwLog.w(this.LOG_TAG, "invalidateOptionsMenu activity is null");
        } else {
            activity.invalidateOptionsMenu();
        }
    }
}
