package tmsdkobf;

import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class nl {
    public static final int[][] Cv;
    private static nl Cx = null;
    private nm Cw = null;
    private int mRefCount = 0;

    static {
        r0 = new int[3][];
        r0[0] = new int[]{0, 0};
        r0[1] = new int[]{1, 1};
        r0[2] = new int[]{2, 2};
        Cv = r0;
    }

    private nl() {
        d.g("QQPimSecure", "BumbleBeeImpl 00");
    }

    public static nl fn() {
        if (Cx == null) {
            synchronized (nl.class) {
                if (Cx == null) {
                    Cx = new nl();
                }
            }
        }
        return Cx;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsCheckResult b(SmsEntity smsEntity, Boolean bool) {
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                nm nmVar = this.Cw;
            } else {
                d.e("QQPimSecure", "BumbleBeeImpl checkSms mRefCount==0");
                return null;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int bK(int i) {
        int i2 = 0;
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                nm nmVar = this.Cw;
            } else {
                d.e("QQPimSecure", "BumbleBeeImpl filterResult mRefCount==0");
                return i;
            }
        }
        if (i2 == 3) {
            i = 3;
        } else if (i2 == 4) {
            i = 4;
        }
        return i;
    }

    public void fl() {
        d.g("QQPimSecure", "BumbleBeeImpl 01");
        synchronized (nl.class) {
            if (this.mRefCount <= 0) {
                this.mRefCount = 1;
                this.Cw = new nm();
                d.g("QQPimSecure", "BumbleBeeImpl 02");
                return;
            }
            this.mRefCount++;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void fm() {
        d.g("QQPimSecure", "BumbleBeeImpl 03");
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                this.mRefCount--;
                if (this.mRefCount <= 0) {
                    this.Cw.fp();
                    this.Cw = null;
                    Cx = null;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SmsCheckResult t(String str, String str2) {
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                nm nmVar = this.Cw;
            } else {
                d.e("QQPimSecure", "BumbleBeeImpl isPaySms mRefCount==0");
                return null;
            }
        }
    }
}
