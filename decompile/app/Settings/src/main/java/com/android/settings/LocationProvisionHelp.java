package com.android.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class LocationProvisionHelp extends SettingsDrawerActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968855);
        initSummaryText();
        initActionBars();
    }

    private void initSummaryText() {
        TextView summary2 = (TextView) findViewById(2131886769);
        ((TextView) findViewById(2131886768)).setText(String.format(getResources().getString(2131628904, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6)}), new Object[0]));
        summary2.setText(String.format(getResources().getString(2131628905, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}), new Object[0]));
    }

    private void initActionBars() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
