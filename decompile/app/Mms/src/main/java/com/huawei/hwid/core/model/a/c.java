package com.huawei.hwid.core.model.a;

import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;

/* compiled from: HttpClientConnetManager */
final class c implements ConnPerRoute {
    c() {
    }

    public int getMaxForRoute(HttpRoute httpRoute) {
        return 8;
    }
}
