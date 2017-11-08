package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseActivity;

public class SlideSmootShowActivity extends HwBaseActivity {
    private SlideSmootShowFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(18);
        this.mFragment = new SlideSmootShowFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }

    public void onBackPressed() {
        if (!this.mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.mFragment.onKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
