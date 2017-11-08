package com.android.org.conscrypt;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import libcore.io.Base64;
import libcore.io.DropBox;

public class PinFailureLogger {
    private static final long LOG_INTERVAL_NANOS = 817405952;
    private static long lastLoggedNanos = 0;

    public static synchronized void log(String cn, boolean chainContainsUserCert, boolean pinIsEnforcing, List<X509Certificate> chain) {
        synchronized (PinFailureLogger.class) {
            if (timeToLog()) {
                writeToLog(cn, chainContainsUserCert, pinIsEnforcing, chain);
                lastLoggedNanos = System.nanoTime();
                return;
            }
        }
    }

    protected static synchronized void writeToLog(String cn, boolean chainContainsUserCert, boolean pinIsEnforcing, List<X509Certificate> chain) {
        synchronized (PinFailureLogger.class) {
            StringBuilder sb = new StringBuilder();
            sb.append(cn);
            sb.append("|");
            sb.append(chainContainsUserCert);
            sb.append("|");
            sb.append(pinIsEnforcing);
            sb.append("|");
            for (X509Certificate cert : chain) {
                try {
                    sb.append(Base64.encode(cert.getEncoded()));
                } catch (CertificateEncodingException e) {
                    sb.append("Error: could not encode certificate");
                }
                sb.append("|");
            }
            DropBox.addText("exp_det_cert_pin_failure", sb.toString());
        }
    }

    protected static boolean timeToLog() {
        return System.nanoTime() - lastLoggedNanos > LOG_INTERVAL_NANOS;
    }
}
