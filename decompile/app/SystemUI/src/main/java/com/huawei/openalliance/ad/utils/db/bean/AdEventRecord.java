package com.huawei.openalliance.ad.utils.db.bean;

import android.text.TextUtils;
import com.huawei.openalliance.ad.a.a.b.m;
import com.huawei.openalliance.ad.a.a.c;
import com.huawei.openalliance.ad.utils.b.d;
import fyusion.vislib.BuildConfig;
import org.json.JSONObject;

/* compiled from: Unknown */
public class AdEventRecord extends a {
    private String _id;
    private int adType_;
    private long lockTime_ = 0;
    private String paramFromServer_;
    private String showid_;
    private long time_;
    private String type_;

    public AdEventRecord(c cVar) {
        this.type_ = cVar.getType__();
        this.time_ = cVar.getTime__();
        this.showid_ = cVar.getShowid__();
        try {
            if (cVar.getParamfromserver__() != null) {
                this.paramFromServer_ = cVar.getParamfromserver__().toJson();
            }
        } catch (Throwable e) {
            d.a("AdEventRecord", "convert param error", e);
            this.paramFromServer_ = BuildConfig.FLAVOR;
        }
    }

    public c a() {
        c cVar = new c();
        cVar.setType__(this.type_);
        cVar.setTime__(this.time_);
        cVar.setSeq__(this._id);
        cVar.setShowid__(this.showid_);
        if (!TextUtils.isEmpty(this.paramFromServer_)) {
            m mVar = new m();
            try {
                mVar.fromJson(new JSONObject(this.paramFromServer_));
            } catch (Throwable e) {
                d.a("AdEventRecord", "convert param error", e);
            }
            cVar.setParamfromserver__(mVar);
        }
        return cVar;
    }

    public void a(int i) {
        this.adType_ = i;
    }

    public String b() {
        return this._id;
    }
}
