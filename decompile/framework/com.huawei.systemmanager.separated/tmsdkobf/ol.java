package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.d;
import tmsdkobf.pb.b;

/* compiled from: Unknown */
public class ol {
    private boolean EA = false;
    private pb Ew;
    private volatile boolean Ex = false;
    private volatile String Ey;
    private volatile long Ez = 0;
    private final String TAG = "GuidCertifier";
    private Context mContext;

    /* compiled from: Unknown */
    public interface a {
        void c(boolean z, String str);
    }

    public ol(Context context, pb pbVar) {
        this.mContext = context;
        this.Ew = pbVar;
        if (this.Ew.gm().aF() == this.EA) {
            pa.b("ocean", "[ocean]common: guid is ok", null, null);
        } else {
            pa.b("ocean", "[ocean]common: clean guid", null, null);
            this.Ew.gm().az("");
            this.Ew.gm().aA("");
        }
        fM();
    }

    private bj H(boolean z) {
        if (!fN() && !z) {
            return null;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (!mp.a(currentTimeMillis, this.Ew.gm().az(), 720) && !z) {
            return null;
        }
        this.Ew.gm().h(currentTimeMillis);
        bj fO = fO();
        bj aw = this.Ew.gm().aw();
        if (fO == null || aw == null) {
            d.c("GuidCertifier", "needUpdateInfoOfGuid() null == realInfo || null == savedInfo");
            return null;
        }
        d.d("GuidCertifier", "needUpdateInfoOfGuid() printCSRegist() savedInfo: ");
        d(aw);
        return (c(fO.di, aw.di) | ((((((((((((((((((((((((((((((((((((((((((((((((((v(fO.cC, aw.cC) | 0) | v(fO.imsi, aw.imsi)) | v(fO.dk, aw.dk)) | v(fO.cD, aw.cD)) | v(fO.cE, aw.cE)) | v(fO.cF, aw.cF)) | s(fO.product, aw.product)) | v(fO.cG, aw.cG)) | s(fO.u, aw.u)) | v(fO.cH, aw.cH)) | s(fO.cI, aw.cI)) | s(fO.cJ, aw.cJ)) | c(fO.cK, aw.cK)) | v(fO.cL, aw.cL)) | v(fO.cM, aw.cM)) | s(fO.cN, aw.cN)) | v(fO.cO, aw.cO)) | s(fO.cP, aw.cP)) | s(fO.cQ, aw.cQ)) | v(fO.cR, aw.cR)) | v(fO.dt, aw.dt)) | v(fO.cS, aw.cS)) | s(fO.cT, aw.cT)) | v(fO.cU, aw.cU)) | c(fO.cV, aw.cV)) | c(fO.cW, aw.cW)) | c(fO.cX, aw.cX)) | c(fO.dy, aw.dy)) | v(fO.cY, aw.cY)) | v(fO.cZ, aw.cZ)) | v(fO.da, aw.da)) | v(fO.version, aw.version)) | s(fO.do, aw.do)) | v(fO.dp, aw.dp)) | v(fO.dd, aw.dd)) | s(fO.dg, aw.dg)) | s(fO.dh, aw.dh)) | v(fO.dq, aw.dq)) | v(fO.dr, aw.dr)) | v(fO.ds, aw.ds)) | v(fO.du, aw.du)) | v(fO.dv, aw.dv)) | v(fO.dw, aw.dw)) | v(fO.dx, aw.dx)) | v(fO.de, aw.de)) | v(fO.dz, aw.dz)) | v(fO.df, aw.df)) | v(fO.db, aw.db)) | v(fO.dc, aw.dc)) | v(fO.dA, aw.dA))) != 0 ? fO : null;
    }

    private void a(String str, bj bjVar) {
        if (!TextUtils.isEmpty(str)) {
            d.d("GuidCertifier", "saveGuid:[" + str + "]");
            this.Ey = str;
            this.Ex = true;
            this.Ew.gm().o(this.EA);
            this.Ew.gm().az(str);
            this.Ew.gm().aA(str);
            this.Ew.gm().b(bjVar);
        }
    }

    private void c(final bj bjVar) {
        d.e("GuidCertifier", "updateGuid() mGuid: " + this.Ey);
        d.e("GuidCertifier", "updateGuid() encodeKey: " + this.Ew.gg());
        fs e = e(bjVar);
        bm bmVar = new bm();
        bmVar.dG = oz.gi().fP();
        bmVar.aZ = 2;
        bmVar.data = ok.c(e);
        if (bmVar.data != null && bmVar.data.length > 0) {
            d.d("GuidCertifier", "updateGuid() printCSRegist()");
            d(e.dC);
            ArrayList arrayList = new ArrayList();
            arrayList.add(bmVar);
            this.Ew.a(0, false, arrayList, new b(this) {
                final /* synthetic */ ol ED;

                public void a(boolean z, int i, int i2, ArrayList<bq> arrayList) {
                    d.e("GuidCertifier", "updateGuid() retCode: " + i);
                    if (i != 0) {
                        d.c("GuidCertifier", "updateGuid() ESharkCode.ERR_NONE != retCode, retCode: " + i);
                    } else if (arrayList != null && arrayList.size() > 0) {
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            bq bqVar = (bq) it.next();
                            if (bqVar == null) {
                                return;
                            }
                            if (10002 == bqVar.aZ) {
                                d.e("GuidCertifier", "updateGuid() rs.seqNo: " + bqVar.dG + "rs.cmd" + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                                if (1 != bqVar.dJ && bqVar.dJ == 0) {
                                    d.d("GuidCertifier", "updateGuid() succed, mGuid: " + this.ED.Ey);
                                    this.ED.a(this.ED.Ey, bjVar);
                                } else {
                                    return;
                                }
                            }
                            d.e("GuidCertifier", "updateGuid() rs.cmd: " + bqVar.aZ);
                        }
                    }
                }
            });
            return;
        }
        d.c("GuidCertifier", "updateGuid() data:[" + bmVar.data + "]");
    }

    private boolean c(long j, long j2) {
        return j != j2;
    }

    private boolean c(boolean z, boolean z2) {
        return z != z2;
    }

    private void d(bj bjVar) {
        d.d("GuidCertifier", "printCSRegist()CSRegist.imei: " + bjVar.cC);
        d.d("GuidCertifier", "printCSRegist()CSRegist.imsi: " + bjVar.imsi);
        d.d("GuidCertifier", "printCSRegist()CSRegist.imsi_2: " + bjVar.dk);
        d.d("GuidCertifier", "printCSRegist()CSRegist.mac: " + bjVar.cD);
        d.d("GuidCertifier", "printCSRegist()CSRegist.qq: " + bjVar.cE);
        d.d("GuidCertifier", "printCSRegist()CSRegist.phone: " + bjVar.cF);
        d.d("GuidCertifier", "printCSRegist()CSRegist.product: " + bjVar.product);
        d.d("GuidCertifier", "printCSRegist()CSRegist.lc: " + bjVar.cG);
        d.d("GuidCertifier", "printCSRegist()CSRegist.buildno: " + bjVar.u);
        d.d("GuidCertifier", "printCSRegist()CSRegist.channelid: " + bjVar.cH);
        d.d("GuidCertifier", "printCSRegist()CSRegist.platform: " + bjVar.cI);
        d.d("GuidCertifier", "printCSRegist()CSRegist.subplatform: " + bjVar.cJ);
        d.d("GuidCertifier", "printCSRegist()CSRegist.isbuildin: " + bjVar.cK);
        d.d("GuidCertifier", "printCSRegist()CSRegist.pkgname: " + bjVar.cL);
        d.d("GuidCertifier", "printCSRegist()CSRegist.ua: " + bjVar.cM);
        d.d("GuidCertifier", "printCSRegist()CSRegist.sdkver: " + bjVar.cN);
        d.d("GuidCertifier", "printCSRegist()CSRegist.androidid: " + bjVar.cO);
        d.d("GuidCertifier", "printCSRegist()CSRegist.lang: " + bjVar.cP);
        d.d("GuidCertifier", "printCSRegist()CSRegist.simnum: " + bjVar.cQ);
        d.d("GuidCertifier", "printCSRegist()CSRegist.cpu: " + bjVar.cR);
        d.d("GuidCertifier", "printCSRegist()CSRegist.cpu_abi2: " + bjVar.dt);
        d.d("GuidCertifier", "printCSRegist()CSRegist.cpufreq: " + bjVar.cS);
        d.d("GuidCertifier", "printCSRegist()CSRegist.cpunum: " + bjVar.cT);
        d.d("GuidCertifier", "printCSRegist()CSRegist.resolution: " + bjVar.cU);
        d.d("GuidCertifier", "printCSRegist()CSRegist.ram: " + bjVar.cV);
        d.d("GuidCertifier", "printCSRegist()CSRegist.rom: " + bjVar.cW);
        d.d("GuidCertifier", "printCSRegist()CSRegist.sdcard: " + bjVar.cX);
        d.d("GuidCertifier", "printCSRegist()CSRegist.inner_storage: " + bjVar.dy);
        d.d("GuidCertifier", "printCSRegist()CSRegist.build_brand: " + bjVar.cY);
        d.d("GuidCertifier", "printCSRegist()CSRegist.build_version_incremental: " + bjVar.cZ);
        d.d("GuidCertifier", "printCSRegist()CSRegist.build_version_release: " + bjVar.da);
        d.d("GuidCertifier", "printCSRegist()CSRegist.version: " + bjVar.version);
        d.d("GuidCertifier", "printCSRegist()CSRegist.extSdkVer: " + bjVar.do);
        d.d("GuidCertifier", "printCSRegist()CSRegist.pkgkey: " + bjVar.dp);
        d.d("GuidCertifier", "printCSRegist()CSRegist.manufactory: " + bjVar.dd);
        d.d("GuidCertifier", "printCSRegist()CSRegist.cam_pix: " + bjVar.dg);
        d.d("GuidCertifier", "printCSRegist()CSRegist.front_cam_pix: " + bjVar.dh);
        d.d("GuidCertifier", "printCSRegist()CSRegist.product_device: " + bjVar.dq);
        d.d("GuidCertifier", "printCSRegist()CSRegist.product_board: " + bjVar.dr);
        d.d("GuidCertifier", "printCSRegist()CSRegist.build_product: " + bjVar.ds);
        d.d("GuidCertifier", "printCSRegist()CSRegist.rom_fingerprint: " + bjVar.du);
        d.d("GuidCertifier", "printCSRegist()CSRegist.product_lanuage: " + bjVar.dv);
        d.d("GuidCertifier", "printCSRegist()CSRegist.product_region: " + bjVar.dw);
        d.d("GuidCertifier", "printCSRegist()CSRegist.build_radiover: " + bjVar.dx);
        d.d("GuidCertifier", "printCSRegist()CSRegist.board_platform: " + bjVar.de);
        d.d("GuidCertifier", "printCSRegist()CSRegist.board_platform_mtk: " + bjVar.dz);
        d.d("GuidCertifier", "printCSRegist()CSRegist.screen_pdi: " + bjVar.df);
        d.d("GuidCertifier", "printCSRegist()CSRegist.romname: " + bjVar.db);
        d.d("GuidCertifier", "printCSRegist()CSRegist.romversion: " + bjVar.dc);
        d.d("GuidCertifier", "printCSRegist()CSRegist.kernel_ver: " + bjVar.dA);
        d.d("GuidCertifier", "printCSRegist()CSRegist.isdual: " + bjVar.di);
    }

    private bk e(bj bjVar) {
        bk bkVar = new bk();
        bkVar.dC = bjVar;
        bkVar.dD = c();
        return bkVar;
    }

    private boolean fN() {
        long currentTimeMillis = System.currentTimeMillis();
        if (!mp.a(currentTimeMillis, this.Ez, 60)) {
            return false;
        }
        this.Ez = currentTimeMillis;
        return true;
    }

    private bj fO() {
        bj ay = this.Ew.gm().ay();
        if (ay != null) {
            if (ay.cC == null) {
                ay.cC = "";
            }
            return ay;
        }
        throw new RuntimeException("reqRegist is null");
    }

    private boolean s(int i, int i2) {
        return i != i2;
    }

    private boolean v(String str, String str2) {
        boolean z = false;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        if (TextUtils.isEmpty(str2)) {
            return true;
        }
        if (!str.equals(str2)) {
            z = true;
        }
        return z;
    }

    public void G(boolean z) {
        boolean z2 = false;
        if (!fL()) {
            bj H = H(z);
            String str = "GuidCertifier";
            StringBuilder append = new StringBuilder().append("checUpdateGuid() forceCheck: ").append(z).append(" need: ");
            if (H != null) {
                z2 = true;
            }
            d.e(str, append.append(z2).toString());
            if (H != null) {
                c(H);
            }
        }
    }

    public void a(final a aVar) {
        d.e("GuidCertifier", "checkGuid()");
        if (fL()) {
            d.e("GuidCertifier", "checkGuid() encodeKey: " + this.Ew.gg());
            final fs fO = fO();
            bm bmVar = new bm();
            bmVar.dG = oz.gi().fP();
            bmVar.aZ = 1;
            bmVar.data = ok.c(fO);
            if (bmVar.data != null && bmVar.data.length > 0) {
                d.d("GuidCertifier", "checkGuid() printCSRegist()");
                d(fO);
                ArrayList arrayList = new ArrayList();
                arrayList.add(bmVar);
                this.Ew.b(arrayList, new b(this) {
                    final /* synthetic */ ol ED;

                    public void a(boolean z, int i, int i2, ArrayList<bq> arrayList) {
                        if (i != 0) {
                            d.c("GuidCertifier", "checkGuid() ESharkCode.ERR_NONE != retCode, retCode: " + i);
                            aVar.c(false, null);
                        } else if (arrayList == null) {
                            aVar.c(false, null);
                        } else if (arrayList.size() > 0) {
                            bq bqVar = (bq) arrayList.get(0);
                            if (bqVar != null) {
                                d.e("GuidCertifier", "checkGuid() rs.seqNo: " + bqVar.dG + " rs.cmd: " + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                                if (1 == bqVar.dJ) {
                                    d.c("GuidCertifier", "checkGuid() 接入层失败了");
                                    aVar.c(false, null);
                                    return;
                                } else if (bqVar.dK == 0) {
                                    byte[] bArr = bqVar.data;
                                    if (bArr != null) {
                                        d.e("GuidCertifier", "checkGuid() rs.data.length: " + bqVar.data.length);
                                        try {
                                            fs a = ok.a(this.ED.mContext, this.ED.Ew.gg().Ft.getBytes(), bArr, new bo(), false);
                                            if (a != null) {
                                                this.ED.a(((bo) a).r, fO);
                                                d.d("GuidCertifier", "checkGuid() ret.guid mGuid: " + this.ED.Ey);
                                                aVar.c(true, null);
                                                return;
                                            }
                                            d.c("GuidCertifier", "checkGuid() null == js");
                                            aVar.c(false, null);
                                            return;
                                        } catch (Exception e) {
                                            d.c("GuidCertifier", "checkGuid() convert failed");
                                            aVar.c(false, null);
                                            return;
                                        }
                                    }
                                    aVar.c(false, null);
                                    return;
                                } else {
                                    d.c("GuidCertifier", "checkGuid() 业务层失败了");
                                    aVar.c(false, null);
                                    return;
                                }
                            }
                            aVar.c(false, null);
                        } else {
                            aVar.c(false, null);
                        }
                    }
                });
                return;
            }
            d.c("GuidCertifier", "checkGuid() data:[" + bmVar.data + "]");
            return;
        }
        d.e("GuidCertifier", "checkGuid() !need, mGuid: " + this.Ey);
    }

    public String c() {
        return this.Ey != null ? this.Ey : "";
    }

    public boolean fL() {
        if (TextUtils.isEmpty(c()) || !this.Ex) {
            return true;
        }
        d.d("GuidCertifier", "checkDoRegist() 已经注册过.");
        return false;
    }

    public void fM() {
        d.d("GuidCertifier", "refreshGuid()");
        this.Ey = this.Ew.gm().as();
        if (TextUtils.isEmpty(this.Ey)) {
            this.Ex = false;
        } else {
            this.Ex = true;
        }
    }
}
