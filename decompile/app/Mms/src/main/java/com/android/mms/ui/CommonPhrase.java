package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.mms.ui.HwBaseActivity;

public class CommonPhrase extends HwBaseActivity {
    private CommonPhraseFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFragment = new CommonPhraseFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }
}
