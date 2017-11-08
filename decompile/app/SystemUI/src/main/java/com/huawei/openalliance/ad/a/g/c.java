package com.huawei.openalliance.ad.a.g;

import android.content.Context;
import android.os.AsyncTask;
import com.huawei.openalliance.ad.a.a.a.b;
import com.huawei.openalliance.ad.utils.b.d;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class c extends AsyncTask<Void, Void, com.huawei.openalliance.ad.a.a.a.c> {
    private Context a;
    private String b;
    private b c;
    private e d;

    public c(Context context, String str, b bVar, e eVar) {
        this.a = context;
        this.b = str;
        this.c = bVar;
        this.d = eVar;
    }

    private void a(e eVar) {
        if (eVar != null) {
            eVar.a();
        }
    }

    private void b(e eVar) {
        if (eVar != null) {
            eVar.b();
        }
    }

    protected com.huawei.openalliance.ad.a.a.a.c a(Void... voidArr) {
        HttpPost httpPost;
        Throwable e;
        Throwable th;
        a(this.d);
        try {
            Class rspClass = this.c.getRspClass();
            if (rspClass != null) {
                com.huawei.openalliance.ad.a.a.a.c cVar = (com.huawei.openalliance.ad.a.a.a.c) rspClass.newInstance();
                try {
                    httpPost = new HttpPost(this.b);
                    try {
                        d.b("HiAdRequester", "request is: ", f.a(this.c.toJson(), this.c));
                        httpPost.setEntity(new StringEntity(r1, "UTF-8"));
                        HttpResponse a = com.huawei.openalliance.ad.utils.c.d.a(this.a, httpPost);
                        int statusCode = a.getStatusLine() != null ? a.getStatusLine().getStatusCode() : 500;
                        d.b("HiAdRequester", "response is: ", f.a(EntityUtils.toString(a.getEntity(), "UTF-8"), cVar));
                        if (200 != statusCode) {
                            cVar.responseCode = 1;
                        } else {
                            cVar.fromJson(new JSONObject(r3));
                            cVar.responseCode = 0;
                        }
                        if (httpPost != null) {
                            try {
                                if (!httpPost.isAborted()) {
                                    httpPost.abort();
                                }
                            } catch (Throwable e2) {
                                d.a("HiAdRequester", "http request failed", e2);
                            }
                        }
                        b(this.d);
                        return cVar;
                    } catch (IOException e3) {
                        e2 = e3;
                        try {
                            d.a("HiAdRequester", "request failed!", e2);
                            cVar.responseCode = 1;
                            if (httpPost != null) {
                                try {
                                    if (!httpPost.isAborted()) {
                                        httpPost.abort();
                                    }
                                } catch (Throwable e22) {
                                    d.a("HiAdRequester", "http request failed", e22);
                                }
                            }
                            return cVar;
                        } catch (Throwable th2) {
                            th = th2;
                            if (httpPost != null) {
                                try {
                                    if (!httpPost.isAborted()) {
                                        httpPost.abort();
                                    }
                                } catch (Throwable e222) {
                                    d.a("HiAdRequester", "http request failed", e222);
                                }
                            }
                            throw th;
                        }
                    } catch (ClassNotFoundException e4) {
                        e222 = e4;
                        d.a("HiAdRequester", "request failed!", e222);
                        cVar.responseCode = 1;
                        if (httpPost != null) {
                            try {
                                if (!httpPost.isAborted()) {
                                    httpPost.abort();
                                }
                            } catch (Throwable e2222) {
                                d.a("HiAdRequester", "http request failed", e2222);
                            }
                        }
                        return cVar;
                    } catch (JSONException e5) {
                        e2222 = e5;
                        d.a("HiAdRequester", "request failed!", e2222);
                        cVar.responseCode = 1;
                        if (httpPost != null) {
                            try {
                                if (!httpPost.isAborted()) {
                                    httpPost.abort();
                                }
                            } catch (Throwable e22222) {
                                d.a("HiAdRequester", "http request failed", e22222);
                            }
                        }
                        return cVar;
                    } catch (Exception e6) {
                        e22222 = e6;
                        d.a("HiAdRequester", "request failed!", e22222);
                        cVar.responseCode = 1;
                        if (httpPost != null) {
                            try {
                                if (!httpPost.isAborted()) {
                                    httpPost.abort();
                                }
                            } catch (Throwable e222222) {
                                d.a("HiAdRequester", "http request failed", e222222);
                            }
                        }
                        return cVar;
                    }
                } catch (IOException e7) {
                    e222222 = e7;
                    httpPost = null;
                    d.a("HiAdRequester", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpPost != null) {
                        if (httpPost.isAborted()) {
                            httpPost.abort();
                        }
                    }
                    return cVar;
                } catch (ClassNotFoundException e8) {
                    e222222 = e8;
                    httpPost = null;
                    d.a("HiAdRequester", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpPost != null) {
                        if (httpPost.isAborted()) {
                            httpPost.abort();
                        }
                    }
                    return cVar;
                } catch (JSONException e9) {
                    e222222 = e9;
                    httpPost = null;
                    d.a("HiAdRequester", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpPost != null) {
                        if (httpPost.isAborted()) {
                            httpPost.abort();
                        }
                    }
                    return cVar;
                } catch (Exception e10) {
                    e222222 = e10;
                    httpPost = null;
                    d.a("HiAdRequester", "request failed!", e222222);
                    cVar.responseCode = 1;
                    if (httpPost != null) {
                        if (httpPost.isAborted()) {
                            httpPost.abort();
                        }
                    }
                    return cVar;
                } catch (Throwable th3) {
                    th = th3;
                    httpPost = null;
                    if (httpPost != null) {
                        if (httpPost.isAborted()) {
                            httpPost.abort();
                        }
                    }
                    throw th;
                }
            }
            throw new InstantiationException("RspBean class not found!");
        } catch (InstantiationException e11) {
            d.c("HiAdRequester", "fail to create rsp object!");
            return null;
        } catch (IllegalAccessException e12) {
            d.c("HiAdRequester", "fail to create rsp object!");
            return null;
        }
    }

    protected void a(com.huawei.openalliance.ad.a.a.a.c cVar) {
        if (this.d != null) {
            this.d.a(this.a, this.c, cVar);
        }
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((Void[]) objArr);
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((com.huawei.openalliance.ad.a.a.a.c) obj);
    }
}
