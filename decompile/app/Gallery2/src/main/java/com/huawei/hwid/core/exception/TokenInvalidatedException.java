package com.huawei.hwid.core.exception;

public class TokenInvalidatedException extends Exception {
    private String a;

    public TokenInvalidatedException(String str) {
        super(str);
        this.a = str;
    }
}
