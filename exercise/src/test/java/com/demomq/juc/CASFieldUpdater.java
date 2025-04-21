package com.demomq.juc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class CASFieldUpdater {
    public static void main(String[] args) throws InterruptedException {
        run(new AtomicSolution());
        run(new ReflectSolution());

    }

    private static void run(Solution solution) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            threadPool.submit(() -> {
                for (int j = 0; j < 10000000; j++) {
                    solution.increment();
                }
                latch.countDown();
            });
        }
        latch.await();
        System.out.println(solution.getClass()+" result = " + solution.increment() + " cost: " + (System.currentTimeMillis() - start));
        threadPool.shutdown();
    }

    interface Solution {
        long increment();
    }

    static class AtomicSolution implements Solution {
        private final AtomicLong atomicLong = new AtomicLong(0);

        @Override
        public long increment() {
            return atomicLong.incrementAndGet();
        }
    }

    static class ReflectSolution implements Solution {
        private static final AtomicLongFieldUpdater<ReflectSolution> atomicReferenceFieldUpdater = AtomicLongFieldUpdater.newUpdater(ReflectSolution.class, "value");
        private volatile long value = 0;

        @Override
        public long increment() {
            return atomicReferenceFieldUpdater.getAndIncrement(this);
        }
    }
}
