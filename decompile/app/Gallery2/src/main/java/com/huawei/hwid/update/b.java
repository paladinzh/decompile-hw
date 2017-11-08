package com.huawei.hwid.update;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.hwid.core.d.b.e;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.client.ClientProtocolException;

public class b extends Thread {
    private g a;
    private Handler b;
    private Context c;
    private int d;
    private boolean e = false;
    private Map<Integer, com.huawei.hwid.update.a.b> f = null;

    b(Context context, g gVar, int i, Handler handler) {
        this.a = gVar;
        this.b = handler;
        this.c = context;
        this.d = i;
    }

    public void a() {
        this.e = true;
    }

    private void a(int i) {
        Message obtainMessage = this.b.obtainMessage(i);
        obtainMessage.obj = this.f;
        obtainMessage.sendToTarget();
    }

    public void run() {
        e.b("OtaCheckVersionThread", "CheckVersionThread run");
        ByteArrayOutputStream a = a(this.c, this.a, this.d, 0);
        if (a != null) {
            String str = new String(a.toByteArray(), Charset.forName(XmlUtils.INPUT_ENCODING));
            try {
                a.close();
            } catch (IOException e) {
                e.d("OtaCheckVersionThread", "outputStream close IOException");
            }
            e.b("OtaCheckVersionThread", "responseStr");
            String str2 = "1";
            this.f = Collections.synchronizedMap(new HashMap(5));
            Map hashMap = new HashMap(5);
            if (TextUtils.isEmpty(str)) {
                a(7);
                e.d("OtaCheckVersionThread", "parse response error");
                return;
            }
            str = j.a(str, hashMap);
            if ("1".equals(str)) {
                e.b("OtaCheckVersionThread", "no new version");
                a(2);
                return;
            } else if ("-1".equals(str) || GpsMeasureMode.MODE_2_DIMENSIONAL.equals(str)) {
                e.d("OtaCheckVersionThread", "tcs server error,response code is " + str);
                a(7);
                return;
            } else {
                Iterator it = hashMap.entrySet().iterator();
                for (boolean hasNext = it.hasNext(); hasNext; hasNext = it.hasNext()) {
                    com.huawei.hwid.update.a.b bVar = (com.huawei.hwid.update.a.b) ((Entry) it.next()).getValue();
                    if (TextUtils.isEmpty(bVar.i())) {
                        a(7);
                        return;
                    }
                    ByteArrayOutputStream a2 = a(this.c, this.a, bVar.i() + "full" + "/filelist.xml", 0);
                    if (a2 == null) {
                        e.d("OtaCheckVersionThread", "tcs server error,response code is " + str);
                        a(7);
                        return;
                    }
                    bVar.a(new ByteArrayInputStream(a2.toByteArray()));
                    try {
                        a2.close();
                    } catch (IOException e2) {
                        e.d("OtaCheckVersionThread", "outputStreamFileList close IOException");
                    }
                    a(this.c, bVar);
                }
                if (!this.e) {
                    this.f = hashMap;
                }
                a(1);
                return;
            }
        }
        a(7);
        e.d("OtaCheckVersionThread", "check response error");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(Context context, com.huawei.hwid.update.a.b bVar) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = this.a.a(context, bVar.i() + "full" + "/changelog.xml");
            if (byteArrayOutputStream != null) {
                bVar.a(context, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (ClientProtocolException e2) {
            e.d("OtaCheckVersionThread", "get apk changelog error");
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (IOException e4) {
            e.d("OtaCheckVersionThread", "get apk changelog error");
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            ByteArrayOutputStream byteArrayOutputStream2 = byteArrayOutputStream;
            Throwable th3 = th2;
            if (byteArrayOutputStream2 != null) {
                try {
                    byteArrayOutputStream2.close();
                } catch (IOException e6) {
                }
            }
            throw th3;
        }
    }

    private ByteArrayOutputStream a(Context context, g gVar, int i, int i2) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        if (i2 >= 3) {
            return byteArrayOutputStream;
        }
        int i3 = i2 + 1;
        try {
            byteArrayOutputStream = gVar.a(context, i);
        } catch (IllegalArgumentException e) {
            e.d("OtaCheckVersionThread", "sendRequestToServer IllegalArgumentException startFromTimes: " + i3);
        } catch (IllegalStateException e2) {
            e.d("OtaCheckVersionThread", "sendRequestToServer IllegalStateException startFromTimes: " + i3);
        } catch (IOException e3) {
            e.d("OtaCheckVersionThread", "sendRequestToServer IOException startFromTimes: " + i3);
        } catch (Exception e4) {
            e.d("OtaCheckVersionThread", "sendRequestToServer Exception startFromTimes: " + i3);
        }
        if (byteArrayOutputStream == null) {
            e.d("OtaCheckVersionThread", "sendRequestToServer fail startFromTimes: " + i3);
            byteArrayOutputStream = a(context, gVar, i, i3);
        }
        return byteArrayOutputStream;
    }

    private ByteArrayOutputStream a(Context context, g gVar, String str, int i) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        if (i >= 3) {
            return byteArrayOutputStream;
        }
        int i2 = i + 1;
        try {
            byteArrayOutputStream = gVar.b(context, str);
        } catch (ClientProtocolException e) {
            e.d("OtaCheckVersionThread", "getFileListFromServer ClientProtocolException startFromTimes: " + i2);
        } catch (IOException e2) {
            e.d("OtaCheckVersionThread", "getFileListFromServer ClientProtocolException startFromTimes: " + i2);
        } catch (Exception e3) {
            e.d("OtaCheckVersionThread", "getFileListFromServer Exception startFromTimes: " + i2);
        }
        if (byteArrayOutputStream == null) {
            e.d("OtaCheckVersionThread", "getFileListFromServer fail startFromTimes: " + i2);
            byteArrayOutputStream = a(context, gVar, str, i2);
        }
        return byteArrayOutputStream;
    }
}
