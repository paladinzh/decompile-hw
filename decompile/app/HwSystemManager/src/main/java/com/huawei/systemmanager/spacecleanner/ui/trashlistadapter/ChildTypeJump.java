package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;
import com.huawei.systemmanager.spacecleanner.utils.VedioCacheUtils;

public class ChildTypeJump extends ChildType {
    private OnClickListener mItemClicker;

    private static class ViewHolder extends ChildViewHolder {
        private final CheckBox checkBox;
        private TextView description;
        private final ImageView icon;
        private ImageView lockIcon;
        private final ImageView tip;
        private final TextView title;

        public ViewHolder(ChildType type, View convertView) {
            super(type);
            this.title = (TextView) convertView.findViewById(R.id.text1);
            this.description = (TextView) convertView.findViewById(R.id.text2);
            this.lockIcon = (ImageView) convertView.findViewById(R.id.lock_icon);
            this.icon = (ImageView) convertView.findViewById(R.id.image);
            this.tip = (ImageView) convertView.findViewById(R.id.red_tip);
            this.checkBox = (CheckBox) convertView.findViewById(R.id.list_item_checkbox);
            ViewUtils.setVisibility(this.checkBox, 8);
        }
    }

    public ChildTypeJump(LayoutInflater inflater, OnClickListener itemClicker) {
        super(inflater);
        this.mItemClicker = itemClicker;
    }

    public View newView(int groupPosition, int childPosition, boolean isLastChild, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.spaceclean_expand_list_item_twolines_tip_lock_image_summary_checkbox, parent, false);
        convertView.setTag(new ViewHolder(this, convertView));
        convertView.setOnClickListener(this.mItemClicker);
        return convertView;
    }

    public void bindView(boolean isLastChild, View convertView, ITrashItem trashItem) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        convertView.setTag(R.id.convertview_tag_item, trashItem);
        holder.description.setText(trashItem.getDesctiption2());
        setTextViewMultiLines(holder.description);
        holder.title.setText(trashItem.getName());
        holder.lockIcon.setVisibility(4);
        Drawable drawable = trashItem.getItemIcon();
        if (drawable != null) {
            holder.icon.setImageDrawable(drawable);
        }
        if (VedioCacheUtils.isRedPoint()) {
            holder.tip.setVisibility(0);
        } else {
            holder.tip.setVisibility(8);
        }
    }

    private void setTextViewMultiLines(TextView text) {
        text.setSingleLine(false);
        text.setMaxLines(2);
    }

    int getType() {
        return 2;
    }
}
