package com.huawei.rcs.utils.map.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.rcs.utils.map.abs.RcsMapFragment.AddressData;
import java.util.ArrayList;

public class RcsMapAdapter extends BaseAdapter {
    private ArrayList<AddressData> addressList;
    int clickposition = -1;
    int count;
    private Context mContext;

    private static class ViewHolder {
        ImageView imageView;
        TextView subView;
        TextView titleView;

        private ViewHolder() {
        }
    }

    public RcsMapAdapter(Context context, ArrayList<AddressData> list) {
        this.mContext = context;
        this.addressList = list;
    }

    public void setItemCount(int nums) {
        this.count = nums;
    }

    public int getItemCount() {
        return this.count;
    }

    public int getCount() {
        return getItemCount();
    }

    public Object getItem(int position) {
        return this.addressList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View contentview, ViewGroup parent) {
        ViewHolder viewhHolder;
        if (contentview == null) {
            contentview = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.rcs_map_item, null);
            viewhHolder = new ViewHolder();
            viewhHolder.titleView = (TextView) contentview.findViewById(R.id.mapitem_title);
            viewhHolder.subView = (TextView) contentview.findViewById(R.id.mapitem_sub);
            viewhHolder.imageView = (ImageView) contentview.findViewById(R.id.mapitem_image);
            contentview.setTag(viewhHolder);
        } else {
            viewhHolder = (ViewHolder) contentview.getTag();
        }
        viewhHolder.titleView.setText(((AddressData) this.addressList.get(position)).title);
        viewhHolder.subView.setText(((AddressData) this.addressList.get(position)).subTitle);
        if (position == this.clickposition) {
            viewhHolder.imageView.setImageResource(R.drawable.ic_sms_location_checked);
            viewhHolder.subView.setTextColor(this.mContext.getResources().getColor(R.color.attachment_tab_button_selected));
        } else {
            viewhHolder.imageView.setImageResource(R.drawable.ic_sms_location);
            viewhHolder.subView.setTextColor(this.mContext.getResources().getColor(R.color.attachment_tab_button_unselected));
        }
        return contentview;
    }
}
