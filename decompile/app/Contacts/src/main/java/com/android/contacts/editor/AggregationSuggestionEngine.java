package com.android.contacts.editor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.MVersionUpgradeUtils.AggregationSuggestions;
import com.android.contacts.util.MVersionUpgradeUtils.AggregationSuggestions.Builder;
import com.google.common.collect.Lists;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AggregationSuggestionEngine extends HandlerThread {
    private long mContactId;
    private ContentObserver mContentObserver;
    private final Context mContext;
    private HwCustAggregationSuggestionEngine mCust = null;
    private Handler mHandler;
    private List mList;
    private Listener mListener;
    private Handler mMainHandler;
    private long[] mSuggestedContactIds = new long[0];
    private Uri mSuggestionsUri;

    private static final class DataQuery {
        public static final String[] COLUMNS = new String[]{"_id", "contact_id", "lookup", "photo_id", "display_name", "raw_contact_id", "mimetype", "data1", "is_super_primary", "data15", "account_type", "account_name", "data_set"};

        private DataQuery() {
        }
    }

    public interface Listener {
        void onAggregationSuggestionChange();
    }

    public static final class RawContact {
        public String accountName;
        public String accountType;
        public String dataSet;
        public long rawContactId;

        public String toString() {
            return "ID: " + this.rawContactId + " account: " + this.accountType + "/" + this.accountName + " dataSet: " + this.dataSet;
        }
    }

    public static final class Suggestion {
        public long contactId;
        public String emailAddress;
        public String lookupKey;
        public String name;
        public String nickname;
        public String phoneNumber;
        public byte[] photo;
        public List<RawContact> rawContacts;

        public String toString() {
            return "ID: " + this.contactId + " rawContacts: " + this.rawContacts + " name: " + this.name + " phone: " + this.phoneNumber + " email: " + this.emailAddress + " nickname: " + this.nickname + (this.photo != null ? " [has photo]" : "");
        }
    }

    private final class SuggestionContentObserver extends ContentObserver {
        private SuggestionContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            AggregationSuggestionEngine.this.scheduleSuggestionLookup();
        }
    }

    public AggregationSuggestionEngine(Context context) {
        super("AggregationSuggestions", 10);
        this.mContext = context.getApplicationContext();
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustAggregationSuggestionEngine) HwCustUtils.createObj(HwCustAggregationSuggestionEngine.class, new Object[0]);
        }
        this.mMainHandler = new Handler() {
            public void handleMessage(Message msg) {
                AggregationSuggestionEngine.this.deliverNotification((List) msg.obj);
            }
        };
    }

    protected Handler getHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler(getLooper()) {
                public void handleMessage(Message msg) {
                    AggregationSuggestionEngine.this.handleMessage(msg);
                }
            };
        }
        return this.mHandler;
    }

    public void setContactId(long contactId) {
        if (contactId != this.mContactId) {
            this.mContactId = contactId;
            reset();
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public boolean quit() {
        if (this.mList != null) {
            this.mList.clear();
        }
        if (this.mContentObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
            this.mContentObserver = null;
        }
        return super.quit();
    }

    public void reset() {
        Handler handler = getHandler();
        handler.removeMessages(1);
        handler.sendEmptyMessage(0);
    }

    public void onNameChange(ValuesDelta values) {
        this.mSuggestionsUri = buildAggregationSuggestionUri(values);
        if (this.mSuggestionsUri != null) {
            if (this.mContentObserver == null) {
                this.mContentObserver = new SuggestionContentObserver(getHandler());
                this.mContext.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContentObserver);
            }
        } else if (this.mContentObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
            this.mContentObserver = null;
        }
        scheduleSuggestionLookup();
    }

    protected void scheduleSuggestionLookup() {
        Handler handler = getHandler();
        handler.removeMessages(1);
        if (this.mSuggestionsUri == null) {
            HwLog.e("AggregationSuggestionEngine", "mSuggestionsUri is null");
        } else {
            handler.sendMessageDelayed(handler.obtainMessage(1, this.mSuggestionsUri), 400);
        }
    }

    private Uri buildAggregationSuggestionUri(ValuesDelta values) {
        StringBuilder nameSb = new StringBuilder();
        appendValue(nameSb, values, "data4");
        appendValue(nameSb, values, "data2");
        appendValue(nameSb, values, "data5");
        appendValue(nameSb, values, "data3");
        appendValue(nameSb, values, "data6");
        if (nameSb.length() == 0) {
            appendValue(nameSb, values, "data1");
        }
        StringBuilder phoneticNameSb = new StringBuilder();
        appendValue(phoneticNameSb, values, "data9");
        appendValue(phoneticNameSb, values, "data8");
        appendValue(phoneticNameSb, values, "data7");
        if (nameSb.length() == 0 && phoneticNameSb.length() == 0) {
            return null;
        }
        Builder builder = AggregationSuggestions.builder().setLimit(3).setContactId(this.mContactId);
        if (nameSb.length() != 0) {
            builder.addNameParameter(nameSb.toString());
        }
        if (phoneticNameSb.length() != 0) {
            builder.addNameParameter(phoneticNameSb.toString());
        }
        return builder.build();
    }

    private void appendValue(StringBuilder sb, ValuesDelta values, String column) {
        String value = values.getAsString(column);
        if (!TextUtils.isEmpty(value)) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(value);
        }
    }

    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                this.mSuggestedContactIds = new long[0];
                return;
            case 1:
                loadAggregationSuggestions((Uri) msg.obj);
                return;
            default:
                return;
        }
    }

    private void loadAggregationSuggestions(Uri uri) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[]{"_id"}, null, null, null);
        Cursor cursor2 = null;
        try {
            if (getHandler().hasMessages(1) || cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
            } else if (updateSuggestedContactIds(cursor)) {
                StringBuilder sb = new StringBuilder("mimetype IN ('vnd.android.cursor.item/phone_v2','vnd.android.cursor.item/email_v2','vnd.android.cursor.item/name','vnd.android.cursor.item/nickname','vnd.android.cursor.item/photo') AND contact_id IN (");
                String[] selectionArgs = new String[1];
                int count = this.mSuggestedContactIds.length;
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    sb.append(this.mSuggestedContactIds[i]);
                }
                sb.append(')');
                sb.append(" AND account_type NOT IN ('com.android.huawei.sim','com.android.huawei.secondsim')");
                if (this.mCust != null) {
                    this.mCust.getDisplayNameForQuery(uri, sb, selectionArgs);
                }
                if (TextUtils.isEmpty(selectionArgs[0])) {
                    selectionArgs = null;
                }
                sb.toString();
                cursor2 = contentResolver.query(Data.CONTENT_URI, DataQuery.COLUMNS, sb.toString(), selectionArgs, "contact_id");
                this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(2, createSuggestionsList(cursor2)));
            } else {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        }
    }

    private boolean updateSuggestedContactIds(Cursor cursor) {
        int count = cursor.getCount();
        if (count <= 0) {
            return false;
        }
        boolean changed;
        if (count != this.mSuggestedContactIds.length) {
            changed = true;
        } else {
            changed = false;
        }
        if (!changed) {
            while (cursor.moveToNext()) {
                if (Arrays.binarySearch(this.mSuggestedContactIds, cursor.getLong(0)) < 0) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            this.mSuggestedContactIds = new long[count];
            cursor.moveToPosition(-1);
            for (int i = 0; i < count; i++) {
                if (cursor.moveToNext()) {
                    this.mSuggestedContactIds[i] = cursor.getLong(0);
                }
            }
            Arrays.sort(this.mSuggestedContactIds);
        }
        return changed;
    }

    protected void deliverNotification(List<Suggestion> list) {
        this.mList = list;
        if (this.mList != null && this.mList.size() != 0 && this.mListener != null) {
            this.mListener.onAggregationSuggestionChange();
        }
    }

    public int getSuggestedContactCount() {
        return this.mList != null ? this.mList.size() : 0;
    }

    private List<Suggestion> createSuggestionsList(Cursor cursor) {
        ArrayList<Suggestion> list = Lists.newArrayList();
        if (cursor != null) {
            Suggestion suggestion = null;
            long currentContactId = -1;
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(1);
                if (contactId != currentContactId) {
                    suggestion = new Suggestion();
                    suggestion.contactId = contactId;
                    suggestion.name = cursor.getString(4);
                    suggestion.lookupKey = cursor.getString(2);
                    suggestion.rawContacts = Lists.newArrayList();
                    list.add(suggestion);
                    currentContactId = contactId;
                }
                long rawContactId = cursor.getLong(5);
                if (!(suggestion == null || containsRawContact(suggestion, rawContactId))) {
                    RawContact rawContact = new RawContact();
                    rawContact.rawContactId = rawContactId;
                    rawContact.accountName = cursor.getString(11);
                    rawContact.accountType = cursor.getString(10);
                    rawContact.dataSet = cursor.getString(12);
                    suggestion.rawContacts.add(rawContact);
                }
                String mimetype = cursor.getString(6);
                String data;
                int superprimary;
                if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                    data = cursor.getString(7);
                    superprimary = cursor.getInt(8);
                    if (!(suggestion == null || TextUtils.isEmpty(data))) {
                        if (superprimary != 0 || suggestion.phoneNumber == null) {
                            suggestion.phoneNumber = data;
                        }
                    }
                } else if ("vnd.android.cursor.item/email_v2".equals(mimetype)) {
                    data = cursor.getString(7);
                    superprimary = cursor.getInt(8);
                    if (!(suggestion == null || TextUtils.isEmpty(data))) {
                        if (superprimary != 0 || suggestion.emailAddress == null) {
                            suggestion.emailAddress = data;
                        }
                    }
                } else if ("vnd.android.cursor.item/nickname".equals(mimetype)) {
                    data = cursor.getString(7);
                    if (!(suggestion == null || TextUtils.isEmpty(data))) {
                        suggestion.nickname = data;
                    }
                } else if ("vnd.android.cursor.item/photo".equals(mimetype)) {
                    long dataId = cursor.getLong(0);
                    long photoId = cursor.getLong(3);
                    if (!(suggestion == null || dataId != photoId || cursor.isNull(9))) {
                        suggestion.photo = cursor.getBlob(9);
                    }
                }
            }
        }
        return list;
    }

    public List<Suggestion> getSuggestions() {
        return this.mList;
    }

    public boolean containsRawContact(Suggestion suggestion, long rawContactId) {
        if (!(suggestion == null || suggestion.rawContacts == null)) {
            int count = suggestion.rawContacts.size();
            for (int i = 0; i < count; i++) {
                if (((RawContact) suggestion.rawContacts.get(i)).rawContactId == rawContactId) {
                    return true;
                }
            }
        }
        return false;
    }
}
