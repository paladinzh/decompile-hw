package com.android.contacts.hap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.contacts.ContactSplitUtils;
import com.android.contacts.ContactsApplication;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.yellowpage.YellowPageUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.huawei.contact.util.SettingsWrapper;
import com.huawei.cspcommon.util.CommonConstants;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import com.huawei.rcs.util.RcsFeatureEnabler;

public class EmuiFeatureManager {
    private static int ISRUSSIANUMBERSEARCHEENABLE = -1;
    private static final boolean IS_BLACKLIST_FEATURE_ENABLED = SystemProperties.getBoolean("ro.config.hw_blacklist", false);
    private static int IS_CAMCARD_APK_ENABLED = -1;
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final boolean IS_CONTACT_SCREEN_PORTRAIT = SystemProperties.getBoolean("ro.config.hw_ContactNewPortrait", false);
    private static boolean IS_CSP_MERGE_VERSION = true;
    private static boolean IS_DETAIL_HEADER_ANIMATION_ENABLED = false;
    private static final boolean IS_DIALPAD_HDICON_ON = SystemProperties.getBoolean("ro.config.hw_dialpad_hd_on", true);
    private static final boolean IS_DIAL_VM_TIP = SystemProperties.getBoolean("ro.config.hw_ContactDialVMTip", false);
    private static final boolean IS_ENABLE_NUM_ONLY_CONTACTS = SystemProperties.getBoolean("ro.config.hideContactWithoutNum", false);
    private static final boolean IS_HIDE_UNKNOWN_GEO = SystemProperties.getBoolean("ro.config.att_unknown", false);
    private static final boolean IS_MULTI_DELETE_CALLLOG_ENABLED = SystemProperties.getBoolean("ro.multi.delete.calllog", true);
    private static int IS_NEED_SPLIT_SCREEN = -1;
    private static final boolean IS_PRODUCT_CUST_FEATURE_ENABLE = SystemProperties.getBoolean("contact.config.prod_cust.enable", true);
    private static final boolean IS_RCS_ENABLE = RcsFeatureEnabler.getInstance().isRcsEnabled();
    private static final boolean IS_RCS_WHOLE_ON = isRcsWholeOn();
    private static int IS_RUSSIA_NUMBER_RELEVANCE_FEATURE_ENABLED = -1;
    private static int IS_SMS_CAPABLE = -1;
    private static boolean IS_SUPPORT_MULTI_COLOR_PHOTO = true;
    private static int IS_VOICE_CAPABLE = -1;
    private static final String TAG = EmuiFeatureManager.class.getSimpleName();
    private static int bEmailAnrSupport = -1;
    private static boolean gIsPTSDKInit = false;
    private static boolean gIsYellowpageApkExist = true;
    private static boolean gIsYellowpageChecked = false;
    private static int mIsSimpleDisplayMode = -1;
    private static int sCanPerformBlackListOperations = -1;
    private static boolean sIsUseCustomAnimation = false;
    private static String sRussiaNumberRelevanceFeatureFlag = "";

    public static boolean isPrivacyFeatureEnabled() {
        return CommonConstants.isPrivacyFeatureEnabled();
    }

    public static boolean isRingTimesDisplayEnabled(Context aContext) {
        return QueryUtil.isHAPProviderInstalled();
    }

    public static boolean isMultiDeleteCallLogFeatureEnabled() {
        return IS_MULTI_DELETE_CALLLOG_ENABLED;
    }

    public static boolean isSimAccountIndicatorEnabled() {
        return true;
    }

    public static boolean isBlackListFeatureEnabled() {
        boolean isSystemSMSCapable;
        boolean z = false;
        if (isSystemVoiceCapable()) {
            isSystemSMSCapable = isSystemSMSCapable();
        } else {
            isSystemSMSCapable = false;
        }
        if (!isSystemSMSCapable) {
            return false;
        }
        if (sCanPerformBlackListOperations == -1) {
            IBinder lIBinder = ServiceManager.getService("com.huawei.harassmentinterception.service.HarassmentInterceptionService");
            if (lIBinder != null) {
                int i;
                if (Stub.asInterface(lIBinder) != null) {
                    i = 1;
                } else {
                    i = 0;
                }
                sCanPerformBlackListOperations = i;
            } else {
                sCanPerformBlackListOperations = 0;
            }
        }
        if (sCanPerformBlackListOperations == 1 && !CommonUtilMethods.isSimplifiedModeEnabled()) {
            z = true;
        }
        return z;
    }

    public static boolean isAutoIpEnabled() {
        return IS_CHINA_AREA;
    }

    public static boolean isNumberMarkFeatureEnabled() {
        return QueryUtil.isHAPProviderInstalled() && IS_CHINA_AREA && !isSuperSaverMode();
    }

    public static boolean isChinaArea() {
        return IS_CHINA_AREA;
    }

    public static boolean getEmailAnrSupport() {
        if (bEmailAnrSupport == -1) {
            loadEmailAnrSupportFlag(ContactsApplication.getContext());
        }
        if (bEmailAnrSupport == 1) {
            return true;
        }
        return false;
    }

    public static void loadEmailAnrSupportFlag(Context c) {
        int i = 1;
        try {
            if (SettingsWrapper.getInt(c.getContentResolver(), "is_email_anr_support", 1) <= 0) {
                i = 2;
            }
            bEmailAnrSupport = i;
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "loadEmailAnrSupportFlag:" + bEmailAnrSupport);
            }
        } catch (Exception e) {
            bEmailAnrSupport = 2;
            e.printStackTrace();
        }
    }

    public static boolean isSimpleSectionEnable() {
        return true;
    }

    public static boolean isAutoDeleteContactsEnable() {
        return false;
    }

    public static boolean isYellowPageEnable() {
        return IS_CHINA_AREA && !isSuperSaverMode();
    }

    public static boolean isNumberCapabilityEnable() {
        if (!IS_CHINA_AREA || isSuperSaverMode()) {
            return false;
        }
        return !isSystemVoiceCapable() ? isSystemSMSCapable() : true;
    }

    public static boolean isSuperSaverMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    private static boolean isVoiceCapable(Context context) {
        try {
            return context.getResources().getBoolean(17956956);
        } catch (NotFoundException e) {
            HwLog.w(TAG, "isVoiceCapable: voice capability is not defined");
            return false;
        }
    }

    private static boolean isSmsCapable(Context context) {
        try {
            return context.getResources().getBoolean(17956959);
        } catch (NotFoundException e) {
            HwLog.w(TAG, "isSmsCapable: sms capability is not defined");
            return false;
        }
    }

    public static void initSystemVoiceCapableFlag(Context aContext) {
        IS_VOICE_CAPABLE = isVoiceCapable(aContext) ? 1 : 2;
    }

    public static void initSystemSMSCapableFlag(Context aContext) {
        IS_SMS_CAPABLE = isSmsCapable(aContext) ? 1 : 2;
    }

    public static boolean isSystemVoiceCapable() {
        if (IS_VOICE_CAPABLE == -1) {
            initSystemVoiceCapableFlag(ContactsApplication.getContext());
        }
        if (IS_VOICE_CAPABLE == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSystemSMSCapable() {
        if (IS_SMS_CAPABLE == -1) {
            initSystemSMSCapableFlag(ContactsApplication.getContext());
        }
        if (IS_SMS_CAPABLE == 1) {
            return true;
        }
        return false;
    }

    public static void initNeedSplitScreenFlag(Context aContext) {
        int i = 1;
        if (SystemProperties.getBoolean("ro.config.enable_split", false)) {
            IS_NEED_SPLIT_SCREEN = 1;
            return;
        }
        if (ContactSplitUtils.calculateDeviceSize(aContext) < 8.0d) {
            i = 2;
        }
        IS_NEED_SPLIT_SCREEN = i;
    }

    public static boolean isNeedSplitScreen() {
        if (IS_NEED_SPLIT_SCREEN == -1) {
            initNeedSplitScreenFlag(ContactsApplication.getContext());
        }
        if (IS_NEED_SPLIT_SCREEN == 1) {
            return true;
        }
        return false;
    }

    public static boolean isDetailHeaderAnimationFeatureEnable(Context aContext) {
        boolean z = true;
        if (aContext == null) {
            return false;
        }
        if (aContext.getResources().getConfiguration().orientation != 1) {
            z = IS_DETAIL_HEADER_ANIMATION_ENABLED;
        }
        return z;
    }

    public static void setDetailHeaderAnimation(boolean isDetailHeaderAnimationFeatureEnable) {
        IS_DETAIL_HEADER_ANIMATION_ENABLED = isDetailHeaderAnimationFeatureEnable;
    }

    public static boolean isProductCustFeatureEnable() {
        return IS_PRODUCT_CUST_FEATURE_ENABLE;
    }

    public static boolean isShowFavoritesTab(Context aContext) {
        return true;
    }

    public static boolean isYellowPageApkExist(Context aContext) {
        boolean z = false;
        if (!isChinaArea()) {
            return false;
        }
        if (YellowPageUtils.checkPackageInstall(aContext, "com.huawei.yellowpage") && !YellowPageUtils.isDisable(aContext, "com.huawei.yellowpage").booleanValue()) {
            z = true;
        }
        return z;
    }

    public static boolean isSearchContactsMulti() {
        return true;
    }

    public static void setPTSDKStatus(boolean initStatus) {
        gIsPTSDKInit = initStatus;
    }

    public static boolean getPTSDKStatus() {
        return gIsPTSDKInit;
    }

    public static boolean isUseCustAnimation() {
        if (HwLog.HWFLOW) {
            HwLog.i(TAG, "isUseCustAnimation:" + sIsUseCustomAnimation);
        }
        if (!sIsUseCustomAnimation || isSuperSaverMode()) {
            return false;
        }
        return true;
    }

    public static boolean isPreLoadingSimContactsEnabled() {
        return true;
    }

    public static boolean isAndroidMVersion() {
        return VERSION.SDK_INT > 22;
    }

    public static boolean isSupportMultiColorPhoto() {
        return IS_SUPPORT_MULTI_COLOR_PHOTO;
    }

    public static boolean isContactScreenPortrait() {
        return IS_CONTACT_SCREEN_PORTRAIT;
    }

    public static void initRussiaNumberSearchEnabled(Context context) {
        ISRUSSIANUMBERSEARCHEENABLE = "true".equals(SettingsWrapper.getString(context.getContentResolver(), "enable_RussiaNumberRelevance")) ? 1 : 2;
    }

    public static void loadRussiaNumberRelevanceFeatureFlag(Context context) {
        sRussiaNumberRelevanceFeatureFlag = SettingsWrapper.getString(context.getContentResolver(), "hw_RussiaNumberRelevance");
        IS_RUSSIA_NUMBER_RELEVANCE_FEATURE_ENABLED = TextUtils.isEmpty(sRussiaNumberRelevanceFeatureFlag) ? 2 : 1;
    }

    public static boolean isSupportRussiaNumberRelevance() {
        if (IS_RUSSIA_NUMBER_RELEVANCE_FEATURE_ENABLED == -1) {
            loadRussiaNumberRelevanceFeatureFlag(ContactsApplication.getContext());
        }
        if (IS_RUSSIA_NUMBER_RELEVANCE_FEATURE_ENABLED == 1) {
            return true;
        }
        return false;
    }

    public static String getRussiaNumberRelevanceFeatureFlag() {
        return sRussiaNumberRelevanceFeatureFlag;
    }

    public static void setRussiaNumberSearchEnabled(boolean value) {
        ISRUSSIANUMBERSEARCHEENABLE = value ? 1 : 2;
    }

    public static boolean isRussiaNumberSearchEnabled() {
        if (ISRUSSIANUMBERSEARCHEENABLE == -1) {
            initRussiaNumberSearchEnabled(ContactsApplication.getContext());
        }
        if (ISRUSSIANUMBERSEARCHEENABLE == 1) {
            return true;
        }
        return false;
    }

    public static boolean isRcsFeatureEnable() {
        return (IS_RCS_ENABLE || IS_RCS_WHOLE_ON) ? MultiUsersUtils.isCurrentUserOwner() : false;
    }

    private static boolean isRcsWholeOn() {
        boolean z = true;
        if (ContactsApplication.getContext() == null) {
            HwLog.e(TAG, "isRcsWholeOn ContextApplication is null");
            return false;
        }
        PackageManager packageManager = ContactsApplication.getContext().getPackageManager();
        if (!CommonUtilMethods.checkApkExist(ContactsApplication.getContext(), "com.android.rcssettingon") || packageManager.checkSignatures(ContactsApplication.getContext().getPackageName(), "com.android.rcssettingon") < 0 || !CommonUtilMethods.checkApkExist(ContactsApplication.getContext(), "com.huawei.rcsserviceapplication")) {
            return false;
        }
        if (System.getInt(ContactsApplication.getContext().getContentResolver(), "rcs_whole_on", 0) != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isCamcardEnabled() {
        if (IS_CAMCARD_APK_ENABLED == -1) {
            isShowCamCard(ContactsApplication.getContext());
        }
        if (IS_CAMCARD_APK_ENABLED == 1) {
            return true;
        }
        return false;
    }

    public static void isShowCamCard(Context aContext) {
        IS_CAMCARD_APK_ENABLED = CommonUtilMethods.checkApkExist(aContext, "com.huawei.contactscamcard") ? 1 : 2;
    }

    public static boolean isCamCardApkInstalled(Context aContext) {
        return YellowPageUtils.checkPackageInstall(aContext, "com.huawei.contactscamcard");
    }

    public static boolean isEnableContactsWithNumberOnlyFeature() {
        return IS_ENABLE_NUM_ONLY_CONTACTS;
    }

    public static boolean isContactDialVMTip() {
        return IS_DIAL_VM_TIP;
    }

    public static boolean isContactDialpadHdIconOn() {
        return IS_DIALPAD_HDICON_ON;
    }

    public static boolean isHideUnknownGeo() {
        return IS_HIDE_UNKNOWN_GEO;
    }

    public static void initIsSimpleDisplayMode(Context context) {
        mIsSimpleDisplayMode = SharePreferenceUtil.getDefaultSp_de(context).getBoolean("preference_display_with_simple_mode", false) ? 1 : 2;
    }

    public static void setSimpleDisplayMode(boolean simpleDisplayMode) {
        mIsSimpleDisplayMode = simpleDisplayMode ? 1 : 2;
    }

    public static boolean isSimpleDisplayMode() {
        if (mIsSimpleDisplayMode == -1) {
            initIsSimpleDisplayMode(ContactsApplication.getContext());
        }
        if (mIsSimpleDisplayMode == 1) {
            return true;
        }
        return false;
    }

    public static int getDefaultSimNumLength() {
        int defaultSimNumLength = 20;
        if (ContactsApplication.getContext() != null) {
            HwLog.i(TAG, "get custom sim number length.");
            defaultSimNumLength = System.getInt(ContactsApplication.getContext().getContentResolver(), "sim_number_length", 20);
        }
        HwLog.i(TAG, "get default sim number length.");
        return defaultSimNumLength;
    }
}
