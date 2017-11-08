package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.PhotoScanResult;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult.PhotoSimilarBucketItem;
import tmsdk.fg.module.spacemanager.SpaceManager;

public class SimilarPhotoTask extends Task {
    private static final String TAG = "SimilarPhotoTask";
    private static final String TASK_NAME = "SimilarPhotoTask";
    private CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private PhotoScanResult mPhotoScanResult;
    private SimpleSpaceScanListener mScanListener = new SimpleSpaceScanListener() {
        public void onFinish(int aErrorCode, Object obj) {
            super.onFinish(aErrorCode, obj);
            if (aErrorCode == 0 && (obj instanceof PhotoScanResult)) {
                SimilarPhotoTask.this.mPhotoScanResult = (PhotoScanResult) obj;
            }
            SimilarPhotoTask.this.mCountDownLatch.countDown();
        }
    };
    private SimpleSpaceScanListener mSimilarListener = new SimpleSpaceScanListener() {
        public void onFinish(int aErrorCode, Object obj) {
            super.onFinish(aErrorCode, obj);
            if (aErrorCode == 0) {
                for (Trash trash : SimilarPhotoTask.this.createPhotoTrashGroup((List) obj)) {
                    SimilarPhotoTask.this.onPublishItemUpdate(trash);
                }
            }
            SimilarPhotoTask.this.onPublishEnd();
        }
    };
    private SpaceManager mSpaceManager = ((SpaceManager) ManagerCreatorF.getManager(SpaceManager.class));

    private static class SimpleSpaceScanListener implements ISpaceScanListener {
        private SimpleSpaceScanListener() {
        }

        public void onStart() {
            HwLog.i("SimilarPhotoTask", "onStart");
        }

        public void onFound(Object o) {
        }

        public void onFinish(int aErrorCode, Object obj) {
            HwLog.i("SimilarPhotoTask", "onFinish, aErrorCode: " + aErrorCode);
        }

        public void onCancelFinished() {
            HwLog.i("SimilarPhotoTask", "onCancelFinished");
        }

        public void onProgressChanged(int i) {
        }
    }

    private TrashGroup createPhotoTrashGroup(List<PhotoSimilarResult> photoSimilarResults) {
        TrashGroup similarPhotoTrashGroup = new TrashGroup(4194304);
        for (PhotoSimilarResult result : photoSimilarResults) {
            similarPhotoTrashGroup.addChildList(createPhotoList(result.mItemList));
        }
        return similarPhotoTrashGroup;
    }

    private List<SimilarPhotoTrash> createPhotoList(List<PhotoSimilarBucketItem> resultList) {
        List<SimilarPhotoTrash> list = new ArrayList();
        for (PhotoSimilarBucketItem item : resultList) {
            list.add(SimilarPhotoTrash.creator(item));
        }
        return list;
    }

    public SimilarPhotoTask(Context ctx) {
        super(ctx);
    }

    protected void doTask(ScanParams p) {
        HwLog.i("SimilarPhotoTask", "doTask start");
        this.mSpaceManager.photoScan(this.mScanListener);
        try {
            if (this.mCountDownLatch.await(5, TimeUnit.SECONDS)) {
                HwLog.i("SimilarPhotoTask", "mCountDownLatch end");
            }
        } catch (InterruptedException e) {
            HwLog.i("SimilarPhotoTask", "interrupted exception");
        }
        if (this.mPhotoScanResult != null) {
            this.mSpaceManager.photoSimilarCategorise(this.mSimilarListener, this.mPhotoScanResult.mResultList);
        } else {
            onPublishEnd();
        }
        HwLog.i("SimilarPhotoTask", "doTask end");
    }

    public void cancel() {
        if (isEnd()) {
            HwLog.i("SimilarPhotoTask", "cancel, task is already end");
            return;
        }
        setCanceled(true);
        this.mSpaceManager.stopPhotoScan();
        this.mSpaceManager.stopPhotoSimilarCategorise();
    }

    protected int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_trash_apk_file);
    }

    public String getTaskName() {
        return "SimilarPhotoTask";
    }

    public int getType() {
        return 55;
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(4194304));
    }

    public boolean isNormal() {
        return false;
    }
}
