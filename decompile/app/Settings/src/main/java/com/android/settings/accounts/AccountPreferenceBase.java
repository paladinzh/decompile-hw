package com.android.settings.accounts;

import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import java.util.ArrayList;
import java.util.Date;

abstract class AccountPreferenceBase extends SettingsPreferenceFragment implements OnAccountsUpdateListener {
    protected AuthenticatorHelper mAuthenticatorHelper;
    private final Handler mHandler = new Handler();
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int which) {
            AccountPreferenceBase.this.mHandler.post(new Runnable() {
                public void run() {
                    AccountPreferenceBase.this.onSyncStateUpdated();
                }
            });
        }
    };
    private UserManager mUm;
    protected UserHandle mUserHandle;

    AccountPreferenceBase() {
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUm = (UserManager) getSystemService("user");
        Activity activity = getActivity();
        this.mUserHandle = Utils.getSecureTargetUser(activity.getActivityToken(), this.mUm, getArguments(), activity.getIntent().getExtras());
        this.mAuthenticatorHelper = new AuthenticatorHelper(activity, this.mUserHandle, this);
    }

    public void onAccountsUpdate(UserHandle userHandle) {
    }

    protected void onAuthDescriptionsUpdated() {
    }

    protected void onSyncStateUpdated() {
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(13, this.mSyncStatusObserver);
        onSyncStateUpdated();
    }

    public void onPause() {
        super.onPause();
        ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        return this.mAuthenticatorHelper.getAuthoritiesForAccountType(type);
    }

    public PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        PreferenceScreen prefs = null;
        if (this.mAuthenticatorHelper.containsAccountType(accountType)) {
            try {
                AuthenticatorDescription desc = this.mAuthenticatorHelper.getAccountTypeDescription(accountType);
                if (!(desc == null || desc.accountPreferencesId == 0)) {
                    Context targetCtx = getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle);
                    int themeID = targetCtx.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
                    Log.d("AccountSettings", "themeID is: " + themeID);
                    prefs = getPreferenceManager().inflateFromResource(new ContextThemeWrapper(targetCtx, themeID), desc.accountPreferencesId, parent);
                }
            } catch (NameNotFoundException e) {
                Log.w("AccountSettings", "Couldn't load preferences.xml file from " + null.packageName);
            } catch (NotFoundException e2) {
                Log.w("AccountSettings", "Couldn't load preferences.xml file from " + null.packageName);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
        return prefs;
    }

    public void updateAuthDescriptions() {
        this.mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        onAuthDescriptionsUpdated();
    }

    protected Drawable getDrawableForType(String accountType) {
        return this.mAuthenticatorHelper.getDrawableForType(getActivity(), accountType);
    }

    protected CharSequence getLabelForType(String accountType) {
        return this.mAuthenticatorHelper.getLabelForType(getActivity(), accountType);
    }

    protected String formatSyncDate(Date date) {
        return DateUtils.formatDateTime(getActivity().getApplicationContext(), date.getTime(), 68117);
    }
}
