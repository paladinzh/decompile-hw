package com.huawei.systemmanager.secpatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchItem;
import java.util.List;
import java.util.Locale;

public class SingleVersionDetailListAdapter extends BaseAdapter {
    private Context mContext;
    private List<SecPatchItem> mSecPatchItemList;

    public SingleVersionDetailListAdapter(Context context, List<SecPatchItem> secPatchItemList) {
        this.mContext = context;
        this.mSecPatchItemList = secPatchItemList;
    }

    public void setSingleVersionDetailInfo(List<SecPatchItem> secPatchItemList) {
        this.mSecPatchItemList = secPatchItemList;
        notifyDataSetChanged();
    }

    public int getCount() {
        if (this.mSecPatchItemList == null) {
            return 0;
        }
        return this.mSecPatchItemList.size();
    }

    public Object getItem(int position) {
        return this.mSecPatchItemList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SecurityPatchItemHolder holder;
        if (convertView == null) {
            holder = new SecurityPatchItemHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.child_list, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.child_version_num);
            holder.mDescription = (TextView) convertView.findViewById(R.id.child_patch_num);
            convertView.setTag(holder);
        } else {
            holder = (SecurityPatchItemHolder) convertView.getTag();
        }
        holder.mTitle.setText(((SecPatchItem) this.mSecPatchItemList.get(position)).mOcid);
        if (ConstValues.CHINA_COUNTRY_CODE.equals(Locale.getDefault().getLanguage())) {
            holder.mDescription.setText(((SecPatchItem) this.mSecPatchItemList.get(position)).mDigest);
        } else {
            holder.mDescription.setText(((SecPatchItem) this.mSecPatchItemList.get(position)).mDigest_en);
        }
        return convertView;
    }
}
