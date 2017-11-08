package sun.security.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertPathParameters;
import java.security.cert.PKIXBuilderParameters;
import java.util.HashMap;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import sun.security.validator.Validator;

abstract class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
    private static final Debug debug = Debug.getInstance("ssl");
    private boolean isInitialized = false;
    private X509TrustManager trustManager = null;

    public static final class PKIXFactory extends TrustManagerFactoryImpl {
        X509TrustManager getInstance(KeyStore ks) throws KeyStoreException {
            return new X509TrustManagerImpl(Validator.TYPE_PKIX, ks);
        }

        X509TrustManager getInstance(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            if (spec instanceof CertPathTrustManagerParameters) {
                CertPathParameters params = ((CertPathTrustManagerParameters) spec).getParameters();
                if (params instanceof PKIXBuilderParameters) {
                    return new X509TrustManagerImpl(Validator.TYPE_PKIX, (PKIXBuilderParameters) params);
                }
                throw new InvalidAlgorithmParameterException("Encapsulated parameters must be PKIXBuilderParameters");
            }
            throw new InvalidAlgorithmParameterException("Parameters must be CertPathTrustManagerParameters");
        }
    }

    public static final class SimpleFactory extends TrustManagerFactoryImpl {
        X509TrustManager getInstance(KeyStore ks) throws KeyStoreException {
            return new X509TrustManagerImpl(Validator.TYPE_SIMPLE, ks);
        }

        X509TrustManager getInstance(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("SunX509 TrustManagerFactory does not use ManagerFactoryParameters");
        }
    }

    abstract X509TrustManager getInstance(KeyStore keyStore) throws KeyStoreException;

    abstract X509TrustManager getInstance(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException;

    TrustManagerFactoryImpl() {
    }

    protected void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks == null) {
            try {
                ks = getCacertsKeyStore("trustmanager");
            } catch (Object se) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println("SunX509: skip default keystore: " + se);
                }
            } catch (Object err) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println("SunX509: skip default keystore: " + err);
                }
                throw err;
            } catch (Object re) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println("SunX509: skip default keystore: " + re);
                }
                throw re;
            } catch (Object e) {
                if (debug != null && Debug.isOn("trustmanager")) {
                    System.out.println("SunX509: skip default keystore: " + e);
                }
                throw new KeyStoreException("problem accessing trust store" + e);
            }
        }
        this.trustManager = getInstance(ks);
        this.isInitialized = true;
    }

    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        this.trustManager = getInstance(spec);
        this.isInitialized = true;
    }

    protected TrustManager[] engineGetTrustManagers() {
        if (this.isInitialized) {
            return new TrustManager[]{this.trustManager};
        }
        throw new IllegalStateException("TrustManagerFactoryImpl is not initialized");
    }

    private static FileInputStream getFileInputStream(final File file) throws Exception {
        return (FileInputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<FileInputStream>() {
            public FileInputStream run() throws Exception {
                try {
                    if (file.exists()) {
                        return new FileInputStream(file);
                    }
                    return null;
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
        });
    }

    static KeyStore getCacertsKeyStore(String dbgname) throws Exception {
        InputStream inputStream = null;
        final HashMap<String, String> props = new HashMap();
        String sep = File.separator;
        KeyStore keyStore = null;
        AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            public Void run() throws Exception {
                props.put("trustStore", System.getProperty("javax.net.ssl.trustStore"));
                props.put("javaHome", System.getProperty("java.home"));
                props.put("trustStoreType", System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType()));
                props.put("trustStoreProvider", System.getProperty("javax.net.ssl.trustStoreProvider", ""));
                props.put("trustStorePasswd", System.getProperty("javax.net.ssl.trustStorePassword", ""));
                return null;
            }
        });
        String storeFileName = (String) props.get("trustStore");
        if (!"NONE".equals(storeFileName)) {
            File storeFile;
            if (storeFileName != null) {
                storeFile = new File(storeFileName);
                inputStream = getFileInputStream(storeFile);
            } else {
                String javaHome = (String) props.get("javaHome");
                storeFile = new File(javaHome + sep + "lib" + sep + "security" + sep + "jssecacerts");
                inputStream = getFileInputStream(storeFile);
                if (inputStream == null) {
                    storeFile = new File(javaHome + sep + "lib" + sep + "security" + sep + "cacerts");
                    inputStream = getFileInputStream(storeFile);
                }
            }
            if (inputStream != null) {
                storeFileName = storeFile.getPath();
            } else {
                storeFileName = "No File Available, using empty keystore.";
            }
        }
        String defaultTrustStoreType = (String) props.get("trustStoreType");
        String defaultTrustStoreProvider = (String) props.get("trustStoreProvider");
        if (debug != null && Debug.isOn(dbgname)) {
            System.out.println("trustStore is: " + storeFileName);
            System.out.println("trustStore type is : " + defaultTrustStoreType);
            System.out.println("trustStore provider is : " + defaultTrustStoreProvider);
        }
        if (defaultTrustStoreType.length() != 0) {
            if (debug != null && Debug.isOn(dbgname)) {
                System.out.println("init truststore");
            }
            if (defaultTrustStoreProvider.length() == 0) {
                keyStore = KeyStore.getInstance(defaultTrustStoreType);
            } else {
                keyStore = KeyStore.getInstance(defaultTrustStoreType, defaultTrustStoreProvider);
            }
            char[] passwd = null;
            String defaultTrustStorePassword = (String) props.get("trustStorePasswd");
            if (defaultTrustStorePassword.length() != 0) {
                passwd = defaultTrustStorePassword.toCharArray();
            }
            keyStore.load(inputStream, passwd);
            if (passwd != null) {
                for (int i = 0; i < passwd.length; i++) {
                    passwd[i] = '\u0000';
                }
            }
        }
        if (inputStream != null) {
            inputStream.close();
        }
        return keyStore;
    }
}
