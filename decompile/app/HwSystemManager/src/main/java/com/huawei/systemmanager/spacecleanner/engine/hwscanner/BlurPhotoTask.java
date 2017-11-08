package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.content.Context;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.jpeg.JpegCode;
import com.huawei.systemmanager.spacecleanner.engine.jpeg.JpegNative;
import com.huawei.systemmanager.spacecleanner.engine.jpeg.JpegUtils;
import com.huawei.systemmanager.spacecleanner.engine.trash.BlurPhotoTrash;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlurPhotoTask extends Task {
    private static final String TAG = "BlurPhotoTask";
    private static AtomicBoolean TASK_RUNNING = new AtomicBoolean(false);
    private PathEntrySet mEntrySet;

    private class PhotoCheckBlur extends Thread {
        private BlockingQueue<JpegCode> mQueue;
        private int mSize;

        public PhotoCheckBlur(BlockingQueue<JpegCode> mQueue, int size) {
            super("PhotoCheckBlur");
            this.mQueue = mQueue;
            this.mSize = size;
        }

        public void run() {
            int initPhotoBlurEnv = JpegNative.getInstance().initPhotoBlurEnv();
            while (this.mSize > 0 && !BlurPhotoTask.this.isCanceled()) {
                JpegCode jpegCode = null;
                try {
                    jpegCode = (JpegCode) this.mQueue.take();
                } catch (InterruptedException e) {
                    HwLog.i(JpegNative.TAG, "exception in check blur ");
                }
                this.mSize--;
                HwLog.i(JpegNative.TAG, "PhotoCheckBlur mSize : " + this.mSize);
                if (jpegCode != null) {
                    if (BlurPhotoTask.this.isCanceled()) {
                        break;
                    } else if (initPhotoBlurEnv != -1 && jpegCode.isDataValid() && JpegCode.isBlur(jpegCode) == 1) {
                        BlurPhotoTask.this.onPublishItemUpdate(new BlurPhotoTrash(jpegCode.getPath(), BlurPhotoTask.this.mEntrySet == null ? null : BlurPhotoTask.this.mEntrySet.getPathEntry(jpegCode.getPath())));
                    }
                }
            }
            HwLog.i(JpegNative.TAG, "PhotoCheckBlur start to release memory");
            if (initPhotoBlurEnv != -1) {
                JpegNative.getInstance().uninitPhotoBlurEnv();
            }
            HwLog.i(JpegNative.TAG, "PhotoCheckBlur end to release memory");
            BlurPhotoTask.this.onPublishEnd();
            BlurPhotoTask.TASK_RUNNING.set(false);
        }
    }

    private class PhotoTransCode extends Thread {
        private List<String> mList;
        private BlockingQueue<JpegCode> mQueue;

        public PhotoTransCode(BlockingQueue<JpegCode> mQueue, List<String> mList) {
            super("PhotoTransCode");
            this.mQueue = mQueue;
            this.mList = mList;
        }

        public void run() {
            int sum = this.mList.size();
            JpegNative.getInstance().initPhotoTransCodeEnv();
            for (int count = 0; count < sum && !BlurPhotoTask.this.isCanceled(); count++) {
                String path = (String) this.mList.get(count);
                JpegCode jpegCode = new JpegCode(path);
                JpegNative.getInstance().transPhotocode(path, jpegCode);
                try {
                    this.mQueue.put(jpegCode);
                } catch (InterruptedException e) {
                    HwLog.i(JpegNative.TAG, "interruption in trans code");
                }
                if (BlurPhotoTask.this.isCanceled()) {
                    break;
                }
                HwLog.i(JpegNative.TAG, "PhotoTransCode start count is: " + count);
            }
            JpegNative.getInstance().uninitPhotoTransCodeEnv();
            HwLog.i(JpegNative.TAG, "PhotoTransCode end ");
        }
    }

    public BlurPhotoTask(Context ctx) {
        super(ctx);
    }

    public String getTaskName() {
        return TAG;
    }

    protected void doTask(ScanParams p) {
        if (TASK_RUNNING.get()) {
            HwLog.i(TAG, "task already running");
            onPublishEnd();
            return;
        }
        TASK_RUNNING.set(true);
        this.mEntrySet = p.getEntrySet();
        List<String> paths = JpegUtils.getCameraPhoto(GlobalContext.getContext());
        ExecutorService executorService = getExecutor();
        if (executorService == null || paths.size() <= 0) {
            onPublishEnd();
            TASK_RUNNING.set(false);
            return;
        }
        BlockingQueue<JpegCode> queue = new LinkedBlockingQueue(5);
        executorService.execute(new PhotoTransCode(queue, paths));
        executorService.execute(new PhotoCheckBlur(queue, paths.size()));
        HwLog.i(JpegNative.TAG, "do task end");
    }

    public int getType() {
        return 56;
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(8388608));
    }

    public boolean isNormal() {
        return false;
    }
}
