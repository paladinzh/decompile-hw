package com.huawei.systemmanager.antivirus.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import java.util.ArrayList;
import java.util.List;

public class AdvertiseAdapt extends BaseAdapter {
    private Activity mActivity = null;
    private final ArrayList<BaseEntity> mList = Lists.newArrayList();

    public static abstract class BaseEntity {
        public abstract String getDetail(Context context);

        public abstract String getTitle(Context context);

        public boolean isGlobalScanItem() {
            return false;
        }
    }

    public static class AdvertiseEntity extends BaseEntity {
        protected int mCheckedCount;
        protected int mTotalCount;

        public AdvertiseEntity(int totalAdCount, int checkedAdCount) {
            this.mTotalCount = totalAdCount;
            this.mCheckedCount = checkedAdCount;
        }

        public String getTitle(Context ctx) {
            if (this.mCheckedCount == 0) {
                return ctx.getResources().getQuantityString(R.plurals.scan_result_advertises, this.mTotalCount, new Object[]{Integer.valueOf(this.mTotalCount)});
            }
            return ctx.getResources().getQuantityString(R.plurals.ad_blocked_num, this.mCheckedCount, new Object[]{Integer.valueOf(this.mCheckedCount)});
        }

        public String getDetail(Context ctx) {
            return ctx.getResources().getString(R.string.ad_block);
        }
    }

    public static class DlBlockEntity extends BaseEntity {
        public String getTitle(Context ctx) {
            int dlBlockSize = AdCheckUrlResult.getBlockRecordSize(ctx.getApplicationContext());
            return ctx.getResources().getQuantityString(R.plurals.ad_dl_block_entity_message, dlBlockSize, new Object[]{Integer.valueOf(dlBlockSize)});
        }

        public String getDetail(Context ctx) {
            return ctx.getResources().getString(R.string.ad_dl_block_records_title);
        }
    }

    public static class GlobalScanEntity extends BaseEntity {
        public String getTitle(Context ctx) {
            return ctx.getResources().getString(R.string.virus_global_scan);
        }

        public boolean isGlobalScanItem() {
            return true;
        }

        public String getDetail(Context ctx) {
            return "";
        }
    }

    public static class NetQinEntity extends BaseEntity {
        public String getTitle(Context ctx) {
            return ctx.getString(R.string.net_qin_scan_title);
        }

        public String getDetail(Context ctx) {
            return "";
        }
    }

    public static class RiskPermEntity extends BaseEntity {
        protected int mNum;

        public RiskPermEntity(int size) {
            this.mNum = size;
        }

        public String getTitle(Context ctx) {
            return ctx.getResources().getQuantityString(R.plurals.virus_risk_permission_apps, this.mNum, new Object[]{Integer.valueOf(this.mNum)});
        }

        public String getDetail(Context ctx) {
            return ctx.getResources().getString(R.string.virus_title_permission);
        }
    }

    private static class ViewHolder {
        RelativeLayout globalLayout;
        TextView itemDetail;
        TextView itemResult;
        ImageView statusIcon;

        private ViewHolder() {
        }
    }

    public AdvertiseAdapt(Activity activity) {
        this.mActivity = activity;
    }

    public void setData(List<BaseEntity> dataSource) {
        this.mList.clear();
        this.mList.addAll(dataSource);
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mList.size();
    }

    public Object getItem(int position) {
        return this.mList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mActivity).inflate(R.layout.virus_scan_item, parent, false);
            holder.statusIcon = (ImageView) convertView.findViewById(R.id.antivirus_scan_item_status_icon);
            holder.itemResult = (TextView) convertView.findViewById(R.id.antivirus_scan_item_name);
            holder.globalLayout = (RelativeLayout) convertView.findViewById(R.id.global_scan_layout);
            holder.itemDetail = (TextView) convertView.findViewById(R.id.antivirus_scan_item_name_detail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BaseEntity entity = (BaseEntity) this.mList.get(position);
        if (entity.isGlobalScanItem()) {
            holder.globalLayout.setVisibility(0);
        } else {
            if (TextUtils.isEmpty(entity.getDetail(this.mActivity))) {
                holder.itemResult.setText(entity.getTitle(this.mActivity));
                holder.itemDetail.setVisibility(8);
            } else {
                holder.itemResult.setText(entity.getDetail(this.mActivity));
                holder.itemDetail.setText(entity.getTitle(this.mActivity));
                holder.itemDetail.setVisibility(0);
            }
            holder.itemResult.setVisibility(0);
            holder.globalLayout.setVisibility(8);
        }
        holder.statusIcon.setImageResource(R.drawable.ic_public_arrow);
        holder.statusIcon.setVisibility(0);
        convertView.setTag(R.id.convertview_tag_item, entity);
        return convertView;
    }
}
