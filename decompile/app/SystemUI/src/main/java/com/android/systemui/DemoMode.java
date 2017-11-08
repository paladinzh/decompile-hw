package com.android.systemui;

import android.os.Bundle;

public interface DemoMode {
    void dispatchDemoCommand(String str, Bundle bundle);
}
