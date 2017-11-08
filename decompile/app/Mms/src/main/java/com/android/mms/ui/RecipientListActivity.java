package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.mms.ui.HwBaseActivity;

public class RecipientListActivity extends HwBaseActivity {
    RecipientListFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFragment = new RecipientListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }
}
