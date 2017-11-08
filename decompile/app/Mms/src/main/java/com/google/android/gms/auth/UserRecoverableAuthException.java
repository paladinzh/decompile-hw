package com.google.android.gms.auth;

import android.content.Intent;

/* compiled from: Unknown */
public class UserRecoverableAuthException extends GoogleAuthException {
    private final Intent mIntent;

    public UserRecoverableAuthException(String msg, Intent intent) {
        super(msg);
        this.mIntent = intent;
    }

    public Intent getIntent() {
        return this.mIntent != null ? new Intent(this.mIntent) : null;
    }
}
