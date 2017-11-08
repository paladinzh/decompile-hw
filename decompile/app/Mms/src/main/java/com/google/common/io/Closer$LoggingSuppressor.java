package com.google.common.io;

import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
final class Closer$LoggingSuppressor implements Closer$Suppressor {
    static final Closer$LoggingSuppressor INSTANCE = new Closer$LoggingSuppressor();

    Closer$LoggingSuppressor() {
    }
}
