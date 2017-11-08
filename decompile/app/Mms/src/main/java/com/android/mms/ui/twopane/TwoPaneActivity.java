package com.android.mms.ui.twopane;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;

public class TwoPaneActivity extends HwBaseActivity {
    HwBaseFragment leftFragment = new LeftPaneConversationListFragment();
    HwBaseFragment oldLeftFragment = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.two_pane_activity);
        Intent intent = (Intent) getIntent().clone();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt("running_mode", 0);
        intent.putExtras(bundle);
        setIntent(intent);
        this.leftFragment.setIntent(intent);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.left_framement, this.leftFragment);
        transaction.commitAllowingStateLoss();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.mms_options, false);
        this.leftFragment.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.leftFragment.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (!this.leftFragment.onBackPressed()) {
            if (!isTaskRoot() || !moveTaskToBack(false)) {
                super.onBackPressed();
            }
        }
    }
}
