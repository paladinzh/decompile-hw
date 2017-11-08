package tmsdk.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Build.VERSION;
import android.os.Process;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jj;
import tmsdkobf.jq;
import tmsdkobf.mo;
import tmsdkobf.ms;
import tmsdkobf.ra;

/* compiled from: Unknown */
public final class ScriptHelper {
    private static final String Lg = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ATHENA_NAME);
    private static final String Lh = (TMSDKContext.getApplicaionContext().getPackageName() + "_" + Lg + "_" + Process.myUid());
    private static final boolean Li = new File("/dev/socket/script_socket").exists();
    private static int Lj = 2;
    private static boolean Lk = false;
    private static Object Ll = new Object();
    private static a Lm = null;
    private static int Ln = Process.myPid();
    private static BroadcastReceiver Lo = new jj() {
        public void doOnRecv(Context context, Intent intent) {
            if (intent != null && intent.getIntExtra("pidky", -1) != ScriptHelper.Ln) {
                String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_CHANGE_ACTION);
                String action = intent.getAction();
                if (strFromEnvMap.equals(action)) {
                    ScriptHelper.Lj = intent.getIntExtra(ScriptHelper.ROOT_STATE_KEY, 2);
                    d.d("Root-ScriptHelper", "get ROOT_CHANGE, state: " + ScriptHelper.Lj);
                } else if ("tms.scripthelper.create".equals(action) && 2 == jq.cw()) {
                    d.d("Root-ScriptHelper", "get SCRIPT_HELPER_CREATE");
                    ScriptHelper.iP();
                }
            }
        }
    };
    private static ra Lp = null;
    public static final int ROOT_GOT = 0;
    public static final int ROOT_NOT_GOT = 2;
    public static final int ROOT_NOT_SUPPORT = 1;
    public static final int ROOT_NO_RESPOND = -1;
    public static final String ROOT_STATE_KEY = "rtstky";
    public static boolean isSuExist;

    /* compiled from: Unknown */
    public interface a {
        int A(long j);

        boolean di(String str);

        int iV();

        void iW();
    }

    /* compiled from: Unknown */
    static final class b {
        byte[] data;
        int size;
        int time;
        int type;

        b() {
        }

        void writeToStream(OutputStream outputStream) throws IOException {
            this.size = this.data == null ? 0 : this.data.length;
            Object obj = new byte[12];
            System.arraycopy(mo.bD(this.type), 0, obj, 0, 4);
            System.arraycopy(mo.bD(this.time), 0, obj, 4, 4);
            System.arraycopy(mo.bD(this.size), 0, obj, 8, 4);
            outputStream.write(obj);
            if (this.data != null && this.data.length > 0) {
                outputStream.write(this.data);
            }
            outputStream.flush();
        }
    }

    /* compiled from: Unknown */
    static final class c {
        byte[] data;
        int size;

        c() {
        }

        void e(InputStream inputStream) throws IOException {
            int i = 0;
            byte[] bArr = new byte[4];
            if (inputStream.read(bArr) == 4) {
                this.size = mo.k(bArr);
                if (this.size <= 0) {
                    this.data = new byte[0];
                    return;
                }
                bArr = new byte[this.size];
                while (true) {
                    int read = inputStream.read(bArr, i, this.size - i);
                    if (read <= 0) {
                        break;
                    }
                    i += read;
                }
                if (i == this.size) {
                    this.data = bArr;
                    return;
                }
                throw new IOException("respond data is invalid");
            }
            throw new IOException("respond data is invalid");
        }
    }

    static {
        boolean z = ms.cy("/system/bin/su") || ms.cy("/system/xbin/su") || ms.cy("/sbin/su");
        isSuExist = z;
    }

    private static synchronized c a(b bVar, boolean z) {
        IOException e;
        InputStream inputStream;
        OutputStream outputStream;
        Exception e2;
        Throwable th;
        Error e3;
        InputStream inputStream2 = null;
        synchronized (ScriptHelper.class) {
            LocalSocket localSocket = new LocalSocket();
            c cVar = new c();
            try {
                LocalSocketAddress localSocketAddress = !Li ? new LocalSocketAddress(Lh, Namespace.ABSTRACT) : new LocalSocketAddress("/dev/socket/script_socket", Namespace.FILESYSTEM);
                try {
                    d.e("Root-ScriptHelper", "connect:[" + localSocketAddress + "]");
                    localSocket.connect(localSocketAddress);
                } catch (IOException e4) {
                    d.e("Root-ScriptHelper", "connect IOException:[" + e4 + "]");
                    e4.printStackTrace();
                    if (!Li && z) {
                        iR();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e5) {
                            e5.printStackTrace();
                        }
                        c a = a(bVar, false);
                        try {
                            localSocket.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                        return a;
                    }
                }
                try {
                    inputStream = localSocket.getInputStream();
                } catch (IOException e7) {
                    e4 = e7;
                    inputStream = null;
                    outputStream = null;
                    try {
                        e4.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e42) {
                                e42.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e422) {
                                e422.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e4222) {
                            e4222.printStackTrace();
                        }
                    } catch (Exception e8) {
                        e2 = e8;
                        try {
                            e2.printStackTrace();
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e42222) {
                                    e42222.printStackTrace();
                                }
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e422222) {
                                    e422222.printStackTrace();
                                }
                            }
                            try {
                                localSocket.close();
                            } catch (IOException e4222222) {
                                e4222222.printStackTrace();
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            inputStream2 = inputStream;
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e62) {
                                    e62.printStackTrace();
                                }
                            }
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (IOException e622) {
                                    e622.printStackTrace();
                                }
                            }
                            try {
                                localSocket.close();
                            } catch (IOException e6222) {
                                e6222.printStackTrace();
                            }
                            throw th;
                        }
                    } catch (Error e9) {
                        e3 = e9;
                        e3.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e42222222) {
                                e42222222.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e422222222) {
                                e422222222.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e4222222222) {
                            e4222222222.printStackTrace();
                        }
                        return null;
                    }
                    return null;
                }
                try {
                    outputStream = localSocket.getOutputStream();
                    try {
                        bVar.writeToStream(outputStream);
                        cVar.e(inputStream);
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e42222222222) {
                                e42222222222.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e422222222222) {
                                e422222222222.printStackTrace();
                            }
                        }
                        try {
                            localSocket.close();
                        } catch (IOException e4222222222222) {
                            e4222222222222.printStackTrace();
                        }
                    } catch (IOException e10) {
                        e4222222222222 = e10;
                        e4222222222222.printStackTrace();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        localSocket.close();
                        return null;
                    }
                } catch (IOException e11) {
                    e4222222222222 = e11;
                    outputStream = null;
                    e4222222222222.printStackTrace();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    localSocket.close();
                    return null;
                } catch (Exception e12) {
                    e2 = e12;
                    outputStream = null;
                    e2.printStackTrace();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    localSocket.close();
                    return null;
                } catch (Error e13) {
                    e3 = e13;
                    outputStream = null;
                    e3.printStackTrace();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    localSocket.close();
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    outputStream = null;
                    inputStream2 = inputStream;
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream2 != null) {
                        inputStream2.close();
                    }
                    localSocket.close();
                    throw th;
                }
            } catch (Exception e14) {
                e2 = e14;
                inputStream = null;
                outputStream = null;
                e2.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                localSocket.close();
                return null;
            } catch (Error e15) {
                e3 = e15;
                inputStream = null;
                outputStream = null;
                e3.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                localSocket.close();
                return null;
            } catch (Throwable th4) {
                th = th4;
                outputStream = null;
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                localSocket.close();
                throw th;
            }
        }
        return cVar;
    }

    public static int acquireRoot() {
        int doAcquireRoot;
        if (Lj == 0) {
            boolean iS = iS();
            d.d("Root-ScriptHelper", "acquireRoot(), sCurrRootState = ROOT_GOT; isReallyGot ? " + iS);
            if (iS) {
                return Lj;
            }
        }
        if (Lm == null) {
            doAcquireRoot = doAcquireRoot();
            d.d("Root-ScriptHelper", "do acquire root locally, root state=" + doAcquireRoot);
        } else {
            doAcquireRoot = Lm.A(4294967299L);
            d.d("Root-ScriptHelper", "do acquire root by proxy-RootService, root state=" + doAcquireRoot);
        }
        return doAcquireRoot;
    }

    public static String acquireRootAndRunScript(int i, List<String> list) {
        return acquireRoot() == 0 ? runScript(i, (List) list) : null;
    }

    public static String acquireRootAndRunScript(int i, String... strArr) {
        return acquireRootAndRunScript(i, new ArrayList(Arrays.asList(strArr)));
    }

    public static void actualStartDaemon() {
        OutputStream outputStream = null;
        int i = Lj;
        Lj = 2;
        if (i != Lj) {
            iP();
        }
        d.d("RootService", "startDaemon @ " + Process.myPid());
        String str = "chmod 755 " + str + "\n" + String.format(Locale.US, "%s %s %d", new Object[]{ms.a(TMSDKContext.getApplicaionContext(), Lg, null), Lh, Integer.valueOf(Process.myUid())}) + "\n";
        if (Lm == null || !Lm.di(str)) {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[0]);
            processBuilder.command(new String[]{"sh"});
            try {
                processBuilder.redirectErrorStream(true);
                outputStream = processBuilder.start().getOutputStream();
                outputStream.write(str.getBytes());
                outputStream.flush();
                try {
                    TMSDKContext.getApplicaionContext().sendBroadcast(new Intent(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_DAEMON_START_ACTION)), "com.tencent.qqsecure.INNER_BROCAST");
                } catch (Throwable e) {
                    d.a("Root-ScriptHelper", "broadcast ROOT_DAEMON_START, err: " + e.getMessage(), e);
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (IOException e22) {
                e22.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            } catch (Error e3) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            }
        }
    }

    public static boolean checkIfSuExist() {
        boolean z = false;
        if (ms.cy("/system/bin/su") || ms.cy("/system/xbin/su") || ms.cy("/sbin/su")) {
            z = true;
        }
        isSuExist = z;
        d.e("Root-ScriptHelper", "checkIfSuExist:[" + isSuExist + "]");
        return isSuExist;
    }

    public static int doAcquireRoot() {
        int i = Lj;
        checkIfSuExist();
        if (Li) {
            Lj = 0;
        } else if (isSuExist) {
            synchronized (Ll) {
                int i2 = 2;
                int i3 = 0;
                while (i3 < 1) {
                    i2 = iQ();
                    if (i2 == -1) {
                        i3++;
                    }
                }
                Lj = i2;
            }
        } else {
            Lj = 1;
        }
        if (!Lk && Lj == 0) {
            try {
                TMSDKContext.getApplicaionContext().sendBroadcast(new Intent(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_GOT_ACTION)), "com.tencent.qqsecure.INNER_BROCAST");
                Lk = true;
                d.d("Root-ScriptHelper", "broadcast ROOT_GOT");
            } catch (Throwable e) {
                d.a("Root-ScriptHelper", "broadcast ROOT_GOT err: " + e.getMessage(), e);
            }
        }
        if (i != Lj) {
            iP();
        }
        return Lj;
    }

    public static String[] exec(File file, String... strArr) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            ProcessBuilder processBuilder = new ProcessBuilder(strArr);
            if (file != null) {
                processBuilder.directory(file);
            }
            processBuilder.redirectErrorStream(false);
            Process start = processBuilder.start();
            InputStream inputStream = start.getInputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) {
                    inputStream.close();
                    start.destroy();
                    return stringBuffer.toString().split("\n");
                }
                stringBuffer.append(new String(bArr, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Error e2) {
        }
        return null;
    }

    public static String[] exec(String... strArr) {
        return exec(null, strArr);
    }

    public static int getRootState() {
        return Lm == null ? Lj : Lm.iV();
    }

    public static int getRootStateActual() {
        return Lj;
    }

    private static void iO() {
        int cw = jq.cw();
        String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_CHANGE_ACTION);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(strFromEnvMap);
        if (2 == cw) {
            intentFilter.addAction("tms.scripthelper.create");
            d.d("Root-ScriptHelper", "register SCRIPT_HELPER_CREATE receiver");
        }
        TMSDKContext.getApplicaionContext().registerReceiver(Lo, intentFilter);
        if (1 == cw) {
            try {
                Intent intent = new Intent("tms.scripthelper.create");
                intent.putExtra("pidky", Ln);
                TMSDKContext.getApplicaionContext().sendBroadcast(intent, "com.tencent.qqsecure.INNER_BROCAST");
                d.d("Root-ScriptHelper", "broadcast SCRIPT_HELPER_CREATE");
            } catch (Throwable e) {
                d.a("Root-ScriptHelper", "broadcast SCRIPT_HELPER_CREATE err: " + e.getMessage(), e);
            }
        }
    }

    private static void iP() {
        try {
            Intent intent = new Intent(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_ROOT_CHANGE_ACTION));
            intent.putExtra(ROOT_STATE_KEY, Lj);
            intent.putExtra("pidky", Ln);
            TMSDKContext.getApplicaionContext().sendBroadcast(intent, "com.tencent.qqsecure.INNER_BROCAST");
            d.d("Root-ScriptHelper", "broadcast root state, state: " + Lj);
        } catch (Throwable e) {
            d.a("Root-ScriptHelper", "broadcast root state, err: " + e.getMessage(), e);
        }
    }

    private static int iQ() {
        String runScript = runScript(-1, "id");
        d.e("Root-ScriptHelper", "run (id):[" + runScript + "]");
        if (runScript == null) {
            return 2;
        }
        if (!runScript.contains("uid=0")) {
            runScript = runScript(-1, "su");
            d.e("Root-ScriptHelper", "run (su):[" + runScript + "]");
            if (runScript == null) {
                return 2;
            }
            if (runScript.contains("Kill") || runScript.contains("kill")) {
                return -1;
            }
            runScript = runScript(-1, "id");
            d.e("Root-ScriptHelper", "run (su--id):[" + runScript + "]");
            if (runScript == null) {
                return 2;
            }
            if (!runScript.contains("uid=0")) {
                return 2;
            }
            List arrayList = new ArrayList();
            z(arrayList);
            runScript(-1, arrayList);
        }
        return 0;
    }

    private static void iR() {
        if (Lm != null) {
            Lm.iW();
        } else {
            actualStartDaemon();
        }
    }

    private static boolean iS() {
        b bVar = new b();
        bVar.time = 1000;
        bVar.data = "id\n".getBytes();
        c a = a(bVar, false);
        return a != null && new String(a.data).contains("uid=0");
    }

    public static void initForeMultiProcessUse() {
        iO();
    }

    public static boolean isRootGot() {
        return getRootState() == 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isRootUid() {
        d.e("Root-ScriptHelper", "isRootUid");
        synchronized (Ll) {
            String runScript = runScript(-1, "id");
            d.e("Root-ScriptHelper", "isRootUid res=" + runScript);
            if (runScript != null && runScript.contains("uid=0")) {
                return true;
            } else if (Lj == 0) {
                Lj = 2;
                iP();
            }
        }
    }

    public static boolean isSystemUid() {
        return Process.myUid() == 1000;
    }

    public static ra provider() {
        return Lp;
    }

    public static boolean providerSupportCancelMissCall() {
        return Lp != null && Lp.cA(2);
    }

    public static boolean providerSupportCpuRelative() {
        return Lp != null && Lp.cA(3);
    }

    public static boolean providerSupportGetAllApkFiles() {
        if (Lp != null) {
            if (Lp.cA(1)) {
                return true;
            }
        }
        return false;
    }

    public static boolean providerSupportPmRelative() {
        return Lp != null && Lp.cA(4);
    }

    public static String runScript(int i, List<String> list) {
        if (i < 0) {
            i = 30000;
        }
        z(list);
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list) {
            String str2;
            if (VERSION.SDK_INT >= 21 && str2 != null) {
                if (str2.indexOf("pm ") == 0 || str2.indexOf("am ") == 0 || str2.indexOf("service ") == 0) {
                    str2 = "su -cn u:r:shell:s0 -c " + str2 + " < /dev/null";
                } else if (str2.indexOf("dumpsys ") == 0) {
                    str2 = "su -cn u:r:system:s0 -c " + str2 + " < /dev/null";
                }
            }
            stringBuilder.append(str2).append("\n");
        }
        b bVar = new b();
        bVar.type = 0;
        bVar.time = i;
        bVar.data = stringBuilder.toString().getBytes();
        c a = a(bVar, true);
        if (a != null) {
            try {
                if (a.data != null) {
                    return new String(a.data).trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e2) {
            }
        }
        return null;
    }

    public static String runScript(int i, String... strArr) {
        return runScript(i, new ArrayList(Arrays.asList(strArr)));
    }

    public static void setProvider(ra raVar) {
        Object obj = null;
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getClass().equals(TMSDKContext.class) && stackTraceElement.getMethodName().indexOf("init") >= 0) {
                obj = 1;
                break;
            }
        }
        if (obj != null) {
            Lp = raVar;
        } else {
            d.c("ScriptHelper", "Unauthorized caller");
        }
    }

    public static void setRootService(a aVar) {
        Lm = aVar;
    }

    public static boolean stopDaemon() {
        b bVar = new b();
        bVar.type = 1;
        bVar.data = "echo old".getBytes();
        c a = a(bVar, false);
        return (a == null || new String(a.data).trim().contains("old")) ? false : true;
    }

    private static void z(List<String> list) {
        for (Entry entry : new ProcessBuilder(new String[0]).environment().entrySet()) {
            list.add("export " + ((String) entry.getKey()) + "=" + ((String) entry.getValue()));
        }
    }
}
