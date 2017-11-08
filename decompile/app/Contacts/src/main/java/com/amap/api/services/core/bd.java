package com.amap.api.services.core;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: Log */
public class bd {
    static final String a = "/a/";
    static final String b = "b";
    static final String c = "c";
    static final String d = "d";

    static void a(Context context, Throwable th, int i, String str, String str2) {
        try {
            ExecutorService a = av.a();
            if (a != null && !a.isShutdown()) {
                final int i2 = i;
                final Context context2 = context;
                final Throwable th2 = th;
                final String str3 = str;
                final String str4 = str2;
                a.submit(new Runnable() {
                    public void run() {
                        try {
                            bg a = bg.a(i2);
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
            bg a = bg.a(2);
            if (a != null) {
                a.a(context);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    static void b(final Context context) {
        try {
            ExecutorService a = av.a();
            if (a != null && !a.isShutdown()) {
                a.submit(new Runnable() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        be a;
                        be a2;
                        Throwable th;
                        be beVar;
                        Throwable th2;
                        be beVar2;
                        be beVar3 = null;
                        try {
                            a = be.a(context, 0);
                            try {
                                a2 = be.a(context, 1);
                                try {
                                    beVar3 = be.a(context, 2);
                                    a.b(context);
                                    a2.b(context);
                                    beVar3.b(context);
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (beVar3 != null) {
                                        beVar3.c();
                                    }
                                } catch (RejectedExecutionException e) {
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (beVar3 != null) {
                                        beVar3.c();
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    beVar = a;
                                    a = a2;
                                    a2 = beVar3;
                                    th2 = th;
                                    if (beVar != null) {
                                        beVar.c();
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
                                a2 = beVar3;
                                if (a != null) {
                                    a.c();
                                }
                                if (a2 != null) {
                                    a2.c();
                                }
                                if (beVar3 != null) {
                                    beVar3.c();
                                }
                            } catch (Throwable th4) {
                                beVar = a;
                                a = beVar3;
                                beVar2 = beVar3;
                                th2 = th4;
                                a2 = beVar2;
                                if (beVar != null) {
                                    beVar.c();
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
                            a2 = beVar3;
                            a = beVar3;
                            if (a != null) {
                                a.c();
                            }
                            if (a2 != null) {
                                a2.c();
                            }
                            if (beVar3 != null) {
                                beVar3.c();
                            }
                        } catch (Throwable th42) {
                            a = beVar3;
                            beVar = beVar3;
                            th = th42;
                            a2 = beVar3;
                            th2 = th;
                            if (beVar != null) {
                                beVar.c();
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
            av.a(th, "Log", "processLog");
            th.printStackTrace();
        }
    }
}
