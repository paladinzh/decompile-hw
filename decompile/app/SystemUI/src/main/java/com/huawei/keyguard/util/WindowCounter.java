package com.huawei.keyguard.util;

public class WindowCounter {
    private Class mClz;
    private int mRefCounter = 0;

    public WindowCounter(Class clz) {
        this.mClz = clz;
    }

    public void onAttach() {
        this.mRefCounter++;
        if (this.mRefCounter > 1) {
            HwLog.w("WindowCounter", "Window maybe leaked. [" + this.mClz.getName() + "]  " + (this.mRefCounter - 1));
        }
    }

    public void onDetach() {
        this.mRefCounter--;
    }
}
