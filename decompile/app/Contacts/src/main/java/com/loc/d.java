package com.loc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import com.amap.api.location.APSServiceBase;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import org.json.JSONObject;

/* compiled from: APSServiceCore */
public class d implements APSServiceBase {
    private boolean A = false;
    private bv B = null;
    private boolean C = true;
    private String D = "";
    Context a;
    boolean b = false;
    Messenger c = null;
    String d = null;
    b e = new b(this, this);
    a f = null;
    boolean g = false;
    Vector<Messenger> h = new Vector();
    Vector<Messenger> i = new Vector();
    volatile boolean j = false;
    boolean k = false;
    Object l = new Object();
    AmapLoc m;
    long n = cw.b();
    JSONObject o = new JSONObject();
    AmapLoc p;
    ServerSocket q = null;
    boolean r = false;
    Socket s = null;
    boolean t = false;
    c u;
    private volatile boolean v = false;
    private boolean w = false;
    private boolean x = false;
    private int y = 0;
    private boolean z = false;

    /* compiled from: APSServiceCore */
    class a extends Thread {
        final /* synthetic */ d a;

        a(d dVar) {
            this.a = dVar;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            try {
                this.a.f();
            } catch (Throwable th) {
                e.a(th, "APSServiceCore", "run part3");
                if (!this.a.c()) {
                    this.a.h();
                    return;
                }
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!this.a.c()) {
                    this.a.h();
                    return;
                }
                return;
            } catch (Throwable th2) {
                this.a.D = th2.getMessage();
                this.a.C = false;
                e.a(th2, "APSServiceCore", "run part1");
            }
            while (this.a.j) {
                Messenger messenger;
                if (this.a.k) {
                    if (this.a.C) {
                        this.a.m = this.a.a(this.a.g);
                        if (this.a.B != null) {
                            this.a.m = this.a.B.a(this.a.m, new String[0]);
                        }
                    } else {
                        this.a.m = this.a.a(9, this.a.D);
                    }
                    synchronized (this.a.l) {
                        if (this.a.m != null) {
                            if (this.a.m.a() == 0) {
                                this.a.n = cw.b();
                            }
                        }
                        this.a.k = false;
                        if (this.a.h != null && this.a.h.size() > 0) {
                            messenger = null;
                            for (int size = this.a.h.size(); size > 0; size--) {
                                Message obtain = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("location", this.a.m);
                                obtain.setData(bundle);
                                obtain.what = 1;
                                messenger = (Messenger) this.a.h.get(0);
                                messenger.send(obtain);
                                this.a.h.remove(0);
                            }
                        } else {
                            messenger = null;
                        }
                    }
                } else {
                    synchronized (this.a.l) {
                        if (this.a.c()) {
                            this.a.l.wait();
                        }
                    }
                    messenger = null;
                }
                if (this.a.x) {
                    this.a.e();
                    this.a.d();
                    this.a.a(messenger);
                    this.a.i();
                }
            }
            if (!this.a.c()) {
                this.a.h();
            }
        }
    }

    /* compiled from: APSServiceCore */
    class b extends Handler {
        d a = null;
        final /* synthetic */ d b;
        private boolean c = true;
        private boolean d = true;

        public b(d dVar, d dVar2) {
            this.b = dVar;
            this.a = dVar2;
        }

        public void handleMessage(Message message) {
            Object obj = 1;
            try {
                switch (message.what) {
                    case 0:
                        synchronized (this.b.l) {
                            this.b.x = true;
                            this.b.c = message.replyTo;
                            this.a.l.notify();
                        }
                        break;
                    case 1:
                        try {
                            synchronized (this.b.l) {
                                Message obtain;
                                Bundle bundle;
                                boolean z;
                                this.b.x = true;
                                Bundle data = message.getData();
                                this.b.g = data.getBoolean("isfirst");
                                Messenger messenger = message.replyTo;
                                long b = cw.b();
                                boolean z2 = data.getBoolean("isNeedAddress");
                                boolean z3 = data.getBoolean("isOffset");
                                if (z2 == this.c) {
                                    if (z3 == this.d) {
                                        this.c = z2;
                                        this.d = z3;
                                        if (this.b.m != null && this.b.m.a() == 0) {
                                            if (b - this.a.n < 800) {
                                                obj = null;
                                            }
                                            if (obj == null) {
                                                obtain = Message.obtain();
                                                bundle = new Bundle();
                                                bundle.putParcelable("location", this.b.m);
                                                obtain.setData(bundle);
                                                obtain.what = 1;
                                                messenger.send(obtain);
                                                z = data.getBoolean("wifiactivescan");
                                                this.b.b = data.getBoolean("isKillProcess");
                                                b = data.getLong("httptimeout");
                                                if (this.b.o != null) {
                                                    this.b.o.put("reversegeo", z2);
                                                    this.b.o.put("isOffset", z3);
                                                    this.b.o.put("wifiactivescan", z ? "0" : CallInterceptDetails.BRANDED_STATE);
                                                    this.b.o.put("httptimeout", b);
                                                }
                                                this.a.a(this.b.o);
                                            }
                                        }
                                        if (!this.b.h.contains(messenger)) {
                                            this.b.h.add(messenger);
                                        }
                                        this.b.k = true;
                                        this.a.l.notify();
                                        z = data.getBoolean("wifiactivescan");
                                        this.b.b = data.getBoolean("isKillProcess");
                                        b = data.getLong("httptimeout");
                                        if (this.b.o != null) {
                                            this.b.o.put("reversegeo", z2);
                                            this.b.o.put("isOffset", z3);
                                            if (z) {
                                            }
                                            this.b.o.put("wifiactivescan", z ? "0" : CallInterceptDetails.BRANDED_STATE);
                                            this.b.o.put("httptimeout", b);
                                        }
                                        this.a.a(this.b.o);
                                    }
                                }
                                this.a.n = 0;
                                this.c = z2;
                                this.d = z3;
                                if (b - this.a.n < 800) {
                                    obj = null;
                                }
                                if (obj == null) {
                                    obtain = Message.obtain();
                                    bundle = new Bundle();
                                    bundle.putParcelable("location", this.b.m);
                                    obtain.setData(bundle);
                                    obtain.what = 1;
                                    messenger.send(obtain);
                                    z = data.getBoolean("wifiactivescan");
                                    this.b.b = data.getBoolean("isKillProcess");
                                    b = data.getLong("httptimeout");
                                    if (this.b.o != null) {
                                        this.b.o.put("reversegeo", z2);
                                        this.b.o.put("isOffset", z3);
                                        if (z) {
                                        }
                                        this.b.o.put("wifiactivescan", z ? "0" : CallInterceptDetails.BRANDED_STATE);
                                        this.b.o.put("httptimeout", b);
                                    }
                                    this.a.a(this.b.o);
                                }
                                if (this.b.h.contains(messenger)) {
                                    this.b.h.add(messenger);
                                }
                                this.b.k = true;
                                this.a.l.notify();
                                z = data.getBoolean("wifiactivescan");
                                this.b.b = data.getBoolean("isKillProcess");
                                b = data.getLong("httptimeout");
                                if (this.b.o != null) {
                                    this.b.o.put("reversegeo", z2);
                                    this.b.o.put("isOffset", z3);
                                    if (z) {
                                    }
                                    this.b.o.put("wifiactivescan", z ? "0" : CallInterceptDetails.BRANDED_STATE);
                                    this.b.o.put("httptimeout", b);
                                }
                                this.a.a(this.b.o);
                                break;
                            }
                            break;
                        } catch (Throwable th) {
                            e.a(th, "APSServiceCore", "handleMessage LOCATION");
                            break;
                        }
                    case 2:
                        this.b.a();
                        break;
                    case 3:
                        this.b.b();
                        break;
                    case 4:
                        synchronized (this.b.l) {
                            if (ct.d()) {
                                if (this.b.y < ct.e()) {
                                    this.b.y = this.b.y + 1;
                                    this.b.k = true;
                                    this.a.l.notify();
                                    this.b.e.sendEmptyMessageDelayed(4, 2000);
                                }
                            }
                        }
                        break;
                    case 5:
                        synchronized (this.b.l) {
                            if (ct.f()) {
                                if (ct.g() > 2) {
                                    this.b.k = true;
                                    if (ct.h()) {
                                        this.a.l.notify();
                                    } else if (!cw.d(this.b.a)) {
                                        this.a.l.notify();
                                    }
                                    this.b.e.sendEmptyMessageDelayed(5, (long) (ct.g() * 1000));
                                }
                            }
                        }
                        break;
                    case 6:
                        synchronized (this.b.l) {
                            this.b.i();
                        }
                        break;
                }
                super.handleMessage(message);
            } catch (Throwable th2) {
                e.a(th2, "APSServiceCore", "handleMessage STARTCOLL");
            }
        }
    }

    /* compiled from: APSServiceCore */
    class c extends Thread {
        final /* synthetic */ d a;

        c(d dVar) {
            this.a = dVar;
        }

        public void run() {
            try {
                if (!this.a.v) {
                    this.a.g();
                }
                if (!this.a.r) {
                    this.a.r = true;
                    this.a.q = new ServerSocket(43689);
                }
                while (this.a.r) {
                    this.a.s = this.a.q.accept();
                    this.a.a(this.a.s);
                }
            } catch (Throwable th) {
                e.a(th, "APSServiceCore", "run");
            }
            super.run();
        }
    }

    public d(Context context) {
        this.a = context.getApplicationContext();
    }

    private AmapLoc a(int i, String str) {
        try {
            AmapLoc amapLoc = new AmapLoc();
            amapLoc.b(i);
            amapLoc.b(str);
            return amapLoc;
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "newInstanceAMapLoc");
            return null;
        }
    }

    private AmapLoc a(boolean z) throws Exception {
        return this.B.a(z);
    }

    private void a(Messenger messenger) {
        try {
            Message obtain;
            if (ct.r() && messenger != null) {
                ct.a("0");
                obtain = Message.obtain();
                obtain.what = 100;
                messenger.send(obtain);
            }
            if (this.c != null) {
                obtain = Message.obtain();
                obtain.what = 6;
                this.c.send(obtain);
                this.c = null;
            }
            if (ct.a()) {
                this.B.a();
            }
            if (ct.d() && !this.z) {
                this.z = true;
                this.e.sendEmptyMessage(4);
            }
            if (ct.f() && !this.A) {
                this.A = true;
                this.e.sendEmptyMessage(5);
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "checkConfig");
        }
    }

    private void a(java.net.Socket r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:123:0x0346
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r12 = this;
        r2 = 0;
        r5 = 1;
        r6 = 0;
        if (r13 == 0) goto L_0x007f;
    L_0x0005:
        r0 = 30000; // 0x7530 float:4.2039E-41 double:1.4822E-319;
        r1 = "jsonp1";	 Catch:{ Throwable -> 0x0174 }
        java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0174 }
        r4 = new java.io.BufferedReader;	 Catch:{ Throwable -> 0x0406, all -> 0x03fd }
        r3 = new java.io.InputStreamReader;	 Catch:{ Throwable -> 0x0406, all -> 0x03fd }
        r7 = r13.getInputStream();	 Catch:{ Throwable -> 0x0406, all -> 0x03fd }
        r8 = "UTF-8";	 Catch:{ Throwable -> 0x0406, all -> 0x03fd }
        r3.<init>(r7, r8);	 Catch:{ Throwable -> 0x0406, all -> 0x03fd }
        r4.<init>(r3);	 Catch:{ Throwable -> 0x0406, all -> 0x03fd }
        r3 = r4.readLine();	 Catch:{ Throwable -> 0x040a }
        if (r3 != 0) goto L_0x0080;
    L_0x0024:
        r3 = r1;
        r1 = r0;
    L_0x0026:
        r7 = com.loc.e.j;	 Catch:{ Throwable -> 0x0262 }
        com.loc.e.j = r1;	 Catch:{ Throwable -> 0x0262 }
        r0 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x0262 }
        r8 = r12.p;	 Catch:{ Throwable -> 0x0262 }
        if (r8 != 0) goto L_0x018e;	 Catch:{ Throwable -> 0x0262 }
    L_0x0032:
        r0 = r12.a;	 Catch:{ Throwable -> 0x0262 }
        r0 = com.loc.cw.d(r0);	 Catch:{ Throwable -> 0x0262 }
        if (r0 == 0) goto L_0x01a2;
    L_0x003a:
        r0 = r2;
    L_0x003b:
        if (r0 == 0) goto L_0x0268;
    L_0x003d:
        r1 = new java.io.PrintStream;	 Catch:{ all -> 0x0364 }
        r2 = r13.getOutputStream();	 Catch:{ all -> 0x0364 }
        r3 = "UTF-8";	 Catch:{ all -> 0x0364 }
        r5 = 1;	 Catch:{ all -> 0x0364 }
        r1.<init>(r2, r5, r3);	 Catch:{ all -> 0x0364 }
        r2 = "HTTP/1.0 200 OK";	 Catch:{ all -> 0x0364 }
        r1.println(r2);	 Catch:{ all -> 0x0364 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0364 }
        r2.<init>();	 Catch:{ all -> 0x0364 }
        r3 = "Content-Length:";	 Catch:{ all -> 0x0364 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0364 }
        r3 = "UTF-8";	 Catch:{ all -> 0x0364 }
        r3 = r0.getBytes(r3);	 Catch:{ all -> 0x0364 }
        r3 = r3.length;	 Catch:{ all -> 0x0364 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0364 }
        r2 = r2.toString();	 Catch:{ all -> 0x0364 }
        r1.println(r2);	 Catch:{ all -> 0x0364 }
        r1.println();	 Catch:{ all -> 0x0364 }
        r1.println(r0);	 Catch:{ all -> 0x0364 }
        r1.close();	 Catch:{ all -> 0x0364 }
        r4.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
    L_0x007e:
        return;
    L_0x007f:
        return;
    L_0x0080:
        r7 = r3.length();	 Catch:{ Throwable -> 0x040a }
        if (r7 <= 0) goto L_0x0024;	 Catch:{ Throwable -> 0x040a }
    L_0x0086:
        r7 = " ";	 Catch:{ Throwable -> 0x040a }
        r3 = r3.split(r7);	 Catch:{ Throwable -> 0x040a }
        if (r3 == 0) goto L_0x0024;	 Catch:{ Throwable -> 0x040a }
    L_0x008f:
        r7 = r3.length;	 Catch:{ Throwable -> 0x040a }
        if (r7 <= r5) goto L_0x0024;	 Catch:{ Throwable -> 0x040a }
    L_0x0092:
        r7 = 1;	 Catch:{ Throwable -> 0x040a }
        r3 = r3[r7];	 Catch:{ Throwable -> 0x040a }
        r7 = "\\?";	 Catch:{ Throwable -> 0x040a }
        r3 = r3.split(r7);	 Catch:{ Throwable -> 0x040a }
        if (r3 == 0) goto L_0x0024;	 Catch:{ Throwable -> 0x040a }
    L_0x009e:
        r7 = r3.length;	 Catch:{ Throwable -> 0x040a }
        if (r7 <= r5) goto L_0x0024;	 Catch:{ Throwable -> 0x040a }
    L_0x00a1:
        r7 = 1;	 Catch:{ Throwable -> 0x040a }
        r3 = r3[r7];	 Catch:{ Throwable -> 0x040a }
        r7 = "&";	 Catch:{ Throwable -> 0x040a }
        r7 = r3.split(r7);	 Catch:{ Throwable -> 0x040a }
        if (r7 == 0) goto L_0x0024;	 Catch:{ Throwable -> 0x040a }
    L_0x00ad:
        r3 = r7.length;	 Catch:{ Throwable -> 0x040a }
        if (r3 <= 0) goto L_0x0024;
    L_0x00b0:
        r3 = r6;
        r11 = r1;
        r1 = r0;
        r0 = r11;
    L_0x00b4:
        r8 = r7.length;	 Catch:{ Throwable -> 0x00f6 }
        if (r3 < r8) goto L_0x00ba;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00b7:
        r3 = r0;	 Catch:{ Throwable -> 0x00f6 }
        goto L_0x0026;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00ba:
        r8 = r7[r3];	 Catch:{ Throwable -> 0x00f6 }
        r9 = "=";	 Catch:{ Throwable -> 0x00f6 }
        r8 = r8.split(r9);	 Catch:{ Throwable -> 0x00f6 }
        if (r8 != 0) goto L_0x00c8;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00c5:
        r3 = r3 + 1;	 Catch:{ Throwable -> 0x00f6 }
        goto L_0x00b4;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00c8:
        r9 = r8.length;	 Catch:{ Throwable -> 0x00f6 }
        if (r9 <= r5) goto L_0x00c5;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00cb:
        r9 = "to";	 Catch:{ Throwable -> 0x00f6 }
        r10 = 0;	 Catch:{ Throwable -> 0x00f6 }
        r10 = r8[r10];	 Catch:{ Throwable -> 0x00f6 }
        r9 = r9.equals(r10);	 Catch:{ Throwable -> 0x00f6 }
        if (r9 != 0) goto L_0x0180;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00d7:
        r9 = "callback";	 Catch:{ Throwable -> 0x00f6 }
        r10 = 0;	 Catch:{ Throwable -> 0x00f6 }
        r10 = r8[r10];	 Catch:{ Throwable -> 0x00f6 }
        r9 = r9.equals(r10);	 Catch:{ Throwable -> 0x00f6 }
        if (r9 != 0) goto L_0x0189;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00e3:
        r9 = "_";	 Catch:{ Throwable -> 0x00f6 }
        r10 = 0;	 Catch:{ Throwable -> 0x00f6 }
        r10 = r8[r10];	 Catch:{ Throwable -> 0x00f6 }
        r9 = r9.equals(r10);	 Catch:{ Throwable -> 0x00f6 }
        if (r9 == 0) goto L_0x00c5;	 Catch:{ Throwable -> 0x00f6 }
    L_0x00ef:
        r9 = 1;	 Catch:{ Throwable -> 0x00f6 }
        r8 = r8[r9];	 Catch:{ Throwable -> 0x00f6 }
        java.lang.Long.parseLong(r8);	 Catch:{ Throwable -> 0x00f6 }
        goto L_0x00c5;
    L_0x00f6:
        r1 = move-exception;
        r3 = r2;
        r2 = r4;
        r11 = r0;
        r0 = r1;
        r1 = r11;
    L_0x00fc:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0401 }
        r4.<init>();	 Catch:{ all -> 0x0401 }
        r4 = r4.append(r1);	 Catch:{ all -> 0x0401 }
        r5 = "&&";	 Catch:{ all -> 0x0401 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0401 }
        r1 = r4.append(r1);	 Catch:{ all -> 0x0401 }
        r4 = "({'package':'";	 Catch:{ all -> 0x0401 }
        r1 = r1.append(r4);	 Catch:{ all -> 0x0401 }
        r4 = r12.d;	 Catch:{ all -> 0x0401 }
        r1 = r1.append(r4);	 Catch:{ all -> 0x0401 }
        r4 = "','error_code':1,'error':'params error'})";	 Catch:{ all -> 0x0401 }
        r1 = r1.append(r4);	 Catch:{ all -> 0x0401 }
        r3 = r1.toString();	 Catch:{ all -> 0x0401 }
        r1 = "APSServiceCore";	 Catch:{ all -> 0x0401 }
        r4 = "invoke part2";	 Catch:{ all -> 0x0401 }
        com.loc.e.a(r0, r1, r4);	 Catch:{ all -> 0x0401 }
        r0 = new java.io.PrintStream;	 Catch:{ all -> 0x038a }
        r1 = r13.getOutputStream();	 Catch:{ all -> 0x038a }
        r4 = "UTF-8";	 Catch:{ all -> 0x038a }
        r5 = 1;	 Catch:{ all -> 0x038a }
        r0.<init>(r1, r5, r4);	 Catch:{ all -> 0x038a }
        r1 = "HTTP/1.0 200 OK";	 Catch:{ all -> 0x038a }
        r0.println(r1);	 Catch:{ all -> 0x038a }
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x038a }
        r1.<init>();	 Catch:{ all -> 0x038a }
        r4 = "Content-Length:";	 Catch:{ all -> 0x038a }
        r1 = r1.append(r4);	 Catch:{ all -> 0x038a }
        r4 = "UTF-8";	 Catch:{ all -> 0x038a }
        r4 = r3.getBytes(r4);	 Catch:{ all -> 0x038a }
        r4 = r4.length;	 Catch:{ all -> 0x038a }
        r1 = r1.append(r4);	 Catch:{ all -> 0x038a }
        r1 = r1.toString();	 Catch:{ all -> 0x038a }
        r0.println(r1);	 Catch:{ all -> 0x038a }
        r0.println();	 Catch:{ all -> 0x038a }
        r0.println(r3);	 Catch:{ all -> 0x038a }
        r0.close();	 Catch:{ all -> 0x038a }
        r2.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;
    L_0x0174:
        r0 = move-exception;
        r1 = "APSServiceCore";
        r2 = "invoke part5";
        com.loc.e.a(r0, r1, r2);
        goto L_0x007e;
    L_0x0180:
        r1 = 1;
        r1 = r8[r1];	 Catch:{ Throwable -> 0x00f6 }
        r1 = java.lang.Integer.parseInt(r1);	 Catch:{ Throwable -> 0x00f6 }
        goto L_0x00d7;	 Catch:{ Throwable -> 0x00f6 }
    L_0x0189:
        r9 = 1;	 Catch:{ Throwable -> 0x00f6 }
        r0 = r8[r9];	 Catch:{ Throwable -> 0x00f6 }
        goto L_0x00e3;
    L_0x018e:
        r8 = r12.p;	 Catch:{ Throwable -> 0x0262 }
        r8 = r8.k();	 Catch:{ Throwable -> 0x0262 }
        r0 = r0 - r8;
        r8 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r0 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
        if (r0 > 0) goto L_0x01a0;
    L_0x019b:
        r0 = r5;
    L_0x019c:
        if (r0 != 0) goto L_0x003a;
    L_0x019e:
        goto L_0x0032;
    L_0x01a0:
        r0 = r6;
        goto L_0x019c;
    L_0x01a2:
        r0 = r12.B;	 Catch:{ Exception -> 0x024d }
        r1 = 0;	 Catch:{ Exception -> 0x024d }
        r0 = r0.a(r1);	 Catch:{ Exception -> 0x024d }
        r12.p = r0;	 Catch:{ Exception -> 0x024d }
        r0 = r12.p;	 Catch:{ Exception -> 0x024d }
        r0 = r0.a();	 Catch:{ Exception -> 0x024d }
        if (r0 != 0) goto L_0x01fd;
    L_0x01b3:
        r0 = r2;
    L_0x01b4:
        com.loc.e.j = r7;	 Catch:{ all -> 0x01b8 }
        goto L_0x003b;
    L_0x01b8:
        r1 = move-exception;
        r2 = r0;
        r0 = r1;
    L_0x01bb:
        r1 = new java.io.PrintStream;	 Catch:{ all -> 0x03b0 }
        r3 = r13.getOutputStream();	 Catch:{ all -> 0x03b0 }
        r5 = "UTF-8";	 Catch:{ all -> 0x03b0 }
        r6 = 1;	 Catch:{ all -> 0x03b0 }
        r1.<init>(r3, r6, r5);	 Catch:{ all -> 0x03b0 }
        r3 = "HTTP/1.0 200 OK";	 Catch:{ all -> 0x03b0 }
        r1.println(r3);	 Catch:{ all -> 0x03b0 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x03b0 }
        r3.<init>();	 Catch:{ all -> 0x03b0 }
        r5 = "Content-Length:";	 Catch:{ all -> 0x03b0 }
        r3 = r3.append(r5);	 Catch:{ all -> 0x03b0 }
        r5 = "UTF-8";	 Catch:{ all -> 0x03b0 }
        r5 = r2.getBytes(r5);	 Catch:{ all -> 0x03b0 }
        r5 = r5.length;	 Catch:{ all -> 0x03b0 }
        r3 = r3.append(r5);	 Catch:{ all -> 0x03b0 }
        r3 = r3.toString();	 Catch:{ all -> 0x03b0 }
        r1.println(r3);	 Catch:{ all -> 0x03b0 }
        r1.println();	 Catch:{ all -> 0x03b0 }
        r1.println(r2);	 Catch:{ all -> 0x03b0 }
        r1.close();	 Catch:{ all -> 0x03b0 }
        r4.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
    L_0x01fc:
        throw r0;	 Catch:{ Throwable -> 0x0174 }
    L_0x01fd:
        r0 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x024d }
        r0.<init>();	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r3);	 Catch:{ Exception -> 0x024d }
        r1 = "&&";	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r3);	 Catch:{ Exception -> 0x024d }
        r1 = "({'package':'";	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r1 = r12.d;	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r1 = "','error_code':";	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r1 = r12.p;	 Catch:{ Exception -> 0x024d }
        r1 = r1.a();	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r1 = ",'error':'";	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r1 = r12.p;	 Catch:{ Exception -> 0x024d }
        r1 = r1.c();	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r1 = "'})";	 Catch:{ Exception -> 0x024d }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x024d }
        r0 = r0.toString();	 Catch:{ Exception -> 0x024d }
        goto L_0x01b4;
    L_0x024d:
        r0 = move-exception;
        r1 = "APSServiceCore";	 Catch:{ all -> 0x025e }
        r5 = "invoke part1";	 Catch:{ all -> 0x025e }
        com.loc.e.a(r0, r1, r5);	 Catch:{ all -> 0x025e }
        com.loc.e.j = r7;	 Catch:{ all -> 0x025b }
        goto L_0x003a;	 Catch:{ all -> 0x025b }
    L_0x025b:
        r0 = move-exception;	 Catch:{ all -> 0x025b }
        goto L_0x01bb;	 Catch:{ all -> 0x025b }
    L_0x025e:
        r0 = move-exception;	 Catch:{ all -> 0x025b }
        com.loc.e.j = r7;	 Catch:{ all -> 0x025b }
        throw r0;	 Catch:{ Throwable -> 0x0262 }
    L_0x0262:
        r0 = move-exception;
        r1 = r3;
        r3 = r2;
        r2 = r4;
        goto L_0x00fc;
    L_0x0268:
        r1 = r12.p;	 Catch:{ Throwable -> 0x040f }
        if (r1 == 0) goto L_0x0319;	 Catch:{ Throwable -> 0x040f }
    L_0x026c:
        r1 = r12.p;	 Catch:{ Throwable -> 0x040f }
        r2 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x040f }
        r2.<init>();	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r3);	 Catch:{ Throwable -> 0x040f }
        r5 = "&&";	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r3);	 Catch:{ Throwable -> 0x040f }
        r5 = "({'package':'";	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r5 = r12.d;	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r5 = "','error_code':0,'error':'','location':{'y':";	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r6 = r1.i();	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r6);	 Catch:{ Throwable -> 0x040f }
        r5 = ",'precision':";	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r5 = r1.j();	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r5 = ",'x':";	 Catch:{ Throwable -> 0x040f }
        r2 = r2.append(r5);	 Catch:{ Throwable -> 0x040f }
        r6 = r1.h();	 Catch:{ Throwable -> 0x040f }
        r1 = r2.append(r6);	 Catch:{ Throwable -> 0x040f }
        r2 = "},'version_code':'";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = "2.4.0";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = "','version':'";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = "2.4.0";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = "'})";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r0 = r1.toString();	 Catch:{ Throwable -> 0x040f }
    L_0x02e3:
        r1 = r12.a;	 Catch:{ Throwable -> 0x040f }
        r1 = com.loc.cw.d(r1);	 Catch:{ Throwable -> 0x040f }
        if (r1 == 0) goto L_0x003d;	 Catch:{ Throwable -> 0x040f }
    L_0x02eb:
        r1 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x040f }
        r1.<init>();	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r3);	 Catch:{ Throwable -> 0x040f }
        r2 = "&&";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r3);	 Catch:{ Throwable -> 0x040f }
        r2 = "({'package':'";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = r12.d;	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = "','error_code':36,'error':'app is background'})";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r0 = r1.toString();	 Catch:{ Throwable -> 0x040f }
        goto L_0x003d;	 Catch:{ Throwable -> 0x040f }
    L_0x0319:
        r1 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x040f }
        r1.<init>();	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r3);	 Catch:{ Throwable -> 0x040f }
        r2 = "&&";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r3);	 Catch:{ Throwable -> 0x040f }
        r2 = "({'package':'";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = r12.d;	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r2 = "','error_code':8,'error':'unknown error'})";	 Catch:{ Throwable -> 0x040f }
        r1 = r1.append(r2);	 Catch:{ Throwable -> 0x040f }
        r0 = r1.toString();	 Catch:{ Throwable -> 0x040f }
        goto L_0x02e3;
        r0 = move-exception;
        r1 = "APSServiceCore";	 Catch:{ all -> 0x0364 }
        r2 = "invoke part3";	 Catch:{ all -> 0x0364 }
        com.loc.e.a(r0, r1, r2);	 Catch:{ all -> 0x0364 }
        r4.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;
        r0 = move-exception;
        r1 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r2 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r0, r1, r2);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;
    L_0x0364:
        r0 = move-exception;
        r4.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
    L_0x036b:
        throw r0;	 Catch:{ Throwable -> 0x0174 }
        r0 = move-exception;
        r1 = "APSServiceCore";	 Catch:{ all -> 0x038a }
        r3 = "invoke part3";	 Catch:{ all -> 0x038a }
        com.loc.e.a(r0, r1, r3);	 Catch:{ all -> 0x038a }
        r2.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;
        r0 = move-exception;
        r1 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r2 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r0, r1, r2);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;
    L_0x038a:
        r0 = move-exception;
        r2.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
    L_0x0391:
        throw r0;	 Catch:{ Throwable -> 0x0174 }
        r1 = move-exception;
        r2 = "APSServiceCore";	 Catch:{ all -> 0x03b0 }
        r3 = "invoke part3";	 Catch:{ all -> 0x03b0 }
        com.loc.e.a(r1, r2, r3);	 Catch:{ all -> 0x03b0 }
        r4.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
        goto L_0x01fc;
        r1 = move-exception;
        r2 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r3 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r1, r2, r3);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x01fc;
    L_0x03b0:
        r0 = move-exception;
        r4.close();	 Catch:{ Throwable -> 0x0174 }
        r13.close();	 Catch:{ Throwable -> 0x0174 }
    L_0x03b7:
        throw r0;	 Catch:{ Throwable -> 0x0174 }
        r0 = move-exception;	 Catch:{ Throwable -> 0x0174 }
        r1 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r2 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r0, r1, r2);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;	 Catch:{ Throwable -> 0x0174 }
        r1 = move-exception;	 Catch:{ Throwable -> 0x0174 }
        r2 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r3 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r1, r2, r3);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x036b;	 Catch:{ Throwable -> 0x0174 }
        r0 = move-exception;	 Catch:{ Throwable -> 0x0174 }
        r1 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r2 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r0, r1, r2);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x007e;	 Catch:{ Throwable -> 0x0174 }
        r1 = move-exception;	 Catch:{ Throwable -> 0x0174 }
        r2 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r3 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r1, r2, r3);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x0391;	 Catch:{ Throwable -> 0x0174 }
        r1 = move-exception;	 Catch:{ Throwable -> 0x0174 }
        r2 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r3 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r1, r2, r3);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x01fc;	 Catch:{ Throwable -> 0x0174 }
        r1 = move-exception;	 Catch:{ Throwable -> 0x0174 }
        r2 = "APSServiceCore";	 Catch:{ Throwable -> 0x0174 }
        r3 = "invoke part4";	 Catch:{ Throwable -> 0x0174 }
        com.loc.e.a(r1, r2, r3);	 Catch:{ Throwable -> 0x0174 }
        goto L_0x03b7;
    L_0x03fd:
        r0 = move-exception;
        r4 = r2;
        goto L_0x01bb;
    L_0x0401:
        r0 = move-exception;
        r4 = r2;
        r2 = r3;
        goto L_0x01bb;
    L_0x0406:
        r0 = move-exception;
        r3 = r2;
        goto L_0x00fc;
    L_0x040a:
        r0 = move-exception;
        r3 = r2;
        r2 = r4;
        goto L_0x00fc;
    L_0x040f:
        r1 = move-exception;
        r2 = r4;
        r11 = r3;
        r3 = r0;
        r0 = r1;
        r1 = r11;
        goto L_0x00fc;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.loc.d.a(java.net.Socket):void");
    }

    private void a(JSONObject jSONObject) {
        try {
            if (this.B != null) {
                this.B.a(jSONObject);
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "setExtra");
        }
    }

    private boolean c() {
        boolean z;
        synchronized (this.l) {
            z = this.j;
        }
        return z;
    }

    private void d() {
        try {
            this.B.g();
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "bindService");
        }
    }

    private void e() {
        try {
            if (!this.w) {
                this.w = true;
                this.B.b(this.a);
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "initAuth");
        }
    }

    private void f() {
        try {
            if (!this.v) {
                g();
            }
        } catch (Throwable th) {
            this.C = false;
            this.D = th.getMessage();
            e.a(th, "APSServiceCore", "init");
        }
    }

    private void g() {
        try {
            e.a(this.a);
            this.B.a(this.a);
            this.B.a("api_serverSDK_130905##S128DF1572465B890OE3F7A13167KLEI##" + m.c(this.a) + "##" + m.f(this.a));
            Object a = o.a(this.a, e.a("2.4.0"), null, true);
        } catch (Throwable th) {
            this.D = th.getMessage();
            this.C = false;
            e.a(th, "APSServiceCore", "doInit part3");
            return;
        }
        try {
            this.o.put("key", m.f(this.a));
            this.o.put("X-INFO", a);
            this.o.put("User-Agent", "AMAP_Location_SDK_Android 2.4.0");
            this.o.put("netloc", "0");
            this.o.put("gpsstatus", "0");
            this.o.put("nbssid", "0");
            if (!this.o.has("reversegeo")) {
                this.o.put("reversegeo", true);
            }
            if (!this.o.has("isOffset")) {
                this.o.put("isOffset", true);
            }
            this.o.put("wait1stwifi", "0");
            this.o.put("autoup", "0");
            this.o.put("upcolmobile", 1);
            this.o.put("enablegetreq", 1);
            this.o.put("wifiactivescan", 1);
        } catch (Throwable th2) {
            this.D = th2.getMessage();
            this.C = false;
            e.a(th2, "APSServiceCore", "doInit part2");
        }
        this.v = true;
        this.B.a(this.o);
    }

    private void h() {
        try {
            b();
            this.v = false;
            this.w = false;
            this.x = false;
            this.z = false;
            this.A = false;
            this.y = 0;
            this.B.b();
            this.h.clear();
            ab.a();
            if (this.b) {
                Process.killProcess(Process.myPid());
            }
            if (this.e != null) {
                this.e.removeCallbacksAndMessages(null);
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "threadDestroy");
        }
    }

    private void i() {
        try {
            if (this.B != null) {
                this.B.h();
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "startColl");
        }
    }

    public synchronized void a() {
        try {
            if (!this.t) {
                this.u = new c(this);
                this.u.start();
                this.t = true;
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "startSocket");
        }
    }

    public synchronized void b() {
        try {
            if (this.q != null) {
                this.q.close();
            }
            if (this.s != null) {
                this.s.close();
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "stopScocket");
        }
        this.q = null;
        this.u = null;
        this.t = false;
        this.r = false;
    }

    public Handler getHandler() {
        return this.e;
    }

    public void onCreate() {
        try {
            this.B = new bv();
            this.d = this.a.getApplicationContext().getPackageName();
            this.j = true;
            this.f = new a(this);
            this.f.setName("serviceThread");
            this.f.start();
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "onCreate");
        }
    }

    public void onDestroy() {
        try {
            synchronized (this.l) {
                this.j = false;
                this.l.notify();
            }
        } catch (Throwable th) {
            e.a(th, "APSServiceCore", "onDestroy");
        }
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return 0;
    }
}
