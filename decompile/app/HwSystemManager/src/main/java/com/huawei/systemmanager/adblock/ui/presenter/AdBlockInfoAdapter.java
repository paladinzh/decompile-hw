package com.huawei.systemmanager.adblock.ui.presenter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.ui.view.AdBlockViewHolder;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.util.HwLog;

class AdBlockInfoAdapter extends CommonAdapter<AdBlock> {
    private static final String TAG = "AdBlockInfoAdapter";
    private final AdPresenterImpl mAdPresenter;

    private class AdItemClickListener implements OnCheckedChangeListener {
        private final int mPosition;

        AdItemClickListener(int position) {
            this.mPosition = position;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            HwLog.i(AdBlockInfoAdapter.TAG, "onCheckedChanged position=" + this.mPosition + ",isChecked=" + isChecked);
            AdBlockInfoAdapter.this.mAdPresenter.itemSwitchChanged(this.mPosition, isChecked);
        }
    }

    public AdBlockInfoAdapter(Context context, AdPresenterImpl adPresenter) {
        super(context);
        this.mAdPresenter = adPresenter;
    }

    protected View newView(int position, ViewGroup parent, AdBlock item) {
        View convertView = this.mInflater.inflate(R.layout.common_list_item_twolines_image_switch, parent, false);
        convertView.setTag(new AdBlockViewHolder(convertView));
        return convertView;
    }

    protected void bindView(int position, View view, AdBlock item) {
        ((AdBlockViewHolder) view.getTag()).setData(item, new AdItemClickListener(position));
    }
}
