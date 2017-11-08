package com.huawei.gallery.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;
import android.print.PrintDocumentInfo;
import android.print.PrintDocumentInfo.Builder;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class DocumentPrintHelper {
    private static final String LOG_TAG = DocumentPrintHelper.class.getSimpleName();
    private static WeakReference<MyPrintDocumentAdapter> sAdapterRef;
    int mColorMode = 2;
    final Context mContext;
    Options mDecodeOptions = null;
    private final Object mLock = new Object();
    int mOrientation = 1;
    int mScaleMode = 1;

    private class MyPrintDocumentAdapter extends PrintDocumentAdapter {
        private PrintAttributes mAttributes;
        private int mFittingMode;
        private final List<MediaItem> mImageFiles;
        private String mJobName;
        private WriterThread mWriterThread;

        MyPrintDocumentAdapter(List<MediaItem> imageFiles, String jobName, int scaleMode) {
            this.mImageFiles = imageFiles;
            this.mJobName = jobName;
            this.mFittingMode = scaleMode;
        }

        public void onLayout(PrintAttributes oldPrintAttributes, PrintAttributes newPrintAttributes, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
            boolean changed = false;
            this.mAttributes = newPrintAttributes;
            GalleryLog.d(DocumentPrintHelper.LOG_TAG, String.format(" [onLayout] newPrintAttributes->%s", new Object[]{newPrintAttributes}));
            if (cancellationSignal.isCanceled()) {
                layoutResultCallback.onLayoutCancelled();
                return;
            }
            if (this.mImageFiles == null) {
                layoutResultCallback.onLayoutFailed("there is none image need print.");
            } else {
                PrintDocumentInfo info = new Builder(this.mJobName).setContentType(0).setPageCount(this.mImageFiles.size()).build();
                if (!newPrintAttributes.equals(oldPrintAttributes)) {
                    changed = true;
                }
                layoutResultCallback.onLayoutFinished(info, changed);
            }
        }

        public void onFinish() {
            super.onFinish();
            DocumentPrintHelper.this.cancelLoad();
        }

        public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            this.mWriterThread = new WriterThread(this, pageRanges, fileDescriptor, cancellationSignal, writeResultCallback);
            this.mWriterThread.start();
        }

        private void stopWrite() {
            GalleryLog.d(DocumentPrintHelper.LOG_TAG, "stop wirte with thead " + this.mWriterThread);
            if (this.mWriterThread != null) {
                this.mWriterThread.canceled = true;
            }
        }
    }

    private class WriterThread extends Thread {
        volatile boolean canceled = false;
        private CancellationSignal cancellationSignal;
        private ParcelFileDescriptor fileDescriptor;
        private MyPrintDocumentAdapter mAdapter;
        private PageRange[] pageRanges;
        private WriteResultCallback writeResultCallback;

        WriterThread(MyPrintDocumentAdapter adapter, PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            this.mAdapter = adapter;
            this.pageRanges = pageRanges;
            this.fileDescriptor = fileDescriptor;
            this.cancellationSignal = cancellationSignal;
            this.writeResultCallback = writeResultCallback;
        }

        public void run() {
            StringBuffer sb = new StringBuffer("size: ").append(this.pageRanges.length).append("  ");
            for (PageRange page : this.pageRanges) {
                sb.append(page);
            }
            GalleryLog.d(DocumentPrintHelper.LOG_TAG, " [ onWrite ] " + sb.toString());
            PrintedPdfDocument printedPdfDocument = new PrintedPdfDocument(DocumentPrintHelper.this.mContext, this.mAdapter.mAttributes);
            this.cancellationSignal.setOnCancelListener(new OnCancelListener() {
                public void onCancel() {
                    GalleryLog.d(DocumentPrintHelper.LOG_TAG, "cancellationSignal canceled");
                    WriterThread.this.canceled = true;
                    DocumentPrintHelper.this.cancelLoad();
                }
            });
            List<MediaItem> imageFiles = this.mAdapter.mImageFiles;
            Bitmap bitmap = null;
            try {
                long start = System.currentTimeMillis();
                int len = imageFiles.size();
                for (int i = 0; i < len; i++) {
                    GalleryLog.d(DocumentPrintHelper.LOG_TAG, String.format(" [draw] index is %s", new Object[]{Integer.valueOf(i)}));
                    if (DocumentPrintHelper.this.shouldDrawCurrentPage(i, this.pageRanges)) {
                        Page page2 = printedPdfDocument.startPage(i + 1);
                        PageInfo info = page2.getInfo();
                        GalleryLog.d(DocumentPrintHelper.LOG_TAG, String.format(" [draw] page(%sx%s) rect %s, page number is %s", new Object[]{Integer.valueOf(info.getPageWidth()), Integer.valueOf(info.getPageHeight()), info.getContentRect(), Integer.valueOf(info.getPageNumber())}));
                        RectF content = new RectF(page2.getInfo().getContentRect());
                        if (i < imageFiles.size()) {
                            MediaItem item = (MediaItem) imageFiles.get(i);
                            try {
                                bitmap = DocumentPrintHelper.this.loadConstrainedBitmap(item.getContentUri(), 3500);
                                if (this.canceled) {
                                    this.writeResultCallback.onWriteCancelled();
                                    try {
                                        printedPdfDocument.close();
                                    } catch (Exception e) {
                                        GalleryLog.d(DocumentPrintHelper.LOG_TAG, "close document filed !!! " + e.getMessage());
                                        this.writeResultCallback.onWriteFailed("Error close pdfDocument.");
                                    }
                                    Utils.closeSilently(this.fileDescriptor);
                                    return;
                                }
                            } catch (FileNotFoundException e2) {
                                printedPdfDocument.finishPage(page2);
                            }
                            if (bitmap != null) {
                                bitmap = BitmapUtils.rotateBitmap(bitmap, item.getRotation(), true);
                                Matrix matrix = DocumentPrintHelper.this.getMatrix(bitmap.getWidth(), bitmap.getHeight(), content, this.mAdapter.mFittingMode);
                                if (page2.getCanvas() != null) {
                                    page2.getCanvas().drawBitmap(bitmap, matrix, null);
                                }
                                bitmap.recycle();
                            }
                            printedPdfDocument.finishPage(page2);
                            if (this.canceled) {
                                this.writeResultCallback.onWriteCancelled();
                                return;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                GalleryLog.d(DocumentPrintHelper.LOG_TAG, "drawBitmap cost time: " + (System.currentTimeMillis() - start));
                try {
                    start = System.currentTimeMillis();
                    printedPdfDocument.writeTo(new FileOutputStream(this.fileDescriptor.getFileDescriptor()));
                    GalleryLog.d(DocumentPrintHelper.LOG_TAG, "writeToFile cost time: " + (System.currentTimeMillis() - start));
                    if (this.canceled) {
                        this.writeResultCallback.onWriteFailed("operation canceled.");
                    } else {
                        this.writeResultCallback.onWriteFinished(this.pageRanges);
                    }
                } catch (IOException ioe) {
                    GalleryLog.e(DocumentPrintHelper.LOG_TAG, "Error writing printed content." + ioe.getMessage());
                    this.writeResultCallback.onWriteFailed("write content to target FD failed");
                }
                try {
                    printedPdfDocument.close();
                } catch (Exception e3) {
                    GalleryLog.d(DocumentPrintHelper.LOG_TAG, "close document filed !!! " + e3.getMessage());
                    this.writeResultCallback.onWriteFailed("Error close pdfDocument.");
                }
                Utils.closeSilently(this.fileDescriptor);
            } catch (Exception e32) {
                GalleryLog.d(DocumentPrintHelper.LOG_TAG, "Unknow error !!! " + e32.getMessage());
                this.writeResultCallback.onWriteFailed("Unknow error !!! see log.");
            } finally {
                try {
                    printedPdfDocument.close();
                } catch (Exception e322) {
                    GalleryLog.d(DocumentPrintHelper.LOG_TAG, "close document filed !!! " + e322.getMessage());
                    this.writeResultCallback.onWriteFailed("Error close pdfDocument.");
                }
                Utils.closeSilently(this.fileDescriptor);
            }
        }
    }

    public DocumentPrintHelper(Context context) {
        this.mContext = context;
    }

    private Matrix getMatrix(int imageWidth, int imageHeight, RectF content, int fittingMode) {
        Matrix matrix = new Matrix();
        float scale = content.width() / ((float) imageWidth);
        if (fittingMode == 2) {
            scale = Math.max(scale, content.height() / ((float) imageHeight));
        } else {
            scale = Math.min(scale, content.height() / ((float) imageHeight));
        }
        matrix.postScale(scale, scale);
        matrix.postTranslate((content.width() - (((float) imageWidth) * scale)) / 2.0f, (content.height() - (((float) imageHeight) * scale)) / 2.0f);
        return matrix;
    }

    public void printBitmap(String jobName, List<MediaItem> imageFiles) throws FileNotFoundException {
        if (sAdapterRef != null) {
            MyPrintDocumentAdapter oldAdapter = (MyPrintDocumentAdapter) sAdapterRef.get();
            if (oldAdapter != null) {
                oldAdapter.stopWrite();
            }
        }
        PrintDocumentAdapter printDocumentAdapter = new MyPrintDocumentAdapter(imageFiles, jobName, this.mScaleMode);
        PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(this.mColorMode);
        if (this.mOrientation == 1) {
            builder.setMediaSize(MediaSize.UNKNOWN_LANDSCAPE);
        } else if (this.mOrientation == 2) {
            builder.setMediaSize(MediaSize.UNKNOWN_PORTRAIT);
        } else {
            builder.setMediaSize(MediaSize.ISO_A4);
        }
        printManager.print(jobName, printDocumentAdapter, builder.build());
    }

    private void cancelLoad() {
        synchronized (this.mLock) {
            if (this.mDecodeOptions != null) {
                this.mDecodeOptions.requestCancelDecode();
                this.mDecodeOptions = null;
            }
        }
    }

    private boolean shouldDrawCurrentPage(int current, PageRange[] pageRanges) {
        for (PageRange pr : pageRanges) {
            if (pr != null && pr.getStart() <= current && pr.getEnd() >= current) {
                return true;
            }
        }
        return false;
    }

    private Bitmap loadConstrainedBitmap(Uri uri, int maxSideLength) throws FileNotFoundException {
        if (maxSideLength <= 0 || uri == null || this.mContext == null) {
            return null;
        }
        Options opt = new Options();
        opt.inJustDecodeBounds = true;
        loadBitmap(uri, opt);
        int w = opt.outWidth;
        int h = opt.outHeight;
        if (w <= 0 || h <= 0) {
            return null;
        }
        int imageSide = Math.max(w, h);
        int sampleSize = 1;
        while (imageSide > maxSideLength) {
            imageSide >>>= 1;
            sampleSize <<= 1;
        }
        if (sampleSize <= 0 || Math.min(w, h) / sampleSize <= 0) {
            return null;
        }
        synchronized (this.mLock) {
            this.mDecodeOptions = new Options();
            this.mDecodeOptions.inMutable = true;
            this.mDecodeOptions.inSampleSize = sampleSize;
            Options decodeOptions = this.mDecodeOptions;
        }
        try {
            Bitmap loadBitmap = loadBitmap(uri, decodeOptions);
            synchronized (this.mLock) {
                this.mDecodeOptions = null;
            }
            return loadBitmap;
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mDecodeOptions = null;
            }
        }
    }

    private Bitmap loadBitmap(Uri uri, Options o) throws FileNotFoundException {
        if (uri == null || this.mContext == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.getContentResolver().openInputStream(uri);
            Bitmap decodeStream = BitmapFactory.decodeStream(inputStream, null, o);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException t) {
                    GalleryLog.w(LOG_TAG, "close fail. " + t.getMessage());
                }
            }
            return decodeStream;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException t2) {
                    GalleryLog.w(LOG_TAG, "close fail. " + t2.getMessage());
                }
            }
        }
    }
}
