package com.android.contacts.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract.AggregationExceptions;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.contacts.ContactSaveService;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.activities.ViewJoinContactsActivity;
import com.android.contacts.hap.copy.CopyContactService;
import com.android.contacts.hap.editor.HAPSelectAccountDialogFragment;
import com.android.contacts.hap.list.ShowSimDialog;
import com.android.contacts.hap.list.ShowSimDialog.SimDialogClickListener;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.util.HAPAccountListAdapter.AccountListFilter;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactLoader;
import com.android.contacts.model.ContactLoader.ContactLoadedListener;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.android.contacts.util.PhoneCapabilityTester;
import com.autonavi.amap.mapcore.MapCore;
import com.google.android.collect.Lists;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import java.util.ArrayList;
import java.util.List;

public class ContactLoaderFragment extends Fragment {
    private static final String TAG = ContactLoaderFragment.class.getSimpleName();
    private AccountWithDataSet mAccountWithDataSet;
    private Contact mContactData;
    private ContactLoadedListener mContactListener = null;
    private Context mContext;
    private String mCustomRingtone;
    private final LoaderCallbacks<Contact> mDetailLoaderListener = new LoaderCallbacks<Contact>() {
        public Loader<Contact> onCreateLoader(int id, Bundle args) {
            Uri lookupUri = (Uri) args.getParcelable("contactUri");
            if (YellowPageContactUtil.isYellowPageUri(lookupUri)) {
                ContactLoaderFragment.this.mLoader = new ContactLoader(ContactLoaderFragment.this.mContext, lookupUri, false, false, false, true);
            } else {
                ContactLoaderFragment.this.mLoader = new ContactLoader(ContactLoaderFragment.this.mContext, lookupUri, true, true, true, true);
            }
            ContactLoaderFragment.this.mLoader.setContactLoadedListener(ContactLoaderFragment.this.mContactListener);
            return ContactLoaderFragment.this.mLoader;
        }

        public void onLoadFinished(Loader<Contact> loader, Contact data) {
            PLog.d(0, "ContactLoaderFragment onLoadFinished");
            if (ContactLoaderFragment.this.mLookupUri == null || ContactLoaderFragment.this.mLookupUri.equals(data.getRequestedUri())) {
                if (data.isError()) {
                    if (ContactLoaderFragment.this.mListener != null) {
                        ContactLoaderFragment.this.mListener.onContactNotFound();
                    }
                } else if (data.isNotFound()) {
                    if (loader != null) {
                        HwLog.i(ContactLoaderFragment.TAG, "No contact found: " + ((ContactLoader) loader).getLookupUri());
                    }
                    ContactLoaderFragment.this.mContactData = null;
                } else {
                    ContactLoaderFragment.this.mContactData = data;
                }
                if (ContactLoaderFragment.this.mListener != null) {
                    if (ContactLoaderFragment.this.mContactData == null) {
                        ContactLoaderFragment.this.mListener.onContactNotFound();
                    } else {
                        ContactLoaderFragment.this.mListener.onDetailsLoaded(ContactLoaderFragment.this.mContactData);
                    }
                }
                if (ContactLoaderFragment.this.getActivity() != null) {
                    ContactLoaderFragment.this.getActivity().invalidateOptionsMenu();
                }
            }
        }

        public void onLoaderReset(Loader<Contact> loader) {
        }
    };
    private boolean mIsLandscape;
    private boolean mIsMultiSelectMode;
    private ContactLoaderFragmentListener mListener;
    private boolean mLoadWhenActivityCreated = true;
    private ContactLoader mLoader = null;
    private Uri mLookupUri;
    private Menu mOptionsMenu = null;
    private boolean mOptionsMenuCanCreateShortcut;
    private boolean mOptionsMenuEditable;
    private boolean mOptionsMenuOptions;
    private boolean mOptionsMenuShareable;
    private AccountWithDataSet mTargetAccount;

    public interface ContactLoaderFragmentListener {
        void onContactNotFound();

        void onDeleteRequested(Uri uri);

        void onDetailsLoaded(Contact contact);

        void onEditRequested(Uri uri);
    }

    class CheckSimCardFreeSpaceTask extends AsyncTask<Void, Void, Boolean> {
        AccountWithDataSet accountWithDataset;
        int type;

        public CheckSimCardFreeSpaceTask(AccountWithDataSet account, int type) {
            this.accountWithDataset = account;
            this.type = type;
        }

        protected Boolean doInBackground(Void... arg0) {
            boolean z = false;
            if (SimFactoryManager.getSimConfig(this.accountWithDataset.type) == null) {
                HwLog.w(ContactLoaderFragment.TAG, "---doInBackgroundSIM sim config is null------");
                return Boolean.valueOf(false);
            }
            if (SimFactoryManager.getSimConfig(this.accountWithDataset.type).getAvailableFreeSpace() != 0) {
                z = true;
            }
            return Boolean.valueOf(z);
        }

        protected void onPostExecute(Boolean result) {
            if (SimFactoryManager.getSimConfig(this.accountWithDataset.type) == null) {
                HwLog.w(ContactLoaderFragment.TAG, "----onPostExecute SIM sim config is null------");
            } else {
                ContactLoaderFragment.this.handleSimCardInteractive(this.accountWithDataset, this.type, result.booleanValue());
            }
        }
    }

    private class UnjoinContactTask extends AsyncTask<Void, Void, Void> {
        private UnjoinContactTask() {
        }

        protected Void doInBackground(Void... params) {
            ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
            ContactLoaderFragment.this.buildSplitContactDiff(diff);
            if (!diff.isEmpty()) {
                try {
                    ContactLoaderFragment.this.mContext.getContentResolver().applyBatch("com.android.contacts", diff);
                } catch (RemoteException e) {
                    HwLog.e(ContactLoaderFragment.TAG, "Problem persisting user edits", e);
                } catch (OperationApplicationException e2) {
                    HwLog.w(ContactLoaderFragment.TAG, "Version consistency failed, re-parenting: " + e2.toString());
                }
                CommonUtilMethods.updateFavoritesWidget(ContactLoaderFragment.this.mContext);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
        }
    }

    public void setCustomRingtone(String customRingtone) {
        this.mCustomRingtone = customRingtone;
    }

    public void setLoadWhenActivityCreated(boolean loadWhenActivityCreated) {
        this.mLoadWhenActivityCreated = loadWhenActivityCreated;
    }

    public void setContactLoadedListener(ContactLoadedListener listener) {
        this.mContactListener = listener;
        if (this.mLoader != null) {
            this.mLoader.setContactLoadedListener(this.mContactListener);
        }
    }

    public ContactLoaderFragment(Context context) {
        this.mContext = context;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (this.mLookupUri == null) {
                this.mLookupUri = (Uri) savedInstanceState.getParcelable("contactUri");
            }
            this.mAccountWithDataSet = (AccountWithDataSet) savedInstanceState.getParcelable("copy_data");
        }
        Fragment dialogFragment = getFragmentManager().findFragmentByTag("accounts_tag");
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 1091);
        }
        if (getResources().getConfiguration().orientation == 2) {
            z = true;
        } else {
            z = false;
        }
        this.mIsLandscape = z;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("contactUri", this.mLookupUri);
        outState.putParcelable("copy_data", this.mAccountWithDataSet);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.contact_detail_loader_fragment, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mLookupUri != null && this.mLoadWhenActivityCreated) {
            Bundle args = new Bundle();
            args.putParcelable("contactUri", this.mLookupUri);
            PLog.d(0, "ContactLoaderFragment init loader");
            getLoaderManager().initLoader(1, args, this.mDetailLoaderListener);
        }
    }

    public void loadUri(Uri lookupUri, boolean forceLoad) {
        loadUri(lookupUri, forceLoad, getActivity());
    }

    public void loadUri(Uri lookupUri, boolean forceLoad, Fragment fragment) {
        if (!Objects.equal(lookupUri, this.mLookupUri) || forceLoad) {
            this.mLookupUri = lookupUri;
            if (this.mLookupUri == null) {
                fragment.getLoaderManager().destroyLoader(1);
                this.mContactData = null;
                if (this.mListener != null) {
                    this.mListener.onDetailsLoaded(this.mContactData);
                }
            } else {
                Bundle args = new Bundle();
                args.putParcelable("contactUri", this.mLookupUri);
                PLog.d(0, "ContactLoaderFragment restart loader");
                fragment.getLoaderManager().restartLoader(1, args, this.mDetailLoaderListener);
            }
        }
    }

    public void setContext(Activity activity) {
        this.mContext = activity;
    }

    public void loadUri(Uri lookupUri, boolean forceLoad, Activity context) {
        if (context != null) {
            if (!Objects.equal(lookupUri, this.mLookupUri) || forceLoad) {
                this.mLookupUri = lookupUri;
                if (this.mLookupUri == null) {
                    context.getLoaderManager().destroyLoader(1);
                    this.mContactData = null;
                    if (this.mListener != null) {
                        this.mListener.onDetailsLoaded(this.mContactData);
                    }
                } else {
                    Bundle args = new Bundle();
                    args.putParcelable("contactUri", this.mLookupUri);
                    PLog.d(0, "ContactLoaderFragment restart loader");
                    context.getLoaderManager().restartLoader(1, args, this.mDetailLoaderListener);
                }
            }
        }
    }

    public void setListener(ContactLoaderFragmentListener value) {
        this.mListener = value;
    }

    public boolean isOptionsMenuChanged() {
        if (this.mOptionsMenuOptions == isContactOptionsChangeEnabled() && this.mOptionsMenuEditable == isContactEditable() && this.mOptionsMenuShareable == isContactShareable() && this.mOptionsMenuCanCreateShortcut == isContactCanCreateShortcut()) {
            return false;
        }
        return true;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (!this.mIsMultiSelectMode) {
        }
    }

    public boolean isContactOptionsChangeEnabled() {
        if (this.mContactData == null || this.mContactData.isDirectoryEntry() || !PhoneCapabilityTester.isPhone(this.mContext) || this.mContactData.isYellowPage()) {
            return false;
        }
        return true;
    }

    public boolean isContactEditable() {
        return (this.mContactData == null || this.mContactData.isDirectoryEntry() || this.mContactData.isYellowPage()) ? false : true;
    }

    public boolean isContactShareable() {
        return (this.mContactData == null || this.mContactData.isDirectoryEntry()) ? false : true;
    }

    public boolean isContactCanCreateShortcut() {
        if (this.mContactData == null || this.mContactData.isUserProfile() || this.mContactData.isDirectoryEntry() || this.mContactData.isYellowPage()) {
            return false;
        }
        return true;
    }

    public boolean isCopyEnabledForCurrentContact() {
        List<AccountWithDataSet> accountsList = AccountTypeManager.getInstance(this.mContext).getAccounts(true);
        if (this.mAccountWithDataSet == null) {
            this.mAccountWithDataSet = getCurrentAccountWithDataSet();
        }
        if (accountsList.size() > 1 || !accountsList.contains(this.mAccountWithDataSet)) {
            return true;
        }
        return false;
    }

    public AccountWithDataSet getCurrentAccountWithDataSet() {
        if (this.mContactData == null) {
            return null;
        }
        RawContact rawContact = (RawContact) this.mContactData.getRawContacts().get(0);
        String accountName = rawContact.getAccountName();
        String accountType = rawContact.getAccountTypeString();
        AccountWithDataSet accountWithDataSet = null;
        if (!(accountName == null || accountType == null)) {
            accountWithDataSet = new AccountWithDataSet(accountName, accountType, rawContact.getDataSet());
        }
        return accountWithDataSet;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mIsMultiSelectMode ? false : false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case 1:
                    handleRingtonePicked((Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI"));
                    break;
                case MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER /*999*/:
                    if (data != null) {
                        joinAggregate(ContentUris.parseId(data.getData()), data.getLongExtra("com.android.contacts.action.CONTACT_ID", -1));
                        break;
                    }
                    break;
                case 1000:
                case 1001:
                    Uri pickedUri;
                    if (requestCode == 1000) {
                        pickedUri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                    } else {
                        pickedUri = data.getData();
                    }
                    handleRingtonePicked(pickedUri);
                    break;
                case 1091:
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        AccountWithDataSet account = (AccountWithDataSet) bundle.get("account");
                        Bundle extrasBundle = (Bundle) bundle.getParcelable("extra_args");
                        if (account != null && extrasBundle != null) {
                            onCopyAccountChosen(account, extrasBundle);
                            break;
                        }
                        return;
                    }
                    break;
            }
        }
    }

    private void joinAggregate(long aContactId, long targetContactId) {
        this.mContext.startService(ContactSaveService.createJoinContactsIntent(this.mContext, targetContactId, aContactId, true, ViewJoinContactsActivity.class, "joinCompleted"));
    }

    private void handleRingtonePicked(Uri pickedUri) {
        if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
            this.mCustomRingtone = null;
        } else {
            this.mCustomRingtone = pickedUri.toString();
        }
        this.mContext.startService(ContactSaveService.createSetRingtone(this.mContext, this.mLookupUri, this.mCustomRingtone));
    }

    public void doCopyContact() {
        HAPSelectAccountDialogFragment.show(getFragmentManager(), this, R.string.copy_contact_to, AccountListFilter.ACCOUNTS_WRITABLE_EXCLUDE_CURRENT, null, this.mAccountWithDataSet, "accounts_tag");
    }

    private void onCopyAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        HwLog.i(TAG, "Copy account chosen");
        this.mTargetAccount = account;
        if (CommonUtilMethods.isSimAccount(this.mTargetAccount.type)) {
            new CheckSimCardFreeSpaceTask(this.mTargetAccount, 3).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        } else {
            copyContact();
        }
    }

    private void handleSimCardInteractive(AccountWithDataSet account, int type, boolean availableFreeSpace) {
        if (account != null && account.equals(this.mTargetAccount) && getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            String accountName;
            if (type == 1) {
                if (availableFreeSpace) {
                    ShowSimDialog.show(getFragmentManager(), new SimDialogClickListener() {
                        public int describeContents() {
                            return 0;
                        }

                        public void dialogClicked() {
                            ContactLoaderFragment.this.copyContact();
                        }
                    }, account.type);
                } else {
                    accountName = SimFactoryManager.getSimCardDisplayLabel(account.type);
                    Toast.makeText(getActivity(), String.format(getString(R.string.sim_full), new Object[]{accountName, accountName}), 0).show();
                }
            } else if (type == 2) {
                if (availableFreeSpace) {
                    ShowSimDialog.show(getFragmentManager(), new SimDialogClickListener() {
                        public int describeContents() {
                            return 0;
                        }

                        public void dialogClicked() {
                            ContactLoaderFragment.this.copyContact();
                        }
                    }, account.type);
                } else {
                    accountName = SimFactoryManager.getSimCardDisplayLabel(account.type);
                    Toast.makeText(getActivity(), String.format(getString(R.string.sim_full), new Object[]{accountName, accountName}), 0).show();
                }
            } else if (type == 3) {
                if (availableFreeSpace) {
                    ShowSimDialog.show(getFragmentManager(), new SimDialogClickListener() {
                        public int describeContents() {
                            return 0;
                        }

                        public void dialogClicked() {
                            ContactLoaderFragment.this.copyContact();
                        }

                        public void onNotificationCancel() {
                            ContactLoaderFragment.this.mTargetAccount = null;
                        }
                    }, this.mTargetAccount.type);
                } else {
                    accountName = SimFactoryManager.getSimCardDisplayLabel(account.type);
                    Toast.makeText(getActivity(), String.format(getString(R.string.sim_full), new Object[]{accountName, accountName}), 0).show();
                    this.mTargetAccount = null;
                }
            }
        }
    }

    public boolean copyContactEvent() {
        if (this.mContactData == null) {
            return false;
        }
        if (isContactJoined()) {
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "Dialog for the joined contact is to be displayed");
            }
            if (CommonUtilMethods.isMergeFeatureEnabled()) {
                AlertDialogFragmet.show(getFragmentManager(), (int) R.string.str_cannot_copy_merged_contact, (int) R.string.str_cannot_copy_merged_contact, (int) R.string.contact_known_button_text, true);
            } else {
                AlertDialogFragmet.show(getFragmentManager(), R.string.copy_to_label, getString(R.string.str_view_contact_cannot_copy), R.string.str_view_contact_cannot_copy, false, null, -1);
            }
        } else {
            if (this.mTargetAccount == null) {
                List<AccountWithDataSet> accountsList = AccountTypeManager.getInstance(this.mContext).getAccounts(true);
                List<AccountWithDataSet> tempList = new ArrayList();
                tempList.addAll(accountsList);
                this.mAccountWithDataSet = getCurrentAccountWithDataSet();
                if (tempList.contains(this.mAccountWithDataSet)) {
                    tempList.remove(this.mAccountWithDataSet);
                }
                int size = tempList.size();
                if (size > 1) {
                    doCopyContact();
                    return true;
                } else if (size == 0) {
                    return true;
                } else {
                    this.mTargetAccount = (AccountWithDataSet) tempList.get(0);
                }
            }
            if (CommonUtilMethods.isSimAccount(this.mTargetAccount.type) || "com.android.huawei.secondsim".equalsIgnoreCase(this.mTargetAccount.type)) {
                int type = -1;
                if (CommonUtilMethods.isSimAccount(this.mTargetAccount.type)) {
                    type = 1;
                } else if ("com.android.huawei.secondsim".equalsIgnoreCase(this.mTargetAccount.type)) {
                    type = 2;
                }
                if (type != -1) {
                    new CheckSimCardFreeSpaceTask(this.mTargetAccount, type).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                }
            } else {
                copyContact();
            }
        }
        return true;
    }

    public boolean isContactJoined() {
        if (this.mContactData == null || this.mContactData.getRawContacts().size() <= 1) {
            return false;
        }
        return true;
    }

    private void copyContact() {
        if (this.mTargetAccount != null) {
            long[] rawContactIds = new long[1];
            if (this.mContactData != null && this.mContactData.getRawContacts() != null && this.mContactData.getRawContactIds().get(0) != null) {
                rawContactIds[0] = ((RawContact) this.mContactData.getRawContacts().get(0)).getId().longValue();
                this.mContext.startService(CopyContactService.createCopyContactsIntent(this.mContext, rawContactIds, this.mTargetAccount.name, this.mTargetAccount.type, this.mTargetAccount.dataSet));
                this.mTargetAccount = null;
            }
        }
    }

    public void setIsMultiSelect(boolean isMultiSelect) {
        this.mIsMultiSelectMode = isMultiSelect;
    }

    private synchronized void buildSplitContactDiff(ArrayList<ContentProviderOperation> aDiff) {
        if (this.mContactData != null) {
            ArrayList<Long> lRawContactIds = this.mContactData.getRawContactIds();
            int count = lRawContactIds.size();
            for (int i = 0; i < count - 1; i++) {
                Long id1 = (Long) lRawContactIds.get(i);
                for (int j = i + 1; j < count; j++) {
                    Long id2 = (Long) lRawContactIds.get(j);
                    Builder builder = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
                    builder.withValue("type", Integer.valueOf(2));
                    builder.withValue("raw_contact_id1", id1);
                    builder.withValue("raw_contact_id2", id2);
                    aDiff.add(builder.build());
                }
            }
        }
    }

    public void initiateUnJoinContacts() {
        new UnjoinContactTask().execute(new Void[]{(Void) null});
    }

    public void setContact(Contact lContact) {
        this.mContactData = lContact;
    }
}
