package com.huawei.systemmanager.secpatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.secpatch.common.SecurityPatchInfoBean;
import java.util.List;

public class SecurityPatchListAdapter extends BaseAdapter {
    private Context mContext;
    private List<SecurityPatchInfoBean> mSecPatchList;

    public SecurityPatchListAdapter(Context context, List<SecurityPatchInfoBean> secPatchList) {
        this.mContext = context;
        this.mSecPatchList = secPatchList;
    }

    public void setSecPatchInfo(List<SecurityPatchInfoBean> secPatchInfoList) {
        this.mSecPatchList = secPatchInfoList;
        notifyDataSetChanged();
    }

    public int getCount() {
        if (this.mSecPatchList == null || this.mSecPatchList.size() == 0) {
            return 0;
        }
        return this.mSecPatchList.size();
    }

    public Object getItem(int position) {
        return this.mSecPatchList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SecurityPatchItemHolder groupholder;
        if (convertView == null) {
            groupholder = new SecurityPatchItemHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.group_list, null);
            groupholder.mTitle = (TextView) convertView.findViewById(R.id.group_version_num);
            groupholder.mDescription = (TextView) convertView.findViewById(R.id.group_patch_num);
            convertView.setTag(groupholder);
        } else {
            groupholder = (SecurityPatchItemHolder) convertView.getTag();
        }
        groupholder.mTitle.setText(((SecurityPatchInfoBean) this.mSecPatchList.get(position)).getSecPatchPver());
        groupholder.mDescription.setText(getPatchDescriptionWithCount(this.mContext, ((SecurityPatchInfoBean) this.mSecPatchList.get(position)).getSecPatchNum()));
        return convertView;
    }

    private String getPatchDescriptionWithCount(Context context, int count) {
        return context.getResources().getString(R.string.Security_Patch_Count_Tip, new Object[]{Integer.valueOf(count)});
    }
}
