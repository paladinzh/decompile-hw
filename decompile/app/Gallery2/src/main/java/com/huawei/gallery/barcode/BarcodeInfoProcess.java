package com.huawei.gallery.barcode;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.util.Log;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class BarcodeInfoProcess {
    private static int MAX_SCAN_REQUEST = 10;
    private static int MSG_BARCODE_PROCESS = 1;
    private static BarcodeInfoProcess mBarcodeProcess;
    private ReceivedBarcodeResultListener mBarcodeResultListener;
    private Handler mHandler;
    private MediaItem mMediaItem = null;
    private ResultReceiver mResultReciever = new ResultReceiver(new Handler()) {
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.d("BarcodeInfoProcess", "onReceiveResult! " + resultCode);
            if (resultData != null && BarcodeInfoProcess.this.requestCode == resultCode) {
                Intent receiver = (Intent) resultData.getParcelable("ResultIntent");
                if (receiver != null) {
                    int resultBitMapWidth = receiver.getIntExtra("ImageWidth", -1);
                    int resultBitMapHeight = receiver.getIntExtra("ImageHeight", -1);
                    Rect resultRect = (Rect) receiver.getParcelableExtra("QrcodeRect");
                    if (resultRect != null) {
                        Log.d("BarcodeInfoProcess", "onReceiveResult  width:" + resultBitMapWidth + " height:" + resultBitMapHeight + " rect top:" + resultRect.top + " left:" + resultRect.left + " rect right:" + resultRect.right + " bottom:" + resultRect.bottom);
                        Object[] barcodeScanResult = new Object[]{receiver, resultRect};
                        BarcodeInfoProcess.this.mMediaItem.setBarcodeScanFlag(true);
                        BarcodeInfoProcess.this.mMediaItem.setBarcodeResult(BarcodeInfoProcess.this.generateBarcodeScanResult(barcodeScanResult, resultBitMapWidth, resultBitMapHeight));
                        if (BarcodeInfoProcess.this.mBarcodeResultListener != null) {
                            BarcodeInfoProcess.this.mBarcodeResultListener.onBarcodeResultReceived();
                        }
                    }
                }
            }
        }
    };
    private BarcodeScanQueue mScanQueue = new BarcodeScanQueue();
    private HandlerThread mThread = new HandlerThread("BarcodeInfo process thread", 10);
    private int requestCode;

    public interface ReceivedBarcodeResultListener {
        void onBarcodeResultReceived();
    }

    private static class BarcodeScanQueue {
        private LinkedList<WeakReference<MediaItem>> mQueue;

        private BarcodeScanQueue() {
            this.mQueue = new LinkedList();
        }

        public void addScanReqAtFront(MediaItem scanItem) {
            WeakReference<MediaItem> request = new WeakReference(scanItem);
            synchronized (this.mQueue) {
                if (this.mQueue.contains(request)) {
                    this.mQueue.remove(request);
                }
                if (this.mQueue.size() >= BarcodeInfoProcess.MAX_SCAN_REQUEST) {
                    this.mQueue.removeLast();
                }
                this.mQueue.addFirst(request);
            }
        }

        public WeakReference<MediaItem> getScanReqAtFront() {
            synchronized (this.mQueue) {
                if (this.mQueue.size() > 0) {
                    WeakReference<MediaItem> weakReference = (WeakReference) this.mQueue.pollFirst();
                    return weakReference;
                }
                return null;
            }
        }
    }

    public void setBarcodeResultListener(ReceivedBarcodeResultListener listener) {
        this.mBarcodeResultListener = listener;
    }

    public static synchronized BarcodeInfoProcess newInstance() {
        BarcodeInfoProcess barcodeInfoProcess;
        synchronized (BarcodeInfoProcess.class) {
            if (mBarcodeProcess == null) {
                mBarcodeProcess = new BarcodeInfoProcess();
            }
            barcodeInfoProcess = mBarcodeProcess;
        }
        return barcodeInfoProcess;
    }

    private void bindService(int requestCode, Uri uri) {
        ComponentName cn = new ComponentName("com.huawei.scanner", "com.huawei.scanner.ScannerService");
        try {
            Intent intent = new Intent();
            intent.setComponent(cn);
            intent.putExtra("ResultReceiver", receiverForSending(this.mResultReciever));
            intent.putExtra("PackageName", GalleryUtils.getContext().getPackageName());
            intent.setData(uri);
            intent.putExtra("RequestCode", requestCode);
            intent.putExtra("ScannerMode", "QrCode");
            intent.addFlags(1);
            GalleryLog.d("BarcodeInfoProcess", "start service!!!");
            GalleryUtils.getContext().startService(intent);
        } catch (SecurityException e) {
            GalleryLog.d("BarcodeInfoProcess", "URI need permission");
        } catch (Exception e2) {
            GalleryLog.d("BarcodeInfoProcess", "Barcode scan exception " + e2.toString());
        }
    }

    private ResultReceiver receiverForSending(ResultReceiver actualReceiver) {
        Parcel parcel = Parcel.obtain();
        actualReceiver.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    private BarcodeInfoProcess() {
        this.mThread.start();
        this.mHandler = new Handler(this.mThread.getLooper()) {
            public void handleMessage(Message msg) {
                MediaItem mediaItem = null;
                if (msg.what == BarcodeInfoProcess.MSG_BARCODE_PROCESS) {
                    WeakReference<MediaItem> reference = BarcodeInfoProcess.this.mScanQueue.getScanReqAtFront();
                    if (reference != null) {
                        mediaItem = (MediaItem) reference.get();
                    }
                    if (mediaItem == null) {
                        return;
                    }
                    if ((mediaItem instanceof GalleryImage) || (mediaItem instanceof LocalImage)) {
                        if (mediaItem.isDrm()) {
                        }
                        BarcodeInfoProcess.this.requestCode = GalleryUtils.getBucketId(mediaItem.getPath().toString());
                        BarcodeInfoProcess.this.bindService(BarcodeInfoProcess.this.requestCode, mediaItem.getContentUri());
                        Log.d("BarcodeInfoProcess", " requestCode :" + BarcodeInfoProcess.this.requestCode + " Path:" + mediaItem.getFilePath());
                        BarcodeInfoProcess.this.mMediaItem = mediaItem;
                    }
                }
            }
        };
    }

    public synchronized void scan(MediaItem scanItem) {
        if (scanItem != null) {
            Message msg = this.mHandler.obtainMessage(MSG_BARCODE_PROCESS);
            this.mScanQueue.addScanReqAtFront(scanItem);
            this.mHandler.sendMessageAtFrontOfQueue(msg);
        }
    }

    private BarcodeScanResultItem generateBarcodeScanResult(Object[] result, int width, int height) {
        BarcodeScanResultItem resultItem = new BarcodeScanResultItem();
        resultItem.setBarcodeScanResult(result);
        resultItem.setBitmapWidth(width);
        resultItem.setBitmapHeight(height);
        return resultItem;
    }
}
