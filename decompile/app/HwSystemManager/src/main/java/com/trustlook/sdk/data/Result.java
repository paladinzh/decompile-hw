package com.trustlook.sdk.data;

public abstract class Result {
    private boolean a;
    private int b;

    public boolean isSuccess() {
        return this.a;
    }

    public void setIsSuccess(boolean z) {
        this.a = z;
    }

    public int getError() {
        return this.b;
    }

    public void setError(int i) {
        this.b = i;
    }
}
