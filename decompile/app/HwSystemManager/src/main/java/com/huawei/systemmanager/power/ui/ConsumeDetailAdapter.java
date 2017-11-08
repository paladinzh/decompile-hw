package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;

public class ConsumeDetailAdapter extends CommonAdapter<ConsumeDetailInfo> {
    private static final int ONE = 1;
    private static final int PROGRESS_FULL = 100;
    private static final String TAG = ConsumeDetailAdapter.class.getSimpleName();
    private int mAhMarginStart = 0;
    private int progressWidth = 0;

    private static class ConsumeDetailHolder {
        TextView consumeValue;
        ImageView icon_app;
        ProgressBar progress;
        TextView title;

        private ConsumeDetailHolder() {
        }
    }

    public ConsumeDetailAdapter(Context context, int screenWidth) {
        super(context);
        this.progressWidth = screenWidth - ((int) context.getResources().getDimension(R.dimen.battery_history_topapps_listItem_progress_margin));
        this.mAhMarginStart = (int) context.getResources().getDimension(R.dimen.battery_history_topapps_listItem_mAh_marginStart);
    }

    protected View newView(int position, ViewGroup parent, ConsumeDetailInfo item) {
        View convertView = this.mInflater.inflate(R.layout.consume_detail_softlist_item, parent, false);
        ConsumeDetailHolder mConsumeDetailHolder = new ConsumeDetailHolder();
        mConsumeDetailHolder.icon_app = (ImageView) convertView.findViewById(R.id.soft_icon_imageview);
        mConsumeDetailHolder.title = (TextView) convertView.findViewById(R.id.soft_title_textview);
        mConsumeDetailHolder.consumeValue = (TextView) convertView.findViewById(R.id.soft_content_textview);
        mConsumeDetailHolder.progress = (ProgressBar) convertView.findViewById(R.id.power_consume_progress_bar);
        convertView.setTag(mConsumeDetailHolder);
        return convertView;
    }

    protected void bindView(int position, View view, ConsumeDetailInfo item) {
        ConsumeDetailHolder holder = (ConsumeDetailHolder) view.getTag();
        holder.icon_app.setImageDrawable(item.getmIcon());
        holder.title.setText(item.getmPkgTitle());
        if (item.getmPowerValue() >= 1.0d) {
            holder.consumeValue.setText(String.format(getContext().getString(R.string.mah), new Object[]{Utility.getLocaleNumber((int) item.getmPowerValue())}));
        } else {
            holder.consumeValue.setText(String.format(getString(R.string.battery_history_below_one_mAh), new Object[]{Integer.valueOf(1)}));
        }
        int progressMarginEnd = (int) (((double) this.progressWidth) * (1.0d - (((double) item.getmPowerLevel()) / 100.0d)));
        if (progressMarginEnd >= this.progressWidth) {
            progressMarginEnd = this.progressWidth - 1;
        }
        MarginLayoutParams params1 = (MarginLayoutParams) holder.progress.getLayoutParams();
        params1.setMarginEnd(progressMarginEnd);
        holder.progress.setLayoutParams(params1);
        holder.progress.setProgress(100);
    }
}
