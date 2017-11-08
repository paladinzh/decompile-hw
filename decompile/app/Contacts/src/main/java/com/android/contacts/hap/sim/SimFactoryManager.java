package com.android.contacts.hap.sim;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.advanced.AdvancedSimFactory;
import com.android.contacts.hap.sim.base.BaseSimFactory;
import com.android.contacts.hap.sim.extended.ExtendedSimFactory;
import com.android.contacts.hap.util.ReflelctionConstant;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.android.provider.IccProviderUtilsEx;
import com.huawei.android.telephony.IIccPhoneBookManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.contact.util.HwUtil;
import java.util.ArrayList;
import java.util.List;

public class SimFactoryManager {
    private static final Object SYNC_FIRST_SIM = new Object();
    private static final Object SYNC_SECOND_SIM = new Object();
    private static Context mContext;
    private static SimFactory sFirstSimFactory;
    private static boolean sIsDualSim = true;
    private static boolean sIsInitSimFactoryManager = false;
    private static boolean sIsSIMCard1Enabled;
    private static boolean sIsSIMCard1Present;
    private static boolean sIsSIMCard2Enabled;
    private static boolean sIsSIMCard2Present;
    private static volatile boolean sIsSim1LoadingFinished = false;
    private static volatile boolean sIsSim2LoadingFinished = false;
    private static SimFactory sSecondSimFactory;
    private static String sSim1AccountHashCode;
    private static String sSim2AccountHashCode;
    private static int sSimCombination = -1;
    private static SimConfigListener sSimConfigListener;
    private static List<SimStateListener> sSimStateListeners = new ArrayList();
    private static final Uri sSingleSimProviderUri = Uri.parse("content://icc/adn");
    private static Object syncObj = new Object();

    public static class SimDisplayInfo {
        public String mName = "";
        public int mResId = 0;
    }

    public static void initDualSim(Context aContext) {
        mContext = aContext;
        if (EmuiVersion.isSupportEmui()) {
            sIsDualSim = MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } else {
            sIsDualSim = false;
        }
    }

    public static void init() {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "Inside init method of SimFactoryManger the combination is:" + sSimCombination);
        }
        if (sIsDualSim) {
            updateSimState(0);
            updateSimState(1);
        } else {
            updateSimState(0);
        }
        sSim1AccountHashCode = String.valueOf(CommonUtilMethods.getAccountHashCode("com.android.huawei.sim", getAccountName(0)));
        sSim2AccountHashCode = String.valueOf(CommonUtilMethods.getAccountHashCode("com.android.huawei.secondsim", getAccountName(1)));
    }

    public static boolean isDualSim() {
        return sIsDualSim;
    }

    public static int getSimCombination() {
        if (SystemProperties.get("ro.config.dsds_mode").equals("cdma_gsm")) {
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "Inside CDMA GSM combination");
            }
            return 2;
        } else if (!SystemProperties.get("ro.config.dsds_mode").equals("umts_gsm")) {
            return -1;
        } else {
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "Inside GSM GSM combination");
            }
            return 4;
        }
    }

    static void initSimFactory(int aSubscription) {
        if (HwLog.HWFLOW) {
            HwLog.i("SimFactoryManager", "Inside init SIM factory with susbscription:" + aSubscription);
        }
        Object obj;
        if (aSubscription == 1) {
            obj = SYNC_SECOND_SIM;
            synchronized (obj) {
                if (sSecondSimFactory == null) {
                    sSecondSimFactory = createSimFactory(1);
                } else if (getSimMaxCapacity(aSubscription) <= 0) {
                    sSecondSimFactory = createSimFactory(1);
                }
                if (!(sSimConfigListener == null || sSecondSimFactory == null)) {
                    sSimConfigListener.configChanged();
                }
            }
        } else {
            obj = SYNC_FIRST_SIM;
            synchronized (obj) {
                if (sFirstSimFactory == null) {
                    sFirstSimFactory = createSimFactory(0);
                } else if (getSimMaxCapacity(aSubscription) <= 0) {
                    sFirstSimFactory = createSimFactory(0);
                }
                if (!(sSimConfigListener == null || sFirstSimFactory == null)) {
                    sSimConfigListener.configChanged();
                }
            }
        }
    }

    private static SimFactory createSimFactory(int slotId) {
        SimFactory simFactory;
        Exception e;
        Throwable th;
        String accountType = getAccountType(slotId);
        Uri providerUri = getProviderUri(slotId);
        StringBuilder exceptionScenes = new StringBuilder();
        int[] simRecords = getSimRecordsSize(slotId);
        if (HwLog.HWFLOW) {
            HwLog.i("SimFactoryManager", "simRecords = " + checkSimRecordsState(simRecords));
        }
        SimFactory simFactory2 = null;
        Cursor cursor = null;
        if (simRecords == null) {
            simFactory = new BaseSimFactory(slotId, accountType, mContext, sSimConfigListener);
            exceptionScenes.append("simRecords==null");
            HwLog.w("SimFactoryManager", "Inside createSimFactory when sim records null creating Base SIM factory with slotId:" + slotId);
            ExceptionCapture.captureInitSimFactoryException("Inside createSimFactory when sim records null creating Base SIM factory with slotId:", slotId);
        } else if (simRecords.length == 3) {
            try {
                cursor = mContext.getContentResolver().query(providerUri, null, null, null, null);
                if (cursor == null) {
                    simFactory = new BaseSimFactory(slotId, accountType, mContext, sSimConfigListener);
                    try {
                        exceptionScenes.append("cursor==null");
                        if (HwLog.HWDBG) {
                            HwLog.d("SimFactoryManager", "Inside createSimFactory when cursor null  creating Base SIM factory with slotId:" + slotId);
                        }
                        ExceptionCapture.captureInitSimFactoryException("Inside createSimFactory when cursor null  creating Base SIM factory with slotId:", slotId);
                    } catch (Exception e2) {
                        e = e2;
                        try {
                            HwLog.e("SimFactoryManager", e.getMessage(), e);
                            simFactory2 = new BaseSimFactory(slotId, accountType, mContext, sSimConfigListener);
                            try {
                                HwLog.i("SimFactoryManager", "Inside createSimFactory when exception occurs creating Base SIM factory with slotId:" + slotId);
                                ExceptionCapture.captureInitSimFactoryException("Inside createSimFactory when exception occurs creating Base SIM factory with slotId:", slotId);
                                if (cursor != null) {
                                    cursor.close();
                                }
                                simFactory = simFactory2;
                                if (simFactory != null) {
                                }
                                ExceptionCapture.captureSimRecordException("simFactory is null exceptionScenes:" + exceptionScenes.toString() + " and values is : " + checkSimRecordsState(simRecords));
                                return simFactory;
                            } catch (Throwable th2) {
                                th = th2;
                                simFactory = simFactory2;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                }
                int efidCol = cursor.getColumnIndex("efid");
                if (HwLog.HWDBG) {
                    HwLog.d("SimFactoryManager", "EFID column : " + efidCol);
                }
                if (efidCol > -1) {
                    simFactory = new ExtendedSimFactory(slotId, accountType, mContext, sSimConfigListener, simRecords);
                    if (HwLog.HWDBG) {
                        HwLog.d("SimFactoryManager", "Inside createSimFactory when efidCol > -1 creating  Extended SIM factory with slotId:" + slotId + ",Efid:" + efidCol);
                    }
                } else {
                    simFactory = new BaseSimFactory(slotId, accountType, mContext, sSimConfigListener);
                    exceptionScenes.append("efidCol <= -1");
                    if (HwLog.HWDBG) {
                        HwLog.d("SimFactoryManager", "Inside createSimFactory when efidCol not > -1 creating Base  SIM factory with slotId:" + slotId + ",Efid:" + efidCol);
                    }
                    ExceptionCapture.captureInitSimFactoryException("Inside createSimFactory when efidCol not > -1 creating Base  SIM factory with ,Efid:" + efidCol, slotId);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e3) {
                e = e3;
                simFactory = null;
                HwLog.e("SimFactoryManager", e.getMessage(), e);
                simFactory2 = new BaseSimFactory(slotId, accountType, mContext, sSimConfigListener);
                HwLog.i("SimFactoryManager", "Inside createSimFactory when exception occurs creating Base SIM factory with slotId:" + slotId);
                ExceptionCapture.captureInitSimFactoryException("Inside createSimFactory when exception occurs creating Base SIM factory with slotId:", slotId);
                if (cursor != null) {
                    cursor.close();
                }
                simFactory = simFactory2;
                if (simFactory != null) {
                }
                ExceptionCapture.captureSimRecordException("simFactory is null exceptionScenes:" + exceptionScenes.toString() + " and values is : " + checkSimRecordsState(simRecords));
                return simFactory;
            }
        } else {
            if (simRecords.length == 9) {
                simFactory = new AdvancedSimFactory(slotId, accountType, mContext, sSimConfigListener, simRecords);
                if (HwLog.HWDBG) {
                    HwLog.d("SimFactoryManager", "Inside createSimFactory when sim record length equals to 9 creating Advanced SIM factory with slotId:" + slotId);
                }
            }
            simFactory = simFactory2;
        }
        if (simFactory != null || (simFactory instanceof BaseSimFactory)) {
            ExceptionCapture.captureSimRecordException("simFactory is null exceptionScenes:" + exceptionScenes.toString() + " and values is : " + checkSimRecordsState(simRecords));
        } else if (simRecords != null && simRecords.length > 2) {
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "Inside createSimFactory when sim record length greater than (SimConstants.SIM_EF_ADN_MAX_RECORDS or 2)with slotId:" + slotId + ",With Size:" + simRecords[2]);
            }
            storeMaxValueForSim(simRecords[2], slotId);
        }
        return simFactory;
    }

    private static void printAdnRecords(int[] adnRecords, int subscription) {
        if (HwLog.HWFLOW && adnRecords != null && adnRecords.length > 2) {
            HwLog.i("SimFactoryManager", "Inside printAdnRecords first position:" + adnRecords[0] + ",With slotId:" + subscription);
            HwLog.i("SimFactoryManager", "Inside printAdnRecords second position:" + adnRecords[1] + ",With slotId:" + subscription);
            HwLog.i("SimFactoryManager", "Inside printAdnRecords third position which is SIM capacity:" + adnRecords[2] + ",With slotId:" + subscription);
        }
    }

    public static boolean isSimFactoryInit(int aSubscription) {
        boolean z = true;
        if (1 == aSubscription) {
            if (sSecondSimFactory == null) {
                z = false;
            }
            return z;
        }
        if (sFirstSimFactory == null) {
            z = false;
        }
        return z;
    }

    public static SimFactory getSimFactory(int aSubscription) {
        if (aSubscription == 1) {
            if (SimUtility.isSimStateLoaded(1, mContext)) {
                return getSecondSimFactory();
            }
            return null;
        } else if (SimUtility.isSimStateLoaded(0, mContext)) {
            return getFirstSimFactory();
        } else {
            return null;
        }
    }

    public static SimFactory getSimFactory(String aAccountType) {
        return getSimFactory(getSlotIdBasedOnAccountType(aAccountType));
    }

    private static SimFactory getFirstSimFactory() {
        if (sFirstSimFactory == null) {
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", " Initializing First SIM factory in background thread ");
            }
            new Thread() {
                public void run() {
                    if (SimFactoryManager.getSimState(0) == 5) {
                        SimFactoryManager.initSimFactory(0);
                    }
                }
            }.start();
        }
        return sFirstSimFactory;
    }

    private static SimFactory getSecondSimFactory() {
        if (sSecondSimFactory == null) {
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "Initializing Second SIM factory in background thread");
            }
            new Thread() {
                public void run() {
                    if (SimFactoryManager.getSimState(1) == 5) {
                        SimFactoryManager.initSimFactory(1);
                    }
                }
            }.start();
        }
        return sSecondSimFactory;
    }

    public static int getSpareEmailCount(int slotId) {
        int[] result = null;
        try {
            if (sIsDualSim) {
                result = IIccPhoneBookManagerEx.getDefault().getRecordsSize(slotId);
            } else {
                result = IIccPhoneBookManagerEx.getDefault().getRecordsSize();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCapture.captureSimEmailInfoException("getRecordsSize or getRecordsSize throw Exception", e);
        }
        if (result == null || result.length <= 4) {
            return -1;
        }
        return result[4];
    }

    public static boolean isUsimType(int subscription) {
        boolean isUsim = false;
        if (sIsDualSim) {
            return false;
        }
        try {
            return "USIM".equals(TelephonyManagerEx.getIccCardType());
        } catch (NoExtAPIException e) {
            e.printStackTrace();
            return isUsim;
        }
    }

    public static int[] getSimRecordsSize(int slotId) {
        IIccPhoneBookAdapter iIccPhoneBookAdapter = null;
        int[] recordSize = null;
        if (HwLog.HWFLOW) {
            HwLog.i("SimFactoryManager", "getSimRecordsSize-sIsDualSim = " + sIsDualSim);
        }
        if (sIsDualSim) {
            if (getSimState(slotId) == 5) {
                iIccPhoneBookAdapter = new IIccPhoneBookAdapter(slotId);
            }
        } else if (isSimReady(-1)) {
            iIccPhoneBookAdapter = new IIccPhoneBookAdapter();
        }
        if (iIccPhoneBookAdapter != null) {
            int i = 0;
            while (i < 5) {
                try {
                    if (EmuiFeatureManager.getEmailAnrSupport()) {
                        recordSize = iIccPhoneBookAdapter.getRecordsSize();
                        if (recordSize == null) {
                            recordSize = iIccPhoneBookAdapter.getAdnRecordsSize();
                        }
                    } else {
                        recordSize = iIccPhoneBookAdapter.getAdnRecordsSize();
                    }
                    if (recordSize != null && recordSize.length >= 3 && recordSize[2] != -1) {
                        printAdnRecords(recordSize, slotId);
                        break;
                    }
                    try {
                        if (HwLog.HWFLOW) {
                            HwLog.i("SimFactoryManager", "it seems that the SIM has not been ready, sleep a while and try again!");
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        HwLog.w("SimFactoryManager", "sleep interrupt, may return null!");
                    }
                    i++;
                } catch (UnsupportedException e2) {
                    HwLog.w("SimFactoryManager", "Unsupported exception thrown while getting record size for the Dual Sim");
                    return null;
                }
            }
        }
        return recordSize;
    }

    private static void updateSimState(int subscription) {
        if (subscription == 1) {
            sIsSIMCard2Present = checkSIM2CardPresentState();
        } else {
            sIsSIMCard1Present = checkSIM1CardPresentState();
        }
        sIsSIMCard1Enabled = checkSimEnabledState(0);
        sIsSIMCard2Enabled = checkSimEnabledState(1);
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "updateSimState,sIsSIMCard1Present=" + sIsSIMCard1Present + ",sIsSIMCard1Enabled=" + sIsSIMCard1Enabled + ",sIsSIMCard2Present=" + sIsSIMCard2Present + ",sIsSIMCard2Enabled=" + sIsSIMCard2Enabled);
        }
    }

    public static boolean isSIM1CardPresent() {
        return sIsSIMCard1Present;
    }

    public static boolean checkSIM1CardPresentState() {
        int lState = getSimState(0);
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "isSIM1CardPresent:" + lState);
        }
        if (lState == 2 || lState == 3 || lState == 4 || lState == 5) {
            return true;
        }
        return false;
    }

    public static boolean isSIM2CardPresent() {
        return sIsSIMCard2Present;
    }

    public static boolean checkSIM2CardPresentState() {
        int lState = getSimState(1);
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "isSIM2CardPresent:" + lState);
        }
        if ((lState == 2 || lState == 3 || lState == 4 || lState == 5) && sIsDualSim) {
            return true;
        }
        return false;
    }

    public static boolean isSimEnabled(int slotId) {
        switch (slotId) {
            case 0:
                return sIsSIMCard1Enabled;
            case 1:
                return sIsSIMCard2Enabled;
            default:
                return false;
        }
    }

    public static boolean checkSimEnabledState(int slotId) {
        boolean z = true;
        if (sIsDualSim) {
            return getSimState(slotId) == 5 && isSimActive(getSubscriptionIdBasedOnSlot(slotId));
        } else {
            if (CommonUtilMethods.getTelephonyManager(mContext).getSimState() != 5) {
                z = false;
            }
            return z;
        }
    }

    public static String getSimCardDisplayLabel(int slotid) {
        return getSimCardDisplayLabelWithResId(slotid).mName;
    }

    public static SimDisplayInfo getSimCardDisplayLabelWithResId(int slotid) {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "getSimCardDisplayLabel with slotid:" + slotid);
        }
        int subscription = getSubscriptionIdBasedOnSlot(slotid);
        SimDisplayInfo displayInfo = new SimDisplayInfo();
        if (isBothSimEnabled()) {
            if (subscription == 0) {
                displayInfo.mResId = R.string.sim_one_account_name;
            } else if (slotid == 0 && subscription == -1) {
                displayInfo.mResId = R.string.sim_one_account_name;
            } else {
                displayInfo.mResId = R.string.sim_two_account_name;
            }
        }
        if (displayInfo.mResId == 0) {
            if (!isCdma(subscription)) {
                displayInfo.mResId = R.string.str_SIM;
            } else if (SystemProperties.get("telephony.lteOnCdmaDevice", "").equals(CallInterceptDetails.BRANDED_STATE)) {
                displayInfo.mResId = R.string.str_SIM;
            } else {
                displayInfo.mResId = R.string.str_UIM;
            }
        }
        if (displayInfo.mResId > 0) {
            displayInfo.mName = mContext.getResources().getString(displayInfo.mResId);
        }
        return displayInfo;
    }

    public static SimDisplayInfo getAccountNameBasedOnSlotWithResId(int aSlotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "getAccountNameBasedOnSlot with slotid:" + aSlotId);
        }
        SimDisplayInfo displayInfo = new SimDisplayInfo();
        if (!isBothSimEnabled()) {
            return getSimCardDisplayLabelWithResId(aSlotId);
        }
        if (aSlotId == 0) {
            displayInfo.mResId = R.string.sim_one_account_name;
        } else {
            displayInfo.mResId = R.string.sim_two_account_name;
        }
        if (displayInfo.mResId > 0) {
            displayInfo.mName = mContext.getResources().getString(displayInfo.mResId);
        }
        return displayInfo;
    }

    public static String getSimCardDisplayLabel(String aAccountType) {
        return getSimCardDisplayLabel(getSlotIdBasedOnAccountType(aAccountType));
    }

    public static int getSimMaxCapacity(int aSubscription) {
        return getSharedPreferences("SimInfoFile", aSubscription).getInt("sim_max_limit", -1);
    }

    public static boolean isMultiSimDsda() {
        if (sIsDualSim) {
            return "dsda".equals(SystemProperties.get(ReflelctionConstant.getGlobalMultiSimConfig()));
        }
        return false;
    }

    public static boolean isCdma(int aSubscription) {
        int phoneType;
        if (sIsDualSim) {
            phoneType = CommonUtilMethods.getTelephonyManager(mContext).getCurrentPhoneType(aSubscription);
        } else {
            phoneType = CommonUtilMethods.getTelephonyManager(mContext).getCurrentPhoneType();
        }
        return phoneType == 2;
    }

    public static String getSimIccNumber() {
        return CommonUtilMethods.getTelephonyManager(mContext).getLine1Number();
    }

    public static String getAccountTypeByContactID(long aContactId) {
        String str = null;
        String whereClause = "contact_id=" + aContactId;
        Cursor cursor = mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"account_type"}, whereClause, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            str = cursor.getString(cursor.getColumnIndex("account_type"));
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "getAccountTypeByContactID:" + str);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return str;
    }

    public static void setConfigChangeListener(SimConfigListener aSimConfigListener) {
        sSimConfigListener = aSimConfigListener;
    }

    public static int getSlotIdBasedOnAccountType(String aAccountType) {
        if (sIsDualSim) {
            if ("com.android.huawei.sim".equals(aAccountType)) {
                return 0;
            }
            if ("com.android.huawei.secondsim".equals(aAccountType)) {
                return 1;
            }
        } else if ("com.android.huawei.sim".equals(aAccountType)) {
            return 0;
        }
        return -1;
    }

    public static String getAccountType(int aSubcription) {
        if (aSubcription == 1) {
            return "com.android.huawei.secondsim";
        }
        return "com.android.huawei.sim";
    }

    static void reset(int subscription) {
        if (subscription == 1) {
            sSecondSimFactory = null;
            if (sSimConfigListener != null) {
                sSimConfigListener.configChanged();
                return;
            }
            return;
        }
        sFirstSimFactory = null;
        if (sSimConfigListener != null) {
            sSimConfigListener.configChanged();
        }
    }

    public static SharedPreferences getSharedPreferences(String name, int aSubscription) {
        String fileName;
        if (sIsDualSim) {
            fileName = name + "_" + aSubscription;
        } else {
            fileName = name;
        }
        return mContext.createDeviceProtectedStorageContext().getSharedPreferences(fileName, 0);
    }

    public static void setSimAccountWritable(int aSubscription, boolean aWritable) {
        if (aSubscription == 1) {
            if (sSecondSimFactory != null) {
                sSecondSimFactory.getSimAccountType().forceWritable(aWritable);
                if (HwLog.HWDBG) {
                    HwLog.d("SimFactoryManager", "setSimAccountWritable sSecondSimFactory ");
                }
            }
        } else if (sFirstSimFactory != null) {
            sFirstSimFactory.getSimAccountType().forceWritable(aWritable);
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "setSimAccountWritable sFirstSimFactory ");
            }
        }
    }

    public static int getSimState(int slotId) {
        if (mContext == null) {
            return -1;
        }
        int state;
        if (sIsDualSim) {
            state = CommonUtilMethods.getTelephonyManager(mContext).getSimState(slotId);
        } else {
            state = CommonUtilMethods.getTelephonyManager(mContext).getSimState();
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "Get SIM state from SIM factory manager: " + state + ",For slotId:" + slotId);
        }
        return state;
    }

    public static Uri getProviderUri(int aSubscription) {
        try {
            if (isUsimType(aSubscription)) {
                if (!sIsDualSim) {
                    return (Uri) IccProviderUtilsEx.getUSimProviderUri().get("sSingleUSimProviderUri");
                }
                if (aSubscription == 0) {
                    return (Uri) IccProviderUtilsEx.getUSimProviderUri().get("sFirstUSimProviderUri");
                }
                return (Uri) IccProviderUtilsEx.getUSimProviderUri().get("sSecondUSimProviderUri");
            } else if (!sIsDualSim) {
                return sSingleSimProviderUri;
            } else {
                if (aSubscription == 0) {
                    return (Uri) IccProviderUtilsEx.getSimProviderUri().get("sFirstSimProviderUri");
                }
                return (Uri) IccProviderUtilsEx.getSimProviderUri().get("sSecondSimProviderUri");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return sSingleSimProviderUri;
        }
    }

    public static String getSimSerialNumber(int slotId) {
        if (sIsDualSim) {
            return CommonUtilMethods.getTelephonyManager(mContext).getSubscriberId(getSubscriptionIdBasedOnSlot(slotId));
        }
        return CommonUtilMethods.getTelephonyManager(mContext).getSubscriberId();
    }

    public static boolean isSimReady(int aSubcription) {
        boolean lSimReady = getSimState(aSubcription) == 5;
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "Sim State : " + lSimReady);
        }
        return lSimReady;
    }

    public static void storeMaxValueForSim(int aSize, int aSubscription) {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "Inside storeMaxValueForSim size:" + aSize + ", with susbscription:" + aSubscription);
        }
        getSharedPreferences("SimInfoFile", aSubscription).edit().putInt("sim_max_limit", aSize).apply();
    }

    public static int getTotalSIMContactsPresent(int slotId) {
        int lTotalSIMContactsPresent = 0;
        Uri providerUri = getProviderUri(slotId);
        synchronized (syncObj) {
            Cursor lCursor = mContext.getContentResolver().query(providerUri, null, null, null, null);
            if (lCursor != null) {
                lTotalSIMContactsPresent = lCursor.getCount();
                lCursor.close();
            }
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "getTotalSIMContactsPresent:" + lTotalSIMContactsPresent + ",slotId:" + slotId);
        }
        return lTotalSIMContactsPresent;
    }

    public static SimConfig getSimConfig(String aAccountType) {
        if ("com.android.huawei.secondsim".equals(aAccountType)) {
            if (sSecondSimFactory != null) {
                return sSecondSimFactory.getSimConfig();
            }
        } else if (sFirstSimFactory != null) {
            return sFirstSimFactory.getSimConfig();
        }
        return null;
    }

    public static void listenPhoneState(PhoneStateListener listener, int type) {
        CommonUtilMethods.getTelephonyManager(mContext).listen(listener, type);
    }

    public static boolean isNetworkRoaming(int aSubscriptionId) {
        if (sIsDualSim) {
            return CommonUtilMethods.getTelephonyManager(mContext).isNetworkRoaming(aSubscriptionId);
        }
        return CommonUtilMethods.getTelephonyManager(mContext).isNetworkRoaming();
    }

    public static String getSimCountryIso(int slotId) {
        if (sIsDualSim) {
            return ((TelephonyManager) mContext.getSystemService("phone")).getSimCountryIso(slotId);
        }
        return CommonUtilMethods.getTelephonyManager(mContext).getSimCountryIso();
    }

    public static String getNetworkCountryIso() {
        if (sIsDualSim) {
            return ((TelephonyManager) mContext.getSystemService("phone")).getNetworkCountryIso();
        }
        return CommonUtilMethods.getTelephonyManager(mContext).getNetworkCountryIso();
    }

    public static String getVoiceMailNumber(int aSubscriptionId) {
        if (sIsDualSim) {
            return CommonUtilMethods.getTelephonyManager(mContext).getVoiceMailNumber(aSubscriptionId);
        }
        return CommonUtilMethods.getTelephonyManager(mContext).getVoiceMailNumber();
    }

    public static boolean hasIccCard(int slotId) {
        if (!sIsDualSim) {
            return CommonUtilMethods.getTelephonyManager(mContext).hasIccCard();
        }
        if (slotId == 0) {
            return isSIM1CardPresent();
        }
        if (slotId == 1) {
            return isSIM2CardPresent();
        }
        return false;
    }

    public static int getCallState() {
        return CommonUtilMethods.getTelephonyManager(mContext).getCallState();
    }

    public static boolean isCallStateIdle() {
        boolean z = true;
        if (sIsDualSim) {
            int phone1State = MSimTelephonyManager.getDefault().getCallState(0);
            int phone2State = MSimTelephonyManager.getDefault().getCallState(1);
            if (!(phone1State == 0 && phone2State == 0)) {
                z = false;
            }
            return z;
        }
        if (CommonUtilMethods.getTelephonyManager(mContext).getCallState() != 0) {
            z = false;
        }
        return z;
    }

    public static String getDeviceId(int aSlotId) {
        if (!sIsDualSim) {
            return ((TelephonyManager) mContext.getSystemService("phone")).getDeviceId();
        }
        try {
            int subId = getSubscriptionIdBasedOnSlot(aSlotId);
            if (subId == -1) {
                subId = aSlotId;
            }
            return CommonUtilMethods.getTelephonyManager(mContext).getDeviceId(subId);
        } catch (Exception e) {
            HwLog.w("SimFactoryManager", "Unsupported exception thrown in getDeviceId method hence returning null");
            return null;
        }
    }

    public static String getPesn(int aSlotId) {
        HwTelephonyManager telephonyManager = HwTelephonyManager.getDefault();
        if (!sIsDualSim) {
            return null;
        }
        try {
            int subId = getSubscriptionIdBasedOnSlot(aSlotId);
            if (subId == -1) {
                subId = aSlotId;
            }
            return telephonyManager.getPesn(subId);
        } catch (Exception e) {
            HwLog.w("SimFactoryManager", "Unsupported exception thrown in getPesn method by solt hence returning null");
            try {
                return telephonyManager.getPesn();
            } catch (Exception e2) {
                HwLog.w("SimFactoryManager", "Unsupported exception thrown in getPesn method in TelephonyManager yet hence returning null");
                return null;
            }
        }
    }

    public static int getPreferredVoiceSubscripion() {
        if (!sIsDualSim) {
            return -1;
        }
        try {
            return MSimTelephonyManager.getDefault().getPreferredVoiceSubscription();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getDefaultSubscription() {
        int defualtSub = -1;
        if (sIsDualSim) {
            try {
                defualtSub = MSimTelephonyManager.getDefault().getPreferredVoiceSubscription();
            } catch (Exception e) {
                e.printStackTrace();
                ExceptionCapture.captureReadDefSubException("getDefaultSubscription throw Exception", e);
            }
        }
        return defualtSub;
    }

    public static synchronized void addSimStateListener(SimStateListener aSimStateListener) {
        synchronized (SimFactoryManager.class) {
            sSimStateListeners.add(aSimStateListener);
        }
    }

    public static synchronized void removeSimStateListener(SimStateListener aSimStateListener) {
        synchronized (SimFactoryManager.class) {
            sSimStateListeners.remove(aSimStateListener);
        }
    }

    public static synchronized void notifySimStateChanged(int aSubscription) {
        synchronized (SimFactoryManager.class) {
            updateSimState(aSubscription);
            int nlisteners = sSimStateListeners.size();
            for (int i = 0; i < nlisteners; i++) {
                ((SimStateListener) sSimStateListeners.get(i)).simStateChanged(aSubscription);
            }
        }
    }

    public static void clearSimStatePreferences(int subscription) {
        Editor editor = getSharedPreferences("SimInfoFile", subscription).edit();
        editor.putString("sim_state_value", "");
        editor.commit();
    }

    public static Drawable getSimAccountDisplayIcon(int aSubscription) {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "getSimCardDisplayIcon with susbscription:" + aSubscription + " Sim combination:" + sSimCombination);
        }
        return mContext.getResources().getDrawable(getSimAccountIconResourceId(aSubscription, true));
    }

    public static String getSimAccountDisplayIconContentDescription(int aSubscription) {
        return mContext.getResources().getString(getSimAccountIconContentDescriptionResourceId(aSubscription));
    }

    public static int getSimAccountIconContentDescriptionResourceId(int slotId) {
        int subscription = getSubscriptionIdBasedOnSlot(slotId);
        int sim1IconContentDescriptionId = R.string.str_filter_sim1;
        int sim2IconContentDescriptionId = R.string.str_filter_sim2;
        if (!isBothSimEnabled()) {
            sim1IconContentDescriptionId = R.string.sim_account_name;
            sim2IconContentDescriptionId = R.string.sim_account_name;
        }
        if (!sIsDualSim) {
            return R.string.sim_account_name;
        }
        switch (getSimCombination()) {
            case 1:
            case 4:
                if (subscription == 0) {
                    return sim1IconContentDescriptionId;
                }
                return sim2IconContentDescriptionId;
            case 2:
                if (subscription == 0) {
                    return sim1IconContentDescriptionId;
                }
                if (subscription == 1) {
                    if (slotId == 0) {
                        return R.string.content_description_card_type_g_roaming;
                    }
                    return sim2IconContentDescriptionId;
                } else if (subscription == -1) {
                    if (slotId != 0) {
                        sim1IconContentDescriptionId = sim2IconContentDescriptionId;
                    }
                    return sim1IconContentDescriptionId;
                }
                break;
        }
        if (subscription == 0) {
            return sim1IconContentDescriptionId;
        }
        return sim2IconContentDescriptionId;
    }

    public static int getSimAccountIconResourceId(int slotId, boolean aIsBigImage) {
        int i = R.drawable.dial_num_0_blk_press;
        int i2 = R.drawable.dial_num_0_blk;
        int subscription = getSubscriptionIdBasedOnSlot(slotId);
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "Get subscription based on slot in SIM account icon resource:" + subscription + ", slotId:" + slotId);
        }
        int sim1Res = R.drawable.stat_sys_sim1_double;
        int sim2Res = R.drawable.stat_sys_sim2_double;
        if (!isBothSimEnabled()) {
            sim1Res = R.drawable.ic_contacts_card_single;
            sim2Res = R.drawable.ic_contacts_card_single;
        }
        if (sIsDualSim) {
            switch (getSimCombination()) {
                case 1:
                case 3:
                case 4:
                    if (subscription == 0) {
                        if (aIsBigImage) {
                            sim1Res = R.drawable.dial_num_0_blk;
                        }
                        return sim1Res;
                    }
                    if (aIsBigImage) {
                        sim2Res = R.drawable.dial_num_0_blk_press;
                    }
                    return sim2Res;
                case 2:
                    if (subscription == 0) {
                        if (!aIsBigImage) {
                            i2 = sim1Res;
                        }
                        return i2;
                    } else if (subscription == 1) {
                        if (slotId == 0) {
                            return aIsBigImage ? R.drawable.dial_devide_line : R.drawable.fastscroll_familyname_normal;
                        }
                        if (!aIsBigImage) {
                            i = sim2Res;
                        }
                        return i;
                    } else if (subscription == -1) {
                        if (slotId != 0) {
                            i2 = aIsBigImage ? R.drawable.dial_num_0_blk_press : sim2Res;
                        } else if (!aIsBigImage) {
                            i2 = sim1Res;
                        }
                        return i2;
                    }
                    break;
            }
            if (subscription == 0) {
                if (!aIsBigImage) {
                    i2 = sim1Res;
                }
                return i2;
            }
            if (!aIsBigImage) {
                i = sim2Res;
            }
            return i;
        }
        return aIsBigImage ? R.drawable.daier_call_btn_normal : R.drawable.ic_contacts_card_single;
    }

    public static boolean phoneIsOffhook(int slotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "phone.isOffhook() called");
        }
        int subId = getSubscriptionIdBasedOnSlot(slotId);
        if (subId == -1) {
            subId = slotId;
        }
        boolean phoneOffhook = sIsDualSim ? MSimTelephonyManager.getDefault().getCallState(subId) == 2 : HwUtil.isOffhook("com.android.contacts");
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "phone.isOffhook() called:" + phoneOffhook + "for subscription:" + slotId);
        }
        return phoneOffhook;
    }

    public static boolean phoneIsInUse() {
        if (!sIsDualSim) {
            return false;
        }
        try {
            if (MSimTelephonyManager.getDefault().getCallState(0) == 0 && MSimTelephonyManager.getDefault().getCallState(1) == 0) {
                return false;
            }
            return true;
        } catch (NoExtAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getSubscriptionIdBasedOnSlot(int slotId) {
        if (!sIsDualSim) {
            return -1;
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "getSubscriptionIdBasedOnSlot() called---start");
        }
        int[] ids = SubscriptionManager.getSubId(slotId);
        if (ids == null || ids.length <= 0) {
            if (HwLog.HWDBG) {
                HwLog.d("SimFactoryManager", "getSubscriptionIdBasedOnSlot() called---end");
            }
            return -1;
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "getSubscriptionIdBasedOnSlot() called---end");
        }
        return ids[0];
    }

    public static int getSlotidBasedOnSubscription(int subId) {
        return SubscriptionManager.getSlotId(subId);
    }

    public static boolean isSimActive(int aSubscription) {
        if (mContext != null && mContext.checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            return false;
        }
        boolean isSubActive;
        try {
            if (1 == HwTelephonyManager.getDefault().getSubState((long) aSubscription)) {
                isSubActive = true;
            } else {
                isSubActive = false;
            }
        } catch (NoExtAPIException e) {
            HwLog.w("SimFactoryManager", "NoExtAPIException!");
            isSubActive = false;
        }
        return isSubActive;
    }

    public static boolean isDualSimPresent() {
        if (isDualSim() && hasIccCard(0)) {
            return hasIccCard(1);
        }
        return false;
    }

    public static String getAccountName(int aSlotId) {
        if (!sIsDualSim) {
            return "sim1";
        }
        if (1 == aSlotId) {
            return "sim2";
        }
        return "sim1";
    }

    public static String getAccountName(String accountType) {
        return getAccountName(getSlotIdBasedOnAccountType(accountType));
    }

    public static String getMeid(int aSlotId) {
        HwTelephonyManager HwTm = HwTelephonyManager.getDefault();
        if (sIsDualSim) {
            try {
                int subId = getSubscriptionIdBasedOnSlot(aSlotId);
                if (subId == -1) {
                    subId = aSlotId;
                }
                return HwTm.getMeid(subId);
            } catch (Exception e) {
                HwLog.w("SimFactoryManager", "Unsupported exception thrown in getMeid method by solt hence returning null");
                try {
                    return HwTm.getMeid();
                } catch (Exception e2) {
                    HwLog.w("SimFactoryManager", "Unsupported exception thrown in getMeid method in TelephonyManager yet hence returning null");
                    ExceptionCapture.captureReadMeidException("HwTm.getMeid() is null", null);
                    return null;
                }
            }
        }
        ExceptionCapture.captureReadMeidException("getMeid is null", null);
        return null;
    }

    public static String getImei(int aSlotId) {
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService("phone");
        if (!sIsDualSim) {
            return CommonUtilMethods.getTelephonyManager(mContext).getImei();
        }
        try {
            int subId = getSubscriptionIdBasedOnSlot(aSlotId);
            if (subId == -1) {
                subId = aSlotId;
            }
            return mTelephonyManager.getImei(subId);
        } catch (Exception e) {
            HwLog.w("SimFactoryManager", "Unsupported exception thrown in getImei method by solt hence returning null");
            try {
                return mTelephonyManager.getImei();
            } catch (Exception e2) {
                HwLog.w("SimFactoryManager", "Unsupported exception thrown in getImei method in TelephonyManager yet hence returning null");
                return null;
            }
        }
    }

    public static int getUserDefaultSubscription() {
        try {
            return TelephonyManagerEx.getDefault4GSlotId();
        } catch (Throwable th) {
            HwLog.w("SimFactoryManager", "Unsupported exception thrown in getDefault4GSlotId method hence returning SUB1");
            ExceptionCapture.captureReadDefSubException("Unsupported exception thrown in getDefault4GSlotId method hence returning SUB1", null);
            return 0;
        }
    }

    private static String checkSimRecordsState(int[] simrecords) {
        StringBuffer sb = new StringBuffer();
        if (simrecords == null) {
            return "null";
        }
        for (int record : simrecords) {
            sb.append(record + ",");
        }
        return sb.toString();
    }

    public static int getDefaultSimcard() {
        return Global.getInt(mContext.getContentResolver(), "default_simcard_slotid", -1);
    }

    public static void setDefaultSimcard(int simCard) {
        Global.putInt(mContext.getContentResolver(), "default_simcard_slotid", simCard);
    }

    public static boolean isExtremeSimplicityMode() {
        boolean isSimEnabled = isSIM1CardPresent() ? isSimEnabled(0) : false;
        boolean isSimEnabled2 = isSIM2CardPresent() ? isSimEnabled(1) : false;
        if (isSimEnabled && isSimEnabled2 && -1 != getDefaultSimcard()) {
            return true;
        }
        return false;
    }

    public static void setSimLoadingState(int slotId, boolean loadingFinished) {
        if (1 == slotId) {
            sIsSim2LoadingFinished = loadingFinished;
        } else {
            sIsSim1LoadingFinished = loadingFinished;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("SimFactoryManager", "setSimLoadingState slotId=" + slotId + " Sim2=" + sIsSim2LoadingFinished + " Sim1=" + sIsSim1LoadingFinished);
        }
    }

    public static boolean isSimLoadingFinished(int slotId) {
        if (1 == slotId) {
            return sIsSim2LoadingFinished;
        }
        return sIsSim1LoadingFinished;
    }

    public static boolean isSimLoadingFinished(String accountType) {
        if ("com.android.huawei.secondsim".equals(accountType)) {
            return sIsSim2LoadingFinished;
        }
        return sIsSim1LoadingFinished;
    }

    public static boolean isBothSimLoadingFinished() {
        if (!sIsDualSim) {
            return sIsSim1LoadingFinished;
        }
        return sIsSim1LoadingFinished ? sIsSim2LoadingFinished : false;
    }

    public static boolean isBothSimEnabled() {
        boolean z = false;
        if (!sIsDualSim) {
            return false;
        }
        if (sIsSIMCard1Present && sIsSIMCard2Present && sIsSIMCard1Enabled) {
            z = sIsSIMCard2Enabled;
        }
        return z;
    }

    public static boolean isNoSimEnabled() {
        boolean z = true;
        if (sIsDualSim) {
            if ((sIsSIMCard1Present || sIsSIMCard2Present) && (sIsSIMCard1Enabled || sIsSIMCard2Enabled)) {
                z = false;
            }
            return z;
        }
        if (sIsSIMCard1Present && sIsSIMCard1Enabled) {
            z = false;
        }
        return z;
    }

    public static String getContactAccountType(Uri uri) {
        if (uri == null) {
            return null;
        }
        String simAccountType = null;
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 4) {
            Object accountHashCode = null;
            String lookupKey = (String) pathSegments.get(2);
            int start = lookupKey.indexOf("r");
            if (start > 0 && start < lookupKey.length()) {
                accountHashCode = lookupKey.substring(0, start);
            }
            if (sSim1AccountHashCode != null && sSim1AccountHashCode.equals(r0)) {
                simAccountType = "com.android.huawei.sim";
            } else if (sSim2AccountHashCode != null && sSim2AccountHashCode.equals(r0)) {
                simAccountType = "com.android.huawei.secondsim";
            }
        }
        return simAccountType;
    }

    public static void reHandleSimState(Context context) {
        Intent simStateIntent = context.registerReceiver(null, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        if (simStateIntent != null) {
            String simState = simStateIntent.getStringExtra("ss");
            int slotId = -1;
            try {
                slotId = IIccPhoneBookManagerEx.getDefault().getSoltIdInSimStateChangeIntent(simStateIntent);
            } catch (NoExtAPIException e) {
                e.printStackTrace();
                ExceptionCapture.captureReadSoltIdException("Call getSoltIdInSimStateChangeIntent error", e);
            }
            if (HwLog.HWFLOW) {
                HwLog.i("SimFactoryManager", "reHandleSimState: " + simState);
            }
            Intent intent = new Intent();
            intent.setAction("com.huawei.settings.HANDLE_PHONESTATE");
            intent.putExtra("simstate", simState);
            intent.putExtra("subscription", slotId);
            intent.setPackage(context.getPackageName());
            context.startService(intent);
        }
    }

    public static void initSimFactoryManager() {
        if (!sIsInitSimFactoryManager) {
            new Thread() {
                public void run() {
                    SimFactoryManager.sIsInitSimFactoryManager = true;
                    SimFactoryManager.init();
                    SharedPreferences prefs;
                    if (SimFactoryManager.isDualSim()) {
                        prefs = SimFactoryManager.getSharedPreferences("SimInfoFile", 0);
                        prefs.edit().remove("sim_delete_progress").remove("sim_copy_contacts_progress").apply();
                        SimFactoryManager.rehandleSimStateLoaded(prefs, 0);
                        prefs = SimFactoryManager.getSharedPreferences("SimInfoFile", 1);
                        prefs.edit().remove("sim_delete_progress").remove("sim_copy_contacts_progress").apply();
                        SimFactoryManager.rehandleSimStateLoaded(prefs, 1);
                    } else {
                        prefs = SimFactoryManager.getSharedPreferences("SimInfoFile", -1);
                        prefs.edit().remove("sim_delete_progress").remove("sim_copy_contacts_progress").apply();
                        SimFactoryManager.rehandleSimStateLoaded(prefs, -1);
                    }
                    ContactDisplayUtils.setNameDisplayOrder(new ContactsPreferences(SimFactoryManager.mContext).getDisplayOrder());
                    AccountTypeManager.getInstance(SimFactoryManager.mContext).hiCloudServiceLogOnOff();
                    AccountsDataManager.getInstance(SimFactoryManager.mContext).preLoadAccountsDataInBackground();
                }
            }.start();
        }
    }

    public static void rehandleSimStateLoaded(SharedPreferences prefs, int slotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimFactoryManager", "rehandleSimStateLoaded");
        }
        if (slotId == -1) {
            slotId = 0;
        }
        if (isReloadSimRecord(prefs, slotId)) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimFactoryManager", "rehandleSimStateLoaded");
            }
            Intent mIntent = new Intent();
            mIntent.setAction("com.huawei.settings.HANDLE_PHONESTATE");
            mIntent.putExtra("simstate", "LOADED");
            mIntent.putExtra("subscription", slotId);
            mIntent.setPackage("com.android.contacts");
            mContext.startService(mIntent);
        }
    }

    public static boolean isReloadSimRecord(SharedPreferences prefs, int slotId) {
        if (prefs == null) {
            HwLog.w("SimFactoryManager", "In isReloadSimRecord(): prefs is null, please check");
            return false;
        } else if (isSimReady(slotId)) {
            String lSimState = prefs.getString("sim_state_value", "");
            if (TextUtils.isEmpty(lSimState)) {
                if (HwLog.HWFLOW) {
                    HwLog.i("SimFactoryManager", "isReloadSimRecord: Not process any sim state event before, reload for slotId: " + slotId);
                }
                return true;
            }
            boolean isSimLoaded = lSimState.equalsIgnoreCase("LOADED");
            boolean isSimStateChangeUnHandled = prefs.getBoolean("sim_handle_state_change_progress", false);
            if (!isSimLoaded) {
                if (CommonUtilMethods.existGroup(mContext, getAccountType(slotId))) {
                    String lOldSimSerial = prefs.getString("simimsinumber", null);
                    String lNewSimSerial = CommonUtilMethods.getMD5Digest(getSimSerialNumber(slotId));
                    if (lOldSimSerial == null || !lOldSimSerial.equals(lNewSimSerial)) {
                        if (HwLog.HWFLOW) {
                            HwLog.i("SimFactoryManager", "isReloadSimRecord: A new sim but not load, reload for slotId: " + slotId);
                        }
                        return true;
                    }
                    if (HwLog.HWFLOW) {
                        HwLog.i("SimFactoryManager", "isReloadSimRecord: Other case, Skip reload for slotId: " + slotId);
                    }
                    return false;
                }
                if (HwLog.HWFLOW) {
                    HwLog.i("SimFactoryManager", "isReloadSimRecord: If sim ready but account not exist, reload sim record for slotId: " + slotId);
                }
                return true;
            } else if (isSimStateChangeUnHandled) {
                if (HwLog.HWFLOW) {
                    HwLog.i("SimFactoryManager", "isReloadSimRecord: Load event was interrupted, reload when process restart for slotId: " + slotId);
                }
                return true;
            } else {
                if (HwLog.HWFLOW) {
                    HwLog.i("SimFactoryManager", "isReloadSimRecord: Load completed before, skip for slotId: " + slotId);
                }
                return false;
            }
        } else {
            if (HwLog.HWFLOW) {
                HwLog.i("SimFactoryManager", "isReloadSimRecord: If sim not ready, skip for slotId: " + slotId);
            }
            return false;
        }
    }
}
