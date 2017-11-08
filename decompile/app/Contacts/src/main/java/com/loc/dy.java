package com.loc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Process;
import com.google.android.gms.common.ConnectionResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class dy {
    private Context a = null;
    private boolean b = true;
    private int c = 1270;
    private int d = 310;
    private int e = 4;
    private int f = 200;
    private int g = 1;
    private int h = 0;
    private int i = 0;
    private long j = 0;
    private dx k = null;

    private dy(Context context) {
        this.a = context;
    }

    private static int a(byte[] bArr, int i) {
        int i2 = 0;
        int i3 = 0;
        while (i2 < 4) {
            i3 += (bArr[i2 + i] & 255) << (i2 << 3);
            i2++;
        }
        return i3;
    }

    protected static dy a(Context context) {
        FileInputStream fileInputStream;
        Throwable th;
        FileInputStream fileInputStream2 = null;
        dy dyVar = new dy(context);
        dyVar.h = 0;
        dyVar.i = 0;
        dyVar.j = ((System.currentTimeMillis() + 28800000) / 86400000) * 86400000;
        try {
            fileInputStream = new FileInputStream(new File(b(context) + File.separator + "data_carrier_status"));
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[32];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
                byteArrayOutputStream.flush();
                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                if (toByteArray != null) {
                    if (toByteArray.length >= 22) {
                        dyVar.b = toByteArray[0] != (byte) 0;
                        dyVar.c = (toByteArray[1] * 10) << 10;
                        dyVar.d = (toByteArray[2] * 10) << 10;
                        dyVar.e = toByteArray[3];
                        dyVar.f = toByteArray[4] * 10;
                        dyVar.g = toByteArray[5];
                        long b = b(toByteArray, 14);
                        if ((dyVar.j - b >= 86400000 ? 1 : 0) == 0) {
                            dyVar.j = b;
                            dyVar.h = a(toByteArray, 6);
                            dyVar.i = a(toByteArray, 10);
                        }
                    }
                }
                byteArrayOutputStream.close();
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                }
            } catch (Exception e2) {
            } catch (Throwable th2) {
                Throwable th3 = th2;
                fileInputStream2 = fileInputStream;
                th = th3;
            }
        } catch (Exception e3) {
            fileInputStream = null;
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e4) {
                }
            }
            return dyVar;
        } catch (Throwable th4) {
            th = th4;
            if (fileInputStream2 != null) {
                try {
                    fileInputStream2.close();
                } catch (Exception e5) {
                }
            }
            throw th;
        }
        return dyVar;
    }

    private static byte[] a(long j) {
        byte[] bArr = new byte[8];
        for (int i = 0; i < 8; i++) {
            bArr[i] = (byte) ((byte) ((int) ((j >> (i << 3)) & 255)));
        }
        return bArr;
    }

    private static long b(byte[] bArr, int i) {
        int i2 = 0;
        int i3 = 0;
        while (i2 < 8) {
            i3 += (bArr[i2 + 14] & 255) << (i2 << 3);
            i2++;
        }
        return (long) i3;
    }

    private static String b(Context context) {
        File file = null;
        boolean z = false;
        if (Process.myUid() != 1000) {
            file = di.a(context);
        }
        try {
            z = "mounted".equals(Environment.getExternalStorageState());
        } catch (Exception e) {
        }
        if (z || !di.c()) {
            if (file != null) {
                return file.getPath();
            }
        }
        return context.getFilesDir().getPath();
    }

    private static byte[] c(int i) {
        byte[] bArr = new byte[4];
        for (int i2 = 0; i2 < 4; i2++) {
            bArr[i2] = (byte) ((byte) (i >> (i2 << 3)));
        }
        return bArr;
    }

    private void g() {
        long currentTimeMillis = System.currentTimeMillis() + 28800000;
        if ((currentTimeMillis - this.j <= 86400000 ? 1 : 0) == 0) {
            this.j = (currentTimeMillis / 86400000) * 86400000;
            this.h = 0;
            this.i = 0;
        }
    }

    protected final void a(int i) {
        g();
        if (i < 0) {
            i = 0;
        }
        this.h = i;
    }

    protected final void a(dx dxVar) {
        this.k = dxVar;
    }

    protected final boolean a() {
        g();
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.a.getSystemService("connectivity")).getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected()) ? activeNetworkInfo.getType() != 1 ? this.b && this.i < this.d : this.b && this.h < this.c : this.b;
    }

    protected final boolean a(String str) {
        boolean z;
        FileOutputStream fileOutputStream;
        FileOutputStream fileOutputStream2;
        Throwable th;
        int i = 0;
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("e")) {
                this.b = jSONObject.getInt("e") != 0;
            }
            if (jSONObject.has("d")) {
                int i2 = jSONObject.getInt("d");
                this.c = ((i2 & 127) * 10) << 10;
                this.d = (((i2 & 3968) >> 7) * 10) << 10;
                this.e = (520192 & i2) >> 12;
                this.f = ((66584576 & i2) >> 19) * 10;
                this.g = (i2 & 2080374784) >> 26;
                if (this.g == 31) {
                    this.g = ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED;
                }
                if (this.k != null) {
                    this.k.a();
                }
            }
            if (jSONObject.has("u") && jSONObject.getInt("u") != 0) {
                z = true;
                g();
                fileOutputStream = new FileOutputStream(new File(b(this.a) + File.separator + "data_carrier_status"));
                byte[] c = c(this.h);
                byte[] c2 = c(this.i);
                byte[] a = a(this.j);
                byte[] bArr = new byte[22];
                if (this.b) {
                    i = 1;
                }
                bArr[0] = (byte) ((byte) i);
                bArr[1] = (byte) ((byte) (this.c / 10240));
                bArr[2] = (byte) ((byte) (this.d / 10240));
                bArr[3] = (byte) ((byte) this.e);
                bArr[4] = (byte) ((byte) (this.f / 10));
                bArr[5] = (byte) ((byte) this.g);
                bArr[6] = (byte) c[0];
                bArr[7] = (byte) c[1];
                bArr[8] = (byte) c[2];
                bArr[9] = (byte) c[3];
                bArr[10] = (byte) c2[0];
                bArr[11] = (byte) c2[1];
                bArr[12] = (byte) c2[2];
                bArr[13] = (byte) c2[3];
                bArr[14] = (byte) a[0];
                bArr[15] = (byte) a[1];
                bArr[16] = (byte) a[2];
                bArr[17] = (byte) a[3];
                bArr[18] = (byte) a[4];
                bArr[19] = (byte) a[5];
                bArr[20] = (byte) a[6];
                bArr[21] = (byte) a[7];
                fileOutputStream.write(bArr);
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                }
                return z;
            }
        } catch (Exception e2) {
        }
        z = false;
        try {
            g();
            fileOutputStream = new FileOutputStream(new File(b(this.a) + File.separator + "data_carrier_status"));
            try {
                byte[] c3 = c(this.h);
                byte[] c22 = c(this.i);
                byte[] a2 = a(this.j);
                byte[] bArr2 = new byte[22];
                if (this.b) {
                    i = 1;
                }
                bArr2[0] = (byte) ((byte) i);
                bArr2[1] = (byte) ((byte) (this.c / 10240));
                bArr2[2] = (byte) ((byte) (this.d / 10240));
                bArr2[3] = (byte) ((byte) this.e);
                bArr2[4] = (byte) ((byte) (this.f / 10));
                bArr2[5] = (byte) ((byte) this.g);
                bArr2[6] = (byte) c3[0];
                bArr2[7] = (byte) c3[1];
                bArr2[8] = (byte) c3[2];
                bArr2[9] = (byte) c3[3];
                bArr2[10] = (byte) c22[0];
                bArr2[11] = (byte) c22[1];
                bArr2[12] = (byte) c22[2];
                bArr2[13] = (byte) c22[3];
                bArr2[14] = (byte) a2[0];
                bArr2[15] = (byte) a2[1];
                bArr2[16] = (byte) a2[2];
                bArr2[17] = (byte) a2[3];
                bArr2[18] = (byte) a2[4];
                bArr2[19] = (byte) a2[5];
                bArr2[20] = (byte) a2[6];
                bArr2[21] = (byte) a2[7];
                fileOutputStream.write(bArr2);
                fileOutputStream.close();
            } catch (Exception e3) {
                fileOutputStream2 = fileOutputStream;
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (Exception e4) {
                    }
                }
                return z;
            } catch (Throwable th2) {
                th = th2;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e5) {
                    }
                }
                throw th;
            }
        } catch (Exception e6) {
            fileOutputStream2 = null;
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            return z;
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th;
        }
        return z;
    }

    protected final int b() {
        return this.e;
    }

    protected final void b(int i) {
        g();
        if (i < 0) {
            i = 0;
        }
        this.i = i;
    }

    protected final int c() {
        return this.f;
    }

    protected final int d() {
        return this.g;
    }

    protected final int e() {
        g();
        return this.h;
    }

    protected final int f() {
        g();
        return this.i;
    }
}
