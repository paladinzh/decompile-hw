package com.huawei.systemmanager.comm.misc;

import android.content.Context;
import com.common.imageloader.cache.disc.naming.Md5FileNameGenerator;
import com.common.imageloader.core.ImageLoader;
import com.common.imageloader.core.ImageLoaderConfiguration.Builder;
import com.common.imageloader.core.assist.QueueProcessingType;
import com.huawei.systemmanager.optimize.MemoryManager;
import com.huawei.systemmanager.spacecleanner.autoclean.AutoCleanService;
import com.huawei.systemmanager.util.HwLog;

public class ImageLoaderUtils {
    private static final String TAG = "ImageLoaderUtils";

    public static void initImageLoader() {
        if (!ImageLoader.getInstance().isInited()) {
            Context context = GlobalContext.getContext();
            int memoryCacheSize = AutoCleanService.AUTO_CACHE_CLEAN_MAX_SIZE;
            long totalMemory = MemoryManager.getMemoryInfo(context).getTotal();
            if (totalMemory > 0) {
                memoryCacheSize = (int) (((double) totalMemory) * 0.007d);
            }
            HwLog.i(TAG, "initImageLoader totalMemory:" + totalMemory + "    memoryCacheSize" + memoryCacheSize);
            Builder config = new Builder(context);
            config.threadPriority(3);
            config.denyCacheImageMultipleSizesInMemory();
            config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
            config.diskCacheSize(31457280);
            config.memoryCacheSize(memoryCacheSize);
            config.tasksProcessingOrder(QueueProcessingType.FIFO);
            config.diskCacheExtraOptions(255, 255, null);
            config.writeDebugLogs();
            ImageLoader.getInstance().init(config.build());
        }
    }
}
