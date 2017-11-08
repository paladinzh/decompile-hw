package com.android.contacts.list;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactStatusUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.ContactDisplayUtils;
import com.google.android.gms.R;
import java.util.HashMap;

public class GroupDetailBrowserAdapter extends BaseAdapter {
    private static final /* synthetic */ int[] -com-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues = null;
    private static final String TAG = GroupDetailBrowserAdapter.class.getSimpleName();
    private final int EMPTY_ADD_FAVORITE_TILE = 1;
    private HashMap<Long, ContactTileAdapter$ContactEntry> mCachedContactEntryMap;
    private Cursor mContactCursor = null;
    private Context mContext;
    private ContactTileAdapter$DisplayType mDisplayType;
    private int mDividerPosition;
    private int mIdIndex;
    private boolean mIsFling;
    private int mLookupIndex;
    private int mNameIndex;
    private int mNumFrequents;
    private boolean mOldSimpleDisplayMode;
    private int mPhoneNumberIndex;
    private int mPhoneNumberLabelIndex;
    private int mPhoneNumberTypeIndex;
    private ContactPhotoManager mPhotoManager;
    private int mPhotoUriIndex;
    private int mPresenceIndex;
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private Resources mResources;
    private boolean mShowAddFavoriteTile = false;
    private int mStarredIndex;
    private int mStatusIndex;
    private CharSequence mUnknowNameText;

    private static /* synthetic */ int[] -getcom-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues() {
        if (-com-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues != null) {
            return -com-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues;
        }
        int[] iArr = new int[ContactTileAdapter$DisplayType.values().length];
        try {
            iArr[ContactTileAdapter$DisplayType.FREQUENT_ONLY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ContactTileAdapter$DisplayType.GROUP_MEMBERS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ContactTileAdapter$DisplayType.SMART_GROUP_ONLY.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ContactTileAdapter$DisplayType.STARRED_ONLY.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ContactTileAdapter$DisplayType.STREQUENT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ContactTileAdapter$DisplayType.STREQUENT_PHONE_ONLY.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues = iArr;
        return iArr;
    }

    public GroupDetailBrowserAdapter(Context context, ContactTileAdapter$DisplayType displayType) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mDisplayType = displayType;
        this.mNumFrequents = 0;
        bindColumnIndices();
        this.mCachedContactEntryMap = new HashMap();
        this.mUnknowNameText = context.getText(R.string.missing_name);
        this.mOldSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
    }

    public void setPhotoLoader(ContactPhotoManager photoLoader) {
        this.mPhotoManager = photoLoader;
    }

    private void bindColumnIndices() {
        if (this.mDisplayType == ContactTileAdapter$DisplayType.GROUP_MEMBERS) {
            this.mIdIndex = 0;
            this.mLookupIndex = 2;
            this.mPhotoUriIndex = 1;
            this.mNameIndex = 3;
            this.mPresenceIndex = 4;
            this.mStatusIndex = 5;
        } else if (this.mDisplayType == ContactTileAdapter$DisplayType.SMART_GROUP_ONLY) {
            this.mIdIndex = 0;
            this.mLookupIndex = 3;
            this.mPhotoUriIndex = 2;
            this.mNameIndex = 1;
            this.mPresenceIndex = 4;
            this.mStatusIndex = 5;
        } else {
            this.mIdIndex = 0;
            this.mLookupIndex = 4;
            this.mPhotoUriIndex = 3;
            this.mNameIndex = 1;
            this.mStarredIndex = 2;
            this.mPresenceIndex = 5;
            this.mStatusIndex = 6;
            this.mPhoneNumberIndex = 5;
            this.mPhoneNumberTypeIndex = 6;
            this.mPhoneNumberLabelIndex = 7;
        }
    }

    public void setContactCursor(Cursor cursor) {
        if (cursor != null) {
            this.mCachedContactEntryMap.clear();
            this.mContactCursor = cursor;
            this.mDividerPosition = getDividerPosition(cursor);
            switch (-getcom-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues()[this.mDisplayType.ordinal()]) {
                case 1:
                    this.mNumFrequents = this.mContactCursor.getCount();
                    break;
                case 2:
                case 3:
                case 4:
                    this.mNumFrequents = 0;
                    break;
                case 5:
                case 6:
                    this.mNumFrequents = this.mContactCursor.getCount() - this.mDividerPosition;
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized DisplayType " + this.mDisplayType);
            }
            notifyDataSetChanged();
        }
    }

    private int getDividerPosition(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            throw new IllegalStateException("Unable to access cursor");
        }
        switch (-getcom-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues()[this.mDisplayType.ordinal()]) {
            case 1:
                return 0;
            case 2:
            case 3:
            case 4:
                return -1;
            case 5:
            case 6:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getInt(this.mStarredIndex) == 0) {
                        return cursor.getPosition();
                    }
                }
                return cursor.getCount();
            default:
                throw new IllegalStateException("Unrecognized DisplayType " + this.mDisplayType);
        }
    }

    private ContactTileAdapter$ContactEntry createContactEntryFromCursor(Cursor cursor, int position) {
        if (cursor == null || cursor.isClosed() || cursor.getCount() <= position) {
            return null;
        }
        cursor.moveToPosition(position);
        long id = cursor.getLong(this.mIdIndex);
        Long key = Long.valueOf(id);
        if (this.mCachedContactEntryMap.containsKey(key)) {
            return (ContactTileAdapter$ContactEntry) this.mCachedContactEntryMap.get(key);
        }
        String photoUri = cursor.getString(this.mPhotoUriIndex);
        String lookupKey = cursor.getString(this.mLookupIndex);
        ContactTileAdapter$ContactEntry contact = new ContactTileAdapter$ContactEntry();
        String name = cursor.getString(this.mNameIndex);
        if (name == null) {
            name = this.mResources.getString(R.string.missing_name);
        }
        contact.name = name;
        contact.status = cursor.getString(this.mStatusIndex);
        contact.photoUri = photoUri != null ? Uri.parse(photoUri) : null;
        contact.mId = id;
        contact.mLookupKeyStr = lookupKey;
        if (!this.mIsFling) {
            contact.getAndUpdateLookupKey();
        }
        contact.photoId = cursor.getLong(cursor.getColumnIndex("photo_id"));
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            contact.isPrivate = CommonUtilMethods.isPrivateContact(cursor);
        }
        if (this.mDisplayType == ContactTileAdapter$DisplayType.STREQUENT_PHONE_ONLY) {
            contact.phoneLabel = (String) Phone.getTypeLabel(this.mResources, cursor.getInt(this.mPhoneNumberTypeIndex), cursor.getString(this.mPhoneNumberLabelIndex));
            contact.phoneNumber = cursor.getString(this.mPhoneNumberIndex);
        } else {
            Drawable icon = null;
            int presence = 0;
            if (!cursor.isNull(this.mPresenceIndex)) {
                presence = cursor.getInt(this.mPresenceIndex);
                icon = ContactPresenceIconUtil.getPresenceIcon(this.mContext, presence);
            }
            contact.presenceIcon = icon;
            String statusMessage = null;
            if (this.mStatusIndex != 0) {
                if (!cursor.isNull(this.mStatusIndex)) {
                    statusMessage = cursor.getString(this.mStatusIndex);
                }
            }
            if (statusMessage == null && presence != 0) {
                statusMessage = ContactStatusUtil.getStatusString(this.mContext, presence);
            }
            contact.status = statusMessage;
        }
        this.mCachedContactEntryMap.put(key, contact);
        return contact;
    }

    public int getCount() {
        int i = 1;
        if (this.mContactCursor == null || this.mContactCursor.isClosed()) {
            return this.mShowAddFavoriteTile ? 1 : 0;
        } else {
            int count;
            switch (-getcom-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues()[this.mDisplayType.ordinal()]) {
                case 1:
                    return this.mContactCursor.getCount();
                case 2:
                case 3:
                case 4:
                    count = this.mContactCursor.getCount();
                    if (!this.mShowAddFavoriteTile) {
                        i = 0;
                    }
                    return i + count;
                case 5:
                case 6:
                    count = this.mDividerPosition;
                    if (!this.mShowAddFavoriteTile) {
                        i = 0;
                    }
                    return (count + i) + (this.mNumFrequents == 0 ? 0 : this.mNumFrequents + 1);
                default:
                    throw new IllegalArgumentException("Unrecognized DisplayType " + this.mDisplayType);
            }
        }
    }

    public ContactTileAdapter$ContactEntry getItem(int position) {
        return createContactEntryFromCursor(this.mContactCursor, position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public boolean areAllItemsEnabled() {
        if (this.mDisplayType == ContactTileAdapter$DisplayType.STREQUENT || this.mDisplayType == ContactTileAdapter$DisplayType.STREQUENT_PHONE_ONLY) {
            return false;
        }
        return true;
    }

    public boolean isEnabled(int position) {
        return position != this.mDividerPosition;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ContactListItemView view;
        if (convertView instanceof ContactListItemView) {
            view = (ContactListItemView) convertView;
        } else {
            view = new ContactListItemView(this.mContext, null);
            view.setUnknownNameText(this.mUnknowNameText);
        }
        if (this.mContactCursor == null || this.mContactCursor.isClosed() || this.mContactCursor.getCount() <= position) {
            return null;
        }
        this.mContactCursor.moveToPosition(position);
        bindPhoto(view, this.mContactCursor);
        bindName(view, this.mContactCursor);
        view.setCompany(null);
        return view;
    }

    public int getViewTypeCount() {
        return 4;
    }

    public int getItemViewType(int position) {
        switch (-getcom-android-contacts-list-ContactTileAdapter$DisplayTypeSwitchesValues()[this.mDisplayType.ordinal()]) {
            case 1:
                return 2;
            case 2:
            case 3:
            case 4:
                return 0;
            case 5:
                if (position < this.mDividerPosition) {
                    return 0;
                }
                return position == this.mDividerPosition ? 1 : 2;
            case 6:
                if (position < this.mDividerPosition) {
                    return 3;
                }
                return position == this.mDividerPosition ? 1 : 2;
            default:
                throw new IllegalStateException("Unrecognized DisplayType " + this.mDisplayType);
        }
    }

    public void setIsListInFlingState(boolean aIsFling) {
        this.mIsFling = aIsFling;
    }

    private void bindPhoto(ContactListItemView view, Cursor cursor) {
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId = cursor.getLong(cursor.getColumnIndex("photo_id"));
        boolean lIsPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            lIsPrivateContact = CommonUtilMethods.isPrivateContact(cursor);
        }
        if (photoId > 0) {
            int i;
            ContactPhotoManager contactPhotoManager = this.mPhotoManager;
            ImageView photoView = view.getPhotoView(photoId);
            if (lIsPrivateContact) {
                i = 6;
            } else {
                i = 2;
            }
            contactPhotoManager.loadThumbnail(photoView, photoId, false, null, i);
        } else {
            int i2;
            this.mRequest.displayName = cursor.getString(this.mNameIndex);
            this.mRequest.identifier = cursor.getString(this.mIdIndex);
            this.mRequest.isCircular = true;
            ContactPhotoManager contactPhotoManager2 = this.mPhotoManager;
            ImageView photoView2 = view.getPhotoView(photoId);
            if (lIsPrivateContact) {
                i2 = 4;
            } else {
                i2 = 0;
            }
            contactPhotoManager2.loadDirectoryPhoto(photoView2, null, false, i2, this.mRequest);
        }
    }

    private void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, this.mNameIndex, 1);
    }

    public final void upateSimpleDisplayMode() {
        boolean newSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
        if (this.mOldSimpleDisplayMode != newSimpleDisplayMode) {
            this.mOldSimpleDisplayMode = newSimpleDisplayMode;
            notifyDataSetChanged();
        }
    }
}
