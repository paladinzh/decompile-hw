package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model;

class OperatorSubInfo implements ICodeName {
    private String code;
    private String name;

    public OperatorSubInfo(String id, String name) {
        this.code = id;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public void set(ICodeName codeName) {
        this.code = codeName.getCode();
        this.name = codeName.getName();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.code.equals(((OperatorSubInfo) o).code);
    }

    public int hashCode() {
        return this.code.hashCode();
    }
}
