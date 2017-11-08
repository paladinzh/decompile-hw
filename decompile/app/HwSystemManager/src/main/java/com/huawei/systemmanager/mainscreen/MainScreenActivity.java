package com.huawei.systemmanager.mainscreen;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivityHelper;
import com.huawei.systemmanager.mainscreen.normal.NormalFragment;
import com.huawei.systemmanager.util.HwLog;

public class MainScreenActivity extends SingleFragmentActivity {
    private static final String TAG = "MainScreenActivity";
    private boolean mIsNavBarOnBottomWhenLand = false;

    protected void onCreate(Bundle savedInstanceState) {
        boolean isLand;
        HwLog.i(TAG, "create!");
        this.mIsNavBarOnBottomWhenLand = Utility.isNavBarOnBottomWhenLand();
        if (getResources().getConfiguration().orientation == 2) {
            isLand = true;
        } else {
            isLand = false;
        }
        if (!isLand || this.mIsNavBarOnBottomWhenLand) {
            HsmActivityHelper.setTranslucentNavigation(this, true);
        } else {
            HsmActivityHelper.setTranslucentNavigation(this, false);
        }
        HsmActivityHelper.setTranslucentNavigation(this, false);
        super.onCreate(savedInstanceState);
    }

    protected Fragment buildFragment() {
        return new NormalFragment();
    }

    protected boolean useHsmActivityHelper() {
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean isLand;
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == 2) {
            isLand = true;
        } else {
            isLand = false;
        }
        if (!isLand || this.mIsNavBarOnBottomWhenLand) {
            HsmActivityHelper.setTranslucentNavigation(this, true);
        } else {
            HsmActivityHelper.setTranslucentNavigation(this, false);
        }
        HsmActivityHelper.setTranslucentNavigation(this, false);
    }

    public void onBackPressed() {
        super.onBackPressed();
        HwLog.i(TAG, "onBackPressed!");
    }
}
