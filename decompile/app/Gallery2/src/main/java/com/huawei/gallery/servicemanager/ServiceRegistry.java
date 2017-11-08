package com.huawei.gallery.servicemanager;

import android.app.Application;
import com.android.gallery3d.settings.HicloudAccountManager;
import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private static Object[] sCache = new Object[sCacheSize];
    private static int sCacheSize;
    private static Map<String, CachedComponentCreator<?>> sServices = new HashMap(10);

    private interface ComponentCreator<T> {
        T create(Application application);
    }

    private static abstract class CachedComponentCreator<T> implements ComponentCreator<T> {
        private int mCacheIndex;

        public CachedComponentCreator() {
            int -get1 = ServiceRegistry.sCacheSize;
            ServiceRegistry.sCacheSize = -get1 + 1;
            this.mCacheIndex = -get1;
        }

        public final T getComponent(Application context) {
            Object cache = ServiceRegistry.sCache[this.mCacheIndex];
            synchronized (ServiceRegistry.sCache) {
                if (cache == null) {
                    cache = create(context);
                    ServiceRegistry.sCache[this.mCacheIndex] = cache;
                }
            }
            return cache;
        }
    }

    static {
        sServices.put(CloudManager.class.getSimpleName(), new CachedComponentCreator<CloudManager>() {
            public CloudManager create(Application context) {
                return new CloudManager(context);
            }
        });
        sServices.put(HicloudAccountManager.class.getSimpleName(), new CachedComponentCreator<HicloudAccountManager>() {
            public HicloudAccountManager create(Application context) {
                return new HicloudAccountManager(context);
            }
        });
        sServices.put(DiscoverLocationNameManager.class.getSimpleName(), new CachedComponentCreator<DiscoverLocationNameManager>() {
            public DiscoverLocationNameManager create(Application context) {
                return new DiscoverLocationNameManager(context);
            }
        });
    }

    public static Object getComponent(Application context, String cmpName) {
        CachedComponentCreator<?> creator = (CachedComponentCreator) sServices.get(cmpName);
        if (creator == null) {
            return null;
        }
        return creator.getComponent(context);
    }
}
