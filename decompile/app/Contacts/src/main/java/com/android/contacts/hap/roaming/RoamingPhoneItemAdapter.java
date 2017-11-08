package com.android.contacts.hap.roaming;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.android.gms.R;
import java.util.List;

public class RoamingPhoneItemAdapter extends ArrayAdapter<RoamingPhoneItem> {
    private Context context;
    private LayoutInflater mlayoutInflater = ((LayoutInflater) this.context.getSystemService("layout_inflater"));

    public RoamingPhoneItemAdapter(Context context, List<RoamingPhoneItem> list, int interactionType) {
        super(context, R.layout.select_dialog_item, list);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = this.mlayoutInflater.inflate(R.layout.select_dialog_item, null);
        }
        RoamingPhoneItem item = (RoamingPhoneItem) getItem(position);
        ((TextView) view.findViewById(16908308)).setText(String.format(this.context.getString(R.string.roaming_dial_by_country), new Object[]{item.country, item.phoneNumber}));
        return view;
    }
}
