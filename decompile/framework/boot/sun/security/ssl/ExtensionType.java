package sun.security.ssl;

import java.util.ArrayList;
import java.util.List;

/* compiled from: HelloExtensions */
final class ExtensionType {
    static final ExtensionType EXT_CERT_TYPE = e(9, "cert_type");
    static final ExtensionType EXT_CLIENT_CERTIFICATE_URL = e(2, "client_certificate_url");
    static final ExtensionType EXT_EC_POINT_FORMATS = e(11, "ec_point_formats");
    static final ExtensionType EXT_ELLIPTIC_CURVES = e(10, "elliptic_curves");
    static final ExtensionType EXT_MAX_FRAGMENT_LENGTH = e(1, "max_fragment_length");
    static final ExtensionType EXT_RENEGOTIATION_INFO = e(65281, "renegotiation_info");
    static final ExtensionType EXT_SERVER_NAME = e(0, "server_name");
    static final ExtensionType EXT_SIGNATURE_ALGORITHMS = e(13, "signature_algorithms");
    static final ExtensionType EXT_SRP = e(12, "srp");
    static final ExtensionType EXT_STATUS_REQUEST = e(5, "status_request");
    static final ExtensionType EXT_TRUNCATED_HMAC = e(4, "truncated_hmac");
    static final ExtensionType EXT_TRUSTED_CA_KEYS = e(3, "trusted_ca_keys");
    static final ExtensionType EXT_USER_MAPPING = e(6, "user_mapping");
    static List<ExtensionType> knownExtensions = new ArrayList(9);
    final int id;
    final String name;

    private ExtensionType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    static ExtensionType get(int id) {
        for (ExtensionType ext : knownExtensions) {
            if (ext.id == id) {
                return ext;
            }
        }
        return new ExtensionType(id, "type_" + id);
    }

    private static ExtensionType e(int id, String name) {
        ExtensionType ext = new ExtensionType(id, name);
        knownExtensions.add(ext);
        return ext;
    }
}
