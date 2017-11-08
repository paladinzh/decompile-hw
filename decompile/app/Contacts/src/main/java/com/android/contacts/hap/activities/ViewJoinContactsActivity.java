package com.android.contacts.hap.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.android.contacts.ContactsActivity;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.list.ViewJoinedContactsFragment;
import com.google.android.gms.R;

public class ViewJoinContactsActivity extends ContactsActivity {
    private ViewJoinedContactsFragment mFragement;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        setContentView(R.layout.join_contacts_activity);
        this.mFragement = (ViewJoinedContactsFragment) getFragmentManager().findFragmentById(R.id.join_fragment);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.join_contacts));
        }
    }

    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onServiceCompleted(Intent callbackIntent) {
        this.mFragement.onServiceCompleted(callbackIntent);
    }
}
