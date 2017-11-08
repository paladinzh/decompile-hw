package com.loc;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: Log */
public class af {
    static final String a = "/a/";
    static final String b = "b";
    static final String c = "c";
    static final String d = "d";

    static ag a(Context context, int i) {
        ag adVar;
        switch (i) {
            case 0:
                adVar = new ad(i);
                break;
            case 1:
                adVar = new ae(i);
                break;
            case 2:
                adVar = new ac(i);
                break;
            default:
                return null;
        }
        return adVar;
    }

    public static String a(int i) {
        switch (i) {
            case 0:
                return am.b;
            case 1:
                return am.c;
            case 2:
                return am.d;
            default:
                return "";
        }
    }

    static void a(Context context) {
        try {
            ag a = a(context, 2);
            if (a != null) {
                a.b(context);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    static void a(final Context context, final v vVar, final String str) {
        try {
            if (vVar.e()) {
                ExecutorService b = ab.b();
                if (b != null && !b.isShutdown()) {
                    b.submit(new Runnable() {
                        public void run() {
                            try {
                                af.a(context, 1).a(vVar, context, new Throwable("gpsstatistics"), str, null, null);
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }
                    });
                }
            }
        } catch (RejectedExecutionException e) {
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    static void a(Context context, Throwable th, int i, String str, String str2) {
        try {
            ExecutorService b = ab.b();
            if (b != null && !b.isShutdown()) {
                final Context context2 = context;
                final int i2 = i;
                final Throwable th2 = th;
                final String str3 = str;
                final String str4 = str2;
                b.submit(new Runnable() {
                    public void run() {
                        try {
                            ag a = af.a(context2, i2);
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

    public static String b(int i) {
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

    static void b(final Context context) {
        try {
            ExecutorService b = ab.b();
            if (b != null && !b.isShutdown()) {
                b.submit(new Runnable() {
                    /* JADX WARNING: inconsistent code. */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        ag a;
                        ag a2;
                        Throwable th;
                        ag agVar;
                        Throwable th2;
                        ag agVar2;
                        ag agVar3 = null;
                        try {
                            a = af.a(context, 0);
                            try {
                                a2 = af.a(context, 1);
                                try {
                                    agVar3 = af.a(context, 2);
                                    a.c(context);
                                    a2.c(context);
                                    agVar3.c(context);
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (agVar3 != null) {
                                        agVar3.c();
                                    }
                                } catch (RejectedExecutionException e) {
                                    if (a != null) {
                                        a.c();
                                    }
                                    if (a2 != null) {
                                        a2.c();
                                    }
                                    if (agVar3 != null) {
                                        agVar3.c();
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    agVar = a;
                                    a = a2;
                                    a2 = agVar3;
                                    th2 = th;
                                    if (agVar != null) {
                                        agVar.c();
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
                                a2 = agVar3;
                                if (a != null) {
                                    a.c();
                                }
                                if (a2 != null) {
                                    a2.c();
                                }
                                if (agVar3 != null) {
                                    agVar3.c();
                                }
                            } catch (Throwable th4) {
                                agVar = a;
                                a = agVar3;
                                agVar2 = agVar3;
                                th2 = th4;
                                a2 = agVar2;
                                if (agVar != null) {
                                    agVar.c();
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
                            a2 = agVar3;
                            a = agVar3;
                            if (a != null) {
                                a.c();
                            }
                            if (a2 != null) {
                                a2.c();
                            }
                            if (agVar3 != null) {
                                agVar3.c();
                            }
                        } catch (Throwable th42) {
                            a = agVar3;
                            agVar = agVar3;
                            th = th42;
                            a2 = agVar3;
                            th2 = th;
                            if (agVar != null) {
                                agVar.c();
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
            aa.a(th, "Log", "processLog");
        }
    }
}
