package com.amap.api.services.core;

/* compiled from: AMapCoreException */
public class v extends Exception {
    private String a = "未知的错误";
    private int b = -1;

    public v(String str) {
        super(str);
        a(str);
    }

    public String a() {
        return this.a;
    }

    private void a(String str) {
        if ("IO 操作异常 - IOException".equals(str)) {
            this.b = 21;
        } else if ("socket 连接异常 - SocketException".equals(str)) {
            this.b = 22;
        } else if ("socket 连接超时 - SocketTimeoutException".equals(str)) {
            this.b = 23;
        } else if ("无效的参数 - IllegalArgumentException".equals(str)) {
            this.b = 24;
        } else if ("空指针异常 - NullPointException".equals(str)) {
            this.b = 25;
        } else if ("url异常 - MalformedURLException".equals(str)) {
            this.b = 26;
        } else if ("未知主机 - UnKnowHostException".equals(str)) {
            this.b = 27;
        } else if ("服务器连接失败 - UnknownServiceException".equals(str)) {
            this.b = 28;
        } else if ("协议解析错误 - ProtocolException".equals(str)) {
            this.b = 29;
        } else if ("http连接失败 - ConnectionException".equals(str)) {
            this.b = 30;
        } else if ("未知的错误".equals(str)) {
            this.b = 31;
        } else if ("key鉴权失败".equals(str)) {
            this.b = 32;
        } else if ("requeust is null".equals(str)) {
            this.b = 1;
        } else if ("request url is empty".equals(str)) {
            this.b = 2;
        } else if ("response is null".equals(str)) {
            this.b = 3;
        } else if ("thread pool has exception".equals(str)) {
            this.b = 4;
        } else if ("sdk name is invalid".equals(str)) {
            this.b = 5;
        } else if ("sdk info is null".equals(str)) {
            this.b = 6;
        } else if ("sdk packages is null".equals(str)) {
            this.b = 7;
        } else if ("线程池为空".equals(str)) {
            this.b = 8;
        } else {
            this.b = -1;
        }
    }
}
