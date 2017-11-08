package com.huawei.openalliance.ad.utils.c.a;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.c;
import com.huawei.openalliance.ad.utils.g;
import com.huawei.openalliance.ad.utils.i;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/* compiled from: Unknown */
public class b extends AsyncTask<Void, Void, String> {
    private HttpClient a = null;
    private String b;
    private final LinkedHashMap<String, String> c = new LinkedHashMap(0, 0.75f, true);
    private a d;
    private Context e;
    private a f;

    /* compiled from: Unknown */
    public interface a {
        void a(Context context, String str);
    }

    public b(Context context, a aVar, a aVar2) {
        this.b = i.a(context) + File.separator + "hiad" + File.separator;
        File file = new File(this.b);
        if (!(file.exists() || file.mkdir())) {
            this.b = i.a(context) + File.separator;
        }
        this.a = com.huawei.openalliance.ad.utils.c.a.a();
        this.d = aVar2;
        this.e = context;
        this.f = aVar;
    }

    private String a(Object obj) {
        if (obj == null || !(obj instanceof a)) {
            return null;
        }
        String d = ((a) obj).d();
        if (d == null) {
            return null;
        }
        File file = new File(this.b + b(obj.toString()));
        if (com.huawei.openalliance.ad.utils.b.b(file)) {
            return file.getAbsolutePath();
        }
        try {
            if (a(d, file.getAbsolutePath())) {
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            com.huawei.openalliance.ad.utils.b.a(file);
            d.c("ImageFetcher", "processBitmap failed");
        }
        return null;
    }

    private boolean a(String str, String str2) {
        RuntimeException e;
        Closeable closeable;
        Throwable th;
        HttpEntity httpEntity = null;
        File file = new File(str2 + ".bak");
        if (this.f == null) {
            return false;
        }
        if (c(file.getAbsolutePath()) == null) {
            d(file.getAbsolutePath());
            Closeable bufferedInputStream;
            Closeable bufferedOutputStream;
            try {
                HttpResponse execute = this.a.execute(new HttpGet(str));
                int statusCode = execute.getStatusLine().getStatusCode();
                if (200 == statusCode || 206 == statusCode) {
                    httpEntity = execute.getEntity();
                    if (httpEntity != null) {
                        long contentLength = httpEntity.getContentLength();
                        long c = ((long) this.f.c()) * 1024;
                        if ((contentLength < 0 ? 1 : null) == null) {
                            if ((contentLength <= c ? 1 : null) != null) {
                                bufferedInputStream = new BufferedInputStream(httpEntity.getContent(), 8192);
                                try {
                                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file), 8192);
                                    int i = 0;
                                    try {
                                        byte[] bArr = new byte[8192];
                                        while (true) {
                                            int read = bufferedInputStream.read(bArr);
                                            if (read <= 0) {
                                                break;
                                            }
                                            bufferedOutputStream.write(bArr, 0, read);
                                            i += read;
                                        }
                                        bufferedOutputStream.flush();
                                        if (contentLength != ((long) i)) {
                                            d.c("ImageFetcher", "downloadUrlToStream error, downloaded size " + i + ", not equal to actual size " + httpEntity.getContentLength() + ",url" + str);
                                            com.huawei.openalliance.ad.utils.b.a(file);
                                            try {
                                                e(file.getAbsolutePath());
                                                com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                                com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                                if (httpEntity != null) {
                                                    httpEntity.consumeContent();
                                                }
                                            } catch (IOException e2) {
                                                d.c("ImageFetcher", "IOException");
                                            }
                                            return false;
                                        } else if (!a(this.f.b(), this.f.a(), file)) {
                                            d.c("ImageFetcher", "downloadUrlToStream error, downloaded file hashcode is not right, url" + str);
                                            com.huawei.openalliance.ad.utils.b.a(file);
                                            try {
                                                e(file.getAbsolutePath());
                                                com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                                com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                                if (httpEntity != null) {
                                                    httpEntity.consumeContent();
                                                }
                                            } catch (IOException e3) {
                                                d.c("ImageFetcher", "IOException");
                                            }
                                            return false;
                                        } else if (file.renameTo(new File(str2))) {
                                            try {
                                                e(file.getAbsolutePath());
                                                com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                                com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                                if (httpEntity != null) {
                                                    httpEntity.consumeContent();
                                                }
                                            } catch (IOException e4) {
                                                d.c("ImageFetcher", "IOException");
                                            }
                                            return true;
                                        } else {
                                            try {
                                                e(file.getAbsolutePath());
                                                com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                                com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                                if (httpEntity != null) {
                                                    httpEntity.consumeContent();
                                                }
                                            } catch (IOException e5) {
                                                d.c("ImageFetcher", "IOException");
                                            }
                                            com.huawei.openalliance.ad.utils.b.a(file);
                                            return false;
                                        }
                                    } catch (RuntimeException e6) {
                                        e = e6;
                                        closeable = bufferedInputStream;
                                    } catch (Exception e7) {
                                    }
                                } catch (RuntimeException e8) {
                                    e = e8;
                                    bufferedOutputStream = null;
                                    closeable = bufferedInputStream;
                                    try {
                                        throw e;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        bufferedInputStream = closeable;
                                    }
                                } catch (Exception e9) {
                                    bufferedOutputStream = null;
                                    try {
                                        d.c("ImageFetcher", "Error in downloadBitmap,url:" + str);
                                        try {
                                            e(file.getAbsolutePath());
                                            com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                            com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                            if (httpEntity != null) {
                                                httpEntity.consumeContent();
                                            }
                                        } catch (IOException e10) {
                                            d.c("ImageFetcher", "IOException");
                                        }
                                        com.huawei.openalliance.ad.utils.b.a(file);
                                        return false;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        try {
                                            e(file.getAbsolutePath());
                                            com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                            com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                            if (httpEntity != null) {
                                                httpEntity.consumeContent();
                                            }
                                        } catch (IOException e11) {
                                            d.c("ImageFetcher", "IOException");
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    bufferedOutputStream = null;
                                    e(file.getAbsolutePath());
                                    com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                                    com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                                    if (httpEntity != null) {
                                        httpEntity.consumeContent();
                                    }
                                    throw th;
                                }
                            }
                        }
                        d.c("ImageFetcher", "fileSize is not in limit", String.valueOf(c));
                        try {
                            e(file.getAbsolutePath());
                            com.huawei.openalliance.ad.utils.b.a(null);
                            com.huawei.openalliance.ad.utils.b.a(null);
                            if (httpEntity != null) {
                                httpEntity.consumeContent();
                            }
                        } catch (IOException e12) {
                            d.c("ImageFetcher", "IOException");
                        }
                        return false;
                    }
                    d.c("ImageFetcher", "response entity is null");
                    try {
                        e(file.getAbsolutePath());
                        com.huawei.openalliance.ad.utils.b.a(null);
                        com.huawei.openalliance.ad.utils.b.a(null);
                        if (httpEntity != null) {
                            httpEntity.consumeContent();
                        }
                    } catch (IOException e13) {
                        d.c("ImageFetcher", "IOException");
                    }
                    return false;
                }
                try {
                    e(file.getAbsolutePath());
                    com.huawei.openalliance.ad.utils.b.a(null);
                    com.huawei.openalliance.ad.utils.b.a(null);
                } catch (IOException e14) {
                    d.c("ImageFetcher", "IOException");
                }
                return false;
            } catch (RuntimeException e15) {
                e = e15;
                closeable = null;
                bufferedOutputStream = null;
                throw e;
            } catch (Exception e16) {
                bufferedInputStream = null;
                bufferedOutputStream = null;
                d.c("ImageFetcher", "Error in downloadBitmap,url:" + str);
                e(file.getAbsolutePath());
                com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                if (httpEntity != null) {
                    httpEntity.consumeContent();
                }
                com.huawei.openalliance.ad.utils.b.a(file);
                return false;
            } catch (Throwable th5) {
                th = th5;
                bufferedInputStream = null;
                bufferedOutputStream = null;
                e(file.getAbsolutePath());
                com.huawei.openalliance.ad.utils.b.a(bufferedOutputStream);
                com.huawei.openalliance.ad.utils.b.a(bufferedInputStream);
                if (httpEntity != null) {
                    httpEntity.consumeContent();
                }
                throw th;
            }
        }
        d.b("ImageFetcher", "file is in progress");
        return false;
    }

    private boolean a(String str, String str2, File file) {
        if (TextUtils.isEmpty(str)) {
            if (!(TextUtils.isEmpty(str2) || str2.equalsIgnoreCase(c.a(file)))) {
                return false;
            }
        } else if (!str.equalsIgnoreCase(g.a(file))) {
            return false;
        }
        return true;
    }

    @SuppressLint({"NewApi"})
    private static String b(String str) {
        if (str == null) {
            return null;
        }
        String a;
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes(Charset.forName("UTF-8")));
            a = com.huawei.openalliance.ad.utils.b.c.a(instance.digest());
        } catch (NoSuchAlgorithmException e) {
            a = String.valueOf(str.hashCode());
        }
        return a;
    }

    private synchronized String c(String str) {
        return (String) this.c.get(str);
    }

    private synchronized void d(String str) {
        this.c.put(str, str);
    }

    private synchronized void e(String str) {
        this.c.remove(str);
    }

    protected String a(Void... voidArr) {
        return a(this.f);
    }

    protected void a(String str) {
        if (this.d != null && this.e != null) {
            this.d.a(this.e, str);
        }
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((Void[]) objArr);
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((String) obj);
    }
}
