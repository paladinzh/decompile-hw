package com.fyusion.sdk.viewer.ext.localfyuse;

import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.n;

/* compiled from: Unknown */
public class d {
    public static h a(String str, e eVar) {
        int i = 1;
        h hVar = new h();
        hVar.setId(str);
        n nVar = new n();
        nVar.setDirectionX(eVar.getDirectionX());
        nVar.setDirectionY(eVar.getDirectionY());
        nVar.setNumProcessedFrames(eVar.getNumberOfProcessedFrames());
        nVar.setCurvature(eVar.getCurvature());
        nVar.setThumbnailIndex(eVar.getThumbnailIndex());
        nVar.setFrontCamera(!eVar.wasRecordedUsingFrontCamera() ? 0 : 1);
        nVar.setAndroid(true);
        nVar.setStabilizationDataFrameOffset(eVar.getStabilizationDataFrameOffset());
        nVar.setSlicesLength(1);
        nVar.addSlice(0, eVar.getNumberOfStabilizedFrames(), 0);
        nVar.setStartFrame(0);
        nVar.setEndFrame(!eVar.isLoopClosed() ? eVar.getNumberOfStabilizedFrames() : eVar.getLoop_closed_end_frame_());
        nVar.setWidth((int) eVar.getProcessedSize().width);
        nVar.setHeight((int) eVar.getProcessedSize().height);
        nVar.setGravityX(eVar.getGravityX());
        nVar.setGravityY(eVar.getGravityY());
        if (eVar.isPortrait()) {
            i = 2;
        } else {
            if (((eVar.getGravityX() < 0.0f ? 1 : 0) ^ eVar.wasRecordedUsingFrontCamera()) != 0) {
                i = 0;
            }
        }
        nVar.setRotation_mode(i);
        nVar.setCameraWidth((int) eVar.getCameraSize().width);
        nVar.setCameraHeight((int) eVar.getCameraSize().height);
        nVar.setLoopClosed(eVar.getLoop_closed_());
        nVar.setLoopClosedEndFrame(eVar.getLoop_closed_end_frame_());
        nVar.setDeviceId(eVar.getDeviceID());
        nVar.setUniqueDeviceId(eVar.getUniqueDeviceID());
        hVar.setHasLowResolutionSlice(false);
        hVar.setMagic(nVar);
        return hVar;
    }
}
