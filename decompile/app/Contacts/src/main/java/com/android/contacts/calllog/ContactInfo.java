package com.android.contacts.calllog;

import android.net.Uri;
import android.text.TextUtils;
import com.android.contacts.util.UriUtils;

public final class ContactInfo {
    public static final ContactInfo EMPTY = new ContactInfo();
    public String formattedNumber;
    public String label;
    public Uri lookupUri;
    public String mCachedLookUpUriString;
    public String mCachedMatchedNumber;
    public int[] mCallTypes;
    public String mGeoLocation;
    public boolean mIsNew;
    public boolean mIsPrivate;
    public String name;
    public String normalizedNumber;
    public String number;
    public long photoId;
    public Uri photoUri;
    public int type;
    public long userType;

    public int hashCode() {
        int i = 0;
        int hashCode = this.lookupUri == null ? this.mCachedLookUpUriString == null ? 0 : this.mCachedLookUpUriString.hashCode() : this.lookupUri.hashCode();
        hashCode = (hashCode + 31) * 31;
        if (this.name != null) {
            i = this.name.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ContactInfo other = (ContactInfo) obj;
        return UriUtils.areEqual(this.lookupUri, other.lookupUri) && TextUtils.equals(this.mCachedLookUpUriString, other.mCachedLookUpUriString) && TextUtils.equals(this.mCachedMatchedNumber, other.mCachedMatchedNumber) && TextUtils.equals(this.name, other.name) && this.type == other.type && TextUtils.equals(this.label, other.label) && TextUtils.equals(this.number, other.number) && TextUtils.equals(this.formattedNumber, other.formattedNumber) && TextUtils.equals(this.normalizedNumber, other.normalizedNumber) && this.photoId == other.photoId && UriUtils.areEqual(this.photoUri, other.photoUri) && this.mIsPrivate == other.mIsPrivate && this.userType == other.userType;
    }
}
