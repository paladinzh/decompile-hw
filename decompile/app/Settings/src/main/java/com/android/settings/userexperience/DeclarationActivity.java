package com.android.settings.userexperience;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import com.android.settings.Utils;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class DeclarationActivity extends SettingsDrawerActivity {
    private static final String TAG = DeclarationActivity.class.getCanonicalName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(2130969254);
        ((TextView) findViewById(2131887423)).setText(2131627580);
        TextView webText = (TextView) findViewById(2131887424);
        boolean hasHwUE = Utils.hasPackageInfo(getPackageManager(), "com.huawei.bd");
        boolean hasHwLogUpload = Utils.hasPackageInfo(getPackageManager(), "com.huawei.logupload");
        String url = "";
        if (hasHwUE || !hasHwLogUpload) {
            url = Utils.getAssetPath(this, "user_improvement_plan.html", false);
        } else {
            url = Utils.getAssetPath(this, "user_improvement_plan_for_log_upload.html", false);
        }
        Log.d(TAG, "The url is " + url);
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "The user experience improvement file is empty.");
            finish();
            return;
        }
        webText.setText(Html.fromHtml(Utils.getStringFromHtmlFile(this, url)));
        webText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 != item.getItemId()) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
