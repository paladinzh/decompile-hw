package com.android.contacts.hap.calllog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.HashSet;

public class CallLogMultiSelectionActivity extends Activity {
    private int mCallTypeFilter = 0;
    private CallLogMultiSelectionFragment mFragment = null;
    private boolean mHasPlayedExitAnimation = false;
    private boolean mIsNeedUpdateWindows;
    private int mNetworkTypeFilter = 2;
    HashSet<Long> mSelectedIds = new HashSet();

    public static class TranslucentActivity extends CallLogMultiSelectionActivity {
        public void finish() {
            super.finish();
            CommonUtilMethods.clearInstanceState();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        setTheme(R.style.MultiSelectTheme);
        setContentView(R.layout.simple_frame_layout);
        Intent intent = getIntent();
        this.mCallTypeFilter = intent.getIntExtra("call_type_filter", 0);
        this.mNetworkTypeFilter = intent.getIntExtra("network_type_filter", 2);
        if (this.mFragment == null) {
            configureListFragment(savedInstanceState);
        }
        if (CommonUtilMethods.calcIfNeedSplitScreen() && 2 == getResources().getConfiguration().orientation) {
            if (savedInstanceState == null) {
                z = true;
            }
            updateWindowsParams(z);
            this.mIsNeedUpdateWindows = true;
        }
    }

    private void updateWindowsParams(boolean bFirstStarted) {
        getWindow().setBackgroundDrawableResource(17170445);
        overridePendingTransition(0, 0);
        findViewById(R.id.simple_frame_id).setBackgroundResource(R.color.split_transparent);
        LinearLayout listContainer = (LinearLayout) findViewById(R.id.list_container);
        listContainer.setLayoutParams(new LayoutParams(0, -1, 1.0f));
        listContainer.setBackgroundResource(R.drawable.multiselection_background);
        View grayView = findViewById(R.id.gray_view);
        grayView.setVisibility(0);
        grayView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                CallLogMultiSelectionActivity.this.finish();
            }
        });
        if (bFirstStarted) {
            Animation animation;
            if (CommonUtilMethods.isLayoutRTL()) {
                animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            } else {
                animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
            }
            listContainer.startAnimation(animation);
        }
    }

    public void finish() {
        if (!this.mIsNeedUpdateWindows || this.mHasPlayedExitAnimation) {
            super.finish();
            if (this.mIsNeedUpdateWindows) {
                overridePendingTransition(0, 0);
            }
            return;
        }
        Animation animation;
        if (CommonUtilMethods.isLayoutRTL()) {
            animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        } else {
            animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        }
        findViewById(R.id.list_container).startAnimation(animation);
        this.mHasPlayedExitAnimation = true;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                CallLogMultiSelectionActivity.this.finish();
            }
        }, 300);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && getResources().getConfiguration().orientation == 2) {
            return false;
        }
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.hapmultiselectmenu_calllog, menu);
        ViewUtil.setMenuItemsStateListIcon(getApplicationContext(), menu);
        return true;
    }

    private void configureListFragment(Bundle args) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment lExistingFragment = fragmentManager.findFragmentById(R.id.list_container);
        if (lExistingFragment != null) {
            lExistingFragment = (CallLogMultiSelectionFragment) lExistingFragment;
        } else {
            lExistingFragment = new CallLogMultiSelectionFragment();
        }
        this.mFragment = lExistingFragment;
        this.mFragment.setCallTypeFilter(this.mCallTypeFilter);
        this.mFragment.setNetworkTypeFilter(this.mNetworkTypeFilter);
        transaction.replace(R.id.list_container, this.mFragment);
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof CallLogMultiSelectionFragment) {
            this.mFragment = (CallLogMultiSelectionFragment) fragment;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!ActivityManager.isUserAMonkey() || event.getKeyCode() == 4) {
            return super.dispatchKeyEvent(event);
        }
        return false;
    }
}
