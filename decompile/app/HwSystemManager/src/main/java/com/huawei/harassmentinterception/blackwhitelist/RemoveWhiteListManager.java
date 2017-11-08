package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class RemoveWhiteListManager extends AbsListManager implements ExistInListCallBack {
    protected static final int MAX_LOAD_NUM = 100;
    private static final String TAG = RemoveWhiteListManager.class.getSimpleName();

    public boolean isExistInList(Context ctx, String phone) {
        return DBAdapter.isContact(ctx, phone);
    }

    public long checkListExist(Context context, ArrayList<ContactInfo> phoneList, ExistInListCallBack callback) {
        return super.checkListExist(context, phoneList, this);
    }

    public long confirmDelWhiteList(Context context, ArrayList<ContactInfo> phoneList) {
        setData(phoneList, new ArrayList());
        this.mTaskID = System.currentTimeMillis();
        if (this.mCallBack != null) {
            this.mCallBack.onAfterCheckListExist(1, false, null);
        }
        return this.mTaskID;
    }

    public void handleList(final Context context, long taskId, Object obj) {
        if (this.mTaskID != taskId) {
            HwLog.e(TAG, "is not the same task");
        } else {
            new Thread("HarassIntercept_RmWhiteList") {
                public void run() {
                    RemoveWhiteListManager.this.mIsRunning = true;
                    int result = RemoveWhiteListManager.this.removeList(context);
                    RemoveWhiteListManager.this.mIsRunning = false;
                    if (RemoveWhiteListManager.this.mCallBack != null) {
                        RemoveWhiteListManager.this.mCallBack.onCompleteHandleList(result);
                    }
                }
            }.start();
        }
    }

    private int removeList(Context context) {
        List<ContactInfo> phoneList = this.mPhoneInList;
        phoneList.addAll(this.mPhoneNotInList);
        List<ContactInfo> transferData = new ArrayList();
        int i = 0;
        for (ContactInfo item : phoneList) {
            if (this.mIsRunning) {
                if (DBAdapter.deleteWhitelist(context, item.getPhone()) <= 0) {
                    HwLog.i(TAG, "removeWhiteList fail");
                    return -1;
                }
                transferData.add(item);
                if (i > 0 && i % 100 == 0 && this.mCallBack != null) {
                    this.mCallBack.onProcessHandleList(transferData);
                    transferData = new ArrayList();
                }
                i++;
            } else {
                HwLog.i(TAG, "removeWhiteList cancel");
                return 2;
            }
        }
        if (transferData.size() > 0 && this.mCallBack != null) {
            this.mCallBack.onProcessHandleList(transferData);
        }
        return 1;
    }
}
