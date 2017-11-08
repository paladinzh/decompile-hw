package com.android.dialer.greeting;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class GreetingsActivity extends Activity {
    private static final String TAG = GreetingsActivity.class.getSimpleName();
    private GreetingsFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        if (savedInstanceState == null) {
            this.mFragment = new GreetingsFragment();
            fm.beginTransaction().replace(16908290, this.mFragment, TAG).commit();
            return;
        }
        this.mFragment = (GreetingsFragment) fm.findFragmentByTag(TAG);
    }

    public void onBackPressed() {
        if (this.mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
