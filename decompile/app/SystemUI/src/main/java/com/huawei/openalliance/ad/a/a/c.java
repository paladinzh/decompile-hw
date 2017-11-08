package com.huawei.openalliance.ad.a.a;

import com.huawei.openalliance.ad.a.a.a.a;
import com.huawei.openalliance.ad.a.a.b.e;
import com.huawei.openalliance.ad.a.a.b.m;

/* compiled from: Unknown */
public class c extends a {
    private m paramfromserver__;
    private String seq__;
    private String showid__;
    private long time__;
    private String type__;

    public c(String str, e eVar) {
        this.type__ = str;
        this.paramfromserver__ = eVar.getParamfromserver__();
        this.showid__ = eVar.getShowid__();
        this.time__ = System.currentTimeMillis();
    }

    public m getParamfromserver__() {
        return this.paramfromserver__;
    }

    public String getSeq__() {
        return this.seq__;
    }

    public String getShowid__() {
        return this.showid__;
    }

    public long getTime__() {
        return this.time__;
    }

    public String getType__() {
        return this.type__;
    }

    public void setParamfromserver__(m mVar) {
        this.paramfromserver__ = mVar;
    }

    public void setSeq__(String str) {
        this.seq__ = str;
    }

    public void setShowid__(String str) {
        this.showid__ = str;
    }

    public void setTime__(long j) {
        this.time__ = j;
    }

    public void setType__(String str) {
        this.type__ = str;
    }
}
