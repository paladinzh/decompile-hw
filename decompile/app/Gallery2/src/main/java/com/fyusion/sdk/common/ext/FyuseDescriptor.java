package com.fyusion.sdk.common.ext;

import com.fyusion.sdk.common.h;
import fyusion.vislib.BoolVec;
import fyusion.vislib.FyuseSize;
import fyusion.vislib.StabilizationData;
import fyusion.vislib.TransformationParametersVec;
import fyusion.vislib.VisualizationMeshStorage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/* compiled from: Unknown */
public class FyuseDescriptor extends h {
    public final boolean VERBOSE_FRAMES = false;
    protected String address;
    public int commentsNo;
    public boolean doesntHaveLowResolutionSlice = false;
    public BoolVec droppedOffline = null;
    public int echoes;
    private String fyusePath = "";
    public float globalScale;
    protected boolean hasAddress;
    public int hashCode = -1;
    public boolean isLocal;
    public boolean isPrivate;
    public int likes;
    protected Magic magic;
    public VisualizationMeshStorage mesh = null;
    public String profilePicture;
    public String s3Path;
    public ArrayList<Slice> slices = new ArrayList();
    public StabilizationData stabilizationData = null;
    public String status;
    public int tilts;
    public int totalHighResolutionLength = 0;
    protected int totalLowResolutionLength = 0;
    public TransformationParametersVec transformParameters = null;
    private boolean tweenLoaded = false;

    /* compiled from: Unknown */
    public static class Slice implements Serializable {
        public int highResolutionLength = 0;
        public int index = 0;
        public int lowResolutionLength = 0;
    }

    public FyuseDescriptor(e eVar, String str) {
        init(str, true);
        this.magic = new Magic();
        this.magic.setSlicesLength(1);
        this.magic.addSlice(eVar.getStartFrame(), 750, 0);
        FyuseSize processedSize = eVar.getProcessedSize();
        this.previewWidth = (int) processedSize.width;
        this.previewHeight = (int) processedSize.height;
        this.magic.setWidth(this.previewWidth);
        this.magic.setHeight(this.previewHeight);
        this.lowResolutionSliceIndex = 0;
        this.slices = new ArrayList();
        Slice slice = new Slice();
        int startFrame = 750 - eVar.getStartFrame();
        slice.lowResolutionLength = startFrame;
        slice.highResolutionLength = startFrame;
        this.slices.add(slice);
    }

    private void init(String str, boolean z) {
        this.fyuseId = str;
        this.hashCode = str.hashCode();
        this.isLocal = z;
    }

    public String briefDescription() {
        return "FyuseDescriptor{isLocal=" + this.isLocal + ", fyuseId='" + this.fyuseId + '\'' + ", name='" + this.name + '\'' + ", doesntHaveLowResolutionSlice=" + this.doesntHaveLowResolutionSlice + ", totalHighResolutionLength=" + this.totalHighResolutionLength + ", totalLowResolutionLength=" + this.totalLowResolutionLength + ", previewWidth=" + this.previewWidth + ", previewHeight=" + this.previewHeight + '}';
    }

    public boolean dropFrame(int i, int i2) {
        if (this.droppedOffline != null) {
            int sliceStartFrame = i - getMagic().getSliceStartFrame(i2);
            if (!(((long) sliceStartFrame) >= this.droppedOffline.size())) {
                return this.droppedOffline.get(sliceStartFrame);
            }
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.fyuseId, ((FyuseDescriptor) obj).fyuseId);
    }

    public String getFyusePath() {
        return this.fyusePath;
    }

    public int getHeight() {
        return getMagic().getHeight();
    }

    public Magic getMagic() {
        return this.magic;
    }

    public int getWidth() {
        return getMagic().getWidth();
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.fyuseId});
    }

    public void setFyusePath(String str) {
        this.fyusePath = str;
    }

    public void setMagic(Magic magic) {
        this.magic = magic;
    }

    public void setStabilizationInfo(BoolVec boolVec, VisualizationMeshStorage visualizationMeshStorage, TransformationParametersVec transformationParametersVec, float f) {
        this.droppedOffline = boolVec;
        int i = 0;
        int i2 = 0;
        while (true) {
            if ((((long) i) >= boolVec.size() ? 1 : null) == null) {
                if (!boolVec.get(i)) {
                    i2++;
                }
                i++;
            } else {
                this.mesh = visualizationMeshStorage;
                this.transformParameters = transformationParametersVec;
                this.globalScale = f;
                return;
            }
        }
    }

    public String toString() {
        return "FyuseDescriptor{isLocal=" + this.isLocal + ", fyuseId='" + this.fyuseId + '\'' + ", name='" + this.name + '\'' + ", url='" + this.url + '\'' + ", doesntHaveLowResolutionSlice=" + this.doesntHaveLowResolutionSlice + ", totalHighResolutionLength=" + this.totalHighResolutionLength + ", totalLowResolutionLength=" + this.totalLowResolutionLength + ", previewWidth=" + this.previewWidth + ", previewHeight=" + this.previewHeight + ", lowResolutionSliceIndex=" + this.lowResolutionSliceIndex + ", commentsNo=" + this.commentsNo + ", likes=" + this.likes + ", isPrivate=" + this.isPrivate + ", echoes=" + this.echoes + ", slices=" + this.slices + ", address='" + this.address + '\'' + ", hasAddress=" + this.hasAddress + ", magic=" + this.magic + ", timeStamp=" + this.timeStamp + ", tweenLoaded=" + this.tweenLoaded + ", traversalIndex=" + this.traversalIndex + ", stabilizationData=" + this.stabilizationData + ", transformParameters=" + this.transformParameters + ", droppedOffline=" + this.droppedOffline + ", mesh=" + this.mesh + '}';
    }
}
