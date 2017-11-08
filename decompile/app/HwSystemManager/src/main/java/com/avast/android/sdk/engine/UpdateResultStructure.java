package com.avast.android.sdk.engine;

/* compiled from: Unknown */
public class UpdateResultStructure {
    public VpsInformation newVps;
    public int nextUpdateCheckMins;
    public UpdateResult result;

    /* compiled from: Unknown */
    public enum UpdateResult {
        RESULT_UP_TO_DATE,
        RESULT_UPDATED,
        RESULT_OLD_APPLICATION_VERSION,
        RESULT_CONNECTION_PROBLEMS,
        RESULT_NOT_ENOUGH_INTERNAL_SPACE_TO_UPDATE,
        RESULT_INVALID_VPS,
        RESULT_UNKNOWN_ERROR
    }

    public UpdateResultStructure() {
        this.result = null;
        this.newVps = null;
        this.nextUpdateCheckMins = 240;
        this.result = UpdateResult.RESULT_UP_TO_DATE;
    }

    public UpdateResultStructure(UpdateResult updateResult, VpsInformation vpsInformation, int i) {
        this.result = null;
        this.newVps = null;
        this.nextUpdateCheckMins = 240;
        if (UpdateResult.RESULT_UPDATED.equals(updateResult) && vpsInformation == null) {
            throw new IllegalArgumentException("Information about new VPS must be provided if the VPS has been updated");
        }
        this.result = updateResult;
        this.nextUpdateCheckMins = i;
        if (UpdateResult.RESULT_UPDATED.equals(updateResult)) {
            this.newVps = vpsInformation;
        } else {
            this.newVps = null;
        }
    }
}
