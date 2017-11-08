package com.android.contacts.preference;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.AccountLoadListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.activities.ContactsMissingItemsActivity;
import com.android.contacts.hap.delete.DuplicateContactsService;
import com.android.contacts.hap.editor.HAPSelectAccountDialogFragment;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.hap.util.HAPAccountListAdapter.AccountListFilter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.MVersionUpgradeUtils.ProviderStatus;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import java.util.List;

public class OrganizeContactsFragment extends PreferenceFragment implements OnAccountsUpdateListener, AccountLoadListener, OnClickListener {
    private String ACCOUNT_CHOOSER_TAG = "accounts_chooser_tag_dcb";
    boolean currentPreferenceValue = true;
    private boolean hasAccountChanged = false;
    private Preference mContactsMissintItems;
    private Context mContext;
    private Preference mCopyContacts;
    private String mCurrentLanguage = "";
    private Preference mDeleteContacts;
    private ContactListFilter mFilter;
    private long mFirstTime = 0;
    Handler mHandler = new Handler();
    private Preference mMergeDuplicateContacts;
    SharedPreferences mPref;
    private ProgressDialog mRebuidingProgressDialog;
    private Preference mRebuildSearchIndex;
    private long mSecondTime;
    boolean startUpdating = true;

    private class RebuildSearchIndex extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public RebuildSearchIndex(Context context) {
            this.mContext = context;
        }

        protected Void doInBackground(Void... arg0) {
            int lProviderStatus = 0;
            Cursor cursor = this.mContext.getContentResolver().query(ProviderStatus.CONTENT_URI, new String[]{"status"}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        lProviderStatus = cursor.getInt(0);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (lProviderStatus == 0) {
                Uri uri = Uri.withAppendedPath(Contacts.CONTENT_URI, "rebuild_search_data");
                ContentValues updataValues = new ContentValues();
                updataValues.put("rebuild_search_data", Boolean.valueOf(true));
                this.mContext.getContentResolver().update(uri, updataValues, null, null);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (OrganizeContactsFragment.this.mRebuidingProgressDialog != null && OrganizeContactsFragment.this.mRebuidingProgressDialog.isShowing()) {
                try {
                    OrganizeContactsFragment.this.mRebuidingProgressDialog.dismiss();
                } catch (IllegalArgumentException e) {
                    HwLog.e("OrganizeContactsFragment", e.toString(), e);
                }
            }
            OrganizeContactsFragment.this.mRebuidingProgressDialog = null;
        }
    }

    private class UpdateCopyContactsEnableTask extends AsyncTask<Void, Void, Boolean> {
        private UpdateCopyContactsEnableTask() {
        }

        protected Boolean doInBackground(Void... params) {
            boolean lShowCopyContacts = false;
            Activity activity = OrganizeContactsFragment.this.getActivity();
            if (activity == null) {
                return Boolean.valueOf(false);
            }
            if (!OrganizeContactsFragment.this.getSimPresence() && AccountTypeManager.getInstance(activity.getApplicationContext()).getAccounts(true).size() > 0 && AccountTypeManager.getInstance(activity.getApplicationContext()).getAccounts(false).size() > 1) {
                lShowCopyContacts = true;
            } else if (OrganizeContactsFragment.this.getSimPresence() && AccountTypeManager.getInstance(activity.getApplicationContext()).getAccountsExcludeSim(true).size() > 0 && AccountTypeManager.getInstance(activity.getApplicationContext()).getAccountsExcludeSim(false).size() > 1) {
                lShowCopyContacts = true;
            }
            return Boolean.valueOf(lShowCopyContacts);
        }

        protected void onPostExecute(Boolean result) {
            if (!(OrganizeContactsFragment.this.mCopyContacts == null || result == null)) {
                OrganizeContactsFragment.this.mCopyContacts.setEnabled(result.booleanValue());
            }
            super.onPostExecute(result);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity().getApplicationContext();
        this.mPref = SharePreferenceUtil.getDefaultSp_de(this.mContext);
        this.mCurrentLanguage = this.mContext.getResources().getConfiguration().locale.getLanguage();
        initialize();
        AccountManager.get(this.mContext).addOnAccountsUpdatedListener(this, null, false);
        AccountTypeManager.getInstance(this.mContext).setAccountLoadListener(this);
    }

    public void onAccountsLoadCompleted() {
        updateCopyContactsEnable();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ListView) getView().findViewById(16908298)).setDivider(null);
    }

    public void onAccountsUpdated(Account[] accounts) {
        this.hasAccountChanged = true;
    }

    private boolean getSimPresence() {
        boolean lExcludeSim = true;
        if (SimFactoryManager.isDualSim()) {
            if (SimFactoryManager.hasIccCard(0) && SimUtility.isSimStateLoaded(0, getActivity())) {
                lExcludeSim = false;
            }
            if (SimFactoryManager.hasIccCard(1) && SimUtility.isSimStateLoaded(1, getActivity())) {
                return false;
            }
            return lExcludeSim;
        } else if (SimFactoryManager.hasIccCard(-1) && SimUtility.isSimStateLoaded(-1, getActivity())) {
            return false;
        } else {
            return true;
        }
    }

    public void onResume() {
        updateCopyContactsEnable();
        String language = this.mContext.getResources().getConfiguration().locale.getLanguage();
        if (!(language == null || language.equals(this.mCurrentLanguage)) || this.hasAccountChanged) {
            this.mCurrentLanguage = language;
            cleanUpPreferenceScreen();
            initialize();
            this.hasAccountChanged = false;
        }
        super.onResume();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1091) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (-1 == resultCode) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                AccountWithDataSet account = (AccountWithDataSet) bundle.get("account");
                Bundle extrasBundle = (Bundle) bundle.getParcelable("extra_args");
                if (account != null) {
                    onCopyAccountChosen(account, extrasBundle);
                }
            }
        } else {
            onAccountSelectorCancelled();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onDestroy() {
        super.onDestroy();
        AccountManager.get(this.mContext).removeOnAccountsUpdatedListener(this);
        if (this.mRebuidingProgressDialog != null && this.mRebuidingProgressDialog.isShowing()) {
            this.mRebuidingProgressDialog.dismiss();
            this.mRebuidingProgressDialog = null;
        }
    }

    private boolean handleDeleteDuplicateRequest() {
        Bundle lBundle = new Bundle();
        lBundle.putInt("resId", R.string.delete_duplicate_contacts);
        List<AccountWithDataSet> lAccounts = AccountTypeManager.getInstance(getActivity()).getAccounts(true);
        if (lAccounts.size() > 1) {
            HAPSelectAccountDialogFragment.show(getFragmentManager(), this, R.string.dialog_delete_duplicate_contacts_account, AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, lBundle, null, this.ACCOUNT_CHOOSER_TAG);
        } else {
            onCopyAccountChosen((AccountWithDataSet) lAccounts.get(0), lBundle);
        }
        return false;
    }

    public void onCopyAccountChosen(AccountWithDataSet aAccount, Bundle aExtraArgs) {
        int resId = -1;
        if (aExtraArgs != null) {
            resId = aExtraArgs.getInt("resId");
        }
        Intent intent;
        if (resId == R.string.delete_duplicate_contacts) {
            intent = DuplicateContactsService.createDeleteDuplicateContactsIntent(getActivity(), aAccount);
            if (!(getActivity() == null || getActivity().isFinishing())) {
                getActivity().startService(intent);
            }
            return;
        }
        if (HwLog.HWDBG) {
            HwLog.d("OrganizeContactsFragment", "Copy contacts option selected");
        }
        intent = new Intent();
        intent.setPackage("com.android.contacts");
        intent.setAction("android.intent.action.HAP_COPY_TO_ACCOUNT");
        intent.putExtra("extra_account_name", aAccount.name);
        intent.putExtra("extra_account_type", aAccount.type);
        intent.putExtra("extra_account_data_set", aAccount.dataSet);
        startActivity(intent);
    }

    public void onAccountSelectorCancelled() {
    }

    private void initialize() {
        addPreferencesFromResource(R.xml.preference_organize_contacts);
        new IntentFilter().addAction("com.android.huawei.profile_exists");
        this.mMergeDuplicateContacts = findPreference(this.mContext.getString(R.string.merge_duplicated_contacts));
        if (!QueryUtil.isHAPProviderInstalled()) {
            getPreferenceScreen().removePreference(this.mMergeDuplicateContacts);
        } else if (CommonUtilMethods.isMergeFeatureEnabled()) {
            this.mMergeDuplicateContacts.setTitle(this.mContext.getString(R.string.merge_duplicated_contacts));
        } else {
            this.mMergeDuplicateContacts.setTitle(this.mContext.getString(R.string.delete_duplicate_contacts));
        }
        this.mMergeDuplicateContacts.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ExceptionCapture.reportScene(73);
                if (CommonUtilMethods.isMergeFeatureEnabled()) {
                    OrganizeContactsFragment.this.startActivity(CommonUtilMethods.getMergeContactsIntent(OrganizeContactsFragment.this.mFilter));
                } else {
                    OrganizeContactsFragment.this.handleDeleteDuplicateRequest();
                }
                return true;
            }
        });
        this.mContactsMissintItems = findPreference(this.mContext.getString(R.string.contacts_missing_items));
        if (!QueryUtil.isHAPProviderInstalled()) {
            getPreferenceScreen().removePreference(this.mContactsMissintItems);
        }
        this.mContactsMissintItems.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ExceptionCapture.reportScene(74);
                StatisticalHelper.report(1134);
                OrganizeContactsFragment.this.onMissingContactsAction();
                return true;
            }
        });
        this.mCopyContacts = findPreference(this.mContext.getString(R.string.key_prefs_copy));
        this.mCopyContacts.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ExceptionCapture.reportScene(75);
                OrganizeContactsFragment.this.onCopyContactsAction();
                return true;
            }
        });
        this.mDeleteContacts = findPreference(this.mContext.getString(R.string.contacts_batch_delete));
        this.mDeleteContacts.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ExceptionCapture.reportScene(76);
                StatisticalHelper.report(1136);
                OrganizeContactsFragment.this.onDeleteContactsAction();
                return true;
            }
        });
        this.mRebuildSearchIndex = findPreference(this.mContext.getString(R.string.contacts_rebuild_index_title));
        this.mRebuildSearchIndex.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = OrganizeContactsFragment.this.getActivity();
                if (OrganizeContactsFragment.this.isAdded() && activity != null) {
                    Builder builder = new Builder(OrganizeContactsFragment.this.getActivity());
                    builder.setTitle(R.string.contacts_rebuild_index_title);
                    builder.setMessage(R.string.contacts_rebuild_index_question);
                    builder.setNegativeButton(17039369, OrganizeContactsFragment.this);
                    builder.setPositiveButton(17039370, OrganizeContactsFragment.this);
                    builder.create().show();
                }
                return true;
            }
        });
        if (!QueryUtil.isHAPProviderInstalled()) {
            getPreferenceScreen().removePreference(this.mRebuildSearchIndex);
        }
        Fragment dialogFragment = getFragmentManager().findFragmentByTag(this.ACCOUNT_CHOOSER_TAG);
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 1091);
        }
    }

    private void onMissingContactsAction() {
        Intent lIntent = new Intent();
        lIntent.setClass(getActivity(), ContactsMissingItemsActivity.class);
        startActivity(lIntent);
    }

    private void onCopyContactsAction() {
        this.mSecondTime = System.currentTimeMillis();
        Bundle lBundle = new Bundle();
        lBundle.putInt("resId", R.string.copy_to_account);
        if (this.mSecondTime - this.mFirstTime >= 500) {
            HAPSelectAccountDialogFragment.show(getFragmentManager(), this, R.string.dialog_copy_contact_account, AccountListFilter.ACCOUNTS_COPY_ALLOWED, lBundle, null, this.ACCOUNT_CHOOSER_TAG);
            this.mFirstTime = this.mSecondTime;
        }
    }

    private void onDeleteContactsAction() {
        Intent lIntent = new Intent();
        lIntent.setAction("android.intent.action.HAP_DELETE_CONTACTS");
        startActivity(lIntent);
    }

    private void rebuildSearchIndex(Context context) {
        showProgressDialog();
        new RebuildSearchIndex(context).executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void showProgressDialog() {
        if (this.mRebuidingProgressDialog == null || !this.mRebuidingProgressDialog.isShowing()) {
            this.mRebuidingProgressDialog = ProgressDialog.show(getActivity(), "", this.mContext.getString(R.string.contacts_rebuild_index_now), true, false);
        }
    }

    private void updateCopyContactsEnable() {
        new UpdateCopyContactsEnableTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void cleanUpPreferenceScreen() {
        getPreferenceScreen().removeAll();
    }

    public void setContactListFilter(ContactListFilter filter) {
        this.mFilter = filter;
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                dialog.dismiss();
                return;
            case -1:
                Activity activity = getActivity();
                if (isAdded() && activity != null) {
                    ExceptionCapture.reportScene(77);
                    rebuildSearchIndex(activity.getApplicationContext());
                }
                StatisticalHelper.report(4032);
                return;
            default:
                return;
        }
    }
}
