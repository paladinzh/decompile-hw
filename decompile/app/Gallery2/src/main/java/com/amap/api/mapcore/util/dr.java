package com.amap.api.mapcore.util;

import android.text.TextUtils;
import android.util.Log;
import com.amap.api.maps.model.Tile;
import com.amap.api.maps.model.TileProvider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/* compiled from: BaseTileProvider */
public abstract class dr implements TileProvider {
    private final int a;
    hc b;
    private final int c;

    /* compiled from: BaseTileProvider */
    class a extends hd {
        final /* synthetic */ dr a;
        private String b = "";

        public a(dr drVar, String str) {
            this.a = drVar;
            this.b = str;
            a(ff.a(p.a));
            a(5000);
            b(50000);
        }

        public Map<String, String> a() {
            Map<String, String> hashMap = new HashMap();
            hashMap.put("User-Agent", g.d);
            hashMap.put("Accept-Encoding", "gzip");
            hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{"4.1.2", "3dmap"}));
            hashMap.put("X-INFO", fb.a(p.a));
            hashMap.put("key", ey.f(p.a));
            hashMap.put("logversion", "2.1");
            return hashMap;
        }

        public Map<String, String> b() {
            return null;
        }

        public String c() {
            return this.b;
        }
    }

    public abstract String a(int i, int i2, int i3);

    public dr(int i, int i2) {
        this.a = i;
        this.c = i2;
    }

    public final Tile getTile(int i, int i2, int i3) {
        Object a = a(i, i2, i3);
        if (TextUtils.isEmpty(a)) {
            return NO_TILE;
        }
        Tile tile;
        try {
            tile = new Tile(this.a, this.c, a(a));
        } catch (IOException e) {
            tile = NO_TILE;
        }
        return tile;
    }

    private byte[] a(String str) throws IOException {
        try {
            hd aVar = new a(this, str);
            this.b = hc.a(false);
            return this.b.d(aVar);
        } catch (Throwable th) {
            Log.e("BaseTileProvider", str);
            th.printStackTrace();
            return null;
        }
    }

    public int getTileWidth() {
        return this.a;
    }

    public int getTileHeight() {
        return this.c;
    }
}
