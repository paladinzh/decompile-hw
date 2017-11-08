package com.huawei.hwid.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.update.a.b;
import com.huawei.hwid.update.a.c;
import com.huawei.hwid.update.b.a;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class f extends Thread {
    private Context a;
    private Handler b;
    private int c;
    private boolean d = false;
    private HttpURLConnection e = null;
    private HttpURLConnection f = null;
    private FileOutputStream g = null;
    private InputStream h = null;

    public f(Context context, Handler handler, int i) {
        this.a = context;
        this.b = handler;
        this.c = i;
    }

    public void a() {
        this.d = true;
    }

    private void a(int i, int i2) {
        Message obtainMessage = this.b.obtainMessage(i);
        obtainMessage.obj = Integer.valueOf(i2);
        obtainMessage.sendToTarget();
    }

    private void b(int i, int i2) {
        Message obtainMessage = this.b.obtainMessage(3);
        obtainMessage.arg1 = i / 1000;
        obtainMessage.arg2 = i2 / 1000;
        obtainMessage.obj = Integer.valueOf(this.c);
        obtainMessage.sendToTarget();
    }

    public boolean a(b bVar) {
        String h = bVar.h();
        String a = a.a(bVar.f());
        if (a == null) {
            e.b("OtaDownloadThread", "checkMd5 md5Hex: is null");
        }
        if (h == null || a == null) {
            return false;
        }
        return h.equals(a);
    }

    private void a(Context context, int i, b bVar, File file, String str) {
        if (i == bVar.c() && a(bVar)) {
            e.b("OtaDownloadThread", "downloadSize == versionInfo.getTotalSize() versionName: " + com.huawei.hwid.core.encrypt.f.a(bVar.b()));
            i.a(this.a).a(this.a, Integer.toString(this.c), bVar.b(), str);
            a(5, this.c);
            a(context, GpsMeasureMode.MODE_3_DIMENSIONAL, bVar.g(), bVar.b(), "update success");
            return;
        }
        e.d("OtaDownloadThread", "download error, file md5 or rsa check failure");
        a(file);
        a(context, "4", bVar.g(), bVar.b(), "download error, file md5 check failure");
        a(4, this.c);
    }

    private void b() {
        if (this.e != null) {
            this.e.disconnect();
        }
        if (this.f != null) {
            this.f.disconnect();
        }
    }

    private boolean a(URL url, b bVar) {
        e.b("OtaDownloadThread", "Exception");
        try {
            this.f = (HttpURLConnection) url.openConnection();
            this.f.setConnectTimeout(30000);
            this.f.setReadTimeout(30000);
            int responseCode = this.f.getResponseCode();
            if (responseCode == SmsCheckResult.ESCT_200) {
                bVar.a(Integer.valueOf(this.f.getHeaderField("Content-Length")).intValue());
                this.h = this.f.getInputStream();
                return true;
            }
            e.d("OtaDownloadThread", "server response code is not 200,code is " + responseCode);
            a(4, this.c);
            return false;
        } catch (NumberFormatException e) {
            e.d("OtaDownloadThread", "get download content-length failure,error is " + e.getMessage());
            a(4, this.c);
            return false;
        } catch (Exception e2) {
            e.d("OtaDownloadThread", "http Exception" + e2.getMessage());
            a(4, this.c);
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        File file;
        SocketException e;
        File file2 = null;
        e.b("OtaDownloadThread", "startDownloadVersion run");
        this.e = null;
        this.f = null;
        this.g = null;
        this.h = null;
        byte[] bArr = new byte[2048];
        b b = e.a().b(this.c);
        if (b != null) {
            b.a(true);
            b(1000, b.c());
            URL url;
            try {
                String e2 = b.e();
                if (e2.startsWith("https:")) {
                    e2 = e2.replaceFirst("https:", "http:");
                }
                url = new URL(e2);
                this.e = (HttpURLConnection) url.openConnection();
                this.e.setConnectTimeout(30000);
                this.e.setReadTimeout(30000);
                this.e.setRequestMethod("GET");
                int responseCode = this.e.getResponseCode();
                if (responseCode == SmsCheckResult.ESCT_200) {
                    b.a(Integer.valueOf(this.e.getHeaderField("Content-Length")).intValue());
                    this.h = this.e.getInputStream();
                    try {
                        int c = b.c();
                        e2 = b.d();
                        if (this.a != null) {
                            String a = a(this.a);
                            if (a != null) {
                                String str;
                                String str2 = a + e2;
                                e.b("OtaDownloadThread", "downloadPath: " + com.huawei.hwid.core.encrypt.f.a(str2));
                                file = new File(str2);
                                this.g = new FileOutputStream(new File(str2));
                                b.e(str2);
                                b(6000, b.c());
                                int read = this.h.read(bArr);
                                int i = 0;
                                responseCode = 0;
                                while (read != -1) {
                                    if (this.d) {
                                        e.b("OtaDownloadThread", "cancledownload is true");
                                        j.a(this.h, "OtaDownloadThread");
                                        j.a(this.g, "OtaDownloadThread");
                                        this.b.obtainMessage(11).sendToTarget();
                                        b.a(false);
                                        j.a(this.h, "OtaDownloadThread");
                                        j.a(this.g, "OtaDownloadThread");
                                        b();
                                        return;
                                    }
                                    this.g.write(bArr, 0, read);
                                    read += i;
                                    responseCode++;
                                    if (responseCode % SmsCheckResult.ESCT_200 != 0) {
                                        if (read != b.c()) {
                                            i = read;
                                            read = this.h.read(bArr);
                                        }
                                    }
                                    Message obtainMessage = this.b.obtainMessage(3);
                                    obtainMessage.arg1 = read / 1000;
                                    obtainMessage.arg2 = c / 1000;
                                    obtainMessage.obj = Integer.valueOf(this.c);
                                    obtainMessage.sendToTarget();
                                    i = read;
                                    read = this.h.read(bArr);
                                }
                                PackageInfo packageArchiveInfo = this.a.getPackageManager().getPackageArchiveInfo(b.f(), 1);
                                if (packageArchiveInfo != null) {
                                    str = packageArchiveInfo.applicationInfo.packageName;
                                }
                                if (str == null || TextUtils.isEmpty(str) || !"com.huawei.hwid".equals(str)) {
                                    e.d("OtaDownloadThread", "PackageName is wrong: " + com.huawei.hwid.core.encrypt.f.a(str));
                                    a(file);
                                    a(this.a, "4", b.g(), b.b(), "download error, file md5 check failure");
                                    a(4, this.c);
                                    b.a(false);
                                    j.a(this.h, "OtaDownloadThread");
                                    j.a(this.g, "OtaDownloadThread");
                                    b();
                                    return;
                                }
                                e.b("OtaDownloadThread", "downloadSize: " + com.huawei.hwid.core.encrypt.f.a(Integer.valueOf(b.c())) + " downloadSize" + com.huawei.hwid.core.encrypt.f.a(Integer.valueOf(i)));
                                a(this.a, i, b, file, str2);
                                b.a(false);
                                j.a(this.h, "OtaDownloadThread");
                                j.a(this.g, "OtaDownloadThread");
                                b();
                                return;
                            }
                            e.b("OtaDownloadThread", "externalCacheDir is null");
                            a(4, this.c);
                            b.a(false);
                            j.a(this.h, "OtaDownloadThread");
                            j.a(this.g, "OtaDownloadThread");
                            b();
                            return;
                        }
                        e.b("OtaDownloadThread", "mContext is null");
                        a(4, this.c);
                        b.a(false);
                        j.a(this.h, "OtaDownloadThread");
                        j.a(this.g, "OtaDownloadThread");
                        b();
                        return;
                    } catch (SocketException e3) {
                        e = e3;
                        file2 = file;
                    } catch (IOException e4) {
                    } catch (Exception e5) {
                        e.d("OtaDownloadThread", "download Exception: " + e5.getMessage());
                        a(4, this.c);
                        b.a(false);
                        j.a(this.h, "OtaDownloadThread");
                        j.a(this.g, "OtaDownloadThread");
                        b();
                    } catch (Throwable th) {
                        b.a(false);
                        j.a(this.h, "OtaDownloadThread");
                        j.a(this.g, "OtaDownloadThread");
                        b();
                    }
                } else {
                    e.d("OtaDownloadThread", "server response code is not 200,code is " + responseCode);
                    a(4, this.c);
                    b.a(false);
                    j.a(this.h, "OtaDownloadThread");
                    j.a(this.g, "OtaDownloadThread");
                    b();
                    return;
                }
            } catch (NumberFormatException e6) {
                e.d("OtaDownloadThread", "NumberFormatException");
                a(4, this.c);
                b.a(false);
                j.a(this.h, "OtaDownloadThread");
                j.a(this.g, "OtaDownloadThread");
                b();
                return;
            } catch (RuntimeException e7) {
                e.b("OtaDownloadThread", "RuntimeException");
                if (!a(url, b)) {
                    b.a(false);
                    j.a(this.h, "OtaDownloadThread");
                    j.a(this.g, "OtaDownloadThread");
                    b();
                    return;
                }
            } catch (Exception e8) {
                e.b("OtaDownloadThread", "Exception");
                if (!a(url, b)) {
                    b.a(false);
                    j.a(this.h, "OtaDownloadThread");
                    j.a(this.g, "OtaDownloadThread");
                    b();
                    return;
                }
            } catch (SocketException e9) {
                e = e9;
                e.d("OtaDownloadThread", "download error, error is socketException");
                a(file2);
                a(this.a, "4", b.g(), b.b(), e.toString());
                a(4, this.c);
                b.a(false);
                j.a(this.h, "OtaDownloadThread");
                j.a(this.g, "OtaDownloadThread");
                b();
                return;
            } catch (IOException e10) {
                file = null;
                e.d("OtaDownloadThread", "download error, error is ioexception");
                a(file);
                a(4, this.c);
                b.a(false);
                j.a(this.h, "OtaDownloadThread");
                j.a(this.g, "OtaDownloadThread");
                b();
                return;
            }
        }
        e.b("OtaDownloadThread", "no new version to download");
        a(4, this.c);
    }

    private boolean a(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }

    private boolean a(Context context, String str, String str2, String str3, String str4) {
        e.b("OtaDownloadThread", "send updateReport");
        g gVar = new g();
        try {
            c cVar = new c();
            cVar.a(str);
            cVar.c(str3);
            cVar.d(str4);
            cVar.b(str2);
            return gVar.a(context, cVar);
        } catch (IOException e) {
            e.d("OtaDownloadThread", "send update report error ,error is " + e.getMessage());
            return false;
        } catch (Exception e2) {
            e.d("OtaDownloadThread", "send update report Exception ,Exception is " + e2.getMessage());
            return false;
        }
    }

    public static String a(Context context) {
        if (!"mounted".equals(Environment.getExternalStorageState())) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) {
            stringBuilder.append(Environment.getExternalStorageDirectory().getPath()).append("/Android/data/").append(context.getPackageName()).append("/cache/");
        } else {
            stringBuilder.append(externalCacheDir.getAbsolutePath()).append(File.separator);
        }
        return stringBuilder.toString();
    }
}
