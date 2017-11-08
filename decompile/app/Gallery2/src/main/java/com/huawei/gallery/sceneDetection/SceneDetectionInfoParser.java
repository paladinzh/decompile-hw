package com.huawei.gallery.sceneDetection;

import com.android.gallery3d.util.GalleryLog;

public class SceneDetectionInfoParser {
    public static byte[] getSceneInfo(int controllerBits, String imageFilePath, int sharpnessLevel) {
        GalleryLog.d("SceneDetectionInfoParser", "getSceneInfo imageFilePath=" + imageFilePath);
        GalleryLog.d("SceneDetectionInfoParser", String.format("getSceneInfo result.length = %d.", new Object[]{Integer.valueOf(new SceneInformationParser(imageFilePath).getSceneInfoMakerNote(controllerBits, sharpnessLevel).length)}));
        return new SceneInformationParser(imageFilePath).getSceneInfoMakerNote(controllerBits, sharpnessLevel);
    }
}
