package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactStatusUtil;
import com.android.contacts.GroupMemberLoader;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.GroupAndContactMetaData;
import com.android.contacts.hap.GroupAndContactMetaData.GroupsData;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.list.ContactTileAdapter$ContactEntry;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.LogConfig;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class IceActivity extends Activity implements OnClickListener {
    private CreateNewGroupTask createNewGroupTask;
    private long huawei_ice_group_id = -1;
    private ListView iceListview;
    private boolean isDialerMode = false;
    private OnItemLongClickListener itemListLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long arg3) {
            final Context context = view.getContext();
            Builder builder = new Builder(context);
            IceActivity.this.mMemberListCursor.moveToPosition(position);
            long contactId = IceActivity.this.mMemberListCursor.getLong(0);
            GroupsData removeGroupData = new GroupsData(IceActivity.this.huawei_ice_group_id, HwCustCommonConstants.ICE_ACCOUNT_NAME, HwCustCommonConstants.ICE_ACCOUNT_TYPE, null);
            builder.setTitle(IceActivity.this.mMemberListCursor.getString(3));
            final long j = contactId;
            final GroupsData groupsData = removeGroupData;
            builder.setNeutralButton(R.string.str_remove_from_group, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    context.startService(ExtendedContactSaveService.createAddAndRemoveMembersToGroupIntent(context, new GroupAndContactMetaData(new long[]{j}, null, groupsData), true));
                }
            });
            builder.show();
            return true;
        }
    };
    private String mDataset;
    private final LoaderCallbacks<Cursor> mGroupMemberListLoaderListener = new LoaderCallbacks<Cursor>() {
        private IceGroupMemberAdapter mAdapter;

        public CursorLoader onCreateLoader(int id, Bundle args) {
            return GroupMemberLoader.constructLoaderForGroupDetailQuery(IceActivity.this, IceActivity.this.huawei_ice_group_id);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                IceActivity.this.mMemberListCursor = data;
                if (this.mAdapter == null) {
                    this.mAdapter = new IceGroupMemberAdapter(IceActivity.this);
                    IceActivity.this.iceListview.setAdapter(this.mAdapter);
                } else {
                    this.mAdapter.notifyDataSetChanged();
                }
                IceActivity.this.invalidateOptionsMenu();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private final LoaderCallbacks<Cursor> mGroupMetadataLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new GroupMetaDataLoader(IceActivity.this, IceActivity.this.mGroupUri);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            boolean deleted = true;
            if (data != null) {
                data.moveToPosition(-1);
                if (data.moveToNext()) {
                    if (data.getInt(8) != 1) {
                        deleted = false;
                    }
                    if (!deleted) {
                        IceActivity.this.startGroupMembersLoader();
                    }
                }
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private Uri mGroupUri;
    private Cursor mMemberListCursor;
    OnItemClickListener onListItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
            Intent intent = new Intent("android.intent.action.VIEW", IceActivity.this.getAndUpdateLookupKey(IceActivity.this.createContactEntryFromCursor(IceActivity.this.mMemberListCursor, position)));
            intent.setClass(IceActivity.this, ContactDetailActivity.class);
            IceActivity.this.startActivity(intent);
        }
    };
    private TextView tvMyEmergencyContactsHeader;

    class CreateNewGroupTask extends AsyncTask<Void, Void, String> {
        Context context;

        public CreateNewGroupTask(Context c) {
            this.context = c;
        }

        protected String doInBackground(Void... arg0) {
            IceActivity.this.createNewGroup(this.context, new AccountWithDataSet(HwCustCommonConstants.ICE_ACCOUNT_NAME, HwCustCommonConstants.ICE_ACCOUNT_TYPE, null), HwCustCommonConstants.ICE_GROUP_LABEL);
            return null;
        }

        protected void onPostExecute(String result) {
            if (IceActivity.this.huawei_ice_group_id >= 0) {
                IceActivity.this.mGroupUri = Uri.parse(Groups.CONTENT_URI + "/" + IceActivity.this.huawei_ice_group_id);
                if (!isCancelled()) {
                    IceActivity.this.startGroupMetadataLoader();
                }
            }
        }
    }

    class IceGroupMemberAdapter extends BaseAdapter {
        Context mContext;

        public IceGroupMemberAdapter(Context context) {
            this.mContext = context;
        }

        public int getCount() {
            return IceActivity.this.mMemberListCursor != null ? IceActivity.this.mMemberListCursor.getCount() : 0;
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = ((Activity) this.mContext).getLayoutInflater().inflate(R.layout.ice_contact_member_list_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.member_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (IceActivity.this.mMemberListCursor.moveToPosition(position)) {
                holder.name.setText(IceActivity.this.mMemberListCursor.getString(3));
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView name;

        ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.ice_emergency_contact_title);
        }
        if (HwCustCommonConstants.ICE_DIALER_MODE_ACTION.equals(getIntent().getAction())) {
            this.isDialerMode = true;
        }
        setContentView(R.layout.ice_main_screen);
        updateUI();
        long id = -1;
        if (savedInstanceState != null) {
            id = savedInstanceState.getLong("GroupId", -1);
        }
        if (id >= 0) {
            this.huawei_ice_group_id = id;
            this.mGroupUri = Uri.parse(Groups.CONTENT_URI + "/" + this.huawei_ice_group_id);
            startGroupMetadataLoader();
            return;
        }
        this.createNewGroupTask = new CreateNewGroupTask(this);
        this.createNewGroupTask.execute(new Void[0]);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("GroupId", this.huawei_ice_group_id);
        super.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        if (this.createNewGroupTask != null) {
            this.createNewGroupTask.cancel(true);
        }
        super.onDestroy();
    }

    private void updateUI() {
        this.iceListview = (ListView) findViewById(R.id.ice_added_list);
        this.tvMyEmergencyContactsHeader = (TextView) findViewById(R.id.tv_my_emergency_contacts_header);
        LinearLayout tvEmergencyContact911 = (LinearLayout) findViewById(R.id.tv_emergency_contact_911);
        LinearLayout tvMyEmergencyInfo = (LinearLayout) findViewById(R.id.tv_my_emergency_info);
        this.iceListview.setOnItemLongClickListener(this.itemListLongClickListener);
        this.iceListview.setOnItemClickListener(this.onListItemClickListener);
        tvEmergencyContact911.setOnClickListener(this);
        tvMyEmergencyInfo.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_emergency_contact_911:
                CommonUtilMethods.dialNumber(v.getContext(), Uri.fromParts("tel", HwCustCommonConstants.ICE_DIAL_911, null), "", true, false);
                return;
            case R.id.tv_my_emergency_info:
                Intent intent = new Intent(v.getContext(), IceMyInfoActivity.class);
                if (this.isDialerMode) {
                    intent.setAction(HwCustCommonConstants.ICE_DIALER_MODE_ACTION);
                }
                startActivity(intent);
                return;
            default:
                return;
        }
    }

    private void startGroupMetadataLoader() {
        if (this.mGroupUri != null) {
            getLoaderManager().restartLoader(0, new Bundle(), this.mGroupMetadataLoaderListener);
        }
    }

    private Uri getAndUpdateLookupKey(ContactTileAdapter$ContactEntry contactEntry) {
        if (contactEntry == null) {
            return null;
        }
        Uri uri = contactEntry.lookupKey;
        if (uri == null && contactEntry.mLookupKeyStr != null && contactEntry.mId > 0) {
            uri = ContentUris.withAppendedId(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, contactEntry.mLookupKeyStr), contactEntry.mId);
        }
        return uri;
    }

    private void startGroupMembersLoader() {
        getLoaderManager().restartLoader(1, new Bundle(), this.mGroupMemberListLoaderListener);
    }

    public void createNewGroup(Context context, AccountWithDataSet account, String label) {
        ContentValues values = new ContentValues();
        values.put("account_type", account.type);
        values.put("account_name", account.name);
        values.put("data_set", account.dataSet);
        values.put("title", label);
        values.put("group_visible", Integer.valueOf(2));
        ContentResolver resolver = getContentResolver();
        if (!isGroupNameExisted(account.type, account.name, account.dataSet, label)) {
            Uri groupUri = resolver.insert(Groups.CONTENT_URI, values);
            if (groupUri == null) {
                Log.e("IceActivity", "Couldn't create group with label " + label);
                return;
            }
            this.huawei_ice_group_id = (long) ((int) ContentUris.parseId(groupUri));
            if (LogConfig.HWDBG) {
                Log.d("IceActivity", "huawei_ice_group is created id=" + this.huawei_ice_group_id);
            }
        }
    }

    private boolean isGroupNameExisted(String accountType, String accountName, String dataSet, String label) {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        int totalCount = 0;
        Cursor cursor = null;
        try {
            selection.append("(account_name=? AND account_type=?");
            selectionArgs.add(accountName);
            selectionArgs.add(accountType);
            if (dataSet != null) {
                selection.append(" AND data_set=?");
                selectionArgs.add(dataSet);
            } else {
                selection.append(" AND data_set IS NULL");
            }
            if (label != null) {
                selection.append(" AND title=?");
                selectionArgs.add(label);
            } else {
                selection.append(" AND title IS NULL");
            }
            selection.append(" AND ").append("deleted").append(" = 0");
            selection.append(")");
            cursor = getContentResolver().query(Groups.CONTENT_URI, new String[]{"_id"}, selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), null);
            if (cursor != null) {
                totalCount = cursor.getCount();
                if (totalCount > 0) {
                    if (cursor.moveToFirst()) {
                        this.huawei_ice_group_id = (long) cursor.getInt(cursor.getColumnIndex("_id"));
                    }
                    if (LogConfig.HWDBG) {
                        Log.d("IceActivity", "huawei_ice_group already exists id=" + this.huawei_ice_group_id);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            if (CommonConstants.LOG_INFO) {
                Log.i("IceActivity", "IllegalArgumentException found in isGroupNameExisted");
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (totalCount > 0) {
            return true;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.ice_menu, menu);
        MenuItem menuItemAdd = menu.findItem(R.id.menu_add_member);
        MenuItem menuItemRemove = menu.findItem(R.id.menu_remove_member);
        if (this.mMemberListCursor == null || this.mMemberListCursor.getCount() <= 0) {
            this.tvMyEmergencyContactsHeader.setVisibility(8);
        } else {
            this.tvMyEmergencyContactsHeader.setVisibility(0);
            menuItemRemove.setVisible(true);
        }
        if (this.isDialerMode) {
            menuItemRemove.setVisible(false);
            menuItemAdd.setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_member:
                Intent lIntent = new Intent("android.intent.action.HAP_ADD_GROUP_MEMBERS");
                lIntent.putExtra("extra_group_id", this.huawei_ice_group_id);
                lIntent.putExtra("extra_account_name", HwCustCommonConstants.ICE_ACCOUNT_NAME);
                lIntent.putExtra("extra_account_type", HwCustCommonConstants.ICE_ACCOUNT_TYPE);
                lIntent.putExtra("extra_account_data_set", this.mDataset);
                startActivity(lIntent);
                break;
            case R.id.menu_remove_member:
                Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, this.huawei_ice_group_id);
                Intent removeIntent = new Intent("android.intent.action.HAP_REMOVE_GROUP_MEMBERS");
                removeIntent.putExtra("extra_group_uri", uri);
                startActivity(removeIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private ContactTileAdapter$ContactEntry createContactEntryFromCursor(Cursor cursor, int position) {
        if (cursor == null || cursor.isClosed() || cursor.getCount() <= position) {
            return null;
        }
        cursor.moveToPosition(position);
        long id = cursor.getLong(0);
        String photoUri = cursor.getString(1);
        String lookupKey = cursor.getString(2);
        ContactTileAdapter$ContactEntry contact = new ContactTileAdapter$ContactEntry();
        String name = cursor.getString(3);
        if (name == null) {
            name = getString(R.string.missing_name);
        }
        contact.name = name;
        contact.status = cursor.getString(5);
        contact.photoUri = photoUri != null ? Uri.parse(photoUri) : null;
        contact.mId = id;
        contact.mLookupKeyStr = lookupKey;
        contact.getAndUpdateLookupKey();
        contact.photoId = cursor.getLong(cursor.getColumnIndex("photo_id"));
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            contact.isPrivate = CommonUtilMethods.isPrivateContact(cursor);
        }
        Drawable icon = null;
        int presence = 0;
        if (!cursor.isNull(4)) {
            presence = cursor.getInt(4);
            icon = ContactPresenceIconUtil.getPresenceIcon(this, presence);
        }
        contact.presenceIcon = icon;
        String statusMessage = null;
        if (!cursor.isNull(5)) {
            statusMessage = cursor.getString(5);
        }
        if (statusMessage == null && presence != 0) {
            statusMessage = ContactStatusUtil.getStatusString(this, presence);
        }
        contact.status = statusMessage;
        return contact;
    }
}
