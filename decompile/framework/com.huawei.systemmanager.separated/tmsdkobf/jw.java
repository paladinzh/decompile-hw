package tmsdkobf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.b;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public final class jw {
    private static final byte[] uB = new byte[]{(byte) 99, (byte) 111, (byte) 109, (byte) 46, (byte) 116, (byte) 101, (byte) 110, (byte) 99, (byte) 101, (byte) 110, (byte) 116, (byte) 46, (byte) 113, (byte) 113, (byte) 112, (byte) 105, (byte) 109, (byte) 115, (byte) 101, (byte) 99, (byte) 117, (byte) 114, (byte) 101};
    private static volatile jw uC = null;
    public static String uF;
    private jt uD;
    private Calendar uE = Calendar.getInstance();
    private jv uG;

    private jw() {
        load();
    }

    private static long a(String str, RSAPublicKey rSAPublicKey) throws Exception {
        byte[] decode = b.decode(str, 0);
        int i = ByteBuffer.wrap(decode).getInt();
        Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        instance.init(2, rSAPublicKey);
        byte[] doFinal = instance.doFinal(decode, 4, i);
        Cipher instance2 = Cipher.getInstance("DES/ECB/PKCS5Padding");
        instance2.init(2, SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(doFinal)));
        return Long.parseLong(new String(instance2.doFinal(decode, i + 4, (decode.length - 4) - i)), 16);
    }

    private static byte[] a(byte[] bArr, byte[] bArr2) {
        try {
            Key generateSecret = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(bArr2));
            Cipher instance = Cipher.getInstance("DES/ECB/NoPadding");
            instance.init(2, generateSecret);
            return instance.doFinal(bArr);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] aY(int i) {
        InputStream inputStream = null;
        try {
            inputStream = TMSDKContext.getApplicaionContext().getAssets().open(i != 0 ? "licence" + i + ".conf" : "licence.conf");
            byte[] bArr = new byte[inputStream.available()];
            inputStream.read(bArr);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bArr;
        } catch (Throwable e2) {
            throw new RuntimeException(e2);
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }

    public static synchronized jw cH() {
        jw jwVar;
        synchronized (jw.class) {
            if (uC == null) {
                uC = new jw();
            }
            jwVar = uC;
        }
        return jwVar;
    }

    private boolean cI() {
        try {
            String cF = this.uG.cF();
            int parseInt = Integer.parseInt(this.uG.cE());
            if (dc.hn.toString().equals(cF) && parseInt == 999001) {
                return true;
            }
        } catch (Throwable e) {
            d.a("TMSLicenceManager", "isQQPimSecure", e);
        }
        return false;
    }

    private final void cK() {
        long a;
        int i;
        Calendar instance;
        boolean z = false;
        String string = new nc("licence").getString("expiry.enc_seconds", null);
        if (string != null) {
            try {
                a = a(string, cL());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (a == -1) {
                a = this.uG.cG();
            }
            if (System.currentTimeMillis() / 1000 >= a) {
                i = 1;
            } else {
                boolean z2 = false;
            }
            if (i == 0) {
                z = true;
            }
            instance = Calendar.getInstance();
            instance.setTimeInMillis(a * 1000);
            d.e("LicMan", "expirySeconds=" + a + "(" + instance.get(1) + "-" + instance.get(2) + "-" + instance.get(5) + ") expired=" + z);
            this.uD = new jt(z);
        }
        a = -1;
        if (a == -1) {
            a = this.uG.cG();
        }
        if (System.currentTimeMillis() / 1000 >= a) {
            boolean z22 = false;
        } else {
            i = 1;
        }
        if (i == 0) {
            z = true;
        }
        instance = Calendar.getInstance();
        instance.setTimeInMillis(a * 1000);
        d.e("LicMan", "expirySeconds=" + a + "(" + instance.get(1) + "-" + instance.get(2) + "-" + instance.get(5) + ") expired=" + z);
        this.uD = new jt(z);
    }

    private static RSAPublicKey cL() {
        return ju.h(b.decode("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM5ekNDQWQ4Q0NRRGlsbUFjTWxiczVEQU5C\nZ2txaGtpRzl3MEJBUVVGQURCK01Rc3dDUVlEVlFRR0V3SkQKVGpFTE1Ba0dBMVVFQ0JNQ1IwUXhD\nekFKQmdOVkJBY1RBa2RhTVJJd0VBWURWUVFLRkFsMFpXTUlibU5sYm5ReApDekFKQmdOVkJBc1RB\nak5ITVE0d0RBWURWUVFERXdWdlltRnRZVEVrTUNJR0NTcUdTSWIzRFFFSkFSWVZiMkpoCmJXRjZa\nVzVuUUhSbGJtTmxiblF1WTI5dE1CNFhEVEV4TVRFeE5qRXhNVGN4TjFvWERURXlNREl5TkRFeE1U\nY3gKTjFvd2dZQXhDekFKQmdOVkJBWVRBa05PTVFzd0NRWURWUVFJRXdKSFJERUxNQWtHQTFVRUJ4\nTUNSMW94RURBTwpCZ05WQkFvVEIzUmxibU5sYm5ReEN6QUpCZ05WQkFzVEFqTkhNUkl3RUFZRFZR\nUURFd2x2WW1GdFlYcGxibWN4CkpEQWlCZ2txaGtpRzl3MEJDUUVXRlc5aVlXMWhlbVZ1WjBCMFpX\nNWpaVzUwTG1OdmJUQ0JuekFOQmdrcWhraUcKOXcwQkFRRUZBQU9CalFBd2dZa0NnWUVBd1kvV3FI\nV2NlRERkSm16anI3TlpSeS9qTllwS1NzVzExZngxaTIrQwpxTUE3NTJXb1d1bDZuSTB1MGZkWitk\nUzVUandRNkU0Qm13dXduVTVnQmJYK1VzQ2VHRHZaQVhQc045UEVWYnZTCkcvR25YclQrcTI2VUpP\nNHcrd3VNdmk5YWxkZHhhbkNKeXJ2ZWQ2NUdvMXhXUEErWGNHaVQxMndubjZtUHhyMnUKcVEwQ0F3\nRUFBVEFOQmdrcWhraUc5dzBCQVFVRkFBT0NBUUVBblpzV3FpSmV5SC9sT0prSWN6L2ZidDN3MXFL\nRApGTXJ5cFVHVFN6Z3NONWNaMW9yOGlvVG5ENGRLaDdJN2ttbDRpcGNvMDF0enc2MGhLYUtwNG9G\nMnYrMEs2NGZDCnBEMG9EUlkrOGoyK2RsMmNxeHBsT0FYdDc1RWFKNW40MG1DZDdTN0VBS0d2Z2Na\naVhyV0Z1eUtCL2QvNTh3Qm4KOEFGUVJhTnBySXNOSHpxMkMwL0JXR1pTSnJicmhOWExFY0ZtL0Ru\nTG14ZEVNYWxPSXhnSkhGcEFOS2tadXBzdgo0L0lDVFhSL0RJaURjbXJjbDFkNkc2VmgyaUcwaS9v\nRDBHQnBMZlFPcEF0Vmx6Y2lxZnBsTkphcnpRUTZUVXRyCm5GRmVNVDNDc2t5VGJwYnp1R2dDdUxj\nQVR3cnRQd1BOOWZzQXYrSjRJZm0rZUNVVDVnZlorMSsyNHc9PQotLS0tLUVORCBDRVJUSUZJQ0FU\nRS0tLS0tCg==\n".getBytes(), 0));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void load() {
        Properties properties;
        uF = new String(uB);
        int i = 0;
        while (true) {
            try {
                Object aY = aY(i);
                if (aY != null) {
                    RSAPublicKey cL = cL();
                    Object obj = new byte[128];
                    System.arraycopy(aY, 0, obj, 0, obj.length);
                    byte[] a = ju.a(obj, cL);
                    if (a != null) {
                        byte[] bArr = new byte[(aY.length - 128)];
                        System.arraycopy(aY, 128, bArr, 0, bArr.length);
                        byte[] a2 = a(bArr, a);
                        if (a2 != null) {
                            InputStream byteArrayInputStream = new ByteArrayInputStream(a2);
                            properties = new Properties();
                            try {
                                properties.load(byteArrayInputStream);
                                try {
                                    byteArrayInputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            } catch (Throwable th) {
                                try {
                                    byteArrayInputStream.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            this.uG = new jv(properties, TMSDKContext.getApplicaionContext());
                            if (this.uG.cD()) {
                                i++;
                            } else {
                                cK();
                                this.uE.setTimeInMillis(System.currentTimeMillis());
                                return;
                            }
                        }
                        return;
                    }
                    throw new RuntimeException("RSA decrypt error.");
                }
                throw new RuntimeException("Certification file is missing! Please contact TMS(Tencent Mobile Secure) group.");
            } catch (RuntimeException e4) {
                throw new RuntimeException("Invaild signature! Please contact TMS(Tencent Mobile Secure) group.");
            }
        }
        this.uG = new jv(properties, TMSDKContext.getApplicaionContext());
        if (this.uG.cD()) {
            cK();
            this.uE.setTimeInMillis(System.currentTimeMillis());
            return;
        }
        i++;
    }

    public void bH(String str) {
        d.e("LicMan", "strTimeSec=" + str);
        mm.a(getClass());
        new nc("licence").a("expiry.enc_seconds", str, true);
        cK();
    }

    public final boolean cD() {
        return this.uG.cD();
    }

    public String cE() {
        return this.uG.cE();
    }

    public String cF() {
        return this.uG.cF();
    }

    public boolean cJ() {
        boolean z = false;
        if (cI()) {
            return true;
        }
        di diVar = new di();
        int a = this.uG.a(diVar);
        if (diVar.ir == null || diVar.ir.iQ == null || diVar.ir.iQ.length() == 0) {
            switch (a) {
                case 1:
                    return true;
                default:
                    if (!cl()) {
                        z = true;
                    }
                    return z;
            }
        }
        boolean z2;
        switch (a) {
            case -1:
                bH(diVar.ir.iQ);
                z2 = true;
                break;
            case 0:
                throw new RuntimeException("Unknown licence! Please contact TMS(Tencent Mobile Secure) group.");
            case 1:
                bH(diVar.ir.iQ);
                z2 = false;
                break;
            default:
                z2 = cl();
                break;
        }
        if (!z2) {
            z = true;
        }
        return z;
    }

    public boolean cl() {
        if (cI()) {
            return false;
        }
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(System.currentTimeMillis());
        if (instance.get(1) != this.uE.get(1) || instance.get(6) != this.uE.get(6)) {
            cK();
        }
        this.uE.setTimeInMillis(System.currentTimeMillis());
        return this.uD.cl();
    }
}
