package com.android.contacts.hap.list;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.util.ContactDisplayUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;

public class FrequentContactSelectAdapter extends CursorAdapter {
    private static int CHINA_PHONE_NUMBER_LENGTH = 11;
    static final String[] PROJECTION_DATA = new String[]{"contact_id", "_id", "display_name", "display_name_alt", "sort_key", "photo_id", "photo_thumb_uri", "lookup", "data1", "data2", "data3", "mimetype", "is_primary", "company", "is_super_primary"};
    private ContactMultiSelectionActivity localActivityRef = ((ContactMultiSelectionActivity) this.mContext);
    private Context mContext;
    private ListView mListView;
    private boolean mOldSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();

    public FrequentContactSelectAdapter(Context context, Cursor c, boolean autoRequery, ListView listView) {
        super(context, c, autoRequery);
        this.mContext = context;
        this.mListView = listView;
    }

    public void bindView(View view, Context contact, Cursor cursor) {
        int position = cursor.getPosition();
        if (position == 0) {
            PLog.d(0, "FrequentContactSelectAdapter bindeView begin");
        }
        ContactListItemView itemView = (ContactListItemView) view;
        bindName(itemView, cursor);
        bindPhoto(itemView, cursor);
        bindCheckBox(itemView);
        bindSnippet(itemView, cursor);
        itemView.setCompany(null);
        this.mListView.setItemChecked(cursor.getPosition(), this.localActivityRef.mSelectedDataUris.contains(getCurrentCursorDataUri(cursor)));
        if (position == 0) {
            PLog.d(20, "FrequentContactSelectAdapter bindeView end");
        }
    }

    public final void upateSimpleDisplayMode() {
        boolean newSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
        if (this.mOldSimpleDisplayMode != newSimpleDisplayMode) {
            this.mOldSimpleDisplayMode = newSimpleDisplayMode;
            notifyDataSetChanged();
        }
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setDividerVisible(false);
        view.setUnknownNameText(context.getText(R.string.missing_name));
        view.setActivatedStateSupported(true);
        return view;
    }

    private void bindName(ContactListItemView view, Cursor cursor) {
        if (TextUtils.isEmpty(getName(cursor))) {
            view.showDisplayName(cursor, 8, -1);
        } else {
            view.showDisplayName(cursor, 2, -1);
        }
    }

    private CharSequence getName(Cursor cursor) {
        return cursor.getString(2);
    }

    private void bindPhoto(ContactListItemView view, Cursor cursor) {
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId = cursor.getLong(5);
        long contactId = cursor.getLong(0);
        DefaultImageRequest defaultImageRequest = null;
        if (photoId <= 0) {
            if (contactId <= 0) {
                contactId = (long) cursor.getPosition();
            }
            defaultImageRequest = new DefaultImageRequest(cursor.getString(2), String.valueOf(contactId), true);
        }
        ContactPhotoManager.getInstance(this.mContext).loadThumbnail(view.getPhotoView(photoId), photoId, false, defaultImageRequest, contactId);
    }

    protected void bindCheckBox(ContactListItemView view) {
        view.showCheckBox();
    }

    protected void bindSnippet(ContactListItemView view, Cursor cursor) {
        if ("vnd.android.cursor.item/himessage".equalsIgnoreCase(cursor.getString(11)) || "vnd.android.cursor.item/rcs".equalsIgnoreCase(cursor.getString(11))) {
            bindSnippetForMessagePlusOrRcse(view, cursor);
            return;
        }
        view.setAccountIcons(null);
        String primaryData = cursor.getString(8);
        String mimetype = cursor.getString(11);
        String typeLabel = "";
        String lookupkey = cursor.getString(7);
        boolean isEmptyName = TextUtils.isEmpty(getName(cursor));
        boolean isCallLog = TextUtils.isEmpty(lookupkey);
        if (cursor.getInt(12) == 1 && cursor.getInt(14) == 1) {
            typeLabel = view.getResources().getString(R.string.contacts_default);
        }
        if ("vnd.android.cursor.item/email_v2".equals(mimetype)) {
            view.setSnippet(typeLabel, "‪" + primaryData + "‬");
        } else {
            String lGeocode = null;
            if (isCallLog) {
                lGeocode = cursor.getString(13);
            }
            if (TextUtils.isEmpty(lGeocode)) {
                lGeocode = NumberLocationLoader.getAndUpdateGeoNumLocation(this.mContext, ContactsUtils.removeDashesAndBlanks(primaryData));
                if (TextUtils.isEmpty(lGeocode) && isEmptyName) {
                    lGeocode = this.mContext.getResources().getString(R.string.numberLocationUnknownLocation2);
                }
            }
            if (isEmptyName) {
                view.setSnippet("‪" + lGeocode + "‬");
            } else {
                if (EmuiFeatureManager.isChinaArea()) {
                    primaryData = ContactsUtils.getChinaFormatNumber(primaryData);
                }
                view.setSnippet(typeLabel, "‪" + primaryData + "‬" + " ‪" + lGeocode + "‬");
            }
        }
    }

    private void bindSnippetForMessagePlusOrRcse(ContactListItemView view, Cursor cursor) {
        view.setSnippet(cursor.getString(8));
        Bitmap lBitmap = null;
        if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
            if ("vnd.android.cursor.item/himessage".equalsIgnoreCase(cursor.getString(11))) {
                lBitmap = AccountsDataManager.getInstance(this.mContext).getAccountIcon("com.huawei.himessage");
            } else if ("vnd.android.cursor.item/rcs".equalsIgnoreCase(cursor.getString(11))) {
                lBitmap = AccountsDataManager.getInstance(this.mContext).getAccountIcon("com.huawei.rcse");
            }
            if (lBitmap != null) {
                view.setAccountIcons(new Bitmap[]{lBitmap});
            }
        }
    }

    public Uri getSelectedDataUri(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }
        return getCurrentCursorDataUri(cursor);
    }

    public String getSelectedData(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }
        return cursor.getString(8);
    }

    public long getSelectedContactId(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || !cursor.moveToPosition(position)) {
            return -1;
        }
        return cursor.getLong(0);
    }

    Uri getCurrentCursorDataUri(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Uri dataUri;
        Long dataId = Long.valueOf(cursor.getLong(1));
        if (cursor.getString(7) != null) {
            dataUri = Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(dataId));
        } else {
            dataUri = Uri.withAppendedPath(Calls.CONTENT_URI, String.valueOf(dataId));
        }
        return dataUri;
    }

    public long getSelectedDataId(int aPosition) {
        Cursor mCursor = getCursor();
        if (mCursor == null || !mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        return mCursor.getLong(1);
    }

    public int getDataType(int aPosition) {
        return getDataTypeByNum(aPosition);
    }

    public int getDataTypeByNum(int aPosition) {
        Cursor mCursor = getCursor();
        if (mCursor == null || !mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        String mimetype = mCursor.getString(11);
        if (!EmuiFeatureManager.isChinaArea() || !"vnd.android.cursor.item/phone_v2".equals(mimetype)) {
            return mCursor.getInt(9);
        }
        String number = ContactsUtils.removeDashesAndBlanks(mCursor.getString(8));
        if (number.length() < CHINA_PHONE_NUMBER_LENGTH || !number.matches("^((\\+86)|(86)|(0086))?(1)\\d{10}$")) {
            return -1;
        }
        return 2;
    }

    public int getDataPrimary(int aPosition) {
        Cursor mCursor = getCursor();
        if (mCursor == null || !mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        return mCursor.getInt(12);
    }
}
