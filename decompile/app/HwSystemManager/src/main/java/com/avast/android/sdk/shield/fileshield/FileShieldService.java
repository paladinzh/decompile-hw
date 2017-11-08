package com.avast.android.sdk.shield.fileshield;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.IntentCompat;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.aw;
import com.avast.android.sdk.engine.obfuscated.ay;
import com.avast.android.sdk.engine.obfuscated.az;
import com.avast.android.sdk.engine.obfuscated.ba;
import com.huawei.permissionmanager.utils.ShareCfg;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/* compiled from: Unknown */
public abstract class FileShieldService extends Service {
    public static final String INTENT_ACTION_SD_CARD_SCAN_STARTED = "intent.action.sd_card_scan_started";
    public static final String INTENT_ACTION_SD_CARD_SCAN_STOPPED = "intent.action.sd_card_scan_stopped";
    private static final IntentFilter a = new IntentFilter();
    private static final IntentFilter b = new IntentFilter(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
    private static final IntentFilter c = new IntentFilter();
    private Intent d;
    private PendingIntent e;
    private final HashMap<String, az> f = new HashMap();
    private g g;
    private Looper h;
    private ay<aw> i;
    private j j;
    private a k;
    private i l;
    private BroadcastReceiver m;
    private BroadcastReceiver n;
    private boolean o = false;
    private boolean p = false;
    private AlarmManager q;
    private BlockingQueue<d> r;
    private f s;

    /* compiled from: Unknown */
    private final class a extends Thread {
        final /* synthetic */ FileShieldService a;
        private final Semaphore b = new Semaphore(0);
        private final Map<String, Long> c = new HashMap();
        private final Map<String, Long> d = new HashMap();
        private Handler e;
        private final HandlerThread f = new HandlerThread("AMS-FS$BThread@HThread");

        public a(FileShieldService fileShieldService) {
            this.a = fileShieldService;
            super("AMS-FS$BThread");
            this.f.start();
            this.e = new d(this, this.f.getLooper(), fileShieldService);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void a(String str, long j) {
            if (j == 256) {
                synchronized (this.d) {
                    synchronized (this.c) {
                        this.c.remove(str);
                    }
                    boolean isEmpty = this.d.isEmpty();
                    this.d.put(str, Long.valueOf(System.nanoTime()));
                    if (isEmpty) {
                        this.e.sendEmptyMessageDelayed(1, 500);
                    }
                }
            } else if (j == 128) {
                synchronized (this.c) {
                    synchronized (this.d) {
                        if (this.d.get(str) == null) {
                        }
                    }
                }
            }
        }

        public void run() {
            super.run();
            while (true) {
                try {
                    Set entrySet;
                    Entry entry;
                    aw awVar;
                    Object obj;
                    Object obj2;
                    this.b.acquire();
                    long nanoTime = System.nanoTime();
                    synchronized (this.c) {
                        entrySet = this.c.entrySet();
                        if (entrySet != null) {
                            Iterator it = entrySet.iterator();
                            while (it.hasNext()) {
                                entry = (Entry) it.next();
                                if ((((Long) entry.getValue()).longValue() + 3000000000L >= nanoTime ? 1 : null) == null) {
                                    awVar = new aw(new File((String) entry.getKey()), 160);
                                    this.a.l.a(awVar, this.a.j.a(awVar));
                                    it.remove();
                                }
                            }
                        }
                        obj = this.c.isEmpty() ? 1 : null;
                    }
                    synchronized (this.d) {
                        entrySet = this.d.entrySet();
                        if (entrySet != null) {
                            Iterator it2 = entrySet.iterator();
                            while (it2.hasNext()) {
                                entry = (Entry) it2.next();
                                if ((((Long) entry.getValue()).longValue() + 500000000 >= nanoTime ? 1 : null) == null) {
                                    awVar = new aw(new File((String) entry.getKey()), 288);
                                    this.a.l.a(awVar, this.a.j.a(awVar));
                                    it2.remove();
                                }
                            }
                        }
                        obj2 = this.d.isEmpty() ? 1 : null;
                    }
                    nanoTime = System.nanoTime() - nanoTime;
                    if (!((nanoTime < 3000000000L ? 1 : null) == null && obj == null)) {
                        if (!((nanoTime < 500000000 ? 1 : null) == null && obj2 == null)) {
                            if (obj2 == null) {
                                this.e.sendEmptyMessageDelayed(0, (500000000 - nanoTime) / 1000000);
                            } else if (obj == null) {
                                this.e.sendEmptyMessageDelayed(0, (3000000000L - nanoTime) / 1000000);
                            }
                        }
                    }
                    this.e.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    this.c.clear();
                    return;
                }
            }
        }
    }

    /* compiled from: Unknown */
    private abstract class d {
        private final e a;
        final /* synthetic */ FileShieldService b;

        public d(FileShieldService fileShieldService, e eVar) {
            this.b = fileShieldService;
            this.a = eVar;
        }

        public e b() {
            return this.a;
        }
    }

    /* compiled from: Unknown */
    private final class b extends d {
        final /* synthetic */ FileShieldService a;
        private final String c;

        public b(FileShieldService fileShieldService, String str) {
            this.a = fileShieldService;
            super(fileShieldService, e.CREATE);
            if (str != null) {
                this.c = str;
                return;
            }
            throw new IllegalArgumentException("Parameter can't be null");
        }

        public String a() {
            return this.c;
        }
    }

    /* compiled from: Unknown */
    private final class c extends d {
        final /* synthetic */ FileShieldService a;
        private final String c;

        public c(FileShieldService fileShieldService, String str) {
            this.a = fileShieldService;
            super(fileShieldService, e.DELETE);
            if (str != null) {
                this.c = str;
                return;
            }
            throw new IllegalArgumentException("Parameter can't be null");
        }

        public String a() {
            return this.c;
        }
    }

    /* compiled from: Unknown */
    private enum e {
        CREATE,
        DELETE,
        RENAME,
        FILE_SHIELD_SETTINGS_CHANGE
    }

    /* compiled from: Unknown */
    private final class f extends Thread {
        final /* synthetic */ FileShieldService a;

        public f(FileShieldService fileShieldService) {
            this.a = fileShieldService;
            super("AMS-SS$DThread");
        }

        private void a() {
            synchronized (this.a.f) {
                List<String> linkedList = new LinkedList();
                for (Object add : this.a.f.keySet()) {
                    linkedList.add(add);
                }
                for (String str : linkedList) {
                    a(new c(this.a, str));
                    a(new b(this.a, str), false);
                }
            }
        }

        private void a(b bVar, boolean z) {
            int i = 0;
            File file = new File(bVar.a());
            int i2 = !this.a.isFileShieldOnReadScanEnabled() ? 0 : 1;
            int i3 = !this.a.isFileShieldOnWriteScanEnabled() ? i2 : i2 | 8;
            if (file != null && file.exists()) {
                az azVar;
                synchronized (this.a.f) {
                    if (this.a.f.get(file.getAbsolutePath()) == null) {
                        azVar = new az(this.a.g, file.getAbsolutePath(), i3);
                        azVar.startWatching();
                        this.a.f.put(file.getAbsolutePath(), azVar);
                    }
                }
                if (z) {
                    File[] listFiles = file.listFiles();
                    if (listFiles != null) {
                        while (i < listFiles.length) {
                            if (listFiles[i] != null && listFiles[i].isDirectory()) {
                                azVar = new az(this.a.g, listFiles[i].getAbsolutePath(), i3);
                                if (listFiles[i].exists()) {
                                    azVar.startWatching();
                                    synchronized (this.a.f) {
                                        azVar = (az) this.a.f.put(listFiles[i].getAbsolutePath(), azVar);
                                    }
                                    if (azVar != null) {
                                        azVar.stopWatching();
                                    } else {
                                        this.a.r.offer(new b(this.a, listFiles[i].getAbsolutePath()));
                                    }
                                } else {
                                    continue;
                                }
                            }
                            i++;
                        }
                    }
                }
            }
        }

        private void a(c cVar) {
            synchronized (this.a.f) {
                az azVar = (az) this.a.f.remove(cVar.a());
                if (azVar != null) {
                    azVar.stopWatching();
                }
            }
        }

        private void a(h hVar) {
            synchronized (this.a.f) {
                String a = hVar.a();
                String c = hVar.c();
                HashMap hashMap = new HashMap();
                List<String> linkedList = new LinkedList();
                for (String str : this.a.f.keySet()) {
                    if (str.startsWith(a)) {
                        az azVar = (az) this.a.f.get(str);
                        String str2 = c + str.substring(a.length());
                        azVar.a(str2);
                        hashMap.put(str2, azVar);
                        linkedList.add(str);
                    }
                }
                for (String str3 : linkedList) {
                    this.a.f.remove(str3);
                }
                for (String str32 : hashMap.keySet()) {
                    this.a.f.put(str32, hashMap.get(str32));
                }
                hashMap.clear();
                az.a();
            }
        }

        public void run() {
            super.run();
            while (true) {
                try {
                    d dVar = (d) this.a.r.take();
                    switch (c.a[dVar.b().ordinal()]) {
                        case 1:
                            a((b) dVar, true);
                            break;
                        case 2:
                            a((h) dVar);
                            break;
                        case 3:
                            a((c) dVar);
                            break;
                        case 4:
                            a();
                            break;
                        default:
                            break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /* compiled from: Unknown */
    private final class g extends Handler {
        final /* synthetic */ FileShieldService a;
        private String b;

        public g(FileShieldService fileShieldService, Looper looper) {
            this.a = fileShieldService;
            super(looper);
        }

        public void handleMessage(Message message) {
            try {
                if (this.a.isFileShieldEnabled()) {
                    switch (message.what) {
                        case 0:
                            if (this.a.isFileShieldOnWriteScanEnabled()) {
                                this.b = (String) message.obj;
                                this.a.k.a(this.b, 256);
                                break;
                            }
                            break;
                        case 1:
                            if (this.a.isFileShieldOnReadScanEnabled() && !this.a.p) {
                                this.b = (String) message.obj;
                                this.a.k.a(this.b, 128);
                                break;
                            }
                        case 2:
                            this.b = (String) message.obj;
                            this.a.r.offer(new b(this.a, this.b));
                            break;
                        case 3:
                            this.b = (String) message.obj;
                            this.a.r.offer(new c(this.a, this.b));
                            break;
                        case 4:
                            List list = (List) message.obj;
                            if (!list.isEmpty() && list.size() == 2) {
                                this.a.r.offer(new h(this.a, (String) list.get(0), (String) list.get(1)));
                                break;
                            }
                            return;
                            break;
                        default:
                            return;
                    }
                }
                this.a.stopSelf();
            } catch (NullPointerException e) {
            } catch (ClassCastException e2) {
            } catch (IllegalArgumentException e3) {
            }
        }
    }

    /* compiled from: Unknown */
    private final class h extends d {
        final /* synthetic */ FileShieldService a;
        private final String c;
        private final String d;

        public h(FileShieldService fileShieldService, String str, String str2) {
            this.a = fileShieldService;
            super(fileShieldService, e.RENAME);
            if (str == null || str2 == null) {
                throw new IllegalArgumentException("Parameter(s) can't be null");
            }
            this.c = str;
            this.d = str2;
        }

        public String a() {
            return this.c;
        }

        public String c() {
            return this.d;
        }
    }

    /* compiled from: Unknown */
    private final class i extends Thread {
        final /* synthetic */ FileShieldService a;
        private final Context b;
        private final Map<aw, String> c = new HashMap();
        private long d;

        public i(FileShieldService fileShieldService, Context context) {
            this.a = fileShieldService;
            super("AMS-SS$SThread");
            this.b = context;
            this.d = 0;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void a() {
            synchronized (this.c) {
                if (!this.c.isEmpty()) {
                    Iterator it = this.c.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry entry = (Entry) it.next();
                        if (!this.a.i.a((String) entry.getValue(), entry.getKey())) {
                            break;
                        }
                        it.remove();
                    }
                }
            }
        }

        private void a(String str, aw awVar) {
            synchronized (this.c) {
                this.c.put(awVar, str);
            }
        }

        public void a(aw awVar, String str) {
            if (!this.a.i.a(str, awVar)) {
                a(str, awVar);
            }
        }

        public void run() {
            super.run();
            while (true) {
                try {
                    a();
                    aw awVar = (aw) this.a.i.a();
                    File b = awVar.b();
                    if (this.a.onPreFileScan(b.getAbsolutePath(), awVar.a())) {
                        List scan = EngineInterface.scan(this.b, null, b, null, awVar.a());
                        this.d++;
                        if (this.d % 1000 == 0) {
                            System.gc();
                        }
                        this.a.onFileScanResult(b.getAbsolutePath(), scan);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /* compiled from: Unknown */
    private final class j extends Thread {
        final /* synthetic */ FileShieldService a;
        private final Semaphore b = new Semaphore(0);
        private com.avast.android.sdk.internal.g c = new com.avast.android.sdk.internal.g();
        private final List<File> d = new LinkedList();

        public j(FileShieldService fileShieldService) {
            this.a = fileShieldService;
            super("AMS-SS$SCDThread");
        }

        public String a(aw awVar) {
            synchronized (this.d) {
                for (File absolutePath : this.d) {
                    String absolutePath2 = absolutePath.getAbsolutePath();
                    if (awVar.b().getAbsolutePath().startsWith(absolutePath2)) {
                        return absolutePath2;
                    }
                }
                return null;
            }
        }

        public void a(Intent intent) {
            String path = (intent == null || intent.getData() == null) ? null : intent.getData().getPath();
            if (path != null && "android.intent.action.MEDIA_EJECT".equals(intent.getAction())) {
                synchronized (this.d) {
                    for (File absolutePath : this.d) {
                        if (absolutePath.getAbsolutePath().equals(path)) {
                            this.a.i.a(path);
                        }
                    }
                }
            }
            this.b.release();
        }

        public void run() {
            super.run();
            while (true) {
                List<String> list;
                Iterator it;
                File file;
                Object obj;
                Object obj2;
                List<String> linkedList;
                this.b.acquire();
                List fileShieldSdCardRoots = EngineInterface.getEngineConfig().getFileShieldSdCardRoots();
                if (fileShieldSdCardRoots != null) {
                    if (!fileShieldSdCardRoots.isEmpty()) {
                        ao.a("Custom SD card roots used in FileShieldService");
                        list = fileShieldSdCardRoots;
                        it = list.iterator();
                        while (it.hasNext()) {
                            file = new File((String) it.next());
                            if (file.exists() && file.canRead()) {
                                ao.a("Starting FileShieldService for path " + file.getAbsolutePath());
                            } else {
                                ao.a("Path " + file.getAbsolutePath() + " doesn't exist or not " + "readable");
                                it.remove();
                            }
                        }
                        synchronized (this.d) {
                            for (String str : list) {
                                obj = null;
                                for (File absolutePath : this.d) {
                                    if (absolutePath.getAbsolutePath().equals(str)) {
                                        obj2 = obj;
                                    } else {
                                        int i = 1;
                                    }
                                    obj = obj2;
                                }
                                if (obj == null) {
                                    this.a.i.b(str);
                                    this.a.r.offer(new b(this.a, str));
                                }
                            }
                        }
                        synchronized (this.d) {
                            for (File file2 : this.d) {
                                obj = 1;
                                for (String equals : list) {
                                    obj = file2.getAbsolutePath().equals(equals) ? obj : null;
                                }
                                if (obj != null) {
                                    this.a.i.a(file2.getAbsolutePath());
                                    synchronized (this.a.f) {
                                        linkedList = new LinkedList();
                                        for (String equals2 : this.a.f.keySet()) {
                                            if (equals2.startsWith(file2.getAbsolutePath())) {
                                                linkedList.add(equals2);
                                            }
                                        }
                                        for (String str2 : linkedList) {
                                            ((az) this.a.f.remove(str2)).stopWatching();
                                        }
                                    }
                                }
                            }
                        }
                        this.c.a();
                        synchronized (this.d) {
                            this.d.clear();
                            for (String str22 : list) {
                                this.d.add(new File(str22));
                            }
                        }
                    }
                }
                fileShieldSdCardRoots = this.c.b();
                list = fileShieldSdCardRoots;
                it = list.iterator();
                while (it.hasNext()) {
                    file = new File((String) it.next());
                    if (file.exists()) {
                        ao.a("Starting FileShieldService for path " + file.getAbsolutePath());
                    }
                    ao.a("Path " + file.getAbsolutePath() + " doesn't exist or not " + "readable");
                    it.remove();
                }
                synchronized (this.d) {
                    for (String str222 : list) {
                        obj = null;
                        while (r8.hasNext()) {
                            if (absolutePath.getAbsolutePath().equals(str222)) {
                                int i2 = 1;
                            } else {
                                obj2 = obj;
                            }
                            obj = obj2;
                        }
                        if (obj == null) {
                            this.a.i.b(str222);
                            this.a.r.offer(new b(this.a, str222));
                        }
                    }
                }
                try {
                    synchronized (this.d) {
                        for (File file22 : this.d) {
                            obj = 1;
                            while (r8.hasNext()) {
                                if (file22.getAbsolutePath().equals(equals2)) {
                                }
                                obj = file22.getAbsolutePath().equals(equals2) ? obj : null;
                            }
                            if (obj != null) {
                                this.a.i.a(file22.getAbsolutePath());
                                synchronized (this.a.f) {
                                    linkedList = new LinkedList();
                                    for (String equals22 : this.a.f.keySet()) {
                                        if (equals22.startsWith(file22.getAbsolutePath())) {
                                            linkedList.add(equals22);
                                        }
                                    }
                                    while (it.hasNext()) {
                                        ((az) this.a.f.remove(str222)).stopWatching();
                                    }
                                }
                            }
                        }
                    }
                    this.c.a();
                    synchronized (this.d) {
                        this.d.clear();
                        while (r2.hasNext()) {
                            this.d.add(new File(str222));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    static {
        a.addDataScheme("file");
        a.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        a.addAction("android.intent.action.MEDIA_MOUNTED");
        a.addAction("android.intent.action.MEDIA_SHARED");
        a.addAction("android.intent.action.MEDIA_REMOVED");
        a.addAction("android.intent.action.MEDIA_EJECT");
        c.addAction(INTENT_ACTION_SD_CARD_SCAN_STARTED);
        c.addAction(INTENT_ACTION_SD_CARD_SCAN_STOPPED);
    }

    private boolean a() {
        boolean z = true;
        boolean z2 = false;
        if (VERSION.SDK_INT >= 16) {
            if (checkCallingOrSelfPermission(ShareCfg.WRITE_STORAGE_PERMISSION) == 0) {
                if (checkCallingOrSelfPermission(ShareCfg.READ_STORAGE_PERMISSION) != 0) {
                }
                return z;
            }
            z = false;
            return z;
        }
        if (checkCallingOrSelfPermission(ShareCfg.WRITE_STORAGE_PERMISSION) == 0) {
            z2 = true;
        }
        return z2;
    }

    public abstract boolean isFileShieldEnabled();

    public abstract boolean isFileShieldOnReadScanEnabled();

    public abstract boolean isFileShieldOnWriteScanEnabled();

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (a()) {
            this.r = new LinkedBlockingQueue();
            this.s = new f(this);
            this.s.start();
            this.i = new ay(aw.d());
            this.j = new j(this);
            this.j.start();
            this.l = new i(this, this);
            this.l.start();
            this.k = new a(this);
            this.k.start();
            HandlerThread handlerThread = new HandlerThread("SSSHThread", 10);
            handlerThread.start();
            this.h = handlerThread.getLooper();
            this.g = new g(this, this.h);
            this.q = (AlarmManager) getSystemService("alarm");
            this.d = new Intent(this, FileShieldService.class);
            this.e = PendingIntent.getService(this, 0, this.d, 134217728);
            this.m = new a(this);
            registerReceiver(this.m, a);
            registerReceiver(this.m, b);
            this.n = new b(this);
            ba.a((Context) this).a(this.n, c);
            return;
        }
        ao.b("Permissions for FileShield not granted! Service stopping.");
        stopSelf();
    }

    public void onDestroy() {
        super.onDestroy();
        if (!(this.q == null || this.e == null)) {
            this.q.cancel(this.e);
        }
        if (this.m != null) {
            try {
                unregisterReceiver(this.m);
            } catch (Exception e) {
            }
        }
        if (this.s != null) {
            this.s.interrupt();
            this.s = null;
        }
        if (this.j != null) {
            this.j.interrupt();
            this.j = null;
        }
        if (this.l != null) {
            this.l.interrupt();
            this.l = null;
        }
        if (this.k != null) {
            this.k.interrupt();
            this.k = null;
        }
        synchronized (this.f) {
            Iterator it = this.f.values().iterator();
            while (it.hasNext()) {
                ((az) it.next()).stopWatching();
                it.remove();
            }
            this.f.clear();
        }
    }

    public abstract void onFileScanResult(String str, List<ScanResultStructure> list);

    public abstract boolean onPreFileScan(String str, long j);

    public int onStartCommand(Intent intent, int i, int i2) {
        if (isFileShieldEnabled() && a()) {
            this.q.set(3, SystemClock.elapsedRealtime() + 8000, this.e);
            if (!this.o) {
                this.j.a(null);
                this.o = true;
            }
            return 1;
        }
        stopSelf();
        return 2;
    }
}
