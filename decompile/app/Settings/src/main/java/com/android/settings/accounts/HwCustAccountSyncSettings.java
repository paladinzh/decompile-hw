package com.android.settings.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;

public class HwCustAccountSyncSettings {
    public void customizeAccountSync(Account account, Bundle extras) {
    }

    public void customizeAutoSync(Account account, Activity activity) {
    }

    public boolean shouldNotSkip(Account account) {
        return false;
    }
}
