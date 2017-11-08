package com.android.systemui.usb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;
import fyusion.vislib.BuildConfig;

public class HwSDCardGuiderActivity extends Activity {
    private int mErrorCode = 0;
    private String mSDTitle = BuildConfig.FLAVOR;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int nActionType = getActionType();
        switch (nActionType) {
            case 0:
            case 1:
                initContentView(nActionType);
                return;
            case 2:
                redirectToVmall();
                return;
            default:
                Log.w("HwSDCardGuiderActivity", "onCreate: Invalid action type");
                finish();
                return;
        }
    }

    private int getActionType() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.w("HwSDCardGuiderActivity", "getActionType: Invalid Intent");
            return 0;
        }
        int nActionType = intent.getIntExtra("SD_ACTION_TYPE", 0);
        this.mSDTitle = intent.getStringExtra("storage_title");
        this.mErrorCode = intent.getIntExtra("error_code", 0);
        Log.i("HwSDCardGuiderActivity", "getActionType: mSDTitle = " + this.mSDTitle);
        Log.i("HwSDCardGuiderActivity", "getActionType: mErrorCode = " + this.mErrorCode);
        Log.i("HwSDCardGuiderActivity", "getActionType: action type = " + nActionType);
        return nActionType;
    }

    private void initContentView(int nActionType) {
        setContentView(R.layout.hw_sdcard_guider_activity);
        BDReporter.e(this, 71, "{" + this.mErrorCode + ":" + this.mSDTitle + "}");
        TextView vmallText = (TextView) findViewById(R.id.txt_vmall);
        vmallText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwSDCardGuiderActivity.this.redirectToVmall();
            }
        });
        if (isAbroad()) {
            vmallText.setVisibility(8);
        }
        if (1 == nActionType) {
            ((RelativeLayout) findViewById(R.id.layout_format)).setVisibility(0);
            vmallText.setVisibility(8);
            ((Button) findViewById(R.id.btn_format)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HwSDCardGuiderActivity.this.tryFromatSdCard();
                }
            });
        }
    }

    private void redirectToVmall() {
        if (isAbroad()) {
            finish();
            return;
        }
        BDReporter.e(this, 71, "{" + this.mErrorCode + ":" + this.mSDTitle + "}");
        BDReporter.e(this, 72, "{" + this.mErrorCode + ":" + this.mSDTitle + "}");
        if (!tryRedirectToVMallClient()) {
            tryRedirectToBroswer();
        }
        finish();
    }

    private boolean tryRedirectToVMallClient() {
        try {
            Log.i("HwSDCardGuiderActivity", "tryRedirectToVMallClient");
            ComponentName component = new ComponentName("com.vmall.client", "com.vmall.client.activity.VmallWapActivity");
            Intent intent = new Intent();
            intent.setComponent(component);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            HwLog.e("HwSDCardGuiderActivity", "tryRedirectToVMallClient: Exception", e);
            return false;
        }
    }

    private boolean tryRedirectToBroswer() {
        try {
            Log.i("HwSDCardGuiderActivity", "tryRedirectToBroswer");
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://m.vmall.com")));
            return true;
        } catch (Exception e) {
            HwLog.e("HwSDCardGuiderActivity", "tryRedirectToBroswer: Exception", e);
            return false;
        }
    }

    private void tryFromatSdCard() {
        try {
            Log.i("HwSDCardGuiderActivity", "tryFromatSdCard");
            String volumeId = getIntent().getStringExtra("android.os.storage.extra.VOLUME_ID");
            String diskId = getIntent().getStringExtra("android.os.storage.extra.DISK_ID");
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardFormatConfirm");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", volumeId);
            intent.putExtra("android.os.storage.extra.DISK_ID", diskId);
            startActivity(intent);
        } catch (Exception e) {
            HwLog.e("HwSDCardGuiderActivity", "tryFromatSdCard: Exception", e);
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isAbroad() {
        return !SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }
}
