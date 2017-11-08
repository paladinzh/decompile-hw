package com.huawei.systemmanager.adblock.ui.view;

import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.comm.widget.ViewUtil;

public class AdBlockViewHolder {
    TextView mDescription;
    ImageView mIcon;
    Switch mSwitch;
    TextView mTitle;

    public AdBlockViewHolder(View convertView) {
        this.mIcon = (ImageView) convertView.findViewById(R.id.image);
        this.mTitle = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
        this.mDescription = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
        this.mSwitch = (Switch) convertView.findViewById(R.id.switcher);
    }

    public void setData(AdBlock adBlock, OnCheckedChangeListener listener) {
        this.mIcon.setImageDrawable(adBlock.getIcon());
        this.mDescription.setText(adBlock.isEnable() ? R.string.ad_blocked : R.string.ad_unblocked);
        this.mTitle.setText(adBlock.getLabel());
        this.mSwitch.setOnCheckedChangeListener(null);
        this.mSwitch.setChecked(adBlock.isEnable());
        this.mSwitch.setOnCheckedChangeListener(listener);
    }
}
