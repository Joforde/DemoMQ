package com.demomq.juc;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 两个线程交替执行输出100以内的随机数，直到1个线程的随机数跟13去余为0，则结束
 */
public class TwoThreadPollingPrint {

    static class LockConditionSolution {

        static int num = -1;
        static int turn = 1;
        static int count = 0;
        static boolean gameOver = false;
        static Lock lock = new ReentrantLock();

        public static void main(String[] args) {
            Condition condition = lock.newCondition();
            Condition condition2 = lock.newCondition();
            new Thread(() -> playBall("Tom", 1, 2, condition, condition2)).start();
            new Thread(() -> playBall("Jack", 2, 1, condition2, condition)).start();
        }

        public static void playBall(String name, int curTurn, int nextTurn, Condition current, Condition next) {
            while (!gameOver) {
                try {
                    lock.lock();
                    while (turn != curTurn) {
                        current.await();
                    }
                    if (gameOver) {
                        return;
                    }
                    if (num % 13 == 0) {
                        gameOver = true;
                        System.out.println(name + " win the game");
                    } else {
                        num = new Random().nextInt(100);
                        System.out.println(name + " take the ball " + count++ + " by " + num);
                    }
                    turn = nextTurn;
                    next.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    static class SemaphoreSolution {
        static int num = -1;
        static int count = 0;
        static boolean gameOver = false;

        public static void main(String[] args) {
            Semaphore semaphore1 = new Semaphore(1);
            Semaphore semaphore2 = new Semaphore(0);

            new Thread(() -> playBall("Tom", semaphore1, semaphore2)).start();
            new Thread(() -> playBall("Jack", semaphore2, semaphore1)).start();
        }

        public static void playBall(String name, Semaphore current, Semaphore next) {
            while (!gameOver) {
                try {
                    current.acquire();
                    if (gameOver) {
                        return;
                    }
                    if (num % 13 == 0) {
                        gameOver = true;
                        System.out.println(name + " win the game");
                    } else {
                        num = new Random().nextInt(100);
                        System.out.println(name + " take the ball " + count++ + " by " + num);
                    }
                    next.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class SynchronizedSolution {
        public static void main(String[] args) throws InterruptedException {
            Object lock = new Object();
            AtomicInteger count = new AtomicInteger(0);
            AtomicBoolean gameOver = new AtomicBoolean(false);
            new BallPlayer("Tom", lock, count, gameOver).start();
            new BallPlayer("Jack", lock, count, gameOver).start();
        }

        static class BallPlayer extends Thread {
            private final String name;
            private final Object lock;
            private final AtomicInteger count;
            private final AtomicBoolean gameOver;

            public BallPlayer(String name, Object lock, AtomicInteger count, AtomicBoolean gameOver) {
                this.name = name;
                this.lock = lock;
                this.count = count;
                this.gameOver = gameOver;
            }

            @Override
            public void run() {
                while (!gameOver.get()) {
                    synchronized (lock) {
                        lock.notifyAll();
                        int num = new Random().nextInt() * 100;
                        System.out.println(name + ":" + count.getAndIncrement());
                        if (num % 13 == 0) {
                            gameOver.set(true);
                            System.out.println(name + " lose the game");
                            return;
                        }
                        System.out.println(name + " take ball " + count.getAndIncrement());
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

    }
}

