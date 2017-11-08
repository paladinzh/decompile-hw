package tmsdk.common;

import android.content.Context;
import android.content.Intent;
import tmsdkobf.jj;
import tmsdkobf.jq;
import tmsdkobf.mn;

/* compiled from: Unknown */
public abstract class TMSBootReceiver extends jj {

    /* compiled from: Unknown */
    private static final class a {
        private static final short[] Af = new short[]{(short) 64, (short) 75, (short) 72, (short) 8, (short) 86, (short) 65, (short) 65, (short) 69, (short) 68, (short) 31, (short) 27, (short) 30, (short) 1, (short) 93, (short) 94, (short) 80, (short) 90, (short) 88, (short) 80, (short) 69, (short) 86, (short) 94, (short) 92};

        private a() {
        }

        private String a(short[] sArr) {
            StringBuffer stringBuffer = new StringBuffer();
            short[] b = b(sArr);
            for (short s : b) {
                stringBuffer.append((char) s);
            }
            return stringBuffer.toString();
        }

        private short[] b(short[] sArr) {
            short[] sArr2 = new short[sArr.length];
            int i = 35;
            int i2 = 0;
            while (i2 < sArr.length) {
                sArr2[i2] = (short) ((short) (sArr[i2] ^ i));
                i2++;
                i = (char) (i + 1);
            }
            return sArr2;
        }

        public boolean j(Context context) {
            return TMServiceFactory.getSystemInfoService().aC(a(Af));
        }
    }

    public void doOnRecv(final Context context, Intent intent) {
        jq.ct().c(new Runnable(this) {
            final /* synthetic */ TMSBootReceiver Ae;

            public void run() {
                int i = 0;
                mn mnVar = new mn();
                mnVar.r(0, (int) (System.currentTimeMillis() / 1000));
                if (!new a().j(context)) {
                    i = 1;
                }
                mnVar.r(1, i);
                mnVar.commit();
            }
        }, "TMSBootReceiveThread").start();
    }
}
