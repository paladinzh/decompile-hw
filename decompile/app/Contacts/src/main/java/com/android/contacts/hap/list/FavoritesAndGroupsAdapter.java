package com.android.contacts.hap.list;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.ContactsUtils;
import com.android.contacts.group.GroupListItem;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.hap.rcs.list.RcsFavoritesAndGroupsAdapter;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.SelectedDataCache;
import com.android.contacts.list.ChildListItemView;
import com.android.contacts.list.GroupListItemView;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.android.contacts.widget.ExpandableAutoScrollListView;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.common.collect.Maps;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FavoritesAndGroupsAdapter extends BaseExpandableListAdapter {
    private static final String[] CHILD_COLUMNS = new String[]{"_id", "display_name", "data1", "photo_id", "contact_id", "company", "is_primary", "mimetype", "is_super_primary", "raw_contact_id", "data2", "data2", "data3"};
    private static final String[] CHILD_COLUMNS_PRIVATE;
    private static int MAX_CURSOR_MEMBER_COUNT = VTMCDataCache.MAXSIZE;
    private static int MAX_WINDOW_CURSOR_COUNT = 30;
    private static final Object lockObj = new Object();
    private static Handler mSetChildrenCursorHander = new Handler();
    private AccountTypeManager mAccountTypeManager;
    SparseArray<MyCursorHelper> mChildrenCursorHelpers;
    private Context mContext;
    private int mCurrentGroupPosition = 1;
    private HwCustFavoritesAndGroupsAdapter mCust = null;
    private HashSet<Integer> mExpanded;
    private int mFilterType;
    ConcurrentHashMap<Long, Long> mFrequentDatadMap = new ConcurrentHashMap();
    MyCursorHelper mGroupCursorHelper;
    private int mGroupLayout;
    HashMap<Long, ArrayList<Long>> mGroupToChildMap = Maps.newHashMap();
    private Handler mHandler;
    private boolean mHasFavourites;
    private LayoutInflater mInflater;
    private boolean mIsFrequentMapLoaded = false;
    private ExpandableAutoScrollListView mListView;
    private boolean mOldSimpleDisplayMode;
    private ContactPhotoManager mPhotoManager;
    private QueryHandler mQueryHandler;
    private RcsFavoritesAndGroupsAdapter mRcsCust = null;
    private boolean mReCreate;
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    SelectedDataCache mSelectedCache;
    private CharSequence mUnknownNameText;
    private int mWindowCursorCount = 0;

    public interface CursorDataChangeListener {
        void onCursorDataChanged();
    }

    public static class GroupListItemViewCache {
        public final View accountHeader;
        public final TextView accountName;
        public final TextView accountType;
        public final View divider;
        private final TextView groupCount;
        private final ImageView groupIndicatorImage;
        public final TextView groupTitle;
        private Uri mUri;

        public GroupListItemViewCache(View view) {
            this.accountType = (TextView) view.findViewById(R.id.account_type);
            this.accountName = (TextView) view.findViewById(R.id.account_name);
            this.groupTitle = (TextView) view.findViewById(R.id.label);
            this.accountHeader = view.findViewById(R.id.group_list_header);
            this.divider = view.findViewById(R.id.divider);
            this.groupCount = (TextView) view.findViewById(R.id.group_count);
            this.groupIndicatorImage = (ImageView) view.findViewById(R.id.expandableIcon);
        }

        public void setUri(Uri uri) {
            this.mUri = uri;
        }
    }

    class MyCursorHelper {
        private MyContentObserver mContentObserver;
        private Cursor mCursor;
        private MyDataSetObserver mDataSetObserver;
        private boolean mDataValid;
        private boolean mRequery = true;
        private int mRowIDColumn;

        private class MyContentObserver extends ContentObserver {
            public MyContentObserver() {
                super(FavoritesAndGroupsAdapter.this.mHandler);
            }

            public boolean deliverSelfNotifications() {
                return true;
            }

            public void onChange(boolean selfChange) {
                if (MyCursorHelper.this.mRequery && MyCursorHelper.this.mCursor != null) {
                    if (HwLog.HWDBG) {
                        HwLog.v("Cursor", "Auto requerying " + MyCursorHelper.this.mCursor + " due to update");
                    }
                    MyCursorHelper.this.mDataValid = MyCursorHelper.this.mCursor.requery();
                }
            }
        }

        private class MyDataSetObserver extends DataSetObserver {
            private MyDataSetObserver() {
            }

            public void onChanged() {
                MyCursorHelper.this.mDataValid = true;
                FavoritesAndGroupsAdapter.this.notifyDataSetChanged();
            }

            public void onInvalidated() {
                MyCursorHelper.this.mDataValid = false;
                FavoritesAndGroupsAdapter.this.notifyDataSetInvalidated();
            }
        }

        MyCursorHelper(Cursor cursor) {
            boolean cursorPresent = cursor != null;
            this.mCursor = cursor;
            this.mDataValid = cursorPresent;
            this.mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
            this.mContentObserver = new MyContentObserver();
            this.mDataSetObserver = new MyDataSetObserver();
            if (cursorPresent) {
                cursor.registerContentObserver(this.mContentObserver);
                cursor.registerDataSetObserver(this.mDataSetObserver);
            }
        }

        public void setAutoRequery(boolean requery) {
            this.mRequery = requery;
        }

        Cursor getCursor() {
            return this.mCursor;
        }

        int getCount() {
            if (!this.mDataValid || this.mCursor == null) {
                return 0;
            }
            return this.mCursor.getCount();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        long getId(int position) {
            if (this.mDataValid && this.mCursor != null && this.mCursor.moveToPosition(position)) {
                return this.mCursor.getLong(this.mRowIDColumn);
            }
            return 0;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        long getKeyId(int position) {
            if (this.mDataValid && this.mCursor != null && this.mCursor.moveToPosition(position)) {
                return this.mCursor.getLong(this.mRowIDColumn);
            }
            return -1;
        }

        Cursor moveTo(int position) {
            if (this.mDataValid && this.mCursor != null && this.mCursor.moveToPosition(position)) {
                return this.mCursor;
            }
            HwLog.i("TAG", "move to position : mDataValid = " + this.mDataValid + "; mCursor != null : " + (this.mCursor != null));
            return null;
        }

        void changeCursor(Cursor cursor, boolean releaseCursors) {
            if (cursor != this.mCursor) {
                deactivate();
                this.mCursor = cursor;
                if (cursor != null) {
                    cursor.registerContentObserver(this.mContentObserver);
                    cursor.registerDataSetObserver(this.mDataSetObserver);
                    this.mRowIDColumn = cursor.getColumnIndex("_id");
                    this.mDataValid = true;
                    FavoritesAndGroupsAdapter.this.notifyDataSetChanged(releaseCursors);
                } else {
                    this.mRowIDColumn = -1;
                    this.mDataValid = false;
                    FavoritesAndGroupsAdapter.this.notifyDataSetInvalidated();
                }
            }
        }

        void deactivate() {
            if (this.mCursor != null) {
                this.mCursor.unregisterContentObserver(this.mContentObserver);
                this.mCursor.unregisterDataSetObserver(this.mDataSetObserver);
                this.mCursor.close();
                this.mCursor = null;
            }
        }

        boolean isValid() {
            return this.mDataValid && this.mCursor != null;
        }
    }

    private static final class QueryHandler extends AsyncQueryHandler {
        private FavoritesAndGroupsAdapter mAdapter;
        private CursorDataChangeListener mCursorDataListener;

        public QueryHandler(Context context, FavoritesAndGroupsAdapter adapter) {
            super(context.getContentResolver());
            this.mAdapter = adapter;
        }

        protected void onQueryComplete(int token, final Object cookie, final Cursor cursor) {
            switch (token) {
                case 0:
                    ContactsThreadPool.getInstance().execute(new Runnable() {
                        public void run() {
                            final Cursor mycursor = QueryHandler.this.mAdapter.toMatrixCursor(cursor);
                            final int groupPosition = ((Integer) cookie).intValue();
                            QueryHandler.this.mAdapter.loadFrequentMap();
                            final ArrayList<Long> numbers = QueryHandler.this.mAdapter.pickNumber(mycursor);
                            FavoritesAndGroupsAdapter.mSetChildrenCursorHander.post(new Runnable() {
                                public void run() {
                                    QueryHandler.this.mAdapter.setChildrenCursor(groupPosition, mycursor);
                                    QueryHandler.this.mAdapter.setGroupToChildMap(groupPosition, numbers);
                                    if (QueryHandler.this.mCursorDataListener != null) {
                                        QueryHandler.this.mCursorDataListener.onCursorDataChanged();
                                    }
                                }
                            });
                        }
                    });
                    return;
                default:
                    return;
            }
        }

        public void setListener(CursorDataChangeListener aCursorDataChangeListener) {
            this.mCursorDataListener = aCursorDataChangeListener;
        }
    }

    static {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            CHILD_COLUMNS_PRIVATE = new String[(CHILD_COLUMNS.length + 1)];
            System.arraycopy(CHILD_COLUMNS, 0, CHILD_COLUMNS_PRIVATE, 0, CHILD_COLUMNS.length);
            CHILD_COLUMNS_PRIVATE[CHILD_COLUMNS.length] = "is_private";
        } else {
            CHILD_COLUMNS_PRIVATE = CHILD_COLUMNS;
        }
    }

    public FavoritesAndGroupsAdapter(Context context, int groupLayout, ListView lv, HashSet<Integer> expanded, boolean recreate) {
        init(context, groupLayout);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustFavoritesAndGroupsAdapter) HwCustUtils.createObj(HwCustFavoritesAndGroupsAdapter.class, new Object[]{context});
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsFavoritesAndGroupsAdapter(context);
            this.mRcsCust.initService(context);
        }
        this.mExpanded = expanded;
        this.mReCreate = recreate;
        this.mListView = (ExpandableAutoScrollListView) lv;
    }

    private void init(Context context, int groupLayout) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mGroupCursorHelper = new MyCursorHelper(null);
        this.mGroupCursorHelper.setAutoRequery(false);
        this.mChildrenCursorHelpers = new SparseArray();
        this.mGroupLayout = groupLayout;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mAccountTypeManager = AccountTypeManager.getInstance(this.mContext);
        this.mUnknownNameText = context.getText(R.string.missing_name);
        this.mPhotoManager = ContactPhotoManager.getInstance(this.mContext);
        this.mQueryHandler = new QueryHandler(this.mContext, this);
        this.mOldSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
    }

    public void setFilterType(int filter) {
        this.mFilterType = filter;
    }

    public View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        ChildListItemView view = new ChildListItemView(context, null);
        view.setUnknownNameText(this.mUnknownNameText);
        view.setActivatedStateSupported(true);
        return view;
    }

    public View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return this.mInflater.inflate(this.mGroupLayout, parent, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized MyCursorHelper getChildrenCursorHelper(int groupPosition, boolean requestCursor) {
        MyCursorHelper cursorHelper = (MyCursorHelper) this.mChildrenCursorHelpers.get(groupPosition);
        if (cursorHelper == null) {
            if (this.mGroupCursorHelper.moveTo(groupPosition) == null) {
                return null;
            }
            cursorHelper = new MyCursorHelper(getChildrenCursor(groupPosition));
            this.mChildrenCursorHelpers.put(groupPosition, cursorHelper);
        }
    }

    private boolean checkConvertCursor(Cursor c) {
        if (c == null || getGroupCount() <= MAX_WINDOW_CURSOR_COUNT) {
            return false;
        }
        if (c.getCount() <= MAX_CURSOR_MEMBER_COUNT || this.mWindowCursorCount >= MAX_WINDOW_CURSOR_COUNT) {
            return true;
        }
        this.mWindowCursorCount++;
        return false;
    }

    private Cursor toMatrixCursor(Cursor c) {
        Object newCursor;
        if (!checkConvertCursor(c)) {
            return c;
        }
        Object o = null;
        boolean hasException = false;
        Cursor cursor = null;
        try {
            String[] columns = c.getColumnNames();
            MatrixCursor newCursor2 = new MatrixCursor(columns, c.getCount());
            while (c.moveToNext()) {
                try {
                    RowBuilder b = newCursor2.newRow();
                    int i = 0;
                    int length = columns.length;
                    while (true) {
                        if (i < length) {
                            String col = columns[i];
                            int index = c.getColumnIndex(col);
                            switch (c.getType(c.getColumnIndex(col))) {
                                case 1:
                                    o = Integer.valueOf(c.getInt(index));
                                    break;
                                case 3:
                                    o = c.getString(index);
                                    break;
                            }
                            b.add(o);
                            i++;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    newCursor = newCursor2;
                } catch (CursorIndexOutOfBoundsException e2) {
                    newCursor = newCursor2;
                }
            }
            cursor = newCursor2;
        } catch (IllegalArgumentException e3) {
            HwLog.w("FavoritesAndGroupsAdapter", "IllegalArgumentException");
            hasException = true;
            if (hasException) {
                c.close();
                return cursor;
            }
            if (cursor != null) {
                cursor.close();
            }
            c.moveToPosition(-1);
            return c;
        } catch (CursorIndexOutOfBoundsException e4) {
            HwLog.w("FavoritesAndGroupsAdapter", "CursorIndexOutOfBoundsException");
            hasException = true;
            if (hasException) {
                if (cursor != null) {
                    cursor.close();
                }
                c.moveToPosition(-1);
                return c;
            }
            c.close();
            return cursor;
        }
        if (hasException) {
            if (cursor != null) {
                cursor.close();
            }
            c.moveToPosition(-1);
            return c;
        }
        c.close();
        return cursor;
    }

    private int getDataTypeByNum(Cursor cursor) {
        if (cursor == null) {
            return -1;
        }
        String mimetype = cursor.getString(7);
        if (!EmuiFeatureManager.isChinaArea() || !"vnd.android.cursor.item/phone_v2".equals(mimetype)) {
            return cursor.getInt(10);
        }
        String number = ContactsUtils.removeDashesAndBlanks(cursor.getString(2));
        if (number.length() < 11 || !number.matches("^((\\+86)|(86)|(0086))?(1)\\d{10}$")) {
            return -1;
        }
        return 2;
    }

    void setGroupToChildMap(int groupPosition, ArrayList<Long> numbers) {
        synchronized (lockObj) {
            if (!(this.mGroupToChildMap == null || numbers == null || numbers.size() <= 0 || getChildrenCursorHelper(groupPosition, false) == null)) {
                long groupId = getGroupKeyId(groupPosition);
                if (groupId != -1) {
                    this.mGroupToChildMap.put(Long.valueOf(groupId), numbers);
                }
            }
        }
    }

    ArrayList<Long> getGroupToChildMapValue(long groupId) {
        ArrayList<Long> result;
        synchronized (lockObj) {
            result = (ArrayList) this.mGroupToChildMap.get(Long.valueOf(groupId));
        }
        return result;
    }

    private void loadFrequentMap() {
        if (!this.mIsFrequentMapLoaded) {
            Uri fraquentUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/data_phone_frequent");
            Cursor cursor = null;
            this.mFrequentDatadMap.clear();
            try {
                cursor = this.mContext.getContentResolver().query(fraquentUri, null, null, null, null);
                while (cursor.moveToNext()) {
                    long data_id = cursor.getLong(cursor.getColumnIndex("data_id"));
                    long times_used = cursor.getLong(cursor.getColumnIndex("times_used"));
                    if (this.mFrequentDatadMap.containsKey(Long.valueOf(data_id))) {
                        this.mFrequentDatadMap.put(Long.valueOf(data_id), Long.valueOf(((Long) this.mFrequentDatadMap.get(Long.valueOf(data_id))).longValue() + times_used));
                    } else {
                        this.mFrequentDatadMap.put(Long.valueOf(data_id), Long.valueOf(times_used));
                    }
                }
                this.mIsFrequentMapLoaded = true;
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                HwLog.w("FavoritesAndGroupsAdapter", "loadfrequentMap query exception : " + e);
                if (this.mCurrentGroupPosition != getGroupCount()) {
                    this.mCurrentGroupPosition++;
                }
                this.mCurrentGroupPosition = 1;
                this.mIsFrequentMapLoaded = false;
                return;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (this.mCurrentGroupPosition != getGroupCount()) {
            this.mCurrentGroupPosition = 1;
            this.mIsFrequentMapLoaded = false;
            return;
        }
        this.mCurrentGroupPosition++;
    }

    private ArrayList<Long> pickNumber(Cursor cursor) {
        ArrayList<Long> list = new ArrayList();
        boolean foundSuperPrimaryPhone = false;
        boolean foundSuperPrimaryEmail = false;
        long phoneId = -1;
        long emailId = -1;
        int limit = this.mSelectedCache.getMaxLimit();
        int selectCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            long rawId = cursor.getLong(9);
            long lDefaultTimes = -1;
            ConcurrentHashMap<Long, Long> frequentDataMap = this.mFrequentDatadMap;
            boolean lIsSelectAllWithDef = ((ContactMultiSelectionActivity) this.mContext).checkIsSelectAllWithDef();
            do {
                long dataId = cursor.getLong(0);
                if (lIsSelectAllWithDef) {
                    if (cursor.getLong(9) != rawId) {
                        if (phoneId != -1) {
                            list.add(Long.valueOf(phoneId));
                            selectCount++;
                            phoneId = -1;
                        }
                        if (emailId != -1) {
                            list.add(Long.valueOf(emailId));
                            selectCount++;
                            emailId = -1;
                        }
                        foundSuperPrimaryEmail = false;
                        foundSuperPrimaryPhone = false;
                        lDefaultTimes = -1;
                    }
                    rawId = cursor.getLong(9);
                    int isSuperPrimary = cursor.getInt(8);
                    String mimetype = cursor.getString(7);
                    int type = getDataTypeByNum(cursor);
                    long currentTimes = frequentDataMap.containsKey(Long.valueOf(dataId)) ? ((Long) frequentDataMap.get(Long.valueOf(dataId))).longValue() : 0;
                    if (foundSuperPrimaryPhone || !mimetype.equals("vnd.android.cursor.item/phone_v2")) {
                        if (!foundSuperPrimaryEmail && mimetype.equals("vnd.android.cursor.item/email_v2")) {
                            if (isSuperPrimary == 1) {
                                foundSuperPrimaryEmail = true;
                                emailId = dataId;
                            } else if (emailId == -1) {
                                emailId = dataId;
                            }
                        }
                    } else if (isSuperPrimary == 1) {
                        foundSuperPrimaryPhone = true;
                        phoneId = dataId;
                    } else if (type == 2) {
                        if (currentTimes > lDefaultTimes) {
                            phoneId = dataId;
                            lDefaultTimes = currentTimes;
                        }
                    } else if (phoneId == -1) {
                        phoneId = dataId;
                    }
                } else {
                    list.add(Long.valueOf(dataId));
                }
                if (selectCount >= limit) {
                    break;
                }
            } while (cursor.moveToNext());
            if (lIsSelectAllWithDef) {
                if (phoneId != -1) {
                    list.add(Long.valueOf(phoneId));
                }
                if (emailId != -1) {
                    list.add(Long.valueOf(emailId));
                }
            }
        }
        return list;
    }

    private Cursor getChildrenCursor(int groupPosition) {
        String selection;
        StringBuffer sb;
        QueryHandler queryHandler;
        Integer valueOf;
        String[] strArr;
        String[] strArr2 = null;
        Uri uri = Data.CONTENT_URI;
        switch (this.mFilterType) {
            case 211:
                if (this.mHasFavourites && groupPosition == 0) {
                    selection = "starred = 1 AND mimetype = 'vnd.android.cursor.item/phone_v2'";
                } else {
                    selection = "mimetype = 'vnd.android.cursor.item/phone_v2' AND  raw_contact_id IN ( SELECT raw_contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/group_membership' AND data1 = ?)";
                }
                sb = new StringBuffer();
                sb.append("contact_id").append(", replace(replace(").append("data1").append(", '-', ''), ' ', '')");
                break;
            case 212:
                if (this.mHasFavourites && groupPosition == 0) {
                    selection = "starred = 1 AND mimetype = 'vnd.android.cursor.item/email_v2'";
                } else {
                    selection = "mimetype = 'vnd.android.cursor.item/email_v2' AND  raw_contact_id IN ( SELECT raw_contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/group_membership' AND data1 = ?)";
                }
                sb = new StringBuffer();
                sb.append("contact_id").append(',').append("data1");
                break;
            default:
                if (this.mRcsCust != null) {
                    selection = this.mRcsCust.getChildCursorSelection(this.mFilterType, this.mHasFavourites, groupPosition);
                    if (selection != null) {
                        sb = this.mRcsCust.appendSbForRcs();
                        break;
                    }
                }
                if (this.mHasFavourites && groupPosition == 0) {
                    if (this.mCust == null || !this.mCust.getEnableEmailContactInMms()) {
                        selection = "starred = 1 AND mimetype = 'vnd.android.cursor.item/phone_v2'";
                    } else {
                        selection = "starred = 1 AND mimetype IN ( 'vnd.android.cursor.item/phone_v2','vnd.android.cursor.item/email_v2')";
                    }
                } else if (this.mCust == null || !this.mCust.getEnableEmailContactInMms()) {
                    selection = "mimetype = 'vnd.android.cursor.item/phone_v2' AND  raw_contact_id IN ( SELECT raw_contact_id FROM view_data WHERE mimetype = 'vnd.android.cursor.item/group_membership' AND data1 = ?)";
                } else {
                    selection = this.mCust.getChildSelection();
                }
                sb = new StringBuffer();
                sb.append("contact_id").append(", replace(replace(").append("data1").append(", '-', ''), ' ', '')");
                break;
        }
        if (sb != null) {
            uri = uri.buildUpon().appendQueryParameter("group_by", sb.toString()).build();
        }
        if (this.mHasFavourites || groupPosition < 0) {
            if (this.mHasFavourites && groupPosition > 0) {
            }
            queryHandler = this.mQueryHandler;
            valueOf = Integer.valueOf(groupPosition);
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                strArr = CHILD_COLUMNS;
            } else {
                strArr = CHILD_COLUMNS_PRIVATE;
            }
            queryHandler.startQuery(0, valueOf, uri, strArr, selection, strArr2, "sort_key");
            return null;
        }
        strArr2 = new String[]{String.valueOf(getGroupId(groupPosition))};
        queryHandler = this.mQueryHandler;
        valueOf = Integer.valueOf(groupPosition);
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            strArr = CHILD_COLUMNS;
        } else {
            strArr = CHILD_COLUMNS_PRIVATE;
        }
        queryHandler.startQuery(0, valueOf, uri, strArr, selection, strArr2, "sort_key");
        return null;
    }

    public void setGroupCursor(Cursor cursor) {
        this.mGroupCursorHelper.changeCursor(cursor, false);
    }

    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
        MyCursorHelper childrenCursorHelper = getChildrenCursorHelper(groupPosition, false);
        if (childrenCursorHelper != null) {
            childrenCursorHelper.changeCursor(childrenCursor, false);
        }
    }

    public Cursor getChild(int groupPosition, int childPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        if (helper != null) {
            return helper.moveTo(childPosition);
        }
        return null;
    }

    public long getChildId(int groupPosition, int childPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        return helper != null ? helper.getId(childPosition) : 0;
    }

    public int getChildrenCount(int groupPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        if (!this.mGroupCursorHelper.isValid() || helper == null) {
            return 0;
        }
        return helper.getCount();
    }

    public Cursor getGroup(int groupPosition) {
        return this.mGroupCursorHelper.moveTo(groupPosition);
    }

    public int getGroupCount() {
        return this.mGroupCursorHelper.getCount();
    }

    public long getGroupId(int groupPosition) {
        return this.mGroupCursorHelper.getId(groupPosition);
    }

    public long getGroupKeyId(int groupPosition) {
        return this.mGroupCursorHelper.getKeyId(groupPosition);
    }

    public GroupListItem getGroupItem(int position) {
        if (!this.mGroupCursorHelper.isValid() || this.mGroupCursorHelper.moveTo(position) == null) {
            return null;
        }
        Cursor grpCursor = this.mGroupCursorHelper.getCursor();
        String accountName = grpCursor.getString(0);
        String accountType = grpCursor.getString(1);
        String dataSet = grpCursor.getString(2);
        long groupId = grpCursor.getLong(3);
        String title = grpCursor.getString(4);
        int memberCount = grpCursor.getInt(5);
        boolean isGroupReadOnly = grpCursor.getInt(6) == 1;
        boolean isPrivateGroup = this.mContext.getString(R.string.private_group_sync1).equals(grpCursor.getString(10));
        int previousIndex = position - 1;
        boolean isFirstGroupInAccount = true;
        if (TextUtils.isEmpty(title)) {
            title = HwCustPreloadContacts.EMPTY_STRING;
            HwLog.w("FavoritesAndGroupsAdapter", "Dirty Data!!!");
        } else {
            title = CamcardGroup.replaceTitle(CommonUtilMethods.parseGroupDisplayName(accountType, title, this.mContext, grpCursor.getString(9), grpCursor.getInt(7), grpCursor.getString(8)), this.mContext);
        }
        if (previousIndex >= 0 && grpCursor.moveToPosition(previousIndex)) {
            String previousGroupAccountName = grpCursor.getString(0);
            String previousGroupAccountType = grpCursor.getString(1);
            String previousGroupDataSet = grpCursor.getString(2);
            if (accountName.equals(previousGroupAccountName) && accountType.equals(previousGroupAccountType) && Objects.equal(dataSet, previousGroupDataSet)) {
                isFirstGroupInAccount = false;
            }
        }
        return new GroupListItem(accountName, accountType, dataSet, groupId, title, isFirstGroupInAccount, memberCount, isGroupReadOnly, isPrivateGroup);
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (this.mReCreate && isGroupExpanded(groupPosition)) {
            this.mListView.expandGroup(groupPosition);
        }
        Cursor cursor = this.mGroupCursorHelper.moveTo(groupPosition);
        if (cursor == null) {
            HwLog.i("FavoritesAndGroupsAdapter", "this should only be called when the cursor is valid");
            return newGroupView(this.mContext, cursor, isExpanded, parent);
        }
        View result;
        GroupListItem entry = getGroupItem(groupPosition);
        if (convertView == null || convertView.getTag() == null) {
            result = newGroupView(this.mContext, cursor, isExpanded, parent);
            GroupListItemViewCache groupListItemViewCache = new GroupListItemViewCache(result);
            result.setTag(groupListItemViewCache);
        } else {
            result = convertView;
            GroupListItemViewCache viewCache = (GroupListItemViewCache) convertView.getTag();
        }
        final ArrayList<Long> dataIds = getGroupToChildMapValue(this.mGroupCursorHelper.getKeyId(groupPosition));
        GroupListItemView groupListItemView = null;
        if (result instanceof FrameLayout) {
            View lContent = result.findViewById(R.id.list_item_content);
            if (lContent instanceof GroupListItemView) {
                groupListItemView = (GroupListItemView) lContent;
            }
        } else if (result instanceof GroupListItemView) {
            groupListItemView = (GroupListItemView) result;
        }
        if (groupListItemView == null) {
            return result;
        }
        if (dataIds == null || dataIds.isEmpty()) {
            groupListItemView.hideCheckBox();
        } else {
            groupListItemView.showCheckBox();
            final GroupListItemView groupItem = groupListItemView;
            final int i = groupPosition;
            groupListItemView.getCheckBox().setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    groupItem.toggle();
                    if (FavoritesAndGroupsAdapter.this.mSelectedCache != null && dataIds != null) {
                        int maxLimit = FavoritesAndGroupsAdapter.this.mSelectedCache.getMaxLimit();
                        if (groupItem.isChecked()) {
                            for (Long longValue : dataIds) {
                                long dataId = longValue.longValue();
                                if (maxLimit > 0 && FavoritesAndGroupsAdapter.this.mSelectedCache.getSelectedDataUri().size() >= maxLimit) {
                                    FavoritesAndGroupsAdapter.this.showToast();
                                    break;
                                }
                                FavoritesAndGroupsAdapter.this.mSelectedCache.setSelectedUri(ContentUris.withAppendedId(Data.CONTENT_URI, dataId));
                            }
                        } else {
                            MyCursorHelper childHelper = FavoritesAndGroupsAdapter.this.getChildrenCursorHelper(i, true);
                            if (childHelper != null) {
                                for (int i = 0; i < childHelper.getCount(); i++) {
                                    FavoritesAndGroupsAdapter.this.mSelectedCache.removeSelectedUri(ContentUris.withAppendedId(Data.CONTENT_URI, childHelper.getId(i)));
                                }
                            }
                        }
                    }
                    FavoritesAndGroupsAdapter.this.notifyDataSetChanged(false);
                }
            });
        }
        if (this.mSelectedCache == null || dataIds == null || dataIds.size() <= 0) {
            groupListItemView.setCheckedState(false);
        } else {
            boolean groupSelection;
            boolean hasMemberSelected = false;
            Set<Uri> selectedUris = this.mSelectedCache.getSelectedDataUri();
            int groupMembersSize = dataIds.size();
            int selectedUriSize = selectedUris.size();
            if (selectedUriSize != 0) {
                if (selectedUriSize >= groupMembersSize) {
                    groupSelection = true;
                    for (Long longValue : dataIds) {
                        if (selectedUris.contains(ContentUris.withAppendedId(Data.CONTENT_URI, longValue.longValue()))) {
                            hasMemberSelected = true;
                        } else {
                            groupSelection = false;
                        }
                        if (!groupSelection && hasMemberSelected) {
                            break;
                        }
                    }
                } else {
                    groupSelection = false;
                    for (Long longValue2 : dataIds) {
                        if (selectedUris.contains(ContentUris.withAppendedId(Data.CONTENT_URI, longValue2.longValue()))) {
                            hasMemberSelected = true;
                            break;
                        }
                    }
                }
            }
            groupSelection = false;
            hasMemberSelected = false;
            int maxLimit = this.mSelectedCache.getMaxLimit();
            boolean isReachMaxLimit = maxLimit > 0 && selectedUriSize >= maxLimit;
            if (groupSelection || (isReachMaxLimit && hasMemberSelected)) {
                groupListItemView.setCheckedState(true);
            } else {
                groupListItemView.setCheckedState(false);
            }
        }
        bindGroupIndicator(viewCache, isExpanded);
        if (entry == null || !entry.isFirstGroupInAccount() || (this.mHasFavourites && (!this.mHasFavourites || groupPosition == 0))) {
            viewCache.accountHeader.setVisibility(8);
            viewCache.divider.setVisibility(8);
        } else {
            bindHeaderView(entry, viewCache);
            viewCache.accountHeader.setVisibility(0);
            viewCache.divider.setVisibility(8);
        }
        if (entry != null) {
            viewCache.setUri(getGroupUriFromId(entry.getGroupId()));
            viewCache.groupTitle.setText(entry.getTitle());
        }
        if (getChildrenCount(groupPosition) > 0) {
            viewCache.groupCount.setText(String.format("%d", new Object[]{Integer.valueOf(getChildrenCount(groupPosition))}));
        } else {
            resetGroupCount(viewCache.groupCount, groupListItemView);
        }
        return result;
    }

    private void resetGroupCount(TextView textView, GroupListItemView groupListItemView) {
        textView.setText("");
        groupListItemView.hideCheckBox();
    }

    public Uri getGroupUriFromId(long groupId) {
        return ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
    }

    private void bindHeaderView(GroupListItem entry, GroupListItemViewCache viewCache) {
        AccountType accountType = this.mAccountTypeManager.getAccountType(entry.getAccountType(), entry.getDataSet());
        viewCache.accountType.setText(this.mContext.getString(R.string.groups_in, new Object[]{accountType.getDisplayLabel(this.mContext).toString()}));
        viewCache.accountName.setText(entry.getAccountName());
    }

    private void bindGroupIndicator(GroupListItemViewCache viewCache, boolean isExpanded) {
        viewCache.groupIndicatorImage.setImageResource(isExpanded ? R.drawable.contact_expander_close_emui : R.drawable.contact_expander_open_emui);
        viewCache.groupIndicatorImage.setContentDescription(this.mContext.getString(isExpanded ? R.string.content_description_fold_button : R.string.content_description_expand_button));
        viewCache.groupIndicatorImage.setVisibility(0);
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        if (cursorHelper == null) {
            HwLog.i("FavoritesAndGroupsAdapter", "this should only be called when cursorhelper is valid");
            return newChildView(this.mContext, null, false, parent);
        }
        Cursor cursor = cursorHelper.moveTo(childPosition);
        if (cursor == null) {
            HwLog.i("FavoritesAndGroupsAdapter", "this should only be called when the cursor is valid");
            return newChildView(this.mContext, null, false, parent);
        }
        ChildListItemView childView;
        if (convertView != null) {
            childView = (ChildListItemView) convertView;
        } else {
            childView = (ChildListItemView) newChildView(this.mContext, cursor, false, parent);
        }
        boolean isFirstEntry = true;
        long currentContactId = cursor.getLong(4);
        if (cursor.moveToPrevious() && !cursor.isBeforeFirst() && currentContactId == cursor.getLong(4)) {
            isFirstEntry = false;
        }
        cursor.moveToPosition(childPosition);
        childView.setGroupPosition(groupPosition);
        childView.setHighlightedPrefix(null);
        childView.showCheckBox();
        if (isFirstEntry) {
            bindName(childView, cursor);
            bindPhoto(childView, cursor);
        } else {
            childView.hideDisplayName();
            if (ContactDisplayUtils.isSimpleDisplayMode()) {
                childView.removePhotoView();
            } else {
                childView.removePhotoView(true, false);
            }
        }
        bindSnippet(childView, cursor);
        childView.setCompany(null);
        Uri dataUri = ContentUris.withAppendedId(Data.CONTENT_URI, cursorHelper.getId(childPosition));
        if (this.mSelectedCache == null || !this.mSelectedCache.getSelectedDataUri().contains(dataUri)) {
            childView.setCheckedState(false);
        } else {
            childView.setCheckedState(true);
        }
        return childView;
    }

    public final void upateSimpleDisplayMode() {
        boolean newSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
        if (this.mOldSimpleDisplayMode != newSimpleDisplayMode) {
            this.mOldSimpleDisplayMode = newSimpleDisplayMode;
            notifyDataSetChanged();
        }
    }

    public void performChildClick(ChildListItemView childView, int groupPosition, int childPosition) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        if (cursorHelper != null) {
            Uri dataUri = ContentUris.withAppendedId(Data.CONTENT_URI, cursorHelper.getId(childPosition));
            if (this.mSelectedCache != null) {
                if (childView.isChecked()) {
                    this.mSelectedCache.removeSelectedUri(dataUri);
                } else {
                    int maxLimit = this.mSelectedCache.getMaxLimit();
                    if (maxLimit <= 0 || this.mSelectedCache.getSelectedDataUri().size() < maxLimit) {
                        this.mSelectedCache.setSelectedUri(dataUri);
                    } else {
                        showToast();
                        return;
                    }
                }
                childView.toggle();
                notifyDataSetChanged(false);
            }
        }
    }

    private void bindName(ChildListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 1, 1);
    }

    private void bindPhoto(ChildListItemView view, Cursor cursor) {
        int i = 4;
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId = cursor.getLong(3);
        boolean lIsPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            lIsPrivateContact = CommonUtilMethods.isPrivateContact(cursor);
        }
        DefaultImageRequest request = null;
        if (photoId <= 0) {
            this.mRequest.displayName = cursor.getString(1);
            this.mRequest.identifier = cursor.getString(4);
            this.mRequest.isCircular = true;
            request = this.mRequest;
        }
        ContactPhotoManager contactPhotoManager = this.mPhotoManager;
        ImageView photoView = view.getPhotoView(photoId);
        if (!lIsPrivateContact) {
            i = 0;
        }
        contactPhotoManager.loadThumbnail(photoView, photoId, false, request, i);
    }

    private void bindSnippet(ChildListItemView aView, Cursor aCursor) {
        aView.setAccountIcons(null);
        String primaryData = aCursor.getString(2);
        int type = aCursor.getInt(11);
        String label = aCursor.getString(12);
        String typeLabel = "";
        String defaultString = "";
        if (aCursor.getInt(6) == 1 && aCursor.getInt(8) == 1) {
            defaultString = aView.getResources().getString(R.string.contacts_default);
        }
        if (this.mFilterType != 212) {
            if (EmuiFeatureManager.isChinaArea()) {
                primaryData = ContactsUtils.getChinaFormatNumber(primaryData);
            }
            typeLabel = Phone.getTypeLabel(this.mContext.getResources(), type, label).toString();
        } else if (type == 0 && TextUtils.isEmpty(label)) {
            typeLabel = this.mContext.getResources().getString(Email.getTypeLabelResource(3));
        } else {
            typeLabel = Email.getTypeLabel(this.mContext.getResources(), type, label).toString();
        }
        if (!TextUtils.isEmpty(defaultString)) {
            typeLabel = typeLabel + HwCustPreloadContacts.EMPTY_STRING + defaultString;
        }
        aView.setSnippet(typeLabel, primaryData);
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    public synchronized void releaseCursorHelpers() {
        for (int pos = this.mChildrenCursorHelpers.size() - 1; pos >= 0; pos--) {
            ((MyCursorHelper) this.mChildrenCursorHelpers.valueAt(pos)).deactivate();
        }
        this.mChildrenCursorHelpers.clear();
    }

    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    public void notifyDataSetChanged(boolean releaseCursors) {
        if (releaseCursors) {
            releaseCursorHelpers();
        }
        super.notifyDataSetChanged();
    }

    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    public void onGroupCollapsed(int groupPosition) {
        this.mExpanded.remove(Integer.valueOf(groupPosition));
    }

    public void onGroupExpanded(int groupPosition) {
        this.mExpanded.add(Integer.valueOf(groupPosition));
    }

    public boolean isGroupExpanded(int groupPosition) {
        return this.mExpanded.contains(Integer.valueOf(groupPosition));
    }

    public void setSelectedCache(SelectedDataCache cache) {
        this.mSelectedCache = cache;
    }

    public void clearQueryHandler() {
        this.mQueryHandler.cancelOperation(0);
    }

    private void showToast() {
        Toast.makeText(this.mContext, this.mContext.getString(R.string.max_contact_selected_Toast, new Object[]{Integer.valueOf(this.mSelectedCache.getMaxLimit())}), 0).show();
    }

    public void setListener(CursorDataChangeListener aCursorDataListener) {
        if (this.mQueryHandler != null) {
            this.mQueryHandler.setListener(aCursorDataListener);
        }
    }

    public void setHasFavourites(boolean aHasFavourites) {
        this.mHasFavourites = aHasFavourites;
    }
}
