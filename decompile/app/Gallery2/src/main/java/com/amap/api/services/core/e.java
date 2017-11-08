package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearchQuery;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: DistrictServerHandler */
public class e extends r<DistrictSearchQuery, DistrictResult> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public e(Context context, DistrictSearchQuery districtSearchQuery) {
        super(context, districtSearchQuery);
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json");
        stringBuffer.append("&page=").append(((DistrictSearchQuery) this.a).getPageNum() + 1);
        stringBuffer.append("&offset=").append(((DistrictSearchQuery) this.a).getPageSize());
        stringBuffer.append("&showChild=").append(((DistrictSearchQuery) this.a).isShowChild());
        if (((DistrictSearchQuery) this.a).isShowBoundary()) {
            stringBuffer.append("&extensions=all");
        } else {
            stringBuffer.append("&extensions=base");
        }
        if (((DistrictSearchQuery) this.a).checkKeyWords()) {
            stringBuffer.append("&keywords=").append(c(((DistrictSearchQuery) this.a).getKeywords()));
        }
        if (((DistrictSearchQuery) this.a).checkLevels()) {
            stringBuffer.append("&level=").append(((DistrictSearchQuery) this.a).getKeywordsLevel());
        }
        stringBuffer.append("&key=" + w.f(this.d));
        return stringBuffer.toString();
    }

    protected DistrictResult a(String str) throws AMapException {
        ArrayList arrayList = new ArrayList();
        DistrictResult districtResult = new DistrictResult((DistrictSearchQuery) this.a, arrayList);
        try {
            JSONObject jSONObject = new JSONObject(str);
            districtResult.setPageCount(jSONObject.optInt("count"));
            JSONArray optJSONArray = jSONObject.optJSONArray("districts");
            if (optJSONArray == null) {
                return districtResult;
            }
            j.a(optJSONArray, arrayList, null);
            return districtResult;
        } catch (Throwable e) {
            d.a(e, "DistrictServerHandler", "paseJSONJSONException");
        } catch (Throwable e2) {
            d.a(e2, "DistrictServerHandler", "paseJSONException");
        }
    }

    public String b() {
        return c.a() + "/config/district?";
    }
}
