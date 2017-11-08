package com.android.systemui.classifier;

import android.app.ActivityThread;
import android.app.Application;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;

public class FalsingLog {
    public static final boolean ENABLED = SystemProperties.getBoolean("debug.falsing_log", Build.IS_DEBUGGABLE);
    private static final boolean LOGCAT = SystemProperties.getBoolean("debug.falsing_logcat", false);
    private static final int MAX_SIZE = SystemProperties.getInt("debug.falsing_log_size", 100);
    private static FalsingLog sInstance;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);
    private final ArrayDeque<String> mLog = new ArrayDeque(MAX_SIZE);

    private FalsingLog() {
    }

    public static void i(String tag, String s) {
        if (LOGCAT) {
            Log.i("FalsingLog", tag + "\t" + s);
        }
        log("I", tag, s);
    }

    public static void e(String tag, String s) {
        if (LOGCAT) {
            Log.e("FalsingLog", tag + "\t" + s);
        }
        log("E", tag, s);
    }

    public static synchronized void log(String level, String tag, String s) {
        synchronized (FalsingLog.class) {
            if (ENABLED) {
                if (sInstance == null) {
                    sInstance = new FalsingLog();
                }
                if (sInstance.mLog.size() >= MAX_SIZE) {
                    sInstance.mLog.removeFirst();
                }
                sInstance.mLog.add(sInstance.mFormat.format(new Date()) + " " + level + " " + tag + " " + s);
                return;
            }
        }
    }

    public static synchronized void dump(PrintWriter pw) {
        synchronized (FalsingLog.class) {
            pw.println("FALSING LOG:");
            if (!ENABLED) {
                pw.println("Disabled, to enable: setprop debug.falsing_log 1");
                pw.println();
            } else if (sInstance == null || sInstance.mLog.isEmpty()) {
                pw.println("<empty>");
                pw.println();
            } else {
                for (String s : sInstance.mLog) {
                    pw.println(s);
                }
                pw.println();
            }
        }
    }

    public static synchronized void wtf(String tag, String s) {
        IOException e;
        Throwable th;
        synchronized (FalsingLog.class) {
            if (ENABLED) {
                e(tag, s);
                Application application = ActivityThread.currentApplication();
                String fileMessage = BuildConfig.FLAVOR;
                if (!Build.IS_DEBUGGABLE || application == null) {
                    Log.e("FalsingLog", "Unable to write log, build must be debuggable.");
                } else {
                    File f = new File(application.getDataDir(), "falsing-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt");
                    PrintWriter pw = null;
                    try {
                        PrintWriter pw2 = new PrintWriter(f);
                        try {
                            dump(pw2);
                            pw2.close();
                            fileMessage = "Log written to " + f.getAbsolutePath();
                            if (pw2 != null) {
                                pw2.close();
                            }
                        } catch (IOException e2) {
                            e = e2;
                            pw = pw2;
                            try {
                                Log.e("FalsingLog", "Unable to write falsing log", e);
                                if (pw != null) {
                                    pw.close();
                                }
                                Log.wtf("FalsingLog", tag + " " + s + "; " + fileMessage);
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                if (pw != null) {
                                    pw.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            pw = pw2;
                            if (pw != null) {
                                pw.close();
                            }
                            throw th;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        Log.e("FalsingLog", "Unable to write falsing log", e);
                        if (pw != null) {
                            pw.close();
                        }
                        Log.wtf("FalsingLog", tag + " " + s + "; " + fileMessage);
                        return;
                    }
                }
                Log.wtf("FalsingLog", tag + " " + s + "; " + fileMessage);
                return;
            }
        }
    }
}
