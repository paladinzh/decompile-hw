package com.huawei.systemmanager.shortcut;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;

public class ShortCutFragmentAdapter extends CommonAdapter<ShortCutInfoItem> {
    public ShortCutFragmentAdapter(Context context) {
        super(context);
    }

    protected View newView(int position, ViewGroup parent, ShortCutInfoItem item) {
        View view = getInflater().inflate(R.layout.common_list_item_twolines_image_switch, parent, false);
        ShortCutViewHolder viewHolder = new ShortCutViewHolder();
        viewHolder.mShortCutIcon = (ImageView) view.findViewById(R.id.image);
        viewHolder.mShortCutName = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
        viewHolder.mShortCutStatusDescription = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
        ShortCutHelper.setTextViewMultiLines(viewHolder.mShortCutStatusDescription);
        viewHolder.mIsInLauncher = (Switch) view.findViewById(R.id.switcher);
        if (ShortCutHelper.isInSimpleLauncher()) {
            viewHolder.mIsInLauncher.setEnabled(false);
        } else {
            viewHolder.mIsInLauncher.setEnabled(true);
        }
        view.setTag(viewHolder);
        return view;
    }

    protected void bindView(int position, View view, ShortCutInfoItem item) {
        ShortCutViewHolder viewHolder = (ShortCutViewHolder) view.getTag();
        viewHolder.mIsInLauncher.setChecked(item.mIsInLauncher);
        viewHolder.mIsInLauncher.setClickable(false);
        viewHolder.mShortCutIcon.setBackground(this.mContex.getResources().getDrawable(item.mShortCutIconResId));
        viewHolder.mShortCutName.setText(this.mContex.getResources().getString(item.mShortCutNameResId));
        viewHolder.mShortCutStatusDescription.setText(this.mContex.getResources().getString(item.mShortCutStatusDescriptionResId));
    }

    public boolean isEnabled(int position) {
        if (ShortCutHelper.isInSimpleLauncher()) {
            return false;
        }
        return super.isEnabled(position);
    }
}
