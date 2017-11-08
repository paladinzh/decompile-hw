package com.android.contacts.activities;

import android.os.Bundle;
import com.android.contacts.ContactsActivity;
import com.google.android.gms.R;

public class SmartGroupBrowserActivity extends ContactsActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        setTheme(R.style.PeopleTheme);
        setContentView(R.layout.smart_group_browser_activity);
    }
}
