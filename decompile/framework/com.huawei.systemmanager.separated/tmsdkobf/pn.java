package tmsdkobf;

import java.util.Iterator;
import java.util.LinkedHashSet;

/* compiled from: Unknown */
public class pn<T> {
    private int Dw = -1;
    private LinkedHashSet<T> Io = new LinkedHashSet();

    public pn(int i) {
        this.Dw = i;
    }

    public synchronized boolean c(T t) {
        return this.Io.contains(t);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized T poll() {
        if (this.Io != null) {
            Iterator it = this.Io.iterator();
            if (it != null && it.hasNext()) {
                T next = it.next();
                this.Io.remove(next);
                return next;
            }
        }
    }

    public synchronized void push(T t) {
        if (this.Io.size() >= this.Dw) {
            poll();
        }
        this.Io.add(t);
    }
}
