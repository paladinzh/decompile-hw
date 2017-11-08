package com.huawei.systemmanager.spacecleanner.autoclean;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.IBinder;
import android.os.SystemClock;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.util.FileAtTimeCheckUtils;
import com.huawei.systemmanager.spacecleanner.SpaceCleanActivity;
import com.huawei.systemmanager.spacecleanner.SpaceCleannerManager;
import com.huawei.systemmanager.spacecleanner.autoclean.ScanType.AutoScan;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.AnalysisMode;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener.SimleListener;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.statistics.AutoCleanInfo;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.utils.NotificationUtil;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AutoCleanService extends Service {
    public static final String ACTION_START_AUTO_CLEAN = "com.huawei.systemmanager.ACTION.startAutoClean";
    public static final int AUTO_CACHE_CLEAN_MAX_SIZE = 5242880;
    public static final int AUTO_CUSTOM_DATA_CLEAN_TIME = 259200000;
    public static final int AUTO_MAX_SCAN_TIMES = 10;
    private static final String MUTIL_CARD_HOOK = "OFFHOOK";
    private static final String MUTIL_CARD_RING = "RINGING";
    public static final String TAG = "AutoCleanService";
    private AutoCleanTask mAutoCleanTask;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                HwLog.i(AutoCleanService.TAG, "receive action:" + action);
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    AutoCleanService.this.tryToCancelTask();
                } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    AutoCleanService.this.tryToCancelTask();
                } else if (ActionConst.INTENT_CHANGE_POWER_MODE.equals(action)) {
                    if (AutoCleanConst.getInstance().checkIfSuperPowerMode()) {
                        AutoCleanService.this.tryToCancelTask();
                    }
                } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                    String mPhoneReceiverState = intent.getStringExtra("state");
                    HwLog.i(AutoCleanService.TAG, "Phone Receiver State:" + mPhoneReceiverState);
                    if (AutoCleanService.MUTIL_CARD_RING.equals(mPhoneReceiverState) || AutoCleanService.MUTIL_CARD_HOOK.equals(mPhoneReceiverState)) {
                        AutoCleanService.this.tryToCancelTask();
                    }
                }
            }
        }
    };
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    private class AutoCleanTask extends AsyncTask<Void, Void, TrashScanHandler> {
        private AutoCleanInfo mAutoCleanInfo = new AutoCleanInfo();
        private List<AutoScan> mSc1anList;
        private int mStartId;

        public AutoCleanTask(int startId) {
            this.mStartId = startId;
            this.mAutoCleanInfo.setSTime(System.currentTimeMillis());
            this.mSc1anList = AutoCleanConst.getInstance().getScanList();
        }

        protected TrashScanHandler doInBackground(Void... params) {
            HwLog.i(AutoCleanService.TAG, "AutoCleanTask start to wait:5");
            doWait(5);
            long start = SystemClock.elapsedRealtime();
            if (isCancelled()) {
                HwLog.i(AutoCleanService.TAG, "after wait, AutoCleanTask is canceled, return");
                return null;
            }
            HwLog.i(AutoCleanService.TAG, "AutoCleanTask wait end, start to doscan");
            if (AutoCleanConst.getInstance().checkScanCondition(AutoCleanService.this, this.mSc1anList)) {
                TrashScanHandler trashHandler = doScan(300);
                if (isCancelled()) {
                    HwLog.i(AutoCleanService.TAG, "after doScan, AutoCleanTask is canceled, return");
                    return trashHandler;
                }
                doCleanInternal(trashHandler, start);
                if (SpaceCleannerManager.isSupportHwFileAnalysis() && FileAtTimeCheckUtils.isChangeAtTimeSuccess()) {
                    if (isCancelled()) {
                        HwLog.i(AutoCleanService.TAG, "after do clean, AutoCleanTask is canceled, return");
                        return trashHandler;
                    }
                    doAnalysisInternal(trashHandler, start);
                }
                return trashHandler;
            }
            HwLog.i(AutoCleanService.TAG, "checkCondition failed, do not scan");
            return null;
        }

        private void doCleanInternal(TrashScanHandler trashHandler, long start) {
            HwLog.i(AutoCleanService.TAG, "AutoCleanTask scan end, start to doclean");
            if (AutoCleanConst.getInstance().checkCleanCondition(AutoCleanService.this, this.mSc1anList)) {
                doClean(trashHandler, 300);
                HwLog.i(AutoCleanService.TAG, "trash Clean all end. cost time:" + (SystemClock.elapsedRealtime() - start));
                AutoCleanConst.getInstance().saveAutoCleanTime(AutoCleanService.this);
                return;
            }
            HwLog.i(AutoCleanService.TAG, "checkCondition failed, do not start clean");
        }

        private void doAnalysisInternal(TrashScanHandler trashHandler, long start) {
            if (AutoCleanConst.getInstance().checkAnalysisCondition(AutoCleanService.this, this.mSc1anList)) {
                HwLog.i(AutoCleanService.TAG, "start to file analysis");
                doAnalysis(trashHandler);
                HwLog.i(AutoCleanService.TAG, "trash analysis all end. cost time:" + (SystemClock.elapsedRealtime() - start));
                AutoCleanConst.getInstance().saveFileAnaylysisTime(AutoCleanService.this);
                return;
            }
            HwLog.i(AutoCleanService.TAG, "checkCondition failed, do not start analysis");
        }

        protected void onPostExecute(TrashScanHandler trashHandler) {
            doTaskEnd(trashHandler);
        }

        protected void onCancelled(TrashScanHandler trashHandler) {
            doTaskEnd(trashHandler);
        }

        private void doTaskEnd(TrashScanHandler trashHandler) {
            HwLog.i(AutoCleanService.TAG, "AutoCleanTask doTaskEnd");
            if (trashHandler != null) {
                trashHandler.destory();
            }
            this.mAutoCleanInfo.setETime(System.currentTimeMillis());
            SpaceStatsUtils.reportAutoCleanRunResult(this.mAutoCleanInfo);
            AutoCleanService.this.stopSelf(this.mStartId);
        }

        private void doWait(long waitTime) {
            try {
                Thread.sleep(1000 * waitTime);
            } catch (InterruptedException e) {
                HwLog.i(AutoCleanService.TAG, "doWait is interrupt");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        private TrashScanHandler doScan(long scanMaxTime) {
            final CountDownLatch scanLatch = new CountDownLatch(1);
            TrashScanHandler handler = ScanManager.startScan(GlobalContext.getContext(), ScanParams.createAutoScan(AutoCleanConst.getInstance().getAllTrashToScan(GlobalContext.getContext())), new SimleListener() {
                public void onScanEnd(int scannerType, int supportTrashType, boolean canceled) {
                    if (scannerType == 100) {
                        scanLatch.countDown();
                    }
                }
            });
            try {
                if (scanLatch.await(scanMaxTime, TimeUnit.SECONDS)) {
                    HwLog.i(AutoCleanService.TAG, "scan end");
                }
            } catch (InterruptedException e) {
                HwLog.i(AutoCleanService.TAG, "scanLatch catch InterruptedException, when do scan");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (!handler.isScanEnd()) {
                this.mAutoCleanInfo.setScanTimeOut(true);
                HwLog.i(AutoCleanService.TAG, "cancel the scan handler");
                handler.cancelScan();
                try {
                    if (scanLatch.await(5, TimeUnit.SECONDS)) {
                        HwLog.i(AutoCleanService.TAG, "cancel scan end");
                    }
                } catch (InterruptedException e3) {
                    HwLog.i(AutoCleanService.TAG, "cancelLatch catch InterruptedException,");
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
            if (!isCancelled()) {
                return handler;
            }
            HwLog.i(AutoCleanService.TAG, "AutoCleanTask is canceled when startScan, return");
            return handler;
        }

        private void doClean(TrashScanHandler trashHandler, long cleanMaxTime) {
            final CountDownLatch cleanLatch = new CountDownLatch(1);
            List<Trash> trashList = getAutoTrash(trashHandler);
            if (trashList.size() > 0) {
                CleanTask cleanTask = CleanTask.startAutoClean(AutoCleanService.this.getApplicationContext(), trashList, new SimpleListener() {
                    public void onCleanEnd(boolean canceled, long cleanedTrashSize) {
                        AutoCleanTask.this.mAutoCleanInfo.setCleanedTrashSize(cleanedTrashSize);
                        cleanLatch.countDown();
                    }
                });
                try {
                    if (cleanLatch.await(cleanMaxTime, TimeUnit.SECONDS)) {
                        HwLog.i(AutoCleanService.TAG, "clean end");
                    }
                } catch (InterruptedException e) {
                    HwLog.i(AutoCleanService.TAG, "cleanLatch catch InterruptedException");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                if (!cleanTask.isEnd()) {
                    HwLog.i(AutoCleanService.TAG, "clean task not end, do cancel");
                    this.mAutoCleanInfo.setCleanTimeOut(true);
                    cleanTask.cancel();
                }
            }
        }

        private void doAnalysis(TrashScanHandler trashHandler) {
            boolean shouldNotify = false;
            SpaceCleannerManager.getInstance().cancelFileAnalysisNotify();
            int shouldAnalysisTrashType = AutoCleanConst.getInstance().getShouldAnalysisTrashType();
            if (shouldAnalysisTrashType <= 0) {
                HwLog.e(AutoCleanService.TAG, "doAnalysis,but should analysis trash type is invalidate:" + shouldAnalysisTrashType);
                return;
            }
            List<AnalysisMode> analysisModes = AutoCleanConst.getInstance().getAnalysisModes();
            if (analysisModes.size() <= 0) {
                HwLog.e(AutoCleanService.TAG, "doAnalysis,but analysisModes is empty");
                return;
            }
            List<Trash> trashList = getAnalysisTrash(trashHandler, shouldAnalysisTrashType, analysisModes);
            AutoCleanConst.getInstance();
            long notifySize = AutoCleanConst.getAnalysisNotifySize();
            long size = 0;
            for (Trash trash : trashList) {
                if (!trash.isCleaned()) {
                    size += trash.getTrashSize();
                }
            }
            if (size < 0 || notifySize < 0) {
                HwLog.e(AutoCleanService.TAG, "error.size can not be negative.analysis trash size is:" + size + " notifysize is:" + notifySize);
                return;
            }
            if (size >= notifySize) {
                shouldNotify = true;
            }
            if (shouldNotify) {
                HwLog.i(AutoCleanService.TAG, "analysis trash size is:" + size + " notifysize is:" + notifySize + " analysis trash size is exceed notifysize.Should report!");
                showAnalysisNotification(shouldAnalysisTrashType);
            } else {
                HwLog.i(AutoCleanService.TAG, "analysis trash size is:" + size + " notifysize is:" + notifySize + "analysis trash size is not exceed notifysize.Should not report!");
            }
            AutoCleanConst.getInstance().reportAnalysisResult(trashList, shouldNotify);
        }

        private void showAnalysisNotification(int shouldAnalysisTrashType) {
            Context context = GlobalContext.getContext();
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            PendingIntent contentIntent = SpaceCleannerManager.getInstance().createFileAnalysisIntent(context, shouldAnalysisTrashType);
            Builder builder = new Builder(context);
            builder.setSmallIcon(R.drawable.ic_cleanup_notification).setAutoCancel(true).setContentTitle(context.getString(R.string.spaceclean_not_commonly_used_notification_title)).setContentText(context.getString(R.string.spaceclean_not_commonly_used_notification_message)).setContentIntent(contentIntent);
            nm.notify(NotificationUtil.SPACE_ANALYSIS_REPORT_NOTIFY_ID, builder.build());
        }

        private List<Trash> getAnalysisTrash(TrashScanHandler trashHandler, int shouldAnalysisTrashType, List<AnalysisMode> analysisModes) {
            List<Trash> analysisTrashs = Lists.newArrayList();
            List<TrashGroup> groups = trashHandler.getTrashByMixType(shouldAnalysisTrashType);
            if (groups == null) {
                HwLog.i(AutoCleanService.TAG, "getAnalysisTrash groups is null");
                return analysisTrashs;
            }
            for (TrashGroup group : groups) {
                List<AnalysisMode> modes = getModesByType(group.getType(), analysisModes);
                List<Trash> trashList = TrashUtils.getBaseTrashList(group.getTrashList());
                for (AnalysisMode mode : modes) {
                    List<Trash> reportTrashList = mode.shouldReport(trashList);
                    if (reportTrashList != null && reportTrashList.size() > 0) {
                        analysisTrashs.addAll(reportTrashList);
                    }
                }
            }
            return analysisTrashs;
        }

        private List<AnalysisMode> getModesByType(int trashType, List<AnalysisMode> analysisModes) {
            List<AnalysisMode> modes = Lists.newArrayList();
            for (AnalysisMode mode : analysisModes) {
                if ((mode.getAnalysisTrashType() & trashType) > 0) {
                    modes.add(mode);
                }
            }
            return modes;
        }

        private List<Trash> getAutoTrash(TrashScanHandler trashHandler) {
            List<Trash> trashList = Lists.newArrayList();
            Map<Integer, TrashGroup> trashMap = trashHandler.getNormalTrashes();
            Map<Integer, TrashGroup> shouldCleanTrashMap = new HashMap();
            int shouldCleanTrashType = AutoCleanConst.getInstance().getShouldCleanTrashType();
            if (shouldCleanTrashType <= 0) {
                HwLog.e(AutoCleanService.TAG, "getAutoTrash,but should clean trash type is invalidate:" + shouldCleanTrashType);
                return trashList;
            }
            for (Entry<Integer, TrashGroup> entry : trashMap.entrySet()) {
                int trashType = ((Integer) entry.getKey()).intValue();
                if ((trashType & shouldCleanTrashType) != 0) {
                    shouldCleanTrashMap.put(Integer.valueOf(trashType), (TrashGroup) entry.getValue());
                }
            }
            TrashGroup cacheGroup = (TrashGroup) shouldCleanTrashMap.get(Integer.valueOf(1));
            if (cacheGroup != null && cacheGroup.getTrashSize() > 5242880) {
                trashList.add(cacheGroup);
            }
            trashList.addAll(getAutoCustomTrash((TrashGroup) shouldCleanTrashMap.get(Integer.valueOf(81920))));
            return trashList;
        }

        private List<Trash> getAutoCustomTrash(TrashGroup group) {
            List<Trash> trashList = Lists.newArrayList();
            if (group == null) {
                HwLog.e(AutoCleanService.TAG, "getAutoCustomTrash: get no custom data.");
                return trashList;
            }
            List<Trash> listGroup = Lists.newArrayList();
            listGroup.addAll(group.getTrashList());
            for (Trash custom : listGroup) {
                if (custom instanceof AppCustomTrash) {
                    AppCustomTrash autoCustomTrash = ((AppCustomTrash) custom).splitNormalTrash();
                    if (autoCustomTrash != null) {
                        for (Trash itemTrash : autoCustomTrash.getNormalChildren()) {
                            if (isCustomDataNeedClean(itemTrash)) {
                                HwLog.d(AutoCleanService.TAG, "add trash, app = " + autoCustomTrash.getPackageName() + "|size = " + itemTrash.getTrashSize() + "|tyep = TYPE_CUSTOM_AND_TOPVIDEO");
                                trashList.add(itemTrash);
                            }
                        }
                    }
                }
            }
            return trashList;
        }

        private boolean isCustomDataNeedClean(Trash trash) {
            boolean result = true;
            if (trash == null) {
                HwLog.e(AutoCleanService.TAG, "isCustomDataNeedClean: null trash.");
                return false;
            }
            List<String> trashes = trash.getFiles();
            if (trashes == null || trashes.size() == 0) {
                HwLog.e(AutoCleanService.TAG, "isCustomDataNeedClean: empty trash.");
                return false;
            }
            for (String file : trashes) {
                if (System.currentTimeMillis() - FileUtil.getlastModified(file) < 259200000) {
                    result = false;
                    break;
                }
            }
            return result;
        }
    }

    public void onCreate() {
        super.onCreate();
        HwLog.i(TAG, "onCreate, registerReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction(ActionConst.INTENT_CHANGE_POWER_MODE);
        filter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public void onDestroy() {
        super.onDestroy();
        HwLog.i(TAG, "onDestroy, cancel task and unregisterReceiver");
        tryToCancelTask();
        unregisterReceiver(this.mReceiver);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (checkShouldStart(intent)) {
            saveAutoScanTime();
            HwLog.i(TAG, "onStartCommand, start new auto-clean task, startId:" + startId);
            SpaceStatsUtils.reportAutoCleanTaskStartOp();
            this.mAutoCleanTask = new AutoCleanTask(startId);
            this.mAutoCleanTask.executeOnExecutor(SpaceConst.sExecutor, new Void[0]);
        } else {
            HwLog.i(TAG, "checkShouldStart, false, startId:" + startId);
            stopSelf(startId);
        }
        return 2;
    }

    private boolean checkShouldStart(Intent intent) {
        if (intent == null) {
            HwLog.i(TAG, "checkShouldStart failed, intent == null, do not start");
            return false;
        } else if (!ACTION_START_AUTO_CLEAN.equals(intent.getAction())) {
            HwLog.i(TAG, "checkShouldStart failed, not ACTION_START_AUTO_CLEAN, do not start");
            return false;
        } else if (!checkAutoCleanTimesOneDay()) {
            HwLog.i(TAG, "checkAutoCleanTimesOneDay failed, do not start");
            return false;
        } else if (!checkAutoCleanTime()) {
            HwLog.i(TAG, "checkShouldStart failed, clean time, last clean time too near, do not start");
            return false;
        } else if (this.mAutoCleanTask != null && this.mAutoCleanTask.getStatus() != Status.FINISHED) {
            HwLog.i(TAG, "pre auto-clean task is not end, do not start");
            return false;
        } else if (SpaceCleanActivity.checkIfAlive()) {
            HwLog.i(TAG, "there is SpaceCleanActivity alive, do not start");
            return false;
        } else if (checkAnyScanTypeOpen()) {
            return true;
        } else {
            HwLog.i(TAG, "there is no scan type can scan.do not start");
            return false;
        }
    }

    private boolean checkAnyScanTypeOpen() {
        List<AutoScan> autoScans = AutoCleanConst.getInstance().getScanList();
        StringBuilder strBuilder = new StringBuilder();
        boolean isAnyScanOpen = false;
        for (AutoScan scanType : autoScans) {
            boolean shouldStart = scanType.shouldStart(this);
            strBuilder.append(scanType.toString()).append(" should start:").append(shouldStart).append("||");
            if (shouldStart) {
                isAnyScanOpen = true;
            }
        }
        HwLog.i(TAG, strBuilder.toString());
        return isAnyScanOpen;
    }

    private void tryToCancelTask() {
        if (this.mAutoCleanTask == null) {
            HwLog.i(TAG, "tryToCancelTask called, mAutoCleanTask == null");
        } else if (this.mAutoCleanTask.getStatus() == Status.FINISHED) {
            HwLog.i(TAG, "tryToCancelTask called, mAutoCleanTask is finished");
        } else {
            HwLog.i(TAG, "tryToCancelTask called");
            this.mAutoCleanTask.cancel(true);
        }
    }

    private boolean checkAutoCleanTime() {
        long diffTime = System.currentTimeMillis() - getSharedPreferences("space_prefence", 0).getLong(AutoCleanConst.KEY_LAST_CLEAN_TIME, 0);
        if (diffTime < 0 || diffTime > AutoCleanConst.AUTOCLEAN_INTERVAL) {
            return true;
        }
        HwLog.i(TAG, "checkAutoCleanTime failed, diffTime:" + diffTime);
        return false;
    }

    private boolean checkAutoCleanTimesOneDay() {
        String dateString = this.simpleDateFormat.format(Calendar.getInstance().getTime());
        HwLog.i(TAG, "check if the scan times of date is more than max time. date" + dateString);
        int scanTimes = getSharedPreferences("space_prefence_scan", 0).getInt(dateString, -1);
        if (-1 == scanTimes) {
            getSharedPreferences("space_prefence_scan", 0).edit().clear().commit();
            return true;
        } else if (scanTimes < 10) {
            return true;
        } else {
            HwLog.i(TAG, "The times of atuo scan is more than max times");
            return false;
        }
    }

    private void saveAutoScanTime() {
        String dateString = this.simpleDateFormat.format(Calendar.getInstance().getTime());
        int scanTimes = getSharedPreferences("space_prefence_scan", 0).getInt(dateString, 0) + 1;
        getSharedPreferences("space_prefence_scan", 0).edit().putInt(dateString, scanTimes).commit();
        HwLog.i(TAG, "saveAutoScanTime  date:" + dateString + " scanTimes:" + scanTimes);
    }
}
