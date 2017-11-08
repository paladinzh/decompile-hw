package com.huawei.harassmentinterception.numbermark;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.util.HarassmentUtil;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.INumQueryRetListener;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.module.numbermarker.NumQueryReq;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.common.module.numbermarker.NumberMarkEntity;
import tmsdk.common.module.numbermarker.OnNumMarkReportFinish;

public class HsmNumberMarkerManager {
    private static final String HSMSTAT_PROP_ADVERTISE_SALES = "4";
    private static final String HSMSTAT_PROP_ESTATE_AGENT = "2";
    private static final String HSMSTAT_PROP_EXPRESS = "6";
    private static final String HSMSTAT_PROP_HARASS_PHONE = "1";
    private static final String HSMSTAT_PROP_INSURANCE_FINANCING = "3";
    private static final String HSMSTAT_PROP_TAXI = "7";
    private static final String HSMSTAT_PROP_TELEPHONE_FRAND = "5";
    private static final String HW_NUMBERMARK_ONLINE_ACTION = "com.android.contacts.NumberMarkSettings";
    private static final String HW_NUMBERMARK_OPTION_KEY = "hw_numbermark_option";
    private static final String HW_NUMBERMARK_OPTION_USENETWORKS = "hw_numbermark_usenetworks";
    private static final String KEY_RESULT = "result";
    private static final SparseArray<String> NUMBER_TAG_TYPE_TO_HSMID = new SparseArray<String>() {
        {
            put(50, "1");
            put(51, "2");
            put(52, "3");
            put(53, "4");
            put(54, "5");
            put(55, "6");
            put(56, "7");
        }
    };
    private static final int QUERY_NUMMARK_SUCCESS = 0;
    private static final String TAG = "HsmNumberMarkerManager";
    private static final int TAG_TYPE_ADVERTISE_SALES = 53;
    private static final int TAG_TYPE_ESTATE_AGENT = 51;
    private static final int TAG_TYPE_EXPRESS = 55;
    private static final int TAG_TYPE_HARASS_PHONE = 50;
    private static final int TAG_TYPE_INSURANCE_FINANCING = 52;
    private static final int TAG_TYPE_TAXI = 56;
    private static final int TAG_TYPE_TELEPHONE_FRAND = 54;
    private static final long WAIT_TIMEOUT_DFT = 5000;
    private static HsmNumberMarkerManager sInstance = null;
    private Context mContext;
    private Executor mExecutor = new HsmSingleExecutor();
    private AtomicInteger mRequestSequence = new AtomicInteger(0);
    private final String[] mResultColumns = new String[]{"property", "number", "name", "tagType", "tagName", "tagCount", "warning", "usedFor"};
    private NumMarkerManager mTmsNumMarkerManager;

    class HsmNumQueryRetListener implements INumQueryRetListener {
        private HsmNumberQueryRequest mRequestObj;

        public HsmNumQueryRetListener(HsmNumberQueryRequest request) {
            this.mRequestObj = request;
        }

        public void onResult(int resultCode, List<NumQueryRet> list) {
            HwLog.i(HsmNumberMarkerManager.TAG, "onResult: [" + this.mRequestObj.getRequestSequence() + "]" + ", resultCode = " + resultCode);
            if (this.mRequestObj.isExpired() || resultCode != 0) {
                HwLog.i(HsmNumberMarkerManager.TAG, "onResult: [" + this.mRequestObj.getRequestSequence() + "]" + ", Request is expired ,skip");
                return;
            }
            this.mRequestObj.setResultList(list);
            this.mRequestObj.setResult(HsmNumberMarkerManager.this.getCursorFromList(list));
            this.mRequestObj.releaseLock();
        }
    }

    static class HsmNumberQueryRequest {
        AtomicBoolean mExpired = new AtomicBoolean(false);
        Object mLock = new Object();
        int mRequestSequence = 0;
        Cursor mResult = null;
        private List<NumQueryRet> mResultList;
        final List<String> numbers = Lists.newArrayList();

        public HsmNumberQueryRequest(int nRequestSequence, List<String> numbers) {
            this.mRequestSequence = nRequestSequence;
            this.numbers.addAll(numbers);
        }

        public int getRequestSequence() {
            return this.mRequestSequence;
        }

        public List<String> getNumbers() {
            return Collections.unmodifiableList(this.numbers);
        }

        public synchronized Cursor getResult() {
            return this.mResult;
        }

        public void setResultList(List<NumQueryRet> list) {
            this.mResultList = list;
        }

        public List<NumQueryRet> getResultList() {
            return this.mResultList;
        }

        public synchronized void setResult(Cursor result) {
            this.mResult = result;
        }

        public void waitRequestResult(long nTimeOut) {
            synchronized (this.mLock) {
                long begin;
                do {
                    try {
                        if (getResult() != null) {
                            break;
                        }
                        HwLog.i(HsmNumberMarkerManager.TAG, "waitRequestResult: [" + this.mRequestSequence + "], timeout = " + nTimeOut);
                        begin = System.currentTimeMillis();
                        this.mLock.wait(nTimeOut);
                    } catch (InterruptedException e) {
                        HwLog.i(HsmNumberMarkerManager.TAG, "InterruptedException, [" + this.mRequestSequence + "]");
                    }
                } while (System.currentTimeMillis() - begin < nTimeOut);
                this.mExpired.set(true);
            }
        }

        public void releaseLock() {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        }

        public boolean isExpired() {
            return this.mExpired.get();
        }
    }

    private class ReportRunnable implements Runnable {
        private boolean mNeedRepeat;
        private List<NumberMarkEntity> mNumberMarkEntityList;
        private long timeStamp;

        ReportRunnable(long timeStamp, List<NumberMarkEntity> list, boolean repeat) {
            this.timeStamp = timeStamp;
            this.mNumberMarkEntityList = new ArrayList(list);
            this.mNeedRepeat = repeat;
        }

        public void run() {
            if (HarassmentUtil.checkNetworkAvaliable(GlobalContext.getContext())) {
                HwLog.i(HsmNumberMarkerManager.TAG, "begint to do report, timeStamp:" + this.timeStamp);
                List<NumberMarkEntity> numberMarkEntityList = this.mNumberMarkEntityList;
                NumMarkerManager numMarkerManager = HsmNumberMarkerManager.this.getNumberMarkerManager();
                if (numMarkerManager == null) {
                    HwLog.e(HsmNumberMarkerManager.TAG, "try to call cloudReportPhoneNum but mTmsNumMarkerManager is null!");
                    return;
                }
                boolean success = false;
                try {
                    success = numMarkerManager.cloudReportPhoneNum(numberMarkEntityList, new OnNumMarkReportFinish() {
                        public void onReportFinish(int result) {
                            boolean success = result == 0;
                            HwLog.i(HsmNumberMarkerManager.TAG, "onReportFinish, result:" + result + ", sucess:" + success + ", timeStamp:" + ReportRunnable.this.timeStamp);
                            if (!success && ReportRunnable.this.mNeedRepeat) {
                                HwLog.i(HsmNumberMarkerManager.TAG, "report failed, try repeat again");
                                HsmNumberMarkerManager.this.mExecutor.execute(new ReportRunnable(ReportRunnable.this.timeStamp, ReportRunnable.this.mNumberMarkEntityList, false));
                            }
                        }
                    });
                } catch (Exception e) {
                    HwLog.e(HsmNumberMarkerManager.TAG, "call cloudReportPhoneNum error", e);
                }
                if (!success) {
                    HwLog.i(HsmNumberMarkerManager.TAG, "onReport Failed!, timeStamp:" + this.timeStamp);
                }
                return;
            }
            HwLog.i(HsmNumberMarkerManager.TAG, "network not avaliable, don not start report, timeStamp:" + this.timeStamp);
        }
    }

    private HsmNumberMarkerManager(Context context) {
        this.mContext = context;
        getNumberMarkerManager();
    }

    public static synchronized HsmNumberMarkerManager getInstance(Context context) {
        HsmNumberMarkerManager hsmNumberMarkerManager;
        synchronized (HsmNumberMarkerManager.class) {
            if (sInstance == null) {
                sInstance = new HsmNumberMarkerManager(context);
            }
            hsmNumberMarkerManager = sInstance;
        }
        return hsmNumberMarkerManager;
    }

    public Cursor getNumberMarkInfoOnline(List<String> numbers, long timeOut, int requestType) {
        if (UserAgreementHelper.getUserAgreementState(this.mContext) || isContactUseNetwokMark(this.mContext)) {
            if (!(requestType == 18 || requestType == 17 || requestType == 16)) {
                HwLog.w(TAG, "requestType is not supported, so used default TYPE_Common, requestType:" + requestType);
                requestType = 16;
            }
            int nSequence = getNextRequestSequence();
            HwLog.i(TAG, "getNumberMarkInfo: Starts, [" + nSequence + "]");
            HsmNumberQueryRequest request = new HsmNumberQueryRequest(nSequence, numbers);
            cloudFetchNumberInfo(request, requestType);
            if (timeOut <= 0) {
                timeOut = WAIT_TIMEOUT_DFT;
            }
            request.waitRequestResult(timeOut);
            HwLog.i(TAG, "getNumberMarkInfo: Ends, [" + nSequence + "]");
            return request.getResult();
        }
        HwLog.w(TAG, "getNumberMarkInfo: User agreement or online number mark agreement is not agreed, skip");
        return null;
    }

    public static boolean isContactUseNetwokMark(Context context) {
        if (HW_NUMBERMARK_OPTION_USENETWORKS.equals(System.getString(context.getContentResolver(), HW_NUMBERMARK_OPTION_KEY))) {
            HwLog.i(TAG, "isContactUseNetwokMark YES");
            return true;
        }
        HwLog.i(TAG, "isContactUseNetwokMark NO");
        return false;
    }

    public static boolean isContactSupprotNumberMark(Context ctx) {
        return TMSEngineFeature.isSupportTMS() ? hasOnLineActionInContact(ctx) : false;
    }

    private static boolean hasOnLineActionInContact(Context context) {
        boolean z = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(HW_NUMBERMARK_ONLINE_ACTION);
        intent.setPackage(HsmStatConst.CONTACTS_PACKAGE_NAME);
        List resolveInfos = null;
        if (packageManager != null) {
            resolveInfos = PackageManagerWrapper.queryIntentActivities(packageManager, intent, 0);
        }
        if (resolveInfos != null) {
            if (!resolveInfos.isEmpty()) {
                z = true;
            }
            return z;
        }
        HwLog.i(TAG, "action com.android.contacts.NumberMarkSettings is not exist");
        return false;
    }

    public Cursor getNumberMarkInfo(String number, long timeout, int queryType, int requestType) {
        HwLog.i(TAG, "queryType = " + queryType + "requestType =  " + requestType);
        switch (queryType) {
            case 0:
                return getNumberMarkInfoLocal(number);
            case 1:
                return getNumberMarkInfoOnline(HsmCollections.newArrayList(number), timeout, requestType);
            case 2:
                return getNumberMarkInfoAuto(number, timeout, requestType);
            default:
                HwLog.w(TAG, "queryType is not supported " + queryType);
                return null;
        }
    }

    public Cursor getMultiNumberMarkInfo(List<String> numbers, long timeOut, int requestType) {
        return getNumberMarkInfoOnline(numbers, timeOut, requestType);
    }

    private Cursor getNumberMarkInfoAuto(String number, long timeout, int requestType) {
        Cursor onlineCursor = getNumberMarkInfoOnline(HsmCollections.newArrayList(number), timeout, requestType);
        if (onlineCursor != null && onlineCursor.getCount() > 0) {
            return onlineCursor;
        }
        Closeables.close(onlineCursor);
        HwLog.i(TAG, "getNumberMarkInfoAuto online cursor in empty, try local");
        Cursor localCurosr = getNumberMarkInfoLocal(number);
        if (localCurosr == null || localCurosr.getCount() <= 0) {
            HwLog.i(TAG, "getNumberMarkInfoAuto local cursor in empty");
        }
        return localCurosr;
    }

    public Bundle doReport(Bundle bundle) {
        Bundle resBundle = new Bundle();
        resBundle.putBoolean("result", false);
        if (bundle == null) {
            HwLog.e(TAG, "doReport called , param is null!");
            return resBundle;
        } else if (CustomizeWrapper.shouldEnableIntelligentEngine()) {
            String repoter = bundle.getString("repoterPkg");
            if (TextUtils.isEmpty(repoter)) {
                HwLog.e(TAG, "doReport called , but repoterPkg is empty");
                return resBundle;
            } else if (bundle.containsKey("phonenum") && bundle.containsKey("tagtype")) {
                NumberMarkEntity entity = new NumberMarkEntity();
                entity.phonenum = bundle.getString("phonenum");
                entity.tagtype = bundle.getInt("tagtype");
                entity.userDefineName = bundle.getString("userDefineName");
                if (bundle.containsKey("localtagtype")) {
                    entity.localTagType = bundle.getInt("localtagtype");
                    entity.originName = bundle.getString("originname");
                }
                long timeStamp = SystemClock.elapsedRealtime();
                HwLog.i(TAG, "doReport called, repoter:" + repoter + ",tagtype:" + entity.tagtype + ", userDefineName:" + entity.userDefineName + ",localtagtype:" + entity.localTagType + ", originname:" + entity.originName + ", timeStamp:" + timeStamp);
                HsmStat.statE((int) Events.E_HARASSMENT_REPORT_NUMBER_MARK, HsmStatConst.PARAM_VAL, String.valueOf(entity.tagtype));
                this.mExecutor.execute(new ReportRunnable(timeStamp, HsmCollections.newArrayList(entity), true));
                resBundle.putBoolean("result", true);
                return resBundle;
            } else {
                HwLog.e(TAG, "doReport called , but not contain key phonenum or tagtype!");
                return resBundle;
            }
        } else {
            HwLog.w(TAG, "doReport called,intelligent engin not enable, not support,");
            return resBundle;
        }
    }

    private Cursor getNumberMarkInfoLocal(String number) {
        NumMarkerManager markerManager = getNumberMarkerManager();
        if (markerManager == null) {
            HwLog.e(TAG, "cloudFetchNumberInfo: Invalid TMS Number mark manager");
            return null;
        }
        NumQueryRet item = null;
        try {
            item = markerManager.localFetchNumberInfo(number);
        } catch (Exception e) {
            HwLog.e(TAG, "TM SDK localFetchNumberInfo error " + e.getMessage());
        }
        if (item != null) {
            return getCursorFromNumQueryRet(item);
        }
        HwLog.i(TAG, "getNumberMarkInfoLocal Failed");
        return null;
    }

    private int getNextRequestSequence() {
        return this.mRequestSequence.incrementAndGet();
    }

    private synchronized NumMarkerManager getNumberMarkerManager() {
        if (this.mTmsNumMarkerManager != null) {
            return this.mTmsNumMarkerManager;
        }
        Utility.initSDK(this.mContext);
        if (TMSEngineFeature.isSupportTMS()) {
            this.mTmsNumMarkerManager = (NumMarkerManager) ManagerCreatorC.getManager(NumMarkerManager.class);
            if (this.mTmsNumMarkerManager == null) {
                HwLog.e(TAG, "getNumberMarkerManager, getManager is null!");
            }
            return this.mTmsNumMarkerManager;
        }
        HwLog.w(TAG, "initTmsNumMarkManager: TMS feature is not supported");
        return null;
    }

    private void cloudFetchNumberInfo(HsmNumberQueryRequest request, int requestType) {
        NumMarkerManager markerManager = getNumberMarkerManager();
        if (markerManager == null) {
            HwLog.e(TAG, "cloudFetchNumberInfo: Invalid TMS Number mark manager");
            request.releaseLock();
            return;
        }
        List<NumQueryReq> numberList = new ArrayList();
        for (String number : request.getNumbers()) {
            numberList.add(new NumQueryReq(number, requestType));
        }
        HwLog.i(TAG, "cloudFetchNumberInfo: [" + request.getRequestSequence() + "]" + ", resultCode = " + markerManager.cloudFetchNumberInfo(numberList, new HsmNumQueryRetListener(request)));
    }

    private Cursor getCursorFromList(List<NumQueryRet> list) {
        MatrixCursor cursor = new MatrixCursor(this.mResultColumns);
        if (Utility.isNullOrEmptyList(list)) {
            HwLog.w(TAG, "getCursorFromList: Invalid list");
            return cursor;
        }
        NumMarkerManager markerManager = getNumberMarkerManager();
        if (markerManager == null) {
            HwLog.e(TAG, "getCursorFromList markerManager is null!");
            return cursor;
        }
        for (NumQueryRet item : list) {
            String tagName = markerManager.getTagName(item.tagType);
            cursor.addRow(new Object[]{Integer.valueOf(item.property), item.number, item.name, Integer.valueOf(item.tagType), tagName, Integer.valueOf(item.tagCount), item.warning, Integer.valueOf(item.usedFor)});
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, getTagNumber(item.tagType));
            HsmStat.statE((int) Events.E_ANTISPAM_QUERY_NUMBERMARK, statParam);
        }
        return cursor;
    }

    private String getTagNumber(int tagType) {
        String tagValue = (String) NUMBER_TAG_TYPE_TO_HSMID.get(tagType);
        if (TextUtils.isEmpty(tagValue)) {
            return "0";
        }
        return tagValue;
    }

    private Cursor getCursorFromNumQueryRet(NumQueryRet NumQueryRet) {
        List<NumQueryRet> list = new ArrayList();
        list.add(NumQueryRet);
        return getCursorFromList(list);
    }
}
