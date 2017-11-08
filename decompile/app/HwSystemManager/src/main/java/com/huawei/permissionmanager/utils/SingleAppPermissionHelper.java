package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.google.common.collect.Lists;
import java.util.List;

public class SingleAppPermissionHelper {

    public static class PermissionItemBase {
    }

    public static class PermissionGroupItem extends PermissionItemBase {
        private static final String TAG = "PermissionGroupItem";
        private String mGroupName;
        private PermissionCategory mPermissionCategory;
        private int mPermissionDescription;
        private int mStatus = -1;
        private List<PermissionItem> mSubItems = Lists.newArrayList();

        public PermissionGroupItem(int descriptionId, String groupName, PermissionCategory category) {
            this.mPermissionDescription = descriptionId;
            this.mGroupName = groupName;
            this.mPermissionCategory = category;
        }

        public void setStatus(int status) {
            this.mStatus = status;
            for (PermissionItem subItem : this.mSubItems) {
                subItem.setStatus(status);
            }
        }

        public int getStatus() {
            if (this.mSubItems.isEmpty()) {
                return this.mStatus;
            }
            int preStatus = -1;
            for (PermissionItem subItem : this.mSubItems) {
                int curState = subItem.getStatus();
                if (preStatus == -1) {
                    preStatus = curState;
                } else if (curState != preStatus) {
                    return -1;
                }
            }
            return preStatus;
        }

        public PermissionCategory getCategory() {
            return this.mPermissionCategory;
        }

        public String getPermissionName(Context context) {
            if (context != null) {
                return context.getString(this.mPermissionDescription);
            }
            return null;
        }

        public String getGroupName() {
            return this.mGroupName;
        }

        public void addSubItem(PermissionItem subItem) {
            this.mSubItems.add(subItem);
        }

        public int getSubItemCount() {
            return this.mSubItems.size();
        }

        public PermissionItem getItemAtIndex(int index) {
            return (PermissionItem) this.mSubItems.get(index);
        }

        public List<PermissionItem> getSubPermissions() {
            return Lists.newArrayList(this.mSubItems);
        }
    }

    public static class PermissionItem extends PermissionItemBase {
        private boolean mIsRecommend = false;
        private OnCheckedChangeListener mListener;
        private PermissionCategory mPermissionCategory;
        private int mPermissionDescription;
        private int mPermissionType;
        private int mRecommendPercent;
        private int mRecommendStatus;
        private int mStatus;

        public PermissionItem(int descriptionId, int permissionType, PermissionCategory permissionCategory) {
            this.mPermissionDescription = descriptionId;
            this.mPermissionType = permissionType;
            this.mIsRecommend = false;
            this.mPermissionCategory = permissionCategory;
        }

        public void setStatus(int status) {
            this.mStatus = status;
        }

        public int getStatus() {
            return this.mStatus;
        }

        public boolean isRecommend() {
            return this.mIsRecommend;
        }

        public int getRecommendStatus() {
            return this.mRecommendStatus;
        }

        public int getPermissionType() {
            return this.mPermissionType;
        }

        public PermissionCategory getCategory() {
            return this.mPermissionCategory;
        }

        public int getRecommendPercent() {
            return this.mRecommendPercent;
        }

        public String getPermissionName(Context context) {
            if (context != null) {
                return context.getString(this.mPermissionDescription);
            }
            return null;
        }

        public boolean isPermissionFrozen() {
            return ShareCfg.isPermissionFrozen(this.mPermissionType);
        }

        public void changeToRecommend(RecommendBaseItem recommendItem) {
            if (recommendItem != null) {
                this.mIsRecommend = true;
                this.mRecommendStatus = recommendItem.getCurrentPermissionRecommendStatus();
                this.mRecommendPercent = recommendItem.getRecommendPercent();
            }
        }

        public void setListener(OnCheckedChangeListener listener) {
            this.mListener = listener;
        }

        public OnCheckedChangeListener getListener() {
            return this.mListener;
        }
    }

    public static class PermissionTagItem extends PermissionItemBase {
        public int mTagText;

        public PermissionTagItem(int stringId) {
            this.mTagText = stringId;
        }
    }
}
