package com.huawei.gallery.photoshare.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.android.cg.vo.ShareReceiver;
import java.util.ArrayList;

public class PhotoShareReceiverListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ShareReceiver> mReceiverList;

    public PhotoShareReceiverListAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(ArrayList<ShareReceiver> receiverList) {
        this.mReceiverList = receiverList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.mReceiverList.remove(position);
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mReceiverList == null ? 0 : this.mReceiverList.size();
    }

    public Object getItem(int position) {
        return this.mReceiverList == null ? null : this.mReceiverList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ShareReceiver item = (ShareReceiver) getItem(position);
        if (convertView == null) {
            convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.photoshare_contacts_receiver_item, parent, false);
            convertView.setTag(R.id.photoshare_contacts_item_receiver_name, convertView.findViewById(R.id.photoshare_contacts_item_receiver_name));
        }
        ((TextView) convertView.getTag(R.id.photoshare_contacts_item_receiver_name)).setText(TextUtils.isEmpty(item.getReceiverName()) ? item.getReceiverAcc() : item.getReceiverName());
        return convertView;
    }
}
