package cn.com.xy.sms.sdk.ui.popu.web;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;

public class NearbyPointListViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mListContainer = LayoutInflater.from(this.mContext);
    private ArrayList<HashMap<String, Object>> mListItems;

    private static class NearbyPointListItemView {
        public TextView nearbyPointAddressTextView;
        public TextView nearbyPointDistanceTextView;
        public TextView nearbyPointNameTextView;

        private NearbyPointListItemView() {
        }
    }

    public NearbyPointListViewAdapter(Context context, ArrayList<HashMap<String, Object>> listItems) {
        this.mContext = context;
        this.mListItems = listItems;
    }

    public int getCount() {
        if (this.mListItems == null) {
            return 0;
        }
        return this.mListItems.size();
    }

    public Object getItem(int arg0) {
        return Integer.valueOf(arg0);
    }

    public long getItemId(int arg0) {
        return (long) arg0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        NearbyPointListItemView nearbyPointListItemView;
        if (convertView == null) {
            nearbyPointListItemView = new NearbyPointListItemView();
            convertView = this.mListContainer.inflate(R.layout.duoqu_nearby_point_list_item, null);
            nearbyPointListItemView.nearbyPointNameTextView = (TextView) convertView.findViewById(R.id.duoqu_tv_nearby_point_name);
            nearbyPointListItemView.nearbyPointAddressTextView = (TextView) convertView.findViewById(R.id.duoqu_tv_nearby_point_address);
            nearbyPointListItemView.nearbyPointDistanceTextView = (TextView) convertView.findViewById(R.id.duoqu_tv_nearby_point_distance);
            convertView.setTag(nearbyPointListItemView);
        } else {
            nearbyPointListItemView = (NearbyPointListItemView) convertView.getTag();
        }
        try {
            HashMap<String, Object> mapItem = (HashMap) this.mListItems.get(position);
            nearbyPointListItemView.nearbyPointNameTextView.setText((String) mapItem.get("name"));
            nearbyPointListItemView.nearbyPointAddressTextView.setText((String) mapItem.get("address"));
            Object distance = mapItem.get("distance");
            if (distance != null) {
                nearbyPointListItemView.nearbyPointDistanceTextView.setText(getDistanceString(Double.parseDouble(distance.toString())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private String getDistanceString(double distance) {
        if (distance > 1000.0d) {
            return String.format("%.1fkm", new Object[]{Double.valueOf(distance / 1000.0d)}).replace(".0", "");
        }
        return String.format("%.0fm", new Object[]{Double.valueOf(distance)});
    }
}
