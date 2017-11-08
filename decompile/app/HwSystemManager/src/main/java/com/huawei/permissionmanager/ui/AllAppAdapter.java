package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.List;

public class AllAppAdapter extends AllAppAdapterBase {
    private String LOG_TAG = "AllAppAdapter";
    private Context mContext = null;
    private LayoutInflater mInfater = null;
    private OnClickListener mItemListener;

    static class AllAppViewHolder extends ListViewHolder {
        ImageView appIcon;
        View divider;
        TextView tvAppName;
        TextView tvAppRecommendTip;
        TextView tvDescription;

        public AllAppViewHolder(View view) {
            super(view);
            this.appIcon = (ImageView) view.findViewById(R.id.image);
            this.tvAppName = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
            this.tvAppRecommendTip = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
            this.tvDescription = (TextView) view.findViewById(R.id.detail);
            this.divider = view.findViewById(R.id.dividing_line_to_bottom);
        }
    }

    public AllAppAdapter(Context context, List<AppInfoWrapper> permissionAppsList, OnClickListener itemListener) {
        super(permissionAppsList);
        HwLog.d(this.LOG_TAG, "AllAppAdapter");
        this.mContext = context;
        this.mInfater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mItemListener = itemListener;
    }

    public View getView(int position, View convertview, ViewGroup arg2) {
        HwLog.d(this.LOG_TAG, "getView at " + position);
        AppInfoWrapper itemInfo = (AppInfoWrapper) getItem(position);
        if (itemInfo == null) {
            HwLog.e(this.LOG_TAG, "ERROR: The AppInfoWrapper object is null!");
            return null;
        }
        HwLog.d(this.LOG_TAG, "itemInfo.isTag():" + itemInfo.isTag());
        if (itemInfo.isTag()) {
            convertview = this.mInfater.inflate(R.layout.permission_cfg_list_item_tag, null);
            new TagViewHolder(convertview).setContentValue(this.mContext, itemInfo);
        } else {
            AllAppViewHolder allAppViewHolder;
            if (convertview == null) {
                convertview = this.mInfater.inflate(R.layout.common_list_item_twolines_image_detail_arrow, null);
                allAppViewHolder = new AllAppViewHolder(convertview);
                convertview.setTag(allAppViewHolder);
            } else {
                allAppViewHolder = (AllAppViewHolder) convertview.getTag();
                HwLog.d(this.LOG_TAG, "allAppViewHolder :" + (allAppViewHolder == null ? "null" : "not null"));
                if (allAppViewHolder == null) {
                    convertview = this.mInfater.inflate(R.layout.common_list_item_twolines_image_detail_arrow, null);
                    allAppViewHolder = new AllAppViewHolder(convertview);
                    convertview.setTag(allAppViewHolder);
                } else {
                    allAppViewHolder = (AllAppViewHolder) convertview.getTag();
                }
            }
            convertview.setBackgroundResource(this.mContext.getResources().getIdentifier(ViewUtil.EMUI_SELECTOR_BACKGROUND, null, null));
            convertview.setOnClickListener(this.mItemListener);
            setHolderContent(allAppViewHolder, itemInfo, position);
            convertview.setTag(R.id.image, itemInfo);
        }
        return convertview;
    }

    private void setHolderContent(AllAppViewHolder holder, AppInfoWrapper appInfoWrapper, int position) {
        if (appInfoWrapper != null && appInfoWrapper.mAppInfo != null) {
            boolean trustFlag;
            holder.appIcon.setImageDrawable(HsmPackageManager.getInstance().getIcon(appInfoWrapper.mAppInfo.mPkgName));
            holder.tvAppName.setText(appInfoWrapper.mAppInfo.mAppLabel);
            int size = appInfoWrapper.mPermissionCount;
            if (size == 0) {
                size = appInfoWrapper.mAppInfo.mRequestPermissions.size();
            }
            String countString = this.mContext.getResources().getQuantityString(R.plurals.Other_PermissionManager_Tip, size, new Object[]{Integer.valueOf(size)});
            HwLog.i(this.LOG_TAG, "countString:" + countString);
            holder.tvDescription.setText(countString);
            int recommendItemCount = appInfoWrapper.mRecommentItemCount;
            if (1 == appInfoWrapper.mAppInfo.mTrust) {
                trustFlag = true;
            } else {
                trustFlag = false;
            }
            if (trustFlag || !appInfoWrapper.mHasRecommendItem || recommendItemCount <= 0) {
                holder.tvAppRecommendTip.setVisibility(8);
            } else {
                holder.tvAppRecommendTip.setText(this.mContext.getResources().getQuantityString(R.plurals.recommend_application_tips, recommendItemCount, new Object[]{Integer.valueOf(recommendItemCount)}));
                holder.tvAppRecommendTip.setVisibility(0);
            }
            if (position + 1 < getCount()) {
                if (((AppInfoWrapper) getItem(position + 1)).isTag()) {
                    holder.divider.setVisibility(8);
                } else {
                    holder.divider.setVisibility(0);
                }
            }
        }
    }
}
