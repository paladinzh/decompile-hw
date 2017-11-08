package com.android.contacts.list;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.contacts.ContactsActivity;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.AccountLoadListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class AccountFilterActivity extends ContactsActivity implements OnItemClickListener, AccountLoadListener, OnCheckedChangeListener {
    private static final String TAG = AccountFilterActivity.class.getSimpleName();
    private static List<ContactsVisibilityListener> mListeners = new ArrayList();
    private ContactListFilter mCurrentFilter;
    private View mEndDivider;
    private ListView mListView;
    private View mLoadingView;
    private SharedPreferences mPreference;
    private View mSimContactsLayout;
    private View mSimDivider;
    private Switch mSimSwitch;
    private TextView mSimTextView;
    private Switch mSimpleDisplayModeSwitch;
    private TextView mTitle;

    public interface ContactsVisibilityListener {
        void onContactVisibilityChanged(boolean z);
    }

    private static class FilterListAdapter extends BaseAdapter {
        private final AccountTypeManager mAccountTypes;
        private final ContactListFilter mCurrentFilter;
        private final List<ContactListFilter> mFilters;
        private final LayoutInflater mLayoutInflater;

        public FilterListAdapter(Context context, List<ContactListFilter> filters, ContactListFilter current) {
            this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mFilters = filters;
            this.mCurrentFilter = current;
            this.mAccountTypes = AccountTypeManager.getInstance(context);
        }

        public int getCount() {
            return this.mFilters.size();
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public ContactListFilter getItem(int position) {
            return (ContactListFilter) this.mFilters.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View outerView;
            ContactListFilterView view;
            boolean z = true;
            if (convertView != null) {
                outerView = convertView;
                view = (ContactListFilterView) convertView.findViewById(R.id.inner_view);
            } else {
                outerView = this.mLayoutInflater.inflate(R.layout.contact_list_filter_item, parent, false);
                view = (ContactListFilterView) outerView.findViewById(R.id.inner_view);
            }
            if (this.mFilters.size() != 1) {
                z = false;
            }
            view.setSingleAccount(z);
            ContactListFilter filter = (ContactListFilter) this.mFilters.get(position);
            view.setContactListFilter(filter);
            view.setActivated(filter.equals(this.mCurrentFilter));
            view.bindView(this.mAccountTypes);
            view.setTag(filter);
            outerView.setTag(filter);
            view.setClickable(false);
            return outerView;
        }
    }

    private static class FilterLoader extends AsyncTaskLoader<List<ContactListFilter>> {
        private Context mContext;

        public FilterLoader(Context context) {
            super(context);
            this.mContext = context;
        }

        public List<ContactListFilter> loadInBackground() {
            return AccountFilterActivity.loadAccountFilters(this.mContext);
        }

        protected void onStartLoading() {
            forceLoad();
        }

        protected void onStopLoading() {
            cancelLoad();
        }

        protected void onReset() {
            onStopLoading();
        }
    }

    private class MyLoaderCallbacks implements LoaderCallbacks<List<ContactListFilter>> {
        private MyLoaderCallbacks() {
        }

        public Loader<List<ContactListFilter>> onCreateLoader(int id, Bundle args) {
            return new FilterLoader(AccountFilterActivity.this);
        }

        public void onLoadFinished(Loader<List<ContactListFilter>> loader, List<ContactListFilter> data) {
            if (data == null) {
                HwLog.e(AccountFilterActivity.TAG, "Failed to load filters");
                return;
            }
            AccountFilterActivity.this.mListView.setVisibility(0);
            AccountFilterActivity.this.mEndDivider.setVisibility(8);
            AccountFilterActivity.this.mLoadingView.setVisibility(8);
            AccountFilterActivity.this.mListView.setAdapter(new FilterListAdapter(AccountFilterActivity.this, data, AccountFilterActivity.this.mCurrentFilter));
            AccountFilterActivity.this.updateSIMSwitch();
        }

        public void onLoaderReset(Loader<List<ContactListFilter>> loader) {
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        if (ActivityManager.isUserAMonkey()) {
            finish();
            return;
        }
        setTheme(R.style.ContactListFilterTheme);
        setContentView(R.layout.contact_list_filter);
        this.mTitle = (TextView) findViewById(R.id.display_accounts_label);
        this.mTitle.setText(CommonUtilMethods.upPercase(getString(R.string.title_display_accounts)));
        this.mListView = (ListView) findViewById(16908298);
        if (this.mListView != null) {
            this.mListView.setFastScrollEnabled(true);
        }
        this.mEndDivider = findViewById(R.id.display_accounts_end_divider);
        this.mLoadingView = findViewById(R.id.loadingcontacts);
        this.mPreference = SharePreferenceUtil.getDefaultSp_de(this);
        handleContactsWithoutNumbers();
        this.mSimpleDisplayModeSwitch = (Switch) findViewById(R.id.simple_mode_switch);
        this.mSimpleDisplayModeSwitch.setOnCheckedChangeListener(this);
        this.mSimpleDisplayModeSwitch.setChecked(this.mPreference.getBoolean("preference_display_with_simple_mode", false));
        this.mSimContactsLayout = findViewById(R.id.sim_contacts_layout);
        this.mSimTextView = (TextView) findViewById(R.id.show_sim_contacts);
        this.mSimDivider = findViewById(R.id.sim_divider);
        this.mSimSwitch = (Switch) findViewById(R.id.sim_switch);
        this.mSimSwitch.setOnCheckedChangeListener(this);
        this.mListView.setOnItemClickListener(this);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        this.mCurrentFilter = (ContactListFilter) getIntent().getParcelableExtra("currentFilter");
        getLoaderManager().initLoader(0, new Bundle(), new MyLoaderCallbacks());
        AccountTypeManager.getInstance(getApplicationContext()).setAccountLoadListener(this);
        this.mSimContactsLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (AccountFilterActivity.this.mPreference.getBoolean("preference_show_sim_contacts", true)) {
                    AccountFilterActivity.this.mSimSwitch.setChecked(false);
                    StatisticalHelper.sendReport(4026, 1);
                    return;
                }
                AccountFilterActivity.this.mSimSwitch.setChecked(true);
                StatisticalHelper.sendReport(4026, 0);
            }
        });
        findViewById(R.id.display_simple_mode).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (AccountFilterActivity.this.mPreference.getBoolean("preference_display_with_simple_mode", false)) {
                    AccountFilterActivity.this.mSimpleDisplayModeSwitch.setChecked(false);
                    EmuiFeatureManager.setSimpleDisplayMode(false);
                    StatisticalHelper.sendReport(4019, 0);
                    return;
                }
                AccountFilterActivity.this.mSimpleDisplayModeSwitch.setChecked(true);
                EmuiFeatureManager.setSimpleDisplayMode(true);
                StatisticalHelper.sendReport(4019, 1);
            }
        });
    }

    protected void onResume() {
        updateSIMSwitch();
        super.onResume();
        this.mCurrentFilter = ContactListFilterController.getInstance(this).getFilter();
    }

    protected void onDestroy() {
        super.onDestroy();
        AccountTypeManager.getInstance(getApplicationContext()).unregisterAccountLoadListener();
    }

    private static List<ContactListFilter> loadAccountFilters(Context context) {
        ArrayList<ContactListFilter> result = Lists.newArrayList();
        ArrayList<ContactListFilter> accountFilters = Lists.newArrayList();
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(context);
        SharedPreferences preference = SharePreferenceUtil.getDefaultSp_de(context);
        boolean isSimEnabled = preference.getBoolean("preference_show_sim_contacts", true);
        boolean isOnlyPhoneNumber = preference.getBoolean("preference_contacts_only_phonenumber", false);
        ContactListFilter currentFilter = ContactListFilterController.getInstance(context).getFilter();
        List<AccountWithDataSet> writableAccounts = accountTypes.getAccountsExcludeBothSim(true);
        int totaRawlContactsCount = 0;
        for (AccountWithDataSet account : accountTypes.getAccounts(false)) {
            AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
            if ((accountType == null || !accountType.isExtension() || account.hasData(context)) && (isSimEnabled || !CommonUtilMethods.isSimAccount(account.type))) {
                int contactsCount;
                Drawable displayIcon = accountType != null ? accountType.getDisplayIcon(context) : null;
                int rawContactsCount = getRawContactCountBasedOnAccountType(context, account, isOnlyPhoneNumber);
                totaRawlContactsCount = getTotaRawlContactsCount(totaRawlContactsCount, rawContactsCount);
                if (currentFilter.filterType == 0 && writableAccounts.contains(account) && account.type != null && account.type.equals(currentFilter.accountType) && account.name != null && account.name.equals(currentFilter.accountName)) {
                    contactsCount = getContactCountBasedOnAccountType(context, account, isOnlyPhoneNumber);
                } else if (CommonUtilMethods.isSimAccount(account.type)) {
                    contactsCount = SimFactoryManager.getSimConfig(account.type).getSimCapacity();
                } else {
                    contactsCount = -1;
                }
                accountFilters.add(ContactListFilter.createAccountFilter(account.type, account.name, account.dataSet, displayIcon, rawContactsCount, contactsCount, -1, -1));
            }
        }
        int totalContactsCount = -1;
        if (currentFilter.filterType == -2) {
            totalContactsCount = getTotalContactCount(context, isSimEnabled, isOnlyPhoneNumber);
        }
        result.add(ContactListFilter.createFilterWithType(-2, totaRawlContactsCount, totalContactsCount));
        if (accountFilters.size() >= 1) {
            result.addAll(accountFilters);
            if (!CommonUtilMethods.isSimplifiedModeEnabled()) {
                result.add(ContactListFilter.createFilterWithType(-3));
            }
        }
        return result;
    }

    private static int getTotaRawlContactsCount(int originCount, int incrementCount) {
        if (incrementCount >= 0) {
            return originCount + incrementCount;
        }
        HwLog.i(TAG, "incrementCount = " + incrementCount + " not legal");
        return originCount;
    }

    private static int getRawContactCountBasedOnAccountType(Context context, AccountWithDataSet aAccountDetails, boolean isOnlyPhoneNumber) {
        String AND = " AND ";
        StringBuffer lWhereClause = new StringBuffer();
        lWhereClause.append("account_name=?");
        lWhereClause.append(AND + "account_type" + "='" + aAccountDetails.type + "'");
        lWhereClause.append(AND + "data_set");
        if (aAccountDetails.dataSet != null) {
            lWhereClause.append("='" + aAccountDetails.dataSet + "'");
        } else {
            lWhereClause.append(" IS NULL");
        }
        if (!CommonUtilMethods.isSimAccount(aAccountDetails.type)) {
            lWhereClause.append(AND + "deleted" + "= 0");
        }
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(context)) {
            lWhereClause.append(AND + "contact_id IN default_directory");
        }
        if (isOnlyPhoneNumber) {
            lWhereClause.append(AND + "contact_id" + " IN (" + "SELECT " + "_id" + " FROM view_contacts" + " WHERE " + "has_phone_number" + "=1");
            lWhereClause.append(")");
        }
        if (!CommonUtilMethods.isPrivacyModeEnabled(context)) {
            lWhereClause.append(" AND contact_id IN (SELECT _id FROM view_contacts WHERE is_private = 0 ) AND is_private = 0");
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"contact_id"}, lWhereClause.toString(), new String[]{aAccountDetails.name}, null);
            int lContactsUsed = 0;
            if (cursor != null) {
                lContactsUsed = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
            return lContactsUsed;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int getContactCountBasedOnAccountType(Context context, AccountWithDataSet aAccountDetails, boolean isOnlyPhoneNumber) {
        String str = null;
        String AND = " AND ";
        StringBuffer lWhereClause = new StringBuffer();
        boolean hasData = false;
        if (isOnlyPhoneNumber) {
            lWhereClause.append("has_phone_number=1");
            hasData = true;
        }
        if (hasData) {
            lWhereClause.append(AND);
        }
        StringBuffer selection = new StringBuffer();
        selection.append("account_name=?");
        selection.append(AND + "account_type" + "='" + aAccountDetails.type + "'");
        selection.append(AND + "data_set");
        if (aAccountDetails.dataSet != null) {
            selection.append("='" + aAccountDetails.dataSet + "'");
        } else {
            selection.append(" IS NULL");
        }
        selection.append(AND + "deleted" + "=0");
        if (!CommonUtilMethods.isPrivacyModeEnabled(context)) {
            selection.append(" AND is_private = 0");
        }
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(context)) {
            selection.append(AND + "contact_id IN default_directory");
        }
        lWhereClause.append("_id IN (SELECT contact_id FROM view_raw_contacts WHERE " + selection.toString());
        lWhereClause.append(")");
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Contacts.CONTENT_URI;
            String[] strArr = new String[]{"_id"};
            if (!TextUtils.isEmpty(lWhereClause)) {
                str = lWhereClause.toString();
            }
            cursor = contentResolver.query(uri, strArr, str, new String[]{aAccountDetails.name}, null);
            int totalCount = 0;
            if (cursor != null) {
                totalCount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
            return totalCount;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int getTotalContactCount(Context context, boolean isSimEnabled, boolean isOnlyPhoneNumber) {
        String str = null;
        String AND = " AND ";
        StringBuffer lWhereClause = new StringBuffer();
        boolean hasData = false;
        if (isOnlyPhoneNumber) {
            lWhereClause.append("has_phone_number=1");
            hasData = true;
        }
        if (hasData) {
            lWhereClause.append(AND);
        }
        StringBuffer selection = new StringBuffer();
        selection.append("deleted=0");
        if (!isSimEnabled) {
            if (SimFactoryManager.isDualSim()) {
                selection.append(AND + "account_type" + "!='" + "com.android.huawei.sim" + "'");
                selection.append(AND + "account_type" + "!='" + "com.android.huawei.secondsim" + "'");
            } else {
                selection.append(AND + "account_type" + "!='" + "com.android.huawei.sim" + "'");
            }
        }
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(context)) {
            selection.append(AND + "contact_id IN default_directory");
        }
        if (!CommonUtilMethods.isPrivacyModeEnabled(context)) {
            selection.append(" AND is_private = 0");
        }
        lWhereClause.append("_id IN (SELECT contact_id FROM view_raw_contacts WHERE " + selection.toString());
        lWhereClause.append(")");
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Contacts.CONTENT_URI;
            String[] strArr = new String[]{"_id"};
            if (!TextUtils.isEmpty(lWhereClause)) {
                str = lWhereClause.toString();
            }
            cursor = contentResolver.query(uri, strArr, str, null, null);
            int totalCount = 0;
            if (cursor != null) {
                totalCount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
            return totalCount;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ContactListFilter filter = (ContactListFilter) view.getTag();
        if (filter != null) {
            if (filter.filterType == -3) {
                StatisticalHelper.sendReport(4027, 3);
            } else if (filter.filterType == -2) {
                StatisticalHelper.sendReport(4027, 0);
            } else if (filter.accountName != null) {
                if (filter.accountName.equals(SimFactoryManager.getAccountName("com.android.huawei.sim"))) {
                    StatisticalHelper.sendReport(4027, 2);
                } else if (filter.accountName.equals(SimFactoryManager.getAccountName("com.android.huawei.secondsim"))) {
                    StatisticalHelper.sendReport(4027, 2);
                } else if (!filter.accountName.equals("Phone")) {
                    StatisticalHelper.sendReport(4027, 3);
                } else if (1 == HiCloudUtil.getHicloudAccountState(this)) {
                    StatisticalHelper.sendReport(4027, 1);
                } else {
                    StatisticalHelper.sendReport(4027, 3);
                }
            }
            if (filter.filterType == -3) {
                startActivityForResult(new Intent(this, CustomContactListFilterActivity.class), 0);
                StatisticalHelper.report(4051);
            } else {
                Intent intent = new Intent();
                intent.putExtra("contactListFilter", filter);
                setResult(-1, intent);
                finish();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case 0:
                    Intent intent = new Intent();
                    intent.putExtra("contactListFilter", ContactListFilter.createFilterWithType(-3));
                    setResult(-1, intent);
                    finish();
                    break;
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onAccountsLoadCompleted() {
        HwLog.i(TAG, "On accounts loaded complete");
        runOnUiThread(new Runnable() {
            public void run() {
                if (!AccountFilterActivity.this.isFinishing()) {
                    AccountFilterActivity.this.getLoaderManager().restartLoader(0, new Bundle(), new MyLoaderCallbacks());
                }
            }
        });
    }

    private void modifyContactsToDisplay(boolean aIsChanged) {
        if (aIsChanged) {
            for (ContactsVisibilityListener listener : mListeners) {
                listener.onContactVisibilityChanged(aIsChanged);
            }
        }
    }

    public static void addContactsVisibilityListener(ContactsVisibilityListener aListener) {
        mListeners.add(aListener);
    }

    public static void removeContactsVisibilityListener(ContactsVisibilityListener aListener) {
        mListeners.remove(aListener);
    }

    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        boolean isChanged = true;
        int id = buttonView.getId();
        Editor editor;
        if (id == R.id.simple_mode_switch) {
            int i;
            editor = this.mPreference.edit();
            editor.putBoolean("preference_display_with_simple_mode", isChecked);
            editor.commit();
            if (isChecked) {
                i = 78;
            } else {
                i = 79;
            }
            ExceptionCapture.reportScene(i);
        } else if (id == R.id.sim_switch) {
            boolean existingPreferencevalue = this.mPreference.getBoolean("preference_show_sim_contacts", true);
            editor = this.mPreference.edit();
            editor.putBoolean("preference_show_sim_contacts", isChecked);
            editor.commit();
            if (isChecked == existingPreferencevalue) {
                isChanged = false;
            }
            modifyContactsToDisplay(isChanged);
            if (isChanged) {
                new Thread(new Runnable() {
                    public void run() {
                        ContentValues values = new ContentValues();
                        Uri uri = Groups.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
                        values.put("group_visible", Boolean.valueOf(isChecked));
                        AccountFilterActivity.this.getApplicationContext().getContentResolver().update(uri, values, "account_type= ? OR account_type=?", new String[]{"com.android.huawei.sim", "com.android.huawei.secondsim"});
                    }
                }).start();
                if (CommonUtilMethods.isSimAccount(this.mCurrentFilter.accountType)) {
                    this.mCurrentFilter = ContactListFilter.createFilterWithType(-2);
                    getIntent().putExtra("currentFilter", this.mCurrentFilter);
                    ContactListFilterController.getInstance(this).setContactListFilter(this.mCurrentFilter, false);
                }
                getLoaderManager().restartLoader(0, new Bundle(), new MyLoaderCallbacks());
            }
        } else if (EmuiFeatureManager.isEnableContactsWithNumberOnlyFeature() && id == R.id.contacts_without_phone_numbers_switch) {
            SharedPreferences sharPre = SharePreferenceUtil.getDefaultSp_de(this);
            boolean existingCheckPreferenceValue = sharPre.getBoolean("preference_contacts_only_phonenumber", false);
            editor = sharPre.edit();
            editor.putBoolean("preference_contacts_only_phonenumber", isChecked);
            editor.commit();
            if (isChecked == existingCheckPreferenceValue) {
                isChanged = false;
            }
            modifyContactsToDisplay(isChanged);
            if (isChecked) {
                StatisticalHelper.report(4007);
            }
            if (isChanged) {
                getLoaderManager().restartLoader(0, new Bundle(), new MyLoaderCallbacks());
            }
        }
    }

    public void updateSIMSwitch() {
        if (MultiUsersUtils.isCurrentUserGuest()) {
            this.mSimDivider.setVisibility(8);
            this.mSimContactsLayout.setVisibility(8);
            return;
        }
        if (SimFactoryManager.isDualSim()) {
            boolean z;
            if (SimUtility.isSimStateLoaded(0, this)) {
                z = true;
            } else {
                z = SimUtility.isSimStateLoaded(1, this);
            }
            if (z) {
                this.mSimDivider.setVisibility(0);
                this.mSimContactsLayout.setVisibility(0);
            } else {
                this.mSimDivider.setVisibility(8);
                this.mSimContactsLayout.setVisibility(8);
                return;
            }
        } else if (SimUtility.isSimStateLoaded(-1, this)) {
            this.mSimDivider.setVisibility(0);
            this.mSimContactsLayout.setVisibility(0);
        } else {
            this.mSimDivider.setVisibility(8);
            this.mSimContactsLayout.setVisibility(8);
            return;
        }
        this.mSimSwitch.setChecked(this.mPreference.getBoolean("preference_show_sim_contacts", true));
    }

    public void onBackPressed() {
        if (isSafeToCommitTransactions()) {
            saveFilterPrefereceChange(false);
            super.onBackPressed();
        }
    }

    private void saveFilterPrefereceChange(boolean aSendBroadCast) {
        if (this.mCurrentFilter != null) {
            ContactListFilter oldFilter = ContactListFilter.restoreDefaultPreferences(this.mPreference);
            if (!(this.mCurrentFilter.filterType == -3 || this.mCurrentFilter.equals(oldFilter))) {
                ContactListFilter.storeToPreferences(this.mPreference, this.mCurrentFilter);
                Intent intent = new Intent();
                intent.putExtra("contactListFilter", this.mCurrentFilter);
                setResult(-1, intent);
                if (aSendBroadCast) {
                    sendBroadcast(new Intent("com.huawei.android.action.SIM_ACCOUNT_DISABLE"));
                }
            }
        }
    }

    protected void onUserLeaveHint() {
        saveFilterPrefereceChange(true);
    }

    private void handleContactsWithoutNumbers() {
        if (EmuiFeatureManager.isEnableContactsWithNumberOnlyFeature()) {
            final Switch phoneNumbersSwitch = (Switch) findViewById(R.id.contacts_without_phone_numbers_switch);
            LinearLayout contactsWithoutPhoneNumbersLayout = (LinearLayout) findViewById(R.id.contact_without_phone_numbers_layout);
            contactsWithoutPhoneNumbersLayout.setVisibility(0);
            phoneNumbersSwitch.setOnCheckedChangeListener(this);
            phoneNumbersSwitch.setChecked(this.mPreference.getBoolean("preference_contacts_only_phonenumber", false));
            contactsWithoutPhoneNumbersLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    boolean z = false;
                    Switch switchR = phoneNumbersSwitch;
                    if (!AccountFilterActivity.this.mPreference.getBoolean("preference_contacts_only_phonenumber", false)) {
                        z = true;
                    }
                    switchR.setChecked(z);
                }
            });
        }
    }
}
