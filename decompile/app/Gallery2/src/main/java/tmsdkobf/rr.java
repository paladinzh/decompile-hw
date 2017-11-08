package tmsdkobf;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.SpaceManager;

/* compiled from: Unknown */
public class rr {
    private static int OA = 2;
    private static int OB = 3;
    private static rr OD = null;
    private static final String[] Ot = new String[]{"_id", "_data", "datetaken", "_size"};
    private static final int Ou = dL("_id");
    private static final int Ov = dL("datetaken");
    private static final int Ow = dL("_data");
    private static final int Ox = dL("_size");
    private static int Oz = 1;
    private rs OC;
    private ISpaceScanListener Or;
    private ISpaceScanListener Os;
    private AtomicBoolean Oy;
    byte[] lL;
    private int mState;
    private Handler yO;

    /* compiled from: Unknown */
    public static class a {
        public long OF;
        public long OG;
        public long mInnerPicSize;
        public long mOutPicSize;
        public Pair<Integer, Long> mPhotoCountAndSize;
        public ArrayList<rq> mResultList;
        public Pair<Integer, Long> mScreenShotCountAndSize;
    }

    private rr() {
        this.lL = new byte[0];
        this.Or = null;
        this.Os = null;
        this.mState = 0;
        this.Oy = new AtomicBoolean();
        this.mState = Oz;
        this.Oy.set(false);
        this.yO = new Handler(this, Looper.getMainLooper()) {
            final /* synthetic */ rr OE;

            public void handleMessage(Message message) {
                switch (message.what) {
                    case FragmentTransaction.TRANSIT_FRAGMENT_OPEN /*4097*/:
                        this.OE.Or.onStart();
                        return;
                    case 4098:
                        this.OE.Or.onFound(message.obj);
                        return;
                    case FragmentTransaction.TRANSIT_FRAGMENT_FADE /*4099*/:
                        this.OE.Or.onProgressChanged(message.arg1);
                        return;
                    case 4100:
                        this.OE.Or.onCancelFinished();
                        return;
                    case 4101:
                        this.OE.Or.onFinish(message.arg1, message.obj);
                        return;
                    case 4353:
                        this.OE.Os.onStart();
                        return;
                    case 4354:
                        this.OE.Os.onFound(message.obj);
                        return;
                    case 4355:
                        this.OE.Os.onProgressChanged(message.arg1);
                        return;
                    case 4356:
                        this.OE.Os.onCancelFinished();
                        return;
                    case 4357:
                        this.OE.Os.onFinish(message.arg1, message.obj);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private ArrayList<rq> a(ContentResolver contentResolver, Uri uri, String[] strArr) {
        Cursor query;
        Throwable th;
        Cursor cursor;
        ArrayList<rq> arrayList = new ArrayList();
        try {
            query = contentResolver.query(uri, strArr, null, null, null);
            if (query == null) {
                try {
                    d.c("PhotoManager", "cursor is null!");
                } catch (Throwable th2) {
                    th = th2;
                }
            } else {
                query.moveToFirst();
                int count = query.getCount();
                int i = 0;
                while (!query.isAfterLast() && !this.Oy.get()) {
                    String string = query.getString(Ow);
                    i++;
                    int i2 = (i * 100) / count;
                    Message obtainMessage = this.yO.obtainMessage(4098);
                    obtainMessage.obj = string;
                    this.yO.sendMessage(obtainMessage);
                    obtainMessage = this.yO.obtainMessage(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    obtainMessage.arg1 = i2;
                    this.yO.sendMessage(obtainMessage);
                    if (dM(string)) {
                        if (rp.dF(string)) {
                            arrayList.add(new rq(b(query.getLong(Ov), string), c(query.getLong(Ox), string), string, query.getLong(Ou)));
                            query.moveToNext();
                        }
                    }
                    d.f("PhotoManager", "media file not exist : " + string);
                    query.moveToNext();
                }
            }
            if (query != null) {
                try {
                    query.close();
                } catch (Exception e) {
                }
            }
        } catch (Throwable th3) {
            th = th3;
            query = null;
            if (query != null) {
                query.close();
            }
            throw th;
        }
        return arrayList;
    }

    private static long b(long j, String str) {
        Object obj = null;
        if (j <= 0) {
            obj = 1;
        }
        if (obj == null) {
            return j;
        }
        long dK = !rp.dH(str) ? j : rp.dK(str);
        if (dK == 0) {
            dK = new File(str).lastModified();
        }
        return dK;
    }

    private void b(a aVar) {
        if (aVar.mResultList != null) {
            aVar.mInnerPicSize = 0;
            aVar.OF = 0;
            aVar.mOutPicSize = 0;
            aVar.OG = 0;
            Iterator it = aVar.mResultList.iterator();
            int i = 0;
            long j = 0;
            long j2 = 0;
            int i2 = 0;
            while (it.hasNext()) {
                rq rqVar = (rq) it.next();
                if (rqVar.mIsScreenShot) {
                    j += rqVar.mSize;
                    i++;
                }
                j2 += rqVar.mSize;
                i2++;
                if (rqVar.mIsOut) {
                    aVar.mOutPicSize += rqVar.mSize;
                } else {
                    aVar.mInnerPicSize += rqVar.mSize;
                }
            }
            aVar.mPhotoCountAndSize = new Pair(Integer.valueOf(i2), Long.valueOf(j2));
            aVar.mScreenShotCountAndSize = new Pair(Integer.valueOf(i), Long.valueOf(j));
        }
    }

    private static long c(long j, String str) {
        return ((j > 0 ? 1 : (j == 0 ? 0 : -1)) <= 0 ? 1 : null) == null ? j : new File(str).length();
    }

    private static int dL(String str) {
        int i = 0;
        String[] strArr = Ot;
        int length = strArr.length;
        int i2 = 0;
        while (i < length) {
            if (strArr[i].equals(str)) {
                return i2;
            }
            i2++;
            i++;
        }
        return -1;
    }

    private static boolean dM(String str) {
        return TextUtils.isEmpty(str) ? false : new File(str).exists();
    }

    public static rr jH() {
        if (OD == null) {
            OD = new rr();
        }
        return OD;
    }

    public List<ro> D(ArrayList<rq> arrayList) {
        if (this.OC == null) {
            this.OC = new rs();
        }
        this.mState = OB;
        this.yO.sendMessage(this.yO.obtainMessage(4353));
        long currentTimeMillis = System.currentTimeMillis();
        List<ro> a = this.OC.a((ArrayList) arrayList, this.yO);
        d.f("PhotoManager", "Similar time consume : " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
        return a;
    }

    public void b(ISpaceScanListener iSpaceScanListener) {
        this.Or = iSpaceScanListener;
    }

    public void c(ISpaceScanListener iSpaceScanListener) {
        this.Os = iSpaceScanListener;
    }

    public int jI() {
        if (OA != this.mState) {
            return -1;
        }
        this.Oy.set(true);
        return 1;
    }

    public int jJ() {
        if (OB != this.mState) {
            return -1;
        }
        this.OC.cancel();
        this.mState = Oz;
        return 1;
    }

    public a jK() {
        this.Oy.set(false);
        this.mState = OA;
        this.yO.sendMessage(this.yO.obtainMessage(FragmentTransaction.TRANSIT_FRAGMENT_OPEN));
        long currentTimeMillis = System.currentTimeMillis();
        Object a = a(TMSDKContext.getApplicaionContext().getContentResolver(), Media.EXTERNAL_CONTENT_URI, Ot);
        if (this.Oy.get()) {
            this.yO.sendMessage(this.yO.obtainMessage(4100));
            this.mState = Oz;
            return null;
        }
        if (a.size() == 0) {
            this.mState = Oz;
            d.c("PhotoManager", "no picture was found");
            Message obtainMessage = this.yO.obtainMessage(4101);
            obtainMessage.obj = null;
            obtainMessage.arg1 = SpaceManager.ERROR_CODE_UNKNOW;
            this.yO.sendMessage(obtainMessage);
        }
        rq.I(a);
        if (this.Oy.get()) {
            this.yO.sendMessage(this.yO.obtainMessage(4100));
            this.mState = Oz;
            return null;
        }
        a aVar = new a();
        aVar.mResultList = a;
        b(aVar);
        this.mState = Oz;
        d.f("PhotoManager", "scan time consume : " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
        Message obtainMessage2 = this.yO.obtainMessage(4101);
        obtainMessage2.obj = rd.a(aVar);
        obtainMessage2.arg1 = 0;
        this.yO.sendMessage(obtainMessage2);
        return aVar;
    }
}
