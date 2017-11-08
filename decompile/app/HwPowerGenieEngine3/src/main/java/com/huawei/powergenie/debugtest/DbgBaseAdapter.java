package com.huawei.powergenie.debugtest;

import java.io.PrintWriter;

public abstract class DbgBaseAdapter {
    private PrintWriter mPw = null;

    protected void startTest(PrintWriter pw) {
        this.mPw = pw;
    }

    protected String getResult(boolean pass) {
        return pass ? "Pass" : "Fail";
    }

    protected void printlnResult(String target, Object msg) {
        if (this.mPw != null) {
            this.mPw.println("    " + target + " -> " + msg);
        }
    }
}
