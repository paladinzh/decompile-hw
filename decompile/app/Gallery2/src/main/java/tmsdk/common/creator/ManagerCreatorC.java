package tmsdk.common.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class ManagerCreatorC {
    private static volatile ManagerCreatorC AQ = null;
    private Context mContext;
    private final Object mLock = new Object();
    private HashMap<Class<? extends jg>, jg> wJ = new HashMap();
    private HashMap<Class<? extends jg>, WeakReference<? extends jg>> wK = new HashMap();

    private ManagerCreatorC(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerC> T c(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (this.mLock) {
                BaseManagerC baseManagerC;
                t = (BaseManagerC) cls.cast(this.wJ.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.wK.get(cls);
                    if (weakReference != null) {
                        baseManagerC = (BaseManagerC) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        baseManagerC = (BaseManagerC) cls.newInstance();
                        baseManagerC.onCreate(this.mContext);
                        if (baseManagerC.getSingletonType() == 1) {
                            this.wJ.put(cls, baseManagerC);
                        } else if (baseManagerC.getSingletonType() == 0) {
                            this.wK.put(cls, new WeakReference(baseManagerC));
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

    static ManagerCreatorC eT() {
        if (AQ == null) {
            synchronized (ManagerCreatorC.class) {
                if (AQ == null) {
                    AQ = new ManagerCreatorC(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return AQ;
    }

    public static <T extends BaseManagerC> T getManager(Class<T> cls) {
        return eT().c(cls);
    }
}
