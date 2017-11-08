package com.fyusion.sdk.share;

import com.fyusion.sdk.common.FyuseSDKException;
import com.fyusion.sdk.share.exception.UserAuthenticationException;

/* compiled from: Unknown */
public interface FyuseUserListener {
    void onSDKError(FyuseSDKException fyuseSDKException);

    void onSuccess(FyuseUser fyuseUser);

    void onUserError(UserAuthenticationException userAuthenticationException);
}
