package com.android.settings;

public class StateSaverFactory {
    private static AbstractStateSaver mStateSaver = null;

    public static AbstractStateSaver getSaver() {
        if (mStateSaver == null) {
            mStateSaver = new StateSaverImpl();
        }
        return mStateSaver;
    }
}
