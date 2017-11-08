package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class RemoveBlackListManager extends AbsListManager {
    protected static final int MAX_LOAD_NUM = 100;
    private static final String TAG = RemoveBlackListManager.class.getSimpleName();

    public long confirmDelBlackList(Context context, ArrayList<ContactInfo> phoneList) {
        setData(phoneList, null);
        this.mTaskID = System.currentTimeMillis();
        if (this.mCallBack != null) {
            this.mCallBack.onAfterCheckListExist(1, false, null);
        }
        return this.mTaskID;
    }

    public void handleList(final Context context, long taskId, Object args) {
        if (taskId != this.mTaskID) {
            HwLog.e(TAG, "is not the same task");
            return;
        }
        final boolean isRestoreMSG = Boolean.parseBoolean(args.toString());
        new Thread("HarassIntercept_RmBlackList") {
            public void run() {
                RemoveBlackListManager.this.mIsRunning = true;
                int result = RemoveBlackListManager.this.removeList(context, isRestoreMSG);
                RemoveBlackListManager.this.mIsRunning = false;
                if (RemoveBlackListManager.this.mCallBack != null) {
                    RemoveBlackListManager.this.mCallBack.onCompleteHandleList(result);
                }
            }
        }.start();
    }

    private int removeList(Context context, boolean isRestore) {
        List<ContactInfo> transferData = new ArrayList();
        int i = 0;
        for (BlacklistInfo info : this.mPhoneInList) {
            if (this.mIsRunning) {
                if (info instanceof BlacklistInfo) {
                    BlacklistInfo item = info;
                    if (DBAdapter.deleteBlacklist(context, item) <= 0) {
                        HwLog.i(TAG, "removeList fail");
                    } else {
                        if (isRestore && item.getMsgCount() > 0) {
                            HwLog.i(TAG, "try to resotre message, count:" + item.getMsgCount());
                            for (MessageInfo message : DBAdapter.getInterceptedMsgs(context, item.getPhone(), item.getType())) {
                                if (DBAdapter.addMsgToSystemInbox(context, message)) {
                                    DBAdapter.deleteInterceptedMsg(context, message);
                                } else {
                                    HwLog.i(TAG, "removeList addMsgToSystemInbox fail");
                                }
                            }
                        }
                        transferData.add(item);
                        if (i > 0 && i % 100 == 0 && this.mCallBack != null) {
                            this.mCallBack.onProcessHandleList(transferData);
                            transferData = new ArrayList();
                        }
                    }
                }
                i++;
            } else {
                HwLog.i(TAG, "removeList cancel");
                return 2;
            }
        }
        if (transferData.size() > 0 && this.mCallBack != null) {
            this.mCallBack.onProcessHandleList(transferData);
        }
        return 1;
    }
}
