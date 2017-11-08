package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class AddWhiteListManager extends AbsListManager implements ExistInListCallBack {
    protected static final int MAX_LOAD_NUM = 20;
    private static final String TAG = AddWhiteListManager.class.getSimpleName();
    private boolean mIsRestoreMSG = false;

    public boolean isExistInList(Context ctx, String phone) {
        return DBAdapter.isBlacklisted(ctx, phone) >= 0;
    }

    public long checkListExist(Context context, ArrayList<ContactInfo> phoneList, ExistInListCallBack callback) {
        return super.checkListExist(context, phoneList, this);
    }

    public void handleList(final Context ctx, long taskId, Object args) {
        if (this.mTaskID != taskId) {
            HwLog.e(TAG, "is not the same task");
            return;
        }
        final boolean isRestoreMSG = Boolean.parseBoolean(args.toString());
        this.mIsRestoreMSG = isRestoreMSG;
        new Thread("HarassIntercept_AddWhiteList") {
            public void run() {
                AddWhiteListManager.this.mIsRunning = true;
                int result = AddWhiteListManager.this.addList(ctx, isRestoreMSG);
                AddWhiteListManager.this.mIsRunning = false;
                if (AddWhiteListManager.this.mCallBack != null) {
                    AddWhiteListManager.this.mCallBack.onCompleteHandleList(result);
                }
            }
        }.start();
    }

    public boolean isWhiteList(Context ctx, String phone) {
        return DBAdapter.isWhitelisted(ctx, phone);
    }

    public boolean isRestoreMSG() {
        return this.mIsRestoreMSG;
    }

    private int addList(Context ctx, boolean isRestoreMSG) {
        int result = addWhiteListFromOthers(ctx);
        if (1 != result) {
            return result;
        }
        return addWhiteListFromBlackList(ctx, isRestoreMSG);
    }

    private int addWhiteListFromOthers(Context context) {
        List<ContactInfo> transferData = new ArrayList();
        int i = 0;
        for (ContactInfo item : this.mPhoneNotInList) {
            if (this.mIsRunning) {
                String phone = item.getPhone();
                int retValue = DBAdapter.addWhitelist(context, phone, item.getName());
                if (-1 == retValue) {
                    HwLog.i(TAG, "addWhiteListFromOthers addWhitelist fail");
                    return -1;
                }
                if (-2 != retValue) {
                    DBAdapter.deleteBlacklist(context, phone);
                    transferData.add(item);
                    if (i > 0 && i % 20 == 0 && this.mCallBack != null) {
                        this.mCallBack.onProcessHandleList(transferData);
                        transferData = new ArrayList();
                    }
                }
                i++;
            } else {
                HwLog.i(TAG, "addWhiteListFromOthers cancel");
                return 2;
            }
        }
        if (transferData.size() > 0 && this.mCallBack != null) {
            this.mCallBack.onProcessHandleList(transferData);
        }
        return 1;
    }

    private int addWhiteListFromBlackList(Context context, boolean isRestore) {
        List<ContactInfo> transferData = new ArrayList();
        int i = 0;
        for (ContactInfo item : this.mPhoneInList) {
            if (this.mIsRunning) {
                String phone = item.getPhone();
                if (-1 == DBAdapter.addWhitelist(context, phone, item.getName())) {
                    HwLog.i(TAG, "addWhiteListFromBlackList addWhitelist fail");
                    return -1;
                } else if (DBAdapter.deleteBlacklist(context, phone) <= 0) {
                    HwLog.i(TAG, "addWhiteListFromBlackList deleteBlacklist fail");
                    return -1;
                } else {
                    if (isRestore) {
                        for (MessageInfo message : DBAdapter.getInterceptedMsgsByFuzzyPhone(context, phone)) {
                            if (DBAdapter.addMsgToSystemInbox(context, message)) {
                                DBAdapter.deleteInterceptedMsg(context, message);
                            } else {
                                HwLog.i(TAG, "addWhiteListFromBlackList addMsgToSystemInbox fail");
                                return -1;
                            }
                        }
                    }
                    transferData.add(item);
                    if (i > 0 && i % 20 == 0 && this.mCallBack != null) {
                        this.mCallBack.onProcessHandleList(transferData);
                        transferData = new ArrayList();
                    }
                    i++;
                }
            } else {
                HwLog.i(TAG, "addWhiteListFromBlackList cancel");
                return 2;
            }
        }
        if (transferData.size() > 0 && this.mCallBack != null) {
            this.mCallBack.onProcessHandleList(transferData);
        }
        return 1;
    }
}
