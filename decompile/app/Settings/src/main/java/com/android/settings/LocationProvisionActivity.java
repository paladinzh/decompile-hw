package com.android.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class LocationProvisionActivity extends SettingsDrawerActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969254);
        TextView webText = (TextView) findViewById(2131887424);
        String url = Utils.getAssetPath(this, "location_provision.html", false);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        webText.setText(Html.fromHtml(Utils.getStringFromHtmlFile(this, url)));
        initActionBars();
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
