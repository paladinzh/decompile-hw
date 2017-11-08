package defpackage;

import java.io.InputStream;

/* renamed from: ab */
class ab extends InputStream {
    private InputStream aD;
    final /* synthetic */ aa aE;
    private byte[] buff = null;
    private int currIndex = 0;

    public ab(aa aaVar, InputStream inputStream) {
        this.aE = aaVar;
        this.aD = inputStream;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read() {
        synchronized (this.aE) {
            if (!this.aE.isInitialized) {
                aw.e("PushLog2841", "secure socket is not initialized, can not read any data");
                return -1;
            }
        }
    }
}
