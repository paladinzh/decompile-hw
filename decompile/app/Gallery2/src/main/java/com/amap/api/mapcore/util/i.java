package com.amap.api.mapcore.util;

import android.content.Context;
import com.autonavi.amap.mapcore.MapCore;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager.RetStyleIconsFile;

/* compiled from: GLMapResManager */
public class i {
    public boolean a = true;
    private b b = null;
    private Context c = null;
    private MapCore d = null;

    /* compiled from: GLMapResManager */
    public enum a {
        NORAML,
        SATELLITE,
        BUS
    }

    /* compiled from: GLMapResManager */
    public enum b {
        NORMAL,
        PREVIEW_CAR,
        PREVIEW_BUS,
        PREVIEW_FOOT,
        NAVI_CAR,
        NAVI_BUS,
        NAVI_FOOT
    }

    /* compiled from: GLMapResManager */
    public enum c {
        DAY,
        NIGHT
    }

    public i(b bVar, Context context) {
        this.b = bVar;
        this.c = context;
        this.d = this.b.a();
    }

    public void a() {
        if (this.b != null) {
            RetStyleIconsFile retStyleIconsFile = new RetStyleIconsFile();
            byte[] styleData = MapTilsCacheAndResManager.getInstance(this.c).getStyleData(b(), retStyleIconsFile);
            if (styleData != null) {
                this.d.setStyleData(styleData, 0, 1);
            }
            byte[] styleData2 = MapTilsCacheAndResManager.getInstance(this.c).getStyleData("style_50_7_1445670996.data", retStyleIconsFile);
            if (styleData2 != null) {
                this.d.setStyleData(styleData2, 1, 1);
            }
        }
    }

    public void a(boolean z) {
        byte[] bArr = null;
        if (this.b != null) {
            RetStyleIconsFile retStyleIconsFile = new RetStyleIconsFile();
            String c = c();
            String a = a(c);
            final byte[] iconsData = MapTilsCacheAndResManager.getInstance(this.c).getIconsData(c, retStyleIconsFile);
            if (this.a) {
                bArr = MapTilsCacheAndResManager.getInstance(this.c).getIconsData(a, new RetStyleIconsFile());
            }
            final byte[] iconsData2 = MapTilsCacheAndResManager.getInstance(this.c).getIconsData("icons_50_7_1444880375.data", retStyleIconsFile);
            if (z) {
                if (iconsData != null) {
                    this.d.setInternaltexture(iconsData, 0);
                }
                if (iconsData2 != null) {
                    this.d.setInternaltexture(iconsData2, 31);
                }
                if (this.a && bArr != null) {
                    this.d.setInternaltexture(bArr, 20);
                }
            } else {
                this.b.a(new Runnable(this) {
                    final /* synthetic */ i d;

                    public void run() {
                        if (iconsData != null) {
                            this.d.d.setInternaltexture(iconsData, 0);
                        }
                        if (iconsData2 != null) {
                            this.d.d.setInternaltexture(iconsData2, 31);
                        }
                        if (this.d.a && bArr != null) {
                            this.d.d.setInternaltexture(bArr, 20);
                        }
                    }
                });
            }
        }
    }

    private String a(String str) {
        if (str.equals("icons_1_7_1444880368.data")) {
            this.a = true;
            return "icons_4_6_1437480571.data";
        }
        this.a = false;
        return null;
    }

    public String b() {
        String str = "";
        if (this.b == null) {
            return str;
        }
        c v = this.b.v();
        a w = this.b.w();
        b x = this.b.x();
        if (c.DAY != v) {
            if (c.NIGHT == v) {
                if (a.NORAML == w) {
                    str = b.NAVI_CAR != x ? b.PREVIEW_BUS != x ? "style_1_7_1445219169.data" : "style_6_7_1445325996.data" : "style_5_7_1445391719.data";
                } else if (a.SATELLITE == w) {
                    str = b.NAVI_CAR != x ? b.PREVIEW_BUS != x ? "style_3_7_1445827513.data" : "style_6_7_1445325996.data" : "style_5_7_1445391719.data";
                } else if (a.BUS == w) {
                    str = b.NAVI_CAR != x ? b.PREVIEW_BUS != x ? "style_6_7_1445325996.data" : "style_6_7_1445325996.data" : "style_5_7_1445391719.data";
                }
            }
        } else if (a.NORAML == w) {
            str = b.NAVI_CAR != x ? b.PREVIEW_BUS != x ? b.PREVIEW_CAR != x ? b.NAVI_BUS != x ? "style_1_7_1445219169.data" : "style_9_7_1445327958.data" : "style_8_7_1445391734.data" : "style_6_7_1445325996.data" : "style_4_7_1445391691.data";
        } else if (a.SATELLITE == w) {
            str = b.NAVI_CAR != x ? b.PREVIEW_BUS != x ? "style_3_7_1445827513.data" : "style_6_7_1445325996.data" : "style_4_7_1445391691.data";
        } else if (a.BUS == w) {
            str = b.NAVI_CAR != x ? b.PREVIEW_BUS != x ? "style_6_7_1445325996.data" : "style_6_7_1445325996.data" : "style_4_7_1445391691.data";
        }
        return str;
    }

    public String c() {
        String str = "";
        if (this.b == null) {
            return str;
        }
        c v = this.b.v();
        a w = this.b.w();
        if (c.DAY == v) {
            str = a.BUS != w ? "icons_1_7_1444880368.data" : "icons_3_7_1444880372.data";
        } else if (c.NIGHT == v) {
            str = a.BUS != w ? "icons_2_7_1445580283.data" : "icons_3_7_1444880372.data";
        }
        return str;
    }

    public void b(boolean z) {
        byte[] otherResData;
        byte[] otherResData2;
        byte[] otherResData3;
        byte[] otherResData4;
        byte[] otherResData5;
        if (this.b.v() == c.NIGHT) {
            otherResData = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tgl_n.data");
            otherResData2 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("trl_n.data");
            otherResData3 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tyl_n.data");
            otherResData4 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tbl_n.data");
            otherResData5 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tnl_n.data");
        } else {
            otherResData = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tgl_l.data");
            otherResData2 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("trl_l.data");
            otherResData3 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tyl_l.data");
            otherResData4 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tbl_l.data");
            otherResData5 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("tnl_l.data");
        }
        if (z) {
            a(otherResData, otherResData2, otherResData3, otherResData4, otherResData5);
            return;
        }
        final byte[] bArr = otherResData;
        final byte[] bArr2 = otherResData2;
        final byte[] bArr3 = otherResData3;
        final byte[] bArr4 = otherResData4;
        final byte[] bArr5 = otherResData5;
        this.b.a(new Runnable(this) {
            final /* synthetic */ i f;

            public void run() {
                this.f.a(bArr, bArr2, bArr3, bArr4, bArr5);
            }
        });
    }

    private void a(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        if (bArr != null) {
            this.d.setInternaltexture(bArr, 6);
        }
        if (bArr2 != null) {
            this.d.setInternaltexture(bArr2, 4);
        }
        if (bArr3 != null) {
            this.d.setInternaltexture(bArr3, 5);
        }
        if (bArr4 != null) {
            this.d.setInternaltexture(bArr4, 7);
        }
        if (bArr5 != null) {
            this.d.setInternaltexture(bArr5, 18);
        }
    }

    public void c(boolean z) {
        byte[] otherResData;
        byte[] otherResData2;
        if (this.b.v() == c.NIGHT) {
            otherResData = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("bktile_n.data");
            otherResData2 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("3d_sky_night.dat");
        } else {
            otherResData = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("bktile.data");
            otherResData2 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("3d_sky_day.dat");
        }
        if (z) {
            this.d.setInternaltexture(otherResData, 1);
            this.d.setInternaltexture(otherResData2, 41);
            return;
        }
        this.b.a(new Runnable(this) {
            final /* synthetic */ i c;

            public void run() {
                this.c.d.setInternaltexture(otherResData, 1);
                this.c.d.setInternaltexture(otherResData2, 41);
            }
        });
    }

    public void d(boolean z) {
    }

    public void e(boolean z) {
        final byte[] otherResData = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("roadarrow.data");
        final byte[] otherResData2 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("lineround.data");
        final byte[] otherResData3 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("dash.data");
        final byte[] otherResData4 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("dash_tq.data");
        final byte[] otherResData5 = MapTilsCacheAndResManager.getInstance(this.c).getOtherResData("dash_cd.data");
        if (z) {
            this.d.setInternaltexture(otherResData, 2);
            this.d.setInternaltexture(otherResData2, 3);
            this.d.setInternaltexture(otherResData3, 8);
            this.d.setInternaltexture(otherResData4, 9);
            this.d.setInternaltexture(otherResData5, 10);
            return;
        }
        this.b.a(new Runnable(this) {
            final /* synthetic */ i f;

            public void run() {
                this.f.d.setInternaltexture(otherResData, 2);
                this.f.d.setInternaltexture(otherResData2, 3);
                this.f.d.setInternaltexture(otherResData3, 8);
                this.f.d.setInternaltexture(otherResData4, 9);
                this.f.d.setInternaltexture(otherResData5, 10);
            }
        });
    }
}
