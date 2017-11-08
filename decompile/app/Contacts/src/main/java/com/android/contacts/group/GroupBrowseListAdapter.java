package com.android.contacts.group;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Groups;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.util.Objects;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;

public class GroupBrowseListAdapter extends BaseAdapter {
    private final AccountTypeManager mAccountTypeManager;
    private final Context mContext;
    protected Cursor mCursor;
    private boolean mGrpListDiffPadding = false;
    private final LayoutInflater mLayoutInflater;
    private Uri mSelectedGroupUri;
    private boolean mSelectionVisible;
    private boolean mShowBottomDivider = true;
    private int mSmartGroupCompanyPosition = -1;
    private int mSmartGroupItemCount = 0;
    private int mSmartGroupLastContactTimePosition = -1;
    private int mSmartGroupLocationPosition = -1;
    private String mSmartGroupType = null;

    public static class GroupListItemViewCache {
        public final View accountHeader;
        public final TextView accountName;
        public final TextView accountType;
        public final View bottomDivider;
        public final View divider;
        public final TextView groupMemberCount;
        public final TextView groupTitle;
        public final View headerDividerTop;
        private String mGroupType = null;
        private int mPredefinedSmartGroupType = -1;
        private Uri mUri;

        public GroupListItemViewCache(View view) {
            this.accountType = (TextView) view.findViewById(R.id.account_type);
            this.accountName = (TextView) view.findViewById(R.id.account_name);
            this.groupTitle = (TextView) view.findViewById(R.id.label);
            this.groupMemberCount = (TextView) view.findViewById(R.id.count);
            this.accountHeader = view.findViewById(R.id.group_list_header);
            this.divider = view.findViewById(R.id.divider);
            this.bottomDivider = view.findViewById(R.id.bottom_divider);
            this.headerDividerTop = view.findViewById(R.id.header_divider_top);
        }

        public void setUri(Uri uri) {
            this.mUri = uri;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public void setGroupType(String groupType) {
            this.mGroupType = groupType;
        }

        public String getGroupType() {
            return this.mGroupType;
        }

        public void setPredefinedSmartGroupType(int predefinedSmartGroupType) {
            this.mPredefinedSmartGroupType = predefinedSmartGroupType;
        }

        public int getPredefinedSmartGroupType() {
            return this.mPredefinedSmartGroupType;
        }
    }

    public GroupBrowseListAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mAccountTypeManager = AccountTypeManager.getInstance(this.mContext);
        this.mGrpListDiffPadding = this.mContext.getResources().getBoolean(R.bool.config_group_list_diff_padding);
        setSmartGroupPosition();
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
        if (this.mSelectedGroupUri == null && cursor != null && cursor.getCount() > 0) {
            GroupListItem firstItem = getItem(0);
            long groupId = -1;
            if (firstItem != null) {
                groupId = firstItem.getGroupId();
            }
            this.mSelectedGroupUri = getGroupUriFromId(groupId);
        }
    }

    public void setShowBottomDivider(boolean show) {
        this.mShowBottomDivider = show;
    }

    public int getSelectedGroupPosition() {
        if (this.mSelectedGroupUri == null || this.mCursor == null || this.mCursor.getCount() == 0) {
            return -1;
        }
        int index = 0;
        this.mCursor.moveToPosition(-1);
        while (this.mCursor.moveToNext()) {
            if (this.mSelectedGroupUri.equals(getGroupUriFromId(this.mCursor.getLong(3)))) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void setSelectionVisible(boolean flag) {
        this.mSelectionVisible = flag;
    }

    public void setSelectedGroup(Uri groupUri) {
        this.mSelectedGroupUri = groupUri;
    }

    private boolean isSelectedGroup(Uri groupUri) {
        return this.mSelectedGroupUri != null ? this.mSelectedGroupUri.equals(groupUri) : false;
    }

    public Uri getSelectedGroup() {
        return this.mSelectedGroupUri;
    }

    public int getCount() {
        int i = 0;
        if (SmartGroupUtil.isSmratGroup(this.mSmartGroupType)) {
            if (!(this.mCursor == null || this.mCursor.isClosed())) {
                i = this.mCursor.getCount();
            }
            return i;
        }
        if (!(this.mCursor == null || this.mCursor.isClosed())) {
            i = (this.mCursor.getCount() + 0) + this.mSmartGroupItemCount;
        }
        return i;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public GroupListItem getItem(int position) {
        if (this.mCursor == null || this.mCursor.isClosed() || !this.mCursor.moveToPosition(position)) {
            return null;
        }
        if (SmartGroupUtil.isSmratGroup(this.mSmartGroupType)) {
            return createSmartGroupListItem();
        }
        return createNormalGroupListItem(position);
    }

    private boolean isInValidSmartGroupTitle(String title) {
        if (title == null || title.replace(HwCustPreloadContacts.EMPTY_STRING, "").isEmpty()) {
            return true;
        }
        if ("smart_groups_location".equals(this.mSmartGroupType)) {
            return "N".equals(title);
        }
        return false;
    }

    private GroupListItem createSmartGroupListItem() {
        String title = this.mCursor.getString(0);
        if (isInValidSmartGroupTitle(title)) {
            title = SmartGroupUtil.getDefaultGroupTitle(this.mSmartGroupType, this.mContext);
        }
        return new GroupListItem(null, null, null, 0, title, false, this.mCursor.getInt(1), true, false);
    }

    private GroupListItem createNormalGroupListItem(int position) {
        String accountName;
        boolean isGroupReadOnly;
        String accountType = this.mCursor.getString(1);
        if ("com.android.huawei.sim".equals(accountType) || "com.android.huawei.secondsim".equals(accountType)) {
            accountName = SimFactoryManager.getSimCardDisplayLabel(accountType);
        } else {
            accountName = this.mCursor.getString(0);
        }
        String dataSet = this.mCursor.getString(2);
        long groupId = this.mCursor.getLong(3);
        String title = this.mCursor.getString(4);
        int memberCount = this.mCursor.getInt(5);
        if (this.mCursor.getInt(6) == 1) {
            isGroupReadOnly = true;
        } else {
            isGroupReadOnly = false;
        }
        boolean isPrivateGroup = this.mContext.getString(R.string.private_group_sync1).equals(this.mCursor.getString(10));
        int previousIndex = position - 1;
        boolean isFirstGroupInAccount = true;
        title = CommonUtilMethods.parseGroupDisplayName(accountType, title, this.mContext, this.mCursor.getString(9), this.mCursor.getInt(7), this.mCursor.getString(8));
        if (previousIndex >= 0 && this.mCursor.moveToPosition(previousIndex)) {
            String previousGroupAccountName = this.mCursor.getString(0);
            String previousGroupAccountType = this.mCursor.getString(1);
            String previousGroupDataSet = this.mCursor.getString(2);
            if (accountName != null && accountType != null && accountName.equals(previousGroupAccountName) && accountType.equals(previousGroupAccountType) && Objects.equal(dataSet, previousGroupDataSet)) {
                isFirstGroupInAccount = false;
            }
        }
        return new GroupListItem(accountName, accountType, dataSet, groupId, title, isFirstGroupInAccount, memberCount, isGroupReadOnly, isPrivateGroup);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !(convertView.getTag() instanceof GroupListItemViewCache)) {
            convertView = this.mLayoutInflater.inflate(R.layout.group_browse_list_item, parent, false);
            ViewUtil.setStateListIcon(this.mContext, convertView.findViewById(R.id.arrowhead), false);
            convertView.setTag(new GroupListItemViewCache(convertView));
        }
        View result = convertView;
        if (this.mGrpListDiffPadding) {
            int padding = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_item_indent);
            View listMain = convertView.findViewById(R.id.group_list_itme_main);
            if (listMain != null) {
                listMain.setPadding(padding, 0, padding, 0);
            }
            View headerMain = convertView.findViewById(R.id.group_account_header_main);
            if (headerMain != null) {
                headerMain.setPadding(padding, 0, padding, 0);
            }
        }
        GroupListItemViewCache viewCache = (GroupListItemViewCache) result.getTag();
        setBottomDividerVisibility(viewCache, position);
        if (SmartGroupUtil.isSmratGroup(this.mSmartGroupType)) {
            return handleSmartGroup(position, viewCache, result);
        }
        if (handlePredefinedGroup(position, viewCache)) {
            return result;
        }
        GroupListItem entry = getItem((position + 0) - this.mSmartGroupItemCount);
        if (entry == null) {
            return result;
        }
        if (entry.isFirstGroupInAccount()) {
            bindHeaderView(entry, viewCache);
            viewCache.accountHeader.setVisibility(0);
            viewCache.headerDividerTop.setVisibility(0);
            viewCache.divider.setVisibility(8);
        } else {
            viewCache.accountHeader.setVisibility(8);
            viewCache.divider.setVisibility(0);
        }
        Uri groupUri = getGroupUriFromId(entry.getGroupId());
        String memberCountString = this.mContext.getResources().getQuantityString(R.plurals.group_list_num_contacts_in_group, entry.getMemberCount(), new Object[]{Integer.valueOf(entry.getMemberCount())});
        viewCache.setUri(groupUri);
        viewCache.groupTitle.setText(entry.getTitle());
        if (entry.isPrivateGroup()) {
            viewCache.groupTitle.setTextColor(-65536);
        } else {
            viewCache.groupTitle.setTextColor(this.mContext.getResources().getColor(R.color.contact_list_item_text_color));
        }
        viewCache.groupMemberCount.setVisibility(0);
        viewCache.setGroupType(null);
        viewCache.groupMemberCount.setText(memberCountString);
        if (this.mSelectionVisible) {
            result.setActivated(isSelectedGroup(groupUri));
        }
        return result;
    }

    private void setBottomDividerVisibility(GroupListItemViewCache viewCache, int position) {
        if (viewCache != null && viewCache.bottomDivider != null) {
            int count = getCount() - 1;
            if (this.mShowBottomDivider && count == position) {
                viewCache.bottomDivider.setVisibility(0);
            } else {
                viewCache.bottomDivider.setVisibility(8);
            }
        }
    }

    private void bindHeaderView(GroupListItem entry, GroupListItemViewCache viewCache) {
        String lDisplayLabel;
        String string;
        AccountType accountType = this.mAccountTypeManager.getAccountType(entry.getAccountType(), entry.getDataSet());
        boolean isSimAccount = CommonUtilMethods.isSimAccount(entry.getAccountType());
        if (isSimAccount) {
            lDisplayLabel = SimFactoryManager.getSimCardDisplayLabel(entry.getAccountType());
        } else {
            lDisplayLabel = accountType.getDisplayLabel(this.mContext).toString();
        }
        TextView textView = viewCache.accountType;
        if (isSimAccount) {
            string = this.mContext.getString(R.string.group_in, new Object[]{lDisplayLabel});
        } else {
            string = this.mContext.getString(R.string.groups_in, new Object[]{lDisplayLabel});
        }
        textView.setText(CommonUtilMethods.upPercase(string));
        if (isSimAccount || "com.android.huawei.phone".equalsIgnoreCase(accountType.accountType)) {
            viewCache.accountName.setText(null);
        } else {
            viewCache.accountName.setText(entry.getAccountName());
        }
    }

    private static Uri getGroupUriFromId(long groupId) {
        return ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
    }

    public void setSmartGroupType(String SmartGroupType) {
        this.mSmartGroupType = SmartGroupType;
    }

    private void setSmartGroupPosition() {
        if (QueryUtil.isHAPProviderInstalled()) {
            if (EmuiFeatureManager.isChinaArea()) {
                this.mSmartGroupCompanyPosition = 0;
                this.mSmartGroupLocationPosition = 1;
                this.mSmartGroupLastContactTimePosition = 2;
                this.mSmartGroupItemCount = 3;
            } else {
                this.mSmartGroupCompanyPosition = 0;
                this.mSmartGroupLastContactTimePosition = 1;
                this.mSmartGroupItemCount = 2;
            }
        }
    }

    private boolean handlePredefinedGroup(int position, GroupListItemViewCache viewCache) {
        if (position == this.mSmartGroupCompanyPosition) {
            viewCache.groupTitle.setText(R.string.contacts_company);
            viewCache.accountHeader.setVisibility(0);
            viewCache.accountType.setText(upPercase(R.string.contacts_smart_group));
            viewCache.accountName.setText(null);
            viewCache.headerDividerTop.setVisibility(8);
            viewCache.divider.setVisibility(8);
            viewCache.groupMemberCount.setVisibility(8);
            viewCache.setGroupType("smart_groups_company");
            return true;
        } else if (position == this.mSmartGroupLocationPosition) {
            viewCache.groupTitle.setText(R.string.contacts_location);
            viewCache.accountHeader.setVisibility(8);
            viewCache.divider.setVisibility(0);
            viewCache.groupMemberCount.setVisibility(8);
            viewCache.setGroupType("smart_groups_location");
            return true;
        } else if (position != this.mSmartGroupLastContactTimePosition) {
            return false;
        } else {
            viewCache.groupTitle.setText(R.string.contacts_last_contact_time);
            viewCache.accountHeader.setVisibility(8);
            viewCache.divider.setVisibility(0);
            viewCache.groupMemberCount.setVisibility(8);
            viewCache.setGroupType("smart_groups_last_contact_time");
            return true;
        }
    }

    private View handleSmartGroup(int position, GroupListItemViewCache viewCache, View result) {
        GroupListItem entry = getItem(position);
        if (entry == null) {
            return result;
        }
        viewCache.groupTitle.setText(entry.getTitle());
        viewCache.accountHeader.setVisibility(8);
        viewCache.divider.setVisibility(8);
        String memberCountString = this.mContext.getResources().getQuantityString(R.plurals.group_list_num_contacts_in_group, entry.getMemberCount(), new Object[]{Integer.valueOf(entry.getMemberCount())});
        viewCache.groupMemberCount.setVisibility(0);
        viewCache.groupMemberCount.setText(memberCountString);
        setPredefinedSmartGroupType(position, viewCache);
        viewCache.setUri(SmartGroupUtil.SMART_GROUP_URI.buildUpon().appendQueryParameter("smart_groups_type", this.mSmartGroupType).appendQueryParameter("smart_group_title", entry.getTitle()).appendQueryParameter("predefined_smart_group_type", Integer.toString(viewCache.getPredefinedSmartGroupType())).build());
        return result;
    }

    private void setPredefinedSmartGroupType(int position, GroupListItemViewCache viewCache) {
        if ("smart_groups_company".equals(this.mSmartGroupType) && position == getCount() - 1) {
            viewCache.setPredefinedSmartGroupType(1);
        } else if ("smart_groups_location".equals(this.mSmartGroupType) && position == getCount() - 1) {
            viewCache.setPredefinedSmartGroupType(2);
        } else if ("smart_groups_last_contact_time".equals(this.mSmartGroupType)) {
            switch (position) {
                case 0:
                    viewCache.setPredefinedSmartGroupType(3);
                    return;
                case 1:
                    viewCache.setPredefinedSmartGroupType(4);
                    return;
                case 2:
                    viewCache.setPredefinedSmartGroupType(5);
                    return;
                case 3:
                    viewCache.setPredefinedSmartGroupType(6);
                    return;
                default:
                    return;
            }
        } else {
            viewCache.setPredefinedSmartGroupType(-1);
        }
    }

    public String upPercase(int resId) {
        return CommonUtilMethods.upPercase(this.mContext.getResources().getString(resId));
    }
}
