package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class eh extends fs {
    static ei kC;
    static ArrayList<dp> kD;
    static ArrayList<String> kE;
    static ArrayList<cy> kF;
    static ArrayList<String> kG;
    static ArrayList<String> kH;
    static byte[] kI;
    public String description = "";
    public String fU = "";
    public ei jH = null;
    public String jI = "";
    public ArrayList<dp> jJ = null;
    public int jK = 0;
    public int jL = 0;
    public int jM = 0;
    public int jN = 0;
    public int jO = 0;
    public String jP = "";
    public int jQ = 0;
    public String jR = "";
    public int jS = 0;
    public String jT = "";
    public int jU = 0;
    public String jV = "";
    public String jW = "";
    public ArrayList<String> jX = null;
    public int jY = 0;
    public int jZ = 0;
    public int jv = 0;
    public int kA = 0;
    public String kB = "";
    public int ka = 0;
    public int kb = 0;
    public int kc = 0;
    public int kd = 0;
    public float ke = 0.0f;
    public String kf = "";
    public String kg = "";
    public ArrayList<cy> kh = null;
    public ArrayList<String> ki = null;
    public String kj = "";
    public float kk = 0.0f;
    public int kl = 0;
    public float km = 0.0f;
    public float kn = 0.0f;
    public ArrayList<String> ko = null;
    public byte[] kp = null;
    public String kq = "";
    public String kr = "";
    public String ks = "";
    public String kt = "";
    public String ku = "";
    public String kv = "";
    public String kw = "";
    public long kx = 0;
    public String ky = "";
    public int kz = 0;
    public int official = 0;
    public int score = 0;
    public String type = "";

    public void readFrom(fq fqVar) {
        if (kC == null) {
            kC = new ei();
        }
        this.jH = (ei) fqVar.a(kC, 0, true);
        this.fU = fqVar.a(1, false);
        this.description = fqVar.a(2, false);
        this.type = fqVar.a(3, false);
        this.jI = fqVar.a(4, false);
        if (kD == null) {
            kD = new ArrayList();
            kD.add(new dp());
        }
        this.jJ = (ArrayList) fqVar.b(kD, 5, false);
        this.jK = fqVar.a(this.jK, 6, false);
        this.jL = fqVar.a(this.jL, 7, false);
        this.jM = fqVar.a(this.jM, 8, false);
        this.jN = fqVar.a(this.jN, 9, false);
        this.jO = fqVar.a(this.jO, 10, false);
        this.jP = fqVar.a(11, false);
        this.jQ = fqVar.a(this.jQ, 12, false);
        this.jR = fqVar.a(13, false);
        this.jS = fqVar.a(this.jS, 14, false);
        this.jT = fqVar.a(15, false);
        this.jU = fqVar.a(this.jU, 16, false);
        this.jV = fqVar.a(17, false);
        this.jW = fqVar.a(18, false);
        if (kE == null) {
            kE = new ArrayList();
            kE.add("");
        }
        this.jX = (ArrayList) fqVar.b(kE, 19, false);
        this.official = fqVar.a(this.official, 20, false);
        this.jY = fqVar.a(this.jY, 21, false);
        this.score = fqVar.a(this.score, 22, false);
        this.jZ = fqVar.a(this.jZ, 23, false);
        this.jv = fqVar.a(this.jv, 24, false);
        this.ka = fqVar.a(this.ka, 25, false);
        this.kb = fqVar.a(this.kb, 26, false);
        this.kc = fqVar.a(this.kc, 27, false);
        this.kd = fqVar.a(this.kd, 28, false);
        this.ke = fqVar.a(this.ke, 29, false);
        this.kf = fqVar.a(30, false);
        this.kg = fqVar.a(31, false);
        if (kF == null) {
            kF = new ArrayList();
            kF.add(new cy());
        }
        this.kh = (ArrayList) fqVar.b(kF, 32, false);
        if (kG == null) {
            kG = new ArrayList();
            kG.add("");
        }
        this.ki = (ArrayList) fqVar.b(kG, 33, false);
        this.kj = fqVar.a(34, false);
        this.kk = fqVar.a(this.kk, 35, false);
        this.kl = fqVar.a(this.kl, 36, false);
        this.km = fqVar.a(this.km, 37, false);
        this.kn = fqVar.a(this.kn, 38, false);
        if (kH == null) {
            kH = new ArrayList();
            kH.add("");
        }
        this.ko = (ArrayList) fqVar.b(kH, 39, false);
        if (kI == null) {
            kI = new byte[1];
            kI[0] = (byte) 0;
        }
        this.kp = fqVar.a(kI, 40, false);
        this.kq = fqVar.a(41, false);
        this.kr = fqVar.a(42, false);
        this.ks = fqVar.a(43, false);
        this.kt = fqVar.a(44, false);
        this.ku = fqVar.a(45, false);
        this.kv = fqVar.a(46, false);
        this.kw = fqVar.a(47, false);
        this.kx = fqVar.a(this.kx, 48, false);
        this.ky = fqVar.a(49, false);
        this.kz = fqVar.a(this.kz, 50, false);
        this.kA = fqVar.a(this.kA, 51, false);
        this.kB = fqVar.a(52, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.jH, 0);
        if (this.fU != null) {
            frVar.a(this.fU, 1);
        }
        if (this.description != null) {
            frVar.a(this.description, 2);
        }
        if (this.type != null) {
            frVar.a(this.type, 3);
        }
        if (this.jI != null) {
            frVar.a(this.jI, 4);
        }
        if (this.jJ != null) {
            frVar.a(this.jJ, 5);
        }
        frVar.write(this.jK, 6);
        frVar.write(this.jL, 7);
        frVar.write(this.jM, 8);
        frVar.write(this.jN, 9);
        frVar.write(this.jO, 10);
        if (this.jP != null) {
            frVar.a(this.jP, 11);
        }
        frVar.write(this.jQ, 12);
        if (this.jR != null) {
            frVar.a(this.jR, 13);
        }
        frVar.write(this.jS, 14);
        if (this.jT != null) {
            frVar.a(this.jT, 15);
        }
        frVar.write(this.jU, 16);
        if (this.jV != null) {
            frVar.a(this.jV, 17);
        }
        if (this.jW != null) {
            frVar.a(this.jW, 18);
        }
        if (this.jX != null) {
            frVar.a(this.jX, 19);
        }
        frVar.write(this.official, 20);
        frVar.write(this.jY, 21);
        frVar.write(this.score, 22);
        frVar.write(this.jZ, 23);
        frVar.write(this.jv, 24);
        frVar.write(this.ka, 25);
        frVar.write(this.kb, 26);
        frVar.write(this.kc, 27);
        frVar.write(this.kd, 28);
        frVar.a(this.ke, 29);
        if (this.kf != null) {
            frVar.a(this.kf, 30);
        }
        if (this.kg != null) {
            frVar.a(this.kg, 31);
        }
        if (this.kh != null) {
            frVar.a(this.kh, 32);
        }
        if (this.ki != null) {
            frVar.a(this.ki, 33);
        }
        if (this.kj != null) {
            frVar.a(this.kj, 34);
        }
        frVar.a(this.kk, 35);
        frVar.write(this.kl, 36);
        frVar.a(this.km, 37);
        frVar.a(this.kn, 38);
        if (this.ko != null) {
            frVar.a(this.ko, 39);
        }
        if (this.kp != null) {
            frVar.a(this.kp, 40);
        }
        if (this.kq != null) {
            frVar.a(this.kq, 41);
        }
        if (this.kr != null) {
            frVar.a(this.kr, 42);
        }
        if (this.ks != null) {
            frVar.a(this.ks, 43);
        }
        if (this.kt != null) {
            frVar.a(this.kt, 44);
        }
        if (this.ku != null) {
            frVar.a(this.ku, 45);
        }
        if (this.kv != null) {
            frVar.a(this.kv, 46);
        }
        if (this.kw != null) {
            frVar.a(this.kw, 47);
        }
        frVar.b(this.kx, 48);
        if (this.ky != null) {
            frVar.a(this.ky, 49);
        }
        frVar.write(this.kz, 50);
        frVar.write(this.kA, 51);
        if (this.kB != null) {
            frVar.a(this.kB, 52);
        }
    }
}
