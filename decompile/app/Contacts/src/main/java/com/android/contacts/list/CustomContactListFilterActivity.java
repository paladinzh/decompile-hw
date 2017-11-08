package com.android.contacts.list;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.ContactsActivity;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.compatibility.ExpandableListViewEx;
import com.android.contacts.hap.AccountLoadListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.EmptyService;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LocalizedNameResolver;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.WeakAsyncTask;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class CustomContactListFilterActivity extends ContactsActivity implements OnChildClickListener, LoaderCallbacks<AccountSet>, AccountLoadListener {
    private static Comparator<GroupDelta> sIdComparator = new Comparator<GroupDelta>() {
        public int compare(GroupDelta object1, GroupDelta object2) {
            Long id1 = object1.getId();
            Long id2 = object2.getId();
            if (id1 == null && id2 == null) {
                return 0;
            }
            if (id1 == null) {
                return -1;
            }
            if (id2 == null) {
                return 1;
            }
            if (id1.longValue() < id2.longValue()) {
                return -1;
            }
            return id1.longValue() > id2.longValue() ? 1 : 0;
        }
    };
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295 || viewId == R.id.icon1) {
                CustomContactListFilterActivity.this.finish();
            } else if (viewId == 16908296 || viewId == R.id.icon2) {
                CustomContactListFilterActivity.this.doSaveAction();
            }
        }
    };
    private DisplayAdapter mAdapter;
    private ActionBarCustom mCustActionBar;
    private ExpandableListViewEx mList;
    private SharedPreferences mPrefs;

    protected static class AccountDisplay {
        public final String mDataSet;
        public final String mName;
        public ArrayList<GroupDelta> mSyncedGroups = Lists.newArrayList();
        public final String mType;
        public GroupDelta mUngrouped;
        public ArrayList<GroupDelta> mUnsyncedGroups = Lists.newArrayList();

        public AccountDisplay(ContentResolver resolver, String accountName, String accountType, String dataSet) {
            this.mName = accountName;
            this.mType = accountType;
            this.mDataSet = dataSet;
        }

        private void addGroup(GroupDelta group) {
            if (group.getShouldSync()) {
                this.mSyncedGroups.add(group);
            } else {
                this.mUnsyncedGroups.add(group);
            }
        }

        private void addGroupAtTop(GroupDelta group) {
            if (group.getShouldSync()) {
                this.mSyncedGroups.add(0, group);
            } else {
                this.mUnsyncedGroups.add(0, group);
            }
        }

        public void setShouldSync(boolean shouldSync) {
            Iterator<GroupDelta> oppositeChildren = shouldSync ? this.mUnsyncedGroups.iterator() : this.mSyncedGroups.iterator();
            while (oppositeChildren.hasNext()) {
                setShouldSync((GroupDelta) oppositeChildren.next(), shouldSync, false);
                oppositeChildren.remove();
            }
        }

        public void setShouldSync(GroupDelta child, boolean shouldSync) {
            setShouldSync(child, shouldSync, true);
        }

        public void setShouldSync(GroupDelta child, boolean shouldSync, boolean attemptRemove) {
            child.putShouldSync(shouldSync);
            if (shouldSync) {
                if (attemptRemove) {
                    this.mUnsyncedGroups.remove(child);
                }
                this.mSyncedGroups.add(child);
                Collections.sort(this.mSyncedGroups, CustomContactListFilterActivity.sIdComparator);
                return;
            }
            if (attemptRemove) {
                this.mSyncedGroups.remove(child);
            }
            this.mUnsyncedGroups.add(child);
        }

        public void buildDiff(ArrayList<ContentProviderOperation> diff) {
            for (GroupDelta group : this.mSyncedGroups) {
                ContentProviderOperation oper = group.buildDiff();
                if (oper != null) {
                    diff.add(oper);
                }
            }
            for (GroupDelta group2 : this.mUnsyncedGroups) {
                oper = group2.buildDiff();
                if (oper != null) {
                    diff.add(oper);
                }
            }
        }
    }

    protected static class AccountSet extends ArrayList<AccountDisplay> {
        protected AccountSet() {
        }

        public ArrayList<ContentProviderOperation> buildDiff() {
            ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
            for (AccountDisplay account : this) {
                account.buildDiff(diff);
            }
            return diff;
        }
    }

    public static class CustomFilterConfigurationLoader extends AsyncTaskLoader<AccountSet> {
        private AccountSet mAccountSet;

        public CustomFilterConfigurationLoader(Context context) {
            super(context);
        }

        public AccountSet loadInBackground() {
            Context context = getContext();
            AccountTypeManager accountTypes = AccountTypeManager.getInstance(context);
            ContentResolver resolver = context.getContentResolver();
            boolean isSimEnabled = SharePreferenceUtil.getDefaultSp_de(context).getBoolean("preference_show_sim_contacts", true);
            AccountSet accounts = new AccountSet();
            int lGroupPosition = -1;
            for (AccountWithDataSet account : accountTypes.getAccounts(false)) {
                if (account != null && ((!accountTypes.getAccountTypeForAccount(account).isExtension() || account.hasData(context)) && (isSimEnabled || !CommonUtilMethods.isSimAccount(account.type)))) {
                    AccountDisplay accountDisplay;
                    if (EmuiFeatureManager.isPrivacyFeatureEnabled() && "com.android.huawei.phone".equals(account.type)) {
                        accountDisplay = new PhoneAccountDisplay(resolver, account.name, account.type, account.dataSet);
                    } else {
                        accountDisplay = new AccountDisplay(resolver, account.name, account.type, account.dataSet);
                    }
                    lGroupPosition++;
                    Builder groupsUri = Groups.CONTENT_URI.buildUpon().appendQueryParameter("account_name", account.name).appendQueryParameter("account_type", account.type);
                    if (account.dataSet != null) {
                        groupsUri.appendQueryParameter("data_set", account.dataSet).build();
                    }
                    EntityIterator iterator = Groups.newEntityIterator(resolver.query(groupsUri.build(), null, "deleted" + "!=1", null, null));
                    boolean hasGroups = false;
                    while (iterator.hasNext()) {
                        try {
                            ContentValues values = ((Entity) iterator.next()).getEntityValues();
                            GroupDelta group = GroupDelta.fromBefore(values);
                            if (!CommonUtilMethods.isSimplifiedModeEnabled() || !"com.android.huawei.phone".equalsIgnoreCase(group.getAccountType())) {
                                if (accountDisplay instanceof PhoneAccountDisplay) {
                                    if (context.getString(R.string.private_group_sync1).equals(values.getAsString("sync1"))) {
                                        ((PhoneAccountDisplay) accountDisplay).mPrivateGroupId = values.getAsInteger("_id").intValue();
                                    }
                                }
                                group.mGroupPosition = lGroupPosition;
                                accountDisplay.addGroup(group);
                                hasGroups = true;
                            }
                        } catch (Throwable th) {
                            iterator.close();
                        }
                    }
                    accountDisplay.mUngrouped = GroupDelta.fromSettings(resolver, account.name, account.type, account.dataSet, hasGroups);
                    accountDisplay.mUngrouped.mGroupPosition = lGroupPosition;
                    if (!(CommonUtilMethods.isSimAccount(account.type) || account.type.startsWith("com.huawei.android"))) {
                        accountDisplay.addGroupAtTop(accountDisplay.mUngrouped);
                    }
                    iterator.close();
                    accounts.add(accountDisplay);
                }
            }
            return accounts;
        }

        public void deliverResult(AccountSet cursor) {
            if (!isReset()) {
                this.mAccountSet = cursor;
                if (isStarted()) {
                    super.deliverResult(cursor);
                }
            }
        }

        protected void onStartLoading() {
            if (this.mAccountSet != null) {
                deliverResult(this.mAccountSet);
            }
            if (takeContentChanged() || this.mAccountSet == null) {
                forceLoad();
            }
        }

        protected void onStopLoading() {
            cancelLoad();
        }

        protected void onReset() {
            super.onReset();
            onStopLoading();
            this.mAccountSet = null;
        }
    }

    protected static class DisplayAdapter extends BaseExpandableListAdapter {
        private AccountTypeManager mAccountTypes;
        private AccountSet mAccounts;
        private Context mContext;
        private LayoutInflater mInflater;

        public DisplayAdapter(Context context) {
            this.mContext = context;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mAccountTypes = AccountTypeManager.getInstance(context);
        }

        public void setAccounts(AccountSet accounts) {
            this.mAccounts = accounts;
            notifyDataSetChanged();
        }

        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.custom_contact_list_filter_account, parent, false);
            }
            TextView text1 = (TextView) convertView.findViewById(16908308);
            TextView text2 = (TextView) convertView.findViewById(16908309);
            LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.ll_custom_contact_list_filter_account_group_item);
            AccountDisplay account = (AccountDisplay) getGroup(groupPosition);
            AccountType accountType = this.mAccountTypes.getAccountType(account.mType, account.mDataSet);
            text2.setText(account.mName);
            text2.setVisibility(account.mName == null ? 8 : 0);
            CheckBox lCheckbox = (CheckBox) convertView.findViewById(R.id.checkbox);
            lCheckbox.setOnCheckedChangeListener(null);
            lCheckbox.setChecked(areAllChildsChecked(groupPosition));
            lCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DisplayAdapter.this.updateChildsVisibility(groupPosition, isChecked);
                }
            });
            if (CommonUtilMethods.isLocalDefaultAccount(account.mType)) {
                int hiCloudAccountState = HiCloudUtil.getHicloudAccountState(this.mContext);
                boolean isSync = HiCloudUtil.isHicloudSyncStateEnabled(this.mContext);
                if (hiCloudAccountState == 1) {
                    text1.setText(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(this.mContext, isSync));
                    text2.setText(HiCloudUtil.getHiCloudAccountName());
                    text2.setVisibility(0);
                } else {
                    text1.setText(CommonUtilMethods.getHiCloudAccountPhoneDisplayString(this.mContext));
                    text2.setVisibility(8);
                }
                text1.setVisibility(0);
            } else if ("com.android.huawei.phone".equalsIgnoreCase(accountType.accountType) || CommonUtilMethods.isSimAccount(accountType.accountType)) {
                text1.setText(accountType.getDisplayLabel(this.mContext));
                text1.setVisibility(0);
                text2.setVisibility(8);
            } else {
                text1.setVisibility(0);
                text1.setText(accountType.getDisplayLabel(this.mContext));
            }
            setViewMargin(ll, text1, text2);
            return convertView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.custom_contact_list_filter_group, parent, false);
            }
            TextView text1 = (TextView) convertView.findViewById(16908308);
            TextView text2 = (TextView) convertView.findViewById(16908309);
            CheckBox checkbox = (CheckBox) convertView.findViewById(16908289);
            RelativeLayout rl = (RelativeLayout) convertView.findViewById(R.id.rl_custom_contact_list_filter_group);
            GroupDelta child = (GroupDelta) getChild(groupPosition, childPosition);
            if (child != null) {
                boolean groupVisible = child.getVisible();
                checkbox.setVisibility(0);
                checkbox.setChecked(groupVisible);
                text1.setText(CamcardGroup.replaceTitle(getChildTitle(groupPosition, child).toString(), this.mContext));
                text2.setVisibility(8);
            } else {
                checkbox.setVisibility(8);
                text1.setText(R.string.display_more_groups);
                text2.setVisibility(8);
            }
            setViewMargin(rl, text1, text2);
            return convertView;
        }

        private void setViewMargin(View parentView, TextView text1, TextView text2) {
            if (text2.getVisibility() == 0) {
                MarginLayoutParams params = (MarginLayoutParams) parentView.getLayoutParams();
                int marginTopBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.custom_contact_list_filter_account_double_margin_top_bottom);
                params.topMargin = marginTopBottom;
                params.bottomMargin = marginTopBottom;
                parentView.setLayoutParams(params);
                return;
            }
            marginTopBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.custom_contact_list_filter_account_single_margin_top_bottom);
            params = (MarginLayoutParams) text1.getLayoutParams();
            params.topMargin = marginTopBottom;
            params.bottomMargin = marginTopBottom;
            text1.setLayoutParams(params);
        }

        public CharSequence getChildTitle(int groupPosition, GroupDelta child) {
            String accountType = ((AccountDisplay) this.mAccounts.get(groupPosition)).mType;
            if (CommonUtilMethods.isSimAccount(accountType)) {
                return SimFactoryManager.getSimCardDisplayLabel(accountType);
            }
            return child.getTitle(this.mContext);
        }

        public Object getChild(int groupPosition, int childPosition) {
            boolean validChild = false;
            AccountDisplay account = (AccountDisplay) this.mAccounts.get(groupPosition);
            if (childPosition >= 0 && childPosition < account.mSyncedGroups.size()) {
                validChild = true;
            }
            if (validChild) {
                return account.mSyncedGroups.get(childPosition);
            }
            return null;
        }

        public void updateChildsVisibility(int aGroupPosition, boolean aIsChecked) {
            for (GroupDelta lDelta : ((AccountDisplay) this.mAccounts.get(aGroupPosition)).mSyncedGroups) {
                if (lDelta.mGroupPosition == aGroupPosition) {
                    lDelta.putVisible(aIsChecked);
                }
            }
            notifyDataSetChanged();
        }

        public boolean areAllChildsChecked(int aGroupPosition) {
            for (GroupDelta lDelta : ((AccountDisplay) this.mAccounts.get(aGroupPosition)).mSyncedGroups) {
                if (lDelta.mGroupPosition == aGroupPosition && !lDelta.getVisible()) {
                    return false;
                }
            }
            return true;
        }

        public long getChildId(int groupPosition, int childPosition) {
            long j = Long.MIN_VALUE;
            GroupDelta child = (GroupDelta) getChild(groupPosition, childPosition);
            if (child == null) {
                return Long.MIN_VALUE;
            }
            Long childId = child.getId();
            if (childId != null) {
                j = childId.longValue();
            }
            return j;
        }

        public int getChildrenCount(int groupPosition) {
            int i = 0;
            AccountDisplay account = (AccountDisplay) this.mAccounts.get(groupPosition);
            boolean anyHidden = account.mUnsyncedGroups.size() > 0;
            int size = account.mSyncedGroups.size();
            if (anyHidden) {
                i = 1;
            }
            return i + size;
        }

        public Object getGroup(int groupPosition) {
            return this.mAccounts.get(groupPosition);
        }

        public int getGroupCount() {
            if (this.mAccounts == null) {
                return 0;
            }
            return this.mAccounts.size();
        }

        public long getGroupId(int groupPosition) {
            return (long) groupPosition;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    protected static class GroupDelta extends ValuesDelta {
        private boolean mAccountHasGroups;
        private int mGroupPosition = -1;
        private boolean mUngrouped = false;

        private GroupDelta() {
        }

        public static GroupDelta fromSettings(ContentResolver resolver, String accountName, String accountType, String dataSet, boolean accountHasGroups) {
            Builder settingsUri = Settings.CONTENT_URI.buildUpon().appendQueryParameter("account_name", accountName).appendQueryParameter("account_type", accountType);
            if (dataSet != null) {
                settingsUri.appendQueryParameter("data_set", dataSet);
            }
            Cursor cursor = resolver.query(settingsUri.build(), new String[]{"should_sync", "ungrouped_visible"}, null, null, null);
            try {
                ContentValues values = new ContentValues();
                values.put("account_name", accountName);
                values.put("account_type", accountType);
                values.put("data_set", dataSet);
                GroupDelta ungrouped;
                if (cursor == null || !cursor.moveToFirst()) {
                    values.put("should_sync", Integer.valueOf(1));
                    values.put("ungrouped_visible", Integer.valueOf(0));
                    ungrouped = fromAfter(values).setUngrouped(accountHasGroups);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return ungrouped;
                }
                values.put("should_sync", Integer.valueOf(cursor.getInt(0)));
                values.put("ungrouped_visible", Integer.valueOf(cursor.getInt(1)));
                ungrouped = fromBefore(values).setUngrouped(accountHasGroups);
                return ungrouped;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        public static GroupDelta fromBefore(ContentValues before) {
            GroupDelta entry = new GroupDelta();
            entry.mBefore = before;
            entry.mAfter = new ContentValues();
            return entry;
        }

        public static GroupDelta fromAfter(ContentValues after) {
            GroupDelta entry = new GroupDelta();
            entry.mBefore = null;
            entry.mAfter = after;
            return entry;
        }

        protected GroupDelta setUngrouped(boolean accountHasGroups) {
            this.mUngrouped = true;
            this.mAccountHasGroups = accountHasGroups;
            return this;
        }

        public boolean beforeExists() {
            return this.mBefore != null;
        }

        public boolean getShouldSync() {
            boolean z = true;
            String key = "should_sync";
            if (this.mUngrouped) {
                key = "should_sync";
            }
            Integer linteger = getAsInteger(key, Integer.valueOf(1));
            if (linteger != null) {
                if (linteger.intValue() == 0) {
                    z = false;
                }
                return z;
            }
            HwLog.e("CustomContactListFilterActivity", "getShouldSync must be error");
            return false;
        }

        public boolean getVisible() {
            Integer linteger = getAsInteger(this.mUngrouped ? "ungrouped_visible" : "group_visible", Integer.valueOf(0));
            if (linteger != null) {
                boolean z;
                if (linteger.intValue() != 0) {
                    z = true;
                } else {
                    z = false;
                }
                return z;
            }
            HwLog.e("CustomContactListFilterActivity", "getVisible must be error");
            return false;
        }

        public void putShouldSync(boolean shouldSync) {
            int i;
            String key = "should_sync";
            if (this.mUngrouped) {
                key = "should_sync";
            }
            if (shouldSync) {
                i = 1;
            } else {
                i = 0;
            }
            put(key, i);
        }

        public void putVisible(boolean visible) {
            put(this.mUngrouped ? "ungrouped_visible" : "group_visible", visible ? 1 : 0);
        }

        private String getAccountType() {
            return (this.mBefore == null ? this.mAfter : this.mBefore).getAsString("account_type");
        }

        public CharSequence getTitle(Context context) {
            if (this.mUngrouped) {
                String customAllContactsName = LocalizedNameResolver.getAllContactsName(context, getAccountType());
                if (customAllContactsName != null) {
                    return customAllContactsName;
                }
                if (this.mAccountHasGroups) {
                    return context.getText(R.string.display_ungrouped);
                }
                return context.getText(R.string.display_all_contacts);
            }
            Integer titleRes = getAsInteger("title_res");
            if (titleRes == null) {
                return getAsString("title");
            }
            return context.getPackageManager().getText(getAsString("res_package"), titleRes.intValue(), null);
        }

        public ContentProviderOperation buildDiff() {
            if (isInsert()) {
                if (this.mUngrouped) {
                    this.mAfter.remove(this.mIdColumn);
                    return ContentProviderOperation.newInsert(Settings.CONTENT_URI).withValues(this.mAfter).build();
                }
                throw new IllegalStateException("Unexpected diff");
            } else if (isUpdate()) {
                if (this.mUngrouped) {
                    return getUpdateOperationForUngrouped();
                }
                return ContentProviderOperation.newUpdate(CustomContactListFilterActivity.addCallerIsSyncAdapterParameter(Groups.CONTENT_URI)).withSelection("_id=" + getId(), null).withValues(this.mAfter).build();
            } else if (!this.mUngrouped || this.mBefore == null) {
                return null;
            } else {
                this.mAfter = new ContentValues();
                this.mAfter.putAll(this.mBefore);
                return getUpdateOperationForUngrouped();
            }
        }

        public ContentProviderOperation getUpdateOperationForUngrouped() {
            String[] selectionArgs;
            String accountName = getAsString("account_name");
            String accountType = getAsString("account_type");
            String dataSet = getAsString("data_set");
            StringBuilder selection = new StringBuilder("account_name=? AND account_type=?");
            if (dataSet == null) {
                selection.append(" AND data_set IS NULL");
                selectionArgs = new String[]{accountName, accountType};
            } else {
                selection.append(" AND data_set=?");
                selectionArgs = new String[]{accountName, accountType, dataSet};
            }
            return ContentProviderOperation.newUpdate(Settings.CONTENT_URI).withSelection(selection.toString(), selectionArgs).withValues(this.mAfter).build();
        }

        public boolean equals(Object object) {
            return super.equals(object);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    private static class PhoneAccountDisplay extends AccountDisplay {
        public int mPrivateGroupId = -1;

        public PhoneAccountDisplay(ContentResolver resolver, String accountName, String accountType, String dataSet) {
            super(resolver, accountName, accountType, dataSet);
        }

        public void buildDiff(ArrayList<ContentProviderOperation> diff) {
            for (GroupDelta group : this.mSyncedGroups) {
                ContentProviderOperation privOper = updatePrivateGroup(group);
                if (privOper != null) {
                    diff.add(privOper);
                }
                ContentProviderOperation oper = group.buildDiff();
                if (oper != null) {
                    diff.add(oper);
                }
            }
            for (GroupDelta group2 : this.mUnsyncedGroups) {
                privOper = updatePrivateGroup(group2);
                if (privOper != null) {
                    diff.add(privOper);
                }
                oper = group2.buildDiff();
                if (oper != null) {
                    diff.add(oper);
                }
            }
        }

        private ContentProviderOperation updatePrivateGroup(GroupDelta group) {
            if (this.mPrivateGroupId == -1 || !group.mUngrouped) {
                return null;
            }
            if (group.isInsert()) {
                return ContentProviderOperation.newUpdate(CustomContactListFilterActivity.addCallerIsSyncAdapterParameter(Groups.CONTENT_URI)).withSelection("_id=" + this.mPrivateGroupId, null).withValue("group_visible", Integer.valueOf(1)).build();
            }
            if (!group.isUpdate() || !group.getAfter().containsKey("ungrouped_visible")) {
                return null;
            }
            return ContentProviderOperation.newUpdate(CustomContactListFilterActivity.addCallerIsSyncAdapterParameter(Groups.CONTENT_URI)).withSelection("_id=" + this.mPrivateGroupId, null).withValue("group_visible", Integer.valueOf(group.getAfter().getAsInteger("ungrouped_visible").intValue())).build();
        }
    }

    public static class UpdateTask extends WeakAsyncTask<ArrayList<ContentProviderOperation>, Void, Void, Activity> {
        private ProgressDialog mProgress;

        public UpdateTask(Activity target) {
            super(target);
        }

        protected void onPreExecute(Activity target) {
            Context context = target;
            this.mProgress = ProgressDialog.show(target, null, target.getText(R.string.savingDisplayGroups));
            target.startService(new Intent(target, EmptyService.class));
        }

        protected Void doInBackground(Activity target, ArrayList<ContentProviderOperation>... params) {
            Context context = target;
            try {
                target.getContentResolver().applyBatch("com.android.contacts", params[0]);
            } catch (RemoteException e) {
                HwLog.e("CustomContactListFilterActivity", "Problem saving display groups", e);
            } catch (OperationApplicationException e2) {
                HwLog.e("CustomContactListFilterActivity", "Problem saving display groups", e2);
            }
            return null;
        }

        protected void onPostExecute(Activity target, Void result) {
            Context context = target;
            try {
                this.mProgress.dismiss();
            } catch (Exception e) {
                HwLog.e("CustomContactListFilterActivity", "Error dismissing progress dialog", e);
            }
            target.finish();
            target.stopService(new Intent(target, EmptyService.class));
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
        setTheme(R.style.ContactListFilterTheme);
        setContentView(R.layout.contact_list_filter_custom);
        this.mList = (ExpandableListViewEx) findViewById(16908298);
        this.mList.setOnChildClickListener(this);
        this.mList.setHeaderDividersEnabled(true);
        this.mPrefs = SharePreferenceUtil.getDefaultSp_de(this);
        this.mAdapter = new DisplayAdapter(this);
        this.mList.setOnCreateContextMenuListener(this);
        this.mList.setAdapter(this.mAdapter);
        ActionBar actionBar = getActionBar();
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(actionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(actionBar, true, null, this.mActionBarListener);
        } else {
            this.mCustActionBar = new ActionBarCustom(this, actionBar);
            this.mCustActionBar.setStartIcon(true, null, this.mActionBarListener);
            this.mCustActionBar.setEndIcon(true, null, this.mActionBarListener);
        }
        AccountTypeManager.getInstance(getApplicationContext()).setAccountLoadListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        AccountTypeManager.getInstance(getApplicationContext()).unregisterAccountLoadListener();
    }

    protected void onStart() {
        getLoaderManager().initLoader(1, new Bundle(), this);
        super.onStart();
    }

    public Loader<AccountSet> onCreateLoader(int id, Bundle args) {
        return new CustomFilterConfigurationLoader(this);
    }

    public void onLoadFinished(Loader<AccountSet> loader, AccountSet data) {
        this.mAdapter.setAccounts(data);
    }

    public void onLoaderReset(Loader<AccountSet> loader) {
        this.mAdapter.setAccounts(null);
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
    }

    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        CheckBox checkbox = (CheckBox) view.findViewById(16908289);
        GroupDelta child = (GroupDelta) this.mAdapter.getChild(groupPosition, childPosition);
        if (child != null) {
            checkbox.toggle();
            child.putVisible(checkbox.isChecked());
            this.mAdapter.notifyDataSetChanged();
        } else {
            openContextMenu(view);
        }
        return true;
    }

    protected int getSyncMode(AccountDisplay account) {
        if ("com.google".equals(account.mType) && account.mDataSet == null) {
            return 2;
        }
        return 0;
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (menuInfo instanceof ExpandableListContextMenuInfo) {
            ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
            int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
            if (childPosition != -1) {
                AccountDisplay account = (AccountDisplay) this.mAdapter.getGroup(groupPosition);
                GroupDelta child = (GroupDelta) this.mAdapter.getChild(groupPosition, childPosition);
                int syncMode = getSyncMode(account);
                if (syncMode != 0) {
                    if (child != null) {
                        showRemoveSync(menu, account, child, syncMode);
                    } else {
                        showAddSync(menu, account, syncMode);
                    }
                }
            }
        }
    }

    protected void showRemoveSync(ContextMenu menu, AccountDisplay account, GroupDelta child, int syncMode) {
        final CharSequence title = child.getTitle(this);
        menu.setHeaderTitle(title);
        final AccountDisplay accountDisplay = account;
        final GroupDelta groupDelta = child;
        final int i = syncMode;
        menu.add(R.string.menu_sync_remove).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                CustomContactListFilterActivity.this.handleRemoveSync(accountDisplay, groupDelta, i, title);
                return true;
            }
        });
    }

    protected void handleRemoveSync(final AccountDisplay account, final GroupDelta child, int syncMode, CharSequence title) {
        boolean shouldSyncUngrouped = account.mUngrouped.getShouldSync();
        if (syncMode == 2 && shouldSyncUngrouped && !child.equals(account.mUngrouped)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            CharSequence removeMessage = getString(R.string.display_warn_remove_ungrouped, new Object[]{title});
            builder.setTitle(R.string.menu_sync_remove);
            View view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(removeMessage);
            builder.setView(view);
            builder.setNegativeButton(17039360, null);
            builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    account.setShouldSync(account.mUngrouped, false);
                    account.setShouldSync(child, false);
                    CustomContactListFilterActivity.this.mAdapter.notifyDataSetChanged();
                }
            });
            builder.show();
            return;
        }
        account.setShouldSync(child, false);
        this.mAdapter.notifyDataSetChanged();
    }

    protected void showAddSync(ContextMenu menu, final AccountDisplay account, final int syncMode) {
        menu.setHeaderTitle(R.string.dialog_sync_add);
        for (final GroupDelta child : account.mUnsyncedGroups) {
            if (!child.getShouldSync()) {
                menu.add(child.getTitle(this)).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (child.mUngrouped && syncMode == 2) {
                            account.setShouldSync(true);
                        } else {
                            account.setShouldSync(child, true);
                        }
                        CustomContactListFilterActivity.this.mAdapter.notifyDataSetChanged();
                        return true;
                    }
                });
            }
        }
    }

    private void doSaveAction() {
        if (this.mAdapter == null || this.mAdapter.mAccounts == null) {
            finish();
            return;
        }
        setResult(-1);
        if (this.mAdapter.mAccounts.buildDiff().isEmpty()) {
            finish();
            return;
        }
        new UpdateTask(this).execute(new ArrayList[]{diff});
    }

    public void onAccountsLoadCompleted() {
        HwLog.i("CustomContactListFilterActivity", "onAccountLoadComplete");
        runOnUiThread(new Runnable() {
            public void run() {
                if (!CustomContactListFilterActivity.this.isFinishing()) {
                    CustomContactListFilterActivity.this.getLoaderManager().restartLoader(1, new Bundle(), CustomContactListFilterActivity.this);
                }
            }
        });
    }
}
