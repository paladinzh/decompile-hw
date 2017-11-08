package com.android.contacts.editor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.GroupMetaData;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.activities.GroupSelectActivity;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.Objects;
import com.google.android.gms.R;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GroupMembershipView extends RelativeLayout implements OnClickListener, ContactEditorInfo {
    List<AccountWithDataSet> mAccountsList = new ArrayList();
    private DataKind mCachedDataKind = null;
    private long[] mCheckedGroupList = null;
    private long mDefaultGroupId;
    private boolean mDefaultGroupVisibilityKnown;
    private boolean mDefaultGroupVisible;
    private long mFavoritesGroupId;
    private ContactEditorFragment mFragment;
    private Map<Long, AccountWithDataSet> mGroupIdAccounts = Maps.newHashMap();
    private TextView mGroupList;
    private TextView mGroupLlstText;
    private Cursor mGroupMetaData;
    private String mNoGroupString;
    private HashSet<GroupMetaData> mSelectedGroupsMetaData = new HashSet();
    private RawContactDeltaList mStateList;

    public GroupMembershipView(Context context) {
        super(context);
    }

    public GroupMembershipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mNoGroupString = getContext().getString(R.string.contact_jointo_group);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.mGroupList != null) {
            this.mGroupList.setEnabled(enabled);
        }
    }

    public void setGroupMetaData(Cursor groupMetaData) {
        this.mGroupMetaData = groupMetaData;
        loadAllGroupData();
        if (this.mCheckedGroupList == null || this.mCheckedGroupList.length <= 0) {
            updateView();
            return;
        }
        updateGroup(this.mCheckedGroupList);
        this.mCheckedGroupList = null;
    }

    public void setFragment(ContactEditorFragment aFragment) {
        this.mFragment = aFragment;
    }

    public void setState(RawContactDeltaList stateList) {
        this.mStateList = stateList;
        if (this.mStateList != null && !this.mStateList.isEmpty()) {
            for (RawContactDelta state : this.mStateList) {
                AccountWithDataSet account = new AccountWithDataSet(state.getAccountName(), state.getAccountType(), state.getDataSet());
                if (!this.mAccountsList.contains(account)) {
                    this.mAccountsList.add(account);
                }
            }
            loadAllGroupData();
            this.mDefaultGroupVisibilityKnown = false;
            updateView();
        }
    }

    public void setState(RawContactDelta state) {
        this.mStateList = new RawContactDeltaList();
        this.mStateList.add(state);
        String accountType = state.getAccountType();
        AccountWithDataSet account = new AccountWithDataSet(state.getAccountName(), accountType, state.getDataSet());
        if (!this.mAccountsList.contains(account)) {
            this.mAccountsList.add(account);
        }
        if (this.mFragment != null) {
            AccountType oldAccountType = this.mFragment.getOldAccountType();
            if (!(oldAccountType == null || accountType.equals(oldAccountType.accountType))) {
                updateGroup(new long[]{this.mDefaultGroupId});
            }
        }
        loadAllGroupData();
        this.mDefaultGroupVisibilityKnown = false;
        updateView();
    }

    private void updateView() {
        if (this.mGroupMetaData == null || this.mGroupMetaData.isClosed()) {
            setVisibility(8);
            return;
        }
        this.mFavoritesGroupId = 0;
        this.mDefaultGroupId = 0;
        StringBuilder sb = new StringBuilder();
        this.mGroupMetaData.moveToPosition(-1);
        while (this.mGroupMetaData.moveToNext()) {
            String accountName = this.mGroupMetaData.getString(0);
            String accountType = this.mGroupMetaData.getString(1);
            if (this.mAccountsList.contains(new AccountWithDataSet(accountName, accountType, this.mGroupMetaData.getString(2)))) {
                long groupId = this.mGroupMetaData.getLong(3);
                if (!this.mGroupMetaData.isNull(6) && this.mGroupMetaData.getInt(6) != 0) {
                    this.mFavoritesGroupId = groupId;
                } else if (!(this.mGroupMetaData.isNull(5) || this.mGroupMetaData.getInt(5) == 0)) {
                    this.mDefaultGroupId = groupId;
                }
                if (!(groupId == this.mFavoritesGroupId || groupId == this.mDefaultGroupId || !hasMembership(groupId, this.mStateList))) {
                    String title = this.mGroupMetaData.getString(4);
                    if (!TextUtils.isEmpty(title)) {
                        title = CommonUtilMethods.parseGroupDisplayName(accountType, title, getContext(), this.mGroupMetaData.getString(9), this.mGroupMetaData.getInt(10), this.mGroupMetaData.getString(11));
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        sb.append(title);
                    }
                }
            }
        }
        if (this.mGroupList == null) {
            this.mGroupList = (TextView) findViewById(R.id.group_list);
        }
        setOnClickListener(this);
        if (this.mGroupLlstText == null) {
            this.mGroupLlstText = (TextView) findViewById(R.id.group_list_text);
        }
        this.mGroupLlstText.setEnabled(isEnabled());
        this.mGroupList.setEnabled(isEnabled());
        if (sb.length() == 0) {
            this.mGroupList.setText(this.mNoGroupString);
            this.mGroupLlstText.setText("");
        } else {
            StringBuilder sb_new = new StringBuilder();
            sb_new.append(getContext().getString(R.string.groupsLabel));
            sb_new.append(": ");
            this.mGroupList.setText(sb_new);
            this.mGroupLlstText.setText(CamcardGroup.replaceTitle(sb, getContext()));
            setVisibility(0);
        }
        if (!this.mDefaultGroupVisibilityKnown) {
            boolean z = (this.mDefaultGroupId == 0 || hasMembership(this.mDefaultGroupId, this.mStateList)) ? false : true;
            this.mDefaultGroupVisible = z;
            this.mDefaultGroupVisibilityKnown = true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadAllGroupData() {
        if (!(this.mGroupMetaData == null || this.mGroupMetaData.isClosed() || this.mAccountsList.isEmpty())) {
            this.mGroupIdAccounts.clear();
            this.mGroupMetaData.moveToPosition(-1);
            while (this.mGroupMetaData.moveToNext()) {
                AccountWithDataSet account = new AccountWithDataSet(this.mGroupMetaData.getString(0), this.mGroupMetaData.getString(1), this.mGroupMetaData.getString(2));
                if (this.mAccountsList.contains(account)) {
                    this.mGroupIdAccounts.put(Long.valueOf(this.mGroupMetaData.getLong(3)), account);
                }
            }
        }
    }

    public void onClick(View v) {
        Intent lSelectIntent = new Intent(getContext(), GroupSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("groupAccountsList", (ArrayList) this.mAccountsList);
        long[] groupID = null;
        if (!(this.mStateList == null || this.mStateList.isEmpty())) {
            ArrayList<ValuesDelta> entries;
            int size = 0;
            for (RawContactDelta state : this.mStateList) {
                entries = state.getMimeEntries("vnd.android.cursor.item/group_membership");
                if (entries != null && entries.size() > 0) {
                    size += entries.size();
                }
            }
            if (size > 0) {
                int index = 0;
                groupID = new long[size];
                for (RawContactDelta state2 : this.mStateList) {
                    entries = state2.getMimeEntries("vnd.android.cursor.item/group_membership");
                    if (entries != null && entries.size() > 0) {
                        for (ValuesDelta values : entries) {
                            if (!values.isDelete()) {
                                Long id = values.getGroupRowId();
                                if (!(id == null || id.longValue() == this.mFavoritesGroupId || id.longValue() == this.mDefaultGroupId)) {
                                    int index2 = index + 1;
                                    groupID[index] = id.longValue();
                                    index = index2;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (groupID != null) {
            bundle.putLongArray("currentGroupList", groupID);
        }
        lSelectIntent.putExtras(bundle);
        if (this.mFragment != null) {
            this.mFragment.setFragmentStatus(4);
            this.mFragment.startActivityForResult(lSelectIntent, 1002);
        }
    }

    public void updateGroup(long[] checkedGroupList) {
        if (this.mStateList != null && !this.mStateList.isEmpty()) {
            for (RawContactDelta state : this.mStateList) {
                ArrayList<ValuesDelta> entries = state.getMimeEntries("vnd.android.cursor.item/group_membership");
                if (entries != null) {
                    for (ValuesDelta entry : entries) {
                        if (!entry.isDelete()) {
                            Long groupId = entry.getGroupRowId();
                            if (!(groupId == null || groupId.longValue() == this.mFavoritesGroupId)) {
                                if ((groupId.longValue() != this.mDefaultGroupId || this.mDefaultGroupVisible) && !isGroupIdIncluded(checkedGroupList, groupId.longValue())) {
                                    entry.markDeleted();
                                }
                            }
                        }
                    }
                }
            }
            if (checkedGroupList != null) {
                AccountTypeManager accountTypeManager = AccountTypeManager.getInstance(getContext());
                for (long groupId2 : checkedGroupList) {
                    AccountWithDataSet account = (AccountWithDataSet) this.mGroupIdAccounts.get(Long.valueOf(groupId2));
                    if (account != null) {
                        RawContactDeltaList<RawContactDelta> stateList = getRawContactState(account);
                        if (!(stateList == null || stateList.isEmpty())) {
                            for (RawContactDelta state2 : stateList) {
                                if (!hasMembership(groupId2, state2)) {
                                    RawContactModifier.insertChild(state2, state2.getAccountType(accountTypeManager).getKindForMimetype("vnd.android.cursor.item/group_membership")).setGroupRowId(groupId2);
                                }
                            }
                        }
                    }
                }
            }
            updateView();
        }
    }

    public void setCheckedGroupList(long[] checkedGroupList) {
        if (checkedGroupList != null && checkedGroupList.length > 0) {
            this.mCheckedGroupList = new long[checkedGroupList.length];
            System.arraycopy(checkedGroupList, 0, this.mCheckedGroupList, 0, checkedGroupList.length);
        }
    }

    public boolean isAllGroupLoaded(long[] checkedGroupList) {
        if (checkedGroupList != null && checkedGroupList.length > 0) {
            for (long groupId : checkedGroupList) {
                if (((AccountWithDataSet) this.mGroupIdAccounts.get(Long.valueOf(groupId))) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private RawContactDeltaList getRawContactState(AccountWithDataSet account) {
        RawContactDeltaList stateList = null;
        if (!(this.mStateList == null || this.mStateList.isEmpty())) {
            stateList = new RawContactDeltaList();
            for (RawContactDelta state : this.mStateList) {
                if (account != null && account.type != null && account.name != null && account.type.equals(state.getAccountType()) && account.name.equals(state.getAccountName()) && Objects.equal(account.dataSet, state.getDataSet())) {
                    stateList.add(state);
                }
            }
        }
        return stateList;
    }

    private boolean isGroupIdIncluded(long[] checkedGroupList, long groupId) {
        if (checkedGroupList == null) {
            return false;
        }
        if (length > 0) {
            for (long j : checkedGroupList) {
                if (groupId == j) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasMembership(long groupId, RawContactDelta state) {
        if (state == null) {
            return false;
        }
        if (groupId == this.mDefaultGroupId && state.isContactInsert()) {
            return true;
        }
        ArrayList<ValuesDelta> entries = state.getMimeEntries("vnd.android.cursor.item/group_membership");
        if (entries != null) {
            for (ValuesDelta values : entries) {
                if (!values.isDelete()) {
                    Long id = values.getGroupRowId();
                    if (id != null && id.longValue() == groupId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasMembership(long groupId, RawContactDeltaList stateList) {
        if (!(stateList == null || stateList.isEmpty())) {
            for (RawContactDelta state : stateList) {
                if (groupId == this.mDefaultGroupId && state.isContactInsert()) {
                    return true;
                }
                ArrayList<ValuesDelta> entries = state.getMimeEntries("vnd.android.cursor.item/group_membership");
                if (entries != null) {
                    for (ValuesDelta values : entries) {
                        if (!values.isDelete()) {
                            Long id = values.getGroupRowId();
                            if (id != null && id.longValue() == groupId) {
                                return true;
                            }
                        }
                    }
                    continue;
                }
            }
        }
        return false;
    }

    public HashSet<GroupMetaData> getSeletedGroupsMetaData() {
        return this.mSelectedGroupsMetaData;
    }

    public String getTitle() {
        return this.mNoGroupString;
    }

    public DataKind getKind() {
        return this.mCachedDataKind;
    }

    public void setKind(DataKind kind) {
        this.mCachedDataKind = kind;
    }
}
