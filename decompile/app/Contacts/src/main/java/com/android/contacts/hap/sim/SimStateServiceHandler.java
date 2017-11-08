package com.android.contacts.hap.sim;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.ext.HwCustContactAndProfileInitializer;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager.SimDisplayInfo;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.SimContactsCache;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.MVersionUpgradeUtils.ProviderStatus;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SimStateServiceHandler extends Handler {
    private static Deque<ISimStateCallback> sCallback = new ConcurrentLinkedDeque();
    private boolean isO2SimCard_new = false;
    private boolean isO2SimCard_old = false;
    private Context mContext;
    boolean mHandleClearCallLog = false;
    private HwCustContactAndProfileInitializer mHwCustConAndProfInializeObj = null;
    private boolean mIsSimHandlingEventSend;
    private int mSlotId;
    private int simState;

    public interface ISimStateCallback {
        void fireCallback(int i);
    }

    public static void registerCallback(ISimStateCallback callback) {
        sCallback.add(callback);
    }

    public static void deRegisterCallback(ISimStateCallback callback) {
        if (sCallback.contains(callback)) {
            sCallback.remove(callback);
            return;
        }
        throw new IllegalStateException("Callback Not Registerd");
    }

    public SimStateServiceHandler(Looper aLooper, Context aContext, int aSlotId, String aAccountType) {
        super(aLooper);
        this.mSlotId = aSlotId;
        this.mContext = aContext;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCustConAndProfInializeObj = (HwCustContactAndProfileInitializer) HwCustUtils.createObj(HwCustContactAndProfileInitializer.class, new Object[0]);
        }
    }

    public void handleMessage(Message msg) {
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateServiceHandler", "handleMessage of serviceHandler called");
        }
        switch (msg.arg1) {
            case 1:
                handleSimStateChangeEvent(msg.getData());
                return;
            default:
                HwLog.e("SimStateServiceHandler", "Invalid message is posted to service handler");
                return;
        }
    }

    public void handleSimStateChangeEvent(Bundle data) {
        HwLog.i("SimStateServiceHandler", "Inside handleSimStateChangeEvent with slotId:" + this.mSlotId);
        if (this.mContext.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            if (data == null) {
                HwLog.e("SimStateServiceHandler", "in handleSimStateChangeEvent(), Bundle is null, returning");
                return;
            }
            SimContactsCache.clearSimSmallBitmapCache();
            String lState = data.getString("simstate");
            HwLog.i("SimStateServiceHandler", "--- STARTED SIM OPERATION --- WITH SIM STATE : " + lState + ", With slotId:" + this.mSlotId);
            SharedPreferences settings = SimFactoryManager.getSharedPreferences("SimInfoFile", this.mSlotId);
            if (!this.mIsSimHandlingEventSend) {
                Intent intent = new Intent(SimUtility.getSimHdlingIntentForStickyBroadcast(this.mSlotId));
                intent.setFlags(67108864);
                this.mContext.sendStickyBroadcast(intent);
                this.mIsSimHandlingEventSend = true;
            }
            settings.edit().putString("sim_state_value", lState).putBoolean("sim_handle_state_change_progress", true).apply();
            String lOldSimSerial;
            String lNewSimSerial;
            if (("LOADED".equals(lState) && SimFactoryManager.isSimActive(this.mSlotId)) || CallInterceptDetails.BRANDED_STATE.equals(lState)) {
                HwLog.i("SimStateServiceHandler", "SIM STATE :" + lState);
                ExceptionCapture.markInsertSimContactsStart();
                lOldSimSerial = settings.getString("simimsinumber", null);
                lNewSimSerial = SimFactoryManager.getSimSerialNumber(this.mSlotId);
                if (!this.mHandleClearCallLog) {
                    this.isO2SimCard_old = settings.getBoolean("sim_is_o2card", false);
                    this.isO2SimCard_new = isO2SimCard(lNewSimSerial);
                }
                lNewSimSerial = CommonUtilMethods.getMD5Digest(lNewSimSerial);
                if (this.simState == 3 || lNewSimSerial == null) {
                    HwLog.i("SimStateServiceHandler", "Already set to sim state loaded ...");
                    if (EmuiFeatureManager.isPreLoadingSimContactsEnabled() && lNewSimSerial == null) {
                        SimDatabaseHelper.markSimContactsAsDeleted(this.mContext, SimFactoryManager.getAccountType(this.mSlotId));
                    }
                    settings.edit().remove("sim_handle_state_change_progress").commit();
                    return;
                }
                this.simState = 3;
                ensureProviderStatus();
                if (lOldSimSerial == null || !lOldSimSerial.equals(lNewSimSerial)) {
                    HwLog.i("SimStateServiceHandler", "Its a new SIM ...! or sim changed with mSlotId:" + this.mSlotId);
                    removeSimContacts();
                    if (!SimFactoryManager.isDualSim()) {
                        boolean needToClearCallLog = SharePreferenceUtil.getDefaultSp_de(this.mContext).getBoolean("clear_call_log_entries", false);
                        HwLog.i("needToClearCallLog", "needToClearCallLog --> " + needToClearCallLog);
                        if (needToClearCallLog) {
                            deleteAllCallsLog();
                        }
                    }
                    if (this.mHandleClearCallLog) {
                        this.mHandleClearCallLog = false;
                    } else {
                        handleO2ClearCallLog(lOldSimSerial, lNewSimSerial);
                    }
                }
                SimFactoryManager.initSimFactory(this.mSlotId);
                this.mContext.sendStickyBroadcast(SimUtility.getIntentForStickyBroadcast(this.mSlotId));
                createSimAccount();
                if (lOldSimSerial == null || !lOldSimSerial.equals(lNewSimSerial)) {
                    SimFactoryManager.setSimAccountWritable(this.mSlotId, false);
                    copySimContactsToSimAccount();
                    if (this.mHwCustConAndProfInializeObj != null) {
                        this.mHwCustConAndProfInializeObj.handleSimSwapCustomization(this.mContext);
                    }
                } else if (lOldSimSerial.equals(lNewSimSerial)) {
                    HwLog.i("SimStateServiceHandler", "Its an exisiting SIM ...! with mSlotId:" + this.mSlotId);
                    SimFactoryManager.setSimAccountWritable(this.mSlotId, false);
                    compareAndCopySimContactsToSimAccount();
                }
                setSimContactVisibility(true, lState);
                sendSimInitCompleteBroadcast(lNewSimSerial);
                settings.edit().remove("sim_delete_progress").apply();
                settings.edit().remove("sim_copy_contacts_progress").apply();
                updateFavoritesWidget();
                if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
                    SimFactoryManager.setSimLoadingState(this.mSlotId, true);
                }
                SimFactoryManager.notifySimStateChanged(this.mSlotId);
            } else if ("ABSENT".equals(lState)) {
                if (this.simState == 1) {
                    HwLog.i("SimStateServiceHandler", "Already set to sim state absent ...");
                    settings.edit().remove("sim_handle_state_change_progress").commit();
                    return;
                }
                this.mContext.sendBroadcast(new Intent("com.android.huawei.sim_intent_absent"), permission.HW_CONTACTS_ALL);
                this.mContext.removeStickyBroadcast(SimUtility.getIntentForStickyBroadcast(this.mSlotId));
                this.simState = 1;
                defineIfWeNeedRemoveSimAccount(this.mSlotId);
                lEditor = settings.edit();
                lEditor.remove("sim_max_limit");
                lEditor.commit();
                SimFactoryManager.reset(this.mSlotId);
                updateFavoritesWidget();
                if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
                    SimFactoryManager.setSimLoadingState(this.mSlotId, true);
                }
            } else if ("NOT_READY".equals(lState)) {
                if (this.simState == 2) {
                    HwLog.i("SimStateServiceHandler", "Already set to sim state not ready ...");
                    settings.edit().remove("sim_handle_state_change_progress").commit();
                    return;
                }
                if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
                    SimFactoryManager.setSimLoadingState(this.mSlotId, false);
                }
                this.mContext.removeStickyBroadcast(SimUtility.getIntentForStickyBroadcast(this.mSlotId));
                ensureProviderStatus();
                this.simState = 2;
                setSimContactVisibility(false, lState);
                updateFavoritesWidget();
            } else if ("LOCKED".equals(lState)) {
                if (this.simState == 4) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("SimStateServiceHandler", "Already set to sim state locaked ...");
                    }
                    settings.edit().remove("sim_handle_state_change_progress").commit();
                    return;
                }
                this.mContext.removeStickyBroadcast(SimUtility.getIntentForStickyBroadcast(this.mSlotId));
                this.simState = 4;
                SimFactoryManager.reset(this.mSlotId);
                setSimContactVisibility(false, lState);
                lEditor = settings.edit();
                lEditor.putBoolean("sim_is_there", true);
                lEditor.commit();
            } else if ("SIM_REFRESH".equals(lState)) {
                this.simState = 5;
                ensureProviderStatus();
                compareAndCopySimContactsToSimAccount();
                this.mContext.sendStickyBroadcast(SimUtility.getIntentForStickyBroadcast(this.mSlotId));
            } else if ("0".equals(lState)) {
                if (this.simState == 6) {
                    HwLog.i("SimStateServiceHandler", "Already set to sim state deactived ...");
                    settings.edit().remove("sim_handle_state_change_progress").commit();
                    return;
                }
                this.mContext.removeStickyBroadcast(SimUtility.getIntentForStickyBroadcast(this.mSlotId));
                this.simState = 6;
                ensureProviderStatus();
                setSimContactVisibility(false, lState);
                updateFavoritesWidget();
            } else if ("IMSI".equals(lState)) {
                if (this.simState == 7) {
                    HwLog.i("SimStateServiceHandler", "Already set to sim state imsi ...");
                    settings.edit().remove("sim_handle_state_change_progress").commit();
                    return;
                }
                this.simState = 7;
                lOldSimSerial = settings.getString("simimsinumber", null);
                lNewSimSerial = SimFactoryManager.getSimSerialNumber(this.mSlotId);
                this.isO2SimCard_old = settings.getBoolean("sim_is_o2card", false);
                this.isO2SimCard_new = isO2SimCard(lNewSimSerial);
                lNewSimSerial = CommonUtilMethods.getMD5Digest(lNewSimSerial);
                this.mHandleClearCallLog = true;
                if (lNewSimSerial != null) {
                    handleO2ClearCallLog(lOldSimSerial, lNewSimSerial);
                }
            }
            HwLog.i("SimStateServiceHandler", "start to fireCallback");
            if (!sCallback.isEmpty()) {
                for (ISimStateCallback callback : sCallback) {
                    callback.fireCallback(this.simState);
                }
            }
            HwLog.i("SimStateServiceHandler", "--- FINISHED SIM OPERATION --- WITH SIM STATE : " + lState + ", With slotId:" + this.mSlotId);
            settings.edit().remove("sim_handle_state_change_progress").commit();
            HwLog.i("SimStateServiceHandler", "--- Notify to UI thread Done:  With slotId:" + this.mSlotId);
            if (!QueryUtil.isHAPProviderInstalled()) {
                SimContactsCache.getInstance(this.mContext.getApplicationContext()).refresh();
            }
        }
    }

    private void createSimAccount() {
        if (!MultiUsersUtils.isCurrentUserGuest()) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "Create the SIM account slotId" + this.mSlotId);
            }
            String lAccountType = SimFactoryManager.getAccountType(this.mSlotId);
            if (!CommonUtilMethods.existGroup(this.mContext, lAccountType)) {
                insertGroup(this.mContext, this.mSlotId, lAccountType);
                if (HwLog.HWFLOW) {
                    HwLog.i("SimStateServiceHandler", "Creation of SIM account succesfully finished with slotId:" + this.mSlotId);
                }
            }
        }
    }

    private static void removeGroup(Context context, String accountType) {
        context.getContentResolver().delete(Groups.CONTENT_URI, "account_type=?", new String[]{accountType});
    }

    private static void insertGroup(Context context, int slotId, String accountType) {
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateServiceHandler", "Create the SIM account :,slotId:" + slotId);
        }
        SimDisplayInfo lSimDisplayInfo = SimFactoryManager.getAccountNameBasedOnSlotWithResId(slotId);
        String accountName = SimFactoryManager.getAccountName(slotId);
        ContentValues lValues = new ContentValues();
        lValues.put("account_name", accountName);
        lValues.put("account_type", accountType);
        lValues.put("res_package", context.getPackageName());
        lValues.put("auto_add", Integer.valueOf(1));
        lValues.put("title", lSimDisplayInfo.mName);
        lValues.put("title_res", Integer.valueOf(lSimDisplayInfo.mResId));
        lValues.put("group_is_read_only", Integer.valueOf(1));
        lValues.put("group_visible", Integer.valueOf(1));
        context.getContentResolver().insert(Groups.CONTENT_URI, lValues);
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateServiceHandler", "Creation of SIM account succesfully finished with slotId:" + slotId);
        }
    }

    public static synchronized void createSimGroup(Context context, int slotId) {
        synchronized (SimStateServiceHandler.class) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "createSimGroup slotId" + slotId);
            }
            String lAccountType = SimFactoryManager.getAccountType(slotId);
            if (!CommonUtilMethods.existGroup(context, lAccountType)) {
                insertGroup(context, slotId, lAccountType);
            }
        }
    }

    private void compareAndCopySimContactsToSimAccount() {
        if (!MultiUsersUtils.isCurrentUserGuest()) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "inside compareAndCopy");
            }
            HashMap<String, SimContact> lDbMap = new HashMap();
            String lAccountType = SimFactoryManager.getAccountType(this.mSlotId);
            int simContactsCount = -1;
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "lAccountType" + lAccountType);
            }
            Uri contactUri = RawContactsEntity.CONTENT_URI;
            String where = "account_type='" + lAccountType + "'";
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "contactUri" + contactUri);
            }
            ContentResolver lContentResolver = this.mContext.getContentResolver();
            SimFactory lSimFactory = SimFactoryManager.getSimFactory(this.mSlotId);
            if (lSimFactory == null && this.simState == 5) {
                if (HwLog.HWFLOW) {
                    HwLog.i("SimStateServiceHandler", "Sim Factory is null && SIM_STATE_REFRESH");
                }
                SimFactoryManager.initSimFactory(this.mSlotId);
                lSimFactory = SimFactoryManager.getSimFactory(this.mSlotId);
                if (lSimFactory == null) {
                    HwLog.e("SimStateServiceHandler", "Sim Factory is null again after retrying to recreate this");
                    ExceptionCapture.captureInitSimFactoryException("Sim Factory is null again after retrying to recreate this", this.mSlotId);
                }
            }
            if (lSimFactory != null) {
                SimContact simContact;
                if (HwLog.HWDBG) {
                    HwLog.v("SimStateServiceHandler", "lSimFactory is not null");
                }
                SimPersistanceManager lPersistanceManager = lSimFactory.getSimPersistanceManager();
                lPersistanceManager.performHealthCheck(lAccountType);
                if (HwLog.HWDBG) {
                    HwLog.v("SimStateServiceHandler", "After performHealthCheck ");
                }
                Cursor lDBCursor = lContentResolver.query(contactUri, null, where, null, null);
                if (lDBCursor != null) {
                    if (HwLog.HWDBG) {
                        HwLog.v("SimStateServiceHandler", "lDBCursor not null ");
                    }
                    EntityIterator entityIterator = RawContacts.newEntityIterator(lDBCursor);
                    ArrayList<SimContact> contactList = new ArrayList();
                    while (entityIterator.hasNext()) {
                        if (HwLog.HWDBG) {
                            HwLog.v("SimStateServiceHandler", "entityIterator.hasNext() ");
                        }
                        lPersistanceManager.getSimContacts((Entity) entityIterator.next(), contactList);
                        if (HwLog.HWDBG) {
                            HwLog.v("SimStateServiceHandler", "After getSimContacts ");
                        }
                    }
                    for (SimContact simContact2 : contactList) {
                        lDbMap.put(simContact2.getUniqueKeyString(), simContact2);
                    }
                    lDBCursor.close();
                }
                Cursor cursor = null;
                try {
                    cursor = lPersistanceManager.queryAll();
                } catch (Exception e) {
                    HwLog.e("SimStateServiceHandler", "Exception is thrown when query is happening  at the time of Sim Hotswap :" + e);
                }
                if (cursor != null) {
                    ArrayList<ContentProviderOperation> operationList = new ArrayList();
                    int lCurrentIndex = 0;
                    simContactsCount = cursor.getCount();
                    if (HwLog.HWDBG) {
                        HwLog.v("SimStateServiceHandler", "Found " + cursor.getCount() + " sim contacts for CompareAndCopy");
                    }
                    String lPhoneCardType = SimFactoryManager.getAccountName(this.mSlotId);
                    String simCountryISO = CommonUtilMethods.getSIMCountryIso(this.mSlotId);
                    while (cursor.moveToNext()) {
                        simContact2 = lPersistanceManager.getContact(cursor);
                        simContact2.setSIMCountryISO(simCountryISO);
                        SimContact storedSimContact = (SimContact) lDbMap.remove(simContact2.getUniqueKeyString());
                        if (storedSimContact == null) {
                            try {
                                simContact2.appendTo(operationList, operationList.size(), lPhoneCardType, lAccountType);
                                lCurrentIndex++;
                                if (lCurrentIndex % 20 == 0) {
                                    lContentResolver.applyBatch("com.android.contacts", operationList);
                                    operationList.clear();
                                }
                            } catch (RemoteException e2) {
                                HwLog.e("SimStateServiceHandler", e2.getMessage(), e2);
                            } catch (OperationApplicationException e3) {
                                HwLog.e("SimStateServiceHandler", e3.getMessage(), e3);
                            } catch (SQLiteFullException e4) {
                                HwLog.e("SimStateServiceHandler", e4.getMessage(), e4);
                                Toast.makeText(this.mContext, R.string.str_databasefull, 1).show();
                                cursor.close();
                                ExceptionCapture.markInsertSimContactsComplete("same sim card");
                                return;
                            } catch (Throwable th) {
                                cursor.close();
                                ExceptionCapture.markInsertSimContactsComplete("same sim card");
                            }
                        } else if (!storedSimContact.equals(simContact2)) {
                            SimDatabaseHelper.markSingleContactAsNonDelete(this.mContext.getApplicationContext(), storedSimContact.id);
                            simContact2.updateSimContact(this.mContext.getApplicationContext(), storedSimContact.id);
                        }
                    }
                    if (lCurrentIndex % 20 > 0) {
                        try {
                            lContentResolver.applyBatch("com.android.contacts", operationList);
                            operationList.clear();
                        } catch (SQLiteFullException e42) {
                            HwLog.e("SimStateServiceHandler", e42.getMessage(), e42);
                            Toast.makeText(this.mContext, R.string.str_databasefull, 1).show();
                        } catch (Throwable e5) {
                            HwLog.e("SimStateServiceHandler", e5.getMessage(), e5);
                        }
                    }
                    cursor.close();
                    ExceptionCapture.markInsertSimContactsComplete("same sim card");
                }
                if (HwLog.HWDBG) {
                    HwLog.v("SimStateServiceHandler", "lSimCursor is null");
                }
                int totalContactsCount = getTotalSimContactCount(this.mContext, lAccountType, SharePreferenceUtil.getDefaultSp_de(this.mContext).getBoolean("preference_contacts_only_phonenumber", false));
                if (totalContactsCount != simContactsCount) {
                    ExceptionCapture.captureCopySimToDbException("total sim count different from database in compare, tsc_" + simContactsCount + " tdc_" + totalContactsCount);
                }
            }
            if (!lDbMap.isEmpty()) {
                if (HwLog.HWDBG) {
                    HwLog.v("SimStateServiceHandler", "Found " + lDbMap.size() + " sim contacts unmatched");
                }
                StringBuilder stringBuilder = new StringBuilder("_id IN (");
                for (SimContact contact : lDbMap.values()) {
                    stringBuilder.append(contact.id).append(",");
                }
                stringBuilder.setLength(stringBuilder.length() - 1);
                stringBuilder.append(")");
                Uri lUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
                lContentResolver.delete(lUri, stringBuilder.toString(), null);
                if (HwLog.HWDBG) {
                    HwLog.v("SimStateServiceHandler", "lUri" + lUri);
                }
            }
            if (HwLog.HWDBG) {
                HwLog.v("SimStateServiceHandler", "FINISH COMPARE AND COPY");
            }
        }
    }

    private void updateFavoritesWidget() {
        this.mContext.sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
    }

    private void copySimContactsToSimAccount() {
        if (!MultiUsersUtils.isCurrentUserGuest()) {
            SimFactory lSimFactory = SimFactoryManager.getSimFactory(this.mSlotId);
            String accounType = SimFactoryManager.getAccountType(this.mSlotId);
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "Inside copySimContactsToSimAccount with mSlotId:" + this.mSlotId + " lSimFactory:" + lSimFactory);
            }
            if (lSimFactory != null) {
                SimPersistanceManager lPersistanceManager = lSimFactory.getSimPersistanceManager();
                lPersistanceManager.performHealthCheck(accounType);
                if (HwLog.HWFLOW) {
                    HwLog.i("SimStateServiceHandler", "performHealthCheck");
                }
                Cursor cursor = null;
                try {
                    cursor = lPersistanceManager.queryAll();
                } catch (Exception e) {
                    HwLog.e("SimStateServiceHandler", "Exception is thrown when query is happening  at the time of Sim Hotswap :" + e);
                    ExceptionCapture.captureSimQueryException("Exception is thrown when query sim contacts is happening", e);
                }
                if (cursor == null) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("SimStateServiceHandler", "SIM cursor is null, in copySimContactsToSimAccount");
                    }
                    return;
                }
                ExceptionCapture.markInsertSimContactsComplete("different sim card");
                int simContactsCount = cursor.getCount();
                if (HwLog.HWDBG) {
                    HwLog.v("SimStateServiceHandler", "Total number of contacts return by the ICC Provider = " + cursor.getCount());
                }
                int lCurrentIndex = 0;
                ContentResolver mContentResolver = this.mContext.getContentResolver();
                ArrayList<ContentProviderOperation> lOperationList = new ArrayList();
                String simAccountName = SimFactoryManager.getAccountName(this.mSlotId);
                String simCountryISO = CommonUtilMethods.getSIMCountryIso(this.mSlotId);
                while (cursor.moveToNext()) {
                    SimContact lSimContact = lPersistanceManager.getContact(cursor);
                    lSimContact.setSIMCountryISO(simCountryISO);
                    lSimContact.appendTo(lOperationList, lOperationList.size(), simAccountName, accounType);
                    lCurrentIndex++;
                    if (lCurrentIndex % 20 == 0) {
                        try {
                            mContentResolver.applyBatch("com.android.contacts", lOperationList);
                            lOperationList.clear();
                        } catch (RemoteException e2) {
                            HwLog.e("SimStateServiceHandler", e2.getMessage(), e2);
                        } catch (OperationApplicationException e3) {
                            HwLog.e("SimStateServiceHandler", e3.getMessage(), e3);
                            e3.printStackTrace();
                        } catch (SQLiteFullException e4) {
                            HwLog.e("SimStateServiceHandler", e4.getMessage(), e4);
                            Toast.makeText(this.mContext, R.string.str_databasefull, 1).show();
                            cursor.close();
                            return;
                        } catch (Throwable th) {
                            cursor.close();
                        }
                    }
                }
                cursor.close();
                if (lCurrentIndex % 20 > 0) {
                    try {
                        mContentResolver.applyBatch("com.android.contacts", lOperationList);
                    } catch (RemoteException e22) {
                        HwLog.e("SimStateServiceHandler", e22.getMessage(), e22);
                    } catch (OperationApplicationException e32) {
                        HwLog.e("SimStateServiceHandler", e32.getMessage(), e32);
                        e32.printStackTrace();
                    } catch (SQLiteFullException e42) {
                        HwLog.e("SimStateServiceHandler", e42.getMessage(), e42);
                        Toast.makeText(this.mContext, R.string.str_databasefull, 1).show();
                    }
                }
                int totalContactsCount = getTotalSimContactCount(this.mContext, accounType, SharePreferenceUtil.getDefaultSp_de(this.mContext).getBoolean("preference_contacts_only_phonenumber", false));
                if (totalContactsCount != simContactsCount) {
                    ExceptionCapture.captureCopySimToDbException("total sim count different from total database in copy, tsc_" + simContactsCount + " tdc_" + totalContactsCount);
                }
            }
        }
    }

    private void removeSimAccount(int slotId) {
        if (!MultiUsersUtils.isCurrentUserGuest()) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateServiceHandler", "Removing the SIM account Account  ,Subcription:" + slotId);
            }
            String lAccountType = SimFactoryManager.getAccountType(slotId);
            if (QueryUtil.isHAPProviderInstalled()) {
                this.mContext.getContentResolver().delete(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "delete_sim_account").buildUpon().appendQueryParameter("accountType", lAccountType).build(), null, null);
            } else {
                removeGroup(this.mContext, lAccountType);
                this.mContext.getContentResolver().delete(RawContacts.CONTENT_URI, "account_type=?", new String[]{lAccountType});
            }
        }
    }

    private void setSimContactVisibility(boolean visibility, String simState) {
        if (HwLog.HWDBG) {
            HwLog.d("SimStateServiceHandler", "setSimContactVisibility ,Is Sim Visible " + visibility);
        }
        SimFactoryManager.setSimAccountWritable(this.mSlotId, visibility);
        String lAccountType = SimFactoryManager.getAccountType(this.mSlotId);
        if (visibility) {
            SimDatabaseHelper.markSimContactsAsNonDeleted(this.mContext, lAccountType);
        } else if (CommonUtilMethods.isAirplaneModeOn(this.mContext) || "0".equals(simState)) {
            SimDatabaseHelper.markSimContactsAsDeleted(this.mContext, lAccountType);
        }
    }

    private void removeSimContacts() {
        if (!MultiUsersUtils.isCurrentUserGuest()) {
            String where = "account_type='" + SimFactoryManager.getAccountType(this.mSlotId) + "'";
            Uri contactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
            if (CommonConstants.LOG_INFO) {
                HwLog.i("SimStateServiceHandler", "Removed the existing contacts : " + this.mContext.getContentResolver().delete(contactUri, where, null) + " ,With slotId:" + this.mSlotId);
            } else {
                this.mContext.getContentResolver().delete(contactUri, where, null);
            }
        }
    }

    private void sendSimInitCompleteBroadcast(String aSimSerial) {
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateServiceHandler", "Sending SIM SYNC complete broadcast with slotId:" + this.mSlotId);
        }
        Editor lEditor = SimFactoryManager.getSharedPreferences("SimInfoFile", this.mSlotId).edit();
        lEditor.putBoolean("sim_is_o2card", this.isO2SimCard_new);
        lEditor.putString("simimsinumber", aSimSerial);
        lEditor.apply();
        if (HwLog.HWDBG) {
            HwLog.d("SimStateServiceHandler", "SIM lock happend for : " + (SystemClock.elapsedRealtime() / 1000) + " sec");
        }
    }

    private void ensureProviderStatus() {
        int lProviderStatus = 1;
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateServiceHandler", "ensureProviderStatus started, lProviderStatus=" + 1 + ", with slodId:" + this.mSlotId);
        }
        do {
            Cursor cursor = this.mContext.getContentResolver().query(ProviderStatus.CONTENT_URI, new String[]{"status"}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        lProviderStatus = cursor.getInt(0);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (!(lProviderStatus == 0 || lProviderStatus == 2)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    HwLog.e("SimStateServiceHandler", e.getMessage(), e);
                }
            }
            if (lProviderStatus == 0) {
                break;
            }
        } while (lProviderStatus != 2);
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateServiceHandler", "ensureProviderStatus finished, lProviderStatus=" + lProviderStatus + ", with slodId:" + this.mSlotId);
        }
    }

    private void deleteAllCallsLog() {
        this.mContext.getContentResolver().delete(Calls.CONTENT_URI, null, null);
    }

    private void handleO2ClearCallLog(String aOldSimSerial, String aNewSimSerial) {
        if (!SharePreferenceUtil.getDefaultSp_de(this.mContext).getBoolean("o2_clear_call_log", false)) {
            return;
        }
        if (aOldSimSerial == null || aNewSimSerial == null || TextUtils.equals(aOldSimSerial, aNewSimSerial)) {
            if (HwLog.HWDBG) {
                HwLog.d("SimStateServiceHandler", "Returning without the operation");
            }
            return;
        }
        if (SimFactoryManager.isDualSim()) {
            if (this.isO2SimCard_new) {
                String otherSimOldSerialNumber = getOtherOldSimSerialNumber();
                if (otherSimOldSerialNumber == null && !this.isO2SimCard_old) {
                    deleteAllCallsLog();
                } else if (!(otherSimOldSerialNumber == null || TextUtils.equals(aNewSimSerial, otherSimOldSerialNumber) || TextUtils.equals(aOldSimSerial, otherSimOldSerialNumber))) {
                    deleteAllCallsLog();
                }
            }
        } else if (this.isO2SimCard_new && !this.isO2SimCard_old) {
            deleteAllCallsLog();
        }
    }

    private boolean isO2SimCard(String serialNumber) {
        if (serialNumber == null || !serialNumber.startsWith("23410")) {
            return false;
        }
        return true;
    }

    private String getOtherOldSimSerialNumber() {
        return SimFactoryManager.getSharedPreferences("SimInfoFile", this.mSlotId == 0 ? 1 : 0).getString("simimsinumber", null);
    }

    private static int getTotalSimContactCount(Context context, String accountType, boolean isOnlyPhoneNumber) {
        String str = null;
        String AND = " AND ";
        StringBuffer lWhereClause = new StringBuffer();
        boolean hasData = false;
        if (isOnlyPhoneNumber) {
            lWhereClause.append("has_phone_number=1");
            hasData = true;
        }
        if (hasData) {
            lWhereClause.append(AND);
        }
        StringBuffer selection = new StringBuffer();
        selection.append("deleted=0");
        if (accountType.equals("com.android.huawei.sim")) {
            selection.append(AND + "account_type" + "='" + "com.android.huawei.sim" + "'");
        } else {
            if (accountType.equals("com.android.huawei.sim")) {
                selection.append(AND + "account_type" + "='" + "com.android.huawei.sim" + "'");
            }
            if (accountType.equals("com.android.huawei.secondsim")) {
                selection.append(AND + "account_type" + "='" + "com.android.huawei.secondsim" + "'");
            }
        }
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(context)) {
            selection.append(AND + "contact_id IN default_directory");
        }
        if (!CommonUtilMethods.isPrivacyModeEnabled(context)) {
            selection.append(" AND is_private = 0");
        }
        lWhereClause.append("_id IN (SELECT contact_id FROM view_raw_contacts WHERE " + selection.toString());
        lWhereClause.append(")");
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Contacts.CONTENT_URI;
            String[] strArr = new String[]{"_id"};
            if (!TextUtils.isEmpty(lWhereClause)) {
                str = lWhereClause.toString();
            }
            cursor = contentResolver.query(uri, strArr, str, null, null);
            int totalCount = 0;
            if (cursor != null) {
                totalCount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
            return totalCount;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void defineIfWeNeedRemoveSimAccount(int slotId) {
        if (this.mContext != null) {
            UserManager userManager = (UserManager) this.mContext.getSystemService("user");
            if (userManager == null || !userManager.isUserUnlocked()) {
                HwLog.i("SimStateServiceHandler", "user locked, not remove sim account");
            } else {
                HwLog.i("SimStateServiceHandler", "user unlocked, remove sim account");
                removeSimAccount(slotId);
            }
        }
    }
}
