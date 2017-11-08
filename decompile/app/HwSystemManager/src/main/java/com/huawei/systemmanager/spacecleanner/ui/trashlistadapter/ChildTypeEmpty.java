package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;

public class ChildTypeEmpty extends ChildType {

    private static class ViewHolder extends ChildViewHolder {
        public TextView title;

        public ViewHolder(ChildType type, View convertView) {
            super(type);
            this.title = (TextView) convertView.findViewById(R.id.title);
        }
    }

    public ChildTypeEmpty(LayoutInflater inflater) {
        super(inflater);
    }

    public View newView(int groupPosition, int childPosition, boolean isLastChild, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.spaceclean_trashlist_child_item_none, parent, false);
        convertView.findViewById(R.id.trash_size).setVisibility(8);
        convertView.setTag(new ViewHolder(this, convertView));
        return convertView;
    }

    public void bindView(boolean isLastChild, View convertView, ITrashItem trashItem) {
        ((ViewHolder) convertView.getTag()).title.setText(trashItem.getName());
        convertView.setTag(R.id.convertview_tag_item, trashItem);
    }

    int getType() {
        return 1;
    }
}
