package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;
import com.huawei.harassmentinterception.callback.HandleListCallBack;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsListManager {
    public static final int INVALID_TASK = -1;
    public static final int RESULT_CANCEL = 2;
    public static final int RESULT_INNERROR = -1;
    public static final int RESULT_SUCCESS = 1;
    private static final String TAG = AbsListManager.class.getSimpleName();
    protected HandleListCallBack mCallBack = null;
    protected boolean mIsRunning = false;
    protected List<ContactInfo> mPhoneInList = new ArrayList();
    protected List<ContactInfo> mPhoneNotInList = new ArrayList();
    protected long mTaskID = -1;

    public abstract void handleList(Context context, long j, Object obj);

    public void registerCallBack(HandleListCallBack callback) {
        this.mCallBack = callback;
    }

    public void unregisterCallBack() {
        this.mCallBack = null;
    }

    public long checkListExist(Context ctx, ArrayList<ContactInfo> phoneList, ExistInListCallBack existCallback) {
        if (phoneList == null || phoneList.size() == 0) {
            HwLog.i(TAG, "Invalid phoneList, should include data");
            return -1;
        }
        this.mTaskID = System.currentTimeMillis();
        final ArrayList<ContactInfo> arrayList = phoneList;
        final ExistInListCallBack existInListCallBack = existCallback;
        final Context context = ctx;
        new Thread("HarassIntercept_checkListExist") {
            public void run() {
                List<ContactInfo> phoneInlist = new ArrayList();
                List<ContactInfo> phoneNotInlist = new ArrayList();
                AbsListManager.this.mIsRunning = true;
                for (ContactInfo item : arrayList) {
                    if (!AbsListManager.this.mIsRunning) {
                        HwLog.i(AbsListManager.TAG, "checkListExist has been cancel");
                        if (AbsListManager.this.mCallBack != null) {
                            AbsListManager.this.mCallBack.onAfterCheckListExist(2, false, arrayList);
                        }
                        return;
                    } else if (existInListCallBack.isExistInList(context, item.getPhone())) {
                        phoneInlist.add(item);
                    } else {
                        phoneNotInlist.add(item);
                    }
                }
                AbsListManager.this.setData(phoneInlist, phoneNotInlist);
                boolean isExist = phoneInlist.size() > 0;
                AbsListManager.this.mIsRunning = false;
                if (AbsListManager.this.mCallBack != null) {
                    HwLog.i(AbsListManager.TAG, "checkListExist finished, list exist" + isExist);
                    AbsListManager.this.mCallBack.onAfterCheckListExist(1, isExist, arrayList);
                }
            }
        }.start();
        return this.mTaskID;
    }

    public void stop() {
        this.mIsRunning = false;
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    protected synchronized void setData(List<ContactInfo> phoneInList, List<ContactInfo> phoneNotInList) {
        this.mPhoneInList = phoneInList;
        this.mPhoneNotInList = phoneNotInList;
    }
}
