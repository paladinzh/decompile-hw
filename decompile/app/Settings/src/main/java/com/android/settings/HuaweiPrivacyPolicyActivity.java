package com.android.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class HuaweiPrivacyPolicyActivity extends SettingsDrawerActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969254);
        ((TextView) findViewById(2131887423)).setText(2131627418);
        TextView webText = (TextView) findViewById(2131887424);
        String url = Utils.getAssetPath(this, "huawei_privacy_policy.html", false);
        Log.d("HuaweiPrivacyPolicyActivity", "The url is " + url);
        if (TextUtils.isEmpty(url)) {
            Log.e("HuaweiPrivacyPolicyActivity", "The huawei privacy policy file is empty.");
            finish();
            return;
        }
        webText.setText(Html.fromHtml(Utils.getStringFromHtmlFile(this, url)));
        webText.setMovementMethod(LinkMovementMethod.getInstance());
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
