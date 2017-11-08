package com.android.contacts.model;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.contacts.GroupMetaData;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.contacts.util.StreamItemEntry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;

public class Contact implements Parcelable {
    private boolean isYellowPage;
    private final String mAltDisplayName;
    private Bitmap mBitmap;
    private final String mCustomRingtone;
    private String mDirectoryAccountName;
    private String mDirectoryAccountType;
    private String mDirectoryDisplayName;
    private int mDirectoryExportSupport;
    private final long mDirectoryId;
    private String mDirectoryType;
    private final String mDisplayName;
    private final int mDisplayNameSource;
    private final Exception mException;
    private ImmutableList<GroupMetaData> mGroups;
    private final long mId;
    private ImmutableList<AccountType> mInvitableAccountTypes;
    private final boolean mIsPrivateContact;
    private final boolean mIsUserProfile;
    private final String mLookupKey;
    private final Uri mLookupUri;
    private final long mNameRawContactId;
    private final String mPhoneticName;
    public byte[] mPhotoBinaryData;
    private final long mPhotoId;
    private final String mPhotoUri;
    private final Integer mPresence;
    private ImmutableList<RawContact> mRawContacts;
    private final Uri mRequestedUri;
    private final boolean mSendToVoicemail;
    private String mSortKey;
    private final boolean mStarred;
    private final Status mStatus;
    private ImmutableMap<Long, DataStatus> mStatuses;
    private final Uri mUri;

    private enum Status {
        LOADED,
        ERROR,
        NOT_FOUND
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    private Contact(Uri requestedUri, Status status, Exception exception) {
        this.isYellowPage = false;
        if (status == Status.ERROR && exception == null) {
            throw new IllegalArgumentException("ERROR result must have exception");
        }
        this.mStatus = status;
        this.mException = exception;
        this.mRequestedUri = requestedUri;
        this.mLookupUri = null;
        this.mUri = null;
        this.mDirectoryId = -1;
        this.mLookupKey = null;
        this.mId = -1;
        this.mRawContacts = null;
        this.mStatuses = null;
        this.mNameRawContactId = -1;
        this.mDisplayNameSource = 0;
        this.mPhotoId = -1;
        this.mPhotoUri = null;
        this.mDisplayName = null;
        this.mAltDisplayName = null;
        this.mPhoneticName = null;
        this.mStarred = false;
        this.mPresence = null;
        this.mInvitableAccountTypes = null;
        this.mSendToVoicemail = false;
        this.mCustomRingtone = null;
        this.mIsUserProfile = false;
        this.mIsPrivateContact = false;
    }

    public static Contact forError(Uri requestedUri, Exception exception) {
        return new Contact(requestedUri, Status.ERROR, exception);
    }

    public static Contact forNotFound(Uri requestedUri) {
        return new Contact(requestedUri, Status.NOT_FOUND, null);
    }

    public Contact(Uri requestedUri, Uri uri, Uri lookupUri, long directoryId, String lookupKey, long id, long nameRawContactId, int displayNameSource, long photoId, String photoUri, String displayName, String altDisplayName, String phoneticName, boolean starred, Integer presence, boolean sendToVoicemail, String customRingtone, boolean isUserProfile, boolean isPrivateContact) {
        this.isYellowPage = false;
        this.mStatus = Status.LOADED;
        this.mException = null;
        this.mRequestedUri = requestedUri;
        this.mLookupUri = lookupUri;
        this.mUri = uri;
        this.mDirectoryId = directoryId;
        this.mLookupKey = lookupKey;
        this.mId = id;
        this.mRawContacts = null;
        this.mStatuses = null;
        this.mNameRawContactId = nameRawContactId;
        this.mDisplayNameSource = displayNameSource;
        this.mPhotoId = photoId;
        this.mPhotoUri = photoUri;
        this.mDisplayName = displayName;
        this.mAltDisplayName = altDisplayName;
        this.mPhoneticName = phoneticName;
        this.mStarred = starred;
        this.mPresence = presence;
        this.mInvitableAccountTypes = null;
        this.mSendToVoicemail = sendToVoicemail;
        this.mCustomRingtone = customRingtone;
        this.mIsUserProfile = isUserProfile;
        this.mIsPrivateContact = isPrivateContact;
    }

    public Contact(Uri requestedUri, Uri uri, Uri lookupUri, long directoryId, String lookupKey, long id, long nameRawContactId, int displayNameSource, long photoId, String photoUri, String displayName, String altDisplayName, String phoneticName, boolean starred, Integer presence, boolean sendToVoicemail, String customRingtone, boolean isUserProfile, boolean isPrivateContact, String sortKey) {
        this(requestedUri, uri, lookupUri, directoryId, lookupKey, id, nameRawContactId, displayNameSource, photoId, photoUri, displayName, altDisplayName, phoneticName, starred, presence, sendToVoicemail, customRingtone, isUserProfile, isPrivateContact);
        this.mSortKey = sortKey;
    }

    public Contact(Uri requestedUri, Uri uri, Uri lookupUri, long directoryId, String lookupKey, long id, long nameRawContactId, int displayNameSource, long photoId, String photoUri, String displayName, String altDisplayName, String phoneticName, boolean starred, Integer presence, boolean sendToVoicemail, String customRingtone, boolean isUserProfile) {
        this.isYellowPage = false;
        this.mStatus = Status.LOADED;
        this.mException = null;
        this.mRequestedUri = requestedUri;
        this.mLookupUri = lookupUri;
        this.mUri = uri;
        this.mDirectoryId = directoryId;
        this.mLookupKey = lookupKey;
        this.mId = id;
        this.mRawContacts = null;
        this.mStatuses = null;
        this.mNameRawContactId = nameRawContactId;
        this.mDisplayNameSource = displayNameSource;
        this.mPhotoId = photoId;
        this.mPhotoUri = photoUri;
        this.mDisplayName = displayName;
        this.mAltDisplayName = altDisplayName;
        this.mPhoneticName = phoneticName;
        this.mStarred = starred;
        this.mPresence = presence;
        this.mInvitableAccountTypes = null;
        this.mSendToVoicemail = sendToVoicemail;
        this.mCustomRingtone = customRingtone;
        this.mIsUserProfile = isUserProfile;
        this.mIsPrivateContact = false;
    }

    public Contact(Uri requestedUri, Uri uri, Uri lookupUri, long directoryId, String lookupKey, long id, long nameRawContactId, int displayNameSource, long photoId, String photoUri, String displayName, String altDisplayName, String phoneticName, boolean starred, Integer presence, boolean sendToVoicemail, String customRingtone, boolean isUserProfile, String sortKey) {
        this(requestedUri, uri, lookupUri, directoryId, lookupKey, id, nameRawContactId, displayNameSource, photoId, photoUri, displayName, altDisplayName, phoneticName, starred, presence, sendToVoicemail, customRingtone, isUserProfile);
        this.mSortKey = sortKey;
    }

    public Contact(Uri requestedUri, Contact from) {
        this.isYellowPage = false;
        this.mRequestedUri = requestedUri;
        this.mStatus = from.mStatus;
        this.mException = from.mException;
        this.mLookupUri = from.mLookupUri;
        this.mUri = from.mUri;
        this.mDirectoryId = from.mDirectoryId;
        this.mLookupKey = from.mLookupKey;
        this.mId = from.mId;
        this.mNameRawContactId = from.mNameRawContactId;
        this.mDisplayNameSource = from.mDisplayNameSource;
        this.mPhotoId = from.mPhotoId;
        this.mPhotoUri = from.mPhotoUri;
        this.mDisplayName = from.mDisplayName;
        this.mAltDisplayName = from.mAltDisplayName;
        this.mPhoneticName = from.mPhoneticName;
        this.mStarred = from.mStarred;
        this.mPresence = from.mPresence;
        this.mRawContacts = from.mRawContacts;
        this.mStatuses = from.mStatuses;
        this.mInvitableAccountTypes = from.mInvitableAccountTypes;
        this.mDirectoryDisplayName = from.mDirectoryDisplayName;
        this.mDirectoryType = from.mDirectoryType;
        this.mDirectoryAccountType = from.mDirectoryAccountType;
        this.mDirectoryAccountName = from.mDirectoryAccountName;
        this.mDirectoryExportSupport = from.mDirectoryExportSupport;
        this.mGroups = from.mGroups;
        this.mPhotoBinaryData = from.mPhotoBinaryData;
        this.mSendToVoicemail = from.mSendToVoicemail;
        this.mCustomRingtone = from.mCustomRingtone;
        this.mIsUserProfile = from.mIsUserProfile;
        this.mIsPrivateContact = from.mIsPrivateContact;
    }

    public void setDirectoryMetaData(String displayName, String directoryType, String accountType, String accountName, int exportSupport) {
        this.mDirectoryDisplayName = displayName;
        this.mDirectoryType = directoryType;
        this.mDirectoryAccountType = accountType;
        this.mDirectoryAccountName = accountName;
        this.mDirectoryExportSupport = exportSupport;
    }

    public void setPhotoBinaryData(byte[] photoBinaryData) {
        this.mPhotoBinaryData = photoBinaryData;
    }

    public Uri getLookupUri() {
        return this.mLookupUri;
    }

    public String getLookupKey() {
        return this.mLookupKey;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public Uri getRequestedUri() {
        return this.mRequestedUri;
    }

    public RawContactDeltaList createRawContactDeltaList() {
        if (getRawContacts() == null || getRawContacts().iterator() == null) {
            return null;
        }
        return RawContactDeltaList.fromIterator(getRawContacts().iterator());
    }

    @VisibleForTesting
    public long getId() {
        return this.mId;
    }

    public boolean isError() {
        return this.mStatus == Status.ERROR;
    }

    public boolean isNotFound() {
        return this.mStatus == Status.NOT_FOUND;
    }

    public boolean isLoaded() {
        return this.mStatus == Status.LOADED;
    }

    public long getNameRawContactId() {
        return this.mNameRawContactId;
    }

    public int getDisplayNameSource() {
        return this.mDisplayNameSource;
    }

    public long getPhotoId() {
        return this.mPhotoId;
    }

    public String getPhotoUri() {
        return (this.mPhotoId == -2 || this.mPhotoId == -3) ? null : this.mPhotoUri;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public String getAltDisplayName() {
        return this.mAltDisplayName;
    }

    public String getPhoneticName() {
        return this.mPhoneticName;
    }

    public boolean getStarred() {
        return this.mStarred;
    }

    public ImmutableList<AccountType> getInvitableAccountTypes() {
        return this.mInvitableAccountTypes;
    }

    public ImmutableList<RawContact> getRawContacts() {
        return this.mRawContacts;
    }

    public ArrayList<Long> getRawContactIds() {
        ArrayList<Long> lRawContactIds = new ArrayList();
        for (RawContact lRawContact : this.mRawContacts) {
            lRawContactIds.add(lRawContact.getId());
        }
        return lRawContactIds;
    }

    public ImmutableList<StreamItemEntry> getStreamItems() {
        return ImmutableList.of();
    }

    public ImmutableMap<Long, DataStatus> getStatuses() {
        return this.mStatuses;
    }

    public long getDirectoryId() {
        return this.mDirectoryId;
    }

    public boolean isDirectoryEntry() {
        if (this.mDirectoryId == -1 || this.mDirectoryId == 0 || this.mDirectoryId == 1) {
            return false;
        }
        return true;
    }

    public boolean isWritableContact(Context context) {
        return getFirstWritableRawContactId(context) != -1;
    }

    public long getFirstWritableRawContactId(Context context) {
        if (isDirectoryEntry()) {
            return -1;
        }
        for (RawContact rawContact : getRawContacts()) {
            AccountType accountType = rawContact.getAccountType(context);
            if (accountType != null && accountType.areContactsWritable()) {
                return rawContact.getId().longValue();
            }
        }
        return -1;
    }

    public int getDirectoryExportSupport() {
        return this.mDirectoryExportSupport;
    }

    public String getDirectoryDisplayName() {
        return this.mDirectoryDisplayName;
    }

    public String getDirectoryType() {
        return this.mDirectoryType;
    }

    public String getDirectoryAccountType() {
        return this.mDirectoryAccountType;
    }

    public String getDirectoryAccountName() {
        return this.mDirectoryAccountName;
    }

    public ArrayList<ContentValues> getContentValues() {
        if (this.mRawContacts.size() != 1) {
            throw new IllegalStateException("Cannot extract content values from an aggregated contact");
        }
        RawContact rawContact = (RawContact) this.mRawContacts.get(0);
        ArrayList<ContentValues> result = new ArrayList();
        for (DataItem dataItem : rawContact.getDataItems()) {
            result.add(dataItem.getContentValues());
        }
        if (this.mPhotoId == 0 && this.mPhotoBinaryData != null) {
            ContentValues photo = new ContentValues();
            photo.put("mimetype", "vnd.android.cursor.item/photo");
            photo.put("data15", this.mPhotoBinaryData);
            result.add(photo);
        }
        return result;
    }

    public ArrayList<String> getAllFormattedPhoneNumbers() {
        ArrayList<String> lFormattedPhoneNumbers = new ArrayList();
        for (RawContact lRawContact : this.mRawContacts) {
            for (DataItem lDataItem : lRawContact.getDataItems()) {
                if (lDataItem instanceof PhoneDataItem) {
                    lFormattedPhoneNumbers.add(PhoneNumberFormatter.parsePhoneNumber(((PhoneDataItem) lDataItem).getFormattedPhoneNumber()));
                }
            }
        }
        return lFormattedPhoneNumbers;
    }

    public ImmutableList<GroupMetaData> getGroupMetaData() {
        return this.mGroups;
    }

    public boolean isSendToVoicemail() {
        return this.mSendToVoicemail;
    }

    public String getCustomRingtone() {
        return this.mCustomRingtone;
    }

    public boolean isUserProfile() {
        return this.mIsUserProfile;
    }

    public boolean isYellowPage() {
        return this.isYellowPage;
    }

    public void setYellowPage(boolean isYellowPage) {
        this.isYellowPage = isYellowPage;
    }

    public boolean isSimContact() {
        if (isUserProfile() || this.mRawContacts.size() != 1) {
            return false;
        }
        return CommonUtilMethods.isSimAccount(((RawContact) this.mRawContacts.get(0)).getValues().getAsString("account_type"));
    }

    public String toString() {
        return "{requested=" + this.mRequestedUri + ",lookupkey=" + this.mLookupKey + ",uri=" + this.mUri + ",status=" + this.mStatus + "}";
    }

    public void setRawContacts(ImmutableList<RawContact> rawContacts) {
        this.mRawContacts = rawContacts;
    }

    public void setStatuses(ImmutableMap<Long, DataStatus> statuses) {
        this.mStatuses = statuses;
    }

    void setInvitableAccountTypes(ImmutableList<AccountType> accountTypes) {
        this.mInvitableAccountTypes = accountTypes;
    }

    void setGroupMetaData(ImmutableList<GroupMetaData> groups) {
        this.mGroups = groups;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }
}
