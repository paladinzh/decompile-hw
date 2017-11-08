package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.amap.api.services.core.bk.b;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.json.JSONObject;

/* compiled from: LogUpDateProcessor */
abstract class bg {
    private bk a;

    protected abstract String a();

    protected abstract boolean a(Context context);

    protected abstract int b();

    public static bg a(Context context, int i) {
        bg bbVar;
        switch (i) {
            case 0:
                bbVar = new bb(context);
                break;
            case 1:
                bbVar = new bd(context);
                break;
            case 2:
                bbVar = new az(context);
                break;
            default:
                return null;
        }
        return bbVar;
    }

    protected bg(Context context) {
        try {
            this.a = a(context, a());
        } catch (Throwable th) {
            ay.a(th, "LogProcessor", "LogUpDateProcessor");
            th.printStackTrace();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void b(Context context) {
        try {
            if (a(context)) {
                synchronized (Looper.getMainLooper()) {
                    ak akVar = new ak(context);
                    a(akVar, b());
                    List a = akVar.a(0, b());
                    if (!(a == null || a.size() == 0)) {
                        String a2 = a(a, context);
                        if (a2 == null) {
                        } else if (b(a2) == 1) {
                            a(a, akVar, b());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            ay.a(th, "LogProcessor", "processUpdateLog");
            th.printStackTrace();
        }
    }

    private boolean a(String str) {
        if (this.a == null) {
            return false;
        }
        boolean c;
        try {
            c = this.a.c(str);
        } catch (IOException e) {
            e.printStackTrace();
            c = false;
        }
        return c;
    }

    private void a(ak akVar, int i) {
        try {
            a(akVar.a(2, i), akVar, i);
        } catch (Throwable th) {
            ay.a(th, "LogProcessor", "processDeleteFail");
            th.printStackTrace();
        }
    }

    private int b(String str) {
        int i = 0;
        Log.i("yiyi.qi", str);
        try {
            byte[] a = bs.a(false).a(new bh(ae.b(str.getBytes())));
            if (a == null) {
                return 0;
            }
            try {
                JSONObject jSONObject = new JSONObject(new String(a));
                String str2 = "code";
                if (jSONObject.has(str2)) {
                    i = jSONObject.getInt(str2);
                }
            } catch (Throwable e) {
                ay.a(e, "LogProcessor", "processUpdate");
                e.printStackTrace();
            }
            return i;
        } catch (Throwable e2) {
            ay.a(e2, "LogProcessor", "processUpdate");
            e2.printStackTrace();
        }
    }

    private void a(List<am> list, ak akVar, int i) {
        if (list != null && list.size() > 0) {
            for (am amVar : list) {
                if (a(amVar.b())) {
                    akVar.a(amVar.b(), i);
                } else {
                    amVar.a(2);
                    akVar.a(amVar, amVar.a());
                }
            }
        }
    }

    private String c(Context context) {
        String a;
        try {
            a = y.a(context);
            if ("".equals(a)) {
                return null;
            }
            a = y.b(context, a.getBytes(XmlUtils.INPUT_ENCODING));
            return a;
        } catch (Throwable e) {
            ay.a(e, "LogProcessor", "getPublicInfo");
            e.printStackTrace();
            a = null;
        } catch (Throwable e2) {
            ay.a(e2, "LogProcessor", "getPublicInfo");
            e2.printStackTrace();
            a = null;
        }
    }

    private String a(List<am> list, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"pinfo\":\"").append(c(context)).append("\",\"els\":[");
        Object obj = 1;
        for (am amVar : list) {
            Object obj2;
            String c = c(amVar.b());
            if (c == null || "".equals(c)) {
                obj2 = obj;
            } else {
                String str = c + "||" + amVar.d();
                if (obj == null) {
                    stringBuilder.append(",");
                } else {
                    obj = null;
                }
                stringBuilder.append("{\"log\":\"").append(str).append("\"}");
                obj2 = obj;
            }
            obj = obj2;
        }
        if (obj != null) {
            return null;
        }
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    private String c(String str) {
        InputStream a;
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable e;
        try {
            if (this.a == null) {
                return null;
            }
            b a2 = this.a.a(str);
            if (a2 == null) {
                return null;
            }
            a = a2.a(0);
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = a.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    String byteArrayOutputStream2 = byteArrayOutputStream.toString("utf-8");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e2) {
                            ay.a(e2, "LogProcessor", "readLog1");
                            e2.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e22) {
                            ay.a(e22, "LogProcessor", "readLog2");
                            e22.printStackTrace();
                        }
                    }
                    return byteArrayOutputStream2;
                } catch (IOException e3) {
                    e = e3;
                    ay.a(e, "LogProcessor", "readLog");
                    e.printStackTrace();
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e4) {
                            ay.a(e4, "LogProcessor", "readLog1");
                            e4.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e42) {
                            ay.a(e42, "LogProcessor", "readLog2");
                            e42.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    e42 = th;
                    ay.a(e42, "LogProcessor", "readLog");
                    e42.printStackTrace();
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e422) {
                            ay.a(e422, "LogProcessor", "readLog1");
                            e422.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e4222) {
                            ay.a(e4222, "LogProcessor", "readLog2");
                            e4222.printStackTrace();
                        }
                    }
                    return null;
                }
            } catch (IOException e5) {
                e4222 = e5;
                byteArrayOutputStream = null;
                ay.a(e4222, "LogProcessor", "readLog");
                e4222.printStackTrace();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (a != null) {
                    a.close();
                }
                return null;
            } catch (Throwable th2) {
                e4222 = th2;
                byteArrayOutputStream = null;
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (Throwable e222) {
                        ay.a(e222, "LogProcessor", "readLog1");
                        e222.printStackTrace();
                    }
                }
                if (a != null) {
                    try {
                        a.close();
                    } catch (Throwable e2222) {
                        ay.a(e2222, "LogProcessor", "readLog2");
                        e2222.printStackTrace();
                    }
                }
                throw e4222;
            }
        } catch (IOException e6) {
            e4222 = e6;
            byteArrayOutputStream = null;
            a = null;
            ay.a(e4222, "LogProcessor", "readLog");
            e4222.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (a != null) {
                a.close();
            }
            return null;
        } catch (Throwable th3) {
            e4222 = th3;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (a != null) {
                a.close();
            }
            throw e4222;
        }
    }

    void c() {
        if (this.a != null && !this.a.a()) {
            try {
                this.a.close();
            } catch (Throwable e) {
                ay.a(e, "LogProcessor", "closeDiskLru");
                e.printStackTrace();
            } catch (Throwable e2) {
                ay.a(e2, "LogProcessor", "closeDiskLru");
                e2.printStackTrace();
            }
        }
    }

    private bk a(Context context, String str) {
        bk a;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getFilesDir().getAbsolutePath());
            stringBuilder.append(bf.a);
            stringBuilder.append(str);
            File file = new File(stringBuilder.toString());
            if (!file.exists() && !file.mkdirs()) {
                return null;
            }
            a = bk.a(file, 1, 1, 20480);
            return a;
        } catch (Throwable e) {
            ay.a(e, "LogProcessor", "initDiskLru");
            e.printStackTrace();
            a = null;
        } catch (Throwable e2) {
            ay.a(e2, "LogProcessor", "initDiskLru");
            e2.printStackTrace();
            a = null;
        }
    }
}
