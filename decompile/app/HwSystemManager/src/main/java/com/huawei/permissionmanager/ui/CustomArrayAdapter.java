package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.huawei.systemmanager.R;

/* compiled from: TagViewHolder */
class CustomArrayAdapter<T> extends ArrayAdapter<T> {
    public CustomArrayAdapter(Context ctx, T[] objects) {
        super(ctx, R.layout.simple_list_item_single_choice_lixing, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text = (TextView) view.findViewById(16908308);
        if (2 == position) {
            text.setTextColor(getContext().getResources().getColor(R.color.hsm_forbidden));
        } else {
            text.setTextColor(getContext().getResources().getColor(R.color.emui_list_primary_text));
        }
        return view;
    }
}
