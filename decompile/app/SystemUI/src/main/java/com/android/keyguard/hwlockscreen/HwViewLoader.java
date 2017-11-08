package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.view.View;
import com.huawei.keyguard.amazinglockscreen.AmazingLockScreen;
import org.w3c.dom.Document;

public class HwViewLoader {
    public static View createView(Context context, Document document) {
        return new AmazingLockScreen(context, document);
    }
}
