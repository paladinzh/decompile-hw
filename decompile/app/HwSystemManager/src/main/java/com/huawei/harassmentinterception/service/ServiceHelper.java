package com.huawei.harassmentinterception.service;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.CallInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.strategy.implement.AbsStrategy;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class ServiceHelper {
    private static final String BLOCK_KEY_AIDL_CONTACTNAME = "BLOCK_CONTACTNAME";
    private static final String BLOCK_KEY_AIDL_PHONENUMBER = "BLOCK_PHONENUMBER";
    private static final String CHECK_KEY_AIDL = "CHECK_PHONENUMBER";
    private static final String TAG = "ServiceHelper";

    private static void interceptCall(Context context, String phone, int subId, BlockReason blockReason) {
        int reason = 0;
        HwLog.i(TAG, "intercept a call");
        CallInfo call = new CallInfo(phone, DBAdapter.getNameFromBlacklist(context, phone), System.currentTimeMillis());
        call.setSubId(subId);
        if (blockReason == null) {
            HwLog.e(TAG, "interceptCall, can not find block reason!!");
        }
        call.setBlockReason(blockReason);
        DBAdapter.addInterceptedCalls(context, call);
        if (DBAdapter.getUnreadCallCount(context) > 0) {
            if (blockReason != null) {
                reason = blockReason.getReason();
            }
            CommonHelper.sendNotificationForAll(context, reason);
        }
        stateBlockReason(phone, subId, blockReason);
    }

    private static void stateBlockReason(String phone, int subId, BlockReason blockReason) {
        if (blockReason != null) {
            List<String> params = Lists.newArrayList();
            params.add(HsmStatConst.PARAM_SUB);
            params.add(String.valueOf(subId));
            params.add(HsmStatConst.PARAM_VAL);
            params.add(String.valueOf(blockReason.getReason()));
            int blockType = blockReason.getType();
            if (blockType == 3) {
                params.add(HsmStatConst.PARAM_FLAG);
                params.add(String.valueOf(blockType));
            } else if (blockType == 2) {
                params.add(HsmStatConst.PARAM_FLAG);
                params.add(String.valueOf(blockType));
                params.add(HsmStatConst.PARAM_COUNT);
                params.add(String.valueOf(blockReason.getMarkCount()));
            }
            String[] paramArray = new String[params.size()];
            params.toArray(paramArray);
            HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_CALL, paramArray);
        }
    }

    public static int setPhoneNumberBlockList(Context context, Bundle blocknumberlist, int type, int source) {
        if (blocknumberlist.containsKey(BLOCK_KEY_AIDL_PHONENUMBER)) {
            List<String> numberList = blocknumberlist.getStringArrayList(BLOCK_KEY_AIDL_PHONENUMBER);
            List<String> nameList = blocknumberlist.getStringArrayList(BLOCK_KEY_AIDL_CONTACTNAME);
            if (Utility.isNullOrEmptyList(numberList)) {
                HwLog.e(TAG, "setPhoneNumberBlockList: Invalid number list");
                return 0;
            }
            int insertCount;
            if (nameList != null) {
                try {
                    if (nameList.size() >= numberList.size()) {
                        List<ContactInfo> contactBlackList = new ArrayList();
                        for (int i = 0; i < numberList.size(); i++) {
                            contactBlackList.add(new ContactInfo((String) numberList.get(i), (String) nameList.get(i)));
                        }
                        insertCount = DBAdapter.addContactsToBlacklist(context, contactBlackList);
                        return insertCount;
                    }
                } catch (Exception e) {
                    insertCount = 0;
                    HwLog.e(TAG, "setPhoneNumberBlockList: exception", e);
                }
            }
            insertCount = DBAdapter.addPhonesToBlacklist(context, numberList);
            return insertCount;
        }
        HwLog.w(TAG, "setPhoneNumberBlockList: No phone number info");
        return 0;
    }

    public static int addPhoneNumberBlockItem(Context context, Bundle blocknumber, int type, int source) throws RemoteException {
        if (blocknumber.containsKey(BLOCK_KEY_AIDL_PHONENUMBER)) {
            String phone = blocknumber.getString(BLOCK_KEY_AIDL_PHONENUMBER);
            String name = blocknumber.getString(BLOCK_KEY_AIDL_CONTACTNAME);
            if (TextUtils.isEmpty(phone)) {
                HwLog.e(TAG, "addPhoneNumberBlockItem: Invalid phone number");
                return -1;
            }
            if (name == null) {
                name = "";
            }
            int nId = -1;
            int result = 1;
            try {
                if (DBAdapter.isWhitelisted(context, phone)) {
                    result = DBAdapter.deleteWhitelist(context, phone);
                }
                if (result <= 0) {
                    HwLog.e(TAG, "addPhoneNumberBlockItem: delete white list failure");
                    return -1;
                }
                nId = DBAdapter.addBlacklist(context, phone, name, 3);
                return nId;
            } catch (Exception e) {
                HwLog.e(TAG, "addPhoneNumberBlockItem: exception.", e);
            }
        } else {
            HwLog.w(TAG, "addPhoneNumberBlockItem: Invalid param, no phone number info , type = " + type);
            return -1;
        }
    }

    public static int removePhoneNumberBlockItem(Context context, Bundle blocknumber, int type, int source) {
        if (blocknumber.containsKey(BLOCK_KEY_AIDL_PHONENUMBER)) {
            String phone = blocknumber.getString(BLOCK_KEY_AIDL_PHONENUMBER);
            if (TextUtils.isEmpty(phone)) {
                HwLog.e(TAG, "removePhoneNumberBlockItem: Invalid phone number");
                return 0;
            }
            int deleteCount = 0;
            try {
                deleteCount = DBAdapter.deleteBlacklist(context, phone);
            } catch (Exception e) {
                HwLog.e(TAG, "removePhoneNumberBlockItem exception ", e);
            }
            return deleteCount;
        }
        HwLog.w(TAG, "removePhoneNumberBlockItem: No phone number info");
        return 0;
    }

    public static String[] queryPhoneNumberBlockItem(Context context) throws RemoteException {
        try {
            List<String> blackList = DBAdapter.getBlacklistedPhones(context);
            if (Utility.isNullOrEmptyList(blackList)) {
                HwLog.d(TAG, "queryPhoneNumberBlockItem: Blacklist is empty");
                return new String[0];
            }
            int length = blackList.size();
            String[] phone = new String[length];
            for (int i = 0; i < length; i++) {
                phone[i] = (String) blackList.get(i);
            }
            return phone;
        } catch (Exception e) {
            HwLog.e(TAG, "queryPhoneNumberBlockItem exception", e);
            return new String[0];
        }
    }

    public static int checkPhoneNumberFromBlockItem(Context context, Bundle checknumber, int type) throws RemoteException {
        return DBAdapter.checkBlackAndWhiteListOption(context, checknumber.getString(CHECK_KEY_AIDL), type);
    }

    public static boolean checkPhoneNumberFromWhiteList(Context context, Bundle checknumber, int type) throws RemoteException {
        String phone = checknumber.getString(CHECK_KEY_AIDL);
        if (!TextUtils.isEmpty(phone)) {
            return DBAdapter.isWhitelisted(context, phone);
        }
        HwLog.e(TAG, "checkPhoneNumberFromWhiteList: Invalid phone number");
        return false;
    }

    public static int removePhoneNumberFromWhitelist(Context context, Bundle blocknumber, int type, int source) {
        if (blocknumber.containsKey(BLOCK_KEY_AIDL_PHONENUMBER)) {
            String phone = blocknumber.getString(BLOCK_KEY_AIDL_PHONENUMBER);
            if (TextUtils.isEmpty(phone)) {
                HwLog.e(TAG, "removePhoneNumberFromWhitelist: Invalid phone number");
                return 0;
            }
            int deleteCount = 0;
            try {
                deleteCount = DBAdapter.deleteWhitelist(context, phone);
            } catch (Exception e) {
                HwLog.e(TAG, "removePhoneNumberFromWhitelist exception ", e);
            }
            return deleteCount;
        }
        HwLog.w(TAG, "removePhoneNumberFromWhitelist: No phone number info");
        return 0;
    }

    public static void sendCallBlockRecords(Context context, Bundle callBlockRecords, BlockReason reason) throws RemoteException {
        if (callBlockRecords.containsKey(BLOCK_KEY_AIDL_PHONENUMBER)) {
            String phone = callBlockRecords.getString(BLOCK_KEY_AIDL_PHONENUMBER);
            int presentation = callBlockRecords.getInt(ConstValues.HANDLE_KEY_AIDL_NUMBER_PRESENTATION, 1);
            if (CommonHelper.isInvalidPhoneNumber(phone, presentation)) {
                HwLog.i(TAG, "sendCallBlockRecords: Block a call from unknow phone number");
                phone = "";
            }
            if (reason == null) {
                reason = AbsStrategy.getLasteBlockCall(phone);
            }
            int subId = callBlockRecords.getInt(ConstValues.HANDLE_KEY_AIDL_SUB_ID, -1);
            HwLog.i(TAG, "sendCallBlockRecords  presentation = " + presentation + ", subId:" + subId);
            interceptCall(context, phone, convertCallSubId(subId), reason);
            return;
        }
        HwLog.w(TAG, "sendCallBlockRecords: No phone number info");
    }

    public static int convertCallSubId(int subId) {
        if (subId != 0 && subId == 1) {
            return 2;
        }
        return 1;
    }
}
