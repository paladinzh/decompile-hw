package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public abstract class rh<T> {
    protected List<T> NE;
    protected int NF;
    private final String TAG = "BaseScanTask";
    protected int pE = 0;

    public rh(List<T> list, int i) {
        this.NE = new ArrayList(list);
        this.NF = i;
    }

    public void bf() {
        if (jC()) {
            d.e("BaseScanTask", "scanCancel");
            bn();
            bl();
        }
    }

    abstract void bl();

    protected void bn() {
        d.e("BaseScanTask", "onScanCancel");
        if (!jE()) {
            cG(2);
        }
    }

    public void cG(int i) {
        this.pE = i;
    }

    public boolean jC() {
        return this.pE == 1;
    }

    public boolean jD() {
        return this.pE == 2 || this.pE == 7;
    }

    public boolean jE() {
        return this.pE == 7;
    }
}
