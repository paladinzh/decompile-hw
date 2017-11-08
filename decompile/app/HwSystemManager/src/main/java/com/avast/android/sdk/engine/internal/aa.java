package com.avast.android.sdk.engine.internal;

import android.content.Context;
import com.avast.android.sdk.engine.obfuscated.aj;
import com.avast.android.sdk.engine.obfuscated.an;
import com.avast.android.sdk.engine.obfuscated.an.a;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.w;
import com.avast.android.sdk.engine.obfuscated.x;
import com.avast.android.sdk.engine.obfuscated.y;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/* compiled from: Unknown */
public class aa {
    private final ZipFile a;
    private final ZipFile b;

    public aa(Context context, File file) throws IOException {
        this.b = new ZipFile(context.getApplicationInfo().sourceDir);
        this.a = new ZipFile(file);
    }

    private String a(ZipEntry zipEntry) {
        try {
            X509Certificate a = a(this.b, null);
            X509Certificate a2 = a(this.a, zipEntry);
            if (a == null || a2 == null) {
                return null;
            }
            if (!a(a, a2)) {
                a = b();
                if (a == null || !a(a, a2)) {
                    ao.a("Signing certificates do not match");
                    return null;
                }
            }
            byte[] b = b(zipEntry);
            Cipher instance = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
            instance.init(2, a2);
            byte[] doFinal = instance.doFinal(b);
            String str = "";
            for (byte b2 : doFinal) {
                str = str + Integer.toString((b2 & 255) + 256, 16).substring(1);
            }
            return str;
        } catch (IOException e) {
            return null;
        } catch (CertificateException e2) {
            return null;
        } catch (NoSuchAlgorithmException e3) {
            return null;
        } catch (NoSuchProviderException e4) {
            return null;
        } catch (NoSuchPaddingException e5) {
            return null;
        } catch (InvalidKeyException e6) {
            return null;
        } catch (IllegalBlockSizeException e7) {
            return null;
        } catch (BadPaddingException e8) {
            return null;
        }
    }

    private X509Certificate a(ZipFile zipFile, ZipEntry zipEntry) throws IOException, CertificateException {
        ZipEntry zipEntry2;
        Enumeration entries = zipFile.entries();
        if (zipEntry != null) {
            zipEntry2 = zipEntry;
        } else {
            zipEntry2 = zipEntry;
            while (entries.hasMoreElements()) {
                zipEntry2 = (ZipEntry) entries.nextElement();
                if (zipEntry2.getName().startsWith("META-INF/")) {
                    if (zipEntry2.getName().endsWith(".RSA")) {
                        break;
                    }
                }
            }
        }
        List list = (List) CertificateFactory.getInstance("X509").generateCertificates(zipFile.getInputStream(zipEntry2));
        return (list == null || list.isEmpty()) ? null : (X509Certificate) list.get(0);
    }

    private boolean a(X509Certificate x509Certificate, X509Certificate x509Certificate2) {
        boolean z = true;
        if (!x509Certificate.getIssuerDN().equals(x509Certificate2.getIssuerDN())) {
            z = false;
        }
        boolean z2 = x509Certificate.getSubjectDN().equals(x509Certificate2.getSubjectDN()) ? z : false;
        RSAPublicKey rSAPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();
        RSAPublicKey rSAPublicKey2 = (RSAPublicKey) x509Certificate2.getPublicKey();
        if (!rSAPublicKey.getAlgorithm().equals(rSAPublicKey2.getAlgorithm())) {
            z2 = false;
        }
        if (rSAPublicKey.getModulus().compareTo(rSAPublicKey2.getModulus()) != 0) {
            z2 = false;
        }
        return rSAPublicKey.getPublicExponent().compareTo(rSAPublicKey2.getPublicExponent()) == 0 ? z2 : false;
    }

    private X509Certificate b() {
        Enumeration entries = this.b.entries();
        ZipEntry zipEntry = null;
        while (entries.hasMoreElements()) {
            zipEntry = (ZipEntry) entries.nextElement();
            if (zipEntry.getName().equals("assets/AVAST.RSA")) {
                break;
            }
        }
        if (zipEntry != null) {
            try {
                return a(this.b, zipEntry);
            } catch (IOException e) {
                return null;
            } catch (CertificateException e2) {
                return null;
            }
        }
        ao.d("Assets certificate at assets/AVAST.RSA not found");
        return null;
    }

    private byte[] b(ZipEntry zipEntry) throws IOException {
        Enumeration entries = this.a.entries();
        if (zipEntry == null) {
            ZipEntry zipEntry2 = zipEntry;
            while (entries.hasMoreElements()) {
                zipEntry2 = (ZipEntry) entries.nextElement();
                if (zipEntry2.getName().startsWith("META-INF/") && !zipEntry2.getName().endsWith(".RSA")) {
                }
            }
            zipEntry = zipEntry2;
        }
        InputStream inputStream = this.a.getInputStream(zipEntry);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((int) zipEntry.getSize());
        byte[] bArr = new byte[((int) zipEntry.getSize())];
        while (true) {
            int read = inputStream.read(bArr);
            if (read < 0) {
                return new w(byteArrayOutputStream.toByteArray()).a();
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    private y c(ZipEntry zipEntry) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.a.getInputStream(zipEntry)));
        y yVar = new y();
        String str = "";
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                return yVar;
            }
            if (readLine.startsWith("SHA1-Digest-Manifest:")) {
                yVar.a(aj.a(readLine.substring(readLine.indexOf(":") + 2), 0));
            } else if (readLine.startsWith("Name:")) {
                str = readLine.substring(readLine.indexOf(":") + 2);
            } else if (readLine.startsWith("SHA1-Digest:")) {
                yVar.a(str, aj.a(readLine.substring(readLine.indexOf(":") + 2), 0));
            }
        }
    }

    public boolean a() {
        try {
            ZipEntry zipEntry;
            Enumeration entries = this.a.entries();
            ZipEntry zipEntry2 = null;
            ZipEntry zipEntry3 = null;
            ZipEntry zipEntry4 = null;
            while (entries.hasMoreElements()) {
                zipEntry = (ZipEntry) entries.nextElement();
                if (zipEntry.getName().startsWith("META-INF/") && zipEntry.getName().endsWith(".RSA")) {
                    zipEntry4 = zipEntry;
                }
                if (zipEntry.getName().startsWith("META-INF/") && zipEntry.getName().endsWith(".SF")) {
                    zipEntry3 = zipEntry;
                }
                if (zipEntry.getName().startsWith("META-INF/") && zipEntry.getName().endsWith(".MF")) {
                    zipEntry2 = zipEntry;
                }
            }
            if (zipEntry4 == null || zipEntry3 == null || zipEntry2 == null) {
                ao.a("META-INF directory is missing files");
                return false;
            }
            String a = a(zipEntry4);
            String str = "3021300906052b0e03021a05000414" + an.a(a.SHA1, this.a.getInputStream(zipEntry3), 0);
            if (a != null && a.compareToIgnoreCase(str) == 0) {
                y c = c(zipEntry3);
                byte[] a2 = an.a(a.SHA1, this.a.getInputStream(zipEntry2));
                if (a2 == null || c.a() == null || a2.length != c.a().length) {
                    ao.a("MF hashes invalid or not present");
                    return false;
                }
                int i = 0;
                while (i < a2.length) {
                    if (a2[i] == c.a()[i]) {
                        i++;
                    } else {
                        ao.a("MF hashes don't match");
                        return false;
                    }
                }
                x xVar = new x();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.a.getInputStream(zipEntry2)));
                a = "";
                str = "";
                str = "";
                str = "";
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    } else if (readLine.startsWith("Name:")) {
                        a = a + readLine + "\r\n";
                        str = readLine.substring(readLine.indexOf(":") + 2);
                    } else if (readLine.startsWith("SHA1-Digest:")) {
                        xVar.a(str, aj.a(readLine.substring(readLine.indexOf(":") + 2), 0));
                        a = a + readLine + "\r\n";
                    } else if ("".equals(readLine) && !"".equals(a)) {
                        String str2 = a + "\r\n";
                        readLine = an.a(a.SHA1, str2, 0);
                        a = "";
                        if (c.a(str) != null) {
                            str2 = a;
                            for (byte b : c.a(str)) {
                                str2 = str2 + Integer.toString((b & 255) + 256, 16).substring(1);
                            }
                            if (readLine.compareToIgnoreCase(str2) == 0) {
                                a = "";
                            } else {
                                ao.a("MF line hashes don't match");
                                return false;
                            }
                        }
                        a = str2;
                    }
                }
                Enumeration entries2 = this.a.entries();
                while (entries2.hasMoreElements()) {
                    zipEntry = (ZipEntry) entries2.nextElement();
                    if (!zipEntry.getName().startsWith("META-INF/") && !xVar.b(zipEntry.getName(), an.a(a.SHA1, this.a.getInputStream(zipEntry)))) {
                        ao.a("Hashes of " + zipEntry.getName() + " don't match");
                        return false;
                    }
                }
                return true;
            }
            ao.a("SF hashes don't match");
            return false;
        } catch (ZipException e) {
            ao.a("ZipException: " + e.getMessage());
            return false;
        } catch (IOException e2) {
            ao.a("IOException: " + e2.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e3) {
            ao.a("NoSuchAlgorithmException: " + e3.getMessage());
            return false;
        }
    }
}
