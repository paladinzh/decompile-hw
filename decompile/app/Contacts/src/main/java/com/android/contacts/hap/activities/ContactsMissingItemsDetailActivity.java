package com.android.contacts.hap.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.list.ContactsMissingItemsDetailFragment;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;

public class ContactsMissingItemsDetailActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setTheme(R.style.PeopleTheme);
        setContentView(R.layout.simple_frame_layout);
        int index = getIntent().getIntExtra("missingItemIndex", 0);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag("missing-items-detail-tag") == null) {
            transaction.replace(R.id.list_container, new ContactsMissingItemsDetailFragment(index), "missing-items-detail-tag");
        }
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.hapmultiselectmenu, menu);
        ViewUtil.setMenuItemsStateListIcon(getApplicationContext(), menu);
        return true;
    }
}
