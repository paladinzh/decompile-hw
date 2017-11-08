package com.huawei.notificationmanager.util;

public class ReflectionActionWrapper {
    static final int CHAR_SEQUENCE = 10;
    static final String SET_TEXT = "setText";
    static final int STRING = 9;
    private Object mInstance = null;
    private String methodName;
    private int type;
    private String value;

    public ReflectionActionWrapper(Object actionInstance) {
        this.mInstance = actionInstance;
    }

    public String getValue() {
        try {
            this.type = ReflectionActionReflector.getType(this.mInstance);
            this.methodName = ReflectionActionReflector.getMethod(this.mInstance);
            this.value = getValueFrom(ReflectionActionReflector.getValue(this.mInstance));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return this.value;
    }

    private String getValueFrom(Object valueInstance) {
        if (!isSetText() || valueInstance == null) {
            return null;
        }
        return valueInstance.toString();
    }

    private boolean isSetText() {
        boolean isSetText = SET_TEXT.equals(this.methodName);
        return (9 == this.type || 10 == this.type) ? isSetText : false;
    }
}
