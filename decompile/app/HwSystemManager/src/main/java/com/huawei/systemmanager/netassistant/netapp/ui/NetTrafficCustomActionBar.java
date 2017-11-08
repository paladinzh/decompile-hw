package com.huawei.systemmanager.netassistant.netapp.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.traffic.roamingtraffic.RoamingTrafficListActivity;
import com.huawei.systemmanager.util.HwLog;

public class NetTrafficCustomActionBar {
    private static final String TAG = "NetTrafficCustomActionBar";
    private TextView mActionBarTitle;
    private Activity mActivity;
    private ActionBar mBar = this.mActivity.getActionBar();
    private ImageView mEndBtn;

    public NetTrafficCustomActionBar(Activity activity) {
        this.mActivity = activity;
        if (this.mBar != null) {
            this.mBar.setDisplayUseLogoEnabled(false);
            this.mBar.setDisplayHomeAsUpEnabled(false);
            this.mBar.setDisplayShowTitleEnabled(false);
            this.mBar.setDisplayShowHomeEnabled(false);
            this.mBar.setDisplayShowCustomEnabled(true);
            this.mBar.setCustomView(R.layout.net_traffic_custom_actionbar);
            View view = this.mBar.getCustomView();
            if (view == null) {
                HwLog.i(TAG, "ActionBar customview is null!");
                return;
            }
            this.mActionBarTitle = (TextView) view.findViewById(R.id.head_title);
            this.mEndBtn = (ImageView) view.findViewById(R.id.head_right_btn);
        }
    }

    public void setEndIcon(int id) {
        try {
            setEndIcon(this.mActivity.getResources().getDrawable(id));
        } catch (NotFoundException e) {
            HwLog.i(TAG, "setStartIcon error : " + e.toString());
        }
    }

    public void setEndIcon(Drawable drawable) {
        if (!isActionBarAvailable()) {
            HwLog.i(TAG, "setEndIcon error : !isActionBarAvailable()");
        } else if (this.mEndBtn != null) {
            this.mEndBtn.setVisibility(0);
            this.mEndBtn.setBackgroundDrawable(drawable);
            this.mEndBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent i = new Intent();
                    i.setClass(NetTrafficCustomActionBar.this.mActivity, RoamingTrafficListActivity.class);
                    NetTrafficCustomActionBar.this.mActivity.startActivity(i);
                }
            });
        }
    }

    public void setTitle(int id) {
        if (isActionBarAvailable()) {
            if (this.mActionBarTitle != null) {
                this.mActionBarTitle.setText(id);
            }
            this.mActivity.setTitle(id);
            return;
        }
        HwLog.i(TAG, "setTitle error : !isActionBarAvailable()");
    }

    public void setTitle(String title) {
        if (isActionBarAvailable()) {
            if (this.mActionBarTitle != null) {
                this.mActionBarTitle.setText(title);
            }
            this.mActivity.setTitle(title);
            return;
        }
        HwLog.i(TAG, "setTitle error : !isActionBarAvailable()");
    }

    private boolean isActionBarAvailable() {
        return this.mBar != null;
    }
}
