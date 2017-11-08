package com.android.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class FaqHelpActivity extends SettingsDrawerActivity implements OnClickListener {
    private TextView mConnectFailDetails;
    private TextView mConnectFailKnowMore;
    private TextView mConnectFailSolution;
    private int mDeviceType;
    private TextView mNotFoundDetails;
    private TextView mNotFoundKnowMore;
    private TextView mNotFoundSolution;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        this.mDeviceType = getIntent().getIntExtra("faq_device_type", 0);
        initView();
    }

    public void finish() {
        super.finish();
        new HwAnimationReflection(this).overrideTransition(2);
    }

    private void setConnectFailInfo() {
        String solutionMessage = String.format(getString(2131628878, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(10)}), new Object[0]);
        if (this.mNotFoundDetails != null) {
            this.mNotFoundDetails.setText(solutionMessage);
        }
        String connectedSolution = String.format(getString(2131628879, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7)}), new Object[0]);
        if (this.mConnectFailDetails != null) {
            this.mConnectFailDetails.setText(connectedSolution);
        }
    }

    private void initView() {
        setContentView(2130968778);
        this.mNotFoundSolution = (TextView) findViewById(2131886557);
        this.mNotFoundDetails = (TextView) findViewById(2131886558);
        this.mConnectFailSolution = (TextView) findViewById(2131886577);
        this.mConnectFailDetails = (TextView) findViewById(2131886578);
        if (1 == this.mDeviceType) {
            this.mNotFoundSolution.setText(2131628170);
            setConnectFailInfo();
            this.mConnectFailSolution.setText(2131628172);
            ItemUseStat.getInstance().handleClick(this, 2, "bt_not_found_know_more");
        } else if (2 == this.mDeviceType) {
            this.mNotFoundSolution.setText(2131628175);
            this.mNotFoundDetails.setText(2131628176);
            this.mConnectFailSolution.setText(2131628177);
            this.mConnectFailDetails.setText(2131628178);
            ItemUseStat.getInstance().handleClick(this, 2, "wifi_p2p_not_found_know_more");
        }
        this.mNotFoundKnowMore = (TextView) findViewById(2131886576);
        this.mConnectFailKnowMore = (TextView) findViewById(2131886579);
        if (Utils.hasPackageInfo(getPackageManager(), "com.huawei.phoneservice")) {
            this.mNotFoundKnowMore.setOnClickListener(this);
            this.mConnectFailKnowMore.setOnClickListener(this);
            return;
        }
        this.mNotFoundKnowMore.setVisibility(8);
        this.mConnectFailKnowMore.setVisibility(8);
    }

    public void onClick(View v) {
        String queryStr = null;
        int resId = v.getId();
        if (2131886576 == resId) {
            if (1 == this.mDeviceType) {
                queryStr = getString(2131628033);
            } else if (2 == this.mDeviceType) {
                queryStr = getString(2131628032);
            }
        } else if (2131886579 == resId) {
            if (1 == this.mDeviceType) {
                queryStr = getString(2131628036);
            } else if (2 == this.mDeviceType) {
                queryStr = getString(2131628031);
            }
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
