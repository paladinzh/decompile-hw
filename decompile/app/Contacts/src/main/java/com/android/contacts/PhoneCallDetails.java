package com.android.contacts;

import android.database.Cursor;
import android.net.Uri;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.calllog.CallRecord.CallRecordItem;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.huawei.cust.HwCustUtils;

public class PhoneCallDetails {
    private static PhoneCallDetails sEmptyInstance = new PhoneCallDetails();
    public final int[] callTypes;
    public ContactInfo contactInfo;
    public final Uri contactUri;
    public long contactUserType;
    public final String countryIso;
    public final long date;
    public final long duration;
    public final CharSequence formattedNumber;
    public String geocode;
    public final boolean isVoicemailNumber;
    public final String mCachedContactUriString;
    public CallRecordItem[] mCallRecordItems;
    public long mCallTypeFeatures;
    public long mCallsTypeFeatures;
    private HwCustPhoneCallDetails mCust;
    private EncryptPhoneCallDetails mEncryptPhoneCallDetails;
    public String mId;
    public boolean mIsContainInCallData;
    public boolean mIsEmergencyNumber;
    public int mIsPrimary;
    private boolean mIsPrivate;
    public double mLatitude;
    public double mLongitude;
    public String mNormalizedNumber;
    public String mPicturePath;
    public String mPostCallText;
    public String mPostCallVoice;
    private int mPresentation;
    public long mRcsCallStartTime;
    public int mReadState;
    public final int mRingTimes;
    public String mSubject;
    public String mTranscription;
    public CharSequence name;
    public final CharSequence number;
    public final CharSequence numberLabel;
    public NumberMarkInfo numberMark;
    public String numberMarkInfo;
    public final int numberType;
    public final Uri photoUri;
    public String postDialDigits;
    public final int subscriptionID;
    public String voiceMailNumber;

    public static class EncryptPhoneCallDetails {
        private boolean mIsEncryptCall;

        public void setEncryptCall(boolean isEncryptCall) {
            this.mIsEncryptCall = isEncryptCall;
        }

        public boolean isEncryptCall() {
            return this.mIsEncryptCall;
        }
    }

    public HwCustPhoneCallDetails getHwCust() {
        return this.mCust;
    }

    public EncryptPhoneCallDetails getEncryptCallStatus() {
        return this.mEncryptPhoneCallDetails;
    }

    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, boolean isVoicemailNumber, String numberMarkInfo, NumberMarkInfo numberInfo) {
        this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, "", 0, "", null, null, isVoicemailNumber, numberMarkInfo, numberInfo);
    }

    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, CharSequence name, int numberType, CharSequence numberLabel, Uri contactUri, Uri photoUri, boolean isVoicemailNumber, String numberMarkInfo, NumberMarkInfo numberMark) {
        this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, name, numberType, numberLabel, contactUri, photoUri, -1, isVoicemailNumber, 0, numberMarkInfo, numberMark);
    }

    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, int subId, boolean isVoicemailNumber, int aRingTimes, String numberMarkInfo, NumberMarkInfo markInfo, long userType, String postDialDigits) {
        this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, "", 0, "", (Uri) null, null, subId, isVoicemailNumber, true, null, aRingTimes, numberMarkInfo, markInfo, -1, userType, postDialDigits);
    }

    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, CharSequence name, int numberType, CharSequence numberLabel, Uri contactUri, Uri photoUri, int subId, boolean isVoicemailNumber, int aRingTimes, int readState) {
        this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, name, numberType, numberLabel, contactUri, photoUri, subId, isVoicemailNumber, true, contactUri != null ? contactUri.toString() : null, aRingTimes, null, null, readState, 0, "");
    }

    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, CharSequence name, int numberType, CharSequence numberLabel, Uri contactUri, Uri photoUri, int subId, boolean isVoicemailNumber, int aRingTimes, String numberMarkInfo, NumberMarkInfo markInfo) {
        this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, name, numberType, numberLabel, contactUri, photoUri, subId, isVoicemailNumber, true, contactUri != null ? contactUri.toString() : null, aRingTimes, numberMarkInfo, markInfo, -1, 0, "");
    }

    public PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, CharSequence name, int numberType, CharSequence numberLabel, Uri contactUri, Uri photoUri, int subId, boolean isVoicemailNumber, String aCachedContactUriString, int aRingTimes, long userType, String postDialDigits) {
        this(number, formattedNumber, countryIso, geocode, callTypes, date, duration, name, numberType, numberLabel, null, photoUri, subId, isVoicemailNumber, true, aCachedContactUriString, aRingTimes, null, null, -1, userType, postDialDigits);
    }

    private PhoneCallDetails(CharSequence number, CharSequence formattedNumber, String countryIso, String geocode, int[] callTypes, long date, long duration, CharSequence name, int numberType, CharSequence numberLabel, Uri contactUri, Uri photoUri, int subId, boolean isVoicemailNumber, boolean isInner, String aCachedContactUriString, int aRingTimes, String numberMarkInfo, NumberMarkInfo markInfo, int readState, long userType, String postDialDigits) {
        this.mCallRecordItems = null;
        this.mIsPrimary = -1;
        this.mSubject = null;
        this.mPostCallText = null;
        this.mPostCallVoice = null;
        this.mPicturePath = null;
        this.mIsContainInCallData = false;
        this.mCust = (HwCustPhoneCallDetails) HwCustUtils.createObj(HwCustPhoneCallDetails.class, new Object[0]);
        this.mEncryptPhoneCallDetails = new EncryptPhoneCallDetails();
        this.number = number;
        this.formattedNumber = formattedNumber;
        this.countryIso = countryIso;
        this.geocode = geocode;
        this.callTypes = callTypes;
        this.date = date;
        this.duration = duration;
        this.name = name;
        this.numberType = numberType;
        this.numberLabel = numberLabel;
        this.contactUri = contactUri;
        this.photoUri = photoUri;
        this.subscriptionID = subId;
        this.isVoicemailNumber = isVoicemailNumber;
        this.mCachedContactUriString = aCachedContactUriString;
        this.mRingTimes = aRingTimes;
        this.numberMarkInfo = numberMarkInfo;
        this.numberMark = markInfo;
        this.mReadState = readState;
        this.contactUserType = userType;
        this.postDialDigits = postDialDigits;
    }

    private PhoneCallDetails() {
        this.mCallRecordItems = null;
        this.mIsPrimary = -1;
        this.mSubject = null;
        this.mPostCallText = null;
        this.mPostCallVoice = null;
        this.mPicturePath = null;
        this.mIsContainInCallData = false;
        this.mCust = (HwCustPhoneCallDetails) HwCustUtils.createObj(HwCustPhoneCallDetails.class, new Object[0]);
        this.mEncryptPhoneCallDetails = new EncryptPhoneCallDetails();
        this.number = null;
        this.formattedNumber = null;
        this.countryIso = null;
        this.geocode = null;
        this.callTypes = null;
        this.name = null;
        this.numberLabel = null;
        this.contactUri = null;
        this.photoUri = null;
        this.date = -1;
        this.duration = -1;
        this.subscriptionID = -1;
        this.numberType = -1;
        this.isVoicemailNumber = false;
        this.mCachedContactUriString = null;
        this.mRingTimes = 1;
        this.numberMarkInfo = null;
        this.voiceMailNumber = null;
    }

    public String getVoiceMailNumber() {
        return this.voiceMailNumber;
    }

    public void setVoiceMailNumber(String voiceMailNumber) {
        this.voiceMailNumber = voiceMailNumber;
    }

    public static PhoneCallDetails getEmptyInstance() {
        return sEmptyInstance;
    }

    public boolean isEmpty() {
        return this.date == -1 && this.number == null && this.name == null && this.numberLabel == null && this.contactUri == null && this.mCachedContactUriString == null;
    }

    public boolean isPrivate() {
        return this.mIsPrivate;
    }

    public void setIsPrivate(boolean aIsPrivate) {
        this.mIsPrivate = aIsPrivate;
    }

    public void setPresentation(int presentation) {
        this.mPresentation = presentation;
    }

    public int getPresentation() {
        return this.mPresentation;
    }

    public void setCallTypeFeatures(long callTypeFeatures) {
        this.mCallTypeFeatures = callTypeFeatures;
    }

    public long getCallTypeFeatures() {
        return this.mCallTypeFeatures;
    }

    public void setCallsTypeFeatures(long callsTypeFeatures) {
        this.mCallsTypeFeatures = callsTypeFeatures;
    }

    public long getCallsTypeFeatures() {
        return this.mCallsTypeFeatures;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("number:" + this.number);
        sb.append(";formattedNumber:" + this.formattedNumber);
        sb.append(";postDialDigits:" + this.postDialDigits);
        sb.append(";numberlabel:" + this.numberLabel);
        sb.append(";countryIso:" + this.countryIso);
        sb.append(";countryIso:" + this.geocode);
        sb.append(";mCachedContactUriString:" + this.mCachedContactUriString);
        sb.append(";mRingTimes:" + this.mRingTimes);
        sb.append(";numberMarkInfo:" + this.numberMarkInfo);
        sb.append(";numberType:" + this.numberType);
        sb.append(";subscriptionID:" + this.subscriptionID);
        sb.append(";name:" + this.name);
        sb.append(";isPrivate():" + isPrivate());
        sb.append(";contactUri:" + this.contactUri);
        sb.append(";name:" + this.name);
        sb.append(";Presentation:" + getPresentation());
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            sb.append(";mIsPrimary:" + this.mIsPrimary);
            sb.append(";mSubject:" + this.mSubject);
            sb.append(";mPostCallText:" + this.mPostCallText);
            sb.append(";mPostCallVoice:" + this.mPostCallVoice);
            sb.append(";mLongitude:" + this.mLongitude);
            sb.append(";mLatitude:" + this.mLatitude);
            sb.append(";mRcsCallStartTime:" + this.mRcsCallStartTime);
            sb.append(";mPicturePath:" + this.mPicturePath);
        }
        return sb.toString();
    }

    public void setRcsInfo(int isPrimary, String subject, String postCallText, String postCallVoice, double longitude, double latitude, long rcsCallStartTime, String picturePath) {
        this.mIsPrimary = isPrimary;
        this.mSubject = subject;
        this.mPostCallText = postCallText;
        this.mPostCallVoice = postCallVoice;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mRcsCallStartTime = rcsCallStartTime;
        this.mPicturePath = picturePath;
    }

    public void addSubject(Cursor cursor) {
        this.mIsPrimary = -1;
        this.mSubject = null;
        this.mIsPrimary = cursor.getInt(CallLogQuery.IS_PRIMARY);
        this.mSubject = cursor.getString(CallLogQuery.SUBJECT);
    }
}
