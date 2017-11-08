package com.fyusion.sdk.viewer.ext.localfyuse;

import com.fyusion.sdk.common.ext.f;
import com.fyusion.sdk.common.i;
import com.fyusion.sdk.common.p;

/* compiled from: Unknown */
public class d {
    public static i a(String str, f fVar) {
        int i = 1;
        i iVar = new i();
        iVar.setId(str);
        p pVar = new p();
        pVar.setDirectionX(fVar.getDirectionX());
        pVar.setDirectionY(fVar.getDirectionY());
        pVar.setNumProcessedFrames(fVar.getNumberOfProcessedFrames());
        pVar.setCurvature(fVar.getCurvature());
        pVar.setThumbnailIndex(fVar.getThumbnailIndex());
        pVar.setFrontCamera(!fVar.wasRecordedUsingFrontCamera() ? 0 : 1);
        pVar.setAndroid(true);
        pVar.setStabilizationDataFrameOffset(fVar.getStabilizationDataFrameOffset());
        pVar.setSlicesLength(1);
        pVar.addSlice(0, fVar.getNumberOfStabilizedFrames(), 0);
        pVar.setStartFrame(0);
        pVar.setEndFrame(!fVar.isLoopClosed() ? fVar.getNumberOfStabilizedFrames() : fVar.getLoop_closed_end_frame_());
        pVar.setWidth((int) fVar.getProcessedSize().width);
        pVar.setHeight((int) fVar.getProcessedSize().height);
        pVar.setGravityX(fVar.getGravityX());
        pVar.setGravityY(fVar.getGravityY());
        if (fVar.isPortrait()) {
            i = 2;
        } else {
            if (((fVar.getGravityX() < 0.0f ? 1 : 0) ^ fVar.wasRecordedUsingFrontCamera()) != 0) {
                i = 0;
            }
        }
        pVar.setRotation_mode(i);
        pVar.setCameraWidth((int) fVar.getCameraSize().width);
        pVar.setCameraHeight((int) fVar.getCameraSize().height);
        pVar.setLoopClosed(fVar.getLoop_closed_());
        pVar.setLoopClosedEndFrame(fVar.getLoop_closed_end_frame_());
        pVar.setDeviceId(fVar.getDeviceID());
        pVar.setUniqueDeviceId(fVar.getUniqueDeviceID());
        iVar.setHasLowResolutionSlice(false);
        iVar.setMagic(pVar);
        return iVar;
    }
}
