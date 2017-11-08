package com.huawei.hwid.core.b.b;

import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;

class f implements ConnPerRoute {
    f() {
    }

    public int getMaxForRoute(HttpRoute httpRoute) {
        return 8;
    }
}
