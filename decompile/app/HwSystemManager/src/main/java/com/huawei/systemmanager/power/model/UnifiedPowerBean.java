package com.huawei.systemmanager.power.model;

public class UnifiedPowerBean {
    private boolean is_protected;
    private boolean is_show;
    private String pkg_name;

    public String getPkg_name() {
        return this.pkg_name;
    }

    public void setPkg_name(String pkg_name) {
        this.pkg_name = pkg_name;
    }

    public boolean is_protected() {
        return this.is_protected;
    }

    public void setIs_protected(boolean is_protected) {
        this.is_protected = is_protected;
    }

    public boolean is_show() {
        return this.is_show;
    }

    public void setIs_show(boolean is_show) {
        this.is_show = is_show;
    }
}
