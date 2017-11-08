package com.android.settings.applications;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class MemoryDetailActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969037);
        initActionBars();
        Intent intent = getIntent();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        ProcessStatsDetail detailsFragment = new ProcessStatsDetail();
        if (intent != null) {
            detailsFragment.setArguments(intent.getExtras());
        }
        fragmentTransaction.add(2131887016, detailsFragment);
        fragmentTransaction.commit();
    }

    private void initActionBars() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(2131627014);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }
}
