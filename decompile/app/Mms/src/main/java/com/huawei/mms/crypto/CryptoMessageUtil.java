package com.huawei.mms.crypto;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.ui.CryptoComposeMessage.ICryptoComposeHolder;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.crypto.account.AccountManager;
import com.huawei.mms.util.CursorUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

public class CryptoMessageUtil {
    private static HashMap<String, Integer> mAccountCache = new HashMap();
    private static String mHuaweiAccountSub1 = "";
    private static String mHuaweiAccountSub2 = "";
    private static final HashMap<String, Boolean> mImsiStateMap = new HashMap();
    public static final boolean mIsExistsApk = isExistsServiceApk(MmsApp.getApplication().getApplicationContext());

    public static boolean isCryptoSmsEnabled() {
        if (!MmsConfig.isEnableCryptoSms()) {
            return false;
        }
        if ((!OsUtil.isAtLeastL() || !OsUtil.isSecondaryUser()) && mIsExistsApk) {
            return true;
        }
        return false;
    }

    public static boolean isExistsServiceApk(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.huawei.cryptosms.service", 128);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getHuaweiAccount(int subID) {
        String account = CryptoMessageServiceProxy.getCardCloudAccount(subID);
        if (!TextUtils.isEmpty(account)) {
            return account;
        }
        MLog.d("CryptoMessageUtil", "getHuaweiAccount: account from service is null");
        switch (subID) {
            case 0:
                return mHuaweiAccountSub1;
            case 1:
                return mHuaweiAccountSub2;
            default:
                return "";
        }
    }

    public static String getAccountOrImsi(String msgBody, int encryptedType) {
        String result = null;
        if (!CryptoMessageServiceProxy.isServiceReady()) {
            return null;
        }
        if (1 == encryptedType) {
            result = CryptoMessageServiceProxy.getAccountFromLEMsg(msgBody, true);
        } else if (3 == encryptedType) {
            result = CryptoMessageServiceProxy.getImsiFromLSNEMsg(msgBody, true);
        }
        return result;
    }

    private static boolean verifyAccount(String account, long msgId) {
        if (!TextUtils.isEmpty(account) && mAccountCache.containsKey(account)) {
            int state = ((Integer) mAccountCache.get(account)).intValue();
            if (1 == state) {
                return true;
            }
            return Long.MIN_VALUE != msgId && state == 0;
        }
    }

    public static void clearImsiState() {
        mImsiStateMap.clear();
        String imsi;
        if (MessageUtils.isMultiSimEnabled()) {
            imsi = formatImsi(MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(0));
            if (!"".equals(imsi)) {
                mImsiStateMap.put(imsi, Boolean.valueOf(AccountManager.getInstance().isCardStateActivated(0)));
            }
            imsi = formatImsi(MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(1));
            if (!"".equals(imsi)) {
                mImsiStateMap.put(imsi, Boolean.valueOf(AccountManager.getInstance().isCardStateActivated(1)));
                return;
            }
            return;
        }
        imsi = formatImsi(MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(0));
        if (!"".equals(imsi)) {
            mImsiStateMap.put(imsi, Boolean.valueOf(AccountManager.getInstance().isCardStateActivated(0)));
        }
    }

    private static boolean verifyImsi(String imsi) {
        int subId = getSubIDByImsi(imsi);
        if (-1 == subId) {
            mImsiStateMap.clear();
            return false;
        } else if (mImsiStateMap.containsKey(imsi)) {
            return ((Boolean) mImsiStateMap.get(imsi)).booleanValue();
        } else {
            boolean ret = AccountManager.getInstance().isCardStateActivated(subId);
            mImsiStateMap.put(imsi, Boolean.valueOf(ret));
            return ret;
        }
    }

    private static boolean verifyMsgId(MessageItem item, long msgId) {
        if (Long.MAX_VALUE == msgId) {
            return false;
        }
        if (Long.MIN_VALUE == msgId) {
            return true;
        }
        long messageId = item.getMessageId();
        if (!Sms.isOutgoingFolder(item.getBoxId()) || messageId < msgId) {
            return false;
        }
        int subId = item.getCryptoMessageItem().getMessageSubId(item);
        if (-1 != subId) {
            String account = getHuaweiAccount(subId);
            if (!mAccountCache.containsKey(account)) {
                addAccountState(account, 0);
            } else if (1 != ((Integer) mAccountCache.get(account)).intValue()) {
                addAccountState(account, 0);
            }
        }
        return true;
    }

    public static boolean couldDecryptSmsForLocal(MessageItem item, long msgId) {
        if (item == null) {
            MLog.d("CryptoMessageUtil", "couldDecryptSmsForLocal: item is null");
            return false;
        } else if (!verifyMsgId(item, msgId)) {
            return false;
        } else {
            String encryptedMsg = item.getMessageSummary();
            if (TextUtils.isEmpty(encryptedMsg)) {
                return false;
            }
            return verifyAccount(getAccountOrImsi(encryptedMsg, 1), msgId);
        }
    }

    public static boolean couldDecryptSmsForLsne(MessageItem item, long msgId) {
        if (item == null) {
            MLog.d("CryptoMessageUtil", "couldDecryptSmsForLsne: item is null");
            return false;
        }
        String eText = item.getMessageSummary();
        if (TextUtils.isEmpty(eText)) {
            return false;
        }
        return verifyImsi(getAccountOrImsi(eText, 3));
    }

    public static boolean couldDecryptSmsForNetwork(MessageItem item, long msgId) {
        if (item == null) {
            MLog.d("CryptoMessageUtil", "couldDecryptSmsForNetwork: item is null");
            return false;
        } else if (!TextUtils.isEmpty(item.getMessageSummary())) {
            return true;
        } else {
            MLog.d("CryptoMessageUtil", "couldDecryptSmsForNetwork: message bosy is null");
            return false;
        }
    }

    public static void addAccountState(String account, int state) {
        mAccountCache.put(account, Integer.valueOf(state));
    }

    public static void clearAccountState() {
        mAccountCache.clear();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("ems_cma_switch_state", 0);
    }

    public static boolean isSmsEncryptionSwitchOn(Context context, ContactList recipientList) {
        if (recipientList == null) {
            return false;
        }
        return getSwitchState(context, recipientList);
    }

    public static boolean isSmsEncryptionSwitchOn(ICryptoComposeHolder composeHolder) {
        return getSwitchState(composeHolder.getFragment().getContext(), composeHolder.getRecipients());
    }

    public static boolean isSmsEncryptionSwitchOn(Context context, String recipients) {
        return getSharedPreferences(context).getBoolean(recipients, false);
    }

    public static void setSmsEncryptionSwitchState(Context context, ContactList recipientList, boolean on) {
        updateSwitchState(context, recipientList, on);
    }

    public static void setSmsEncryptionSwitchState(Context context, String recipients, boolean on) {
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        if (on) {
            editor.putBoolean(recipients, true);
        } else if (prefs.contains(recipients)) {
            editor.remove(recipients);
        }
        editor.commit();
    }

    public static void setSmsEncryptionSwitchState(ICryptoComposeHolder composeHolder, boolean on) {
        updateSwitchState(composeHolder.getFragment().getContext(), composeHolder.getRecipients(), on);
    }

    public static void clearSmsEncryptionSwitchState(Context context) {
        MLog.d("CryptoMessageUtil", "clearSmsEncryptionSwitchState: clear all data in shared preference");
        Editor editor = context.getSharedPreferences("ems_cma_switch_state", 0).edit();
        editor.clear();
        editor.commit();
    }

    public static String getFormatedRecipients(ICryptoComposeHolder composeHolder) {
        return formatRecipients(composeHolder.getRecipients());
    }

    public static String formatRecipients(ContactList recipientList) {
        if (recipientList == null || recipientList.size() == 0) {
            return "esms_empty_number";
        }
        String recipients = recipientList.serialize();
        if (TextUtils.isEmpty(recipients)) {
            MLog.i("CryptoMessageUtil", "Error in formatRecipients, recipients is empty");
            return "esms_empty_number";
        }
        recipients = recipients.replaceAll("\\s*", "");
        if (recipients.contains(";")) {
            String[] sa = recipients.split(";");
            Arrays.sort(sa);
            StringBuilder sb = new StringBuilder();
            for (String s : sa) {
                sb.append(s);
                sb.append(";");
            }
            int length = sb.length();
            if (length <= 1) {
                MLog.i("CryptoMessageUtil", "Error in formatRecipients, recipientList.size()=" + recipientList.size() + ", sa.length=" + sa.length + ", sb.length=" + length);
                return "esms_empty_number";
            }
            sb.setLength(length - 1);
            recipients = sb.toString();
        }
        if ("esms_empty_number".equals(recipients)) {
            recipients = "esms_empty_number";
        } else {
            recipients = getHashCode(recipients);
        }
        return recipients;
    }

    public static void updateSwitchState(final Context context) {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                SharedPreferences prefs = context.getSharedPreferences("ems_cma_switch_state", 0);
                HashMap<String, Object> switchState = (HashMap) prefs.getAll();
                if (switchState.isEmpty()) {
                    MLog.d("CryptoMessageUtil", "updateSwitchState: esms switch state shared preference is empty");
                    return;
                }
                Cursor cursor = SqliteWrapper.query(context, Conversation.sAllThreadsUri, Conversation.getThreadProjection(), null, null, "date DESC");
                if (cursor == null) {
                    MLog.w("CryptoMessageUtil", "updateSwitchState: cursor is null");
                    return;
                }
                try {
                    if (cursor.getCount() < 1) {
                        MLog.d("CryptoMessageUtil", "updateSwitchState: all conversations had been deleted");
                        CryptoMessageUtil.clearSmsEncryptionSwitchState(context);
                        cursor.close();
                        return;
                    }
                    HashMap<String, Boolean> newSwitchState = new HashMap();
                    cursor = CursorUtils.getFastCursor(cursor);
                    while (cursor.moveToNext()) {
                        ContactList recipients = ContactList.getByIds(cursor.getString(3), false);
                        if (TextUtils.isEmpty(recipients.serialize())) {
                            MLog.d("CryptoMessageUtil", "updateSwitchState: recipients is null or empty");
                        } else {
                            String hashCode = CryptoMessageUtil.formatRecipients(recipients);
                            if (switchState.containsKey(hashCode)) {
                                Object object = switchState.get(hashCode);
                                if ((object instanceof Boolean) && ((Boolean) object).booleanValue()) {
                                    newSwitchState.put(hashCode, Boolean.valueOf(true));
                                }
                            }
                        }
                    }
                    prefs.edit().clear().commit();
                    if (!newSwitchState.isEmpty()) {
                        Editor editor = prefs.edit();
                        for (Entry<String, Boolean> entry : newSwitchState.entrySet()) {
                            editor.putBoolean((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
                        }
                        editor.commit();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    private static String getHashCode(String msg) {
        String tmp = "esms_empty_number";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] bs = msg.getBytes("UTF-8");
            digest.update(bs, 0, bs.length);
            return new String(digest.digest(), "UTF-8");
        } catch (Exception e) {
            MLog.e("CryptoMessageUtil", "getHashCode: error happened", (Throwable) e);
            return "esms_empty_number";
        }
    }

    private static boolean getSwitchState(Context context, ContactList recipientList) {
        return getSharedPreferences(context).getBoolean(formatRecipients(recipientList), false);
    }

    private static void updateSwitchState(Context context, ContactList recipientList, boolean on) {
        String recipients = formatRecipients(recipientList);
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        if (on) {
            editor.putBoolean(recipients, true);
        } else if (prefs.contains(recipients)) {
            editor.remove(recipients);
        }
        editor.commit();
    }

    public static void asyncClearSwitch() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... none) {
                CryptoMessageUtil.clearSmsEncryptionSwitchState(MmsApp.getApplication().getApplicationContext());
                return null;
            }
        }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
    }

    public static int decryptNetworkSmsFailureReason(MessageItem item) {
        if (item == null) {
            return -1;
        }
        String encryptedMsg = item.getMessageSummary();
        if (TextUtils.isEmpty(encryptedMsg)) {
            return -1;
        }
        int subId = getSubIDByImsi(CryptoMessageServiceProxy.getImsiFromLSNEMsg(encryptedMsg, true));
        if (subId == -1) {
            return 2;
        }
        if (1 != AccountManager.getInstance().getCardActivatedState(subId)) {
            return 1;
        }
        return 0;
    }

    public static int getSubIDByImsi(String imsi) {
        int subId = -1;
        if (imsi == null || 8 != imsi.length()) {
            return -1;
        }
        if (imsi.equals(formatImsi(MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(0)))) {
            subId = 0;
        }
        if (-1 == subId && imsi.equals(formatImsi(MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(1)))) {
            subId = 1;
        }
        return subId;
    }

    public static String getImsiBySubID(int subID) {
        if (subID < 0 || subID > 1) {
            return "";
        }
        return formatImsi(MmsApp.getDefaultMSimTelephonyManager().getSubscriberId(subID));
    }

    private static String formatImsi(String imsi) {
        if (imsi == null) {
            return "";
        }
        int len = imsi.length();
        if (len <= 8) {
            return "";
        }
        return imsi.substring(len - 8, len);
    }

    public static String getHwIDSystemAccountName(Context context) {
        Account[] accs = android.accounts.AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        if (accs == null || accs.length <= 0) {
            return null;
        }
        return accs[0].name;
    }

    public static void storeSystemHwIdInfo(Context context, CloudAccount sysAccount) {
        if (context == null) {
            MLog.e("CryptoMessageUtil", "storeSystemHwIdInfo, context is null");
        } else if (sysAccount == null) {
            MLog.e("CryptoMessageUtil", "storeSystemHwIdInfo, sysAccount is null");
        } else {
            String hashedAccountName = getHashCode(sysAccount.getAccountName());
            String sysAccountUserId = sysAccount.getUserId();
            Editor editor = context.getSharedPreferences("esms_id_info", 0).edit();
            editor.putString(hashedAccountName, sysAccountUserId);
            editor.commit();
        }
    }

    public static String getSystemHwIdUserId(Context context, String accountName) {
        if (context == null) {
            return null;
        }
        return context.getSharedPreferences("esms_id_info", 0).getString(getHashCode(accountName), null);
    }

    public static boolean isMsgEncrypted(String msg) {
        if (!isCryptoSmsEnabled()) {
            return false;
        }
        int eType = CryptoMessageServiceProxy.getEncryptedType(msg);
        boolean encrypted = (4 == eType || 3 == eType || 2 == eType) ? true : 1 == eType;
        MLog.d("CryptoMessageUtil", "isMsgEncrypted: encrypted=" + encrypted);
        return encrypted;
    }
}
