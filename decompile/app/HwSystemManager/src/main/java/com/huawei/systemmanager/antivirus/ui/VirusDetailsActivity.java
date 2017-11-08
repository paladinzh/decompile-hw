package com.huawei.systemmanager.antivirus.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.Locale;

public class VirusDetailsActivity extends HsmActivity {
    private String LANGUAGE_ZH = ConstValues.CHINA_COUNTRY_CODE;
    private String TAG = "VirusDetailsActivity";
    private TextView mAppNameView = null;
    private TextView mAppTypeView = null;
    private Context mContext = null;
    private TextView mDangerLeverView = null;
    private ImageView mIconView = null;
    private boolean mIsItemDelete = false;
    private ScanResultEntity mResultEntity;
    private int mResultType = -1;
    private Button mUninstallButton = null;
    private TextView mVirusDetailsView = null;
    private TextView mVirusNameView = null;
    private LinearLayout mVirusdetailsLayout = null;

    private class ButtonClickListener implements OnClickListener {
        private ButtonClickListener() {
        }

        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.uninstall_button:
                    HwLog.v(VirusDetailsActivity.this.TAG, "onClick: uninstall");
                    VirusDetailsActivity.this.mIsItemDelete = VirusDetailsActivity.this.mResultEntity.delete(VirusDetailsActivity.this.mContext);
                    VirusDetailsActivity.this.setButtonEnabled(!VirusDetailsActivity.this.mIsItemDelete);
                    return;
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.virus_details);
        this.mContext = getApplicationContext();
        initActionBar();
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        this.mResultType = intent.getIntExtra(AntiVirusTools.RESULT_TYPE, -1);
        this.mResultEntity = (ScanResultEntity) intent.getSerializableExtra("result");
        if (this.mResultType == -1 || this.mResultEntity == null) {
            finish();
            return;
        }
        initView();
        initData();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.virus_details);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initView() {
        this.mVirusdetailsLayout = (LinearLayout) findViewById(R.id.virus_details_layout);
        if (!isLocaleZH()) {
            this.mVirusdetailsLayout.setVisibility(8);
            findViewById(R.id.view).setVisibility(8);
        }
        this.mIconView = (ImageView) findViewById(R.id.app_icon);
        this.mAppNameView = (TextView) findViewById(R.id.app_name);
        this.mAppTypeView = (TextView) findViewById(R.id.app_type);
        this.mVirusNameView = (TextView) findViewById(R.id.summary_virus_name);
        this.mDangerLeverView = (TextView) findViewById(R.id.summary_danger_lever);
        this.mVirusDetailsView = (TextView) findViewById(R.id.summary_details);
        this.mUninstallButton = (Button) findViewById(R.id.uninstall_button);
        this.mUninstallButton.setTextColor(getResources().getColor(R.color.hsm_forbidden));
        this.mUninstallButton.setOnClickListener(new ButtonClickListener());
        this.mUninstallButton.setText(this.mResultEntity.getOperationDescripId());
    }

    private void initData() {
        String appName = this.mResultEntity.appName;
        String virusName = this.mResultEntity.getVirusName(this.mContext);
        String dangerLeverText = this.mResultEntity.getDangerLevelText(this.mContext, this.mResultType);
        String appTypeText = this.mResultEntity.getAppTypeText(this.mContext, this.mResultType);
        String virusDetails = this.mResultEntity.getVirusDetail(this.mContext);
        Drawable icon = this.mResultEntity.getAppIcon(this.mContext);
        this.mAppNameView.setText(appName);
        this.mVirusNameView.setText(virusName);
        this.mDangerLeverView.setText(dangerLeverText);
        this.mAppTypeView.setText(appTypeText);
        this.mVirusDetailsView.setText(virusDetails);
        this.mIconView.setImageDrawable(icon);
        if (TextUtils.isEmpty(this.mResultEntity.virusInfo)) {
            this.mVirusdetailsLayout.setVisibility(8);
        }
    }

    private void setButtonEnabled(boolean enabled) {
        int color;
        this.mUninstallButton.setEnabled(enabled);
        Button button = this.mUninstallButton;
        if (enabled) {
            color = getResources().getColor(R.color.hsm_forbidden);
        } else {
            color = getResources().getColor(R.color.hsm_widget_disable);
        }
        button.setTextColor(color);
    }

    private boolean isLocaleZH() {
        String language = Locale.getDefault().getLanguage();
        if (language == null || !this.LANGUAGE_ZH.equals(language)) {
            return false;
        }
        return true;
    }

    protected void onResume() {
        super.onResume();
        this.mIsItemDelete = this.mResultEntity.isDeleted(this.mContext);
        setButtonEnabled(!this.mIsItemDelete);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finishActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(AntiVirusTools.DELETE_ITEM, this.mIsItemDelete);
        setResult(AntiVirusTools.RESULT_CODE, intent);
        finish();
    }
}
