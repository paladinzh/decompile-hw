package com.huawei.gallery.panorama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;
import java.util.List;

public class ShareServicesAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context m_Context;
    private ServiceSelectClickListener serviceSelectClickListener;
    private List<ShareService> shareServices;

    public ShareServicesAdapter(Context context, List<ShareService> shareServices, ServiceSelectClickListener serviceSelectClickListener) {
        this.shareServices = shareServices;
        this.m_Context = context;
        this.serviceSelectClickListener = serviceSelectClickListener;
    }

    public int getCount() {
        return this.shareServices.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        this.inflater = (LayoutInflater) this.m_Context.getSystemService("layout_inflater");
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.share_service_item, parent, false);
        }
        final ShareService shareService = (ShareService) this.shareServices.get(position);
        ImageView serviceIcon = (ImageView) convertView.findViewById(R.id.service_icon);
        ((TextView) convertView.findViewById(R.id.service_label)).setText(shareService.label);
        serviceIcon.setImageDrawable(shareService.icon);
        convertView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ShareServicesAdapter.this.serviceSelectClickListener.onSelected(shareService);
            }
        });
        return convertView;
    }
}
