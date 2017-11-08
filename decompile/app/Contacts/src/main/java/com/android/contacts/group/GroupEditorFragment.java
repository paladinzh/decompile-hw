package com.android.contacts.group;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GroupMemberLoader;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;
import java.util.List;

public class GroupEditorFragment extends Fragment implements com.android.contacts.editor.SelectAccountDialogFragment.Listener {
    static final String[] PROJECTION_CONTACT = new String[]{"_id", "display_name", "display_name_alt", "sort_key", "starred", "contact_presence", "contact_chat_capability", "photo_id", "photo_thumb_uri", "lookup", "phonetic_name", "has_phone_number", "is_user_profile"};
    private String mAccountName;
    private String mAccountType;
    private String mAction;
    private long mContactId;
    private final LoaderCallbacks<Cursor> mContactLoaderListener = new LoaderCallbacks<Cursor>() {
        private long mRawContactId;

        public CursorLoader onCreateLoader(int id, Bundle args) {
            String memberId = args.getString("memberLookupUri");
            this.mRawContactId = args.getLong("rawContactId");
            return new CursorLoader(GroupEditorFragment.this.mContext, Uri.withAppendedPath(Contacts.CONTENT_URI, memberId), GroupEditorFragment.PROJECTION_CONTACT, null, null, null);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {
                long contactId = cursor.getLong(0);
                String displayName = cursor.getString(1);
                String lookupKey = cursor.getString(9);
                String photoUri = cursor.getString(8);
                GroupEditorFragment.this.getLoaderManager().destroyLoader(3);
                GroupEditorFragment.this.addMember(new Member(this.mRawContactId, lookupKey, contactId, displayName, photoUri));
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private ContentResolver mContentResolver;
    private Context mContext;
    private String mDataSet;
    private View mDividerView;
    private ArrayList<AccountWithDataSet> mFilteredAccountsWithDataSet;
    private long mGroupId;
    private final LoaderCallbacks<Cursor> mGroupMemberListLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return GroupMemberLoader.constructLoaderForGroupEditorQuery(GroupEditorFragment.this.mContext, GroupEditorFragment.this.mGroupId);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<Member> listExistingMembers = new ArrayList();
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    long contactId = data.getLong(0);
                    long rawContactId = data.getLong(1);
                    String lookupKey = data.getString(4);
                    String displayName = data.getString(2);
                    String photoUri = data.getString(3);
                    int photoId = data.getInt(5);
                    Member lMember = new Member(rawContactId, lookupKey, contactId, displayName, photoUri);
                    lMember.setPhotoId(photoId);
                    boolean lIsPrivateContact = false;
                    if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                        lIsPrivateContact = CommonUtilMethods.isPrivateContact(data);
                    }
                    lMember.setIsPrivate(lIsPrivateContact);
                    listExistingMembers.add(lMember);
                }
                GroupEditorFragment.this.addExistingMembers(listExistingMembers);
                GroupEditorFragment.this.getLoaderManager().destroyLoader(2);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private final LoaderCallbacks<Cursor> mGroupMetaDataLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new GroupMetaDataLoader(GroupEditorFragment.this.mContext, GroupEditorFragment.this.mGroupUri);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                GroupEditorFragment.this.bindGroupMetaData(data);
                GroupEditorFragment.this.getLoaderManager().initLoader(2, null, GroupEditorFragment.this.mGroupMemberListLoaderListener);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private boolean mGroupNameIsReadOnly;
    private TextView mGroupNameView;
    private Uri mGroupUri;
    private Bundle mIntentExtras;
    private boolean mIsFirstLoad = true;
    private boolean mIsPredefined;
    private long[] mJoinedMembers;
    private int mLastGroupEditorId;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Member> mListMembersToAdd = new ArrayList();
    private ArrayList<Member> mListMembersToRemove = new ArrayList();
    private ArrayList<Member> mListToDisplay = new ArrayList();
    private ListView mListView;
    private Listener mListener;
    private MemberListAdapter mMemberListAdapter;
    private TextView mMembersCountTextLabelView;
    private String mOriginalGroupName = "";
    private ContactPhotoManager mPhotoManager;
    private ViewGroup mRootView;
    private Status mStatus;

    public interface Listener {
        void onAccountsNotFound();

        void onGroupNotFound();

        void onReverted();

        void onSaveFinished(int i, Intent intent);
    }

    public static class CancelEditDialogFragment extends DialogFragment {
        public static void show(GroupEditorFragment fragment) {
            CancelEditDialogFragment dialog = new CancelEditDialogFragment();
            dialog.setTargetFragment(fragment, 0);
            dialog.show(fragment.getFragmentManager(), "cancelEditor");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity()).setIconAttribute(16843605).setTitle(R.string.cancel_confirmation_dialog_title).setPositiveButton(getString(R.string.contact_menu_discard), new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int whichButton) {
                    ((GroupEditorFragment) CancelEditDialogFragment.this.getTargetFragment()).doRevertAction();
                }
            }).setNegativeButton(17039360, null);
            if (!isAdded() || getActivity() == null) {
                builder.setMessage(R.string.cancel_confirmation_dialog_message);
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(R.string.cancel_confirmation_dialog_message);
                builder.setView(view);
            }
            return builder.create();
        }
    }

    public static class Member implements Parcelable {
        public static final Creator<Member> CREATOR = new Creator<Member>() {
            public Member createFromParcel(Parcel in) {
                return new Member(in);
            }

            public Member[] newArray(int size) {
                return new Member[size];
            }
        };
        private final long mContactId;
        private final String mDisplayName;
        private boolean mIsPrivate;
        private final Uri mLookupUri;
        private int mPhotoId;
        private final Uri mPhotoUri;
        private final long mRawContactId;

        public Member(long rawContactId, String lookupKey, long contactId, String displayName, String photoUri) {
            Uri uri = null;
            this.mRawContactId = rawContactId;
            this.mContactId = contactId;
            this.mLookupUri = Contacts.getLookupUri(contactId, lookupKey);
            this.mDisplayName = displayName;
            if (photoUri != null) {
                uri = Uri.parse(photoUri);
            }
            this.mPhotoUri = uri;
        }

        public long getRawContactId() {
            return this.mRawContactId;
        }

        public long getContactId() {
            return this.mContactId;
        }

        public Uri getLookupUri() {
            return this.mLookupUri;
        }

        public String getDisplayName() {
            return this.mDisplayName;
        }

        public Uri getPhotoUri() {
            return this.mPhotoUri;
        }

        public int getPhotoId() {
            return this.mPhotoId;
        }

        public void setPhotoId(int aPhotoId) {
            this.mPhotoId = aPhotoId;
        }

        public void setIsPrivate(boolean aIsPrivate) {
            this.mIsPrivate = aIsPrivate;
        }

        public boolean isPrivate() {
            return this.mIsPrivate;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Member)) {
                return false;
            }
            return Objects.equal(this.mLookupUri, ((Member) object).getLookupUri());
        }

        public int hashCode() {
            return ((((Long.valueOf(this.mContactId ^ (this.mContactId >>> 32)).intValue() + 31) * 31) + (this.mLookupUri == null ? 0 : this.mLookupUri.hashCode())) * 31) + Long.valueOf(this.mRawContactId ^ (this.mRawContactId >>> 32)).intValue();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.mRawContactId);
            dest.writeLong(this.mContactId);
            dest.writeParcelable(this.mLookupUri, flags);
            dest.writeString(this.mDisplayName);
            dest.writeParcelable(this.mPhotoUri, flags);
        }

        private Member(Parcel in) {
            this.mRawContactId = in.readLong();
            this.mContactId = in.readLong();
            this.mLookupUri = (Uri) in.readParcelable(getClass().getClassLoader());
            this.mDisplayName = in.readString();
            this.mPhotoUri = (Uri) in.readParcelable(getClass().getClassLoader());
        }
    }

    private final class MemberListAdapter extends BaseAdapter {
        private boolean mIsGroupMembershipEditable;

        private MemberListAdapter() {
            this.mIsGroupMembershipEditable = true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View result;
            int flags;
            if (convertView == null) {
                result = GroupEditorFragment.this.mLayoutInflater.inflate(this.mIsGroupMembershipEditable ? R.layout.group_member_item : R.layout.external_group_member_item, parent, false);
            } else {
                result = convertView;
            }
            Member member = getItem(position);
            ImageView badge = (QuickContactBadge) result.findViewById(R.id.badge);
            badge.setClickable(false);
            TextView name = (TextView) result.findViewById(R.id.name);
            String lDisplayname = member.getDisplayName();
            if (TextUtils.isEmpty(lDisplayname)) {
                lDisplayname = GroupEditorFragment.this.getString(R.string.missing_name);
            }
            name.setText(lDisplayname);
            View deleteButton = result.findViewById(R.id.delete_button_container);
            if (deleteButton != null) {
                final Member member2 = member;
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        GroupEditorFragment.this.removeMember(member2);
                        if (GroupEditorFragment.this.mListToDisplay.size() == 0) {
                            GroupEditorFragment.this.mDividerView.setVisibility(8);
                        }
                    }
                });
            }
            long photoId = (long) member.getPhotoId();
            if (member.isPrivate()) {
                flags = 4;
            } else {
                flags = 0;
            }
            if (photoId < -3) {
                GroupEditorFragment.this.mPhotoManager.loadThumbnail(badge, photoId, false, null, member.getContactId(), flags);
            } else {
                GroupEditorFragment.this.mPhotoManager.loadPhoto(badge, member.getPhotoUri(), ViewUtil.getConstantPreLayoutWidth(badge), false, flags, null);
            }
            return result;
        }

        public int getCount() {
            return GroupEditorFragment.this.mListToDisplay.size();
        }

        public Member getItem(int position) {
            return (Member) GroupEditorFragment.this.mListToDisplay.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public void setIsGroupMembershipEditable(boolean editable) {
            this.mIsGroupMembershipEditable = editable;
        }
    }

    public enum Status {
        SELECTING_ACCOUNT,
        LOADING,
        EDITING,
        SAVING,
        CLOSING
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        setHasOptionsMenu(true);
        this.mLayoutInflater = inflater;
        this.mRootView = (ViewGroup) inflater.inflate(R.layout.group_editor_fragment, container, false);
        return this.mRootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        this.mPhotoManager = ContactPhotoManager.getInstance(this.mContext);
        this.mMemberListAdapter = new MemberListAdapter();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
            if (this.mStatus != Status.SELECTING_ACCOUNT) {
                if (this.mStatus == Status.LOADING) {
                    startGroupMetaDataLoader();
                } else {
                    setupEditorForAccount();
                }
            }
        } else if ("android.intent.action.EDIT".equals(this.mAction)) {
            startGroupMetaDataLoader();
        } else if ("android.intent.action.INSERT".equals(this.mAction)) {
            Account account;
            String str;
            if (EmuiFeatureManager.isAndroidMVersion()) {
                if (this.mIntentExtras == null) {
                    account = null;
                } else {
                    account = (Account) this.mIntentExtras.getParcelable("android.provider.extra.ACCOUNT");
                }
                if (this.mIntentExtras == null) {
                    str = null;
                } else {
                    str = this.mIntentExtras.getString("android.provider.extra.DATA_SET");
                }
            } else {
                if (this.mIntentExtras == null) {
                    account = null;
                } else {
                    account = (Account) this.mIntentExtras.getParcelable("com.android.contacts.extra.ACCOUNT");
                }
                if (this.mIntentExtras == null) {
                    str = null;
                } else {
                    str = this.mIntentExtras.getString("com.android.contacts.extra.DATA_SET");
                }
            }
            if (account != null) {
                this.mAccountName = account.name;
                this.mAccountType = account.type;
                this.mDataSet = str;
                setupEditorForAccount();
                return;
            }
            this.mFilteredAccountsWithDataSet = getActivity().getIntent().getParcelableArrayListExtra("target_account");
            selectAccountAndCreateGroup();
        } else {
            HwLog.e("GroupEditorFragment", "Unknown Action String " + this.mAction + ". Only support " + "android.intent.action.EDIT" + " or " + "Intent.ACTION_INSERT");
            getActivity().finish();
        }
    }

    private void startGroupMetaDataLoader() {
        this.mStatus = Status.LOADING;
        getLoaderManager().initLoader(1, new Bundle(), this.mGroupMetaDataLoaderListener);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("action", this.mAction);
        outState.putParcelable("groupUri", this.mGroupUri);
        outState.putLong("groupId", this.mGroupId);
        outState.putSerializable("status", this.mStatus);
        outState.putString("accountName", this.mAccountName);
        outState.putString("accountType", this.mAccountType);
        outState.putString("dataSet", this.mDataSet);
        outState.putBoolean("groupNameIsReadOnly", this.mGroupNameIsReadOnly);
        outState.putString("originalGroupName", this.mOriginalGroupName);
        outState.putParcelableArrayList("membersToAdd", this.mListMembersToAdd);
        outState.putParcelableArrayList("membersToRemove", this.mListMembersToRemove);
        outState.putParcelableArrayList("membersToDisplay", this.mListToDisplay);
    }

    private void onRestoreInstanceState(Bundle state) {
        this.mAction = state.getString("action");
        this.mGroupUri = (Uri) state.getParcelable("groupUri");
        this.mGroupId = state.getLong("groupId");
        this.mStatus = (Status) state.getSerializable("status");
        this.mAccountName = state.getString("accountName");
        this.mAccountType = state.getString("accountType");
        this.mDataSet = state.getString("dataSet");
        this.mGroupNameIsReadOnly = state.getBoolean("groupNameIsReadOnly");
        this.mOriginalGroupName = state.getString("originalGroupName");
        this.mListMembersToAdd = state.getParcelableArrayList("membersToAdd");
        this.mListMembersToRemove = state.getParcelableArrayList("membersToRemove");
        this.mListToDisplay = state.getParcelableArrayList("membersToDisplay");
    }

    public void setContentResolver(ContentResolver resolver) {
        this.mContentResolver = resolver;
    }

    private void selectAccountAndCreateGroup() {
        List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(this.mContext).getGroupWritableAccounts();
        if (accounts.isEmpty()) {
            HwLog.e("GroupEditorFragment", "No accounts were found.");
            if (this.mListener != null) {
                this.mListener.onAccountsNotFound();
            }
        } else if (accounts.size() == 1) {
            this.mAccountName = ((AccountWithDataSet) accounts.get(0)).name;
            this.mAccountType = ((AccountWithDataSet) accounts.get(0)).type;
            this.mDataSet = ((AccountWithDataSet) accounts.get(0)).dataSet;
            setupEditorForAccount();
        } else if (this.mFilteredAccountsWithDataSet == null || this.mFilteredAccountsWithDataSet.size() != 1) {
            this.mStatus = Status.SELECTING_ACCOUNT;
            SelectAccountDialogFragment.show(getFragmentManager(), this, R.string.dialog_new_group_account, AccountListFilter.ACCOUNTS_GROUP_WRITABLE, null);
        } else {
            this.mAccountName = ((AccountWithDataSet) this.mFilteredAccountsWithDataSet.get(0)).name;
            this.mAccountType = ((AccountWithDataSet) this.mFilteredAccountsWithDataSet.get(0)).type;
            this.mDataSet = ((AccountWithDataSet) this.mFilteredAccountsWithDataSet.get(0)).dataSet;
            setupEditorForAccount();
        }
    }

    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        this.mAccountName = account.name;
        this.mAccountType = account.type;
        this.mDataSet = account.dataSet;
        setupEditorForAccount();
    }

    public void onAccountSelectorCancelled() {
        if (this.mListener != null) {
            this.mListener.onGroupNotFound();
        }
    }

    private AccountType getAccountType() {
        return AccountTypeManager.getInstance(this.mContext).getAccountType(this.mAccountType, this.mDataSet);
    }

    private boolean isGroupMembershipEditable() {
        if (this.mAccountType == null) {
            return false;
        }
        return getAccountType().isGroupMembershipEditable();
    }

    private void setupEditorForAccount() {
        View editorView;
        AccountType accountType = getAccountType();
        boolean editable = isGroupMembershipEditable();
        boolean isNewEditor = false;
        this.mMemberListAdapter.setIsGroupMembershipEditable(editable);
        int newGroupEditorId = editable ? R.layout.group_editor_view : R.layout.external_group_editor_view;
        if (newGroupEditorId != this.mLastGroupEditorId) {
            View oldEditorView = this.mRootView.findViewWithTag("currentEditorForAccount");
            if (oldEditorView != null) {
                this.mRootView.removeView(oldEditorView);
            }
            editorView = this.mLayoutInflater.inflate(newGroupEditorId, this.mRootView, false);
            editorView.setTag("currentEditorForAccount");
            this.mLastGroupEditorId = newGroupEditorId;
            isNewEditor = true;
        } else {
            editorView = this.mRootView.findViewWithTag("currentEditorForAccount");
            if (editorView == null) {
                throw new IllegalStateException("Group editor view not found");
            }
        }
        this.mGroupNameView = (TextView) editorView.findViewById(R.id.group_name);
        this.mGroupNameView.requestFocus();
        this.mMembersCountTextLabelView = (TextView) editorView.findViewById(R.id.label);
        Button lAddMembersButton = (Button) editorView.findViewById(R.id.btn_add_members);
        if (lAddMembersButton != null) {
            lAddMembersButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    StatisticalHelper.report(1149);
                    GroupEditorFragment.this.launchAddMultipleMembersToGroup();
                }
            });
        }
        this.mDividerView = editorView.findViewById(R.id.divider);
        this.mListView = (ListView) editorView.findViewById(16908298);
        this.mListView.setFastScrollEnabled(true);
        this.mListView.setAdapter(this.mMemberListAdapter);
        updateGroupMembersCountView();
        if (editorView.findViewById(R.id.account_header) != null) {
            CharSequence accountTypeDisplayLabel = accountType.getDisplayLabel(this.mContext);
            ImageView accountIcon = (ImageView) editorView.findViewById(R.id.account_icon);
            TextView accountTypeTextView = (TextView) editorView.findViewById(R.id.noneditableaccount_type);
            TextView accountNameTextView = (TextView) editorView.findViewById(R.id.noneditableaccount_name);
            ((LinearLayout) editorView.findViewById(R.id.noneditableaccountlayout)).setVisibility(0);
            ((LinearLayout) editorView.findViewById(R.id.account)).setVisibility(8);
            accountNameTextView.setVisibility(8);
            if (!(TextUtils.isEmpty(this.mAccountName) || "com.android.huawei.phone".equalsIgnoreCase(this.mAccountType) || CommonUtilMethods.isSimAccount(this.mAccountType))) {
                accountNameTextView.setVisibility(0);
                accountNameTextView.setText(this.mContext.getString(R.string.from_account_format, new Object[]{this.mAccountName}));
            }
            if (accountTypeTextView != null) {
                accountTypeTextView.setText(accountTypeDisplayLabel);
            }
            if (accountIcon != null) {
                accountIcon.setImageDrawable(accountType.getDisplayIcon(this.mContext));
            }
        }
        this.mGroupNameView.setFocusable(!this.mGroupNameIsReadOnly);
        if (isNewEditor) {
            this.mRootView.addView(editorView);
        }
        this.mStatus = Status.EDITING;
        if (this.mContactId != 0) {
            loadMemberToAddToGroup();
        }
    }

    private void updateGroupMembersCountView() {
        int lCountMembers = this.mMemberListAdapter.getCount();
        String format = getString(R.string.group_members_count_label, new Object[]{Integer.valueOf(lCountMembers)});
        if (this.mMembersCountTextLabelView != null) {
            this.mMembersCountTextLabelView.setText(format);
        }
    }

    public void onPause() {
        super.onPause();
        this.mContactId = 0;
    }

    public void load(String action, Uri groupUri, Bundle intentExtras) {
        this.mAction = action;
        this.mGroupUri = groupUri;
        this.mGroupId = groupUri != null ? ContentUris.parseId(this.mGroupUri) : 0;
        this.mIntentExtras = intentExtras;
    }

    private void bindGroupMetaData(Cursor cursor) {
        if (cursor.moveToFirst()) {
            this.mOriginalGroupName = cursor.getString(4);
            this.mAccountName = cursor.getString(0);
            this.mAccountType = cursor.getString(1);
            this.mOriginalGroupName = CommonUtilMethods.parseGroupDisplayName(this.mAccountType, this.mOriginalGroupName, this.mContext, cursor.getString(9), cursor.getInt(10), cursor.getString(11));
            this.mIsPredefined = CommonUtilMethods.isPredefinedGroup(cursor.getString(12));
            this.mDataSet = cursor.getString(2);
            this.mGroupNameIsReadOnly = cursor.getInt(7) == 1;
            setupEditorForAccount();
            if (this.mIsFirstLoad) {
                this.mGroupNameView.setText(this.mOriginalGroupName);
                this.mIsFirstLoad = false;
            }
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
            }
            return;
        }
        HwLog.i("GroupEditorFragment", "Group not found with URI: " + this.mGroupUri + " Closing activity now.");
        if (this.mListener != null) {
            this.mListener.onGroupNotFound();
        }
    }

    public void loadMemberToAddToGroup(long rawContactId, String contactId) {
        Bundle args = new Bundle();
        args.putLong("rawContactId", rawContactId);
        args.putString("memberLookupUri", contactId);
        getLoaderManager().restartLoader(3, args, this.mContactLoaderListener);
    }

    private void loadMemberToAddToGroup() {
        String[] args;
        long aRawContactId = -1;
        String whereClause = "contact_id = " + this.mContactId + " AND " + "deleted" + " = 0";
        String accountClause = "account_name=? AND account_type=?";
        if (this.mDataSet == null) {
            accountClause = accountClause + " AND data_set IS NULL";
            args = new String[]{this.mAccountName, this.mAccountType};
        } else {
            accountClause = accountClause + " AND data_set=?";
            args = new String[]{this.mAccountName, this.mAccountType, this.mDataSet};
        }
        Cursor cursor = this.mContentResolver.query(RawContacts.CONTENT_URI, new String[]{"_id"}, whereClause + " AND " + accountClause, args, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int i = 0;
                this.mJoinedMembers = new long[cursor.getCount()];
                do {
                    aRawContactId = cursor.getLong(cursor.getColumnIndex("_id"));
                    this.mJoinedMembers[i] = aRawContactId;
                    i++;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (aRawContactId != -1) {
            loadMemberToAddToGroup(aRawContactId, String.valueOf(this.mContactId));
        }
    }

    public void setContactId(long aContactID) {
        this.mContactId = aContactID;
    }

    public void setListener(Listener value) {
        this.mListener = value;
    }

    public void onDoneClicked() {
        if (isGroupMembershipEditable()) {
            save();
        } else {
            doRevertAction();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_discard:
                return revert();
            default:
                return false;
        }
    }

    private void launchAddMultipleMembersToGroup() {
        long[] lSelectedContactIds;
        Intent lIntent = new Intent("android.intent.action.HAP_ADD_GROUP_MEMBERS");
        lIntent.setPackage("com.android.contacts");
        lIntent.putExtra("extra_group_id", this.mGroupId);
        lIntent.putExtra("extra_account_name", this.mAccountName);
        lIntent.putExtra("extra_account_type", this.mAccountType);
        lIntent.putExtra("extra_account_data_set", this.mDataSet);
        long[] lRemovedContactIds = convertToArray(this.mListMembersToRemove);
        if (this.mJoinedMembers != null) {
            lSelectedContactIds = getAllNewlyAddedMembers();
        } else {
            lSelectedContactIds = convertToArray(this.mListMembersToAdd);
        }
        lIntent.putExtra("selected_members_raw_contact_ids", lSelectedContactIds);
        lIntent.putExtra("removed_members_raw_contact_ids", lRemovedContactIds);
        lIntent.putExtra("result_required_back", true);
        startActivityForResult(lIntent, 10);
    }

    public void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (HwLog.HWFLOW) {
            HwLog.i("GroupEditorFragment", "onActivityResult() called.. aRequestCode " + aRequestCode + " aResultCode : " + aResultCode + " aData : " + aData);
        }
        if (aResultCode == -1 && aRequestCode == 10 && aData != null) {
            ContactsApplication contactsApp = (ContactsApplication) this.mContext.getApplicationContext();
            ArrayList<Member> lSelectedMembers = contactsApp.getSelectedMemberList();
            if (lSelectedMembers != null) {
                this.mDividerView.setVisibility(0);
                if (lSelectedMembers.size() != 0) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("GroupEditorFragment", "lSelectedMembers size : " + lSelectedMembers.size());
                    }
                    this.mListToDisplay.addAll(lSelectedMembers);
                    this.mListMembersToAdd.addAll(lSelectedMembers);
                    this.mListMembersToAdd.removeAll(this.mListMembersToRemove);
                    this.mListMembersToRemove.removeAll(lSelectedMembers);
                    this.mMemberListAdapter.notifyDataSetChanged();
                    updateGroupMembersCountView();
                    contactsApp.clearSelectedMemberList();
                }
            }
        }
    }

    public boolean revert() {
        if (hasNameChange() || hasMembershipChange()) {
            CancelEditDialogFragment.show(this);
        } else {
            doRevertAction();
        }
        return true;
    }

    private void doRevertAction() {
        this.mStatus = Status.CLOSING;
        if (this.mListener != null) {
            this.mListener.onReverted();
        }
    }

    private long[] getAllNewlyAddedMembers() {
        int lJoinedMembersCount = this.mJoinedMembers.length;
        int lNewMembersCount = this.mListMembersToAdd.size();
        long[] lMembersToAddArray = new long[((lJoinedMembersCount + lNewMembersCount) - 1)];
        int i = 0;
        while (i < lJoinedMembersCount) {
            lMembersToAddArray[i] = this.mJoinedMembers[i];
            i++;
        }
        for (int j = 1; j < lNewMembersCount; j++) {
            lMembersToAddArray[i] = ((Member) this.mListMembersToAdd.get(j)).getRawContactId();
            i++;
        }
        return lMembersToAddArray;
    }

    public boolean save() {
        if (hasValidGroupName() && this.mStatus == Status.EDITING) {
            getLoaderManager().destroyLoader(2);
            if (hasNameChange() || hasMembershipChange()) {
                this.mStatus = Status.SAVING;
                Context activity = getActivity();
                if (activity == null) {
                    return false;
                }
                Intent saveIntent;
                long[] membersToAddArray;
                if ("android.intent.action.INSERT".equals(this.mAction)) {
                    if (this.mJoinedMembers != null) {
                        membersToAddArray = getAllNewlyAddedMembers();
                    } else {
                        membersToAddArray = convertToArray(this.mListMembersToAdd);
                    }
                    saveIntent = ContactSaveService.createNewGroupIntent(activity, new AccountWithDataSet(this.mAccountName, this.mAccountType, this.mDataSet), this.mGroupNameView.getText().toString(), membersToAddArray, activity.getClass(), "saveCompleted");
                } else if ("android.intent.action.EDIT".equals(this.mAction)) {
                    membersToAddArray = convertToArray(this.mListMembersToAdd);
                    Context context = activity;
                    long[] jArr = membersToAddArray;
                    saveIntent = ContactSaveService.createGroupUpdateIntent(context, this.mGroupId, getUpdatedName(), jArr, getContactIds(this.mListMembersToRemove), activity.getClass(), "saveCompleted", this.mIsPredefined);
                } else {
                    throw new IllegalStateException("Invalid intent action type " + this.mAction);
                }
                activity.startService(saveIntent);
                return true;
            }
            onSaveCompleted(false, this.mGroupUri);
            return true;
        }
        if (this.mMemberListAdapter.getCount() <= 0 || hasValidGroupName()) {
            getActivity().finish();
        } else {
            Toast.makeText(this.mContext, R.string.cannot_save_group_Toast, 0).show();
        }
        return false;
    }

    public void onSaveCompleted(boolean hadChanges, Uri groupUri) {
        int resultCode;
        Intent resultIntent;
        boolean success = groupUri != null;
        HwLog.d("GroupEditorFragment", "onSaveCompleted(" + groupUri + ")");
        if (hadChanges && !success) {
            Toast.makeText(this.mContext, R.string.groupSavedErrorToast_Toast, 0).show();
        }
        if (!success || groupUri == null) {
            resultCode = 0;
            resultIntent = null;
        } else {
            String requestAuthority = groupUri.getAuthority();
            resultIntent = new Intent();
            if ("contacts".equals(requestAuthority)) {
                resultIntent.setData(ContentUris.withAppendedId(Uri.parse("content://contacts/groups"), ContentUris.parseId(groupUri)));
            } else {
                resultIntent.setData(groupUri);
            }
            resultCode = -1;
        }
        this.mStatus = Status.CLOSING;
        if (this.mListener != null) {
            this.mListener.onSaveFinished(resultCode, resultIntent);
        }
    }

    private boolean hasValidGroupName() {
        return (this.mGroupNameView == null || TextUtils.isEmpty(this.mGroupNameView.getText().toString().trim())) ? false : true;
    }

    private boolean hasNameChange() {
        if (this.mGroupNameView == null || this.mGroupNameView.getText().toString().equals(this.mOriginalGroupName)) {
            return false;
        }
        return true;
    }

    private boolean hasMembershipChange() {
        return this.mListMembersToAdd.size() > 0 || this.mListMembersToRemove.size() > 0;
    }

    private String getUpdatedName() {
        String groupNameFromTextView = this.mGroupNameView.getText().toString().trim();
        if (groupNameFromTextView.equals(this.mOriginalGroupName)) {
            return null;
        }
        return groupNameFromTextView;
    }

    private static long[] convertToArray(List<Member> listMembers) {
        int size = listMembers.size();
        long[] membersArray = new long[size];
        for (int i = 0; i < size; i++) {
            membersArray[i] = ((Member) listMembers.get(i)).getRawContactId();
        }
        return membersArray;
    }

    private static long[] getContactIds(List<Member> listMembers) {
        int size = listMembers.size();
        long[] membersArray = new long[size];
        for (int i = 0; i < size; i++) {
            membersArray[i] = ((Member) listMembers.get(i)).getContactId();
        }
        return membersArray;
    }

    private void addExistingMembers(List<Member> members) {
        this.mListToDisplay.clear();
        this.mListToDisplay.addAll(members);
        for (Member lMem : this.mListMembersToAdd) {
            if (!this.mListToDisplay.contains(lMem)) {
                this.mListToDisplay.add(lMem);
            }
        }
        this.mListToDisplay.removeAll(this.mListMembersToRemove);
        this.mMemberListAdapter.notifyDataSetChanged();
        updateGroupMembersCountView();
    }

    private void addMember(Member member) {
        if (!this.mListMembersToAdd.contains(member)) {
            this.mListMembersToAdd.add(member);
        }
        if (!this.mListToDisplay.contains(member)) {
            this.mListToDisplay.add(member);
        }
        this.mMemberListAdapter.notifyDataSetChanged();
        updateGroupMembersCountView();
        if (this.mListMembersToRemove.contains(member)) {
            this.mListMembersToRemove.remove(member);
        }
    }

    private void removeMember(Member member) {
        if (this.mJoinedMembers != null && ((Member) this.mListMembersToAdd.get(0)).equals(member)) {
            this.mJoinedMembers = null;
        }
        if (this.mListMembersToAdd.contains(member)) {
            this.mListMembersToAdd.remove(member);
        }
        this.mListMembersToRemove.add(member);
        this.mListToDisplay.remove(member);
        this.mMemberListAdapter.notifyDataSetChanged();
        updateGroupMembersCountView();
    }
}
