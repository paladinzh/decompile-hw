package com.android.contacts.hap.numbermark;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class NumberMarkSettings extends Activity implements OnCheckedChangeListener {
    private Context mContext = null;
    private NumberMarkManager mMarkManager;
    private TextView mSpecialItemTV;
    private Switch mSwitch;
    private View mSwitchLayout;
    private TextView mSwitchSummary;
    private TextView mSwitchTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_in_right, R.anim.activity_out_left);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setContentView(R.layout.numbermark_settings);
        if (EmuiFeatureManager.isChinaArea()) {
            this.mContext = this;
            this.mSwitchTitle = (TextView) findViewById(R.id.title);
            this.mSwitchSummary = (TextView) findViewById(R.id.summary);
            this.mSwitchSummary.setVisibility(8);
            this.mSwitch = (Switch) findViewById(R.id.switchWidget);
            this.mSwitch.setClickable(false);
            this.mSwitchLayout = findViewById(R.id.numbermarkswitch);
            this.mSwitchLayout.setFocusable(true);
            this.mSwitchLayout.setFocusableInTouchMode(true);
            this.mSwitchLayout.requestFocus();
            this.mSwitchLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    NumberMarkSettings.this.mSwitch.setChecked(!NumberMarkSettings.this.mSwitch.isChecked());
                    NumberMarkSettings.this.onCheckedChanged(null, NumberMarkSettings.this.mSwitch.isChecked());
                }
            });
            this.mSwitchTitle.setText(R.string.numbermark_setting_switch_text);
            this.mSpecialItemTV = (TextView) findViewById(R.id.contact_numbermark_special_item);
            MarginLayoutParams margin = new MarginLayoutParams(this.mSpecialItemTV.getLayoutParams());
            if (ContactDpiAdapter.REAL_Dpi == 400) {
                margin.topMargin = (int) getResources().getDimension(R.dimen.numbermark_special_item_marigin_top_lowdpi);
            } else if (ContactDpiAdapter.REAL_Dpi == 440) {
                margin.topMargin = (int) getResources().getDimension(R.dimen.numbermark_special_item_marigin_top_middpi);
            } else {
                margin.topMargin = (int) getResources().getDimension(R.dimen.numbermark_special_item_marigin_top_hidpi);
            }
            this.mSpecialItemTV.setLayoutParams(new LayoutParams(margin));
            if (NumberMarkUtil.isUseNetwokMark(this)) {
                this.mSwitch.setChecked(true);
            } else {
                this.mSwitch.setChecked(false);
            }
            if (getActionBar() != null) {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }
            this.mMarkManager = new NumberMarkManager(this, null);
            return;
        }
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (HwLog.HWFLOW) {
            HwLog.i("NumberMarkSettings", "onCheckedChanged enableCloudMark : " + isChecked);
        }
        if (isChecked) {
            this.mMarkManager.enableCloudMark(true);
            StatisticalHelper.reportYellowPageTimes(this.mContext);
            return;
        }
        this.mMarkManager.enableCloudMark(false);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == 0) {
            super.onBackPressed();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onDestroy() {
        if (this.mMarkManager != null) {
            this.mMarkManager.destory();
        }
        super.onDestroy();
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_in_left, R.anim.activity_out_right);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        overridePendingTransition(R.anim.activity_in_left, R.anim.activity_out_right);
        finish();
        return true;
    }
}
