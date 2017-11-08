package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ServerInformation;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.UrlCheckResultStructure.UrlCheckResult;
import com.avast.android.sdk.engine.UrlSource;
import com.avast.android.sdk.engine.obfuscated.aj;
import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.b;
import com.avast.android.sdk.engine.obfuscated.bk;
import com.avast.android.sdk.engine.obfuscated.s;
import com.avast.android.sdk.engine.obfuscated.z.d;
import com.avast.cloud.webrep.proto.Urlinfo.AvastIdentity;
import com.avast.cloud.webrep.proto.Urlinfo.AvastIdentity.Builder;
import com.avast.cloud.webrep.proto.Urlinfo.Blocker;
import com.avast.cloud.webrep.proto.Urlinfo.BrowserExtInfo;
import com.avast.cloud.webrep.proto.Urlinfo.BrowserType;
import com.avast.cloud.webrep.proto.Urlinfo.Client;
import com.avast.cloud.webrep.proto.Urlinfo.Client.CType;
import com.avast.cloud.webrep.proto.Urlinfo.Identity;
import com.avast.cloud.webrep.proto.Urlinfo.KeyValue;
import com.avast.cloud.webrep.proto.Urlinfo.MessageClientInfo;
import com.avast.cloud.webrep.proto.Urlinfo.OS;
import com.avast.cloud.webrep.proto.Urlinfo.Phishing;
import com.avast.cloud.webrep.proto.Urlinfo.Typo;
import com.avast.cloud.webrep.proto.Urlinfo.UrlInfo;
import com.avast.cloud.webrep.proto.Urlinfo.UrlInfoRequest;
import com.avast.cloud.webrep.proto.Urlinfo.UrlInfoResponse;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;

/* compiled from: Unknown */
public class n {
    private static final Map<String, Long> a = new HashMap();
    private static final Map<String, byte[]> b = new HashMap();
    private static final byte[] c = new byte[]{(byte) 0, (byte) 10};

    private static Builder a(String str, String str2, String str3, String str4) {
        Builder newBuilder = AvastIdentity.newBuilder();
        newBuilder.setGuid(ByteString.copyFromUtf8(str));
        if (!TextUtils.isEmpty(str2)) {
            newBuilder.setAuid(ByteString.copyFromUtf8(str2));
        }
        if (!TextUtils.isEmpty(str4)) {
            newBuilder.setHwid(ByteString.copyFromUtf8(str4));
        }
        if (!TextUtils.isEmpty(str3)) {
            newBuilder.setUuid(ByteString.copyFromUtf8(str3));
        }
        return newBuilder;
    }

    private static BrowserExtInfo.Builder a(UrlSource urlSource) {
        BrowserType browserType = null;
        BrowserExtInfo.Builder newBuilder = BrowserExtInfo.newBuilder();
        newBuilder.setOs(OS.ANDROID);
        newBuilder.setOsVersion(OS.ANDROID.name() + " " + VERSION.RELEASE);
        switch (o.a[urlSource.ordinal()]) {
            case 1:
                browserType = BrowserType.STOCK;
                break;
            case 2:
                browserType = BrowserType.STOCK_JB;
                break;
            case 3:
                browserType = BrowserType.CHROME;
                break;
            case 4:
                browserType = BrowserType.DOLPHIN_MINI;
                break;
            case 5:
                browserType = BrowserType.DOLPHIN;
                break;
            case 6:
                browserType = BrowserType.SILK;
                break;
            case 7:
                browserType = BrowserType.BOAT_MINI;
                break;
            case 8:
                browserType = BrowserType.BOAT;
                break;
            case 9:
                browserType = BrowserType.CHROME_M;
                break;
        }
        if (browserType != null) {
            newBuilder.setBrowserType(browserType);
        }
        return newBuilder;
    }

    private static Identity.Builder a(String str, String str2) {
        Identity.Builder newBuilder = Identity.newBuilder();
        newBuilder.setGuid(ByteString.copyFromUtf8(str));
        if (!TextUtils.isEmpty(str2)) {
            newBuilder.setAuid(ByteString.copyFromUtf8(str2));
        }
        return newBuilder;
    }

    private static MessageClientInfo.Builder a() {
        MessageClientInfo.Builder newBuilder = MessageClientInfo.newBuilder();
        newBuilder.setOs(OS.ANDROID);
        newBuilder.setOsVersion(OS.ANDROID.name() + " " + VERSION.RELEASE);
        return newBuilder;
    }

    @SuppressLint({"NewApi"})
    public static List<UrlCheckResultStructure> a(Context context, Integer num, String str, UrlSource urlSource) {
        boolean z = false;
        List<UrlCheckResultStructure> linkedList = new LinkedList();
        if (a(str)) {
            ao.a("found in whitelist: " + str);
            linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_OK));
            return linkedList;
        }
        ServerInformation a = k.a(context, num);
        if (a != null) {
            URI uri = a.getUri();
            String guid = EngineInterface.getEngineConfig().getGuid();
            String auid = EngineInterface.getEngineConfig().getAuid();
            String uuid = EngineInterface.getEngineConfig().getUuid();
            String a2 = b.a(context);
            if (!(str.startsWith("http://") || str.startsWith("https://"))) {
                str = "http://" + str;
            }
            Client.Builder newBuilder = Client.newBuilder();
            newBuilder.setId(a(guid, auid, uuid, a2));
            if (urlSource != UrlSource.MESSAGE) {
                newBuilder.setBrowserExtInfo(a(urlSource));
                newBuilder.setType(CType.BROWSER_EXT);
            } else {
                newBuilder.setType(CType.MESSAGE);
                newBuilder.setMessageClientInfo(a());
            }
            UrlInfoRequest.Builder newBuilder2 = UrlInfoRequest.newBuilder();
            newBuilder2.setApikey(ByteString.copyFromUtf8(EngineInterface.getEngineConfig().getUrlInfoApiKey()));
            newBuilder2.addUri(str);
            newBuilder2.setClient(newBuilder);
            newBuilder2.setLocale(Locale.getDefault().getCountry());
            newBuilder2.setCallerId(EngineInterface.getEngineConfig().getUrlInfoCallerId().longValue());
            newBuilder2.setDnl(!EngineInterface.getEngineConfig().isWebLoggingEnabled());
            newBuilder2.setRequestedServices(14);
            if (UrlSource.MESSAGE.equals(urlSource)) {
                newBuilder2.setVisited(false);
            } else {
                newBuilder2.setVisited(true);
            }
            newBuilder2.setIdentity(a(guid, auid));
            KeyValue.Builder newBuilder3 = KeyValue.newBuilder();
            newBuilder3.setKey("AndroidSource");
            newBuilder3.setValue(urlSource.name());
            newBuilder2.addCustomKeyValue(newBuilder3.build());
            try {
                UrlInfoResponse parseFrom = UrlInfoResponse.parseFrom(bk.a(context).a(uri.toString() + "%s/%s", newBuilder2.build().toByteArray()));
                if (parseFrom.getUrlInfoCount() >= 1) {
                    boolean z2;
                    UrlInfo urlInfo = parseFrom.getUrlInfo(0);
                    if (urlInfo.hasBlocker()) {
                        Blocker blocker = urlInfo.getBlocker();
                        if (blocker.hasBlock()) {
                            ao.a("blocker = " + blocker.getBlock());
                            if (!(blocker.getBlock() <= 0)) {
                                linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_MALWARE));
                            }
                        }
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    if (urlInfo.hasPhishing()) {
                        Phishing phishing = urlInfo.getPhishing();
                        if (phishing.hasPhishing()) {
                            ao.a("phishing = " + phishing.getPhishing());
                            switch (phishing.getPhishing()) {
                                case 0:
                                case 1:
                                    break;
                                case 2:
                                    linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_PHISHING));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    z2 = false;
                    if (urlInfo.hasTypo()) {
                        Typo typo = urlInfo.getTypo();
                        ao.a("typo = " + typo.getIsTypo());
                        if (typo.getIsTypo()) {
                            UrlCheckResultStructure urlCheckResultStructure = new UrlCheckResultStructure(UrlCheckResult.RESULT_TYPO_SQUATTING);
                            if (typo.hasUrlTo()) {
                                urlCheckResultStructure.desiredSite = typo.getUrlTo();
                            } else {
                                urlCheckResultStructure.desiredSite = null;
                            }
                            if (typo.hasBrandDomain()) {
                                urlCheckResultStructure.brandDomain = typo.getBrandDomain();
                            } else {
                                urlCheckResultStructure.brandDomain = null;
                            }
                            linkedList.add(urlCheckResultStructure);
                        }
                        z = z2;
                    }
                    if (linkedList.isEmpty()) {
                        if (z) {
                            linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_OK));
                        } else {
                            linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
                        }
                    }
                    return linkedList;
                }
                linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
                return linkedList;
            } catch (ClientProtocolException e) {
                ao.a("ClientProtocolException: " + e);
                linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
                return linkedList;
            } catch (IOException e2) {
                ao.a("IOException: " + e2);
                linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
                return linkedList;
            } catch (s e3) {
                ao.a("EncryptionException: " + e3);
                linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
                return linkedList;
            }
        }
        linkedList.add(new UrlCheckResultStructure(UrlCheckResult.RESULT_UNKNOWN_ERROR));
        return linkedList;
    }

    public static void a(Context context, Integer num) {
        synchronized (b) {
            List linkedList = new LinkedList();
            for (Entry entry : b.entrySet()) {
                if (a(context, num, new ByteArrayEntity((byte[]) entry.getValue()))) {
                    linkedList.add(entry.getKey());
                }
            }
            for (int i = 0; i < linkedList.size(); i++) {
                String str = (String) linkedList.get(i);
                b.remove(str);
                File file = new File(context.getDir("tstmp", 0) + "/" + str);
                if (file.exists()) {
                    file.delete();
                }
            }
            File[] listFiles = context.getDir("tstmp", 0).listFiles();
            for (int i2 = 0; i2 < listFiles.length; i2++) {
                if (a(context, num, new FileEntity(listFiles[i2], "binary/octet-stream"))) {
                    listFiles[i2].delete();
                    b.remove(listFiles[i2].getName());
                }
            }
        }
    }

    public static void a(android.content.Context r10, java.lang.Integer r11, java.lang.String r12, com.avast.android.sdk.engine.UrlCheckResultStructure r13, boolean r14, boolean r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:79:0x0193
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r1 = 0;
        r4 = 0;
        if (r13 == 0) goto L_0x0033;
    L_0x0004:
        r0 = new java.io.File;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "tstmp";
        r3 = r10.getDir(r3, r4);
        r2 = r2.append(r3);
        r3 = "/";
        r2 = r2.append(r3);
        r3 = r13.getRedirectId();
        r2 = r2.append(r3);
        r2 = r2.toString();
        r0.<init>(r2);
        r0 = r0.exists();
        if (r0 == 0) goto L_0x0034;
    L_0x0032:
        return;
    L_0x0033:
        return;
    L_0x0034:
        r0 = com.avast.android.sdk.engine.obfuscated.z.b.v();
        r2 = 1;
        r2 = new byte[r2];
        r2[r4] = r4;
        if (r14 != 0) goto L_0x00c5;
    L_0x003f:
        if (r15 != 0) goto L_0x00cf;
    L_0x0041:
        r3 = com.avast.android.sdk.engine.EngineInterface.getEngineConfig();
        r3 = r3.getAuid();
        if (r3 != 0) goto L_0x00d9;
    L_0x004b:
        r3 = r13.brandDomain;
        r0.f(r3);
        r2 = com.google.protobuf.ByteString.copyFrom(r2);
        r0.a(r2);
        r2 = com.avast.android.sdk.engine.EngineInterface.getEngineConfig();
        r2 = r2.getGuid();
        r0.a(r2);
        r2 = java.util.Locale.getDefault();
        r2 = r2.getCountry();
        r0.g(r2);
        r2 = java.util.Locale.getDefault();
        r2 = r2.getCountry();
        r0.h(r2);
        r2 = com.avast.android.sdk.engine.obfuscated.z.a.ANDROID;
        r0.a(r2);
        r2 = r13.getRedirectId();
        r0.e(r2);
        r0.c(r12);
        r2 = r13.desiredSite;
        r0.d(r2);
        r0 = r0.d();
        r5 = new java.util.LinkedList;
        r5.<init>();
        r6 = b;	 Catch:{ IOException -> 0x019e, all -> 0x0170 }
        monitor-enter(r6);	 Catch:{ IOException -> 0x019e, all -> 0x0170 }
        r2 = b;	 Catch:{ all -> 0x0132 }
        r3 = r13.getRedirectId();	 Catch:{ all -> 0x0132 }
        r0 = r0.toByteArray();	 Catch:{ all -> 0x0132 }
        r2.put(r3, r0);	 Catch:{ all -> 0x0132 }
        r0 = b;	 Catch:{ all -> 0x0132 }
        r0 = r0.entrySet();	 Catch:{ all -> 0x0132 }
        r7 = r0.iterator();	 Catch:{ all -> 0x0132 }
        r2 = r1;
    L_0x00b0:
        r0 = r7.hasNext();	 Catch:{ all -> 0x01a6 }
        if (r0 != 0) goto L_0x00de;	 Catch:{ all -> 0x01a6 }
    L_0x00b6:
        monitor-exit(r6);	 Catch:{ all -> 0x01a6 }
        if (r2 != 0) goto L_0x0144;
    L_0x00b9:
        r1 = r4;
    L_0x00ba:
        r0 = r5.size();
        if (r1 < r0) goto L_0x014c;
    L_0x00c0:
        a(r10, r11);
        goto L_0x0032;
    L_0x00c5:
        r3 = r2[r4];
        r3 = r3 | 1;
        r3 = (byte) r3;
        r3 = (byte) r3;
        r2[r4] = r3;
        goto L_0x003f;
    L_0x00cf:
        r3 = r2[r4];
        r3 = r3 | 2;
        r3 = (byte) r3;
        r3 = (byte) r3;
        r2[r4] = r3;
        goto L_0x0041;
    L_0x00d9:
        r0.b(r3);
        goto L_0x004b;
    L_0x00de:
        r0 = r7.next();	 Catch:{ all -> 0x01a6 }
        r0 = (java.util.Map.Entry) r0;	 Catch:{ all -> 0x01a6 }
        r8 = new java.io.File;	 Catch:{ all -> 0x01a6 }
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01a6 }
        r1.<init>();	 Catch:{ all -> 0x01a6 }
        r3 = "tstmp";	 Catch:{ all -> 0x01a6 }
        r9 = 0;	 Catch:{ all -> 0x01a6 }
        r3 = r10.getDir(r3, r9);	 Catch:{ all -> 0x01a6 }
        r1 = r1.append(r3);	 Catch:{ all -> 0x01a6 }
        r3 = "/";	 Catch:{ all -> 0x01a6 }
        r3 = r1.append(r3);	 Catch:{ all -> 0x01a6 }
        r1 = r0.getKey();	 Catch:{ all -> 0x01a6 }
        r1 = (java.lang.String) r1;	 Catch:{ all -> 0x01a6 }
        r1 = r3.append(r1);	 Catch:{ all -> 0x01a6 }
        r1 = r1.toString();	 Catch:{ all -> 0x01a6 }
        r8.<init>(r1);	 Catch:{ all -> 0x01a6 }
        r1 = r8.exists();	 Catch:{ all -> 0x01a6 }
        if (r1 == 0) goto L_0x011f;
    L_0x0115:
        r1 = r2;
    L_0x0116:
        r0 = r0.getKey();	 Catch:{ all -> 0x0132 }
        r5.add(r0);	 Catch:{ all -> 0x0132 }
        r2 = r1;
        goto L_0x00b0;
    L_0x011f:
        r1 = r0.getValue();	 Catch:{ all -> 0x01a6 }
        r1 = (byte[]) r1;	 Catch:{ all -> 0x01a6 }
        r3 = new java.io.FileOutputStream;	 Catch:{ all -> 0x01a6 }
        r3.<init>(r8);	 Catch:{ all -> 0x01a6 }
        r3.write(r1);	 Catch:{ all -> 0x01a9 }
        r3.flush();	 Catch:{ all -> 0x01a9 }
        r1 = r3;
        goto L_0x0116;
    L_0x0132:
        r0 = move-exception;
    L_0x0133:
        monitor-exit(r6);	 Catch:{ all -> 0x0132 }
        throw r0;	 Catch:{ IOException -> 0x0135, all -> 0x019a }
    L_0x0135:
        r0 = move-exception;
        r2 = r1;
    L_0x0137:
        if (r2 != 0) goto L_0x015c;
    L_0x0139:
        r0 = r5.size();
        if (r4 < r0) goto L_0x0162;
    L_0x013f:
        a(r10, r11);
        goto L_0x0032;
    L_0x0144:
        r2.close();	 Catch:{ IOException -> 0x0149 }
        goto L_0x00b9;
    L_0x0149:
        r0 = move-exception;
        goto L_0x00b9;
    L_0x014c:
        r0 = r5.get(r1);
        r0 = (java.lang.String) r0;
        r2 = b;
        r2.remove(r0);
        r0 = r1 + 1;
        r1 = r0;
        goto L_0x00ba;
    L_0x015c:
        r2.close();	 Catch:{ IOException -> 0x0160 }
        goto L_0x0139;
    L_0x0160:
        r0 = move-exception;
        goto L_0x0139;
    L_0x0162:
        r0 = r5.get(r4);
        r0 = (java.lang.String) r0;
        r1 = b;
        r1.remove(r0);
        r4 = r4 + 1;
        goto L_0x0139;
    L_0x0170:
        r0 = move-exception;
        r2 = r1;
        r1 = r0;
    L_0x0173:
        if (r2 != 0) goto L_0x017f;
    L_0x0175:
        r0 = r5.size();
        if (r4 < r0) goto L_0x0185;
    L_0x017b:
        a(r10, r11);
        throw r1;
    L_0x017f:
        r2.close();	 Catch:{ IOException -> 0x0183 }
        goto L_0x0175;
    L_0x0183:
        r0 = move-exception;
        goto L_0x0175;
    L_0x0185:
        r0 = r5.get(r4);
        r0 = (java.lang.String) r0;
        r2 = b;
        r2.remove(r0);
        r4 = r4 + 1;
        goto L_0x0175;
        r0 = move-exception;
        r1 = r0;
        goto L_0x0173;
        r0 = move-exception;
        r1 = r0;
        r2 = r3;
        goto L_0x0173;
    L_0x019a:
        r0 = move-exception;
        r2 = r1;
        r1 = r0;
        goto L_0x0173;
    L_0x019e:
        r0 = move-exception;
        r2 = r1;
        goto L_0x0137;
        r0 = move-exception;
        goto L_0x0137;
        r0 = move-exception;
        r2 = r3;
        goto L_0x0137;
    L_0x01a6:
        r0 = move-exception;
        r1 = r2;
        goto L_0x0133;
    L_0x01a9:
        r0 = move-exception;
        r1 = r3;
        goto L_0x0133;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.avast.android.sdk.engine.internal.n.a(android.content.Context, java.lang.Integer, java.lang.String, com.avast.android.sdk.engine.UrlCheckResultStructure, boolean, boolean):void");
    }

    public static void a(String str, Integer num) {
        if (str.indexOf(47, 7) != -1) {
            str = str.substring(0, str.indexOf(47, 7));
        }
        String substring = str.substring(str.indexOf("//") + 2);
        synchronized (a) {
            String[] split = substring.split("\\.");
            for (int i = 0; i < split.length - 1; i++) {
                String str2 = "";
                for (int i2 = i; i2 < split.length; i2++) {
                    str2 = str2 + split[i2] + ".";
                }
                str2 = str2.substring(0, str2.length() - 1);
                long nanoTime = System.nanoTime() + (((((long) num.intValue()) * 1000) * 1000) * 1000);
                Long l = (Long) a.get(str2);
                if (l != null) {
                    if ((nanoTime <= l.longValue() ? 1 : 0) == 0) {
                        a.remove(str2);
                    } else {
                        return;
                    }
                }
                a.put(str2, Long.valueOf(nanoTime));
                if (a.size() > 1000) {
                    b();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressLint({"NewApi"})
    private static boolean a(Context context, Integer num, HttpEntity httpEntity) {
        HttpResponse httpResponse = null;
        ServerInformation d = k.d(context, num);
        if (d == null) {
            return false;
        }
        URI uri = d.getUri();
        try {
            String replace = new String(aj.b(al.a(c, EngineInterface.getEngineConfig().getGuid().replace("-", "")), 0)).replace('+', '-').replace('/', '_');
            AndroidHttpClient newInstance = AndroidHttpClient.newInstance("avdroid");
            try {
                HttpUriRequest httpPost = new HttpPost(uri + URLEncoder.encode(replace, "UTF-8"));
                httpPost.setEntity(httpEntity);
                try {
                    httpResponse = newInstance.execute(httpPost);
                    d a = d.a(httpResponse.getEntity().getContent());
                    if (a.c()) {
                        switch (o.b[a.d().ordinal()]) {
                            case 1:
                            case 2:
                                if (httpResponse != null) {
                                    try {
                                        httpResponse.getEntity().consumeContent();
                                    } catch (IOException e) {
                                    }
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                return true;
                            case 3:
                                if (httpResponse != null) {
                                    try {
                                        httpResponse.getEntity().consumeContent();
                                    } catch (IOException e2) {
                                    }
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                return false;
                            default:
                                if (httpResponse != null) {
                                    try {
                                        httpResponse.getEntity().consumeContent();
                                    } catch (IOException e3) {
                                    }
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                return false;
                        }
                    }
                    if (httpResponse != null) {
                        try {
                            httpResponse.getEntity().consumeContent();
                        } catch (IOException e4) {
                        }
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    return false;
                } catch (IOException e5) {
                    if (httpResponse != null) {
                        try {
                            httpResponse.getEntity().consumeContent();
                        } catch (IOException e6) {
                        }
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    return false;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    HttpResponse httpResponse2 = httpResponse;
                    Throwable th3 = th2;
                    if (httpResponse2 != null) {
                        try {
                            httpResponse2.getEntity().consumeContent();
                        } catch (IOException e7) {
                        }
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    throw th3;
                }
            } catch (UnsupportedEncodingException e8) {
                return false;
            }
        } catch (IOException e9) {
            return false;
        }
    }

    private static boolean a(String str) {
        if (str.indexOf("://") != -1) {
            Object substring = str.substring(str.indexOf("://") + 3);
        }
        if (substring.indexOf(47) != -1) {
            substring = substring.substring(0, substring.indexOf(47));
        }
        synchronized (a) {
            if (a.containsKey(substring)) {
                if (System.nanoTime() <= ((Long) a.get(substring)).longValue()) {
                    return true;
                }
                a.remove(substring);
                return false;
            }
            return false;
        }
    }

    private static void b() {
        for (Entry entry : a.entrySet()) {
            if ((System.nanoTime() <= ((Long) entry.getValue()).longValue() ? 1 : null) == null) {
                a.remove(entry.getKey());
            }
        }
    }
}
