package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;

public class NoIconTrashListAdapter extends BaseTrashListAdapter {

    private static class ViewHolder {
        final CheckBox checkBox;
        final TextView description;
        final TextView title;

        ViewHolder(TextView title, TextView des, CheckBox cb) {
            this.title = title;
            this.description = des;
            this.checkBox = cb;
        }
    }

    public NoIconTrashListAdapter(OnClickListener clickListener, Context context) {
        super(clickListener, null);
    }

    protected View newView(int position, ViewGroup parent, ITrashItem item) {
        View convertView = getInflater().inflate(R.layout.trash_list_noicon_item, parent, false);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView des = (TextView) convertView.findViewById(R.id.description);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkbox);
        cb.setOnClickListener(this.mCheckClicker);
        convertView.setTag(new ViewHolder(title, des, cb));
        return convertView;
    }

    protected void bindView(int position, View view, ITrashItem item) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (item.getName() != null) {
            holder.title.setText(item.getName().trim());
        } else {
            holder.title.setText("");
        }
        holder.description.setText(item.getDescription(GlobalContext.getContext()));
        holder.checkBox.setTag(item);
        holder.checkBox.setChecked(item.isChecked());
    }
}
