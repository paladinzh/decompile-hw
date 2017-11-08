package com.android.contacts.activities;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import com.android.contacts.ContactsActivity;
import com.android.contacts.group.GroupDetailDisplayUtils;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.google.android.gms.R;

public class GroupDetailActivity extends ContactsActivity {
    private String mAccountTypeString;
    private String mDataSet;
    private GroupDetailFragment mFragment;
    private Menu mOptionsMenu;
    private boolean mShowGroupSourceInActionBar;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        getWindow().setFlags(16777216, 16777216);
        setTheme(R.style.PeopleTheme);
        setContentView(R.layout.group_detail_activity);
        this.mShowGroupSourceInActionBar = getResources().getBoolean(R.bool.config_show_group_action_in_action_bar);
        this.mFragment = (GroupDetailFragment) getFragmentManager().findFragmentById(R.id.group_detail_fragment);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (this.mShowGroupSourceInActionBar) {
            getMenuInflater().inflate(R.menu.group_source, menu);
        }
        this.mOptionsMenu = menu;
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long duration = event.getEventTime() - event.getDownTime();
        if (this.mOptionsMenu == null || keyCode != 82 || duration >= ((long) ViewConfiguration.getLongPressTimeout())) {
            return super.onKeyUp(keyCode, event);
        }
        this.mOptionsMenu.performIdentifierAction(R.id.overflow_menu_group_detail, 1);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!this.mShowGroupSourceInActionBar) {
            return false;
        }
        MenuItem groupSourceMenuItem = menu.findItem(R.id.menu_group_source);
        if (groupSourceMenuItem == null) {
            return false;
        }
        final AccountType accountType = AccountTypeManager.getInstance(this).getAccountType(this.mAccountTypeString, this.mDataSet);
        if (TextUtils.isEmpty(this.mAccountTypeString) || TextUtils.isEmpty(accountType.getViewGroupActivity())) {
            groupSourceMenuItem.setVisible(false);
            return false;
        }
        View groupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(this);
        GroupDetailDisplayUtils.bindGroupSourceView(this, groupSourceView, this.mAccountTypeString, this.mDataSet);
        groupSourceView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW", ContentUris.withAppendedId(Groups.CONTENT_URI, GroupDetailActivity.this.mFragment.getGroupId()));
                intent.setClassName(accountType.syncAdapterPackageName, accountType.getViewGroupActivity());
                GroupDetailActivity.this.startActivity(intent);
            }
        });
        groupSourceMenuItem.setActionView(groupSourceView);
        groupSourceMenuItem.setVisible(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
