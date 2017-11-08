package com.avast.android.sdk.engine;

/* compiled from: Unknown */
public class UpdateCheckResultStructure {
    public UpdateCheck checkResult = null;
    public String vpsUrl = null;

    /* compiled from: Unknown */
    public enum UpdateCheck {
        RESULT_UPDATE_AVAILABLE,
        RESULT_UP_TO_DATE,
        ERROR_OLD_APPLICATION_VERSION,
        ERROR_CONNECTION_PROBLEMS,
        ERROR_SIGNATURE_NOT_VALID,
        ERROR_WRONG_PROTO_FILE,
        ERROR_BROKEN_VERSION_STRINGS,
        ERROR_CURRENT_VPS_INVALID
    }
}
