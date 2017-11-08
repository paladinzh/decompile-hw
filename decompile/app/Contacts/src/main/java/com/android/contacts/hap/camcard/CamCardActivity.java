package com.android.contacts.hap.camcard;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.activities.TransactionSafeActivity;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.util.ExceptionCapture;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;

public class CamCardActivity extends TransactionSafeActivity {
    private static final String TAG = CamCardActivity.class.getSimpleName();
    ContactEntryListFragment<ContactEntryListAdapter> mCCBrowseListFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        getWindow().setFlags(16777216, 16777216);
        setTheme(R.style.MultiSelectTheme);
        setContentView(R.layout.simple_frame_layout);
        if (configureListFragment(savedInstanceState)) {
            ExceptionCapture.reportScene(53);
        } else {
            finish();
        }
    }

    protected boolean configureListFragment(Bundle args) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        this.mCCBrowseListFragment = (ContactEntryListFragment) fragmentManager.findFragmentByTag("cc_list_fragment");
        if (this.mCCBrowseListFragment == null) {
            this.mCCBrowseListFragment = new CCBrowseListFragment();
            transaction.replace(R.id.list_container, this.mCCBrowseListFragment, "cc_list_fragment");
        }
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.camcard_menu, menu);
        ImmersionUtils.setImmersionMommonMenu(this, menu.findItem(R.id.menu_scan_card));
        ViewUtil.setMenuItemsStateListIcon(this, menu);
        return true;
    }

    public void onBackPressed() {
        if (!(this.mCCBrowseListFragment instanceof CCBrowseListFragment) || !((CCBrowseListFragment) this.mCCBrowseListFragment).onBackPressedRet()) {
            super.onBackPressed();
        }
    }
}
