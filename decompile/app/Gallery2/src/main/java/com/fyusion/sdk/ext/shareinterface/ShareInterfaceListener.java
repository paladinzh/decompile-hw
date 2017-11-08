package com.fyusion.sdk.ext.shareinterface;

/* compiled from: Unknown */
public interface ShareInterfaceListener {
    void onError(Exception exception);

    void onSuccess(String str);

    void onUserCancel();
}
