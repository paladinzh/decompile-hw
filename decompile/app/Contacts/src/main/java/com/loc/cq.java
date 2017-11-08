package com.loc;

import android.text.TextUtils;
import com.amap.api.services.district.DistrictSearchQuery;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/* compiled from: Parser */
public class cq {

    /* compiled from: Parser */
    private class a extends DefaultHandler {
        public AmapLoc a;
        final /* synthetic */ cq b;
        private String c;

        private a(cq cqVar) {
            this.b = cqVar;
            this.a = new AmapLoc();
            this.c = "";
        }

        public void characters(char[] cArr, int i, int i2) {
            this.c = String.valueOf(cArr, i, i2);
        }

        public void endElement(String str, String str2, String str3) {
            if (str2.equals("retype")) {
                this.a.g(this.c);
            } else if (str2.equals("rdesc")) {
                this.a.h(this.c);
            } else if (str2.equals("adcode")) {
                this.a.k(this.c);
            } else if (str2.equals("citycode")) {
                this.a.i(this.c);
            } else if (str2.equals("radius")) {
                try {
                    this.a.a(Float.parseFloat(this.c));
                } catch (Throwable th) {
                    e.a(th, "parser", "endElement3");
                    this.a.a(3891.0f);
                }
            } else if (str2.equals("cenx")) {
                try {
                    this.a.a(Double.parseDouble(this.c));
                } catch (Throwable th2) {
                    e.a(th2, "parser", "endElement2");
                    this.a.a(0.0d);
                }
            } else if (str2.equals("ceny")) {
                try {
                    this.a.b(Double.parseDouble(this.c));
                } catch (Throwable th22) {
                    e.a(th22, "parser", "endElement1");
                    this.a.b(0.0d);
                }
            } else if (str2.equals("desc")) {
                this.a.j(this.c);
            } else if (str2.equals(DistrictSearchQuery.KEYWORDS_COUNTRY)) {
                this.a.l(this.c);
            } else if (str2.equals(DistrictSearchQuery.KEYWORDS_PROVINCE)) {
                this.a.m(this.c);
            } else if (str2.equals(DistrictSearchQuery.KEYWORDS_CITY)) {
                this.a.n(this.c);
            } else if (str2.equals(DistrictSearchQuery.KEYWORDS_DISTRICT)) {
                this.a.o(this.c);
            } else if (str2.equals("road")) {
                this.a.p(this.c);
            } else if (str2.equals("street")) {
                this.a.q(this.c);
            } else if (str2.equals("number")) {
                this.a.r(this.c);
            } else if (str2.equals("poiname")) {
                this.a.s(this.c);
            } else if (str2.equals("BIZ")) {
                if (this.a.E() == null) {
                    this.a.a(new JSONObject());
                }
                try {
                    this.a.E().put("BIZ", this.c);
                } catch (Throwable th222) {
                    e.a(th222, "parser", "endElement");
                }
            } else if (str2.equals("cens")) {
                this.a.u(this.c);
            } else if (str2.equals("pid")) {
                this.a.v(this.c);
            } else if (str2.equals("flr")) {
                this.a.w(this.c);
            } else if (str2.equals("coord")) {
                if (TextUtils.isEmpty(e.g)) {
                    e.g = this.c;
                }
                this.a.x(this.c);
            } else if (str2.equals("mcell")) {
                this.a.y(this.c);
            } else if (!str2.equals("gkeyloc") && !str2.equals("gkeygeo")) {
                if (str2.equals("apiTime")) {
                    this.a.a(Long.parseLong(this.c));
                } else if (str2.equals("aoiname")) {
                    this.a.t(this.c);
                }
            }
        }

        public void startElement(String str, String str2, String str3, Attributes attributes) {
            this.c = "";
        }
    }

    public AmapLoc a(String str) {
        InputStream inputStream;
        if (!str.contains("SuccessCode")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.reverse();
            try {
                str = new String(r.b(stringBuilder.toString()), "UTF-8");
            } catch (Throwable e) {
                e.a(e, "parser", "ParserApsResp1");
            }
            stringBuilder.delete(0, stringBuilder.length());
        }
        if (!str.contains("SuccessCode=\"0\"")) {
            try {
            } catch (Throwable e2) {
                e.a(e2, "parser", "ParserApsResp");
                inputStream = null;
            }
        }
        inputStream = new ByteArrayInputStream(str.getBytes("UTF-8"));
        SAXParserFactory newInstance = SAXParserFactory.newInstance();
        DefaultHandler aVar = new a();
        try {
            SAXParser newSAXParser = newInstance.newSAXParser();
            if (inputStream != null) {
                newSAXParser.parse(inputStream, aVar);
                inputStream.close();
            }
            aVar.a.c("network");
            return aVar.a;
        } catch (Throwable e22) {
            e.a(e22, "parser", "endElement4");
            AmapLoc amapLoc = new AmapLoc();
            amapLoc.b(5);
            bv.c.append("parser error:" + e22.getMessage());
            amapLoc.b(bv.c.toString());
            return amapLoc;
        }
    }

    public AmapLoc b(String str) {
        AmapLoc amapLoc = new AmapLoc();
        amapLoc.b(7);
        try {
            String string;
            String string2;
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("status")) {
                if (jSONObject.has("info")) {
                    string = jSONObject.getString("status");
                    string2 = jSONObject.getString("info");
                    if (string.equals(CallInterceptDetails.BRANDED_STATE)) {
                        bv.c.append("json is error " + str);
                    }
                    if (string.equals("0")) {
                        bv.c.append("auth fail:" + string2);
                    }
                    amapLoc.b(bv.c.toString());
                    return amapLoc;
                }
            }
            bv.c.append("json is error " + str);
            string = jSONObject.getString("status");
            string2 = jSONObject.getString("info");
            if (string.equals(CallInterceptDetails.BRANDED_STATE)) {
                bv.c.append("json is error " + str);
            }
            if (string.equals("0")) {
                bv.c.append("auth fail:" + string2);
            }
        } catch (Throwable th) {
            bv.c.append("json exception error:" + th.getMessage());
            e.a(th, "parser", "paseAuthFailurJson");
        }
        amapLoc.b(bv.c.toString());
        return amapLoc;
    }
}
