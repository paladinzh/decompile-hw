package com.a.a;

import android.content.Intent;

/* compiled from: Unknown */
public class a extends s {
    private Intent b;

    public a(i iVar) {
        super(iVar);
    }

    public String getMessage() {
        return this.b == null ? super.getMessage() : "User needs to (re)enter credentials.";
    }
}
