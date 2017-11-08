package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FilenameFilter;

public class TelecInfo extends Activity {
    private static String CERTIFICATION_FILE_PATH = "/data/cust/certification";
    private static FilenameFilter mFilenameFilter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
            if (filename.endsWith(".png")) {
                return true;
            }
            return false;
        }
    };
    private HwCustTelecInfo mCustTelecInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mCustTelecInfo = (HwCustTelecInfo) HwCustUtils.createObj(HwCustTelecInfo.class, new Object[0]);
        setContentView(2130969210);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        drawPngs();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean hasCertification(Context context) {
        File[] files = new File(CERTIFICATION_FILE_PATH).listFiles(mFilenameFilter);
        boolean result = files != null && files.length > 0;
        HwCustTelecInfo custTelecInfo = (HwCustTelecInfo) HwCustUtils.createObj(HwCustTelecInfo.class, new Object[0]);
        if (custTelecInfo == null || !custTelecInfo.isHideCustomizedAuthen(context)) {
            return result;
        }
        return false;
    }

    private void drawPngs() {
        File[] files = new File(CERTIFICATION_FILE_PATH).listFiles(mFilenameFilter);
        if (files == null || files.length == 0) {
            finish();
            return;
        }
        LinearLayout linearLayout = (LinearLayout) findViewById(2131887251);
        LayoutParams params = new LayoutParams(-2, -2);
        float density = getResources().getDisplayMetrics().density;
        if (this.mCustTelecInfo != null) {
            files = this.mCustTelecInfo.filterAuthenticatePngs(files, this);
        }
        for (File pngFile : files) {
            Bitmap bitmap = BitmapFactory.decodeFile(pngFile.getPath());
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setPadding(0, (int) (5.0f * density), 0, (int) (5.0f * density));
            imageView.setImageBitmap(bitmap);
            linearLayout.addView(imageView);
        }
    }
}
