package com.android.contacts.util;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.gms.R;
import java.io.IOException;

public class AccountPromptUtils {
    private static final String TAG = AccountPromptUtils.class.getSimpleName();

    private static SharedPreferences getSharedPreferences(Context context) {
        return SharePreferenceUtil.getDefaultSp_de(context);
    }

    public static boolean shouldShowAccountPrompt(Context context) {
        for (AuthenticatorDescription authenticatorType : AccountManager.get(context).getAuthenticatorTypes()) {
            if ("com.google".equals(authenticatorType.type)) {
                return getSharedPreferences(context).getBoolean("settings.showAccountPrompt", true);
            }
        }
        return false;
    }

    public static void neverShowAccountPromptAgain(Context context) {
        getSharedPreferences(context).edit().putBoolean("settings.showAccountPrompt", false).apply();
    }

    public static void launchAccountPrompt(Activity activity) {
        Bundle options = new Bundle();
        options.putCharSequence("introMessage", activity.getString(R.string.no_account_prompt));
        options.putBoolean("allowSkip", true);
        AccountManager.get(activity).addAccount("com.google", null, null, options, activity, getAccountManagerCallback(activity), null);
    }

    private static AccountManagerCallback<Bundle> getAccountManagerCallback(final Activity activity) {
        return new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                if (future.isCancelled()) {
                    activity.finish();
                    return;
                }
                try {
                    if (((Bundle) future.getResult()).getBoolean("setupSkipped")) {
                        AccountPromptUtils.neverShowAccountPromptAgain(activity);
                    }
                } catch (OperationCanceledException e) {
                    HwLog.e(AccountPromptUtils.TAG, "Account setup error: account creation process canceled");
                } catch (IOException e2) {
                    HwLog.e(AccountPromptUtils.TAG, "Account setup error: No authenticator was registered for thisaccount type or the authenticator failed to respond");
                } catch (AuthenticatorException e3) {
                    HwLog.e(AccountPromptUtils.TAG, "Account setup error: Authenticator experienced an I/O problem");
                }
            }
        };
    }
}
