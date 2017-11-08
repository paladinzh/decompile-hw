package com.android.settings;

import android.os.Bundle;
import android.security.KeyStore;

public class HwCustCredentialStorage {
    public HwCustCredentialStorage(CredentialStorage credentialStorage) {
    }

    public boolean installIfAvailable(Bundle bundle, KeyStore mKeyStore, int uid, int flags) {
        return false;
    }

    public void resetKeyStore(KeyStore mKeyStore) {
    }
}
