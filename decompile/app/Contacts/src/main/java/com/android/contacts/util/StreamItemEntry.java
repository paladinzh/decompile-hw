package com.android.contacts.util;

import com.android.contacts.test.NeededForTesting;
import java.util.ArrayList;
import java.util.List;

public class StreamItemEntry implements Comparable<StreamItemEntry> {
    private final String mAccountName;
    private final String mAccountType;
    private final String mComments;
    private final String mDataSet;
    private boolean mDecoded;
    private CharSequence mDecodedComments;
    private CharSequence mDecodedText;
    private final String mIconRes;
    private final long mId;
    private final String mLabelRes;
    private List<StreamItemPhotoEntry> mPhotos = new ArrayList();
    private final String mResPackage;
    private final String mText;
    private final long mTimestamp;

    @NeededForTesting
    public static StreamItemEntry createForTest(long id, String text, String comments, long timestamp, String accountType, String accountName, String dataSet, String resPackage, String iconRes, String labelRes) {
        return new StreamItemEntry(id, text, comments, timestamp, accountType, accountName, dataSet, resPackage, iconRes, labelRes);
    }

    private StreamItemEntry(long id, String text, String comments, long timestamp, String accountType, String accountName, String dataSet, String resPackage, String iconRes, String labelRes) {
        this.mId = id;
        this.mText = text;
        this.mComments = comments;
        this.mTimestamp = timestamp;
        this.mAccountType = accountType;
        this.mAccountName = accountName;
        this.mDataSet = dataSet;
        this.mResPackage = resPackage;
        this.mIconRes = iconRes;
        this.mLabelRes = labelRes;
    }

    public int compareTo(StreamItemEntry other) {
        if (this.mTimestamp == other.mTimestamp) {
            return 0;
        }
        return this.mTimestamp > other.mTimestamp ? -1 : 1;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public String getResPackage() {
        return this.mResPackage;
    }

    public String getLabelRes() {
        return this.mLabelRes;
    }

    public CharSequence getDecodedText() {
        checkDecoded();
        return this.mDecodedText;
    }

    public CharSequence getDecodedComments() {
        checkDecoded();
        return this.mDecodedComments;
    }

    private void checkDecoded() {
        if (!this.mDecoded) {
            throw new IllegalStateException("decodeHtml must have been called");
        }
    }
}
