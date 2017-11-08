package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;

class TagViewHolder extends ListViewHolder {
    TextView tvTagDescription;
    TextView tvTagTitle;
    View viewForEmptyTag;

    public TagViewHolder(View view) {
        super(view);
        this.tvTagTitle = (TextView) view.findViewById(R.id.tvTagName);
        this.tvTagDescription = (TextView) view.findViewById(R.id.tvTagEmpty);
        this.viewForEmptyTag = view.findViewById(R.id.dividerForEmptyTag);
    }

    public void setContentValue(Context mContext, AppInfoWrapper wrapper) {
        String titleText = wrapper.getTagText(mContext);
        String descriptText = wrapper.getTagDescription(mContext);
        this.tvTagTitle.setText(titleText);
        this.tvTagDescription.setText(descriptText);
        if (wrapper.getAppCount() > 0) {
            this.viewForEmptyTag.setVisibility(8);
            this.tvTagDescription.setVisibility(8);
        }
    }
}
