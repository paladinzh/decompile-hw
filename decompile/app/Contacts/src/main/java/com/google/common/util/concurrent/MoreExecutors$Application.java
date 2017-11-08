package com.google.common.util.concurrent;

import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
class MoreExecutors$Application {
    MoreExecutors$Application() {
    }

    @VisibleForTesting
    void addShutdownHook(Thread hook) {
        Runtime.getRuntime().addShutdownHook(hook);
    }
}
