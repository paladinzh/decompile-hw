package com.huawei.systemmanager.rainbow.client.background.handle;

import android.content.Context;
import android.content.Intent;
import com.google.javax.annotation.Nonnull;

public interface IIntentHandler {
    void handleIntent(Context context, @Nonnull Intent intent);
}
