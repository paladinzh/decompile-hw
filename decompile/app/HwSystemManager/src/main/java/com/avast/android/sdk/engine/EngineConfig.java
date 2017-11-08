package com.avast.android.sdk.engine;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/* compiled from: Unknown */
public final class EngineConfig {
    private String a;
    private String b;
    private Long c;
    private String d;
    private String e;
    private String f;
    private Uri g;
    private Uri h;
    private EngineLoggerInterface i;
    private VpsClassLoaderFactory j;
    private boolean k;
    private boolean l;
    private boolean m;
    private List<String> n;
    private boolean o;
    private boolean p;
    private List<String> q;

    /* compiled from: Unknown */
    public static class Builder {
        private final EngineConfig a;

        private Builder() {
            this.a = new EngineConfig();
        }

        private Builder(EngineConfig engineConfig) {
            this.a = new EngineConfig();
            setGuid(engineConfig.getGuid());
            setAuid(engineConfig.getAuid());
            setUrlInfoCredentials(engineConfig.getUrlInfoCallerId(), engineConfig.getUrlInfoApiKey());
            setCustomUrlInfoServerUri(engineConfig.getCustomUrlInfoServerUri());
            setCustomVpsUpdateServerUri(engineConfig.getCustomVpsUpdateServerUri());
            setEngineLogger(engineConfig.getEngineLogger());
            setCustomVpsClassLoaderFactory(engineConfig.getCustomVpsClassLoaderFactory());
            setScanPupsEnabled(engineConfig.getScanPupsEnabled());
            setScanReportingEnabled(engineConfig.getScanReportingEnabled());
            setAutomaticUpdates(engineConfig.isAutomaticUpdateEnabled());
            setCustomScanStorages(engineConfig.getCustomScanStorages());
            setApiKey(engineConfig.getApiKey());
            setFileCloudScanningEnabled(engineConfig.isFileCloudScanningEnabled());
            setFileShieldCustomSdCardRoots(engineConfig.getFileShieldSdCardRoots());
        }

        private EngineConfig a() {
            try {
                UUID.fromString(this.a.a);
                if (this.a.b != null) {
                    if (this.a.e != null) {
                        try {
                            UUID.fromString(this.a.e);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("AUID in invalid format");
                        }
                    }
                    if (!((this.a.c == null || this.a.c.longValue() == 0) && TextUtils.isEmpty(this.a.d))) {
                        if (TextUtils.isEmpty(this.a.d)) {
                            throw new IllegalArgumentException("Invalid URLInfo API key");
                        }
                        if (this.a.c != null) {
                            if (this.a.c.longValue() == 0) {
                            }
                        }
                        throw new IllegalArgumentException("Invalid URLInfo caller ID");
                    }
                    if (this.a.g != null && !a(this.a.g)) {
                        throw new IllegalArgumentException("Invalid UrlInfo server Uri");
                    } else if (this.a.h == null || a(this.a.h)) {
                        if (this.a.n != null) {
                            for (String str : this.a.n) {
                                if (TextUtils.isEmpty(str) || !str.startsWith("/") || str.length() != str.replaceFirst("[?:\\\"*|\\\\<>]", "").length()) {
                                    throw new IllegalArgumentException("Invalid custom scan storage path: " + str);
                                }
                            }
                        }
                        return this.a;
                    } else {
                        throw new IllegalArgumentException("Invalid VPS update server Uri");
                    }
                }
                throw new IllegalArgumentException("API key must be supplied");
            } catch (Exception e2) {
                throw new IllegalArgumentException("GUID null or in invalid format");
            }
        }

        @SuppressLint({"NewApi"})
        private boolean a(Uri uri) {
            return Patterns.WEB_URL.matcher(uri.toString()).matches();
        }

        public EngineConfig build() throws IllegalArgumentException {
            return new Builder(this.a).a();
        }

        public Builder setApiKey(String str) {
            this.a.b = str;
            return this;
        }

        public Builder setAuid(String str) {
            this.a.e = str;
            return this;
        }

        public Builder setAutomaticUpdates(boolean z) {
            this.a.m = z;
            return this;
        }

        public Builder setCustomScanStorages(List<String> list) {
            this.a.n = list;
            return this;
        }

        public Builder setCustomUrlInfoServerUri(Uri uri) {
            this.a.g = uri;
            return this;
        }

        public Builder setCustomVpsClassLoaderFactory(VpsClassLoaderFactory vpsClassLoaderFactory) {
            this.a.j = vpsClassLoaderFactory;
            return this;
        }

        public Builder setCustomVpsUpdateServerUri(Uri uri) {
            this.a.h = uri;
            return this;
        }

        public Builder setEngineLogger(EngineLoggerInterface engineLoggerInterface) {
            this.a.i = engineLoggerInterface;
            return this;
        }

        public Builder setFileCloudScanningEnabled(boolean z) {
            this.a.p = z;
            return this;
        }

        public Builder setFileShieldCustomSdCardRoots(List<String> list) {
            if (list != null) {
                this.a.q = new LinkedList();
                this.a.q.addAll(list);
            }
            return this;
        }

        public Builder setGuid(String str) {
            this.a.a = str;
            return this;
        }

        public Builder setScanPupsEnabled(boolean z) {
            this.a.k = z;
            return this;
        }

        public Builder setScanReportingEnabled(boolean z) {
            this.a.l = z;
            return this;
        }

        public Builder setUrlInfoCredentials(Long l, String str) {
            this.a.c = l;
            this.a.d = str;
            return this;
        }

        public Builder setUuid(String str) {
            this.a.f = str;
            return this;
        }

        public Builder setWebLoggingEnabled(boolean z) {
            this.a.o = z;
            return this;
        }
    }

    private EngineConfig() {
        this.k = true;
        this.l = false;
        this.m = true;
        this.o = true;
        this.p = true;
        this.q = null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(EngineConfig engineConfig) {
        if (engineConfig != null) {
            return new Builder();
        }
        throw new IllegalArgumentException("Config must not be null");
    }

    public String getApiKey() {
        return this.b;
    }

    public String getAuid() {
        return this.e;
    }

    public List<String> getCustomScanStorages() {
        return this.n;
    }

    public Uri getCustomUrlInfoServerUri() {
        return this.g;
    }

    public VpsClassLoaderFactory getCustomVpsClassLoaderFactory() {
        return this.j;
    }

    public Uri getCustomVpsUpdateServerUri() {
        return this.h;
    }

    public EngineLoggerInterface getEngineLogger() {
        return this.i;
    }

    public List<String> getFileShieldSdCardRoots() {
        return this.q;
    }

    public String getGuid() {
        return this.a;
    }

    public boolean getScanPupsEnabled() {
        return this.k;
    }

    public boolean getScanReportingEnabled() {
        return this.l;
    }

    public String getUrlInfoApiKey() {
        return this.d;
    }

    public Long getUrlInfoCallerId() {
        return this.c;
    }

    public String getUuid() {
        return this.f;
    }

    public boolean isAutomaticUpdateEnabled() {
        return this.m;
    }

    public boolean isFileCloudScanningEnabled() {
        return this.p;
    }

    public boolean isWebLoggingEnabled() {
        return this.o;
    }
}
