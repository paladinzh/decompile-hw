package com.amap.api.services.core;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: Log */
public class bf {
    static final String a = "/a/";
    static final String b = "b";
    static final String c = "c";
    static final String d = "d";

    static void a(Context context, Throwable th, int i, String str, String str2) {
        try {
            ExecutorService a = ay.a();
            if (a != null && !a.isShutdown()) {
                final int i2 = i;
                final Context context2 = context;
                final Throwable th2 = th;
                final String str3 = str;
                final String str4 = str2;
                a.submit(new Runnable() {
                    public void run() {
                        try {
                            bi a = bi.a(i2);
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
            bi a = bi.a(2);
            if (a != null) {
                a.a(context);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    static void b(final Context context) {
        try {
            ExecutorService a = ay.a();
            if (a != null && !a.isShutdown()) {
                a.submit(new Runnable() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        bg a;
                        bg a2;
                        Throwable th;
                        bg bgVar;
                        Throwable th2;
                        bg bgVar2;
                        bg bgVar3 = null;
                        try {
                            a = bg.a(context, 0);
                            try {
                                a2 = bg.a(context, 1);
                                try {
                                    bgVar3 = bg.a(context, 2);
                                    a.b(context);
                                    a2.b(context);
                                    bgVar3.b(context);
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (bgVar3 != null) {
                                        bgVar3.c();
                                    }
                                } catch (RejectedExecutionException e) {
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (bgVar3 != null) {
                                        bgVar3.c();
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    bgVar = a;
                                    a = a2;
                                    a2 = bgVar3;
                                    th2 = th;
                                    if (bgVar != null) {
                                        bgVar.c();
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
                                a2 = bgVar3;
                                if (a != null) {
                                    a.c();
                                }
                                if (a2 != null) {
                                    a2.c();
                                }
                                if (bgVar3 != null) {
                                    bgVar3.c();
                                }
                            } catch (Throwable th4) {
                                bgVar = a;
                                a = bgVar3;
                                bgVar2 = bgVar3;
                                th2 = th4;
                                a2 = bgVar2;
                                if (bgVar != null) {
                                    bgVar.c();
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
                            a2 = bgVar3;
                            a = bgVar3;
                            if (a != null) {
                                a.c();
                            }
                            if (a2 != null) {
                                a2.c();
                            }
                            if (bgVar3 != null) {
                                bgVar3.c();
                            }
                        } catch (Throwable th42) {
                            a = bgVar3;
                            bgVar = bgVar3;
                            th = th42;
                            a2 = bgVar3;
                            th2 = th;
                            if (bgVar != null) {
                                bgVar.c();
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
            ay.a(th, "Log", "processLog");
            th.printStackTrace();
        }
    }
}
