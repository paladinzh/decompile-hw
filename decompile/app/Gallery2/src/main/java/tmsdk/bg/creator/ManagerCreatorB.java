package tmsdk.bg.creator;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class ManagerCreatorB {
    private static volatile ManagerCreatorB wI = null;
    private Context mContext;
    private HashMap<Class<? extends jg>, jg> wJ = new HashMap();
    private HashMap<Class<? extends jg>, WeakReference<? extends jg>> wK = new HashMap();

    private ManagerCreatorB(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private <T extends BaseManagerB> T a(Class<T> cls) {
        if (cls != null) {
            T t;
            synchronized (cls) {
                BaseManagerB baseManagerB;
                t = (BaseManagerB) cls.cast(this.wJ.get(cls));
                if (t == null) {
                    WeakReference weakReference = (WeakReference) this.wK.get(cls);
                    if (weakReference != null) {
                        baseManagerB = (BaseManagerB) cls.cast(weakReference.get());
                    }
                }
                if (t == null) {
                    try {
                        baseManagerB = (BaseManagerB) cls.newInstance();
                        baseManagerB.onCreate(this.mContext);
                        if (baseManagerB.getSingletonType() == 1) {
                            synchronized (ManagerCreatorB.class) {
                                this.wJ.put(cls, baseManagerB);
                            }
                        } else if (baseManagerB.getSingletonType() == 0) {
                            this.wK.put(cls, new WeakReference(baseManagerB));
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

    private void b(Class<? extends jg> cls) {
        synchronized (ManagerCreatorB.class) {
            this.wJ.remove(cls);
        }
    }

    static ManagerCreatorB dG() {
        if (wI == null) {
            synchronized (ManagerCreatorB.class) {
                if (wI == null) {
                    wI = new ManagerCreatorB(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return wI;
    }

    public static void destroyManager(BaseManagerB baseManagerB) {
        if (baseManagerB != null) {
            dG().b(baseManagerB.getClass());
        }
    }

    public static <T extends BaseManagerB> T getManager(Class<T> cls) {
        return dG().a(cls);
    }
}
