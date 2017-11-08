package com.android.server.wifi.anqp.eap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class EAP {
    public static final int CredentialType = 5;
    public static final int EAP_3Com = 24;
    public static final int EAP_AKA = 23;
    public static final int EAP_AKAPrim = 50;
    public static final int EAP_ActiontecWireless = 35;
    public static final int EAP_EKE = 53;
    public static final int EAP_FAST = 43;
    public static final int EAP_GPSK = 51;
    public static final int EAP_HTTPDigest = 38;
    public static final int EAP_IKEv2 = 49;
    public static final int EAP_KEA = 11;
    public static final int EAP_KEA_VALIDATE = 12;
    public static final int EAP_LEAP = 17;
    public static final int EAP_Link = 45;
    public static final int EAP_MD5 = 4;
    public static final int EAP_MOBAC = 42;
    public static final int EAP_MSCHAPv2 = 26;
    public static final int EAP_OTP = 5;
    public static final int EAP_PAX = 46;
    public static final int EAP_PEAP = 29;
    public static final int EAP_POTP = 32;
    public static final int EAP_PSK = 47;
    public static final int EAP_PWD = 52;
    public static final int EAP_RSA = 9;
    public static final int EAP_SAKE = 48;
    public static final int EAP_SIM = 18;
    public static final int EAP_SPEKE = 41;
    public static final int EAP_TEAP = 55;
    public static final int EAP_TLS = 13;
    public static final int EAP_TTLS = 21;
    public static final int EAP_ZLXEAP = 44;
    public static final int ExpandedEAPMethod = 1;
    public static final int ExpandedInnerEAPMethod = 4;
    public static final int InnerAuthEAPMethodType = 3;
    public static final int NonEAPInnerAuthType = 2;
    public static final int TunneledEAPMethodCredType = 6;
    public static final int VendorSpecific = 221;
    private static final Map<Integer, AuthInfoID> sAuthIds = new HashMap();
    private static final Map<Integer, EAPMethodID> sEapIds = new HashMap();
    private static final Map<EAPMethodID, Integer> sRevEapIds = new HashMap();

    public enum AuthInfoID {
        Undefined,
        ExpandedEAPMethod,
        NonEAPInnerAuthType,
        InnerAuthEAPMethodType,
        ExpandedInnerEAPMethod,
        CredentialType,
        TunneledEAPMethodCredType,
        VendorSpecific
    }

    public enum EAPMethodID {
        EAP_MD5,
        EAP_OTP,
        EAP_RSA,
        EAP_KEA,
        EAP_KEA_VALIDATE,
        EAP_TLS,
        EAP_LEAP,
        EAP_SIM,
        EAP_TTLS,
        EAP_AKA,
        EAP_3Com,
        EAP_MSCHAPv2,
        EAP_PEAP,
        EAP_POTP,
        EAP_ActiontecWireless,
        EAP_HTTPDigest,
        EAP_SPEKE,
        EAP_MOBAC,
        EAP_FAST,
        EAP_ZLXEAP,
        EAP_Link,
        EAP_PAX,
        EAP_PSK,
        EAP_SAKE,
        EAP_IKEv2,
        EAP_AKAPrim,
        EAP_GPSK,
        EAP_PWD,
        EAP_EKE,
        EAP_TEAP
    }

    static {
        sEapIds.put(Integer.valueOf(4), EAPMethodID.EAP_MD5);
        sEapIds.put(Integer.valueOf(5), EAPMethodID.EAP_OTP);
        sEapIds.put(Integer.valueOf(9), EAPMethodID.EAP_RSA);
        sEapIds.put(Integer.valueOf(11), EAPMethodID.EAP_KEA);
        sEapIds.put(Integer.valueOf(12), EAPMethodID.EAP_KEA_VALIDATE);
        sEapIds.put(Integer.valueOf(13), EAPMethodID.EAP_TLS);
        sEapIds.put(Integer.valueOf(17), EAPMethodID.EAP_LEAP);
        sEapIds.put(Integer.valueOf(18), EAPMethodID.EAP_SIM);
        sEapIds.put(Integer.valueOf(21), EAPMethodID.EAP_TTLS);
        sEapIds.put(Integer.valueOf(23), EAPMethodID.EAP_AKA);
        sEapIds.put(Integer.valueOf(24), EAPMethodID.EAP_3Com);
        sEapIds.put(Integer.valueOf(26), EAPMethodID.EAP_MSCHAPv2);
        sEapIds.put(Integer.valueOf(29), EAPMethodID.EAP_PEAP);
        sEapIds.put(Integer.valueOf(32), EAPMethodID.EAP_POTP);
        sEapIds.put(Integer.valueOf(35), EAPMethodID.EAP_ActiontecWireless);
        sEapIds.put(Integer.valueOf(38), EAPMethodID.EAP_HTTPDigest);
        sEapIds.put(Integer.valueOf(41), EAPMethodID.EAP_SPEKE);
        sEapIds.put(Integer.valueOf(42), EAPMethodID.EAP_MOBAC);
        sEapIds.put(Integer.valueOf(43), EAPMethodID.EAP_FAST);
        sEapIds.put(Integer.valueOf(44), EAPMethodID.EAP_ZLXEAP);
        sEapIds.put(Integer.valueOf(45), EAPMethodID.EAP_Link);
        sEapIds.put(Integer.valueOf(46), EAPMethodID.EAP_PAX);
        sEapIds.put(Integer.valueOf(47), EAPMethodID.EAP_PSK);
        sEapIds.put(Integer.valueOf(48), EAPMethodID.EAP_SAKE);
        sEapIds.put(Integer.valueOf(49), EAPMethodID.EAP_IKEv2);
        sEapIds.put(Integer.valueOf(50), EAPMethodID.EAP_AKAPrim);
        sEapIds.put(Integer.valueOf(51), EAPMethodID.EAP_GPSK);
        sEapIds.put(Integer.valueOf(52), EAPMethodID.EAP_PWD);
        sEapIds.put(Integer.valueOf(53), EAPMethodID.EAP_EKE);
        sEapIds.put(Integer.valueOf(55), EAPMethodID.EAP_TEAP);
        for (Entry<Integer, EAPMethodID> entry : sEapIds.entrySet()) {
            sRevEapIds.put((EAPMethodID) entry.getValue(), (Integer) entry.getKey());
        }
        sAuthIds.put(Integer.valueOf(1), AuthInfoID.ExpandedEAPMethod);
        sAuthIds.put(Integer.valueOf(2), AuthInfoID.NonEAPInnerAuthType);
        sAuthIds.put(Integer.valueOf(3), AuthInfoID.InnerAuthEAPMethodType);
        sAuthIds.put(Integer.valueOf(4), AuthInfoID.ExpandedInnerEAPMethod);
        sAuthIds.put(Integer.valueOf(5), AuthInfoID.CredentialType);
        sAuthIds.put(Integer.valueOf(6), AuthInfoID.TunneledEAPMethodCredType);
        sAuthIds.put(Integer.valueOf(VendorSpecific), AuthInfoID.VendorSpecific);
    }

    public static EAPMethodID mapEAPMethod(int methodID) {
        return (EAPMethodID) sEapIds.get(Integer.valueOf(methodID));
    }

    public static Integer mapEAPMethod(EAPMethodID methodID) {
        return (Integer) sRevEapIds.get(methodID);
    }

    public static AuthInfoID mapAuthMethod(int methodID) {
        return (AuthInfoID) sAuthIds.get(Integer.valueOf(methodID));
    }
}
