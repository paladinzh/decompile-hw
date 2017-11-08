package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.mapcore.s;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: OfflineUpdateCityHandler */
public class p extends aj<String, List<OfflineMapProvince>> {
    private Context j;

    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    protected /* synthetic */ Object b(byte[] bArr) throws AMapException {
        return a(bArr);
    }

    public p(Context context, String str) {
        super(context, str);
        getClass();
        a(5000);
        getClass();
        b(50000);
    }

    public void a(Context context) {
        this.j = context;
    }

    protected List<OfflineMapProvince> a(byte[] bArr) throws AMapException {
        List<OfflineMapProvince> arrayList = new ArrayList();
        List<OfflineMapProvince> a;
        try {
            String str = new String(bArr, "utf-8");
            bj.a(str);
            if (str == null || "".equals(str)) {
                return arrayList;
            }
            String optString = new JSONObject(str).optString("status");
            if (optString == null || optString.equals("") || optString.equals("0")) {
                return arrayList;
            }
            a = a(str);
            return a;
        } catch (Throwable th) {
            ce.a(th, "OfflineUpdateCityHandler", "loadData jsonInit");
            th.printStackTrace();
            a = arrayList;
        }
    }

    private void c(String str) {
        OutputStream fileOutputStream;
        Throwable e;
        Object obj = null;
        if (!bj.b(this.j).equals("")) {
            File file = new File(bj.b(this.j) + "offlinemapv4.png");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Throwable e2) {
                    ce.a(e2, "OfflineUpdateCityHandler", "writeSD dirCreate");
                    e2.printStackTrace();
                }
            }
            if (b_() <= 1048576) {
                obj = 1;
            }
            if (obj == null) {
                try {
                    fileOutputStream = new FileOutputStream(file);
                    try {
                        fileOutputStream.write(str.getBytes("utf-8"));
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        try {
                            ce.a(e, "OfflineUpdateCityHandler", "writeSD filenotfound");
                            e.printStackTrace();
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                }
                            }
                        } catch (Throwable th) {
                            e = th;
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e5) {
                                    e5.printStackTrace();
                                }
                            }
                            throw e;
                        }
                    } catch (IOException e6) {
                        e = e6;
                        ce.a(e, "OfflineUpdateCityHandler", "writeSD io");
                        e.printStackTrace();
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                    }
                } catch (FileNotFoundException e7) {
                    e = e7;
                    fileOutputStream = null;
                    ce.a(e, "OfflineUpdateCityHandler", "writeSD filenotfound");
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e8) {
                    e = e8;
                    fileOutputStream = null;
                    ce.a(e, "OfflineUpdateCityHandler", "writeSD io");
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (Throwable th2) {
                    e = th2;
                    fileOutputStream = null;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw e;
                }
            }
        }
    }

    public long b_() {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return 0;
        }
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
    }

    public String a() {
        return "http://restapi.amap.com/v3/config/resource";
    }

    protected List<OfflineMapProvince> a(String str) throws AMapException {
        try {
            if (this.j != null) {
                c(str);
            }
        } catch (Throwable th) {
            ce.a(th, "OfflineUpdateCityHandler", "loadData jsonInit");
            th.printStackTrace();
        }
        try {
            return af.b(str);
        } catch (Throwable th2) {
            ce.a(th2, "OfflineUpdateCityHandler", "loadData parseJson");
            th2.printStackTrace();
            return null;
        }
    }

    public Map<String, String> b() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("key", bl.f(this.j));
        hashMap.put("opertype", "offlinemap_with_province_vfour");
        hashMap.put("plattype", "android");
        hashMap.put("product", s.b);
        hashMap.put(NumberInfo.VERSION_KEY, "3.3.0");
        hashMap.put("ext", "standard");
        hashMap.put("output", "json");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(bl.f(this.j));
        stringBuffer.append("&opertype=offlinemap_with_province_vfour");
        stringBuffer.append("&plattype=android");
        stringBuffer.append("&product=").append(s.b);
        stringBuffer.append("&version=").append("3.3.0");
        stringBuffer.append("&ext=standard");
        stringBuffer.append("&output=json");
        String d = bx.d(stringBuffer.toString());
        String a = bn.a();
        hashMap.put("ts", a);
        hashMap.put("scode", bn.a(this.j, a, d));
        return hashMap;
    }
}
