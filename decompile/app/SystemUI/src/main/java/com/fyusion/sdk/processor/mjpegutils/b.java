package com.fyusion.sdk.processor.mjpegutils;

import android.content.Context;
import com.fyusion.sdk.common.ext.f;
import fyusion.vislib.FyuseSlice;
import fyusion.vislib.FyuseSliceVec;
import java.util.LinkedList;

/* compiled from: Unknown */
public class b {
    private Context a;

    public b(Context context) {
        this.a = context;
    }

    public FyuseSliceVec a(f fVar, String str) {
        int numberOfProcessedFrames = fVar.getEndFrame() < 0 ? fVar.getNumberOfProcessedFrames() - 1 : fVar.getEndFrame();
        FyuseSlice fyuseSlice = new FyuseSlice();
        fyuseSlice.setStart_frame((int) Math.max(((double) fVar.getThumbnailIndex()) - 37.0d, (double) fVar.getStartFrame()));
        fyuseSlice.setEnd_frame((int) Math.min(((double) fVar.getThumbnailIndex()) + 37.0d, (double) numberOfProcessedFrames));
        if (fyuseSlice.getStart_frame() == fVar.getStartFrame()) {
            fyuseSlice.setEnd_frame(Math.min(fyuseSlice.getStart_frame() + 74, numberOfProcessedFrames));
        }
        if (fyuseSlice.getEnd_frame() == numberOfProcessedFrames) {
            fyuseSlice.setStart_frame(Math.max(fyuseSlice.getEnd_frame() - 74, fVar.getStartFrame()));
        }
        if (fyuseSlice.getStart_frame() <= fVar.getStartFrame() + 3) {
            fyuseSlice.setStart_frame(fVar.getStartFrame());
        }
        if (fyuseSlice.getEnd_frame() >= numberOfProcessedFrames - 3) {
            fyuseSlice.setEnd_frame(numberOfProcessedFrames);
        }
        int start_frame = fyuseSlice.getStart_frame();
        LinkedList linkedList = new LinkedList();
        FyuseSliceVec fyuseSliceVec = new FyuseSliceVec();
        while (start_frame > fVar.getStartFrame()) {
            FyuseSlice fyuseSlice2 = new FyuseSlice();
            fyuseSlice2.setEnd_frame(start_frame - 1);
            fyuseSlice2.setStart_frame(Math.max(start_frame - 75, fVar.getStartFrame()));
            if (fyuseSlice2.getStart_frame() <= fVar.getStartFrame() + 3) {
                fyuseSlice2.setStart_frame(fVar.getStartFrame());
            }
            if (fyuseSlice2.getEnd_frame() >= numberOfProcessedFrames - 3) {
                fyuseSlice2.setEnd_frame(numberOfProcessedFrames);
            }
            start_frame = fyuseSlice2.getStart_frame();
            linkedList.addFirst(new Integer((int) fyuseSliceVec.size()));
            fyuseSliceVec.add(fyuseSlice2);
        }
        linkedList.addLast(new Integer((int) fyuseSliceVec.size()));
        fyuseSliceVec.add(fyuseSlice);
        start_frame = fyuseSlice.getEnd_frame();
        while (start_frame < numberOfProcessedFrames) {
            fyuseSlice = new FyuseSlice();
            fyuseSlice.setStart_frame(start_frame + 1);
            fyuseSlice.setEnd_frame(Math.min(start_frame + 75, numberOfProcessedFrames));
            if (fyuseSlice.getStart_frame() <= fVar.getStartFrame() + 3) {
                fyuseSlice.setStart_frame(fVar.getStartFrame());
            }
            if (fyuseSlice.getEnd_frame() >= numberOfProcessedFrames - 3) {
                fyuseSlice.setEnd_frame(numberOfProcessedFrames);
            }
            start_frame = fyuseSlice.getEnd_frame();
            linkedList.addLast(new Integer((int) fyuseSliceVec.size()));
            fyuseSliceVec.add(fyuseSlice);
        }
        FyuseSliceVec fyuseSliceVec2 = new FyuseSliceVec();
        start_frame = 0;
        while (true) {
            if ((((long) start_frame) >= fyuseSliceVec.size() ? 1 : null) != null) {
                return fyuseSliceVec2;
            }
            FyuseSlice fyuseSlice3 = fyuseSliceVec.get(((Integer) linkedList.get(start_frame)).intValue());
            fyuseSlice3.setIndex(start_frame);
            fyuseSlice3.setMjpeg_file_name("fyuse_mjpeg_slice" + start_frame + ".mp4");
            fyuseSlice3.setIndex_file_name("fyuse_mjpeg_slice" + start_frame + ".index");
            fyuseSlice3.setH264_file_name("fyuse_h264_slice" + start_frame + ".mp4");
            fyuseSliceVec2.add(fyuseSlice3);
            start_frame++;
        }
    }
}
