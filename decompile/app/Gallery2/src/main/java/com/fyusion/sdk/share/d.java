package com.fyusion.sdk.share;

import android.os.AsyncTask;
import com.fyusion.sdk.share.exception.ConnectionException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
class d extends AsyncTask<Void, Void, Boolean> {
    private e a;
    private String b;
    private InputStream c;
    private int d = 0;
    private int e = 0;

    public d(InputStream inputStream, String str, int i, e eVar) {
        this.a = eVar;
        this.c = inputStream;
        this.b = str;
        this.e = i;
    }

    public d(InputStream inputStream, String str, e eVar) {
        this.a = eVar;
        this.c = inputStream;
        this.b = str;
    }

    private ConnectionException a(int i, Exception exception) {
        return a(i, exception.getMessage());
    }

    private ConnectionException a(int i, String str) {
        return new ConnectionException(d.class.getSimpleName() + i + " " + str);
    }

    protected Boolean a(Void... voidArr) {
        try {
            int available = this.c.available();
            if (this.c != null && available >= 1) {
                try {
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.b).openConnection();
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setFixedLengthStreamingMode((int) Math.min(2097152, (long) (available - this.e)));
                        httpURLConnection.setRequestProperty("Content-length", "" + ((int) Math.min(2097152, (long) (available - this.e))));
                        httpURLConnection.setRequestProperty("Content-Type", "application/stream");
                        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        try {
                            httpURLConnection.setRequestMethod("POST");
                            try {
                                OutputStream outputStream = httpURLConnection.getOutputStream();
                                if (outputStream != null) {
                                    try {
                                        FileInputStream fileInputStream = (FileInputStream) this.c;
                                        fileInputStream.skip((long) this.e);
                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                                        byte[] bArr = new byte[16384];
                                        Object obj = 1;
                                        while (true) {
                                            try {
                                                if ((((long) this.d) >= 2097152 ? 1 : null) == null && r1 != null) {
                                                    int read = bufferedInputStream.read(bArr);
                                                    if (read > 0) {
                                                        if ((this.e + this.d) + read > available) {
                                                            obj = null;
                                                            read = (available - this.d) - this.e;
                                                        }
                                                        outputStream.write(bArr, 0, read);
                                                        this.d += read;
                                                        if (this.a != null) {
                                                            this.a.a((long) read, (long) available);
                                                        }
                                                        if (isCancelled()) {
                                                        }
                                                    }
                                                }
                                                break;
                                            } catch (Exception e) {
                                                if (this.a != null) {
                                                    this.a.a(a(7, e));
                                                }
                                                return Boolean.valueOf(false);
                                            }
                                        }
                                        outputStream.flush();
                                        if (!isCancelled()) {
                                            try {
                                                if (httpURLConnection.getResponseCode() != SmsCheckResult.ESCT_200) {
                                                    if (this.a != null) {
                                                        this.a.a(a(10, httpURLConnection.getResponseCode() + ""));
                                                    }
                                                    return Boolean.valueOf(false);
                                                }
                                                InputStream inputStream = httpURLConnection.getInputStream();
                                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                                                StringBuilder stringBuilder = new StringBuilder();
                                                while (true) {
                                                    try {
                                                        String readLine = bufferedReader.readLine();
                                                        if (readLine == null) {
                                                            break;
                                                        }
                                                        stringBuilder.append(readLine);
                                                    } catch (Exception e2) {
                                                        if (this.a != null) {
                                                            this.a.a(a(9, e2));
                                                        }
                                                        inputStream.close();
                                                        return Boolean.valueOf(false);
                                                    }
                                                }
                                                if (this.a != null) {
                                                    this.a.a(stringBuilder.toString());
                                                }
                                                inputStream.close();
                                                return Boolean.valueOf(true);
                                            } catch (Exception e22) {
                                                if (this.a != null) {
                                                    this.a.a(a(11, e22));
                                                }
                                                return Boolean.valueOf(false);
                                            }
                                        }
                                    } catch (Exception e222) {
                                        if (this.a != null) {
                                            this.a.a(a(6, e222));
                                        }
                                        return Boolean.valueOf(false);
                                    }
                                }
                                return Boolean.valueOf(!isCancelled());
                            } catch (Exception e2222) {
                                if (this.a != null) {
                                    this.a.a(a(5, e2222));
                                }
                                return Boolean.valueOf(false);
                            }
                        } catch (Exception e22222) {
                            if (this.a != null) {
                                this.a.a(a(4, e22222));
                            }
                            return Boolean.valueOf(false);
                        }
                    } catch (Exception e222222) {
                        if (this.a != null) {
                            this.a.a(a(3, e222222));
                        }
                        return Boolean.valueOf(false);
                    }
                } catch (Exception e2222222) {
                    if (this.a != null) {
                        this.a.a(a(2, e2222222));
                    }
                    return Boolean.valueOf(false);
                }
            }
            if (this.a != null) {
                this.a.a(a(1, "File not found"));
            }
            return Boolean.valueOf(false);
        } catch (Exception e22222222) {
            if (this.a != null) {
                this.a.a(a(12, e22222222));
            }
            return Boolean.valueOf(false);
        }
    }

    protected void a(Boolean bool) {
        if (this.a != null) {
            this.a.a(bool.booleanValue(), (long) (this.e + this.d));
        }
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((Void[]) objArr);
    }

    protected void onCancelled() {
        if (this.a != null) {
            this.a.a();
        }
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((Boolean) obj);
    }
}
