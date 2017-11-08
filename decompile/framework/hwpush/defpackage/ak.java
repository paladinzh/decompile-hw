package defpackage;

import android.text.TextUtils;

/* renamed from: ak */
public class ak implements Comparable {
    private long bk;
    private boolean bl;

    private ak() {
    }

    public int a(ak akVar) {
        return (int) ((bF() - akVar.bF()) / 1000);
    }

    public long bF() {
        return this.bk;
    }

    public boolean bG() {
        return this.bl;
    }

    public /* synthetic */ int compareTo(Object obj) {
        return a((ak) obj);
    }

    public boolean equals(Object obj) {
        return this == obj ? true : obj == null ? false : getClass() != obj.getClass() ? false : !(obj instanceof ak) ? false : this.bl == ((ak) obj).bl && this.bk == ((ak) obj).bk;
    }

    public int hashCode() {
        return (this.bl ? 1 : 0) + ((((int) (this.bk ^ (this.bk >>> 32))) + 527) * 31);
    }

    public void j(long j) {
        this.bk = j;
    }

    public void j(boolean z) {
        this.bl = z;
    }

    public boolean load(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            String[] split = str.split(";");
            if (split == null || split.length < 2) {
                aw.e("PushLog2841", "load connectinfo " + str + " error");
                return false;
            }
            this.bk = Long.parseLong(split[0]);
            this.bl = Boolean.parseBoolean(split[1]);
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2841", "load connectinfo " + str + " error:" + e.toString(), e);
            return false;
        }
    }

    public String toString() {
        if (this.bk <= 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.bk).append(";").append(this.bl);
        return stringBuffer.toString();
    }
}
