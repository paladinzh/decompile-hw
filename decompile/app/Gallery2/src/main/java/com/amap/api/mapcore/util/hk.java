package com.amap.api.mapcore.util;

import com.amap.api.mapcore.util.gt.b;
import java.io.InputStream;

/* compiled from: Utils */
public class hk {
    static byte[] a(gt gtVar, String str) {
        Throwable th;
        InputStream inputStream = null;
        byte[] bArr = new byte[0];
        b a;
        try {
            a = gtVar.a(str);
            if (a != null) {
                try {
                    inputStream = a.a(0);
                    if (inputStream != null) {
                        bArr = new byte[inputStream.available()];
                        inputStream.read(bArr);
                        gtVar.c(str);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th2) {
                                th2.printStackTrace();
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable th22) {
                                th22.printStackTrace();
                            }
                        }
                        return bArr;
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th222) {
                            th222.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable th2222) {
                            th2222.printStackTrace();
                        }
                    }
                    return bArr;
                } catch (Throwable th3) {
                    th2222 = th3;
                    try {
                        fl.a(th2222, "Utils", "readSingleLog");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th22222) {
                                th22222.printStackTrace();
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable th222222) {
                                th222222.printStackTrace();
                            }
                        }
                        return bArr;
                    } catch (Throwable th4) {
                        th222222 = th4;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th5) {
                                th5.printStackTrace();
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable th52) {
                                th52.printStackTrace();
                            }
                        }
                        throw th222222;
                    }
                }
            }
            if (a != null) {
                try {
                    a.close();
                } catch (Throwable th2222222) {
                    th2222222.printStackTrace();
                }
            }
            return bArr;
        } catch (Throwable th6) {
            th2222222 = th6;
            a = inputStream;
            if (inputStream != null) {
                inputStream.close();
            }
            if (a != null) {
                a.close();
            }
            throw th2222222;
        }
    }
}
