package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonEnterDeepItem;
import com.huawei.systemmanager.spacecleanner.utils.VedioCacheUtils;

public class EnterDeepType extends ViewType {

    public static class EnterDeepViewHolder {
        private TextView description;
        private final ImageView icon;
        private final EnterDeepType mType;
        private final ImageView tip;
        private final TextView title;

        public EnterDeepViewHolder(EnterDeepType type, View convertView) {
            this.mType = type;
            this.title = (TextView) convertView.findViewById(R.id.title);
            this.description = (TextView) convertView.findViewById(R.id.description);
            this.icon = (ImageView) convertView.findViewById(R.id.icon);
            this.tip = (ImageView) convertView.findViewById(R.id.red_tip);
        }

        void bindView(View convertView, CommonEnterDeepItem item) {
            if (this.mType != null) {
                this.mType.bindView(convertView, item);
            }
        }
    }

    public EnterDeepType(LayoutInflater inflater) {
        super(inflater);
    }

    int getType() {
        return 0;
    }

    void bindView(View convertView, CommonEnterDeepItem item) {
        EnterDeepViewHolder holder = (EnterDeepViewHolder) convertView.getTag();
        convertView.setTag(R.id.convertview_tag_item, item);
        holder.description.setText(item.getDescription());
        holder.title.setText(item.getName());
        Drawable drawable = item.getItemIcon();
        if (drawable != null) {
            holder.icon.setImageDrawable(drawable);
        }
        if (1 == item.getType() && VedioCacheUtils.isRedPoint()) {
            holder.tip.setVisibility(0);
        } else {
            holder.tip.setVisibility(8);
        }
    }

    public View newView(CommonEnterDeepItem item, ViewGroup parent) {
        View convertView = getInflater().inflate(R.layout.spaceclean_normal_cleanned_list_view, parent, false);
        convertView.setTag(new EnterDeepViewHolder(this, convertView));
        convertView.setOnClickListener(item.getClickListener());
        return convertView;
    }
}
