package com.google.android.gms.auth.firstparty.shared;

/* compiled from: Unknown */
public enum zzd {
    CLIENT_LOGIN_DISABLED("ClientLoginDisabled"),
    DEVICE_MANAGEMENT_REQUIRED("DeviceManagementRequiredOrSyncDisabled"),
    SOCKET_TIMEOUT("SocketTimeout"),
    SUCCESS("Ok"),
    UNKNOWN_ERROR("UNKNOWN_ERR"),
    NETWORK_ERROR("NetworkError"),
    SERVICE_UNAVAILABLE("ServiceUnavailable"),
    INTNERNAL_ERROR("InternalError"),
    BAD_AUTHENTICATION("BadAuthentication"),
    EMPTY_CONSUMER_PKG_OR_SIG("EmptyConsumerPackageOrSig"),
    NEEDS_2F("InvalidSecondFactor"),
    NEEDS_POST_SIGN_IN_FLOW("PostSignInFlowRequired"),
    NEEDS_BROWSER("NeedsBrowser"),
    UNKNOWN("Unknown"),
    NOT_VERIFIED("NotVerified"),
    TERMS_NOT_AGREED("TermsNotAgreed"),
    ACCOUNT_DISABLED("AccountDisabled"),
    CAPTCHA("CaptchaRequired"),
    ACCOUNT_DELETED("AccountDeleted"),
    SERVICE_DISABLED("ServiceDisabled"),
    NEED_PERMISSION("NeedPermission"),
    INVALID_SCOPE("INVALID_SCOPE"),
    USER_CANCEL("UserCancel"),
    PERMISSION_DENIED("PermissionDenied"),
    THIRD_PARTY_DEVICE_MANAGEMENT_REQUIRED("ThirdPartyDeviceManagementRequired"),
    DM_INTERNAL_ERROR("DeviceManagementInternalError"),
    DM_SYNC_DISABLED("DeviceManagementSyncDisabled"),
    DM_ADMIN_BLOCKED("DeviceManagementAdminBlocked"),
    DM_ADMIN_PENDING_APPROVAL("DeviceManagementAdminPendingApproval"),
    DM_STALE_SYNC_REQUIRED("DeviceManagementStaleSyncRequired"),
    DM_DEACTIVATED("DeviceManagementDeactivated"),
    DM_REQUIRED("DeviceManagementRequired"),
    REAUTH_REQUIRED("ReauthRequired"),
    ALREADY_HAS_GMAIL("ALREADY_HAS_GMAIL"),
    BAD_PASSWORD("WeakPassword"),
    BAD_REQUEST("BadRequest"),
    BAD_USERNAME("BadUsername"),
    DELETED_GMAIL("DeletedGmail"),
    EXISTING_USERNAME("ExistingUsername"),
    LOGIN_FAIL("LoginFail"),
    NOT_LOGGED_IN("NotLoggedIn"),
    NO_GMAIL("NoGmail"),
    REQUEST_DENIED("RequestDenied"),
    SERVER_ERROR("ServerError"),
    USERNAME_UNAVAILABLE("UsernameUnavailable"),
    GPLUS_OTHER("GPlusOther"),
    GPLUS_NICKNAME("GPlusNickname"),
    GPLUS_INVALID_CHAR("GPlusInvalidChar"),
    GPLUS_INTERSTITIAL("GPlusInterstitial"),
    GPLUS_PROFILE_ERROR("ProfileUpgradeError");
    
    private final String zzZA;

    private zzd(String str) {
        this.zzZA = str;
    }

    public static boolean zza(zzd zzd) {
        return BAD_AUTHENTICATION.equals(zzd) || CAPTCHA.equals(zzd) || NEED_PERMISSION.equals(zzd) || NEEDS_BROWSER.equals(zzd) || USER_CANCEL.equals(zzd) || THIRD_PARTY_DEVICE_MANAGEMENT_REQUIRED.equals(zzd) || zzb(zzd);
    }

    public static boolean zzb(zzd zzd) {
        return DEVICE_MANAGEMENT_REQUIRED.equals(zzd) || DM_INTERNAL_ERROR.equals(zzd) || DM_SYNC_DISABLED.equals(zzd) || DM_ADMIN_BLOCKED.equals(zzd) || DM_ADMIN_PENDING_APPROVAL.equals(zzd) || DM_STALE_SYNC_REQUIRED.equals(zzd) || DM_DEACTIVATED.equals(zzd) || DM_REQUIRED.equals(zzd);
    }

    public static final zzd zzbY(String str) {
        zzd zzd = null;
        for (zzd zzd2 : values()) {
            if (zzd2.zzZA.equals(str)) {
                zzd = zzd2;
            }
        }
        return zzd;
    }

    public static boolean zzc(zzd zzd) {
        return NETWORK_ERROR.equals(zzd) || SERVICE_UNAVAILABLE.equals(zzd);
    }
}
