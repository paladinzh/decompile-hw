package com.huawei.optimizer.addetect.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.optimizer.addetect.IAdAppInfo;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.util.ArrayList;
import java.util.List;

public class AdDetailActivity extends HsmActivity implements OnClickListener {
    private static final String TAG = "AdDetailActivity";
    private IAdAppInfo mAppInfo;
    private Drawable mIcon;
    private boolean mIsFromMainScreenEnter = false;
    private boolean mIsItemDelete = false;

    private class AdInfoAdapter implements IAdAppInfo {
        private ScanResultEntity mEntity;

        public AdInfoAdapter(ScanResultEntity entity) {
            this.mEntity = entity;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }

        public int getAdPlatformSize() {
            return this.mEntity.mPlugNames.size();
        }

        public String getPackageName() {
            return this.mEntity.packageName;
        }

        public Drawable getIcon() {
            return this.mEntity.getAppIcon(AdDetailActivity.this.getApplicationContext());
        }

        public String getAppLabel() {
            return this.mEntity.appName;
        }

        public List<String> getAdPlatformInfoList() {
            return this.mEntity.mPlugNames;
        }

        public int getActionsSize() {
            return TextUtils.isEmpty(this.mEntity.mDescribtion) ? 0 : 1;
        }

        public List<String> getRiskActionsDescription() {
            return null;
        }

        public List<String> getCommonActionsDescription() {
            List<String> comments = new ArrayList();
            comments.add(this.mEntity.mDescribtion);
            return comments;
        }

        public String getApkVersion() {
            return this.mEntity.mVersion;
        }

        public boolean deleteApp(Context context) {
            return this.mEntity.delete(context);
        }

        public boolean isDeleted(Context context) {
            return this.mEntity.isDeleted(context);
        }

        public int getOperationDescripId() {
            return this.mEntity.getOperationDescripId();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addetect_app_detail);
        Intent intent = getIntent();
        if (intent != null) {
            ScanResultEntity entity = (ScanResultEntity) intent.getSerializableExtra("result");
            if (entity != null) {
                if (intent.getIntExtra(AntiVirusTools.KEY_FROM_MAIN_SCREEN_TO_AD, -1) > 0) {
                    z = true;
                }
                this.mIsFromMainScreenEnter = z;
                this.mAppInfo = new AdInfoAdapter(entity);
                ActionBar actionBar = getActionBar();
                actionBar.setTitle(R.string.title_details);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.show();
                this.mIcon = this.mAppInfo.getIcon();
                initView();
            }
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mAppInfo != null) {
            boolean z;
            this.mIsItemDelete = this.mAppInfo.isDeleted(getApplicationContext());
            if (this.mIsItemDelete) {
                z = false;
            } else {
                z = true;
            }
            setButtonEnabled(z);
        }
    }

    private void setButtonEnabled(boolean isDeleted) {
        int color;
        Button uninstallButton = (Button) findViewById(R.id.uninstall_button);
        uninstallButton.setEnabled(isDeleted);
        if (isDeleted) {
            color = getResources().getColor(R.color.hsm_forbidden);
        } else {
            color = getResources().getColor(R.color.hsm_widget_disable);
        }
        uninstallButton.setTextColor(color);
    }

    private void initView() {
        if (this.mAppInfo.getActionsSize() <= 0) {
            findViewById(R.id.ad_action_view).setVisibility(8);
        }
        if (this.mAppInfo.getAdPlatformSize() <= 0) {
            findViewById(R.id.ad_platform_view).setVisibility(8);
        }
        ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(this.mIcon);
        ((TextView) findViewById(R.id.app_name)).setText(this.mAppInfo.getAppLabel());
        ((TextView) findViewById(R.id.app_type)).setText(getString(R.string.addetect_detail_tip_text));
        Button uninstallButton = (Button) findViewById(R.id.uninstall_button);
        uninstallButton.setTextColor(getResources().getColor(R.color.hsm_forbidden));
        uninstallButton.setOnClickListener(this);
        uninstallButton.setText(this.mAppInfo.getOperationDescripId());
        ((TextView) findViewById(R.id.ad_risk_title)).setText(getString(R.string.app_ad_detail_groups_risk));
        ((TextView) findViewById(R.id.ad_risk_content)).setText(getContentString(this.mAppInfo.getCommonActionsDescription()));
        ((TextView) findViewById(R.id.ad_platform_title)).setText(getString(R.string.app_ad_detail_groups_plugin));
        ((TextView) findViewById(R.id.ad_platform_content)).setText(getContentString(this.mAppInfo.getAdPlatformInfoList()));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uninstall_button:
                boolean z;
                HsmStat.statAdUnistallButtonClick("d");
                this.mIsItemDelete = this.mAppInfo.deleteApp(getApplicationContext());
                if (this.mIsItemDelete) {
                    z = false;
                } else {
                    z = true;
                }
                setButtonEnabled(z);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, this.mAppInfo.getPackageName());
                HsmStat.statE((int) Events.E_AD_UNSTALL, statParam);
                if (this.mIsFromMainScreenEnter) {
                    String param = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, this.mAppInfo.getPackageName());
                    HsmStat.statE((int) Events.E_FROM_MAINSCREEN_TO_AD_UNINSTALL_VIRUS_ITEM, param);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private String getContentString(List<String> sList) {
        StringBuffer ret = new StringBuffer();
        int count = 1;
        int size = sList.size();
        for (String name : sList) {
            ret.append(count + "." + name);
            if (count != size) {
                ret.append("\n");
            }
            count++;
        }
        return ret.toString();
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
