package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class HuaweiSafetyInfoActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getIntent().getStringExtra("android.intent.extra.TITLE");
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        if (isFileExist(getIntent().getStringExtra("file_name"))) {
            File file = new File(getFullFilePath(getIntent().getStringExtra("file_name")));
            String canonicalPath = "";
            try {
                canonicalPath = file.getCanonicalPath();
            } catch (IOException e1) {
                Log.e("HuaweiSafetyInfoActivity", "Failed to getCanonicalPath" + e1.toString());
            }
            if (TextUtils.isEmpty(canonicalPath) || canonicalPath.startsWith("/system/etc/safety_certification")) {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(Uri.fromFile(file), "text/html");
                intent.putExtra("android.intent.extra.TITLE", getIntent().getStringExtra("android.intent.extra.TITLE"));
                intent.putExtra("from_package", getPackageName());
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setPackage("com.android.htmlviewer");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("HuaweiSafetyInfoActivity", "Failed to find viewer" + e.toString());
                } finally {
                    finish();
                }
                return;
            }
            Log.e("HuaweiSafetyInfoActivity", "attempt to access outer file:" + canonicalPath);
            finish();
            return;
        }
        finish();
    }

    public static String getFullFilePath(String fileName) {
        return Utils.getFilePath("/system/etc/safety_certification", fileName);
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(getFullFilePath(fileName));
        if (file.exists() && file.length() != 0) {
            return true;
        }
        Log.e("HuaweiSafetyInfoActivity", "Safety info file " + file.getName() + " does not exist");
        return false;
    }
}
