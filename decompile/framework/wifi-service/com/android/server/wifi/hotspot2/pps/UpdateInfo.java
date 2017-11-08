package com.android.server.wifi.hotspot2.pps;

import android.util.Base64;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.OMAException;
import com.android.server.wifi.hotspot2.omadm.OMANode;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import java.nio.charset.StandardCharsets;

public class UpdateInfo {
    private final String mCertFP;
    private final String mCertURL;
    private final long mInterval;
    private final String mPassword;
    private final boolean mSPPClientInitiated;
    private final String mURI;
    private final UpdateRestriction mUpdateRestriction;
    private final String mUsername;

    public enum UpdateRestriction {
        HomeSP,
        RoamingPartner,
        Unrestricted
    }

    public UpdateInfo(OMANode policyUpdate) throws OMAException {
        this.mInterval = PasspointManagementObjectManager.getLong(policyUpdate, PasspointManagementObjectManager.TAG_UpdateInterval, null) * PasspointManagementObjectManager.IntervalFactor;
        this.mSPPClientInitiated = ((Boolean) PasspointManagementObjectManager.getSelection(policyUpdate, PasspointManagementObjectManager.TAG_UpdateMethod)).booleanValue();
        this.mUpdateRestriction = (UpdateRestriction) PasspointManagementObjectManager.getSelection(policyUpdate, PasspointManagementObjectManager.TAG_Restriction);
        this.mURI = PasspointManagementObjectManager.getString(policyUpdate, PasspointManagementObjectManager.TAG_URI);
        OMANode unp = policyUpdate.getChild(PasspointManagementObjectManager.TAG_UsernamePassword);
        if (unp != null) {
            this.mUsername = PasspointManagementObjectManager.getString(unp.getChild(PasspointManagementObjectManager.TAG_Username));
            this.mPassword = new String(Base64.decode(PasspointManagementObjectManager.getString(unp.getChild(PasspointManagementObjectManager.TAG_Password)).getBytes(StandardCharsets.US_ASCII), 0), StandardCharsets.UTF_8);
        } else {
            this.mUsername = null;
            this.mPassword = null;
        }
        OMANode trustRoot = PasspointManagementObjectManager.getChild(policyUpdate, PasspointManagementObjectManager.TAG_TrustRoot);
        this.mCertURL = PasspointManagementObjectManager.getString(trustRoot, PasspointManagementObjectManager.TAG_CertURL);
        this.mCertFP = PasspointManagementObjectManager.getString(trustRoot, PasspointManagementObjectManager.TAG_CertSHA256Fingerprint);
    }

    public long getInterval() {
        return this.mInterval;
    }

    public boolean isSPPClientInitiated() {
        return this.mSPPClientInitiated;
    }

    public UpdateRestriction getUpdateRestriction() {
        return this.mUpdateRestriction;
    }

    public String getURI() {
        return this.mURI;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public String getCertURL() {
        return this.mCertURL;
    }

    public String getCertFP() {
        return this.mCertFP;
    }

    public String toString() {
        return "UpdateInfo{interval=" + Utils.toHMS(this.mInterval) + ", SPPClientInitiated=" + this.mSPPClientInitiated + ", updateRestriction=" + this.mUpdateRestriction + ", URI='" + this.mURI + '\'' + ", username='" + this.mUsername + '\'' + ", password=" + this.mPassword + ", certURL='" + this.mCertURL + '\'' + ", certFP='" + this.mCertFP + '\'' + '}';
    }
}
