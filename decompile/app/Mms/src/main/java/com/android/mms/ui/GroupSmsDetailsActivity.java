package com.android.mms.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

public class GroupSmsDetailsActivity extends Activity {
    private GroupSmsDetailsFragment mFragment = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment lFragment = fragmentManager.findFragmentByTag("group-sms-detail-tag");
        if (lFragment instanceof GroupSmsDetailsFragment) {
            this.mFragment = (GroupSmsDetailsFragment) lFragment;
        } else {
            this.mFragment = new GroupSmsDetailsFragment();
            transaction.replace(16908290, this.mFragment, "group-sms-detail-tag");
        }
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }
}
