package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.util.HwLog;

public class DetailOfHardConsumptionActivity extends HsmActivity {
    private static final String TAG = "DetailOfHardConsumptionActivity";
    private TextView hardLabel;
    private String hardName = null;
    private Drawable icon;
    private ImageView iconView;
    private Button screenSettingBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_hardware_list);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.consuption_detail_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                this.hardName = data.getString("hardLabel");
            }
        }
        HwLog.d(TAG, "--hardName" + this.hardName);
        if (this.hardName == null) {
            HwLog.i(TAG, "hardName is null.");
            return;
        }
        this.hardLabel = (TextView) findViewById(R.id.hardware_name);
        this.hardLabel.setText(this.hardName);
        PowerStatsHelper helper = PowerStatsHelper.newInstance(getApplicationContext(), true);
        long rawRealTime = SystemClock.elapsedRealtime() * 1000;
        TextView cpuTimeOnValue = (TextView) findViewById(R.id.first_time_on_value);
        if (this.hardName.equals(getResources().getString(R.string.power_phone))) {
            this.icon = getResources().getDrawable(R.drawable.ic_call_battery);
            cpuTimeOnValue.setText(s2HMS(helper.getPhoneOnTime(rawRealTime)));
        }
        if (this.hardName.equals(getResources().getString(R.string.power_screen))) {
            this.icon = getResources().getDrawable(R.drawable.ic_settings_screen);
            this.screenSettingBtn = (Button) findViewById(R.id.screenBtn);
            this.screenSettingBtn.setVisibility(0);
            this.screenSettingBtn.setText(getString(R.string.display_setting));
            this.screenSettingBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.DISPLAY_SETTINGS");
                    DetailOfHardConsumptionActivity.this.startActivity(intent);
                }
            });
            cpuTimeOnValue.setText(s2HMS(helper.getScreenOnTime(rawRealTime)));
        }
        if (this.hardName.equals(getResources().getString(R.string.power_wifi_new))) {
            this.icon = getResources().getDrawable(R.drawable.ic_wifi_battery);
            cpuTimeOnValue.setText(s2HMS(helper.getWifiOnTime(rawRealTime)));
        }
        if (this.hardName.equals(getResources().getString(R.string.power_bluetooth))) {
            this.icon = getResources().getDrawable(R.drawable.ic_bluetooth_battery);
            cpuTimeOnValue.setText(s2HMS(helper.getBluetoothOnTime(rawRealTime)));
        }
        if (this.hardName.equals(getResources().getString(R.string.power_idle))) {
            this.icon = getResources().getDrawable(R.drawable.ic_standby_battery);
            cpuTimeOnValue.setText(s2HMS(helper.getIdleTime(rawRealTime)));
        }
        if (this.hardName.equals(getResources().getString(R.string.power_cell))) {
            this.icon = getResources().getDrawable(R.drawable.ic_signal_battery);
            cpuTimeOnValue.setText(s2HMS(helper.getRadioTime(rawRealTime)));
            ((RelativeLayout) findViewById(R.id.third_time_on_layout)).setVisibility(0);
            ((TextView) findViewById(R.id.third_time_on_value)).setText(s2HMS(helper.getRadioScanningTime(rawRealTime)));
        }
        this.iconView = (ImageView) findViewById(R.id.hardware_icon);
        this.iconView.setImageDrawable(this.icon);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    String s2HMS(long seconds) {
        if (seconds < 1) {
            return String.format(getResources().getString(R.string.power_time_under_s_new), new Object[]{Integer.valueOf(1)});
        }
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        String string;
        Object[] objArr;
        if (h > 0) {
            string = getResources().getString(R.string.power_time_h_m_s_connect);
            objArr = new Object[3];
            objArr[0] = getResources().getQuantityString(R.plurals.power_time_hour_array, (int) h, new Object[]{Long.valueOf(h)});
            objArr[1] = getResources().getQuantityString(R.plurals.power_time_min_array, (int) m, new Object[]{Long.valueOf(m)});
            objArr[2] = getResources().getQuantityString(R.plurals.power_time_s_array, (int) s, new Object[]{Long.valueOf(s)});
            return String.format(string, objArr);
        } else if (m > 0) {
            string = getResources().getString(R.string.power_time_connect);
            objArr = new Object[2];
            objArr[0] = getResources().getQuantityString(R.plurals.power_time_min_array, (int) m, new Object[]{Long.valueOf(m)});
            objArr[1] = getResources().getQuantityString(R.plurals.power_time_s_array, (int) s, new Object[]{Long.valueOf(s)});
            return String.format(string, objArr);
        } else {
            return getResources().getQuantityString(R.plurals.power_time_s_array, (int) s, new Object[]{Long.valueOf(s)});
        }
    }
}
