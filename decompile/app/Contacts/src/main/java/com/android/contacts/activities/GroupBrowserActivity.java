package com.android.contacts.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import com.android.contacts.ContactsActivity;
import com.android.contacts.editor.SelectAccountDialogFragment.Listener;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.model.account.AccountWithDataSet;
import com.google.android.gms.R;

public class GroupBrowserActivity extends ContactsActivity implements Listener {
    private GroupBrowseListFragment mGroupBrowseListFragment;

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        setTheme(R.style.PeopleTheme);
        FragmentManager fm = getFragmentManager();
        if (savedState == null) {
            createGroupBrowseListFragment(fm);
        } else {
            this.mGroupBrowseListFragment = (GroupBrowseListFragment) fm.findFragmentByTag("GroupBrowseListFragment");
            if (this.mGroupBrowseListFragment == null) {
                createGroupBrowseListFragment(fm);
            }
        }
    }

    private void createGroupBrowseListFragment(FragmentManager fm) {
        this.mGroupBrowseListFragment = new GroupBrowseListFragment();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(16908290, this.mGroupBrowseListFragment, "GroupBrowseListFragment");
        transaction.commit();
    }

    protected void onNewIntent(Intent intent) {
        this.mGroupBrowseListFragment.onNewIntent(intent);
    }

    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        if (this.mGroupBrowseListFragment != null) {
            this.mGroupBrowseListFragment.onAccountChosen(account, extraArgs);
        }
    }

    public void onAccountSelectorCancelled() {
        if (this.mGroupBrowseListFragment != null) {
            this.mGroupBrowseListFragment.onAccountSelectorCancelled();
        }
    }
}
