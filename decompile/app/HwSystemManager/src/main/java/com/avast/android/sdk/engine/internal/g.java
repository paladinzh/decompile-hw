package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Build.VERSION;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ProgressObserver;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.ServerInformation;
import com.avast.android.sdk.engine.SubmitInformation;
import com.avast.android.sdk.engine.SubmitResult;
import com.avast.android.sdk.engine.VpsInformation;
import com.avast.android.sdk.engine.obfuscated.ak;
import com.avast.android.sdk.engine.obfuscated.an;
import com.avast.android.sdk.engine.obfuscated.an.a;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.aq;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.VirusTableConst;
import com.huawei.systemmanager.spacecleanner.utils.HwMediaFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/* compiled from: Unknown */
public class g {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressLint({"NewApi"})
    public static SubmitResult a(Context context, Integer num, File file, PackageInfo packageInfo, ScanResultStructure scanResultStructure, SubmitInformation submitInformation, ProgressObserver progressObserver) {
        String a;
        File a2;
        ServerInformation b;
        AndroidHttpClient androidHttpClient;
        HttpResponse httpResponse;
        AndroidHttpClient newInstance;
        List arrayList;
        HttpUriRequest httpPost;
        HttpResponse a3;
        SubmitResult submitResult;
        File dir;
        File[] listFiles;
        Object e;
        Throwable th;
        File dir2;
        File[] listFiles2;
        SubmitResult submitResult2;
        ao.a("beginning to send the false positive");
        VpsInformation vpsInformation = EngineInterface.getVpsInformation(context, null);
        String str = "";
        if (vpsInformation != null) {
            str = String.valueOf(vpsInformation.version).replace("-", "").replace("-", "");
        }
        String str2 = str;
        ApplicationInfo applicationInfo = null;
        if (packageInfo != null) {
            applicationInfo = packageInfo.applicationInfo;
        }
        try {
            a = an.a(a.SHA256, file, 64);
        } catch (NoSuchAlgorithmException e2) {
            a = null;
            ao.a("checksum = " + a);
            if (a != null) {
                return SubmitResult.RESULT_ERROR_FILE_NOT_ACCESSIBLE;
            }
            a2 = a(file, a, context, applicationInfo, scanResultStructure, submitInformation);
            if (a2 != null) {
                return SubmitResult.RESULT_ERROR_INSUFFICIENT_SPACE;
            }
            ao.a("file created at " + a2.getAbsolutePath());
            b = k.b(context, num);
            if (b != null) {
                return SubmitResult.RESULT_DONE;
            }
            androidHttpClient = null;
            httpResponse = null;
            try {
                ao.a("starting to call the server");
                newInstance = AndroidHttpClient.newInstance("avdroid");
                try {
                    newInstance.disableCurlLogging();
                    arrayList = new ArrayList();
                    arrayList.add(new BasicNameValuePair("id", a));
                    arrayList.add(new BasicNameValuePair("type", "fp"));
                    arrayList.add(new BasicNameValuePair("vps", str2));
                    arrayList.add(new BasicNameValuePair(VirusTableConst.VIRUS_TABLE, scanResultStructure.infectionType));
                    httpPost = new HttpPost(URIUtils.createURI(b.serverProtocol, b.serverAddress, b.serverPort.intValue(), b.serverPath, URLEncodedUtils.format(arrayList, "UTF-8"), null).toString().replace("&", SqlMarker.SQL_END) + SqlMarker.SQL_END);
                    httpPost.addHeader("Content-type", "iavs4/upload");
                    ao.a("going to execute request " + httpPost.getURI());
                    httpResponse = newInstance.execute(httpPost);
                    ao.a("First response: " + httpResponse.getStatusLine().getStatusCode());
                    switch (httpResponse.getStatusLine().getStatusCode()) {
                        case HwMediaFile.FILE_TYPE_BMP /*204*/:
                            if (httpResponse.getEntity() != null) {
                                httpResponse.getEntity().consumeContent();
                            }
                            arrayList.add(new BasicNameValuePair("len", Long.toHexString(a2.length())));
                            a3 = a(newInstance, URIUtils.createURI(b.serverProtocol, b.serverAddress, b.serverPort.intValue(), b.serverPath, URLEncodedUtils.format(arrayList, "UTF-8"), null), a2, progressObserver);
                            if (a3 != null) {
                                submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                try {
                                    a3.getEntity().consumeContent();
                                } catch (IOException e3) {
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                if (a2 != null) {
                                    dir = context.getDir("submit", 0);
                                    if (dir != null) {
                                        listFiles = dir.listFiles();
                                        if (listFiles != null) {
                                            for (File delete : listFiles) {
                                                delete.delete();
                                            }
                                        }
                                    }
                                }
                                return submitResult;
                            }
                            try {
                                ao.a("Second response: " + a3.getStatusLine().getStatusCode());
                                switch (a3.getStatusLine().getStatusCode()) {
                                    case HwMediaFile.FILE_TYPE_BMP /*204*/:
                                        a2.delete();
                                        submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                        try {
                                            a3.getEntity().consumeContent();
                                        } catch (IOException e4) {
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        if (a2 != null) {
                                            dir = context.getDir("submit", 0);
                                            if (dir != null) {
                                                listFiles = dir.listFiles();
                                                if (listFiles != null) {
                                                    for (File delete2 : listFiles) {
                                                        delete2.delete();
                                                    }
                                                }
                                            }
                                        }
                                        return submitResult;
                                    case 206:
                                    case 404:
                                        a2.delete();
                                        submitResult = SubmitResult.RESULT_DONE;
                                        try {
                                            a3.getEntity().consumeContent();
                                        } catch (IOException e5) {
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        if (a2 != null) {
                                            dir = context.getDir("submit", 0);
                                            if (dir != null) {
                                                listFiles = dir.listFiles();
                                                if (listFiles != null) {
                                                    for (File delete22 : listFiles) {
                                                        delete22.delete();
                                                    }
                                                }
                                            }
                                        }
                                        return submitResult;
                                    case 406:
                                        a2.delete();
                                        submitResult = SubmitResult.RESULT_DONE;
                                        try {
                                            a3.getEntity().consumeContent();
                                        } catch (IOException e6) {
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        if (a2 != null) {
                                            dir = context.getDir("submit", 0);
                                            if (dir != null) {
                                                listFiles = dir.listFiles();
                                                if (listFiles != null) {
                                                    for (File delete222 : listFiles) {
                                                        delete222.delete();
                                                    }
                                                }
                                            }
                                        }
                                        return submitResult;
                                    default:
                                        a2.delete();
                                        submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                        try {
                                            a3.getEntity().consumeContent();
                                        } catch (IOException e7) {
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        if (a2 != null) {
                                            dir = context.getDir("submit", 0);
                                            if (dir != null) {
                                                listFiles = dir.listFiles();
                                                if (listFiles != null) {
                                                    for (File delete2222 : listFiles) {
                                                        delete2222.delete();
                                                    }
                                                }
                                            }
                                        }
                                        return submitResult;
                                }
                            } catch (URISyntaxException e8) {
                                e = e8;
                                androidHttpClient = newInstance;
                                try {
                                    ao.a("URISyntaxException: " + e);
                                    SubmitResult submitResult3 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                    try {
                                        a3.getEntity().consumeContent();
                                    } catch (IOException e9) {
                                    }
                                    if (androidHttpClient != null) {
                                        androidHttpClient.close();
                                    }
                                    if (a2 != null) {
                                        dir = context.getDir("submit", 0);
                                        if (dir != null) {
                                            listFiles = dir.listFiles();
                                            if (listFiles != null) {
                                                for (File delete3 : listFiles) {
                                                    delete3.delete();
                                                }
                                            }
                                        }
                                    }
                                    return submitResult3;
                                } catch (Throwable th2) {
                                    th = th2;
                                    httpResponse = a3;
                                    newInstance = androidHttpClient;
                                    if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                        try {
                                            httpResponse.getEntity().consumeContent();
                                        } catch (IOException e10) {
                                        }
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    if (a2 != null) {
                                        dir2 = context.getDir("submit", 0);
                                        if (dir2 != null) {
                                            listFiles2 = dir2.listFiles();
                                            if (listFiles2 != null) {
                                                for (File delete22222 : listFiles2) {
                                                    delete22222.delete();
                                                }
                                            }
                                        }
                                    }
                                    throw th;
                                }
                            } catch (IOException e11) {
                                e = e11;
                                httpResponse = a3;
                                try {
                                    ao.a("IOException: " + e);
                                    submitResult2 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                    try {
                                        httpResponse.getEntity().consumeContent();
                                    } catch (IOException e12) {
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    if (a2 != null) {
                                        dir = context.getDir("submit", 0);
                                        if (dir != null) {
                                            listFiles2 = dir.listFiles();
                                            if (listFiles2 != null) {
                                                for (File delete222222 : listFiles2) {
                                                    delete222222.delete();
                                                }
                                            }
                                        }
                                    }
                                    return submitResult2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    httpResponse.getEntity().consumeContent();
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    if (a2 != null) {
                                        dir2 = context.getDir("submit", 0);
                                        if (dir2 != null) {
                                            listFiles2 = dir2.listFiles();
                                            if (listFiles2 != null) {
                                                while (r3 < listFiles2.length) {
                                                    delete222222.delete();
                                                }
                                            }
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                httpResponse = a3;
                                httpResponse.getEntity().consumeContent();
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                if (a2 != null) {
                                    dir2 = context.getDir("submit", 0);
                                    if (dir2 != null) {
                                        listFiles2 = dir2.listFiles();
                                        if (listFiles2 != null) {
                                            while (r3 < listFiles2.length) {
                                                delete222222.delete();
                                            }
                                        }
                                    }
                                }
                                throw th;
                            }
                        case 206:
                        case 404:
                            a2.delete();
                            submitResult2 = SubmitResult.RESULT_DONE;
                            try {
                                httpResponse.getEntity().consumeContent();
                            } catch (IOException e13) {
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles2 = dir.listFiles();
                                    if (listFiles2 != null) {
                                        for (File delete2222222 : listFiles2) {
                                            delete2222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult2;
                        case 406:
                            a2.delete();
                            submitResult2 = SubmitResult.RESULT_DONE;
                            try {
                                httpResponse.getEntity().consumeContent();
                            } catch (IOException e14) {
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles2 = dir.listFiles();
                                    if (listFiles2 != null) {
                                        for (File delete22222222 : listFiles2) {
                                            delete22222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult2;
                        default:
                            a2.delete();
                            submitResult2 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                            try {
                                httpResponse.getEntity().consumeContent();
                            } catch (IOException e15) {
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles2 = dir.listFiles();
                                    if (listFiles2 != null) {
                                        for (File delete222222222 : listFiles2) {
                                            delete222222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult2;
                    }
                } catch (URISyntaxException e16) {
                    e = e16;
                    a3 = httpResponse;
                    androidHttpClient = newInstance;
                    ao.a("URISyntaxException: " + e);
                    SubmitResult submitResult32 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                    a3.getEntity().consumeContent();
                    if (androidHttpClient != null) {
                        androidHttpClient.close();
                    }
                    if (a2 != null) {
                        dir = context.getDir("submit", 0);
                        if (dir != null) {
                            listFiles = dir.listFiles();
                            if (listFiles != null) {
                                while (r2 < listFiles.length) {
                                    delete3.delete();
                                }
                            }
                        }
                    }
                    return submitResult32;
                } catch (IOException e17) {
                    e = e17;
                    ao.a("IOException: " + e);
                    submitResult2 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                    httpResponse.getEntity().consumeContent();
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    if (a2 != null) {
                        dir = context.getDir("submit", 0);
                        if (dir != null) {
                            listFiles2 = dir.listFiles();
                            if (listFiles2 != null) {
                                while (r2 < listFiles2.length) {
                                    delete222222222.delete();
                                }
                            }
                        }
                    }
                    return submitResult2;
                }
            } catch (URISyntaxException e18) {
                e = e18;
                a3 = httpResponse;
                ao.a("URISyntaxException: " + e);
                SubmitResult submitResult322 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                if (!(a3 == null || a3.getEntity() == null)) {
                    a3.getEntity().consumeContent();
                }
                if (androidHttpClient != null) {
                    androidHttpClient.close();
                }
                if (a2 != null) {
                    dir = context.getDir("submit", 0);
                    if (dir != null) {
                        listFiles = dir.listFiles();
                        if (listFiles != null) {
                            while (r2 < listFiles.length) {
                                delete3.delete();
                            }
                        }
                    }
                }
                return submitResult322;
            } catch (IOException e19) {
                e = e19;
                newInstance = null;
                ao.a("IOException: " + e);
                submitResult2 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                    httpResponse.getEntity().consumeContent();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                if (a2 != null) {
                    dir = context.getDir("submit", 0);
                    if (dir != null) {
                        listFiles2 = dir.listFiles();
                        if (listFiles2 != null) {
                            while (r2 < listFiles2.length) {
                                delete222222222.delete();
                            }
                        }
                    }
                }
                return submitResult2;
            } catch (Throwable th5) {
                th = th5;
                newInstance = null;
                httpResponse.getEntity().consumeContent();
                if (newInstance != null) {
                    newInstance.close();
                }
                if (a2 != null) {
                    dir2 = context.getDir("submit", 0);
                    if (dir2 != null) {
                        listFiles2 = dir2.listFiles();
                        if (listFiles2 != null) {
                            while (r3 < listFiles2.length) {
                                delete222222222.delete();
                            }
                        }
                    }
                }
                throw th;
            }
        } catch (Exception e20) {
            a = null;
            ao.a("checksum = " + a);
            if (a != null) {
                return SubmitResult.RESULT_ERROR_FILE_NOT_ACCESSIBLE;
            }
            a2 = a(file, a, context, applicationInfo, scanResultStructure, submitInformation);
            if (a2 != null) {
                return SubmitResult.RESULT_ERROR_INSUFFICIENT_SPACE;
            }
            ao.a("file created at " + a2.getAbsolutePath());
            b = k.b(context, num);
            if (b != null) {
                return SubmitResult.RESULT_DONE;
            }
            androidHttpClient = null;
            httpResponse = null;
            ao.a("starting to call the server");
            newInstance = AndroidHttpClient.newInstance("avdroid");
            newInstance.disableCurlLogging();
            arrayList = new ArrayList();
            arrayList.add(new BasicNameValuePair("id", a));
            arrayList.add(new BasicNameValuePair("type", "fp"));
            arrayList.add(new BasicNameValuePair("vps", str2));
            arrayList.add(new BasicNameValuePair(VirusTableConst.VIRUS_TABLE, scanResultStructure.infectionType));
            httpPost = new HttpPost(URIUtils.createURI(b.serverProtocol, b.serverAddress, b.serverPort.intValue(), b.serverPath, URLEncodedUtils.format(arrayList, "UTF-8"), null).toString().replace("&", SqlMarker.SQL_END) + SqlMarker.SQL_END);
            httpPost.addHeader("Content-type", "iavs4/upload");
            ao.a("going to execute request " + httpPost.getURI());
            httpResponse = newInstance.execute(httpPost);
            ao.a("First response: " + httpResponse.getStatusLine().getStatusCode());
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case HwMediaFile.FILE_TYPE_BMP /*204*/:
                    if (httpResponse.getEntity() != null) {
                        httpResponse.getEntity().consumeContent();
                    }
                    arrayList.add(new BasicNameValuePair("len", Long.toHexString(a2.length())));
                    a3 = a(newInstance, URIUtils.createURI(b.serverProtocol, b.serverAddress, b.serverPort.intValue(), b.serverPath, URLEncodedUtils.format(arrayList, "UTF-8"), null), a2, progressObserver);
                    if (a3 != null) {
                        ao.a("Second response: " + a3.getStatusLine().getStatusCode());
                        switch (a3.getStatusLine().getStatusCode()) {
                            case HwMediaFile.FILE_TYPE_BMP /*204*/:
                                a2.delete();
                                submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                a3.getEntity().consumeContent();
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                if (a2 != null) {
                                    dir = context.getDir("submit", 0);
                                    if (dir != null) {
                                        listFiles = dir.listFiles();
                                        if (listFiles != null) {
                                            while (r2 < listFiles.length) {
                                                delete222222222.delete();
                                            }
                                        }
                                    }
                                }
                                return submitResult;
                            case 206:
                            case 404:
                                a2.delete();
                                submitResult = SubmitResult.RESULT_DONE;
                                a3.getEntity().consumeContent();
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                if (a2 != null) {
                                    dir = context.getDir("submit", 0);
                                    if (dir != null) {
                                        listFiles = dir.listFiles();
                                        if (listFiles != null) {
                                            while (r2 < listFiles.length) {
                                                delete222222222.delete();
                                            }
                                        }
                                    }
                                }
                                return submitResult;
                            case 406:
                                a2.delete();
                                submitResult = SubmitResult.RESULT_DONE;
                                a3.getEntity().consumeContent();
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                if (a2 != null) {
                                    dir = context.getDir("submit", 0);
                                    if (dir != null) {
                                        listFiles = dir.listFiles();
                                        if (listFiles != null) {
                                            while (r2 < listFiles.length) {
                                                delete222222222.delete();
                                            }
                                        }
                                    }
                                }
                                return submitResult;
                            default:
                                a2.delete();
                                submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                                a3.getEntity().consumeContent();
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                if (a2 != null) {
                                    dir = context.getDir("submit", 0);
                                    if (dir != null) {
                                        listFiles = dir.listFiles();
                                        if (listFiles != null) {
                                            while (r2 < listFiles.length) {
                                                delete222222222.delete();
                                            }
                                        }
                                    }
                                }
                                return submitResult;
                        }
                    }
                    submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                    a3.getEntity().consumeContent();
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    if (a2 != null) {
                        dir = context.getDir("submit", 0);
                        if (dir != null) {
                            listFiles = dir.listFiles();
                            if (listFiles != null) {
                                while (r2 < listFiles.length) {
                                    delete222222222.delete();
                                }
                            }
                        }
                    }
                    return submitResult;
                case 206:
                case 404:
                    a2.delete();
                    submitResult2 = SubmitResult.RESULT_DONE;
                    httpResponse.getEntity().consumeContent();
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    if (a2 != null) {
                        dir = context.getDir("submit", 0);
                        if (dir != null) {
                            listFiles2 = dir.listFiles();
                            if (listFiles2 != null) {
                                while (r2 < listFiles2.length) {
                                    delete222222222.delete();
                                }
                            }
                        }
                    }
                    return submitResult2;
                case 406:
                    a2.delete();
                    submitResult2 = SubmitResult.RESULT_DONE;
                    httpResponse.getEntity().consumeContent();
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    if (a2 != null) {
                        dir = context.getDir("submit", 0);
                        if (dir != null) {
                            listFiles2 = dir.listFiles();
                            if (listFiles2 != null) {
                                while (r2 < listFiles2.length) {
                                    delete222222222.delete();
                                }
                            }
                        }
                    }
                    return submitResult2;
                default:
                    a2.delete();
                    submitResult2 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                    httpResponse.getEntity().consumeContent();
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    if (a2 != null) {
                        dir = context.getDir("submit", 0);
                        if (dir != null) {
                            listFiles2 = dir.listFiles();
                            if (listFiles2 != null) {
                                while (r2 < listFiles2.length) {
                                    delete222222222.delete();
                                }
                            }
                        }
                    }
                    return submitResult2;
            }
        }
        ao.a("checksum = " + a);
        if (a != null) {
            return SubmitResult.RESULT_ERROR_FILE_NOT_ACCESSIBLE;
        }
        a2 = a(file, a, context, applicationInfo, scanResultStructure, submitInformation);
        if (a2 != null) {
            return SubmitResult.RESULT_ERROR_INSUFFICIENT_SPACE;
        }
        ao.a("file created at " + a2.getAbsolutePath());
        b = k.b(context, num);
        if (b != null) {
            return SubmitResult.RESULT_DONE;
        }
        androidHttpClient = null;
        httpResponse = null;
        ao.a("starting to call the server");
        newInstance = AndroidHttpClient.newInstance("avdroid");
        newInstance.disableCurlLogging();
        arrayList = new ArrayList();
        arrayList.add(new BasicNameValuePair("id", a));
        arrayList.add(new BasicNameValuePair("type", "fp"));
        arrayList.add(new BasicNameValuePair("vps", str2));
        arrayList.add(new BasicNameValuePair(VirusTableConst.VIRUS_TABLE, scanResultStructure.infectionType));
        httpPost = new HttpPost(URIUtils.createURI(b.serverProtocol, b.serverAddress, b.serverPort.intValue(), b.serverPath, URLEncodedUtils.format(arrayList, "UTF-8"), null).toString().replace("&", SqlMarker.SQL_END) + SqlMarker.SQL_END);
        httpPost.addHeader("Content-type", "iavs4/upload");
        ao.a("going to execute request " + httpPost.getURI());
        httpResponse = newInstance.execute(httpPost);
        ao.a("First response: " + httpResponse.getStatusLine().getStatusCode());
        switch (httpResponse.getStatusLine().getStatusCode()) {
            case HwMediaFile.FILE_TYPE_BMP /*204*/:
                if (httpResponse.getEntity() != null) {
                    httpResponse.getEntity().consumeContent();
                }
                arrayList.add(new BasicNameValuePair("len", Long.toHexString(a2.length())));
                a3 = a(newInstance, URIUtils.createURI(b.serverProtocol, b.serverAddress, b.serverPort.intValue(), b.serverPath, URLEncodedUtils.format(arrayList, "UTF-8"), null), a2, progressObserver);
                if (a3 != null) {
                    ao.a("Second response: " + a3.getStatusLine().getStatusCode());
                    switch (a3.getStatusLine().getStatusCode()) {
                        case HwMediaFile.FILE_TYPE_BMP /*204*/:
                            a2.delete();
                            submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                            if (!(a3 == null || a3.getEntity() == null)) {
                                a3.getEntity().consumeContent();
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles = dir.listFiles();
                                    if (listFiles != null) {
                                        while (r2 < listFiles.length) {
                                            delete222222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult;
                        case 206:
                        case 404:
                            a2.delete();
                            submitResult = SubmitResult.RESULT_DONE;
                            if (!(a3 == null || a3.getEntity() == null)) {
                                a3.getEntity().consumeContent();
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles = dir.listFiles();
                                    if (listFiles != null) {
                                        while (r2 < listFiles.length) {
                                            delete222222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult;
                        case 406:
                            a2.delete();
                            submitResult = SubmitResult.RESULT_DONE;
                            if (!(a3 == null || a3.getEntity() == null)) {
                                a3.getEntity().consumeContent();
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles = dir.listFiles();
                                    if (listFiles != null) {
                                        while (r2 < listFiles.length) {
                                            delete222222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult;
                        default:
                            a2.delete();
                            submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                            if (!(a3 == null || a3.getEntity() == null)) {
                                a3.getEntity().consumeContent();
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            if (a2 != null) {
                                dir = context.getDir("submit", 0);
                                if (dir != null) {
                                    listFiles = dir.listFiles();
                                    if (listFiles != null) {
                                        while (r2 < listFiles.length) {
                                            delete222222222.delete();
                                        }
                                    }
                                }
                            }
                            return submitResult;
                    }
                }
                submitResult = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                if (!(a3 == null || a3.getEntity() == null)) {
                    a3.getEntity().consumeContent();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                if (a2 != null) {
                    dir = context.getDir("submit", 0);
                    if (dir != null) {
                        listFiles = dir.listFiles();
                        if (listFiles != null) {
                            while (r2 < listFiles.length) {
                                delete222222222.delete();
                            }
                        }
                    }
                }
                return submitResult;
            case 206:
            case 404:
                a2.delete();
                submitResult2 = SubmitResult.RESULT_DONE;
                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                    httpResponse.getEntity().consumeContent();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                if (a2 != null) {
                    dir = context.getDir("submit", 0);
                    if (dir != null) {
                        listFiles2 = dir.listFiles();
                        if (listFiles2 != null) {
                            while (r2 < listFiles2.length) {
                                delete222222222.delete();
                            }
                        }
                    }
                }
                return submitResult2;
            case 406:
                a2.delete();
                submitResult2 = SubmitResult.RESULT_DONE;
                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                    httpResponse.getEntity().consumeContent();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                if (a2 != null) {
                    dir = context.getDir("submit", 0);
                    if (dir != null) {
                        listFiles2 = dir.listFiles();
                        if (listFiles2 != null) {
                            while (r2 < listFiles2.length) {
                                delete222222222.delete();
                            }
                        }
                    }
                }
                return submitResult2;
            default:
                a2.delete();
                submitResult2 = SubmitResult.RESULT_ERROR_INTERNET_CONNECTION;
                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                    httpResponse.getEntity().consumeContent();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                if (a2 != null) {
                    dir = context.getDir("submit", 0);
                    if (dir != null) {
                        listFiles2 = dir.listFiles();
                        if (listFiles2 != null) {
                            while (r2 < listFiles2.length) {
                                delete222222222.delete();
                            }
                        }
                    }
                }
                return submitResult2;
        }
    }

    private static File a(File file, String str, Context context, ApplicationInfo applicationInfo, ScanResultStructure scanResultStructure, SubmitInformation submitInformation) {
        FileOutputStream fileOutputStream;
        ak akVar;
        Throwable th;
        Throwable th2;
        String toUpperCase = UUID.randomUUID().toString().toUpperCase();
        File file2 = new File(context.getDir("submit", 0) + "/" + toUpperCase);
        if (file2.exists()) {
            file2.delete();
        }
        try {
            if (!file2.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
        }
        FileOutputStream fileOutputStream2 = null;
        ak akVar2 = null;
        FileInputStream fileInputStream = null;
        FileInputStream fileInputStream2;
        try {
            fileOutputStream = new FileOutputStream(file2);
            try {
                akVar = new ak();
                try {
                    akVar.a("A1A57000");
                    akVar.a(ConstValues.MARK_NAME, file.getAbsolutePath());
                    akVar.a("SIZE", file.length());
                    akVar.b("HTYP", "SHA256");
                    akVar.c("HASH", str);
                    akVar.a("TYPE", "Submit [FP]");
                    akVar.a("STYP", 2);
                    VpsInformation vpsInformation = EngineInterface.getVpsInformation(context, null);
                    if (vpsInformation != null) {
                        String str2 = "";
                        String replace = vpsInformation.version.replace("-", "");
                        for (int length = replace.length() - 1; length > -1; length -= 2) {
                            str2 = (str2 + replace.charAt(length - 1)) + replace.charAt(length);
                        }
                        akVar.c("VPS ", str2);
                    }
                    try {
                        String[] split = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName.split("\\.");
                        akVar.b("PROG", ((Long.parseLong(split[0]) * 1000000) + (Long.parseLong(split[1]) * DBHelper.HISTORY_MAX_SIZE)) + (Long.parseLong(split[2]) % DBHelper.HISTORY_MAX_SIZE));
                    } catch (Exception e2) {
                    }
                    akVar.a("VIRU", scanResultStructure.infectionType);
                    akVar.c(HsmStatConst.PARAM_FLAG, "40800000");
                    akVar.a("DATE", Calendar.getInstance().getTimeInMillis() / 1000);
                    akVar.a("O/SA", VERSION.RELEASE);
                    akVar.a("DEVI", Build.MANUFACTURER + " " + Build.MODEL + "(" + Build.BRAND + ")");
                    if (applicationInfo != null) {
                        akVar.a("PRNA", applicationInfo.packageName);
                    }
                    if (submitInformation != null) {
                        if (submitInformation.description != null) {
                            akVar.a("DESC", submitInformation.description);
                        }
                        if (submitInformation.email != null) {
                            akVar.a("EMAI", submitInformation.email);
                        }
                    }
                    akVar.b("UNID", "{" + toUpperCase + "}");
                    akVar.b("GUID", EngineInterface.getEngineConfig().getGuid());
                    fileOutputStream.write(akVar.a());
                    fileOutputStream.flush();
                    fileInputStream2 = new FileInputStream(file);
                    try {
                        akVar.c("DATA", file.length());
                        ao.a("inputFile.length() = " + file.length());
                        byte[] bArr = new byte[65536];
                        ByteBuffer allocate = ByteBuffer.allocate(65536);
                        while (true) {
                            int read = fileInputStream2.read(bArr);
                            if (read == -1) {
                                break;
                            } else if (allocate.position() + read == 65536) {
                                allocate.put(bArr);
                                akVar.a(akVar.a(allocate.array(), 65536, 0));
                                ao.a("1) inserted " + bArr.length + " bytes");
                                fileOutputStream.write(akVar.a());
                                fileOutputStream.flush();
                                allocate.clear();
                            } else if (allocate.position() + read >= 65536) {
                                allocate.put(bArr, 0, 65536 - allocate.position());
                                akVar.a(akVar.a(allocate.array(), 65536, 0));
                                fileOutputStream.write(akVar.a());
                                fileOutputStream.flush();
                                ao.a("2) inserted " + bArr.length + " bytes");
                                allocate.clear();
                                allocate.put(bArr, 65536 - allocate.position(), read - (65536 - allocate.position()));
                            } else {
                                allocate.put(bArr, 0, read);
                            }
                        }
                        akVar.a(akVar.a(allocate.array(), allocate.position(), 0));
                        fileOutputStream.write(akVar.a());
                        fileOutputStream.flush();
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.flush();
                                fileOutputStream.close();
                            } catch (IOException e3) {
                                return null;
                            }
                        }
                        if (akVar != null) {
                            try {
                                akVar.b();
                            } catch (IOException e4) {
                                return null;
                            }
                        }
                        if (fileInputStream2 != null) {
                            try {
                                fileInputStream2.close();
                            } catch (IOException e5) {
                                return null;
                            }
                        }
                        ao.a("3) outputFile.length = " + file2.length());
                        ao.a("4) " + file2.getAbsolutePath());
                        return file2;
                    } catch (FileNotFoundException e6) {
                        fileInputStream = fileInputStream2;
                        akVar2 = akVar;
                        fileOutputStream2 = fileOutputStream;
                    } catch (IOException e7) {
                        fileInputStream = fileInputStream2;
                    } catch (OutOfMemoryError e8) {
                        fileInputStream = fileInputStream2;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } catch (FileNotFoundException e9) {
                    akVar2 = akVar;
                    fileOutputStream2 = fileOutputStream;
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.flush();
                            fileOutputStream2.close();
                        } catch (IOException e10) {
                            return null;
                        }
                    }
                    if (akVar2 != null) {
                        try {
                            akVar2.b();
                        } catch (IOException e11) {
                            return null;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e12) {
                            return null;
                        }
                    }
                    return null;
                } catch (IOException e13) {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e14) {
                            return null;
                        }
                    }
                    if (akVar != null) {
                        try {
                            akVar.b();
                        } catch (IOException e15) {
                            return null;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e16) {
                            return null;
                        }
                    }
                    return null;
                } catch (OutOfMemoryError e17) {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e18) {
                            return null;
                        }
                    }
                    if (akVar != null) {
                        try {
                            akVar.b();
                        } catch (IOException e19) {
                            return null;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e20) {
                            return null;
                        }
                    }
                    return null;
                } catch (Throwable th4) {
                    th2 = th4;
                    fileInputStream2 = null;
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e21) {
                            return null;
                        }
                    }
                    if (akVar != null) {
                        try {
                            akVar.b();
                        } catch (IOException e22) {
                            return null;
                        }
                    }
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e23) {
                            return null;
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e24) {
                fileOutputStream2 = fileOutputStream;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.flush();
                    fileOutputStream2.close();
                }
                if (akVar2 != null) {
                    akVar2.b();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (IOException e25) {
                akVar = null;
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (akVar != null) {
                    akVar.b();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (OutOfMemoryError e26) {
                akVar = null;
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (akVar != null) {
                    akVar.b();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return null;
            } catch (Throwable th5) {
                th2 = th5;
                akVar = null;
                fileInputStream2 = null;
                th = th2;
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (akVar != null) {
                    akVar.b();
                }
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e27) {
            if (fileOutputStream2 != null) {
                fileOutputStream2.flush();
                fileOutputStream2.close();
            }
            if (akVar2 != null) {
                akVar2.b();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (IOException e28) {
            fileOutputStream = null;
            akVar = null;
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            if (akVar != null) {
                akVar.b();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (OutOfMemoryError e29) {
            fileOutputStream = null;
            akVar = null;
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            if (akVar != null) {
                akVar.b();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return null;
        } catch (Throwable th6) {
            th2 = th6;
            fileOutputStream = null;
            akVar = null;
            fileInputStream2 = null;
            th = th2;
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            if (akVar != null) {
                akVar.b();
            }
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            throw th;
        }
    }

    private static HttpResponse a(HttpClient httpClient, URI uri, File file, ProgressObserver progressObserver) {
        HttpUriRequest httpPost = new HttpPost(uri.toString().replace("&", SqlMarker.SQL_END) + ";full;");
        httpPost.addHeader("Content-type", "iavs4/upload");
        httpPost.setEntity(new aq(file, "binary/octet-stream", progressObserver));
        try {
            return httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }
}
