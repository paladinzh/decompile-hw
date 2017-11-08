package com.android.contacts.calllog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.contacts.GeoUtil;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetails.EncryptPhoneCallDetails;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.NumberLocationCache;
import com.android.contacts.compatibility.NumberLocationLoader;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.detail.RcsCallLogDetailHelper;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class CallLogDetailHelper {
    static final String[] CALL_LOG_PROJECTION = new String[(HAP_OR_BASE_CALL_LOG_PROJECTION.length + 1)];
    static String[] HAP_OR_BASE_CALL_LOG_PROJECTION;
    public static final int IS_PRIMARY_INDEX = _PROJECTION.length;
    public static final int LATITUDE_INDEX = (_PROJECTION.length + 3);
    public static final int LONGITUDE_INDEX = (_PROJECTION.length + 2);
    public static final int PICTURE_INDEX = (_PROJECTION.length + 5);
    public static final int POST_CALL_TEXT_INDEX = (_PROJECTION.length + 6);
    public static final int POST_CALL_VOICE_INDEX = (_PROJECTION.length + 7);
    static final int POST_DIAL_DIGITS;
    public static final int RCS_CALL_START_TIME_INDEX = (_PROJECTION.length + 4);
    static final int RING_TIMES;
    public static final int SUBJECT_INDEX = (_PROJECTION.length + 1);
    static final int SUBSCRIPTION = (CALL_LOG_PROJECTION.length - 1);
    private static final String TAG = CallLogDetailHelper.class.getSimpleName();
    static final String[] _PROJECTION = new String[]{"date", "duration", "number", "type", "countryiso", "geocoded_location", "formatted_number", "voicemail_uri", "subscription_component_name", "subscription_id", "presentation", "name", "features", "normalized_number", "is_read", "_id", "transcription", "encrypt_call"};
    static final String[] _PROJECTION_RCS_EXTEND = new String[]{"is_primary", "subject", "longitude", "latitude", "rcs_call_start_time", "picture", "post_call_text", "post_call_voice"};
    private static CallLogDetailHelper instance;
    private static HwCustCallLogDetailHelper mCust;
    private static RcsCallLogDetailHelper mRcs = new RcsCallLogDetailHelper();
    private Context mActivity;
    private ContactInfoHelper mContactInfoHelper;
    private String mDefaultCountryIso;
    private HashMap<String, NumberInfo> mTmpNumberMap = new HashMap();

    private static class NumberInfo {
        private ContactInfo info;
        private boolean isCachedData;
        private boolean isVoiceMailNumber;

        private NumberInfo() {
            this.isCachedData = false;
            this.info = null;
        }
    }

    static {
        mCust = null;
        List<String> projectionList = Lists.newArrayList(_PROJECTION);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            projectionList.addAll(Lists.newArrayList(_PROJECTION_RCS_EXTEND));
        }
        if (EmuiFeatureManager.isRingTimesDisplayEnabled(null)) {
            projectionList.add("ring_times");
            RING_TIMES = projectionList.size() - 1;
        } else {
            RING_TIMES = 0;
        }
        if (CompatUtils.isNCompatible()) {
            projectionList.add("post_dial_digits");
            POST_DIAL_DIGITS = projectionList.size() - 1;
        } else {
            POST_DIAL_DIGITS = 0;
        }
        HAP_OR_BASE_CALL_LOG_PROJECTION = (String[]) projectionList.toArray(new String[projectionList.size()]);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            mCust = (HwCustCallLogDetailHelper) HwCustUtils.createObj(HwCustCallLogDetailHelper.class, new Object[0]);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
        }
        System.arraycopy(HAP_OR_BASE_CALL_LOG_PROJECTION, 0, CALL_LOG_PROJECTION, 0, HAP_OR_BASE_CALL_LOG_PROJECTION.length);
        CALL_LOG_PROJECTION[HAP_OR_BASE_CALL_LOG_PROJECTION.length] = "subscription";
    }

    public static synchronized CallLogDetailHelper getInstance(Context activity) {
        CallLogDetailHelper callLogDetailHelper;
        synchronized (CallLogDetailHelper.class) {
            if (instance == null) {
                instance = new CallLogDetailHelper(activity.getApplicationContext());
            }
            callLogDetailHelper = instance;
        }
        return callLogDetailHelper;
    }

    private CallLogDetailHelper(Context activity) {
        this.mActivity = activity;
        if (activity != null) {
            this.mDefaultCountryIso = GeoUtil.getCurrentCountryIso(activity.getApplicationContext());
            this.mContactInfoHelper = new ContactInfoHelper(activity.getApplicationContext(), this.mDefaultCountryIso);
        }
    }

    public static String[] getCallLogProjection() {
        if (QueryUtil.isSupportDualSim()) {
            String[] ret = new String[CALL_LOG_PROJECTION.length];
            System.arraycopy(CALL_LOG_PROJECTION, 0, ret, 0, CALL_LOG_PROJECTION.length);
            return ret;
        }
        ret = new String[HAP_OR_BASE_CALL_LOG_PROJECTION.length];
        System.arraycopy(HAP_OR_BASE_CALL_LOG_PROJECTION, 0, ret, 0, HAP_OR_BASE_CALL_LOG_PROJECTION.length);
        return ret;
    }

    public void clearNumberInfoCache() {
        this.mTmpNumberMap.clear();
    }

    public PhoneCallDetails getPhoneCallDetailsForUri(Cursor callCursor, PhoneNumberHelper phoneNumberHelper) {
        if (callCursor == null) {
            throw new IllegalArgumentException("Cannot find content: ");
        }
        String postDialDigits;
        boolean isVoiceMailNum;
        ContactInfo info = null;
        String number = callCursor.getString(2);
        if (CompatUtils.isNCompatible()) {
            postDialDigits = callCursor.getString(POST_DIAL_DIGITS);
        } else {
            postDialDigits = "";
        }
        NumberInfo tmpNumberInfo = (NumberInfo) this.mTmpNumberMap.get(number);
        if (tmpNumberInfo == null) {
            NumberInfo numberInfo = new NumberInfo();
            this.mTmpNumberMap.put(number, numberInfo);
        }
        long date = callCursor.getLong(0);
        long duration = callCursor.getLong(1);
        int callType = callCursor.getInt(3);
        String countryIso = callCursor.getString(4);
        int readState = callCursor.getInt(14);
        int columIndex = callCursor.getColumnIndex("presentation");
        int numPresentation = 0;
        if (columIndex != -1) {
            numPresentation = callCursor.getInt(columIndex);
        }
        long callFeature = callCursor.getLong(12);
        CharSequence formattedNumber = callCursor.getString(6);
        if (formattedNumber == null) {
            formattedNumber = PhoneNumberUtils.formatNumber(number);
        }
        int lSubID = getSubId(callType, callCursor);
        if (TextUtils.isEmpty(countryIso)) {
            countryIso = this.mDefaultCountryIso;
        }
        CharSequence nameText = "";
        int numberType = 0;
        CharSequence numberLabel = "";
        Uri photoUri = null;
        Uri lookupUri = null;
        String voiceMailNum = callCursor.getString(7);
        if (tmpNumberInfo.isCachedData) {
            isVoiceMailNum = tmpNumberInfo.isVoiceMailNumber;
        } else {
            isVoiceMailNum = PhoneNumberUtils.isVoiceMailNumber(number);
            tmpNumberInfo.isVoiceMailNumber = isVoiceMailNum;
        }
        if (phoneNumberHelper != null) {
            if (!phoneNumberHelper.canPlaceCallsTo(number, numPresentation)) {
                info = null;
            } else if (!tmpNumberInfo.isCachedData || tmpNumberInfo.info == null) {
                info = this.mContactInfoHelper.lookupNumber(number, countryIso);
                tmpNumberInfo.info = info;
            } else {
                info = tmpNumberInfo.info;
            }
        }
        boolean lIsPrivate = false;
        if (info == null && phoneNumberHelper != null) {
            formattedNumber = phoneNumberHelper.getDisplayNumber(formattedNumber, numPresentation, formattedNumber, postDialDigits, isVoiceMailNum);
            nameText = "";
            numberType = 0;
            numberLabel = "";
            photoUri = null;
            lookupUri = null;
        } else if (info != null) {
            formattedNumber = phoneNumberHelper.getDisplayNumber(info.formattedNumber, numPresentation, info.formattedNumber, postDialDigits, isVoiceMailNum);
            Object nameText2 = info.name;
            numberType = info.type;
            Object numberLabel2 = info.label;
            photoUri = info.photoUri;
            lookupUri = info.lookupUri;
            lIsPrivate = info.mIsPrivate;
        }
        String geocode = "";
        geocode = getFinalGeoCode(callCursor.getString(5), number, countryIso, this.mActivity);
        int ringTimes = 1;
        if (callCursor.getColumnIndex("ring_times") >= 0) {
            ringTimes = callCursor.getInt(RING_TIMES);
        }
        PhoneCallDetails lTempPhoneDetails = new PhoneCallDetails(number, formattedNumber, countryIso, geocode, new int[]{callType}, date, duration, nameText, numberType, numberLabel, lookupUri, photoUri, lSubID, isVoiceMailNum, ringTimes, readState);
        lTempPhoneDetails.setVoiceMailNumber(voiceMailNum);
        lTempPhoneDetails.setIsPrivate(lIsPrivate);
        lTempPhoneDetails.setPresentation(numPresentation);
        lTempPhoneDetails.mId = callCursor.getString(callCursor.getColumnIndex("_id"));
        lTempPhoneDetails.mTranscription = callCursor.getString(callCursor.getColumnIndex("transcription"));
        tmpNumberInfo.isCachedData = true;
        lTempPhoneDetails.setCallsTypeFeatures(callFeature);
        lTempPhoneDetails.mNormalizedNumber = callCursor.getString(13);
        updateEncryptCall(callCursor, lTempPhoneDetails);
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            mRcs.updateRcsInfo(this.mActivity, callCursor, lTempPhoneDetails);
        }
        return lTempPhoneDetails;
    }

    private int getSubId(int callType, Cursor c) {
        int subId = -1;
        if (QueryUtil.isSupportDualSim()) {
            String accountComponentName = c.getString(8);
            String accountId = c.getString(9);
            if (accountComponentName == null || accountId == null) {
                subId = c.getInt(SUBSCRIPTION);
            } else if (callType != 4) {
                try {
                    subId = Integer.parseInt(accountId);
                } catch (NumberFormatException e) {
                    subId = c.getInt(SUBSCRIPTION);
                }
            } else if (this.mActivity != null) {
                List<PhoneAccountHandle> callCapablePhoneAccounts = ((TelecomManager) this.mActivity.getSystemService("telecom")).getCallCapablePhoneAccounts();
                if (callCapablePhoneAccounts.size() > 1) {
                    return (!accountId.equals(((PhoneAccountHandle) callCapablePhoneAccounts.get(0)).getId()) && accountId.equals(((PhoneAccountHandle) callCapablePhoneAccounts.get(1)).getId())) ? 1 : 0;
                }
            }
        }
        return subId;
    }

    public static String getFinalGeoCode(String geoCode, String phoneNum, String countryIso, Context context) {
        String geo = geoCode;
        if (PhoneCapabilityTester.isGeoCodeFeatureEnabled(context) && !QueryUtil.checkGeoLocation(geoCode, phoneNum)) {
            geo = NumberLocationCache.getLocation(phoneNum);
            if (geo == null) {
                geo = NumberLocationLoader.getAndUpdateGeoNumLocation(context, phoneNum);
            }
        }
        if (!TextUtils.isEmpty(geo) || context == null) {
            return geo;
        }
        if (EmuiFeatureManager.isHideUnknownGeo()) {
            return "";
        }
        return context.getResources().getString(R.string.numberLocationUnknownLocation2);
    }

    public static void startRecordPlaybackSafely(Activity context, String absolutePath) {
        Intent localIntent = new Intent();
        localIntent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.RecordListActivity");
        File file = new File(absolutePath);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", file.getAbsolutePath());
        bundle.putString("fileName", file.getName());
        bundle.putString("PlayUri", Uri.fromFile(file).toString());
        bundle.putBoolean("isCallfolder", true);
        localIntent.putExtras(bundle);
        try {
            context.startActivityForResult(localIntent, OfflineMapStatus.EXCEPTION_SDCARD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void release() {
        synchronized (CallLogDetailHelper.class) {
            instance = null;
        }
    }

    public static void deleteRecordSafely(String absolutePath) {
        if (absolutePath != null) {
            File file = new File(absolutePath);
            if (file.exists() && !file.delete() && HwLog.HWDBG) {
                HwLog.i(TAG, "delete record fail !");
            }
        }
    }

    private void updateEncryptCall(Cursor callCursor, PhoneCallDetails lTempPhoneDetails) {
        boolean z = true;
        int encryptCallIndex = callCursor.getColumnIndex("encrypt_call");
        if (encryptCallIndex >= 0) {
            int encryptCall = callCursor.getInt(encryptCallIndex);
            if (lTempPhoneDetails.getEncryptCallStatus() != null) {
                EncryptPhoneCallDetails encryptCallStatus = lTempPhoneDetails.getEncryptCallStatus();
                if (encryptCall != 1) {
                    z = false;
                }
                encryptCallStatus.setEncryptCall(z);
            }
        }
    }
}
