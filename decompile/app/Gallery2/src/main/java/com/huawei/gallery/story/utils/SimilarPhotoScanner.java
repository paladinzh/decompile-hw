package com.huawei.gallery.story.utils;

import android.content.ContentResolver;
import android.content.Context;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMSService;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult.PhotoSimilarBucketItem;
import tmsdk.fg.module.spacemanager.SpaceManager;

public class SimilarPhotoScanner {
    private static final MyPrinter LOG = new MyPrinter("Clustering_SimilarPhotoScan");
    static ITMSApplicaionConfig sITMSApplicaionConfig = new ITMSApplicaionConfig() {
        public HashMap<String, String> config(Map<String, String> src) {
            return new HashMap(src);
        }
    };
    private ContentResolver mContentResolver;
    private boolean mInitResult = false;
    private SpaceManager mPhotoManager;
    private SimilarScanFinishListener mScanfinishListener;
    final ISpaceScanListener similarImageResultListener = new ISpaceScanListener() {
        public void onStart() {
            SimilarPhotoScanner.LOG.d("scan on start");
        }

        public void onProgressChanged(int percent) {
        }

        public void onFound(Object obj) {
        }

        public void onFinish(int aErrorCode, Object obj) {
            SimilarPhotoScanner.LOG.d("scan onFinish :  aErrorCode = " + aErrorCode);
            ArrayList<Integer> similarIdList = new ArrayList();
            if (aErrorCode != 0) {
                SimilarPhotoScanner.this.mScanfinishListener.onFinished(similarIdList);
                return;
            }
            ArrayList<PhotoSimilarResult> photoSimilarResults = (ArrayList) obj;
            SimilarPhotoScanner.LOG.d("result size is " + photoSimilarResults.size());
            for (PhotoSimilarResult result : photoSimilarResults) {
                for (PhotoSimilarBucketItem item : result.mItemList) {
                    if (item.mSelected) {
                        similarIdList.add(Integer.valueOf((int) item.mId));
                    }
                }
            }
            SimilarPhotoScanner.LOG.d("scan onFinish done!");
            SimilarPhotoScanner.this.mScanfinishListener.onFinished(similarIdList);
        }

        public void onCancelFinished() {
            SimilarPhotoScanner.LOG.d("scan cancelled");
        }
    };

    public interface SimilarScanFinishListener {
        void onFinished(ArrayList<Integer> arrayList);
    }

    public SimilarPhotoScanner(Context context) {
        this.mContentResolver = context.getContentResolver();
        initTMSDK(context);
        this.mPhotoManager = (SpaceManager) ManagerCreatorF.getManager(SpaceManager.class);
    }

    private void initTMSDK(Context context) {
        LOG.d("init begin");
        TMSDKContext.setTMSDKLogEnable(false);
        long start = System.currentTimeMillis();
        TMSDKContext.setAutoConnectionSwitch(true);
        try {
            this.mInitResult = TMSDKContext.init(context.getApplicationContext(), TMSService.class, sITMSApplicaionConfig);
        } catch (RuntimeException e) {
            LOG.d("init tmsdk fail, error info : " + e.getMessage());
        }
        LOG.d("TMSDK init spend =" + (System.currentTimeMillis() - start));
        LOG.d("init result =" + this.mInitResult);
    }

    public void scanSimilarPhotos(String storyId, SimilarScanFinishListener finishListener) {
        if (this.mInitResult) {
            this.mScanfinishListener = finishListener;
            ArrayList<PhotoItem> photos = StoryAlbumUtils.queryStoryAlbumPhotoItem(storyId, this.mContentResolver);
            LOG.d("photoSimilarCategorise start: " + storyId + " photo size is " + photos.size());
            this.mPhotoManager.photoSimilarCategorise(this.similarImageResultListener, photos);
            return;
        }
        finishListener.onFinished(new ArrayList());
    }
}
