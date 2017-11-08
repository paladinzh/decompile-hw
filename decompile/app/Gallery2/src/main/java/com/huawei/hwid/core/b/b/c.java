package com.huawei.hwid.core.b.b;

import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;

class c implements ConnPerRoute {
    c() {
    }

    public int getMaxForRoute(HttpRoute httpRoute) {
        return 8;
    }
}
