package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionGroupItem;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItem;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItemBase;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionTagItem;
import com.huawei.permissionmanager.utils.SuperAppPermisionChecker;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;
import java.util.ArrayList;
import java.util.List;

class SingleAppAdapter extends BaseExpandableListAdapter {
    private static final int GROUP_VIEW_TYPE_COUNT = 3;
    private static final int GROUP_VIEW_TYPE_GROUP_PERM = 1;
    private static final int GROUP_VIEW_TYPE_SINGLE_PERM = 2;
    private static final int GROUP_VIEW_TYPE_TAG = 0;
    private static boolean mTrusted = false;
    private String LOG_TAG = "SingleAppAdapter";
    private int disableColor;
    private int enableColor;
    String[] mCfgStringArr;
    private Context mContext = null;
    private LayoutInflater mInfater = null;
    private ArrayList<PermissionItemBase> mItems = Lists.newArrayList();
    private String mPkgName = null;

    static class AppTagHolder extends ListViewHolder {
        TextView tvTagText;

        public AppTagHolder(View view) {
            super(view);
            this.tvTagText = (TextView) view.findViewById(R.id.tvTagName);
        }
    }

    static class AppViewHolder extends ListViewHolder {
        View divider;
        Switch mValueSwitch = null;
        TextView tvDefaultPermission = null;
        TextView tvPermissionCfg = null;
        TextView tvPermissionDescription = null;
        TextView tvPermissionForbitContent = null;
        TextView tvPermissionRecommend = null;

        public AppViewHolder(View view) {
            super(view);
            this.tvPermissionDescription = (TextView) view.findViewById(R.id.PermissionAppDescription);
            this.tvPermissionRecommend = (TextView) view.findViewById(R.id.PermissionRecommendDescription);
            this.tvPermissionCfg = (TextView) view.findViewById(R.id.PermissionCfg);
            this.tvDefaultPermission = (TextView) view.findViewById(R.id.DefaultPermission);
            this.tvPermissionForbitContent = (TextView) view.findViewById(R.id.PermissionForbitContent);
            this.mValueSwitch = (Switch) view.findViewById(R.id.PermissionCfgSwitch);
            this.divider = view.findViewById(R.id.divider);
        }
    }

    static class PermGroupViewHolder extends AppViewHolder {
        public PermGroupViewHolder(View view) {
            super(view);
            this.tvPermissionRecommend.setVisibility(8);
        }
    }

    public SingleAppAdapter(Context context, ArrayList<PermissionItemBase> permissionItemList, AppInfo appInfo, OnClickListener groupClicker) {
        this.mContext = context;
        this.mItems = permissionItemList;
        this.mPkgName = appInfo != null ? appInfo.mPkgName : "";
        this.mInfater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mCfgStringArr = this.mContext.getResources().getStringArray(R.array.permission_spinner_textarray);
        this.disableColor = this.mContext.getResources().getColor(R.color.color_disable);
        this.enableColor = this.mContext.getResources().getColor(R.color.emui_list_primary_text);
    }

    private boolean isItemFrozen(PermissionItemBase item) {
        if ((Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) && (item instanceof PermissionItem)) {
            return ((PermissionItem) item).isPermissionFrozen();
        }
        return false;
    }

    private void setHolderContent(AppViewHolder holder, PermissionItemBase itemObj) {
        if (itemObj instanceof PermissionItem) {
            boolean z;
            PermissionItem permissionItem = (PermissionItem) itemObj;
            holder.tvPermissionDescription.setText(permissionItem.getPermissionName(this.mContext));
            if (Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
                holder.tvPermissionDescription.setTextColor(this.mContext.getResources().getColor(R.color.emui_list_primary_text));
                if (permissionItem.isPermissionFrozen()) {
                    holder.tvPermissionDescription.setTextColor(this.disableColor);
                    permissionItem.setStatus(2);
                    holder.mView.setEnabled(false);
                }
            }
            holder.tvPermissionCfg.setText(getPermissionCfgString(permissionItem.getStatus()));
            if (2 == permissionItem.getStatus()) {
                holder.tvPermissionCfg.setTextColor(this.mContext.getResources().getColor(R.color.hsm_forbidden));
            } else {
                holder.tvPermissionCfg.setTextColor(this.mContext.getResources().getColor(R.color.emui_list_secondray_text));
            }
            holder.mValueSwitch.setOnCheckedChangeListener(null);
            Switch switchR = holder.mValueSwitch;
            if (permissionItem.getStatus() == 1) {
                z = true;
            } else {
                z = false;
            }
            switchR.setChecked(z);
            holder.mValueSwitch.setOnCheckedChangeListener(permissionItem.getListener());
            holder.mValueSwitch.setTag(R.id.convertview_tag_item, permissionItem);
            if (permissionItem.isRecommend()) {
                holder.tvPermissionRecommend.setVisibility(0);
                if (1 == permissionItem.getRecommendStatus()) {
                    String recommendPercent = NumberLocationPercent.getPercentage(Double.valueOf((double) permissionItem.getRecommendPercent()).doubleValue(), 0);
                    holder.tvPermissionRecommend.setText(this.mContext.getResources().getString(R.string.recommend_user_selection, new Object[]{recommendPercent}));
                } else {
                    holder.tvPermissionRecommend.setVisibility(0);
                    holder.tvPermissionRecommend.setText(R.string.Recommend_Forbid);
                }
            } else {
                holder.tvPermissionRecommend.setVisibility(8);
            }
            if (CommonFunctionUtil.isDefaultSmsPermission(this.mContext, this.mPkgName, permissionItem.getPermissionType())) {
                holder.tvDefaultPermission.setVisibility(0);
                holder.tvPermissionRecommend.setVisibility(8);
            } else {
                holder.tvDefaultPermission.setVisibility(8);
            }
            if (holder.tvDefaultPermission.getVisibility() != 0 && SuperAppPermisionChecker.getInstance(this.mContext).checkIfIsInAppPermissionList(this.mPkgName, permissionItem.getPermissionType())) {
                Permission permissionObj = PermissionTableManager.getInstance(this.mContext).getPermissionObjectByPermissionType(permissionItem.getPermissionType());
                if (permissionObj == null) {
                    HwLog.e(this.LOG_TAG, "getPermissionItemView get null permissionObj");
                    return;
                }
                holder.tvPermissionRecommend.setVisibility(8);
                if (holder.tvPermissionForbitContent != null) {
                    holder.tvPermissionForbitContent.setVisibility(0);
                    holder.tvPermissionForbitContent.setText(permissionObj.getmPermissionForbitTips(this.mContext, this.mPkgName));
                }
            } else if (holder.tvPermissionForbitContent != null) {
                holder.tvPermissionForbitContent.setVisibility(8);
            }
        }
    }

    private String getPermissionCfgString(int status) {
        String result = "";
        switch (status) {
            case 1:
                return this.mCfgStringArr[0];
            case 2:
                return this.mCfgStringArr[2];
            default:
                HwLog.w(this.LOG_TAG, "get congif string for unexcepted status.");
                return result;
        }
    }

    public int getGroupCount() {
        return this.mItems.size();
    }

    public int getChildrenCount(int groupPosition) {
        PermissionItemBase item = (PermissionItemBase) this.mItems.get(groupPosition);
        if (item instanceof PermissionGroupItem) {
            return ((PermissionGroupItem) item).getSubItemCount();
        }
        return 0;
    }

    public Object getGroup(int groupPosition) {
        return this.mItems.get(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        PermissionItemBase item = (PermissionItemBase) this.mItems.get(groupPosition);
        if (item instanceof PermissionGroupItem) {
            return ((PermissionGroupItem) item).getItemAtIndex(childPosition);
        }
        return null;
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public int getGroupTypeCount() {
        return 3;
    }

    public int getGroupType(int groupPosition) {
        PermissionItemBase item = (PermissionItemBase) this.mItems.get(groupPosition);
        if (item instanceof PermissionTagItem) {
            return 0;
        }
        if (item instanceof PermissionGroupItem) {
            return 1;
        }
        return 2;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newGroupView(groupPosition, isExpanded, parent);
        }
        bindGroupView(groupPosition, isExpanded, convertView, parent);
        return convertView;
    }

    public boolean hasStableIds() {
        return false;
    }

    private View newGroupView(int groupPosition, boolean isExpanded, ViewGroup parent) {
        View view;
        switch (getGroupType(groupPosition)) {
            case 0:
                view = this.mInfater.inflate(R.layout.permission_list_tab_item_tag, parent, false);
                view.setTag(new AppTagHolder(view));
                return view;
            default:
                view = this.mInfater.inflate(R.layout.single_app_list_item, parent, false);
                view.setTag(new AppViewHolder(view));
                return view;
        }
    }

    private void bindGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        if (view == null) {
            HwLog.e(this.LOG_TAG, "bindGroupView convertview is null!!");
            return;
        }
        PermissionItemBase item = (PermissionItemBase) this.mItems.get(groupPosition);
        if (item instanceof PermissionTagItem) {
            ((AppTagHolder) view.getTag()).tvTagText.setText(((PermissionTagItem) item).mTagText);
        } else if (item instanceof PermissionItem) {
            PermissionItem permItem = (PermissionItem) item;
            if (groupPosition + 1 < getGroupCount()) {
                AppViewHolder holder = (AppViewHolder) view.getTag();
                if (!(((PermissionItemBase) this.mItems.get(groupPosition + 1)) instanceof PermissionTagItem) || isExpanded) {
                    holder.divider.setVisibility(0);
                } else {
                    holder.divider.setVisibility(8);
                }
            }
            bindSinglPerm(view, permItem, false);
        }
        view.setTag(R.id.convertview_tag_item, item);
    }

    private void bindSinglPerm(View view, PermissionItem item, boolean isChildView) {
        if (item != null) {
            AppViewHolder holder = (AppViewHolder) view.getTag();
            setHolderContent(holder, item);
            if (!mTrusted || isItemFrozen(item)) {
                holder.tvPermissionDescription.setTextColor(this.enableColor);
            } else {
                holder.tvPermissionCfg.setText(getPermissionCfgString(1));
                holder.tvPermissionCfg.setTextColor(this.disableColor);
                if (isChildView) {
                    holder.tvPermissionDescription.setTextColor(this.disableColor);
                }
                view.setEnabled(false);
            }
        }
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView != null) {
            return convertView;
        }
        convertView = this.mInfater.inflate(R.layout.single_child_app_list_item, parent, false);
        convertView.setTag(new AppViewHolder(convertView));
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public List<Integer> getExpandablePos() {
        List<Integer> result = Lists.newArrayList();
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            if (((PermissionItemBase) this.mItems.get(i)) instanceof PermissionGroupItem) {
                result.add(Integer.valueOf(i));
            }
        }
        return result;
    }

    public static void setGlobalSwitchStatus(boolean trust) {
        mTrusted = trust;
    }
}
