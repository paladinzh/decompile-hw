package com.android.settings.accessibility;

import android.app.Activity;
import android.os.Bundle;

public class PersistentNotification extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968609);
        setTitle(2131629235);
        addFragment();
    }

    private void addFragment() {
        getFragmentManager().beginTransaction().add(2131886200, new PersistentNotificationSettings(), "persistentNotification").commit();
    }
}
