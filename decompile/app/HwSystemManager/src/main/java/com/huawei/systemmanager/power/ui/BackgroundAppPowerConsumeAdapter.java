package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.os.BatterySipper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.power.util.L2MAdapter;
import com.huawei.systemmanager.util.HwLog;

public class BackgroundAppPowerConsumeAdapter extends CommonAdapter<BackgroundConsumeInfo> {
    private static final String TAG = BackgroundAppPowerConsumeAdapter.class.getSimpleName();
    private OnCheckedChangeListener mOnCheckedChangeListener;

    private static class BgConsumeHolder {
        CheckBox checker;
        ImageView icon_app;
        TextView indicator;
        TextView isShareUid;
        TextView title;

        private BgConsumeHolder() {
        }
    }

    public BackgroundAppPowerConsumeAdapter(Context context, OnCheckedChangeListener listener) {
        super(context);
        this.mOnCheckedChangeListener = listener;
    }

    protected View newView(int position, ViewGroup parent, BackgroundConsumeInfo item) {
        View convertView = this.mInflater.inflate(R.layout.backgroundapp_consume_detail, parent, false);
        BgConsumeHolder mBackgroundAppConsumeView = new BgConsumeHolder();
        mBackgroundAppConsumeView.icon_app = (ImageView) convertView.findViewById(R.id.app_icon);
        mBackgroundAppConsumeView.title = (TextView) convertView.findViewById(R.id.app_name);
        mBackgroundAppConsumeView.indicator = (TextView) convertView.findViewById(R.id.consume_level_description);
        mBackgroundAppConsumeView.checker = (CheckBox) convertView.findViewById(R.id.app_active);
        mBackgroundAppConsumeView.isShareUid = (TextView) convertView.findViewById(R.id.isShareUidApp);
        convertView.setTag(mBackgroundAppConsumeView);
        return convertView;
    }

    protected void bindView(int position, View view, BackgroundConsumeInfo item) {
        BgConsumeHolder holder = (BgConsumeHolder) view.getTag();
        holder.checker.setTag(Integer.valueOf(position));
        holder.checker.setOnCheckedChangeListener(null);
        holder.checker.setChecked(item.ismIsChecked());
        holder.checker.setOnCheckedChangeListener(this.mOnCheckedChangeListener);
        holder.icon_app.setImageDrawable(item.getmIcon());
        holder.title.setText(item.getmPkgTitle());
        if (item.ismIsSharedId()) {
            holder.isShareUid.setVisibility(0);
        } else {
            holder.isShareUid.setVisibility(8);
        }
        int consumeAmount = item.getmPowerLevel();
        HwLog.i(TAG, " consumeAmount = " + consumeAmount + "appTitle = " + item.getmPkgTitle());
        int rogueType = item.getmRogueType();
        if (rogueType == 2) {
            holder.indicator.setText(getString(R.string.power_rogue_reason_holdlock_info));
            holder.indicator.setVisibility(0);
        } else if (rogueType == 1) {
            holder.indicator.setText(getString(R.string.rogue_wakeup_info));
            holder.indicator.setVisibility(0);
        } else if (rogueType == 3) {
            holder.indicator.setText(getContext().getResources().getString(R.string.power_rogue_reason_gpslocation_consume_info));
            holder.indicator.setVisibility(0);
        } else {
            holder.indicator.setVisibility(8);
        }
        if (consumeAmount >= 10) {
            if (consumeAmount >= 500) {
                BatterySipper sip = item.getmSipper();
                float hours = (float) (((((double) (((float) L2MAdapter.cpuTime(sip)) - ((float) L2MAdapter.cpuFgTime(sip)))) / 1000.0d) / 60.0d) / 60.0d);
                float costPerHour = ((float) consumeAmount) / hours;
                HwLog.i(TAG, " consumeAmount=" + consumeAmount + " sip.cpuTime =" + L2MAdapter.cpuTime(sip) + " sip.cpuFgTime=" + L2MAdapter.cpuFgTime(sip) + " hours=" + hours + " costPerHour =" + costPerHour);
                if (costPerHour >= 500.0f) {
                    HwLog.i(TAG, "BackgroundAppPowerConsumeAdapter super high power apps uid =" + item.getmUid() + "  item.mPkgName =" + item.getmPkgName());
                }
            } else if (consumeAmount <= 40) {
            }
        } else if (consumeAmount >= 1) {
            HwLog.i(TAG, "consumeAmount >= 1 ");
        }
    }
}
