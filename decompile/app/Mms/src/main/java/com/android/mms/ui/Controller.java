package com.android.mms.ui;

import android.app.Fragment;
import android.content.Intent;

public interface Controller {
    void finishFragment(Fragment fragment);

    void setResult(Fragment fragment, int i, Intent intent);

    void startComposeMessage(Intent intent);

    void startComposeMessage(Intent intent, int i, int i2);
}
