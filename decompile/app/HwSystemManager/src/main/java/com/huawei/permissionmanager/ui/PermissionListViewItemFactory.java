package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.SuperAppPermisionChecker;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;

public class PermissionListViewItemFactory {
    private static final String LOG_TAG = "PermissionListViewItemFactory";
    String[] mCfgStringArr;
    private Context mContext;
    private LayoutInflater mInfater = null;
    private DisplayImageOptions options = null;

    static class OtherTypeViewHolder extends ListViewHolder {
        ImageView appIcon = null;
        Switch cfgSwitch = null;
        TextView tvAppLabel = null;
        TextView tvDefaultSmsApp = null;
        TextView tvForbitContent = null;
        TextView tvRecommendDesp = null;

        public OtherTypeViewHolder(View view) {
            super(view);
            this.appIcon = (ImageView) view.findViewById(R.id.PermissionCfgAppIcon);
            this.tvAppLabel = (TextView) view.findViewById(R.id.PermissionCfgAppName);
            this.tvRecommendDesp = (TextView) view.findViewById(R.id.PermissionRecommendDesp);
            this.tvDefaultSmsApp = (TextView) view.findViewById(R.id.DefaultSmsApp);
            this.tvForbitContent = (TextView) view.findViewById(R.id.PermissionCfgAppForbitContent);
            this.cfgSwitch = (Switch) view.findViewById(R.id.PermissionCfgSwitch);
        }
    }

    public PermissionListViewItemFactory(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mInfater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mCfgStringArr = this.mContext.getResources().getStringArray(R.array.permission_spinner_textarray);
            this.options = new Builder().useApkDecoder(true).loadDrawableByPkgInfo(true).cacheInMemory(true).cacheOnDisk(false).showImageOnLoading(17301651).considerExifParams(true).resetViewBeforeLoading(true).build();
        }
    }

    public View getPermissionItemView(ListViewObject listViewObj, View convertview, OnCheckedChangeListener listener) {
        if (listViewObj == null) {
            return null;
        }
        if (!(listViewObj instanceof AppInfoWrapperForSinglePermission)) {
            return null;
        }
        OtherTypeViewHolder viewHolder;
        AppInfoWrapperForSinglePermission appInfoWrapper = (AppInfoWrapperForSinglePermission) listViewObj;
        if (convertview == null) {
            convertview = this.mInfater.inflate(R.layout.permission_cfg_list_item, null);
            viewHolder = new OtherTypeViewHolder(convertview);
            convertview.setTag(viewHolder);
        } else {
            viewHolder = (OtherTypeViewHolder) convertview.getTag();
        }
        if (viewHolder == null || viewHolder.cfgSwitch == null) {
            return null;
        }
        viewHolder.cfgSwitch.setTag(R.id.convertview_tag_item, appInfoWrapper);
        if (this.options != null) {
            ImageLoader.getInstance().displayImage(appInfoWrapper.mPkgName, viewHolder.appIcon, this.options, null);
        } else {
            viewHolder.appIcon.setImageDrawable(HsmPackageManager.getInstance().getIcon(appInfoWrapper.mPkgName));
        }
        viewHolder.tvAppLabel.setText(appInfoWrapper.mLabel);
        if (appInfoWrapper.mRecommend) {
            viewHolder.tvRecommendDesp.setVisibility(0);
            if (1 == appInfoWrapper.mRecommendStatus) {
                String recommendPercent = NumberLocationPercent.getPercentage(Double.valueOf((double) appInfoWrapper.mRecommendPercent).doubleValue(), 0);
                viewHolder.tvRecommendDesp.setText(this.mContext.getResources().getString(R.string.recommend_user_selection, new Object[]{recommendPercent}));
            } else {
                viewHolder.tvRecommendDesp.setVisibility(0);
                viewHolder.tvRecommendDesp.setText(R.string.Recommend_Forbid);
            }
        } else {
            viewHolder.tvRecommendDesp.setVisibility(8);
        }
        int permissionStatus = appInfoWrapper.mPermissionStatus;
        if ((Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) && ShareCfg.isPermissionFrozen(appInfoWrapper.mPermissionType)) {
            viewHolder.tvAppLabel.setTextColor(this.mContext.getResources().getColor(R.color.color_disable));
            permissionStatus = 2;
        }
        viewHolder.cfgSwitch.setOnCheckedChangeListener(null);
        viewHolder.cfgSwitch.setChecked(2 != permissionStatus);
        viewHolder.cfgSwitch.setOnCheckedChangeListener(listener);
        if (CommonFunctionUtil.isDefaultSmsPermission(this.mContext, appInfoWrapper.mPkgName, appInfoWrapper.mPermissionType)) {
            viewHolder.tvDefaultSmsApp.setVisibility(0);
            viewHolder.tvRecommendDesp.setVisibility(8);
        } else {
            viewHolder.tvDefaultSmsApp.setVisibility(8);
        }
        if (viewHolder.tvDefaultSmsApp.getVisibility() == 0 || !SuperAppPermisionChecker.getInstance(this.mContext).checkIfIsInAppPermissionList(appInfoWrapper.mPkgName, appInfoWrapper.mPermissionType)) {
            viewHolder.tvForbitContent.setVisibility(8);
        } else {
            viewHolder.tvForbitContent.setVisibility(0);
            Permission permissionObj = PermissionTableManager.getInstance(this.mContext).getPermissionObjectByPermissionType(appInfoWrapper.mPermissionType);
            if (permissionObj != null) {
                viewHolder.tvForbitContent.setText(permissionObj.getmPermissionForbitTips(this.mContext, appInfoWrapper.mPkgName));
            }
        }
        return convertview;
    }

    public String getPermissionCfgString(int status) {
        String result = this.mCfgStringArr[0];
        switch (status) {
            case 0:
                return this.mCfgStringArr[1];
            case 1:
                return this.mCfgStringArr[0];
            case 2:
                return this.mCfgStringArr[2];
            default:
                return result;
        }
    }
}
