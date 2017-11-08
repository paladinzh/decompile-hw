package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.DataUsageGraph;
import java.text.DecimalFormat;

public class DataUsageDetailView extends LinearLayout {
    private final DecimalFormat FORMAT = new DecimalFormat("#.##");

    public DataUsageDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(this, 16908310, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_text, R.dimen.qs_data_usage_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_carrier_text, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_info_top_text, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_period_text, R.dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, R.id.usage_info_bottom_text, R.dimen.qs_data_usage_text_size);
    }

    public void bind(DataUsageInfo info) {
        int titleId;
        long bytes;
        String top;
        Resources res = this.mContext.getResources();
        int usageColor = R.color.system_accent_color;
        CharSequence bottom = null;
        Object[] objArr;
        if (info.usageLevel < info.warningLevel || info.limitLevel <= 0) {
            titleId = R.string.quick_settings_cellular_detail_data_usage;
            bytes = info.usageLevel;
            objArr = new Object[1];
            objArr[0] = formatBytes(info.warningLevel);
            top = res.getString(R.string.quick_settings_cellular_detail_data_warning, objArr);
        } else if (info.usageLevel <= info.limitLevel) {
            titleId = R.string.quick_settings_cellular_detail_remaining_data;
            bytes = info.limitLevel - info.usageLevel;
            objArr = new Object[1];
            objArr[0] = formatBytes(info.usageLevel);
            top = res.getString(R.string.quick_settings_cellular_detail_data_used, objArr);
            objArr = new Object[1];
            objArr[0] = formatBytes(info.limitLevel);
            bottom = res.getString(R.string.quick_settings_cellular_detail_data_limit, objArr);
        } else {
            titleId = R.string.quick_settings_cellular_detail_over_limit;
            bytes = info.usageLevel - info.limitLevel;
            objArr = new Object[1];
            objArr[0] = formatBytes(info.usageLevel);
            top = res.getString(R.string.quick_settings_cellular_detail_data_used, objArr);
            objArr = new Object[1];
            objArr[0] = formatBytes(info.limitLevel);
            bottom = res.getString(R.string.quick_settings_cellular_detail_data_limit, objArr);
            usageColor = R.color.system_warning_color;
        }
        ((TextView) findViewById(16908310)).setText(titleId);
        TextView usage = (TextView) findViewById(R.id.usage_text);
        usage.setText(formatBytes(bytes));
        usage.setTextColor(this.mContext.getColor(usageColor));
        ((DataUsageGraph) findViewById(R.id.usage_graph)).setLevels(info.limitLevel, info.warningLevel, info.usageLevel);
        ((TextView) findViewById(R.id.usage_carrier_text)).setText(info.carrier);
        ((TextView) findViewById(R.id.usage_period_text)).setText(info.period);
        TextView infoTop = (TextView) findViewById(R.id.usage_info_top_text);
        infoTop.setVisibility(top != null ? 0 : 8);
        infoTop.setText(top);
        TextView infoBottom = (TextView) findViewById(R.id.usage_info_bottom_text);
        infoBottom.setVisibility(bottom != null ? 0 : 8);
        infoBottom.setText(bottom);
    }

    private String formatBytes(long bytes) {
        double val;
        String suffix;
        int i;
        long b = Math.abs(bytes);
        if (((double) b) > 1.048576E8d) {
            val = ((double) b) / 1.073741824E9d;
            suffix = "GB";
        } else if (((double) b) > 102400.0d) {
            val = ((double) b) / 1048576.0d;
            suffix = "MB";
        } else {
            val = ((double) b) / 1024.0d;
            suffix = "KB";
        }
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = this.FORMAT;
        if (bytes < 0) {
            i = -1;
        } else {
            i = 1;
        }
        return stringBuilder.append(decimalFormat.format(((double) i) * val)).append(" ").append(suffix).toString();
    }
}
