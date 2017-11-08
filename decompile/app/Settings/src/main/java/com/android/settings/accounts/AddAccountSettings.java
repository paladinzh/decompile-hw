package com.android.settings.accounts;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.Settings.ChooseAccountActivity;
import com.android.settings.Utils;

public class AddAccountSettings extends Activity {
    private boolean mAddAccountCalled = false;
    private final AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() {
        public void run(android.accounts.AccountManagerFuture<android.os.Bundle> r12) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0076 in list [B:9:0x0071]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r11 = this;
            r2 = 1;
            r1 = r12.getResult();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r1 = (android.os.Bundle) r1;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = "intent";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r7 = r1.get(r8);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r7 = (android.content.Intent) r7;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r7 == 0) goto L_0x0077;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x0012:
            r2 = 0;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r0 = new android.os.Bundle;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r0.<init>();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = "pendingIntent";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.mPendingIntent;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r0.putParcelable(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = "hasMultipleUsers";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = com.android.settings.Utils.hasMultipleUsers(r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r0.putBoolean(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = "android.intent.extra.USER";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.mUserHandle;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r0.putParcelable(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r7.putExtras(r0);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.mUserHandle;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r10 = 2;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8.startActivityForResultAsUser(r7, r10, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x004b:
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = 2;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = android.util.Log.isLoggable(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r8 == 0) goto L_0x006f;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x0055:
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = new java.lang.StringBuilder;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9.<init>();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r10 = "account added: ";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.append(r10);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.append(r1);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.toString();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            android.util.Log.v(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x006f:
            if (r2 == 0) goto L_0x0076;
        L_0x0071:
            r8 = com.android.settings.accounts.AddAccountSettings.this;
            r8.finish();
        L_0x0076:
            return;
        L_0x0077:
            r8 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = -1;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8.setResult(r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = r8.mPendingIntent;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r8 == 0) goto L_0x004b;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x0085:
            r8 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = r8.mPendingIntent;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8.cancel();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = 0;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8.mPendingIntent = r9;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            goto L_0x004b;
        L_0x0095:
            r4 = move-exception;
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = 2;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = android.util.Log.isLoggable(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r8 == 0) goto L_0x00a9;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x00a0:
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = "addAccount was canceled";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            android.util.Log.v(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x00a9:
            if (r2 == 0) goto L_0x0076;
        L_0x00ab:
            r8 = com.android.settings.accounts.AddAccountSettings.this;
            r8.finish();
            goto L_0x0076;
        L_0x00b1:
            r6 = move-exception;
            r6.printStackTrace();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r2 == 0) goto L_0x0076;
        L_0x00b7:
            r8 = com.android.settings.accounts.AddAccountSettings.this;
            r8.finish();
            goto L_0x0076;
        L_0x00bd:
            r3 = move-exception;
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = 2;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = android.util.Log.isLoggable(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r8 == 0) goto L_0x00e2;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x00c8:
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = new java.lang.StringBuilder;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9.<init>();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r10 = "addAccount failed: ";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.append(r10);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.append(r3);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.toString();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            android.util.Log.v(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x00e2:
            if (r2 == 0) goto L_0x0076;
        L_0x00e4:
            r8 = com.android.settings.accounts.AddAccountSettings.this;
            r8.finish();
            goto L_0x0076;
        L_0x00ea:
            r5 = move-exception;
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = 2;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r8 = android.util.Log.isLoggable(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            if (r8 == 0) goto L_0x010f;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x00f5:
            r8 = "AccountSettings";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = new java.lang.StringBuilder;	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9.<init>();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r10 = "addAccount failed: ";	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.append(r10);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.append(r5);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            r9 = r9.toString();	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
            android.util.Log.v(r8, r9);	 Catch:{ OperationCanceledException -> 0x0095, IOException -> 0x00ea, AuthenticatorException -> 0x00bd, Exception -> 0x00b1, all -> 0x0118 }
        L_0x010f:
            if (r2 == 0) goto L_0x0076;
        L_0x0111:
            r8 = com.android.settings.accounts.AddAccountSettings.this;
            r8.finish();
            goto L_0x0076;
        L_0x0118:
            r8 = move-exception;
            if (r2 == 0) goto L_0x0120;
        L_0x011b:
            r9 = com.android.settings.accounts.AddAccountSettings.this;
            r9.finish();
        L_0x0120:
            throw r8;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.accounts.AddAccountSettings.1.run(android.accounts.AccountManagerFuture):void");
        }
    };
    private PendingIntent mPendingIntent;
    private UserHandle mUserHandle;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }
        if (savedInstanceState != null) {
            this.mAddAccountCalled = savedInstanceState.getBoolean("AddAccountCalled");
            if (Log.isLoggable("AccountSettings", 2)) {
                Log.v("AccountSettings", "restored");
            }
        }
        UserManager um = (UserManager) getSystemService("user");
        this.mUserHandle = Utils.getSecureTargetUser(getActivityToken(), um, null, getIntent().getExtras());
        if (um.hasUserRestriction("no_modify_accounts", this.mUserHandle)) {
            Toast.makeText(this, 2131627369, 1).show();
            finish();
        } else if (this.mAddAccountCalled) {
            finish();
        } else if (Utils.startQuietModeDialogIfNecessary(this, um, this.mUserHandle.getIdentifier())) {
            finish();
        } else {
            if (um.isUserUnlocked(this.mUserHandle)) {
                requestChooseAccount();
            } else if (!new ChooseLockSettingsHelper(this).launchConfirmationActivity(3, getString(2131624724), false, this.mUserHandle.getIdentifier())) {
                requestChooseAccount();
            }
            if (getActionBar() != null) {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode != 0) {
                    addAccount(data.getStringExtra("selected_account"));
                    break;
                }
                if (data != null) {
                    startActivityAsUser(data, this.mUserHandle);
                }
                setResult(resultCode);
                finish();
                return;
            case 2:
                setResult(resultCode);
                if (this.mPendingIntent != null) {
                    this.mPendingIntent.cancel();
                    this.mPendingIntent = null;
                }
                finish();
                break;
            case 3:
                if (resultCode != -1) {
                    finish();
                    break;
                } else {
                    requestChooseAccount();
                    break;
                }
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("AddAccountCalled", this.mAddAccountCalled);
        if (Log.isLoggable("AccountSettings", 2)) {
            Log.v("AccountSettings", "saved");
        }
    }

    private void requestChooseAccount() {
        String[] authorities = getIntent().getStringArrayExtra("authorities");
        String[] accountTypes = getIntent().getStringArrayExtra("account_types");
        Intent intent = new Intent(this, ChooseAccountActivity.class);
        if (authorities != null) {
            intent.putExtra("authorities", authorities);
        }
        if (accountTypes != null) {
            intent.putExtra("account_types", accountTypes);
        }
        intent.putExtra("android.intent.extra.USER", this.mUserHandle);
        startActivityForResult(intent, 1);
    }

    private void addAccount(String accountType) {
        Bundle addAccountOptions = new Bundle();
        Intent identityIntent = new Intent();
        identityIntent.setComponent(new ComponentName("SHOULDN'T RESOLVE!", "SHOULDN'T RESOLVE!"));
        identityIntent.setAction("SHOULDN'T RESOLVE!");
        identityIntent.addCategory("SHOULDN'T RESOLVE!");
        this.mPendingIntent = PendingIntent.getBroadcast(this, 0, identityIntent, 0);
        addAccountOptions.putParcelable("pendingIntent", this.mPendingIntent);
        addAccountOptions.putBoolean("hasMultipleUsers", Utils.hasMultipleUsers(this));
        AccountManager.get(this).addAccountAsUser(accountType, null, null, addAccountOptions, null, this.mCallback, null, this.mUserHandle);
        this.mAddAccountCalled = true;
    }
}
