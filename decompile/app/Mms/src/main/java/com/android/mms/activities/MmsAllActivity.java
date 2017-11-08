package com.android.mms.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;

public class MmsAllActivity extends HwBaseActivity {
    private Fragment mCurrentFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mms_all_activity);
        getActionBar().hide();
        if (savedInstanceState == null) {
            addFragment();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onBackPressed() {
        if (!hookBackKeyEvent()) {
            super.onBackPressed();
        }
    }

    private boolean hookBackKeyEvent() {
        if (this.mCurrentFragment != null && (this.mCurrentFragment instanceof HwBaseFragment) && ((HwBaseFragment) this.mCurrentFragment).onBackPressed()) {
            return true;
        }
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (4 != event.getKeyCode()) {
            return super.dispatchKeyEvent(event);
        }
        if (1 == event.getAction()) {
            onBackPressed();
        }
        return true;
    }

    private void addFragment() {
        int activity_type = 0;
        Intent intent = getIntent();
        if (intent != null) {
            activity_type = intent.getIntExtra("fromWidget", 0);
        }
        Fragment fragment = createFragment(activity_type);
        if (fragment == null) {
            finish();
            MLog.e("MmsAllActivity", "Can't get Fragment for MmsAllActivity.");
            return;
        }
        getFragmentManager().beginTransaction().replace(R.id.activity_mms_all, fragment).commit();
    }

    private Fragment createFragment(int activity_type) {
        if (1 == activity_type) {
            return new CopyTextFragment();
        }
        return null;
    }

    public static Intent createIntent(Context context, int target) {
        Intent intent = new Intent(context, MmsAllActivity.class);
        intent.putExtra("fromWidget", target);
        return intent;
    }
}
