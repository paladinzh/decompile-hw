package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.listener.SimpleImageLoadingListener;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;
import java.util.List;

public class ComsumeLevelAdapter extends BaseExpandableListAdapter {
    private static final int ACCURACY = 2;
    private static final int HARDWARE_GROUP = 0;
    private static final double LOW_PERCENT = 0.01d;
    private static final int SOFTWARE_GROUP = 1;
    private static final String TAG = ComsumeLevelAdapter.class.getSimpleName();
    private static final int TOTAL_GROUP_COUNT = 2;
    private static final SimpleImageLoadingListener sLoadListener = new SimpleImageLoadingListener() {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ImageView iconView = (ImageView) view;
            iconView.setScaleType(ScaleType.CENTER_CROP);
            iconView.setImageBitmap(loadedImage);
        }
    };
    private boolean cacheInMemory = true;
    private boolean loadDrawableByUid = true;
    private Context mContext;
    private List<ConsumeLevelHardwareInfo> mHardwareList = Lists.newArrayList();
    private int mHardwarePercentageAll = 0;
    private LayoutInflater mInflater;
    private List<ConsumeLevelSoftwareInfo> mSoftwareList = Lists.newArrayList();
    private int mSoftwarePercentageAll = 0;
    private DisplayImageOptions options;

    private static class GroupViewHolder {
        ImageView expandArrowView;
        TextView groupTitle;

        private GroupViewHolder() {
        }
    }

    private static class HardwareViewHolder {
        ImageView hardwareImage;
        TextView hardwareTitle;
        ProgressBar hwPowerConsumeProgressBar;
        TextView percentageView;

        private HardwareViewHolder() {
        }
    }

    private static class SoftwareViewHolder {
        TextView percentageView;
        TextView runningStatusView;
        TextView shareUidView;
        ImageView softwareImage;
        TextView softwareTitle;
        ProgressBar swPowerConsumeProgressBar;

        private SoftwareViewHolder() {
        }
    }

    public ComsumeLevelAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.options = new Builder().useApkDecoder(false).loadDrawableByUid(this.loadDrawableByUid).cacheInMemory(this.cacheInMemory).cacheOnDisk(false).showImageOnLoading((int) R.drawable.ic_storagecleaner_apppackages).considerExifParams(true).build();
    }

    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == 0) {
            return this.mHardwareList.get(childPosition);
        }
        if (1 == groupPosition) {
            return this.mSoftwareList.get(childPosition);
        }
        return null;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newChildViewFromType(groupPosition, parent);
        }
        bindChildView(groupPosition, childPosition, isLastChild, convertView);
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        if (groupPosition == 0) {
            return this.mHardwareList.size();
        }
        if (1 == groupPosition) {
            return this.mSoftwareList.size();
        }
        return 0;
    }

    public int getChildType(int groupPosition, int childPosition) {
        if (groupPosition == 0) {
            return 0;
        }
        if (1 == groupPosition) {
            return 1;
        }
        return -1;
    }

    public int getChildTypeCount() {
        return 2;
    }

    public Object getGroup(int groupPosition) {
        if (groupPosition == 0) {
            return this.mContext.getString(R.string.hardware_consume_title, new Object[]{NumberLocationPercent.getPercentage((double) this.mHardwarePercentageAll, 0)});
        } else if (1 != groupPosition) {
            return null;
        } else {
            return this.mContext.getString(R.string.software_consume_title, new Object[]{NumberLocationPercent.getPercentage((double) this.mSoftwarePercentageAll, 0)});
        }
    }

    public int getGroupCount() {
        return 2;
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        int i;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.power_consume_expandable, parent, false);
            holder = new GroupViewHolder();
            holder.groupTitle = (TextView) convertView.findViewById(R.id.consume_title);
            holder.expandArrowView = (ImageView) convertView.findViewById(R.id.indicate_arrow);
            convertView.setTag(holder);
        }
        holder = (GroupViewHolder) convertView.getTag();
        holder.groupTitle.setText((String) getGroup(groupPosition));
        ImageView imageView = holder.expandArrowView;
        if (isExpanded) {
            i = R.drawable.expander_close_emui;
        } else {
            i = R.drawable.expander_open_emui;
        }
        imageView.setImageResource(i);
        return convertView;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean swapData(int hardwarePercentage, int softwarePercentage, List<ConsumeLevelHardwareInfo> hardList, List<ConsumeLevelSoftwareInfo> softList) {
        this.mHardwarePercentageAll = hardwarePercentage;
        this.mHardwareList.clear();
        this.mHardwareList.addAll(hardList);
        this.mSoftwarePercentageAll = softwarePercentage;
        this.mSoftwareList.clear();
        this.mSoftwareList.addAll(softList);
        notifyDataSetChanged();
        return true;
    }

    private View newChildViewFromType(int groupPosition, ViewGroup parent) {
        ViewGroup view;
        if (groupPosition == 0) {
            HardwareViewHolder hardView = new HardwareViewHolder();
            view = (ViewGroup) this.mInflater.inflate(R.layout.hardware_power_list, parent, false);
            hardView.hardwareImage = (ImageView) view.findViewById(R.id.hard_icon_imageview);
            hardView.hardwareTitle = (TextView) view.findViewById(R.id.hard_title_textview);
            hardView.percentageView = (TextView) view.findViewById(R.id.hard_content_textview);
            hardView.hwPowerConsumeProgressBar = (ProgressBar) view.findViewById(R.id.hw_power_consume_progress_bar);
            view.setTag(hardView);
            HwLog.e(TAG, "newChildViewFromType hard: " + groupPosition);
            return view;
        } else if (1 != groupPosition) {
            return null;
        } else {
            SoftwareViewHolder softView = new SoftwareViewHolder();
            view = (ViewGroup) this.mInflater.inflate(R.layout.software_power_list, parent, false);
            softView.softwareImage = (ImageView) view.findViewById(R.id.soft_icon_imageview);
            softView.softwareTitle = (TextView) view.findViewById(R.id.soft_title_textview);
            softView.percentageView = (TextView) view.findViewById(R.id.soft_content_textview);
            softView.runningStatusView = (TextView) view.findViewById(R.id.soft_isalive_textview);
            softView.shareUidView = (TextView) view.findViewById(R.id.isShareUidApp);
            softView.swPowerConsumeProgressBar = (ProgressBar) view.findViewById(R.id.sw_power_consume_progress_bar);
            view.setTag(softView);
            HwLog.e(TAG, "newChildViewFromType soft: " + groupPosition);
            return view;
        }
    }

    private void bindChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView) {
        HwLog.e(TAG, "bindChildView: " + groupPosition + SqlMarker.COMMA_SEPARATE + childPosition + SqlMarker.COMMA_SEPARATE + isLastChild);
        if (groupPosition == 0) {
            bindHardView(convertView, groupPosition, childPosition);
        } else if (1 == groupPosition) {
            bindSoftView(convertView, groupPosition, childPosition);
        }
    }

    private View bindHardView(View convertView, int groupPosition, int childPosition) {
        ConsumeLevelHardwareInfo info = (ConsumeLevelHardwareInfo) this.mHardwareList.get(childPosition);
        HardwareViewHolder hardView = (HardwareViewHolder) convertView.getTag();
        hardView.hardwareImage.setImageDrawable(info.commInfo.icon);
        hardView.hardwareTitle.setText(info.commInfo.labelName);
        if (info.commInfo.exactPercentage >= LOW_PERCENT) {
            hardView.percentageView.setText(String.format(this.mContext.getString(R.string.percentage_format_s), new Object[]{NumberLocationPercent.getPercentage(((double) info.commInfo.adjValue) / 100.0d, 2)}));
        } else {
            String threshValue = NumberLocationPercent.getPercentage(LOW_PERCENT, 2);
            hardView.percentageView.setText(this.mContext.getString(R.string.low_percentage_text, new Object[]{threshValue}));
        }
        hardView.hwPowerConsumeProgressBar.setProgress(info.commInfo.adjValue);
        return convertView;
    }

    private View bindSoftView(View convertView, int groupPosition, int childPosition) {
        ConsumeLevelSoftwareInfo info = (ConsumeLevelSoftwareInfo) this.mSoftwareList.get(childPosition);
        SoftwareViewHolder softView = (SoftwareViewHolder) convertView.getTag();
        HwLog.d(TAG, "bindSoftView softInfo: " + info);
        softView.percentageView.setText(String.format(this.mContext.getString(R.string.percentage_format_s), new Object[]{NumberLocationPercent.getPercentage(((double) info.commInfo.adjValue) / 100.0d, 2)}));
        softView.swPowerConsumeProgressBar.setProgress(info.commInfo.adjValue);
        ImageLoader.getInstance().displayImage(String.valueOf(info.uid), softView.softwareImage, this.options, sLoadListener);
        if (-1 == info.aliveStatus) {
            softView.runningStatusView.setVisibility(8);
        } else {
            softView.runningStatusView.setVisibility(0);
            if (info.aliveStatus == 0) {
                softView.runningStatusView.setText(this.mContext.getString(R.string.app_alive_status_running));
            } else if (1 == info.aliveStatus) {
                softView.runningStatusView.setText(this.mContext.getString(R.string.app_alive_status_stopped));
            }
        }
        if (info.isShareUidApp) {
            softView.shareUidView.setVisibility(0);
        } else {
            softView.shareUidView.setVisibility(8);
        }
        softView.softwareTitle.setText(info.commInfo.labelName);
        return convertView;
    }
}
