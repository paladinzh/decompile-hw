package com.android.contacts.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ContactsRequest implements Parcelable {
    public static Creator<ContactsRequest> CREATOR = new Creator<ContactsRequest>() {
        public ContactsRequest[] newArray(int size) {
            return new ContactsRequest[size];
        }

        public ContactsRequest createFromParcel(Parcel source) {
            boolean z;
            boolean z2 = true;
            ClassLoader classLoader = getClass().getClassLoader();
            ContactsRequest request = new ContactsRequest();
            request.mValid = source.readInt() != 0;
            request.mActionCode = source.readInt();
            request.mRedirectIntent = (Intent) source.readParcelable(classLoader);
            request.mTitle = source.readCharSequence();
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            request.mSearchMode = z;
            request.mQueryString = source.readString();
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            request.mIncludeProfile = z;
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            request.mLegacyCompatibilityMode = z;
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            request.mDirectorySearchEnabled = z;
            request.mContactUri = (Uri) source.readParcelable(classLoader);
            if (source.readInt() == 0) {
                z2 = false;
            }
            request.mIsMultiPickIntentFromEmail = z2;
            request.mDescription = source.readString();
            return request;
        }
    };
    private int mActionCode = 10;
    private Uri mContactUri;
    private String mDescription;
    private boolean mDirectorySearchEnabled = true;
    private boolean mIncludeProfile;
    private boolean mIsMultiPickIntentFromEmail;
    private boolean mIsSearchDetail;
    private boolean mLegacyCompatibilityMode;
    private String mQueryString;
    private Intent mRedirectIntent;
    private boolean mSearchMode;
    private String mSimAccountTypeFilter;
    private CharSequence mTitle;
    private boolean mValid = true;

    public String toString() {
        return "{ContactsRequest:mValid=" + this.mValid + " mActionCode=" + this.mActionCode + " mRedirectIntent=" + this.mRedirectIntent + " mTitle=" + this.mTitle + " mSearchMode=" + this.mSearchMode + " mQueryString=" + this.mQueryString + " mIncludeProfile=" + this.mIncludeProfile + " mLegacyCompatibilityMode=" + this.mLegacyCompatibilityMode + " mDirectorySearchEnabled=" + this.mDirectorySearchEnabled + " mContactUri=" + this.mContactUri + " mIsMultiPickIntentFromEmail=" + this.mIsMultiPickIntentFromEmail + "}";
    }

    public void copyFrom(ContactsRequest request) {
        this.mValid = request.mValid;
        this.mActionCode = request.mActionCode;
        this.mRedirectIntent = request.mRedirectIntent;
        this.mTitle = request.mTitle;
        this.mSearchMode = request.mSearchMode;
        this.mQueryString = request.mQueryString;
        this.mIncludeProfile = request.mIncludeProfile;
        this.mLegacyCompatibilityMode = request.mLegacyCompatibilityMode;
        this.mDirectorySearchEnabled = request.mDirectorySearchEnabled;
        this.mContactUri = request.mContactUri;
        this.mIsMultiPickIntentFromEmail = request.mIsMultiPickIntentFromEmail;
        this.mDescription = request.mDescription;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        if (this.mValid) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mActionCode);
        dest.writeParcelable(this.mRedirectIntent, 0);
        dest.writeCharSequence(this.mTitle);
        if (this.mSearchMode) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.mQueryString);
        if (this.mIncludeProfile) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mLegacyCompatibilityMode) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mDirectorySearchEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeParcelable(this.mContactUri, 0);
        if (!this.mIsMultiPickIntentFromEmail) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeString(this.mDescription);
    }

    public int describeContents() {
        return 0;
    }

    public boolean isValid() {
        return this.mValid;
    }

    public void setValid(boolean flag) {
        this.mValid = flag;
    }

    public Intent getRedirectIntent() {
        return this.mRedirectIntent;
    }

    public void setActivityTitle(CharSequence title) {
        this.mTitle = title;
    }

    public CharSequence getActivityTitle() {
        return this.mTitle;
    }

    public int getActionCode() {
        return this.mActionCode;
    }

    public void setActionCode(int actionCode) {
        this.mActionCode = actionCode;
    }

    public boolean isSearchMode() {
        return this.mSearchMode;
    }

    public void setSearchMode(boolean flag) {
        this.mSearchMode = flag;
    }

    public String getQueryString() {
        return this.mQueryString;
    }

    public void setQueryString(String string) {
        this.mQueryString = string;
    }

    public boolean shouldIncludeProfile() {
        return this.mIncludeProfile;
    }

    public void setSimAccountTypeFilter(String aAccountType) {
        this.mSimAccountTypeFilter = aAccountType;
    }

    public String getSimAccountTypeFilter() {
        return this.mSimAccountTypeFilter;
    }

    public boolean isLegacyCompatibilityMode() {
        return this.mLegacyCompatibilityMode;
    }

    public void setLegacyCompatibilityMode(boolean flag) {
        this.mLegacyCompatibilityMode = flag;
    }

    public boolean isDirectorySearchEnabled() {
        return this.mDirectorySearchEnabled;
    }

    public Uri getContactUri() {
        return this.mContactUri;
    }

    public void setContactUri(Uri contactUri) {
        this.mContactUri = contactUri;
    }

    public void setSearchDetail(boolean flag) {
        this.mIsSearchDetail = flag;
    }

    public boolean isFromSearchDetail() {
        return this.mIsSearchDetail;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
