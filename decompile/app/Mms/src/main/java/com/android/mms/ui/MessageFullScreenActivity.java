package com.android.mms.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MessageFullScreenActivity extends Activity {
    MessageFullScreenFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setSoftInputMode(20);
        this.mFragment = new MessageFullScreenFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }

    public void onBackPressed() {
        this.mFragment.onBackPressed();
        super.onBackPressed();
    }
}
