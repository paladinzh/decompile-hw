package com.amap.api.services.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/* compiled from: Utils */
public class bj {
    static synchronized String a(Throwable th) {
        Writer stringWriter;
        Throwable cause;
        synchronized (bj.class) {
            PrintWriter printWriter;
            try {
                stringWriter = new StringWriter();
                try {
                    printWriter = new PrintWriter(stringWriter);
                    try {
                        th.printStackTrace(printWriter);
                        for (cause = th.getCause(); cause != null; cause = cause.getCause()) {
                            cause.printStackTrace(printWriter);
                        }
                        String replaceAll = stringWriter.toString().replaceAll("\n", "<br/>");
                        if (stringWriter != null) {
                            try {
                                stringWriter.close();
                            } catch (Throwable th2) {
                                th2.printStackTrace();
                            }
                        }
                        if (printWriter != null) {
                            try {
                                printWriter.close();
                            } catch (Throwable th3) {
                                th3.printStackTrace();
                            }
                        }
                    } catch (Throwable th4) {
                        cause = th4;
                    }
                } catch (Throwable th5) {
                    cause = th5;
                    printWriter = null;
                    if (stringWriter != null) {
                        stringWriter.close();
                    }
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    throw cause;
                }
            } catch (Throwable th6) {
                cause = th6;
                printWriter = null;
                stringWriter = null;
                if (stringWriter != null) {
                    stringWriter.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
                throw cause;
            }
        }
        return replaceAll;
    }

    public static String a(long j) {
        try {
            return new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS").format(new Date(j));
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
