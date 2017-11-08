package com.android.contacts.preference;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceActivity.Header;
import android.view.MenuItem;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.list.ContactListFilter;
import com.google.android.gms.R;
import java.util.List;

public final class ContactsPreferenceActivity extends PreferenceActivity {
    private static final String TAG = ContactsPreferenceActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setTheme(R.style.ContactsPreferencesTheme);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(4, 4);
        }
    }

    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof OrganizeContactsFragment) {
            ((OrganizeContactsFragment) fragment).setContactListFilter((ContactListFilter) getIntent().getParcelableExtra("contactListFilter"));
        }
    }

    public static boolean isToRemoveOrderPreferences(Context context) {
        if (context.getResources().getBoolean(R.bool.config_sort_order_user_changeable) || context.getResources().getBoolean(R.bool.config_display_order_user_changeable)) {
            return false;
        }
        return true;
    }

    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return false;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
