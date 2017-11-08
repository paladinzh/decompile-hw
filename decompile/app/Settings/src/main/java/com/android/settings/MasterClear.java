package com.android.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.List;

public class MasterClear extends MasterClearHwBase {
    private TextView mDescTextView;
    private TextView mDescTextViewSummary;
    private CheckBox mExternalStorage;
    private View mExternalStorageContainer;
    private Button mInitiateButton;
    private final OnClickListener mInitiateListener = new OnClickListener() {
        public void onClick(View v) {
            if (!MasterClear.this.runKeyguardConfirmation(55)) {
                MasterClear.this.showFinalConfirmation();
                if (MasterClear.this.mInternalStorage.isChecked()) {
                    ItemUseStat.getInstance().handleClick(MasterClear.this.getActivity(), 2, "format_internal_storage_checked");
                } else {
                    ItemUseStat.getInstance().handleClick(MasterClear.this.getActivity(), 2, "format_internal_storage_not_checked");
                }
                ItemUseStat.getInstance().handleClick(MasterClear.this.getActivity(), 2, "reset_phone");
            }
        }
    };
    private StorageEventListener mStorageListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {
            if (("unmounted".equals(newState) || "mounted".equals(newState) || "mounted_ro".equals(newState)) && MasterClear.this.mDescTextView != null && MasterClear.this.mDescTextViewSummary != null) {
                if (SdCardLockUtils.isSdCardUnlocked(MasterClear.this.getActivity())) {
                    MasterClear.this.mDescTextView.setText(2131628657);
                    MasterClear.this.mDescTextViewSummary.setText(2131628824);
                    return;
                }
                MasterClear.this.mDescTextView.setText(2131628659);
                MasterClear.this.mDescTextViewSummary.setText(2131628825);
            }
        }
    };
    protected StorageManager mStorageManager;

    private boolean runKeyguardConfirmation(int request) {
        if (ParentControl.isChildModeOn(getActivity())) {
            Intent intent = new Intent();
            intent.setClassName("com.huawei.parentcontrol", "com.huawei.parentcontrol.ui.activity.ConfirmPasswordActivity");
            startActivityForResult(intent, 56);
            return true;
        }
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(2131625416));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55 || requestCode == 56) {
            if (resultCode == -1) {
                showFinalConfirmation();
            } else {
                establishInitialState();
            }
        }
    }

    private void showFinalConfirmation() {
        Preference preference = new Preference(getActivity());
        preference.setFragment(MasterClearConfirm.class.getName());
        preference.setTitle(2131625428);
        preference.getExtras().putBoolean("erase_sd", this.mExternalStorage != null ? this.mExternalStorage.isChecked() : false);
        addExtrasExt(preference);
        ((SettingsActivity) getActivity()).onPreferenceStartFragment(null, preference);
    }

    private void establishInitialState() {
        this.mInitiateButton = (Button) this.mContentView.findViewById(2131886790);
        this.mInitiateButton.setOnClickListener(this.mInitiateListener);
        this.mExternalStorageContainer = this.mContentView.findViewById(2131886787);
        this.mExternalStorage = (CheckBox) this.mContentView.findViewById(2131886788);
        initViews();
        this.mDescTextView = (TextView) this.mContentView.findViewById(2131886516);
        this.mDescTextViewSummary = (TextView) this.mContentView.findViewById(2131886778);
        if (SdCardLockUtils.isSdCardUnlocked(getActivity())) {
            this.mDescTextView.setText(2131628657);
            this.mDescTextViewSummary.setText(2131628824);
        } else {
            this.mDescTextView.setText(2131628659);
            this.mDescTextViewSummary.setText(2131628825);
        }
        boolean isExtStorageEmulated = Environment.isExternalStorageEmulated();
        this.mExternalStorageContainer.setVisibility(8);
        this.mExternalStorage.setChecked(!isExtStorageEmulated);
        loadAccountList((UserManager) getActivity().getSystemService("user"));
        StringBuffer contentDescription = new StringBuffer();
        View masterClearContainer = this.mContentView.findViewById(2131886777);
        getContentDescription(masterClearContainer, contentDescription);
        masterClearContainer.setContentDescription(contentDescription);
    }

    private void getContentDescription(View v, StringBuffer description) {
        if (v.getVisibility() == 0) {
            if (v instanceof ViewGroup) {
                ViewGroup vGroup = (ViewGroup) v;
                for (int i = 0; i < vGroup.getChildCount(); i++) {
                    getContentDescription(vGroup.getChildAt(i), description);
                }
            } else if (v instanceof TextView) {
                description.append(((TextView) v).getText());
                description.append(",");
            }
        }
    }

    private void loadAccountList(UserManager um) {
        View accountsLabel = this.mContentView.findViewById(2131886780);
        LinearLayout contents = (LinearLayout) this.mContentView.findViewById(2131886781);
        contents.removeAllViews();
        Context context = getActivity();
        List<UserInfo> profiles = um.getProfiles(UserHandle.myUserId());
        int profilesSize = profiles.size();
        AccountManager mgr = AccountManager.get(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        int accountsCount = 0;
        for (int profileIndex = 0; profileIndex < profilesSize; profileIndex++) {
            int i;
            UserInfo userInfo = (UserInfo) profiles.get(profileIndex);
            int profileId = userInfo.id;
            UserHandle userHandle = new UserHandle(profileId);
            if (N != 0) {
                accountsCount += N;
                AuthenticatorDescription[] descs = AccountManager.get(context).getAuthenticatorTypesAsUser(profileId);
                int M = descs.length;
                View titleView = inflater.inflate(2130969053, contents, false);
                TextView titleText = (TextView) titleView.findViewById(16908310);
                if (userInfo.isManagedProfile()) {
                    i = R$string.category_work;
                } else {
                    i = R$string.category_personal;
                }
                titleText.setText(i);
                contents.addView(titleView);
                for (Account account : mgr.getAccountsAsUser(profileId)) {
                    AuthenticatorDescription authenticatorDescription = null;
                    int j = 0;
                    while (j < M) {
                        if (!isFilterAccountType(descs[j].type) && account.type.equals(descs[j].type)) {
                            authenticatorDescription = descs[j];
                            break;
                        }
                        j++;
                    }
                    if (authenticatorDescription == null) {
                        Log.w("MasterClear", "No descriptor for account name=" + account.name + " type=" + account.type);
                    } else {
                        Drawable icon = null;
                        try {
                            if (authenticatorDescription.iconId != 0) {
                                icon = context.getPackageManager().getUserBadgedIcon(context.createPackageContextAsUser(authenticatorDescription.packageName, 0, userHandle).getDrawable(authenticatorDescription.iconId), userHandle);
                            }
                        } catch (NameNotFoundException e) {
                            Log.w("MasterClear", "Bad package name for account type " + authenticatorDescription.type);
                        } catch (Throwable e2) {
                            Log.w("MasterClear", "Invalid icon id for account type " + authenticatorDescription.type, e2);
                        }
                        View accountview = inflater.inflate(2130968865, contents, false);
                        TextView child = (TextView) accountview.findViewById(2131886455);
                        child.setText(account.name);
                        setIcon(child, icon);
                        contents.addView(accountview);
                    }
                }
            }
        }
        if (contents.getChildCount() != 0) {
            if (accountsCount > 0) {
                accountsLabel.setVisibility(0);
                contents.setVisibility(0);
            }
            View otherUsers = this.mContentView.findViewById(2131886782);
            if (um.getUserCount() - profilesSize > 0) {
                i = 0;
            } else {
                i = 8;
            }
            otherUsers.setVisibility(i);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_factory_reset", UserHandle.myUserId());
        if (!UserManager.get(getActivity()).isAdminUser() || RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_factory_reset", UserHandle.myUserId())) {
            return inflater.inflate(2130968867, null);
        }
        if (admin != null) {
            View view = inflater.inflate(2130968617, null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(0);
            return view;
        }
        this.mContentView = inflater.inflate(2130968864, null);
        if (isFinal(savedInstanceState)) {
            showFinalConfirmation();
        } else {
            establishInitialState();
        }
        return this.mContentView;
    }

    protected int getMetricsCategory() {
        return 66;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mStorageManager = (StorageManager) getActivity().getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
    }

    public void onDestroy() {
        if (!(this.mStorageManager == null || this.mStorageListener == null)) {
            this.mStorageManager.unregisterListener(this.mStorageListener);
        }
        super.onDestroy();
    }
}
