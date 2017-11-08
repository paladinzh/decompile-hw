package com.huawei.openalliance.ad.a.g;

import android.content.Context;
import android.os.AsyncTask;
import com.huawei.openalliance.ad.a.a.a.c;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.c.e;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/* compiled from: Unknown */
public class b extends AsyncTask<Void, Void, c> {
    private Context a;
    private String b;
    private com.huawei.openalliance.ad.a.a.a.b c;
    private e d;

    public b(Context context, String str, com.huawei.openalliance.ad.a.a.a.b bVar, e eVar) {
        this.a = context;
        this.b = str;
        this.c = bVar;
        this.d = eVar;
    }

    protected c a(Void... voidArr) {
        HttpGet httpGet;
        Throwable e;
        Throwable th;
        try {
            Class rspClass = this.c.getRspClass();
            if (rspClass != null) {
                c cVar = (c) rspClass.newInstance();
                try {
                    httpGet = new HttpGet(this.b);
                    try {
                        HttpResponse a = e.a(this.a, httpGet);
                        d.b("HiAdGetRequest", "responsecode is: ", String.valueOf(a.getStatusLine() != null ? a.getStatusLine().getStatusCode() : 500));
                        cVar.responseCode = r1;
                        if (httpGet != null) {
                            try {
                                if (!httpGet.isAborted()) {
                                    httpGet.abort();
                                }
                            } catch (Throwable e2) {
                                d.a("HiAdGetRequest", "http get requester failed", e2);
                            }
                        }
                        return cVar;
                    } catch (IllegalArgumentException e3) {
                        e2 = e3;
                        try {
                            d.a("HiAdGetRequest", "request failed!", e2);
                            cVar.responseCode = 1;
                            if (httpGet != null) {
                                try {
                                    if (!httpGet.isAborted()) {
                                        httpGet.abort();
                                    }
                                } catch (Throwable e22) {
                                    d.a("HiAdGetRequest", "http get requester failed", e22);
                                }
                            }
                            return cVar;
                        } catch (Throwable th2) {
                            th = th2;
                            if (httpGet != null) {
                                try {
                                    if (!httpGet.isAborted()) {
                                        httpGet.abort();
                                    }
                                } catch (Throwable e222) {
                                    d.a("HiAdGetRequest", "http get requester failed", e222);
                                }
                            }
                            throw th;
                        }
                    } catch (IllegalStateException e4) {
                        e222 = e4;
                        d.a("HiAdGetRequest", "request failed!", e222);
                        cVar.responseCode = 1;
                        if (httpGet != null) {
                            try {
                                if (!httpGet.isAborted()) {
                                    httpGet.abort();
                                }
                            } catch (Throwable e2222) {
                                d.a("HiAdGetRequest", "http get requester failed", e2222);
                            }
                        }
                        return cVar;
                    } catch (IOException e5) {
                        e2222 = e5;
                        d.a("HiAdGetRequest", "request failed!", e2222);
                        cVar.responseCode = 1;
                        if (httpGet != null) {
                            try {
                                if (!httpGet.isAborted()) {
                                    httpGet.abort();
                                }
                            } catch (Throwable e22222) {
                                d.a("HiAdGetRequest", "http get requester failed", e22222);
                            }
                        }
                        return cVar;
                    } catch (Exception e6) {
                        e22222 = e6;
                        d.a("HiAdGetRequest", "request failed!", e22222);
                        cVar.responseCode = 1;
                        if (httpGet != null) {
                            try {
                                if (!httpGet.isAborted()) {
                                    httpGet.abort();
                                }
                            } catch (Throwable e222222) {
                                d.a("HiAdGetRequest", "http get requester failed", e222222);
                            }
                        }
                        return cVar;
                    }
                } catch (IllegalArgumentException e7) {
                    e222222 = e7;
                    httpGet = null;
                    d.a("HiAdGetRequest", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpGet != null) {
                        if (httpGet.isAborted()) {
                            httpGet.abort();
                        }
                    }
                    return cVar;
                } catch (IllegalStateException e8) {
                    e222222 = e8;
                    httpGet = null;
                    d.a("HiAdGetRequest", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpGet != null) {
                        if (httpGet.isAborted()) {
                            httpGet.abort();
                        }
                    }
                    return cVar;
                } catch (IOException e9) {
                    e222222 = e9;
                    httpGet = null;
                    d.a("HiAdGetRequest", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpGet != null) {
                        if (httpGet.isAborted()) {
                            httpGet.abort();
                        }
                    }
                    return cVar;
                } catch (Exception e10) {
                    e222222 = e10;
                    httpGet = null;
                    d.a("HiAdGetRequest", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpGet != null) {
                        if (httpGet.isAborted()) {
                            httpGet.abort();
                        }
                    }
                    return cVar;
                } catch (Throwable th3) {
                    th = th3;
                    httpGet = null;
                    if (httpGet != null) {
                        if (httpGet.isAborted()) {
                            httpGet.abort();
                        }
                    }
                    throw th;
                }
            }
            throw new InstantiationException("RspBean class not found!");
        } catch (InstantiationException e11) {
            d.c("HiAdGetRequest", "fail to create rsp object!");
            return null;
        } catch (IllegalAccessException e12) {
            d.c("HiAdGetRequest", "fail to create rsp object!");
            return null;
        }
    }

    protected void a(c cVar) {
        this.d.a(this.a, this.c, cVar);
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((Void[]) objArr);
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((c) obj);
    }
}
