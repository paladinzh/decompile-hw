package tmsdk.fg.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class ManagerCreatorF {
    private static volatile ManagerCreatorF Ls = null;
    private Context mContext;
    private HashMap<Class<? extends jg>, jg> wJ = new HashMap();
    private HashMap<Class<? extends jg>, WeakReference<? extends jg>> wK = new HashMap();

    private ManagerCreatorF(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerF> T d(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (cls) {
                BaseManagerF baseManagerF;
                t = (BaseManagerF) cls.cast(this.wJ.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.wK.get(cls);
                    if (weakReference != null) {
                        baseManagerF = (BaseManagerF) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        baseManagerF = (BaseManagerF) cls.newInstance();
                        baseManagerF.onCreate(this.mContext);
                        if (baseManagerF.getSingletonType() == 1) {
                            this.wJ.put(cls, baseManagerF);
                        } else if (baseManagerF.getSingletonType() == 0) {
                            this.wK.put(cls, new WeakReference(baseManagerF));
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return t;
        }
        throw new NullPointerException("the param of getManager can't be null.");
    }

    public static <T extends BaseManagerF> T getManager(Class<T> cls) {
        return iY().d(cls);
    }

    static ManagerCreatorF iY() {
        if (Ls == null) {
            synchronized (ManagerCreatorF.class) {
                if (Ls == null) {
                    Ls = new ManagerCreatorF(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return Ls;
    }
}
