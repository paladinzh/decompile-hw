package com.huawei.mms.crypto.account;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.CloudAccountManager;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.cspcommon.MLog;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.account.ui.PwdCheckActivity;

public class AccountManager {
    private static final Uri FINGER_BIND_QUERY_URI = Uri.parse("content://com.huawei.hwid.api.provider/query/0");
    private static final AccountManager sInstance = new AccountManager();
    private AccountCheckHandler mAccountCheckHandler;
    private CloudAccount mCurrentSysAccount;
    private int mNeedReactiveSubId;
    private String mNeedUnbindAccountName;
    private boolean mNeedUnbindPrompt;
    private int mNeedUnbindSubId;

    public boolean isAnyfingerRecorded() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0046 in list []
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
        r6 = this;
        r4 = 1;
        r5 = 0;
        r0 = 0;
        r3 = 1;
        r0 = com.huawei.securitymgr.AuthenticationManager.open(r3);	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        if (r0 != 0) goto L_0x0019;	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
    L_0x000a:
        r3 = "AccountManager";	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        r4 = "showFingerprintBindDialog, authManager is null";	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        com.huawei.cspcommon.MLog.i(r3, r4);	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        if (r0 == 0) goto L_0x0018;
    L_0x0015:
        r0.release();
    L_0x0018:
        return r5;
    L_0x0019:
        r2 = r0.getIds();	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        if (r2 == 0) goto L_0x0022;	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
    L_0x001f:
        r3 = r2.length;	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        if (r3 != 0) goto L_0x0031;	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
    L_0x0022:
        r3 = "AccountManager";	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        r4 = "showFingerprintBindDialog, fingerprintIds is empty";	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        com.huawei.cspcommon.MLog.i(r3, r4);	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        if (r0 == 0) goto L_0x0030;
    L_0x002d:
        r0.release();
    L_0x0030:
        return r5;
    L_0x0031:
        if (r0 == 0) goto L_0x0036;
    L_0x0033:
        r0.release();
    L_0x0036:
        return r4;
    L_0x0037:
        r1 = move-exception;
        r3 = "AccountManager";	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        r4 = "exception in isAnyfingerRecorded";	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        com.huawei.cspcommon.MLog.e(r3, r4, r1);	 Catch:{ Exception -> 0x0037, all -> 0x0047 }
        if (r0 == 0) goto L_0x0046;
    L_0x0043:
        r0.release();
    L_0x0046:
        return r5;
    L_0x0047:
        r3 = move-exception;
        if (r0 == 0) goto L_0x004d;
    L_0x004a:
        r0.release();
    L_0x004d:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.mms.crypto.account.AccountManager.isAnyfingerRecorded():boolean");
    }

    public boolean isNeedUnbindPrompt() {
        return this.mNeedUnbindPrompt;
    }

    public void setNeedUnbindPrompt(boolean needUnbindPrompt) {
        this.mNeedUnbindPrompt = needUnbindPrompt;
    }

    public int getNeedUnbindSubId() {
        return this.mNeedUnbindSubId;
    }

    public void setNeedUnbindSubId(int needUnbindSubId) {
        this.mNeedUnbindSubId = needUnbindSubId;
    }

    public String getNeedUnbindAccountName() {
        return this.mNeedUnbindAccountName;
    }

    public void setNeedUnbindAccountName(String needUnbindAccountName) {
        this.mNeedUnbindAccountName = needUnbindAccountName;
    }

    public boolean isNeedReactive(Context context) {
        return 1 == getNeedReactiveState(context);
    }

    private int getNeedReactiveState(Context context) {
        if (context != null) {
            return context.getSharedPreferences("sms_encrypt_prompt", 0).getInt("need_reactive_state", 0);
        }
        MLog.w("AccountManager", "context is null!");
        return 0;
    }

    private void setNeedReactiveState(Context context, int needReactiveState) {
        if (context == null) {
            MLog.w("AccountManager", "context is null!");
            return;
        }
        Editor editor = context.getSharedPreferences("sms_encrypt_prompt", 0).edit();
        editor.putInt("need_reactive_state", needReactiveState);
        editor.commit();
    }

    public boolean isNeedReactiveLater(Context context) {
        return 2 == getNeedReactiveState(context);
    }

    public void setNeedReactive(Context context) {
        int needReactiveState;
        if (this.mNeedReactiveSubId == 0 || this.mNeedReactiveSubId == 1) {
            int state = getCardActivatedState(this.mNeedReactiveSubId);
            if (state == 5) {
                MLog.d("AccountManager", "setNeedReactive subId " + this.mNeedReactiveSubId + " need to activate later");
                needReactiveState = 2;
            } else if (state == 1) {
                needReactiveState = 1;
            } else {
                MLog.d("AccountManager", "setNeedReactive subId " + this.mNeedReactiveSubId + " is not activate");
                needReactiveState = 0;
            }
        } else {
            int state1 = getCardActivatedState(0);
            int state2 = getCardActivatedState(1);
            if (state1 == 5 || state2 == 5) {
                MLog.d("AccountManager", "setNeedReactive subId " + this.mNeedReactiveSubId + " need to activate later");
                needReactiveState = 2;
            } else if (state1 == 1 || state2 == 1) {
                needReactiveState = 1;
            } else {
                MLog.d("AccountManager", "setNeedReactive subId " + this.mNeedReactiveSubId + " is not activate");
                needReactiveState = 0;
            }
        }
        setNeedReactiveState(context, needReactiveState);
    }

    public void setNeedReactiveSubId(int needReactiveSubId) {
        this.mNeedReactiveSubId = needReactiveSubId;
    }

    public void updateStateForNewKeyVersion(Context context) {
        if (1 == getNeedReactiveState(context)) {
            MLog.d("AccountManager", "updateStateForNewKeyVersion");
            if (this.mNeedReactiveSubId == 0 || this.mNeedReactiveSubId == 1) {
                closeAccountBind(this.mNeedReactiveSubId, null, null);
            } else {
                closeAccountBind(0, null, null);
                closeAccountBind(1, null, null);
            }
            setNeedReactiveState(context, 0);
        }
    }

    public boolean isShouldBindFingerPrompt(Context context) {
        if (context == null) {
            return false;
        }
        return context.getSharedPreferences("sms_encrypt_prompt", 0).getBoolean("should_bind_finger_prompt", false);
    }

    public void setShouldBindFingerPrompt(Context context, boolean shouldBindFingerPrompt) {
        if (context != null) {
            Editor editor = context.getSharedPreferences("sms_encrypt_prompt", 0).edit();
            editor.putBoolean("should_bind_finger_prompt", shouldBindFingerPrompt);
            editor.commit();
        }
    }

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        return sInstance;
    }

    public CloudAccount getCurrentSysAccount() {
        return this.mCurrentSysAccount;
    }

    public void setCurrentSysAccount(CloudAccount cloudAccount) {
        this.mCurrentSysAccount = cloudAccount;
    }

    public AccountCheckHandler getAccountCheckHandler() {
        return this.mAccountCheckHandler;
    }

    public void setAccountCheckHandler(AccountCheckHandler accountCheckHandler) {
        this.mAccountCheckHandler = accountCheckHandler;
    }

    public int getCardActivatedState(int subId) {
        return CryptoMessageServiceProxy.getCardActivatedState(subId);
    }

    public int getCardActivatedState() {
        return CryptoMessageServiceProxy.getCardActivatedState(0);
    }

    public String getCardCloudAccount(int subId) {
        return CryptoMessageServiceProxy.getCardCloudAccount(subId);
    }

    public String getCardCloudAccount() {
        return CryptoMessageServiceProxy.getCardCloudAccount(0);
    }

    public void loginOrCheckSystemAccount(final Context context, final CloudRequestHandler cloudRequestHandler, LoginHandler loginHandler) {
        if (context == null || cloudRequestHandler == null || loginHandler == null) {
            MLog.e("AccountManager", "loginOrCheckSystemAccount, invalid params");
            return;
        }
        if (CloudAccount.hasLoginAccount(context)) {
            obtainSystemAccountInfo(context, new Runnable() {
                public void run() {
                    if (AccountManager.this.mCurrentSysAccount == null) {
                        MLog.e("AccountManager", "loginOrCheckSystemAccount, currentSysAccount is null");
                        cloudRequestHandler.onError(new ErrorStatus(12, ""));
                        return;
                    }
                    AccountManager.this.checkSystemAccountPwd(context, cloudRequestHandler, AccountManager.this.mCurrentSysAccount.getUserId());
                }
            });
        } else {
            login(context, loginHandler);
        }
    }

    private void login(Context context, LoginHandler loginHandler) {
        Bundle bundle = new Bundle();
        bundle.putInt("loginChannel", 32000100);
        bundle.putInt("reqClientType", 32);
        CloudAccount.getAccountsByType(context, context.getPackageName(), bundle, loginHandler);
    }

    public int bindAccount(CloudAccount account) {
        return bindAccount(0, account);
    }

    public int bindAccount(int subId, CloudAccount account) {
        if (account == null) {
            MLog.e("AccountManager", "account is null in bindAccount");
            return -1;
        }
        String serviceToken = account.getServiceToken();
        return CryptoMessageServiceProxy.bindAccount(subId, account.getAccountName(), account.getUserId(), serviceToken);
    }

    public int unbindAccount(String userName, String pwd) {
        return unbindAccount(0, userName, pwd);
    }

    public int unbindAccount(int subId, String userName, String pwd) {
        if (userName != null && pwd != null) {
            return CryptoMessageServiceProxy.unbindAccount(subId, userName, pwd);
        }
        MLog.e("AccountManager", "invalid params in unbindAccount");
        return -1;
    }

    public int closeAccountBind(int subId, String userName, String pwd) {
        return CryptoMessageServiceProxy.closeAccountBind(subId, userName, pwd);
    }

    public int closeAccountBind(String userName, String pwd) {
        return CryptoMessageServiceProxy.closeAccountBind(0, userName, pwd);
    }

    public void obtainSystemAccountInfo(final Context context, final Runnable callback) {
        if (context == null) {
            MLog.e("AccountManager", "context is null in obtainSystemAccountInfo");
            return;
        }
        Context fContext = context;
        MLog.d("AccountManager", "start to obtainSystemAccountInfo");
        Bundle bundle = new Bundle();
        bundle.putBoolean("needAuth", false);
        bundle.putInt("loginChannel", 32000100);
        bundle.putInt("reqClientType", 32);
        CloudAccount.getAccountsByType(context, context.getPackageName(), bundle, new LoginHandler() {
            public void onLogin(CloudAccount[] mAccounts, int index) {
                MLog.d("AccountManager", "obtainSystemAccountInfo, onLogin");
                if (mAccounts == null || index == -1 || mAccounts.length <= index) {
                    MLog.e("AccountManager", "onLogin invalid params");
                    return;
                }
                MLog.d("AccountManager", "obtainSystemAccountInfo, onLogin: mAccounts length= " + mAccounts.length);
                MLog.d("AccountManager", "obtainSystemAccountInfo, onLogin: index=" + index);
                AccountManager.this.mCurrentSysAccount = mAccounts[index];
                CryptoMessageUtil.storeSystemHwIdInfo(context, AccountManager.this.mCurrentSysAccount);
                if (callback != null) {
                    callback.run();
                }
            }

            public void onError(ErrorStatus status) {
                MLog.e("AccountManager", "obtainSystemAccountInfo, onError: " + status.getErrorCode());
            }
        });
    }

    public int getAccountBindFingerState(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(FINGER_BIND_QUERY_URI, null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return -1;
            }
            int fingerprintBindType = cursor.getInt(cursor.getColumnIndex("fingerprintBindType"));
            MLog.d("AccountManager", "the current fingerprintBindType is: " + fingerprintBindType);
            if (cursor != null) {
                cursor.close();
            }
            return fingerprintBindType;
        } catch (Exception e) {
            MLog.e("AccountManager", "get account bind finger state exception", (Throwable) e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean isAccountBindFinger(Context context) {
        if (1 == getAccountBindFingerState(context)) {
            return true;
        }
        return false;
    }

    public void checkHwIDPassword(Context context, String accountName, AccountCheckHandler requestHandler) {
        if (requestHandler == null) {
            MLog.e("AccountManager", "checkHwIDPassword, requestHandler is null");
        } else if (context == null || accountName == null) {
            MLog.e("AccountManager", "checkHwIDPassword, param invalid");
            requestHandler.onError(new ErrorStatus(12, ""));
        } else {
            if (CloudAccount.hasLoginAccount(context)) {
                String sysAccountName = CryptoMessageUtil.getHwIDSystemAccountName(context);
                if (sysAccountName == null) {
                    MLog.e("AccountManager", "checkHwIDPassword, sysAccountName is null");
                    checkPasswordIndependent(context, accountName, requestHandler, 1);
                } else if (sysAccountName.equals(accountName)) {
                    String sysAccountUserId = CryptoMessageUtil.getSystemHwIdUserId(context, sysAccountName);
                    if (sysAccountUserId == null) {
                        MLog.d("AccountManager", "checkHwIDPassword sysAccountUserId is null");
                        checkPasswordIndependent(context, accountName, requestHandler, 1);
                    } else {
                        MLog.d("AccountManager", "account is current system account");
                        checkSystemAccountPwd(context, requestHandler, sysAccountUserId);
                    }
                } else {
                    checkPasswordIndependent(context, accountName, requestHandler, 1);
                }
            } else {
                checkPasswordIndependent(context, accountName, requestHandler, 1);
            }
        }
    }

    private void checkSystemAccountPwd(Context context, CloudRequestHandler cloudRequestHandler, String sysAccountUserId) {
        Bundle bundle = new Bundle();
        bundle.putInt("reqClientType", 32);
        bundle.putInt("loginChannel", 32000100);
        CloudAccountManager.checkHwIDPassword(context, sysAccountUserId, isAccountBindFinger(context), cloudRequestHandler, bundle);
    }

    public void checkPasswordIndependent(Context context, String accountName, final AccountCheckHandler origRequestHandler, int requestType) {
        if (origRequestHandler == null) {
            MLog.e("AccountManager", "checkPasswordIndependent, origRequestHandler is null");
        } else if (context == null || accountName == null) {
            MLog.e("AccountManager", "checkPasswordIndependent, param invalid");
            origRequestHandler.onError(new ErrorStatus(12, ""));
        } else {
            MLog.d("AccountManager", "start to checkPasswordIndependent, requestType is: " + requestType);
            Intent intent = new Intent(context, PwdCheckActivity.class);
            intent.putExtra("account_name", accountName);
            intent.putExtra("request_type", requestType);
            setAccountCheckHandler(new AccountCheckHandler() {
                public void onError(ErrorStatus status) {
                    MLog.i("AccountManager", "OnError");
                    Handler handler = getMainHandler();
                    if (handler != null) {
                        handler.sendMessage(Message.obtain(handler, 2, status));
                    }
                    origRequestHandler.onError(status);
                }

                public void onFinish(Bundle bundle) {
                    MLog.i("AccountManager", "onFinish");
                    Handler handler = getMainHandler();
                    if (handler != null) {
                        handler.sendMessage(Message.obtain(handler, 1));
                    }
                    origRequestHandler.setPwd(getPwd());
                    origRequestHandler.setAccountName(getAccountName());
                    origRequestHandler.onFinish(bundle);
                }
            });
            context.startActivity(intent);
        }
    }

    public void bindFingerPrompt(Activity activity) {
        if (isShouldBindFingerPrompt(activity)) {
            setShouldBindFingerPrompt(activity, false);
            if (isAnyfingerRecorded() && getAccountBindFingerState(activity) == 0) {
                showFingerprintBindDialog(activity);
            }
        }
    }

    private void showFingerprintBindDialog(final Activity activity) {
        Builder builder = new Builder(activity);
        builder.setMessage(R.string.bind_fingerprint_dialog_message);
        builder.setTitle(R.string.bind_fingerprint_dialog_title);
        builder.setPositiveButton(R.string.bind_fingerprint_dialog_bind_button, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AccountManager.this.bindFingerprint(activity);
            }
        });
        builder.setNegativeButton(17039360, null);
        builder.show();
    }

    private void bindFingerprint(Activity activity) {
        MLog.i("AccountManager", "start to bind fingerprint to hwid");
        Intent intent = new Intent("com.huawei.hwid.FINGERPRINT_MANAGER");
        intent.setPackage("com.huawei.hwid");
        intent.putExtra("fingerprintManagerType", 1);
        try {
            activity.startActivityForResult(intent, 100);
        } catch (Exception e) {
            MLog.w("AccountManager", "bindFingerprint: start activity fail", e);
        }
    }

    public boolean isCardStateActivated() {
        if (MessageUtils.isMultiSimEnabled()) {
            if (1 == CryptoMessageServiceProxy.getCardActivatedState(0) || 1 == CryptoMessageServiceProxy.getCardActivatedState(1)) {
                return true;
            }
        } else if (1 == getCardActivatedState()) {
            return true;
        }
        return false;
    }

    public boolean isCardStateActivated(int subId) {
        if (1 == CryptoMessageServiceProxy.getCardActivatedState(subId)) {
            return true;
        }
        return false;
    }
}
