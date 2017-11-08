package com.huawei.rcs.capability;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.huawei.rcs.RCSServiceListener;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.IfMsgplus.Stub;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.metadata.Capabilities;
import com.huawei.rcs.util.MLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CapabilityService {
    private static HashMap<String, CapabilityService> mCapabilityServices = new HashMap();
    private static IfMsgplus mRcsService = null;
    private Context mContext;
    private boolean mIsBindService = false;
    private HashMap<Integer, IfMsgplusCb> mRcseCallbackList;
    private List<RCSServiceListener> mServiceBindStatusListenerList;
    private ServiceConnection mrcsServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName aClassName, IBinder aService) {
            CapabilityService.mRcsService = Stub.asInterface(aService);
            MLog.d("RCSCapability", "bindservice");
            if (CapabilityService.mRcsService != null) {
                try {
                    for (Entry<Integer, IfMsgplusCb> entry : CapabilityService.this.mRcseCallbackList.entrySet()) {
                        CapabilityService.mRcsService.registerCallback(((Integer) entry.getKey()).intValue(), (IfMsgplusCb) entry.getValue());
                    }
                    MLog.d("RCSCapability", "registerCallback mRcseCallback");
                } catch (RemoteException e) {
                    MLog.d("RCSCapability", "registerCallback" + e.toString());
                }
            }
            if (CapabilityService.this.mServiceBindStatusListenerList != null) {
                for (int i = 0; i < CapabilityService.this.mServiceBindStatusListenerList.size(); i++) {
                    ((RCSServiceListener) CapabilityService.this.mServiceBindStatusListenerList.get(i)).onServiceConnected();
                }
            }
        }

        public void onServiceDisconnected(ComponentName aClassName) {
            MLog.d("RCSCapability", "the remote of RcsService is crashed or killed");
            if (CapabilityService.this.mIsBindService) {
                CapabilityService.this.mContext.unbindService(CapabilityService.this.mrcsServiceConnection);
                CapabilityService.this.mIsBindService = false;
            }
            CapabilityService.mRcsService = null;
            MLog.d("RCSCapability", "try to rebind service");
            CapabilityService.this.bindservice();
            CapabilityService.this.mIsBindService = true;
        }
    };

    public boolean isRCSUeserByContactId(long r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0075 in list []
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
        r11 = this;
        r9 = 0;
        r8 = 0;
        r1 = r11.mContext;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r0 = r1.getContentResolver();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r1 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2.<init>();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r3 = "contact_id=";	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = r2.append(r12);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r3 = r2.toString();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = 0;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r4 = 0;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r5 = 0;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r8 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        if (r8 != 0) goto L_0x002d;
    L_0x0027:
        if (r8 == 0) goto L_0x002c;
    L_0x0029:
        r8.close();
    L_0x002c:
        return r9;
    L_0x002d:
        r1 = r8.moveToNext();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        if (r1 == 0) goto L_0x004b;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
    L_0x0033:
        r1 = "data1";	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r1 = r8.getColumnIndex(r1);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r6 = r8.getString(r1);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r1 = r11.isRCSContact(r6);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        if (r1 == 0) goto L_0x002d;
    L_0x0044:
        r1 = 1;
        if (r8 == 0) goto L_0x004a;
    L_0x0047:
        r8.close();
    L_0x004a:
        return r1;
    L_0x004b:
        if (r8 == 0) goto L_0x0050;
    L_0x004d:
        r8.close();
    L_0x0050:
        return r9;
    L_0x0051:
        r7 = move-exception;
        r1 = "RCSCapability";	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2.<init>();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r3 = "isRCSUeserByContactId";	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r3 = r7.toString();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        r2 = r2.toString();	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        com.huawei.rcs.util.MLog.d(r1, r2);	 Catch:{ SQLException -> 0x0051, all -> 0x0076 }
        if (r8 == 0) goto L_0x0075;
    L_0x0072:
        r8.close();
    L_0x0075:
        return r9;
    L_0x0076:
        r1 = move-exception;
        if (r8 == 0) goto L_0x007c;
    L_0x0079:
        r8.close();
    L_0x007c:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.rcs.capability.CapabilityService.isRCSUeserByContactId(long):boolean");
    }

    private CapabilityService(Context context) {
        this.mContext = context;
        this.mRcseCallbackList = new HashMap();
        this.mServiceBindStatusListenerList = new ArrayList();
        if (mRcsService == null) {
            bindservice();
            this.mIsBindService = true;
        }
    }

    public static void init(Context context, String appName) {
        MLog.d("RCSCapability", "please make sure this fuction only be called once in one app");
        mCapabilityServices.put(appName, new CapabilityService(context));
    }

    public static void deinit(String AppName) {
        MLog.d("RCSCapability", "delete " + AppName + "'s CapabilityService");
        mCapabilityServices.remove(AppName);
    }

    public static CapabilityService getInstance(String appName) {
        return (CapabilityService) mCapabilityServices.get(appName);
    }

    public void registerBindStatusListen(RCSServiceListener observer) {
        if (this.mServiceBindStatusListenerList != null && !this.mServiceBindStatusListenerList.contains(observer)) {
            this.mServiceBindStatusListenerList.add(observer);
        }
    }

    public void unRegisterBindStatusListen(RCSServiceListener observer) {
        if (this.mServiceBindStatusListenerList != null && this.mServiceBindStatusListenerList.contains(observer)) {
            this.mServiceBindStatusListenerList.remove(observer);
        }
    }

    private boolean needStartRcsService() {
        if (this.mContext != null) {
            return this.mContext.getSharedPreferences("rcsconfig", 0).getBoolean("need_start_rcsservice", true);
        }
        return false;
    }

    private void bindservice() {
        Intent bindAction = new Intent();
        bindAction.setPackage("com.huawei.rcsserviceapplication");
        bindAction.setClassName("com.huawei.rcsserviceapplication", "com.huawei.rcs.service.RcsService");
        bindAction.setAction("com.huawei.msgplus.IfMsgplus");
        bindAction.setType("vnd.android.cursor.item/rcs");
        if (needStartRcsService()) {
            MLog.d("RCSCapability", "first strat rcs service");
            this.mContext.startService(bindAction);
        }
        this.mContext.bindService(bindAction, this.mrcsServiceConnection, 1);
    }

    public void end() {
        if (mRcsService != null) {
            try {
                for (Entry<Integer, IfMsgplusCb> entry : this.mRcseCallbackList.entrySet()) {
                    mRcsService.unRegisterCallback(((Integer) entry.getKey()).intValue(), (IfMsgplusCb) entry.getValue());
                    MLog.d("RCSCapability", "unRegisterCallback mRcseCallback");
                }
                this.mRcseCallbackList.clear();
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "unRegisterCallback mRcseCallback error is " + e.toString());
            }
            if (this.mIsBindService) {
                this.mContext.unbindService(this.mrcsServiceConnection);
                MLog.d("RCSCapability", "unbindservice");
                this.mrcsServiceConnection = null;
                setEmtpyRcsService();
            }
        }
    }

    private synchronized void setEmtpyRcsService() {
        setRcsServiceStatus(null);
        this.mIsBindService = false;
    }

    private static void setRcsServiceStatus(IfMsgplus mRcs) {
        mRcsService = mRcs;
    }

    public boolean isSupportFT(String msisdn) {
        boolean result = false;
        if (msisdn.isEmpty() || mRcsService == null) {
            return false;
        }
        try {
            Capabilities ca = mRcsService.getContactCapabilities(msisdn.replaceAll("-", "").replaceAll(HwCustPreloadContacts.EMPTY_STRING, ""));
            if (ca != null) {
                result = ca.isFileTransferSupported();
            }
        } catch (RemoteException e) {
            MLog.d("RCSCapability", "remotexception in inSupportFT, " + e.toString());
        }
        return result;
    }

    public Capabilities getCapabilities(String msisdn) {
        Capabilities ca = null;
        if (msisdn.isEmpty() || mRcsService == null) {
            MLog.d("RCSCapability", "getCapabilities, return ca");
            return ca;
        }
        try {
            ca = mRcsService.getContactCapabilities(msisdn.replaceAll("-", "").replaceAll(HwCustPreloadContacts.EMPTY_STRING, ""));
        } catch (RemoteException e) {
            MLog.d("RCSCapability", "remotexception in getCapabilities, " + e.toString());
        }
        return ca;
    }

    public void setRcsCallBack(Integer eventId, IfMsgplusCb rcsCallback) {
        if (mRcsService == null) {
            this.mRcseCallbackList.put(eventId, rcsCallback);
            return;
        }
        try {
            mRcsService.registerCallback(eventId.intValue(), rcsCallback);
            this.mRcseCallbackList.put(eventId, rcsCallback);
            MLog.d("RCSCapability", "registerCallback mRcseCallback");
        } catch (RemoteException e) {
            MLog.d("RCSCapability", e.toString());
        }
    }

    public void removeRcsCallBack(Integer eventId, IfMsgplusCb rcsCallback) {
        if (mRcsService == null) {
            this.mRcseCallbackList.remove(eventId);
            return;
        }
        try {
            mRcsService.unRegisterCallback(eventId.intValue(), rcsCallback);
            this.mRcseCallbackList.remove(eventId);
            MLog.d("RCSCapability", "removeRcsCallBack mRcsCallback");
        } catch (RemoteException e) {
            MLog.d("RCSCapability", e.toString());
        }
    }

    public boolean sendRequestContactCapabilities(String msisdn) {
        boolean result = false;
        if (msisdn.isEmpty() || mRcsService == null) {
            return false;
        }
        try {
            if (mRcsService.requestContactCapabilities(msisdn.replaceAll("-", "").replaceAll(HwCustPreloadContacts.EMPTY_STRING, "")) == 0) {
                result = true;
            }
        } catch (RemoteException e) {
            result = false;
        }
        return result;
    }

    public boolean isRCSContact(String number) {
        boolean flag = false;
        if (mRcsService != null) {
            try {
                flag = mRcsService.isRcsUeser(number);
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "remotexception in isRCSContact, " + e.toString());
            }
        }
        return flag;
    }

    public boolean getLoginState() {
        boolean bState = false;
        if (mRcsService != null) {
            try {
                bState = mRcsService.getLoginState();
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "remotexception in getLoginState, " + e.toString());
            }
        }
        return bState;
    }

    public int getMatchNum() {
        int iMatchNum = 0;
        if (mRcsService != null) {
            try {
                iMatchNum = mRcsService.getNumMatch();
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "remotexception in getMatchNum, " + e.toString());
            }
        }
        return iMatchNum;
    }

    public boolean compareUri(String number1, String number2) {
        boolean bCompResult = false;
        if (number1 == null || number2 == null) {
            MLog.e("RCSCapability", "enter function [compareUri], input param error");
            return bCompResult;
        }
        if (mRcsService != null) {
            try {
                bCompResult = mRcsService.compareUri(number1, number2);
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "remotexception in compareUri, " + e.toString());
            }
        }
        return bCompResult;
    }

    public void checkRcsServiceBind() {
        if (mRcsService == null && this.mIsBindService && this.mContext != null) {
            this.mContext.unbindService(this.mrcsServiceConnection);
            MLog.d("RCSCapability", "enter [checkRcsServiceBind] called unbindService() and bindservice()");
            bindservice();
        }
    }

    public String getCurrentLoginUserNumber() {
        String result = "";
        if (mRcsService != null) {
            try {
                result = mRcsService.getCurrentLoginUserNumber();
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "remotexception in getCurrentLoginUserNumber, " + e.toString());
            }
        }
        return result;
    }

    public int getMaxGroupMemberSize() {
        int size = 100;
        if (mRcsService != null) {
            try {
                size = mRcsService.getMaxGroupMemberSize();
            } catch (RemoteException e) {
                MLog.d("RCSCapability", "remote exception in getMaxGroupMemberSize, " + e.toString());
            }
        }
        return size;
    }
}
