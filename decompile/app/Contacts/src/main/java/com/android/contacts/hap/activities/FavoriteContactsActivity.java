package com.android.contacts.hap.activities;

import android.app.Activity;
import android.os.Bundle;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.util.ExceptionCapture;
import com.google.android.gms.R;

public class FavoriteContactsActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        setTheme(R.style.PeopleTheme);
        setContentView(R.layout.activity_favorite_contacts);
        ExceptionCapture.reportScene(12);
    }
}
