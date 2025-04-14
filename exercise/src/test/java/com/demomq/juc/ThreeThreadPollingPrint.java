package com.demomq.juc;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 实现三个线程交替打印1、2、3；4、5、6…的序列
 */
public class ThreeThreadPollingPrint {

    static class SemaphoreSolution {
        private static final Semaphore s1 = new Semaphore(1);
        private static final Semaphore s2 = new Semaphore(0);
        private static final Semaphore s3 = new Semaphore(0);
        private static final AtomicInteger counter = new AtomicInteger(1);
        private static final int MAX = 10;

        public static void main(String[] args) {
            new Thread(() -> print("Thread-1", s1, s2)).start();
            new Thread(() -> print("Thread-2", s2, s3)).start();
            new Thread(() -> print("Thread-3", s3, s1)).start();
        }

        private static void print(String name, Semaphore current, Semaphore next) {
            while (counter.get() <= MAX) {
                try {
                    current.acquire();
                    System.out.println(name + ": " + counter.getAndIncrement());
                    next.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class LockConditionSolution {
        private static final Lock lock = new ReentrantLock();
        private static final Condition cond1 = lock.newCondition();
        private static final Condition cond2 = lock.newCondition();
        private static final Condition cond3 = lock.newCondition();
        private static int counter = 1;
        private static int turn = 1;

        public static void main(String[] args) {
            new Thread(() -> print(1, 2, cond1, cond2)).start();
            new Thread(() -> print(2, 3, cond2, cond3)).start();
            new Thread(() -> print(3, 1, cond3, cond1)).start();
        }

        private static void print(int currentTurn, int nextTurn, Condition currentCond, Condition nextCond) {
            while (counter <= 10) {
                lock.lock();
                try {
                    while (turn != currentTurn) {
                        currentCond.await();
                    }
                    System.out.println(Thread.currentThread().getName() + ": " + counter++);
                    turn = nextTurn;
                    nextCond.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    static class SynchronizedSolution {
        private static final Object lock = new Object();
        private static int counter = 1;
        private static int turn = 1;

        public static void main(String[] args) throws InterruptedException {
            new Thread(() -> print(1, 2)).start();
            new Thread(() -> print(2, 3)).start();
            new Thread(() -> print(3, 1)).start();
        }

        private static void print(int currentTurn, int nextTurn) {
            while (counter <= 10) {
                synchronized (lock) {
                    try {
                        while (turn != currentTurn) {
                            lock.wait();
                        }
                        System.out.println(Thread.currentThread().getName() + ": " + counter++);
                        turn = nextTurn;
                        lock.notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}