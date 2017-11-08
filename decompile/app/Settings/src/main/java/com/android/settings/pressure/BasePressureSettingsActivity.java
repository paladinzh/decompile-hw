package com.android.settings.pressure;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.android.settings.SettingsActivity;

public abstract class BasePressureSettingsActivity extends SettingsActivity {
    protected abstract String getFragmentClassName();

    public Intent getIntent() {
        Intent intent = super.getIntent();
        Bundle bundle = intent.getBundleExtra("ps_fragment_bundle");
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", getFragmentClassName());
        newIntent.putExtra(":settings:show_fragment_args", bundle);
        return newIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (getFragmentClassName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
