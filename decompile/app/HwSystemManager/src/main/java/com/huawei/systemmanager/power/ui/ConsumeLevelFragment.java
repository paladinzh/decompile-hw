package com.huawei.systemmanager.power.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v4.view.PointerIconCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.data.stats.PowerStatsException;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.ui.ConsumeLevelInfo.ConsumeLevelHardwareInfo.Cmp;
import com.huawei.systemmanager.power.util.AppRangeWrapper;
import com.huawei.systemmanager.power.util.ExtAppInfo;
import com.huawei.systemmanager.power.util.L2MAdapter;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConsumeLevelFragment extends Fragment {
    private static final String HARDWARE_BLUETOOTH = "BLUETOOTH";
    private static final String HARDWARE_CELL = "CELL";
    private static final String HARDWARE_IDLE = "IDLE";
    private static final String HARDWARE_PHONE = "PHONE";
    private static final String HARDWARE_SCREEN = "SCREEN";
    private static final String HARDWARE_WIFI = "WIFI";
    static final int MINIMUM_ADJUST_VALUE = 1;
    static final double MINIMUM_PERCENTAGE = 0.01d;
    static final int PERCENTAGE_RATIO_100 = 100;
    private static final String TAG = "ConsumeLevelFragment";
    private Activity mActivity;
    private ComsumeLevelAdapter mAdapter = null;
    private Context mAppContext = null;
    private ResumeAsyncLoader mAsyncLoader = null;
    private Set<Integer> mConsumeAppUidSet = null;
    private ExpandableListView mExpandableListView;
    private int mHardPercentageMultiplyRatio = 0;
    private List<ConsumeLevelHardwareInfo> mHardwareList = Lists.newArrayList();
    PowerStatsHelper mPowerStatsHelper = null;
    private int mSoftPercentageMultiplyRatio = 0;
    private List<ConsumeLevelSoftwareInfo> mSoftwareList = Lists.newArrayList();
    private ProgressDialog mWaitingDialog = null;

    private class ConsumeChildClickListener implements OnChildClickListener {
        private ConsumeChildClickListener() {
        }

        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            HwLog.e(ConsumeLevelFragment.TAG, "onChildClick ");
            if (groupPosition == 0) {
                try {
                    ConsumeLevelFragment.this.doHardwareClick(childPosition);
                } catch (Exception ex) {
                    HwLog.e(ConsumeLevelFragment.TAG, "onChildClick catch Exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                ConsumeLevelFragment.this.doSoftwareClick(childPosition);
            }
            return true;
        }
    }

    private class ResumeAsyncLoader extends AsyncTask<Void, Void, Void> {
        private ResumeAsyncLoader() {
        }

        protected Void doInBackground(Void... arg0) {
            try {
                ConsumeLevelFragment.this.mHardwareList.clear();
                ConsumeLevelFragment.this.mSoftwareList.clear();
                ConsumeLevelFragment.this.mHardPercentageMultiplyRatio = 0;
                ConsumeLevelFragment.this.mSoftPercentageMultiplyRatio = 0;
                List<BatterySipper> sipperList = ConsumeLevelFragment.this.mPowerStatsHelper.computeSwAndHwConsumption(ConsumeLevelFragment.this.mAppContext, true);
                ConsumeLevelFragment.this.mConsumeAppUidSet = AppRangeWrapper.getAppThirdUidSet(ConsumeLevelFragment.this.mAppContext);
                ConsumeLevelFragment.this.fillSoftwareAndHardwareList(sipperList);
                ConsumeLevelFragment.this.adjustPercentage();
                for (ConsumeLevelHardwareInfo hard : ConsumeLevelFragment.this.mHardwareList) {
                    HwLog.v(ConsumeLevelFragment.TAG, "doInBackground hardList of : " + hard);
                }
                for (ConsumeLevelSoftwareInfo soft : ConsumeLevelFragment.this.mSoftwareList) {
                    HwLog.v(ConsumeLevelFragment.TAG, "doInBackground softList of : " + soft);
                }
                HwLog.d(ConsumeLevelFragment.TAG, "doInBackground softAdj: " + ConsumeLevelFragment.this.mSoftPercentageMultiplyRatio + ", hardAjd: " + ConsumeLevelFragment.this.mHardPercentageMultiplyRatio);
            } catch (PowerStatsException ex) {
                HwLog.e(ConsumeLevelFragment.TAG, "doInBackground catch PowerStatsException: " + ex.getMessage());
                ex.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int hardPercentAll = new BigDecimal(((double) ConsumeLevelFragment.this.mHardPercentageMultiplyRatio) / 100.0d).setScale(0, 4).intValue();
            HwLog.d(ConsumeLevelFragment.TAG, "onPostExecute hardPercent before ROUND_HALF_UP:" + ConsumeLevelFragment.this.mHardPercentageMultiplyRatio + ", after:" + hardPercentAll);
            ConsumeLevelFragment.this.mAdapter.swapData(hardPercentAll, 100 - hardPercentAll, ConsumeLevelFragment.this.mHardwareList, ConsumeLevelFragment.this.mSoftwareList);
            ConsumeLevelFragment.this.mExpandableListView.expandGroup(1);
            ConsumeLevelFragment.this.hideWaitingDialog();
            ConsumeLevelFragment.this.mAsyncLoader = null;
            HwLog.d(ConsumeLevelFragment.TAG, "ResumeAsyncLoader onPostExecute out");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mActivity = getActivity();
        this.mAppContext = this.mActivity.getApplicationContext();
        this.mPowerStatsHelper = PowerStatsHelper.newInstance(this.mAppContext, true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.consume_status_expandable, null);
        this.mExpandableListView = (ExpandableListView) fragmentView.findViewById(R.id.consume_soft_list);
        this.mAdapter = new ComsumeLevelAdapter(getActivity().getApplicationContext());
        this.mExpandableListView.setAdapter(this.mAdapter);
        return fragmentView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.mExpandableListView.setOnChildClickListener(new ConsumeChildClickListener());
    }

    public void onResume() {
        super.onResume();
        showWaitingDialog();
        refreshList();
    }

    public void onPause() {
        hideWaitingDialog();
        super.onPause();
    }

    public void onDestroy() {
        if (this.mAsyncLoader != null) {
            this.mAsyncLoader.cancel(false);
            this.mAsyncLoader = null;
        }
        super.onDestroy();
    }

    private void refreshList() {
        if (this.mAsyncLoader == null) {
            this.mAsyncLoader = new ResumeAsyncLoader();
            this.mAsyncLoader.execute(new Void[0]);
        }
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
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

    private boolean judgeAppIsCurrentUserId(int mUid, UserManager userManager) {
        int currentUserId = ActivityManager.getCurrentUser();
        if (currentUserId == UserHandle.getUserId(mUid)) {
            return true;
        }
        UserInfo currentUser = userManager.getUserInfo(UserHandle.getUserId(mUid));
        return currentUser != null && currentUser.isManagedProfile() && currentUserId == 0;
    }

    private void fillSoftwareAndHardwareList(List<BatterySipper> sipperList) {
        HwLog.d(TAG, "fillSoftwareAndHardwareList sipperSize: " + sipperList.size());
        initialDefaultHardInfoList();
        Set<Integer> runningUids = SysCoreUtils.getRunningUids(this.mAppContext);
        UserManager userManager = (UserManager) this.mAppContext.getSystemService("user");
        for (BatterySipper sipper : sipperList) {
            if (DrainType.APP == sipper.drainType) {
                ConsumeLevelSoftwareInfo softInfo = new ConsumeLevelSoftwareInfo();
                softInfo.sipper = sipper;
                softInfo.uid = sipper.getUid();
                softInfo.commInfo.value = L2MAdapter.value(sipper);
                if (judgeAppIsCurrentUserId(softInfo.uid, userManager)) {
                    if (fillExtSoftwareInfo(softInfo, runningUids)) {
                        this.mSoftwareList.add(softInfo);
                    }
                    if (softInfo.isShareUidApp) {
                        softInfo.procPowerMap = this.mPowerStatsHelper.getPackageNameAndPower(this.mAppContext, sipper.uidObj);
                    }
                }
            } else {
                for (ConsumeLevelHardwareInfo hardInfo : this.mHardwareList) {
                    if (hardInfo.type == sipper.drainType) {
                        hardInfo.commInfo.value = L2MAdapter.value(sipper);
                    }
                }
            }
        }
        Collections.sort(this.mHardwareList, new Cmp());
        Collections.sort(this.mSoftwareList, new ConsumeLevelSoftwareInfo.Cmp());
    }

    private boolean fillExtSoftwareInfo(ConsumeLevelSoftwareInfo softInfo, Set<Integer> runningUids) {
        int i = 0;
        if (softInfo.uid == 0) {
            softInfo.commInfo.icon = null;
            softInfo.commInfo.labelName = this.mAppContext.getString(R.string.android_os_new);
            softInfo.aliveStatus = -1;
        } else if (PointerIconCompat.TYPE_ALL_SCROLL == softInfo.uid) {
            softInfo.commInfo.icon = null;
            softInfo.commInfo.labelName = this.mAppContext.getString(R.string.process_mediaserver_label);
            softInfo.aliveStatus = -1;
        } else {
            ExtAppInfo extApp = SysCoreUtils.getExtAppInfoByUid(this.mAppContext, softInfo.uid);
            if (extApp == null) {
                HwLog.e(TAG, "fillExtSoftwareInfo empty extApInfo of uid: " + softInfo.uid);
                return false;
            }
            softInfo.commInfo.icon = extApp.getmIcon();
            softInfo.commInfo.labelName = extApp.getmPkgLabel();
            softInfo.isShareUidApp = extApp.ismIsShareUid();
            softInfo.pkgName = extApp.getmPkgName();
            if (!runningUids.contains(Integer.valueOf(softInfo.uid))) {
                i = 1;
            }
            softInfo.aliveStatus = i;
        }
        return true;
    }

    private void initialDefaultHardInfoList() {
        if (!Utility.isWifiOnlyMode()) {
            ConsumeLevelHardwareInfo hardInfoPhone = new ConsumeLevelHardwareInfo();
            hardInfoPhone.type = DrainType.PHONE;
            hardInfoPhone.commInfo.labelName = this.mAppContext.getString(R.string.power_phone);
            hardInfoPhone.commInfo.value = 0.0d;
            hardInfoPhone.commInfo.icon = this.mAppContext.getDrawable(R.drawable.ic_call_battery);
            this.mHardwareList.add(hardInfoPhone);
        }
        ConsumeLevelHardwareInfo hardInfoScreen = new ConsumeLevelHardwareInfo();
        hardInfoScreen.type = DrainType.SCREEN;
        hardInfoScreen.commInfo.labelName = this.mAppContext.getString(R.string.power_screen);
        hardInfoScreen.commInfo.value = 0.0d;
        hardInfoScreen.commInfo.icon = this.mAppContext.getDrawable(R.drawable.ic_settings_screen);
        this.mHardwareList.add(hardInfoScreen);
        if (!Utility.isWifiOnlyMode()) {
            ConsumeLevelHardwareInfo hardInfoCell = new ConsumeLevelHardwareInfo();
            hardInfoCell.type = DrainType.CELL;
            hardInfoCell.commInfo.labelName = this.mAppContext.getString(R.string.power_cell);
            hardInfoCell.commInfo.value = 0.0d;
            hardInfoCell.commInfo.icon = this.mAppContext.getDrawable(R.drawable.ic_signal_battery);
            this.mHardwareList.add(hardInfoCell);
        }
        ConsumeLevelHardwareInfo hardInfoWIfi = new ConsumeLevelHardwareInfo();
        hardInfoWIfi.type = DrainType.WIFI;
        hardInfoWIfi.commInfo.labelName = this.mAppContext.getString(R.string.power_wifi_new);
        hardInfoWIfi.commInfo.value = 0.0d;
        hardInfoWIfi.commInfo.icon = this.mAppContext.getDrawable(R.drawable.ic_wifi_battery);
        this.mHardwareList.add(hardInfoWIfi);
        ConsumeLevelHardwareInfo hardInfoIdle = new ConsumeLevelHardwareInfo();
        hardInfoIdle.type = DrainType.IDLE;
        hardInfoIdle.commInfo.labelName = this.mAppContext.getString(R.string.power_idle);
        hardInfoIdle.commInfo.value = 0.0d;
        hardInfoIdle.commInfo.icon = this.mAppContext.getDrawable(R.drawable.ic_standby_battery);
        this.mHardwareList.add(hardInfoIdle);
        ConsumeLevelHardwareInfo hardInfoBt = new ConsumeLevelHardwareInfo();
        hardInfoBt.type = DrainType.BLUETOOTH;
        hardInfoBt.commInfo.labelName = this.mAppContext.getString(R.string.power_bluetooth);
        hardInfoBt.commInfo.value = 0.0d;
        hardInfoBt.commInfo.icon = this.mAppContext.getDrawable(R.drawable.ic_bluetooth_battery);
        this.mHardwareList.add(hardInfoBt);
    }

    private void adjustPercentage() {
        double totalConsumptionValue = 0.0d;
        for (ConsumeLevelHardwareInfo hardInfo : this.mHardwareList) {
            totalConsumptionValue += hardInfo.commInfo.value;
        }
        for (ConsumeLevelSoftwareInfo softInfo : this.mSoftwareList) {
            totalConsumptionValue += softInfo.commInfo.value;
        }
        HwLog.e(TAG, "adjustPercentage total consumption: " + totalConsumptionValue);
        adjustHardware(totalConsumptionValue);
        adjustSoftware(totalConsumptionValue);
        adjustOther();
    }

    private void adjustHardware(double totalConsumptionValue) {
        for (ConsumeLevelHardwareInfo hardInfo : this.mHardwareList) {
            hardInfo.commInfo.exactPercentage = (hardInfo.commInfo.value * 100.0d) / totalConsumptionValue;
            if (hardInfo.commInfo.exactPercentage < MINIMUM_PERCENTAGE) {
                hardInfo.commInfo.adjValue = 1;
            } else {
                hardInfo.commInfo.adjValue = (int) (hardInfo.commInfo.exactPercentage * 100.0d);
            }
            this.mHardPercentageMultiplyRatio += hardInfo.commInfo.adjValue;
        }
    }

    private void adjustSoftware(double totalConsumptionValue) {
        Iterator<ConsumeLevelSoftwareInfo> softIterator = this.mSoftwareList.iterator();
        while (softIterator.hasNext()) {
            ConsumeLevelSoftwareInfo softInfo = (ConsumeLevelSoftwareInfo) softIterator.next();
            softInfo.commInfo.exactPercentage = (softInfo.commInfo.value * 100.0d) / totalConsumptionValue;
            softInfo.commInfo.adjValue = (int) (softInfo.commInfo.exactPercentage * 100.0d);
            if (1 > softInfo.commInfo.adjValue) {
                HwLog.d(TAG, "going to remove adjustSoftware remove an soft:" + softInfo);
                softIterator.remove();
            } else {
                this.mSoftPercentageMultiplyRatio += softInfo.commInfo.adjValue;
            }
        }
    }

    private void adjustOther() {
        int otherLeftAdj = (10000 - this.mHardPercentageMultiplyRatio) - this.mSoftPercentageMultiplyRatio;
        HwLog.d(TAG, "adjustOther left otherLeftAdj: " + otherLeftAdj);
        this.mSoftPercentageMultiplyRatio += otherLeftAdj;
        if (10000 != this.mHardPercentageMultiplyRatio + this.mSoftPercentageMultiplyRatio) {
            HwLog.w(TAG, "adjustPercentage soft: " + this.mSoftPercentageMultiplyRatio + ", hard: " + this.mHardPercentageMultiplyRatio + " can't match 100%");
        }
        if (otherLeftAdj > 0) {
            ConsumeLevelSoftwareInfo other = new ConsumeLevelSoftwareInfo();
            other.commInfo.exactPercentage = ((double) otherLeftAdj) / 100.0d;
            other.commInfo.adjValue = otherLeftAdj;
            other.commInfo.icon = null;
            other.commInfo.labelName = this.mAppContext.getString(R.string.power_other);
            other.aliveStatus = -1;
            other.isShareUidApp = false;
            other.pkgName = "";
            other.uid = -100;
            this.mSoftwareList.add(other);
        }
    }

    private void judgeHardwareType(ConsumeLevelHardwareInfo info) {
        String statParam;
        if (info.type == DrainType.PHONE) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, HARDWARE_PHONE);
            HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_HARDWARE_CLICK, statParam);
        } else if (info.type == DrainType.SCREEN) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, HARDWARE_SCREEN);
            HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_HARDWARE_CLICK, statParam);
        } else if (info.type == DrainType.CELL) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, HARDWARE_CELL);
            HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_HARDWARE_CLICK, statParam);
        } else if (info.type == DrainType.WIFI) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "WIFI");
            HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_HARDWARE_CLICK, statParam);
        } else if (info.type == DrainType.IDLE) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, HARDWARE_IDLE);
            HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_HARDWARE_CLICK, statParam);
        } else if (info.type == DrainType.BLUETOOTH) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, HARDWARE_BLUETOOTH);
            HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_HARDWARE_CLICK, statParam);
        }
    }

    private void doHardwareClick(int childPosition) {
        HwLog.d(TAG, "doHardwareClick " + childPosition);
        ConsumeLevelHardwareInfo info = (ConsumeLevelHardwareInfo) this.mHardwareList.get(childPosition);
        judgeHardwareType(info);
        Bundle hardData = new Bundle();
        hardData.putString("hardLabel", info.commInfo.labelName);
        Intent intent = new Intent();
        intent.putExtras(hardData);
        intent.setClass(getActivity(), DetailOfHardConsumptionActivity.class);
        startActivity(intent);
    }

    private void doSoftwareClick(int childPosition) {
        HwLog.d(TAG, "doSoftwareClick " + childPosition);
        ConsumeLevelSoftwareInfo info = (ConsumeLevelSoftwareInfo) this.mSoftwareList.get(childPosition);
        if (info.uid >= 0) {
            boolean isExist = false;
            if (this.mConsumeAppUidSet != null) {
                for (Integer intValue : this.mConsumeAppUidSet) {
                    if (info.uid == intValue.intValue()) {
                        isExist = true;
                        break;
                    }
                }
            }
            Intent intent = new Intent();
            if (isExist) {
                intent.putExtras(DetailExtConst.sipperToBundle(info.pkgName, info.uid, info.sipper, true));
            } else {
                intent.putExtras(DetailExtConst.sipperToBundle(info.pkgName, info.uid, info.sipper, false));
            }
            if (info.isShareUidApp) {
                intent.putExtras(DetailExtConst.mapToBundle(info.procPowerMap));
            }
            Bundle bundle = new Bundle();
            bundle.putInt(DetailExtConst.ENTER_INTO_DETAIL_TYPE_KEY, 2);
            intent.putExtras(bundle);
            intent.setClass(getActivity(), DetailOfSoftConsumptionActivity.class);
            String statParam;
            if (info.uid == 0) {
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, DetailExtConst.SOFTWARE_ANDROID_OS);
                HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_SOFTWARE_CLICK, statParam);
            } else if (PointerIconCompat.TYPE_ALL_SCROLL == info.uid) {
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, DetailExtConst.SOFTWARE_MEDIA);
                HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_SOFTWARE_CLICK, statParam);
            } else {
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, info.pkgName);
                HsmStat.statE((int) Events.E_POWER_CONSUMELEVLE_SOFTWARE_CLICK, statParam);
            }
            startActivity(intent);
        }
    }
}
