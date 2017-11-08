package com.android.contacts.hap.editor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.android.contacts.util.TextUtil;
import com.google.android.gms.R;
import java.util.HashSet;

public class GroupSelectListAdapter extends BaseAdapter {
    private final AccountTypeManager mAccountTypeManager;
    private final Context mContext;
    protected Cursor mCursor;
    private HashSet<Long> mGroupList = new HashSet();
    private final LayoutInflater mLayoutInflater;

    private static class GroupListItemViewCache {
        public final View accountHeader;
        public final TextView accountName;
        public final TextView accountType;
        public final CheckBox checkBox;
        public final TextView groupTitle;
        public final LinearLayout titleParentView;

        public GroupListItemViewCache(View view) {
            this.accountType = (TextView) view.findViewById(R.id.account_type);
            this.accountName = (TextView) view.findViewById(R.id.account_name);
            this.groupTitle = (TextView) view.findViewById(R.id.label);
            this.checkBox = (CheckBox) view.findViewById(R.id.check_box);
            this.accountHeader = view.findViewById(R.id.group_list_header);
            this.titleParentView = (LinearLayout) view.findViewById(R.id.group_account_header_main);
        }
    }

    public GroupSelectListAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mAccountTypeManager = AccountTypeManager.getInstance(this.mContext);
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    public int getCount() {
        return (this.mCursor == null || this.mCursor.isClosed()) ? 0 : this.mCursor.getCount();
    }

    public GroupSelectListItem getItem(int position) {
        if (this.mCursor == null || this.mCursor.isClosed() || !this.mCursor.moveToPosition(position)) {
            return null;
        }
        String accountName = this.mCursor.getString(0);
        String accountType = this.mCursor.getString(1);
        String dataSet = this.mCursor.getString(2);
        long groupId = this.mCursor.getLong(3);
        String title = this.mCursor.getString(4);
        boolean isGroupReadOnly = this.mCursor.getInt(7) == 1;
        boolean isPrivateGroup = this.mContext.getString(R.string.private_group_sync1).equals(this.mCursor.getString(12));
        if (TextUtils.isEmpty(title)) {
            title = HwCustPreloadContacts.EMPTY_STRING;
            HwLog.i("GroupSelectListAdapter", "dirty data");
        } else {
            title = CamcardGroup.replaceTitle(CommonUtilMethods.parseGroupDisplayName(accountType, title, this.mContext, this.mCursor.getString(9), this.mCursor.getInt(10), this.mCursor.getString(11)), this.mContext);
        }
        int previousIndex = position - 1;
        boolean isFirstGroupInAccount = true;
        if (previousIndex >= 0 && this.mCursor.moveToPosition(previousIndex)) {
            String previousGroupAccountName = this.mCursor.getString(0);
            String previousGroupAccountType = this.mCursor.getString(1);
            String previousGroupDataSet = this.mCursor.getString(2);
            if (accountName != null && accountType != null && accountName.equals(previousGroupAccountName) && accountType.equals(previousGroupAccountType) && Objects.equal(dataSet, previousGroupDataSet)) {
                isFirstGroupInAccount = false;
            }
        }
        boolean isChecked = false;
        if (hasMembership(groupId)) {
            isChecked = true;
        }
        return new GroupSelectListItem(accountName, accountType, dataSet, groupId, title, isFirstGroupInAccount, isChecked, isGroupReadOnly, isPrivateGroup);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !(convertView.getTag() instanceof GroupListItemViewCache)) {
            convertView = this.mLayoutInflater.inflate(R.layout.group_select_list_item, parent, false);
            convertView.setTag(new GroupListItemViewCache(convertView));
        }
        View result = convertView;
        GroupListItemViewCache viewCache = (GroupListItemViewCache) convertView.getTag();
        GroupSelectListItem entry = getItem(position);
        if (entry == null) {
            return result;
        }
        if (entry.isFirstGroupInAccount()) {
            bindHeaderView(entry, viewCache);
            viewCache.accountHeader.setVisibility(0);
        } else {
            viewCache.accountHeader.setVisibility(8);
        }
        viewCache.groupTitle.setText(entry.getTitle());
        viewCache.checkBox.setChecked(entry.isChecked());
        return result;
    }

    public void bindView(int position, View convertView, ViewGroup parent) {
        GroupListItemViewCache viewCache = (GroupListItemViewCache) convertView.getTag();
        GroupSelectListItem entry = getItem(position);
        if (entry != null) {
            viewCache.checkBox.setChecked(entry.isChecked());
        }
    }

    private void bindHeaderView(GroupSelectListItem entry, GroupListItemViewCache viewCache) {
        AccountType accountType = this.mAccountTypeManager.getAccountType(entry.getAccountType(), entry.getDataSet());
        String lDisplayLabel = accountType.getDisplayLabel(this.mContext).toString();
        String accountTypeValue = this.mContext.getString(R.string.groups_in, new Object[]{lDisplayLabel});
        Object accountNameValue = entry.getAccountName();
        viewCache.accountType.setText(accountTypeValue);
        if ("com.android.huawei.phone".equalsIgnoreCase(accountType.accountType)) {
            accountNameValue = null;
        }
        viewCache.accountName.setText(accountNameValue);
        int accountTypeWidth = TextUtil.getTextWidth(accountTypeValue, viewCache.accountType.getTextSize());
        int accountNameWidth = TextUtil.getTextWidth(accountNameValue, viewCache.accountName.getTextSize());
        int marginRight = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_right_margin);
        if (((accountTypeWidth + accountNameWidth) + marginRight) + this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_popup_list_item_padding) > this.mContext.getResources().getDisplayMetrics().widthPixels) {
            viewCache.titleParentView.setOrientation(1);
            viewCache.accountName.setGravity(8388611);
            return;
        }
        viewCache.titleParentView.setOrientation(0);
        viewCache.accountName.setGravity(8388613);
    }

    public void setGroupList(HashSet<Long> groupList) {
        this.mGroupList = groupList;
    }

    private boolean hasMembership(long groupId) {
        if (this.mGroupList != null) {
            for (Long id : this.mGroupList) {
                if (id.longValue() == groupId) {
                    return true;
                }
            }
        }
        return false;
    }
}
