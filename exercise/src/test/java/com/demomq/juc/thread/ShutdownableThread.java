package com.demomq.juc.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ShutdownableThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ShutdownableThread.class);
    private final String name;
    private final boolean isInterruptible;
    private final CountDownLatch shutdownInitiated = new CountDownLatch(1);
    private final CountDownLatch shutdownComplete = new CountDownLatch(1);

    public ShutdownableThread(String name, boolean isInterruptible) {
        super(name);
        this.name = name;
        this.isInterruptible = isInterruptible;
        setDaemon(false);
    }

    public ShutdownableThread(String name) {
        this(name, true);
    }

    public void shutdown() {
        initiateShutdown();
        awaitShutdown();
    }

    public boolean isShutdownComplete() {
        return shutdownComplete.getCount() == 0;
    }

    public synchronized boolean initiateShutdown() {
        if (isRunning()) {
            LOG.info("[{}] Shutting down", name);
            shutdownInitiated.countDown();
            if (isInterruptible) {
                interrupt();
            }
            return true;
        } else {
            return false;
        }
    }

    public void awaitShutdown() {
        try {
            shutdownComplete.await();
            LOG.info("[{}] Shutdown completed", name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("[{}] Interrupted while waiting for shutdown", name, e);
        }
    }

    public void pause(long timeout, TimeUnit unit) {
        try {
            if (shutdownInitiated.await(timeout, unit)) {
                LOG.trace("[{}] shutdownInitiated latch count reached zero. Shutdown called.", name);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("[{}] Interrupted while pausing", name, e);
        }
    }

    public abstract void doWork();

    @Override
    public void run() {
        LOG.info("[{}] Starting", name);
        try {
            while (isRunning()) {
                doWork();
            }
        } catch (Error e) {
            shutdownInitiated.countDown();
            shutdownComplete.countDown();
            LOG.info("[{}] Stopped", name);
            System.exit(1);
        } catch (Throwable e) {
            if (isRunning()) {
                LOG.error("[{}] Error due to", name, e);
            }
        } finally {
            shutdownComplete.countDown();
        }
        LOG.info("[{}] Stopped", name);
    }

    protected boolean isRunning() {
        return shutdownInitiated.getCount() != 0;
    }
}