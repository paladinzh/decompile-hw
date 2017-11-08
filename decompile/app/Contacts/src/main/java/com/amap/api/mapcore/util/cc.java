package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: Log */
public class cc {
    public static final String a = "/a/";
    static final String b = "b";
    static final String c = "c";
    static final String d = "d";
    public static final String e = "e";

    public static String a(Context context, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getFilesDir().getAbsolutePath());
        stringBuilder.append(a);
        stringBuilder.append(str);
        return stringBuilder.toString();
    }

    public static Class<? extends ct> a(int i) {
        switch (i) {
            case 0:
                return co.class;
            case 1:
                return cq.class;
            case 2:
                return cn.class;
            default:
                return null;
        }
    }

    public static ct b(int i) {
        switch (i) {
            case 0:
                return new co();
            case 1:
                return new cq();
            case 2:
                return new cn();
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

    static ci a(Context context, int i) {
        ci cgVar;
        switch (i) {
            case 0:
                cgVar = new cg(i);
                break;
            case 1:
                cgVar = new ch(i);
                break;
            case 2:
                cgVar = new cf(i);
                break;
            default:
                return null;
        }
        return cgVar;
    }

    static void a(Context context, Throwable th, int i, String str, String str2) {
        try {
            ExecutorService c = ce.c();
            if (c != null && !c.isShutdown()) {
                final Context context2 = context;
                final int i2 = i;
                final Throwable th2 = th;
                final String str3 = str;
                final String str4 = str2;
                c.submit(new Runnable() {
                    public void run() {
                        try {
                            ci a = cc.a(context2, i2);
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
            ci a = a(context, 2);
            if (a != null) {
                a.b(context);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    static void b(final Context context) {
        try {
            ExecutorService c = ce.c();
            if (c != null && !c.isShutdown()) {
                c.submit(new Runnable() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        ci a;
                        ci a2;
                        Throwable th;
                        ci ciVar;
                        Throwable th2;
                        ci ciVar2;
                        ci ciVar3 = null;
                        try {
                            a = cc.a(context, 0);
                            try {
                                a2 = cc.a(context, 1);
                                try {
                                    ciVar3 = cc.a(context, 2);
                                    a.c(context);
                                    a2.c(context);
                                    ciVar3.c(context);
                                    bz.a(context);
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (ciVar3 != null) {
                                        ciVar3.c();
                                    }
                                } catch (RejectedExecutionException e) {
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (ciVar3 != null) {
                                        ciVar3.c();
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    ciVar = a;
                                    a = a2;
                                    a2 = ciVar3;
                                    th2 = th;
                                    if (ciVar != null) {
                                        ciVar.c();
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
                                a2 = ciVar3;
                                if (a != null) {
                                    a.c();
                                }
                                if (a2 != null) {
                                    a2.c();
                                }
                                if (ciVar3 != null) {
                                    ciVar3.c();
                                }
                            } catch (Throwable th4) {
                                ciVar = a;
                                a = ciVar3;
                                ciVar2 = ciVar3;
                                th2 = th4;
                                a2 = ciVar2;
                                if (ciVar != null) {
                                    ciVar.c();
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
                            a2 = ciVar3;
                            a = ciVar3;
                            if (a != null) {
                                a.c();
                            }
                            if (a2 != null) {
                                a2.c();
                            }
                            if (ciVar3 != null) {
                                ciVar3.c();
                            }
                        } catch (Throwable th42) {
                            a = ciVar3;
                            ciVar = ciVar3;
                            th = th42;
                            a2 = ciVar3;
                            th2 = th;
                            if (ciVar != null) {
                                ciVar.c();
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
            cb.a(th, "Log", "processLog");
        }
    }
}
