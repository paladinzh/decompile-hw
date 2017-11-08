package com.android.mms.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import com.huawei.mms.ui.HwBaseActivity;

public class ContactItemPickActivity extends HwBaseActivity {
    ContactItemPickFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        if (savedInstanceState == null) {
            createContactItemPickFragment(fm);
            return;
        }
        this.mFragment = (ContactItemPickFragment) fm.findFragmentByTag("Mms_UI_CPF");
        if (this.mFragment == null) {
            createContactItemPickFragment(fm);
        } else {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        }
    }

    private void createContactItemPickFragment(FragmentManager fm) {
        this.mFragment = new ContactItemPickFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(16908290, this.mFragment, "Mms_UI_CPF");
        transaction.commit();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.mFragment.onKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }
}
