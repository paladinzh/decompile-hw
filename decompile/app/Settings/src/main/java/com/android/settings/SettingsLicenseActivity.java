package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

public class SettingsLicenseActivity extends Activity {
    private LicenseHelper mLicenseHelper;

    protected void onCreate(Bundle savedInstanceState) {
        Uri uri;
        super.onCreate(savedInstanceState);
        this.mLicenseHelper = new LicenseHelper(this);
        String path;
        File file;
        if (this.mLicenseHelper.isFlyHorse30()) {
            boolean needUpdate = this.mLicenseHelper.updateMd5IfNeed();
            path = this.mLicenseHelper.getLicenseHtml();
            if (needUpdate || TextUtils.isEmpty(path)) {
                this.mLicenseHelper.rebuildLicenseFile();
            }
            file = new File(path);
            if (!file.exists() || file.length() == 0) {
                Log.e("SettingsLicenseActivity", "License file " + path + " does not exist");
                showErrorAndFinish();
                return;
            }
            try {
                uri = FileProvider.getUriForFile(this, "com.android.settings.files", file);
            } catch (Exception e) {
                Log.e("SettingsLicenseActivity", "build license err: invaid file path");
                showErrorAndFinish();
                return;
            }
        }
        path = SystemProperties.get("ro.config.license_path", "/system/etc/NOTICE.html.gz");
        if (TextUtils.isEmpty(path)) {
            Log.e("SettingsLicenseActivity", "The system property for the license file is empty");
            showErrorAndFinish();
            return;
        }
        file = new File(path);
        if (!file.exists() || file.length() == 0) {
            Log.e("SettingsLicenseActivity", "License file " + path + " does not exist");
            showErrorAndFinish();
            return;
        }
        uri = Uri.fromFile(file);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        intent.addFlags(1);
        intent.putExtra("android.intent.extra.TITLE", getString(2131625515));
        intent.putExtra("from_package", getPackageName());
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setPackage("com.android.htmlviewer");
        try {
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e2) {
            Log.e("SettingsLicenseActivity", "Failed to find viewer" + e2.toString());
            showErrorAndFinish();
        }
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, 2131627299, 1).show();
        finish();
        if (this.mLicenseHelper != null) {
            this.mLicenseHelper.clearCache();
        }
    }
}
