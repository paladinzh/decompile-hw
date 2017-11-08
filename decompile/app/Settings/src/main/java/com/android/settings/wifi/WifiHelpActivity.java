package com.android.settings.wifi;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.settings.HwAnimationReflection;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class WifiHelpActivity extends SettingsDrawerActivity implements OnClickListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initView();
    }

    public void onResume() {
        super.onResume();
    }

    public void finish() {
        super.finish();
        new HwAnimationReflection(this).overrideTransition(2);
    }

    private void initView() {
        setContentView(2130969279);
        TextView wifiApView = (TextView) findViewById(2131887551);
        TextView wifiConnectView = (TextView) findViewById(2131887554);
        TextView wifiLoginView = (TextView) findViewById(2131887557);
        TextView tv2 = (TextView) findViewById(2131887553);
        TextView tv3 = (TextView) findViewById(2131887556);
        ((TextView) findViewById(2131887550)).setText(String.format(getResources().getString(2131628047, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}), new Object[0]));
        tv2.setText(String.format(getResources().getString(2131628049, new Object[]{Integer.valueOf(1), Integer.valueOf(8), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}), new Object[0]));
        tv3.setText(String.format(getResources().getString(2131628051, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}), new Object[0]));
        if (Utils.hasPackageInfo(getPackageManager(), "com.huawei.phoneservice")) {
            wifiApView.setOnClickListener(this);
            wifiConnectView.setOnClickListener(this);
            wifiLoginView.setOnClickListener(this);
            return;
        }
        wifiApView.setVisibility(8);
        wifiConnectView.setVisibility(8);
        wifiLoginView.setVisibility(8);
    }

    public void onClick(View v) {
        String queryStr = null;
        int resId = v.getId();
        if (2131887551 == resId) {
            queryStr = getString(2131628028);
        } else if (2131887554 == resId) {
            queryStr = getString(2131628029);
        } else if (2131887557 == resId) {
            queryStr = getString(2131628030);
        }
        SettingsExtUtils.transferToSmartHelper(this, queryStr);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
