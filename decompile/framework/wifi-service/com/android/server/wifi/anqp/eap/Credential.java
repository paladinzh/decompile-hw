package com.android.server.wifi.anqp.eap;

import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class Credential implements AuthParam {
    private final AuthInfoID mAuthInfoID;
    private final CredType mCredType;

    public enum CredType {
        Reserved,
        SIM,
        USIM,
        NFC,
        HWToken,
        Softoken,
        Certificate,
        Username,
        None,
        Anonymous,
        VendorSpecific
    }

    public Credential(AuthInfoID infoID, int length, ByteBuffer payload) throws ProtocolException {
        if (length != 1) {
            throw new ProtocolException("Bad length: " + length);
        }
        CredType credType;
        this.mAuthInfoID = infoID;
        int typeID = payload.get() & 255;
        if (typeID < CredType.values().length) {
            credType = CredType.values()[typeID];
        } else {
            credType = CredType.Reserved;
        }
        this.mCredType = credType;
    }

    public AuthInfoID getAuthInfoID() {
        return this.mAuthInfoID;
    }

    public int hashCode() {
        return (this.mAuthInfoID.hashCode() * 31) + this.mCredType.hashCode();
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (thatObject == null || thatObject.getClass() != Credential.class) {
            return false;
        }
        if (((Credential) thatObject).getCredType() != getCredType()) {
            z = false;
        }
        return z;
    }

    public CredType getCredType() {
        return this.mCredType;
    }

    public String toString() {
        return "Auth method " + this.mAuthInfoID + " = " + this.mCredType + "\n";
    }
}
