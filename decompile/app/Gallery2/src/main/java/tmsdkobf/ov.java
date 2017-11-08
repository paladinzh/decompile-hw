package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
public class ov extends BaseManagerC {
    public static String TAG = "SharkNetService";
    private qt CR;
    private qo EV;
    private ExecutorService EW;
    private Handler yB = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ ov EX;

        public void handleMessage(Message message) {
            c cVar;
            a aVar;
            switch (message.what) {
                case 1:
                    cVar = (c) message.obj;
                    if (!cVar.EZ.dC()) {
                        cVar.EZ.setState(1);
                        this.EX.EW.submit(new d(this.EX, cVar));
                        break;
                    }
                    return;
                case 2:
                    aVar = (a) message.obj;
                    if (!aVar.EZ.dC()) {
                        aVar.EZ.setState(1);
                        this.EX.EW.submit(new b(this.EX, aVar));
                        break;
                    }
                    return;
                case 3:
                    cVar = (c) message.obj;
                    if (!(cVar == null || cVar.Fg == null)) {
                        cVar.Fg.a(cVar.EY, cVar.Ff);
                        break;
                    }
                case 4:
                    aVar = (a) message.obj;
                    if (!(aVar == null || aVar.Fb == null)) {
                        aVar.Fb.a(aVar.EY, aVar.Fa);
                        break;
                    }
            }
        }
    };

    /* compiled from: Unknown */
    static class a {
        public int EY;
        public ll EZ;
        public List<qs> Fa;
        public le Fb;
        public int qM;
    }

    /* compiled from: Unknown */
    class b implements Runnable {
        final /* synthetic */ ov EX;
        public a Fc;

        public b(ov ovVar, a aVar) {
            this.EX = ovVar;
            this.Fc = aVar;
        }

        public void run() {
            if (this.Fc != null && this.Fc.Fa != null) {
                final le leVar = this.Fc.Fb;
                this.Fc.EY = this.EX.EV.w(this.Fc.Fa);
                if (this.Fc.EZ != null) {
                    this.Fc.EZ.setState(2);
                }
                if (leVar != null) {
                    switch (lk.bf(this.Fc.qM)) {
                        case 8:
                            Message obtain = Message.obtain();
                            obtain.obj = this.Fc;
                            obtain.what = 4;
                            this.EX.yB.sendMessage(obtain);
                            break;
                        case 16:
                            leVar.a(this.Fc.EY, this.Fc.Fa);
                            break;
                        default:
                            jq.ct().a(new Runnable(this) {
                                final /* synthetic */ b Fe;

                                public void run() {
                                    leVar.a(this.Fe.Fc.EY, this.Fe.Fc.Fa);
                                }
                            }, "run callback");
                            break;
                    }
                }
            }
        }
    }

    /* compiled from: Unknown */
    static class c {
        public int EY;
        public ll EZ = new ll();
        public qs Ff;
        public ld Fg;
        public int qM;

        public c(qs qsVar, ld ldVar, int i) {
            this.Ff = qsVar;
            this.Fg = ldVar;
            this.qM = i;
        }
    }

    /* compiled from: Unknown */
    class d implements Runnable {
        final /* synthetic */ ov EX;
        public c Fh;

        public d(ov ovVar, c cVar) {
            this.EX = ovVar;
            this.Fh = cVar;
        }

        public void run() {
            if (this.Fh != null && this.Fh.Ff != null) {
                final ld ldVar = this.Fh.Fg;
                this.Fh.EY = this.EX.EV.a(this.Fh.Ff);
                tmsdk.common.utils.d.g(ov.TAG, "runHttpSession err : " + this.Fh.EY);
                if (this.Fh.EZ != null) {
                    this.Fh.EZ.setState(2);
                }
                if (ldVar != null) {
                    switch (lk.bf(this.Fh.qM)) {
                        case 8:
                            Message obtain = Message.obtain();
                            obtain.obj = this.Fh;
                            obtain.what = 3;
                            this.EX.yB.sendMessage(obtain);
                            break;
                        case 16:
                            ldVar.a(this.Fh.EY, this.Fh.Ff);
                            break;
                        default:
                            jq.ct().a(new Runnable(this) {
                                final /* synthetic */ d Fj;

                                public void run() {
                                    ldVar.a(this.Fj.Fh.EY, this.Fj.Fh.Ff);
                                }
                            }, "run callback");
                            break;
                    }
                }
            }
        }
    }

    WeakReference<ll> a(long j, qs qsVar, int i, ld ldVar) {
        tmsdk.common.utils.d.g(TAG, "ident : " + j + " sendOldProtocol ");
        if (qsVar == null) {
            return null;
        }
        c cVar = new c(qsVar, ldVar, i);
        Message obtain = Message.obtain();
        obtain.obj = cVar;
        obtain.what = 1;
        this.yB.sendMessage(obtain);
        return new WeakReference(cVar.EZ);
    }

    WeakReference<ll> a(long j, qs qsVar, ld ldVar) {
        return a(j, qsVar, 0, ldVar);
    }

    public void onCreate(Context context) {
        this.EW = Executors.newSingleThreadExecutor();
        this.CR = (qt) ManagerCreatorC.getManager(qt.class);
        this.EV = this.CR.ic();
    }
}
