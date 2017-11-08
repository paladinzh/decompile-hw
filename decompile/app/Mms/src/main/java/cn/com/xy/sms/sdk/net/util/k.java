package cn.com.xy.sms.sdk.net.util;

import cn.com.xy.sms.sdk.db.entity.E;
import cn.com.xy.sms.sdk.db.entity.H;
import cn.com.xy.sms.sdk.db.entity.a.l;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.service.e.b;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquSourceAdapterDataSource;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/* compiled from: Unknown */
final class k implements Runnable {
    private final /* synthetic */ NodeList a;

    k(NodeList nodeList) {
        this.a = nodeList;
    }

    public final void run() {
        try {
            int length = this.a.getLength();
            JSONArray jSONArray = new JSONArray();
            for (int i = 0; i < length; i++) {
                try {
                    Element element = (Element) this.a.item(i);
                    String nodeValue = element.getFirstChild().getNodeValue();
                    String attribute = element.getAttribute("attr1");
                    String attribute2 = element.getAttribute("attr2");
                    if (StringUtils.allValuesIsNotNull(nodeValue, attribute, attribute2)) {
                        List a = l.a("content_sign=? ", new String[]{nodeValue}, 1);
                        cn.com.xy.sms.sdk.db.entity.a.k kVar = (a != null && a.size() > 0) ? (cn.com.xy.sms.sdk.db.entity.a.k) a.get(0) : null;
                        if (kVar != null) {
                            String[] parseShard = DexUtil.parseShard(StringUtils.decode(kVar.c), attribute, attribute2);
                            if (parseShard != null && parseShard.length >= 2) {
                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("contentSign", nodeValue);
                                jSONObject.put(DuoquSourceAdapterDataSource.INDEX_KEY, attribute);
                                jSONObject.put("mod", attribute2);
                                jSONObject.put("characterSequence", parseShard[0]);
                                jSONObject.put("eof", parseShard[1]);
                                jSONArray.put(jSONObject);
                            }
                        }
                    }
                } catch (Throwable th) {
                }
            }
            if (jSONArray.length() > 0) {
                E.a(jSONArray.toString(), H.UPLOAD_SHARD, 0);
                b.a();
            }
        } catch (Throwable th2) {
        }
    }
}
