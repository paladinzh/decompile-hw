package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class AddBlackListManager extends AbsListManager implements ExistInListCallBack {
    private static final String TAG = AddBlackListManager.class.getSimpleName();

    public boolean isExistInList(Context ctx, String phone) {
        return DBAdapter.isWhitelisted(ctx, phone);
    }

    public long checkListExist(Context context, ArrayList<ContactInfo> phoneList, ExistInListCallBack callback) {
        return super.checkListExist(context, phoneList, this);
    }

    public void handleList(final Context context, long taskId, Object args) {
        if (this.mTaskID != taskId) {
            HwLog.e(TAG, "is not the same task");
        } else {
            new Thread("HarassIntercept_AddBlackList") {
                public void run() {
                    AddBlackListManager.this.mIsRunning = true;
                    int result = AddBlackListManager.this.addList(context);
                    AddBlackListManager.this.mIsRunning = false;
                    if (AddBlackListManager.this.mCallBack != null) {
                        AddBlackListManager.this.mCallBack.onCompleteHandleList(result);
                    }
                }
            }.start();
        }
    }

    private int addList(Context context) {
        int result = addBlackListFromOthers(context);
        if (1 != result) {
            return result;
        }
        return addBlackListFromWhiteList(context);
    }

    private int addBlackListFromOthers(Context context) {
        for (ContactInfo item : this.mPhoneNotInList) {
            if (this.mIsRunning) {
                if (DBAdapter.addBlacklist(context, item.getPhone(), item.getName(), 3) <= 0) {
                    HwLog.i(TAG, "addBlackListFromOthers addBlacklist function fail");
                    return -1;
                }
            }
            HwLog.i(TAG, "addBlackListFromOthers has been cancel");
            return 2;
        }
        return 1;
    }

    private int addBlackListFromWhiteList(Context context) {
        for (ContactInfo item : this.mPhoneInList) {
            if (this.mIsRunning) {
                String phone = item.getPhone();
                if (DBAdapter.addBlacklist(context, phone, item.getName(), 3) <= 0) {
                    HwLog.i(TAG, "addBlackListFromWhiteList addBlacklist function fail");
                    return -1;
                } else if (DBAdapter.deleteWhitelist(context, phone) <= 0) {
                    HwLog.i(TAG, "addBlackListFromWhiteList deleteWhitelist function fail");
                    return -1;
                }
            }
            HwLog.i(TAG, "addBlackListFromWhiteList cancel");
            return 2;
        }
        return 1;
    }
}
