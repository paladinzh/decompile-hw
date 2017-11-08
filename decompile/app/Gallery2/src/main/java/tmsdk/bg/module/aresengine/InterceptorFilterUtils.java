package tmsdk.bg.module.aresengine;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.common.module.aresengine.FilterConfig;

/* compiled from: Unknown */
public final class InterceptorFilterUtils {
    public static final int INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST = 2;
    public static final int INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST = 1;
    public static final int INTERCEPTOR_MODE_STANDARD = 0;

    private InterceptorFilterUtils() {
    }

    private static void dH() {
        FilterConfig config;
        FilterConfig defalutFilterConfig;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            FilterConfig config2 = dataFilter2.getConfig();
            config = dataFilter2.defalutFilterConfig();
            if (config2 != null) {
                config.set(1, config2.get(1));
            }
            dataFilter2.setConfig(config);
        }
    }

    private static void dI() {
        FilterConfig config;
        FilterConfig defalutFilterConfig;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            defalutFilterConfig.set(2, 3);
            defalutFilterConfig.set(4, 1);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 2);
            defalutFilterConfig.set(64, 3);
            defalutFilterConfig.set(128, 3);
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            defalutFilterConfig.set(2, 3);
            defalutFilterConfig.set(4, 1);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 3);
            defalutFilterConfig.set(64, 0);
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            FilterConfig config2 = dataFilter2.getConfig();
            config = dataFilter2.defalutFilterConfig();
            if (config2 != null) {
                config.set(1, config2.get(1));
            }
            config.set(2, 3);
            config.set(4, 1);
            config.set(8, 3);
            config.set(16, 3);
            config.set(32, 0);
            config.set(64, 3);
            config.set(128, 3);
            config.set(256, 2);
            dataFilter2.setConfig(config);
        }
    }

    private static void dJ() {
        FilterConfig config;
        FilterConfig defalutFilterConfig;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            defalutFilterConfig.set(2, 0);
            defalutFilterConfig.set(4, 3);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 3);
            defalutFilterConfig.set(64, 3);
            defalutFilterConfig.set(128, 1);
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            defalutFilterConfig.set(64, 0);
            defalutFilterConfig.set(2, 0);
            defalutFilterConfig.set(4, 3);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 1);
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            FilterConfig config2 = dataFilter2.getConfig();
            config = dataFilter2.defalutFilterConfig();
            if (config2 != null) {
                config.set(1, config2.get(1));
            }
            config.set(2, 0);
            config.set(4, 3);
            config.set(8, 3);
            config.set(16, 3);
            config.set(32, 1);
            config.set(64, 3);
            config.set(128, 3);
            config.set(256, 2);
            dataFilter2.setConfig(config);
        }
    }

    public static void setInterceptorMode(int i) {
        switch (i) {
            case 0:
                dH();
                return;
            case 1:
                dI();
                return;
            case 2:
                dJ();
                return;
            default:
                return;
        }
    }
}
