package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;

public class AuthenticationInformationActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968640);
        ContentResolver resolver = getContentResolver();
        TextView authenticationInfoText = (TextView) findViewById(2131886271);
        ImageView authenticationInfoImage = (ImageView) findViewById(2131886270);
        String customCertifyCode = System.getString(resolver, "custom_certify_code");
        String customCertifyPath = System.getString(resolver, "custom_certify_picture");
        if (TextUtils.isEmpty(customCertifyPath) && TextUtils.isEmpty(customCertifyCode)) {
            finish();
        }
        if (!TextUtils.isEmpty(customCertifyPath) && new File(customCertifyPath).exists()) {
            Bitmap bmpDefaultPic = BitmapFactory.decodeFile(customCertifyPath);
            if (bmpDefaultPic != null) {
                authenticationInfoImage.setImageBitmap(bmpDefaultPic);
            }
        }
        if (!TextUtils.isEmpty(customCertifyCode)) {
            authenticationInfoText.setText(customCertifyCode);
        }
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

    public static boolean shouldDisplay(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        String customCertifyCode = System.getString(context.getContentResolver(), "custom_certify_code");
        String customCertifyPath = System.getString(context.getContentResolver(), "custom_certify_picture");
        boolean isCustomCertifyFileExists = false;
        if (!TextUtils.isEmpty(customCertifyPath)) {
            isCustomCertifyFileExists = new File(customCertifyPath).exists();
        }
        if (!isCustomCertifyFileExists && TextUtils.isEmpty(customCertifyCode)) {
            z = false;
        }
        return z;
    }
}
