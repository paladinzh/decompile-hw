package com.android.settings.wifi.ap;

import android.text.method.NumberKeyListener;

public class MacKeyListener extends NumberKeyListener {
    private static final char[] CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F', ':'};
    private static MacKeyListener sInstance;

    protected char[] getAcceptedChars() {
        return CHARACTERS;
    }

    public int getInputType() {
        return 524288;
    }

    public static synchronized MacKeyListener getInstance() {
        MacKeyListener macKeyListener;
        synchronized (MacKeyListener.class) {
            if (sInstance == null) {
                sInstance = new MacKeyListener();
            }
            macKeyListener = sInstance;
        }
        return macKeyListener;
    }
}
