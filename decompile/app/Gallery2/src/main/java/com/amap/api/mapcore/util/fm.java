package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: Log */
public class fm {
    public static final String a = "/a/";
    static final String b = "b";
    static final String c = "c";
    static final String d = "d";
    public static final String e = "e";
    public static final String f = "f";

    public static String a(Context context, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getFilesDir().getAbsolutePath());
        stringBuilder.append(a);
        stringBuilder.append(str);
        return stringBuilder.toString();
    }

    public static Class<? extends gd> a(int i) {
        switch (i) {
            case 0:
                return fy.class;
            case 1:
                return ga.class;
            case 2:
                return fx.class;
            default:
                return null;
        }
    }

    public static gd b(int i) {
        switch (i) {
            case 0:
                return new fy();
            case 1:
                return new ga();
            case 2:
                return new fx();
            default:
                return null;
        }
    }

    public static String c(int i) {
        switch (i) {
            case 0:
                return c;
            case 1:
                return b;
            case 2:
                return d;
            default:
                return "";
        }
    }

    static fs a(Context context, int i) {
        fs fqVar;
        switch (i) {
            case 0:
                fqVar = new fq(i);
                break;
            case 1:
                fqVar = new fr(i);
                break;
            case 2:
                fqVar = new fp(i);
                break;
            default:
                return null;
        }
        return fqVar;
    }

    static void a(Context context, Throwable th, int i, String str, String str2) {
        try {
            ExecutorService c = fo.c();
            if (c != null && !c.isShutdown()) {
                final Context context2 = context;
                final int i2 = i;
                final Throwable th2 = th;
                final String str3 = str;
                final String str4 = str2;
                c.submit(new Runnable() {
                    public void run() {
                        try {
                            fs a = fm.a(context2, i2);
                            if (a != null) {
                                a.a(context2, th2, str3, str4);
                            }
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            }
        } catch (RejectedExecutionException e) {
        } catch (Throwable th3) {
            th3.printStackTrace();
        }
    }

    static void a(Context context) {
        try {
            fs a = a(context, 2);
            if (a != null) {
                a.b(context);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    static void b(final Context context) {
        try {
            ExecutorService c = fo.c();
            if (c != null && !c.isShutdown()) {
                c.submit(new Runnable() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        fs a;
                        fs a2;
                        Throwable th;
                        fs fsVar;
                        Throwable th2;
                        fs fsVar2;
                        fs fsVar3 = null;
                        try {
                            a = fm.a(context, 0);
                            try {
                                a2 = fm.a(context, 1);
                                try {
                                    fsVar3 = fm.a(context, 2);
                                    a.c(context);
                                    a2.c(context);
                                    fsVar3.c(context);
                                    hj.a(context);
                                    hh.a(context);
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (fsVar3 != null) {
                                        fsVar3.c();
                                    }
                                } catch (RejectedExecutionException e) {
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (fsVar3 != null) {
                                        fsVar3.c();
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    fsVar = a;
                                    a = a2;
                                    a2 = fsVar3;
                                    th2 = th;
                                    if (fsVar != null) {
                                        fsVar.c();
                                    }
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    throw th2;
                                }
                            } catch (RejectedExecutionException e2) {
                                a2 = fsVar3;
                                if (a != null) {
                                    a.c();
                                }
                                if (a2 != null) {
                                    a2.c();
                                }
                                if (fsVar3 != null) {
                                    fsVar3.c();
                                }
                            } catch (Throwable th4) {
                                fsVar = a;
                                a = fsVar3;
                                fsVar2 = fsVar3;
                                th2 = th4;
                                a2 = fsVar2;
                                if (fsVar != null) {
                                    fsVar.c();
                                }
                                if (a != null) {
                                    a.c();
                                }
                                if (a2 != null) {
                                    a2.c();
                                }
                                throw th2;
                            }
                        } catch (RejectedExecutionException e3) {
                            a2 = fsVar3;
                            a = fsVar3;
                            if (a != null) {
                                a.c();
                            }
                            if (a2 != null) {
                                a2.c();
                            }
                            if (fsVar3 != null) {
                                fsVar3.c();
                            }
                        } catch (Throwable th42) {
                            a = fsVar3;
                            fsVar = fsVar3;
                            th = th42;
                            a2 = fsVar3;
                            th2 = th;
                            if (fsVar != null) {
                                fsVar.c();
                            }
                            if (a != null) {
                                a.c();
                            }
                            if (a2 != null) {
                                a2.c();
                            }
                            throw th2;
                        }
                    }
                });
            }
        } catch (Throwable th) {
            fl.a(th, "Log", "processLog");
        }
    }
}
