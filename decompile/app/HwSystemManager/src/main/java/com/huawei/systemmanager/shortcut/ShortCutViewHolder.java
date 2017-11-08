package com.huawei.systemmanager.shortcut;

import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.ViewUtil;

/* compiled from: ShortCutFragmentAdapter */
class ShortCutViewHolder {
    Switch mIsInLauncher;
    ImageView mShortCutIcon;
    TextView mShortCutName;
    TextView mShortCutStatusDescription;

    public ShortCutViewHolder(View view) {
        this.mShortCutIcon = (ImageView) view.findViewById(R.id.image);
        this.mShortCutName = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
        this.mShortCutStatusDescription = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
        this.mIsInLauncher = (Switch) view.findViewById(R.id.switcher);
    }
}
