package com.huawei.systemmanager.power.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.misc.Constant;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.stats.PowerStatsException;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.data.stats.UidAndPower.Cmp;
import com.huawei.systemmanager.power.data.xml.PowerWarningParam;
import com.huawei.systemmanager.power.notification.UserNotifier;
import com.huawei.systemmanager.power.util.Conversion;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BackgroundConsumeFragment extends Fragment {
    private static final String TAG = "BackgroundConsumeFragment";
    private Activity mActivity;
    private BackgroundAppPowerConsumeAdapter mAdapter;
    private DataAsyncLoader mAsyncLoader = null;
    private List<String> mCheckedItemNameList = Lists.newArrayList();
    private MenuItem mCloseMenu;
    private List<BackgroundConsumeInfo> mConsumeDataList = Lists.newArrayList();
    private Context mContext;
    private View mDivider2View;
    private LinearLayout mEmptyLayout;
    private boolean mIsLoadingProgress = true;
    private List<String> mPkgNameFromNotifyList = Lists.newArrayList();
    private MenuItem mSelectAllMenu;
    private ListView mSoftListView;
    private PowerStatsHelper mStatsHelper;
    private TextView mTipTitleView;
    private List<String> mUnCheckedItemNameList = Lists.newArrayList();
    private ProgressDialog mWaitingDialog = null;

    private class CheckBoxChangeListener implements OnCheckedChangeListener {
        private CheckBoxChangeListener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BackgroundConsumeInfo info = (BackgroundConsumeInfo) BackgroundConsumeFragment.this.mConsumeDataList.get(((Integer) buttonView.getTag()).intValue());
            info.setmIsChecked(isChecked);
            BackgroundConsumeFragment.this.remarkCheckedItem(isChecked, info.getmPkgName());
            BackgroundConsumeFragment.this.mAdapter.swapData(BackgroundConsumeFragment.this.mConsumeDataList);
            BackgroundConsumeFragment.this.updateMenuViews();
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_KEY;
            strArr[1] = info.getmPkgName();
            strArr[2] = HsmStatConst.PARAM_VAL;
            strArr[3] = isChecked ? "1" : "0";
            HsmStat.statE((int) Events.E_POWER_BGCONSUME_CHEACK, HsmStatConst.constructJsonParams(strArr));
        }
    }

    private class ClearAppTask extends AsyncTask<Void, Void, Integer> {
        private ClearAppTask() {
        }

        protected Integer doInBackground(Void... param) {
            HwLog.d(BackgroundConsumeFragment.TAG, "ClearAppTask doInBackground in ");
            return Integer.valueOf(BackgroundConsumeFragment.this.forceStopPackageAndSyncSaving());
        }

        protected void onPostExecute(Integer result) {
        }
    }

    private class DataAsyncLoader extends AsyncTask<Void, Void, List<BackgroundConsumeInfo>> {
        private DataAsyncLoader() {
        }

        protected List<BackgroundConsumeInfo> doInBackground(Void... param) {
            HwLog.d(BackgroundConsumeFragment.TAG, "DataAsyncLoader doInBackground in ");
            List<BackgroundConsumeInfo> result = BackgroundConsumeFragment.this.getBackgroundConsumeData();
            HwLog.d(BackgroundConsumeFragment.TAG, "DataAsyncLoader doInBackground out ");
            return result;
        }

        protected void onPostExecute(List<BackgroundConsumeInfo> result) {
            if (isCancelled()) {
                HwLog.w(BackgroundConsumeFragment.TAG, "onPostExecute, The task is canceled");
                return;
            }
            HwLog.d(BackgroundConsumeFragment.TAG, "DataAsyncLoader onPostExecute in");
            BackgroundConsumeFragment.this.mConsumeDataList.clear();
            BackgroundConsumeFragment.this.mConsumeDataList.addAll(result);
            BackgroundConsumeFragment.this.mAdapter.swapData(result);
            BackgroundConsumeFragment.this.updateAllViews();
            BackgroundConsumeFragment.this.hideWaitingDialog();
            BackgroundConsumeFragment.this.popNotRunningPkgToast();
            BackgroundConsumeFragment.this.mAsyncLoader = null;
            HwLog.d(BackgroundConsumeFragment.TAG, "DataAsyncLoader onPostExecute out");
        }
    }

    public static BackgroundConsumeFragment newInstance(boolean IsLoadingProgress) {
        BackgroundConsumeFragment fragment = new BackgroundConsumeFragment();
        fragment.mIsLoadingProgress = IsLoadingProgress;
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mActivity = getActivity();
        this.mContext = this.mActivity.getApplicationContext();
        Intent recIntent = this.mActivity.getIntent();
        if (recIntent != null) {
            this.mPkgNameFromNotifyList = recIntent.getStringArrayListExtra(ApplicationConstant.USERNOTIFY_BUNDLE_NOTIFY_PKGNAME_LIST);
        }
        HwLog.d(TAG, "recIntent = " + recIntent + "###mPkgNameFromNotifyList = " + this.mPkgNameFromNotifyList);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.backgroundapp_consume_list, null);
        initViewAndAdapter(fragmentView);
        return fragmentView;
    }

    private void initViewAndAdapter(View fragmentView) {
        this.mSoftListView = (ListView) fragmentView.findViewById(R.id.background_consume_listview);
        View footerView = this.mActivity.getLayoutInflater().inflate(R.layout.blank_footer_view, this.mSoftListView, false);
        this.mSoftListView.setFooterDividersEnabled(false);
        this.mSoftListView.addFooterView(footerView, null, false);
        this.mSoftListView.setTag(Constant.DISALBE_LISTVIEW_CHECKOBX_MULTI_SELECT);
        this.mEmptyLayout = (LinearLayout) fragmentView.findViewById(R.id.backgroundapp_consume_no_item);
        this.mDivider2View = fragmentView.findViewById(R.id.dividerLine2);
        this.mTipTitleView = (TextView) fragmentView.findViewById(R.id.setting_title_textview);
        this.mAdapter = new BackgroundAppPowerConsumeAdapter(this.mContext, new CheckBoxChangeListener());
        this.mSoftListView.setAdapter(this.mAdapter);
        setClickListener();
    }

    private void updateAllViews() {
        updateMenuViews();
        if (this.mConsumeDataList.size() == 0) {
            this.mEmptyLayout.setVisibility(0);
            this.mDivider2View.setVisibility(8);
            this.mSoftListView.setVisibility(8);
            this.mTipTitleView.setVisibility(8);
            return;
        }
        this.mEmptyLayout.setVisibility(8);
        this.mDivider2View.setVisibility(0);
        this.mSoftListView.setVisibility(0);
        this.mTipTitleView.setVisibility(0);
        this.mTipTitleView.setText(this.mContext.getResources().getString(R.string.power_high_consume_des, new Object[]{Integer.valueOf(this.mConsumeDataList.size())}));
    }

    private void updateMenuViews() {
        setCloseState();
        setSelectAllCheckState();
    }

    private void setClickListener() {
        this.mSoftListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent();
                String pkgName = ((BackgroundConsumeInfo) BackgroundConsumeFragment.this.mConsumeDataList.get(position)).getmPkgName();
                int uid = ((BackgroundConsumeInfo) BackgroundConsumeFragment.this.mConsumeDataList.get(position)).getmUid();
                if (pkgName == null) {
                    HwLog.w(BackgroundConsumeFragment.TAG, "onItemClick package name is null");
                    return;
                }
                intent.putExtras(DetailExtConst.sipperToBundle(pkgName, uid, ((BackgroundConsumeInfo) BackgroundConsumeFragment.this.mConsumeDataList.get(position)).getmSipper(), true));
                if (((BackgroundConsumeInfo) BackgroundConsumeFragment.this.mConsumeDataList.get(position)).ismIsSharedId()) {
                    Map<String, Double> tempProcPowerMap = ((BackgroundConsumeInfo) BackgroundConsumeFragment.this.mConsumeDataList.get(position)).procPowerMap;
                    if (tempProcPowerMap != null) {
                        intent.putExtras(DetailExtConst.mapToBundle(tempProcPowerMap));
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putInt(DetailExtConst.ENTER_INTO_DETAIL_TYPE_KEY, 1);
                intent.putExtras(bundle);
                intent.setClass(BackgroundConsumeFragment.this.mContext, DetailOfSoftConsumptionActivity.class);
                BackgroundConsumeFragment.this.startActivity(intent);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, pkgName);
                HsmStat.statE((int) Events.E_POWER_BGCONSUME_LIST_CLICK, statParam);
            }
        });
    }

    private List<BackgroundConsumeInfo> getBackgroundConsumeData() {
        List<BackgroundConsumeInfo> tmpDataList = Lists.newArrayList();
        try {
            if (this.mStatsHelper == null) {
                this.mStatsHelper = PowerStatsHelper.newInstance(this.mContext, true);
            }
            List<UidAndPower> bgAppConsumpList = this.mStatsHelper.computerBackgroundConsumption(this.mContext, true);
            PackageManager pm = this.mContext.getPackageManager();
            Integer isWakeUpApp = Integer.valueOf(0);
            Integer rogueType = Integer.valueOf(0);
            Collections.sort(bgAppConsumpList, new Cmp());
            this.mUnCheckedItemNameList.clear();
            this.mCheckedItemNameList.clear();
            HwLog.d(TAG, "getBackgroundConsumeData sorted bgAppConsumpList:" + bgAppConsumpList);
            for (UidAndPower uap : bgAppConsumpList) {
                HwLog.e(TAG, "getBackgroundConsumeData loop print UidPower: " + uap);
                List<ApplicationInfo> appInfos = SysCoreUtils.getAppInfoByUid(this.mContext, uap.getUid());
                if (appInfos.size() < 1) {
                    HwLog.w(TAG, "the size of appInfos less than one");
                } else {
                    if (appInfos.size() > 1) {
                        for (int i = 0; i < appInfos.size(); i++) {
                            if (SavingSettingUtil.isPkgNameExistInRogueDB(this.mContext.getContentResolver(), ((ApplicationInfo) appInfos.get(i)).packageName)) {
                                Collections.swap(appInfos, 0, i);
                                HwLog.e(TAG, "swap sharedUid appInfos:  0  and  " + i);
                                break;
                            }
                        }
                    }
                    try {
                        isWakeUpApp = (Integer) SavingSettingUtil.getRogue(this.mContext.getContentResolver(), ((ApplicationInfo) appInfos.get(0)).packageName, 4);
                        rogueType = (Integer) SavingSettingUtil.getRogue(this.mContext.getContentResolver(), ((ApplicationInfo) appInfos.get(0)).packageName, 7);
                    } catch (SQLException e) {
                        HwLog.e(TAG, "The HwSystemManager db RuntimeException!!");
                        e.printStackTrace();
                    }
                    if (isWakeUpApp == null) {
                        isWakeUpApp = Integer.valueOf(0);
                    }
                    if (rogueType == null) {
                        rogueType = Integer.valueOf(0);
                    }
                    HwLog.e(TAG, "getBackgroundConsumeData UID = " + uap.getUid() + "###isWakeUpApp = " + isWakeUpApp + "packageName = " + ((ApplicationInfo) appInfos.get(0)).packageName + ", rogueType: " + rogueType);
                    if (isValidType(rogueType.intValue(), uap.getUid())) {
                        HwLog.i(TAG, "wangqing not show uap.uid= " + uap.getUid());
                    } else {
                        ApplicationInfo appInfo = (ApplicationInfo) appInfos.get(0);
                        if (appInfo != null) {
                            String packageName = appInfo.packageName;
                            BackgroundConsumeInfo info = new BackgroundConsumeInfo();
                            String packageLabel = Conversion.toShortName(appInfo.loadLabel(this.mContext.getPackageManager()).toString());
                            info.setmIcon(pm.getApplicationIcon(appInfo));
                            info.setmPkgTitle(packageLabel);
                            info.setmPkgName(packageName);
                            info.setmUid(uap.getUid());
                            info.setmPowerLevel((int) uap.getPower());
                            info.setmRogueType(rogueType.intValue());
                            info.setmIsChecked(isNeedChecked(packageName, uap.getPower(), rogueType.intValue()));
                            remarkCheckedItem(info.ismIsChecked(), packageName);
                            String[] packages = pm.getPackagesForUid(uap.getUid());
                            HwLog.d(TAG, "getBackgroundConsumeData : uap.uid:" + uap.getUid());
                            if (packages == null || packages.length <= 1) {
                                info.setmIsSharedId(false);
                            } else {
                                info.setmIsSharedId(true);
                                if (uap.getSipper() != null) {
                                    info.procPowerMap = this.mStatsHelper.getBackgroundPackageNameAndPower(this.mContext, uap.getSipper().uidObj);
                                }
                            }
                            info.setmSipper(uap.getSipper());
                            tmpDataList.add(info);
                            if (this.mPkgNameFromNotifyList != null) {
                                int positionOfPackageName = this.mPkgNameFromNotifyList.indexOf(packageName);
                                if (positionOfPackageName >= 0 && positionOfPackageName < this.mPkgNameFromNotifyList.size()) {
                                    this.mPkgNameFromNotifyList.remove(positionOfPackageName);
                                }
                            }
                        }
                    }
                }
            }
            return tmpDataList;
        } catch (PowerStatsException e2) {
            HwLog.e(TAG, "getBackgroundConsumeData catch PowerStatsException");
            e2.printStackTrace();
            return tmpDataList;
        }
    }

    private boolean isValidType(int rogueType, int uid) {
        if (rogueType == 0 || rogueType == 4 || uid == 1000) {
            return true;
        }
        return false;
    }

    private boolean isNeedChecked(String mPackageName, double mPackagePower, int rogueType) {
        for (BackgroundConsumeInfo info : this.mConsumeDataList) {
            if (mPackageName.equals(info.getmPkgName())) {
                return info.ismIsChecked();
            }
        }
        boolean isCheck = false;
        int isIgnore = 0;
        Object tempObject = SavingSettingUtil.getRogue(this.mContext.getContentResolver(), mPackageName, 1);
        if (tempObject != null) {
            isIgnore = Integer.parseInt(tempObject.toString());
        }
        if (mPackagePower > ((double) PowerWarningParam.getHigh_level_standard(this.mContext)) || rogueType != 0) {
            isCheck = true;
        }
        if (isIgnore == 1) {
            isCheck = false;
        }
        boolean bTotalSwitchOn = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, true);
        if (SysCoreUtils.IS_ATT) {
            bTotalSwitchOn = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, false);
        }
        if (!bTotalSwitchOn) {
            isCheck = false;
        }
        return isCheck;
    }

    public void onDestroy() {
        if (this.mAsyncLoader != null) {
            this.mAsyncLoader.cancel(false);
            this.mAsyncLoader = null;
        }
        if (this.mConsumeDataList != null) {
            this.mConsumeDataList.clear();
        }
        if (this.mCheckedItemNameList != null) {
            this.mCheckedItemNameList.clear();
        }
        if (this.mUnCheckedItemNameList != null) {
            this.mUnCheckedItemNameList.clear();
        }
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        UserNotifier.destroyNotification(this.mContext);
        showWaitingDialog();
        loadAppListAndRefreshViews();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_power_bg_two_menu, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        this.mCloseMenu = menu.findItem(R.id.power_bg_menu_close);
        this.mSelectAllMenu = menu.findItem(R.id.power_bg_menu_selectall);
        updateMenuViews();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String[] strArr;
        switch (item.getItemId()) {
            case R.id.power_bg_menu_close:
                boolean isSelAll = this.mSelectAllMenu.isChecked();
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = isSelAll ? "1" : "0";
                HsmStat.statE(25, HsmStatConst.constructJsonParams(strArr));
                refreshViewsAfterClean();
                new ClearAppTask().execute(new Void[0]);
                break;
            case R.id.power_bg_menu_selectall:
                boolean newCheckState = !this.mSelectAllMenu.isChecked();
                this.mSelectAllMenu.setChecked(newCheckState);
                if (newCheckState) {
                    this.mSelectAllMenu.setIcon(R.drawable.menu_check_pressed);
                    this.mSelectAllMenu.setTitle(R.string.unselect_all);
                    this.mSelectAllMenu.setChecked(true);
                } else {
                    this.mSelectAllMenu.setIcon(R.drawable.menu_check_status);
                    this.mSelectAllMenu.setTitle(R.string.select_all);
                    this.mSelectAllMenu.setChecked(false);
                }
                int appsNum = this.mConsumeDataList.size();
                for (int idx = 0; idx != appsNum; idx++) {
                    ((BackgroundConsumeInfo) this.mConsumeDataList.get(idx)).setmIsChecked(newCheckState);
                }
                if (newCheckState) {
                    this.mCheckedItemNameList.addAll(this.mUnCheckedItemNameList);
                    this.mUnCheckedItemNameList.clear();
                } else {
                    this.mUnCheckedItemNameList.addAll(this.mCheckedItemNameList);
                    this.mCheckedItemNameList.clear();
                }
                updateAppCheckStateBatch(newCheckState);
                this.mAdapter.swapData(this.mConsumeDataList);
                updateMenuViews();
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = newCheckState ? "1" : "0";
                HsmStat.statE((int) Events.E_POWER_BGCONSUME_CHEACKALL, HsmStatConst.constructJsonParams(strArr));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshViewsAfterClean() {
        Iterator<BackgroundConsumeInfo> it = this.mConsumeDataList.iterator();
        while (it.hasNext()) {
            if (((BackgroundConsumeInfo) it.next()).ismIsChecked()) {
                it.remove();
            }
        }
        this.mAdapter.swapData(this.mConsumeDataList);
        updateAllViews();
    }

    private int forceStopPackageAndSyncSaving() {
        int checkedNameSize = this.mCheckedItemNameList.size();
        ArrayList<String> packageName = new ArrayList();
        PackageManager pm = this.mContext.getPackageManager();
        int i = 0;
        while (i < checkedNameSize) {
            try {
                ApplicationInfo appInfo = SysCoreUtils.getAppInfoByPackageName(this.mContext, (String) this.mCheckedItemNameList.get(i));
                if (appInfo != null) {
                    String[] pkgNames = pm.getPackagesForUid(appInfo.uid);
                    if (pkgNames == null || pkgNames.length <= 1) {
                        packageName.add((String) this.mCheckedItemNameList.get(i));
                    } else {
                        for (String temp : pkgNames) {
                            packageName.add(temp);
                        }
                    }
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!packageName.isEmpty()) {
            SysCoreUtils.forceStopPackageAndSyncSaving(this.mContext.getApplicationContext(), packageName);
        }
        if (this.mPkgNameFromNotifyList != null) {
            this.mPkgNameFromNotifyList.clear();
        }
        this.mCheckedItemNameList.clear();
        return checkedNameSize;
    }

    private void remarkCheckedItem(boolean isChecked, String packageName) {
        if (isChecked) {
            if (!this.mCheckedItemNameList.contains(packageName)) {
                this.mCheckedItemNameList.add(packageName);
            }
            this.mUnCheckedItemNameList.remove(packageName);
            return;
        }
        this.mCheckedItemNameList.remove(packageName);
        if (!this.mUnCheckedItemNameList.contains(packageName)) {
            this.mUnCheckedItemNameList.add(packageName);
        }
    }

    private String getMenuTitle() {
        return String.format(this.mContext.getResources().getString(R.string.Button_Accelerater_Close), new Object[]{Integer.valueOf(getCheckedNumer())});
    }

    private int getCheckedNumer() {
        int total = 0;
        for (BackgroundConsumeInfo data : this.mConsumeDataList) {
            if (data.ismIsChecked()) {
                total++;
            }
        }
        return total;
    }

    public void onPause() {
        if (this.mPkgNameFromNotifyList != null) {
            this.mPkgNameFromNotifyList.clear();
        }
        hideWaitingDialog();
        super.onPause();
    }

    private void setSelectAllCheckState() {
        if (this.mSelectAllMenu != null) {
            int totalAppNum = this.mConsumeDataList.size();
            int totalSelectedNum = getCheckedNumer();
            if (totalAppNum == 0) {
                this.mSelectAllMenu.setChecked(false);
                this.mSelectAllMenu.setIcon(R.drawable.menu_check_status);
                this.mSelectAllMenu.setTitle(R.string.select_all);
                this.mSelectAllMenu.setEnabled(false);
            } else if (totalAppNum == totalSelectedNum) {
                this.mSelectAllMenu.setEnabled(true);
                this.mSelectAllMenu.setChecked(true);
                this.mSelectAllMenu.setIcon(R.drawable.menu_check_pressed);
                this.mSelectAllMenu.setTitle(R.string.unselect_all);
            } else {
                this.mSelectAllMenu.setEnabled(true);
                this.mSelectAllMenu.setChecked(false);
                this.mSelectAllMenu.setIcon(R.drawable.menu_check_status);
                this.mSelectAllMenu.setTitle(R.string.select_all);
            }
        }
    }

    private void setCloseState() {
        if (this.mCloseMenu != null) {
            this.mCloseMenu.setTitle(getMenuTitle());
            if (getCheckedNumer() == 0) {
                this.mCloseMenu.setEnabled(false);
            } else {
                this.mCloseMenu.setEnabled(true);
            }
        }
    }

    private void updateAppCheckStateBatch(boolean checkState) {
        for (BackgroundConsumeInfo info : this.mConsumeDataList) {
            info.setmIsChecked(checkState);
        }
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null && this.mIsLoadingProgress) {
            this.mWaitingDialog = ProgressDialog.show(this.mActivity, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void hideWaitingDialog() {
        if (this.mWaitingDialog != null) {
            if (this.mWaitingDialog.isShowing()) {
                this.mWaitingDialog.dismiss();
            }
            this.mWaitingDialog = null;
        }
    }

    private void popNotRunningPkgToast() {
        PackageManager pm = this.mContext.getPackageManager();
        if (this.mPkgNameFromNotifyList != null && this.mPkgNameFromNotifyList.size() > 0) {
            try {
                if (this.mActivity.getIntent() != null && this.mActivity.getIntent().getStringArrayListExtra(ApplicationConstant.USERNOTIFY_BUNDLE_NOTIFY_PKGNAME_LIST) != null) {
                    StringBuilder noRunningPkgs = new StringBuilder();
                    int size = this.mPkgNameFromNotifyList.size();
                    for (int i = 0; i < size - 1; i++) {
                        noRunningPkgs.append(Conversion.toShortName(pm.getApplicationInfo((String) this.mPkgNameFromNotifyList.get(i), 0).loadLabel(pm).toString())).append(SqlMarker.COMMA_SEPARATE);
                    }
                    if (size > 0) {
                        noRunningPkgs.append(Conversion.toShortName(pm.getApplicationInfo((String) this.mPkgNameFromNotifyList.get(size - 1), 0).loadLabel(pm).toString()));
                        HwLog.d(TAG, noRunningPkgs.toString() + " not run anymore");
                    }
                }
            } catch (NameNotFoundException e) {
                HwLog.e(TAG, "popNotRunningPkgToast NameNotFoundException: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadAppListAndRefreshViews() {
        if (this.mAsyncLoader == null && getActivity() != null) {
            this.mAsyncLoader = new DataAsyncLoader();
            this.mAsyncLoader.execute(new Void[0]);
        }
    }
}
