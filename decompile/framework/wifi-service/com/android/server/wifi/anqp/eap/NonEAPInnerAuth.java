package com.android.server.wifi.anqp.eap;

import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NonEAPInnerAuth implements AuthParam {
    private static final Map<NonEAPType, String> sOmaMap = new EnumMap(NonEAPType.class);
    private static final Map<String, NonEAPType> sRevOmaMap = new HashMap();
    private final NonEAPType mType;

    public enum NonEAPType {
        Reserved,
        PAP,
        CHAP,
        MSCHAP,
        MSCHAPv2
    }

    static {
        sOmaMap.put(NonEAPType.PAP, "PAP");
        sOmaMap.put(NonEAPType.CHAP, "CHAP");
        sOmaMap.put(NonEAPType.MSCHAP, "MS-CHAP");
        sOmaMap.put(NonEAPType.MSCHAPv2, "MS-CHAP-V2");
        for (Entry<NonEAPType, String> entry : sOmaMap.entrySet()) {
            sRevOmaMap.put((String) entry.getValue(), (NonEAPType) entry.getKey());
        }
    }

    public NonEAPInnerAuth(int length, ByteBuffer payload) throws ProtocolException {
        if (length != 1) {
            throw new ProtocolException("Bad length: " + payload.remaining());
        }
        NonEAPType nonEAPType;
        int typeID = payload.get() & 255;
        if (typeID < NonEAPType.values().length) {
            nonEAPType = NonEAPType.values()[typeID];
        } else {
            nonEAPType = NonEAPType.Reserved;
        }
        this.mType = nonEAPType;
    }

    public NonEAPInnerAuth(NonEAPType type) {
        this.mType = type;
    }

    public NonEAPInnerAuth(String eapType) {
        this.mType = (NonEAPType) sRevOmaMap.get(eapType);
    }

    public AuthInfoID getAuthInfoID() {
        return AuthInfoID.NonEAPInnerAuthType;
    }

    public NonEAPType getType() {
        return this.mType;
    }

    public String getOMAtype() {
        return (String) sOmaMap.get(this.mType);
    }

    public static String mapInnerType(NonEAPType type) {
        return (String) sOmaMap.get(type);
    }

    public int hashCode() {
        return this.mType.hashCode();
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (thatObject == null || thatObject.getClass() != NonEAPInnerAuth.class) {
            return false;
        }
        if (((NonEAPInnerAuth) thatObject).getType() != getType()) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "Auth method NonEAPInnerAuthEAP, inner = " + this.mType + '\n';
    }
}
