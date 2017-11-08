package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hsm.MediaTransactWrapper;
import android.os.AsyncTask;
import android.rms.HwSysResManager;
import com.android.internal.os.BatterySipper;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.optimize.process.Predicate.InputMethodPredicate;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.optimize.process.ProtectAppItem;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.stats.PowerStatsException;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.data.xml.PowerWarningParam;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.power.notification.PowerNotificationMgr;
import com.huawei.systemmanager.power.provider.ProviderWrapper;
import com.huawei.systemmanager.power.util.DbmsHelper;
import com.huawei.systemmanager.power.util.L2MAdapter;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HandlePowerStatistic implements IBroadcastHandler {
    private static final String KEY_LAST_NOTIFY_TIME_BEFORE_FREQAPP_EFFECT = "lastNotifyTimeBeforeFreqAppEffect";
    private static final String KEY_NOTIFY_TIMES = "notify_times";
    private static final String KEY_POWER_NOTIFICATION_TIMES = "power_notification_times";
    private static final int LIMIT_TIMES_BEFORE_FREQAPP_EFFECT = 24;
    private static final String REPORT_TYPE_BGPOWER = "BgPower";
    private static final String REPORT_TYPE_WAKELOCK = "Wakelock";
    private static final String TAG = HandlePowerStatistic.class.getSimpleName();
    private static final int TOP_APP_NUM = 5;
    private static Long mNowTime = Long.valueOf(0);
    private Context mContext = null;
    private ArrayList<Integer> mHighPowerList = new ArrayList();
    private int mHighPowerQuickShow = 0;
    private SharedPreferences power_notification;
    private SharedPreferences power_notification_times;

    private class AsyncHandlePowerConsumeAPPsTask extends AsyncTask<Void, Void, Void> {
        private String logType;
        private Map<String, ArrayList> mMap;

        AsyncHandlePowerConsumeAPPsTask(Map<String, ArrayList> map, String logType) {
            this.logType = logType;
            this.mMap = map;
        }

        protected Void doInBackground(Void... params) {
            HandlePowerStatistic.this.handlePowerConsumeAPPs(this.mMap, this.logType);
            return null;
        }
    }

    public void handleBroadcast(Context ctx, Intent intent) {
        String logType;
        this.mContext = ctx;
        this.power_notification = ctx.getSharedPreferences("power_notification", 4);
        this.power_notification_times = ctx.getSharedPreferences(KEY_POWER_NOTIFICATION_TIMES, 4);
        HwLog.i(TAG, "MSG_POWER_STATISTIC Power-hungry apps found.");
        Map<String, ArrayList> map = notificationShowJudger();
        ArrayList<Integer> maxUids = (ArrayList) map.get(ApplicationConstant.HIGH_POWER_UID);
        if (ActionConst.INTENT_POWER_STATISTIC.equals(intent.getAction())) {
            logType = REPORT_TYPE_BGPOWER;
            this.mHighPowerQuickShow = intent.getIntExtra(ApplicationConstant.HIGH_POWER_VIP_SHOW_KEY, 0);
        } else {
            logType = REPORT_TYPE_WAKELOCK;
        }
        if (maxUids.size() != 0) {
            new AsyncHandlePowerConsumeAPPsTask(map, logType).execute(new Void[0]);
        }
        HwLog.d(TAG, "MSG_POWER_STATISTIC deal finished");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Map<String, ArrayList> notificationShowJudger() {
        Map<String, ArrayList> map = new HashMap();
        Object needNotifyUids = new ArrayList();
        ArrayList<UidAndPower> appConsumption = new ArrayList();
        this.mHighPowerList.clear();
        ArrayList<Integer> needNotifyUidsBeforeFreqAppEffect = new ArrayList();
        boolean isFreqAppEffect = true;
        try {
            PowerStatsHelper helper = PowerStatsHelper.newInstance(this.mContext, true);
            appConsumption = (ArrayList) helper.computerBackgroundConsumption(this.mContext, true);
            PowerManagementModel powerManagementModel = PowerManagementModel.getInstance(this.mContext);
            Map<String, ?> tempMap = this.power_notification.getAll();
            Long lastNotifyTime = Long.valueOf(this.power_notification.getLong("lastNotifyTime", 0));
            Long intervalTime = Long.valueOf(((long) PowerWarningParam.getMsgSendInterval(this.mContext)) * 60000);
            mNowTime = Long.valueOf(System.currentTimeMillis());
            int number_add_by_wakeuptimes = 0;
            Long lastNotifyTimeBeforeFreqAppEffect = Long.valueOf(this.power_notification.getLong(KEY_LAST_NOTIFY_TIME_BEFORE_FREQAPP_EFFECT, 0));
            Editor newNotifyAppsEditor = this.power_notification.edit();
            newNotifyAppsEditor.clear();
            newNotifyAppsEditor.commit();
            int highLevelStd = PowerWarningParam.getHigh_level_standard(this.mContext);
            int higtLevelForFreqAppStd = PowerWarningParam.getHigh_level_for_freqapp_standard(this.mContext);
            List<Integer> frequids = getFreqAppUids(this.mContext);
            int notify_times = this.power_notification_times.getInt(KEY_NOTIFY_TIMES, 0);
            isFreqAppEffect = notify_times > 24;
            Editor powerNotificationTimesEditor = this.power_notification_times.edit();
            for (UidAndPower uap : appConsumption) {
                if (this.mContext == null) {
                    HwLog.e(TAG, "notificationShowJudger mContext == null");
                } else {
                    if (tempMap != null) {
                        if (tempMap.containsKey(Integer.toString(uap.getUid()))) {
                            HwLog.v(TAG, "tempMap contains " + uap.getUid());
                        }
                    }
                    boolean isFreqApp = isFreqAppEffect ? frequids.contains(Integer.valueOf(uap.getUid())) : false;
                    int highLevelStdModify = isFreqApp ? higtLevelForFreqAppStd : highLevelStd;
                    HwLog.i(TAG, "uap.power " + uap.getPower() + " uap.uid=" + uap.getUid());
                    if (uap.getPower() < ((double) highLevelStdModify)) {
                    }
                    HwLog.d(TAG, "mNowTime - lastNotifyTime :" + (mNowTime.longValue() - lastNotifyTime.longValue()) + ", intervalTime:" + intervalTime);
                    if (mNowTime.longValue() - lastNotifyTime.longValue() > intervalTime.longValue() && !needNotifyUids.contains(Integer.valueOf(uap.getUid()))) {
                        needNotifyUids.add(Integer.valueOf(uap.getUid()));
                        this.mHighPowerList.add(Integer.valueOf(uap.getUid()));
                        newNotifyAppsEditor.putFloat(Integer.toString(uap.getUid()), (float) uap.getPower());
                    }
                    if (!isFreqAppEffect) {
                        if (uap.getPower() < ((double) higtLevelForFreqAppStd)) {
                        }
                        if (mNowTime.longValue() - lastNotifyTimeBeforeFreqAppEffect.longValue() > intervalTime.longValue() && !needNotifyUidsBeforeFreqAppEffect.contains(Integer.valueOf(uap.getUid()))) {
                            needNotifyUidsBeforeFreqAppEffect.add(Integer.valueOf(uap.getUid()));
                        }
                    }
                    if (!(isFreqApp || uap.getPower() < ((double) PowerWarningParam.getDangerous_level_standard(this.mContext)) || needNotifyUids.contains(Integer.valueOf(uap.getUid())))) {
                        needNotifyUids.add(Integer.valueOf(uap.getUid()));
                        this.mHighPowerList.add(Integer.valueOf(uap.getUid()));
                        newNotifyAppsEditor.putFloat(Integer.toString(uap.getUid()), (float) uap.getPower());
                    }
                    if (this.mHighPowerQuickShow == 1 && uap.getPower() >= ((double) highLevelStdModify) && !needNotifyUids.contains(Integer.valueOf(uap.getUid()))) {
                        needNotifyUids.add(Integer.valueOf(uap.getUid()));
                        this.mHighPowerList.add(Integer.valueOf(uap.getUid()));
                        newNotifyAppsEditor.putFloat(Integer.toString(uap.getUid()), (float) uap.getPower());
                        HwLog.i(TAG, "high power quick show,uid =" + uap.getUid() + ",power= " + uap.getPower());
                    }
                    if (!isFreqAppEffect && this.mHighPowerQuickShow == 1 && uap.getPower() >= ((double) higtLevelForFreqAppStd) && !needNotifyUidsBeforeFreqAppEffect.contains(Integer.valueOf(uap.getUid()))) {
                        needNotifyUidsBeforeFreqAppEffect.add(Integer.valueOf(uap.getUid()));
                        HwLog.i(TAG, "high power quick show before freq app effect,uid =" + uap.getUid() + ",power= " + uap.getPower());
                    }
                    if (mNowTime.longValue() - lastNotifyTime.longValue() > 3600000 && wakeupNumJudge(uap, needNotifyUids, needNotifyUidsBeforeFreqAppEffect, powerManagementModel, newNotifyAppsEditor)) {
                        number_add_by_wakeuptimes++;
                    }
                }
            }
            if (needNotifyUids.size() == 0) {
                newNotifyAppsEditor.putLong("lastNotifyTime", lastNotifyTime.longValue());
            } else {
                newNotifyAppsEditor.putLong("lastNotifyTime", mNowTime.longValue());
                if (!isFreqAppEffect && needNotifyUids.size() - number_add_by_wakeuptimes > 0) {
                    powerNotificationTimesEditor.putInt(KEY_NOTIFY_TIMES, notify_times + 1);
                }
                HwLog.d(TAG, "Power consume notification lastNotifyTime is " + mNowTime);
            }
            if (!isFreqAppEffect) {
                if (needNotifyUidsBeforeFreqAppEffect.size() == 0) {
                    newNotifyAppsEditor.putLong(KEY_LAST_NOTIFY_TIME_BEFORE_FREQAPP_EFFECT, lastNotifyTimeBeforeFreqAppEffect.longValue());
                } else {
                    newNotifyAppsEditor.putLong(KEY_LAST_NOTIFY_TIME_BEFORE_FREQAPP_EFFECT, mNowTime.longValue());
                    HwLog.i(TAG, "Power consume notification lastNotifyTimeBeforeFreqAppEffect is " + mNowTime);
                }
            }
            powerNotificationTimesEditor.commit();
            newNotifyAppsEditor.commit();
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "notificationShowJudger catch IllegalArgumentException ");
            ex.printStackTrace();
        } catch (PowerStatsException ex2) {
            HwLog.e(TAG, "notificationShowJudger catch PowerStatsException ");
            ex2.printStackTrace();
        }
        String str = ApplicationConstant.HIGH_POWER_UID;
        if (!isFreqAppEffect) {
            ArrayList<Integer> needNotifyUids2 = needNotifyUidsBeforeFreqAppEffect;
        }
        map.put(str, needNotifyUids);
        map.put(ApplicationConstant.HIGH_POWER_INFO, appConsumption);
        return map;
    }

    private boolean wakeupNumJudge(UidAndPower uap, ArrayList<Integer> needNotifyUids, ArrayList<Integer> needNotifyUidsBeforeFreqAppEffect, PowerManagementModel pmModel, Editor newNotifyAppsEditor) {
        int wakeUpNum = 0;
        int oldWakeupNum = 0;
        boolean highWakeUpPkg = false;
        boolean hasNewWakeupPkg = false;
        String[] pkgNames = this.mContext.getPackageManager().getPackagesForUid(uap.getUid());
        if (pkgNames != null) {
            for (int i = 0; i < pkgNames.length; i++) {
                Integer oldNum = (Integer) SavingSettingUtil.getWakeUpApp(this.mContext.getContentResolver(), pkgNames[i], 0);
                int newWakeUpNum = SysCoreUtils.getAppWakeUpNum(this.mContext, pkgNames[i]);
                if (oldNum != null) {
                    oldWakeupNum = oldNum.intValue();
                    wakeUpNum = newWakeUpNum - oldWakeupNum;
                } else {
                    wakeUpNum = newWakeUpNum;
                }
                if (wakeUpNum < 0) {
                    HwLog.d(TAG, "newWakeUpNum - oldWakeupNum = " + wakeUpNum);
                    oldWakeupNum = newWakeUpNum;
                    wakeUpNum = 0;
                }
                ProviderWrapper.insertWakeUpDB(this.mContext, pkgNames[i], newWakeUpNum, wakeUpNum);
                HwLog.d(TAG, "notificationShowJudger PkgName = " + pkgNames[i] + "###mWakeUpNum = " + wakeUpNum + "###newWakeUpNum = " + newWakeUpNum + "###oldWakeupNum = " + oldWakeupNum);
                if (wakeUpNum >= 20) {
                    highWakeUpPkg = true;
                }
            }
            HwLog.i(TAG, "notificationShowJudger highWakeUpPkg = " + highWakeUpPkg);
            if (highWakeUpPkg) {
                if (!needNotifyUids.contains(Integer.valueOf(uap.getUid()))) {
                    HwLog.d(TAG, "notificationShowJudger mWakeUpNum!!! ");
                    needNotifyUids.add(Integer.valueOf(uap.getUid()));
                    newNotifyAppsEditor.putFloat(Integer.toString(uap.getUid()), (float) wakeUpNum);
                    hasNewWakeupPkg = true;
                }
                if (!needNotifyUidsBeforeFreqAppEffect.contains(Integer.valueOf(uap.getUid()))) {
                    HwLog.i(TAG, "notificationShowJudger mWakeUpNum before app Effect!!! ");
                    needNotifyUidsBeforeFreqAppEffect.add(Integer.valueOf(uap.getUid()));
                }
            }
        }
        return hasNewWakeupPkg;
    }

    private void handlePowerConsumeAPPs(Map<String, ArrayList> map, String logType) {
        HwLog.d(TAG, "handlePowerConsumeAPPs");
        ArrayList<Integer> mMaxConsumptionAppUids = (ArrayList) map.get(ApplicationConstant.HIGH_POWER_UID);
        ArrayList<UidAndPower> uidAndPowers = (ArrayList) map.get(ApplicationConstant.HIGH_POWER_INFO);
        PackageManager pm = this.mContext.getPackageManager();
        ArrayList<Integer> notifyUidList = new ArrayList();
        ArrayList<String> notifyPkgList = new ArrayList();
        ArrayList<Integer> superHighPowernotifyUidList = new ArrayList();
        ArrayList<String> superHighPowernotifyPkgList = new ArrayList();
        ArrayList<Integer> superHighPowerApps = getSuperHighPowerApp(uidAndPowers);
        for (Integer intValue : mMaxConsumptionAppUids) {
            int appUid = intValue.intValue();
            String[] pkgName = pm.getPackagesForUid(appUid);
            if (pkgName != null) {
                if (superHighPowerApps.contains(Integer.valueOf(appUid))) {
                    HwLog.i(TAG, "handleSuperHighPower");
                    handleSuperHighPower(pkgName, superHighPowernotifyUidList, superHighPowernotifyPkgList, appUid);
                } else {
                    HwLog.i(TAG, "handleTotalControl");
                    handleTotalControl(pkgName, notifyUidList, notifyPkgList, appUid);
                }
            }
        }
        boolean autoClearSuperHighPowerApp = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_HIGH_POWER_SWITCH_KEY, false);
        if (autoClearSuperHighPowerApp && superHighPowernotifyPkgList.size() > 0) {
            PowerManagementModel.getInstance(this.mContext).batchKillApps(superHighPowernotifyPkgList);
            for (String pkg : superHighPowernotifyPkgList) {
                SavingSettingUtil.recordSuperHighPower(this.mContext, pkg);
                HwLog.i(TAG, "clear super high power apps and record high consume apps to db, pkg=" + pkg);
            }
        }
        HwLog.i(TAG, "notifyPkgList1=" + notifyPkgList);
        HwLog.i(TAG, "superHighPowernotifyPkgList1=" + superHighPowernotifyPkgList);
        if (notifyPkgList.size() > 0) {
            if (superHighPowernotifyPkgList.size() <= 0) {
                HandleComm.notifyConsume(this.mContext, notifyPkgList, notifyUidList);
                HwLog.i(TAG, "handlePowerConsumeAPPs notify 1");
            } else if (autoClearSuperHighPowerApp) {
                HandleComm.notifyConsume(this.mContext, notifyPkgList, notifyUidList);
                HandleComm.notifyConsumeForSuperHighPower(this.mContext, superHighPowernotifyPkgList, superHighPowernotifyUidList);
                HwLog.i(TAG, "handlePowerConsumeAPPs notify 4");
            } else {
                boolean bTotalSwitchOn = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, true);
                if (SysCoreUtils.IS_ATT) {
                    bTotalSwitchOn = SharePrefWrapper.getPrefValue(this.mContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, false);
                }
                if (bTotalSwitchOn) {
                    notifyPkgList.addAll(superHighPowernotifyPkgList);
                    notifyUidList.addAll(superHighPowernotifyUidList);
                    HandleComm.notifyConsume(this.mContext, notifyPkgList, notifyUidList);
                    HwLog.i(TAG, "handlePowerConsumeAPPs notifyPkgList2=" + notifyPkgList + " notifyUidList = " + notifyUidList);
                    HwLog.i(TAG, "handlePowerConsumeAPPs notify 2");
                } else {
                    HandleComm.notifyConsumeForSuperHighPower(this.mContext, superHighPowernotifyPkgList, superHighPowernotifyUidList);
                    HwLog.i(TAG, "handlePowerConsumeAPPs notify 3");
                }
            }
        } else if (superHighPowernotifyPkgList.size() > 0) {
            HandleComm.notifyConsumeForSuperHighPower(this.mContext, superHighPowernotifyPkgList, superHighPowernotifyUidList);
            HwLog.i(TAG, "handlePowerConsumeAPPs notify 5");
        }
        if (notifyPkgList.size() == 0 && superHighPowernotifyPkgList.size() == 0) {
            HwLog.i(TAG, "notifyPkgList.size() == 0 && superHighPowernotifyPkgList.size() == 0");
            return;
        }
        try {
            for (String pkg2 : notifyPkgList) {
                DbmsHelper.logHighPowerApp(this.mContext, pkg2, logType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTotalControl(String[] pkgName, ArrayList<Integer> notifyList, ArrayList<String> notifyPkgList, int appUid) {
        ArrayList<String> rogueApp = SavingSettingUtil.rogueDBQuery(this.mContext);
        HwLog.d(TAG, "handlePowerConsumeAPPs rogueApp = " + rogueApp);
        if (pkgName.length >= 1 && SysCoreUtils.checkIsBackground(this.mContext, pkgName[0])) {
            if (this.mHighPowerList.contains(Integer.valueOf(appUid))) {
                SavingSettingUtil.recordHighPower(this.mContext, pkgName[0], rogueApp);
            } else {
                SavingSettingUtil.recordRogue(this.mContext, pkgName[0], rogueApp, 1);
            }
            if (PowerNotificationMgr.isAppNotified(this.mContext, pkgName[0]) && this.mHighPowerQuickShow == 0) {
                HwLog.i(TAG, "handleTotalControl isAppNotified pkgName = " + pkgName[0]);
                return;
            }
            if (!PowerNotificationMgr.isAppIgnore(this.mContext, pkgName[0])) {
                notifyList.add(Integer.valueOf(appUid));
                notifyPkgList.add(pkgName[0]);
            }
        }
    }

    private void handleSuperHighPower(String[] pkgName, ArrayList<Integer> notifyList, ArrayList<String> notifyPkgList, int appUid) {
        HwLog.d(TAG, "handlePowerConsumeAPPs2 rogueApp = " + SavingSettingUtil.rogueDBQuery(this.mContext));
        if (pkgName.length >= 1 && SysCoreUtils.checkIsBackground(this.mContext, pkgName[0])) {
            notifyList.add(Integer.valueOf(appUid));
            notifyPkgList.add(pkgName[0]);
        }
    }

    private ArrayList<Integer> getSuperHighPowerApp(ArrayList<UidAndPower> uidAndPowers) {
        ArrayList<Integer> list = new ArrayList();
        for (UidAndPower tuid : uidAndPowers) {
            if (tuid.getPower() >= 500.0d) {
                BatterySipper sip = tuid.getSipper();
                float hours = (float) (((((double) (((float) L2MAdapter.cpuTime(sip)) - ((float) L2MAdapter.cpuFgTime(sip)))) / 1000.0d) / 60.0d) / 60.0d);
                float costPerHour = (float) (tuid.getPower() / ((double) hours));
                HwLog.i(TAG, "tuid.uid=" + tuid.getUid() + " tuid.power=" + tuid.getPower() + " sip.cpuTime =" + L2MAdapter.cpuTime(sip) + " sip.cpuFgTime=" + L2MAdapter.cpuFgTime(sip) + " hours=" + hours + " costPerHour =" + costPerHour);
                Set<Integer> playingMusicUids = MediaTransactWrapper.playingMusicUidSet();
                Set<Integer> specialApps = getSpecialUids();
                Set<Integer> inputPkgs = getInputMethodUids(this.mContext);
                HwLog.i(TAG, "inputPkgs = " + inputPkgs.toString());
                Set<Integer> protectedApps = getProtectedApps();
                if (!(tuid.getPower() < 500.0d || costPerHour < 500.0f || playingMusicUids.contains(Integer.valueOf(tuid.getUid())) || specialApps.contains(Integer.valueOf(tuid.getUid())) || inputPkgs.contains(Integer.valueOf(tuid.getUid())) || protectedApps.contains(Integer.valueOf(tuid.getUid())))) {
                    list.add(Integer.valueOf(tuid.getUid()));
                    HwLog.i(TAG, "super high power apps uid =" + tuid.getUid());
                }
            }
        }
        return list;
    }

    private Set<Integer> getProtectedApps() {
        Set<Integer> protectedApps = Sets.newHashSet();
        for (ProtectAppItem pai : ProtectAppControl.getInstance(this.mContext).getProtectAppItems()) {
            if (pai.isProtect() && pai.getUid() != -1) {
                protectedApps.add(Integer.valueOf(pai.getUid()));
                HwLog.i(TAG, "pai.getLabel() = " + pai.getLabel());
            }
        }
        return protectedApps;
    }

    private Set<Integer> getSpecialUids() {
        Set<Integer> set = new HashSet();
        ApplicationInfo mm = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.tencent.mm");
        ApplicationInfo qq = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.tencent.mobileqq");
        ApplicationInfo soundrecorder = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.android.soundrecorder");
        ApplicationInfo jingdong = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.jingdong.app.mall");
        ApplicationInfo Fitbit = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.fitbit.FitbitMobile");
        ApplicationInfo gaodeditu = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.autonavi.minimap");
        ApplicationInfo tencentWeibo = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.tencent.WBlog");
        ApplicationInfo sinaWeibo = SysCoreUtils.getAppInfoByPackageName(this.mContext, "com.sina.weibo");
        if (mm != null) {
            set.add(Integer.valueOf(mm.uid));
        }
        if (qq != null) {
            set.add(Integer.valueOf(qq.uid));
        }
        if (soundrecorder != null) {
            set.add(Integer.valueOf(soundrecorder.uid));
        }
        if (jingdong != null) {
            set.add(Integer.valueOf(jingdong.uid));
        }
        if (Fitbit != null) {
            set.add(Integer.valueOf(Fitbit.uid));
        }
        if (gaodeditu != null) {
            set.add(Integer.valueOf(gaodeditu.uid));
        }
        if (tencentWeibo != null) {
            set.add(Integer.valueOf(tencentWeibo.uid));
        }
        if (sinaWeibo != null) {
            set.add(Integer.valueOf(sinaWeibo.uid));
        }
        return set;
    }

    private Set<Integer> getInputMethodUids(Context ctx) {
        Set<Integer> uids = Sets.newHashSet();
        List<String> inputPkgs = InputMethodPredicate.getInputMethod(ctx);
        PackageManager pm = ctx.getPackageManager();
        for (String pkg : inputPkgs) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                if (info != null) {
                    uids.add(Integer.valueOf(info.uid));
                }
            } catch (NameNotFoundException ex) {
                HwLog.e(TAG, "getInputMethodUids catch NameNotFoundException: " + ex.getMessage());
            }
        }
        HwLog.d(TAG, "getInputMethodUids pkgs: " + inputPkgs + ", uids: " + uids);
        return uids;
    }

    private List<Integer> getFreqAppUids(Context context) {
        List<Integer> frequids = new ArrayList();
        if (context == null) {
            return frequids;
        }
        List<String> freqApps = HwSysResManager.getInstance().getMostFrequentUsedApps(5);
        PackageManager pm = this.mContext.getPackageManager();
        if (freqApps != null) {
            HwLog.i(TAG, "freqApps = " + freqApps.toString());
            for (String app : freqApps) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(app, 1);
                    if (ai != null) {
                        frequids.add(Integer.valueOf(ai.uid));
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return frequids;
    }
}
