package tmsdkobf;

/* compiled from: Unknown */
public class fy extends py implements Cloneable {
    private int downloadCount = 0;
    private String eD = "";
    private int jY = 0;
    private int kz = 0;
    private String nA = "";
    private int nB;
    private int nC = -1;
    private String nD = "";
    private int nE = -1;
    private int nF = 0;
    private int nG = -1;
    private boolean nH = false;
    private String nI = null;
    private String nJ = "";
    private String nK = "";
    private String nL = "";
    private int nM = 0;
    private int nN = 0;
    private String nO = "";
    private long nP = 0;
    private String nQ = "";
    private String nR = "";
    private int nS = 0;
    private int nT = 0;
    private int nU = 0;
    private String nV = "";
    private int nW = 0;
    private int nt = -1;
    private String nu = "";
    private float nv = 0.0f;
    private String nw = "";
    private long nx = 0;
    private boolean ny = false;
    private int nz = -1;

    public int al() {
        return this.nt;
    }

    public boolean am() {
        return this.ny;
    }

    public void an(int i) {
        this.nt = i;
    }

    public void ao(int i) {
        this.nB = i;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        boolean z2 = false;
        if (obj != null && (obj instanceof fy)) {
            fy fyVar = (fy) obj;
            if (hG()) {
                if (!(fyVar.aZ() == null || aZ() == null)) {
                    if (aZ().toLowerCase().hashCode() == fyVar.aZ().toLowerCase().hashCode()) {
                        z2 = true;
                    }
                    return z2;
                }
            } else if (!(getPackageName() == null || fyVar.getPackageName() == null)) {
                if (getPackageName().hashCode() == fyVar.getPackageName().hashCode()) {
                    if (this.ny != fyVar.am()) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
        }
        return false;
    }

    public void l(int i) {
        super.l(i);
        ao(i);
    }
}
