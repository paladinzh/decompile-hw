package com.android.contacts.hap.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.editor.GroupSelectListAdapter;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.interactions.GroupCreationDialogFragment;
import com.android.contacts.interactions.GroupCreationDialogFragment.OnGroupCreatedListener;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.TextUtil;
import com.android.contacts.widget.ActionBarEx;
import com.android.contacts.widget.AutoScrollListView;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GroupSelectActivity extends Activity implements OnItemClickListener {
    private AccountWithDataSet mAccountOfNewGroup;
    private List<AccountWithDataSet> mAccountsList = new ArrayList();
    private ActionBar mActionBar;
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == 16908295) {
                GroupSelectActivity.this.finish();
            } else if (viewId == 16908296) {
                GroupSelectActivity.this.doOperation();
            }
        }
    };
    private GroupSelectListAdapter mAdapter;
    private boolean mCreatedNewGroup;
    private TextView mEmptyView;
    private Cursor mGroupCursor;
    private HashSet<Long> mGroupList = new HashSet();
    private final LoaderCallbacks<Cursor> mGroupLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            String[] strArr = null;
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            selection.append(CallInterceptDetails.BRANDED_STATE);
            if (!(GroupSelectActivity.this.mAccountsList == null || GroupSelectActivity.this.mAccountsList.isEmpty())) {
                selection.append(" AND (");
                int i = 0;
                for (AccountWithDataSet account : GroupSelectActivity.this.mAccountsList) {
                    if (i > 0) {
                        selection.append(" OR ");
                    }
                    selection.append("(");
                    selection.append("account_type=?");
                    selectionArgs.add(account.type);
                    selection.append(" AND ");
                    selection.append("account_name=?");
                    selectionArgs.add(account.name);
                    if (account.dataSet != null) {
                        selection.append(" AND data_set=?");
                        selectionArgs.add(account.dataSet);
                    } else {
                        selection.append(" AND data_set IS NULL");
                    }
                    selection.append(")");
                    i++;
                }
                selection.append(")");
            }
            selection.append(" AND ").append("deleted").append(" = 0");
            selection.append(" AND ").append("favorites").append("=0");
            Context context = GroupSelectActivity.this;
            Uri uri = Groups.CONTENT_URI;
            String stringBuilder = selection.toString();
            if (selectionArgs.size() > 0) {
                strArr = (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
            }
            return new GroupMetaDataLoader(context, uri, stringBuilder, strArr);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                GroupSelectActivity.this.mGroupCursor = data;
                GroupSelectActivity.this.removeGroupNotExisted();
                GroupSelectActivity.this.createAdapter();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private AutoScrollListView mListView;
    private Menu mMenu;
    private String mNewGroupName;
    private ActionBarCustomTitle mTitle;

    private class AccountSelectedListener implements DialogInterface.OnClickListener {
        private final List<AccountWithDataSet> mAccountList;

        public AccountSelectedListener(List<AccountWithDataSet> accountList) {
            this.mAccountList = accountList;
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            GroupSelectActivity.this.createOneNewGroup((AccountWithDataSet) this.mAccountList.get(which));
        }
    }

    private class GroupCreatedListener implements OnGroupCreatedListener {
        private GroupCreatedListener() {
        }

        public void onGroupCreated(String groupLabel) {
            GroupSelectActivity.this.mCreatedNewGroup = true;
            GroupSelectActivity.this.mNewGroupName = groupLabel;
            int count = GroupSelectActivity.this.mAdapter.getCount();
            boolean isGroupExisted = false;
            int i = 0;
            while (i < count) {
                if (GroupSelectActivity.this.mAdapter.getItem(i).getTitle() != null && GroupSelectActivity.this.mAdapter.getItem(i).getTitle().equals(groupLabel) && GroupSelectActivity.this.isSameAccount(GroupSelectActivity.this.mAccountOfNewGroup, GroupSelectActivity.this.mAdapter.getItem(i).getAccountWithDataSet())) {
                    isGroupExisted = true;
                    break;
                }
                i++;
            }
            if (isGroupExisted) {
                GroupSelectActivity.this.createAdapter();
            }
        }
    }

    private static class GroupSelectAccountsListAdapter extends BaseAdapter {
        private List<AccountWithDataSet> mAccountList;
        private final AccountTypeManager mAccountTypes;
        private Context mContext;
        private final LayoutInflater mInflater;

        public GroupSelectAccountsListAdapter(Context context, List<AccountWithDataSet> accountList) {
            this.mContext = context;
            this.mAccountList = accountList;
            this.mInflater = LayoutInflater.from(context);
            this.mAccountTypes = AccountTypeManager.getInstance(context);
        }

        public int getCount() {
            return this.mAccountList.size();
        }

        public Object getItem(int position) {
            return this.mAccountList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.account_selector_list_item_for_editor, parent, false);
            }
            TextView textName = (TextView) convertView.findViewById(16908309);
            AccountWithDataSet account = (AccountWithDataSet) this.mAccountList.get(position);
            ((TextView) convertView.findViewById(16908308)).setText(this.mAccountTypes.getAccountType(account.type, account.dataSet).getDisplayLabel(this.mContext));
            if (CommonUtilMethods.isLocalDefaultAccount(account.type)) {
                textName.setVisibility(8);
            } else {
                textName.setText(account.name);
                textName.setVisibility(0);
            }
            return convertView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setContentView(R.layout.contact_list_group_selection);
        if (savedInstanceState != null) {
            this.mAccountsList = savedInstanceState.getParcelableArrayList("accounts_list");
            this.mGroupList = (HashSet) savedInstanceState.getSerializable("selected_groups");
        } else {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                HwLog.w("GroupSelectActivity", "this is a null intent ");
                finish();
                return;
            }
            this.mAccountsList = bundle.getParcelableArrayList("groupAccountsList");
            this.mGroupList.clear();
            long[] groupList = bundle.getLongArray("currentGroupList");
            if (groupList != null && groupList.length > 0) {
                for (int i = 0; i < groupList.length; i++) {
                    if (groupList[i] > 0) {
                        this.mGroupList.add(Long.valueOf(groupList[i]));
                    }
                }
            }
        }
        this.mListView = (AutoScrollListView) findViewById(R.id.group_list);
        this.mListView.setChoiceMode(2);
        this.mListView.setOnItemClickListener(this);
        this.mEmptyView = (TextView) findViewById(R.id.empty_group);
        this.mActionBar = getActionBar();
        this.mTitle = new ActionBarCustomTitle(this);
        if (EmuiVersion.isSupportEmui3()) {
            ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setCustomTitle(this.mActionBar, this.mTitle.getTitleLayout());
        }
        this.mTitle.setCustomTitle(getString(R.string.contact_select_group), 0);
        getLoaderManager().initLoader(0, new Bundle(), this.mGroupLoaderListener);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selected_groups", this.mGroupList);
        outState.putParcelableArrayList("accounts_list", (ArrayList) this.mAccountsList);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_browser, menu);
        ImmersionUtils.setImmersionMommonMenu(this, menu.findItem(R.id.menu_group_browser_newgroup));
        ImmersionUtils.setImmersionMommonMenu(this, menu.findItem(R.id.menu_delete_groups_action));
        ImmersionUtils.setImmersionMommonMenu(this, menu.findItem(R.id.menu_confirm));
        ViewUtil.setMenuItemStateListIcon(getApplicationContext(), menu.findItem(R.id.menu_group_browser_newgroup));
        if (!EmuiVersion.isSupportEmui()) {
            ViewUtil.setMenuItemStateListIcon(getApplicationContext(), menu.findItem(R.id.menu_confirm));
        }
        this.mMenu = menu;
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        int marginTop;
        if (this.mMenu != null) {
            MenuItem newGroupItem = this.mMenu.findItem(R.id.menu_group_browser_newgroup);
            newGroupItem.setIcon(R.drawable.ic_new_contact);
            ViewUtil.setMenuItemStateListIcon(getApplicationContext(), newGroupItem);
        }
        if (getResources().getConfiguration().orientation == 1) {
            marginTop = getResources().getDimensionPixelSize(R.dimen.empty_contacts_group_top_margin_p);
        } else {
            marginTop = getResources().getDimensionPixelSize(R.dimen.empty_contacts_group_top_margin_l);
        }
        LayoutParams params = new LayoutParams(this.mEmptyView.getLayoutParams());
        params.setMargins(params.leftMargin, marginTop, params.rightMargin, params.bottomMargin);
        params.gravity = 17;
        this.mEmptyView.setLayoutParams(params);
        super.onConfigurationChanged(newConfig);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete_groups_action).setVisible(false);
        if (!EmuiVersion.isSupportEmui()) {
            menu.findItem(R.id.menu_confirm).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_group_browser_newgroup) {
            createNewGroup();
            return true;
        }
        if (item.getItemId() == R.id.menu_confirm && !EmuiVersion.isSupportEmui()) {
            doOperation();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAdapter() {
        if (this.mGroupCursor != null && !this.mGroupCursor.isClosed()) {
            int i;
            this.mAdapter = new GroupSelectListAdapter(this);
            if (this.mGroupCursor.getCount() == 0) {
                this.mEmptyView.setVisibility(0);
            } else {
                this.mEmptyView.setVisibility(8);
            }
            this.mListView.setAdapter(this.mAdapter);
            this.mAdapter.setCursor(this.mGroupCursor);
            this.mAdapter.setGroupList(this.mGroupList);
            int count = this.mListView.getCount();
            for (i = 0; i < count; i++) {
                this.mListView.setItemChecked(i, this.mAdapter.getItem(i).isChecked());
            }
            if (this.mCreatedNewGroup) {
                i = 0;
                while (i < count) {
                    if (this.mAdapter.getItem(i).getTitle() != null && this.mAdapter.getItem(i).getTitle().equals(this.mNewGroupName) && isSameAccount(this.mAccountOfNewGroup, this.mAdapter.getItem(i).getAccountWithDataSet())) {
                        this.mListView.setItemChecked(i, true);
                        this.mGroupList.add(Long.valueOf(this.mAdapter.getItem(i).getGroupId()));
                        break;
                    }
                    i++;
                }
                this.mCreatedNewGroup = false;
            }
            this.mTitle.setCustomTitle(getString(R.string.contact_select_group), this.mGroupList.size());
        }
    }

    private void createNewGroup() {
        if (this.mAccountsList.size() == 1) {
            createOneNewGroup((AccountWithDataSet) this.mAccountsList.get(0));
        } else if (this.mAccountsList.size() > 1) {
            Builder builder = new Builder(this).setTitle(R.string.dialog_new_group_account);
            builder.setAdapter(new GroupSelectAccountsListAdapter(this, this.mAccountsList), new AccountSelectedListener(this.mAccountsList));
            builder.create().show();
        }
    }

    private void createOneNewGroup(AccountWithDataSet accountWithAccountDataSet) {
        this.mAccountOfNewGroup = new AccountWithDataSet(accountWithAccountDataSet);
        GroupCreationDialogFragment.show(getFragmentManager(), accountWithAccountDataSet, new GroupCreatedListener());
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView list = (ListView) parent;
        int count = this.mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            if (list.isItemChecked(i)) {
                this.mGroupList.add(Long.valueOf(this.mAdapter.getItem(i).getGroupId()));
            } else {
                this.mGroupList.remove(Long.valueOf(this.mAdapter.getItem(i).getGroupId()));
            }
        }
        this.mAdapter.bindView(position, view, parent);
        this.mTitle.setCustomTitle(getString(R.string.contact_select_group), this.mGroupList.size());
    }

    private void doOperation() {
        long[] jArr = null;
        if (this.mGroupList != null && this.mGroupList.size() > 0) {
            jArr = new long[this.mGroupList.size()];
            int indexChecked = 0;
            for (Long id : this.mGroupList) {
                int indexChecked2 = indexChecked + 1;
                jArr[indexChecked] = id.longValue();
                indexChecked = indexChecked2;
            }
        }
        Intent lSelectIntent = new Intent();
        lSelectIntent.putExtra("checkedGroupList", jArr);
        setResult(-1, lSelectIntent);
        finish();
    }

    private void removeGroupNotExisted() {
        if (this.mGroupCursor != null) {
            HashSet<Long> mActuralGroupList = new HashSet();
            this.mGroupCursor.moveToPosition(-1);
            while (this.mGroupCursor.moveToNext()) {
                mActuralGroupList.add(Long.valueOf(this.mGroupCursor.getLong(3)));
            }
            if (this.mGroupList != null) {
                int size = this.mGroupList.size();
                int index = 0;
                if (size > 0) {
                    long[] excludedGroupList = new long[size];
                    for (Long id : this.mGroupList) {
                        if (!mActuralGroupList.contains(id)) {
                            int index2 = index + 1;
                            excludedGroupList[index] = id.longValue();
                            index = index2;
                        }
                    }
                    for (int i = 0; i < index; i++) {
                        if (excludedGroupList[i] > 0) {
                            this.mGroupList.remove(Long.valueOf(excludedGroupList[i]));
                        }
                    }
                }
            }
        }
    }

    private boolean isSameAccount(AccountWithDataSet account1, AccountWithDataSet account2) {
        if (account1 != null && account2 != null && TextUtil.stringOrNullEquals(account1.type, account2.type) && TextUtil.stringOrNullEquals(account1.name, account2.name) && TextUtil.stringOrNullEquals(account1.dataSet, account2.dataSet)) {
            return true;
        }
        return false;
    }
}
