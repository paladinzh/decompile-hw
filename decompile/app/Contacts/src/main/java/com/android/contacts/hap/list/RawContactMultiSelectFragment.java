package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.activities.CopyProgressBarActivity;
import com.android.contacts.hap.copy.CopyContactService;
import com.android.contacts.hap.editor.HAPSelectAccountDialogFragment;
import com.android.contacts.hap.list.ContactMultiselectionFragment.MultiSelectListener;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.util.HAPAccountListAdapter;
import com.android.contacts.hap.util.HAPAccountListAdapter.AccountListFilter;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.DirectoryPartition;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RawContactMultiSelectFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    private boolean mAccountChanged = false;
    private RelativeLayout mAccountContainter;
    private AccountWithDataSet mAccountWithDataSet;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == 16908295) {
                RawContactMultiSelectFragment.this.getActivity().finish();
            }
        }
    };
    private ContactListAdapter mAdapter;
    private AlertDialog mAlertDialog;
    private ArrayList<Uri> mAllMembers = new ArrayList();
    private Context mContext;
    private boolean mIsAllSelected = true;
    private MultiSelectListener mMultiListener;
    private Activity mParentActivity;
    private ArrayList<Uri> mSelectedContacts = new ArrayList();
    private AccountWithDataSet mTargetAccount;
    private TextView mTextViewCount;
    private ActionBarCustomTitle mTitle;

    public RawContactMultiSelectFragment() {
        setPhotoLoaderEnabled(true);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(false);
        setVisibleScrollbarEnabled(true);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View actionBarButtons = inflater.inflate(R.layout.hapmultiselect, new LinearLayout(this.mParentActivity), false);
        ActionBar lActionBar = this.mParentActivity.getActionBar();
        if (lActionBar != null) {
            if (EmuiVersion.isSupportEmui()) {
                ActionBarEx.setStartIcon(lActionBar, true, null, this.mActionBarListener);
                ActionBarEx.setEndIcon(lActionBar, false, null, null);
                this.mTitle = new ActionBarCustomTitle(this.mParentActivity, inflater);
                ActionBarEx.setCustomTitle(lActionBar, this.mTitle.getTitleLayout());
            } else {
                this.mTitle = new ActionBarCustomTitle(this.mParentActivity, inflater);
                ActionBarCustom custActionBar = new ActionBarCustom(this.mContext, lActionBar);
                custActionBar.init();
                custActionBar.setStartIcon(true, null, this.mActionBarListener);
                custActionBar.setEndIcon(false, null, null);
                custActionBar.setCustomTitle(this.mTitle.getTitleLayout());
            }
            setActionBarTitle();
        }
        this.mTextViewCount = (TextView) actionBarButtons.findViewById(R.id.selected_item_count);
        this.mTextViewCount.setVisibility(4);
        View lView = inflater.inflate(R.layout.contacts_multiselect_list_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        return lView;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        this.mAccountContainter = (RelativeLayout) getView().findViewById(R.id.account_container);
        this.mAccountContainter.setBackground(ViewUtil.getSelectorDrawable(this.mAccountContainter.getBackground(), 0.5f, 0.5f));
        if (populateCurrentAccount()) {
            enableOrDisableListPopUp();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mAccountWithDataSet == null) {
            this.mParentActivity.finish();
        }
    }

    public void onAttach(Activity activity) {
        if (HwLog.HWDBG) {
            HwLog.d("RawContactMultiSelectFragment", "onAttach() is called!");
        }
        super.configureAdapter();
        super.onAttach(activity);
        this.mParentActivity = activity;
        this.mContext = this.mParentActivity.getApplicationContext();
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (savedState != null) {
            for (String uriString : savedState.getStringArrayList("selected_contacts_keys")) {
                this.mSelectedContacts.add(Uri.withAppendedPath(RawContacts.CONTENT_URI, uriString));
            }
            String accountName = savedState.getString("name");
            String accountType = savedState.getString("type");
            String accountDataset = savedState.getString("dataset");
            if (!(TextUtils.isEmpty(accountName) && TextUtils.isEmpty(accountType))) {
                this.mAccountWithDataSet = new AccountWithDataSet(accountName, accountType, accountDataset);
            }
        }
        Fragment dialogFragment = getFragmentManager().findFragmentByTag("accounts_chooser_tag_rcm");
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 1091);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mAdapter != null) {
            this.mAdapter.upateSimpleDisplayMode();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> selectedContactsUriString = new ArrayList();
        for (Uri uri : this.mSelectedContacts) {
            selectedContactsUriString.add(uri.getLastPathSegment());
        }
        outState.putStringArrayList("selected_contacts_keys", selectedContactsUriString);
        if (this.mAccountWithDataSet != null) {
            outState.putString("name", this.mAccountWithDataSet.name);
            outState.putString("type", this.mAccountWithDataSet.type);
            outState.putString("dataset", this.mAccountWithDataSet.dataSet);
        }
    }

    protected void configureAdapter() {
        super.configureAdapter();
        if (HwLog.HWDBG) {
            HwLog.d("RawContactMultiSelectFragment", "configureAdapter() is called!");
        }
    }

    private void enableAccountListDialog() {
        HAPAccountListAdapter adapter;
        if (218 != getContactsRequest().getActionCode()) {
            adapter = new HAPAccountListAdapter(getContext(), AccountListFilter.ACCOUNTS_COPY_ALLOWED, this.mAccountWithDataSet, true);
        } else if (this.mParentActivity.getIntent().getExtras() != null) {
            adapter = new HAPAccountListAdapter(getContext(), AccountListFilter.ACCOUNTS_COPY_ALLOWED, this.mAccountWithDataSet, this.mParentActivity.getIntent().getExtras());
        } else {
            return;
        }
        this.mAccountContainter.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RawContactMultiSelectFragment.this.mAlertDialog == null || !RawContactMultiSelectFragment.this.mAlertDialog.isShowing()) {
                    Builder builder = new Builder(RawContactMultiSelectFragment.this.getContext());
                    ListAdapter listAdapter = adapter;
                    final HAPAccountListAdapter hAPAccountListAdapter = adapter;
                    builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AccountWithDataSet newAccount = hAPAccountListAdapter.getItem(which);
                            if (!newAccount.equals(RawContactMultiSelectFragment.this.mAccountWithDataSet)) {
                                if (HwLog.HWDBG) {
                                    HwLog.d("RawContactMultiSelectFragment", "New account is selected");
                                }
                                RawContactMultiSelectFragment.this.mAccountWithDataSet = newAccount;
                                hAPAccountListAdapter.setCurrentAccount(newAccount);
                                RawContactMultiSelectFragment.this.mAdapter.setFilter(new ContactListFilter(-7, RawContactMultiSelectFragment.this.mAccountWithDataSet.type, RawContactMultiSelectFragment.this.mAccountWithDataSet.name, RawContactMultiSelectFragment.this.mAccountWithDataSet.dataSet, null, -1));
                                RawContactMultiSelectFragment.this.startLoadingPhotos();
                                RawContactMultiSelectFragment.this.fillAccountData();
                                RawContactMultiSelectFragment.this.clearSelectedItemInListview();
                                Partition partition = RawContactMultiSelectFragment.this.mAdapter.getPartition(0);
                                if (partition != null && (partition instanceof DirectoryPartition)) {
                                    ((DirectoryPartition) partition).setStatus(0);
                                }
                                RawContactMultiSelectFragment.this.forceLoading();
                                RawContactMultiSelectFragment.this.mSelectedContacts.clear();
                                RawContactMultiSelectFragment.this.setActionBarTitle();
                                RawContactMultiSelectFragment.this.mAccountChanged = true;
                            }
                        }
                    });
                    RawContactMultiSelectFragment.this.mAlertDialog = builder.create();
                    RawContactMultiSelectFragment.this.mAlertDialog.show();
                }
            }
        });
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
            this.mAlertDialog = null;
        }
        super.onConfigurationChanged(newConfig);
    }

    private void disableAccountListPopUp() {
    }

    private boolean populateCurrentAccount() {
        if (this.mAccountWithDataSet != null) {
            return true;
        }
        Intent intent = this.mParentActivity.getIntent();
        String accountName = intent.getStringExtra("extra_account_name");
        String accountType = intent.getStringExtra("extra_account_type");
        String dataSet = intent.getStringExtra("extra_account_data_set");
        if (TextUtils.isEmpty(accountName) && TextUtils.isEmpty(accountType)) {
            return false;
        }
        this.mAccountWithDataSet = new AccountWithDataSet(accountName, accountType, dataSet);
        return true;
    }

    private void fillAccountData() {
        AccountTypeManager accManager = AccountTypeManager.getInstance(this.mParentActivity);
        if (this.mAccountWithDataSet == null) {
            populateCurrentAccount();
        }
        AccountType accountType = accManager.getAccountTypeForAccount(this.mAccountWithDataSet);
        if (accountType != null) {
            CharSequence label = accountType.getDisplayLabel(this.mParentActivity);
            TextView accoutTypyView = (TextView) getView().findViewById(R.id.account_type);
            TextView accoutNameView = (TextView) getView().findViewById(R.id.account_name);
            LinearLayout llDownArrow = (LinearLayout) getView().findViewById(R.id.ll_down_arrow);
            setValueToUpper(accoutTypyView, label);
            if ("com.android.huawei.phone".equalsIgnoreCase(this.mAccountWithDataSet.type) || CommonUtilMethods.isSimAccount(this.mAccountWithDataSet.type)) {
                accoutNameView.setVisibility(8);
            } else {
                accoutNameView.setVisibility(0);
                setValueToUpper(accoutNameView, this.mAccountWithDataSet.name);
            }
            ContactsRequest lRequest = getContactsRequest();
            switch (lRequest.getActionCode()) {
                case 206:
                case 218:
                    if (getAdapter() != null) {
                        setValueToUpper(accoutTypyView, getString(R.string.peoples, new Object[]{Integer.valueOf(getAdapter().getCount())}));
                    } else {
                        setValueToUpper(accoutTypyView, getString(R.string.peoples, new Object[]{Integer.valueOf(0)}));
                    }
                    setValueToUpper(accoutNameView, label);
                    accoutNameView.setVisibility(0);
                    llDownArrow.setVisibility(0);
                    break;
                case 207:
                    accoutNameView.setVisibility(8);
                    if (getAdapter() == null) {
                        setValueToUpper(accoutTypyView, getString(R.string.peoples, new Object[]{Integer.valueOf(0)}));
                        break;
                    }
                    setValueToUpper(accoutTypyView, getString(R.string.peoples, new Object[]{Integer.valueOf(getAdapter().getCount())}));
                    break;
                default:
                    HwLog.w("RawContactMultiSelectFragment", "Don't handle this action code: " + lRequest.getActionCode());
                    break;
            }
        }
    }

    private void setValueToUpper(TextView tv, CharSequence label) {
        tv.setText(CommonUtilMethods.upPercase(label.toString()));
    }

    private void enableOrDisableListPopUp() {
        fillAccountData();
        switch (getContactsRequest().getActionCode()) {
            case 206:
            case 218:
                enableAccountListDialog();
                return;
            case 207:
                disableAccountListPopUp();
                return;
            default:
                return;
        }
    }

    protected ContactEntryListAdapter createListAdapter() {
        this.mAdapter = getListAdapter();
        this.mAdapter.setFilter(getFilterForRequest());
        this.mAdapter.setDisplayPhotos(true);
        startLoadingPhotos();
        return this.mAdapter;
    }

    protected void onItemClick(int position, long id) {
        if (HwLog.HWDBG) {
            HwLog.d("RawContactMultiSelectFragment", "onItemClick() is called!, position: [" + position + "]");
        }
        if (getListView().isItemChecked(getListView().getHeaderViewsCount() + position)) {
            this.mSelectedContacts.add(getAdapter().getSelectedContactUri(position));
            if (this.mMultiListener != null) {
                Uri lLookupUri = ((RawContactMultiselectionAdapter) getAdapter()).getSelectedContactLookupUri(position);
                if (lLookupUri != null) {
                    this.mMultiListener.onSelectionChanged(lLookupUri);
                    ((RawContactMultiselectionAdapter) getAdapter()).setSelectedContactUri(lLookupUri);
                }
            }
        } else {
            this.mSelectedContacts.remove(getAdapter().getSelectedContactUri(position));
            if (this.mMultiListener != null) {
                int count = this.mSelectedContacts.size();
                if (count > 1) {
                    Uri lookupUri = (Uri) this.mSelectedContacts.get(count - 1);
                    this.mMultiListener.onSelectionChanged(lookupUri);
                    ((RawContactMultiselectionAdapter) getAdapter()).setSelectedContactUri(lookupUri);
                }
            }
        }
        setActionBarTitle();
        getActivity().invalidateOptionsMenu();
    }

    private ContactListAdapter getListAdapter() {
        return new RawContactMultiselectionAdapter(getActivity());
    }

    private ContactListFilter getFilterForRequest() {
        switch (getContactsRequest().getActionCode()) {
            case 206:
            case 218:
                if (this.mAccountWithDataSet != null) {
                    return new ContactListFilter(-7, this.mAccountWithDataSet.type, this.mAccountWithDataSet.name, this.mAccountWithDataSet.dataSet, null, -1);
                }
                return ContactListFilter.createFromIntent(-7, getActivity().getIntent());
            case 207:
                String simAccountType = getActivity().getIntent().getStringExtra("extra_account_type");
                return new ContactListFilter(-8, simAccountType, SimFactoryManager.getAccountName(simAccountType), null, null);
            default:
                return ContactListFilter.restoreDefaultPreferences(SharePreferenceUtil.getDefaultSp_de(this.mParentActivity));
        }
    }

    private void startLoadingPhotos() {
        if (this.mAdapter instanceof RawContactMultiselectionAdapter) {
            ((RawContactMultiselectionAdapter) this.mAdapter).startFetchingPhotoIdsInBackground(this.mParentActivity.getContentResolver());
        }
    }

    private long[] getIdsFromSelectedList() {
        long[] idArray = new long[this.mSelectedContacts.size()];
        for (int i = 0; i < this.mSelectedContacts.size(); i++) {
            idArray[i] = Long.parseLong(((Uri) this.mSelectedContacts.get(i)).getLastPathSegment());
        }
        return idArray;
    }

    public void showSelectedItemCountInfo(int aCount) {
        if (aCount > 0) {
            this.mTextViewCount.setText(aCount + "");
        } else {
            this.mTextViewCount.setText("");
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getActivity().invalidateOptionsMenu();
        showSelectedItemCountInfo();
        super.onItemClick(parent, view, position, id);
    }

    private void showSelectedItemCountInfo() {
        int lCheckedItemCount = getListView().getCheckedItemCount();
        if (lCheckedItemCount > 0) {
            this.mTextViewCount.setText(lCheckedItemCount + "");
        } else {
            this.mTextViewCount.setText("");
        }
    }

    public void onCopyAccountChosen(AccountWithDataSet aAccount, Bundle aExtraArgs, String aCurrentAccountType) {
        if (aAccount != null) {
            this.mTargetAccount = aAccount;
            Intent intent = CopyContactService.createCopyContactsIntent(this.mContext, getIdsFromSelectedList(), this.mTargetAccount.name, this.mTargetAccount.type, this.mTargetAccount.dataSet, aCurrentAccountType);
            if (aExtraArgs != null) {
                if (aExtraArgs.getBoolean("export_to_sim", false)) {
                    intent.putExtra("export_to_sim", aExtraArgs.getBoolean("export_to_sim", false));
                    intent.putExtra("category", aExtraArgs.getInt("category"));
                }
            }
            if (getContactsRequest().getActionCode() == 207) {
                intent.putExtra("import_to_sim", true);
            }
            if ("com.android.huawei.sim".equals(this.mTargetAccount.type) || "com.android.huawei.secondsim".equals(this.mTargetAccount.type)) {
                boolean isFirstSimDeleteInProgress = SimFactoryManager.getSharedPreferences("SimInfoFile", 0).getBoolean("sim_delete_progress", false);
                boolean isSecondSimDeleteInProgress = SimFactoryManager.getSharedPreferences("SimInfoFile", 1).getBoolean("sim_delete_progress", false);
                Bundle bundle;
                if (("com.android.huawei.sim".equals(this.mTargetAccount.type) && isFirstSimDeleteInProgress) || ("com.android.huawei.secondsim".equals(this.mTargetAccount.type) && isSecondSimDeleteInProgress)) {
                    bundle = new Bundle();
                    bundle.putString("accountType", this.mTargetAccount.type);
                    this.mParentActivity.showDialog(R.id.simDeleteProgress, bundle);
                    return;
                }
                int freeSpaceInSIM = -1;
                SimConfig simConfig = SimFactoryManager.getSimConfig(this.mTargetAccount.type);
                if (simConfig != null) {
                    freeSpaceInSIM = simConfig.getAvailableFreeSpace();
                }
                if (freeSpaceInSIM == -1 || this.mSelectedContacts.size() <= freeSpaceInSIM) {
                    bundle = new Bundle();
                    bundle.putParcelable("intent", intent);
                    bundle.putString("accountType", this.mTargetAccount.type);
                    this.mParentActivity.showDialog(R.id.copySimInfo, bundle);
                    return;
                } else if (freeSpaceInSIM == SimFactoryManager.getSimConfig(this.mTargetAccount.type).getSimCapacity()) {
                    bundle = new Bundle();
                    bundle.putParcelable("intent", intent);
                    bundle.putInt("freeSpaceInSIM", freeSpaceInSIM);
                    bundle.putString("accountType", this.mTargetAccount.type);
                    this.mParentActivity.removeDialog(R.id.targetAccountChosenEmptySim);
                    this.mParentActivity.showDialog(R.id.targetAccountChosenEmptySim, bundle);
                    return;
                } else {
                    bundle = new Bundle();
                    bundle.putInt("freeSpaceInSIM", freeSpaceInSIM);
                    bundle.putString("accountType", this.mTargetAccount.type);
                    this.mParentActivity.removeDialog(R.id.targetAccountChosen);
                    this.mParentActivity.showDialog(R.id.targetAccountChosen, bundle);
                    return;
                }
            }
            if (CommonUtilMethods.isServiceRunning(this.mContext, "com.android.contacts.hap.copy.CopyContactService") && this.mParentActivity != null) {
                this.mParentActivity.finish();
            }
            Bundle lBundle = new Bundle();
            lBundle.putParcelable("intent", intent);
            Intent intent2 = new Intent(getContext(), CopyProgressBarActivity.class);
            intent2.putExtra("bundle", lBundle);
            startActivity(intent2);
            if (this.mParentActivity != null) {
                this.mParentActivity.finish();
            }
        }
    }

    public void onAccountSelectorCancelled() {
    }

    private void showTargetAccountDialog(int aResId) {
        ContactListFilter filter = getAdapter().getFilter();
        int i = aResId;
        HAPSelectAccountDialogFragment.show(getFragmentManager(), this, i, AccountListFilter.ACCOUNTS_WRITABLE_EXCLUDE_CURRENT, null, new AccountWithDataSet(filter.accountName, filter.accountType, filter.dataSet), "accounts_chooser_tag_rcm");
    }

    protected void configureListView(ListView aListView) {
        if (HwLog.HWDBG) {
            HwLog.d("RawContactMultiSelectFragment", "configureListView() is called!");
        }
        aListView.setOnFocusChangeListener(null);
        aListView.setOnTouchListener(null);
        aListView.setChoiceMode(2);
        CommonUtilMethods.addFootEmptyViewPortrait(aListView, this.mContext);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_operation:
                if (getListView().getCheckedItemCount() > 0) {
                    doOperation();
                    break;
                }
                break;
            case R.id.menu_action_selectall:
                if (this.mIsAllSelected) {
                    deselectAllItems();
                    this.mIsAllSelected = false;
                } else {
                    selectAllItems();
                    this.mIsAllSelected = true;
                }
                showSelectedItemCountInfo();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.menu_select_cancel:
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doOperation() {
        if (HwLog.HWDBG) {
            HwLog.d("RawContactMultiSelectFragment", "doOperation() is called!");
        }
        ContactsRequest lRequest = getContactsRequest();
        switch (lRequest.getActionCode()) {
            case 206:
                if (this.mSelectedContacts.size() > 0) {
                    showTargetAccountDialog(R.string.dialog_copy_contact_to);
                    break;
                }
                break;
            case 207:
                if (this.mSelectedContacts.size() > 0) {
                    StatisticalHelper.report(4017);
                    showTargetAccountDialog(R.string.Import_from_storage);
                    break;
                }
                break;
            case 218:
                if (this.mSelectedContacts.size() > 0) {
                    StatisticalHelper.report(4018);
                    Bundle bundle = this.mParentActivity.getIntent().getExtras();
                    if (bundle != null) {
                        bundle.putInt("category", 218);
                        int slotId = -1;
                        if (bundle.getBoolean("EXCLUDE_SIM", false)) {
                            slotId = -1;
                        } else if (bundle.getBoolean("EXCLUDE_SIM1", false)) {
                            slotId = 0;
                        } else if (bundle.getBoolean("EXCLUDE_SIM2", false)) {
                            slotId = 1;
                        }
                        onCopyAccountChosen(new AccountWithDataSet(SimFactoryManager.getAccountName(slotId), SimFactoryManager.getAccountType(slotId), null), bundle, null);
                        break;
                    }
                    return;
                }
                break;
            default:
                if (HwLog.HWDBG) {
                    HwLog.d("RawContactMultiSelectFragment", "Don't handle this action code: " + lRequest.getActionCode());
                    break;
                }
                break;
        }
    }

    private void selectAllItems() {
        for (int i = getListView().getHeaderViewsCount(); i < getListView().getCount() - getListView().getFooterViewsCount(); i++) {
            getListView().setItemChecked(i, true);
        }
        synchronized (this.mAllMembers) {
            this.mSelectedContacts.clear();
            this.mSelectedContacts.addAll(this.mAllMembers);
        }
        setActionBarTitle();
    }

    private void deselectAllItems() {
        for (int i = getListView().getHeaderViewsCount(); i < getListView().getCount() - getListView().getFooterViewsCount(); i++) {
            getListView().setItemChecked(i, false);
        }
        this.mSelectedContacts.clear();
        setActionBarTitle();
    }

    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem lOperation = menu.findItem(R.id.menu_action_operation);
        MenuItem lSelectAll = menu.findItem(R.id.menu_action_selectall);
        if (this.mAdapter.getCount() == 0) {
            lOperation.setVisible(false);
            lSelectAll.setVisible(false);
        } else {
            lOperation.setVisible(true);
            lSelectAll.setVisible(true);
            if (this.mSelectedContacts.size() > 0) {
                lOperation.setEnabled(true);
            }
        }
        if (getAdapter().getCount() == this.mSelectedContacts.size()) {
            this.mIsAllSelected = true;
        } else {
            this.mIsAllSelected = false;
        }
        if (this.mIsAllSelected) {
            lSelectAll.setTitle(getString(R.string.menu_select_none));
            lSelectAll.setIcon(this.mSelectNoneDrawable);
        } else {
            lSelectAll.setTitle(getString(R.string.contact_menu_select_all));
            lSelectAll.setIcon(this.mSelectAllDrawable);
        }
        lSelectAll.setChecked(this.mIsAllSelected);
        fillAccountData();
        ContactsRequest lRequest = getContactsRequest();
        switch (lRequest.getActionCode()) {
            case 206:
                lOperation.setTitle(getText(R.string.copy_to_label).toString());
                lOperation.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_icon_copy_light, R.drawable.contact_icon_copy));
                break;
            case 207:
                lOperation.setTitle(getText(R.string.title_prefs_cat_import).toString());
                lOperation.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_icon_import_in_light, R.drawable.contact_icon_import_in));
                break;
            case 218:
                lOperation.setTitle(getText(R.string.export_label).toString());
                lOperation.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_icon_export_out_light, R.drawable.contact_icon_export_out));
                break;
            default:
                HwLog.w("RawContactMultiSelectFragment", "Don't handle this action code: " + lRequest.getActionCode());
                break;
        }
        ViewUtil.setMenuItemStateListIcon(getActivity(), lOperation);
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        int[] iArr = null;
        Set<Uri> selectedContacts = new HashSet();
        selectedContacts.addAll(this.mSelectedContacts);
        if (this.mAccountChanged || this.mAllMembers.isEmpty()) {
            iArr = ((ContactMultiSelectionActivity) this.mParentActivity).getSelectionPositions(data, 6, selectedContacts, RawContacts.CONTENT_URI);
            prepareList(data);
        }
        super.onPartitionLoaded(partitionIndex, data);
        if (data.getCount() == 0) {
            setEmptyText(R.string.noContacts);
            showSelectedItemCountInfo(0);
            this.mSelectedContacts.clear();
            return;
        }
        getEmptyTextView().setVisibility(4);
        getListView().setVisibility(0);
        if (this.mAccountChanged) {
            this.mAccountChanged = false;
            clearSelectedItemInListview();
            showSelectedItemCountInfo();
            this.mSelectedContacts.clear();
            return;
        }
        ((ContactMultiSelectionActivity) getActivity()).setSelectedItemInListview(iArr);
        showSelectedItemCountInfo();
    }

    private void prepareList(Cursor mainCursor) {
        synchronized (this.mAllMembers) {
            this.mAllMembers.clear();
            if (mainCursor.moveToFirst()) {
                do {
                    this.mAllMembers.add(Uri.withAppendedPath(RawContacts.CONTENT_URI, mainCursor.getString(6)));
                } while (mainCursor.moveToNext());
            }
        }
    }

    public void clearSelectedItemInListview() {
        ListView lListView = getListView();
        for (int i = 0; i < lListView.getCount(); i++) {
            lListView.setItemChecked(i, false);
        }
    }

    protected void setEmptyText(int resourceId) {
        CharSequence string;
        TextView emptyView = getEmptyTextView();
        emptyView.setVisibility(0);
        if (isSearchMode()) {
            string = getString(R.string.contact_list_FoundAllContactsZero);
        } else {
            string = getString(resourceId);
        }
        emptyView.setText(string);
        getListView().setVisibility(4);
    }

    protected void showCount(int partitionIndex, Cursor data) {
        if (this.mMultiListener != null && data != null && data.getCount() > 0) {
            data.moveToFirst();
            this.mMultiListener.onSelectionChanged(Uri.withAppendedPath(Contacts.CONTENT_URI, data.getString(data.getColumnIndex("contact_id"))));
        }
    }

    public void setListener(MultiSelectListener aListener) {
        this.mMultiListener = aListener;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMultiListener = null;
        if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
            this.mAlertDialog = null;
        }
    }

    public void handleEmptyList(int aCount) {
        setEmptyText(R.string.noContacts);
        super.handleEmptyList(aCount);
    }

    protected void checkAlphaScrollerAndUpdateViews() {
        super.checkAlphaScrollerAndUpdateViews();
        if (this.mAccountContainter != null) {
            LayoutParams lParams = this.mAccountContainter.getLayoutParams();
            if (lParams != null && (lParams instanceof RelativeLayout.LayoutParams)) {
                int i;
                RelativeLayout.LayoutParams lLP = (RelativeLayout.LayoutParams) lParams;
                Resources resources = this.mContext.getResources();
                if (isAlphaScrollerVisible()) {
                    i = R.dimen.drop_down_account_layout_margin_right_default;
                } else {
                    i = R.dimen.drop_down_account_layout_margin_left_right;
                }
                lLP.rightMargin = resources.getDimensionPixelOffset(i);
                this.mAccountContainter.setLayoutParams(lLP);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String str = null;
        if (1091 == requestCode) {
            if (-1 == resultCode) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    AccountWithDataSet account = (AccountWithDataSet) bundle.get("account");
                    Bundle extrasBundle = (Bundle) bundle.getParcelable("extra_args");
                    if (account != null) {
                        ContactListFilter filter = getAdapter().getFilter();
                        if (filter != null) {
                            str = filter.accountType;
                        }
                        onCopyAccountChosen(account, extrasBundle, str);
                    } else {
                        return;
                    }
                }
                return;
            }
            onAccountSelectorCancelled();
        }
    }

    private void setActionBarTitle() {
        this.mTitle.setCustomTitle(CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.mSelectedContacts.size()), this.mSelectedContacts.size());
    }
}
