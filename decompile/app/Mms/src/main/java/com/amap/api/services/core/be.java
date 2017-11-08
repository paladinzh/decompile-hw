package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;
import com.amap.api.services.core.bz.b;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.json.JSONObject;

/* compiled from: LogUpDateProcessor */
abstract class be {
    private bz a;

    protected abstract String a();

    protected abstract boolean a(Context context);

    protected abstract int b();

    public static be a(Context context, int i) {
        be azVar;
        switch (i) {
            case 0:
                azVar = new az(context);
                break;
            case 1:
                azVar = new bb(context);
                break;
            case 2:
                azVar = new aw(context);
                break;
            default:
                return null;
        }
        return azVar;
    }

    protected be(Context context) {
        try {
            this.a = a(context, a());
        } catch (Throwable th) {
            av.a(th, "LogProcessor", "LogUpDateProcessor");
            th.printStackTrace();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void b(Context context) {
        try {
            if (a(context)) {
                synchronized (Looper.getMainLooper()) {
                    bq bqVar = new bq(context);
                    a(bqVar, b());
                    List a = bqVar.a(0, b());
                    if (!(a == null || a.size() == 0)) {
                        String a2 = a(a, context);
                        if (a2 == null) {
                        } else if (b(a2) == 1) {
                            a(a, bqVar, b());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            av.a(th, "LogProcessor", "processUpdateLog");
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

    private void a(bq bqVar, int i) {
        try {
            a(bqVar.a(2, i), bqVar, i);
        } catch (Throwable th) {
            av.a(th, "LogProcessor", "processDeleteFail");
            th.printStackTrace();
        }
    }

    private int b(String str) {
        byte[] b;
        try {
            b = as.b(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            b = as.b(str.getBytes());
        }
        int i;
        try {
            byte[] b2 = ci.a(false).b(new bf(b));
            if (b2 == null) {
                return 0;
            }
            String str2;
            try {
                str2 = new String(b2, "UTF-8");
            } catch (UnsupportedEncodingException e2) {
                str2 = new String(b2);
            }
            try {
                JSONObject jSONObject = new JSONObject(str2);
                str2 = "code";
                i = !jSONObject.has(str2) ? 0 : jSONObject.getInt(str2);
            } catch (Throwable e3) {
                av.a(e3, "LogProcessor", "processUpdate");
                e3.printStackTrace();
                i = 0;
            }
            return i;
        } catch (Throwable e32) {
            av.a(e32, "LogProcessor", "processUpdate");
            e32.printStackTrace();
            i = 0;
        }
    }

    private void a(List<bs> list, bq bqVar, int i) {
        if (list != null && list.size() > 0) {
            for (bs bsVar : list) {
                if (a(bsVar.b())) {
                    bqVar.a(bsVar.b(), i);
                } else {
                    bsVar.a(2);
                    bqVar.a(bsVar, bsVar.a());
                }
            }
        }
    }

    private String c(Context context) {
        String a;
        try {
            a = at.a(context);
            if ("".equals(a)) {
                return null;
            }
            a = al.b(context, a.getBytes("UTF-8"));
            return a;
        } catch (Throwable e) {
            av.a(e, "LogProcessor", "getPublicInfo");
            e.printStackTrace();
            a = null;
        } catch (Throwable e2) {
            av.a(e2, "LogProcessor", "getPublicInfo");
            e2.printStackTrace();
            a = null;
        }
    }

    private String a(List<bs> list, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"pinfo\":\"").append(c(context)).append("\",\"els\":[");
        Object obj = 1;
        for (bs bsVar : list) {
            Object obj2;
            String c = c(bsVar.b());
            if (c == null || "".equals(c)) {
                obj2 = obj;
            } else {
                String str = c + "||" + bsVar.d();
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
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable e;
        InputStream a;
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
                    byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
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
                            av.a(e2, "LogProcessor", "readLog1");
                            e2.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e22) {
                            av.a(e22, "LogProcessor", "readLog2");
                            e22.printStackTrace();
                        }
                    }
                    return byteArrayOutputStream2;
                } catch (IOException e3) {
                    e = e3;
                    try {
                        av.a(e, "LogProcessor", "readLog");
                        e.printStackTrace();
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable e4) {
                                av.a(e4, "LogProcessor", "readLog1");
                                e4.printStackTrace();
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable e42) {
                                av.a(e42, "LogProcessor", "readLog2");
                                e42.printStackTrace();
                            }
                        }
                        return null;
                    } catch (Throwable th) {
                        e42 = th;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable e222) {
                                av.a(e222, "LogProcessor", "readLog1");
                                e222.printStackTrace();
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable e2222) {
                                av.a(e2222, "LogProcessor", "readLog2");
                                e2222.printStackTrace();
                            }
                        }
                        throw e42;
                    }
                } catch (Throwable th2) {
                    e42 = th2;
                    av.a(e42, "LogProcessor", "readLog");
                    e42.printStackTrace();
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e422) {
                            av.a(e422, "LogProcessor", "readLog1");
                            e422.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e4222) {
                            av.a(e4222, "LogProcessor", "readLog2");
                            e4222.printStackTrace();
                        }
                    }
                    return null;
                }
            } catch (IOException e5) {
                e4222 = e5;
                byteArrayOutputStream = null;
                av.a(e4222, "LogProcessor", "readLog");
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
                byteArrayOutputStream = null;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (a != null) {
                    a.close();
                }
                throw e4222;
            }
        } catch (IOException e6) {
            e4222 = e6;
            byteArrayOutputStream = null;
            a = null;
            av.a(e4222, "LogProcessor", "readLog");
            e4222.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (a != null) {
                a.close();
            }
            return null;
        } catch (Throwable th4) {
            e4222 = th4;
            byteArrayOutputStream = null;
            a = null;
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
                av.a(e, "LogProcessor", "closeDiskLru");
                e.printStackTrace();
            } catch (Throwable e2) {
                av.a(e2, "LogProcessor", "closeDiskLru");
                e2.printStackTrace();
            }
        }
    }

    private bz a(Context context, String str) {
        bz a;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getFilesDir().getAbsolutePath());
            stringBuilder.append(bd.a);
            stringBuilder.append(str);
            File file = new File(stringBuilder.toString());
            if (!file.exists() && !file.mkdirs()) {
                return null;
            }
            a = bz.a(file, 1, 1, 20480);
            return a;
        } catch (Throwable e) {
            av.a(e, "LogProcessor", "initDiskLru");
            e.printStackTrace();
            a = null;
        } catch (Throwable e2) {
            av.a(e2, "LogProcessor", "initDiskLru");
            e2.printStackTrace();
            a = null;
        }
    }
}
