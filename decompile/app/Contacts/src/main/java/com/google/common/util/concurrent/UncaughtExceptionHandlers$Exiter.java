package com.google.common.util.concurrent;

import com.google.common.annotations.VisibleForTesting;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@VisibleForTesting
final class UncaughtExceptionHandlers$Exiter implements UncaughtExceptionHandler {
    private static final Logger logger = Logger.getLogger(UncaughtExceptionHandlers$Exiter.class.getName());
    private final Runtime runtime;

    public void uncaughtException(Thread t, Throwable e) {
        try {
            logger.log(Level.SEVERE, String.format("Caught an exception in %s.  Shutting down.", new Object[]{t}), e);
        } catch (Throwable errorInLogging) {
            System.err.println(e.getMessage());
            System.err.println(errorInLogging.getMessage());
        } finally {
            this.runtime.exit(1);
        }
    }
}
