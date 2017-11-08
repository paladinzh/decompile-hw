package ucd.ui.util;

import android.content.Context;
import java.util.HashMap;
import java.util.List;
import ucd.ui.util.DOMLoader.Dom;
import ucd.ui.util.DOMLoader.Dom.XMLAttributeSet;
import ucd.ui.util.DOMLoader.OnLoadCallback;

public class CSSManager {
    private static volatile HashMap<String, XMLAttributeSet> list = new HashMap();

    private CSSManager() {
    }

    public static void read(Context context, String url) {
        DOMLoader.read(context, url, new OnLoadCallback() {
            public void onLoadDom(Dom parent, Dom current) {
                CSSManager.list.put(current.getTagName(), current.getAttrs());
            }

            public void onLoad(List<Dom> list) {
            }
        });
    }
}
