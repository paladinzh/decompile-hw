package com.android.contacts.hap.numbermark;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.contacts.Collapser;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.blacklist.BlacklistCommonUtils;
import com.android.contacts.hap.numbermark.base.HWCallApiFactory;
import com.android.contacts.hap.numbermark.base.ISDKCallApi;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.NumberMark;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.NumberMarkExtras;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.utils.FixedPhoneNumberMatchUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NumberMarkManager {
    private static String EROR_MSG = "error_msg";
    private static Map<String, Integer> sMarkMap = new HashMap();
    private Callback mCallback;
    private Context mContext;
    private String maritimeName = "";

    public interface Callback {
        void onServiceConnected();
    }

    public interface CapabilityQueryCallback {
        void refresh(List<CapabilityInfo> list);
    }

    public static final class NumberMarkExtrasQuery {
        static final String[] MARK_NUMBER_EXTRAS_COLUMNS = new String[]{"NUMBER", "TITLE", "CONTENT", "TYPE", "EXTERNAL_LINK", "INTERNAL_LINK", "TIMESTAMP"};
    }

    public static final class NumberMarkQuery {
        static final String[] MARK_NUMBER_COLUMNS = new String[]{"NUMBER", "CLASSIFY", "NAME", "MARKED_COUNT", "IS_CLOUD", "DESCRIPTION", "SAVE_TIMESTAMP", "SUPPLIER"};
    }

    private static class ReportContent {
        private int localtagtype;
        private String originname;
        private String phonenum;
        private int tagtype;
        private String userDefineName;

        public ReportContent(String phonenum, int tagtype, String userDefineName) {
            this.phonenum = phonenum;
            this.tagtype = tagtype;
            this.userDefineName = userDefineName;
        }

        public ReportContent(String phonenum, int tagtype, String userDefineName, int localtagtype, String originname) {
            this.phonenum = phonenum;
            this.tagtype = tagtype;
            this.userDefineName = userDefineName;
            this.localtagtype = localtagtype;
            this.originname = originname;
        }

        public String getPhoneNum() {
            return this.phonenum;
        }

        public int getTagType() {
            return this.tagtype;
        }

        public String getUserDefineName() {
            return this.userDefineName;
        }

        public int getLocalTagType() {
            return this.localtagtype;
        }

        public String getOriginName() {
            return this.originname;
        }
    }

    static class SearchCallable implements Callable<NumberMarkInfo> {
        private String callType;
        private String phoneNum;
        private ISDKCallApi sdk;

        public SearchCallable(String phoneNum, ISDKCallApi sdk, String callType) {
            this.phoneNum = phoneNum;
            this.sdk = sdk;
            this.callType = callType;
        }

        public NumberMarkInfo call() throws Exception {
            return this.sdk.getInfoByNum(this.phoneNum, this.callType);
        }
    }

    class SearchNumMarkFromSupplierThread extends Thread {
        private String callType = null;
        private boolean isGetResult = false;
        private Object lock;
        private NumberMarkInfo supplierNMI = null;
        private String targetNum = null;

        public SearchNumMarkFromSupplierThread(Object lock, String targetNum, String callType) {
            this.lock = lock;
            this.targetNum = targetNum;
            this.callType = callType;
        }

        public void run() {
            if (this.targetNum != null) {
                this.supplierNMI = NumberMarkManager.this.getCloudMarkFromSupplier(this.targetNum, this.callType);
                setIsGetResult(true);
                synchronized (this.lock) {
                    this.lock.notifyAll();
                }
            }
        }

        public boolean getIsGetResult() {
            return this.isGetResult;
        }

        public void setIsGetResult(boolean isGetResult) {
            this.isGetResult = isGetResult;
        }

        public NumberMarkInfo getSupplierNMI() {
            return this.supplierNMI;
        }
    }

    public NumberMarkManager(Context context, Callback cb) {
        initMarkMap();
        this.mContext = context.getApplicationContext();
        if (this.mContext != null) {
            this.maritimeName = this.mContext.getString(R.string.contacts_str_filter_Maritime_Satellite_calls);
            if (RefelctionUtils.isAppInstalled(this.mContext, "huawei.w3")) {
                NumberMarkUtil.setW3AppInstalled(true);
            } else {
                NumberMarkUtil.setW3AppInstalled(false);
            }
        }
        this.mCallback = cb;
        if (this.mCallback != null) {
            this.mCallback.onServiceConnected();
        }
    }

    public void enableCloudMark(boolean isEnable) {
        if (isEnable) {
            System.putString(this.mContext.getContentResolver(), "hw_numbermark_option", "hw_numbermark_usenetworks");
        } else {
            System.putString(this.mContext.getContentResolver(), "hw_numbermark_option", "hw_numbermark_uselocal");
        }
    }

    public NumberMarkInfo getLocalMark(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        Cursor cursor = getLocalMarkCursor(this.mContext, standardizationPhoneNum(phoneNumber, this.mContext));
        if (cursor == null) {
            HwLog.e("NumberMarkManager", "getLocalMarkList with cursor is null error!");
            return null;
        }
        try {
            NumberMarkInfo revertCursorToNumberMarkInfo;
            if (cursor.getCount() > 1) {
                while (cursor.moveToNext()) {
                    boolean isCloudMark;
                    if (cursor.getInt(4) != 0) {
                        isCloudMark = true;
                        continue;
                    } else {
                        isCloudMark = false;
                        continue;
                    }
                    if (!isCloudMark) {
                        revertCursorToNumberMarkInfo = revertCursorToNumberMarkInfo(cursor);
                        return revertCursorToNumberMarkInfo;
                    }
                }
            } else if (cursor.moveToFirst()) {
                revertCursorToNumberMarkInfo = revertCursorToNumberMarkInfo(cursor);
                cursor.close();
                return revertCursorToNumberMarkInfo;
            }
            cursor.close();
            return null;
        } finally {
            cursor.close();
        }
    }

    public static Cursor getLocalMarkCursor(Context context, String phoneNumber) {
        String[] selectionArgs = new String[]{new PhoneMatch(phoneNumber).getMatchPhone()};
        return ContactsAppDatabaseHelper.getInstance(context).getReadableDatabase().query("number_mark", NumberMarkQuery.MARK_NUMBER_COLUMNS, "NUMBER=?", selectionArgs, null, null, null);
    }

    private Cursor getLocalMarkCursor(String phoneNumber) {
        String[] selectionArgs = new String[]{new PhoneMatch(phoneNumber).getMatchPhone(), "0"};
        return ContactsAppDatabaseHelper.getInstance(this.mContext).getReadableDatabase().query("number_mark", NumberMarkQuery.MARK_NUMBER_COLUMNS, "NUMBER=? and IS_CLOUD=?", selectionArgs, null, null, null);
    }

    public static String standardizationPhoneNum(String num, Context context) {
        String noCountryHeadNum = null;
        if (num == null) {
            return null;
        }
        num = iPHeadBarber(removeDashesAndBlanksBrackets(num));
        if (IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging() && !num.startsWith("+86")) {
            String countryIso = CountryMonitor.getInstance(context).getCountryIso();
            if (countryIso != null) {
                noCountryHeadNum = PhoneNumberUtils.formatNumberToE164(num, countryIso);
            }
            if (!TextUtils.isEmpty(noCountryHeadNum) && noCountryHeadNum.startsWith("+86")) {
                String noPrefixNum = noCountryHeadNum.substring(3);
                if (noPrefixNum.matches("1\\d{10}$")) {
                    noCountryHeadNum = noPrefixNum;
                } else if (noPrefixNum.startsWith("400") || noPrefixNum.startsWith("800")) {
                    noCountryHeadNum = noPrefixNum;
                } else {
                    noCountryHeadNum = "0" + noPrefixNum;
                }
                num = noCountryHeadNum;
            }
        }
        if (num.matches("0(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}")) {
            num = num.substring(1);
        }
        if (num.equals("86") || num.equals("+86") || num.equals("0086")) {
            return num;
        }
        if (num.startsWith("86")) {
            num = num.substring(2);
        } else if (num.startsWith("+86")) {
            num = num.substring(3);
        } else if (num.startsWith("0086")) {
            num = num.substring(4);
        }
        return num;
    }

    private static String iPHeadBarber(String oriNumber) {
        String result = oriNumber;
        String[] IPHEAD = new String[]{"17900", "17901", "17908", "17909", "11808", "17950", "17951", "12593", "17931", "17910", "17911", "17960", "17968", "17969", "10193", "96435"};
        int numberLen = oriNumber.length();
        if (numberLen < 5) {
            return oriNumber;
        }
        String ipHead = oriNumber.substring(0, 5);
        if (ipHead.equals(IPHEAD[0]) || ipHead.equals(IPHEAD[1]) || ipHead.equals(IPHEAD[2]) || ipHead.equals(IPHEAD[3]) || ipHead.equals(IPHEAD[4]) || ipHead.equals(IPHEAD[5]) || ipHead.equals(IPHEAD[6]) || ipHead.equals(IPHEAD[7]) || ipHead.equals(IPHEAD[8]) || ipHead.equals(IPHEAD[9]) || ipHead.equals(IPHEAD[10]) || ipHead.equals(IPHEAD[11]) || ipHead.equals(IPHEAD[12]) || ipHead.equals(IPHEAD[13]) || ipHead.equals(IPHEAD[14]) || ipHead.equals(IPHEAD[15])) {
            result = oriNumber.substring(5, numberLen);
        }
        return result;
    }

    private MatrixCursor updateLocalMark(String phoneNumber, NumberMarkInfo info, boolean dataExitInLocalDb, String type) {
        if (dataExitInLocalDb) {
            if (info != null) {
                String phone = new PhoneMatch(phoneNumber).getMatchPhone();
                HwLog.i("NumberMarkManager", "contacts loacal db has this number info, update it when get result");
                updateColums(phone, createContentValues(info), info.isCloudMark(), type);
                return createMatrixCursor(info.getNumber(), info.getClassify(), info.getName(), info.getMarkedCount(), info.isCloudMark(), info.getDescription(), null, info.getSupplier());
            }
            HwLog.i("NumberMarkManager", "contacts loacal db has this number info, delete it when server not has info");
            deleteColums(NumberMark.CONTENT_URI, new PhoneMatch(standardizationPhoneNum(phoneNumber, this.mContext)).getMatchPhone(), true, type);
            return createSymbolMatrixCursor(phoneNumber);
        } else if (info == null) {
            HwLog.i("NumberMarkManager", "contacts loacal no this number info, return symbol matrix cursor");
            return createSymbolMatrixCursor(phoneNumber);
        } else if ("connect overtime".equals(info.getErrorMsg())) {
            HwLog.i("NumberMarkManager", "contacts loacal no this number info, return timeout");
            return createTimeoutMatrixCursor(phoneNumber);
        } else {
            HwLog.i("NumberMarkManager", "contacts loacal no this number info, insert it when get result");
            insertColum(NumberMark.CONTENT_URI, "number_mark", createContentValues(info), type);
            return createMatrixCursor(info.getNumber(), info.getClassify(), info.getName(), info.getMarkedCount(), info.isCloudMark(), info.getDescription(), null, info.getSupplier());
        }
    }

    private NumberMarkInfo getMark(String phoneNumber, String callType) {
        if (!isCloudMarkFeatureEnable()) {
            return null;
        }
        NumberMarkInfo finalInfo;
        if (isCloudMarkSwitchOpen()) {
            HwLog.i("NumberMarkManager", "switch open");
            finalInfo = getCloudMark(phoneNumber, callType);
        } else {
            HwLog.i("NumberMarkManager", "switch close");
            finalInfo = getPresetMark(phoneNumber);
        }
        return finalInfo;
    }

    protected NumberMarkInfo getCloudMark(String phoneNumber, String callType) {
        NumberMarkInfo finalInfo = null;
        if (!NumberMarkUtil.isW3AppInstalled()) {
            return getCloudMarkFromSupplier(phoneNumber, callType);
        }
        Object lock = new Object();
        SearchNumMarkFromSupplierThread sThread = new SearchNumMarkFromSupplierThread(lock, phoneNumber, callType);
        ContactsThreadPool.getInstance().execute(sThread);
        NumberMarkInfo w3NMI = getCloudMarkFromW3(FixedPhoneNumberMatchUtils.parseFixedPhoneNumber(this.mContext, phoneNumber));
        if (w3NMI == null || "connect overtime".equals(w3NMI.getErrorMsg())) {
            if (w3NMI == null) {
                HwLog.i("NumberMarkManager", "no info on 3 server");
            } else {
                HwLog.i("NumberMarkManager", "get info on 3 server timeout");
            }
            NumberMarkInfo supplierNMI;
            if (sThread.getIsGetResult()) {
                supplierNMI = sThread.getSupplierNMI();
                if (supplierNMI != null) {
                    return supplierNMI;
                }
                if (w3NMI != null) {
                    return w3NMI;
                }
                return null;
            }
            synchronized (lock) {
                while (!sThread.getIsGetResult()) {
                    try {
                        lock.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                supplierNMI = sThread.getSupplierNMI();
                if (supplierNMI != null) {
                    finalInfo = supplierNMI;
                } else if (w3NMI != null) {
                    finalInfo = w3NMI;
                }
            }
            return finalInfo;
        }
        finalInfo = w3NMI;
        HwLog.i("NumberMarkManager", "get info from 3 server success!");
        return finalInfo;
    }

    private NumberMarkInfo getPresetMark(String phoneNumber) {
        ISDKCallApi sdk = HWCallApiFactory.getInstance().getSDKCallApiByName("tencent", this.mContext);
        NumberMarkInfo info = null;
        if (sdk != null) {
            info = sdk.getInfoFromPresetDB(phoneNumber);
            if (info != null && info.getMarkedCount() == 501) {
                info.setMarkedCount(-501);
            }
        }
        return info;
    }

    private NumberMarkInfo getMarkInfoByCallable(String phoneNum, ISDKCallApi sdk, String callType) {
        if (sdk == null) {
            HwLog.i("NumberMarkManager", "mark sdk not exist");
            return null;
        }
        ExecutorService exec = ContactsThreadPool.getInstance().getExecutorservice();
        if (exec == null) {
            return null;
        }
        NumberMarkInfo numberMarkInfo;
        try {
            numberMarkInfo = (NumberMarkInfo) exec.submit(new SearchCallable(phoneNum, sdk, callType)).get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            numberMarkInfo = new NumberMarkInfo("connect overtime");
            HwLog.i("NumberMarkManager", sdk.toString() + " search timeout");
        } catch (Exception e2) {
            numberMarkInfo = null;
        }
        return numberMarkInfo;
    }

    private NumberMarkInfo getCloudMarkFromW3(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 11 || !phoneNumber.startsWith(CallInterceptDetails.BRANDED_STATE)) {
            return null;
        }
        ISDKCallApi sdk = HWCallApiFactory.getInstance().getSDKCallApiByName("w3", this.mContext);
        NumberMarkInfo info = null;
        if (sdk != null) {
            HwLog.i("NumberMarkManager", "start get mobile info from 3 server.");
            info = getMarkInfoByCallable(phoneNumber, sdk, null);
        }
        return info;
    }

    private NumberMarkInfo getCloudMarkFromSupplier(String phoneNumber, String callType) {
        if (phoneNumber.length() < 10) {
            return null;
        }
        NumberMarkInfo info;
        if ("toms".equals(HWCallApiFactory.getSupplier(phoneNumber))) {
            ISDKCallApi sdk = HWCallApiFactory.getInstance().getSDKCallApiByName("toms", this.mContext);
            HwLog.i("NumberMarkManager", "fix search start.");
            info = getMarkInfoByCallable(phoneNumber, sdk, callType);
            if (info == null) {
                HwLog.i("NumberMarkManager", "no this fix info on 1 server, we will search in present.");
                info = getPresetMark(phoneNumber);
                if (info == null) {
                    HwLog.i("NumberMarkManager", "no this fix info in present, we will search on 2 server.");
                    info = getMarkInfoByCallable(phoneNumber, HWCallApiFactory.getInstance().getSDKCallApiByName("tencent", this.mContext), callType);
                }
            } else if ("connect overtime".equals(info.getErrorMsg())) {
                HwLog.i("NumberMarkManager", "get fix info from 1 server timeout, we will search in present.");
                NumberMarkInfo tempInfo = info;
                info = getPresetMark(phoneNumber);
                if (info == null) {
                    HwLog.i("NumberMarkManager", "no this fix info in present, we will return timeout.");
                    info = tempInfo;
                }
            }
        } else {
            HwLog.i("NumberMarkManager", "mobile search start.");
            info = getPresetMark(phoneNumber);
            if (info == null) {
                HwLog.i("NumberMarkManager", "no this mobile info in present db, we will search on 2 server.");
                info = getMarkInfoByCallable(phoneNumber, HWCallApiFactory.getInstance().getSDKCallApiByName("tencent", this.mContext), callType);
            }
        }
        if (info == null) {
            HwLog.i("NumberMarkManager", "supplier final info is null, search end.");
        } else if ("connect overtime".equals(info.getErrorMsg())) {
            HwLog.i("NumberMarkManager", "supplier final timeout, search end.");
        } else {
            HwLog.i("NumberMarkManager", "supplier final info get success, search end.");
        }
        return info;
    }

    private List<CapabilityInfo> getCloudMarkExtras(List<CapabilityInfo> totalList, String matchNum, String originNum, boolean exitsInDb) {
        ISDKCallApi sdk = HWCallApiFactory.getInstance().getSDKCallApiByNum(originNum, this.mContext);
        if (exitsInDb) {
            deleteColums(NumberMarkExtras.CONTENT_URI, matchNum);
        }
        if (isCloudMarkEnable() && sdk != null) {
            List<CapabilityInfo> eachInfos = sdk.getExtraInfoByNum(originNum);
            if (eachInfos != null) {
                if (eachInfos.size() == 0) {
                    insertColum(NumberMarkExtras.CONTENT_URI, "number_mark_extras", createNullDataContentValues(matchNum), null);
                } else {
                    totalList.addAll(eachInfos);
                    for (CapabilityInfo info : eachInfos) {
                        insertColum(NumberMarkExtras.CONTENT_URI, "number_mark_extras", createContentValues(matchNum, info), null);
                    }
                }
            }
        }
        return totalList;
    }

    protected List<CapabilityInfo> getCapabilityInfo(String[] numbers) {
        List<CapabilityInfo> infos = new ArrayList();
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = standardizationPhoneNum(numbers[i], this.mContext);
        }
        for (String num : numbers) {
            if (num.length() >= 5) {
                if (!num.matches("1\\d{10}$")) {
                    String matchNum = new PhoneMatch(num).getMatchPhone();
                    Cursor queryCursor = this.mContext.getContentResolver().query(NumberMarkExtras.CONTENT_URI, NumberMarkExtrasQuery.MARK_NUMBER_EXTRAS_COLUMNS, "NUMBER=?", new String[]{matchNum}, null);
                    if (queryCursor == null || queryCursor.getCount() <= 0) {
                        if (queryCursor != null) {
                            queryCursor.close();
                        }
                        infos = getCloudMarkExtras(infos, matchNum, num, false);
                    } else {
                        boolean flag = true;
                        while (queryCursor.moveToNext() && flag) {
                            if (isCloudDataOverTime(queryCursor.getLong(6), "toms")) {
                                infos = getCloudMarkExtras(infos, matchNum, num, true);
                                flag = false;
                            } else {
                                CapabilityInfo info = revertCursorToCapability(queryCursor);
                                if (info != null) {
                                    infos.add(info);
                                }
                            }
                        }
                        queryCursor.close();
                    }
                }
            }
        }
        Collapser.collapseList(infos, false);
        Collections.sort(infos);
        return infos;
    }

    public void getCapabilityInfoAsync(final String[] numbers, final CapabilityQueryCallback cb) {
        new AsyncTask<Void, Void, List<CapabilityInfo>>() {
            protected void onPostExecute(List<CapabilityInfo> result) {
                if (result != null && result.size() > 0) {
                    cb.refresh(result);
                }
            }

            protected List<CapabilityInfo> doInBackground(Void... params) {
                int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(10);
                List<CapabilityInfo> list = NumberMarkManager.this.getCapabilityInfo(numbers);
                Process.setThreadPriority(origPri);
                return list;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public synchronized Cursor getNumberMarkCursor(String originNumber, String callType, String type) {
        if (TextUtils.isEmpty(originNumber)) {
            HwLog.i("NumberMarkManager", "number mark:search number is null");
            return createSymbolMatrixCursor(originNumber);
        }
        String phoneNumber = standardizationPhoneNum(originNumber, this.mContext);
        Cursor cursor = getLocalMarkCursor(this.mContext, phoneNumber);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            if (IsPhoneNetworkRoamingUtils.isMaritimeSatelliteNumber(originNumber)) {
                HwLog.i("NumberMarkManager", "return satelite mark");
                updateCallLog(this.mContext, originNumber, "sate", this.maritimeName, 0, true, false);
                return createMatrixCursor(originNumber, "sate", this.maritimeName, 0, true, null, null, null);
            }
            return updateLocalMark(phoneNumber, getMark(phoneNumber, callType), false, type);
        }
        Object[] cursorArray = getCursorArray(cursor);
        boolean isFromCloud = ((Boolean) cursorArray[0]).booleanValue();
        long saveTimeStamp = ((Long) cursorArray[1]).longValue();
        String classify = cursorArray[2];
        String name = cursorArray[3];
        String supplier = cursorArray[4];
        if (!isFromCloud) {
            cursor.close();
            HwLog.i("NumberMarkManager", "return user mark");
            return createMatrixCursor(originNumber, classify, name, 0, isFromCloud, null, null, null);
        } else if (isCloudDataOverTime(saveTimeStamp, supplier)) {
            info = getMark(phoneNumber, callType);
            if (info == null || !"connect overtime".equals(info.getErrorMsg())) {
                cursor.close();
                return updateLocalMark(phoneNumber, info, true, type);
            }
            HwLog.i("NumberMarkManager", "contacts loacal db has this number info, not delete it when time out");
            return cursor;
        } else if (CommonUtilMethods.isNetworkWifi(this.mContext)) {
            info = getMark(phoneNumber, callType);
            if (info == null) {
                cursor.close();
                HwLog.i("NumberMarkManager", "under wifi, delete local db data when server has no this number info");
                deleteColums(NumberMark.CONTENT_URI, new PhoneMatch(standardizationPhoneNum(phoneNumber, this.mContext)).getMatchPhone(), true, type);
                return createSymbolMatrixCursor(phoneNumber);
            } else if ("connect overtime".equals(info.getErrorMsg())) {
                HwLog.i("NumberMarkManager", "under wifi, return local db result when search mark timeout");
                return cursor;
            } else {
                String phone = new PhoneMatch(phoneNumber).getMatchPhone();
                HwLog.i("NumberMarkManager", "under wifi, return result when get result success");
                updateColums(phone, createContentValues(info), info.isCloudMark(), type);
                cursor.close();
                return createMatrixCursor(info.getNumber(), info.getClassify(), info.getName(), info.getMarkedCount(), info.isCloudMark(), info.getDescription(), null, info.getSupplier());
            }
        } else {
            HwLog.i("NumberMarkManager", "under data service, return local db data");
            return cursor;
        }
    }

    private MatrixCursor createSymbolMatrixCursor(String phoneNumber) {
        return createMatrixCursor(phoneNumber, null, null, 0, false, null, null, null);
    }

    private MatrixCursor createTimeoutMatrixCursor(String phoneNumber) {
        int markColumnsLength = NumberMarkQuery.MARK_NUMBER_COLUMNS.length;
        String[] timeoutMatrixItems = new String[(markColumnsLength + 1)];
        for (int i = 0; i < markColumnsLength; i++) {
            timeoutMatrixItems[i] = NumberMarkQuery.MARK_NUMBER_COLUMNS[i];
        }
        timeoutMatrixItems[markColumnsLength] = EROR_MSG;
        MatrixCursor matCursor = new MatrixCursor(timeoutMatrixItems);
        matCursor.addRow(new Object[]{phoneNumber, null, null, Integer.valueOf(0), Integer.valueOf(0), null, null, null, "connect overtime"});
        return matCursor;
    }

    private MatrixCursor createMatrixCursor(String num, String classify, String name, int markedCount, boolean isCloudMark, String description, String saveTimeStamp, String supplier) {
        int i = 1;
        MatrixCursor matCursor = new MatrixCursor(NumberMarkQuery.MARK_NUMBER_COLUMNS);
        Object[] objvalues = new Object[8];
        objvalues[0] = num;
        objvalues[1] = classify;
        objvalues[2] = name;
        objvalues[3] = Integer.valueOf(markedCount);
        if (!isCloudMark) {
            i = 0;
        }
        objvalues[4] = Integer.valueOf(i);
        objvalues[5] = description;
        objvalues[6] = saveTimeStamp;
        objvalues[7] = supplier;
        matCursor.addRow(objvalues);
        return matCursor;
    }

    private Object[] getCursorArray(Cursor cursor) {
        boolean z = false;
        long saveTimestamp = -1;
        String classify = "";
        String name = "";
        String supplier = "";
        int markCount = -1;
        if (cursor.getCount() > 1) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(4) == 1) {
                    z = true;
                    continue;
                } else {
                    z = false;
                    continue;
                }
                if (!z) {
                    classify = cursor.getString(1);
                    name = cursor.getString(2);
                    break;
                }
            }
        } else if (cursor.moveToFirst()) {
            z = cursor.getInt(4) == 1;
            classify = cursor.getString(1);
            name = cursor.getString(2);
            if (z) {
                saveTimestamp = cursor.getLong(6);
                supplier = cursor.getString(7);
                markCount = cursor.getInt(3);
            }
        }
        return new Object[]{Boolean.valueOf(z), Long.valueOf(saveTimestamp), classify, name, supplier, Integer.valueOf(markCount)};
    }

    private boolean isCloudDataOverTime(long saveTimestamp, String supplier) {
        boolean z = true;
        if ("toms".equals(supplier)) {
            if (System.currentTimeMillis() - saveTimestamp <= 259200000) {
                z = false;
            }
            return z;
        } else if ("tencent".equals(supplier)) {
            if (System.currentTimeMillis() - saveTimestamp <= 86400000) {
                z = false;
            }
            return z;
        } else if (!"w3".equals(supplier)) {
            return false;
        } else {
            if (System.currentTimeMillis() - saveTimestamp <= 259200000) {
                z = false;
            }
            return z;
        }
    }

    private ContentValues createContentValues(NumberMarkInfo info) {
        if (info == null) {
            return null;
        }
        ContentValues cv = new ContentValues();
        cv.put("NUMBER", new PhoneMatch(standardizationPhoneNum(info.getNumber(), this.mContext)).getMatchPhone());
        cv.put("IS_CLOUD", Boolean.valueOf(true));
        cv.put("CLASSIFY", info.getClassify());
        cv.put("NAME", info.getName());
        cv.put("SUPPLIER", info.getSupplier());
        cv.put("MARKED_COUNT", Integer.valueOf(info.getMarkedCount()));
        cv.put("SAVE_TIMESTAMP", Long.valueOf(System.currentTimeMillis()));
        cv.put("DESCRIPTION", info.getDescription());
        return cv;
    }

    private ContentValues createContentValues(String matchNum, CapabilityInfo info) {
        if (info == null) {
            return null;
        }
        ContentValues cv = new ContentValues();
        cv.put("NUMBER", matchNum);
        cv.put("TITLE", info.getTitle());
        cv.put("CONTENT", info.getSubTitle());
        cv.put("TYPE", info.getType());
        cv.put("EXTERNAL_LINK", info.getExternalLink());
        cv.put("TIMESTAMP", Long.valueOf(System.currentTimeMillis()));
        if (info.getType().equals("address")) {
            cv.put("INTERNAL_LINK", info.getInternalLink());
        }
        return cv;
    }

    private ContentValues createNullDataContentValues(String matchNumber) {
        ContentValues cv = new ContentValues();
        cv.put("NUMBER", matchNumber);
        cv.put("TIMESTAMP", Long.valueOf(System.currentTimeMillis()));
        return cv;
    }

    private CapabilityInfo revertCursorToCapability(Cursor queryCursor) {
        String title = queryCursor.getString(1);
        if (TextUtils.isEmpty(title)) {
            return null;
        }
        return new CapabilityInfo(queryCursor.getString(3), title, queryCursor.getString(2), queryCursor.getString(0), queryCursor.getString(4), queryCursor.getString(5));
    }

    private NumberMarkInfo revertCursorToNumberMarkInfo(Cursor queryCursor) {
        return new NumberMarkInfo(queryCursor.getString(0), queryCursor.getString(2), queryCursor.getString(1), queryCursor.getInt(3), queryCursor.getInt(4) != 0);
    }

    private void insertColum(Uri tableUri, String tableName, ContentValues cv, String type) {
        if (cv != null) {
            if ("eSpace".equals(type)) {
                ContactsAppProvider.insertNumberMarkInfo(this.mContext, cv);
                ContactsAppProvider.deleteNumberMarkInfo(this.mContext, "_id IN (SELECT _id FROM " + tableName + " ORDER BY _id DESC LIMIT -1 OFFSET 5000)", null);
            } else {
                try {
                    this.mContext.getContentResolver().insert(tableUri, cv);
                    this.mContext.getContentResolver().delete(tableUri, "_id IN (SELECT _id FROM " + tableName + " ORDER BY _id DESC LIMIT -1 OFFSET 5000)", null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteColums(Uri tableUri, String matchNum) {
        String[] whereArgs = new String[]{matchNum};
        this.mContext.getContentResolver().delete(tableUri, "NUMBER=?", whereArgs);
    }

    private void deleteColums(Uri tableUri, String matchNum, boolean isCloudMark, String type) {
        String whereClause = "NUMBER=? and IS_CLOUD=?";
        String[] whereArgs = new String[2];
        whereArgs[0] = matchNum;
        whereArgs[1] = isCloudMark ? CallInterceptDetails.BRANDED_STATE : "0";
        if ("eSpace".equals(type)) {
            ContactsAppProvider.deleteNumberMarkInfo(this.mContext, whereClause, whereArgs);
            return;
        }
        try {
            this.mContext.getContentResolver().delete(tableUri, whereClause, whereArgs);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateColums(String matchNum, ContentValues cv, boolean isCloudMark, String type) {
        String whereClause = "NUMBER=? and IS_CLOUD=?";
        String[] whereArgs = new String[2];
        whereArgs[0] = matchNum;
        whereArgs[1] = isCloudMark ? CallInterceptDetails.BRANDED_STATE : "0";
        if ("eSpace".equals(type)) {
            ContactsAppProvider.updateNumberMarkInfo(this.mContext, cv, whereClause, whereArgs);
            return;
        }
        try {
            this.mContext.getContentResolver().update(NumberMark.CONTENT_URI, cv, whereClause, whereArgs);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private boolean isCloudMarkEnable() {
        return isCloudMarkFeatureEnable() ? isCloudMarkSwitchOpen() : false;
    }

    private boolean isCloudMarkFeatureEnable() {
        if (EmuiFeatureManager.isNumberMarkFeatureEnabled()) {
            return MultiUsersUtils.isCurrentUserOwner();
        }
        return false;
    }

    private boolean isCloudMarkSwitchOpen() {
        return NumberMarkUtil.isUseNetwokMark(this.mContext);
    }

    public void mark(String number, int type, String custom) {
        Throwable th;
        String phoneNumber = new PhoneMatch(standardizationPhoneNum(number, this.mContext)).getMatchPhone();
        if (!TextUtils.isEmpty(phoneNumber)) {
            String markType;
            IHarassmentInterceptionService hiService = Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService"));
            IHoldService holdService = StubController.getHoldService();
            switch (type) {
                case 0:
                    markType = "crank";
                    addToBlackList(markType, number, hiService);
                    StatisticalHelper.report(4039);
                    break;
                case 1:
                    markType = "fraud";
                    addToBlackList(markType, number, hiService);
                    StatisticalHelper.report(4040);
                    break;
                case 2:
                    markType = "express";
                    removeFromBlackList(number, hiService);
                    StatisticalHelper.report(4041);
                    break;
                case 3:
                    markType = "promote sales";
                    removeFromBlackList(number, hiService);
                    StatisticalHelper.report(4042);
                    break;
                case 4:
                    markType = "house agent";
                    removeFromBlackList(number, hiService);
                    StatisticalHelper.report(4043);
                    break;
                default:
                    markType = "others";
                    removeFromBlackList(number, hiService);
                    StatisticalHelper.report(4044);
                    break;
            }
            ContentValues values = new ContentValues();
            values.put("NUMBER", phoneNumber);
            values.put("CLASSIFY", markType);
            values.put("NAME", custom);
            values.put("MARKED_COUNT", Integer.valueOf(1));
            values.put("IS_CLOUD", Integer.valueOf(0));
            values.put("SAVE_TIMESTAMP", "");
            values.put("SUPPLIER", "");
            updateCallLog(this.mContext, phoneNumber, markType, custom, 1, false, number.length() == phoneNumber.length());
            Cursor userMarkCursor = getLocalMarkCursor(standardizationPhoneNum(number, this.mContext));
            try {
                if (userMarkCursor.getCount() <= 0 || !userMarkCursor.moveToFirst()) {
                    reportUserMarkToSystemServer(holdService, 2, new ReportContent(number, switchUserMarkTypeToReportType(markType), custom));
                    insertColum(NumberMark.CONTENT_URI, "number_mark", values, null);
                } else {
                    try {
                        reportUserMarkToSystemServer(holdService, 1, new ReportContent(number, switchUserMarkTypeToReportType(markType), custom, switchUserMarkTypeToReportType(userMarkCursor.getString(1)), userMarkCursor.getString(2)));
                        updateColums(phoneNumber, values, false, null);
                    } catch (Throwable th2) {
                        th = th2;
                        if (userMarkCursor != null) {
                            userMarkCursor.close();
                        }
                        throw th;
                    }
                }
                if (userMarkCursor != null) {
                    userMarkCursor.close();
                }
            } catch (Throwable th3) {
                th = th3;
                ReportContent reportContent = null;
                if (userMarkCursor != null) {
                    userMarkCursor.close();
                }
                throw th;
            }
        }
    }

    public synchronized void unmark(String number) {
        String phoneNumber = new PhoneMatch(standardizationPhoneNum(number, this.mContext)).getMatchPhone();
        removeFromBlackList(number, Stub.asInterface(ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService")));
        deleteColums(NumberMark.CONTENT_URI, phoneNumber, false, null);
        NumberMarkInfo info = getLocalMark(number);
        if (info != null) {
            updateCallLog(this.mContext, phoneNumber, info.getClassify(), info.getName(), info.getMarkedCount(), info.isCloudMark(), false);
        } else if (IsPhoneNetworkRoamingUtils.isMaritimeSatelliteNumber(number)) {
            updateCallLog(this.mContext, number, "sate", this.maritimeName, 0, true, false);
        } else {
            updateCallLog(this.mContext, phoneNumber, null, null, 0, false, false);
        }
    }

    private void addToBlackList(String markType, String markNum, IHarassmentInterceptionService service) {
        Set<String> blackListedNumbersSet = BlacklistCommonUtils.getBlockListNumberHashSet(service);
        if (blackListedNumbersSet == null || !blackListedNumbersSet.contains(CommonUtilMethods.trimNumberForMatching(markNum))) {
            if (BlacklistCommonUtils.isInWhiteList(service, markNum)) {
                BlacklistCommonUtils.removePhoneNumberFromWhiteList(service, markNum);
            }
            BlacklistCommonUtils.handleNumberBlockList(this.mContext, service, markNum, "", 0, false);
        }
    }

    private void removeFromBlackList(String markNum, IHarassmentInterceptionService service) {
        Set<String> blackListedNumbersSet = BlacklistCommonUtils.getBlockListNumberHashSet(service);
        if (blackListedNumbersSet != null && blackListedNumbersSet.contains(CommonUtilMethods.trimNumberForMatching(markNum))) {
            BlacklistCommonUtils.handleNumberBlockList(this.mContext, service, markNum, "", 1, false);
        }
    }

    public static void updateCallLog(Context context, String number, String markType, String markContent, int markCount, boolean isCloudMark, boolean isExactMatch) {
        if (context != null && !TextUtils.isEmpty(number)) {
            int i;
            String selection;
            String[] selectionArgs;
            ContentValues values = new ContentValues();
            values.put("mark_type", markType);
            values.put("mark_content", markContent);
            values.put("mark_count", Integer.valueOf(markCount));
            String str = "is_cloud_mark";
            if (isCloudMark) {
                i = 1;
            } else {
                i = 0;
            }
            values.put(str, Integer.valueOf(i));
            if (isExactMatch) {
                selection = "number=?";
                selectionArgs = new String[]{number};
            } else {
                selection = "number like ?";
                selectionArgs = new String[]{"%" + number};
            }
            context.getContentResolver().update(QueryUtil.getCallsContentUri(context), values, selection, selectionArgs);
        }
    }

    public void destory() {
    }

    public static String appendSupplierInfo(Context context, String numberMarkInfo, String supplier) {
        if ("toms".equals(supplier)) {
            supplier = context.getString(R.string.call_log_detail_info_supplier_toms);
        } else if ("tencent".equals(supplier)) {
            supplier = context.getString(R.string.call_log_detail_info_supplier_tencent);
        }
        if (!TextUtils.isEmpty(supplier)) {
            return numberMarkInfo + String.format(" (", new Object[0]) + supplier + String.format(")", new Object[0]);
        }
        return String.format(context.getString(R.string.marked), new Object[]{numberMarkInfo});
    }

    public static String removeDashesAndBlanksBrackets(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return paramString;
        }
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramString.length(); i++) {
            char c = paramString.charAt(i);
            if (!(c == ' ' || c == '-' || c == '(' || c == ')')) {
                localStringBuilder.append(c);
            }
        }
        return localStringBuilder.toString();
    }

    private void reportUserMarkToSystemServer(IHoldService service, int reportType, ReportContent content) {
        Bundle requestBundle = new Bundle();
        requestBundle.putString("phonenum", content.getPhoneNum());
        requestBundle.putInt("tagtype", content.getTagType());
        requestBundle.putString("userDefineName", content.getUserDefineName());
        requestBundle.putString("repoterPkg", "com.android.contacts");
        if (reportType == 1) {
            requestBundle.putInt("localtagtype", content.getLocalTagType());
            requestBundle.putString("originname", content.getOriginName());
        }
        try {
            Bundle responseBundle = service.callHsmService("reportNumberMark", requestBundle);
            if (responseBundle == null) {
                return;
            }
            if (responseBundle.getBoolean("result")) {
                HwLog.i("NumberMarkManager", "start report");
            } else {
                HwLog.w("NumberMarkManager", "report param error");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NoSuchMethodError e2) {
            HwLog.w("NumberMarkManager", "no report method");
            e2.printStackTrace();
        }
    }

    private void initMarkMap() {
        sMarkMap.clear();
        sMarkMap.put("crank", Integer.valueOf(50));
        sMarkMap.put("promote sales", Integer.valueOf(53));
        sMarkMap.put("fraud", Integer.valueOf(54));
        sMarkMap.put("house agent", Integer.valueOf(51));
        sMarkMap.put("express", Integer.valueOf(55));
        sMarkMap.put("others", Integer.valueOf(10055));
    }

    private int switchUserMarkTypeToReportType(String userMarkType) {
        return ((Integer) sMarkMap.get(userMarkType)).intValue();
    }
}
