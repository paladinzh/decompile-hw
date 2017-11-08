package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.mms.ui.HwBaseActivity;

public class RecyclerSmsListActivity extends HwBaseActivity {
    RecyclerSmsListFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFragment = new RecyclerSmsListFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (!this.mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
