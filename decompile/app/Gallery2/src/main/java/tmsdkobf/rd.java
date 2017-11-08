package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.OfflineVideo;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.spacemanager.FileMedia;
import tmsdk.fg.module.spacemanager.FileScanResult;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.PhotoScanResult;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult;
import tmsdk.fg.module.spacemanager.PhotoSimilarResult.PhotoSimilarBucketItem;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles;
import tmsdkobf.ru.a;

/* compiled from: Unknown */
public class rd extends BaseManagerF {
    ru ME;
    ri MF;
    Runnable MG;
    Runnable MH;
    a MI;
    a MJ;
    public ISpaceScanListener MK;
    public ISpaceScanListener ML;
    private SpaceManager MM;
    private rr MN = rr.jH();
    Context mContext = TMSDKContext.getApplicaionContext();
    Object mLock = new Object();
    Handler yO = new Handler(Looper.getMainLooper());

    public rd() {
        init();
    }

    private List<WeChatCacheFiles> E(List<rj> list) {
        List<WeChatCacheFiles> arrayList = new ArrayList();
        for (rj rjVar : list) {
            if (rjVar.mFileModes.size() > 0) {
                arrayList.add(rjVar.jG());
            }
        }
        return arrayList;
    }

    public static ArrayList<rq> F(List<PhotoItem> list) {
        ArrayList<rq> arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            PhotoItem photoItem = (PhotoItem) list.get(i);
            arrayList.add(new rq(photoItem.mTime, photoItem.mSize, photoItem.mPath, photoItem.mDbId));
        }
        return arrayList;
    }

    public static List<PhotoSimilarResult> G(List<ro> list) {
        List arrayList = new ArrayList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ro roVar = (ro) list.get(i);
            PhotoSimilarResult photoSimilarResult = new PhotoSimilarResult();
            photoSimilarResult.mItemList = new ArrayList();
            photoSimilarResult.mTime = roVar.mTime;
            photoSimilarResult.mTimeString = roVar.mTimeString;
            int size2 = roVar.mItemList.size();
            for (int i2 = 0; i2 < size2; i2++) {
                PhotoSimilarBucketItem photoSimilarBucketItem = new PhotoSimilarBucketItem();
                photoSimilarBucketItem.mId = ((ro.a) roVar.mItemList.get(i2)).mDbId;
                photoSimilarBucketItem.mPath = ((ro.a) roVar.mItemList.get(i2)).mPath;
                photoSimilarBucketItem.mFileSize = ((ro.a) roVar.mItemList.get(i2)).mSize;
                photoSimilarBucketItem.mSelected = ((ro.a) roVar.mItemList.get(i2)).mSelected;
                photoSimilarResult.mItemList.add(photoSimilarBucketItem);
            }
            arrayList.add(photoSimilarResult);
        }
        return arrayList;
    }

    public static PhotoScanResult a(rr.a aVar) {
        PhotoScanResult photoScanResult = new PhotoScanResult();
        photoScanResult.mInnerPicSize = aVar.mInnerPicSize;
        photoScanResult.mOutPicSize = aVar.mOutPicSize;
        photoScanResult.mPhotoCountAndSize = new Pair(Integer.valueOf(((Integer) aVar.mPhotoCountAndSize.first).intValue()), Long.valueOf(((Long) aVar.mPhotoCountAndSize.second).longValue()));
        photoScanResult.mScreenShotCountAndSize = new Pair(Integer.valueOf(((Integer) aVar.mScreenShotCountAndSize.first).intValue()), Long.valueOf(((Long) aVar.mScreenShotCountAndSize.second).longValue()));
        photoScanResult.mResultList = new ArrayList();
        int size = aVar.mResultList.size();
        for (int i = 0; i < size; i++) {
            rq rqVar = (rq) aVar.mResultList.get(i);
            PhotoItem photoItem = new PhotoItem();
            photoItem.mDbId = rqVar.mDbId;
            photoItem.mIsOut = rqVar.mIsOut;
            photoItem.mIsScreenShot = rqVar.mIsScreenShot;
            photoItem.mPath = rqVar.mPath;
            photoItem.mSize = rqVar.mSize;
            photoItem.mTime = rqVar.mTime;
            photoScanResult.mResultList.add(photoItem);
        }
        return photoScanResult;
    }

    private void a(a aVar, FileScanResult fileScanResult, boolean z) {
        List<OfflineVideo> bw = new hh(null, z).bw();
        if (bw != null && bw.size() != 0) {
            long j = 0;
            List arrayList = new ArrayList();
            rf rfVar = null;
            for (OfflineVideo offlineVideo : bw) {
                if (rfVar == null) {
                    rfVar = new rf();
                    rfVar.jz();
                }
                if (TextUtils.isEmpty(offlineVideo.mAppName)) {
                    String toLowerCase = re.a(offlineVideo.mPath, rf.MY).toLowerCase();
                    offlineVideo.mPackage = rfVar.f(toLowerCase, z);
                    offlineVideo.mAppName = rfVar.g(toLowerCase, z);
                }
                FileMedia fileMedia = new FileMedia();
                fileMedia.type = 2;
                fileMedia.mPath = offlineVideo.mPath;
                fileMedia.title = offlineVideo.mTitle;
                fileMedia.pkg = offlineVideo.mPackage;
                fileMedia.mSrcName = offlineVideo.mAppName;
                fileMedia.mSize = offlineVideo.mSize;
                fileMedia.mOfflineVideo = offlineVideo;
                j += fileMedia.mSize;
                arrayList.add(fileMedia);
                if (aVar != null) {
                    aVar.a(j, fileMedia);
                }
            }
            if (fileScanResult.mVideoFiles != null) {
                arrayList.addAll(fileScanResult.mVideoFiles);
            }
            fileScanResult.mVideoFiles = arrayList;
        }
    }

    private void init() {
        js();
        jv();
    }

    private void js() {
        this.MG = new Runnable(this) {
            final /* synthetic */ rd MO;

            {
                this.MO = r1;
            }

            public void run() {
                this.MO.jx();
                boolean isENG = ((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG();
                if (this.MO.MK != null) {
                    this.MO.yO.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 MP;

                        {
                            this.MP = r1;
                        }

                        public void run() {
                            this.MP.MO.MK.onStart();
                        }
                    });
                    this.MO.ME = new ru(this.MO.mContext, this.MO.MI, 263);
                    final FileScanResult S = this.MO.ME.S(isENG);
                    this.MO.a(this.MO.MI, S, isENG);
                    synchronized (this.MO.mLock) {
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass1 MP;

                            public void run() {
                                this.MP.MO.MK.onFinish(0, S);
                            }
                        });
                    }
                }
            }
        };
    }

    private void jv() {
        this.MH = new Runnable(this) {
            final /* synthetic */ rd MO;

            {
                this.MO = r1;
            }

            public void run() {
                this.MO.jw();
            }
        };
    }

    private boolean jw() {
        jy();
        if (this.ML == null) {
            return false;
        }
        this.yO.post(new Runnable(this) {
            final /* synthetic */ rd MO;

            {
                this.MO = r1;
            }

            public void run() {
                this.MO.ML.onStart();
            }
        });
        List R = rg.R(((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        this.MF = new ri(R, 1);
        if (this.MJ == null) {
            rg.ND = null;
            this.yO.post(new Runnable(this) {
                final /* synthetic */ rd MO;

                {
                    this.MO = r1;
                }

                public void run() {
                    this.MO.ML.onFinish(-2, null);
                    this.MO.ML = null;
                }
            });
            return false;
        } else if (this.MF.a(this.MJ)) {
            R = E(R);
            this.yO.post(new Runnable(this) {
                final /* synthetic */ rd MO;

                public void run() {
                    this.MO.ML.onFinish(0, R);
                }
            });
            this.MF = null;
            rg.ND = null;
            return true;
        } else {
            rg.ND = null;
            this.yO.post(new Runnable(this) {
                final /* synthetic */ rd MO;

                {
                    this.MO = r1;
                }

                public void run() {
                    this.MO.ML.onFinish(-1, null);
                    this.MO.ML = null;
                }
            });
            return false;
        }
    }

    private void jx() {
        if (this.MI == null) {
            this.MI = new a(this) {
                final /* synthetic */ rd MO;

                {
                    this.MO = r1;
                }

                public void a(long j, final Object obj) {
                    if (this.MO.MK != null) {
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass7 MT;

                            public void run() {
                                this.MT.MO.MK.onFound(obj);
                            }
                        });
                    }
                }

                public void onCancel() {
                    if (this.MO.MK != null) {
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass7 MT;

                            {
                                this.MT = r1;
                            }

                            public void run() {
                                this.MT.MO.MK.onCancelFinished();
                            }
                        });
                    }
                }

                public void onProgressChanged(int i) {
                    if (this.MO.MK != null) {
                        if (i >= 100) {
                            i = 99;
                        }
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass7 MT;

                            public void run() {
                                this.MT.MO.MK.onProgressChanged(i);
                            }
                        });
                    }
                }
            };
        }
    }

    private void jy() {
        if (this.MJ == null) {
            this.MJ = new a(this) {
                final /* synthetic */ rd MO;

                {
                    this.MO = r1;
                }

                public void a(long j, final Object obj) {
                    if (this.MO.ML != null) {
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass8 MV;

                            public void run() {
                                this.MV.MO.ML.onFound(obj);
                            }
                        });
                    }
                }

                public void onCancel() {
                    if (this.MO.ML != null) {
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass8 MV;

                            {
                                this.MV = r1;
                            }

                            public void run() {
                                this.MV.MO.ML.onCancelFinished();
                            }
                        });
                    }
                }

                public void onProgressChanged(final int i) {
                    if (this.MO.ML != null) {
                        this.MO.yO.post(new Runnable(this) {
                            final /* synthetic */ AnonymousClass8 MV;

                            public void run() {
                                this.MV.MO.ML.onProgressChanged(i);
                            }
                        });
                    }
                }
            };
        }
    }

    public void a(ISpaceScanListener iSpaceScanListener) {
        this.MN.b(iSpaceScanListener);
        this.MN.jK();
        ma.bx(29992);
    }

    public void a(ISpaceScanListener iSpaceScanListener, List<PhotoItem> list) {
        this.MN.c(iSpaceScanListener);
        this.MN.D(F(list));
        ma.bx(29993);
    }

    public void a(SpaceManager spaceManager) {
        this.MM = spaceManager;
    }

    public void jq() {
        jq.ct().a(this.MG, "scanbigfile");
        ma.bx(29990);
    }

    public void jr() {
        if (this.ME != null) {
            this.ME.jM();
        }
    }

    public void jt() {
        jq.ct().a(this.MH, "startWeChatScan");
        ma.bx(29991);
    }

    public void ju() {
        if (this.MF != null) {
            this.MF.bf();
            this.MF.NI = true;
        }
    }

    public void onCreate(Context context) {
    }

    public int stopPhotoScan() {
        return this.MN.jI();
    }

    public int stopPhotoSimilarCategorise() {
        return this.MN.jJ();
    }
}
