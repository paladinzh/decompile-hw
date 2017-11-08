package com.huawei.systemmanager.spacecleanner.ui.upperview;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonEnterDeepItem;
import com.huawei.systemmanager.spacecleanner.utils.VedioCacheUtils;

public class SpaceCleanItemViewController {
    private final ViewGroup convertView;
    private final TextView description = ((TextView) this.convertView.findViewById(R.id.description));
    private final ImageView icon = ((ImageView) this.convertView.findViewById(R.id.icon));
    private CommonEnterDeepItem mItem;
    private final ImageView tip = ((ImageView) this.convertView.findViewById(R.id.red_tip));
    private final TextView title = ((TextView) this.convertView.findViewById(R.id.title));

    public SpaceCleanItemViewController(LayoutInflater inflater, ViewGroup parentView, CommonEnterDeepItem item) {
        this.convertView = (ViewGroup) inflater.inflate(R.layout.spaceclean_normal_cleanned_list_view, parentView, false);
        this.mItem = item;
        bindView();
    }

    public View getItemView() {
        return this.convertView;
    }

    private void bindView() {
        this.convertView.setOnClickListener(this.mItem.getClickListener());
        this.description.setText(this.mItem.getDescription());
        this.title.setText(this.mItem.getName());
        Drawable drawable = this.mItem.getItemIcon();
        if (drawable != null) {
            this.icon.setImageDrawable(drawable);
        }
        if (1 == this.mItem.getType() && VedioCacheUtils.isRedPoint()) {
            this.tip.setVisibility(0);
        } else {
            this.tip.setVisibility(8);
        }
    }
}
