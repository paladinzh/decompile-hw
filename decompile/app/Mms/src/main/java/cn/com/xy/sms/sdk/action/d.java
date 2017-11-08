package cn.com.xy.sms.sdk.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/* compiled from: Unknown */
final class d implements Callable<String> {
    private final /* synthetic */ String a;

    d(String str) {
        this.a = str;
    }

    private String a() {
        BufferedReader bufferedReader;
        PrintWriter printWriter;
        Throwable th;
        BufferedReader bufferedReader2 = null;
        PrintWriter printWriter2;
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.a).openConnection();
            httpURLConnection.setRequestProperty("accept", "*/*");
            httpURLConnection.setRequestProperty("connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("user-agent", "alitester");
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            printWriter2 = new PrintWriter(httpURLConnection.getOutputStream());
            try {
                printWriter2.flush();
                BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    while (true) {
                        String readLine = bufferedReader3.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    }
                    String stringBuffer2 = stringBuffer.toString();
                    printWriter2.close();
                    try {
                        bufferedReader3.close();
                    } catch (IOException e) {
                    }
                    return stringBuffer2;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader2 = bufferedReader3;
                    if (printWriter2 != null) {
                        printWriter2.close();
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e2) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (printWriter2 != null) {
                    printWriter2.close();
                }
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            printWriter2 = null;
            if (printWriter2 != null) {
                printWriter2.close();
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            throw th;
        }
    }

    public final /* synthetic */ Object call() {
        return a();
    }
}
