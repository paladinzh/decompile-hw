package com.android.internal.telephony.dataconnection;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import java.util.HashMap;

public enum DcFailCause {
    NONE(0),
    OPERATOR_BARRED(8),
    NAS_SIGNALLING(14),
    LLC_SNDCP(25),
    INSUFFICIENT_RESOURCES(26),
    MISSING_UNKNOWN_APN(27),
    UNKNOWN_PDP_ADDRESS_TYPE(28),
    USER_AUTHENTICATION(29),
    ACTIVATION_REJECT_GGSN(30),
    ACTIVATION_REJECT_UNSPECIFIED(31),
    SERVICE_OPTION_NOT_SUPPORTED(32),
    SERVICE_OPTION_NOT_SUBSCRIBED(33),
    SERVICE_OPTION_OUT_OF_ORDER(34),
    NSAPI_IN_USE(35),
    REGULAR_DEACTIVATION(36),
    QOS_NOT_ACCEPTED(37),
    NETWORK_FAILURE(38),
    UMTS_REACTIVATION_REQ(39),
    FEATURE_NOT_SUPP(40),
    TFT_SEMANTIC_ERROR(41),
    TFT_SYTAX_ERROR(42),
    UNKNOWN_PDP_CONTEXT(43),
    FILTER_SEMANTIC_ERROR(44),
    FILTER_SYTAX_ERROR(45),
    PDP_WITHOUT_ACTIVE_TFT(46),
    ONLY_IPV4_ALLOWED(50),
    ONLY_IPV6_ALLOWED(51),
    ONLY_SINGLE_BEARER_ALLOWED(52),
    ESM_INFO_NOT_RECEIVED(53),
    PDN_CONN_DOES_NOT_EXIST(54),
    MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED(55),
    MAX_ACTIVE_PDP_CONTEXT_REACHED(65),
    UNSUPPORTED_APN_IN_CURRENT_PLMN(66),
    INVALID_TRANSACTION_ID(81),
    MESSAGE_INCORRECT_SEMANTIC(95),
    INVALID_MANDATORY_INFO(96),
    MESSAGE_TYPE_UNSUPPORTED(97),
    MSG_TYPE_NONCOMPATIBLE_STATE(98),
    UNKNOWN_INFO_ELEMENT(99),
    CONDITIONAL_IE_ERROR(100),
    MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE(CallFailCause.MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE),
    PROTOCOL_ERRORS(111),
    APN_TYPE_CONFLICT(112),
    INVALID_PCSCF_ADDR(113),
    INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN(114),
    EMM_ACCESS_BARRED(115),
    EMERGENCY_IFACE_ONLY(116),
    IFACE_MISMATCH(117),
    COMPANION_IFACE_IN_USE(118),
    IP_ADDRESS_MISMATCH(119),
    IFACE_AND_POL_FAMILY_MISMATCH(AbstractPhoneBase.BUFFER_SIZE),
    EMM_ACCESS_BARRED_INFINITE_RETRY(121),
    AUTH_FAILURE_ON_EMERGENCY_CALL(122),
    OEM_DCFAILCAUSE_1(SmsEnvelope.SERVICE_CATEGORY_CMAS_EXTREME_THREAT),
    OEM_DCFAILCAUSE_2(4098),
    OEM_DCFAILCAUSE_3(4099),
    OEM_DCFAILCAUSE_4(4100),
    OEM_DCFAILCAUSE_5(SmsEnvelope.TELESERVICE_WEMT),
    OEM_DCFAILCAUSE_6(SmsEnvelope.TELESERVICE_SCPT),
    OEM_DCFAILCAUSE_7(4103),
    OEM_DCFAILCAUSE_8(4104),
    OEM_DCFAILCAUSE_9(4105),
    OEM_DCFAILCAUSE_10(4106),
    OEM_DCFAILCAUSE_11(4107),
    OEM_DCFAILCAUSE_12(4108),
    OEM_DCFAILCAUSE_13(4109),
    OEM_DCFAILCAUSE_14(4110),
    OEM_DCFAILCAUSE_15(4111),
    REGISTRATION_FAIL(-1),
    GPRS_REGISTRATION_FAIL(-2),
    SIGNAL_LOST(-3),
    PREF_RADIO_TECH_CHANGED(-4),
    RADIO_POWER_OFF(-5),
    TETHERED_CALL_ACTIVE(-6),
    ERROR_UNSPECIFIED(65535),
    PDP_ACTIVE_LIMIT(2051),
    UNKNOWN(65536),
    RADIO_NOT_AVAILABLE(65537),
    UNACCEPTABLE_NETWORK_PARAMETER(65538),
    CONNECTION_TO_DATACONNECTIONAC_BROKEN(65539),
    LOST_CONNECTION(65540),
    RESET_BY_FRAMEWORK(65541);
    
    private static final HashMap<Integer, DcFailCause> sErrorCodeToFailCauseMap = null;
    private final int mErrorCode;
    private final boolean mRestartRadioOnRegularDeactivation;

    static {
        sErrorCodeToFailCauseMap = new HashMap();
        DcFailCause[] values = values();
        int length = values.length;
        int i;
        while (i < length) {
            DcFailCause fc = values[i];
            sErrorCodeToFailCauseMap.put(Integer.valueOf(fc.getErrorCode()), fc);
            i++;
        }
    }

    private DcFailCause(int errorCode) {
        this.mRestartRadioOnRegularDeactivation = Resources.getSystem().getBoolean(17957017);
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public boolean isRestartRadioFail() {
        return this == REGULAR_DEACTIVATION ? this.mRestartRadioOnRegularDeactivation : false;
    }

    public boolean isPermanentFail() {
        boolean z = true;
        if (isMatchedDsFail()) {
            return false;
        }
        if (!(this == OPERATOR_BARRED || this == MISSING_UNKNOWN_APN || this == UNKNOWN_PDP_ADDRESS_TYPE || this == USER_AUTHENTICATION || this == ACTIVATION_REJECT_GGSN || this == SERVICE_OPTION_NOT_SUPPORTED || this == SERVICE_OPTION_NOT_SUBSCRIBED || this == NSAPI_IN_USE || this == ONLY_IPV4_ALLOWED || this == ONLY_IPV6_ALLOWED || this == PROTOCOL_ERRORS || this == RADIO_POWER_OFF || this == TETHERED_CALL_ACTIVE || this == RADIO_NOT_AVAILABLE || this == UNACCEPTABLE_NETWORK_PARAMETER || this == SIGNAL_LOST)) {
            z = false;
        }
        return z;
    }

    public boolean isEventLoggable() {
        if (this == OPERATOR_BARRED || this == INSUFFICIENT_RESOURCES || this == UNKNOWN_PDP_ADDRESS_TYPE || this == USER_AUTHENTICATION || this == ACTIVATION_REJECT_GGSN || this == ACTIVATION_REJECT_UNSPECIFIED || this == SERVICE_OPTION_NOT_SUBSCRIBED || this == SERVICE_OPTION_NOT_SUPPORTED || this == SERVICE_OPTION_OUT_OF_ORDER || this == NSAPI_IN_USE || this == ONLY_IPV4_ALLOWED || this == ONLY_IPV6_ALLOWED || this == PROTOCOL_ERRORS || this == SIGNAL_LOST || this == RADIO_POWER_OFF || this == TETHERED_CALL_ACTIVE || this == UNACCEPTABLE_NETWORK_PARAMETER) {
            return true;
        }
        return false;
    }

    public static DcFailCause fromInt(int errorCode) {
        DcFailCause fc = (DcFailCause) sErrorCodeToFailCauseMap.get(Integer.valueOf(errorCode));
        if (fc == null) {
            return UNKNOWN;
        }
        return fc;
    }

    private boolean isMatchedDsFail() {
        boolean isMatched = false;
        try {
            String cntelfailcau = SystemProperties.get("ro.hwpp_ds_fail", "");
            Rlog.d("DcFailCause", "isMatchedDsFail cntelfailcau: " + cntelfailcau);
            for (String fcau : cntelfailcau.split(",")) {
                if (Integer.toString(this.mErrorCode).equals(fcau)) {
                    Rlog.d("DcFailCause", "ErrorCode has been matched: " + this.mErrorCode);
                    isMatched = true;
                }
            }
        } catch (Exception ex) {
            Rlog.e("DcFailCause", "Exception isMatchedDsFail get ds fail cause, ", ex);
        }
        return isMatched;
    }
}
