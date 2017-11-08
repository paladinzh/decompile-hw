package com.huawei.systemmanager.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DXLogHelper {
    public static final String TAG = "DX-Optimizer";

    public static void v(String subTag, String msg) {
        HwLog.v(TAG, getLogMsg(subTag, msg));
    }

    public static void d(String subTag, String msg) {
        HwLog.d(TAG, getLogMsg(subTag, msg));
    }

    public static void i(String subTag, String msg) {
        HwLog.i(TAG, getLogMsg(subTag, msg));
    }

    public static void w(String subTag, String msg) {
        HwLog.w(TAG, getLogMsg(subTag, msg));
    }

    public static void w(String subTag, String msg, Throwable e) {
        HwLog.w(TAG, getLogMsg(subTag, msg + " Exception: " + getExceptionMsg(e)));
    }

    public static void e(String subTag, String msg) {
        HwLog.e(TAG, getLogMsg(subTag, msg));
    }

    public static void e(String subTag, String msg, Throwable e) {
        HwLog.e(TAG, getLogMsg(subTag, msg + " Exception: " + getExceptionMsg(e)));
    }

    private static String getLogMsg(String subTag, String msg) {
        return "[" + subTag + "] " + msg;
    }

    private static String getExceptionMsg(Throwable e) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
