package com.android.contacts.hap.copy;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.EntityIterator;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimContact;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimPersistanceManager;
import com.android.contacts.hap.sim.advanced.AdvancedSimContact;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.ProcessorBase;
import com.google.android.collect.Sets;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CopyContactsProcessor extends ProcessorBase {
    private static HashSet<String> mIncludedMimetypes = Sets.newHashSet();
    private Handler handler;
    private String mAccountName;
    private final String mAccountType;
    private volatile boolean mCanceled;
    private Context mContext;
    private String mCopyLabel;
    private String mCurrentAccountType;
    private int mCurrentCount;
    private final String mDataset;
    private volatile boolean mDone;
    public boolean mExportToSimFlag;
    private HwCustCopyContactsProcessor mHwCust;
    public boolean mImportToSimFlag;
    private boolean mInterrupt;
    private final int mJobId;
    private final ContactsCopyListener mListener;
    private ArrayList<NamedContentValues> mNCVList;
    private long[] mRawContactIds;
    private ContentResolver mResolver;
    private final CopyContactService mService;
    private int mTotalCount;
    private Messenger msger;

    private void runInternal() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00f9 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r13 = this;
        r0 = com.android.contacts.util.HwLog.HWDBG;
        if (r0 == 0) goto L_0x000d;
    L_0x0004:
        r0 = "CopyContactsProcessor";
        r1 = ">>>>>>>>> Copy Contacts Started <<<<<<<<";
        com.android.contacts.util.HwLog.d(r0, r1);
    L_0x000d:
        r0 = r13.mAccountType;
        r12 = com.android.contacts.hap.sim.SimFactoryManager.getSlotIdBasedOnAccountType(r0);
        r0 = "SimInfoFile";
        r11 = com.android.contacts.hap.sim.SimFactoryManager.getSharedPreferences(r0, r12);
        r0 = r13.mRawContactIds;
        r0 = r0.length;
        r13.mTotalCount = r0;
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r0 = "_id IN (";
        r10.append(r0);
        r13.appendIds(r10);
        r0 = ")";
        r10.append(r0);
        r8 = 0;
        r0 = r13.mResolver;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r1 = android.provider.ContactsContract.RawContactsEntity.CONTENT_URI;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r3 = r10.toString();	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r2 = 0;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r4 = 0;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r5 = 0;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r8 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r0 = r13.mAccountType;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r0 = com.android.contacts.hap.CommonUtilMethods.isSimAccount(r0);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        if (r0 == 0) goto L_0x00c8;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x004b:
        r0 = com.android.contacts.util.HwLog.HWDBG;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        if (r0 == 0) goto L_0x0058;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x004f:
        r0 = "CopyContactsProcessor";	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r1 = "Its sim contact";	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        com.android.contacts.util.HwLog.d(r0, r1);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x0058:
        r13.insertRawContactToSim(r8);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x005b:
        if (r8 == 0) goto L_0x0060;
    L_0x005d:
        r8.close();
    L_0x0060:
        r0 = r11.edit();
        r1 = "sim_copy_contacts_progress";
        r2 = 0;
        r0 = r0.putBoolean(r1, r2);
        r0.apply();
    L_0x006f:
        r0 = r13.mCurrentCount;
        if (r0 < 0) goto L_0x0180;
    L_0x0073:
        r0 = r13.mExportToSimFlag;
        if (r0 == 0) goto L_0x0120;
    L_0x0077:
        r0 = r13.mService;
        r0 = r0.getResources();
        r1 = r13.mCurrentCount;
        r2 = 2131886086; // 0x7f120006 float:1.940674E38 double:1.053291676E-314;
        r9 = r0.getQuantityString(r2, r1);
    L_0x0086:
        r0 = "com.android.huawei.phone";
        r1 = r13.mAccountType;
        r0 = r0.equalsIgnoreCase(r1);
        if (r0 == 0) goto L_0x0146;
    L_0x0091:
        r0 = 2;
        r0 = new java.lang.Object[r0];
        r1 = r13.mCurrentCount;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = 0;
        r0[r2] = r1;
        r1 = r13.mService;
        r2 = 2131362172; // 0x7f0a017c float:1.8344117E38 double:1.053032828E-314;
        r1 = r1.getString(r2);
        r2 = 1;
        r0[r2] = r1;
        r9 = java.lang.String.format(r9, r0);
    L_0x00ad:
        r0 = r13.mListener;
        r1 = r13.mJobId;
        r0.onCopyContactsFinished(r9, r1);
        r0 = 1;
        r1 = -1;
        r2 = 0;
        r13.sendMessage(r0, r1, r2);
        r0 = com.android.contacts.util.HwLog.HWFLOW;
        if (r0 == 0) goto L_0x00c7;
    L_0x00be:
        r0 = "CopyContactsProcessor";
        r1 = "send message finish MAG_CANACL";
        com.android.contacts.util.HwLog.i(r0, r1);
    L_0x00c7:
        return;
    L_0x00c8:
        r0 = 5;
        r1 = 0;
        r2 = 0;
        r13.sendMessage(r0, r1, r2);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r0 = com.android.contacts.util.HwLog.HWFLOW;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        if (r0 == 0) goto L_0x00db;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x00d2:
        r0 = "CopyContactsProcessor";	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r1 = "send message MSG_SHOW_LATER MAG_DATA_DEFAULT";	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        com.android.contacts.util.HwLog.i(r0, r1);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x00db:
        r13.insertRawContactAndDataTable(r8);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r0 = r13.mAccountType;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r1 = "com.android.huawei.phone";	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        r0 = android.text.TextUtils.equals(r0, r1);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        if (r0 == 0) goto L_0x005b;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
    L_0x00e9:
        r0 = r13.mContext;	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        com.android.contacts.hap.util.AutoMergeContacts.autoMergeRawContacts(r0);	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        goto L_0x005b;
    L_0x00f0:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x00f0, all -> 0x010a }
        if (r8 == 0) goto L_0x00f9;
    L_0x00f6:
        r8.close();
    L_0x00f9:
        r0 = r11.edit();
        r1 = "sim_copy_contacts_progress";
        r2 = 0;
        r0 = r0.putBoolean(r1, r2);
        r0.apply();
        goto L_0x006f;
    L_0x010a:
        r0 = move-exception;
        if (r8 == 0) goto L_0x0110;
    L_0x010d:
        r8.close();
    L_0x0110:
        r1 = r11.edit();
        r2 = "sim_copy_contacts_progress";
        r3 = 0;
        r1 = r1.putBoolean(r2, r3);
        r1.apply();
        throw r0;
    L_0x0120:
        r0 = r13.mImportToSimFlag;
        if (r0 == 0) goto L_0x0135;
    L_0x0124:
        r0 = r13.mService;
        r0 = r0.getResources();
        r1 = r13.mCurrentCount;
        r2 = 2131886098; // 0x7f120012 float:1.9406765E38 double:1.053291682E-314;
        r9 = r0.getQuantityString(r2, r1);
        goto L_0x0086;
    L_0x0135:
        r0 = r13.mService;
        r0 = r0.getResources();
        r1 = r13.mCurrentCount;
        r2 = 2131886085; // 0x7f120005 float:1.9406739E38 double:1.0532916754E-314;
        r9 = r0.getQuantityString(r2, r1);
        goto L_0x0086;
    L_0x0146:
        r0 = r13.mAccountType;
        r0 = com.android.contacts.hap.CommonUtilMethods.isSimAccount(r0);
        if (r0 == 0) goto L_0x0169;
    L_0x014e:
        r0 = r13.mAccountType;
        r6 = com.android.contacts.hap.sim.SimFactoryManager.getSimCardDisplayLabel(r0);
        r0 = 2;
        r0 = new java.lang.Object[r0];
        r1 = r13.mCurrentCount;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = 0;
        r0[r2] = r1;
        r1 = 1;
        r0[r1] = r6;
        r9 = java.lang.String.format(r9, r0);
        goto L_0x00ad;
    L_0x0169:
        r0 = 2;
        r0 = new java.lang.Object[r0];
        r1 = r13.mCurrentCount;
        r1 = java.lang.Integer.valueOf(r1);
        r2 = 0;
        r0[r2] = r1;
        r1 = r13.mAccountName;
        r2 = 1;
        r0[r2] = r1;
        r9 = java.lang.String.format(r9, r0);
        goto L_0x00ad;
    L_0x0180:
        r0 = r13.mExportToSimFlag;
        if (r0 == 0) goto L_0x018f;
    L_0x0184:
        r0 = r13.mService;
        r1 = 2131362590; // 0x7f0a031e float:1.8344965E38 double:1.0530330346E-314;
        r9 = r0.getString(r1);
        goto L_0x00ad;
    L_0x018f:
        r0 = r13.mImportToSimFlag;
        if (r0 == 0) goto L_0x019e;
    L_0x0193:
        r0 = r13.mService;
        r1 = 2131362888; // 0x7f0a0448 float:1.834557E38 double:1.053033182E-314;
        r9 = r0.getString(r1);
        goto L_0x00ad;
    L_0x019e:
        r0 = r13.mService;
        r1 = 2131362589; // 0x7f0a031d float:1.8344963E38 double:1.053033034E-314;
        r9 = r0.getString(r1);
        goto L_0x00ad;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.copy.CopyContactsProcessor.runInternal():void");
    }

    static {
        mIncludedMimetypes.add("vnd.android.cursor.item/email_v2");
        mIncludedMimetypes.add("vnd.android.cursor.item/identity");
        mIncludedMimetypes.add("vnd.android.cursor.item/im");
        mIncludedMimetypes.add("vnd.android.cursor.item/nickname");
        mIncludedMimetypes.add("vnd.android.cursor.item/note");
        mIncludedMimetypes.add("vnd.android.cursor.item/organization");
        mIncludedMimetypes.add("vnd.android.cursor.item/phone_v2");
        mIncludedMimetypes.add("vnd.android.cursor.item/photo");
        mIncludedMimetypes.add("vnd.android.cursor.item/relation");
        mIncludedMimetypes.add("vnd.android.cursor.item/sip_address");
        mIncludedMimetypes.add("vnd.android.cursor.item/name");
        mIncludedMimetypes.add("vnd.android.cursor.item/postal-address_v2");
        mIncludedMimetypes.add("vnd.android.cursor.item/website");
        mIncludedMimetypes.add("vnd.android.cursor.item/contact_event");
    }

    public CopyContactsProcessor(Context context, CopyContactService service, ContactsCopyListener listener, long[] aRawContactIds, int jobId, String aAccountName, String aAccountType, String aDataset, String aCopyLabel, Messenger aMsger) {
        this(service, listener, jobId, aAccountName, aAccountType, aDataset, aCopyLabel);
        this.mRawContactIds = new long[aRawContactIds.length];
        System.arraycopy(aRawContactIds, 0, this.mRawContactIds, 0, aRawContactIds.length);
        this.mContext = context;
        this.msger = aMsger;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCust = (HwCustCopyContactsProcessor) HwCustUtils.createObj(HwCustCopyContactsProcessor.class, new Object[]{context});
        }
    }

    public CopyContactsProcessor(Context context, CopyContactService service, ContactsCopyListener listener, long[] aRawContactIds, int jobId, String aAccountName, String aAccountType, String aDataset, String aCopyLabel, Messenger aMsger, String aCurrentAccountType) {
        this(context, service, listener, aRawContactIds, jobId, aAccountName, aAccountType, aDataset, aCopyLabel, aMsger);
        this.mCurrentAccountType = aCurrentAccountType;
    }

    public CopyContactsProcessor(CopyContactService service, ContactsCopyListener listener, int jobId, String aAccountName, String aAccountType, String aDataset, String aCopyLabel) {
        this.msger = null;
        this.mCurrentCount = 0;
        this.mTotalCount = 0;
        this.mExportToSimFlag = false;
        this.mImportToSimFlag = false;
        this.mContext = null;
        this.mHwCust = null;
        this.mNCVList = new ArrayList();
        this.handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 3) {
                    CopyContactsProcessor.this.cancel(((Boolean) msg.obj).booleanValue());
                }
            }
        };
        this.mService = service;
        this.mListener = listener;
        this.mJobId = jobId;
        this.mAccountName = aAccountName;
        this.mAccountType = aAccountType;
        this.mDataset = aDataset;
        this.mCopyLabel = aCopyLabel;
        initProcessorData();
    }

    private void initProcessorData() {
        if (TextUtils.isEmpty(this.mCopyLabel)) {
            this.mCopyLabel = this.mAccountName;
        }
        this.mResolver = this.mService.getContentResolver();
    }

    public void setExportToSimFlag(boolean aExportToSimFlag) {
        this.mExportToSimFlag = aExportToSimFlag;
    }

    public void setImportToSimFlag(boolean aImportToSimFlag) {
        this.mImportToSimFlag = aImportToSimFlag;
    }

    public final int getType() {
        return 0;
    }

    public void run() {
        try {
            if (this.mCanceled || !isAccountExists()) {
                String lDescription;
                if (!this.mCanceled) {
                    lDescription = String.format(this.mService.getString(R.string.copy_contacts_canceled_no_account_title), new Object[]{this.mCopyLabel});
                } else if (this.mExportToSimFlag) {
                    lDescription = String.format(this.mService.getString(R.string.export_contacts_canceled_title), new Object[]{this.mCopyLabel});
                } else if (this.mImportToSimFlag) {
                    lDescription = String.format(this.mService.getString(R.string.import_contacts_canceled_title), new Object[]{this.mCopyLabel});
                } else {
                    lDescription = String.format(this.mService.getString(R.string.copy_contacts_canceled_title), new Object[]{this.mCopyLabel});
                }
                this.mListener.onCopyContactsCanceled(lDescription, this.mJobId);
                sendMessage(1, -1, null);
                if (HwLog.HWFLOW) {
                    HwLog.i("CopyContactsProcessor", "send message MAG_CANACL MAG_CANACL");
                }
                this.mDone = true;
                synchronized (this) {
                    this.mDone = true;
                    this.mService.handleFinishCopyContactsNotification(this.mJobId, this.mDone);
                }
                return;
            }
            runInternal();
            synchronized (this) {
                this.mDone = true;
                this.mService.handleFinishCopyContactsNotification(this.mJobId, this.mDone);
            }
        } catch (OutOfMemoryError e) {
            HwLog.e("CopyContactsProcessor", "OutOfMemoryError thrown during copy", e);
            throw e;
        } catch (RuntimeException e2) {
            HwLog.e("CopyContactsProcessor", "RuntimeException thrown during copy", e2);
            throw e2;
        } catch (Throwable th) {
            synchronized (this) {
                this.mDone = true;
                this.mService.handleFinishCopyContactsNotification(this.mJobId, this.mDone);
            }
        }
    }

    private boolean isAccountExists() {
        for (AccountWithDataSet account : AccountTypeManager.getInstance(this.mService).getAccounts(true)) {
            if ((account.name.equals(this.mAccountName) && account.type.equals(this.mAccountType) && TextUtils.equals(account.dataSet, this.mDataset)) || (CommonUtilMethods.isSimAccount(this.mAccountType) && account.type.equals(this.mAccountType))) {
                return true;
            }
        }
        return false;
    }

    private void appendIds(StringBuilder aBuilder) {
        for (long aRawContactId : this.mRawContactIds) {
            aBuilder.append(aRawContactId);
            aBuilder.append(",");
        }
        aBuilder.setLength(aBuilder.length() - 1);
    }

    private void insertRawContactToSim(Cursor aCursor) {
        if (HwLog.HWDBG) {
            HwLog.d("CopyContactsProcessor", "inside insertRawContactToSim");
        }
        int sloTd = SimFactoryManager.getSlotIdBasedOnAccountType(this.mAccountType);
        SharedPreferences prefs = SimFactoryManager.getSharedPreferences("SimInfoFile", sloTd);
        for (boolean isDeleteInProgress = prefs.getBoolean("sim_delete_progress", false); isDeleteInProgress; isDeleteInProgress = prefs.getBoolean("sim_delete_progress", false)) {
            try {
                HwLog.i("CopyContactsProcessor", "Waiting for SIM to complete delete operation");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        HashMap<String, Integer> simUidMap = getAvailableContactsInSIM();
        int lAvailableFreeSpace = SimFactoryManager.getSimConfig(SimFactoryManager.getAccountType(sloTd)).getAvailableFreeSpace();
        if (aCursor.moveToFirst()) {
            SimFactory lSimFactory = SimFactoryManager.getSimFactory(sloTd);
            String accountType = SimFactoryManager.getAccountType(sloTd);
            if (lSimFactory != null) {
                SimPersistanceManager manager = lSimFactory.getSimPersistanceManager();
                ArrayList<ContentProviderOperation> lOperation = new ArrayList();
                EntityIterator entityIterator = RawContacts.newEntityIterator(aCursor);
                String simAccountName = SimFactoryManager.getAccountName(sloTd);
                List<SimContact> contactList = new ArrayList();
                sendMessage(3, 0, this.handler);
                if (HwLog.HWFLOW) {
                    HwLog.i("CopyContactsProcessor", "send message MSG_HANDLE MAG_DATA_DEFAULT");
                }
                while (entityIterator.hasNext() && contactList.size() < lAvailableFreeSpace) {
                    if (this.mInterrupt) {
                        this.mListener.onCopyContactsCanceled(this.mCopyLabel, this.mJobId);
                        sendMessage(1, -1, null);
                        if (HwLog.HWFLOW) {
                            HwLog.i("CopyContactsProcessor", "send message interrupt MAG_CANACL");
                        }
                        return;
                    }
                    manager.getContacts((Entity) entityIterator.next(), contactList);
                }
                if (HwLog.HWDBG) {
                    HwLog.d("CopyContactsProcessor", ">>>>>>>>> Copying Contacts to the SIM card Started <<<<<<<<");
                }
                prefs.edit().putBoolean("sim_copy_contacts_progress", true).apply();
                this.mTotalCount = contactList.size();
                if (this.mTotalCount > lAvailableFreeSpace) {
                    this.mTotalCount = lAvailableFreeSpace;
                }
                sendMessage(2, this.mTotalCount, null);
                if (HwLog.HWFLOW) {
                    HwLog.i("CopyContactsProcessor", "send message MSG_SHOW " + this.mTotalCount);
                }
                lOperation.clear();
                String simCountryISO = CommonUtilMethods.getSIMCountryIso(sloTd);
                boolean isEmailFull = false;
                for (SimContact contact : contactList) {
                    contact.setSIMCountryISO(simCountryISO);
                    if (lAvailableFreeSpace <= 0) {
                        if (lOperation.size() > 0) {
                            executeSimBatch(lOperation);
                        }
                        return;
                    }
                    if (simUidMap.containsKey(contact.hashCode() + "") || contact.isContactEmpty()) {
                        if (HwLog.HWFLOW) {
                            HwLog.i("CopyContactsProcessor", "It's a duplicated contact.");
                        }
                    } else if (this.mInterrupt) {
                        if (lOperation.size() > 0) {
                            executeSimBatch(lOperation);
                        }
                        this.mListener.onCopyContactsCanceled(this.mCopyLabel, this.mJobId);
                        sendMessage(1, -1, null);
                        if (HwLog.HWFLOW) {
                            HwLog.i("CopyContactsProcessor", "send message interrupt MAG_CANACL");
                        }
                        return;
                    } else {
                        if ((contact instanceof AdvancedSimContact) && !TextUtils.isEmpty(((AdvancedSimContact) contact).email) && lSimFactory.getSimConfig().isEmailEnabled() && SimFactoryManager.getSpareEmailCount(sloTd) == 0) {
                            if (!isEmailFull) {
                                isEmailFull = true;
                                this.mListener.onCopyContactsFailed(this.mService.getString(R.string.sim_available_space_full));
                            }
                            ((AdvancedSimContact) contact).email = "";
                        }
                        if (contact instanceof AdvancedSimContact) {
                            String lEmail = ((AdvancedSimContact) contact).email;
                            if (!TextUtils.isEmpty(lEmail) && lEmail.length() > 38) {
                                ((AdvancedSimContact) contact).email = lEmail.substring(0, 38);
                            }
                        }
                        if (manager.insert(contact, false) != null) {
                            contact.appendTo(lOperation, lOperation.size(), simAccountName, accountType);
                            simUidMap.put(contact.hashCode() + "", Integer.valueOf(1));
                            this.mCurrentCount++;
                            lAvailableFreeSpace--;
                            if (this.mCurrentCount % 25 == 0) {
                                try {
                                    this.mResolver.applyBatch("com.android.contacts", lOperation);
                                    lOperation.clear();
                                    this.mListener.onCopyContactsParsed(this.mCopyLabel, null, this.mJobId, contact.name, this.mCurrentCount, this.mTotalCount, this.mService);
                                    sendMessage(1, this.mCurrentCount, contact.name);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i("CopyContactsProcessor", "send message MSG_DATE " + this.mCurrentCount);
                                    }
                                } catch (Throwable e3) {
                                    HwLog.e("CopyContactsProcessor", e3.getMessage(), e3);
                                    this.mListener.onCopyContactsParsed(this.mCopyLabel, null, this.mJobId, contact.name, this.mCurrentCount, this.mTotalCount, this.mService);
                                    sendMessage(1, this.mCurrentCount, contact.name);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i("CopyContactsProcessor", "send message MSG_DATE " + this.mCurrentCount);
                                    }
                                } catch (Throwable e4) {
                                    HwLog.e("CopyContactsProcessor", e4.getMessage(), e4);
                                    this.mListener.onCopyContactsParsed(this.mCopyLabel, null, this.mJobId, contact.name, this.mCurrentCount, this.mTotalCount, this.mService);
                                    sendMessage(1, this.mCurrentCount, contact.name);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i("CopyContactsProcessor", "send message MSG_DATE " + this.mCurrentCount);
                                    }
                                } catch (SQLiteFullException e5) {
                                    this.mListener.onCopyContactsFailed(this.mService.getString(R.string.str_databasefull));
                                    sendMessage(1, -1, null);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i("CopyContactsProcessor", "send message exception MAG_CANACL");
                                    }
                                    this.mCurrentCount = 0;
                                    this.mListener.onCopyContactsParsed(this.mCopyLabel, null, this.mJobId, contact.name, this.mCurrentCount, this.mTotalCount, this.mService);
                                    sendMessage(1, this.mCurrentCount, contact.name);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i("CopyContactsProcessor", "send message MSG_DATE " + this.mCurrentCount);
                                    }
                                    return;
                                } catch (Throwable th) {
                                    Throwable th2 = th;
                                    this.mListener.onCopyContactsParsed(this.mCopyLabel, null, this.mJobId, contact.name, this.mCurrentCount, this.mTotalCount, this.mService);
                                    sendMessage(1, this.mCurrentCount, contact.name);
                                    if (HwLog.HWFLOW) {
                                        HwLog.i("CopyContactsProcessor", "send message MSG_DATE " + this.mCurrentCount);
                                    }
                                }
                            } else if (this.mCurrentCount >= this.mTotalCount) {
                                this.mListener.onCopyContactsParsed(this.mCopyLabel, null, this.mJobId, contact.name, this.mCurrentCount, this.mTotalCount, this.mService);
                                sendMessage(1, this.mCurrentCount, contact.name);
                                if (HwLog.HWFLOW) {
                                    HwLog.i("CopyContactsProcessor", "send message finish MSG_DATE " + this.mCurrentCount);
                                }
                            }
                        } else {
                            if (lOperation.size() > 0) {
                                executeSimBatch(lOperation);
                            }
                            String accountName = SimFactoryManager.getSimCardDisplayLabel(accountType);
                            this.mListener.onCopyContactsFailed(this.mService.getString(R.string.sim_full, new Object[]{accountName, accountName}));
                            sendMessage(1, -1, null);
                            if (HwLog.HWFLOW) {
                                HwLog.i("CopyContactsProcessor", "send message MSG_DATE MAG_CANACL");
                            }
                            return;
                        }
                    }
                }
                if (lOperation.size() > 0) {
                    executeSimBatch(lOperation);
                }
            } else {
                this.mListener.onCopyContactsFailed(this.mService.getString(R.string.str_sim_busy));
                sendMessage(1, -1, null);
                if (HwLog.HWFLOW) {
                    HwLog.i("CopyContactsProcessor", "send message fail MAG_CANACL");
                }
            }
        }
    }

    private HashMap<String, Integer> getAvailableContactsInSIM() {
        HashMap<String, Integer> simContactsUidMap = new HashMap();
        StringBuilder selection = new StringBuilder("account_type").append("=? AND deleted=0");
        String[] selectionArgs = new String[]{this.mAccountType};
        Cursor lCursor = this.mResolver.query(RawContacts.CONTENT_URI, new String[]{"sync3"}, selection.toString(), selectionArgs, null);
        try {
            if (lCursor.moveToFirst()) {
                do {
                    simContactsUidMap.put(lCursor.getString(0), Integer.valueOf(1));
                } while (lCursor.moveToNext());
            }
            lCursor.close();
            return simContactsUidMap;
        } catch (Throwable th) {
            lCursor.close();
        }
    }

    private void insertRawContactAndDataTable(Cursor aDataCursor) {
        if (HwLog.HWFLOW) {
            HwLog.i("CopyContactsProcessor", "insertRawContactAndDataTable");
        }
        String lSourceAccountType = null;
        int copyContactsBatchSize = 25;
        if (CommonUtilMethods.isSimAccount(this.mCurrentAccountType)) {
            copyContactsBatchSize = 50;
        }
        if (aDataCursor.moveToFirst()) {
            lSourceAccountType = aDataCursor.getString(aDataCursor.getColumnIndex("account_type"));
        }
        if (CommonUtilMethods.isSimAccount(lSourceAccountType)) {
            mIncludedMimetypes.remove("vnd.android.cursor.item/photo");
        } else if (!mIncludedMimetypes.contains("vnd.android.cursor.item/photo")) {
            mIncludedMimetypes.add("vnd.android.cursor.item/photo");
        }
        int index = aDataCursor.getColumnIndex("is_private");
        long aPrivate = -1;
        if (index >= 0) {
            aPrivate = aDataCursor.getLong(index);
        }
        int singleBatchCount = 0;
        ContentValues lContentValues = new ContentValues();
        ArrayList<ContentProviderOperation> lOperation = new ArrayList();
        EntityIterator entityIterator = RawContacts.newEntityIterator(aDataCursor);
        sendMessage(3, 0, this.handler);
        while (entityIterator.hasNext()) {
            if (this.mInterrupt) {
                this.mListener.onCopyContactsCanceled(this.mCopyLabel, this.mJobId);
                sendMessage(1, -1, null);
                return;
            }
            int backReference = lOperation.size();
            Entity lEntity = (Entity) entityIterator.next();
            Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
            builder.withValue("account_name", this.mAccountName);
            builder.withValue("account_type", this.mAccountType);
            builder.withValue("data_set", this.mDataset);
            if (this.mHwCust != null) {
                this.mHwCust.setAggregationMode(builder);
            }
            if (aPrivate != -1) {
                builder.withValue("is_private", Long.valueOf(aPrivate));
            }
            lOperation.add(builder.build());
            this.mNCVList.clear();
            this.mNCVList.addAll(lEntity.getSubValues());
            for (NamedContentValues ncValues : this.mNCVList) {
                lContentValues.clear();
                lContentValues.putAll(ncValues.values);
                if (mIncludedMimetypes.contains(lContentValues.getAsString("mimetype"))) {
                    lContentValues.remove("_id");
                    lContentValues.remove("group_sourceid");
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference("raw_contact_id", backReference);
                    builder.withValues(lContentValues);
                    lOperation.add(builder.build());
                }
            }
            singleBatchCount++;
            if (singleBatchCount >= copyContactsBatchSize) {
                this.mCurrentCount += singleBatchCount;
                if (executeBatch(lOperation)) {
                    lOperation.clear();
                    singleBatchCount = 0;
                } else {
                    return;
                }
            }
        }
        if (!lOperation.isEmpty() && singleBatchCount > 0) {
            if (HwLog.HWDBG) {
                HwLog.d("CopyContactsProcessor", "last execution for : " + singleBatchCount);
            }
            this.mCurrentCount += singleBatchCount;
            if (executeBatch(lOperation)) {
                lOperation.clear();
            }
        }
    }

    private boolean executeBatch(ArrayList<ContentProviderOperation> aOperation) {
        try {
            Thread.sleep(350);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            this.mResolver.applyBatch("com.android.contacts", aOperation);
            this.mListener.onCopyContactsParsed(this.mCopyLabel, this.mAccountType, this.mJobId, this.mCopyLabel, this.mCurrentCount, this.mTotalCount, this.mService);
            sendMessage(1, this.mCurrentCount, null);
        } catch (RemoteException e) {
            if (this.mExportToSimFlag) {
                this.mListener.onCopyContactsFailed(this.mService.getString(R.string.str_export_failed));
                sendMessage(1, -1, null);
            } else if (this.mImportToSimFlag) {
                this.mListener.onCopyContactsFailed(this.mService.getString(R.string.str_import_failed));
                sendMessage(1, -1, null);
            } else {
                this.mListener.onCopyContactsFailed(this.mService.getString(R.string.str_copy_failed));
                sendMessage(1, -1, null);
            }
            this.mCurrentCount = 0;
            this.mListener.onCopyContactsParsed(this.mCopyLabel, this.mAccountType, this.mJobId, this.mCopyLabel, this.mCurrentCount, this.mTotalCount, this.mService);
            sendMessage(1, this.mCurrentCount, null);
            return false;
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
            this.mListener.onCopyContactsParsed(this.mCopyLabel, this.mAccountType, this.mJobId, this.mCopyLabel, this.mCurrentCount, this.mTotalCount, this.mService);
            sendMessage(1, this.mCurrentCount, null);
        } catch (SQLiteFullException e3) {
            this.mListener.onCopyContactsFailed(this.mService.getString(R.string.str_databasefull));
            sendMessage(1, -1, null);
            this.mCurrentCount = 0;
            this.mListener.onCopyContactsParsed(this.mCopyLabel, this.mAccountType, this.mJobId, this.mCopyLabel, this.mCurrentCount, this.mTotalCount, this.mService);
            sendMessage(1, this.mCurrentCount, null);
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            this.mListener.onCopyContactsParsed(this.mCopyLabel, this.mAccountType, this.mJobId, this.mCopyLabel, this.mCurrentCount, this.mTotalCount, this.mService);
            sendMessage(1, this.mCurrentCount, null);
        }
        return true;
    }

    private boolean executeSimBatch(ArrayList<ContentProviderOperation> aOperation) {
        try {
            Thread.sleep(350);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            this.mResolver.applyBatch("com.android.contacts", aOperation);
            aOperation.clear();
        } catch (RemoteException e) {
            HwLog.e("CopyContactsProcessor", e.getMessage(), e);
        } catch (OperationApplicationException e2) {
            HwLog.e("CopyContactsProcessor", e2.getMessage(), e2);
        } catch (SQLiteFullException e3) {
            HwLog.e("CopyContactsProcessor", e3.getMessage(), e3);
            return false;
        }
        return true;
    }

    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (HwLog.HWDBG) {
            HwLog.d("CopyContactsProcessor", "CopyContactsProcessor received cancel request");
        }
        if (this.mDone || this.mCanceled) {
            return false;
        }
        this.mCanceled = true;
        synchronized (this) {
            this.mInterrupt = mayInterruptIfRunning;
        }
        return true;
    }

    public synchronized boolean isCancelled() {
        return this.mCanceled;
    }

    public synchronized boolean isDone() {
        return this.mDone;
    }

    public synchronized void cancelAndNotified(boolean aNotified) {
        if (HwLog.HWDBG) {
            HwLog.d("CopyContactsProcessor", "received cancel request and notified");
        }
    }

    private void sendMessage(int what, int count, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        if (count != 0) {
            msg.arg1 = count;
        }
        if (obj != null) {
            msg.obj = obj;
        }
        try {
            if (this.msger != null) {
                this.msger.send(msg);
            }
        } catch (Exception e) {
            HwLog.i("CopyContactsProcessor", "send message fail.");
            e.printStackTrace();
        }
    }
}
