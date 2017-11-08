package com.android.mms;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.provider.Telephony.Sms;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Xml;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.data.Contact;
import com.android.mms.ui.AdvancedPreferenceFragment;
import com.android.mms.ui.GeneralPreferenceFragment;
import com.android.mms.ui.HwCustPreferenceUtils;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.SignatureUtil;
import com.android.rcs.RcsMmsRcsConfig;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.csp.util.MmsInfo;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwCustHwMessageUtils;
import com.huawei.mms.util.HwSpecialUtils;
import com.huawei.mms.util.Log;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MmsConfig {
    public static final Uri SYSTEMEX_URI = System.CONTENT_URI;
    private static boolean bStrictAddrCharSet = false;
    private static boolean enableAFW = true;
    private static boolean enableCryptoSms = false;
    private static Map initMmsConfigMap = new HashMap();
    private static boolean isShowSignatureDialog = true;
    private static boolean mAliasEnabled = false;
    private static int mAliasRuleMaxChars = 48;
    private static int mAliasRuleMinChars = 2;
    private static boolean mAllowAttachAudio = true;
    private static boolean mAutoRetrievalSingleCardPref = false;
    private static String mChar7bitVenezuela = null;
    private static String mChar_7bit = null;
    private static boolean mCheckEmailPopup = true;
    private static boolean mClosePopupMsgOption = true;
    private static boolean mCoexistWithMms = false;
    private static Context mContext;
    private static boolean mCreationModeEnabled = false;
    private static String mCustMccFor7bitMatchMap = "734";
    private static int mCustomDefaultSlideDuration = -1;
    private static boolean mDefault7bitOptionValue = false;
    private static int mDefaultAlwaysAllowMms = 1;
    private static boolean mDefaultAutoDeleteMessages = false;
    private static int mDefaultAutoRetrievalMms = 1;
    private static boolean mDefaultGroupMessage = false;
    private static boolean mDefaultMMSAutoReplyReadReports = false;
    private static boolean mDefaultMMSAutoRetrieval = true;
    private static boolean mDefaultMMSDeliveryReports = false;
    private static int mDefaultMMSMessagesPerThread = 50;
    private static boolean mDefaultMMSReadReports = false;
    private static boolean mDefaultMMSRetrievalDuringRoaming = false;
    private static boolean mDefaultMMSRetrievalDuringRoamingCard1 = false;
    private static boolean mDefaultMMSRetrievalDuringRoamingCard2 = false;
    private static boolean mDefaultMMSSendDeliveryReports = true;
    private static String mDefaultNotificationRingtone = "Microwave Oven";
    private static boolean mDefaultSMSDeliveryReports = false;
    private static int mDefaultSMSMessagesPerThread = VTMCDataCache.MAXSIZE;
    private static String mDefaultSignatureText = "";
    private static boolean mDefaultVibrateWhenNotification = true;
    private static boolean mDisplayModePref = true;
    private static boolean mDisplaySentTime = false;
    private static String mEmailGateway = null;
    private static boolean mEnableAutoDelete = false;
    private static boolean mEnableCBS = true;
    private static boolean mEnableCNAddress = true;
    private static boolean mEnableCancelAutoRetrieve = false;
    private static boolean mEnableChangeClassZeroMessageShow = false;
    private static int mEnableDeliveryReportState = 0;
    private static boolean mEnableEmailSpanAsMmsRecipient = true;
    private static boolean mEnableForwardMessageFrom = true;
    private static boolean mEnableGroupMms = false;
    private static boolean mEnableHarassment = true;
    private static boolean mEnableMMSAutoReplyReadReports = true;
    private static boolean mEnableMMSDeliveryReports = true;
    private static boolean mEnableMMSReadReports = true;
    private static boolean mEnableMemoryStatus = true;
    private static boolean mEnableMmsCustomizedUA = false;
    private static boolean mEnableMmsDRInRussia = true;
    private static boolean mEnableMmsVcard = true;
    private static boolean mEnableModifySMSCenterNumber = true;
    private static boolean mEnableMultiReciepentSingleView = false;
    private static boolean mEnableMultipartSMS = false;
    private static boolean mEnableOptionalRingtone = true;
    private static boolean mEnablePhoneAttribute = true;
    private static boolean mEnablePopupMsg = false;
    private static boolean mEnableSMSDeliveryReports = true;
    private static boolean mEnableSendMMSDeliveryReports = true;
    private static int mEnableSendMmsNumMinLength = 0;
    private static boolean mEnableSendVcalByMms = false;
    private static boolean mEnableSendingBlankSMS = false;
    private static boolean mEnableShortPhoneNumberLink = false;
    private static boolean mEnableShowMmsLog = true;
    private static boolean mEnableShowSMSCenterNumber = true;
    private static boolean mEnableShowTotalCount = false;
    private static boolean mEnableSlideDuration = true;
    private static boolean mEnableSlideShowforSingleMedia = true;
    private static boolean mEnableSmartAchive = true;
    private static boolean mEnableSmsRecyclerFeature = false;
    private static boolean mEnableSmsTextCounterShow = true;
    private static boolean mEnableSpecialSMS = true;
    private static boolean mEnableSplitNumberAndEmailRecipients = false;
    private static boolean mEnableTimeParse = true;
    private static boolean mEnableWapSenderAddress = true;
    private static boolean mEnableWappushReplace = false;
    private static boolean mEnableWhatsAppMenu = SystemProperties.getBoolean("ro.config.hw_mms_whatsapp", false);
    private static boolean mEnabletMmsParamsFromGlobal = false;
    private static boolean mForbiddenSetFrom = false;
    private static int mForwardLimitSize = 100;
    private static boolean mGcfMms305Enabled = true;
    private static boolean mGcfMmsTest = SystemProperties.getBoolean("ro.config.mms_file_upper_cmp", false);
    private static int mHWMmsRetry = 1;
    private static boolean mHas7BitAlaphsetInHwDefaults = false;
    private static String mHttpParams = null;
    private static String mHttpParamsLine1Key = null;
    private static int mHttpSocketTimeout = 120000;
    private static RcsMmsRcsConfig mHwCust = new RcsMmsRcsConfig();
    private static HwCustHwMessageUtils mHwCustMessageUtils = ((HwCustHwMessageUtils) HwCustUtils.createObj(HwCustHwMessageUtils.class, new Object[0]));
    private static HwCustMmsConfig mHwCustMmsConfig = ((HwCustMmsConfig) HwCustUtils.createObj(HwCustMmsConfig.class, new Object[0]));
    private static boolean mIsFiltSubject = true;
    private static boolean mIsRefreshRxNum = false;
    private static boolean mIsRenameAttachmentName = true;
    private static boolean mIsShowAttachmentSize = true;
    private static boolean mIsSimpleUi;
    private static int mMaxCacheMsgNum = 5;
    private static int mMaxImageHeight = 480;
    private static int mMaxImageWidth = 640;
    private static int mMaxMessageCountPerThread = 5000;
    private static int mMaxMessageSize = 307200;
    private static int mMaxRecentContacts = 20;
    private static int mMaxRestrictedImageHeight = 480;
    private static int mMaxRestrictedImageWidth = 640;
    private static int mMaxSizeScaleForPendingMmsAllowed = 4;
    private static int mMaxSlides = 20;
    private static int mMaxSubjectLength = 40;
    private static int mMaxTextLength = -1;
    private static int mMinMessageCountPerThread = 10;
    private static int mMinimumSlideElementDuration = 7;
    private static Map mMmsConfigMap = new HashMap();
    private static int mMmsDownloadAvailableSpaceLimit = 1048576;
    private static int mMmsEnabled = 1;
    private static int mMmsExpiry = 604800;
    private static boolean mMmsExpiryMaxEnable = false;
    private static boolean mMmsExpiryModifyEnable = false;
    private static boolean mModifySMSCenterAddressOnCard = true;
    private static boolean mMuilCardAutoRetrievalEbable = false;
    private static boolean mMutisimDislpayModel = true;
    private static ArrayList<String> mNorthEastEuropeMccList = new ArrayList();
    private static boolean mNotifyWapMMSC = false;
    private static String mPrefPlaymode = "slidesmoothshowactivity";
    private static int mRecipientLimit = 1000;
    private static boolean mRepyAllEnabled = false;
    private static boolean mRingWhenOnTalk = true;
    private static String mRussiaMccBlankSMS = null;
    private static String mSMSCAddress = null;
    private static boolean mSMSDeliveryRptMultiCardPerf = false;
    private static boolean mSaveModeMultiCardPerf = false;
    private static boolean mSaveModePref = false;
    private static boolean mShowMMSReadReports = false;
    private static boolean mShowMmsReadReportDialog = false;
    private static boolean mSms7BitEnabled = false;
    private static int mSmsAllCharTo7Bit = -1;
    private static boolean mSmsOptimizationCharacters = false;
    private static int mSmsToMmsTextThreshold = 11;
    private static boolean mSupportCTInGsmMode = false;
    private static boolean mSupportSmartSmsFeature = false;
    private static boolean mSupportVCalendarMode = true;
    private static boolean mTransIdEnabled = false;
    private static String mUaProfTagName = "x-wap-profile";
    private static String mUaProfUrl = "http://www.google.com/oha/rdf/ua-profile-kila.xml";
    private static boolean mUseGgSmsAddressCheck = true;
    private static String mUserAgent = "Android-Mms/2.0";
    private static int mWAPPushSupported = 1;
    private static boolean mWapPushSettingEnabled = true;
    private static boolean mZoomWhenView = true;
    private static NumberFilter msfilter = null;
    private static SharedPreferences prefs;
    private static float sPressureThreshold = 0.25f;

    private static class NumberFilter {
        private NumberFilter() {
        }

        public String filteNumberByLocal(String number) {
            return number;
        }
    }

    private static class NumberFilterCn extends NumberFilter {
        private NumberFilterCn() {
            super();
        }

        public String filteNumberByLocal(String number) {
            if (TextUtils.isEmpty(number)) {
                return number;
            }
            if (!Contact.isEmailAddress(number)) {
                number = number.replace("-", "");
            }
            return number.replace(" ", "");
        }
    }

    public static String getSMSCAddress() {
        return mSMSCAddress;
    }

    public static String getCustMccFor7bitMatchMap() {
        return mCustMccFor7bitMatchMap;
    }

    public static String getDefaultSignatureText() {
        return mDefaultSignatureText;
    }

    public static final boolean isSupportCtInGsmMode() {
        return mSupportCTInGsmMode;
    }

    public static boolean getEnableSendVcalByMms() {
        return mEnableSendVcalByMms;
    }

    public static int getMaxSlides() {
        return mMaxSlides;
    }

    public static boolean compareWithMaxSlides(int compareSize) {
        return compareSize >= mMaxSlides;
    }

    public static boolean isFoldeModeEnabled() {
        return false;
    }

    public static boolean isSaveModeEnabled() {
        return mSaveModePref;
    }

    public static boolean readMmsConfigXML(Context context, String path) {
        XmlPullParserException e;
        Throwable th;
        Log.logPerformance("MmsConfig.readMmsConfigXML()");
        XmlPullParser xmlPullParser = null;
        InputStream inputStream = null;
        try {
            InputStream in = new FileInputStream(path);
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(in, null);
                loadMmsSettings(context, xmlPullParser);
                MLog.v("MmsConfig", switchXmlLogPath(path));
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        inputStream = in;
                    } catch (XmlPullParserException e3) {
                        inputStream = in;
                        MLog.e("MmsConfig", "load " + path + " MmsSettings XmlPullParserException ");
                        return true;
                    }
                }
                inputStream = in;
                if (xmlPullParser != null) {
                    try {
                        if (!XmlResourceParser.class.isInstance(xmlPullParser)) {
                            xmlPullParser.setInput(null);
                        }
                    } catch (IOException e4) {
                    } catch (XmlPullParserException e5) {
                        MLog.e("MmsConfig", "load " + path + " MmsSettings XmlPullParserException ");
                    }
                }
                return true;
            } catch (FileNotFoundException e6) {
                inputStream = in;
                MLog.e("MmsConfig", "load " + path + " MmsSettings caught FileNotFoundException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        return false;
                    } catch (XmlPullParserException e8) {
                        MLog.e("MmsConfig", "load " + path + " MmsSettings XmlPullParserException ");
                        return false;
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    xmlPullParser.setInput(null);
                }
                return false;
            } catch (XmlPullParserException e9) {
                e = e9;
                inputStream = in;
                try {
                    MLog.e("MmsConfig", "load " + path + " MmsSettings caught XmlPullParserException");
                    MLog.e("MmsConfig", "load " + path + " MmsSettings caught ", (Throwable) e);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e10) {
                            return false;
                        } catch (XmlPullParserException e11) {
                            MLog.e("MmsConfig", "load " + path + " MmsSettings XmlPullParserException ");
                            return false;
                        }
                    }
                    if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                        xmlPullParser.setInput(null);
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e12) {
                        } catch (XmlPullParserException e13) {
                            MLog.e("MmsConfig", "load " + path + " MmsSettings XmlPullParserException ");
                        }
                    }
                    xmlPullParser.setInput(null);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    xmlPullParser.setInput(null);
                }
                throw th;
            }
        } catch (FileNotFoundException e14) {
            MLog.e("MmsConfig", "load " + path + " MmsSettings caught FileNotFoundException");
            if (inputStream != null) {
                inputStream.close();
            }
            xmlPullParser.setInput(null);
            return false;
        } catch (XmlPullParserException e15) {
            e = e15;
            MLog.e("MmsConfig", "load " + path + " MmsSettings caught XmlPullParserException");
            MLog.e("MmsConfig", "load " + path + " MmsSettings caught ", (Throwable) e);
            if (inputStream != null) {
                inputStream.close();
            }
            xmlPullParser.setInput(null);
            return false;
        }
    }

    public static void init(Context context) {
        Log.logPerformance("MmsConfig.init()");
        mContext = context.getApplicationContext();
        MLog.v("MmsConfig", "mnc/mcc: " + SystemProperties.get("gsm.sim.operator.numeric"));
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = false;
        Iterable cfgFileList = null;
        try {
            cfgFileList = HwCfgFilePolicy.getCfgFileList("xml/mms_config.xml", 0);
        } catch (NoClassDefFoundError e) {
            MLog.e("MmsConfig", "class HwCfgFilePolicy not found error");
        } catch (Exception e2) {
            MLog.e("MmsConfig", "class HwCfgFilePolicy exception");
        }
        if (r2 == null || r2.size() == 0) {
            result = readMmsConfigXML(context, "/system/etc/xml/mms_config.xml");
            if (readMmsConfigXML(context, "data/cust/xml/mms_config.xml")) {
                result = true;
            }
        } else {
            for (File cfg : r2) {
                if (readMmsConfigXML(context, cfg.getPath())) {
                    result = true;
                }
            }
        }
        if (!result) {
            MLog.v("MmsConfig", "system/etc and cust do not exist the xml file!!");
        }
        if (mHwCustMessageUtils != null && mHwCustMessageUtils.getEnableCotaFeature()) {
            MLog.v("MmsConfig", "come into process cota feature");
            mHwCustMessageUtils.processCotaAtlXml(context);
            mHwCustMessageUtils.processCotaBtlXml(context);
        }
        processRegionalPhoneXmls(context);
        Log.logPerformance("MmsConfig init() finish");
        if (mEnablePopupMsg && !mCoexistWithMms) {
            String packagename = "com.huawei.floatMms";
            try {
                context.getPackageManager().getPackageInfo(packagename, 0);
            } catch (NameNotFoundException e3) {
                MLog.w("MmsConfig", "turning off EnablePopupMessage due to not exist: " + packagename + "");
                mEnablePopupMsg = false;
            }
        }
        String ringToneUri = getRingToneUriFromDatabase(context, "message");
        if (ringToneUri != null && ringToneUri.contains(";")) {
            String[] ringToneUris = ringToneUri.split(";");
            setRingToneUriToDatabase(context, ringToneUris[0], 0);
            setRingToneUriToDatabase(context, ringToneUris[1], 1);
        }
        SmartArchiveSettingUtils.initSmartArcihivSettings(context);
    }

    public static void processRegionalPhoneXmls(Context context) {
        if (mHwCustMessageUtils != null) {
            mHwCustMessageUtils.backupMmsConfigXml(context);
        }
        if (mHwCustMessageUtils != null && mHwCustMessageUtils.isReginalPhoneActivated(context)) {
            mHwCustMessageUtils.processRegionalPhoneXmls(context);
        }
    }

    public static boolean isSmsDefaultApp(Context context) {
        String defaultSmsApplication;
        if (!OsUtil.isAppStart() || HwBackgroundLoader.getDefaultSmsApp().getDefaultApp() == null) {
            defaultSmsApplication = Sms.getDefaultSmsPackage(context);
        } else {
            defaultSmsApplication = HwBackgroundLoader.getDefaultSmsApp().getDefaultApp().toString();
        }
        if (isCspVersion()) {
            return "com.android.contacts".equalsIgnoreCase(defaultSmsApplication);
        }
        return "com.android.mms".equalsIgnoreCase(defaultSmsApplication);
    }

    public static boolean isSmsEnabled(Context context) {
        return true;
    }

    public static boolean isSmsPromoDismissed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sms_promo_dismissed_key", false);
    }

    public static boolean getSmsOptimizationCharacters() {
        return mSmsOptimizationCharacters;
    }

    public static boolean getDefault7bitOptionValue() {
        return mDefault7bitOptionValue;
    }

    public static boolean isLossless7bit() {
        if (System.getInt(MmsApp.getApplication().getContentResolver(), "sms_coding_national_backup", 0) != 0) {
            return true;
        }
        return false;
    }

    public static void setDefault7bitOptionValue() {
        boolean sms7BitEnabled = false;
        if (isSms7BitEabled()) {
            sms7BitEnabled = true;
        } else if (MmsApp.getDefaultTelephonyManager() != null) {
            try {
                Method get7BitEnabled = TelephonyManager.getDefault().getClass().getDeclaredMethod("isSms7BitEnabled", new Class[0]);
                get7BitEnabled.setAccessible(true);
                sms7BitEnabled = ((Boolean) get7BitEnabled.invoke(MmsApp.getDefaultTelephonyManager(), new Object[0])).booleanValue();
            } catch (RuntimeException e) {
                MLog.v("MmsConfig", "No function to isSms7BitEnabled");
            } catch (Exception e2) {
                MLog.e("MmsConfig", "No function to isSms7BitEnabled");
            }
        }
        if (isLossless7bit()) {
            sms7BitEnabled = true;
        }
        if (sms7BitEnabled) {
            mDefault7bitOptionValue = true;
        } else {
            mDefault7bitOptionValue = false;
        }
        MLog.e("MmsConfig", "mDefault7bitOptionValue =" + mDefault7bitOptionValue);
    }

    public static boolean isModifySMSCenterAddressOnCard() {
        return mModifySMSCenterAddressOnCard;
    }

    public static Intent getRequestDefaultSmsAppActivity() {
        Intent intent = new Intent("android.provider.Telephony.ACTION_CHANGE_DEFAULT");
        intent.putExtra("package", MmsApp.getApplication().getPackageName());
        return intent;
    }

    public static boolean getReplyAllEnabled() {
        return mRepyAllEnabled;
    }

    public static int getSmsToMmsTextThreshold() {
        return mSmsToMmsTextThreshold;
    }

    public static int getDefaultAutoRetrievalMms() {
        if (mHwCustMmsConfig != null) {
            mDefaultAutoRetrievalMms = mHwCustMmsConfig.getMccMncMmsRetrive(mDefaultAutoRetrievalMms);
        }
        return mDefaultAutoRetrievalMms;
    }

    public static int getDefaultAlwaysAllowMms() {
        return mDefaultAlwaysAllowMms;
    }

    public static void setSmsToMmsTextThreshold(int threshold) {
        mSmsToMmsTextThreshold = threshold;
    }

    public static void setMaxMessageSize(int maxSize) {
        mMaxMessageSize = maxSize;
    }

    public static void setMultipartSmsEnabled(boolean enable) {
        mEnableMultipartSMS = enable;
    }

    public static boolean isEnabletMmsParamsFromGlobal() {
        return mEnabletMmsParamsFromGlobal;
    }

    public static boolean getMmsEnabled() {
        return mMmsEnabled == 1;
    }

    public static void setMmsEnabled(int isMmsEnabled) {
        mMmsEnabled = isMmsEnabled;
    }

    public static boolean isFiltSubject() {
        return mIsFiltSubject;
    }

    public static int getMaxMessageSize() {
        return mMaxMessageSize;
    }

    public static int getMaxMessageSizeCharge(int chargeSize) {
        return mMaxMessageSize - chargeSize;
    }

    public static boolean getTransIdEnabled() {
        return mTransIdEnabled;
    }

    public static String getUserAgent() {
        return mUserAgent;
    }

    public static boolean getEnableMmsCustomizedUA() {
        return mEnableMmsCustomizedUA;
    }

    public static String getUaProfTagName() {
        return mUaProfTagName;
    }

    public static String getUaProfUrl() {
        return mUaProfUrl;
    }

    public static String getHttpParams() {
        return mHttpParams;
    }

    public static String getHttpParamsLine1Key() {
        return mHttpParamsLine1Key;
    }

    public static String getEmailGateway() {
        return mEmailGateway;
    }

    public static String getPrefPlaymode() {
        return mPrefPlaymode;
    }

    public static int getMaxImageHeight() {
        return mMaxImageHeight;
    }

    public static int getMaxImageWidth() {
        return mMaxImageWidth;
    }

    public static int getMaxRestrictedImageHeight() {
        return mMaxRestrictedImageHeight;
    }

    public static int getMaxRestrictedImageWidth() {
        return mMaxRestrictedImageWidth;
    }

    public static boolean getCreationModeEnabled() {
        return mCreationModeEnabled;
    }

    public static boolean isGcfMms305Enabled() {
        return mGcfMms305Enabled;
    }

    public static int getRecipientLimit() {
        return mRecipientLimit;
    }

    public static void setRecipientLimit(int recipientLimit) {
        mRecipientLimit = recipientLimit;
    }

    public static int getMaxTextLimit() {
        return mMaxTextLength > -1 ? mMaxTextLength : 5000;
    }

    public static int getDefaultSMSMessagesPerThread() {
        return mDefaultSMSMessagesPerThread;
    }

    public static int getDefaultMMSMessagesPerThread() {
        return mDefaultMMSMessagesPerThread;
    }

    public static int getMinMessageCountPerThread() {
        return mMinMessageCountPerThread;
    }

    public static int getMaxMessageCountPerThread() {
        return mMaxMessageCountPerThread;
    }

    public static int getHttpSocketTimeout() {
        return mHttpSocketTimeout;
    }

    public static int getMinimumSlideElementDuration() {
        return mMinimumSlideElementDuration;
    }

    public static boolean getMultipartSmsEnabled() {
        return mEnableMultipartSMS;
    }

    public static boolean getSlideDurationEnabled() {
        return mEnableSlideDuration;
    }

    public static boolean getMMSReadReportsEnabled() {
        return mEnableMMSReadReports;
    }

    public static boolean isShowMMSReadReports() {
        return mShowMMSReadReports;
    }

    public static boolean getMMSSendDeliveryReportsEnabled() {
        return mEnableSendMMSDeliveryReports;
    }

    public static boolean getDefaultMMSSendDeliveryReports() {
        return mDefaultMMSSendDeliveryReports;
    }

    public static boolean getSMSDeliveryReportsEnabled() {
        return mEnableSMSDeliveryReports;
    }

    public static boolean getMMSDeliveryReportsEnabled() {
        return mEnableMMSDeliveryReports;
    }

    public static boolean getNotifyWapMMSC() {
        return mNotifyWapMMSC;
    }

    public static int getMaxSizeScaleForPendingMmsAllowed() {
        return mMaxSizeScaleForPendingMmsAllowed;
    }

    public static boolean isAliasEnabled() {
        return mAliasEnabled;
    }

    public static int getAliasMinChars() {
        return mAliasRuleMinChars;
    }

    public static int getAliasMaxChars() {
        return mAliasRuleMaxChars;
    }

    public static boolean getAllowAttachAudio() {
        return mAllowAttachAudio;
    }

    public static int getMaxRecentContactsCount() {
        return mMaxRecentContacts;
    }

    public static int getMaxSubjectLength() {
        return mMaxSubjectLength;
    }

    public static int getMmsExpiry(Context mContext) {
        String expireVal = PreferenceManager.getDefaultSharedPreferences(mContext).getString("expireValue", "");
        if (mMmsExpiryMaxEnable) {
            if (expireVal != null && "2".equals(expireVal)) {
                mMmsExpiry = 172800;
            } else if (expireVal == null || !"7".equals(expireVal)) {
                mMmsExpiry = 0;
            } else {
                mMmsExpiry = 604800;
            }
        } else if (mMmsExpiryModifyEnable) {
            if (expireVal != null && "1".equals(expireVal)) {
                mMmsExpiry = 86400;
            } else if (expireVal == null || !"2".equals(expireVal)) {
                mMmsExpiry = 259200;
            } else {
                mMmsExpiry = 172800;
            }
        } else if (!(mHwCustMmsConfig == null || TextUtils.isEmpty(getMmsStringConfig("custMccMncForMmsExpiry")))) {
            mMmsExpiry = mHwCustMmsConfig.getMccMncMmsExpiry(mMmsExpiry);
        }
        MLog.i("TAG", "MmsExpiry  :" + mMmsExpiry);
        return mMmsExpiry;
    }

    public static boolean isMmsExpiryModifyEnable() {
        return false;
    }

    public static boolean getEnableWapSenderAddress() {
        return mEnableWapSenderAddress;
    }

    public static boolean getMultiRecipientSingleViewEnabled() {
        return mEnableMultiReciepentSingleView;
    }

    public static boolean isMmsExpiryMaxEnable() {
        return false;
    }

    public static boolean getGroupMmsEnabled() {
        return mEnableGroupMms;
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new XmlPullParserException("No start tag found");
        } else if (parser.getName() == null || !parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                return;
            }
        } while (type != 1);
    }

    private static void loadMmsSettings(Context context, XmlPullParser parser) {
        try {
            beginDocument(parser, "mms_config");
            while (true) {
                nextElement(parser);
                String tag = parser.getName();
                if (tag == null) {
                    break;
                }
                String name = parser.getAttributeName(0);
                String value = parser.getAttributeValue(0);
                String text = null;
                if (parser.next() == 4) {
                    text = parser.getText();
                }
                if (isStringEqual("name", name)) {
                    if (isStringEqual("bool", tag)) {
                        if (isStringEqual("enabledMMS", value)) {
                            int i;
                            if ("true".equalsIgnoreCase(text)) {
                                i = 1;
                            } else {
                                i = 0;
                            }
                            mMmsEnabled = i;
                        } else if (isStringEqual("enabledTransID", value)) {
                            mTransIdEnabled = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enabledNotifyWapMMSC", value)) {
                            mNotifyWapMMSC = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("aliasEnabled", value)) {
                            mAliasEnabled = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("modifySMSCenterAddressOnCard", value)) {
                            mModifySMSCenterAddressOnCard = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("ForbiddenSetFrom", value)) {
                            mForbiddenSetFrom = "true".equalsIgnoreCase(text);
                            MLog.d("MmsConfig", "mForbiddenSetForm is evaluated by equalsIgnoreCase(text) to " + mForbiddenSetFrom);
                        } else if (isStringEqual("allowAttachAudio", value)) {
                            mAllowAttachAudio = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSpecialSMS", value)) {
                            mEnableSpecialSMS = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("gcfMms305Enabled", value)) {
                            mGcfMms305Enabled = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("sms7BitEnabled", value)) {
                            mSms7BitEnabled = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableVCalendarSupport", value)) {
                            mSupportVCalendarMode = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMultipartSMS", value)) {
                            mEnableMultipartSMS = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSlideDuration", value)) {
                            mEnableSlideDuration = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("showMMSReadReports", value)) {
                            mShowMMSReadReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMMSReadReports", value)) {
                            mEnableMMSReadReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSendMMSDeliveryReports", value)) {
                            mEnableSendMMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultMMSSendDeliveryReports", value)) {
                            mDefaultMMSSendDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSMSDeliveryReports", value)) {
                            mEnableSMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMMSDeliveryReports", value)) {
                            mEnableMMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultAutoDeleteMessages", value)) {
                            mDefaultAutoDeleteMessages = "true".equalsIgnoreCase(text);
                        } else if ("mmsReadReportChecked".compareTo(value) == 0) {
                            mDefaultMMSReadReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultSMSDeliveryReports", value)) {
                            mDefaultSMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultMMSDeliveryReports", value)) {
                            mDefaultMMSDeliveryReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defAutoRetrievalNotEnable", value)) {
                            mDefaultMMSAutoRetrieval = !"true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultMMSRetrievalDuringRoaming", value)) {
                            mDefaultMMSRetrievalDuringRoaming = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultMMSRetrievalDuringRoamingCard1", value)) {
                            mDefaultMMSRetrievalDuringRoamingCard1 = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultMMSRetrievalDuringRoamingCard2", value)) {
                            mDefaultMMSRetrievalDuringRoamingCard2 = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultVibrateWhenNotification", value)) {
                            mDefaultVibrateWhenNotification = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("modifySMSCenterNumber", value)) {
                            mEnableModifySMSCenterNumber = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSMSCenterNumber", value)) {
                            mEnableShowSMSCenterNumber = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableStrictAddrCharSet", value)) {
                            bStrictAddrCharSet = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableFolderMode", value)) {
                            mDisplayModePref = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSaveMode", value)) {
                            mSaveModePref = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMmsDRInRussia", value)) {
                            mEnableMmsDRInRussia = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableEmailSpanAsMmsRecipient", value)) {
                            mEnableEmailSpanAsMmsRecipient = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableShortPhoneNumberLink", value)) {
                            mEnableShortPhoneNumberLink = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSplitNumberAndEmailRecipients", value)) {
                            mEnableSplitNumberAndEmailRecipients = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableOptionalRingtone", value)) {
                            mEnableOptionalRingtone = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMMSAutoReplyReadReports", value)) {
                            mEnableMMSAutoReplyReadReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultAutoReplyReadReports", value)) {
                            mDefaultMMSAutoReplyReadReports = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isShowMmsReadReportDialog", value)) {
                            mShowMmsReadReportDialog = "true".equalsIgnoreCase(text);
                        } else if ("smsOptimizationCharacters".equalsIgnoreCase(value)) {
                            mSmsOptimizationCharacters = "true".equalsIgnoreCase(text);
                        } else if ("enableReplyAll".equalsIgnoreCase(value)) {
                            mRepyAllEnabled = "true".equalsIgnoreCase(text);
                        } else if ("enableWappushReplace".equalsIgnoreCase(value)) {
                            mEnableWappushReplace = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSendingBlankSMS", value)) {
                            mEnableSendingBlankSMS = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMmsVcard", value)) {
                            mEnableMmsVcard = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("showTatalConut", value)) {
                            mEnableShowTotalCount = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isUseGgSmsAddressCheck", value)) {
                            mUseGgSmsAddressCheck = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableGroupMms", value)) {
                            mEnableGroupMms = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("defaultGroupMessage", value)) {
                            mDefaultGroupMessage = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableCellBroadCast", value)) {
                            mEnableCBS = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isRingWhenOnTalk", value)) {
                            mRingWhenOnTalk = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMmsCustomizedUA", value)) {
                            mEnableMmsCustomizedUA = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isRenameAttachmentName", value)) {
                            mIsRenameAttachmentName = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMemoryStatus", value)) {
                            mEnableMemoryStatus = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enablePhoneAtrribute", value)) {
                            mEnablePhoneAttribute = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableZoomWhenView", value)) {
                            mZoomWhenView = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("WapPushSettingEnabled", value)) {
                            mWapPushSettingEnabled = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("EnablePopupMessage", value)) {
                            MLog.w("MmsConfig", "Message popup window not support anymore!!");
                        } else if (isStringEqual("coexistWithFloatMms", value)) {
                            mCoexistWithMms = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("smsTextCounterShow", value)) {
                            mEnableSmsTextCounterShow = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableWapSenderAddress", value)) {
                            mEnableWapSenderAddress = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("MutiSimDisplayModel", value)) {
                            mMutisimDislpayModel = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isShowAttachmentSize", value)) {
                            mIsShowAttachmentSize = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isShowSignature", value)) {
                            isShowSignatureDialog = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("mmsExpiryModifyEnable", value)) {
                            mMmsExpiryModifyEnable = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSlideshowforsinglemedia", value)) {
                            mEnableSlideShowforSingleMedia = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableShowMmsLog", value)) {
                            mEnableShowMmsLog = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSubjectEditorByteFilter", value)) {
                            mIsFiltSubject = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMmsExpiryWhthMax", value)) {
                            mMmsExpiryMaxEnable = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("creationModeEnabled", value)) {
                            mCreationModeEnabled = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("displaySentTime", value)) {
                            mDisplaySentTime = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableForwardMessageFrom", value)) {
                            mEnableForwardMessageFrom = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableCancelAutoRetrieve", value)) {
                            mEnableCancelAutoRetrieve = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableMmsParamsFromGlobal", value)) {
                            mEnabletMmsParamsFromGlobal = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("closePopupMsgOption", value)) {
                            mClosePopupMsgOption = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSmartAchive", value)) {
                            mEnableSmartAchive = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableHarassment", value)) {
                            mEnableHarassment = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableCNAddress", value)) {
                            mEnableCNAddress = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("isChangeClassZeroMessageShow", value)) {
                            mEnableChangeClassZeroMessageShow = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("checkEmailPopup", value)) {
                            mCheckEmailPopup = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableCTInGsmMode", value)) {
                            mSupportCTInGsmMode = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableTimeParse", value)) {
                            mEnableTimeParse = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("EnableSMSDeliveryRptMultiCardPerf", value)) {
                            mSMSDeliveryRptMultiCardPerf = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("EnableSaveModeMultiCardPerf", value)) {
                            mSaveModeMultiCardPerf = "true".equalsIgnoreCase(text);
                        } else if ("isRefreshRxNum".equalsIgnoreCase(value)) {
                            mIsRefreshRxNum = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSmartSms", value)) {
                            mSupportSmartSmsFeature = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSendVcalByMms", value)) {
                            mEnableSendVcalByMms = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableAutoDelete", value)) {
                            mEnableAutoDelete = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableCryptoSms", value)) {
                            enableCryptoSms = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableAFW", value)) {
                            enableAFW = "true".equalsIgnoreCase(text);
                        } else if (isStringEqual("enableSmsRecycler", value)) {
                            mEnableSmsRecyclerFeature = "true".equalsIgnoreCase(text);
                        } else {
                            mMmsConfigMap.put(value, Boolean.valueOf("true".equalsIgnoreCase(text)));
                        }
                    } else if ("int".equals(tag)) {
                        if (isStringEqual("maxMessageSize", value)) {
                            mMaxMessageSize = Integer.parseInt(text);
                            initMmsConfigMap.put(value, Integer.valueOf(Integer.parseInt(text)));
                        } else if (isStringEqual("maxImageHeight", value)) {
                            mMaxImageHeight = Integer.parseInt(text);
                        } else if (isStringEqual("maxImageWidth", value)) {
                            mMaxImageWidth = Integer.parseInt(text);
                        } else if (isStringEqual("defaultSMSMessagesPerThread", value)) {
                            mDefaultSMSMessagesPerThread = Integer.parseInt(text);
                        } else if (isStringEqual("defaultMMSMessagesPerThread", value)) {
                            mDefaultMMSMessagesPerThread = Integer.parseInt(text);
                        } else if (isStringEqual("minMessageCountPerThread", value)) {
                            mMinMessageCountPerThread = Integer.parseInt(text);
                        } else if (isStringEqual("maxMessageCountPerThread", value)) {
                            mMaxMessageCountPerThread = Integer.parseInt(text);
                        } else if (isStringEqual("recipientLimit", value)) {
                            mRecipientLimit = Integer.parseInt(text);
                            if (mRecipientLimit < 0) {
                                mRecipientLimit = 1000;
                            }
                        } else if (isStringEqual("httpSocketTimeout", value)) {
                            mHttpSocketTimeout = Integer.parseInt(text);
                        } else if (isStringEqual("minimumSlideElementDuration", value)) {
                            mMinimumSlideElementDuration = Integer.parseInt(text);
                        } else if (isStringEqual("maxSizeScaleForPendingMmsAllowed", value)) {
                            mMaxSizeScaleForPendingMmsAllowed = Integer.parseInt(text);
                        } else if (isStringEqual("aliasMinChars", value)) {
                            mAliasRuleMinChars = Integer.parseInt(text);
                        } else if (isStringEqual("aliasMaxChars", value)) {
                            mAliasRuleMaxChars = Integer.parseInt(text);
                        } else if (isStringEqual("smsToMmsTextThreshold", value)) {
                            mSmsToMmsTextThreshold = Integer.parseInt(text);
                            initMmsConfigMap.put(value, Integer.valueOf(Integer.parseInt(text)));
                        } else if (isStringEqual("maxMessageTextSize", value)) {
                            mMaxTextLength = Integer.parseInt(text);
                        } else if (isStringEqual("maxSubjectLength", value)) {
                            mMaxSubjectLength = Integer.parseInt(text);
                        } else if (isStringEqual("smsAllCharTo7Bit", value)) {
                            mSmsAllCharTo7Bit = Integer.parseInt(text);
                        } else if (isStringEqual("mmsExpiryKey", value)) {
                            mMmsExpiry = Integer.parseInt(text);
                        } else if (isStringEqual("hw_mms_retry", value)) {
                            mHWMmsRetry = Integer.parseInt(text);
                        } else if (isStringEqual("WAPPushSupported", value)) {
                            mWAPPushSupported = Integer.parseInt(text);
                        } else if (isStringEqual("mmsDownloadAvailableSpaceLimit", value)) {
                            mMmsDownloadAvailableSpaceLimit = Integer.parseInt(text);
                        } else if (isStringEqual("mmsExpiry", value)) {
                            mMmsExpiry = Integer.parseInt(text);
                        } else if (isStringEqual("maxSlides", value)) {
                            mMaxSlides = Integer.parseInt(text);
                        } else if (isStringEqual("maxCacheMsgNum", value)) {
                            mMaxCacheMsgNum = Integer.parseInt(text);
                        } else if (isStringEqual("maxRestrictedImageHeight", value)) {
                            mMaxRestrictedImageHeight = Integer.parseInt(text);
                        } else if (isStringEqual("maxRestrictedImageWidth", value)) {
                            mMaxRestrictedImageWidth = Integer.parseInt(text);
                        } else if ("customDefaultSlideDuration".equals(value)) {
                            mCustomDefaultSlideDuration = Integer.parseInt(text);
                        } else if ("max_recent_contacts".equals(value)) {
                            mMaxRecentContacts = Integer.parseInt(text);
                        } else if ("forward_limit_size".equals(value)) {
                            mForwardLimitSize = Integer.parseInt(text);
                        } else if (isStringEqual("enableSendMmsNumMinLength", value)) {
                            mEnableSendMmsNumMinLength = Integer.parseInt(text);
                        } else if (isStringEqual("enableDeliveryReportState", value)) {
                            mEnableDeliveryReportState = Integer.parseInt(text);
                        } else if (isStringEqual("defaultAutoRetrievalMms", value)) {
                            mDefaultAutoRetrievalMms = Integer.parseInt(text);
                        } else if (isStringEqual("defaultAlwaysAllowMms", value)) {
                            mDefaultAlwaysAllowMms = Integer.parseInt(text);
                        } else {
                            mMmsConfigMap.put(value, Integer.valueOf(Integer.parseInt(text)));
                        }
                    } else if ("string".equals(tag)) {
                        if (isStringEqual("userAgent", value)) {
                            mUserAgent = text;
                        } else if (isStringEqual("SMSCAddress", value)) {
                            mSMSCAddress = text;
                        } else if (isStringEqual("custMccFor7bitMatchMap", value)) {
                            mCustMccFor7bitMatchMap = text;
                        } else if (isStringEqual("defaultSignatureText", value)) {
                            mDefaultSignatureText = text;
                        } else if (isStringEqual("uaProfTagName", value)) {
                            mUaProfTagName = text;
                        } else if (isStringEqual("uaProfUrl", value)) {
                            mUaProfUrl = text;
                        } else if (isStringEqual("httpParams", value)) {
                            mHttpParams = text;
                        } else if (isStringEqual("httpParamsLine1Key", value)) {
                            mHttpParamsLine1Key = text;
                        } else if (isStringEqual("emailGatewayNumber", value)) {
                            mEmailGateway = text;
                        } else if (isStringEqual("defaultNotificationRingtone", value)) {
                            mDefaultNotificationRingtone = text;
                        } else if (isStringEqual("char_7bit", value)) {
                            mChar_7bit = text;
                        } else if (isStringEqual("char7bitVenezuela", value)) {
                            mChar7bitVenezuela = text;
                        } else if (isStringEqual("prefPlaymode", value)) {
                            mPrefPlaymode = text;
                        } else if (isStringEqual("northEastEuroMCCList", value)) {
                            parseMccArrayList(text);
                        } else if (isStringEqual("MCCMNCListForBlankSMS", value)) {
                            mRussiaMccBlankSMS = text;
                        } else {
                            mMmsConfigMap.put(value, text);
                        }
                    }
                }
            }
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e) {
                    MLog.e("MmsConfig", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (XmlPullParserException e2) {
            MLog.e("MmsConfig", "loadMmsSettings caught ", (Throwable) e2);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e3) {
                    MLog.e("MmsConfig", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (NumberFormatException e4) {
            MLog.e("MmsConfig", "loadMmsSettings caught ", (Throwable) e4);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e5) {
                    MLog.e("MmsConfig", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (IOException e6) {
            MLog.e("MmsConfig", "loadMmsSettings caught ", (Throwable) e6);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e7) {
                    MLog.e("MmsConfig", "loadMmsSettings: fail close the parser");
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (parser instanceof KXmlParser) {
                try {
                    ((KXmlParser) parser).close();
                } catch (IOException e8) {
                    MLog.e("MmsConfig", "loadMmsSettings: fail close the parser");
                }
            }
        }
        String uaProfUrlSpecial = SystemProperties.get("ro.config.hw_use_browser_ua");
        if (!TextUtils.isEmpty(uaProfUrlSpecial)) {
            String str = mUaProfUrl;
            mUaProfUrl = String.format(uaProfUrlSpecial, new Object[]{Build.PRODUCT});
        }
        String errorStr = null;
        if (getMmsEnabled() && mUaProfUrl == null) {
            errorStr = "uaProfUrl";
        }
        if (errorStr != null) {
            MLog.e("MmsConfig", String.format("MmsConfig.loadMmsSettings mms_config.xml missing %s setting", new Object[]{errorStr}));
        }
    }

    public static boolean getWAPPushEnabled() {
        return mWAPPushSupported == 1;
    }

    public static int getModifyMmsRetryScheme() {
        return mHWMmsRetry;
    }

    public static void setUserAgent(String customUA) {
        mUserAgent = customUA;
    }

    public static boolean getMmsDRInRussiaEnabled() {
        return mEnableMmsDRInRussia;
    }

    public static boolean getEnableEmailSpanAsMmsRecipient() {
        return mEnableEmailSpanAsMmsRecipient;
    }

    public static boolean enableShortPhoneNumberLink() {
        return mEnableShortPhoneNumberLink;
    }

    public static boolean enableSplitNumberAndEmailRecipients() {
        return mEnableSplitNumberAndEmailRecipients;
    }

    public static Boolean isCBSEnabled() {
        return Boolean.valueOf(mEnableCBS);
    }

    public static String getChar_7bit() {
        return mChar_7bit;
    }

    public static int getSmsAllCharTo7Bit() {
        return mSmsAllCharTo7Bit;
    }

    public static void setSmsAllCharTo7Bit(int smsAllCharTo7bit) {
        mSmsAllCharTo7Bit = smsAllCharTo7bit;
    }

    public static String getChar7bitVenezuela() {
        return mChar7bitVenezuela;
    }

    public static boolean getSendingBlankSMSEnabled() {
        return mEnableSendingBlankSMS;
    }

    public static String getRussiaMccBlankSMS() {
        return mRussiaMccBlankSMS;
    }

    public static void setSendingBlankSMSEnabledForRussia() {
        String lDefaultMccMnc = "";
        if (MmsApp.getDefaultTelephonyManager().isMultiSimEnabled()) {
            lDefaultMccMnc = MmsApp.getDefaultTelephonyManager().getSimOperator(SubscriptionManager.getDefaultSubscriptionId());
        } else {
            lDefaultMccMnc = MmsApp.getDefaultTelephonyManager().getSimOperator();
        }
        for (String lRussiaNumberTemp : mRussiaMccBlankSMS.split(",")) {
            if (!(TextUtils.isEmpty(lDefaultMccMnc) || TextUtils.isEmpty(lRussiaNumberTemp))) {
                if (3 == lRussiaNumberTemp.length() && lDefaultMccMnc.startsWith(lRussiaNumberTemp)) {
                    mEnableSendingBlankSMS = true;
                    return;
                } else if (5 == lRussiaNumberTemp.length() && lDefaultMccMnc.equals(lRussiaNumberTemp)) {
                    mEnableSendingBlankSMS = true;
                    return;
                }
            }
        }
    }

    public static boolean getEnableOptionalRingtone() {
        return mEnableOptionalRingtone;
    }

    public static boolean getDefaultMMSReadReports() {
        return mDefaultMMSReadReports;
    }

    public static void setDefaultSMSDeliveryReports(boolean setValue) {
        mDefaultSMSDeliveryReports = setValue;
    }

    public static void setDefaultMMSDeliveryReports(boolean setValue) {
        mDefaultMMSDeliveryReports = setValue;
    }

    public static boolean getDefaultMMSRetrievalDuringRoamingCard1() {
        return mDefaultMMSRetrievalDuringRoamingCard1;
    }

    public static boolean getDefaultMMSRetrievalDuringRoamingCard2() {
        return mDefaultMMSRetrievalDuringRoamingCard2;
    }

    public static boolean getEnableModifySMSCenterNumber() {
        return mEnableModifySMSCenterNumber;
    }

    public static void setHas7BitAlaphsetInHwDefaults(boolean has7BitAlaphsetInHwDefaults) {
        mHas7BitAlaphsetInHwDefaults = has7BitAlaphsetInHwDefaults;
    }

    public static boolean getForbiddenSetFrom() {
        MLog.d("MmsConfig", "[getForbiddenSetForm] run! " + mForbiddenSetFrom);
        return mForbiddenSetFrom;
    }

    public static void setForbiddenSetFrom(boolean forbiddenSetFrom) {
        MLog.d("MmsConfig", "[setForbiddenSetForm] run! " + forbiddenSetFrom);
        mForbiddenSetFrom = forbiddenSetFrom;
    }

    public static boolean getEnableSpecialSMS() {
        return mEnableSpecialSMS;
    }

    public static boolean isSms7BitEabled() {
        return !mSms7BitEnabled ? mHas7BitAlaphsetInHwDefaults : true;
    }

    public static boolean getEnableMmsVcard() {
        return mEnableMmsVcard;
    }

    public static boolean getPhoneAttributeEnabled() {
        return mEnablePhoneAttribute;
    }

    public static String getDefaultNotificationRingtone() {
        return mDefaultNotificationRingtone;
    }

    public static int getMmsDownloadAvailableSpaceLimit() {
        return mMmsDownloadAvailableSpaceLimit;
    }

    public static boolean isShowTotalCount() {
        return mEnableShowTotalCount;
    }

    public static int getEnableSendMmsNumMinLength() {
        return mEnableSendMmsNumMinLength;
    }

    public static boolean getEnableWapPushReplace() {
        return mEnableWappushReplace;
    }

    public static boolean getIsRenameAttachmentName() {
        return mIsRenameAttachmentName;
    }

    public static boolean getEnableMMSAutoReplyReadReports() {
        return mEnableMMSAutoReplyReadReports;
    }

    public static boolean getDefaultMMSAutoReplyReadReports() {
        return mDefaultMMSAutoReplyReadReports;
    }

    public static boolean isShowMmsReadReportDialog() {
        return mShowMmsReadReportDialog;
    }

    public static boolean isEnableZoomWhenView() {
        return mZoomWhenView;
    }

    public static boolean getWapPushSettingEnabled() {
        return mWapPushSettingEnabled;
    }

    public static boolean getSmsTextCounterShowEnabled() {
        return mEnableSmsTextCounterShow;
    }

    public static ArrayList<String> getNorthEastEuropeMccList() {
        return mNorthEastEuropeMccList;
    }

    private static void parseMccArrayList(String text) {
        if (!TextUtils.isEmpty(text)) {
            String[] vaules = text.replace("\r", "").replace("\n", "").replace(" ", "").split(",");
            if (vaules != null && vaules.length >= 1) {
                for (String value : vaules) {
                    mNorthEastEuropeMccList.add(value);
                }
            }
        }
    }

    public static boolean isEuropeCust() {
        return mNorthEastEuropeMccList != null && mNorthEastEuropeMccList.size() > 0;
    }

    public static boolean isStrictAddrCharSet() {
        return bStrictAddrCharSet;
    }

    public static boolean getSupportedVCalendarEnabled() {
        return mSupportVCalendarMode;
    }

    public static boolean getIsShowAttachmentSize() {
        return mIsShowAttachmentSize;
    }

    public static void setCustomDefaultValues(SharedPreferences sp) {
        Editor editor = sp.edit();
        if (getSmsOptimizationCharacters()) {
            editor.putBoolean("pref_key_sms_optimization_characters", mDefault7bitOptionValue).commit();
            if (isLossless7bit()) {
                int[] temp = new int[]{System.getInt(MmsApp.getApplication().getContentResolver(), "sms_coding_national_backup", 0)};
                MessageUtils.setSmsCodingNationalCode(String.valueOf(temp[0]));
                MessageUtils.setSingleShiftTable(temp);
            }
        }
        if (HwSpecialUtils.isAlwaysMms()) {
            AdvancedPreferenceFragment.setAlwaysReceiveAndSendMmsPrefState(getDefaultAlwaysAllowMms());
        }
        if (isEnablePopupMessage() && isClosePopupMsgOption()) {
            editor.putBoolean("pref_key_enable_popup_message", false).commit();
        }
        editor.putBoolean("pref_key_auto_delete", mDefaultAutoDeleteMessages);
        if (mHwCustMmsConfig != null) {
            mEnableDeliveryReportState = mHwCustMmsConfig.getCustConfigForDeliveryReports(mEnableDeliveryReportState);
        }
        initDefaultDeliverReportState(getDefaultDeliveryReportState());
        if (MessageUtils.isMultiSimEnabled() && isSMSDeliveryRptMultiCardPerf()) {
            editor.putBoolean("pref_key_sms_delivery_reports_sub0", mDefaultSMSDeliveryReports);
            editor.putBoolean("pref_key_sms_delivery_reports_sub1", mDefaultSMSDeliveryReports);
        } else {
            editor.putBoolean("pref_key_sms_delivery_reports", mDefaultSMSDeliveryReports);
        }
        if (mHwCustMmsConfig != null) {
            mDefaultMMSDeliveryReports = mHwCustMmsConfig.getCustDefaultMMSDeliveryReports(mDefaultMMSDeliveryReports);
            mDefaultMMSReadReports = mHwCustMmsConfig.getCustConfigForMmsReadReports(mDefaultMMSReadReports);
            mDefaultMMSAutoReplyReadReports = mHwCustMmsConfig.getCustConfigForMmsReplyReadReports(mDefaultMMSAutoReplyReadReports);
        }
        editor.putBoolean("pref_key_mms_delivery_reports", mDefaultMMSDeliveryReports);
        editor.putBoolean("pref_key_mms_read_reports", mDefaultMMSReadReports);
        HwCustPreferenceUtils mCustPreferenceUtils = (HwCustPreferenceUtils) HwCustUtils.createObj(HwCustPreferenceUtils.class, new Object[]{MmsApp.getApplication().getApplicationContext()});
        if (mCustPreferenceUtils != null) {
            mDefaultMMSRetrievalDuringRoaming = mCustPreferenceUtils.getCustDefaultMmsAutoRetreive(mDefaultMMSRetrievalDuringRoaming);
        }
        if (getMMSSendDeliveryReportsEnabled()) {
            editor.putBoolean("pref_key_mms_enable_to_send_delivery_reports", mDefaultMMSSendDeliveryReports);
        }
        editor.putBoolean("pref_key_vibrateWhen", mDefaultVibrateWhenNotification);
        editor.putBoolean("pref_key_vibrateWhen_sub0", mDefaultVibrateWhenNotification);
        editor.putBoolean("pref_key_vibrateWhen_sub1", mDefaultVibrateWhenNotification);
        editor.putBoolean("pref_key_forward_message_from_settings", mEnableForwardMessageFrom);
        editor.putBoolean("pref_key_sms_wappush_enable", getWAPPushEnabled()).commit();
        editor.putBoolean("pref_key_mms_group_mms", mDefaultGroupMessage);
        if (getEnableMMSAutoReplyReadReports()) {
            editor.putBoolean("pref_key_mms_auto_reply_read_reports", mDefaultMMSAutoReplyReadReports);
        }
        GeneralPreferenceFragment.restoreDefaultMMSRetrievalDuringRoamingMultiSim(mContext);
        String str = getDefaultSignatureText();
        if (!TextUtils.isEmpty(str)) {
            SignatureUtil.putSignature(mContext, SignatureUtil.deleteNewlineSymbol(str));
        }
        setAutoReceivePrefState();
        editor.apply();
    }

    public static void setAutoReceivePrefState() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        int which = getDefaultAutoRetrievalMms();
        if (which == 1) {
            setMultiSimRoamingAutoRetrieveValue(false);
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", false);
            editor.putBoolean("pref_key_mms_auto_retrieval", true);
            editor.putInt("autoReceiveMms", 1);
        } else if (which == 0) {
            setMultiSimRoamingAutoRetrieveValue(true);
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", true);
            editor.putBoolean("pref_key_mms_auto_retrieval", true);
            editor.putInt("autoReceiveMms", 0);
        } else {
            setMultiSimRoamingAutoRetrieveValue(false);
            editor.putBoolean("pref_key_mms_retrieval_during_roaming", false);
            editor.putBoolean("pref_key_mms_auto_retrieval", false);
            editor.putInt("autoReceiveMms", 2);
        }
        editor.commit();
    }

    private static void setMultiSimRoamingAutoRetrieveValue(boolean isChecked) {
        if (1 == MessageUtils.getIccCardStatus(0)) {
            MessageUtils.setRoamingAutoRetrieveValue(MmsApp.getApplication().getApplicationContext().getContentResolver(), 1, isChecked);
        }
        if (1 == MessageUtils.getIccCardStatus(1)) {
            MessageUtils.setRoamingAutoRetrieveValue(MmsApp.getApplication().getApplicationContext().getContentResolver(), 2, isChecked);
        }
    }

    public static void setRingToneUriToDatabase(Context context, String ringToneUri) {
        try {
            System.putString(context.getContentResolver(), "message", ringToneUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRingToneUriToDatabase(Context context, String ringToneUri, int subId) {
        String key = "message";
        if (subId > 0) {
            key = "messageSub1";
        }
        try {
            System.putString(context.getContentResolver(), key, ringToneUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRingToneUriFromDatabase(Context context, String strName) {
        String str = null;
        try {
            str = System.getString(context.getContentResolver(), strName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getRingToneUriFromDatabase(Context context, int subId) {
        String key = "message";
        if (subId == 1) {
            key = "messageSub1";
        }
        String str = null;
        try {
            str = System.getString(context.getContentResolver(), key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static boolean isUseGgSmsAddressCheck() {
        return mUseGgSmsAddressCheck;
    }

    public static boolean isRingWhentalk() {
        return mRingWhenOnTalk;
    }

    public static boolean isEnableSlideShowforSingleMedia() {
        return mEnableSlideShowforSingleMedia;
    }

    public static boolean isEnableShowMmsLog() {
        return mEnableShowMmsLog;
    }

    public static boolean hasMmsRingtoneUri(Context context) {
        Cursor cursor = null;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, SYSTEMEX_URI, null, "name= ? ", new String[]{"message"}, null);
            if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
                return true;
            }
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isExtraHugeEnabled(float fontScale) {
        return Math.abs(ContentUtil.FONT_SIZE_EXTRA_HUGE - fontScale) < 1.0E-7f;
    }

    public static boolean isExtraHugeEnabled() {
        return Math.abs(ContentUtil.FONT_SIZE_EXTRA_HUGE - mContext.getResources().getConfiguration().fontScale) < 1.0E-7f;
    }

    public static boolean isHugeEnabled() {
        return Math.abs(ContentUtil.FONT_SIZE_HUGE - mContext.getResources().getConfiguration().fontScale) < 1.0E-7f;
    }

    public static String filteNumberByLocal(String number) {
        if (msfilter == null) {
            msfilter = new NumberFilterCn();
        }
        return msfilter.filteNumberByLocal(number);
    }

    public static void setLocal(String local) {
        if (local != null && (local.contains("CN") || local.contains("HK"))) {
            msfilter = new NumberFilterCn();
        } else if (mUseGgSmsAddressCheck) {
            msfilter = new NumberFilter();
        } else {
            msfilter = new NumberFilterCn();
        }
    }

    public static boolean isSupportPrivacy() {
        return SystemProperties.getBoolean("ro.config.hw_privacymode", false);
    }

    public static boolean isEnablePopupMessage() {
        return mEnablePopupMsg;
    }

    public static boolean isInSimpleUI() {
        return mIsSimpleUi;
    }

    public static boolean isSupportMmsSubject() {
        return mIsSimpleUi ? mHwCustMmsConfig.isSupportSubjectForSimpleUI() : true;
    }

    public static boolean isShowSignatureDialog() {
        return isShowSignatureDialog;
    }

    public static void checkSimpleUi() {
        boolean z = false;
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            ConfigurationEx mExtraConfig = new com.huawei.android.content.res.ConfigurationEx(curConfig).getExtraConfig();
            if (mExtraConfig != null && 2 == mExtraConfig.getConfigItem(2)) {
                z = true;
            }
            mIsSimpleUi = z;
        } catch (RemoteException e) {
            MLog.e("MmsConfig", "checkSimpleUi has RemoteException " + e.getClass().getName());
        } catch (RuntimeException e2) {
            MLog.e("MmsConfig", "checkSimpleUi has RuntimeException " + e2.getClass().getName());
        } catch (NoSuchFieldError e3) {
            MLog.e("MmsConfig", "checkSimpleUi has NoSuchFieldError " + e3.getClass().getName());
        } catch (Error e4) {
            MLog.e("MmsConfig", "checkSimpleUi has no such method error");
        }
    }

    public static boolean isCoexistWithMms() {
        return mCoexistWithMms;
    }

    public static String getCurrentCreationMode() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString("pref_key_creation_mode", "freemodemode");
    }

    public static boolean isCurrentRestrictedMode() {
        return getCreationModeEnabled() ? "restrictionmode".equals(getCurrentCreationMode()) : false;
    }

    public static boolean isMmsGcfTest() {
        return mGcfMmsTest;
    }

    public static boolean isDisplaySentTime() {
        return mDisplaySentTime;
    }

    public static final boolean isStringEqual(String cfgTag, String xmlTag) {
        int cfgLen = cfgTag.length();
        if (cfgLen != xmlTag.length()) {
            return false;
        }
        for (int i = cfgLen - 1; i >= 0; i--) {
            if (cfgTag.charAt(i) != xmlTag.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isStringStartWithPrefix(String strOrg, String prefix) {
        if (prefix == null || strOrg.length() < prefix.length()) {
            return false;
        }
        for (int i = 0; i < prefix.length(); i++) {
            if (strOrg.charAt(i) != prefix.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEnableCancelAutoRetrieve() {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            return false;
        }
        return mEnableCancelAutoRetrieve;
    }

    public static int getCustomDefaultSlideDuration() {
        return mCustomDefaultSlideDuration;
    }

    public static boolean isClosePopupMsgOption() {
        return mClosePopupMsgOption;
    }

    public static boolean isCspVersion() {
        return MmsInfo.isInCspMode(MmsApp.getApplication());
    }

    public static boolean isSupportSmartFolder() {
        return mEnableSmartAchive;
    }

    public static HwCustMmsConfig getCustMmsConfig() {
        return mHwCustMmsConfig;
    }

    public static RcsMmsRcsConfig getHwCustMmsConfig() {
        return mHwCust;
    }

    public static boolean isRcsSwitchOn() {
        if (mHwCust == null || !mHwCust.isRcsSwitchOn()) {
            return false;
        }
        return true;
    }

    public static boolean isSupportHarassment() {
        return mEnableHarassment;
    }

    public static boolean isSupportCNAddress() {
        return mEnableCNAddress;
    }

    public static boolean isShowCheckEmailPoup() {
        return mCheckEmailPopup;
    }

    public static void setShowCheckEmailPoup(boolean isShowCheckEmailPoup) {
        mCheckEmailPopup = isShowCheckEmailPoup;
    }

    public static int getForwardLimitSize() {
        return mForwardLimitSize;
    }

    public static int getChangeScrollerHeightDelayLong() {
        MmsApp app = MmsApp.getApplication();
        if (app != null) {
            try {
                return app.getResources().getInteger(R.integer.change_scroller_height_delay_long);
            } catch (NotFoundException e) {
                MLog.e("MmsConfig", "Not found resources change_scroller_height_delay_long");
            }
        }
        return SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
    }

    public static boolean getMmsBoolConfig(String name) {
        return getMmsBoolConfig(name, false);
    }

    public static boolean getMmsBoolConfig(String name, boolean defaultValue) {
        if (mMmsConfigMap.containsKey(name) && (mMmsConfigMap.get(name) instanceof Boolean)) {
            return ((Boolean) mMmsConfigMap.get(name)).booleanValue();
        }
        return defaultValue;
    }

    public static int getMmsIntConfig(String name, int defaultValue) {
        if (mMmsConfigMap.containsKey(name) && (mMmsConfigMap.get(name) instanceof Integer)) {
            return ((Integer) mMmsConfigMap.get(name)).intValue();
        }
        return defaultValue;
    }

    public static int getInitMmsIntConfig(String name, int defaultValue) {
        if (initMmsConfigMap.containsKey(name)) {
            Object value = initMmsConfigMap.get(name);
            if (value instanceof Integer) {
                return ((Integer) initMmsConfigMap.get(name)).intValue();
            }
            MLog.d("MmsConfig", "Error>>getMmsIntConfig2>the configuration of " + name + " should be defined as int " + "but now it is defined as " + value.getClass().getName());
        }
        return defaultValue;
    }

    public static int getDefaultDeliveryReportState() {
        return mEnableDeliveryReportState;
    }

    public static String getMmsStringConfig(String name) {
        return getMmsStringConfig(name, null);
    }

    public static String getMmsStringConfig(String name, String defaultValue) {
        if (mMmsConfigMap.containsKey(name) && (mMmsConfigMap.get(name) instanceof String)) {
            return (String) mMmsConfigMap.get(name);
        }
        return defaultValue;
    }

    public static boolean isSupportTimeParse() {
        return mEnableTimeParse;
    }

    public static boolean isSaveModeMultiCardPerf() {
        return mSaveModeMultiCardPerf;
    }

    public static boolean isSMSDeliveryRptMultiCardPerf() {
        return mSMSDeliveryRptMultiCardPerf;
    }

    public static boolean getIsRefreshRxNum() {
        if (mHwCustMmsConfig != null) {
            mIsRefreshRxNum = mHwCustMmsConfig.isRefreshRxNumByMccMnc(mIsRefreshRxNum);
        }
        return mIsRefreshRxNum;
    }

    public static boolean getEnableChangeClassZeroMessageShow() {
        return mEnableChangeClassZeroMessageShow;
    }

    public static boolean isVoiceCapable() {
        if (mContext == null) {
            return true;
        }
        Resources resources = mContext.getResources();
        if (resources == null) {
            return true;
        }
        return resources.getBoolean(17956956);
    }

    public static boolean noticeNewMessageWhenBootup() {
        return false;
    }

    public static boolean getSupportSmartSmsFeature() {
        return mSupportSmartSmsFeature;
    }

    public static boolean getAutoRetrievalSingleCardEnable() {
        return mAutoRetrievalSingleCardPref;
    }

    public static boolean getMuilCardAutoRetrievalEnable() {
        return mMuilCardAutoRetrievalEbable;
    }

    public static boolean isContainsTheKey(Context context) {
        MLog.e("MmsConfig", "Unsuppoert pc call");
        return hasMmsRingtoneUri(context);
    }

    public static boolean isEnableAutoDelete() {
        return mEnableAutoDelete;
    }

    public static boolean isEnableWhatsApp() {
        return mEnableWhatsAppMenu;
    }

    public static boolean isEnableCryptoSms() {
        return enableCryptoSms;
    }

    public static boolean isEnableAFW() {
        return enableAFW;
    }

    public static boolean isSmsRecyclerEnable() {
        return mEnableSmsRecyclerFeature;
    }

    public static boolean isSupportSafeVerifitionSms() {
        return "CN".equals(SystemProperties.get("ro.product.locale.region", ""));
    }

    public static boolean isVerifitionSmsProtectEnable(Context context) {
        int smsProtectEnable = System.getInt(context.getContentResolver(), "verifition_sms_protect_enable", 1);
        if (isSupportSafeVerifitionSms() && smsProtectEnable == 1) {
            return true;
        }
        return false;
    }

    private static String switchXmlLogPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        if ("data/cust/xml/mms_config.xml".equals(path)) {
            return "load xml file from CUST_PATH_CFG";
        }
        if ("/system/etc/xml/mms_config.xml".equals(path)) {
            return "load xml file from SYSTEM_PATH_CFG";
        }
        if ("xml/mms_config.xml".equals(path)) {
            return "load xml file from BASE_PATH_CFG";
        }
        return "load xml file from UNKNOWN_PATH_CFG";
    }

    public static float getPrePressureThreshold() {
        return sPressureThreshold * 0.3f;
    }

    public static float getPressureThreshold() {
        return sPressureThreshold;
    }

    public static void addPreviewFlag(Intent intent) {
        intent.putExtra("android.intent.action.START_PEEK_ACTIVITY", "startPeekActivity");
    }

    public static void initDefaultDeliverReportState(int deliveryReportIndex) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("pref_key_delivery_reports", deliveryReportIndex).commit();
        if (1 == deliveryReportIndex) {
            setDefaultSMSDeliveryReports(true);
            setDefaultMMSDeliveryReports(false);
        } else if (2 == deliveryReportIndex) {
            setDefaultSMSDeliveryReports(false);
            setDefaultMMSDeliveryReports(true);
        } else if (3 == deliveryReportIndex) {
            setDefaultSMSDeliveryReports(true);
            setDefaultMMSDeliveryReports(true);
        } else {
            setDefaultSMSDeliveryReports(false);
            setDefaultMMSDeliveryReports(false);
        }
    }

    public static boolean isSupportDraftWithoutRecipient() {
        return SystemProperties.getBoolean("ro.config.mmssms.draft", true);
    }

    public static boolean getUnreadPinupEnable(Context context) {
        if (prefs != null) {
            return prefs.getBoolean("pref_key_pinup_unread_message_enable", false);
        }
        Context result;
        if (mContext != null) {
            result = mContext;
        } else {
            result = context;
        }
        if (result != null) {
            return PreferenceManager.getDefaultSharedPreferences(result).getBoolean("pref_key_pinup_unread_message_enable", false);
        }
        MLog.e("MmsConfig", "result is null when get UnreadPinupEnable");
        return false;
    }
}
