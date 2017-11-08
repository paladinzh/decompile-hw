package com.google.android.gms.tagmanager;

/* compiled from: Unknown */
class n {
    private Container TU;
    private a TX;
    private boolean zk;

    /* compiled from: Unknown */
    public interface a {
        void bc(String str);

        String iF();

        void iH();
    }

    public synchronized void ba(String str) {
        if (!this.zk) {
            this.TU.ba(str);
        }
    }

    void bc(String str) {
        if (this.zk) {
            bh.t("setCtfeUrlPathAndQuery called on a released ContainerHolder.");
        } else {
            this.TX.bc(str);
        }
    }

    String getContainerId() {
        if (!this.zk) {
            return this.TU.getContainerId();
        }
        bh.t("getContainerId called on a released ContainerHolder.");
        return "";
    }

    String iF() {
        if (!this.zk) {
            return this.TX.iF();
        }
        bh.t("setCtfeUrlPathAndQuery called on a released ContainerHolder.");
        return "";
    }

    public synchronized void refresh() {
        if (this.zk) {
            bh.t("Refreshing a released ContainerHolder.");
        } else {
            this.TX.iH();
        }
    }
}
