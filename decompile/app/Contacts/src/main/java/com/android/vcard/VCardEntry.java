package com.android.vcard;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.vcard.VCardUtils.PhoneNumberUtilsPort;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCardEntry {
    private static final List<String> sEmptyList = Collections.unmodifiableList(new ArrayList(0));
    private static final Map<String, Integer> sImMap = new HashMap();
    private final Account mAccount;
    private List<AndroidCustomData> mAndroidCustomDataList;
    private AnniversaryData mAnniversary;
    private BirthdayData mBirthday;
    private List<VCardEntry> mChildren;
    private List<EmailData> mEmailList;
    private ExtendDataCommitter mExtendDataCommitter;
    private List<GroupMembershipData> mGroupMembershipList;
    private List<ImData> mImList;
    private final NameData mNameData;
    private List<NicknameData> mNicknameList;
    private List<NoteData> mNoteList;
    private List<OrganizationData> mOrganizationList;
    private List<PhoneData> mPhoneList;
    private List<PhotoData> mPhotoList;
    private List<PostalData> mPostalList;
    private List<SipData> mSipList;
    private List<Pair<String, String>> mUnknownXData;
    private final int mVCardType;
    private List<WebsiteData> mWebsiteList;

    public interface EntryElement {
        void constructInsertOperation(List<ContentProviderOperation> list, int i);

        EntryLabel getEntryLabel();

        boolean isEmpty();
    }

    public static class AndroidCustomData implements EntryElement {
        private final List<String> mDataList;
        private final String mMimeType;

        public AndroidCustomData(String mimeType, List<String> dataList) {
            this.mMimeType = mimeType;
            this.mDataList = dataList;
        }

        public static AndroidCustomData constructAndroidCustomData(List<String> list) {
            String mimeType;
            List list2;
            if (list == null) {
                mimeType = null;
                list2 = null;
            } else if (list.size() < 2) {
                mimeType = (String) list.get(0);
                list2 = null;
            } else {
                int max;
                if (list.size() < 16) {
                    max = list.size();
                } else {
                    max = 16;
                }
                mimeType = (String) list.get(0);
                list2 = list.subList(1, max);
            }
            return new AndroidCustomData(mimeType, list2);
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", this.mMimeType);
            for (int i = 0; i < this.mDataList.size(); i++) {
                String value = (String) this.mDataList.get(i);
                if (!TextUtils.isEmpty(value)) {
                    builder.withValue(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH + (i + 1), value);
                }
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mMimeType) || this.mDataList == null || this.mDataList.size() == 0;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AndroidCustomData)) {
                return false;
            }
            AndroidCustomData data = (AndroidCustomData) obj;
            if (!TextUtils.equals(this.mMimeType, data.mMimeType)) {
                return false;
            }
            if (this.mDataList == null) {
                return data.mDataList == null;
            }
            int size = this.mDataList.size();
            if (size != data.mDataList.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!TextUtils.equals((CharSequence) this.mDataList.get(i), (CharSequence) data.mDataList.get(i))) {
                    return false;
                }
            }
            return true;
        }

        public int hashCode() {
            int hash = this.mMimeType != null ? this.mMimeType.hashCode() : 0;
            if (this.mDataList != null) {
                for (String data : this.mDataList) {
                    hash = (hash * 31) + (data != null ? data.hashCode() : 0);
                }
            }
            return hash;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("android-custom: ").append(this.mMimeType).append(", data: ");
            builder.append(this.mDataList == null ? "null" : Arrays.toString(this.mDataList.toArray()));
            return builder.toString();
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.ANDROID_CUSTOM;
        }
    }

    public static class AnniversaryData implements EntryElement {
        private final String mAnniversary;

        public AnniversaryData(String anniversary) {
            this.mAnniversary = anniversary;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/contact_event");
            builder.withValue("data1", this.mAnniversary);
            builder.withValue("data2", Integer.valueOf(1));
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mAnniversary);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AnniversaryData)) {
                return false;
            }
            return TextUtils.equals(this.mAnniversary, ((AnniversaryData) obj).mAnniversary);
        }

        public int hashCode() {
            return this.mAnniversary != null ? this.mAnniversary.hashCode() : 0;
        }

        public String toString() {
            return "anniversary: " + this.mAnniversary;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.ANNIVERSARY;
        }
    }

    public static class BirthdayData implements EntryElement {
        private final String mBirthday;

        public BirthdayData(String birthday) {
            this.mBirthday = birthday;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/contact_event");
            builder.withValue("data1", this.mBirthday);
            builder.withValue("data2", Integer.valueOf(3));
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mBirthday);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BirthdayData)) {
                return false;
            }
            return TextUtils.equals(this.mBirthday, ((BirthdayData) obj).mBirthday);
        }

        public int hashCode() {
            return this.mBirthday != null ? this.mBirthday.hashCode() : 0;
        }

        public String toString() {
            return "birthday: " + this.mBirthday;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.BIRTHDAY;
        }
    }

    public static class EmailData implements EntryElement {
        private final String mAddress;
        private final boolean mIsPrimary;
        private final String mLabel;
        private final int mType;

        public EmailData(String data, int type, String label, boolean isPrimary) {
            this.mType = type;
            this.mAddress = data;
            this.mLabel = label;
            this.mIsPrimary = isPrimary;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/email_v2");
            builder.withValue("data2", Integer.valueOf(this.mType));
            if (this.mType == 0) {
                builder.withValue("data3", this.mLabel);
            }
            builder.withValue("data1", this.mAddress);
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Integer.valueOf(1));
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mAddress);
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EmailData)) {
                return false;
            }
            EmailData emailData = (EmailData) obj;
            if (this.mType != emailData.mType || !TextUtils.equals(this.mAddress, emailData.mAddress) || !TextUtils.equals(this.mLabel, emailData.mLabel)) {
                z = false;
            } else if (this.mIsPrimary != emailData.mIsPrimary) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            int i2 = this.mType * 31;
            if (this.mAddress != null) {
                hashCode = this.mAddress.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (i2 + hashCode) * 31;
            if (this.mLabel != null) {
                i = this.mLabel.hashCode();
            }
            return ((hashCode + i) * 31) + (this.mIsPrimary ? 1231 : 1237);
        }

        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s", new Object[]{Integer.valueOf(this.mType), this.mAddress, this.mLabel, Boolean.valueOf(this.mIsPrimary)});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.EMAIL;
        }
    }

    public interface EntryElementIterator {
        boolean onElement(EntryElement entryElement);

        void onElementGroupEnded();

        void onElementGroupStarted(EntryLabel entryLabel);

        void onIterationEnded();

        void onIterationStarted();
    }

    public enum EntryLabel {
        NAME,
        PHONE,
        EMAIL,
        POSTAL_ADDRESS,
        ORGANIZATION,
        IM,
        PHOTO,
        WEBSITE,
        SIP,
        NICKNAME,
        NOTE,
        BIRTHDAY,
        ANNIVERSARY,
        ANDROID_CUSTOM,
        GROUPMEMBERSHIP
    }

    public interface ExtendDataCommitter {
        long commitGroupmemberShip(String str, Account account);
    }

    public static class GroupMembershipData implements EntryElement {
        private final String mTitle;
        VCardEntry mVCardEntry;

        public GroupMembershipData(String title, VCardEntry entry) {
            this.mTitle = title;
            this.mVCardEntry = entry;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Log.i("vCard", "GroupMembershipData constructInsertOperation begin");
            if (this.mVCardEntry.mExtendDataCommitter == null || TextUtils.isEmpty(this.mTitle)) {
                Log.i("vCard", "GroupMembershipData return mVCardEntry.mExtendDataCommitter = " + this.mVCardEntry.mExtendDataCommitter + " mTitle = " + this.mTitle);
                return;
            }
            long groupId = this.mVCardEntry.mExtendDataCommitter.commitGroupmemberShip(this.mTitle, this.mVCardEntry.mAccount);
            if (groupId < 0) {
                Log.i("vCard", "GroupMembershipData constructInsertOperation failed groupId = " + groupId);
                return;
            }
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/group_membership");
            builder.withValue("data1", Long.valueOf(groupId));
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mTitle);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof GroupMembershipData)) {
                return false;
            }
            return TextUtils.equals(this.mTitle, ((GroupMembershipData) obj).mTitle);
        }

        public int hashCode() {
            return this.mTitle != null ? this.mTitle.hashCode() : 0;
        }

        public String toString() {
            return "title: " + this.mTitle;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.GROUPMEMBERSHIP;
        }
    }

    public static class ImData implements EntryElement {
        private final String mAddress;
        private final String mCustomProtocol;
        private final boolean mIsPrimary;
        private final int mProtocol;
        private final int mType;

        public ImData(int protocol, String customProtocol, String address, int type, boolean isPrimary) {
            this.mProtocol = protocol;
            this.mCustomProtocol = customProtocol;
            this.mType = type;
            this.mAddress = address;
            this.mIsPrimary = isPrimary;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/im");
            builder.withValue("data2", Integer.valueOf(this.mType));
            builder.withValue("data5", Integer.valueOf(this.mProtocol));
            builder.withValue("data1", this.mAddress);
            if (this.mProtocol == -1) {
                builder.withValue("data6", this.mCustomProtocol);
            }
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Integer.valueOf(1));
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mAddress);
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ImData)) {
                return false;
            }
            ImData imData = (ImData) obj;
            if (this.mType != imData.mType || this.mProtocol != imData.mProtocol || !TextUtils.equals(this.mCustomProtocol, imData.mCustomProtocol) || !TextUtils.equals(this.mAddress, imData.mAddress)) {
                z = false;
            } else if (this.mIsPrimary != imData.mIsPrimary) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            int i2 = ((this.mType * 31) + this.mProtocol) * 31;
            if (this.mCustomProtocol != null) {
                hashCode = this.mCustomProtocol.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (i2 + hashCode) * 31;
            if (this.mAddress != null) {
                i = this.mAddress.hashCode();
            }
            return ((hashCode + i) * 31) + (this.mIsPrimary ? 1231 : 1237);
        }

        public String toString() {
            return String.format("type: %d, protocol: %d, custom_protcol: %s, data: %s, isPrimary: %s", new Object[]{Integer.valueOf(this.mType), Integer.valueOf(this.mProtocol), this.mCustomProtocol, this.mAddress, Boolean.valueOf(this.mIsPrimary)});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.IM;
        }
    }

    private class InsertOperationConstrutor implements EntryElementIterator {
        private final int mBackReferenceIndex;
        private final List<ContentProviderOperation> mOperationList;

        public InsertOperationConstrutor(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            this.mOperationList = operationList;
            this.mBackReferenceIndex = backReferenceIndex;
        }

        public void onIterationStarted() {
        }

        public void onIterationEnded() {
        }

        public void onElementGroupStarted(EntryLabel label) {
        }

        public void onElementGroupEnded() {
        }

        public boolean onElement(EntryElement elem) {
            if (!elem.isEmpty()) {
                elem.constructInsertOperation(this.mOperationList, this.mBackReferenceIndex);
            }
            return true;
        }
    }

    private class IsIgnorableIterator implements EntryElementIterator {
        private boolean mEmpty;

        private IsIgnorableIterator() {
            this.mEmpty = true;
        }

        public void onIterationStarted() {
        }

        public void onIterationEnded() {
        }

        public void onElementGroupStarted(EntryLabel label) {
        }

        public void onElementGroupEnded() {
        }

        public boolean onElement(EntryElement elem) {
            if (elem.isEmpty()) {
                return true;
            }
            this.mEmpty = false;
            return false;
        }

        public boolean getResult() {
            return this.mEmpty;
        }
    }

    public static class NameData implements EntryElement {
        public String displayName;
        private String mFamily;
        private String mFormatted;
        private String mGiven;
        private String mMiddle;
        private String mPhoneticFamily;
        private String mPhoneticGiven;
        private String mPhoneticMiddle;
        private String mPrefix;
        private String mSortString;
        private String mSuffix;

        public boolean emptyStructuredName() {
            if (TextUtils.isEmpty(this.mFamily) && TextUtils.isEmpty(this.mGiven) && TextUtils.isEmpty(this.mMiddle) && TextUtils.isEmpty(this.mPrefix)) {
                return TextUtils.isEmpty(this.mSuffix);
            }
            return false;
        }

        public boolean emptyPhoneticStructuredName() {
            if (TextUtils.isEmpty(this.mPhoneticFamily) && TextUtils.isEmpty(this.mPhoneticGiven)) {
                return TextUtils.isEmpty(this.mPhoneticMiddle);
            }
            return false;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/name");
            if (!TextUtils.isEmpty(this.mGiven)) {
                builder.withValue("data2", this.mGiven);
            }
            if (!TextUtils.isEmpty(this.mFamily)) {
                builder.withValue("data3", this.mFamily);
            }
            if (!TextUtils.isEmpty(this.mMiddle)) {
                builder.withValue("data5", this.mMiddle);
            }
            if (!TextUtils.isEmpty(this.mPrefix)) {
                builder.withValue("data4", this.mPrefix);
            }
            if (!TextUtils.isEmpty(this.mSuffix)) {
                builder.withValue("data6", this.mSuffix);
            }
            boolean phoneticNameSpecified = false;
            if (!TextUtils.isEmpty(this.mPhoneticGiven)) {
                builder.withValue("data7", this.mPhoneticGiven);
                phoneticNameSpecified = true;
            }
            if (!TextUtils.isEmpty(this.mPhoneticFamily)) {
                builder.withValue("data9", this.mPhoneticFamily);
                phoneticNameSpecified = true;
            }
            if (!TextUtils.isEmpty(this.mPhoneticMiddle)) {
                builder.withValue("data8", this.mPhoneticMiddle);
                phoneticNameSpecified = true;
            }
            if (!phoneticNameSpecified) {
                builder.withValue("data7", this.mSortString);
            }
            builder.withValue("data1", this.displayName);
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            if (TextUtils.isEmpty(this.mFamily) && TextUtils.isEmpty(this.mMiddle) && TextUtils.isEmpty(this.mGiven) && TextUtils.isEmpty(this.mPrefix) && TextUtils.isEmpty(this.mSuffix) && TextUtils.isEmpty(this.mFormatted) && TextUtils.isEmpty(this.mPhoneticFamily) && TextUtils.isEmpty(this.mPhoneticMiddle) && TextUtils.isEmpty(this.mPhoneticGiven)) {
                return TextUtils.isEmpty(this.mSortString);
            }
            return false;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NameData)) {
                return false;
            }
            NameData nameData = (NameData) obj;
            if (TextUtils.equals(this.mFamily, nameData.mFamily) && TextUtils.equals(this.mMiddle, nameData.mMiddle) && TextUtils.equals(this.mGiven, nameData.mGiven) && TextUtils.equals(this.mPrefix, nameData.mPrefix) && TextUtils.equals(this.mSuffix, nameData.mSuffix) && TextUtils.equals(this.mFormatted, nameData.mFormatted) && TextUtils.equals(this.mPhoneticFamily, nameData.mPhoneticFamily) && TextUtils.equals(this.mPhoneticMiddle, nameData.mPhoneticMiddle) && TextUtils.equals(this.mPhoneticGiven, nameData.mPhoneticGiven)) {
                z = TextUtils.equals(this.mSortString, nameData.mSortString);
            }
            return z;
        }

        public int hashCode() {
            int hash = 0;
            for (String hashTarget : new String[]{this.mFamily, this.mMiddle, this.mGiven, this.mPrefix, this.mSuffix, this.mFormatted, this.mPhoneticFamily, this.mPhoneticMiddle, this.mPhoneticGiven, this.mSortString}) {
                int hashCode;
                int i = hash * 31;
                if (hashTarget != null) {
                    hashCode = hashTarget.hashCode();
                } else {
                    hashCode = 0;
                }
                hash = i + hashCode;
            }
            return hash;
        }

        public String toString() {
            return String.format("family: %s, given: %s, middle: %s, prefix: %s, suffix: %s", new Object[]{this.mFamily, this.mGiven, this.mMiddle, this.mPrefix, this.mSuffix});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.NAME;
        }
    }

    public static class NicknameData implements EntryElement {
        private final String mNickname;

        public NicknameData(String nickname) {
            this.mNickname = nickname;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/nickname");
            builder.withValue("data2", Integer.valueOf(1));
            builder.withValue("data1", this.mNickname);
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mNickname);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof NicknameData)) {
                return false;
            }
            return TextUtils.equals(this.mNickname, ((NicknameData) obj).mNickname);
        }

        public int hashCode() {
            return this.mNickname != null ? this.mNickname.hashCode() : 0;
        }

        public String toString() {
            return "nickname: " + this.mNickname;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.NICKNAME;
        }
    }

    public static class NoteData implements EntryElement {
        public final String mNote;

        public NoteData(String note) {
            this.mNote = note;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/note");
            builder.withValue("data1", this.mNote);
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mNote);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NoteData)) {
                return false;
            }
            return TextUtils.equals(this.mNote, ((NoteData) obj).mNote);
        }

        public int hashCode() {
            return this.mNote != null ? this.mNote.hashCode() : 0;
        }

        public String toString() {
            return "note: " + this.mNote;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.NOTE;
        }
    }

    public static class OrganizationData implements EntryElement {
        private String mDepartmentName;
        private boolean mIsPrimary;
        private String mOrganizationName;
        private final String mPhoneticName;
        private String mTitle;
        private final int mType;

        public OrganizationData(String organizationName, String departmentName, String titleName, String phoneticName, int type, boolean isPrimary) {
            this.mType = type;
            this.mOrganizationName = organizationName;
            this.mDepartmentName = departmentName;
            this.mTitle = titleName;
            this.mPhoneticName = phoneticName;
            this.mIsPrimary = isPrimary;
        }

        public String getFormattedString() {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(this.mOrganizationName)) {
                builder.append(this.mOrganizationName);
            }
            if (!TextUtils.isEmpty(this.mDepartmentName)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(this.mDepartmentName);
            }
            if (!TextUtils.isEmpty(this.mTitle)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(this.mTitle);
            }
            return builder.toString();
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/organization");
            builder.withValue("data2", Integer.valueOf(this.mType));
            if (this.mOrganizationName != null) {
                builder.withValue("data1", this.mOrganizationName);
            }
            if (this.mDepartmentName != null) {
                builder.withValue("data5", this.mDepartmentName);
            }
            if (this.mTitle != null) {
                builder.withValue("data4", this.mTitle);
            }
            if (this.mPhoneticName != null) {
                builder.withValue("data8", this.mPhoneticName);
            }
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Integer.valueOf(1));
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            if (TextUtils.isEmpty(this.mOrganizationName) && TextUtils.isEmpty(this.mDepartmentName) && TextUtils.isEmpty(this.mTitle)) {
                return TextUtils.isEmpty(this.mPhoneticName);
            }
            return false;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof OrganizationData)) {
                return false;
            }
            OrganizationData organization = (OrganizationData) obj;
            if (this.mType != organization.mType || !TextUtils.equals(this.mOrganizationName, organization.mOrganizationName) || !TextUtils.equals(this.mDepartmentName, organization.mDepartmentName) || !TextUtils.equals(this.mTitle, organization.mTitle)) {
                z = false;
            } else if (this.mIsPrimary != organization.mIsPrimary) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            int i2 = this.mType * 31;
            if (this.mOrganizationName != null) {
                hashCode = this.mOrganizationName.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (i2 + hashCode) * 31;
            if (this.mDepartmentName != null) {
                hashCode = this.mDepartmentName.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (i2 + hashCode) * 31;
            if (this.mTitle != null) {
                i = this.mTitle.hashCode();
            }
            return ((hashCode + i) * 31) + (this.mIsPrimary ? 1231 : 1237);
        }

        public String toString() {
            return String.format("type: %d, organization: %s, department: %s, title: %s, isPrimary: %s", new Object[]{Integer.valueOf(this.mType), this.mOrganizationName, this.mDepartmentName, this.mTitle, Boolean.valueOf(this.mIsPrimary)});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.ORGANIZATION;
        }
    }

    public static class PhoneData implements EntryElement {
        private boolean mIsPrimary;
        private final String mLabel;
        private final String mNumber;
        private final int mType;

        public PhoneData(String data, int type, String label, boolean isPrimary) {
            this.mNumber = data;
            this.mType = type;
            this.mLabel = label;
            this.mIsPrimary = isPrimary;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
            builder.withValue("data2", Integer.valueOf(this.mType));
            if (this.mType == 0) {
                builder.withValue("data3", this.mLabel);
            }
            builder.withValue("data1", this.mNumber);
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Integer.valueOf(1));
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mNumber);
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PhoneData)) {
                return false;
            }
            PhoneData phoneData = (PhoneData) obj;
            if (this.mType != phoneData.mType || !TextUtils.equals(this.mNumber, phoneData.mNumber) || !TextUtils.equals(this.mLabel, phoneData.mLabel)) {
                z = false;
            } else if (this.mIsPrimary != phoneData.mIsPrimary) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            int i2 = this.mType * 31;
            if (this.mNumber != null) {
                hashCode = this.mNumber.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (i2 + hashCode) * 31;
            if (this.mLabel != null) {
                i = this.mLabel.hashCode();
            }
            return ((hashCode + i) * 31) + (this.mIsPrimary ? 1231 : 1237);
        }

        public String toString() {
            return String.format("type: %d, data: %s, label: %s, isPrimary: %s", new Object[]{Integer.valueOf(this.mType), this.mNumber, this.mLabel, Boolean.valueOf(this.mIsPrimary)});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.PHONE;
        }
    }

    public static class PhotoData implements EntryElement {
        private final byte[] mBytes;
        private final String mFormat;
        private Integer mHashCode = null;
        private final boolean mIsPrimary;

        public PhotoData(String format, byte[] photoBytes, boolean isPrimary) {
            this.mFormat = format;
            this.mBytes = photoBytes;
            this.mIsPrimary = isPrimary;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/photo");
            builder.withValue("data15", this.mBytes);
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Integer.valueOf(1));
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return this.mBytes == null || this.mBytes.length == 0;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PhotoData)) {
                return false;
            }
            PhotoData photoData = (PhotoData) obj;
            if (!TextUtils.equals(this.mFormat, photoData.mFormat) || !Arrays.equals(this.mBytes, photoData.mBytes)) {
                z = false;
            } else if (this.mIsPrimary != photoData.mIsPrimary) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            if (this.mHashCode != null) {
                return this.mHashCode.intValue();
            }
            int hash;
            if (this.mFormat != null) {
                hash = this.mFormat.hashCode();
            } else {
                hash = 0;
            }
            hash *= 31;
            if (this.mBytes != null) {
                byte[] bArr = this.mBytes;
                while (i < bArr.length) {
                    hash += bArr[i];
                    i++;
                }
            }
            hash = (hash * 31) + (this.mIsPrimary ? 1231 : 1237);
            this.mHashCode = Integer.valueOf(hash);
            return hash;
        }

        public String toString() {
            return String.format("format: %s: size: %d, isPrimary: %s", new Object[]{this.mFormat, Integer.valueOf(this.mBytes.length), Boolean.valueOf(this.mIsPrimary)});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.PHOTO;
        }
    }

    public static class PostalData implements EntryElement {
        private final String mCountry;
        private final String mExtendedAddress;
        private boolean mIsPrimary;
        private final String mLabel;
        private final String mLocalty;
        private final String mPobox;
        private final String mPostalCode;
        private final String mRegion;
        private final String mStreet;
        private final int mType;
        private int mVCardType;

        public PostalData(String pobox, String extendedAddress, String street, String localty, String region, String postalCode, String country, int type, String label, boolean isPrimary, int vcardType) {
            this.mType = type;
            this.mPobox = pobox;
            this.mExtendedAddress = extendedAddress;
            this.mStreet = street;
            this.mLocalty = localty;
            this.mRegion = region;
            this.mPostalCode = postalCode;
            this.mCountry = country;
            this.mLabel = label;
            this.mIsPrimary = isPrimary;
            this.mVCardType = vcardType;
        }

        public static com.android.vcard.VCardEntry.PostalData constructPostalData(java.util.List<java.lang.String> r20, int r21, java.lang.String r22, boolean r23, int r24) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r2 = 7;
            r0 = new java.lang.String[r2];
            r16 = r0;
            r19 = r20.size();
            r2 = 7;
            r0 = r19;
            if (r0 <= r2) goto L_0x0010;
        L_0x000e:
            r19 = 7;
        L_0x0010:
            r17 = 0;
            r15 = r20.iterator();
        L_0x0016:
            r2 = r15.hasNext();
            if (r2 == 0) goto L_0x0038;
        L_0x001c:
            r14 = r15.next();
            r14 = (java.lang.String) r14;
            r16[r17] = r14;
            r17 = r17 + 1;
            r0 = r17;
            r1 = r19;
            if (r0 < r1) goto L_0x0016;
        L_0x002c:
            r18 = r17;
        L_0x002e:
            r2 = 7;
            r0 = r18;
            if (r0 >= r2) goto L_0x003b;
        L_0x0033:
            r17 = r18 + 1;
            r2 = 0;
            r16[r18] = r2;
        L_0x0038:
            r18 = r17;
            goto L_0x002e;
        L_0x003b:
            r2 = new com.android.vcard.VCardEntry$PostalData;
            r3 = 0;
            r3 = r16[r3];
            r4 = 1;
            r4 = r16[r4];
            r5 = 2;
            r5 = r16[r5];
            r6 = 3;
            r6 = r16[r6];
            r7 = 4;
            r7 = r16[r7];
            r8 = 5;
            r8 = r16[r8];
            r9 = 6;
            r9 = r16[r9];
            r10 = r21;
            r11 = r22;
            r12 = r23;
            r13 = r24;
            r2.<init>(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13);
            return r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.vcard.VCardEntry.PostalData.constructPostalData(java.util.List, int, java.lang.String, boolean, int):com.android.vcard.VCardEntry$PostalData");
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/postal-address_v2");
            builder.withValue("data2", Integer.valueOf(this.mType));
            if (this.mType == 0) {
                builder.withValue("data3", this.mLabel);
            }
            builder.withValue("data5", this.mPobox);
            builder.withValue("data6", this.mExtendedAddress);
            builder.withValue("data4", this.mStreet);
            builder.withValue("data7", this.mLocalty);
            builder.withValue("data8", this.mRegion);
            builder.withValue("data9", this.mPostalCode);
            builder.withValue("data10", this.mCountry);
            builder.withValue("data1", getFormattedAddress(this.mVCardType));
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Integer.valueOf(1));
            }
            operationList.add(builder.build());
        }

        public String getFormattedAddress(int vcardType) {
            StringBuilder builder = new StringBuilder();
            boolean empty = true;
            String[] dataArray = new String[]{this.mPobox, this.mExtendedAddress, this.mStreet, this.mLocalty, this.mRegion, this.mPostalCode, this.mCountry};
            int i;
            String addressPart;
            if (VCardConfig.isJapaneseDevice(vcardType)) {
                for (i = 6; i >= 0; i--) {
                    addressPart = dataArray[i];
                    if (!TextUtils.isEmpty(addressPart)) {
                        if (empty) {
                            empty = false;
                        } else {
                            builder.append(' ');
                        }
                        builder.append(addressPart);
                    }
                }
            } else {
                for (i = 0; i < 7; i++) {
                    addressPart = dataArray[i];
                    if (!TextUtils.isEmpty(addressPart)) {
                        if (empty) {
                            empty = false;
                        } else {
                            builder.append(' ');
                        }
                        builder.append(addressPart);
                    }
                }
            }
            return builder.toString().trim();
        }

        public boolean isEmpty() {
            return (TextUtils.isEmpty(this.mPobox) && TextUtils.isEmpty(this.mExtendedAddress) && TextUtils.isEmpty(this.mStreet) && TextUtils.isEmpty(this.mLocalty) && TextUtils.isEmpty(this.mRegion) && TextUtils.isEmpty(this.mPostalCode)) ? TextUtils.isEmpty(this.mCountry) : false;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PostalData)) {
                return false;
            }
            PostalData postalData = (PostalData) obj;
            if (this.mType == postalData.mType && ((this.mType != 0 || TextUtils.equals(this.mLabel, postalData.mLabel)) && this.mIsPrimary == postalData.mIsPrimary && TextUtils.equals(this.mPobox, postalData.mPobox) && TextUtils.equals(this.mExtendedAddress, postalData.mExtendedAddress) && TextUtils.equals(this.mStreet, postalData.mStreet) && TextUtils.equals(this.mLocalty, postalData.mLocalty) && TextUtils.equals(this.mRegion, postalData.mRegion) && TextUtils.equals(this.mPostalCode, postalData.mPostalCode))) {
                z = TextUtils.equals(this.mCountry, postalData.mCountry);
            }
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = this.mType * 31;
            if (this.mLabel != null) {
                hashCode = this.mLabel.hashCode();
            } else {
                hashCode = 0;
            }
            int hash = ((i + hashCode) * 31) + (this.mIsPrimary ? 1231 : 1237);
            for (String hashTarget : new String[]{this.mPobox, this.mExtendedAddress, this.mStreet, this.mLocalty, this.mRegion, this.mPostalCode, this.mCountry}) {
                int i2 = hash * 31;
                if (hashTarget != null) {
                    hashCode = hashTarget.hashCode();
                } else {
                    hashCode = 0;
                }
                hash = i2 + hashCode;
            }
            return hash;
        }

        public String toString() {
            return String.format("type: %d, label: %s, isPrimary: %s, pobox: %s, extendedAddress: %s, street: %s, localty: %s, region: %s, postalCode %s, country: %s", new Object[]{Integer.valueOf(this.mType), this.mLabel, Boolean.valueOf(this.mIsPrimary), this.mPobox, this.mExtendedAddress, this.mStreet, this.mLocalty, this.mRegion, this.mPostalCode, this.mCountry});
        }

        public final EntryLabel getEntryLabel() {
            return EntryLabel.POSTAL_ADDRESS;
        }
    }

    public static class SipData implements EntryElement {
        private final String mAddress;
        private final boolean mIsPrimary;
        private final String mLabel;
        private final int mType;

        public SipData(String rawSip, int type, String label, boolean isPrimary) {
            if (rawSip.startsWith("sip:")) {
                this.mAddress = rawSip.substring(4);
            } else {
                this.mAddress = rawSip;
            }
            this.mType = type;
            this.mLabel = label;
            this.mIsPrimary = isPrimary;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/sip_address");
            builder.withValue("data1", this.mAddress);
            builder.withValue("data2", Integer.valueOf(this.mType));
            if (this.mType == 0) {
                builder.withValue("data3", this.mLabel);
            }
            if (this.mIsPrimary) {
                builder.withValue("is_primary", Boolean.valueOf(this.mIsPrimary));
            }
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mAddress);
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SipData)) {
                return false;
            }
            SipData sipData = (SipData) obj;
            if (this.mType != sipData.mType || !TextUtils.equals(this.mLabel, sipData.mLabel) || !TextUtils.equals(this.mAddress, sipData.mAddress)) {
                z = false;
            } else if (this.mIsPrimary != sipData.mIsPrimary) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            int i2 = this.mType * 31;
            if (this.mLabel != null) {
                hashCode = this.mLabel.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (i2 + hashCode) * 31;
            if (this.mAddress != null) {
                i = this.mAddress.hashCode();
            }
            return ((hashCode + i) * 31) + (this.mIsPrimary ? 1231 : 1237);
        }

        public String toString() {
            return "sip: " + this.mAddress;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.SIP;
        }
    }

    private class ToStringIterator implements EntryElementIterator {
        private StringBuilder mBuilder;
        private boolean mFirstElement;

        private ToStringIterator() {
        }

        public void onIterationStarted() {
            this.mBuilder = new StringBuilder();
            this.mBuilder.append("[[hash: ").append(VCardEntry.this.hashCode()).append("\n");
        }

        public void onElementGroupStarted(EntryLabel label) {
            this.mBuilder.append(label.toString()).append(": ");
            this.mFirstElement = true;
        }

        public boolean onElement(EntryElement elem) {
            if (!this.mFirstElement) {
                this.mBuilder.append(", ");
                this.mFirstElement = false;
            }
            this.mBuilder.append("[").append(elem.toString()).append("]");
            return true;
        }

        public void onElementGroupEnded() {
            this.mBuilder.append("\n");
        }

        public void onIterationEnded() {
            this.mBuilder.append("]]\n");
        }

        public String toString() {
            return this.mBuilder.toString();
        }
    }

    public static class WebsiteData implements EntryElement {
        private final String mWebsite;

        public WebsiteData(String website) {
            this.mWebsite = website;
        }

        public void constructInsertOperation(List<ContentProviderOperation> operationList, int backReferenceIndex) {
            Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference("raw_contact_id", backReferenceIndex);
            builder.withValue("mimetype", "vnd.android.cursor.item/website");
            builder.withValue("data1", this.mWebsite);
            builder.withValue("data2", Integer.valueOf(1));
            operationList.add(builder.build());
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mWebsite);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof WebsiteData)) {
                return false;
            }
            return TextUtils.equals(this.mWebsite, ((WebsiteData) obj).mWebsite);
        }

        public int hashCode() {
            return this.mWebsite != null ? this.mWebsite.hashCode() : 0;
        }

        public String toString() {
            return "website: " + this.mWebsite;
        }

        public EntryLabel getEntryLabel() {
            return EntryLabel.WEBSITE;
        }
    }

    static {
        sImMap.put("X-AIM", Integer.valueOf(0));
        sImMap.put("X-MSN", Integer.valueOf(1));
        sImMap.put("X-YAHOO", Integer.valueOf(2));
        sImMap.put("X-ICQ", Integer.valueOf(6));
        sImMap.put("X-QQ", Integer.valueOf(4));
        sImMap.put("X-JABBER", Integer.valueOf(7));
        sImMap.put("X-SKYPE-USERNAME", Integer.valueOf(3));
        sImMap.put("X-GOOGLE-TALK", Integer.valueOf(5));
        sImMap.put("X-GOOGLE TALK", Integer.valueOf(5));
    }

    public final void iterateAllData(EntryElementIterator iterator) {
        iterator.onIterationStarted();
        iterator.onElementGroupStarted(this.mNameData.getEntryLabel());
        iterator.onElement(this.mNameData);
        iterator.onElementGroupEnded();
        iterateOneList(this.mPhoneList, iterator);
        iterateOneList(this.mEmailList, iterator);
        iterateOneList(this.mPostalList, iterator);
        iterateOneList(this.mOrganizationList, iterator);
        iterateOneList(this.mImList, iterator);
        iterateOneList(this.mPhotoList, iterator);
        iterateOneList(this.mWebsiteList, iterator);
        iterateOneList(this.mSipList, iterator);
        iterateOneList(this.mNicknameList, iterator);
        iterateOneList(this.mNoteList, iterator);
        iterateOneList(this.mAndroidCustomDataList, iterator);
        if (this.mBirthday != null) {
            iterator.onElementGroupStarted(this.mBirthday.getEntryLabel());
            iterator.onElement(this.mBirthday);
            iterator.onElementGroupEnded();
        }
        if (this.mAnniversary != null) {
            iterator.onElementGroupStarted(this.mAnniversary.getEntryLabel());
            iterator.onElement(this.mAnniversary);
            iterator.onElementGroupEnded();
        }
        iterateOneList(this.mGroupMembershipList, iterator);
        iterator.onIterationEnded();
    }

    private void iterateOneList(List<? extends EntryElement> elemList, EntryElementIterator iterator) {
        if (elemList != null && elemList.size() > 0) {
            iterator.onElementGroupStarted(((EntryElement) elemList.get(0)).getEntryLabel());
            for (EntryElement elem : elemList) {
                iterator.onElement(elem);
            }
            iterator.onElementGroupEnded();
        }
    }

    public String toString() {
        ToStringIterator iterator = new ToStringIterator();
        iterateAllData(iterator);
        return iterator.toString();
    }

    public VCardEntry() {
        this(-1073741824);
    }

    public VCardEntry(int vcardType) {
        this(vcardType, null);
    }

    public VCardEntry(int vcardType, Account account) {
        this.mNameData = new NameData();
        this.mVCardType = vcardType;
        this.mAccount = account;
    }

    private void addPhone(int type, String data, String label, boolean isPrimary) {
        String formattedNumber;
        if (this.mPhoneList == null) {
            this.mPhoneList = new ArrayList();
        }
        StringBuilder builder = new StringBuilder();
        String trimmed = data.trim();
        if (type == 6 || VCardConfig.refrainPhoneNumberFormatting(this.mVCardType)) {
            formattedNumber = trimmed;
        } else {
            boolean hasPauseOrWait = false;
            int length = trimmed.length();
            int i = 0;
            while (i < length) {
                char ch = trimmed.charAt(i);
                if (ch == 'p' || ch == 'P') {
                    builder.append(',');
                    hasPauseOrWait = true;
                } else if (ch == 'w' || ch == 'W') {
                    builder.append(';');
                    hasPauseOrWait = true;
                } else if (('0' <= ch && ch <= '9') || (i == 0 && ch == '+')) {
                    builder.append(ch);
                } else if (VCardConfig.getNotConvertPhoneNumber()) {
                    builder.append(ch);
                }
                i++;
            }
            if (hasPauseOrWait || VCardConfig.getNotConvertPhoneNumber()) {
                formattedNumber = builder.toString();
            } else {
                formattedNumber = PhoneNumberUtilsPort.formatNumber(builder.toString(), VCardUtils.getPhoneNumberFormat(this.mVCardType));
            }
        }
        this.mPhoneList.add(new PhoneData(formattedNumber, type, label, isPrimary));
    }

    private void addGroupMembershipData(String groupTitle) {
        if (this.mGroupMembershipList == null) {
            this.mGroupMembershipList = new ArrayList();
        }
        this.mGroupMembershipList.add(new GroupMembershipData(groupTitle, this));
    }

    private void addSip(String sipData, int type, String label, boolean isPrimary) {
        if (this.mSipList == null) {
            this.mSipList = new ArrayList();
        }
        this.mSipList.add(new SipData(sipData, type, label, isPrimary));
    }

    private void addNickName(String nickName) {
        if (this.mNicknameList == null) {
            this.mNicknameList = new ArrayList();
        }
        this.mNicknameList.add(new NicknameData(nickName));
    }

    private void addEmail(int type, String data, String label, boolean isPrimary) {
        if (this.mEmailList == null) {
            this.mEmailList = new ArrayList();
        }
        this.mEmailList.add(new EmailData(data, type, label, isPrimary));
    }

    private void addPostal(int type, List<String> propValueList, String label, boolean isPrimary) {
        if (this.mPostalList == null) {
            this.mPostalList = new ArrayList(0);
        }
        this.mPostalList.add(PostalData.constructPostalData(propValueList, type, label, isPrimary, this.mVCardType));
    }

    private void addNewOrganization(String organizationName, String departmentName, String titleName, String phoneticName, int type, boolean isPrimary) {
        if (this.mOrganizationList == null) {
            this.mOrganizationList = new ArrayList();
        }
        this.mOrganizationList.add(new OrganizationData(organizationName, departmentName, titleName, phoneticName, type, isPrimary));
    }

    private String buildSinglePhoneticNameFromSortAsParam(Map<String, Collection<String>> paramMap) {
        Collection<String> sortAsCollection = (Collection) paramMap.get("SORT-AS");
        if (sortAsCollection == null || sortAsCollection.size() == 0) {
            return null;
        }
        if (sortAsCollection.size() > 1) {
            Log.w("vCard", "Incorrect multiple SORT_AS parameters detected: " + Arrays.toString(sortAsCollection.toArray()));
        }
        List<String> sortNames = VCardUtils.constructListFromValue((String) sortAsCollection.iterator().next(), this.mVCardType);
        StringBuilder builder = new StringBuilder();
        for (String elem : sortNames) {
            builder.append(elem);
        }
        return builder.toString();
    }

    private void handleOrgValue(int type, List<String> orgList, Map<String, Collection<String>> paramMap, boolean isPrimary) {
        String organizationName;
        String str;
        String phoneticName = buildSinglePhoneticNameFromSortAsParam(paramMap);
        if (orgList == null) {
            orgList = sEmptyList;
        }
        int size = orgList.size();
        switch (size) {
            case 0:
                organizationName = "";
                str = null;
                break;
            case 1:
                organizationName = (String) orgList.get(0);
                str = null;
                break;
            default:
                organizationName = (String) orgList.get(0);
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < size; i++) {
                    if (i > 1) {
                        builder.append(' ');
                    }
                    builder.append((String) orgList.get(i));
                }
                str = builder.toString();
                break;
        }
        if (this.mOrganizationList == null) {
            addNewOrganization(organizationName, str, null, phoneticName, type, isPrimary);
            return;
        }
        for (OrganizationData organizationData : this.mOrganizationList) {
            if (organizationData.mOrganizationName == null && organizationData.mDepartmentName == null) {
                organizationData.mOrganizationName = organizationName;
                organizationData.mDepartmentName = str;
                organizationData.mIsPrimary = isPrimary;
                return;
            }
        }
        addNewOrganization(organizationName, str, null, phoneticName, type, isPrimary);
    }

    private void handleTitleValue(String title) {
        if (this.mOrganizationList == null) {
            addNewOrganization(null, null, title, null, 1, false);
            return;
        }
        for (OrganizationData organizationData : this.mOrganizationList) {
            if (organizationData.mTitle == null) {
                organizationData.mTitle = title;
                return;
            }
        }
        addNewOrganization(null, null, title, null, 1, false);
    }

    private void addIm(int protocol, String customProtocol, String propValue, int type, boolean isPrimary) {
        if (this.mImList == null) {
            this.mImList = new ArrayList();
        }
        this.mImList.add(new ImData(protocol, customProtocol, propValue, type, isPrimary));
    }

    private void addNote(String note) {
        if (this.mNoteList == null) {
            this.mNoteList = new ArrayList(1);
        }
        this.mNoteList.add(new NoteData(note));
    }

    private void addPhotoBytes(String formatName, byte[] photoBytes, boolean isPrimary) {
        if (this.mPhotoList == null) {
            this.mPhotoList = new ArrayList(1);
        }
        this.mPhotoList.add(new PhotoData(formatName, photoBytes, isPrimary));
    }

    private void tryHandleSortAsName(Map<String, Collection<String>> paramMap) {
        if (!VCardConfig.isVersion30(this.mVCardType) || (TextUtils.isEmpty(this.mNameData.mPhoneticFamily) && TextUtils.isEmpty(this.mNameData.mPhoneticMiddle) && TextUtils.isEmpty(this.mNameData.mPhoneticGiven))) {
            Collection<String> sortAsCollection = (Collection) paramMap.get("SORT-AS");
            if (!(sortAsCollection == null || sortAsCollection.size() == 0)) {
                if (sortAsCollection.size() > 1) {
                    Log.w("vCard", "Incorrect multiple SORT_AS parameters detected: " + Arrays.toString(sortAsCollection.toArray()));
                }
                List<String> sortNames = VCardUtils.constructListFromValue((String) sortAsCollection.iterator().next(), this.mVCardType);
                int size = sortNames.size();
                if (size > 3) {
                    size = 3;
                }
                switch (size) {
                    case 2:
                        break;
                    case 3:
                        this.mNameData.mPhoneticMiddle = (String) sortNames.get(2);
                        break;
                }
                this.mNameData.mPhoneticGiven = (String) sortNames.get(1);
                this.mNameData.mPhoneticFamily = (String) sortNames.get(0);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleNProperty(List<String> paramValues, Map<String, Collection<String>> paramMap) {
        tryHandleSortAsName(paramMap);
        if (paramValues != null) {
            int size = paramValues.size();
            if (size >= 1) {
                if (size > 5) {
                    size = 5;
                }
                switch (size) {
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        this.mNameData.mSuffix = (String) paramValues.get(4);
                        break;
                }
            }
        }
    }

    private void handlePhoneticNameFromSound(List<String> elems) {
        boolean isEmpty;
        if (TextUtils.isEmpty(this.mNameData.mPhoneticFamily) && TextUtils.isEmpty(this.mNameData.mPhoneticMiddle)) {
            isEmpty = TextUtils.isEmpty(this.mNameData.mPhoneticGiven);
        } else {
            isEmpty = false;
        }
        if (isEmpty && elems != null) {
            int size = elems.size();
            if (size >= 1) {
                if (size > 3) {
                    size = 3;
                }
                if (((String) elems.get(0)).length() > 0) {
                    boolean onlyFirstElemIsNonEmpty = true;
                    for (int i = 1; i < size; i++) {
                        if (((String) elems.get(i)).length() > 0) {
                            onlyFirstElemIsNonEmpty = false;
                            break;
                        }
                    }
                    if (onlyFirstElemIsNonEmpty) {
                        String[] namesArray = ((String) elems.get(0)).split(HwCustPreloadContacts.EMPTY_STRING);
                        int nameArrayLength = namesArray.length;
                        if (nameArrayLength == 3) {
                            this.mNameData.mPhoneticFamily = namesArray[0];
                            this.mNameData.mPhoneticMiddle = namesArray[1];
                            this.mNameData.mPhoneticGiven = namesArray[2];
                        } else if (nameArrayLength == 2) {
                            this.mNameData.mPhoneticFamily = namesArray[0];
                            this.mNameData.mPhoneticGiven = namesArray[1];
                        } else {
                            this.mNameData.mPhoneticGiven = (String) elems.get(0);
                        }
                        return;
                    }
                }
                switch (size) {
                    case 2:
                        break;
                    case 3:
                        this.mNameData.mPhoneticMiddle = (String) elems.get(2);
                        break;
                }
                this.mNameData.mPhoneticGiven = (String) elems.get(1);
                this.mNameData.mPhoneticFamily = (String) elems.get(0);
            }
        }
    }

    public void addProperty(VCardProperty property) {
        String propertyName = property.getName();
        Map<String, Collection<String>> paramMap = property.getParameterMap();
        List<String> propertyValueList = property.getValueList();
        byte[] propertyBytes = property.getByteValue();
        if ((propertyValueList != null && propertyValueList.size() != 0) || propertyBytes != null) {
            String trim;
            if (propertyValueList != null) {
                trim = listToString(propertyValueList).trim();
            } else {
                trim = null;
            }
            if (!propertyName.equals("VERSION")) {
                if (propertyName.equals("FN")) {
                    this.mNameData.mFormatted = trim;
                } else {
                    if (!propertyName.equals("NAME")) {
                        if (propertyName.equals("N")) {
                            handleNProperty(propertyValueList, paramMap);
                        } else {
                            if (propertyName.equals("SORT-STRING")) {
                                this.mNameData.mSortString = trim;
                            } else {
                                if (!propertyName.equals("NICKNAME")) {
                                    if (!propertyName.equals("X-NICKNAME")) {
                                        Collection<String> typeCollection;
                                        if (propertyName.equals("SOUND")) {
                                            typeCollection = (Collection) paramMap.get("TYPE");
                                            if (typeCollection != null) {
                                                if (typeCollection.contains("X-IRMC-N")) {
                                                    handlePhoneticNameFromSound(VCardUtils.constructListFromValue(trim, this.mVCardType));
                                                }
                                            }
                                        } else {
                                            int type;
                                            String str;
                                            boolean isPrimary;
                                            String typeStringUpperCase;
                                            if (propertyName.equals("ADR")) {
                                                boolean valuesAreAllEmpty = true;
                                                for (String value : propertyValueList) {
                                                    if (!TextUtils.isEmpty(value)) {
                                                        valuesAreAllEmpty = false;
                                                        break;
                                                    }
                                                }
                                                if (!valuesAreAllEmpty) {
                                                    type = -1;
                                                    str = null;
                                                    isPrimary = false;
                                                    typeCollection = (Collection) paramMap.get("TYPE");
                                                    if (typeCollection != null) {
                                                        for (String typeStringOrg : typeCollection) {
                                                            typeStringUpperCase = typeStringOrg.toUpperCase();
                                                            if (typeStringUpperCase.equals("PREF")) {
                                                                isPrimary = true;
                                                            } else {
                                                                if (typeStringUpperCase.equals("HOME")) {
                                                                    type = 1;
                                                                    str = null;
                                                                } else {
                                                                    if (!typeStringUpperCase.equals("WORK")) {
                                                                        if (!typeStringUpperCase.equalsIgnoreCase("COMPANY")) {
                                                                            if (!typeStringUpperCase.equals("PARCEL")) {
                                                                                if (!typeStringUpperCase.equals("DOM")) {
                                                                                    if (!typeStringUpperCase.equals("INTL") && type < 0) {
                                                                                        type = 0;
                                                                                        if (typeStringUpperCase.startsWith("X-")) {
                                                                                            str = typeStringOrg.substring(2);
                                                                                        } else {
                                                                                            str = typeStringOrg;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    type = 2;
                                                                    str = null;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (type < 0) {
                                                        type = 1;
                                                    }
                                                    addPostal(type, propertyValueList, str, isPrimary);
                                                } else {
                                                    return;
                                                }
                                            }
                                            if (propertyName.equals("EMAIL")) {
                                                type = -1;
                                                str = null;
                                                isPrimary = false;
                                                typeCollection = (Collection) paramMap.get("TYPE");
                                                if (typeCollection != null) {
                                                    for (String typeStringOrg2 : typeCollection) {
                                                        typeStringUpperCase = typeStringOrg2.toUpperCase();
                                                        if (typeStringUpperCase.equals("PREF")) {
                                                            isPrimary = true;
                                                        } else {
                                                            if (typeStringUpperCase.equals("HOME")) {
                                                                type = 1;
                                                            } else {
                                                                if (typeStringUpperCase.equals("WORK")) {
                                                                    type = 2;
                                                                } else {
                                                                    if (typeStringUpperCase.equals("CELL")) {
                                                                        type = 4;
                                                                    } else if (type < 0) {
                                                                        if (typeStringUpperCase.startsWith("X-")) {
                                                                            str = typeStringOrg2.substring(2);
                                                                        } else {
                                                                            str = typeStringOrg2;
                                                                        }
                                                                        type = 0;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if (type < 0) {
                                                    type = 3;
                                                }
                                                addEmail(type, trim, str, isPrimary);
                                            } else {
                                                if (propertyName.equals("ORG")) {
                                                    isPrimary = false;
                                                    typeCollection = (Collection) paramMap.get("TYPE");
                                                    if (typeCollection != null) {
                                                        for (String equals : typeCollection) {
                                                            if (equals.equals("PREF")) {
                                                                isPrimary = true;
                                                            }
                                                        }
                                                    }
                                                    handleOrgValue(1, propertyValueList, paramMap, isPrimary);
                                                } else {
                                                    if (propertyName.equals("TITLE")) {
                                                        handleTitleValue(trim);
                                                    } else {
                                                        if (!propertyName.equals("ROLE")) {
                                                            if (!propertyName.equals("PHOTO")) {
                                                                if (!propertyName.equals("LOGO")) {
                                                                    if (propertyName.equals("TEL")) {
                                                                        String phoneNumber = null;
                                                                        boolean isSip = false;
                                                                        if (!VCardConfig.isVersion40(this.mVCardType)) {
                                                                            phoneNumber = trim;
                                                                        } else if (trim.startsWith("sip:")) {
                                                                            isSip = true;
                                                                        } else if (trim.startsWith("tel:")) {
                                                                            phoneNumber = trim.substring(4);
                                                                        } else {
                                                                            phoneNumber = trim;
                                                                        }
                                                                        if (isSip) {
                                                                            handleSipCase(trim, (Collection) paramMap.get("TYPE"));
                                                                        } else if (trim.length() != 0) {
                                                                            typeCollection = (Collection) paramMap.get("TYPE");
                                                                            Object typeObject = VCardUtils.getPhoneTypeFromStrings(typeCollection, phoneNumber);
                                                                            if (typeObject instanceof Integer) {
                                                                                type = ((Integer) typeObject).intValue();
                                                                                str = null;
                                                                            } else {
                                                                                type = 0;
                                                                                str = typeObject.toString();
                                                                            }
                                                                            if (typeCollection != null) {
                                                                                if (typeCollection.contains("PREF")) {
                                                                                    isPrimary = true;
                                                                                    addPhone(type, phoneNumber, str, isPrimary);
                                                                                }
                                                                            }
                                                                            isPrimary = false;
                                                                            addPhone(type, phoneNumber, str, isPrimary);
                                                                        } else {
                                                                            return;
                                                                        }
                                                                    }
                                                                    if (propertyName.equals("X-SKYPE-PSTNNUMBER")) {
                                                                        typeCollection = (Collection) paramMap.get("TYPE");
                                                                        if (typeCollection != null) {
                                                                            if (typeCollection.contains("PREF")) {
                                                                                isPrimary = true;
                                                                                addPhone(7, trim, null, isPrimary);
                                                                            }
                                                                        }
                                                                        isPrimary = false;
                                                                        addPhone(7, trim, null, isPrimary);
                                                                    } else if (sImMap.containsKey(propertyName)) {
                                                                        int protocol = ((Integer) sImMap.get(propertyName)).intValue();
                                                                        isPrimary = false;
                                                                        type = -1;
                                                                        typeCollection = (Collection) paramMap.get("TYPE");
                                                                        if (typeCollection != null) {
                                                                            for (String typeString : typeCollection) {
                                                                                if (typeString.equals("PREF")) {
                                                                                    isPrimary = true;
                                                                                } else if (type < 0) {
                                                                                    if (typeString.equalsIgnoreCase("HOME")) {
                                                                                        type = 1;
                                                                                    } else {
                                                                                        if (typeString.equalsIgnoreCase("WORK")) {
                                                                                            type = 2;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        if (type < 0) {
                                                                            type = 1;
                                                                        }
                                                                        addIm(protocol, null, trim, type, isPrimary);
                                                                    } else {
                                                                        if (propertyName.equals("NOTE")) {
                                                                            addNote(trim);
                                                                        } else {
                                                                            if (propertyName.equals("URL")) {
                                                                                if (this.mWebsiteList == null) {
                                                                                    this.mWebsiteList = new ArrayList(1);
                                                                                }
                                                                                this.mWebsiteList.add(new WebsiteData(trim));
                                                                            } else {
                                                                                if (propertyName.equals("BDAY")) {
                                                                                    this.mBirthday = new BirthdayData(trim);
                                                                                } else {
                                                                                    if (propertyName.equals("ANNIVERSARY")) {
                                                                                        this.mAnniversary = new AnniversaryData(trim);
                                                                                    } else {
                                                                                        if (propertyName.equals("X-GROUP-MEMBERSHIP")) {
                                                                                            addGroupMembershipData(trim);
                                                                                        } else {
                                                                                            if (propertyName.equals("X-PHONETIC-FIRST-NAME")) {
                                                                                                this.mNameData.mPhoneticGiven = trim;
                                                                                            } else {
                                                                                                if (propertyName.equals("X-PHONETIC-MIDDLE-NAME")) {
                                                                                                    this.mNameData.mPhoneticMiddle = trim;
                                                                                                } else {
                                                                                                    if (propertyName.equals("X-PHONETIC-LAST-NAME")) {
                                                                                                        this.mNameData.mPhoneticFamily = trim;
                                                                                                    } else {
                                                                                                        if (!propertyName.equals("IMPP")) {
                                                                                                            if (!propertyName.equals("X-SIP")) {
                                                                                                                if (propertyName.equals("X-ANDROID-CUSTOM")) {
                                                                                                                    handleAndroidCustomProperty(VCardUtils.constructListFromValue(trim, this.mVCardType));
                                                                                                                } else if (propertyName.toUpperCase().startsWith("X-")) {
                                                                                                                    if (this.mUnknownXData == null) {
                                                                                                                        this.mUnknownXData = new ArrayList();
                                                                                                                    }
                                                                                                                    this.mUnknownXData.add(new Pair(propertyName, trim));
                                                                                                                }
                                                                                                            } else if (!TextUtils.isEmpty(trim)) {
                                                                                                                handleSipCase(trim, (Collection) paramMap.get("TYPE"));
                                                                                                            }
                                                                                                        } else if (trim.startsWith("sip:")) {
                                                                                                            handleSipCase(trim, (Collection) paramMap.get("TYPE"));
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            Collection<String> paramMapValue = (Collection) paramMap.get("VALUE");
                                                            if (paramMapValue == null || !paramMapValue.contains("URL")) {
                                                                typeCollection = (Collection) paramMap.get("TYPE");
                                                                String str2 = null;
                                                                isPrimary = false;
                                                                if (typeCollection != null) {
                                                                    for (String typeValue : typeCollection) {
                                                                        if ("PREF".equals(typeValue)) {
                                                                            isPrimary = true;
                                                                        } else if (str2 == null) {
                                                                            str2 = typeValue;
                                                                        }
                                                                    }
                                                                }
                                                                addPhotoBytes(str2, propertyBytes, isPrimary);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                addNickName(trim);
                            }
                        }
                    } else if (TextUtils.isEmpty(this.mNameData.mFormatted)) {
                        this.mNameData.mFormatted = trim;
                    }
                }
            }
        }
    }

    private void handleSipCase(String propValue, Collection<String> typeCollection) {
        if (!TextUtils.isEmpty(propValue)) {
            if (propValue.startsWith("sip:")) {
                propValue = propValue.substring(4);
                if (propValue.length() == 0) {
                    return;
                }
            }
            int type = -1;
            String str = null;
            boolean isPrimary = false;
            if (typeCollection != null) {
                for (String typeStringOrg : typeCollection) {
                    String typeStringUpperCase = typeStringOrg.toUpperCase();
                    if (typeStringUpperCase.equals("PREF")) {
                        isPrimary = true;
                    } else if (typeStringUpperCase.equals("HOME")) {
                        type = 1;
                    } else if (typeStringUpperCase.equals("WORK")) {
                        type = 2;
                    } else if (type < 0) {
                        if (typeStringUpperCase.startsWith("X-")) {
                            str = typeStringOrg.substring(2);
                        } else {
                            str = typeStringOrg;
                        }
                        type = 0;
                    }
                }
            }
            if (type < 0) {
                type = 3;
            }
            addSip(propValue, type, str, isPrimary);
        }
    }

    public void addChild(VCardEntry child) {
        if (this.mChildren == null) {
            this.mChildren = new ArrayList();
        }
        this.mChildren.add(child);
    }

    private void handleAndroidCustomProperty(List<String> customPropertyList) {
        if (this.mAndroidCustomDataList == null) {
            this.mAndroidCustomDataList = new ArrayList();
        }
        this.mAndroidCustomDataList.add(AndroidCustomData.constructAndroidCustomData(customPropertyList));
    }

    private String constructDisplayName() {
        String displayName = null;
        if (!TextUtils.isEmpty(this.mNameData.mFormatted)) {
            displayName = this.mNameData.mFormatted;
        } else if (!this.mNameData.emptyStructuredName()) {
            displayName = VCardUtils.constructNameFromElements(this.mVCardType, this.mNameData.mFamily, this.mNameData.mMiddle, this.mNameData.mGiven, this.mNameData.mPrefix, this.mNameData.mSuffix);
        } else if (!this.mNameData.emptyPhoneticStructuredName()) {
            displayName = VCardUtils.constructNameFromElements(this.mVCardType, this.mNameData.mPhoneticFamily, this.mNameData.mPhoneticMiddle, this.mNameData.mPhoneticGiven);
        } else if (this.mEmailList != null && this.mEmailList.size() > 0) {
            displayName = ((EmailData) this.mEmailList.get(0)).mAddress;
        } else if (this.mPhoneList != null && this.mPhoneList.size() > 0) {
            displayName = ((PhoneData) this.mPhoneList.get(0)).mNumber;
        } else if (this.mPostalList != null && this.mPostalList.size() > 0) {
            displayName = ((PostalData) this.mPostalList.get(0)).getFormattedAddress(this.mVCardType);
        } else if (this.mOrganizationList != null && this.mOrganizationList.size() > 0) {
            displayName = ((OrganizationData) this.mOrganizationList.get(0)).getFormattedString();
        }
        if (displayName == null) {
            return "";
        }
        return displayName;
    }

    public void consolidateFields() {
        this.mNameData.displayName = constructDisplayName();
    }

    public boolean isIgnorable() {
        IsIgnorableIterator iterator = new IsIgnorableIterator();
        iterateAllData(iterator);
        return iterator.getResult();
    }

    public ArrayList<ContentProviderOperation> constructInsertOperations(ContentResolver resolver, ArrayList<ContentProviderOperation> operationList) {
        if (operationList == null) {
            operationList = new ArrayList();
        }
        if (isIgnorable()) {
            return operationList;
        }
        int backReferenceIndex = operationList.size();
        Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        if (this.mAccount != null) {
            builder.withValue("account_name", this.mAccount.name);
            builder.withValue("account_type", this.mAccount.type);
        } else {
            builder.withValue("account_name", null);
            builder.withValue("account_type", null);
        }
        operationList.add(builder.build());
        int start = operationList.size();
        iterateAllData(new InsertOperationConstrutor(operationList, backReferenceIndex));
        int end = operationList.size();
        return operationList;
    }

    private String listToString(List<String> list) {
        int size = list.size();
        if (size > 1) {
            StringBuilder builder = new StringBuilder();
            for (String type : list) {
                builder.append(type);
                if (size - 1 > 0) {
                    builder.append(";");
                }
            }
            return builder.toString();
        } else if (size == 1) {
            return (String) list.get(0);
        } else {
            return "";
        }
    }

    public String getDisplayName() {
        if (this.mNameData.displayName == null) {
            this.mNameData.displayName = constructDisplayName();
        }
        return this.mNameData.displayName;
    }

    public void setExtendDataCommitter(ExtendDataCommitter extendDataCommitter) {
        this.mExtendDataCommitter = extendDataCommitter;
    }
}
